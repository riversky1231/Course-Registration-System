package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.CourseMapper;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
public class CourseService {

  private final CourseMapper courseMapper;
  private final SelectionMapper selectionMapper;
  private final SessionService sessionService;
  private final AdminAuditLogService adminAuditLogService;

  public CourseService(
      CourseMapper courseMapper,
      SelectionMapper selectionMapper,
      SessionService sessionService,
      AdminAuditLogService adminAuditLogService) {
    this.courseMapper = courseMapper;
    this.selectionMapper = selectionMapper;
    this.sessionService = sessionService;
    this.adminAuditLogService = adminAuditLogService;
  }

  public PageResult<Course> list(
      String keyword,
      String dept,
      String teacherId,
      Double minScore,
      Double maxScore,
      Boolean onlyAvailable,
      Integer page,
      Integer pageSize,
      HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String teacherScopeId = current.getRole() == Role.TEACHER ? current.getId() : null;
    List<Course> courses = courseMapper.selectListWithTeacher(keyword, teacherScopeId);
    List<Course> filtered =
        courses.stream()
            .filter(course -> !StringUtils.hasText(dept) || containsIgnoreCase(course.getDept(), dept))
            .filter(course -> !StringUtils.hasText(teacherId) || Objects.equals(course.getTid(), teacherId))
            .filter(course -> minScore == null || (course.getScore() != null && course.getScore() >= minScore))
            .filter(course -> maxScore == null || (course.getScore() != null && course.getScore() <= maxScore))
            .filter(course -> !Boolean.TRUE.equals(onlyAvailable) || isAvailable(course))
            .toList();
    return PageResult.of(filtered, PageQuery.of(page, pageSize));
  }

  public List<Course> listAllVisible(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String teacherScopeId = current.getRole() == Role.TEACHER ? current.getId() : null;
    return courseMapper.selectListWithTeacher(null, teacherScopeId);
  }

  public Course create(Course course, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    if (!StringUtils.hasText(course.getName()) || !StringUtils.hasText(course.getNumb())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程名称和编号不能为空");
    }
    Course existed = courseMapper.selectByNumbWithTeacher(course.getNumb());
    if (existed != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程编号已存在");
    }
    if (current.getRole() == Role.TEACHER) {
      course.setTid(current.getId());
    } else {
      course.setTid(normalizeTeacherId(course.getTid()));
    }
    course.setDept(normalizeDept(course.getDept()));
    course.setMaxStudents(normalizeMaxStudents(course.getMaxStudents()));
    course.setTimeSlot(normalizeTimeSlot(course.getTimeSlot()));
    course.setId(IdGenerator.newId());
    courseMapper.insert(course);
    if (current.getRole() == Role.ADMIN) {
      adminAuditLogService.record(
          current, "新增", "课程", course.getId(), displayName(course), detail(course));
    }
    return courseMapper.selectByIdWithTeacher(course.getId());
  }

  @Transactional
  public Course update(String id, Course input, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    Course course = require(id);
    if (current.getRole() == Role.TEACHER && !current.getId().equals(course.getTid())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能修改自己的课程");
    }
    Course duplicated =
        StringUtils.hasText(input.getNumb()) ? courseMapper.selectByNumbWithTeacher(input.getNumb()) : null;
    if (duplicated != null && !duplicated.getId().equals(id)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程编号已存在");
    }
    String previousTeacherId = course.getTid();
    course.setName(defaultValue(input.getName(), course.getName()));
    course.setScore(input.getScore());
    course.setNumb(defaultValue(input.getNumb(), course.getNumb()));
    course.setTid(
        current.getRole() == Role.TEACHER
            ? current.getId()
            : (input.getTid() != null ? normalizeTeacherId(input.getTid()) : course.getTid()));
    course.setJianjie(input.getJianjie());
    course.setDept(input.getDept() != null ? normalizeDept(input.getDept()) : course.getDept());
    course.setMaxStudents(
        input.getMaxStudents() != null
            ? normalizeMaxStudents(input.getMaxStudents())
            : course.getMaxStudents());
    course.setTimeSlot(input.getTimeSlot() != null ? normalizeTimeSlot(input.getTimeSlot()) : course.getTimeSlot());
    courseMapper.updateById(course);
    if (!Objects.equals(previousTeacherId, course.getTid())) {
      selectionMapper.updateTeacherByCourseId(course.getId(), course.getTid());
    }
    if (current.getRole() == Role.ADMIN) {
      adminAuditLogService.record(
          current, "编辑", "课程", course.getId(), displayName(course), detail(course));
    }
    return courseMapper.selectByIdWithTeacher(id);
  }

  @Transactional
  public void delete(String id, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    Course course = require(id);
    if (current.getRole() == Role.TEACHER && !current.getId().equals(course.getTid())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能删除自己的课程");
    }
    if (selectionMapper.countByCourse(id, null) > 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该课程已有学生选课，不能直接删除");
    }
    courseMapper.deleteById(id);
    if (current.getRole() == Role.ADMIN) {
      adminAuditLogService.record(
          current, "删除", "课程", course.getId(), displayName(course), detail(course));
    }
  }

  public long count(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String teacherId = current.getRole() == Role.TEACHER ? current.getId() : null;
    LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(teacherId)) {
      wrapper.eq(Course::getTid, teacherId);
    }
    return courseMapper.selectCount(wrapper);
  }

  public Course require(String id) {
    Course course = courseMapper.selectByIdWithTeacher(id);
    if (course == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "课程不存在");
    }
    return course;
  }

  public Course requireForUpdate(String id) {
    Course course = courseMapper.selectByIdForUpdate(id);
    if (course == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "课程不存在");
    }
    return course;
  }

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }

  private Integer normalizeMaxStudents(Integer value) {
    return value == null ? 0 : value;
  }

  private String normalizeDept(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String normalizeTimeSlot(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String normalizeTeacherId(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private boolean containsIgnoreCase(String source, String keyword) {
    return StringUtils.hasText(source)
        && source.toLowerCase().contains(keyword.trim().toLowerCase());
  }

  private boolean isAvailable(Course course) {
    if (course.getMaxStudents() == null || course.getMaxStudents() <= 0) {
      return true;
    }
    return selectionMapper.countByCourse(course.getId(), null) < course.getMaxStudents();
  }

  private String displayName(Course course) {
    return StringUtils.hasText(course.getName()) ? course.getName() : course.getNumb();
  }

  private String detail(Course course) {
    return "课程编号："
        + defaultValue(course.getNumb(), "-")
        + "，学分："
        + (course.getScore() == null ? "-" : course.getScore().toString())
        + "，开课学院："
        + defaultValue(course.getDept(), "-")
        + "，时间："
        + defaultValue(course.getTimeSlot(), "未排课");
  }
}
