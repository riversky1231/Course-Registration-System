package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.dto.StudentGradeReport;
import com.codeying.stuselect.service.SelectionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-grade-report")
public class StudentGradeReportController {

  private final SelectionService selectionService;

  public StudentGradeReportController(SelectionService selectionService) {
    this.selectionService = selectionService;
  }

  @GetMapping
  public ApiResponse<StudentGradeReport> report(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      HttpSession session) {
    return ApiResponse.success(selectionService.gradeReport(keyword, page, pageSize, session));
  }
}
