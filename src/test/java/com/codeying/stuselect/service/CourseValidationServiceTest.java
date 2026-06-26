package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.mapper.CoursePrerequisiteMapper;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.mapper.StudentMapper;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.CoursePrerequisite;
import com.codeying.stuselect.model.SelectionRecord;
import com.codeying.stuselect.model.Student;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseValidationServiceTest {

  @Test
  void validateGradeLimitRejectsStudentWithoutGrade() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    Student student = new Student();
    student.setId("S1");
    when(studentMapper.selectById("S1")).thenReturn(student);
    CourseValidationService service =
        new CourseValidationService(null, null, null, null, null, studentMapper, null);

    Course course = new Course();
    course.setGradeLimit(2);

    assertThrows(AppException.class, () -> service.validateGradeLimit(course, "S1"));
  }

  @Test
  void validateGradeLimitAllowsStudentAtRequiredGrade() {
    StudentMapper studentMapper = mock(StudentMapper.class);
    Student student = new Student();
    student.setId("S1");
    student.setGrade(2);
    when(studentMapper.selectById("S1")).thenReturn(student);
    CourseValidationService service =
        new CourseValidationService(null, null, null, null, null, studentMapper, null);

    Course course = new Course();
    course.setGradeLimit(2);

    assertDoesNotThrow(() -> service.validateGradeLimit(course, "S1"));
  }

  @Test
  void validatePrerequisitesAllowsSharedPrerequisiteAcrossBranches() {
    CoursePrerequisiteMapper prerequisiteMapper = mock(CoursePrerequisiteMapper.class);
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    when(prerequisiteMapper.selectWithNamesByCourseId("A"))
        .thenReturn(List.of(prereq("A", "B"), prereq("A", "C")));
    when(prerequisiteMapper.selectWithNamesByCourseId("B"))
        .thenReturn(List.of(prereq("B", "D")));
    when(prerequisiteMapper.selectWithNamesByCourseId("C"))
        .thenReturn(List.of(prereq("C", "D")));
    when(prerequisiteMapper.selectWithNamesByCourseId("D")).thenReturn(List.of());
    when(selectionMapper.selectOne(any())).thenReturn(completedSelection());

    CourseValidationService service =
        new CourseValidationService(prerequisiteMapper, null, null, null, selectionMapper, null, null);

    assertDoesNotThrow(() -> service.validatePrerequisites("A", "S1"));
  }

  @Test
  void validatePrerequisitesRejectsRealCycleOnCurrentPath() {
    CoursePrerequisiteMapper prerequisiteMapper = mock(CoursePrerequisiteMapper.class);
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    when(prerequisiteMapper.selectWithNamesByCourseId("A"))
        .thenReturn(List.of(prereq("A", "B")));
    when(prerequisiteMapper.selectWithNamesByCourseId("B"))
        .thenReturn(List.of(prereq("B", "A")));
    when(selectionMapper.selectOne(any())).thenReturn(completedSelection());

    CourseValidationService service =
        new CourseValidationService(prerequisiteMapper, null, null, null, selectionMapper, null, null);

    assertThrows(AppException.class, () -> service.validatePrerequisites("A", "S1"));
  }

  private CoursePrerequisite prereq(String courseId, String prerequisiteCourseId) {
    CoursePrerequisite prerequisite = new CoursePrerequisite();
    prerequisite.setCourseId(courseId);
    prerequisite.setPrerequisiteCourseId(prerequisiteCourseId);
    prerequisite.setPrerequisiteCourseName(prerequisiteCourseId);
    prerequisite.setMinScore(60.0);
    return prerequisite;
  }

  private SelectionRecord completedSelection() {
    SelectionRecord record = new SelectionRecord();
    record.setGraded(true);
    record.setScore(90.0);
    return record;
  }
}
