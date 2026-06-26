package com.codeying.stuselect.service;

import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.DashboardInsights;
import com.codeying.stuselect.model.Admin;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.SelectionRecord;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.model.Teacher;
import jakarta.servlet.http.HttpSession;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DashboardService {

  private final SessionService sessionService;
  private final AdminService adminService;
  private final TeacherService teacherService;
  private final StudentService studentService;
  private final CourseService courseService;
  private final SelectionService selectionService;
  private final SelectionWindowService selectionWindowService;

  public DashboardService(
      SessionService sessionService,
      AdminService adminService,
      TeacherService teacherService,
      StudentService studentService,
      CourseService courseService,
      SelectionService selectionService,
      SelectionWindowService selectionWindowService) {
    this.sessionService = sessionService;
    this.adminService = adminService;
    this.teacherService = teacherService;
    this.studentService = studentService;
    this.courseService = courseService;
    this.selectionService = selectionService;
    this.selectionWindowService = selectionWindowService;
  }

  @Cacheable(cacheNames = "dashboardSummary", keyGenerator = "userSessionKeyGenerator")
  public Map<String, Object> summary(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("role", current.getRole().getCode());
    result.put("displayName", current.getDisplayName());
    result.put("admins", current.getRole() == Role.ADMIN ? adminService.count(session) : 0L);
    result.put("teachers", teacherService.count(session));
    result.put("students", studentService.count(session));
    result.put("courses", courseService.count(session));
    result.put("selections", selectionService.count(session));
    result.put(
        "selectionWindows", current.getRole() == Role.ADMIN ? selectionWindowService.count(session) : 0L);
    return result;
  }

  @Cacheable(cacheNames = "dashboardInsights", keyGenerator = "userSessionKeyGenerator")
  public DashboardInsights insights(HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    List<Admin> admins =
        current.getRole() == Role.ADMIN ? adminService.listAll(session) : List.of();
    List<Teacher> teachers = teacherService.listAllVisible(session);
    List<Student> students = studentService.listAllVisible(session);
    List<Course> courses = courseService.listAllVisible(session);
    List<SelectionRecord> selections = selectionService.listAllVisible(session);

    double averageScore =
        round(
            selections.stream()
                .filter(record -> Boolean.TRUE.equals(record.getGraded()))
                .map(SelectionRecord::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0D));
    double averageCredits =
        round(
            courses.stream()
                .map(Course::getScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0D));
    long pendingGrades =
        selections.stream().filter(record -> !Boolean.TRUE.equals(record.getGraded())).count();
    long unassignedCourses = courses.stream().filter(course -> course.getTid() == null || course.getTid().isBlank()).count();

    return new DashboardInsights(
        current.getRole().getCode(),
        current.getRole().getLabel(),
        current.getDisplayName(),
        current.getRole() == Role.ADMIN ? admins.size() : 0L,
        teachers.size(),
        students.size(),
        courses.size(),
        selections.size(),
        averageScore,
        averageCredits,
        pendingGrades,
        unassignedCourses,
        selections.stream()
            .sorted(Comparator.comparing(SelectionRecord::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(6)
            .toList(),
        buildCourseSpotlights(courses, selections),
        buildPeopleSpotlights(current.getRole(), courses, selections, students),
        buildDepartmentSpotlights(courses),
        buildNotices(current, courses.size(), selections.size(), pendingGrades, unassignedCourses));
  }

  private List<DashboardInsights.InsightCard> buildCourseSpotlights(
      List<Course> courses, List<SelectionRecord> selections) {
    Map<String, List<SelectionRecord>> selectionByCourse =
        selections.stream().collect(Collectors.groupingBy(SelectionRecord::getCourseId));
    return courses.stream()
        .sorted(
            Comparator.<Course>comparingInt(
                    course -> selectionByCourse.getOrDefault(course.getId(), List.of()).size())
                .reversed()
                .thenComparing(Course::getName, Comparator.nullsLast(String::compareTo)))
        .limit(6)
        .map(
            course -> {
              List<SelectionRecord> matched = selectionByCourse.getOrDefault(course.getId(), List.of());
              double average =
                  round(
                      matched.stream()
                          .filter(record -> Boolean.TRUE.equals(record.getGraded()))
                          .map(SelectionRecord::getScore)
                          .filter(Objects::nonNull)
                          .mapToDouble(Double::doubleValue)
                          .average()
                          .orElse(0D));
              return new DashboardInsights.InsightCard(
                  nullSafe(course.getName(), "未命名课程"),
                  nullSafe(course.getTeacherName(), "未分配教师"),
                  matched.size() + " 条记录",
                  "课程编号 "
                      + nullSafe(course.getNumb(), "-")
                      + " · 平均分 "
                      + formatNumber(average)
                      + " · 学分 "
                      + formatNumber(course.getScore()));
            })
        .toList();
  }

  private List<DashboardInsights.InsightCard> buildPeopleSpotlights(
      Role role,
      List<Course> courses,
      List<SelectionRecord> selections,
      List<Student> students) {
    if (role == Role.TEACHER) {
      Map<String, String> studentNames =
          students.stream()
              .collect(Collectors.toMap(Student::getId, student -> nullSafe(student.getSname(), student.getUsername()), (left, right) -> left));
      return selections.stream()
          .collect(Collectors.groupingBy(SelectionRecord::getStudentId))
          .entrySet()
          .stream()
          .sorted(Comparator.<Map.Entry<String, List<SelectionRecord>>>comparingInt(entry -> entry.getValue().size()).reversed())
          .limit(6)
          .map(
              entry ->
                  new DashboardInsights.InsightCard(
                      studentNames.getOrDefault(entry.getKey(), "未识别学生"),
                      "教师视角重点学生",
                      entry.getValue().size() + " 门课程",
                      "最近课程 "
                          + entry.getValue().stream()
                              .map(SelectionRecord::getCourseName)
                              .filter(Objects::nonNull)
                              .findFirst()
                              .orElse("-")))
          .toList();
    }

    Map<String, List<Course>> courseByTeacher =
        courses.stream().collect(Collectors.groupingBy(course -> nullSafe(course.getTid(), "UNASSIGNED")));
    Map<String, List<SelectionRecord>> selectionByTeacher =
        selections.stream().collect(Collectors.groupingBy(record -> nullSafe(record.getTeacherId(), "UNASSIGNED")));

    return courseByTeacher.entrySet().stream()
        .sorted(
            Comparator.<Map.Entry<String, List<Course>>>comparingInt(
                    entry -> selectionByTeacher.getOrDefault(entry.getKey(), List.of()).size())
                .reversed())
        .limit(6)
        .map(
            entry -> {
              List<Course> teacherCourses = entry.getValue();
              List<SelectionRecord> teacherSelections =
                  selectionByTeacher.getOrDefault(entry.getKey(), List.of());
              Course sample = teacherCourses.isEmpty() ? null : teacherCourses.get(0);
              String teacherName = sample == null ? "未分配教师" : nullSafe(sample.getTeacherName(), "未命名教师");
              return new DashboardInsights.InsightCard(
                  teacherName,
                  "教师负载观察",
                  teacherSelections.size() + " 条记录",
                  "负责 "
                      + teacherCourses.size()
                      + " 门课程 · 最近课程 "
                      + (sample == null ? "-" : nullSafe(sample.getName(), "-")));
            })
        .toList();
  }

  private List<DashboardInsights.InsightCard> buildDepartmentSpotlights(List<Course> courses) {
    return courses.stream()
        .collect(Collectors.groupingBy(course -> nullSafe(course.getDept(), "未设学院")))
        .entrySet()
        .stream()
        .sorted(Comparator.<Map.Entry<String, List<Course>>>comparingInt(entry -> entry.getValue().size()).reversed())
        .limit(6)
        .map(
            entry -> {
              List<Course> deptCourses = entry.getValue();
              String sample =
                  deptCourses.stream()
                      .map(Course::getName)
                      .filter(Objects::nonNull)
                      .filter(value -> !value.isBlank())
                      .limit(2)
                      .collect(Collectors.joining(" / "));
              return new DashboardInsights.InsightCard(
                  entry.getKey(),
                  "开课学院",
                  deptCourses.size() + " 门课程",
                  sample.isBlank() ? "暂无课程信息" : "代表课程 " + sample);
            })
        .toList();
  }

  private List<String> buildNotices(
      UserSession current,
      int courseCount,
      int selectionCount,
      long pendingGrades,
      long unassignedCourses) {
    return switch (current.getRole()) {
      case ADMIN ->
          List.of(
              "当前系统共维护 " + courseCount + " 门课程与 " + selectionCount + " 条选课记录，可在工作台统一调度。",
              unassignedCourses > 0
                  ? "仍有 " + unassignedCourses + " 门课程未分配授课教师，建议尽快完成排课。"
                  : "当前课程均已完成授课教师分配，排课状态稳定。",
              pendingGrades > 0
                  ? "当前还有 " + pendingGrades + " 条记录待录成绩，请提醒相关教师及时处理。"
                  : "当前成绩录入状态完整，可继续维护课程与人员资料。",
              "管理员可直接维护教师、学生、课程和选课全量信息。");
      case TEACHER ->
          List.of(
              "教师工作台仅展示你负责的课程、相关学生与成绩记录。",
              pendingGrades > 0
                  ? "你当前有 " + pendingGrades + " 条选课记录待录成绩，可在选课记录模块直接维护。"
                  : "你负责的课程成绩已经录入完成，可以继续优化课程简介与教学安排。",
              "课程中心支持维护本人课程的课程编号、学分和课程简介。",
              "成绩录入只允许操作你本人负责的选课记录。");
      case STUDENT ->
          List.of(
              "学生工作台只显示你的选课记录、课程信息与个人资料。",
              "当前你可浏览全校课程并在选课记录模块完成选课或退选。",
              pendingGrades > 0
                  ? "你当前还有 " + pendingGrades + " 门课程待教师录入成绩，请持续关注课程进展。"
                  : "当前已选课程成绩记录完整，可继续查看课程详情与个人信息。",
              "选课前请重点核对课程编号、学分、授课教师与上课安排。");
    };
  }

  private String nullSafe(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private String formatNumber(Double value) {
    if (value == null) {
      return "-";
    }
    return formatNumber(value.doubleValue());
  }

  private String formatNumber(double value) {
    return value == Math.rint(value) ? String.valueOf((long) value) : String.format("%.1f", value);
  }

  private double round(double value) {
    return Math.round(value * 10D) / 10D;
  }
}
