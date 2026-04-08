package com.codeying.stuselect.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.IdGenerator;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.mapper.AdminMapper;
import com.codeying.stuselect.model.Admin;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminService {

  private final AdminMapper adminMapper;
  private final SessionService sessionService;

  public AdminService(AdminMapper adminMapper, SessionService sessionService) {
    this.adminMapper = adminMapper;
    this.sessionService = sessionService;
  }

  public List<Admin> list(String keyword, HttpSession session) {
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
    return adminMapper.selectList(wrapper);
  }

  public Admin create(Admin admin, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    if (!StringUtils.hasText(admin.getUsername()) || !StringUtils.hasText(admin.getPassword())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "用户名和密码不能为空");
    }
    if (findByUsername(admin.getUsername()) != null) {
      throw new AppException(HttpStatus.BAD_REQUEST, "管理员账号已存在");
    }
    admin.setId(IdGenerator.newId());
    adminMapper.insert(admin);
    return adminMapper.selectById(admin.getId());
  }

  public Admin update(String id, Admin admin, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    Admin current = require(id);
    current.setUsername(defaultValue(admin.getUsername(), current.getUsername()));
    current.setPassword(defaultValue(admin.getPassword(), current.getPassword()));
    current.setName(admin.getName());
    current.setTele(admin.getTele());
    adminMapper.updateById(current);
    return adminMapper.selectById(id);
  }

  public void delete(String id, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN);
    require(id);
    adminMapper.deleteById(id);
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
}
