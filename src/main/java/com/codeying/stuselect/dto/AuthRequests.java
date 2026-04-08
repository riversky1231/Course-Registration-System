package com.codeying.stuselect.dto;

import jakarta.validation.constraints.NotBlank;

public final class AuthRequests {

  private AuthRequests() {}

  public record LoginRequest(
      @NotBlank(message = "用户名不能为空") String username,
      @NotBlank(message = "密码不能为空") String password,
      @NotBlank(message = "角色不能为空") String role) {}

  public record RegisterRequest(
      @NotBlank(message = "用户名不能为空") String username,
      @NotBlank(message = "密码不能为空") String password,
      @NotBlank(message = "角色不能为空") String role) {}
}
