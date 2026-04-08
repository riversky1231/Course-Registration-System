package com.codeying.stuselect.common;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Arrays;

public enum Role {
  ADMIN("admin", "管理员"),
  TEACHER("teacher", "教师"),
  STUDENT("student", "学生");

  private final String code;
  private final String label;

  Role(String code, String label) {
    this.code = code;
    this.label = label;
  }

  @JsonValue
  public String getCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }

  public static Role from(String value) {
    if (!StringUtils.hasText(value)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "角色不能为空");
    }
    return Arrays.stream(values())
        .filter(role -> role.code.equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "不支持的角色类型"));
  }
}
