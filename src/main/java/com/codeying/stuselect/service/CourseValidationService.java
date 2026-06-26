package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.mapper.*;
import com.codeying.stuselect.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 选课智能冲突检测服务
 * 实现六重校验：时间冲突、容量限制、重复选课、年级权限、先修课程、互斥课程、类型限选、学分上限
 */
@Service
public class CourseValidationService {

  private final CoursePrerequisiteMapper prerequisiteMapper;
  private final CourseMutexMapper mutexMapper;
  private final CourseTypeLimitMapper typeLimitMapper;
  private final SemesterCreditLimitMapper creditLimitMapper;
  private final SelectionMapper selectionMapper;
  private final StudentMapper studentMapper;
  private final CourseMapper courseMapper;

  public CourseValidationService(
      CoursePrerequisiteMapper prerequisiteMapper,
      CourseMutexMapper mutexMapper,
      CourseTypeLimitMapper typeLimitMapper,
      SemesterCreditLimitMapper creditLimitMapper,
      SelectionMapper selectionMapper,
      StudentMapper studentMapper,
      CourseMapper courseMapper) {
    this.prerequisiteMapper = prerequisiteMapper;
    this.mutexMapper = mutexMapper;
    this.typeLimitMapper = typeLimitMapper;
    this.creditLimitMapper = creditLimitMapper;
    this.selectionMapper = selectionMapper;
    this.studentMapper = studentMapper;
    this.courseMapper = courseMapper;
  }

