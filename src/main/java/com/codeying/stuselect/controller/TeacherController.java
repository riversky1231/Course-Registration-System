package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.model.Teacher;
import jakarta.validation.Valid;
import com.codeying.stuselect.service.TeacherService;
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
@RequestMapping("/api/teachers")
public class TeacherController {

  private final TeacherService teacherService;

  public TeacherController(TeacherService teacherService) {
    this.teacherService = teacherService;
  }

  @GetMapping
  public ApiResponse<List<Teacher>> list(
      @RequestParam(required = false) String keyword, HttpSession session) {
    return ApiResponse.success(teacherService.list(keyword, session));
  }

  @PostMapping
  public ApiResponse<Teacher> create(@Valid @RequestBody Teacher teacher, HttpSession session) {
    return ApiResponse.success(teacherService.create(teacher, session));
  }

  @PutMapping("/{id}")
  public ApiResponse<Teacher> update(
      @PathVariable String id, @Valid @RequestBody Teacher teacher, HttpSession session) {
    return ApiResponse.success(teacherService.update(id, teacher, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    teacherService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }
}
