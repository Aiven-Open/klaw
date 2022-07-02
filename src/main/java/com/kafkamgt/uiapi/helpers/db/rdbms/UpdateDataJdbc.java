package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.model.RequestStatus;
import com.kafkamgt.uiapi.model.TopicRequestTypes;
import com.kafkamgt.uiapi.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
@Slf4j
public class UpdateDataJdbc {

    @Autowired(required=false)
    private TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    private KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

    @Autowired(required=false)
    private TopicRepo topicRepo;

    @Autowired(required=false)
    private KwKafkaConnectorRepo kafkaConnectorRepo;

    @Autowired(required=false)
    private AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    private UserInfoRepo userInfoRepo;

    @Autowired(required=false)
    private TeamRepo teamRepo;

    @Autowired(required=false)
    private RegisterInfoRepo registerInfoRepo;

    @Autowired(required=false)
    private SchemaRequestRepo schemaRequestRepo;

    @Autowired(required=false)
    private InsertDataJdbc insertDataJdbcHelper;

    @Autowired(required=false)
    private DeleteDataJdbc deleteDataJdbcHelper;

    @Autowired(required=false)
    private KwPropertiesRepo kwPropertiesRepo;

    @Autowired(required=false)
    private KwRolesPermsRepo kwRolesPermsRepo;

    @Autowired(required=false)
    private TenantRepo tenantRepo;


    public UpdateDataJdbc(TopicRequestsRepo topicRequestsRepo, AclRequestsRepo aclRequestsRepo,
                          UserInfoRepo userInfoRepo, SchemaRequestRepo schemaRequestRepo,
                          InsertDataJdbc insertDataJdbcHelper){
        this.topicRequestsRepo = topicRequestsRepo;
        this.aclRequestsRepo = aclRequestsRepo;
        this.userInfoRepo = userInfoRepo;
        this.schemaRequestRepo = schemaRequestRepo;
        this.insertDataJdbcHelper = insertDataJdbcHelper;
    }

    public UpdateDataJdbc(){}

    public String declineTopicRequest(TopicRequest topicRequest, String approver){
        log.debug("declineTopicRequest {} {}", topicRequest.getTopicname(), approver);
        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus("declined");
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);

