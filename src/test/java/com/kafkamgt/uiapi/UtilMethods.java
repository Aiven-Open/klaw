package com.kafkamgt.uiapi;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.model.*;
import org.apache.thrift.transport.TTransportException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.*;

public class UtilMethods {

    public static void startEmbeddedCassandraServer(){
        try {
            EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
            EmbeddedCassandraServerHelper.startEmbeddedCassandra(200000L);
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
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
        listMSchemas.add(mSchema);

        return listMSchemas;
    }

    public UserInfo getUserInfoMock(){
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");
        userInfo.setUsername("uiuser1");

        return userInfo;
    }

    public List<UserInfo> getUserInfoList(String username, String role){
        List<UserInfo> userInfoList = new ArrayList<>();
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");
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
        topic.setTeamname("Team1");
        topic.setTopicname(topicName);

        return topic;
    }

    public SchemaRequestPK getSchemaRequestPk(){
        SchemaRequestPK schemaPK = new SchemaRequestPK();
        schemaPK.setEnvironment("DEV");
        schemaPK.setSchemaversion("1.0");
        schemaPK.setTopicname("testtopic");

        return schemaPK;
    }

    public List<Topic> getTopics() {
        List<Topic> allTopicReqs = new ArrayList<>();
        Topic topicRequest = new Topic();
        TopicPK topicPK = new TopicPK();
        topicPK.setEnvironment("DEV");
        topicPK.setTopicname("testtopic");

        topicRequest.setTopicPK(topicPK);
        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<Topic> getTopics(String topicName) {
        List<Topic> allTopicReqs = new ArrayList<>();
        Topic topicRequest = new Topic();
        TopicPK topicPK = new TopicPK();
        topicPK.setEnvironment("DEV");
        topicPK.setTopicname(topicName);

        topicRequest.setTopicPK(topicPK);
        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<Acl> getAcls() {
        List<Acl> allTopicReqs = new ArrayList<>();
        Acl topicRequest = new Acl();
        topicRequest.setTeamname("Team1");
        topicRequest.setTopictype("Producer");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<AclInfo> getAclInfoList() {
        List<AclInfo> allTopicReqs = new ArrayList<>();
        AclInfo topicRequest = new AclInfo();
        topicRequest.setTeamname("Team1");
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
        topicRequest.setEnvironment("DEV");
        topicRequest.setAclip("12.22.126.21");
        topicRequest.setTopictype("Producer");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<Acl> getAllAcls() {
        List<Acl> allTopicReqs = new ArrayList<>();
        Acl topicRequest = new Acl();
        topicRequest.setTeamname("Team1");
        topicRequest.setTopictype("Producer");
        allTopicReqs.add(topicRequest);

        topicRequest = new Acl();
        topicRequest.setTopicname("testtopic");
        topicRequest.setConsumergroup("congrp");
        topicRequest.setEnvironment("DEV");
        topicRequest.setAclip("12.22.126.21");
        topicRequest.setTopictype("Producer");
        allTopicReqs.add(topicRequest);

        return allTopicReqs;
    }

    public List<Team> getTeams() {
        List<Team> allTopicReqs = new ArrayList<>();
        Team topicRequest = new Team();
        TeamPK teamPk = new TeamPK();
        teamPk.setTeamname("Team1");
        topicRequest.setTeamPK(teamPk);

        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public List<TopicRequest> getTopicRequests() {
        List<TopicRequest> allTopicReqs = new ArrayList<>();
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);
        return allTopicReqs;
    }

    public TopicRequest getTopicRequest(String topicName) {
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTeamname("Team1");
        topicRequest.setUsername("uiuser1");
        topicRequest.setTopicname(topicName);
        topicRequest.setAcl_ip("12.11.223.12");
        topicRequest.setTopicpartitions("2");
        topicRequest.setReplicationfactor("1");
        topicRequest.setEnvironment("DEV");
        return topicRequest;
    }

    public AclRequests getAclRequest(String topicName) {
        AclRequests aclRequest = new AclRequests();
        aclRequest.setTeamname("Team1");
        aclRequest.setEnvironment("DEV");
        aclRequest.setTopicname(topicName);
        aclRequest.setUsername("uiuser1");
        aclRequest.setTopictype("Consumer");
        aclRequest.setAclType("Delete");
        aclRequest.setConsumergroup("congroup1");
        aclRequest.setAcl_ip("10.11.112.113");
        return aclRequest;
    }

    public AclRequests getAclRequestCreate(String topicName) {
        AclRequests aclRequest = new AclRequests();
        aclRequest.setTeamname("Team1");
        aclRequest.setEnvironment("DEV");
        aclRequest.setTopicname(topicName);
        aclRequest.setUsername("uiuser1");
        aclRequest.setTopictype("Consumer");
        aclRequest.setAclType("Create");
        aclRequest.setConsumergroup("congroup1");
        aclRequest.setAcl_ip("10.11.112.113");
        return aclRequest;
    }

    public List<SchemaRequest> getSchemaRequests() {
        List<SchemaRequest> schemaList = new ArrayList<>();
        SchemaRequest schemaRequest = new SchemaRequest();
        SchemaRequestPK schemaRequestPK = new SchemaRequestPK();
        schemaRequestPK.setEnvironment("DEV");
        schemaRequest.setTeamname("Team1");
        schemaRequest.setUsername("uiuser1");
        schemaRequest.setSchemaRequestPK(schemaRequestPK);
        schemaRequest.setSchemafull("schema");
        schemaList.add(schemaRequest);
        return schemaList;
    }

    public List<AclRequests> getAclRequests() {
        List<AclRequests> aclRequests = new ArrayList<>();
        AclRequests aclRequests1 = new AclRequests();
        aclRequests1.setTeamname("Team1");
        aclRequests1.setRequestingteam("Team1");
        aclRequests1.setAclType("Create");
        aclRequests.add(aclRequests1);
        return aclRequests;
    }

    public List<List<AclRequests>> getAclRequestsList() {
        List<List<AclRequests>> aclList = new ArrayList<>();

        List<AclRequests> aclRequests = new ArrayList<>();
        AclRequests aclRequests1 = new AclRequests();
        aclRequests1.setTeamname("Team1");
        aclRequests1.setRequestingteam("Team1");
        aclRequests.add(aclRequests1);

        aclList.add(aclRequests);

        return aclList;
    }

    public List<List<TopicRequest>> getTopicRequestsList() {
        List<List<TopicRequest>> topicReqs = new ArrayList<>();

        List<TopicRequest> allTopicReqs = new ArrayList<>();
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);
        topicReqs.add(allTopicReqs);

        return topicReqs;
    }

    public List<List<TopicInfo>> getTopicInfoList() {
        List<List<TopicInfo>> topicReqs = new ArrayList<>();

        List<TopicInfo> allTopicReqs = new ArrayList<>();
        TopicInfo topicRequest = new TopicInfo();
        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);
        topicReqs.add(allTopicReqs);

        return topicReqs;
    }

    public List<Env> getEnvList() {
        List<Env> envList = new ArrayList<>();
        Env env = new Env();
        env.setName("DEV");
        env.setHost("localhost");
        env.setPort("9092");
        envList.add(env);
        return envList;
    }

    public List<HashMap<String,String>> getSyncEnv() {
        List<HashMap<String,String>> envList = new ArrayList<>();

        HashMap<String,String> hMap = new HashMap<>();
        hMap.put("key","DEV");
        hMap.put("name","DEV");
        envList.add(hMap);

        HashMap<String,String>  hMap1 = new HashMap<>();
        hMap1.put("key","TST");
        hMap1.put("name","TST");
        envList.add(hMap1);

        return envList;
    }

    public AclRequests getAclRequest11(String topic) {
        AclRequests aclRequest = new AclRequests();
        aclRequest.setTeamname("Team1");
        aclRequest.setEnvironment("DEV");
        aclRequest.setTopicname(topic);
        aclRequest.setUsername("uiuser1");
        aclRequest.setTopictype("Consumer");
        aclRequest.setConsumergroup("mygrp1");
        aclRequest.setAcl_ip("2.1.2.1");
        aclRequest.setAcl_ssl(null);
        return aclRequest;
    }

    public List<String> getClusterApiTopics(String topicPrefix, int size){
        List<String> listTopics = new ArrayList<>();
        for(int i=0;i<size;i++) {
            listTopics.add(topicPrefix +i+ ":::::" + "1" + ":::::" + "2");
        }
        return listTopics;
    }

    public List<SyncTopicUpdates> getSyncTopicUpdates(){
        List<SyncTopicUpdates> syncUpdatesList = new ArrayList<>();
        SyncTopicUpdates syncTopicUpdates = new SyncTopicUpdates();
        syncTopicUpdates.setEnvSelected("DEV");
        syncTopicUpdates.setPartitions("2");
        syncTopicUpdates.setReplicationFactor("1");
        syncTopicUpdates.setTopicName("testtopic");
        syncTopicUpdates.setTeamSelected("Team1");
        syncTopicUpdates.setReq_no("fsadFDS");
        syncUpdatesList.add(syncTopicUpdates);

        return syncUpdatesList;
    }

    public List<SyncAclUpdates> getSyncAclsUpdates() {
        List<SyncAclUpdates> syncUpdatesList = new ArrayList<>();
        SyncAclUpdates syncAclUpdates = new SyncAclUpdates();
        syncAclUpdates.setTopicName("testtopic");
        syncAclUpdates.setReq_no("fsdFDSFa");
        syncAclUpdates.setAclType("Producer");
        syncAclUpdates.setAclIp("12.2.4.55");
        syncAclUpdates.setTeamSelected("Team2");
        syncAclUpdates.setEnvSelected("DEV");

        SyncAclUpdates syncAclUpdates1 = new SyncAclUpdates();
        syncAclUpdates1.setTopicName("testtopic1");
        syncAclUpdates1.setReq_no("fsdFDSFa");
        syncAclUpdates1.setAclType("Consumer");
        syncAclUpdates1.setAclIp("12.2.4.55");
        syncAclUpdates1.setTeamSelected("Team2");
        syncAclUpdates1.setEnvSelected("DEV");

        syncUpdatesList.add(syncAclUpdates);
        syncUpdatesList.add(syncAclUpdates1);

        return syncUpdatesList;
    }

    public TopicOverview getTopicOverview() {
        TopicOverview topicOverview = new TopicOverview();

        List<TopicInfo> allTopicReqs = new ArrayList<>();
        TopicInfo topicRequest = new TopicInfo();
        topicRequest.setTeamname("Team1");
        allTopicReqs.add(topicRequest);

        topicOverview.setAclInfoList(getAclInfoList());
        topicOverview.setTopicInfoList(allTopicReqs);
        return topicOverview;
    }

    public List<String> getEnvsOnly() {
        List<String> envsStrList = new ArrayList<>();
        envsStrList.add("DEV");
        envsStrList.add("TST");

        return envsStrList;
    }

    public List<String> getAllTeamsSUOnly() {
        List<String> teamsList = new ArrayList<>();
        teamsList.add("Team1");
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
