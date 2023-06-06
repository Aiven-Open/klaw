package io.aiven.klaw.model.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TopicClaimRequestModel {
  @NotNull private String topicName;

  @NotNull private String env;
}
