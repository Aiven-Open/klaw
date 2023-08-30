package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class TopicPromotionStatus extends BasePromotionStatus {

  private String topicName;
}
