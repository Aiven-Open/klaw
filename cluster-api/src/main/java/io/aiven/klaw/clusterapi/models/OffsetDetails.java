package io.aiven.klaw.clusterapi.models;

import lombok.Data;

@Data
public class OffsetDetails {
  private String topicPartitionId;

  private String currentOffset;

  private String endOffset;

  private String lag;
}
