package com.kafkamgt.uiapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.PermissionType;
import com.kafkamgt.uiapi.model.RequestStatus;
import com.kafkamgt.uiapi.model.SchemaRequestModel;
import com.kafkamgt.uiapi.model.TopicRequestTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.kafkamgt.uiapi.model.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

@Service
@Slf4j
public class SchemaRegstryControllerService {

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    ClusterApiService clusterApiService;

    @Autowired
    private MailUtils mailService;

    @Autowired
    private CommonUtilsService commonUtilsService;

    @Autowired
    private RolesPermissionsControllerService rolesPermissionsControllerService;

    public SchemaRegstryControllerService(ClusterApiService clusterApiService,
                                          MailUtils mailService){
        this.clusterApiService = clusterApiService;
        this.mailService = mailService;
    }

    public List<SchemaRequestModel> getSchemaRequests(String pageNo, String currentPage, String requestsType) {
        log.debug("getSchemaRequests page {} requestsType {}", pageNo, requestsType);

        int tenantId = commonUtilsService.getTenantId(getUserName());
        String userDetails = getUserName();
        List<SchemaRequest> schemaReqs = manageDatabase.getHandleDbRequests()
                .getAllSchemaRequests(false, userDetails, tenantId);

        // tenant filtering
        List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
        if(schemaReqs != null)
            schemaReqs = schemaReqs.stream()
                    .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
                    .collect(Collectors.toList());

        // request status filtering
        if(!requestsType.equals("all") && EnumUtils.isValidEnum(RequestStatus.class, requestsType))
            if (schemaReqs != null) {
                schemaReqs = schemaReqs.stream()
                        .filter(schemaRequest -> schemaRequest.getTopicstatus().equals(requestsType))
                        .collect(Collectors.toList());
            }

        Integer userTeamId = getMyTeamId(userDetails);
        List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(userTeamId, tenantId);

        List<String> approverRoles = rolesPermissionsControllerService.getApproverRoles("CONNECTORS", tenantId);
        List<SchemaRequestModel> schemaRequestModels = new ArrayList<>();

        SchemaRequestModel schemaRequestModel;
        if (schemaReqs != null) {
            for (SchemaRequest schemaReq : schemaReqs) {
                schemaRequestModel = new SchemaRequestModel();
                schemaReq.setEnvironmentName(manageDatabase.getHandleDbRequests()
                        .selectEnvDetails(schemaReq.getEnvironment(), tenantId).getName());
                copyProperties(schemaReq, schemaRequestModel);

                // show approving info only before approvals
                if (!schemaRequestModel.getTopicstatus().equals(RequestStatus.approved.name())) {
                    schemaRequestModel.setApprovingTeamDetails(updateApproverInfo(userList, manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId), approverRoles,
                            schemaRequestModel.getUsername()));
                }
                schemaRequestModels.add(schemaRequestModel);
            }
        }

