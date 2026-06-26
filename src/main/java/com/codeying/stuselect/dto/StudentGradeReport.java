package com.codeying.stuselect.dto;

import com.codeying.stuselect.common.PageResult;
import com.codeying.stuselect.model.SelectionRecord;

public record StudentGradeReport(
    double gpa,
    double averageScore,
    double earnedCredits,
    double totalCredits,
    long gradedCourses,
    long pendingCourses,
    long failedCourses,
    PageResult<SelectionRecord> page) {}
