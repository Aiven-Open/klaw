package io.aiven.klaw.constants;

import io.aiven.klaw.service.CommonUtilsService;
import java.io.File;
import java.util.List;

public class TestConstants {
  public static final String ENV_ID = "ENV_ID";
  public static final String USERNAME = "USERNAME";
  public static final String PASSWORD = "PASSWORD";
  public static final String ENV_NAME = "ENV_NAME";
  public static final String ACLS_COUNT = "ACLS_COUNT";
  public static final String TOPICS_COUNT = "TOPICS_COUNT";
  public static final int TENANT_ID = 1;
  public static final String TEAM_NAME = "TEAM_NAME";
  public static final String KW_REPORTS_LOCATION = File.separator + "target";
  public static final int TEAM_ID = 1;
  public static final String X_AXIS_LABEL = "X_AXIS_LABEL";
  public static final String Y_AXIS_LABEL = "Y_AXIS_LABEL";
  public static final String TOPIC_NAME = "TOPIC_NAME";
  public static final String CONSUMER_GROUP = "CONSUMER_GROUP";
  public static final String TENANT_NAME = "TENANT_NAME";
  public static final String ENV_STATUS = "ENV_STATUS";
  public static final int CLUSTER_ID = 1;
  public static final String CAPTCHA_RESPONSE = "CAPTCHA_RESPONSE";
  public static final String ROLE = "ROLE";
  public static final String PERMISSION = "PERMISSION";
  public static final String REGISTRATION_ID = "REGISTRATION_ID";
  public static final List<CommonUtilsService.ChartsOverviewItem<String, String>>
      TOPICS_COUNT_BY_ENV_ID =
          List.of(
              CommonUtilsService.ChartsOverviewItem.of(MapConstants.CLUSTER_KEY, ENV_ID),
              CommonUtilsService.ChartsOverviewItem.of(
                  MapConstants.TOPICS_COUNT_KEY, TOPICS_COUNT));

  public static final List<CommonUtilsService.ChartsOverviewItem<String, Integer>>
      TOPICS_COUNT_STRING_BY_ENV_ID_INT =
          List.of(
              CommonUtilsService.ChartsOverviewItem.of(MapConstants.CLUSTER_KEY, 1),
              CommonUtilsService.ChartsOverviewItem.of(ENV_ID, 123));

  public static final List<CommonUtilsService.ChartsOverviewItem<String, Integer>>
      ACLS_COUNT_BY_ENV_ID_INTEGERE =
          List.of(
              CommonUtilsService.ChartsOverviewItem.of(ENV_ID, 123),
              CommonUtilsService.ChartsOverviewItem.of(MapConstants.ACLS_COUNT_KEY, 2));

  public static final List<CommonUtilsService.ChartsOverviewItem<String, String>>
      ACLS_COUNT_BY_ENV_ID =
          List.of(
              CommonUtilsService.ChartsOverviewItem.of(MapConstants.CLUSTER_KEY, ENV_ID),
              CommonUtilsService.ChartsOverviewItem.of(MapConstants.ACLS_COUNT_KEY, ACLS_COUNT));
}
