package io.aiven.klaw.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.service.CacheService;
import io.aiven.klaw.service.HighAvailabilityUtilsService;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class CacheConfig {

  @Autowired private HighAvailabilityUtilsService highAvailabilityUtilsService;

  @Value("${klaw.uiapi.servers:server1,server2}")
  private String uiApiServers;

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

  @Bean
  public CacheService<Env> kafkaEnvListPerTenant() {
    return new CacheService<Env>("environment", uiApiServers, highAvailabilityUtilsService);
  }

  @Bean
  public CacheService<Env> schemaRegEnvListPerTenant() {
    return new CacheService<Env>("environment", uiApiServers, highAvailabilityUtilsService);
  }

  @Bean
  public CacheService<Env> kafkaConnectEnvListPerTenant() {
    return new CacheService<Env>("environment", uiApiServers, highAvailabilityUtilsService);
  }

  @Bean
  public CacheService<Env> allEnvListPerTenant() {
    return new CacheService<Env>("environment", uiApiServers, highAvailabilityUtilsService);
  }

  @Bean
  public CacheService<Team> teamsPerTenant() {
    return new CacheService<Team>("team", uiApiServers, highAvailabilityUtilsService);
  }
}
