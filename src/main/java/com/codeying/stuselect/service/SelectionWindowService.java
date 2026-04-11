package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.SelectionWindowMapper;
import com.codeying.stuselect.model.SelectionWindow;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SelectionWindowService {

  private final SelectionWindowMapper selectionWindowMapper;
  private final SessionService sessionService;

  public SelectionWindowService(
      SelectionWindowMapper selectionWindowMapper, SessionService sessionService) {
    this.selectionWindowMapper = selectionWindowMapper;
    this.sessionService = sessionService;
  }

  public PageResult<SelectionWindow> list(
      String keyword, Integer page, Integer pageSize, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    LambdaQueryWrapper<SelectionWindow> wrapper = buildWrapper(keyword);
    List<SelectionWindow> windows = selectionWindowMapper.selectList(wrapper);
    windows.forEach(this::fillActiveFlag);
    return PageResult.of(windows, PageQuery.of(page, pageSize));
  }

  public SelectionWindow create(SelectionWindow input, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    validateWindow(input, null);
    SelectionWindow window = new SelectionWindow();
    window.setId(IdGenerator.newId());
    window.setActionType(normalizeActionType(input.getActionType()));
    window.setName(input.getName());
    window.setStartTime(input.getStartTime());
    window.setEndTime(input.getEndTime());
    window.setEnabled(Boolean.TRUE.equals(input.getEnabled()));
    window.setDescription(input.getDescription());
    selectionWindowMapper.insert(window);
    fillActiveFlag(window);
    return window;
  }

  public SelectionWindow update(String id, SelectionWindow input, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    SelectionWindow window = require(id);
    SelectionWindow merged = new SelectionWindow();
    merged.setId(window.getId());
    merged.setActionType(
        StringUtils.hasText(input.getActionType()) ? normalizeActionType(input.getActionType()) : window.getActionType());
    merged.setName(defaultValue(input.getName(), window.getName()));
    merged.setStartTime(input.getStartTime() == null ? window.getStartTime() : input.getStartTime());
    merged.setEndTime(input.getEndTime() == null ? window.getEndTime() : input.getEndTime());
    merged.setEnabled(input.getEnabled() == null ? window.getEnabled() : input.getEnabled());
    merged.setDescription(defaultValue(input.getDescription(), window.getDescription()));
    validateWindow(merged, id);
    selectionWindowMapper.updateById(merged);
    SelectionWindow refreshed = require(id);
    fillActiveFlag(refreshed);
    return refreshed;
  }

  public void delete(String id, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    selectionWindowMapper.deleteById(require(id).getId());
  }

  public long count(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    return current.getRole() == Role.ADMIN ? selectionWindowMapper.selectCount(null) : 0L;
  }

  public void requireOpen(String actionType, UserSession current) {
    if (current.getRole() != Role.STUDENT) {
      return;
    }
    String normalizedAction = normalizeActionType(actionType);
    SelectionWindow window =
        selectionWindowMapper.selectOne(
            new LambdaQueryWrapper<SelectionWindow>()
                .eq(SelectionWindow::getActionType, normalizedAction)
                .last("limit 1"));
    if (window == null || !Boolean.TRUE.equals(window.getEnabled())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST, "当前未开放" + actionLabel(normalizedAction) + "，请联系管理员");
    }
    LocalDateTime now = LocalDateTime.now();
    if (window.getStartTime() == null || window.getEndTime() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, actionLabel(normalizedAction) + "时间窗口尚未配置完整");
    }
    if (now.isBefore(window.getStartTime()) || now.isAfter(window.getEndTime())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          actionLabel(normalizedAction)
              + "仅在 "
              + window.getStartTime()
              + " 至 "
              + window.getEndTime()
              + " 开放");
    }
  }

  public SelectionWindow require(String id) {
    SelectionWindow window = selectionWindowMapper.selectById(id);
    if (window == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "时间窗口不存在");
    }
    return window;
  }

  private LambdaQueryWrapper<SelectionWindow> buildWrapper(String keyword) {
    LambdaQueryWrapper<SelectionWindow> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(SelectionWindow::getActionType, keyword)
                  .or()
                  .like(SelectionWindow::getName, keyword)
                  .or()
                  .like(SelectionWindow::getDescription, keyword));
    }
    wrapper.orderByAsc(SelectionWindow::getActionType).orderByAsc(SelectionWindow::getStartTime);
    return wrapper;
  }

  private void validateWindow(SelectionWindow input, String currentId) {
    String actionType = normalizeActionType(input.getActionType());
    if (!StringUtils.hasText(input.getName())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "窗口名称不能为空");
    }
    if (input.getStartTime() == null || input.getEndTime() == null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "开始时间和结束时间不能为空");
    }
    if (!input.getEndTime().isAfter(input.getStartTime())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
    }
    SelectionWindow duplicated =
        selectionWindowMapper.selectOne(
            new LambdaQueryWrapper<SelectionWindow>()
                .eq(SelectionWindow::getActionType, actionType)
                .last("limit 1"));
    if (duplicated != null && !duplicated.getId().equals(currentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "同一操作类型只能配置一个时间窗口");
    }
  }

  private void fillActiveFlag(SelectionWindow window) {
    if (window == null) {
      return;
    }
    LocalDateTime now = LocalDateTime.now();
    boolean active =
        Boolean.TRUE.equals(window.getEnabled())
            && window.getStartTime() != null
            && window.getEndTime() != null
            && !now.isBefore(window.getStartTime())
            && !now.isAfter(window.getEndTime());
    window.setActive(active);
  }

  private String normalizeActionType(String value) {
    if (!StringUtils.hasText(value)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "窗口类型不能为空");
    }
    String normalized = value.trim().toUpperCase();
    if (!"SELECT".equals(normalized) && !"DROP".equals(normalized)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "窗口类型只能是SELECT或DROP");
    }
    return normalized;
  }

  private String actionLabel(String actionType) {
    return "DROP".equals(actionType) ? "退课" : "选课";
  }

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }
}
