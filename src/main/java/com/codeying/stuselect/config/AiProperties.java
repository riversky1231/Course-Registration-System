package com.codeying.stuselect.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 能力相关配置，对应 application.yml 中的 {@code ai.*}。
 *
 * <p>默认对接 DeepSeek（OpenAI 兼容接口）。未配置 {@code apiKey} 时，
 * 上层服务会回退到示例内容，便于本地联调。
 */
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

  /** 模型服务基础地址（OpenAI 兼容）。 */
  private String baseUrl = "https://api.deepseek.com";

  /** 接口密钥，建议通过环境变量 DEEPSEEK_API_KEY 注入。 */
  private String apiKey = "";

  /** 模型名称。 */
  private String model = "deepseek-chat";

  /** 读取超时时间（毫秒）。 */
  private int timeoutMs = 60000;

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }

  public void setTimeoutMs(int timeoutMs) {
    this.timeoutMs = timeoutMs;
  }
}
