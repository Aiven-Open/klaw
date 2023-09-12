package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.CLUSTER_API_ERR_117;

import io.aiven.klaw.error.KlawException;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class HighAvailabilityUtilsService {

  public static final String BEARER = "Bearer ";
  public static final String AUTHORIZATION = "Authorization";
  @Autowired Environment environment;

  private static HttpComponentsClientHttpRequestFactory requestFactory = null;

  private static Map<String, String> baseUrlsMap;

  @Value("${klaw.clusterapi.access.base64.secret:#{''}}")
  private String clusterApiAccessBase64Secret;

  @Value("${klaw.clusterapi.access.username}")
  private String apiUser;

  @Value("${klaw.uiapi.servers:server1,server2}")
  private String clusterUrlsAsString;

  public static final String BASE_URL_ADDRESS = "BASE_URL_ADDRESS";
  public static final String BASE_URL_NAME = "BASE_URL_NAME";

  public Map<String, String> getBaseIpUrlFromEnvironment() {
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
      if (baseUrlsFromEnv.containsKey(BASE_URL_ADDRESS)
          && basePath.contains(baseUrlsFromEnv.get(BASE_URL_ADDRESS))) {
        return true;
      }
      return baseUrlsFromEnv.containsKey(BASE_URL_NAME)
          && basePath.contains(baseUrlsFromEnv.get(BASE_URL_NAME));
    }
    return false;
  }

  public RestTemplate getRestTemplate() {
    log.info("https start? {}", clusterUrlsAsString);
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
    if (clusterUrlsAsString != null && clusterUrlsAsString.length() > 0) {
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
    if (clusterApiAccessBase64Secret.isBlank()) {
      log.error(CLUSTER_API_ERR_117);
      throw new KlawException(CLUSTER_API_ERR_117);
    }

    Key hmacKey =
        new SecretKeySpec(
            Base64.decodeBase64(clusterApiAccessBase64Secret),
            SignatureAlgorithm.HS256.getJcaName());
    Instant now = Instant.now();

    return Jwts.builder()
        .claim("name", username)
        .claim("Role", "CACHE_ADMIN")
        .setSubject(username)
        .setId(UUID.randomUUID().toString())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(3L, ChronoUnit.MINUTES))) // expiry in 3 minutes
        .signWith(hmacKey)
        .compact();
  }
}
