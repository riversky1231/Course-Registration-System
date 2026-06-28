package com.codeying.stuselect.service;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.AiChatRequest;
import com.codeying.stuselect.dto.AiChatResponse;
import com.codeying.stuselect.dto.AiRecommendation;
import com.codeying.stuselect.dto.AiSyllabusRequest;
import com.codeying.stuselect.dto.AiSyllabusResponse;
import com.codeying.stuselect.dto.StudentGradeReport;
import com.codeying.stuselect.model.Course;
import com.codeying.stuselect.model.SelectionRecord;
import com.codeying.stuselect.model.Student;
import com.codeying.stuselect.service.AiClient.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 业务编排：把系统内的学生画像、选课数据与可选课程组装为上下文，
 * 调用大模型实现「选课助手对话」「智能课程推荐」「课程简介/大纲生成」。
 */
@Service
public class AiService {

  private static final int MAX_CANDIDATES = 30;
  private static final int MAX_HISTORY = 6;
  private static final long STREAM_TIMEOUT_MS = 180_000L;

  private final AiClient aiClient;
  private final SessionService sessionService;
  private final StudentService studentService;
  private final CourseService courseService;
  private final SelectionService selectionService;
  private final ObjectMapper objectMapper;

  /** 流式对话的专用线程池：SseEmitter 需在请求线程外持续推送增量。 */
  private final ExecutorService chatStreamExecutor =
      Executors.newCachedThreadPool(
          runnable -> {
            Thread thread = new Thread(runnable, "ai-chat-stream");
            thread.setDaemon(true);
            return thread;
          });

  public AiService(
      AiClient aiClient,
      SessionService sessionService,
      StudentService studentService,
      CourseService courseService,
      SelectionService selectionService,
      ObjectMapper objectMapper) {
    this.aiClient = aiClient;
    this.sessionService = sessionService;
    this.studentService = studentService;
    this.courseService = courseService;
    this.selectionService = selectionService;
    this.objectMapper = objectMapper;
  }

  // ==================== 选课助手对话 ====================

  public AiChatResponse chat(AiChatRequest request, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (request == null || !StringUtils.hasText(request.question())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "请输入你的问题");
    }
    String question = request.question().trim();

    if (!aiClient.isConfigured()) {
      return new AiChatResponse(demoChatReply(question), false);
    }

