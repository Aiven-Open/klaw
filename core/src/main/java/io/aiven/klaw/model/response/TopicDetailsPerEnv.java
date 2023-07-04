package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicBaseInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicDetailsPerEnv {

  @NotNull private boolean topicExists;

  private String error;

  private String topicId;

  private TopicBaseInfo topicContents;
}
