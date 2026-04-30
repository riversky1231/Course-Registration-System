package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("tb_course_mutex")
public class CourseMutex {

  @TableId(type = IdType.INPUT)
  private String id;
  private String courseIdA;
  private String courseIdB;
  private String reason;

  @TableField(exist = false)
  private String courseNameA;
  @TableField(exist = false)
  private String courseNameB;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCourseIdA() {
    return courseIdA;
  }

  public void setCourseIdA(String courseIdA) {
    this.courseIdA = courseIdA;
  }

  public String getCourseIdB() {
    return courseIdB;
  }

  public void setCourseIdB(String courseIdB) {
    this.courseIdB = courseIdB;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getCourseNameA() {
    return courseNameA;
  }

  public void setCourseNameA(String courseNameA) {
    this.courseNameA = courseNameA;
  }

  public String getCourseNameB() {
    return courseNameB;
  }

  public void setCourseNameB(String courseNameB) {
    this.courseNameB = courseNameB;
  }
}
