package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.AdminMapper;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.mapper.TeacherMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

class SessionServiceTest {

  @Test
  void currentClearsSessionWhenAccountNoLongerExists() {
    AdminMapper adminMapper = mock(AdminMapper.class);
    TeacherMapper teacherMapper = mock(TeacherMapper.class);
    StudentMapper studentMapper = mock(StudentMapper.class);
    HttpSession session = mock(HttpSession.class);
    UserSession user = new UserSession("A1", "admin", Role.ADMIN, "Admin");

    when(session.getAttribute(SessionService.LOGIN_USER)).thenReturn(user);
    when(adminMapper.selectById("A1")).thenReturn(null);

    SessionService service = new SessionService(adminMapper, teacherMapper, studentMapper);

    assertNull(service.current(session));
    verify(session).invalidate();
  }
}
