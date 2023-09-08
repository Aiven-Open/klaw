package io.aiven.klaw.service;

import java.net.InetAddress;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class HighAvailabilityUtilsService {

  @Autowired Environment environment;

  private static HttpComponentsClientHttpRequestFactory requestFactory = null;

  private static Map<String, String> baseUrlsMap;

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
      if (baseUrlsFromEnv.containsKey(BASE_URL_NAME)
          && basePath.contains(baseUrlsFromEnv.get(BASE_URL_NAME))) {
        return true;
      }
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
}
