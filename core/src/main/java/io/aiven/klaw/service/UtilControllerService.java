package io.aiven.klaw.service;

import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.RolesType.SUPERADMIN;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.model.KwMetadataUpdates;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UtilControllerService {

  @Autowired ManageDatabase manageDatabase;

  @Autowired MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Value("${klaw.version}")
  private String klawVersion;

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${klaw.enable.authorization.ad:false}")
  private String adAuthRoleEnabled;

  @Value("${klaw.admin.mailid:info@klaw-project.io}")
  private String saasKwAdminMailId;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${server.servlet.context-path:}")
  private String kwContextPath;

  @Value("${klaw.enable.sso:false}")
  private String ssoEnabled;

  @Value("${klaw.sso.server.loginurl:url}")
  private String ssoServerLoginUrl;

  @Autowired private ConfigurableApplicationContext context;

  public Map<String, String> getDashboardStats() {
    log.debug("getDashboardInfo");
    String userName = getUserName();
    HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
    if (userName != null) {
      return reqsHandle.getDashboardStats(
          commonUtilsService.getTeamId(userName), commonUtilsService.getTenantId(getUserName()));
    }

    return new HashMap<>();
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  public String getTenantNameFromUser(String userId) {
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
    if (userInfo != null) {
      return manageDatabase.getTenantMap().entrySet().stream()
          .filter(obj -> Objects.equals(obj.getKey(), userInfo.getTenantId()))
          .findFirst()
          .get()
          .getValue();

    } else {
      return null;
    }
  }

  public Map<String, String> getAllRequestsToBeApproved(String requestor, int tenantId) {
    log.debug("getAllRequestsToBeApproved {}", requestor);
    HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();

    Map<String, String> countList = new HashMap<>();
    String roleToSet = "";
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_SUBSCRIPTIONS)) {
      roleToSet = "approver_subscriptions";
    }
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
      roleToSet = "requestor_subscriptions";
    }
    List<SchemaRequest> allSchemaReqs = reqsHandle.getAllSchemaRequests(true, requestor, tenantId);

    List<AclRequests> allAclReqs;
    List<TopicRequest> allTopicReqs;
    List<KafkaConnectorRequest> allConnectorReqs;

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS)) {
      allAclReqs =
          reqsHandle.getAllAclRequests(
              true, requestor, roleToSet, RequestStatus.CREATED.value, false, tenantId);
      allTopicReqs =
          reqsHandle.getCreatedTopicRequests(
              requestor, RequestStatus.CREATED.value, false, tenantId);
      allConnectorReqs =
          reqsHandle.getCreatedConnectorRequests(
              requestor, RequestStatus.CREATED.value, false, tenantId);
    } else {
      allAclReqs =
          reqsHandle.getAllAclRequests(
              true, requestor, roleToSet, RequestStatus.CREATED.value, true, tenantId);
      allTopicReqs =
          reqsHandle.getCreatedTopicRequests(
              requestor, RequestStatus.CREATED.value, true, tenantId);
      allConnectorReqs =
          reqsHandle.getCreatedConnectorRequests(
              requestor, RequestStatus.CREATED.value, true, tenantId);
    }

    try {
      // tenant filtering
      final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());
      allAclReqs =
          allAclReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());

      // tenant filtering
      allSchemaReqs =
          allSchemaReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());

      // tenant filtering
      allTopicReqs =
          allTopicReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());

      // tenant filtering
      allConnectorReqs =
          allConnectorReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
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

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      countList.put("users", "0");
    } else {
      countList.put("users", allUserReqs.size() + "");
    }

    return countList;
  }

  public Map<String, String> getAuth() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String userName = getUserName();
    HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
    if (userName != null) {
      String teamName =
          manageDatabase.getTeamNameFromTeamId(tenantId, commonUtilsService.getTeamId(userName));
      String authority = commonUtilsService.getAuthority(getPrincipal());
      Map<String, String> outstanding = getAllRequestsToBeApproved(userName, tenantId);

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

      if (outstandingTopicReqsInt <= 0) {
        outstandingTopicReqs = "0";
      }

      if (outstandingAclReqsInt <= 0) {
        outstandingAclReqs = "0";
      }

      if (outstandingSchemasReqsInt <= 0) {
        outstandingSchemasReqs = "0";
      }

      if (outstandingConnectorReqsInt <= 0) {
        outstandingConnectorReqs = "0";
      }

      if (outstandingUserReqsInt <= 0) {
        outstandingUserReqs = "0";
      }

      Map<String, String> dashboardData =
          reqsHandle.getDashboardInfo(commonUtilsService.getTeamId(userName), tenantId);

      dashboardData.put("contextPath", kwContextPath);
      dashboardData.put("teamsize", "" + manageDatabase.getTeamsForTenant(tenantId).size());
      dashboardData.put(
          "schema_clusters_count", "" + manageDatabase.getSchemaRegEnvList(tenantId).size());
      dashboardData.put(
          "kafka_clusters_count", "" + manageDatabase.getKafkaEnvList(tenantId).size());
      dashboardData.put(
          "kafkaconnect_clusters_count",
          "" + manageDatabase.getKafkaConnectEnvList(tenantId).size());

      String canUpdatePermissions, addEditRoles;

      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.UPDATE_PERMISSIONS)) {
        canUpdatePermissions = "NotAuthorized";
      } else {
        canUpdatePermissions = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.ADD_EDIT_DELETE_ROLES)) {
        addEditRoles = "NotAuthorized";
      } else {
        addEditRoles = ApiResultStatus.AUTHORIZED.value;
      }

      String canShutdownKw;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SHUTDOWN_KLAW)) {
        canShutdownKw = "NotAuthorized";
      } else {
        canShutdownKw = ApiResultStatus.AUTHORIZED.value;
      }

      String syncBackTopics, syncBackAcls;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_BACK_TOPICS)) {
        syncBackTopics = "NotAuthorized";
      } else {
        syncBackTopics = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(
          userName, PermissionType.SYNC_BACK_SUBSCRIPTIONS)) {
        syncBackAcls = "NotAuthorized";
      } else {
        syncBackAcls = ApiResultStatus.AUTHORIZED.value;
      }

      String addUser, addTeams;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_USERS)) {
        addUser = "NotAuthorized";
      } else {
        addUser = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_TEAMS)) {
        addTeams = "NotAuthorized";
      } else {
        addTeams = ApiResultStatus.AUTHORIZED.value;
      }

      String viewKafkaConnect;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.VIEW_CONNECTORS)) {
        viewKafkaConnect = "NotAuthorized";
      } else {
        viewKafkaConnect = ApiResultStatus.AUTHORIZED.value;
      }

      String requestTopics;
      String requestAcls;
      String requestSchemas;
      String requestConnector;
      String requestItems;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.REQUEST_CREATE_TOPICS)) {
        requestTopics = "NotAuthorized";
      } else {
        requestTopics = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(
          userName, PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)) {
        requestAcls = "NotAuthorized";
      } else {
        requestAcls = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.REQUEST_CREATE_SCHEMAS)) {
        requestSchemas = "NotAuthorized";
      } else {
        requestSchemas = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(
          userName, PermissionType.REQUEST_CREATE_CONNECTORS)) {
        requestConnector = "NotAuthorized";
      } else {
        requestConnector = ApiResultStatus.AUTHORIZED.value;
      }

      if (ApiResultStatus.AUTHORIZED.value.equals(requestTopics)
          || ApiResultStatus.AUTHORIZED.value.equals(requestAcls)
          || ApiResultStatus.AUTHORIZED.value.equals(requestSchemas)
          || ApiResultStatus.AUTHORIZED.value.equals(requestConnector)) {
        requestItems = ApiResultStatus.AUTHORIZED.value;
      } else {
        requestItems = "NotAuthorized";
      }

      String approveDeclineTopics;
      String approveDeclineSubscriptions;
      String approveDeclineSchemas;
      String approveDeclineConnectors;

      String approveAtleastOneRequest = "NotAuthorized";

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_TOPICS)) {
        approveDeclineTopics = "NotAuthorized";
      } else {
        approveDeclineTopics = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_SUBSCRIPTIONS)) {
        approveDeclineSubscriptions = "NotAuthorized";
      } else {
        approveDeclineSubscriptions = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_SCHEMAS)) {
        approveDeclineSchemas = "NotAuthorized";
      } else {
        approveDeclineSchemas = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.APPROVE_CONNECTORS)) {
        approveDeclineConnectors = "NotAuthorized";
      } else {
        approveDeclineConnectors = ApiResultStatus.AUTHORIZED.value;
      }

      if (ApiResultStatus.AUTHORIZED.value.equals(approveDeclineTopics)
          || ApiResultStatus.AUTHORIZED.value.equals(approveDeclineSubscriptions)
          || ApiResultStatus.AUTHORIZED.value.equals(approveDeclineSchemas)
          || ApiResultStatus.AUTHORIZED.value.equals(approveDeclineConnectors)
          || ApiResultStatus.AUTHORIZED.value.equals(addUser)) {
        approveAtleastOneRequest = ApiResultStatus.AUTHORIZED.value;
      }

      String redirectionPage = "";
      if (ApiResultStatus.AUTHORIZED.value.equals(approveAtleastOneRequest)) {
        if (outstandingTopicReqsInt > 0
            && ApiResultStatus.AUTHORIZED.value.equals(approveDeclineTopics)) {
          redirectionPage = "execTopics";
        } else if (outstandingAclReqsInt > 0
            && ApiResultStatus.AUTHORIZED.value.equals(approveDeclineSubscriptions)) {
          redirectionPage = "execAcls";
        } else if (outstandingSchemasReqsInt > 0
            && ApiResultStatus.AUTHORIZED.value.equals(approveDeclineSchemas)) {
          redirectionPage = "execSchemas";
        } else if (outstandingConnectorReqsInt > 0
            && ApiResultStatus.AUTHORIZED.value.equals(approveDeclineConnectors)) {
          redirectionPage = "execConnectors";
        } else if (outstandingUserReqsInt > 0 && ApiResultStatus.AUTHORIZED.value.equals(addUser)) {
          redirectionPage = "execUsers";
        }
      }

      String syncTopicsAcls, syncConnectors;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_TOPICS)
          || commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_SUBSCRIPTIONS)) {
        syncTopicsAcls = "NotAuthorized";
      } else {
        syncTopicsAcls = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.SYNC_CONNECTORS)) {
        syncConnectors = "NotAuthorized";
      } else {
        syncConnectors = ApiResultStatus.AUTHORIZED.value;
      }

      String addDeleteEditTenants;
      String addDeleteEditEnvs;
      String addDeleteEditClusters;
      String updateServerConfig, showServerConfigEnvProperties;
      String viewTopics;

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.UPDATE_SERVERCONFIG)) {
        updateServerConfig = "NotAuthorized";
      } else {
        updateServerConfig = ApiResultStatus.AUTHORIZED.value;
      }

      if (tenantId == KwConstants.DEFAULT_TENANT_ID
          && !commonUtilsService.isNotAuthorizedUser(
              userName, PermissionType.UPDATE_SERVERCONFIG)) {
        showServerConfigEnvProperties = ApiResultStatus.AUTHORIZED.value;
      } else {
        showServerConfigEnvProperties = "NotAuthorized";
      }

      if (tenantId == KwConstants.DEFAULT_TENANT_ID
          && SUPERADMIN
              .name()
              .equals(
                  manageDatabase
                      .getHandleDbRequests()
                      .getUsersInfo(userName)
                      .getRole())) { // allow adding tenants only to "default"
        addDeleteEditTenants = ApiResultStatus.AUTHORIZED.value;
      } else {
        addDeleteEditTenants = "NotAuthorized";
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_ENVS)) {
        addDeleteEditEnvs = "NotAuthorized";
      } else {
        addDeleteEditEnvs = ApiResultStatus.AUTHORIZED.value;
      }

      if (commonUtilsService.isNotAuthorizedUser(
          userName, PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
        addDeleteEditClusters = "NotAuthorized";
      } else {
        addDeleteEditClusters = ApiResultStatus.AUTHORIZED.value;
      }

      if (ACTIVE_DIRECTORY.value.equals(authenticationType) && "true".equals(adAuthRoleEnabled)) {
        dashboardData.put("adAuthRoleEnabled", "true");
      } else {
        dashboardData.put("adAuthRoleEnabled", "false");
      }

      if (commonUtilsService.isNotAuthorizedUser(userName, PermissionType.VIEW_TOPICS)) {
        viewTopics = "NotAuthorized";
      } else {
        viewTopics = ApiResultStatus.AUTHORIZED.value;
      }

      String companyInfo = manageDatabase.getTenantFullConfig(tenantId).getOrgName();
      if (companyInfo == null || companyInfo.equals("")) {
        companyInfo = "Our Organization";
      }

      if ("saas".equals(kwInstallationType)) {
        dashboardData.put("supportlink", "https://github.com/aiven/klaw/issues");
      } else {
        dashboardData.put("supportlink", "https://github.com/aiven/klaw/issues");
      }

      // broadcast text
      String broadCastText = "";
      String broadCastTextLocal =
          manageDatabase.getKwPropertyValue(KwConstants.broadCastTextProperty, tenantId);
      String broadCastTextGlobal =
          manageDatabase.getKwPropertyValue(
              KwConstants.broadCastTextProperty, KwConstants.DEFAULT_TENANT_ID);

      if (broadCastTextLocal != null && !broadCastTextLocal.equals("")) {
        broadCastText = "Announcement : " + broadCastTextLocal;
      }
      if (broadCastTextGlobal != null
          && !broadCastTextGlobal.equals("")
          && tenantId != KwConstants.DEFAULT_TENANT_ID) {
        broadCastText += " Announcement : " + broadCastTextGlobal;
      }

      dashboardData.put("broadcastText", broadCastText);
      dashboardData.put("saasEnabled", kwInstallationType);
      dashboardData.put(
          "tenantActiveStatus",
          manageDatabase.getTenantFullConfig(tenantId).getIsActive()); // true/false
      dashboardData.put("username", userName);
      dashboardData.put("authenticationType", authenticationType);
      dashboardData.put("teamname", teamName);
      dashboardData.put("tenantName", getTenantNameFromUser(getUserName()));
      dashboardData.put("userrole", authority);
      dashboardData.put("companyinfo", companyInfo);
      dashboardData.put("klawversion", klawVersion);

      dashboardData.put("notifications", outstandingTopicReqs);
      dashboardData.put("notificationsAcls", outstandingAclReqs);
      dashboardData.put("notificationsSchemas", outstandingSchemasReqs);
      dashboardData.put("notificationsUsers", outstandingUserReqs);
      dashboardData.put("notificationsConnectors", outstandingConnectorReqs);

      dashboardData.put("canShutdownKw", canShutdownKw);
      dashboardData.put("canUpdatePermissions", canUpdatePermissions);
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

      dashboardData.put("approveAtleastOneRequest", approveAtleastOneRequest);
      dashboardData.put("approveDeclineTopics", approveDeclineTopics);
      dashboardData.put("approveDeclineSubscriptions", approveDeclineSubscriptions);
      dashboardData.put("approveDeclineSchemas", approveDeclineSchemas);
      dashboardData.put("approveDeclineConnectors", approveDeclineConnectors);
      dashboardData.put("pendingApprovalsRedirectionPage", redirectionPage);

      dashboardData.put("showAddDeleteTenants", addDeleteEditTenants);
      dashboardData.put("addDeleteEditClusters", addDeleteEditClusters);
      dashboardData.put("addDeleteEditEnvs", addDeleteEditEnvs);
      dashboardData.put("larit", "");

      return dashboardData;
    } else return null;
  }

  public void getLogoutPage(HttpServletRequest request, HttpServletResponse response) {
    Authentication authentication = commonUtilsService.getAuthentication();
    if (authentication != null) {
      new SecurityContextLogoutHandler().logout(request, response, authentication);
    }
  }

  public void shutdownContext() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (tenantId != KwConstants.DEFAULT_TENANT_ID) {
      return;
    }

    if (!commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SHUTDOWN_KLAW)) {
      log.info("Klaw Shutdown requested by {}", getUserName());
      context.close();
    }
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public Map<String, String> getBasicInfo() {
    Map<String, String> resultBasicInfo = new HashMap<>();
    resultBasicInfo.put("contextPath", kwContextPath);

    if ("true".equals(ssoEnabled)) {
      resultBasicInfo.put("ssoServerUrl", ssoServerLoginUrl);
    }

    return resultBasicInfo;
  }

  public void resetCache(String tenantName, String entityType, String operationType) {
    int tenantId = 0;
    try {
      tenantId =
          manageDatabase.getHandleDbRequests().getTenants().stream()
              .filter(kwTenant -> Objects.equals(kwTenant.getTenantName(), tenantName))
              .findFirst()
              .get()
              .getTenantId();
    } catch (Exception e) {
      log.info("ignore", e);
      return;
    }

    KwMetadataUpdates kwMetadataUpdates =
        KwMetadataUpdates.builder()
            .tenantId(tenantId)
            .entityType(entityType)
            .operationType(operationType)
            .createdTime(new Timestamp(System.currentTimeMillis()))
            .build();
    try {
      CompletableFuture.runAsync(
              () -> {
                commonUtilsService.updateMetadata(kwMetadataUpdates);
              })
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Error from resetCache ", e);
    }
  }
}
