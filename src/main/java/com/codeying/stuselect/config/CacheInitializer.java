package com.codeying.stuselect.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheInitializer implements CommandLineRunner {

    private static final String[] APPLICATION_CACHE_NAMES = {
            "dashboardSummary",
            "dashboardInsights"
    };

    private static final Logger logger = LoggerFactory.getLogger(CacheInitializer.class);

    private final CacheManager cacheManager;

    public CacheInitializer(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void run(String... args) {
        try {
            for (String cacheName : APPLICATION_CACHE_NAMES) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            }
            logger.info("Application caches initialized: dashboard caches cleared");
        } catch (Exception e) {
            logger.warn("Failed to clear application caches on startup: {}", e.getMessage());
        }
    }
}
