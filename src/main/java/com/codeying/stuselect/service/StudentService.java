package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.CredentialRules;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.model.Student;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Manages student account query, profile update, and administrator
 * maintenance actions.
 */
@Service
public class StudentService {

  /** Student persistence mapper. */
  private final StudentMapper studentMapper;

  /** Session validation service. */
  private final SessionService sessionService;

  /** Password encoding service. */
  private final PasswordService passwordService;

  /** Administrator audit log service. */
  private final AdminAuditLogService adminAuditLogService;

  /**
   * Creates a student service with mapper, session, password, and audit
   * dependencies.
   *
   * @param studentMapperBean student mapper
   * @param sessionServiceBean session service
   * @param passwordServiceBean password service
   * @param auditLogService audit log service
   */
  public StudentService(
      final StudentMapper studentMapperBean,
      final SessionService sessionServiceBean,
      final PasswordService passwordServiceBean,
      final AdminAuditLogService auditLogService) {
    this.studentMapper = studentMapperBean;
    this.sessionService = sessionServiceBean;
    this.passwordService = passwordServiceBean;
    this.adminAuditLogService = auditLogService;
  }

  /**
   * Returns paged students visible to the current user.
   *
   * @param keyword search keyword
   * @param page requested page
   * @param pageSize requested page size
   * @param session current HTTP session
   * @return paged student result
   */
  public PageResult<Student> list(
      final String keyword,
      final Integer page,
      final Integer pageSize,
      final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.STUDENT) {
      return PageResult.of(
          List.of(require(current.getId())),
          PageQuery.of(page, pageSize));
    }
    final LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(Student::getUsername, keyword)
                  .or()
                  .like(Student::getNumb, keyword)
                  .or()
                  .like(Student::getSname, keyword)
                  .or()
                  .like(Student::getSdept, keyword)
                  .or()
                  .like(Student::getSmajor, keyword)
                  .or()
                  .like(Student::getSclass, keyword));
    }
    wrapper.orderByDesc(Student::getId);
    return PageResult.of(
        studentMapper.selectList(wrapper),
        PageQuery.of(page, pageSize));
  }

  /**
   * Lists all students visible to the current user.
   *
   * @param session current HTTP session
   * @return visible students
   */
  public List<Student> listAllVisible(final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.STUDENT) {
      return List.of(require(current.getId()));
    }
    return studentMapper.selectList(
        new LambdaQueryWrapper<Student>().orderByDesc(Student::getId));
  }

  /**
   * Creates a student account.
   *
   * @param student submitted student data
   * @param session current HTTP session
   * @return created student
   */
  public Student create(final Student student, final HttpSession session) {
    final UserSession current = sessionService.requireRole(session, Role.ADMIN);
    CredentialRules.requirePassword(student.getPassword());
    ensureUsernameAvailable(student.getUsername(), null);
    student.setId(IdGenerator.newId());
    student.setPassword(passwordService.encode(student.getPassword()));
    studentMapper.insert(student);
    adminAuditLogService.record(
        current,
        "新增",
        "学生",
        student.getId(),
        displayName(student),
        "账号：" + student.getUsername());
    return studentMapper.selectById(student.getId());
  }

  /**
   * Updates a student account by administrator.
   *
   * @param id student id
   * @param student submitted student data
   * @param session current HTTP session
   * @return updated student
   */
  public Student update(
      final String id,
      final Student student,
      final HttpSession session) {
    final UserSession actor = sessionService.requireRole(session, Role.ADMIN);
    final Student target = require(id);
    final String nextUsername =
        defaultValue(student.getUsername(), target.getUsername());
    CredentialRules.requirePasswordIfProvided(student.getPassword());
    ensureUsernameAvailable(nextUsername, target.getId());
    mergeStudent(target, student);
    studentMapper.updateById(target);
    adminAuditLogService.record(
        actor,
        "编辑",
        "学生",
        target.getId(),
        displayName(target),
        "账号："
            + target.getUsername()
            + "，学号："
            + defaultValue(target.getNumb(), "-"));
    return studentMapper.selectById(id);
  }

  /**
   * Deletes a student account.
   *
   * @param id student id
   * @param session current HTTP session
   */
  public void delete(final String id, final HttpSession session) {
    final UserSession current = sessionService.requireRole(session, Role.ADMIN);
    final Student target = require(id);
    studentMapper.deleteById(id);
    adminAuditLogService.record(
        current,
        "删除",
        "学生",
        target.getId(),
        displayName(target),
        "账号：" + target.getUsername());
  }

  /**
   * Returns the profile of the logged-in student.
   *
   * @param session current HTTP session
   * @return student profile
   */
  public Student getProfile(final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.STUDENT);
    return require(current.getId());
  }

  /**
   * Updates the profile of the logged-in student.
   *
   * @param input submitted profile data
   * @param session current HTTP session
   * @return updated student profile
   */
  public Student updateProfile(
      final Student input,
      final HttpSession session) {
    final UserSession current =
        sessionService.requireRole(session, Role.STUDENT);
    final Student student = require(current.getId());
    final String nextUsername =
        defaultValue(input.getUsername(), student.getUsername());
    CredentialRules.requirePasswordIfProvided(input.getPassword());
    ensureUsernameAvailable(nextUsername, student.getId());
    mergeStudent(student, input);
    studentMapper.updateById(student);
    return studentMapper.selectById(student.getId());
  }

  /**
   * Counts students visible to the current user.
   *
   * @param session current HTTP session
   * @return visible student count
   */
  public long count(final HttpSession session) {
    final UserSession current = sessionService.requireUser(session);
    return current.getRole() == Role.ADMIN || current.getRole() == Role.TEACHER
        ? studentMapper.selectCount(null)
        : 1L;
  }

  /**
   * Finds a student by username.
   *
   * @param username account username
   * @return matched student or null
   */
  public Student findByUsername(final String username) {
    return studentMapper.selectOne(
        new LambdaQueryWrapper<Student>()
            .eq(Student::getUsername, username)
            .last("limit 1"));
  }

  /**
   * Loads a student or throws when it does not exist.
   *
   * @param id student id
   * @return existing student
   */
  public Student require(final String id) {
    final Student student = studentMapper.selectById(id);
    if (student == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "学生不存在");
    }
    return student;
  }

  /**
   * Locks a student row before creating a selection.
   *
   * @param id student id
   */
  public void lockForSelection(final String id) {
    final Student student = studentMapper.selectByIdForUpdate(id);
    if (student == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "学生不存在");
    }
  }

  private void mergeStudent(final Student target, final Student input) {
    target.setUsername(defaultValue(input.getUsername(), target.getUsername()));
    target.setPassword(
        passwordService.encodeIfProvided(
            input.getPassword(),
            target.getPassword()));
    target.setNumb(input.getNumb());
    target.setSname(input.getSname());
    target.setSdept(input.getSdept());
    target.setSbirthday(input.getSbirthday());
    target.setTele(input.getTele());
    target.setEmail(input.getEmail());
    target.setSsex(input.getSsex());
    target.setAge(input.getAge());
    target.setSmajor(input.getSmajor());
    target.setSclass(input.getSclass());
  }

  private String defaultValue(final String input, final String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }

  private void ensureUsernameAvailable(
      final String username,
      final String currentId) {
    CredentialRules.requireUsername(username);
    final Student existed = findByUsername(username);
    if (existed != null && !existed.getId().equals(currentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "学生账号已存在");
    }
  }

  private String displayName(final Student student) {
    return StringUtils.hasText(student.getSname())
        ? student.getSname()
        : student.getUsername();
  }
}
