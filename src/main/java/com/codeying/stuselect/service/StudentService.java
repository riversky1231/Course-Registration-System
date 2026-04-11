package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.model.Student;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentService {

  private final StudentMapper studentMapper;
  private final SessionService sessionService;
  private final PasswordService passwordService;
  private final AdminAuditLogService adminAuditLogService;

  public StudentService(
      StudentMapper studentMapper,
      SessionService sessionService,
      PasswordService passwordService,
      AdminAuditLogService adminAuditLogService) {
    this.studentMapper = studentMapper;
    this.sessionService = sessionService;
    this.passwordService = passwordService;
    this.adminAuditLogService = adminAuditLogService;
  }

  public PageResult<Student> list(
      String keyword, Integer page, Integer pageSize, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.STUDENT) {
      return PageResult.of(java.util.List.of(require(current.getId())), PageQuery.of(page, pageSize));
    }
    LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
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
    return PageResult.of(studentMapper.selectList(wrapper), PageQuery.of(page, pageSize));
  }

  public List<Student> listAllVisible(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.STUDENT) {
      return List.of(require(current.getId()));
    }
    return studentMapper.selectList(new LambdaQueryWrapper<Student>().orderByDesc(Student::getId));
  }

  public Student create(Student student, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN);
    if (!StringUtils.hasText(student.getUsername()) || !StringUtils.hasText(student.getPassword())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
    }
    ensureUsernameAvailable(student.getUsername(), null);
    student.setId(IdGenerator.newId());
    student.setPassword(passwordService.encode(student.getPassword()));
    studentMapper.insert(student);
    adminAuditLogService.record(
        current, "新增", "学生", student.getId(), displayName(student), "账号：" + student.getUsername());
    return studentMapper.selectById(student.getId());
  }

  public Student update(String id, Student student, HttpSession session) {
    UserSession actor = sessionService.requireRole(session, Role.ADMIN);
    Student target = require(id);
    ensureUsernameAvailable(defaultValue(student.getUsername(), target.getUsername()), target.getId());
    mergeStudent(target, student);
    studentMapper.updateById(target);
    adminAuditLogService.record(
        actor,
        "编辑",
        "学生",
        target.getId(),
        displayName(target),
        "账号：" + target.getUsername() + "，学号：" + defaultValue(target.getNumb(), "-"));
    return studentMapper.selectById(id);
  }

  public void delete(String id, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN);
    Student target = require(id);
    studentMapper.deleteById(id);
    adminAuditLogService.record(
        current, "删除", "学生", target.getId(), displayName(target), "账号：" + target.getUsername());
  }

  public Student getProfile(HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.STUDENT);
    return require(current.getId());
  }

  public Student updateProfile(Student input, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.STUDENT);
    Student student = require(current.getId());
    ensureUsernameAvailable(defaultValue(input.getUsername(), student.getUsername()), student.getId());
    mergeStudent(student, input);
    studentMapper.updateById(student);
    return studentMapper.selectById(student.getId());
  }

  public long count(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return current.getRole() == Role.ADMIN || current.getRole() == Role.TEACHER
        ? studentMapper.selectCount(null)
        : 1L;
  }

  public Student findByUsername(String username) {
    return studentMapper.selectOne(
        new LambdaQueryWrapper<Student>().eq(Student::getUsername, username).last("limit 1"));
  }

  public Student require(String id) {
    Student student = studentMapper.selectById(id);
    if (student == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "学生不存在");
    }
    return student;
  }

  public void lockForSelection(String id) {
    Student student = studentMapper.selectByIdForUpdate(id);
    if (student == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "学生不存在");
    }
  }

  private void mergeStudent(Student target, Student input) {
    target.setUsername(defaultValue(input.getUsername(), target.getUsername()));
    target.setPassword(passwordService.encodeIfProvided(input.getPassword(), target.getPassword()));
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

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }

  private void ensureUsernameAvailable(String username, String currentId) {
    if (!StringUtils.hasText(username)) {
      return;
    }
    Student existed = findByUsername(username);
    if (existed != null && !existed.getId().equals(currentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "学生账号已存在");
    }
  }

  private String displayName(Student student) {
    return StringUtils.hasText(student.getSname()) ? student.getSname() : student.getUsername();
  }
}
