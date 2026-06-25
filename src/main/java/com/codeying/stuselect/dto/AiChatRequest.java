package com.codeying.stuselect.dto;

import java.util.List;

/** AI 选课助手对话请求。 */
public record AiChatRequest(String question, List<ChatTurn> history) {

  /** 历史对话轮次。 */
  public record ChatTurn(String role, String content) {}
}
