package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.CourseMapper;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CourseServiceTest {

  @Test
  void updatePersistsCourseTypeAndGradeLimit() {
    CourseMapper courseMapper = mock(CourseMapper.class);
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    SessionService sessionService = mock(SessionService.class);
    HttpSession session = mock(HttpSession.class);

    Course existing = new Course();
    existing.setId("C1");
    existing.setName("数据库");
    existing.setNumb("DB01");
    existing.setTid("T1");

    when(sessionService.requireRole(session, Role.ADMIN, Role.TEACHER))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(courseMapper.selectByIdWithTeacher("C1")).thenReturn(existing);

    Course input = new Course();
    input.setCourseType(" 专业必修 ");
    input.setGradeLimit(3);

    CourseService service = new CourseService(courseMapper, selectionMapper, sessionService);
    service.update("C1", input, session);

    ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
    verify(courseMapper).updateById(captor.capture());
    assertEquals("专业必修", captor.getValue().getCourseType());
    assertEquals(3, captor.getValue().getGradeLimit());
  }
}
