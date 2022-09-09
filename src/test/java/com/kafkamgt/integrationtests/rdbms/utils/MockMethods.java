package com.kafkamgt.integrationtests.rdbms.utils;

import com.kafkamgt.uiapi.model.*;

public class MockMethods {

    private static int defaultTenantId =  101;

    public TeamModel getTeamModel(String teamName){
        TeamModel teamModel = new TeamModel();
        teamModel.setTeamname(teamName);
        teamModel.setTenantId(defaultTenantId);
        teamModel.setContactperson("Contact Person");
        teamModel.setTeammail(teamName+"@"+teamName+".com");
        teamModel.setTeamphone("0031003423432");

        return teamModel;
    }

    // Invalid mail id
    public TeamModel getTeamModelFailure(String teamName){
        TeamModel teamModel = new TeamModel();
        teamModel.setTeamname(teamName);
        teamModel.setTenantId(defaultTenantId);
        teamModel.setContactperson("Contact Person");
        teamModel.setTeammail("Invalidmailid");
        teamModel.setTeamphone("0031003423432");

        return teamModel;
    }

    public UserInfoModel getUserInfoModel(String username, String role, String team){
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
        kwClustersModel.setProtocol("PLAINTEXT");
        kwClustersModel.setClusterType(KafkaClustersType.kafka.value);

        return kwClustersModel;
    }

    public EnvModel getEnvModel(String envName) {
        EnvModel envModel = new EnvModel();

        envModel.setName(envName);
        envModel.setTenantId(101);
        envModel.setClusterId(1);
        envModel.setType(KafkaClustersType.kafka.value);
        envModel.setOtherParams("default.partitions=2,max.partitions=2,replication.factor=1,topic.prefix=,topic.suffix=");

        return envModel;
    }
}
