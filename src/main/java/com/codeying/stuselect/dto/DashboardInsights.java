package com.codeying.stuselect.dto;

import com.codeying.stuselect.model.SelectionRecord;

import java.util.List;

public record DashboardInsights(
    String role,
    String roleLabel,
    String displayName,
    long admins,
    long teachers,
    long students,
    long courses,
    long selections,
    double averageScore,
    double averageCredits,
    long pendingGrades,
    long unassignedCourses,
    List<SelectionRecord> recentSelections,
    List<InsightCard> courseSpotlights,
    List<InsightCard> peopleSpotlights,
    List<InsightCard> departmentSpotlights,
    List<String> notices) {

  public record InsightCard(String title, String subtitle, String metric, String detail) {}
}
