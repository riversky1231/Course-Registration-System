package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.CredentialRules;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.TeacherMapper;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TeacherService {

  private final TeacherMapper teacherMapper;
  private final SessionService sessionService;
  private final PasswordService passwordService;

  public TeacherService(
      TeacherMapper teacherMapper,
      SessionService sessionService,
      PasswordService passwordService) {
    this.teacherMapper = teacherMapper;
    this.sessionService = sessionService;
    this.passwordService = passwordService;
  }

  public PageResult<Teacher> list(
      String keyword, Integer page, Integer pageSize, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.TEACHER) {
      return PageResult.of(java.util.List.of(require(current.getId())), PageQuery.of(page, pageSize));
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
    return PageResult.of(teacherMapper.selectList(wrapper), PageQuery.of(page, pageSize));
  }

  public List<Teacher> listAllVisible(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (current.getRole() == Role.TEACHER) {
      return List.of(require(current.getId()));
    }
    return teacherMapper.selectList(new LambdaQueryWrapper<Teacher>().orderByDesc(Teacher::getId));
  }

  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public Teacher create(Teacher teacher, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    CredentialRules.requirePassword(teacher.getPassword());
    ensureUsernameAvailable(teacher.getUsername(), null);
    teacher.setId(IdGenerator.newId());
    teacher.setPassword(passwordService.encode(teacher.getPassword()));
    teacherMapper.insert(teacher);
    return teacherMapper.selectById(teacher.getId());
  }

  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public Teacher update(String id, Teacher teacher, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    Teacher target = require(id);
    String nextUsername = defaultValue(teacher.getUsername(), target.getUsername());
    CredentialRules.requirePasswordIfProvided(teacher.getPassword());
    ensureUsernameAvailable(nextUsername, target.getId());
    mergeTeacher(target, teacher);
    teacherMapper.updateById(target);
    return teacherMapper.selectById(id);
  }

  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public void delete(String id, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    require(id);
    if (teacherMapper.countCourseReferences(id) > 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该教师已有授课课程，不能删除");
    }
    if (teacherMapper.countSelectionReferences(id) > 0) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该教师已有选课记录，不能删除");
    }
    teacherMapper.deleteById(id);
  }

  public Teacher getProfile(HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.TEACHER);
    return require(current.getId());
  }

  public Teacher updateProfile(Teacher input, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.TEACHER);
    Teacher teacher = require(current.getId());
    String nextUsername = defaultValue(input.getUsername(), teacher.getUsername());
    CredentialRules.requirePasswordIfProvided(input.getPassword());
    ensureUsernameAvailable(nextUsername, teacher.getId());
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
    target.setPassword(passwordService.encodeIfProvided(input.getPassword(), target.getPassword()));
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

  private void ensureUsernameAvailable(String username, String currentId) {
    CredentialRules.requireUsername(username);
    Teacher existed = findByUsername(username);
    if (existed != null && !existed.getId().equals(currentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "教师账号已存在");
    }
  }
}
