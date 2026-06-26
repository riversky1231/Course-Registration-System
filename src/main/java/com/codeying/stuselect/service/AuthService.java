package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.CredentialRules;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.AuthRequests;
import com.codeying.stuselect.mapper.AdminMapper;
import jakarta.servlet.http.HttpServletRequest;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.mapper.TeacherMapper;
import com.codeying.stuselect.model.Admin;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import java.time.Year;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

  private final AdminMapper adminMapper;
  private final TeacherMapper teacherMapper;
  private final StudentMapper studentMapper;
  private final SessionService sessionService;
  private final PasswordService passwordService;

  public AuthService(
      AdminMapper adminMapper,
      TeacherMapper teacherMapper,
      StudentMapper studentMapper,
      SessionService sessionService,
      PasswordService passwordService) {
    this.adminMapper = adminMapper;
    this.teacherMapper = teacherMapper;
    this.studentMapper = studentMapper;
    this.sessionService = sessionService;
    this.passwordService = passwordService;
  }

  public UserSession login(AuthRequests.LoginRequest request, HttpServletRequest servletRequest) {
    Role role = Role.from(request.role());

    // 防止用户名枚举：总是执行BCrypt验证，即使用户不存在
    UserSession userSession =
        switch (role) {
          case ADMIN ->
              require(findAdminWithTimingSafety(request.username(), request.password()), role);
          case TEACHER ->
              require(findTeacherWithTimingSafety(request.username(), request.password()), role);
          case STUDENT ->
              require(findStudentWithTimingSafety(request.username(), request.password()), role);
        };
    sessionService.save(servletRequest, userSession);
    return userSession;
  }

  public void register(AuthRequests.RegisterRequest request) {
    Role role = Role.from(request.role());
    if (role != Role.STUDENT) {
      throw new AppException(HttpStatus.FORBIDDEN, "当前仅支持学生自助注册");
    }
    CredentialRules.requireUsername(request.username());
    CredentialRules.requirePassword(request.password());
    switch (role) {
      case ADMIN -> {
        throw new AppException(HttpStatus.FORBIDDEN, "当前仅支持学生自助注册");
      }
      case TEACHER -> {
        throw new AppException(HttpStatus.FORBIDDEN, "当前仅支持学生自助注册");
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
        student.setPassword(passwordService.encode(request.password()));
        student.setSname("新学生");
        student.setGrade(1);
        student.setEnrollmentYear(Year.now().getValue());
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
    Admin admin =
        adminMapper.selectOne(
            new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username).last("limit 1"));
    if (admin == null || !passwordService.matches(password, admin.getPassword())) {
      return null;
    }
    migratePasswordIfNeeded(admin, password);
    return admin;
  }

  /**
   * 防止时序攻击的Admin查找方法
   * 即使用户不存在也会执行BCrypt验证，避免通过响应时间判断用户是否存在
   */
  private Admin findAdminWithTimingSafety(String username, String password) {
    Admin admin =
        adminMapper.selectOne(
            new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username).last("limit 1"));

    // 始终执行BCrypt验证，即使用户不存在
    String hashToCheck = (admin != null) ? admin.getPassword() :
        "$2a$10$dummyhashtopreventtimingattacks.eG7vW8Z8qX8mK2QZ8Z8Z8Z8Z8Z8Z8Z8";
    boolean matches = passwordService.matches(password, hashToCheck);

    if (admin == null || !matches) {
      return null;
    }
    migratePasswordIfNeeded(admin, password);
    return admin;
  }

  private Teacher findTeacherWithTimingSafety(String username, String password) {
    Teacher teacher =
        teacherMapper.selectOne(
            new LambdaQueryWrapper<Teacher>().eq(Teacher::getUsername, username).last("limit 1"));

    String hashToCheck = (teacher != null) ? teacher.getPassword() :
        "$2a$10$dummyhashtopreventtimingattacks.eG7vW8Z8qX8mK2QZ8Z8Z8Z8Z8Z8Z8Z8";
    boolean matches = passwordService.matches(password, hashToCheck);

    if (teacher == null || !matches) {
      return null;
    }
    migratePasswordIfNeeded(teacher, password);
    return teacher;
  }

  private Student findStudentWithTimingSafety(String username, String password) {
    Student student =
        studentMapper.selectOne(
            new LambdaQueryWrapper<Student>().eq(Student::getUsername, username).last("limit 1"));

    String hashToCheck = (student != null) ? student.getPassword() :
        "$2a$10$dummyhashtopreventtimingattacks.eG7vW8Z8qX8mK2QZ8Z8Z8Z8Z8Z8Z8Z8";
    boolean matches = passwordService.matches(password, hashToCheck);

    if (student == null || !matches) {
      return null;
    }
    migratePasswordIfNeeded(student, password);
    return student;
  }

  private Teacher findTeacher(String username, String password) {
    Teacher teacher =
        teacherMapper.selectOne(
            new LambdaQueryWrapper<Teacher>().eq(Teacher::getUsername, username).last("limit 1"));
    if (teacher == null || !passwordService.matches(password, teacher.getPassword())) {
      return null;
    }
    migratePasswordIfNeeded(teacher, password);
    return teacher;
  }

  private Student findStudent(String username, String password) {
    Student student =
        studentMapper.selectOne(
            new LambdaQueryWrapper<Student>().eq(Student::getUsername, username).last("limit 1"));
    if (student == null || !passwordService.matches(password, student.getPassword())) {
      return null;
    }
    migratePasswordIfNeeded(student, password);
    return student;
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

  private void migratePasswordIfNeeded(Admin admin, String rawPassword) {
    if (!passwordService.needsMigration(admin.getPassword())) {
      return;
    }
    Admin patch = new Admin();
    patch.setId(admin.getId());
    patch.setPassword(passwordService.encode(rawPassword));
    adminMapper.updateById(patch);
    admin.setPassword(patch.getPassword());
  }

  private void migratePasswordIfNeeded(Teacher teacher, String rawPassword) {
    if (!passwordService.needsMigration(teacher.getPassword())) {
      return;
    }
    Teacher patch = new Teacher();
    patch.setId(teacher.getId());
    patch.setPassword(passwordService.encode(rawPassword));
    teacherMapper.updateById(patch);
    teacher.setPassword(patch.getPassword());
  }

  private void migratePasswordIfNeeded(Student student, String rawPassword) {
    if (!passwordService.needsMigration(student.getPassword())) {
      return;
    }
    Student patch = new Student();
    patch.setId(student.getId());
    patch.setPassword(passwordService.encode(rawPassword));
    studentMapper.updateById(patch);
    student.setPassword(patch.getPassword());
  }
}
