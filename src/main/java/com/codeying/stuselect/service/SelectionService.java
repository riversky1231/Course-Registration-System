package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.StudentGradeReport;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Handles selection creation, updates, deletion, and student grade reports.
 *
 * @author 陈佳兴
 */
@Service
public class SelectionService {

  /** Minimum score for a passed course. */
  private static final double PASSING_SCORE = 60D;

  /** Base score used by the GPA conversion formula. */
  private static final double GPA_BASE_SCORE = 50D;

  /** Score interval for one GPA point. */
  private static final double GPA_SCORE_STEP = 10D;

  /** Scale used by the two-decimal rounding helper. */
  private static final double ROUND_SCALE = 100D;

  /** Selection persistence mapper. */
  private final SelectionMapper selectionMapper;

  /** Course business service. */
  private final CourseService courseService;

  /** Student business service. */
  private final StudentService studentService;

  /** Session validation service. */
  private final SessionService sessionService;

  /** Selection window validation service. */
  private final SelectionWindowService selectionWindowService;

  /** Additional course rule validation service. */
  private final CourseValidationService courseValidationService;

  /**
   * Creates a selection service with its required collaborators.
   *
   * @param selectionMapperBean selection mapper
   * @param courseServiceBean course service
   * @param studentServiceBean student service
   * @param sessionServiceBean session service
   * @param selectionWindowServiceBean selection window service
   * @param courseValidationServiceBean course validation service
   */
  public SelectionService(
      final SelectionMapper selectionMapperBean,
      final CourseService courseServiceBean,
      final StudentService studentServiceBean,
      final SessionService sessionServiceBean,
      final SelectionWindowService selectionWindowServiceBean,
      final CourseValidationService courseValidationServiceBean) {
    this.selectionMapper = selectionMapperBean;
    this.courseService = courseServiceBean;
    this.studentService = studentServiceBean;
    this.sessionService = sessionServiceBean;
    this.selectionWindowService = selectionWindowServiceBean;
    this.courseValidationService = courseValidationServiceBean;
  }

  /**
   * Lists selection records visible to the current user.
   *
   * @param keyword optional search keyword
   * @param page requested page number
   * @param pageSize requested page size
   * @param session current HTTP session
   * @return paged selection records
   */
  public PageResult<SelectionRecord> list(
      final String keyword,
      final Integer page,
      final Integer pageSize,
      final HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String studentId =
        current.getRole() == Role.STUDENT ? current.getId() : null;
    String teacherId =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    return PageResult.of(
        selectionMapper.selectJoinedList(keyword, studentId, teacherId),
        PageQuery.of(page, pageSize));
  }

  /**
   * Lists all selection records visible to the current user.
   *
   * @param session current HTTP session
   * @return visible selection records
   */
  public List<SelectionRecord> listAllVisible(final HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String studentId =
        current.getRole() == Role.STUDENT ? current.getId() : null;
    String teacherId =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    return selectionMapper.selectJoinedList(null, studentId, teacherId);
  }

  /**
   * Lists students enrolled in a specific course (teacher/admin only).
   *
   * @param courseId course id
   * @param session current HTTP session
   * @return selection records for the course
   */
  public List<SelectionRecord> listStudentsByCourse(
      final String courseId, final HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    if (current.getRole() == Role.TEACHER) {
      Course course = courseService.require(courseId);
      if (!current.getId().equals(course.getTid())) {
        throw new AppException(HttpStatus.FORBIDDEN, "只能查看自己课程的学生");
      }
    }
    return selectionMapper.selectByCourseId(courseId);
  }

