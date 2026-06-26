package com.codeying.stuselect.config;

import com.codeying.stuselect.common.UserSession;
import com.codeying.stuselect.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.CachingConfigurer;

import java.lang.reflect.Method;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);

    @Bean("userSessionKeyGenerator")
    @Override
    public KeyGenerator keyGenerator() {
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

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.warn("Cache get failed for key {}: {}", key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, org.springframework.cache.Cache cache, Object key, Object value) {
                logger.warn("Cache put failed for key {}: {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, org.springframework.cache.Cache cache, Object key) {
                logger.warn("Cache evict failed for key {}: {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, org.springframework.cache.Cache cache) {
                logger.warn("Cache clear failed: {}", exception.getMessage());
            }
        };
    }
}
