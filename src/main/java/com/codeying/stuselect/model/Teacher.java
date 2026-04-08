package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@TableName("tb_teacher")
public class Teacher {

  @TableId(type = IdType.INPUT)
  private String id;
  @Size(max = 20, message = "教师用户名长度不能超过20位")
  private String username;
  @Size(max = 20, message = "教师密码长度不能超过20位")
  private String password;
  @Size(max = 32, message = "教工号长度不能超过32位")
  private String numb;
  @Size(max = 18, message = "教师姓名长度不能超过18位")
  private String tname;
  private LocalDateTime tbirthday;
  @Size(max = 255, message = "职位长度不能超过255位")
  private String tposition;
  @Pattern(regexp = "^(|1\\d{10})$", message = "教师电话格式不正确")
  private String ttel;
  @Min(value = 0, message = "教师年龄不能小于0")
  @Max(value = 120, message = "教师年龄不能大于120")
  private Integer age;
  @Pattern(regexp = "^(|男|女)$", message = "教师性别只能填写男或女")
  private String gender;

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

  public String getNumb() {
    return numb;
  }

  public void setNumb(String numb) {
    this.numb = numb;
  }

  public String getTname() {
    return tname;
  }

  public void setTname(String tname) {
    this.tname = tname;
  }

  public LocalDateTime getTbirthday() {
    return tbirthday;
  }

  public void setTbirthday(LocalDateTime tbirthday) {
    this.tbirthday = tbirthday;
  }

  public String getTposition() {
    return tposition;
  }

  public void setTposition(String tposition) {
    this.tposition = tposition;
  }

  public String getTtel() {
    return ttel;
  }

  public void setTtel(String ttel) {
    this.ttel = ttel;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }
}
