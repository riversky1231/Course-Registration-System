package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
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

  public StudentService(StudentMapper studentMapper, SessionService sessionService) {
    this.studentMapper = studentMapper;
    this.sessionService = sessionService;
  }

  public List<Student> list(String keyword, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.STUDENT) {
      return List.of(require(current.getId()));
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
    return studentMapper.selectList(wrapper);
  }

  public Student create(Student student, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    if (!StringUtils.hasText(student.getUsername()) || !StringUtils.hasText(student.getPassword())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
    }
    if (findByUsername(student.getUsername()) != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "学生账号已存在");
    }
    student.setId(IdGenerator.newId());
    studentMapper.insert(student);
    return studentMapper.selectById(student.getId());
  }

  public Student update(String id, Student student, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    Student current = require(id);
    mergeStudent(current, student);
    studentMapper.updateById(current);
    return studentMapper.selectById(id);
  }

  public void delete(String id, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    require(id);
    studentMapper.deleteById(id);
  }

  public Student getProfile(HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.STUDENT);
    return require(current.getId());
  }

  public Student updateProfile(Student input, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.STUDENT);
    Student student = require(current.getId());
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

  private void mergeStudent(Student target, Student input) {
    target.setUsername(defaultValue(input.getUsername(), target.getUsername()));
    target.setPassword(defaultValue(input.getPassword(), target.getPassword()));
    target.setNumb(input.getNumb());
    target.setSname(input.getSname());
    target.setSdept(input.getSdept());
    target.setSbirthday(input.getSbirthday());
    target.setTele(input.getTele());
    target.setSsex(input.getSsex());
    target.setAge(input.getAge());
    target.setSmajor(input.getSmajor());
    target.setSclass(input.getSclass());
  }

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }
}
