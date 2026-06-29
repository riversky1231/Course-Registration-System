package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.AdminMapper;
import com.codeying.stuselect.model.Admin;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

class AdminServiceTest {

  @Test
  void deleteRejectsCurrentAdmin() {
    AdminMapper adminMapper = mock(AdminMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(adminMapper.selectById("A1")).thenReturn(existingAdmin("A1"));

    AdminService service = new AdminService(adminMapper, sessionService, passwordService);

    assertThrows(AppException.class, () -> service.delete("A1", session));
    verify(adminMapper, never()).deleteById("A1");
  }

  @Test
  void deleteRejectsLastAdmin() {
    AdminMapper adminMapper = mock(AdminMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(adminMapper.selectById("A2")).thenReturn(existingAdmin("A2"));
    when(adminMapper.selectCount(null)).thenReturn(1L);

    AdminService service = new AdminService(adminMapper, sessionService, passwordService);

    assertThrows(AppException.class, () -> service.delete("A2", session));
    verify(adminMapper, never()).deleteById("A2");
  }

  @Test
  void deleteRemovesOtherAdminWhenAnotherAdminRemains() {
    AdminMapper adminMapper = mock(AdminMapper.class);
    SessionService sessionService = mock(SessionService.class);
    PasswordService passwordService = mock(PasswordService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN))
        .thenReturn(new UserSession("A1", "admin", Role.ADMIN, "Admin"));
    when(adminMapper.selectById("A2")).thenReturn(existingAdmin("A2"));
    when(adminMapper.selectCount(null)).thenReturn(2L);

    AdminService service = new AdminService(adminMapper, sessionService, passwordService);

    service.delete("A2", session);

    verify(adminMapper).deleteById("A2");
  }

  private Admin existingAdmin(String id) {
    Admin admin = new Admin();
    admin.setId(id);
    return admin;
  }
}
