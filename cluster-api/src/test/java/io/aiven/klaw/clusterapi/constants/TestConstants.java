package io.aiven.klaw.clusterapi.constants;

import java.util.Map;

public class TestConstants {
  public static final String ENVIRONMENT = "ENVIRONMENT";
  public static final String CLUSTER_IDENTIFICATION = "CLUSTER_IDENTIFICATION";
  public static final String TOPIC_NAME = "TOPIC_NAME";
  public static final int MULTIPLE_PARTITIONS = 5;
  public static final int SINGLE_PARTITION = 1;
  public static final short REPLICATION_FACTOR = 1;
  public static final Map<String, String> ADVANCED_TOPIC_CONFIGURATION = Map.of("topic", "config");
  public static final String CONSUMER_GROUP_ID = "consumerGroupId";
  public static final String CLUSTER_NAME = "clusterName";
}
