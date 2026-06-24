package com.codeying.stuselect.config;

import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * Spring Cache配置
 * 提供自定义KeyGenerator，从HttpSession中提取role和userId作为缓存键
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 自定义缓存键生成器
     * 当方法参数中包含HttpSession时，自动提取role:userId作为缓存键
     * 其他参数按原值拼接
     */
    @Bean("userSessionKeyGenerator")
    public KeyGenerator userSessionKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder key = new StringBuilder();
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        key.append(":");
                    }
                    if (params[i] instanceof HttpSession httpSession) {
                        UserSession userSession =
                                (UserSession) httpSession.getAttribute(SessionService.LOGIN_USER);
                        if (userSession != null) {
                            key.append(userSession.getRole().getCode())
                                    .append(":")
                                    .append(userSession.getId());
                        } else {
                            key.append("anonymous");
                        }
                    } else if (params[i] != null) {
                        key.append(params[i]);
                    }
                }
                return key.length() > 0 ? key.toString()
                        : SimpleKeyGenerator.generateKey(params);
            }
        };
    }
}
