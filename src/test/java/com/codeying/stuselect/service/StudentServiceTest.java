package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.model.Student;
import jakarta.servlet.http.HttpSession;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StudentServiceTest {

  @Test
  void listForTeacherReturnsOnlyStudentsInTeacherSelections() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student visible = new Student();
    visible.setId("S1");
    visible.setSname("student");

    when(sessionService.requireUser(session))
        .thenReturn(new UserSession("T1", "teacher", Role.TEACHER, "Teacher"));
    when(studentMapper.selectVisibleToTeacher("T1", "student")).thenReturn(List.of(visible));

    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    var result = service.list("student", 1, 10, session);

    assertEquals(1, result.total());
    assertEquals("S1", result.items().get(0).getId());
    verify(studentMapper).selectVisibleToTeacher("T1", "student");
  }

  @Test
  void listAllVisibleForTeacherReturnsOnlyStudentsInTeacherSelections() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student visible = new Student();
    visible.setId("S1");

    when(sessionService.requireUser(session))
        .thenReturn(new UserSession("T1", "teacher", Role.TEACHER, "Teacher"));
    when(studentMapper.selectVisibleToTeacher("T1", null)).thenReturn(List.of(visible));

    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    List<Student> result = service.listAllVisible(session);

    assertEquals(1, result.size());
    assertEquals("S1", result.get(0).getId());
    verify(studentMapper).selectVisibleToTeacher("T1", null);
  }

  @Test
  void countForTeacherCountsOnlyStudentsInTeacherSelections() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireUser(session))
        .thenReturn(new UserSession("T1", "teacher", Role.TEACHER, "Teacher"));
    when(studentMapper.countVisibleToTeacher("T1")).thenReturn(2L);

    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    assertEquals(2L, service.count(session));
    verify(studentMapper).countVisibleToTeacher("T1");
  }

  @Test
  void updateDefaultsMissingGradeAndEnrollmentYear() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student existing = new Student();
    existing.setId("S1");
    existing.setUsername("stu1");
    existing.setPassword("stored");

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(studentMapper.selectById("S1")).thenReturn(existing);
    when(studentMapper.selectOne(any())).thenReturn(null);
    when(passwordService.encodeIfProvided(null, "stored")).thenReturn("stored");

    Student input = new Student();
    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    service.update("S1", input, session);

    ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
    verify(studentMapper).updateById(captor.capture());
    assertEquals(1, captor.getValue().getGrade());
    assertEquals(Year.now().getValue(), captor.getValue().getEnrollmentYear());
  }

  @Test
  void updatePreservesGradeAndEnrollmentYearWhenInputOmitsThem() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student existing = new Student();
    existing.setId("S1");
    existing.setUsername("stu1");
    existing.setPassword("stored");
    existing.setGrade(3);
    existing.setEnrollmentYear(2023);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(studentMapper.selectById("S1")).thenReturn(existing);
    when(studentMapper.selectOne(any())).thenReturn(null);
    when(passwordService.encodeIfProvided(null, "stored")).thenReturn("stored");

    Student input = new Student();
    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    service.update("S1", input, session);

    ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
    verify(studentMapper).updateById(captor.capture());
    assertEquals(3, captor.getValue().getGrade());
    assertEquals(2023, captor.getValue().getEnrollmentYear());
  }

  @Test
  void updateProfileIgnoresSubmittedGradeAndEnrollmentYear() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student existing = new Student();
    existing.setId("S1");
    existing.setUsername("stu1");
    existing.setPassword("stored");
    existing.setGrade(1);
    existing.setEnrollmentYear(2026);

    when(sessionService.requireRole(session, Role.STUDENT))
        .thenReturn(new UserSession("S1", "stu1", Role.STUDENT, "Student"));
    when(studentMapper.selectById("S1")).thenReturn(existing);
    when(studentMapper.selectOne(any())).thenReturn(null);
    when(passwordService.encodeIfProvided(null, "stored")).thenReturn("stored");

    Student input = new Student();
    input.setGrade(4);
    input.setEnrollmentYear(2023);
    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    service.updateProfile(input, session);

    ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
    verify(studentMapper).updateById(captor.capture());
    assertEquals(1, captor.getValue().getGrade());
    assertEquals(2026, captor.getValue().getEnrollmentYear());
  }

  @Test
  void deleteRejectsStudentReferencedBySelections() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student existing = new Student();
    existing.setId("S1");

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(studentMapper.selectById("S1")).thenReturn(existing);
    when(studentMapper.countSelectionReferences("S1")).thenReturn(1L);

    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    assertThrows(AppException.class, () -> service.delete("S1", session));
    verify(studentMapper, never()).deleteById("S1");
  }

  @Test
  void deleteRemovesUnreferencedStudent() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    Student existing = new Student();
    existing.setId("S1");

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(studentMapper.selectById("S1")).thenReturn(existing);
    when(studentMapper.countSelectionReferences("S1")).thenReturn(0L);

    StudentService service = new StudentService(studentMapper, sessionService, passwordService);

    service.delete("S1", session);

    verify(studentMapper).deleteById("S1");
  }
}
