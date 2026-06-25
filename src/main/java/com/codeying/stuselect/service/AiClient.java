package com.codeying.stuselect.service;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 直连大模型（OpenAI 兼容 {@code /chat/completions}）的底层客户端。
 *
 * <p>默认对接 DeepSeek，可通过配置切换 base-url / model 到任意 OpenAI 兼容服务。
 */
@Service
public class AiClient {

  /** 一条对话消息（role/content）。 */
  public record Message(String role, String content) {
    public static Message system(String content) {
      return new Message("system", content);
    }

    public static Message user(String content) {
      return new Message("user", content);
    }

    public static Message assistant(String content) {
      return new Message("assistant", content);
    }
  }

  private final AiProperties properties;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public AiClient(AiProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(10000);
    factory.setReadTimeout(properties.getTimeoutMs());
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  /** 是否已配置密钥。未配置时上层应回退到示例内容。 */
  public boolean isConfigured() {
    return StringUtils.hasText(properties.getApiKey());
  }

  /**
   * 发起一次对话补全。
   *
   * @param messages 完整消息列表（含 system/user/assistant）
   * @param jsonMode 是否要求模型输出 JSON
   * @return 模型返回的文本内容
   */
  public String chat(List<Message> messages, boolean jsonMode) {
    if (!isConfigured()) {
      throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "AI 服务未配置密钥");
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", properties.getModel());
    body.put(
        "messages",
        messages.stream()
            .map(m -> Map.of("role", m.role(), "content", m.content()))
            .toList());
    body.put("temperature", jsonMode ? 0.3 : 0.7);
    body.put("stream", false);
    if (jsonMode) {
      body.put("response_format", Map.of("type", "json_object"));
    }

    try {
      String response =
          restClient
              .post()
              .uri(properties.getBaseUrl() + "/chat/completions")
              .header("Authorization", "Bearer " + properties.getApiKey())
              .contentType(MediaType.APPLICATION_JSON)
              .body(body)
              .retrieve()
              .body(String.class);

      JsonNode root = objectMapper.readTree(response);
      JsonNode content = root.path("choices").path(0).path("message").path("content");
      if (content.isMissingNode() || content.isNull()) {
        throw new AppException(HttpStatus.BAD_GATEWAY, "AI 返回内容为空");
      }
      return content.asText();
    } catch (AppException ex) {
      throw ex;
    } catch (RestClientException ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "调用 AI 服务失败：" + rootMessage(ex));
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "解析 AI 响应失败：" + rootMessage(ex));
    }
  }

  private String rootMessage(Throwable ex) {
    Throwable cause = ex;
    while (cause.getCause() != null && cause.getCause() != cause) {
      cause = cause.getCause();
    }
    return cause.getMessage() == null ? ex.getClass().getSimpleName() : cause.getMessage();
  }
}
