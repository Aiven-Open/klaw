package io.aiven.klaw.listeners;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.model.EntityType;
import io.aiven.klaw.model.KwMetadataUpdates;
import java.util.Objects;
import javax.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetadataListener implements ApplicationContextAware {

  private static ApplicationContext context;

  private MetadataListener(ApplicationContext context) {
    MetadataListener.context = context;
  }

  public static <T> T getBean(Class<T> clazz) throws BeansException {

    Assert.state(
        context != null, "Spring context in the MetadataListener is not been initialized yet!");
    return context.getBean(clazz);
  }

  @PostPersist
  @PostUpdate
  @PostRemove
  private void afterAnyUpdate(KwMetadataUpdates metadataUpdates) {
    log.info("MetadataUpdates : " + metadataUpdates);
    if (Objects.equals(metadataUpdates.getEntityType(), EntityType.TEAM.name()))
      getBean(ManageDatabase.class)
          .loadTenantTeamsForOneTenant(null, metadataUpdates.getTenantId());
  }

  @PrePersist
  @PreUpdate
  @PreRemove
  private void beforeAnyUpdate(KwMetadataUpdates metadataUpdates) {
    log.info("KwMetadataUpdates About to update/delete rec: " + metadataUpdates);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    context = applicationContext;
  }
}
