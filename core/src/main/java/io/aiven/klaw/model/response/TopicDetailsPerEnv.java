package io.aiven.klaw.model.response;

import io.aiven.klaw.model.TopicBaseConfig;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicDetailsPerEnv {

  @NotNull private boolean topicExists;

  private String error;

  private String topicId;

  private TopicBaseConfig topicContents;
}
