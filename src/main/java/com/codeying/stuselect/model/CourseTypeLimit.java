package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("tb_course_type_limit")
public class CourseTypeLimit {

  @TableId(type = IdType.INPUT)
  private String id;
  private String courseType;
  private Integer maxCourses;
  private String description;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCourseType() {
    return courseType;
  }

  public void setCourseType(String courseType) {
    this.courseType = courseType;
  }

  public Integer getMaxCourses() {
    return maxCourses;
  }

  public void setMaxCourses(Integer maxCourses) {
    this.maxCourses = maxCourses;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
