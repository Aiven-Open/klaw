package io.aiven.klaw.service.utils;

import io.aiven.klaw.service.HARestMessagingService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;

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

  Map<Integer, Map<Integer, T>> cache;

  private final String urlEndpoint;
  private final String entityType;

  private final HARestMessagingService utilsService;

  public CacheService(String entityType, HARestMessagingService utilsService) {
    // TODO an interface needs to be added to allow the passing in of different communication
    // methods (kafka/https/rabbitmq etc) to allow any org to use what they want for maintaining
    // cache.
    this.cache = new HashMap<>();
    this.urlEndpoint = "cache";
    this.entityType = entityType;
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

  private void sendHighAvailabilityUpdate(int tenantId, Integer id, T entry) {
    utilsService.sendUpdate(entityType, tenantId, id, entry);
  }

  private void sendHighAvailabilityRemove(int tenantId, Integer id) {
    utilsService.sendRemove(entityType, tenantId, id);
  }
}
