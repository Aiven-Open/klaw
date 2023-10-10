package io.aiven.klaw.service.interfaces;

public interface HAMessagingServiceI {

  void sendUpdate(String entityType, int tenantId, Object entry);

  void sendRemove(String entityType, int tenantId, int id);
}
