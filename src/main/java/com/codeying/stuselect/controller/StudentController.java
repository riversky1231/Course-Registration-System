package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.model.Student;
import jakarta.validation.Valid;
import com.codeying.stuselect.service.StudentService;
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
@RequestMapping("/api/students")
public class StudentController {

  private final StudentService studentService;

  public StudentController(StudentService studentService) {
    this.studentService = studentService;
  }

  @GetMapping
  public ApiResponse<List<Student>> list(
      @RequestParam(required = false) String keyword, HttpSession session) {
    return ApiResponse.success(studentService.list(keyword, session));
  }

  @PostMapping
  public ApiResponse<Student> create(@Valid @RequestBody Student student, HttpSession session) {
    return ApiResponse.success(studentService.create(student, session));
  }

  @PutMapping("/{id}")
  public ApiResponse<Student> update(
      @PathVariable String id, @Valid @RequestBody Student student, HttpSession session) {
    return ApiResponse.success(studentService.update(id, student, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    studentService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }
}
