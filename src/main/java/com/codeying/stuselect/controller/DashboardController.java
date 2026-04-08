package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.dto.DashboardInsights;
import com.codeying.stuselect.service.DashboardService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

  private final DashboardService dashboardService;

  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @GetMapping("/summary")
  public ApiResponse<Map<String, Object>> summary(HttpSession session) {
    return ApiResponse.success(dashboardService.summary(session));
  }

  @GetMapping("/insights")
  public ApiResponse<DashboardInsights> insights(HttpSession session) {
    return ApiResponse.success(dashboardService.insights(session));
  }
}
