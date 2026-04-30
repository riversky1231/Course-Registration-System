package com.codeying.stuselect.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("tb_semester_credit_limit")
public class SemesterCreditLimit {

  @TableId(type = IdType.INPUT)
  private String id;
  private Double minGpa;
  private Double maxGpa;
  private Double maxCredits;
  private String description;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Double getMinGpa() {
    return minGpa;
  }

  public void setMinGpa(Double minGpa) {
    this.minGpa = minGpa;
  }

  public Double getMaxGpa() {
    return maxGpa;
  }

  public void setMaxGpa(Double maxGpa) {
    this.maxGpa = maxGpa;
  }

  public Double getMaxCredits() {
    return maxCredits;
  }

  public void setMaxCredits(Double maxCredits) {
    this.maxCredits = maxCredits;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
