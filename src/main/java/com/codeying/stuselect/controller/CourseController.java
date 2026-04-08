package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.model.Course;
import jakarta.validation.Valid;
import com.codeying.stuselect.service.CourseService;
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
@RequestMapping("/api/courses")
public class CourseController {

  private final CourseService courseService;

  public CourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  @GetMapping
  public ApiResponse<List<Course>> list(
      @RequestParam(required = false) String keyword, HttpSession session) {
    return ApiResponse.success(courseService.list(keyword, session));
  }

  @PostMapping
  public ApiResponse<Course> create(@Valid @RequestBody Course course, HttpSession session) {
    return ApiResponse.success(courseService.create(course, session));
  }

  @PutMapping("/{id}")
  public ApiResponse<Course> update(
      @PathVariable String id, @Valid @RequestBody Course course, HttpSession session) {
    return ApiResponse.success(courseService.update(id, course, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    courseService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }
}
