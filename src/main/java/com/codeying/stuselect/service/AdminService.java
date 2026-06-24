package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.CredentialRules;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.PageQuery;
import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.AdminMapper;
import com.codeying.stuselect.model.Admin;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminService {

  private final AdminMapper adminMapper;
  private final SessionService sessionService;
  private final PasswordService passwordService;
  private final AdminAuditLogService adminAuditLogService;

  public AdminService(
      AdminMapper adminMapper,
      SessionService sessionService,
      PasswordService passwordService,
      AdminAuditLogService adminAuditLogService) {
    this.adminMapper = adminMapper;
    this.sessionService = sessionService;
    this.passwordService = passwordService;
    this.adminAuditLogService = adminAuditLogService;
  }

  public PageResult<Admin> list(String keyword, Integer page, Integer pageSize, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(keyword)) {
      wrapper.and(
          w ->
              w.like(Admin::getUsername, keyword)
                  .or()
                  .like(Admin::getName, keyword)
                  .or()
                  .like(Admin::getTele, keyword));
    }
    wrapper.orderByAsc(Admin::getUsername);
    return PageResult.of(adminMapper.selectList(wrapper), PageQuery.of(page, pageSize));
  }

  public List<Admin> listAll(HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    return adminMapper.selectList(new LambdaQueryWrapper<Admin>().orderByAsc(Admin::getUsername));
  }

  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public Admin create(Admin admin, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN);
    CredentialRules.requirePassword(admin.getPassword());
    ensureUsernameAvailable(admin.getUsername(), null);
    admin.setId(IdGenerator.newId());
    admin.setPassword(passwordService.encode(admin.getPassword()));
    adminMapper.insert(admin);
    adminAuditLogService.record(
        current, "新增", "管理员", admin.getId(), displayName(admin), "账号：" + admin.getUsername());
    return adminMapper.selectById(admin.getId());
  }

  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public Admin update(String id, Admin admin, HttpSession session) {
    UserSession actor = sessionService.requireRole(session, Role.ADMIN);
    Admin target = require(id);
    String nextUsername = defaultValue(admin.getUsername(), target.getUsername());
    CredentialRules.requirePasswordIfProvided(admin.getPassword());
    ensureUsernameAvailable(nextUsername, target.getId());
    target.setUsername(nextUsername);
    target.setPassword(passwordService.encodeIfProvided(admin.getPassword(), target.getPassword()));
    target.setName(admin.getName());
    target.setTele(admin.getTele());
    adminMapper.updateById(target);
    adminAuditLogService.record(
        actor,
        "编辑",
        "管理员",
        target.getId(),
        displayName(target),
        "账号：" + target.getUsername() + "，电话：" + defaultValue(target.getTele(), "-"));
    return adminMapper.selectById(id);
  }

  public Admin getProfile(HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN);
    return require(current.getId());
  }

  public Admin updateProfile(Admin input, HttpSession session) {
    UserSession actor = sessionService.requireRole(session, Role.ADMIN);
    Admin target = require(actor.getId());
    String nextUsername = defaultValue(input.getUsername(), target.getUsername());
    CredentialRules.requirePasswordIfProvided(input.getPassword());
    ensureUsernameAvailable(nextUsername, target.getId());
    target.setUsername(nextUsername);
    target.setPassword(passwordService.encodeIfProvided(input.getPassword(), target.getPassword()));
    target.setName(input.getName());
    target.setTele(input.getTele());
    adminMapper.updateById(target);
    return adminMapper.selectById(target.getId());
  }

  @CacheEvict(cacheNames = {"dashboardSummary", "dashboardInsights"}, allEntries = true)
  public void delete(String id, HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.ADMIN);
    Admin target = require(id);
    adminMapper.deleteById(id);
    adminAuditLogService.record(
        current, "删除", "管理员", target.getId(), displayName(target), "账号：" + target.getUsername());
  }

  public long count(HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    return adminMapper.selectCount(null);
  }

  public Admin findByUsername(String username) {
    return adminMapper.selectOne(
        new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username).last("limit 1"));
  }

  private Admin require(String id) {
    Admin admin = adminMapper.selectById(id);
    if (admin == null) {
      throw new AppException(HttpStatus.NOT_FOUND, "管理员不存在");
    }
    return admin;
  }

  private String defaultValue(String input, String fallback) {
    return StringUtils.hasText(input) ? input : fallback;
  }

  private void ensureUsernameAvailable(String username, String currentId) {
    CredentialRules.requireUsername(username);
    Admin existed = findByUsername(username);
    if (existed != null && !existed.getId().equals(currentId)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "管理员账号已存在");
    }
  }

  private String displayName(Admin admin) {
    return StringUtils.hasText(admin.getName()) ? admin.getName() : admin.getUsername();
  }
}