  /**
   * Creates a selection record after applying all selection rules.
   *
   * @param record selection request
   * @param session current HTTP session
   * @return created selection record
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public SelectionRecord create(
      final SelectionRecord record,
      final HttpSession session) {
    UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.STUDENT);
    selectionWindowService.requireOpen("SELECT", current);
    String studentId =
        current.getRole() == Role.STUDENT
            ? current.getId()
            : record.getStudentId();
    Course course =
        validateSelectionConstraints(null, record.getCourseId(), studentId);
    String teacherId = course.getTid();
    String id = IdGenerator.newId();
    SelectionRecord insert = new SelectionRecord();
    insert.setId(id);
    insert.setCourseId(record.getCourseId());
    insert.setStudentId(studentId);
    insert.setTeacherId(teacherId);
    insert.setScore(current.getRole() == Role.ADMIN ? record.getScore() : null);
    insert.setGraded(
        current.getRole() == Role.ADMIN && record.getScore() != null);
    insert.setCreateTime(LocalDateTime.now());
    selectionMapper.insert(insert);
    return selectionMapper.selectJoinedById(id);
  }

  /**
   * Updates a selection record or records a teacher score.
   *
   * @param id selection record id
   * @param record update payload
   * @param session current HTTP session
   * @return updated selection record
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public SelectionRecord update(
      final String id,
      final SelectionRecord record,
      final HttpSession session) {
    UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    SelectionRecord existed = require(id);
    if (current.getRole() == Role.TEACHER) {
      if (!current.getId().equals(existed.getTeacherId())) {
        throw new AppException(HttpStatus.FORBIDDEN, "只能修改自己负责的选课记录");
      }
      SelectionRecord update = new SelectionRecord();
      update.setId(id);
      update.setScore(record.getScore());
      update.setGraded(record.getScore() != null);
      selectionMapper.updateById(update);
      return selectionMapper.selectJoinedById(id);
    }

    Course course =
        validateSelectionConstraints(
            id,
            record.getCourseId(),
            record.getStudentId());
    SelectionRecord update = new SelectionRecord();
    update.setId(id);
    update.setCourseId(record.getCourseId());
    update.setStudentId(record.getStudentId());
    update.setTeacherId(course.getTid());
    update.setScore(record.getScore());
    update.setGraded(record.getScore() != null);
    selectionMapper.updateById(update);
    return selectionMapper.selectJoinedById(id);
  }

  /**
   * Deletes a selection record after drop-window and ownership checks.
   *
   * @param id selection record id
   * @param session current HTTP session
   */
  @Transactional
  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public void delete(final String id, final HttpSession session) {
    UserSession current =
        sessionService.requireRole(session, Role.ADMIN, Role.STUDENT);
    selectionWindowService.requireOpen("DROP", current);
    SelectionRecord existed = require(id);
    if (current.getRole() == Role.STUDENT
        && !current.getId().equals(existed.getStudentId())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能退选自己的课程");
    }
    selectionMapper.deleteById(id);
  }

  /**
   * Counts selection records visible to the current user.
   *
   * @param session current HTTP session
   * @return visible selection count
   */
  public long count(final HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String studentId =
        current.getRole() == Role.STUDENT ? current.getId() : null;
    String teacherId =
        current.getRole() == Role.TEACHER ? current.getId() : null;
    LambdaQueryWrapper<SelectionRecord> wrapper = new LambdaQueryWrapper<>();
    if (studentId != null) {
      wrapper.eq(SelectionRecord::getStudentId, studentId);
    }
    if (teacherId != null) {
      wrapper.eq(SelectionRecord::getTeacherId, teacherId);
    }
    return selectionMapper.selectCount(wrapper);
  }

