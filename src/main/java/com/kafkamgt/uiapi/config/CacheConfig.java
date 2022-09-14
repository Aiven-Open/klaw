package com.kafkamgt.uiapi.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfig {

  @Primary
  public CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    ConcurrentMapCache userCache =
        new ConcurrentMapCache(
            "tenantsusernames",
            Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(60)).build().asMap(),
            false);
    ConcurrentMapCache teamsCache =
        new ConcurrentMapCache(
            "emailconfigtenants",
            Caffeine.newBuilder().expireAfterWrite(Duration.ofDays(1)).build().asMap(),
            false);
    ConcurrentMapCache tenantsCache =
        new ConcurrentMapCache(
            "tenantsinfo",
            Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(1)).build().asMap(),
            false);

    cacheManager.setCaches(Arrays.asList(userCache, teamsCache));
    return cacheManager;
  }
}
