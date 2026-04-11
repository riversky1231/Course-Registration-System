package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@TableName("tb_admin")
public class Admin {

  @TableId(type = IdType.INPUT)
  private String id;
  @Size(max = 20, message = "管理员用户名长度不能超过20位")
  private String username;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Size(max = 20, message = "管理员密码长度不能超过20位")
  private String password;
  @Size(max = 18, message = "管理员姓名长度不能超过18位")
  private String name;
  @Pattern(regexp = "^(|1\\d{10})$", message = "管理员电话格式不正确")
  private String tele;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTele() {
    return tele;
  }

  public void setTele(String tele) {
    this.tele = tele;
  }
}