        return getSchemaRequestsPaged(schemaRequestModels, pageNo, currentPage, tenantId);
    }

    private String updateApproverInfo(List<UserInfo> userList, String teamName, List<String> approverRoles, String requestor){
        StringBuilder approvingInfo = new StringBuilder("Team : " + teamName + ", Users : ");

        for (UserInfo userInfo : userList) {
            if(approverRoles.contains(userInfo.getRole()) && !requestor.equals(userInfo.getUsername()))
                approvingInfo.append(userInfo.getUsername()).append(",");
        }

        return String.valueOf(approvingInfo);
    }

    private List<String> getEnvsFromUserId(String userDetails) {
        Integer userTeamId = getMyTeamId(userDetails);
        return manageDatabase.getTeamsAndAllowedEnvs(userTeamId, commonUtilsService.getTenantId(userDetails));
    }

    private Integer getMyTeamId(String userName){
        return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
    }

    private List<SchemaRequestModel> getSchemaRequestsPaged(List<SchemaRequestModel> schemaRequestModelList,
                                                            String pageNo, String currentPage, int tenantId){

        List<SchemaRequestModel> newList = new ArrayList<>();

        if(schemaRequestModelList!=null && schemaRequestModelList.size() > 0) {
            int totalRecs = schemaRequestModelList.size();
            int recsPerPage = 10;
            int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);

            pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
            int requestPageNo = Integer.parseInt(pageNo);
            int startVar = (requestPageNo - 1) * recsPerPage;
            int lastVar = (requestPageNo) * (recsPerPage);

            List<String> numList = new ArrayList<>();
            commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

            for (int i = 0; i < totalRecs; i++) {
                SchemaRequestModel schemaRequestModel = schemaRequestModelList.get(i);
                if (i >= startVar && i < lastVar) {
                    schemaRequestModel.setAllPageNos(numList);
                    schemaRequestModel.setTotalNoPages("" + totalPages);
                    schemaRequestModel.setCurrentPage(pageNo);
                    schemaRequestModel.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, schemaRequestModel.getTeamId()));
                    newList.add(schemaRequestModel);
                }
            }
        }

        return newList;
    }

     public String deleteSchemaRequests(String avroSchemaId) {
        log.info("deleteSchemaRequests {}", avroSchemaId);

         if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.REQUEST_DELETE_SCHEMAS)){
             return "{\"result\":\"Not Authorized\"}";
         }

        try{
            return manageDatabase.getHandleDbRequests().deleteSchemaRequest(Integer.parseInt(avroSchemaId),
                    commonUtilsService.getTenantId(getUserName()));
        }catch (Exception e){
            log.error("Exception:", e);
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    public String execSchemaRequests(String avroSchemaId) throws KafkawizeException {
        log.info("execSchemaRequests {}", avroSchemaId);
        String userDetails = getUserName();
        int tenantId = commonUtilsService.getTenantId(getUserName());
        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_SCHEMAS)){
            return "{\"result\":\"Not Authorized\"}";
        }

        SchemaRequest schemaRequest = manageDatabase.getHandleDbRequests()
                .selectSchemaRequest(Integer.parseInt(avroSchemaId), tenantId);

        if(schemaRequest.getUsername().equals(userDetails))
            return "{\"result\":\"You are not allowed to approve your own schema requests.\"}";

        List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());

        if(!allowedEnvIdList.contains(schemaRequest.getEnvironment()))
            return "{\"result\":\"Not Authorized\"}";

        ResponseEntity<String> response = clusterApiService.postSchema(schemaRequest, schemaRequest.getEnvironment(),
                schemaRequest.getTopicname(), tenantId);
        String responseDb;
        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        if(Objects.requireNonNull(response.getBody()).contains("id\":")) {
            try {
                responseDb = dbHandle.updateSchemaRequest(schemaRequest, userDetails);
                mailService.sendMail(schemaRequest.getTopicname(), null, "",
                        schemaRequest.getUsername(), dbHandle, SCHEMA_REQUEST_APPROVED, commonUtilsService.getLoginUrl());
            }catch (Exception e){
                log.error("Exception:", e);
                return e.getMessage();
            }

            return responseDb;
        }
        else {
            String errStr = response.getBody();
            if(errStr.length() > 100)
                errStr = errStr.substring(0,98) + "...";
            return "Failure in uploading schema. Error : " + errStr ;
        }
    }

    public String execSchemaRequestsDecline(String avroSchemaId, String reasonForDecline) {
        log.info("execSchemaRequestsDecline {}", avroSchemaId);
        String userDetails = getUserName();
        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_SCHEMAS)){
            return "{\"result\":\"Not Authorized\"}";
        }
        int tenantId = commonUtilsService.getTenantId(getUserName());
        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        SchemaRequest schemaRequest = dbHandle.selectSchemaRequest(Integer.parseInt(avroSchemaId), tenantId);
        List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());

        if(!allowedEnvIdList.contains(schemaRequest.getEnvironment()))
            return "{\"result\":\"Not Authorized\"}";

        String responseDb =  dbHandle.updateSchemaRequestDecline(schemaRequest, userDetails);
        mailService.sendMail(schemaRequest.getTopicname(), null, reasonForDecline, schemaRequest.getUsername(),
                dbHandle, SCHEMA_REQUEST_DENIED, commonUtilsService.getLoginUrl());

        return responseDb;
    }

    private List<Topic> getFilteredTopicsForTenant(List<Topic> topicsFromSOT) {
        // tenant filtering
        try {
            List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
            if(topicsFromSOT != null)
                topicsFromSOT = topicsFromSOT.stream()
                        .filter(topic -> allowedEnvIdList.contains(topic.getEnvironment()))
                        .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("No environments/clusters found.", e);
            return new ArrayList<>();
        }
        return topicsFromSOT;
    }

    public String uploadSchema(SchemaRequestModel schemaRequest){
        log.info("uploadSchema {}", schemaRequest);
        String userDetails = getUserName();

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.REQUEST_CREATE_SCHEMAS)){
            return "Not Authorized";
        }

        try {
            new ObjectMapper().readValue(schemaRequest.getSchemafull(), Object.class);
        } catch (IOException e) {
            log.error("Exception:", e);
            return "Failure. Invalid json";
        }

        Integer userTeamId = getMyTeamId(userDetails);
        int tenantId = commonUtilsService.getTenantId(getUserName());
        List<Topic> topicsSearchList = manageDatabase.getHandleDbRequests()
                .getTopicTeam(schemaRequest.getTopicname(), tenantId);

        // tenant filtering
        Integer topicOwnerTeam = getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();

        if(!userTeamId.equals(topicOwnerTeam)){
            return "No topic selected Or Not authorized to register schema for this topic.";
        }

        List<SchemaRequest> schemaReqs = manageDatabase.getHandleDbRequests()
                .getAllSchemaRequests(false, userDetails, tenantId);

        // tenant filtering
        List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
        if(schemaReqs != null)
            schemaReqs = schemaReqs.stream()
                    .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
                    .collect(Collectors.toList());

        // request status filtering
        if (schemaReqs != null) {
            schemaReqs = schemaReqs.stream()
                    .filter(schemaRequest1 -> schemaRequest1.getTopicstatus().equals("created") && schemaRequest1.getTopicname().equals(schemaRequest.getTopicname()))
                    .collect(Collectors.toList());
            if (schemaReqs.size() > 0) {
                return "Failure. A request already exists for this topic.";
            }
        }

        schemaRequest.setUsername(userDetails);
        SchemaRequest schemaRequestDao = new SchemaRequest();
        copyProperties(schemaRequest, schemaRequestDao);
        schemaRequestDao.setRequesttype(TopicRequestTypes.Create.name());
        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        schemaRequestDao.setTenantId(tenantId);
        try {
            String responseDb =  dbHandle.requestForSchema(schemaRequestDao);
            mailService.sendMail(schemaRequest.getTopicname(), null,
                    "", schemaRequest.getUsername(), dbHandle, SCHEMA_REQUESTED, commonUtilsService.getLoginUrl());

            return responseDb;
        }catch (Exception e){
            log.error("Exception:", e);
            return "failure " + e;
        }
    }

    private String getUserName(){
        return mailService.getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private Object getPrincipal(){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
