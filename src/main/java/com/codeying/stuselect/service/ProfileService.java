package com.codeying.stuselect.service;

import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.model.Admin;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

  private final SessionService sessionService;
  private final AdminService adminService;
  private final TeacherService teacherService;
  private final StudentService studentService;

  public ProfileService(
      SessionService sessionService,
      AdminService adminService,
      TeacherService teacherService,
      StudentService studentService) {
    this.sessionService = sessionService;
    this.adminService = adminService;
    this.teacherService = teacherService;
    this.studentService = studentService;
  }

  public Object profile(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return switch (current.getRole()) {
      case ADMIN -> adminService.getProfile(session);
      case TEACHER -> teacherService.getProfile(session);
      case STUDENT -> studentService.getProfile(session);
    };
  }

  public Object update(Object payload, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return switch (current.getRole()) {
      case ADMIN -> adminService.updateProfile((Admin) payload, session);
      case TEACHER -> teacherService.updateProfile((Teacher) payload, session);
      case STUDENT -> studentService.updateProfile((Student) payload, session);
    };
  }
}
