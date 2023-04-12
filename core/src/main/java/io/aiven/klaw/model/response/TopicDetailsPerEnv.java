package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicDetailsPerEnv {

  @NotNull private boolean topicExists;

  private String error;

  private String topicId;

  private TopicInfo topicContents;
}
