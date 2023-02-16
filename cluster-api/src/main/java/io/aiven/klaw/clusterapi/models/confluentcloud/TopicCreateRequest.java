package io.aiven.klaw.clusterapi.models.confluentcloud;

import java.util.ArrayList;
import lombok.Data;

@Data
public class TopicCreateRequest {
  private String topic_name;
  private int replication_factor;
  private int partitions_count;
  private ArrayList<Config> configs;
}
