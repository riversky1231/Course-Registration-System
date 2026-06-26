package com.codeying.stuselect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class CacheInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CacheInitializer.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            logger.info("Cache initialized: Redis database flushed");
        } catch (Exception e) {
            logger.warn("Failed to flush Redis cache on startup: {}", e.getMessage());
        }
    }
}
