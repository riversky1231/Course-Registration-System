package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.TeacherMapper;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TeacherService {

  private final TeacherMapper teacherMapper;
  private final SessionService sessionService;

  public TeacherService(TeacherMapper teacherMapper, SessionService sessionService) {
    this.teacherMapper = teacherMapper;
    this.sessionService = sessionService;
  }

  public List<Teacher> list(String keyword, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.TEACHER) {
      return List.of(require(current.getId()));
    }
    LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(Teacher::getUsername, keyword)
                  .or()
                  .like(Teacher::getNumb, keyword)
                  .or()
                  .like(Teacher::getTname, keyword)
                  .or()
                  .like(Teacher::getTtel, keyword)
                  .or()
                  .like(Teacher::getTposition, keyword));
    }
    wrapper.orderByDesc(Teacher::getId);
    return teacherMapper.selectList(wrapper);
  }

  public Teacher create(Teacher teacher, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    if (!StringUtils.hasText(teacher.getUsername()) || !StringUtils.hasText(teacher.getPassword())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
    }
    if (findByUsername(teacher.getUsername()) != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "教师账号已存在");
    }
    teacher.setId(IdGenerator.newId());
    teacherMapper.insert(teacher);
    return teacherMapper.selectById(teacher.getId());
  }

  public Teacher update(String id, Teacher teacher, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    Teacher current = require(id);
    mergeTeacher(current, teacher);
    teacherMapper.updateById(current);
    return teacherMapper.selectById(id);
  }

  public void delete(String id, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    require(id);
    teacherMapper.deleteById(id);
  }

  public Teacher getProfile(HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.TEACHER);
    return require(current.getId());
  }

  public Teacher updateProfile(Teacher input, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.TEACHER);
    Teacher teacher = require(current.getId());
    mergeTeacher(teacher, input);
    teacherMapper.updateById(teacher);
    return teacherMapper.selectById(teacher.getId());
  }

  public long count(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return current.getRole() == Role.ADMIN || current.getRole() == Role.STUDENT
        ? teacherMapper.selectCount(null)
        : 1L;
  }

  public Teacher findByUsername(String username) {
    return teacherMapper.selectOne(
        new LambdaQueryWrapper<Teacher>().eq(Teacher::getUsername, username).last("limit 1"));
  }

  public Teacher require(String id) {
    Teacher teacher = teacherMapper.selectById(id);
    if (teacher == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "教师不存在");
    }
    return teacher;
  }

  private void mergeTeacher(Teacher target, Teacher input) {
    target.setUsername(defaultValue(input.getUsername(), target.getUsername()));
    target.setPassword(defaultValue(input.getPassword(), target.getPassword()));
    target.setNumb(input.getNumb());
    target.setTname(input.getTname());
    target.setTbirthday(input.getTbirthday());
    target.setTposition(input.getTposition());
    target.setTtel(input.getTtel());
    target.setAge(input.getAge());
    target.setGender(input.getGender());
  }

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }
}
