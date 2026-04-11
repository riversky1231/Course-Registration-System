package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@TableName("tb_course")
public class Course {

  @TableId(type = IdType.INPUT)
  private String id;
  @Size(max = 18, message = "课程名称长度不能超过18位")
  private String name;
  @DecimalMin(value = "0.0", message = "课程学分不能小于0")
  @DecimalMax(value = "100.0", message = "课程学分不能大于100")
  private Double score;
  @Size(max = 32, message = "课程编号长度不能超过32位")
  private String numb;
  @Size(max = 32, message = "教师编号长度不能超过32位")
  private String tid;
  @TableField(exist = false)
  private String teacherName;
  @Size(max = 255, message = "课程简介长度不能超过255位")
  private String jianjie;
  @Size(max = 255, message = "开课学院长度不能超过255位")
  private String dept;
  @Min(value = 0, message = "选课容量不能小于0")
  private Integer maxStudents = 0;
  @Size(max = 64, message = "上课时间段长度不能超过64位")
  private String timeSlot;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public String getNumb() {
    return numb;
  }

  public void setNumb(String numb) {
    this.numb = numb;
  }

  public String getTid() {
    return tid;
  }

  public void setTid(String tid) {
    this.tid = tid;
  }

  public String getTeacherName() {
    return teacherName;
  }

  public void setTeacherName(String teacherName) {
    this.teacherName = teacherName;
  }

  public String getJianjie() {
    return jianjie;
  }

  public void setJianjie(String jianjie) {
    this.jianjie = jianjie;
  }

  public String getDept() {
    return dept;
  }

  public void setDept(String dept) {
    this.dept = dept;
  }

  public Integer getMaxStudents() {
    return maxStudents;
  }

  public void setMaxStudents(Integer maxStudents) {
    this.maxStudents = maxStudents;
  }

  public String getTimeSlot() {
    return timeSlot;
  }

  public void setTimeSlot(String timeSlot) {
    this.timeSlot = timeSlot;
  }
}
