package com.codeying.stuselect.service;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SessionService {

  public static final String LOGIN_USER = "LOGIN_USER";

  public UserSession current(HttpSession session) {
    return (UserSession) session.getAttribute(LOGIN_USER);
  }

  public UserSession requireUser(HttpSession session) {
    UserSession user = current(session);
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    return user;
  }

  public UserSession requireRole(HttpSession session, Role... roles) {
    UserSession user = requireUser(session);
    boolean allowed = Arrays.stream(roles).anyMatch(role -> role == user.getRole());
    if (!allowed) {
      throw new AppException(HttpStatus.FORBIDDEN, "当前角色无权限执行该操作");
    }
    return user;
  }

  public void save(HttpSession session, UserSession userSession) {
    session.setAttribute(LOGIN_USER, userSession);
  }

  public void clear(HttpSession session) {
    session.removeAttribute(LOGIN_USER);
    session.invalidate();
  }
}
