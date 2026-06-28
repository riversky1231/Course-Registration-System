package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 课程评价（评教）实体。学生在结课后对所修课程进行打分与评论。
 */
@TableName("tb_course_evaluation")
public class CourseEvaluation {

  @TableId(type = IdType.INPUT)
  private String id;

  @TableField("course_id")
  private String courseId;

  @TableField("student_id")
  private String studentId;

  @TableField("teacher_id")
  private String teacherId;

  @Min(value = 1, message = "评分不能低于1星")
  @Max(value = 5, message = "评分不能高于5星")
  private Integer rating;

  @Size(max = 500, message = "评价内容长度不能超过500字")
  private String comment;

  /** 是否匿名评价：匿名后教师视角看不到学生身份。 */
  private Boolean anonymous;

  @TableField("create_time")
  private LocalDateTime createTime;

  @TableField(exist = false)
  private String courseName;

  @TableField(exist = false)
  private String studentName;

  @TableField(exist = false)
  private String teacherName;

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

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(String teacherId) {
    this.teacherId = teacherId;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Boolean getAnonymous() {
    return anonymous;
  }

  public void setAnonymous(Boolean anonymous) {
    this.anonymous = anonymous;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public String getStudentName() {
    return studentName;
  }

  public void setStudentName(String studentName) {
    this.studentName = studentName;
  }

  public String getTeacherName() {
    return teacherName;
  }

  public void setTeacherName(String teacherName) {
    this.teacherName = teacherName;
  }
}
