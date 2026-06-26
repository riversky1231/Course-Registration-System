package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@TableName("tb_student")
public class Student {

  @TableId(type = IdType.INPUT)
  private String id;
  @Size(max = 20, message = "学生用户名长度不能超过20位")
  private String username;
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Size(max = 20, message = "学生密码长度不能超过20位")
  private String password;
  @Size(max = 32, message = "学号长度不能超过32位")
  private String numb;
  @Size(max = 255, message = "学生姓名长度不能超过255位")
  private String sname;
  @Size(max = 255, message = "学院长度不能超过255位")
  private String sdept;
  private LocalDateTime sbirthday;
  @Pattern(regexp = "^(|1\\d{10})$", message = "学生电话格式不正确")
  private String tele;
  @Email(message = "学生邮箱格式不正确")
  @Size(max = 255, message = "学生邮箱长度不能超过255位")
  private String email;
  @Pattern(regexp = "^(|男|女)$", message = "学生性别只能填写男或女")
  private String ssex;
  @Min(value = 0, message = "学生年龄不能小于0")
  @Max(value = 120, message = "学生年龄不能大于120")
  private Integer age;
  @Size(max = 255, message = "专业长度不能超过255位")
  private String smajor;
  @Size(max = 255, message = "班级长度不能超过255位")
  private String sclass;
  @Min(value = 1, message = "学生年级不能小于1")
  @Max(value = 8, message = "学生年级不能大于8")
  private Integer grade;
  @Min(value = 1900, message = "入学年份不能早于1900")
  @Max(value = 2100, message = "入学年份不能晚于2100")
  private Integer enrollmentYear;

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

  public String getSname() {
    return sname;
  }

  public void setSname(String sname) {
    this.sname = sname;
  }

  public String getSdept() {
    return sdept;
  }

  public void setSdept(String sdept) {
    this.sdept = sdept;
  }

  public LocalDateTime getSbirthday() {
    return sbirthday;
  }

  public void setSbirthday(LocalDateTime sbirthday) {
    this.sbirthday = sbirthday;
  }

  public String getTele() {
    return tele;
  }

  public void setTele(String tele) {
    this.tele = tele;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSsex() {
    return ssex;
  }

  public void setSsex(String ssex) {
    this.ssex = ssex;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getSmajor() {
    return smajor;
  }

  public void setSmajor(String smajor) {
    this.smajor = smajor;
  }

  public String getSclass() {
    return sclass;
  }

  public void setSclass(String sclass) {
    this.sclass = sclass;
  }

  public Integer getGrade() {
    return grade;
  }

  public void setGrade(Integer grade) {
    this.grade = grade;
  }

  public Integer getEnrollmentYear() {
    return enrollmentYear;
  }

  public void setEnrollmentYear(Integer enrollmentYear) {
    this.enrollmentYear = enrollmentYear;
  }
}
