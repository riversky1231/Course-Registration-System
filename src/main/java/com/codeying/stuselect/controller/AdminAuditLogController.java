package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.model.AdminAuditLog;
import com.codeying.stuselect.service.AdminAuditLogService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin-audit-logs")
public class AdminAuditLogController {

  private final AdminAuditLogService adminAuditLogService;

  public AdminAuditLogController(AdminAuditLogService adminAuditLogService) {
    this.adminAuditLogService = adminAuditLogService;
  }

  @GetMapping
  public ApiResponse<PageResult<AdminAuditLog>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      HttpSession session) {
    return ApiResponse.success(adminAuditLogService.list(keyword, page, pageSize, session));
  }
}
