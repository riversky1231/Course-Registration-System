package com.codeying.stuselect.service;

import com.codeying.stuselect.common.AppException;
import com.codeying.stuselect.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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
  private final HttpClient streamingHttpClient;

  public AiClient(AiProperties properties, ObjectMapper objectMapper) {
    this.properties = properties;
    this.objectMapper = objectMapper;
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(10000);
    factory.setReadTimeout(properties.getTimeoutMs());
    this.restClient = RestClient.builder().requestFactory(factory).build();
    this.streamingHttpClient =
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
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

  /**
   * 发起一次「流式」对话补全（SSE）。
   *
   * <p>使用 OpenAI 兼容的 {@code stream:true} 协议：服务端以 {@code data: {json}} 的形式
   * 逐块返回增量内容，最后以 {@code data: [DONE]} 结束。每收到一个非空增量 token，
   * 即回调 {@code onDelta}，由上层推送给前端实现「打字机」效果。
   *
   * @param messages 完整消息列表（含 system/user/assistant）
   * @param onDelta 增量内容回调（按到达顺序，可能为单个/多个字符）
   */
  public void chatStream(List<Message> messages, Consumer<String> onDelta) {
    if (!isConfigured()) {
      throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "AI 服务未配置密钥");
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("model", properties.getModel());
    body.put(
        "messages",
        messages.stream().map(m -> Map.of("role", m.role(), "content", m.content())).toList());
    body.put("temperature", 0.7);
    body.put("stream", true);

    try {
      String payload = objectMapper.writeValueAsString(body);
      HttpRequest httpRequest =
          HttpRequest.newBuilder()
              .uri(URI.create(properties.getBaseUrl() + "/chat/completions"))
              .timeout(Duration.ofMillis(properties.getTimeoutMs()))
              .header("Authorization", "Bearer " + properties.getApiKey())
              .header("Content-Type", "application/json")
              .header("Accept", "text/event-stream")
              .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
              .build();

      HttpResponse<InputStream> response =
          streamingHttpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
      if (response.statusCode() != 200) {
        throw new AppException(
            HttpStatus.BAD_GATEWAY, "调用 AI 服务失败：HTTP " + response.statusCode());
      }

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isEmpty() || !line.startsWith("data:")) {
            continue;
          }
          String data = line.substring(5).trim();
          if ("[DONE]".equals(data)) {
            break;
          }
          JsonNode delta =
              objectMapper.readTree(data).path("choices").path(0).path("delta").path("content");
          if (!delta.isMissingNode() && !delta.isNull()) {
            String token = delta.asText();
            if (!token.isEmpty()) {
              onDelta.accept(token);
            }
          }
        }
      }
    } catch (AppException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new AppException(HttpStatus.BAD_GATEWAY, "调用 AI 流式服务失败：" + rootMessage(ex));
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
