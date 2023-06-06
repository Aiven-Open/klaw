package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicDeleteRequestModel {
  @NotNull private String topicName;

  @NotNull private String env;

  @NotNull private boolean deleteAssociatedSchema;
}
