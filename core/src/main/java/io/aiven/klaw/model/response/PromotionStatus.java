package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class PromotionStatus {
  private String status;

  private String sourceEnv;

  private String targetEnv;

  private String targetEnvId;

  private String topicName;

  private String error;
}
