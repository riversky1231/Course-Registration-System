package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.Test;

class SelectionServiceTimeConflictTest {

  @Test
  void createRejectsOverlappingTimeSlotRanges() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    CourseService courseService = mock(CourseService.class);
    StudentService studentService = mock(StudentService.class);
    SessionService sessionService = mock(SessionService.class);
    SelectionWindowService selectionWindowService = mock(SelectionWindowService.class);
    CourseValidationService courseValidationService = mock(CourseValidationService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN, Role.STUDENT))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));

    Course newCourse = new Course();
    newCourse.setId("C2");
    newCourse.setTid("T1");
    newCourse.setTimeSlot("周一第2-3节");
    when(courseService.requireForUpdate("C2")).thenReturn(newCourse);
    when(selectionMapper.selectByCourseAndStudent("C2", "S1")).thenReturn(null);

    SelectionRecord existing = new SelectionRecord();
    existing.setId("R1");
    existing.setTimeSlot("周一第1-2节");
    when(selectionMapper.selectJoinedList(null, "S1", null)).thenReturn(List.of(existing));

    SelectionRecord request = new SelectionRecord();
    request.setCourseId("C2");
    request.setStudentId("S1");
    SelectionService service =
        new SelectionService(
            selectionMapper,
            courseService,
            studentService,
            sessionService,
            selectionWindowService,
            courseValidationService);

    assertThrows(AppException.class, () -> service.create(request, session));
    verify(studentService).lockForSelection("S1");
  }
}
