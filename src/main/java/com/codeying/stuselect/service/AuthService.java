package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.AuthRequests;
import com.codeying.stuselect.mapper.AdminMapper;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.mapper.TeacherMapper;
import com.codeying.stuselect.model.Admin;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

  private final AdminMapper adminMapper;
  private final TeacherMapper teacherMapper;
  private final StudentMapper studentMapper;
  private final SessionService sessionService;

  public AuthService(
      AdminMapper adminMapper,
      TeacherMapper teacherMapper,
      StudentMapper studentMapper,
      SessionService sessionService) {
    this.adminMapper = adminMapper;
    this.teacherMapper = teacherMapper;
    this.studentMapper = studentMapper;
    this.sessionService = sessionService;
  }

  public UserSession login(AuthRequests.LoginRequest request, HttpSession session) {
    Role role = Role.from(request.role());
    UserSession userSession =
        switch (role) {
          case ADMIN ->
              require(findAdmin(request.username(), request.password()), role);
          case TEACHER ->
              require(findTeacher(request.username(), request.password()), role);
          case STUDENT ->
              require(findStudent(request.username(), request.password()), role);
        };
    sessionService.save(session, userSession);
    return userSession;
  }

  public void register(AuthRequests.RegisterRequest request) {
    Role role = Role.from(request.role());
    switch (role) {
      case ADMIN -> {
        if (adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, request.username()).last("limit 1"))
            != null) {
          throw new AppException(HttpStatus.BAD_REQUEST, "账号已存在");
        }
        Admin admin = new Admin();
        admin.setId(IdGenerator.newId());
        admin.setUsername(request.username());
        admin.setPassword(request.password());
        admin.setName("新管理员");
        adminMapper.insert(admin);
      }
      case TEACHER -> {
        if (teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>()
                    .eq(Teacher::getUsername, request.username())
                    .last("limit 1"))
            != null) {
          throw new AppException(HttpStatus.BAD_REQUEST, "账号已存在");
        }
        Teacher teacher = new Teacher();
        teacher.setId(IdGenerator.newId());
        teacher.setUsername(request.username());
        teacher.setPassword(request.password());
        teacher.setTname("新教师");
        teacherMapper.insert(teacher);
      }
      case STUDENT -> {
        if (studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                    .eq(Student::getUsername, request.username())
                    .last("limit 1"))
            != null) {
          throw new AppException(HttpStatus.BAD_REQUEST, "账号已存在");
        }
        Student student = new Student();
        student.setId(IdGenerator.newId());
        student.setUsername(request.username());
        student.setPassword(request.password());
        student.setSname("新学生");
        studentMapper.insert(student);
      }
    }
  }

  public UserSession current(HttpSession session) {
    return sessionService.current(session);
  }

  public void logout(HttpSession session) {
    sessionService.clear(session);
  }

  private Admin findAdmin(String username, String password) {
    return adminMapper.selectOne(
        new LambdaQueryWrapper<Admin>()
            .eq(Admin::getUsername, username)
            .eq(Admin::getPassword, password)
            .last("limit 1"));
  }

  private Teacher findTeacher(String username, String password) {
    return teacherMapper.selectOne(
        new LambdaQueryWrapper<Teacher>()
            .eq(Teacher::getUsername, username)
            .eq(Teacher::getPassword, password)
            .last("limit 1"));
  }

  private Student findStudent(String username, String password) {
    return studentMapper.selectOne(
        new LambdaQueryWrapper<Student>()
            .eq(Student::getUsername, username)
            .eq(Student::getPassword, password)
            .last("limit 1"));
  }

  private UserSession require(Object user, Role role) {
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "账号或密码错误");
    }
    return switch (role) {
      case ADMIN -> toSession((Admin) user);
      case TEACHER -> toSession((Teacher) user);
      case STUDENT -> toSession((Student) user);
    };
  }

  private UserSession toSession(Admin admin) {
    return new UserSession(
        admin.getId(),
        admin.getUsername(),
        Role.ADMIN,
        StringUtils.hasText(admin.getName()) ? admin.getName() : admin.getUsername());
  }

  private UserSession toSession(Teacher teacher) {
    return new UserSession(
        teacher.getId(),
        teacher.getUsername(),
        Role.TEACHER,
        StringUtils.hasText(teacher.getTname()) ? teacher.getTname() : teacher.getUsername());
  }

  private UserSession toSession(Student student) {
    return new UserSession(
        student.getId(),
        student.getUsername(),
        Role.STUDENT,
        StringUtils.hasText(student.getSname()) ? student.getSname() : student.getUsername());
  }
}
