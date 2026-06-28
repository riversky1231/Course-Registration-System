package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.CourseEvaluationSummary;
import com.codeying.stuselect.mapper.CourseEvaluationMapper;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.CourseEvaluation;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 课程评价（评教）业务服务。
 *
 * <p>评价依赖选课与成绩模块：学生只能对“已选修且已结课（成绩已录入）”的课程评价，
 * 且每门课程仅能评价一次。教师可查看自己课程的评价（匿名评价隐藏学生身份），
 * 管理员可查看全部评价。
 */
@Service
public class CourseEvaluationService {

  /** 最低评分。 */
  private static final int MIN_RATING = 1;

  /** 最高评分。 */
  private static final int MAX_RATING = 5;

  /** 两位小数四舍五入比例。 */
  private static final double ROUND_SCALE = 100D;

  private final CourseEvaluationMapper evaluationMapper;
  private final SelectionMapper selectionMapper;
  private final CourseService courseService;
  private final SessionService sessionService;

  public CourseEvaluationService(
      final CourseEvaluationMapper evaluationMapperBean,
      final SelectionMapper selectionMapperBean,
      final CourseService courseServiceBean,
      final SessionService sessionServiceBean) {
    this.evaluationMapper = evaluationMapperBean;
    this.selectionMapper = selectionMapperBean;
    this.courseService = courseServiceBean;
    this.sessionService = sessionServiceBean;
  }

  /**
   * 分页查询当前用户可见的课程评价。
   *
   * @param keyword 可选关键字（课程名/评价内容/教师名）
   * @param courseId 可选课程过滤
   * @param page 页码
   * @param pageSize 页大小
   * @param session 当前会话
   * @return 分页评价结果
   */
  public PageResult<CourseEvaluation> list(
      final String keyword,
      final String courseId,
      final Integer page,
      final Integer pageSize,
      final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    final String studentScope =
        current.getRole() == Role.STUDENT ? current.getId() : null;
    final String teacherScope =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    final List<CourseEvaluation> list =
        evaluationMapper.selectJoinedList(keyword, studentScope, teacherScope, courseId);
    list.forEach(item -> maskAnonymous(item, current));
    return PageResult.of(list, PageQuery.of(page, pageSize));
  }

  /**
   * 创建课程评价（学生本人或管理员代录）。
   *
   * @param input 评价请求
   * @param session 当前会话
   * @return 创建后的评价（含课程/教师信息）
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public CourseEvaluation create(final CourseEvaluation input, final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.STUDENT);
    final String studentId =
        current.getRole() == Role.STUDENT ? current.getId() : input.getStudentId();
    if (!StringUtils.hasText(input.getCourseId()) || !StringUtils.hasText(studentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程和学生不能为空");
    }
    validateRating(input.getRating());

    final Course course = courseService.require(input.getCourseId());

    // 评价前置依赖：必须选修过该课程，且课程已结课（成绩已录入）。
    final SelectionRecord selection =
        selectionMapper.selectByCourseAndStudent(input.getCourseId(), studentId);
    if (selection == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "只能评价自己选修过的课程");
    }
    if (!Boolean.TRUE.equals(selection.getGraded())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该课程成绩尚未录入，结课后才能评价");
    }

    // 每门课程仅能评价一次。
    final CourseEvaluation duplicated =
        evaluationMapper.selectByCourseAndStudent(input.getCourseId(), studentId);
    if (duplicated != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "你已评价过该课程，不能重复评价");
    }

    final CourseEvaluation insert = new CourseEvaluation();
    insert.setId(IdGenerator.newId());
    insert.setCourseId(input.getCourseId());
    insert.setStudentId(studentId);
    insert.setTeacherId(course.getTid());
    insert.setRating(input.getRating());
    insert.setComment(StringUtils.hasText(input.getComment()) ? input.getComment().trim() : null);
    insert.setAnonymous(Boolean.TRUE.equals(input.getAnonymous()));
    insert.setCreateTime(LocalDateTime.now());
    evaluationMapper.insert(insert);
    return evaluationMapper.selectJoinedById(insert.getId());
  }

  /**
   * 删除课程评价（学生本人或管理员）。
   *
   * @param id 评价ID
   * @param session 当前会话
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public void delete(final String id, final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.STUDENT);
    final CourseEvaluation existed = require(id);
    if (current.getRole() == Role.STUDENT
        && !current.getId().equals(existed.getStudentId())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能删除自己的评价");
    }
    evaluationMapper.deleteById(id);
  }

  /**
   * 统计某门课程的评价聚合信息（教师仅限本人课程，管理员不限）。
   *
   * @param courseId 课程ID
   * @param session 当前会话
   * @return 课程评价聚合统计
   */
  public CourseEvaluationSummary courseSummary(final String courseId, final HttpSession session) {
    final UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    final Course course = courseService.require(courseId);
    if (current.getRole() == Role.TEACHER && !current.getId().equals(course.getTid())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能查看自己课程的评价统计");
    }
    final List<CourseEvaluation> evaluations =
        evaluationMapper.selectJoinedList(null, null, null, courseId);
    final Map<Integer, Long> distribution = new LinkedHashMap<>();
    for (int star = MIN_RATING; star <= MAX_RATING; star++) {
      distribution.put(star, 0L);
    }
    double sum = 0D;
    long count = 0L;
    for (CourseEvaluation evaluation : evaluations) {
      final Integer rating = evaluation.getRating();
      if (rating == null) {
        continue;
      }
      sum += rating;
      count++;
      distribution.computeIfPresent(rating, (key, value) -> value + 1);
    }
    final double average = count == 0 ? 0D : round(sum / count);
    return new CourseEvaluationSummary(
        courseId, course.getName(), count, average, distribution);
  }

  /**
   * 统计当前用户可见的评价数量（供仪表盘使用）。
   *
   * @param session 当前会话
   * @return 可见评价数量
   */
  public long count(final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    final LambdaQueryWrapper<CourseEvaluation> wrapper = new LambdaQueryWrapper<>();
    if (current.getRole() == Role.STUDENT) {
      wrapper.eq(CourseEvaluation::getStudentId, current.getId());
    } else if (current.getRole() == Role.TEACHER) {
      wrapper.eq(CourseEvaluation::getTeacherId, current.getId());
    }
    return evaluationMapper.selectCount(wrapper);
  }

  /**
   * 加载评价或抛出未找到异常。
   *
   * @param id 评价ID
   * @return 评价记录
   */
  public CourseEvaluation require(final String id) {
    final CourseEvaluation evaluation = evaluationMapper.selectJoinedById(id);
    if (evaluation == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "评价记录不存在");
    }
    return evaluation;
  }

  private void validateRating(final Integer rating) {
    if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
      throw new AppException(HttpStatus.BAD_REQUEST, "评分必须在1~5星之间");
    }
  }

  /**
   * 匿名评价对“非本人、非管理员”的查看者隐藏学生身份。
   */
  private void maskAnonymous(final CourseEvaluation evaluation, final UserSession current) {
    if (!Boolean.TRUE.equals(evaluation.getAnonymous())) {
      return;
    }
    final boolean owner =
        current.getRole() == Role.STUDENT
            && Objects.equals(current.getId(), evaluation.getStudentId());
    if (current.getRole() == Role.ADMIN || owner) {
      return;
    }
    evaluation.setStudentId(null);
    evaluation.setStudentName("匿名学生");
  }

  private double round(final double value) {
    return Math.round(value * ROUND_SCALE) / ROUND_SCALE;
  }
}
