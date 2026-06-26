package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.CourseListCriteria;
import com.codeying.stuselect.mapper.CourseMapper;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Manages course query, creation, update, deletion, and teacher-scoped
 * course visibility.
 */
@Service
public class CourseService {

  /** Course persistence mapper. */
  private final CourseMapper courseMapper;

  /** Selection persistence mapper. */
  private final SelectionMapper selectionMapper;

  /** Session validation service. */
  private final SessionService sessionService;

  /**
   * Creates a course service with mapper dependencies.
   *
   * @param courseMapperBean course mapper
   * @param selectionMapperBean selection mapper
   * @param sessionServiceBean session service
   */
  public CourseService(
      final CourseMapper courseMapperBean,
      final SelectionMapper selectionMapperBean,
      final SessionService sessionServiceBean) {
    this.courseMapper = courseMapperBean;
    this.selectionMapper = selectionMapperBean;
    this.sessionService = sessionServiceBean;
  }

  /**
   * Returns paged courses filtered by the submitted criteria.
   *
   * @param criteria course list filters
   * @param session current HTTP session
   * @return paged course result
   */
  public PageResult<Course> list(
      final CourseListCriteria criteria,
      final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    final String teacherScopeId =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    final List<Course> courses =
        courseMapper.selectListWithTeacher(criteria.keyword(), teacherScopeId);
    final List<Course> filtered =
        courses.stream()
            .filter(
                course ->
                    !StringUtils.hasText(criteria.dept())
                        || containsIgnoreCase(
                            course.getDept(),
                            criteria.dept()))
            .filter(
                course ->
                    !StringUtils.hasText(criteria.teacherId())
                        || Objects.equals(
                            course.getTid(),
                            criteria.teacherId()))
            .filter(
                course ->
                    criteria.minScore() == null
                        || (course.getScore() != null
                            && course.getScore() >= criteria.minScore()))
            .filter(
                course ->
                    criteria.maxScore() == null
                        || (course.getScore() != null
                            && course.getScore() <= criteria.maxScore()))
            .filter(
                course ->
                    !Boolean.TRUE.equals(criteria.onlyAvailable())
                        || isAvailable(course))
            .toList();
    return PageResult.of(
        filtered,
        PageQuery.of(criteria.page(), criteria.pageSize()));
  }

  /**
   * Lists all courses visible to the current user.
   *
   * @param session current HTTP session
   * @return visible courses
   */
  public List<Course> listAllVisible(final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    final String teacherScopeId =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    return courseMapper.selectListWithTeacher(null, teacherScopeId);
  }

  /**
   * Creates a new course after validating course number and teacher scope.
   *
   * @param course submitted course data
   * @param session current HTTP session
   * @return created course with teacher information
   */
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public Course create(final Course course, final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    if (!StringUtils.hasText(course.getName())
        || !StringUtils.hasText(course.getNumb())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程名称和编号不能为空");
    }
    final Course existed =
        courseMapper.selectByNumbWithTeacher(course.getNumb());
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
    return courseMapper.selectByIdWithTeacher(course.getId());
  }

  /**
   * Updates a course and synchronizes teacher changes.
   *
   * @param id course id
   * @param input submitted course data
   * @param session current HTTP session
   * @return updated course with teacher information
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public Course update(
      final String id,
      final Course input,
      final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    final Course course = require(id);
    if (current.getRole() == Role.TEACHER
        && !current.getId().equals(course.getTid())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能修改自己的课程");
    }
    final Course duplicated =
        StringUtils.hasText(input.getNumb())
            ? courseMapper.selectByNumbWithTeacher(input.getNumb())
            : null;
    if (duplicated != null && !duplicated.getId().equals(id)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程编号已存在");
    }
    final String previousTeacherId = course.getTid();
    course.setName(defaultValue(input.getName(), course.getName()));
    course.setScore(input.getScore());
    course.setNumb(defaultValue(input.getNumb(), course.getNumb()));
    course.setTid(
        current.getRole() == Role.TEACHER
            ? current.getId()
            : normalizedTeacherId(input, course));
    course.setJianjie(input.getJianjie());
    course.setDept(
        input.getDept() != null
            ? normalizeDept(input.getDept())
            : course.getDept());
    course.setMaxStudents(
        input.getMaxStudents() != null
            ? normalizeMaxStudents(input.getMaxStudents())
            : course.getMaxStudents());
    course.setTimeSlot(
        input.getTimeSlot() != null
            ? normalizeTimeSlot(input.getTimeSlot())
            : course.getTimeSlot());
    courseMapper.updateById(course);
    if (!Objects.equals(previousTeacherId, course.getTid())) {
      selectionMapper.updateTeacherByCourseId(course.getId(), course.getTid());
    }
    return courseMapper.selectByIdWithTeacher(id);
  }

  /**
   * Deletes a course when no selection record is bound to it.
   *
   * @param id course id
   * @param session current HTTP session
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public void delete(final String id, final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    final Course course = require(id);
    if (current.getRole() == Role.TEACHER
        && !current.getId().equals(course.getTid())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能删除自己的课程");
    }
    if (selectionMapper.countByCourse(id, null) > 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该课程已有学生选课，不能直接删除");
    }
    courseMapper.deleteById(id);
  }

  /**
   * Counts courses visible to the current user.
   *
   * @param session current HTTP session
   * @return course count
   */
  public long count(final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    final String teacherId =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    final LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(teacherId)) {
      wrapper.eq(Course::getTid, teacherId);
    }
    return courseMapper.selectCount(wrapper);
  }

  /**
   * Loads a course with teacher information or throws when it does not exist.
   *
   * @param id course id
   * @return existing course
   */
  public Course require(final String id) {
    final Course course = courseMapper.selectByIdWithTeacher(id);
    if (course == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "课程不存在");
    }
    return course;
  }

  /**
   * Locks and loads a course for update.
   *
   * @param id course id
   * @return locked course
   */
  public Course requireForUpdate(final String id) {
    final Course course = courseMapper.selectByIdForUpdate(id);
    if (course == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "课程不存在");
    }
    return course;
  }

  private String defaultValue(final String input, final String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }

  private Integer normalizeMaxStudents(final Integer value) {
    return value == null ? 0 : value;
  }

  private String normalizeDept(final String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String normalizeTimeSlot(final String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String normalizeTeacherId(final String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private String normalizedTeacherId(final Course input, final Course course) {
    return input.getTid() != null
        ? normalizeTeacherId(input.getTid())
        : course.getTid();
  }

  private boolean containsIgnoreCase(
      final String source,
      final String keyword) {
    return StringUtils.hasText(source)
        && source.toLowerCase().contains(keyword.trim().toLowerCase());
  }

  private boolean isAvailable(final Course course) {
    if (course.getMaxStudents() == null || course.getMaxStudents() <= 0) {
      return true;
    }
    return selectionMapper.countByCourse(course.getId(), null)
        < course.getMaxStudents();
  }

}
