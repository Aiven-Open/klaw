package com.kafkamgt.uiapi;

import com.kafkamgt.uiapi.dao.*;

import java.util.ArrayList;
import java.util.List;

public class UtilMethods {

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
        return topicRequest;
    }

    public AclRequests getAclRequest(String topicName) {
        AclRequests topicRequest = new AclRequests();
        topicRequest.setTeamname("Team1");
        topicRequest.setTopicname(topicName);
        topicRequest.setUsername("uiuser1");
        return topicRequest;
    }

    public List<SchemaRequest> getSchemaRequests() {
        List<SchemaRequest> schemaList = new ArrayList<>();
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setTeamname("Team1");
        schemaRequest.setUsername("uiuser1");
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
}
