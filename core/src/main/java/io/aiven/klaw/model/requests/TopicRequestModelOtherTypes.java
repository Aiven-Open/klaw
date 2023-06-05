package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicRequestModelOtherTypes {
  @NotNull private String topicName;

  @NotNull private String env;

  private boolean deleteAssociatedSchema;
}
