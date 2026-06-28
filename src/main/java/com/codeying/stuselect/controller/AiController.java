package com.codeying.stuselect.controller;

import com.codeying.stuselect.common.ApiResponse;
import com.codeying.stuselect.dto.AiChatRequest;
import com.codeying.stuselect.dto.AiChatResponse;
import com.codeying.stuselect.dto.AiRecommendation;
import com.codeying.stuselect.dto.AiSyllabusRequest;
import com.codeying.stuselect.dto.AiSyllabusResponse;
import com.codeying.stuselect.service.AiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** AI 能力接口：选课助手对话、智能课程推荐、课程简介/大纲生成。 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

  private final AiService aiService;

  public AiController(AiService aiService) {
    this.aiService = aiService;
  }

  @PostMapping("/chat")
  public ApiResponse<AiChatResponse> chat(@RequestBody AiChatRequest request, HttpSession session) {
    return ApiResponse.success(aiService.chat(request, session));
  }

  /** 选课助手「流式」对话：以 SSE 持续返回增量内容，实现打字机效果。 */
  @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
  public SseEmitter chatStream(@RequestBody AiChatRequest request, HttpSession session) {
    return aiService.chatStream(request, session);
  }

  @GetMapping("/recommend")
  public ApiResponse<AiRecommendation> recommend(HttpSession session) {
    return ApiResponse.success(aiService.recommend(session));
  }

  @PostMapping("/syllabus")
  public ApiResponse<AiSyllabusResponse> syllabus(
      @RequestBody AiSyllabusRequest request, HttpSession session) {
    return ApiResponse.success(aiService.syllabus(request, session));
  }
}
