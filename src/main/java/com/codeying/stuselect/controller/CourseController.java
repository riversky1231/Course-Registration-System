package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.dto.CourseListCriteria;
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

@RestController
@RequestMapping("/api/courses")
public class CourseController {

  private final CourseService courseService;

  public CourseController(CourseService courseService) {
    this.courseService = courseService;
  }

  @GetMapping
  public ApiResponse<PageResult<Course>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String dept,
      @RequestParam(required = false) String teacherId,
      @RequestParam(required = false) Double minScore,
      @RequestParam(required = false) Double maxScore,
      @RequestParam(required = false) Boolean onlyAvailable,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      HttpSession session) {
    return ApiResponse.success(
        courseService.list(
            new CourseListCriteria(
                keyword,
                dept,
                teacherId,
                minScore,
                maxScore,
                onlyAvailable,
                page,
                pageSize),
            session));
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
