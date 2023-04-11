package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicInfo;
import lombok.Data;

@Data
public class TopicDetailsPerEnv {
  private boolean topicExists;
  private String error;
  private String topicId;
  private TopicInfo topicContents;
}
