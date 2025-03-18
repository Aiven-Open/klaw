package io.aiven.klaw;

import io.aiven.klaw.model.*;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.KwClustersModel;
import io.aiven.klaw.model.requests.KwRolesPermissionsModel;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import io.aiven.klaw.model.requests.TeamModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.EnvParams;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockMethods {

  private static int defaultTenantId = 101;

  public TeamModel getTeamModel(String teamName) {
    TeamModel teamModel = new TeamModel();
    teamModel.setTeamname(teamName);
    teamModel.setTenantId(defaultTenantId);
    teamModel.setContactperson("Contact Person");
    teamModel.setTeammail(teamName + "@" + teamName + ".com");
    teamModel.setTeamphone("0031003423432");

    return teamModel;
  }

  // Invalid mail id
  public TeamModel getTeamModelFailure(String teamName) {
    TeamModel teamModel = new TeamModel();
    teamModel.setTeamname(teamName);
    teamModel.setTenantId(defaultTenantId);
    teamModel.setContactperson("Contact Person");
    teamModel.setTeammail("Invalidmailid");
    teamModel.setTeamphone("0031003423432");

    return teamModel;
  }

  public UserInfoModel getUserInfoModel(String username, String role, String team) {
    UserInfoModel userInfoModel = new UserInfoModel();
    userInfoModel.setUsername(username);
    userInfoModel.setUserPassword("userpasS2@");
    userInfoModel.setRole(role);
    userInfoModel.setTeamId(1001);
    userInfoModel.setFullname("New User");
    userInfoModel.setMailid("test@test.com");

    return userInfoModel;
  }

  public RegisterUserInfoModel getRegisterUserInfoModel(String username, String role) {
    RegisterUserInfoModel userInfoModel = new RegisterUserInfoModel();
    userInfoModel.setUsername(username);
    userInfoModel.setPwd("testpwD32@");
    userInfoModel.setRole(role);
    userInfoModel.setTeamId(1001);
    userInfoModel.setFullname("New User");
    userInfoModel.setMailid("test@test.com");

    return userInfoModel;
  }

  public UserInfoModel getUserInfoModelSwitchTeams(
      String username, String role, int teamId, int switchTeamSize) {
    UserInfoModel userInfoModel = new UserInfoModel();
    userInfoModel.setUsername(username);
    userInfoModel.setUserPassword("userpasS2#");
    userInfoModel.setRole(role);
    userInfoModel.setTeamId(teamId);
    userInfoModel.setFullname("New User");
    userInfoModel.setMailid("test@test.com");
    userInfoModel.setSwitchTeams(true);
    Set<Integer> teamIds = new HashSet<>();
    if (switchTeamSize > 0) teamIds.add(1001); // INFRATEAM
    if (switchTeamSize > 1) teamIds.add(1002); // STAGINGTEAM

    userInfoModel.setSwitchAllowedTeamIds(teamIds);

    return userInfoModel;
  }

  public KwTenantModel getTenantModel(String nltenant) {
    KwTenantModel kwTenantModel = new KwTenantModel();
    kwTenantModel.setTenantName(nltenant);
    kwTenantModel.setTenantDesc("tenant desc");
    kwTenantModel.setContactPerson("Contact person");
    kwTenantModel.setOrgName("Organization");
    return kwTenantModel;
  }

  public KwClustersModel getKafkaClusterModel(String dev_cluster) {
    KwClustersModel kwClustersModel = new KwClustersModel();
    kwClustersModel.setClusterName(dev_cluster);
    kwClustersModel.setBootstrapServers("localhost:9092");
    kwClustersModel.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
    kwClustersModel.setClusterType(KafkaClustersType.KAFKA);
    kwClustersModel.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA);
    kwClustersModel.setAssociatedServers("https://localhost:12695");
    kwClustersModel.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA);

    return kwClustersModel;
  }

  public KwClustersModel getSchemaClusterModel(String dev_cluster) {
    KwClustersModel kwClustersModel = new KwClustersModel();
    kwClustersModel.setClusterName(dev_cluster);
    kwClustersModel.setBootstrapServers("localhost:8081");
    kwClustersModel.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
    kwClustersModel.setClusterType(KafkaClustersType.SCHEMA_REGISTRY);
    kwClustersModel.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA);
    kwClustersModel.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA);

    return kwClustersModel;
  }

  public KwClustersModel getAivenKafkaClusterModel(String dev_cluster) {
    KwClustersModel kwClustersModel = new KwClustersModel();
    kwClustersModel.setClusterName(dev_cluster);
    kwClustersModel.setBootstrapServers("localhost:9092");
    kwClustersModel.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
    kwClustersModel.setClusterType(KafkaClustersType.KAFKA);
    kwClustersModel.setKafkaFlavor(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA);

    return kwClustersModel;
  }

  public EnvModel getEnvModel(String envName) {
    EnvModel envModel = new EnvModel();

    envModel.setName(envName);
    envModel.setTenantId(101);
    envModel.setClusterId(1);
    envModel.setType(KafkaClustersType.KAFKA.value);

    EnvParams params = new EnvParams();
    params.setDefaultPartitions("2");
    params.setMaxPartitions("2");
    params.setPartitionsList(List.of("1 (default)", "2"));
    params.setDefaultRepFactor("1");
    params.setMaxRepFactor("2");
    params.setReplicationFactorList(List.of("1", "2"));
    envModel.setParams(params);
    return envModel;
  }

  protected KwRolesPermissionsModel[] getPermissions(String role) {
    KwRolesPermissionsModel[] permissionsList = new KwRolesPermissionsModel[3];
    KwRolesPermissionsModel kwRolesPermissionsModel1 = new KwRolesPermissionsModel();
    kwRolesPermissionsModel1.setRolePermission(role + "-----ADD_EDIT_DELETE_CLUSTERS");
    kwRolesPermissionsModel1.setPermissionEnabled("true");
    permissionsList[0] = kwRolesPermissionsModel1;

    KwRolesPermissionsModel kwRolesPermissionsModel2 = new KwRolesPermissionsModel();
    kwRolesPermissionsModel2.setRolePermission(role + "-----ADD_EDIT_DELETE_ENVS");
    kwRolesPermissionsModel2.setPermissionEnabled("true");
    permissionsList[1] = kwRolesPermissionsModel2;

    KwRolesPermissionsModel kwRolesPermissionsModel3 = new KwRolesPermissionsModel();
    kwRolesPermissionsModel3.setRolePermission(role + "-----VIEW_TOPICS");
    kwRolesPermissionsModel3.setPermissionEnabled("false");
    permissionsList[2] = kwRolesPermissionsModel3;
    return permissionsList;
  }
}