  /**
   * Loads a selection record or fails with a not-found response.
   *
   * @param id selection record id
   * @return loaded selection record
   */
  public SelectionRecord require(final String id) {
    SelectionRecord record = selectionMapper.selectJoinedById(id);
    if (record == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "选课记录不存在");
    }
    return record;
  }

  /**
   * Builds a grade report for the current student.
   *
   * @param keyword optional search keyword
   * @param page requested page number
   * @param pageSize requested page size
   * @param session current HTTP session
   * @return student grade report
   */
  public StudentGradeReport gradeReport(
      final String keyword,
      final Integer page,
      final Integer pageSize,
      final HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.STUDENT);
    List<SelectionRecord> all =
        selectionMapper.selectJoinedList(null, current.getId(), null);
    List<SelectionRecord> filtered =
        all.stream()
            .filter(
                item ->
                    !StringUtils.hasText(keyword)
                        || contains(item.getCourseName(), keyword)
                        || contains(item.getTeacherName(), keyword)
                        || contains(item.getTimeSlot(), keyword))
            .toList();

    double totalCredits =
        round(
            all.stream()
                .map(SelectionRecord::getCourseCredit)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
    double earnedCredits =
        round(
            all.stream()
                .filter(this::isPassed)
                .map(SelectionRecord::getCourseCredit)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
    long gradedCourses = all.stream().filter(this::isGraded).count();
    long pendingCourses = all.size() - gradedCourses;
    long failedCourses =
        all.stream().filter(this::isFailed).count();
    double averageScore =
        round(
            all.stream()
                .filter(this::isGraded)
                .map(SelectionRecord::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0D));
    double gpaCredits =
        all.stream()
            .filter(this::isGraded)
            .map(SelectionRecord::getCourseCredit)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .sum();
    double gpaValue =
        gpaCredits <= 0
            ? 0D
            : round(
                all.stream()
                    .filter(this::isGraded)
                    .mapToDouble(
                        item ->
                            toGpa(item.getScore()) * courseCreditOrZero(item))
                    .sum()
                    / gpaCredits);

    return new StudentGradeReport(
        gpaValue,
        averageScore,
        earnedCredits,
        totalCredits,
        gradedCourses,
        pendingCourses,
        failedCourses,
        PageResult.of(filtered, PageQuery.of(page, pageSize)));
  }

  private Course validateSelectionConstraints(
      final String selectionId,
      final String courseId,
      final String studentId) {
    if (!StringUtils.hasText(courseId) || !StringUtils.hasText(studentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "课程和学生不能为空");
    }
    studentService.lockForSelection(studentId);
    Course course = courseService.requireForUpdate(courseId);

    // 校验1：重复选课检测
    SelectionRecord duplicated =
        StringUtils.hasText(selectionId)
            ? selectionMapper.selectByCourseAndStudentExcludingId(
                courseId,
                studentId,
                selectionId)
            : selectionMapper.selectByCourseAndStudent(courseId, studentId);
    if (duplicated != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该学生已选过这门课");
    }

    // 校验2：课程容量检测
    if (course.getMaxStudents() != null && course.getMaxStudents() > 0) {
      long currentCount = selectionMapper.countByCourse(courseId, selectionId);
      if (currentCount >= course.getMaxStudents()) {
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "该课程选课人数已达上限（" + course.getMaxStudents() + "人），无法再选");
      }
    }

    validateTimeSlotConflict(course, studentId, selectionId);

    // 计算学生当前GPA（用于学分上限校验）
    double currentGpa = calculateStudentGpa(studentId);

    // 校验4-8：新增的智能冲突检测（年级权限、先修课程、互斥课程、类型限选、学分上限）
    courseValidationService.validateAll(
        course,
        studentId,
        selectionId,
        currentGpa);

    return course;
  }

  private void validateTimeSlotConflict(
      final Course course,
      final String studentId,
      final String excludeSelectionId) {
    if (!StringUtils.hasText(course.getTimeSlot())) {
      return;
    }
    List<SelectionRecord> selections =
        selectionMapper.selectJoinedList(null, studentId, null);
    for (SelectionRecord selection : selections) {
      if (selection == null || Objects.equals(selection.getId(), excludeSelectionId)) {
        continue;
      }
      if (CourseTimeSlot.overlaps(course.getTimeSlot(), selection.getTimeSlot())) {
        String conflictTime =
            StringUtils.hasText(selection.getTimeSlot())
                ? selection.getTimeSlot()
                : course.getTimeSlot();
        throw new AppException(
            HttpStatus.BAD_REQUEST,
            "上课时间与已选课程冲突：" + conflictTime);
      }
    }
  }

  /**
   * Calculates the current student GPA.
   *
   * @param studentId student id
   * @return calculated GPA
   */
  private double calculateStudentGpa(final String studentId) {
    List<SelectionRecord> all =
        selectionMapper.selectJoinedList(null, studentId, null);
    double gpaCredits =
        all.stream()
            .filter(this::isGraded)
            .map(SelectionRecord::getCourseCredit)
            .filter(Objects::nonNull)
            .mapToDouble(Double::doubleValue)
            .sum();
    if (gpaCredits <= 0) {
      return 0D;
    }
    double gpaSum =
        all.stream()
            .filter(this::isGraded)
            .mapToDouble(
                item ->
                    toGpa(item.getScore())
                        * courseCreditOrZero(item))
            .sum();
    return round(gpaSum / gpaCredits);
  }

  private boolean contains(final String source, final String keyword) {
    return StringUtils.hasText(source)
        && source.toLowerCase().contains(keyword.trim().toLowerCase());
  }

  private boolean isGraded(final SelectionRecord record) {
    return Boolean.TRUE.equals(record.getGraded());
  }

  private boolean isPassed(final SelectionRecord record) {
    return isGraded(record)
        && record.getScore() != null
        && record.getScore() >= PASSING_SCORE;
  }

  private boolean isFailed(final SelectionRecord record) {
    return isGraded(record)
        && record.getScore() != null
        && record.getScore() < PASSING_SCORE;
  }

  private double toGpa(final Double score) {
    if (score == null || score < PASSING_SCORE) {
      return 0D;
    }
    return (score - GPA_BASE_SCORE) / GPA_SCORE_STEP;
  }

  private double courseCreditOrZero(final SelectionRecord record) {
    return record.getCourseCredit() == null ? 0D : record.getCourseCredit();
  }

  private double round(final double value) {
    return Math.round(value * ROUND_SCALE) / ROUND_SCALE;
  }
}
