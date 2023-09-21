package io.aiven.klaw.service;

public interface HAMessagingServiceI<T> {

  public void sendUpdate(String entityType, int tenantId, int id, Object entry);

  public void sendRemove(String entityType, int tenantId, int id);
}
