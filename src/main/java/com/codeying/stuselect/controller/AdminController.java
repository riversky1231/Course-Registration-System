package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.model.Admin;
import jakarta.validation.Valid;
import com.codeying.stuselect.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping
  public ApiResponse<List<Admin>> list(
      @RequestParam(required = false) String keyword, HttpSession session) {
    return ApiResponse.success(adminService.list(keyword, session));
  }

  @PostMapping
  public ApiResponse<Admin> create(@Valid @RequestBody Admin admin, HttpSession session) {
    return ApiResponse.success(adminService.create(admin, session));
  }

  @PutMapping("/{id}")
  public ApiResponse<Admin> update(
      @PathVariable String id, @Valid @RequestBody Admin admin, HttpSession session) {
    return ApiResponse.success(adminService.update(id, admin, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    adminService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }
}
