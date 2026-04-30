package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("tb_course_prerequisite")
public class CoursePrerequisite {

  @TableId(type = IdType.INPUT)
  private String id;
  private String courseId;
  private String prerequisiteCourseId;
  private Double minScore;

  @TableField(exist = false)
  private String courseName;
  @TableField(exist = false)
  private String prerequisiteCourseName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getCourseId() {
    return courseId;
  }

  public void setCourseId(String courseId) {
    this.courseId = courseId;
  }

  public String getPrerequisiteCourseId() {
    return prerequisiteCourseId;
  }

  public void setPrerequisiteCourseId(String prerequisiteCourseId) {
    this.prerequisiteCourseId = prerequisiteCourseId;
  }

  public Double getMinScore() {
    return minScore;
  }

  public void setMinScore(Double minScore) {
    this.minScore = minScore;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public String getPrerequisiteCourseName() {
    return prerequisiteCourseName;
  }

  public void setPrerequisiteCourseName(String prerequisiteCourseName) {
    this.prerequisiteCourseName = prerequisiteCourseName;
  }
}
