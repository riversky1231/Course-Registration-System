package com.codeying.stuselect.dto;

import java.util.List;

/** AI 智能课程推荐结果。 */
public record AiRecommendation(
    String summary, double totalCredits, List<Item> items, String note, boolean configured) {

  /** 单条推荐课程。 */
  public record Item(String courseId, String courseName, double credit, String type, String reason) {}
}