    List<Message> messages = buildChatMessages(request, session, current, question);
    return new AiChatResponse(aiClient.chat(messages, false), true);
  }

  /**
   * 「流式」选课助手对话：在请求线程内完成鉴权与上下文组装（依赖 HttpSession），
   * 随后在独立线程中调用大模型流式接口，通过 {@link SseEmitter} 把增量 token
   * 逐块推送给前端，实现打字机效果。
   */
  public SseEmitter chatStream(AiChatRequest request, HttpSession session) {
    UserSession current = sessionService.requireUser(session);
    if (request == null || !StringUtils.hasText(request.question())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "请输入你的问题");
    }
    String question = request.question().trim();
    SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);

    if (!aiClient.isConfigured()) {
      String demo = demoChatReply(question);
      chatStreamExecutor.execute(
          () -> {
            try {
              emitDelta(emitter, demo);
              emitDone(emitter, false);
              emitter.complete();
            } catch (Exception ex) {
              emitter.completeWithError(ex);
            }
          });
      return emitter;
    }

    List<Message> messages = buildChatMessages(request, session, current, question);
    chatStreamExecutor.execute(
        () -> {
          try {
            aiClient.chatStream(messages, token -> emitDelta(emitter, token));
            emitDone(emitter, true);
            emitter.complete();
          } catch (Exception ex) {
            String message =
                ex instanceof AppException ? ex.getMessage() : "AI 流式服务暂不可用，请稍后再试";
            emitError(emitter, message);
          }
        });
    return emitter;
  }

  private String demoChatReply(String question) {
    return "（演示模式：尚未配置 AI 密钥）我是你的选课助手。配置 DEEPSEEK_API_KEY 后，"
        + "我会结合你的年级、已修课程、GPA 和可选课程给出个性化建议。\n\n你的问题是：「"
        + question
        + "」";
  }

  private List<Message> buildChatMessages(
      AiChatRequest request, HttpSession session, UserSession current, String question) {
    List<Message> messages = new ArrayList<>();
    messages.add(
        Message.system(
            "你是一名大学「智能选课助手」，熟悉培养方案、先修课程、互斥课程、年级限制与学分上限规则。"
                + "请用简洁、专业、友好的中文回答，必要时给出具体课程名称与可执行建议。"
                + "只能基于提供的课程信息作答，不要编造不存在的课程。"));
    messages.add(Message.system(buildContext(session, current)));

    if (request.history() != null) {
      List<AiChatRequest.ChatTurn> history = request.history();
      int from = Math.max(0, history.size() - MAX_HISTORY);
      for (AiChatRequest.ChatTurn turn : history.subList(from, history.size())) {
        if (turn == null || !StringUtils.hasText(turn.content())) {
          continue;
        }
        String role = "assistant".equals(turn.role()) ? "assistant" : "user";
        messages.add(new Message(role, turn.content()));
      }
    }
    messages.add(Message.user(question));
    return messages;
  }

  private void emitDelta(SseEmitter emitter, String token) {
    try {
      emitter.send(
          SseEmitter.event()
              .name("delta")
              .data(Map.of("content", token), MediaType.APPLICATION_JSON));
    } catch (IOException ex) {
      throw new IllegalStateException("推送增量失败", ex);
    }
  }

  private void emitDone(SseEmitter emitter, boolean configured) {
    try {
      emitter.send(
          SseEmitter.event()
              .name("done")
              .data(Map.of("configured", configured), MediaType.APPLICATION_JSON));
    } catch (IOException ex) {
      throw new IllegalStateException("推送结束事件失败", ex);
    }
  }

  private void emitError(SseEmitter emitter, String message) {
    try {
      emitter.send(
          SseEmitter.event()
              .name("error")
              .data(Map.of("message", message), MediaType.APPLICATION_JSON));
      emitter.complete();
    } catch (IOException ex) {
      emitter.completeWithError(ex);
    }
  }

  @PreDestroy
  public void shutdown() {
    chatStreamExecutor.shutdownNow();
  }

  // ==================== 智能课程推荐 ====================

  public AiRecommendation recommend(HttpSession session) {
    UserSession current = sessionService.requireRole(session, Role.STUDENT);
    Student student = studentService.require(current.getId());
    List<SelectionRecord> selections = selectionService.listAllVisible(session);
    double gpa = selectionService.gradeReport(null, 1, 1, session).gpa();
    double creditCap = creditCap(gpa);
    List<Course> candidates = buildCandidates(session, selections);

    if (candidates.isEmpty()) {
      throw new AppException(HttpStatus.BAD_REQUEST, "当前没有可推荐的课程，可能你已选完全部可选课程");
    }

    if (!aiClient.isConfigured()) {
      return mockRecommendation(candidates, creditCap);
    }

    String context =
        "【学生情况】\n"
            + studentProfile(student, gpa, creditCap)
            + "\n\n【候选课程（只能从中选择，括号内为课程ID）】\n"
            + candidateLines(candidates);

    List<Message> messages =
        List.of(
            Message.system(
                "你是大学「选课规划助手」。请根据【学生情况】与【候选课程】为该学生规划本学期推荐课表。"
                    + "要求：1) 只能从候选课程中选择；2) 优先专业必修与先修条件已满足的课程；"
                    + "3) 合计学分尽量贴近但不超过学分上限；4) 避免同时推荐互斥课程。"
                    + "必须只输出一个 JSON 对象，格式："
                    + "{\"summary\":\"一句话总体说明\",\"totalCredits\":数字,"
                    + "\"items\":[{\"courseId\":\"候选课程ID\",\"courseName\":\"课程名\",\"credit\":数字,"
                    + "\"type\":\"课程类型\",\"reason\":\"推荐理由\"}],\"note\":\"补充提示\"}。"),
            Message.user(context));

    String json = aiClient.chat(messages, true);
    return parseRecommendation(json, candidates);
  }

  // ==================== 课程简介/大纲生成 ====================

  public AiSyllabusResponse syllabus(AiSyllabusRequest request, HttpSession session) {
    sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
    if (request == null || !StringUtils.hasText(request.courseName())) {
      throw new AppException(HttpStatus.BAD_REQUEST, "请填写课程名称");
    }
    String courseName = request.courseName().trim();
    String keywords = request.keywords() == null ? "" : request.keywords().trim();

    if (!aiClient.isConfigured()) {
      return new AiSyllabusResponse(
          "（演示）本课程「" + courseName + "」围绕" + (keywords.isBlank() ? "核心知识与实践能力" : keywords)
              + "展开，注重理论与动手实践结合，帮助学生建立完整知识体系并提升工程应用能力。",
          List.of(
              "课程导论与学习目标",
              "核心概念与基础知识",
              "关键方法与原理讲解",
              "案例分析与课堂实践",
              "综合项目与作业",
              "复习总结与考核"),
          false);
    }

    List<Message> messages =
        List.of(
            Message.system(
                "你是大学课程教学设计助手。根据课程名称与关键词，生成中文课程简介与教学大纲。"
                    + "必须只输出一个 JSON 对象，格式："
                    + "{\"jianjie\":\"不超过200字的课程简介\",\"outline\":[\"要点1\",\"要点2\"]}。"
                    + "outline 建议 6-10 条，覆盖从导论到考核的完整教学安排。"),
            Message.user("课程名称：" + courseName + "\n关键词：" + (keywords.isBlank() ? "无" : keywords)));

    String json = aiClient.chat(messages, true);
    return parseSyllabus(json);
  }

  // ==================== 上下文与提示词构建 ====================

  private String buildContext(HttpSession session, UserSession current) {
    if (current.getRole() == Role.STUDENT) {
      Student student = studentService.require(current.getId());
      List<SelectionRecord> selections = selectionService.listAllVisible(session);
      double gpa = selectionService.gradeReport(null, 1, 1, session).gpa();
      double creditCap = creditCap(gpa);
      List<Course> candidates = buildCandidates(session, selections);
      return "【学生情况】\n"
          + studentProfile(student, gpa, creditCap)
          + "\n\n【已选/已修课程】\n"
          + selectionLines(selections)
          + "\n\n【当前可选课程（括号内为课程ID）】\n"
          + candidateLines(candidates);
    }
    // 教师/管理员：提供课程目录概览。
    List<Course> courses = courseService.listAllVisible(session);
    return "【课程目录概览】共 " + courses.size() + " 门课程。\n" + candidateLines(courses);
  }

  private String studentProfile(Student student, double gpa, double creditCap) {
    return "姓名："
        + nullSafe(student.getSname(), student.getUsername())
        + "；年级：" + gradeLabel(student.getGrade())
        + "；学院：" + nullSafe(student.getSdept(), "未知")
        + "；专业：" + nullSafe(student.getSmajor(), "未知")
        + "；当前GPA：" + formatNumber(gpa)
        + "；本学期建议选课学分上限：" + formatNumber(creditCap) + " 学分"
        + "（GPA<2.0→15，2.0~3.0→20，3.0~3.5→25，≥3.5→30）。";
  }

  private List<Course> buildCandidates(HttpSession session, List<SelectionRecord> selections) {
    Set<String> selectedCourseIds = new HashSet<>();
    for (SelectionRecord record : selections) {
      if (record.getCourseId() != null) {
        selectedCourseIds.add(record.getCourseId());
      }
    }
    List<Course> result = new ArrayList<>();
    for (Course course : courseService.listAllVisible(session)) {
      if (selectedCourseIds.contains(course.getId())) {
        continue;
      }
      if (isFull(course)) {
        continue;
      }
      result.add(course);
      if (result.size() >= MAX_CANDIDATES) {
        break;
      }
    }
    return result;
  }

  private boolean isFull(Course course) {
    Integer max = course.getMaxStudents();
    if (max == null || max <= 0) {
      return false;
    }
    long selected = course.getSelectedCount() == null ? 0 : course.getSelectedCount();
    return selected >= max;
  }

  private String candidateLines(List<Course> courses) {
    StringBuilder sb = new StringBuilder();
    for (Course course : courses) {
      sb.append("- ")
          .append(nullSafe(course.getName(), "未命名"))
          .append("（ID:").append(course.getId()).append("）")
          .append(" 学分:").append(formatNumber(course.getScore()))
          .append(" 类型:").append(nullSafe(course.getCourseType(), "未分类"))
          .append(" 学院:").append(nullSafe(course.getDept(), "-"))
          .append(" 时间:").append(nullSafe(course.getTimeSlot(), "未排"))
          .append(" 年级限制:").append(course.getGradeLimit() == null ? "不限" : gradeLabel(course.getGradeLimit()))
          .append(" 教师:").append(nullSafe(course.getTeacherName(), "待安排"))
          .append("\n");
    }
    return sb.length() == 0 ? "（无）" : sb.toString();
  }

  private String selectionLines(List<SelectionRecord> selections) {
    if (selections.isEmpty()) {
      return "（暂无选课记录）";
    }
    StringBuilder sb = new StringBuilder();
    for (SelectionRecord record : selections) {
      sb.append("- ")
          .append(nullSafe(record.getCourseName(), "未命名"))
          .append(" 学分:").append(formatNumber(record.getCourseCredit()))
          .append(Boolean.TRUE.equals(record.getGraded())
              ? " 成绩:" + formatNumber(record.getScore())
              : " 状态:在修/待出成绩")
          .append("\n");
    }
    return sb.toString();
  }

  // ==================== 解析与兜底 ====================

  private AiRecommendation parseRecommendation(String json, List<Course> candidates) {
    try {
      JsonNode root = objectMapper.readTree(extractJson(json));
      String summary = root.path("summary").asText("");
      String note = root.path("note").asText("");
      List<AiRecommendation.Item> items = new ArrayList<>();
      double total = 0D;
      for (JsonNode node : root.path("items")) {
        String courseId = node.path("courseId").asText("");
        Course matched =
            candidates.stream().filter(c -> Objects.equals(c.getId(), courseId)).findFirst().orElse(null);
        String name = node.path("courseName").asText(matched == null ? "" : nullSafe(matched.getName(), ""));
        double credit =
            node.has("credit")
                ? node.path("credit").asDouble(0D)
                : (matched == null || matched.getScore() == null ? 0D : matched.getScore());
        String type = node.path("type").asText(matched == null ? "" : nullSafe(matched.getCourseType(), ""));
        String reason = node.path("reason").asText("");
        items.add(new AiRecommendation.Item(courseId, name, credit, type, reason));
        total += credit;
      }
      double totalCredits = root.has("totalCredits") ? root.path("totalCredits").asDouble(total) : total;
      return new AiRecommendation(summary, round(totalCredits), items, note, true);
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "AI 推荐结果解析失败，请重试");
    }
  }

  private AiSyllabusResponse parseSyllabus(String json) {
    try {
      JsonNode root = objectMapper.readTree(extractJson(json));
      String jianjie = root.path("jianjie").asText("");
      List<String> outline = new ArrayList<>();
      for (JsonNode node : root.path("outline")) {
        if (StringUtils.hasText(node.asText())) {
          outline.add(node.asText());
        }
      }
      return new AiSyllabusResponse(jianjie, outline, true);
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "AI 大纲结果解析失败，请重试");
    }
  }

  private AiRecommendation mockRecommendation(List<Course> candidates, double creditCap) {
    List<Course> sorted = new ArrayList<>(candidates);
    sorted.sort((a, b) -> Integer.compare(typeWeight(b.getCourseType()), typeWeight(a.getCourseType())));
    List<AiRecommendation.Item> items = new ArrayList<>();
    double total = 0D;
    for (Course course : sorted) {
      double credit = course.getScore() == null ? 0D : course.getScore();
      if (total + credit > creditCap) {
        continue;
      }
      items.add(
          new AiRecommendation.Item(
              course.getId(),
              nullSafe(course.getName(), "未命名"),
              credit,
              nullSafe(course.getCourseType(), "未分类"),
              "示例推荐：优先必修与基础课程，合理控制学分。"));
      total += credit;
      if (items.size() >= 6) {
        break;
      }
    }
    return new AiRecommendation(
        "（演示模式）已根据学分上限与课程类型生成示例推荐，配置 AI 密钥后将给出更智能的规划。",
        round(total),
        items,
        "配置 DEEPSEEK_API_KEY 后可获得结合先修/互斥/GPA 的个性化推荐。",
        false);
  }

  private int typeWeight(String type) {
    if (type == null) {
      return 0;
    }
    if (type.contains("必修")) {
      return 3;
    }
    if (type.contains("专业")) {
      return 2;
    }
    return 1;
  }

  private double creditCap(double gpa) {
    if (gpa < 2.0) {
      return 15D;
    }
    if (gpa < 3.0) {
      return 20D;
    }
    if (gpa < 3.5) {
      return 25D;
    }
    return 30D;
  }

  private String extractJson(String text) {
    if (text == null) {
      return "{}";
    }
    int start = text.indexOf('{');
    int end = text.lastIndexOf('}');
    if (start >= 0 && end > start) {
      return text.substring(start, end + 1);
    }
    return text;
  }

  private String gradeLabel(Integer grade) {
    if (grade == null) {
      return "未知";
    }
    return switch (grade) {
      case 1 -> "大一";
      case 2 -> "大二";
      case 3 -> "大三";
      case 4 -> "大四";
      default -> grade + "年级";
    };
  }

  private String nullSafe(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private String formatNumber(Double value) {
    if (value == null) {
      return "-";
    }
    return value == Math.rint(value) ? String.valueOf((long) (double) value) : String.format("%.1f", value);
  }

  private double round(double value) {
    return Math.round(value * 10D) / 10D;
  }
}
