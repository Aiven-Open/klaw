package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.CLUSTER_API_ERR_117;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.service.interfaces.HAMessagingServiceI;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.net.InetAddress;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class HARestMessagingService implements HAMessagingServiceI {

  public static final String URL_SEPERATOR = "/";
  public static final String TENANT = "tenant";
  public static final String ID = "id";
  public static final String BEARER = "Bearer ";
  public static final String AUTHORIZATION = "Authorization";
  public static final String ROLES = "Roles";
  public static final String NAME = "name";
  public static final String ENTITY_TYPE = "entityType";
  public static final String CACHE = "cache";
  public static final String CACHE_ADMIN = "CACHE_ADMIN";
  public static final String APP_2_APP = "App2App";
  @Autowired Environment environment;

  private List<String> clusterUrls;

  private RestTemplate rest;
  private static HttpComponentsClientHttpRequestFactory requestFactory = null;

  private static Map<String, String> baseUrlsMap;

  @Value("${klaw.core.app2app.base64.secret:#{''}}")
  private String app2AppApiKey;

  @Value("${klaw.core.app2app.username:KlawApp2App}")
  private String apiUser;

  @Value("${klaw.uiapi.servers:server1,server2}")
  private String clusterUrlsAsString;

  public static final String BASE_URL_ADDRESS = "BASE_URL_ADDRESS";
  public static final String BASE_URL_NAME = "BASE_URL_NAME";

  private Map<String, String> getBaseIpUrlFromEnvironment() {
    if (baseUrlsMap != null && !baseUrlsMap.isEmpty()) {
      return baseUrlsMap;
    } else {
      baseUrlsMap = new HashMap<>();
      String hostAddress = InetAddress.getLoopbackAddress().getHostAddress();
      String hostName = InetAddress.getLoopbackAddress().getHostName();
      int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));

      baseUrlsMap.put(BASE_URL_ADDRESS, hostAddress + ":" + port);
      baseUrlsMap.put(BASE_URL_NAME, hostName + ":" + port);
      return baseUrlsMap;
    }
  }

  public boolean isLocalServerUrl(String basePath) {
    Map<String, String> baseUrlsFromEnv = getBaseIpUrlFromEnvironment();
    if (baseUrlsFromEnv != null && !baseUrlsFromEnv.isEmpty()) {
      if (existsInMap(basePath, baseUrlsFromEnv, BASE_URL_ADDRESS)) {
        return true;
      }
      return existsInMap(basePath, baseUrlsFromEnv, BASE_URL_NAME);
    }
    return false;
  }

  private static boolean existsInMap(
      String basePath, Map<String, String> baseUrlsFromEnv, String key) {
    return baseUrlsFromEnv.containsKey(key) && basePath.contains(baseUrlsFromEnv.get(key));
  }

  public RestTemplate getRestTemplate() {
    if (clusterUrlsAsString.toLowerCase().startsWith("https")) {
      if (requestFactory == null) {
        requestFactory = ClusterApiService.requestFactory;
      }
      return new RestTemplate(requestFactory);
    } else {
      return new RestTemplate();
    }
  }

  public List<String> getHAClusterUrls() {
    if (clusterUrlsAsString != null && !clusterUrlsAsString.isEmpty()) {
      return Arrays.stream(clusterUrlsAsString.split(","))
          .filter(serverUrl -> !isLocalServerUrl(serverUrl))
          .toList();
    } else {
      return new ArrayList<>();
    }
  }

  public HttpHeaders createHeaders() throws KlawException {
    HttpHeaders httpHeaders = new HttpHeaders();
    String authHeader = BEARER + generateToken(apiUser);
    httpHeaders.set(AUTHORIZATION, authHeader);
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);

    return httpHeaders;
  }

  private String generateToken(String username) throws KlawException {
    if (app2AppApiKey.isBlank()) {
      log.error(CLUSTER_API_ERR_117);
      throw new KlawException(CLUSTER_API_ERR_117);
    }

    Key hmacKey =
        new SecretKeySpec(
            Base64.decodeBase64(app2AppApiKey), SignatureAlgorithm.HS256.getJcaName());
    Instant now = Instant.now();

    return Jwts.builder()
        .claim(NAME, username)
        .claim(ROLES, List.of(CACHE_ADMIN, APP_2_APP))
        .setSubject(username)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(3L, ChronoUnit.MINUTES))) // expiry in 3 minutes
        .signWith(hmacKey)
        .compact();
  }

  @Override
  public void sendUpdate(String entityType, int tenantId, Object entry) {
    if (isLazyLoaded()) {
      throw new RuntimeException("Unable to load High Availability Cache");
    }

    for (String url : clusterUrls) {
      try {
        HttpEntity<Object> request = new HttpEntity<>(entry, createHeaders());
        rest.postForObject(getUrl(url, entityType, tenantId, null), request, ApiResponse.class);

      } catch (KlawException | RestClientException clientException) {
        log.error(
            "Exception while sending HA updates to another instance {} in the cluster.",
            url,
            clientException);
      }
    }
  }

  @Override
  public void sendRemove(String entityType, int tenantId, int id) {
    if (isLazyLoaded()) {
      throw new RuntimeException("Unable to load High Availability Cache");
    }

    for (String url : clusterUrls) {
      try {
        rest.exchange(
            getUrl(url, entityType, tenantId, id),
            HttpMethod.DELETE,
            new HttpEntity<>(createHeaders()),
            Void.class);
      } catch (RestClientException | KlawException clientException) {
        log.error(
            "Exception while sending HA updates to another instance {} in the cluster.",
            url,
            clientException);
      }
    }
  }

  private boolean isLazyLoaded() {

    if (rest == null || clusterUrls == null) {
      this.rest = getRestTemplate();
      this.clusterUrls = getHAClusterUrls();
    }
    return (rest == null && clusterUrls == null);
  }

  private String getUrl(String url, String entityType, int tenantId, Integer id) {
    if (url.endsWith(URL_SEPERATOR)) {
      url = url.substring(0, url.length() - 1);
    }
    if (id != null) {
      return String.join(
          URL_SEPERATOR,
          url,
          CACHE,
          TENANT,
          Integer.toString(tenantId),
          ENTITY_TYPE,
          entityType,
          ID,
          Integer.toString(id));
    } else {
      return String.join(
          URL_SEPERATOR, url, CACHE, TENANT, Integer.toString(tenantId), ENTITY_TYPE, entityType);
    }
  }
}
