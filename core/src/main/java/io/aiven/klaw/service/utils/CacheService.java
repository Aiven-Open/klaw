package io.aiven.klaw.service.utils;

import io.aiven.klaw.service.HighAvailabilityUtilsService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * This cache provides a standardised way with built in inter instance update to other caches when
 * working in a multi cluster deployment. Standard querying mechanisms are provided to ensure that
 * the most efficient use of the cache is used along with the ability to use Java Streams with the
 * output for complex queries.
 *
 * @param <T> The Entity Type that is to be stored in this Cache.
 */
@Slf4j
public class CacheService<T> {

  public static final String URL_SEPERATOR = "/";
  public static final String TENANT = "tenant";
  public static final String ID = "id";
  Map<Integer, Map<Integer, T>> cache;

  RestTemplate rest;

  private final String urlEndpoint;

  private final HighAvailabilityUtilsService utilsService;

  private List<String> clusterUrls;

  public CacheService(String endpoint, HighAvailabilityUtilsService utilsService) {
    // TODO an interface needs to be added to allow the passing in of different communication
    // methods (kafka/https/rabbitmq etc) to allow any org to use what they want for maintaining
    // cache.
    this.cache = new HashMap<>();
    this.urlEndpoint = "cache" + URL_SEPERATOR + endpoint;
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

  public List<T> getCacheAsList(Integer tenantId) {
    if (!cache.containsKey(tenantId)) {
      cache.put(tenantId, new HashMap<>());
    }
    return new ArrayList<>(cache.get(tenantId).values());
  }

  // Implement Interface here so sync can be done via kafka if wanted
  private void sendHighAvailabilityUpdate(int tenantId, Integer id, T entry) {

    if (isLazyLoaded()) {
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

    if (isLazyLoaded()) {
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
    return String.join(
        URL_SEPERATOR, url, TENANT, Integer.toString(tenantId), ID, Integer.toString(id));
  }

  private boolean isLazyLoaded() {

    if (rest == null || clusterUrls == null) {
      this.rest = utilsService.getRestTemplate();
      this.clusterUrls = utilsService.getHAClusterUrls();
    }
    return (rest == null && clusterUrls == null);
  }
}
