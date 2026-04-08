package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SelectionService {

  private final SelectionMapper selectionMapper;
  private final CourseService courseService;
  private final SessionService sessionService;

  public SelectionService(
      SelectionMapper selectionMapper,
      CourseService courseService,
      SessionService sessionService) {
    this.selectionMapper = selectionMapper;
    this.courseService = courseService;
    this.sessionService = sessionService;
  }

  public List<SelectionRecord> list(String keyword, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String studentId = current.getRole() == Role.STUDENT ? current.getId() : null;
    String teacherId = current.getRole() == Role.TEACHER ? current.getId() : null;
    return selectionMapper.selectJoinedList(keyword, studentId, teacherId);
  }

  public SelectionRecord create(SelectionRecord record, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.STUDENT);
    String studentId = current.getRole() == Role.STUDENT ? current.getId() : record.getStudentId();
    Course course = courseService.require(record.getCourseId());
    if (selectionMapper.selectByCourseAndStudent(record.getCourseId(), studentId) != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "该学生已选过这门课");
    }
    String teacherId = course.getTid();
    String id = IdGenerator.newId();
    SelectionRecord insert = new SelectionRecord();
    insert.setId(id);
    insert.setCourseId(record.getCourseId());
    insert.setStudentId(studentId);
    insert.setTeacherId(teacherId);
    insert.setScore(current.getRole() == Role.ADMIN ? record.getScore() : 0D);
    insert.setCreateTime(LocalDateTime.now());
    selectionMapper.insert(insert);
    return selectionMapper.selectJoinedById(id);
  }

  public SelectionRecord update(String id, SelectionRecord record, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    SelectionRecord existed = require(id);
    if (current.getRole() == Role.TEACHER) {
      if (!current.getId().equals(existed.getTeacherId())) {
        throw new AppException(HttpStatus.FORBIDDEN, "只能修改自己负责的选课记录");
      }
      SelectionRecord update = new SelectionRecord();
      update.setId(id);
      update.setScore(record.getScore());
      selectionMapper.updateById(update);
      return selectionMapper.selectJoinedById(id);
    }

    Course course = courseService.require(record.getCourseId());
    SelectionRecord update = new SelectionRecord();
    update.setId(id);
    update.setCourseId(record.getCourseId());
    update.setStudentId(record.getStudentId());
    update.setTeacherId(course.getTid());
    update.setScore(record.getScore());
    selectionMapper.updateById(update);
    return selectionMapper.selectJoinedById(id);
  }

  public void delete(String id, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN, Role.STUDENT);
    SelectionRecord existed = require(id);
    if (current.getRole() == Role.STUDENT && !current.getId().equals(existed.getStudentId())) {
      throw new AppException(HttpStatus.FORBIDDEN, "只能退选自己的课程");
    }
    selectionMapper.deleteById(id);
  }

  public long count(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    String studentId = current.getRole() == Role.STUDENT ? current.getId() : null;
    String teacherId = current.getRole() == Role.TEACHER ? current.getId() : null;
    LambdaQueryWrapper<SelectionRecord> wrapper = new LambdaQueryWrapper<>();
    if (studentId != null) {
      wrapper.eq(SelectionRecord::getStudentId, studentId);
    }
    if (teacherId != null) {
      wrapper.eq(SelectionRecord::getTeacherId, teacherId);
    }
    return selectionMapper.selectCount(wrapper);
  }

  public SelectionRecord require(String id) {
    SelectionRecord record = selectionMapper.selectJoinedById(id);
    if (record == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "选课记录不存在");
    }
    return record;
  }
}
