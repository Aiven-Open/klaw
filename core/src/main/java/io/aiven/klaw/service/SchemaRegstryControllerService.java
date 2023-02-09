package io.aiven.klaw.service;

import static io.aiven.klaw.model.enums.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaPromotion;
import io.aiven.klaw.model.SchemaRequestModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchemaRegstryControllerService {
  @Autowired ManageDatabase manageDatabase;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  public SchemaRegstryControllerService(
      ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public List<SchemaRequestModel> getSchemaRequests(
      String pageNo,
      String currentPage,
      String requestsType,
      boolean isApproval,
      String topic,
      String env,
      String search) {
    log.debug("getSchemaRequests page {} requestsType {}", pageNo, requestsType);
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    List<SchemaRequest> schemaReqs =
        manageDatabase
            .getHandleDbRequests()
            .getAllSchemaRequests(isApproval, userName, tenantId, topic, env, requestsType, search);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    if (schemaReqs != null) {
      schemaReqs =
          schemaReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());
    }

    Integer userTeamId = commonUtilsService.getTeamId(userName);
    List<UserInfo> userList =
        manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(userTeamId, tenantId);

    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("CONNECTORS", tenantId);
    List<SchemaRequestModel> schemaRequestModels = new ArrayList<>();

    SchemaRequestModel schemaRequestModel;
    if (schemaReqs != null) {
      for (SchemaRequest schemaReq : schemaReqs) {
        schemaRequestModel = new SchemaRequestModel();
        schemaReq.setEnvironmentName(
            manageDatabase
                .getHandleDbRequests()
                .selectEnvDetails(schemaReq.getEnvironment(), tenantId)
                .getName());
        copyProperties(schemaReq, schemaRequestModel);

        // show approving info only before approvals
        if (!RequestStatus.APPROVED.value.equals(schemaRequestModel.getTopicstatus())) {
          schemaRequestModel.setApprovingTeamDetails(
              updateApproverInfo(
                  userList,
                  manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                  approverRoles,
                  schemaRequestModel.getUsername()));
        }
        schemaRequestModels.add(schemaRequestModel);
      }
    }

    return getSchemaRequestsPaged(schemaRequestModels, pageNo, currentPage, tenantId);
  }

  private String updateApproverInfo(
      List<UserInfo> userList, String teamName, List<String> approverRoles, String requestor) {
    StringBuilder approvingInfo = new StringBuilder("Team : " + teamName + ", Users : ");

    for (UserInfo userInfo : userList) {
      if (approverRoles.contains(userInfo.getRole())
          && !Objects.equals(requestor, userInfo.getUsername())) {
        approvingInfo.append(userInfo.getUsername()).append(",");
      }
    }

    return String.valueOf(approvingInfo);
  }

  private List<SchemaRequestModel> getSchemaRequestsPaged(
      List<SchemaRequestModel> schemaRequestModelList,
      String pageNo,
      String currentPage,
      int tenantId) {

    List<SchemaRequestModel> newList = new ArrayList<>();

    if (schemaRequestModelList != null && schemaRequestModelList.size() > 0) {
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
          schemaRequestModel.setTeamname(
              manageDatabase.getTeamNameFromTeamId(tenantId, schemaRequestModel.getTeamId()));
          newList.add(schemaRequestModel);
        }
      }
    }

    return newList;
  }

  public ApiResponse deleteSchemaRequests(String avroSchemaId) throws KlawException {
    log.info("deleteSchemaRequests {}", avroSchemaId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_DELETE_SCHEMAS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    try {
      String result =
          manageDatabase
              .getHandleDbRequests()
              .deleteSchemaRequest(
                  Integer.parseInt(avroSchemaId), commonUtilsService.getTenantId(getUserName()));
      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse execSchemaRequests(String avroSchemaId) throws KlawException {
    log.info("execSchemaRequests {}", avroSchemaId);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_SCHEMAS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    SchemaRequest schemaRequest =
        manageDatabase
            .getHandleDbRequests()
            .selectSchemaRequest(Integer.parseInt(avroSchemaId), tenantId);

    if (Objects.equals(schemaRequest.getUsername(), userDetails))
      return ApiResponse.builder()
          .result("You are not allowed to approve your own schema requests.")
          .build();

    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());

    if (!allowedEnvIdSet.contains(schemaRequest.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    ResponseEntity<ApiResponse> response =
        clusterApiService.postSchema(
            schemaRequest, schemaRequest.getEnvironment(), schemaRequest.getTopicname(), tenantId);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    if (Objects.requireNonNull(Objects.requireNonNull(response.getBody()).getResult())
        .contains("id\":")) {
      try {
        String responseDb = dbHandle.updateSchemaRequest(schemaRequest, userDetails);
        mailService.sendMail(
            schemaRequest.getTopicname(),
            null,
            "",
            schemaRequest.getUsername(),
            dbHandle,
            SCHEMA_REQUEST_APPROVED,
            commonUtilsService.getLoginUrl());
        return ApiResponse.builder().result(responseDb).build();
      } catch (Exception e) {
        log.error("Exception:", e);
        throw new KlawException(e.getMessage());
      }

    } else {
      String errStr = response.getBody().getResult();
      if (errStr.length() > 100) {
        errStr = errStr.substring(0, 98) + "...";
      }
      return ApiResponse.builder().result("Failure in uploading schema. Error : " + errStr).build();
    }
  }

  public ApiResponse execSchemaRequestsDecline(String avroSchemaId, String reasonForDecline)
      throws KlawException {
    log.info("execSchemaRequestsDecline {}", avroSchemaId);
    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_SCHEMAS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    SchemaRequest schemaRequest =
        dbHandle.selectSchemaRequest(Integer.parseInt(avroSchemaId), tenantId);
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());

    if (!allowedEnvIdSet.contains(schemaRequest.getEnvironment())) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    try {
      String responseDb = dbHandle.updateSchemaRequestDecline(schemaRequest, userDetails);
      mailService.sendMail(
          schemaRequest.getTopicname(),
          null,
          reasonForDecline,
          schemaRequest.getUsername(),
          dbHandle,
          SCHEMA_REQUEST_DENIED,
          commonUtilsService.getLoginUrl());
      return ApiResponse.builder().result(responseDb).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse promoteSchema(SchemaPromotion schemaPromotion) throws Exception {
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SCHEMAS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    Integer userTeamId = commonUtilsService.getTeamId(userDetails);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    int teamId = commonUtilsService.getTeamId(userDetails);

    if (!userAndTopicOwnerAreOnTheSameTeam(schemaPromotion.getTopicName(), userTeamId, tenantId)) {
      return ApiResponse.builder()
          .result("No topic selected Or Not authorized to register schema for this topic.")
          .build();
    }

    SchemaRequestModel schemaRequest =
        buildSchemaRequestFromPromotionRequest(schemaPromotion, teamId, tenantId);

    Optional<Env> optionalEnv = getSchemaEnvFromId(schemaPromotion.getSourceEnvironment());

    if (!optionalEnv.isPresent()) {
      return ApiResponse.builder()
          .status(HttpStatus.BAD_REQUEST)
          .result("Unable to find or access the source Schema Registry")
          .build();
    }
    Env schemaSourceEnv = optionalEnv.get();
    SortedMap<Integer, Map<String, Object>> schemaObjects =
        getSchemasFromTopicName(schemaPromotion.getTopicName(), tenantId, schemaSourceEnv);
    log.info(
        "getSchemaVersion {}, schemaObjects keySet {}",
        schemaPromotion.getSchemaVersion(),
        schemaObjects.keySet());
    Map<String, Object> schemaObject =
        schemaObjects.get(Integer.valueOf(schemaPromotion.getSchemaVersion()));
    // Pretty Print the Json String so that it can be seen clearly in the UI.
    schemaRequest.setSchemafull(prettyPrintUglyJsonString((String) schemaObject.get("schema")));
    return uploadSchema(schemaRequest);
  }

  private boolean userAndTopicOwnerAreOnTheSameTeam(
      String topicName, Integer userTeamId, Integer tenantId) {
    List<Topic> topicsSearchList =
        manageDatabase.getHandleDbRequests().getTopicTeam(topicName, tenantId);

    // tenant filtering
    Integer topicOwnerTeam =
        commonUtilsService.getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();
    return Objects.equals(userTeamId, topicOwnerTeam);
  }

  private SortedMap<Integer, Map<String, Object>> getSchemasFromTopicName(
      String topicName, int tenantId, Env schemaEnv) throws Exception {
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
            .get(schemaEnv.getClusterId());

    SortedMap<Integer, Map<String, Object>> schemaObjects =
        clusterApiService.getAvroSchema(
            kwClusters.getBootstrapServers(),
            kwClusters.getProtocol(),
            kwClusters.getClusterName() + kwClusters.getClusterId(),
            topicName,
            tenantId);

    return schemaObjects;
  }

  private Optional<Env> getSchemaEnvFromId(String envId) {
    return manageDatabase
        .getSchemaRegEnvList(commonUtilsService.getTenantId(getUserName()))
        .stream()
        .filter(env -> env.getId().equals(envId))
        .findFirst();
  }

  private SchemaRequestModel buildSchemaRequestFromPromotionRequest(
      SchemaPromotion schemaPromotion, int teamId, int tenantId) {
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    // setup schema Request
    schemaRequest.setAppname(schemaPromotion.getAppName());
    schemaRequest.setRemarks(schemaPromotion.getRemarks());
    schemaRequest.setEnvironment(schemaPromotion.getTargetEnvironment());
    schemaRequest.setSchemaversion(schemaPromotion.getSchemaVersion());
    schemaRequest.setTopicname(schemaPromotion.getTopicName());
    schemaRequest.setForceRegister(schemaPromotion.isForceRegister());
    return schemaRequest;
  }

  public ApiResponse uploadSchema(SchemaRequestModel schemaRequest) throws KlawException {
    log.info("uploadSchema {}", schemaRequest);
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SCHEMAS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    try {
      new ObjectMapper().readValue(schemaRequest.getSchemafull(), Object.class);
    } catch (IOException e) {
      log.error("Exception:", e);
      return ApiResponse.builder().result("Failure. Invalid json").build();
    }

    Integer userTeamId = commonUtilsService.getTeamId(userDetails);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (!userAndTopicOwnerAreOnTheSameTeam(schemaRequest.getTopicname(), userTeamId, tenantId)) {
      return ApiResponse.builder()
          .result("No topic selected Or Not authorized to register schema for this topic.")
          .build();
    }

    List<SchemaRequest> schemaReqs =
        manageDatabase
            .getHandleDbRequests()
            .getAllSchemaRequests(false, userDetails, tenantId, null, null, null, null);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());
    if (schemaReqs != null)
      schemaReqs =
          schemaReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());

    // request status filtering
    if (schemaReqs != null) {
      schemaReqs =
          schemaReqs.stream()
              .filter(
                  schemaRequest1 ->
                      "created".equals(schemaRequest1.getTopicstatus())
                          && Objects.equals(
                              schemaRequest1.getTopicname(), schemaRequest.getTopicname()))
              .collect(Collectors.toList());
      if (schemaReqs.size() > 0) {
        return ApiResponse.builder()
            .result("Failure. A request already exists for this topic.")
            .build();
      }
    }

    schemaRequest.setUsername(userDetails);
    SchemaRequest schemaRequestDao = new SchemaRequest();
    copyProperties(schemaRequest, schemaRequestDao);
    schemaRequestDao.setRequesttype(RequestOperationType.CREATE.value);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    schemaRequestDao.setTenantId(tenantId);
    try {
      String responseDb = dbHandle.requestForSchema(schemaRequestDao);
      mailService.sendMail(
          schemaRequest.getTopicname(),
          null,
          "",
          schemaRequest.getUsername(),
          dbHandle,
          SCHEMA_REQUESTED,
          commonUtilsService.getLoginUrl());

      return ApiResponse.builder().result(responseDb).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private String prettyPrintUglyJsonString(String json) {
    ObjectMapper mapper = new ObjectMapper();

    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json));
    } catch (JsonProcessingException e) {
      log.error("Unable to pretty print json : ", e);
    }
    return json;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
