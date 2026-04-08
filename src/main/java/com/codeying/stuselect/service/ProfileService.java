package com.codeying.stuselect.service;

import com.codeying.stuselect.mapper.AdminMapper;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.model.Admin;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

  private final SessionService sessionService;
  private final AdminMapper adminMapper;
  private final TeacherService teacherService;
  private final StudentService studentService;

  public ProfileService(
      SessionService sessionService,
      AdminMapper adminMapper,
      TeacherService teacherService,
      StudentService studentService) {
    this.sessionService = sessionService;
    this.adminMapper = adminMapper;
    this.teacherService = teacherService;
    this.studentService = studentService;
  }

  public Object profile(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return switch (current.getRole()) {
      case ADMIN -> adminMapper.selectById(current.getId());
      case TEACHER -> teacherService.getProfile(session);
      case STUDENT -> studentService.getProfile(session);
    };
  }

  public Object update(Object payload, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return switch (current.getRole()) {
      case ADMIN -> {
        Admin input = (Admin) payload;
        Admin admin = adminMapper.selectById(current.getId());
        admin.setUsername(input.getUsername() == null ? admin.getUsername() : input.getUsername());
        admin.setPassword(
            input.getPassword() == null || input.getPassword().isBlank()
                ? admin.getPassword()
                : input.getPassword());
        admin.setName(input.getName());
        admin.setTele(input.getTele());
        adminMapper.updateById(admin);
        yield adminMapper.selectById(current.getId());
      }
      case TEACHER -> teacherService.updateProfile((Teacher) payload, session);
      case STUDENT -> studentService.updateProfile((Student) payload, session);
    };
  }
}
