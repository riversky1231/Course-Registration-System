package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.CourseMapper;
import com.codeying.stuselect.model.Course;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class CourseService {

  private final CourseMapper courseMapper;
  private final SessionService sessionService;

  public CourseService(CourseMapper courseMapper, SessionService sessionService) {
    this.courseMapper = courseMapper;
    this.sessionService = sessionService;
  }

  public List<Course> list(String keyword, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String teacherId = current.getRole() == Role.TEACHER ? current.getId() : null;
    return courseMapper.selectListWithTeacher(keyword, teacherId);
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
    }
    course.setId(IdGenerator.newId());
    courseMapper.insert(course);
    return courseMapper.selectByIdWithTeacher(course.getId());
  }

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
    course.setName(defaultValue(input.getName(), course.getName()));
    course.setScore(input.getScore());
    course.setNumb(defaultValue(input.getNumb(), course.getNumb()));
    course.setTid(
        current.getRole() == Role.TEACHER
            ? current.getId()
            : defaultValue(input.getTid(), course.getTid()));
    course.setJianjie(input.getJianjie());
    courseMapper.updateById(course);
    return courseMapper.selectByIdWithTeacher(id);
  }

  public void delete(String id, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    Course course = require(id);
    if (current.getRole() == Role.TEACHER && !current.getId().equals(course.getTid())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能删除自己的课程");
    }
    courseMapper.deleteById(id);
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

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }
}
