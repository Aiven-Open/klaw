package io.aiven.klaw.model.response;

import java.util.List;
import lombok.Data;

@Data
public class SyncTopicsList {
  private List<TopicSyncResponseModel> resultSet;
  private int allTopicsCount;
  private int invalidTopicNamesCount;
}
