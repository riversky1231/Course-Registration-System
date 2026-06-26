package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.StudentGradeReport;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SelectionServiceGradeReportJUnit4Test {

  @Test
  public void testGradeReportCalculatesStudentSummaryAndFiltersRows() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    HttpSession httpSession = mock(HttpSession.class);
    prepareStudentSelections(selectionMapper, httpSession, mixedGradeRecords());

    SelectionService service =
        new SelectionService(
            selectionMapper, null, null, new SessionService(), null, null);

    StudentGradeReport report = service.gradeReport("Java", 1, 5, httpSession);

    assertEquals(2.4, report.gpa(), 0.001);
    assertEquals(70.0, report.averageScore(), 0.001);
    assertEquals(3.0, report.earnedCredits(), 0.001);
    assertEquals(6.0, report.totalCredits(), 0.001);
    assertEquals(2, report.gradedCourses());
    assertEquals(1, report.pendingCourses());
    assertEquals(1, report.page().total());
    assertEquals("Java基础", report.page().items().get(0).getCourseName());
  }

  @Test
  public void testGradeReportFiltersByTeacherName() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    HttpSession httpSession = mock(HttpSession.class);
    prepareStudentSelections(selectionMapper, httpSession, mixedGradeRecords());

    SelectionService service =
        new SelectionService(
            selectionMapper, null, null, new SessionService(), null, null);

    StudentGradeReport report = service.gradeReport("李老师", 1, 5, httpSession);

    assertEquals(1, report.page().total());
    assertEquals("数据库原理", report.page().items().get(0).getCourseName());
  }

  @Test
  public void testGradeReportFiltersByTimeSlot() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    HttpSession httpSession = mock(HttpSession.class);
    prepareStudentSelections(selectionMapper, httpSession, mixedGradeRecords());

    SelectionService service =
        new SelectionService(
            selectionMapper, null, null, new SessionService(), null, null);

    StudentGradeReport report = service.gradeReport("周三", 1, 5, httpSession);

    assertEquals(1, report.page().total());
    assertEquals("艺术鉴赏", report.page().items().get(0).getCourseName());
  }

  @Test
  public void testGradeReportReturnsZeroGpaWhenNoGradedCredits() {
    SelectionMapper selectionMapper = mock(SelectionMapper.class);
    HttpSession httpSession = mock(HttpSession.class);
    prepareStudentSelections(
        selectionMapper,
        httpSession,
        List.of(record("艺术鉴赏", "王老师", "周三第5-6节", 1.0, false, null)));

    SelectionService service =
        new SelectionService(
            selectionMapper, null, null, new SessionService(), null, null);

    StudentGradeReport report = service.gradeReport("", null, null, httpSession);

    assertEquals(0.0, report.gpa(), 0.001);
    assertEquals(0.0, report.averageScore(), 0.001);
    assertEquals(0.0, report.earnedCredits(), 0.001);
    assertEquals(1.0, report.totalCredits(), 0.001);
    assertEquals(0, report.gradedCourses());
    assertEquals(1, report.pendingCourses());
    assertEquals(1, report.page().total());
  }

  private void prepareStudentSelections(
      final SelectionMapper selectionMapper,
      final HttpSession httpSession,
      final List<SelectionRecord> records) {
    when(httpSession.getAttribute(SessionService.LOGIN_USER))
        .thenReturn(new UserSession("S3001", "stu_chen", Role.STUDENT, "陈知远"));
    when(selectionMapper.selectJoinedList(null, "S3001", null)).thenReturn(records);
  }

  private List<SelectionRecord> mixedGradeRecords() {
    return List.of(
        record("Java基础", "张老师", "周一第1-2节", 3.0, true, 90.0),
        record("数据库原理", "李老师", "周二第3-4节", 2.0, true, 50.0),
        record("艺术鉴赏", "王老师", "周三第5-6节", 1.0, false, null));
  }

  private SelectionRecord record(
      final String courseName,
      final String teacherName,
      final String timeSlot,
      final Double courseCredit,
      final Boolean graded,
      final Double score) {
    SelectionRecord record = new SelectionRecord();
    record.setCourseName(courseName);
    record.setTeacherName(teacherName);
    record.setTimeSlot(timeSlot);
    record.setCourseCredit(courseCredit);
    record.setGraded(graded);
    record.setScore(score);
    return record;
  }
}
