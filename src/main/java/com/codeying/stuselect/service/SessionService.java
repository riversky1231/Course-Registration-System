package com.codeying.stuselect.service;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class SessionService {

  public static final String LOGIN_USER = "LOGIN_USER";

  public UserSession current(HttpSession session) {
    if (session == null) {
      return null;
    }
    return (UserSession) session.getAttribute(LOGIN_USER);
  }

  public UserSession requireUser(HttpSession session) {
    if (session == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "会话无效，请重新登录");
    }
    UserSession user = current(session);
    if (user == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    return user;
  }

  public UserSession requireRole(HttpSession session, Role... roles) {
    UserSession user = requireUser(session);
    if (user.getRole() == null) {
      throw new AppException(HttpStatus.FORBIDDEN, "用户角色未设置，请联系管理员");
    }
    boolean allowed = Arrays.stream(roles).anyMatch(role -> role == user.getRole());
    if (!allowed) {
      throw new AppException(HttpStatus.FORBIDDEN, "当前角色无权限执行该操作");
    }
    return user;
  }

  /**
   * 保存用户会话（防止会话固定攻击）
   * 在用户登录时调用，会使旧会话失效并创建新会话
   */
  public void save(HttpServletRequest request, UserSession userSession) {
    // 获取旧会话并使其失效
    HttpSession oldSession = request.getSession(false);
    if (oldSession != null) {
      try {
        oldSession.invalidate();
      } catch (IllegalStateException e) {
        // 会话已失效，忽略
      }
    }

    // 创建新会话（防止会话固定攻击）
    HttpSession newSession = request.getSession(true);
    newSession.setAttribute(LOGIN_USER, userSession);
    newSession.setMaxInactiveInterval(12 * 60 * 60); // 12小时
  }

  public void clear(HttpSession session) {
    if (session != null) {
      try {
        session.invalidate(); // invalidate会自动清除所有属性
      } catch (IllegalStateException e) {
        // 会话已失效，忽略
      }
    }
  }
}
