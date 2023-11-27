package io.aiven.klaw.model.cluster.consumergroup;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OffsetDetails {
  @NotNull private String topicPartitionId;

  @NotNull private String currentOffset;

  @NotNull private String endOffset;

  @NotNull private String lag;
}
