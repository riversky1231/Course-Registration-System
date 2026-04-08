package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.AuthRequests;
import com.codeying.stuselect.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ApiResponse<UserSession> login(
      @Valid @RequestBody AuthRequests.LoginRequest request, HttpSession session) {
    return ApiResponse.success(authService.login(request, session));
  }

  @PostMapping("/register")
  public ApiResponse<Void> register(@Valid @RequestBody AuthRequests.RegisterRequest request) {
    authService.register(request);
    return ApiResponse.successMessage("注册成功，请登录");
  }

  @GetMapping("/me")
  public ApiResponse<UserSession> me(HttpSession session) {
    UserSession current = authService.current(session);
    if (current == null) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "未登录");
    }
    return ApiResponse.success(current);
  }

  @GetMapping("/status")
  public ApiResponse<UserSession> status(HttpSession session) {
    return ApiResponse.success(authService.current(session));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(HttpSession session) {
    authService.logout(session);
    return ApiResponse.successMessage("已退出登录");
  }
}
