package com.codeying.stuselect.common;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class CredentialRules {

  public static final String USERNAME_REGEX = "^[A-Za-z][A-Za-z0-9_]{3,19}$";
  public static final String PASSWORD_REGEX = "^[A-Za-z0-9_@#$%]{6,20}$";
  public static final String USERNAME_MESSAGE = "用户名必须为4-20位，以字母开头，且只能包含字母、数字或下划线";
  public static final String PASSWORD_MESSAGE = "密码必须为6-20位，且只能包含字母、数字、下划线和@#$%";

  private static final Pattern USERNAME_PATTERN = Pattern.compile(USERNAME_REGEX);
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

  private CredentialRules() {}

  public static void requireUsername(String username) {
    if (!StringUtils.hasText(username)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "用户名不能为空");
    }
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      throw new AppException(HttpStatus.BAD_REQUEST, USERNAME_MESSAGE);
    }
  }

  public static void requirePassword(String password) {
    if (!StringUtils.hasText(password)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "密码不能为空");
    }
    if (!PASSWORD_PATTERN.matcher(password).matches()) {
      throw new AppException(HttpStatus.BAD_REQUEST, PASSWORD_MESSAGE);
    }
  }

  public static void requirePasswordIfProvided(String password) {
    if (StringUtils.hasText(password)) {
      requirePassword(password);
    }
  }
}