        return "success";
    }

    public String declineConnectorRequest(KafkaConnectorRequest connectorRequest, String approver){
        log.debug("declineConnectorRequest {} {}", connectorRequest.getConnectorName(), approver);
        connectorRequest.setApprover(approver);
        connectorRequest.setConnectorStatus("declined");
        connectorRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        kafkaConnectorRequestsRepo.save(connectorRequest);

        return "success";
    }

    public String updateTopicRequestStatus(TopicRequest topicRequest, String approver) {
        log.debug("updateTopicRequestStatus {} {}", topicRequest.getTopicname(), approver);
        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus(RequestStatus.approved.name());
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);

        return "success";
    }

    public String updateConnectorRequestStatus(KafkaConnectorRequest connectorRequest, String approver) {
        log.debug("updateConnectorRequestStatus {} {}", connectorRequest.getConnectorName(), approver);
        connectorRequest.setApprover(approver);
        connectorRequest.setConnectorStatus(RequestStatus.approved.name());
        connectorRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        kafkaConnectorRequestsRepo.save(connectorRequest);

        return "success";
    }

    public String updateTopicRequest(TopicRequest topicRequest, String approver){
        log.debug("updateTopicRequest {} {}", topicRequest.getTopicname(), approver);
        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus(RequestStatus.approved.name());
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);

        // insert into SOT
        List<Topic> topics = new ArrayList<>();

        Topic topicObj = new Topic();
        copyProperties(topicRequest, topicObj);
        topicObj.setTopicid(insertDataJdbcHelper.getNextTopicRequestId("TOPIC_ID", topicRequest.getTenantId()));
        topicObj.setNoOfReplcias(topicRequest.getReplicationfactor());
        topicObj.setNoOfPartitions(topicRequest.getTopicpartitions());
        topicObj.setExistingTopic(false);
        topicObj.setHistory(topicRequest.getHistory());
        topics.add(topicObj);

        if(topicRequest.getTopictype().equals(TopicRequestTypes.Create.name())) {
            insertDataJdbcHelper.insertIntoTopicSOT(topics, false);
        }
        else if(topicRequest.getTopictype().equals(TopicRequestTypes.Update.name()))
            updateTopicSOT(topics, topicRequest.getOtherParams());
        else if(topicRequest.getTopictype().equals(TopicRequestTypes.Delete.name()))
            deleteDataJdbcHelper.deleteTopics(topicObj);

        return "success";
    }

    public String updateConnectorRequest(KafkaConnectorRequest connectorRequest, String approver){
        log.debug("updateConnectorRequest {} {}", connectorRequest.getConnectorName(), approver);
        connectorRequest.setApprover(approver);
        connectorRequest.setConnectorStatus(RequestStatus.approved.name());
        connectorRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        kafkaConnectorRequestsRepo.save(connectorRequest);

        // insert into SOT
        List<KwKafkaConnector> connectors = new ArrayList<>();

        KwKafkaConnector topicObj = new KwKafkaConnector();
        copyProperties(connectorRequest, topicObj);
        topicObj.setConnectorId(insertDataJdbcHelper.getNextConnectorRequestId("CONNECTOR_ID", connectorRequest.getTenantId()));
        topicObj.setExistingConnector(false);
        topicObj.setHistory(connectorRequest.getHistory());
        connectors.add(topicObj);

        if(connectorRequest.getConnectortype().equals(TopicRequestTypes.Create.name())) {
            insertDataJdbcHelper.insertIntoConnectorSOT(connectors, false);
        }
        else if(connectorRequest.getConnectortype().equals(TopicRequestTypes.Update.name()))
            updateConnectorSOT(connectors, connectorRequest.getOtherParams());
        else if(connectorRequest.getConnectortype().equals(TopicRequestTypes.Delete.name()))
            deleteDataJdbcHelper.deleteConnectors(topicObj);

        return "success";
    }

    private void updateTopicSOT(List<Topic> topics, String topicId){
        topics.forEach(topic->
        {
            log.debug("updateTopicSOT {}", topic.getTopicname());
            topic.setTopicid(Integer.parseInt(topicId));
            topicRepo.save(topic);
        });
    }

    private void updateConnectorSOT(List<KwKafkaConnector> topics, String topicId){
        topics.forEach(topic->
        {
            log.debug("updateConnectorSOT {}", topic.getConnectorName());
            topic.setConnectorId(Integer.parseInt(topicId));
            kafkaConnectorRepo.save(topic);
        });
    }

    public String updateAclRequest(AclRequests aclReq, String approver){
        log.debug("updateAclRequest {} {}", aclReq.getTopicname(), approver);
        aclReq.setApprover(approver);
        aclReq.setAclstatus("approved");
        aclReq.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        aclRequestsRepo.save(aclReq);

        return processMultipleAcls(aclReq);
    }

    private String processMultipleAcls(AclRequests aclReq) {
        List<Acl> acls;
        if(aclReq.getAcl_ip() != null){
            String[] aclListIp = aclReq.getAcl_ip().split("<ACL>");
            for (String aclString : aclListIp) {
                Acl aclObj = new Acl();
                copyProperties(aclReq, aclObj);
                aclObj.setTeamId(aclReq.getRequestingteam());

                acls = new ArrayList<>();
                aclObj.setAclip(aclString);
                aclObj.setAclssl(aclReq.getAcl_ssl());
                acls.add(aclObj);

                if(aclReq.getAclType() != null && aclReq.getAclType().equals("Create")){
                    acls.forEach(acl -> acl.setReq_no(null));
                    insertDataJdbcHelper.insertIntoAclsSOT(acls,false);
                }else{
                    // Delete SOT //
                    deleteDataJdbcHelper.deletePrevAclRecs(aclReq);
                }
            }
        }
        else if(aclReq.getAcl_ssl() != null){
            String[] aclListSsl = aclReq.getAcl_ssl().split("<ACL>");
            for (String aclString : aclListSsl) {
                Acl aclObj = new Acl();
                copyProperties(aclReq, aclObj);
                aclObj.setTeamId(aclReq.getRequestingteam());

                acls = new ArrayList<>();
                aclObj.setAclip(aclReq.getAcl_ip());
                aclObj.setAclssl(aclString);
                acls.add(aclObj);

                if(aclReq.getAclType() != null && aclReq.getAclType().equals("Create")){
                    acls.forEach(acl -> acl.setReq_no(null));
                    insertDataJdbcHelper.insertIntoAclsSOT(acls,false);
                }else{
                    // Delete SOT //
                    deleteDataJdbcHelper.deletePrevAclRecs(aclReq);
                }
            }
        }

        return "success";
    }

    public String declineAclRequest(AclRequests aclRequests, String approver){
        log.debug("declineAclRequest {} {}", aclRequests.getTopicname(), approver);
        aclRequests.setApprover(approver);
        aclRequests.setAclstatus("declined");
        aclRequests.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        aclRequestsRepo.save(aclRequests);

        return "success";
    }

    public String updatePassword(String username, String password){
        log.debug("updatePassword {}", username);
        Optional<UserInfo> userRec = userInfoRepo.findById(username);
        if(userRec.isPresent()) {
            UserInfo userInfo = userRec.get();
            userInfo.setPwd(password);
            userInfoRepo.save(userInfo);
            return "success";
        }
        return "failure";
    }

    public String updateSchemaRequest(SchemaRequest schemaRequest, String approver){
        log.debug("updateSchemaRequest {} {}", schemaRequest.getTopicname(), approver);
        schemaRequest.setApprover(approver);
        schemaRequest.setTopicstatus("approved");
        schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

        schemaRequestRepo.save(schemaRequest);

        // Insert to SOT
        List<MessageSchema> schemas = new ArrayList<>();
        MessageSchema schemaObj = new MessageSchema();
        copyProperties(schemaRequest,schemaObj);
        schemas.add(schemaObj);
        insertDataJdbcHelper.insertIntoMessageSchemaSOT(schemas);

        return "success";
    }


    public String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver) {
        log.debug("updateSchemaRequestDecline {} {}", schemaRequest.getTopicname(), approver);
        schemaRequest.setApprover(approver);
        schemaRequest.setTopicstatus("declined");
        schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

        schemaRequestRepo.save(schemaRequest);

        return "success";
    }

    public void updateNewUserRequest(String username, String approver, boolean isApprove) {
        log.debug("updateNewUserRequest {} {} {}", username, approver, isApprove);
        Optional<RegisterUserInfo> registerUser = registerInfoRepo.findById(username);
        String status;
        if(isApprove)
            status = "APPROVED";
        else
            status = "DECLINED";
        if(registerUser.isPresent()){
            RegisterUserInfo registerUserInfo = registerUser.get();
            if(registerUserInfo.getStatus().equals("PENDING")){
                registerUserInfo.setStatus(status);
                registerUserInfo.setApprover(approver);
                registerUserInfo.setRegisteredTime(new Timestamp(System.currentTimeMillis()));

                registerInfoRepo.save(registerUserInfo);

            }
        }
    }

    public String updateUser(UserInfo userInfo) {
        log.debug("updateUser {}", userInfo.getUsername());
        Optional<UserInfo> userExists = userInfoRepo.findById(userInfo.getUsername());
        if(!userExists.isPresent())
            return "Failure. User doesn't exist";

        userInfoRepo.save(userInfo);
        return "success";
    }

    public String updateTeam(Team team) {
        log.debug("updateTeam {}", team);
        TeamID teamID = new TeamID(team.getTeamId(), team.getTenantId());
        Optional<Team> teamExists = teamRepo.findById(teamID);
        if(teamExists.isEmpty())
            return "Failure. Team doesn't exist";

        teamRepo.save(team);
        return "success";
    }

    public String updateKwProperty(KwProperties kwProperties, int tenantId) {
        List<KwProperties> kwProp = kwPropertiesRepo.findAllByKwKeyAndTenantId(kwProperties.getKwKey(), tenantId);
        if(!kwProp.isEmpty())
        {
            kwProp.get(0).setKwValue(kwProperties.getKwValue());
            kwPropertiesRepo.save(kwProp.get(0));
            return "success";
        }

        return "failure";
    }

    public Integer getNextRolePermissionId(int tenantId) {
        Integer maxId = kwRolesPermsRepo.getMaxRolePermissionId(tenantId);

        if(maxId == null)
            maxId = 1;

        return maxId + 1;
    }

    public String updatePermissions(List<KwRolesPermissions> permissions, String addDelete) {

        List<KwRolesPermissions> rolePermFound;

        if(addDelete.equals("DELETE")) {
            for (KwRolesPermissions permission : permissions) {
                rolePermFound = kwRolesPermsRepo
                        .findAllByRoleIdAndPermissionAndTenantId(permission.getRoleId(), permission.getPermission(), permission.getTenantId());
                kwRolesPermsRepo.deleteAll(rolePermFound);
            }
        }else{
            for (KwRolesPermissions permission : permissions) {
                permission.setId(getNextRolePermissionId(permission.getTenantId()));
                kwRolesPermsRepo.save(permission);
            }
        }

        return "success";
    }

    public String updateTopicDocumentation(Topic topic) {
        TopicID topicID = new TopicID();
        topicID.setTenantId(topic.getTenantId());
        topicID.setTopicid(topic.getTopicid());

        Optional<Topic> optTopic = topicRepo.findById(topicID);
        if(optTopic.isPresent()){
            optTopic.get().setDocumentation(topic.getDocumentation());
            topicRepo.save(optTopic.get());
            return "success";
        }

        return "failure";
    }

    public String updateConnectorDocumentation(KwKafkaConnector topic) {
        KwKafkaConnectorID kwKafkaConnectorID = new KwKafkaConnectorID();
        kwKafkaConnectorID.setConnectorId(topic.getConnectorId());
        kwKafkaConnectorID.setTenantId(topic.getTenantId());

        Optional<KwKafkaConnector> optTopic = kafkaConnectorRepo.findById(kwKafkaConnectorID);
        if(optTopic.isPresent()){
            optTopic.get().setDocumentation(topic.getDocumentation());
            kafkaConnectorRepo.save(optTopic.get());
            return "success";
        }

        return "failure";
    }

    public String setTenantActivestatus(int tenantId, boolean status) {
        Optional<KwTenants> kwTenant = tenantRepo.findById(tenantId);
        if(kwTenant.isPresent()){
            if(status)
                kwTenant.get().setIsActive("true");
            else
                kwTenant.get().setIsActive("false");

            tenantRepo.save(kwTenant.get());
        }
        return "success";
    }

    public String updateTenant(int tenantId, String organizationName) {
        Optional<KwTenants> kwTenant = tenantRepo.findById(tenantId);
        if(kwTenant.isPresent()){
            kwTenant.get().setOrgName(organizationName);
            tenantRepo.save(kwTenant.get());
        }
        return "success";
    }

    public String disableTenant(int tenantId) {
        Optional<KwTenants> kwTenant = tenantRepo.findById(tenantId);
        if(kwTenant.isPresent()){
            kwTenant.get().setIsActive("false");
            tenantRepo.save(kwTenant.get());
        }
        return "success";
    }
}
