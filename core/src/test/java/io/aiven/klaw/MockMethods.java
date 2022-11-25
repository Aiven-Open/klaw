package io.aiven.klaw;

import io.aiven.klaw.model.*;
import io.aiven.klaw.model.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.KafkaClustersType;

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
    userInfoModel.setUserPassword("user");
    userInfoModel.setRole(role);
    userInfoModel.setTeam(team);
    userInfoModel.setFullname("New User");
    userInfoModel.setMailid("test@test.com");

    return userInfoModel;
  }

  public KwTenantModel getTenantModel(String nltenant) {
    KwTenantModel kwTenantModel = new KwTenantModel();
    kwTenantModel.setTenantName(nltenant);
    kwTenantModel.setTenantDesc("tenant desc");
    return kwTenantModel;
  }

  public KwClustersModel getClusterModel(String dev_cluster) {
    KwClustersModel kwClustersModel = new KwClustersModel();
    kwClustersModel.setClusterName(dev_cluster);
    kwClustersModel.setBootstrapServers("localhost:9092");
    kwClustersModel.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
    kwClustersModel.setClusterType(KafkaClustersType.KAFKA.value);
    kwClustersModel.setKafkaFlavor("Apache Kafka");

    return kwClustersModel;
  }

  public EnvModel getEnvModel(String envName) {
    EnvModel envModel = new EnvModel();

    envModel.setName(envName);
    envModel.setTenantId(101);
    envModel.setClusterId(1);
    envModel.setClusterType(KafkaClustersType.KAFKA);
    envModel.setOtherParams(
        "default.partitions=2,max.partitions=2,replication.factor=1,topic.prefix=,topic.suffix=");

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
