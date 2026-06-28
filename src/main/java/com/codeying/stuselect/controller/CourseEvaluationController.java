package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.dto.CourseEvaluationSummary;
import com.codeying.stuselect.model.CourseEvaluation;
import com.codeying.stuselect.service.CourseEvaluationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 课程评价（评教）接口：学生评价、教师/管理员查看与统计。 */
@RestController
@RequestMapping("/api/evaluations")
public class CourseEvaluationController {

  private final CourseEvaluationService evaluationService;

  public CourseEvaluationController(CourseEvaluationService evaluationService) {
    this.evaluationService = evaluationService;
  }

  @GetMapping
  public ApiResponse<PageResult<CourseEvaluation>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String courseId,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      HttpSession session) {
    return ApiResponse.success(
        evaluationService.list(keyword, courseId, page, pageSize, session));
  }

  @GetMapping("/summary")
  public ApiResponse<CourseEvaluationSummary> summary(
      @RequestParam String courseId, HttpSession session) {
    return ApiResponse.success(evaluationService.courseSummary(courseId, session));
  }

  @PostMapping
  public ApiResponse<CourseEvaluation> create(
      @Valid @RequestBody CourseEvaluation evaluation, HttpSession session) {
    return ApiResponse.success(evaluationService.create(evaluation, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    evaluationService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }
}
