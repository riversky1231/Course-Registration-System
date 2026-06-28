package com.codeying.stuselect.dto;

import java.util.Map;

/**
 * 课程评价聚合统计：平均分、评价总数与星级分布。
 *
 * @param courseId 课程ID
 * @param courseName 课程名称
 * @param totalCount 评价总数
 * @param averageRating 平均评分（保留两位小数）
 * @param ratingDistribution 星级分布，key 为 1~5，value 为该星级数量
 */
public record CourseEvaluationSummary(
    String courseId,
    String courseName,
    long totalCount,
    double averageRating,
    Map<Integer, Long> ratingDistribution) {}
