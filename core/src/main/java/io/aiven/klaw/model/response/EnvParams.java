package io.aiven.klaw.model.response;

import java.util.List;
import lombok.Data;

@Data
public class EnvParams {
  private String defaultPartitions;
  private String maxPartitions;
  private List<String> partitionsList;
  private String defaultRepFactor;
  private String maxRepFactor;
  private List<String> replicationFactorList;
  private List<String> topicPrefix;
  private List<String> topicSuffix;
  private List<String> topicRegex;
  private boolean applyRegex;
}
