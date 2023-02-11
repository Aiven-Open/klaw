package io.aiven.klaw.clusterapi.models.confluentcloud;

import java.util.ArrayList;
import lombok.Data;

@Data
public class TopicCreateRequest {
  public String topic_name;
  public int replication_factor;
  public int partitions_count;
  public ArrayList<Config> configs;
}
