package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.AdminAuditLogMapper;
import com.codeying.stuselect.model.AdminAuditLog;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
@Service
public class AdminAuditLogService {

  private final AdminAuditLogMapper adminAuditLogMapper;
  private final SessionService sessionService;

  public AdminAuditLogService(
      AdminAuditLogMapper adminAuditLogMapper, SessionService sessionService) {
    this.adminAuditLogMapper = adminAuditLogMapper;
    this.sessionService = sessionService;
  }

  public PageResult<AdminAuditLog> list(
      String keyword, Integer page, Integer pageSize, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    LambdaQueryWrapper<AdminAuditLog> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(AdminAuditLog::getAdminUsername, keyword)
                  .or()
                  .like(AdminAuditLog::getAction, keyword)
                  .or()
                  .like(AdminAuditLog::getTargetType, keyword)
                  .or()
                  .like(AdminAuditLog::getTargetName, keyword)
                  .or()
                  .like(AdminAuditLog::getDetail, keyword));
    }
    wrapper.orderByDesc(AdminAuditLog::getCreateTime).orderByDesc(AdminAuditLog::getId);
    return PageResult.of(adminAuditLogMapper.selectList(wrapper), PageQuery.of(page, pageSize));
  }

  public long count(HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    return adminAuditLogMapper.selectCount(null);
  }

  public void record(
      UserSession actor,
      String action,
      String targetType,
      String targetId,
      String targetName,
      String detail) {
    if (actor == null || actor.getRole() != Role.ADMIN) {
      return;
    }
    AdminAuditLog log = new AdminAuditLog();
    log.setId(IdGenerator.newId());
    log.setAdminId(actor.getId());
    log.setAdminUsername(actor.getUsername());
    log.setAction(action);
    log.setTargetType(targetType);
    log.setTargetId(targetId);
    log.setTargetName(StringUtils.hasText(targetName) ? targetName : "-");
    log.setDetail(StringUtils.hasText(detail) ? detail : "-");
    log.setCreateTime(LocalDateTime.now());
    adminAuditLogMapper.insert(log);
  }
}
