package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SelectionServiceScoreUpdateTest {

  @Test
  void adminCanUpdateOnlyScoreWithoutCourseAndStudentPayload() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    CourseService courseService = mock(CourseService.class);
    StudentService studentService = mock(StudentService.class);
    SessionService sessionService = mock(SessionService.class);
    SelectionWindowService selectionWindowService = mock(SelectionWindowService.class);
    CourseValidationService courseValidationService = mock(CourseValidationService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN, Role.TEACHER))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));

    SelectionRecord existed = new SelectionRecord();
    existed.setId("R1");
    existed.setCourseId("C1");
    existed.setStudentId("S1");
    existed.setTeacherId("T1");

    SelectionRecord updated = new SelectionRecord();
    updated.setId("R1");
    updated.setScore(88D);
    updated.setGraded(true);
    when(selectionMapper.selectJoinedById("R1")).thenReturn(existed, updated);

    SelectionService service =
        new SelectionService(
            selectionMapper,
            courseService,
            studentService,
            sessionService,
            selectionWindowService,
            courseValidationService);

    SelectionRecord request = new SelectionRecord();
    request.setScore(88D);
    SelectionRecord result = service.update("R1", request, session);

    ArgumentCaptor<SelectionRecord> captor = ArgumentCaptor.forClass(SelectionRecord.class);
    verify(selectionMapper).updateById(captor.capture());
    assertEquals("R1", captor.getValue().getId());
    assertEquals(88D, captor.getValue().getScore());
    assertTrue(captor.getValue().getGraded());
    assertEquals(88D, result.getScore());
    verify(studentService, never()).lockForSelection("S1");
    verify(courseService, never()).requireForUpdate("C1");
  }

  @Test
  void adminCanUpdateScoreWhenCourseAndStudentAreUnchanged() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    CourseService courseService = mock(CourseService.class);
    StudentService studentService = mock(StudentService.class);
    SessionService sessionService = mock(SessionService.class);
    SelectionWindowService selectionWindowService = mock(SelectionWindowService.class);
    CourseValidationService courseValidationService = mock(CourseValidationService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN, Role.TEACHER))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));

    SelectionRecord existed = new SelectionRecord();
    existed.setId("R1");
    existed.setCourseId("C1");
    existed.setStudentId("S1");
    existed.setTeacherId("T1");

    SelectionRecord updated = new SelectionRecord();
    updated.setId("R1");
    updated.setCourseId("C1");
    updated.setStudentId("S1");
    updated.setScore(91D);
    updated.setGraded(true);
    when(selectionMapper.selectJoinedById("R1")).thenReturn(existed, updated);

    SelectionService service =
        new SelectionService(
            selectionMapper,
            courseService,
            studentService,
            sessionService,
            selectionWindowService,
            courseValidationService);

    SelectionRecord request = new SelectionRecord();
    request.setCourseId("C1");
    request.setStudentId("S1");
    request.setScore(91D);
    SelectionRecord result = service.update("R1", request, session);

    ArgumentCaptor<SelectionRecord> captor = ArgumentCaptor.forClass(SelectionRecord.class);
    verify(selectionMapper).updateById(captor.capture());
    assertEquals("R1", captor.getValue().getId());
    assertEquals(91D, captor.getValue().getScore());
    assertTrue(captor.getValue().getGraded());
    assertEquals(91D, result.getScore());
    verify(studentService, never()).lockForSelection("S1");
    verify(courseService, never()).requireForUpdate("C1");
  }
}
