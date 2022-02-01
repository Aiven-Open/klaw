package com.kafkamgt.uiapi.service;


import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.KwMetadataUpdates;
import com.kafkamgt.uiapi.model.PermissionType;
import com.kafkamgt.uiapi.model.RequestStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.kafkamgt.uiapi.model.RolesType.SUPERADMIN;
import static com.kafkamgt.uiapi.service.KwConstants.*;

@Service
@Slf4j
public class UtilControllerService {

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    MailUtils mailService;

    @Autowired
    private CommonUtilsService commonUtilsService;

    @Value("${kafkawize.version:1.0.1}")
    private
    String kafkawizeVersion;

    @Value("${kafkawize.login.authentication.type}")
    private String authenticationType;

    @Value("${kafkawize.enable.authorization.ad:false}")
    private String adAuthRoleEnabled;

    @Value("${kafkawize.admin.mailid:info@kafkawize.io}")
    private String saasKwAdminMailId;

    @Value("${kafkawize.installation.type:onpremise}")
    private String kwInstallationType;

    @Value("${server.servlet.context-path:}")
    private String kwContextPath;

    @Value("${kafkawize.enable.sso:false}")
    private String ssoEnabled;

    @Value("${kafkawize.sso.server.loginurl:url}")
    private String ssoServerLoginUrl;

    @Autowired
    private ConfigurableApplicationContext context;

    public HashMap<String, String> getDashboardStats(){
        log.debug("getDashboardInfo");
        String userName = getUserName();
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
        if(userName!=null) {
            return reqsHandle.getDashboardStats(getMyTeamId(userName), commonUtilsService.getTenantId(getUserName()));
        }

        return new HashMap<>();
    }

    private String getUserName(){
        return mailService.getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public String getTenantNameFromUser(String userId) {
        UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
        if(userInfo != null) {
            return manageDatabase.getTenantMap().entrySet().stream()
                    .filter(obj -> obj.getKey().equals(userInfo.getTenantId()))
                    .findFirst().get().getValue();

        }
        else return null;
    }

    private List<String> getEnvsFromUserId(String userDetails) {
        Integer userTeamId = getMyTeamId(userDetails);
        return manageDatabase.getTeamsAndAllowedEnvs(userTeamId, commonUtilsService.getTenantId(userDetails));
    }

    private Integer getMyTeamId(String userName){
        return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
    }

    public HashMap<String, String> getAllRequestsToBeApproved(String requestor, int tenantId){
        log.debug("getAllRequestsToBeApproved {}", requestor);
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();

        HashMap<String, String> countList = new HashMap<>();
        String roleToSet = "";
        if(!commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)){
            roleToSet = "approver_subscriptions";
        }
        if(!commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)){
            roleToSet = "requestor_subscriptions";
        }
        List<SchemaRequest> allSchemaReqs = reqsHandle.getAllSchemaRequests(true, requestor, tenantId);

