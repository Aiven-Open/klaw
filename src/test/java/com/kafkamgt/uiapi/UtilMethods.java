package com.kafkamgt.uiapi;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.model.AclInfo;
import com.kafkamgt.uiapi.model.TopicInfo;
import org.apache.thrift.transport.TTransportException;
import org.apache.tomcat.util.codec.binary.Base64;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class UtilMethods {

    public static void startEmbeddedCassandraServer(){
        try {
            EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
            EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        } catch (TTransportException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
        aclbindingMap.put("operation", "WRITE");
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

        List<HashMap<String, String>> aclListOriginal = new ArrayList<>(acls);
        return aclListOriginal;
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
        teamPk.setApp("App");
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
        schemaList.add(schemaRequest);
        return schemaList;
    }

    public List<AclRequests> getAclRequests() {
        List<AclRequests> aclRequests = new ArrayList<>();
        AclRequests aclRequests1 = new AclRequests();
        aclRequests1.setTeamname("Team1");
        aclRequests1.setRequestingteam("Team1");
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

    public AclRequests getAclRequest11(String testtopic1) {
        AclRequests aclRequest = new AclRequests();
        aclRequest.setTeamname("Team1");
        aclRequest.setEnvironment("DEV");
        aclRequest.setTopicname(testtopic1);
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
}
