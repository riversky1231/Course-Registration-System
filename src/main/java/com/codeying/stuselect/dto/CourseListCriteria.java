package com.codeying.stuselect.dto;

/**
 * Course list filters submitted by the course query endpoint.
 *
 * @param keyword search keyword
 * @param dept department filter
 * @param teacherId teacher filter
 * @param minScore minimum credit filter
 * @param maxScore maximum credit filter
 * @param onlyAvailable whether to show only available courses
 * @param page requested page
 * @param pageSize requested page size
 */
public record CourseListCriteria(
    String keyword,
    String dept,
    String teacherId,
    Double minScore,
    Double maxScore,
    Boolean onlyAvailable,
    Integer page,
    Integer pageSize) { }
