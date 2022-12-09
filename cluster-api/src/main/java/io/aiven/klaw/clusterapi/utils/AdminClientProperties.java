package io.aiven.klaw.clusterapi.utils;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Usually one should consider using {@link ConfigurationProperties}, which may mean to define a few
 * inner classes. To avoid this effort and to keep things simple (without changing too much of the
 * sources without aligning with the dev lead first), I prefer to keep it simple and arguably a bit
 * dirty.
 *
 * <p>This class is introduced in order to improve the testability.
 */
@Getter
@Component
class AdminClientProperties {

  @Value("${klaw.request.timeout.ms:15000}")
  private String requestTimeOutMs;

  @Value("${klaw.retries.config:25}")
  private String retriesConfig;

  @Value("${klaw.retry.backoff.ms:15000}")
  private String retryBackOffMsConfig;
}
