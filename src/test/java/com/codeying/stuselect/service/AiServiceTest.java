package com.codeying.stuselect.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codeying.stuselect.common.Role;
import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.dto.AiSyllabusRequest;
import com.codeying.stuselect.dto.AiSyllabusResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

class AiServiceTest {

  @Test
  void syllabusRemovesNumberingFromOutlineItems() {
    AiClient aiClient = mock(AiClient.class);
    SessionService sessionService = mock(SessionService.class);
    StudentService studentService = mock(StudentService.class);
    CourseService courseService = mock(CourseService.class);
    SelectionService selectionService = mock(SelectionService.class);
    HttpSession session = mock(HttpSession.class);

    when(sessionService.requireRole(session, Role.ADMIN, Role.TEACHER))
        .thenReturn(new UserSession("T1", "teacher", Role.TEACHER, "张老师"));
    when(aiClient.isConfigured()).thenReturn(true);
    when(aiClient.chat(any(), anyBoolean()))
        .thenReturn(
            """
            {
              "jianjie": "课程简介",
              "outline": [
                "1. 1. 导论：分布式系统定义、挑战与设计目标",
                "（二）通信与网络基础",
                "三、时间与顺序",
                "- 一致性模型"
              ]
            }
            """);

    AiService service =
        new AiService(
            aiClient,
            sessionService,
            studentService,
            courseService,
            selectionService,
            new ObjectMapper());

    AiSyllabusResponse response =
        service.syllabus(new AiSyllabusRequest("分布式系统", "一致性"), session);

    assertEquals(
        "导论：分布式系统定义、挑战与设计目标",
        response.outline().get(0));
    assertEquals("通信与网络基础", response.outline().get(1));
    assertEquals("时间与顺序", response.outline().get(2));
    assertEquals("一致性模型", response.outline().get(3));
  }
}
