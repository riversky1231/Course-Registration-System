package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.TeacherMapper;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

class TeacherServiceTest {

  @Test
  void deleteRejectsTeacherReferencedByCourses() {
    TeacherMapper teacherMapper = mock(TeacherMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(teacherMapper.selectById("T1")).thenReturn(existingTeacher());
    when(teacherMapper.countCourseReferences("T1")).thenReturn(1L);

    TeacherService service = new TeacherService(teacherMapper, sessionService, passwordService);

    assertThrows(AppException.class, () -> service.delete("T1", session));
    verify(teacherMapper, never()).deleteById("T1");
  }

  @Test
  void deleteRejectsTeacherReferencedBySelections() {
    TeacherMapper teacherMapper = mock(TeacherMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(teacherMapper.selectById("T1")).thenReturn(existingTeacher());
    when(teacherMapper.countCourseReferences("T1")).thenReturn(0L);
    when(teacherMapper.countSelectionReferences("T1")).thenReturn(1L);

    TeacherService service = new TeacherService(teacherMapper, sessionService, passwordService);

    assertThrows(AppException.class, () -> service.delete("T1", session));
    verify(teacherMapper, never()).deleteById("T1");
  }

  @Test
  void deleteRemovesUnreferencedTeacher() {
    TeacherMapper teacherMapper = mock(TeacherMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(teacherMapper.selectById("T1")).thenReturn(existingTeacher());
    when(teacherMapper.countCourseReferences("T1")).thenReturn(0L);
    when(teacherMapper.countSelectionReferences("T1")).thenReturn(0L);

    TeacherService service = new TeacherService(teacherMapper, sessionService, passwordService);

    service.delete("T1", session);

    verify(teacherMapper).deleteById("T1");
  }

  private Teacher existingTeacher() {
    Teacher teacher = new Teacher();
    teacher.setId("T1");
    return teacher;
  }
}
