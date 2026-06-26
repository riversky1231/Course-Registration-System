package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.validation.Valid;
import com.codeying.stuselect.service.SelectionService;
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

@RestController
@RequestMapping("/api/selections")
public class SelectionController {

  private final SelectionService selectionService;

  public SelectionController(SelectionService selectionService) {
    this.selectionService = selectionService;
  }

  @GetMapping
  public ApiResponse<PageResult<SelectionRecord>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      HttpSession session) {
    return ApiResponse.success(selectionService.list(keyword, page, pageSize, session));
  }

  @PostMapping
  public ApiResponse<SelectionRecord> create(
      @Valid @RequestBody SelectionRecord record, HttpSession session) {
    return ApiResponse.success(selectionService.create(record, session));
  }

  @PutMapping("/{id}")
  public ApiResponse<SelectionRecord> update(
      @PathVariable String id, @Valid @RequestBody SelectionRecord record, HttpSession session) {
    return ApiResponse.success(selectionService.update(id, record, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    selectionService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }

  @GetMapping("/course/{courseId}/students")
  public ApiResponse<java.util.List<SelectionRecord>> listStudentsByCourse(
      @PathVariable String courseId, HttpSession session) {
    return ApiResponse.success(selectionService.listStudentsByCourse(courseId, session));
  }
}
