package io.aiven.klaw.service;

import io.aiven.klaw.model.response.SyncTopicsList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchemaRegistrySyncControllerService {
  public SyncTopicsList getSchemasFromCluster(
      String envId, String pageNo, String currentPage, String topicNameSearch) {
    return null;
  }
}
