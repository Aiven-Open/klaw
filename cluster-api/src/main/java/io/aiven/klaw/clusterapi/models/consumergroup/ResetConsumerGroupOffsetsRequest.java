package io.aiven.klaw.clusterapi.models.consumergroup;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetConsumerGroupOffsetsRequest implements Serializable {

  @NotNull private OffsetResetType offsetResetType;

  @NotNull private String topicName;

  @NotNull private String consumerGroup;

  private Long consumerGroupResetTimestampMilliSecs;
}