        List<AclRequests> allAclReqs;
        List<TopicRequest> allTopicReqs;
        List<KafkaConnectorRequest> allConnectorReqs;

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS)) {
            allAclReqs = reqsHandle.getAllAclRequests(true, requestor, roleToSet, RequestStatus.created.name(),
                    false, tenantId);
            allTopicReqs = reqsHandle.getCreatedTopicRequests(requestor, RequestStatus.created.name(), false, tenantId);
            allConnectorReqs = reqsHandle.getCreatedConnectorRequests(requestor, RequestStatus.created.name(), false, tenantId);
        }
        else {
            allAclReqs = reqsHandle.getAllAclRequests(true, requestor, roleToSet, RequestStatus.created.name(), true, tenantId);
            allTopicReqs = reqsHandle.getCreatedTopicRequests(requestor, RequestStatus.created.name(), true, tenantId);
            allConnectorReqs = reqsHandle.getCreatedConnectorRequests(requestor, RequestStatus.created.name(), true, tenantId);
        }

        try {
            // tenant filtering
            List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
            allAclReqs = allAclReqs.stream()
                    .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
                    .collect(Collectors.toList());

            // tenant filtering
            allSchemaReqs = allSchemaReqs.stream()
                    .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
                    .collect(Collectors.toList());

            // tenant filtering
            allTopicReqs = allTopicReqs.stream()
                    .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
                    .collect(Collectors.toList());

            // tenant filtering
            allConnectorReqs = allConnectorReqs.stream()
                    .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            log.error("No environments/clusters found.");
            allAclReqs = new ArrayList<>();
            allSchemaReqs = new ArrayList<>();
            allTopicReqs = new ArrayList<>();
            allConnectorReqs = new ArrayList<>();
        }

        List<RegisterUserInfo> allUserReqs = reqsHandle.selectAllRegisterUsersInfoForTenant(tenantId);

        countList.put("topics", allTopicReqs.size() + "");
        countList.put("acls", allAclReqs.size() + "");
        countList.put("schemas", allSchemaReqs.size() + "");
        countList.put("connectors", allConnectorReqs.size() + "");

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS))
            countList.put("users", "0");
        else
            countList.put("users", allUserReqs.size() + "");

        return countList;
    }

    public HashMap<String, String> getAuth() {
        int tenantId = commonUtilsService.getTenantId(getUserName());
        String userName = getUserName();
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
        if(userName!=null) {

            String teamName = manageDatabase.getTeamNameFromTeamId(tenantId, getMyTeamId(userName));

            String authority = commonUtilsService.getAuthority(getPrincipal());

            HashMap<String, String> outstanding = getAllRequestsToBeApproved(userName, tenantId);

            String outstandingTopicReqs = outstanding.get("topics");
            int outstandingTopicReqsInt = Integer.parseInt(outstandingTopicReqs);

            String outstandingAclReqs = outstanding.get("acls");
            int outstandingAclReqsInt = Integer.parseInt(outstandingAclReqs);

            String outstandingSchemasReqs = outstanding.get("schemas");
            int outstandingSchemasReqsInt = Integer.parseInt(outstandingSchemasReqs);

            String outstandingConnectorReqs = outstanding.get("connectors");
            int outstandingConnectorReqsInt = Integer.parseInt(outstandingConnectorReqs);

            String outstandingUserReqs = outstanding.get("users");
            int outstandingUserReqsInt = Integer.parseInt(outstandingUserReqs);

            if(outstandingTopicReqsInt<=0)
                outstandingTopicReqs = "0";

            if(outstandingAclReqsInt<=0)
                outstandingAclReqs = "0";

            if(outstandingSchemasReqsInt<=0)
                outstandingSchemasReqs = "0";

            if(outstandingConnectorReqsInt<=0)
                outstandingConnectorReqs = "0";

            if(outstandingUserReqsInt<=0)
                outstandingUserReqs = "0";

            HashMap<String, String> dashboardData = reqsHandle.getDashboardInfo(getMyTeamId(userName), tenantId);

            dashboardData.put("contextPath", kwContextPath);
            dashboardData.put("teamsize", ""+manageDatabase.getTeamsForTenant(tenantId).size());
            dashboardData.put("schema_clusters_count", ""+manageDatabase.getSchemaRegEnvList(tenantId).size());
            dashboardData.put("kafka_clusters_count", ""+manageDatabase.getKafkaEnvList(tenantId).size());
            dashboardData.put("kafkaconnect_clusters_count", ""+manageDatabase.getKafkaConnectEnvList(tenantId).size());

            String canUpdatePermissions, addEditRoles;

            if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.UPDATE_PERMISSIONS))
                canUpdatePermissions = "NotAuthorized";
            else canUpdatePermissions = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_ROLES))
                addEditRoles = "NotAuthorized";
            else addEditRoles = "Authorized";

            String canShutdownKw;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SHUTDOWN_KAFKAWIZE)){
                canShutdownKw = "NotAuthorized";
            }
            else canShutdownKw = "Authorized";

            String syncBackTopics,syncBackAcls;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_BACK_TOPICS)){
                syncBackTopics = "NotAuthorized";
            }
            else syncBackTopics = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_BACK_SUBSCRIPTIONS)){
                syncBackAcls = "NotAuthorized";
            }
            else syncBackAcls = "Authorized";

            String addUser, addTeams;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_USERS)){
                addUser = "NotAuthorized";
            }
            else addUser = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_TEAMS)){
                addTeams = "NotAuthorized";
            }
            else addTeams = "Authorized";

            String viewKafkaConnect;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.VIEW_CONNECTORS)){
                viewKafkaConnect = "NotAuthorized";
            }
            else viewKafkaConnect = "Authorized";

            String requestTopics;
            String requestAcls;
            String requestSchemas;
            String requestConnector;
            String requestItems;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.REQUEST_CREATE_TOPICS)){
                requestTopics = "NotAuthorized";
            }
            else requestTopics = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)){
                requestAcls = "NotAuthorized";
            }
            else requestAcls = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.REQUEST_CREATE_SCHEMAS)){
                requestSchemas = "NotAuthorized";
            }
            else requestSchemas = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.REQUEST_CREATE_CONNECTORS)){
                requestConnector = "NotAuthorized";
            }
            else requestConnector = "Authorized";

            if(requestTopics.equals("Authorized") || requestAcls.equals("Authorized") || requestSchemas.equals("Authorized") || requestConnector.equals("Authorized"))
                requestItems = "Authorized";
            else
                requestItems = "NotAuthorized";

            String approveDeclineTopics;
            String approveDeclineSubscriptions;
            String approveDeclineSchemas;
            String approveDeclineConnectors;

            String approveAtleastOneRequest = "NotAuthorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_TOPICS)){
                approveDeclineTopics = "NotAuthorized";
            }
            else approveDeclineTopics = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_SUBSCRIPTIONS)){
                approveDeclineSubscriptions = "NotAuthorized";
            }
            else approveDeclineSubscriptions = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_SCHEMAS)){
                approveDeclineSchemas = "NotAuthorized";
            }
            else approveDeclineSchemas = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_CONNECTORS)){
                approveDeclineConnectors = "NotAuthorized";
            }
            else approveDeclineConnectors = "Authorized";

            if(approveDeclineTopics.equals("Authorized") || approveDeclineSubscriptions.equals("Authorized") ||
                    approveDeclineSchemas.equals("Authorized") || approveDeclineConnectors.equals("Authorized") ||
                    addUser.equals("Authorized"))
                approveAtleastOneRequest = "Authorized";

            String redirectionPage = "";
            if(approveAtleastOneRequest.equals("Authorized"))
            {
                if(outstandingTopicReqsInt > 0 && approveDeclineTopics.equals("Authorized"))
                    redirectionPage = "execTopics";
                else if(outstandingAclReqsInt > 0 && approveDeclineSubscriptions.equals("Authorized"))
                    redirectionPage = "execAcls";
                else if(outstandingSchemasReqsInt > 0 && approveDeclineSchemas.equals("Authorized"))
                    redirectionPage = "execSchemas";
                else if(outstandingConnectorReqsInt > 0 && approveDeclineConnectors.equals("Authorized"))
                    redirectionPage = "execConnectors";
                else if(outstandingUserReqsInt > 0 && addUser.equals("Authorized"))
                    redirectionPage = "execUsers";
            }

            String syncTopicsAcls, syncConnectors;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_TOPICS) ||
                    commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_SUBSCRIPTIONS)){
                syncTopicsAcls = "NotAuthorized";
            }
            else syncTopicsAcls = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_CONNECTORS)){
                syncConnectors = "NotAuthorized";
            }
            else syncConnectors = "Authorized";

            String addDeleteEditTenants;
            String addDeleteEditEnvs;
            String addDeleteEditClusters;
            String updateServerConfig, showServerConfigEnvProperties;
            String viewTopics;

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.UPDATE_SERVERCONFIG))
                updateServerConfig = "NotAuthorized";
            else updateServerConfig = "Authorized";

            if(tenantId == DEFAULT_TENANT_ID && !commonUtilsService.isNotAuthorizedUser(userName, PermissionType.UPDATE_SERVERCONFIG))
                showServerConfigEnvProperties = "Authorized";
            else showServerConfigEnvProperties = "NotAuthorized";

            if(tenantId == DEFAULT_TENANT_ID &&
                    manageDatabase.getHandleDbRequests().getUsersInfo(userName).getRole().equals(SUPERADMIN.name())) // allow adding tenants only to "default"
                addDeleteEditTenants = "Authorized";
            else addDeleteEditTenants = "NotAuthorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_ENVS))
                addDeleteEditEnvs = "NotAuthorized";
            else addDeleteEditEnvs = "Authorized";

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_CLUSTERS))
                addDeleteEditClusters = "NotAuthorized";
            else addDeleteEditClusters = "Authorized";

            if(authenticationType.equals("ad") && adAuthRoleEnabled.equals("true"))
                dashboardData.put("adAuthRoleEnabled","true");
            else
                dashboardData.put("adAuthRoleEnabled","false");

            if(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.VIEW_TOPICS))
                viewTopics = "NotAuthorized";
            else viewTopics = "Authorized";

            String companyInfo = manageDatabase.getTenantFullConfig(tenantId).getOrgName();
            if(companyInfo == null || companyInfo.equals(""))
            {
                companyInfo = "Our Organization";
            }

            if(kwInstallationType.equals("saas"))
                dashboardData.put("supportlink","https://github.com/muralibasani/kafkawize/issues");
            else
                dashboardData.put("supportlink","https://github.com/muralibasani/kafkawize/issues");

            // broadcast text
            String broadCastText = "";
            String broadCastTextLocal = manageDatabase.getKwPropertyValue(broadCastTextProperty, tenantId);
            String broadCastTextGlobal = manageDatabase.getKwPropertyValue(broadCastTextProperty, DEFAULT_TENANT_ID);

            if(broadCastTextLocal != null && !broadCastTextLocal.equals(""))
                broadCastText = "Announcement : " + broadCastTextLocal;
            if(broadCastTextGlobal != null && !broadCastTextGlobal.equals("") && tenantId != DEFAULT_TENANT_ID) {
                broadCastText += " Announcement : " + broadCastTextGlobal;
            }

            dashboardData.put("broadcastText", broadCastText);

            dashboardData.put("saasEnabled",kwInstallationType);
            dashboardData.put("tenantActiveStatus", manageDatabase.getTenantFullConfig(tenantId).getIsActive()); // true/false

            dashboardData.put("username",userName);
            dashboardData.put("authenticationType", authenticationType);
            dashboardData.put("teamname",teamName);
            dashboardData.put("tenantName",getTenantNameFromUser(getUserName()));
            dashboardData.put("userrole",authority);
            dashboardData.put("companyinfo", companyInfo);
            dashboardData.put("kafkawizeversion",kafkawizeVersion);

            dashboardData.put("notifications",outstandingTopicReqs);
            dashboardData.put("notificationsAcls",outstandingAclReqs);
            dashboardData.put("notificationsSchemas",outstandingSchemasReqs);
            dashboardData.put("notificationsUsers",outstandingUserReqs);
            dashboardData.put("notificationsConnectors",outstandingConnectorReqs);

            dashboardData.put("canShutdownKw",canShutdownKw);
            dashboardData.put("canUpdatePermissions",canUpdatePermissions);
            dashboardData.put("addEditRoles", addEditRoles);
            dashboardData.put("viewTopics", viewTopics);
            dashboardData.put("requestItems", requestItems);
            dashboardData.put("viewKafkaConnect", viewKafkaConnect);

            dashboardData.put("syncBackTopics", syncBackTopics);
            dashboardData.put("syncBackAcls", syncBackAcls);
            dashboardData.put("updateServerConfig", updateServerConfig);
            dashboardData.put("showServerConfigEnvProperties", showServerConfigEnvProperties);

            dashboardData.put("addUser", addUser);
            dashboardData.put("addTeams", addTeams);

            dashboardData.put("syncTopicsAcls", syncTopicsAcls);
            dashboardData.put("syncConnectors", syncConnectors);

            dashboardData.put("approveAtleastOneRequest",approveAtleastOneRequest);
            dashboardData.put("approveDeclineTopics",approveDeclineTopics);
            dashboardData.put("approveDeclineSubscriptions",approveDeclineSubscriptions);
            dashboardData.put("approveDeclineSchemas",approveDeclineSchemas);
            dashboardData.put("approveDeclineConnectors",approveDeclineConnectors);
            dashboardData.put("pendingApprovalsRedirectionPage", redirectionPage);

            dashboardData.put("showAddDeleteTenants",addDeleteEditTenants);
            dashboardData.put("addDeleteEditClusters",addDeleteEditClusters);
            dashboardData.put("addDeleteEditEnvs",addDeleteEditEnvs);
