package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.model.Admin;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.model.Teacher;
import com.codeying.stuselect.service.ProfileService;
import com.codeying.stuselect.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

  private final ProfileService profileService;
  private final SessionService sessionService;
  private final ObjectMapper objectMapper;
  private final Validator validator;

  public ProfileController(
      ProfileService profileService,
      SessionService sessionService,
      ObjectMapper objectMapper,
      Validator validator) {
    this.profileService = profileService;
    this.sessionService = sessionService;
    this.objectMapper = objectMapper;
    this.validator = validator;
  }

  @GetMapping
  public ApiResponse<Object> profile(HttpSession session) {
    return ApiResponse.success(profileService.profile(session));
  }

  @PutMapping
  public ApiResponse<Object> update(
      @RequestBody Map<String, Object> payload, HttpSession session) {
    Role role = sessionService.requireUser(session).getRole();
    Object request =
        switch (role) {
          case ADMIN -> objectMapper.convertValue(payload, Admin.class);
          case TEACHER -> objectMapper.convertValue(payload, Teacher.class);
          case STUDENT -> objectMapper.convertValue(payload, Student.class);
        };
    Set<ConstraintViolation<Object>> violations = validator.validate(request);
    if (!violations.isEmpty()) {
      throw new ConstraintViolationException(violations);
    }
    return ApiResponse.success(profileService.update(request, session));
  }
}