  /**
   * 校验4：年级权限校验
   * 低年级学生不能选高年级课程
   */
  public void validateGradeLimit(Course course, String studentId) {
    if (course.getGradeLimit() == null) {
      return; // 没有年级限制
    }

    Student student = studentMapper.selectById(studentId);
    if (student == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "学生不存在");
    }
    if (student.getGrade() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "学生年级信息未维护，无法选修有年级限制的课程");
    }

    if (student.getGrade() < course.getGradeLimit()) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "该课程仅限大" + course.getGradeLimit() + "及以上年级学生选修，你当前是大" + student.getGrade());
    }
  }

  /**
   * 校验5：先修课程校验
   * 必须先修完前置课程且成绩达标
   */
  public void validatePrerequisites(String courseId, String studentId) {
    // 使用Set追踪访问过的课程，防止循环依赖导致无限递归
    validatePrerequisitesRecursive(courseId, studentId, new HashSet<>());
  }

  /**
   * 递归校验先修课程（带循环依赖检测）
   */
  private void validatePrerequisitesRecursive(String courseId, String studentId, Set<String> visited) {
    // 检测循环依赖
    if (visited.contains(courseId)) {
      throw new AppException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "检测到循环先修课程依赖，请联系管理员修正课程配置");
    }
    visited.add(courseId);

    try {
    List<CoursePrerequisite> prerequisites = prerequisiteMapper.selectWithNamesByCourseId(courseId);
    if (prerequisites.isEmpty()) {
      return; // 没有先修要求
    }

    for (CoursePrerequisite prereq : prerequisites) {
      // 查询学生是否已修完该先修课程
      SelectionRecord completed =
          selectionMapper.selectOne(
              new LambdaQueryWrapper<SelectionRecord>()
                  .eq(SelectionRecord::getStudentId, studentId)
                  .eq(SelectionRecord::getCourseId, prereq.getPrerequisiteCourseId())
                  .last("LIMIT 1"));

      if (completed == null) {
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "需要先修完《" + prereq.getPrerequisiteCourseName() + "》才能选修该课程");
      }

      if (!Boolean.TRUE.equals(completed.getGraded())) {
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "先修课程《" + prereq.getPrerequisiteCourseName() + "》成绩尚未录入，无法选课");
      }

      if (completed.getScore() == null || completed.getScore() < prereq.getMinScore()) {
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "先修课程《"
                + prereq.getPrerequisiteCourseName()
                + "》成绩需达到"
                + prereq.getMinScore()
                + "分（当前："
                + (completed.getScore() == null ? "未录入" : completed.getScore())
                + "分）");
      }

      // 递归校验先修课程的先修课程（检测整个依赖链）
      validatePrerequisitesRecursive(prereq.getPrerequisiteCourseId(), studentId, visited);
    }
    } finally {
      visited.remove(courseId);
    }
  }

  /**
   * 校验6：互斥课程校验
   * 已选其中一门课程后，不能再选另一门
   */
  public void validateMutexCourses(String courseId, String studentId, String excludeSelectionId) {
    List<CourseMutex> mutexList = mutexMapper.selectWithNamesByCourseId(courseId);
    if (mutexList.isEmpty()) {
      return; // 没有互斥课程
    }

    for (CourseMutex mutex : mutexList) {
      // 确定互斥的另一门课程ID
      String mutexCourseId = mutex.getCourseIdA().equals(courseId)
          ? mutex.getCourseIdB()
          : mutex.getCourseIdA();
      String mutexCourseName = mutex.getCourseIdA().equals(courseId)
          ? mutex.getCourseNameB()
          : mutex.getCourseNameA();

      // 检查学生是否已选修互斥课程（排除当前正在更新的选课记录）
      LambdaQueryWrapper<SelectionRecord> wrapper = new LambdaQueryWrapper<SelectionRecord>()
          .eq(SelectionRecord::getStudentId, studentId)
          .eq(SelectionRecord::getCourseId, mutexCourseId);

      if (StringUtils.hasText(excludeSelectionId)) {
        wrapper.ne(SelectionRecord::getId, excludeSelectionId);
      }

      long count = selectionMapper.selectCount(wrapper);

      if (count > 0) {
        String reason = StringUtils.hasText(mutex.getReason())
            ? "（" + mutex.getReason() + "）"
            : "";
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "你已选修《" + mutexCourseName + "》，与当前课程互斥，不能同时选修" + reason);
      }
    }
  }

  /**
   * 校验7：课程类型限选校验
   * 某类型课程最多只能选N门
   */
  public void validateCourseTypeLimit(Course course, String studentId, String excludeSelectionId) {
    if (!StringUtils.hasText(course.getCourseType())) {
      return; // 没有课程类型，跳过校验
    }

    // 查询该类型的限选规则
    CourseTypeLimit limit =
        typeLimitMapper.selectOne(
            new LambdaQueryWrapper<CourseTypeLimit>()
                .eq(CourseTypeLimit::getCourseType, course.getCourseType()));

    if (limit == null) {
      return; // 该类型没有限选规则
    }

    // 统计学生已选该类型课程数量
    // 修复SQL注入漏洞：先查询该类型的所有课程ID，再用IN查询
    List<String> courseIdsOfType = courseMapper.selectList(
        new LambdaQueryWrapper<Course>()
            .eq(Course::getCourseType, course.getCourseType())
    ).stream().map(Course::getId).collect(Collectors.toList());

    if (courseIdsOfType.isEmpty()) {
      return; // 该类型没有课程
    }

    LambdaQueryWrapper<SelectionRecord> wrapper =
        new LambdaQueryWrapper<SelectionRecord>()
            .eq(SelectionRecord::getStudentId, studentId)
            .in(SelectionRecord::getCourseId, courseIdsOfType);

    // 排除当前正在更新的选课记录（用于更新场景）
    if (StringUtils.hasText(excludeSelectionId)) {
      wrapper.ne(SelectionRecord::getId, excludeSelectionId);
    }

    long currentCount = selectionMapper.selectCount(wrapper);

    if (currentCount >= limit.getMaxCourses()) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "《"
              + course.getCourseType()
              + "》类课程最多选"
              + limit.getMaxCourses()
              + "门，你已选"
              + currentCount
              + "门");
    }
  }

  /**
   * 校验8：学分上限校验
   * 根据学生GPA动态限制本学期可选学分
   */
  public void validateCreditLimit(
      Course course, String studentId, String excludeSelectionId, double currentGPA) {
    // 查询学生当前GPA对应的学分上限
    // 修复GPA查询逻辑：应该是 minGpa <= currentGPA < maxGpa
    SemesterCreditLimit limit =
        creditLimitMapper.selectOne(
            new LambdaQueryWrapper<SemesterCreditLimit>()
                .le(SemesterCreditLimit::getMinGpa, currentGPA)  // minGpa <= currentGPA
                .gt(SemesterCreditLimit::getMaxGpa, currentGPA)  // maxGpa > currentGPA
                .orderByDesc(SemesterCreditLimit::getMinGpa)
                .last("LIMIT 1"));

    if (limit == null) {
      return; // 没有配置学分上限规则
    }

    // 统计学生本学期已选学分
    // 修复N+1查询问题：一次性查询所有选课记录及关联的课程信息
    List<SelectionRecord> selections =
        selectionMapper.selectJoinedList(null, studentId, null);

    // 排除当前正在更新的选课记录
    if (StringUtils.hasText(excludeSelectionId)) {
      selections =
          selections.stream()
              .filter(s -> !s.getId().equals(excludeSelectionId))
              .collect(Collectors.toList());
    }

    // 计算已选学分总和（增加空指针检查）
    double currentCredits = 0.0;
    for (SelectionRecord selection : selections) {
      if (selection != null && selection.getCourseCredit() != null) {
        currentCredits += selection.getCourseCredit();
      }
    }

    // 检查是否超过学分上限（增加空指针检查）
    double courseCredits = (course.getScore() != null) ? course.getScore() : 0.0;
    double newTotalCredits = currentCredits + courseCredits;
    if (newTotalCredits > limit.getMaxCredits()) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "本学期最多可选"
              + limit.getMaxCredits()
              + "学分（GPA "
              + String.format("%.2f", currentGPA)
              + "），当前已选"
              + String.format("%.1f", currentCredits)
              + "学分，加上该课程"
              + String.format("%.1f", courseCredits)
              + "学分将超出上限");
    }
  }

  /**
   * 执行所有新增的智能校验（不包括已有的时间冲突、容量、重复选课校验）
   * @param course 要选的课程
   * @param studentId 学生ID
   * @param excludeSelectionId 排除的选课记录ID（用于更新场景）
   * @param currentGPA 学生当前GPA
   */
  public void validateAll(
      Course course, String studentId, String excludeSelectionId, double currentGPA) {
    // 校验4：年级权限
    validateGradeLimit(course, studentId);

    // 校验5：先修课程
    validatePrerequisites(course.getId(), studentId);

    // 校验6：互斥课程
    validateMutexCourses(course.getId(), studentId, excludeSelectionId);

    // 校验7：课程类型限选
    validateCourseTypeLimit(course, studentId, excludeSelectionId);

    // 校验8：学分上限
    validateCreditLimit(course, studentId, excludeSelectionId, currentGPA);
  }
}
