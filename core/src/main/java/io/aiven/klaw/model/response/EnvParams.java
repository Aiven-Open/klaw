package io.aiven.klaw.model.response;

import java.util.List;
import lombok.Data;

@Data
public class EnvParams {
  private List<String> defaultPartitions;
  private List<String> partitionsList;
  private List<String> defaultRepFactor;
  private List<String> replicationFactorList;
  private List<String> topicPrefix;
  private List<String> topicSuffix;
  private List<String> topicRegex;
  private List<String> advancedTopicConfiguration;
}
