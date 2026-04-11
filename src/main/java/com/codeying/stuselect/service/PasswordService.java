package com.codeying.stuselect.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PasswordService {

  private final PasswordEncoder passwordEncoder;

  public PasswordService(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  public String encode(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
  }

  public boolean matches(String rawPassword, String storedPassword) {
    if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
      return false;
    }
    return isEncoded(storedPassword)
        ? passwordEncoder.matches(rawPassword, storedPassword)
        : storedPassword.equals(rawPassword);
  }

  public String encodeIfProvided(String rawPassword, String fallback) {
    return StringUtils.hasText(rawPassword) ? encode(rawPassword) : fallback;
  }

  public boolean needsMigration(String storedPassword) {
    return StringUtils.hasText(storedPassword) && !isEncoded(storedPassword);
  }

  private boolean isEncoded(String value) {
    return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
  }
}
