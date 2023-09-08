package io.aiven.klaw.service;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class CacheService<T> {

  public static final String URL_SEPERATOR = "/";
  public static final String TENANT = "tenant";
  Map<Integer, Map<Integer, T>> cache;

  RestTemplate rest;

  private String urlEndpoint;

  private HighAvailabilityUtilsService utilsService;

  private String clusterUrlsAsString;

  private List<String> clusterUrls;

  public CacheService(
      String endpoint, String clusterUrlsAsString, HighAvailabilityUtilsService utilsService) {
    // TODO an interface needs to be added to allow the passing in of different communication
    // methods (kafka/https/rabbitmq etc) to allow any org to use what they want for maintaining
    // cache.
    this.cache = new HashMap<>();
    this.urlEndpoint = "cache" + URL_SEPERATOR + endpoint;
    this.clusterUrlsAsString = clusterUrlsAsString;
    this.utilsService = utilsService;
  }

  public T add(int tenantId, Integer id, T entry) {
    return addOrUpdate(tenantId, id, entry, false);
  }

  public void addAll(int tenantId, Map<Integer, T> entries) {
    if (entries == null) {
      entries = new HashMap<>();
    }
    getCache(tenantId).putAll(entries);
  }

  public T remove(int tenantId, Integer id, boolean isLocalUpdate) {
    log.info("remove id {}", id);
    if (!isLocalUpdate) {
      sendHighAvailabilityRemove(tenantId, id);
    }
    return getCache(tenantId).remove(id);
  }

  public Map<Integer, T> removeCache(int tenantId) {
    return cache.remove(tenantId);
  }

  public T update(int tenantId, Integer id, T entry) {
    return addOrUpdate(tenantId, id, entry, false);
  }

  public T addOrUpdate(int tenantId, Integer id, T entry, boolean isLocalUpdate) {
    log.info("addOrUpdate {}", entry);
    getCache(tenantId).put(id, entry);
    if (!isLocalUpdate) {
      sendHighAvailabilityUpdate(tenantId, id, entry);
    }
    return entry;
  }

  public Optional<T> get(int tenantId, Integer id) {
    return Optional.ofNullable(getCache(tenantId).get(id));
  }

  public Map<Integer, T> getCache(Integer tenantId) {
    if (!cache.containsKey(tenantId)) {
      cache.put(tenantId, new HashMap<>());
    }
    return cache.get(tenantId);
  }

  // Implement Interface here so sync can be done via kafka if wanted
  private void sendHighAvailabilityUpdate(int tenantId, Integer id, T entry) {

    if (!isLazyLoaded()) {
      throw new RuntimeException("Unable to load High Availability Cache");
    }

    for (String url : clusterUrls) {
      try {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<T> request = new HttpEntity<>(entry, headers);
        rest.exchange(
            getUrl(url, tenantId, id),
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<>() {});

      } catch (RestClientException clientException) {
        log.error(
            "Exception while sending HA updates to another instance {} in the cluster.",
            url,
            clientException);
      }
    }
  }

  private void sendHighAvailabilityRemove(int tenantId, Integer id) {

    if (!isLazyLoaded()) {
      throw new RuntimeException("Unable to load High Availability Cache");
    }

    for (String url : clusterUrls) {
      try {
        rest.delete(getUrl(url, tenantId, id));
      } catch (RestClientException clientException) {
        log.error(
            "Exception while sending HA updates to another instance {} in the cluster.",
            url,
            clientException);
      }
    }
  }

  private String getUrl(String url, int tenantId, Integer id) {
    if (url.endsWith(URL_SEPERATOR)) {
      url = url + urlEndpoint;
    } else {
      url = url + URL_SEPERATOR + urlEndpoint;
    }
    return url
        + URL_SEPERATOR
        + TENANT
        + URL_SEPERATOR
        + tenantId
        + URL_SEPERATOR
        + "id"
        + URL_SEPERATOR
        + id;
  }

  private boolean isLazyLoaded() {

    if (rest == null || clusterUrls == null) {
      this.rest = utilsService.getRestTemplate();
      this.clusterUrls = utilsService.getHAClusterUrls();
    }
    return (rest != null || clusterUrls != null);
  }
}
