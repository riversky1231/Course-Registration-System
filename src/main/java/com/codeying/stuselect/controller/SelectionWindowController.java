package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.model.SelectionWindow;
import com.codeying.stuselect.service.SelectionWindowService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
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
@RequestMapping("/api/selection-windows")
public class SelectionWindowController {

  private final SelectionWindowService selectionWindowService;

  public SelectionWindowController(SelectionWindowService selectionWindowService) {
    this.selectionWindowService = selectionWindowService;
  }

  @GetMapping
  public ApiResponse<PageResult<SelectionWindow>> list(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize,
      HttpSession session) {
    return ApiResponse.success(selectionWindowService.list(keyword, page, pageSize, session));
  }

  @PostMapping
  public ApiResponse<SelectionWindow> create(
      @Valid @RequestBody SelectionWindow selectionWindow, HttpSession session) {
    return ApiResponse.success(selectionWindowService.create(selectionWindow, session));
  }

  @PutMapping("/{id}")
  public ApiResponse<SelectionWindow> update(
      @PathVariable String id,
      @Valid @RequestBody SelectionWindow selectionWindow,
      HttpSession session) {
    return ApiResponse.success(selectionWindowService.update(id, selectionWindow, session));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable String id, HttpSession session) {
    selectionWindowService.delete(id, session);
    return ApiResponse.successMessage("删除成功");
  }
}
