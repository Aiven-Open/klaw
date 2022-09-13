package com.kafkamgt.uiapi;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.model.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.tomcat.util.codec.binary.Base64;

import org.springframework.http.HttpHeaders;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
public class UtilMethods {

    public HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(StandardCharsets.US_ASCII));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }

    public List<MessageSchema> getMSchemas(){
        List<MessageSchema> listMSchemas = new ArrayList<>();
        MessageSchema mSchema = new MessageSchema();
        mSchema.setEnvironment("DEV");
        mSchema.setSchemaversion("1.0");
        mSchema.setTopicname("testtopic");
        mSchema.setSchemafull("schema");
        mSchema.setTenantId(101);
        listMSchemas.add(mSchema);

        return listMSchemas;
    }

    public UserInfoModel getUserInfoMock(){
        UserInfoModel userInfo = new UserInfoModel();
        userInfo.setTeam("Seahorses");
        userInfo.setUsername("kwusera");
        userInfo.setTeamId(101);
        userInfo.setTenantId(101);
        userInfo.setTenantName("default");
        userInfo.setRole("USER");
        userInfo.setMailid("test@test.com");
        userInfo.setFullname("My full name");
        userInfo.setUserPassword("mypwadasdas");

        return userInfo;
    }

    public UserInfo getUserInfoMockDao(){
        UserInfo userInfo = new UserInfo();
        userInfo.setTeamId(3);
        userInfo.setUsername("kwusera");
        userInfo.setTenantId(101);
        userInfo.setRole("USER");

        return userInfo;
    }

    public List<UserInfo> getUserInfoList(String username, String role){
        List<UserInfo> userInfoList = new ArrayList<>();
        UserInfo userInfo = new UserInfo();
        userInfo.setTeamId(3);
        userInfo.setUsername(username);
        userInfo.setRole(role);
        userInfoList.add(userInfo);

        return userInfoList;
    }

    public List<UserInfoModel> getUserInfoListModel(String username, String role){
        List<UserInfoModel> userInfoList = new ArrayList<>();
        UserInfoModel userInfo = new UserInfoModel();
        userInfo.setTeam("Seahorses");
        userInfo.setUsername(username);
        userInfo.setRole(role);
        userInfoList.add(userInfo);

        return userInfoList;
    }

    public List<ActivityLog> getLogs(){
        List<ActivityLog> activityLogs = new ArrayList<>();
        ActivityLog activityLog = new ActivityLog();
        activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
        activityLogs.add(activityLog);
        return activityLogs;
    }

    public Topic getTopic(String topicName){
        Topic topic = new Topic();
        topic.setTeamId(3);
        topic.setTopicname(topicName);

        return topic;
    }

    public int getSchemaRequestPk(){

        return 1001;
    }

    public List<Topic> getTopics() {
        List<Topic> allTopicReqs = new ArrayList<>();
        Topic topicRequest = new Topic();
        topicRequest.setEnvironment("1");
        topicRequest.setTopicname("testtopic");

        topicRequest.setTeamId(3);
        topicRequest.setEnvironment("1");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<Topic> getTopics(String topicName) {
        List<Topic> allTopicReqs = new ArrayList<>();
        Topic topicRequest = new Topic();
        topicRequest.setEnvironment("1");
        topicRequest.setTopicname(topicName);

        topicRequest.setTeamId(3);
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<Acl> getAcls() {
        List<Acl> allTopicReqs = new ArrayList<>();
        Acl topicRequest = new Acl();
        topicRequest.setTeamId(3);
        topicRequest.setTopictype("Producer");
        topicRequest.setTenantId(101);
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<AclInfo> getAclInfoList() {
        List<AclInfo> allTopicReqs = new ArrayList<>();
        AclInfo topicRequest = new AclInfo();
        topicRequest.setTeamname("Seahorses");
        topicRequest.setTopictype("Producer");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<HashMap<String, String>> getClusterAcls(){
        Set<HashMap<String,String>> acls = new HashSet<>();

        HashMap<String,String> aclbindingMap = new HashMap<>();

        aclbindingMap.put("host","1.1.1.1");
        aclbindingMap.put("principle", "User:*");
        aclbindingMap.put("operation", "READ");
        aclbindingMap.put("permissionType", "ALLOW");
        aclbindingMap.put("resourceType", "GROUP");
        aclbindingMap.put("resourceName", "myconsumergroup1");
        acls.add(aclbindingMap);

        aclbindingMap = new HashMap<>();
        aclbindingMap.put("host","2.1.2.1");
        aclbindingMap.put("principle", "User:*");
        aclbindingMap.put("operation", "READ");
        aclbindingMap.put("permissionType", "ALLOW");
        aclbindingMap.put("resourceType", "TOPIC");
        aclbindingMap.put("resourceName", "testtopic1");
        acls.add(aclbindingMap);

        aclbindingMap = new HashMap<>();
        aclbindingMap.put("host","2.1.2.1");
        aclbindingMap.put("principle", "User:*");
        aclbindingMap.put("operation", "READ");
        aclbindingMap.put("permissionType", "ALLOW");
        aclbindingMap.put("resourceType", "GROUP");
        aclbindingMap.put("resourceName", "mygrp1");
        acls.add(aclbindingMap);

        return new ArrayList<>(acls);
    }

    public List<HashMap<String, String>> getClusterAcls2(){
        Set<HashMap<String,String>> acls = new HashSet<>();

        HashMap<String,String> aclbindingMap = new HashMap<>();

        aclbindingMap.put("host","1.1.1.1");
        aclbindingMap.put("principle", "User:*");
        aclbindingMap.put("operation", "READ");
        aclbindingMap.put("permissionType", "ALLOW");
        aclbindingMap.put("resourceType", "GROUP");
        aclbindingMap.put("resourceName", "myconsumergroup1");
        acls.add(aclbindingMap);

        aclbindingMap = new HashMap<>();
        aclbindingMap.put("host","2.1.2.1");
        aclbindingMap.put("principle", "User:*");
        aclbindingMap.put("operation", "READ");
        aclbindingMap.put("permissionType", "ALLOW");
        aclbindingMap.put("resourceType", "TOPIC");
        aclbindingMap.put("resourceName", "testtopic");
        acls.add(aclbindingMap);

        aclbindingMap = new HashMap<>();
        aclbindingMap.put("host","2.1.2.1");
        aclbindingMap.put("principle", "User:*");
        aclbindingMap.put("operation", "READ");
        aclbindingMap.put("permissionType", "ALLOW");
        aclbindingMap.put("resourceType", "GROUP");
        aclbindingMap.put("resourceName", "mygrp1");
        acls.add(aclbindingMap);

        return new ArrayList<>(acls);
    }

    public Set<HashMap<String, String>> getAclsMock(){
        Set<HashMap<String, String>> listAcls = new HashSet<>();
        HashMap<String, String> hsMp = new HashMap<>();
        hsMp.put("key","val");
        listAcls.add(hsMp);

        hsMp = new HashMap<>();
        hsMp.put("key","val");
        listAcls.add(hsMp);
        return listAcls;
    }

    public List<Acl> getAclsForDelete() {
        List<Acl> allTopicReqs = new ArrayList<>();
        Acl topicRequest = new Acl();
        topicRequest.setTopicname("testtopic");
        topicRequest.setConsumergroup("congrp");
        topicRequest.setEnvironment("1");
        topicRequest.setAclip("12.22.126.21");
        topicRequest.setTopictype("Producer");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<Acl> getAllAcls() {
        List<Acl> allTopicReqs = new ArrayList<>();
        Acl acl = new Acl();
        acl.setTeamId(3);
        acl.setTopictype("Producer");
        allTopicReqs.add(acl);

        acl = new Acl();
        acl.setTopicname("testtopic");
        acl.setConsumergroup("congrp");
        acl.setEnvironment("1");
        acl.setAclip("12.22.126.21");
        acl.setTopictype("Producer");
        acl.setOtherParams("101");
        allTopicReqs.add(acl);

        return allTopicReqs;
    }

    public List<Team> getTeams() {
        List<Team> allTopicReqs = new ArrayList<>();
        Team team = new Team();
        team.setTeamname("Seahorses");
        team.setTeamId(101);
        team.setContactperson("Contact Person");
        team.setTenantId(101);
        team.setTeamphone("3142342343242");
        team.setTeammail("test@test.com");

        allTopicReqs.add(team);
        return allTopicReqs;
    }

    public List<TeamModel> getTeamsModel() {
        List<TeamModel> allTopicReqs = new ArrayList<>();
        TeamModel team = new TeamModel();
        team.setTeamname("Seahorses");
        allTopicReqs.add(team);
        return allTopicReqs;
    }

    public List<TopicRequest> getTopicRequests() {
        List<TopicRequest> allTopicReqs = new ArrayList<>();
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTeamId(3);
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<TopicRequestModel> getTopicRequestsModel() {
        List<TopicRequestModel> allTopicReqs = new ArrayList<>();
        TopicRequestModel topicRequest = new TopicRequestModel();
        topicRequest.setTeamname("Seahorses");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public TopicRequest getTopicRequest(int topicId) {
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicid(topicId);
        topicRequest.setTeamId(1001); // INFRATEAM
        topicRequest.setUsername("kwusera");
        topicRequest.setTopicname("testtopic" + topicId);
        topicRequest.setTopicpartitions(2);
        topicRequest.setReplicationfactor("1");
        topicRequest.setEnvironment("1");
        topicRequest.setTopictype("Create");
        topicRequest.setDescription("Test desc");
        topicRequest.setTenantId(101);
        return topicRequest;
    }

    public TopicRequestModel getTopicRequestModel(int topicId) {
        TopicRequestModel topicRequest = new TopicRequestModel();
        topicRequest.setTopicid(topicId);
        topicRequest.setTeamId(1001); // INFRATEAM
        topicRequest.setTeamname("INFRAMTEAM");
        topicRequest.setUsername("kwusera");
        topicRequest.setTopicname("testtopic" + topicId);
        topicRequest.setTopicpartitions(2);
        topicRequest.setReplicationfactor("1");
        topicRequest.setEnvironment("1");
        topicRequest.setTopictype("Create");
        topicRequest.setDescription("Test desc");
        return topicRequest;
    }

    public AclRequests getAclRequest(String topicName) {
        AclRequests aclRequest = new AclRequests();
        aclRequest.setTeamId(3);
        aclRequest.setEnvironment("1");
        aclRequest.setTopicname(topicName);
        aclRequest.setUsername("kwusera");
        aclRequest.setTopictype("Consumer");
        aclRequest.setAclType("Delete");
        aclRequest.setConsumergroup("congroup1");
        aclRequest.setAcl_ip("10.11.112.113");
        aclRequest.setAclPatternType("LITERAL");
        aclRequest.setOtherParams("101");
        aclRequest.setTenantId(101);
        return aclRequest;
    }

    public AclRequests getAclRequestCreate(String topicName) {
        AclRequests aclRequest = new AclRequests();
        aclRequest.setTeamId(3);
        aclRequest.setEnvironment("1");
        aclRequest.setTopicname(topicName);
        aclRequest.setUsername("kwusera");
        aclRequest.setTopictype("Consumer");
        aclRequest.setAclType("Create");
        aclRequest.setConsumergroup("congroup1");
        aclRequest.setAcl_ip("10.11.112.113");
        aclRequest.setAclPatternType("LITERAL");
        return aclRequest;
    }

    public List<SchemaRequestModel> getSchemaRequests() {
        List<SchemaRequestModel> schemaList = new ArrayList<>();
        SchemaRequestModel schemaRequest = new SchemaRequestModel();
        schemaRequest.setEnvironment("1");
        schemaRequest.setTeamname("Seahorses");
        schemaRequest.setUsername("kwusera");
        schemaRequest.setSchemafull("schemafdsfsd");
        schemaRequest.setTeamId(1001);
        schemaRequest.setRemarks("pls approve");
        schemaList.add(schemaRequest);
        return schemaList;
    }

    public List<SchemaRequest> getSchemaRequestsDao() {
        List<SchemaRequest> schemaList = new ArrayList<>();
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setEnvironment("1");
        schemaRequest.setTeamId(3);
        schemaRequest.setUsername("kwusera");
        schemaRequest.setSchemafull("schema");
        schemaRequest.setTenantId(101);
        schemaList.add(schemaRequest);
        return schemaList;
    }

    public List<AclRequestsModel> getAclRequestsModel() {
        List<AclRequestsModel> aclRequests = new ArrayList<>();
        AclRequestsModel aclRequests1 = new AclRequestsModel();
        aclRequests1.setTeamname("Seahorses");
        aclRequests1.setRequestingteam(2);
        aclRequests1.setAclType("Create");
        aclRequests1.setAclPatternType("LITERAL");
        aclRequests.add(aclRequests1);
        return aclRequests;
    }

    public List<AclRequests> getAclRequests() {
        List<AclRequests> aclRequests = new ArrayList<>();
        AclRequests aclRequests1 = new AclRequests();
        aclRequests1.setTeamId(3);
        aclRequests1.setRequestingteam(3);
        aclRequests1.setAclType("Create");
        aclRequests1.setAclPatternType("LITERAL");
        aclRequests.add(aclRequests1);
        return aclRequests;
    }

    public List<AclRequestsModel> getAclRequestsList() {
        List<AclRequestsModel> aclList = new ArrayList<>();

        List<AclRequestsModel> aclRequests = new ArrayList<>();
        AclRequestsModel aclRequests1 = new AclRequestsModel();
        aclRequests1.setTeamname("Seahorses");
        aclRequests1.setRequestingteam(3);
        aclRequests.add(aclRequests1);
        return aclRequests;
    }

    public List<TopicRequestModel> getTopicRequestsList() {
        List<TopicRequestModel> allTopicReqs = new ArrayList<>();
        TopicRequestModel topicRequest = new TopicRequestModel();
        topicRequest.setTeamname("Seahorses");
        allTopicReqs.add(topicRequest);
//        topicReqs.add(allTopicReqs);

        return allTopicReqs;
    }

    public List<List<TopicInfo>> getTopicInfoList() {
        List<List<TopicInfo>> topicReqs = new ArrayList<>();

        List<TopicInfo> allTopicReqs = new ArrayList<>();
        TopicInfo topicRequest = new TopicInfo();
        topicRequest.setTeamname("Seahorses");
        allTopicReqs.add(topicRequest);
        topicReqs.add(allTopicReqs);

        return topicReqs;
    }

    public List<Env> getEnvLists() {
        List<Env> envList = new ArrayList<>();
        Env env = new Env();
        env.setId("1");
        env.setName("DEV");
        envList.add(env);
        return envList;
    }

    public List<Env> getEnvListsIncorrect1() {
        List<Env> envList = new ArrayList<>();
        Env env = new Env();
        env.setId("1");
        env.setName("DEV");
        env.setOtherParams("");
        envList.add(env);
        return envList;
    }

    public List<EnvModel> getEnvList() {
        List<EnvModel> envList = new ArrayList<>();
        EnvModel env = new EnvModel();
        env.setId("1");
        env.setName("DEV");
        env.setType(KafkaClustersType.KAFKA.value);
        env.setClusterId(101);
        envList.add(env);
        return envList;
    }

    public List<HashMap<String,String>> getSyncEnv() {
        List<HashMap<String,String>> envList = new ArrayList<>();

        HashMap<String,String> hMap = new HashMap<>();
        hMap.put("key","1");
        hMap.put("name","DEV");
        envList.add(hMap);

        HashMap<String,String>  hMap1 = new HashMap<>();
        hMap1.put("key","2");
        hMap1.put("name","TST");
        envList.add(hMap1);

        return envList;
    }

    public AclRequestsModel getAclRequestModel(String topic) {
        AclRequestsModel aclRequest = new AclRequestsModel();
        aclRequest.setTeamId(3);
        aclRequest.setEnvironment("1");
        aclRequest.setTopicname(topic);
        aclRequest.setUsername("kwusera");
        aclRequest.setTopictype("Consumer");
        aclRequest.setConsumergroup("mygrp1");
        ArrayList<String> ipList = new ArrayList<>();
        ipList.add("2.1.2.1");
        aclRequest.setAcl_ip(ipList);
        aclRequest.setAcl_ssl(null);
        aclRequest.setAclPatternType("LITERAL");
        aclRequest.setRequestingteam(1);
        aclRequest.setTeamname("INFRATEAM");

        return aclRequest;
    }

    public List<HashMap<String, String>> getClusterApiTopics(String topicPrefix, int size){
        List<HashMap<String, String>> listTopics = new ArrayList<>();
        HashMap<String, String> hashMap;
        for(int i=0;i<size;i++) {
            hashMap = new HashMap<>();
            hashMap.put("topicName", topicPrefix +i);
            hashMap.put("replicationFactor", "1");
            hashMap.put("partitions", "2");
            listTopics.add(hashMap);
        }
        return listTopics;
    }

    public List<SyncTopicUpdates> getSyncTopicUpdates(){
        List<SyncTopicUpdates> syncUpdatesList = new ArrayList<>();
        SyncTopicUpdates syncTopicUpdates = new SyncTopicUpdates();
        syncTopicUpdates.setEnvSelected("1");
        syncTopicUpdates.setPartitions(2);
        syncTopicUpdates.setReplicationFactor("1");
        syncTopicUpdates.setTopicName("testtopic");
        syncTopicUpdates.setTeamSelected("Seahorses");
        syncTopicUpdates.setReq_no("fsadFDS");
        syncUpdatesList.add(syncTopicUpdates);

        return syncUpdatesList;
    }

    public List<SyncAclUpdates> getSyncAclsUpdates() {
        List<SyncAclUpdates> syncUpdatesList = new ArrayList<>();
        SyncAclUpdates syncAclUpdates = new SyncAclUpdates();
        syncAclUpdates.setTopicName("testtopic");
        syncAclUpdates.setReq_no("101");
        syncAclUpdates.setAclType("Producer");
        syncAclUpdates.setAclIp("12.2.4.55");
        syncAclUpdates.setTeamSelected("Team2");
        syncAclUpdates.setEnvSelected("DEV");

        SyncAclUpdates syncAclUpdates1 = new SyncAclUpdates();
        syncAclUpdates1.setTopicName("testtopic1");
        syncAclUpdates1.setReq_no("102");
        syncAclUpdates1.setAclType("Consumer");
        syncAclUpdates1.setAclIp("12.2.4.55");
        syncAclUpdates1.setTeamSelected("Team2");
        syncAclUpdates1.setEnvSelected("1");

        syncUpdatesList.add(syncAclUpdates);
        syncUpdatesList.add(syncAclUpdates1);

        return syncUpdatesList;
    }

    public TopicOverview getTopicOverview() {
        TopicOverview topicOverview = new TopicOverview();

        List<TopicInfo> allTopicReqs = new ArrayList<>();
        TopicInfo topicRequest = new TopicInfo();
        topicRequest.setTeamname("Seahorses");
        allTopicReqs.add(topicRequest);

        topicOverview.setAclInfoList(getAclInfoList());
        topicOverview.setTopicInfoList(allTopicReqs);
        return topicOverview;
    }


    public List<String> getAllTeamsSUOnly() {
        List<String> teamsList = new ArrayList<>();
        teamsList.add("Seahorses");
        teamsList.add("Team2");

        return teamsList;
    }

    public List<ServerConfigProperties> getServerConfig() {
        List<ServerConfigProperties> serverConfigPropertiesList = new ArrayList<>();
        ServerConfigProperties serverConfigProperties = new ServerConfigProperties();
        serverConfigProperties.setKey("JDK");
        serverConfigProperties.setValue("OpenJDK");

        serverConfigPropertiesList.add(serverConfigProperties);

        return serverConfigPropertiesList;
    }

}
