package io.aiven.klaw.constants;

import java.util.List;
import java.util.Map;

public class TestConstants {
  public static final String ENV_ID = "ENV_ID";
  public static final String USERNAME = "USERNAME";
  public static final String ENV_NAME = "ENV_NAME";
  public static final String ACLS_COUNT = "ACLS_COUNT";
  public static final String TOPICS_COUNT = "TOPICS_COUNT";
  public static final int TENANT_ID = 1;
  public static final String TEAM_NAME = "TEAM_NAME";
  public static final String KW_REPORTS_LOCATION = "KW_REPORTS_LOCATION";
  public static final int TEAM_ID = 1;
  public static final String X_AXIS_LABEL = "X_AXIS_LABEL";
  public static final String Y_AXIS_LABEL = "Y_AXIS_LABEL";
  public static final String TOPIC_NAME = "TOPIC_NAME";
  public static final String CONSUMER_GROUP = "CONSUMER_GROUP";

  public static final List<Map<String, String>> TOPICS_COUNT_BY_ENV_ID =
      List.of(
              Map.of(
                      MapConstants.CLUSTER_KEY, ENV_ID,
                      MapConstants.TOPICS_COUNT_KEY, TOPICS_COUNT));
  public static final List<Map<String, String>> ACLS_COUNT_BY_ENV_ID =
      List.of(
          Map.of(
              MapConstants.CLUSTER_KEY, ENV_ID,
              MapConstants.ACLS_COUNT_KEY, ACLS_COUNT));
}
