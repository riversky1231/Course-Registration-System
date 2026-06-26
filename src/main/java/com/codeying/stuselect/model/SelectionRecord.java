package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@TableName("tb_sct")
public class SelectionRecord {

  @TableId(type = IdType.INPUT)
  private String id;
  @Size(max = 32, message = "课程编号长度不能超过32位")
  @TableField("courseid")
  private String courseId;
  @TableField(exist = false)
  private String courseName;
  @TableField(exist = false)
  private Double courseCredit;
  @TableField(exist = false)
  private String courseDept;
  @TableField(exist = false)
  private Integer maxStudents;
  @TableField(exist = false)
  private String timeSlot;
  @Size(max = 32, message = "学生编号长度不能超过32位")
  @TableField("studentId")
  private String studentId;
  @TableField(exist = false)
  private String studentName;
  @TableField(exist = false)
  private String studentNumb;
  @TableField(exist = false)
  private String studentDept;
  @TableField(exist = false)
  private String studentMajor;
  @TableField(exist = false)
  private String studentClass;
  @Size(max = 32, message = "教师编号长度不能超过32位")
  @TableField("teaid")
  private String teacherId;
  @TableField(exist = false)
  private String teacherName;
  private Boolean graded;
  @DecimalMin(value = "0.0", message = "成绩不能小于0")
  @DecimalMax(value = "100.0", message = "成绩不能大于100")
  private Double score;
  @TableField("createtime")
  private LocalDateTime createTime;

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

  public String getCourseName() {
    return courseName;
  }

  public void setCourseName(String courseName) {
    this.courseName = courseName;
  }

  public Double getCourseCredit() {
    return courseCredit;
  }

  public void setCourseCredit(Double courseCredit) {
    this.courseCredit = courseCredit;
  }

  public String getCourseDept() {
    return courseDept;
  }

  public void setCourseDept(String courseDept) {
    this.courseDept = courseDept;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getStudentName() {
    return studentName;
  }

  public void setStudentName(String studentName) {
    this.studentName = studentName;
  }

  public String getStudentNumb() {
    return studentNumb;
  }

  public void setStudentNumb(String studentNumb) {
    this.studentNumb = studentNumb;
  }

  public String getStudentDept() {
    return studentDept;
  }

  public void setStudentDept(String studentDept) {
    this.studentDept = studentDept;
  }

  public String getStudentMajor() {
    return studentMajor;
  }

  public void setStudentMajor(String studentMajor) {
    this.studentMajor = studentMajor;
  }

  public String getStudentClass() {
    return studentClass;
  }

  public void setStudentClass(String studentClass) {
    this.studentClass = studentClass;
  }

  public String getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(String teacherId) {
    this.teacherId = teacherId;
  }

  public String getTeacherName() {
    return teacherName;
  }

  public void setTeacherName(String teacherName) {
    this.teacherName = teacherName;
  }

  public Boolean getGraded() {
    return graded;
  }

  public void setGraded(Boolean graded) {
    this.graded = graded;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
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
