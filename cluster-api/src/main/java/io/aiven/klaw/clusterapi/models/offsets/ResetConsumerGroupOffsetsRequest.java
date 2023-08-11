package io.aiven.klaw.clusterapi.models.offsets;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResetConsumerGroupOffsetsRequest {

  @NotNull OffsetResetType offsetResetType;

  @NotNull String topicName;

  @NotNull String consumerGroup;

  Long consumerGroupResetTimestampMilliSecs;
}
