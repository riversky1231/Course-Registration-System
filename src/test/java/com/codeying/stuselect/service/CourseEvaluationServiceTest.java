package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.mapper.CourseEvaluationMapper;
import com.codeying.stuselect.mapper.SelectionMapper;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.CourseEvaluation;
import com.codeying.stuselect.model.SelectionRecord;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseEvaluationServiceTest {

  private CourseEvaluationMapper evaluationMapper;
  private SelectionMapper selectionMapper;
  private CourseService courseService;
  private SessionService sessionService;
  private CourseEvaluationService service;
  private HttpSession session;

  @BeforeEach
  void setUp() {
    evaluationMapper = mock(CourseEvaluationMapper.class);
    selectionMapper = mock(SelectionMapper.class);
    courseService = mock(CourseService.class);
    sessionService = mock(SessionService.class);
    session = mock(HttpSession.class);
    service =
        new CourseEvaluationService(evaluationMapper, selectionMapper, courseService, sessionService);

    UserSession student = new UserSession("S1", "stu", Role.STUDENT, "学生一");
    when(sessionService.requireRole(any(HttpSession.class), any(Role.class), any(Role.class)))
        .thenReturn(student);
    Course course = new Course();
    course.setId("C1");
    course.setName("Java基础");
    course.setTid("T1");
    when(courseService.require("C1")).thenReturn(course);
  }

  @Test
  void createRejectsWhenStudentNeverTookCourse() {
    when(selectionMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(null);

    AppException ex = assertThrows(AppException.class, () -> service.create(request(5), session));
    assertEquals("只能评价自己选修过的课程", ex.getMessage());
    verify(evaluationMapper, never()).insert(any(CourseEvaluation.class));
  }

  @Test
  void createRejectsWhenCourseNotGraded() {
    when(selectionMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(selection(false));

    AppException ex = assertThrows(AppException.class, () -> service.create(request(5), session));
    assertEquals("该课程成绩尚未录入，结课后才能评价", ex.getMessage());
    verify(evaluationMapper, never()).insert(any(CourseEvaluation.class));
  }

  @Test
  void createRejectsDuplicateEvaluation() {
    when(selectionMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(selection(true));
    when(evaluationMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(new CourseEvaluation());

    AppException ex = assertThrows(AppException.class, () -> service.create(request(5), session));
    assertEquals("你已评价过该课程，不能重复评价", ex.getMessage());
    verify(evaluationMapper, never()).insert(any(CourseEvaluation.class));
  }

  @Test
  void createRejectsInvalidRating() {
    when(selectionMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(selection(true));

    assertThrows(AppException.class, () -> service.create(request(6), session));
    verify(evaluationMapper, never()).insert(any(CourseEvaluation.class));
  }

  @Test
  void createPersistsEvaluationWithCourseTeacher() {
    when(selectionMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(selection(true));
    when(evaluationMapper.selectByCourseAndStudent("C1", "S1")).thenReturn(null);
    when(evaluationMapper.selectJoinedById(any())).thenReturn(new CourseEvaluation());

    assertDoesNotThrow(() -> service.create(request(5), session));

    org.mockito.ArgumentCaptor<CourseEvaluation> captor =
        org.mockito.ArgumentCaptor.forClass(CourseEvaluation.class);
    verify(evaluationMapper).insert(captor.capture());
    CourseEvaluation persisted = captor.getValue();
    assertEquals("S1", persisted.getStudentId());
    assertEquals("T1", persisted.getTeacherId());
    assertEquals(5, persisted.getRating());
  }

  private CourseEvaluation request(int rating) {
    CourseEvaluation request = new CourseEvaluation();
    request.setCourseId("C1");
    request.setRating(rating);
    request.setComment("不错");
    return request;
  }

  private SelectionRecord selection(boolean graded) {
    SelectionRecord record = new SelectionRecord();
    record.setId("R1");
    record.setCourseId("C1");
    record.setStudentId("S1");
    record.setGraded(graded);
    return record;
  }
}
