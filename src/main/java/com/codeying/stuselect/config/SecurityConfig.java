package com.codeying.stuselect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security配置
 * 提供CSRF保护、会话安全、XSS防护
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // CSRF保护配置
    CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
    requestHandler.setCsrfRequestAttributeName("_csrf");

    // 强制每次请求都解析CSRF token，确保XSRF-TOKEN cookie被写入
    CsrfTokenRequestHandler eagerHandler = (request, response, csrfTokenSupplier) -> {
      CsrfToken csrfToken = csrfTokenSupplier.get();
      csrfToken.getToken(); // 触发cookie写入
      requestHandler.handle(request, response, () -> csrfToken);
    };

    http
        // 允许所有请求通过（身份验证由SessionService处理）
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

        // 启用CSRF保护
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(eagerHandler)
            // 登录和注册接口需要特殊处理（前端需要先获取CSRF token）
            .ignoringRequestMatchers("/api/auth/login", "/api/auth/register")
        )

        // 会话管理
        .sessionManagement(session -> session
            .maximumSessions(1) // 同一用户最多1个会话
            .maxSessionsPreventsLogin(false) // 新会话踢掉旧会话
        )

        // 安全响应头
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:")
            )
            .xssProtection(xss -> xss.headerValue(
                org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK
            ))
            .frameOptions(frame -> frame.deny())
        );

    return http.build();
  }
}