//            if(manageDatabase.getIsTrialLicense())
//                dashboardData.put("larit","(Trial) ");
//            else
                dashboardData.put("larit","");

            return dashboardData;
        }
        else return null;
    }

    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response){
        Authentication authentication = commonUtilsService.getAuthentication();
        if (authentication != null)
            new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    public void shutdownContext(){
        int tenantId = commonUtilsService.getTenantId(getUserName());
        if(tenantId != DEFAULT_TENANT_ID)
            return;

        if(!commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SHUTDOWN_KAFKAWIZE)){
            log.info("Kafkawize Shutdown requested by {}", getUserName());
            context.close();
        }
    }

    private Object getPrincipal(){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public HashMap<String, String> getBasicInfo() {
        HashMap<String, String> resultBasicInfo = new HashMap<>();
        resultBasicInfo.put("contextPath", kwContextPath);

        if(ssoEnabled.equals("true")) {
            resultBasicInfo.put("ssoServerUrl", ssoServerLoginUrl);
        }

        return resultBasicInfo;
    }

    public void resetCache(String tenantName, String entityType, String operationType) {
        int tenantId = 0;
        try {
            tenantId = manageDatabase.getHandleDbRequests().getTenants().stream()
                    .filter(kwTenant -> kwTenant.getTenantName().equals(tenantName))
                    .findFirst().get().getTenantId();
        } catch (Exception exception) {
            log.info("ignore");
            return;
        }

        KwMetadataUpdates kwMetadataUpdates = KwMetadataUpdates.builder()
                .tenantId(tenantId)
                .entityType(entityType)
                .operationType(operationType)
                .createdTime(new Timestamp(System.currentTimeMillis())).build();
        try {
            CompletableFuture.runAsync(() -> {
                commonUtilsService.updateMetadata(kwMetadataUpdates);
            }).get();
        } catch (InterruptedException | ExecutionException e) {
         log.error("Error from resetCache "+e.getMessage());
        }
    }
}
