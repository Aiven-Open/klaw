package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_107;
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
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.SchemaPromotion;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.response.BaseRequestsResponseModel;
import io.aiven.klaw.model.response.SchemaRequestsResponseModel;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchemaRegistryControllerService {
  @Value("${klaw.schema.validate.compatibility.onSave:true}")
  private Boolean validateCompatiblityOnSave;

  @Autowired ManageDatabase manageDatabase;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  public SchemaRegistryControllerService(
      ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public List<SchemaRequestsResponseModel> getSchemaRequests(
      String pageNo,
      String currentPage,
      String requestStatus,
      RequestOperationType requestOperationType,
      boolean isApproval,
      String topic,
      String env,
      String search,
      Order order,
      boolean isMyRequest) {
    log.debug("getSchemaRequests page {} requestsType {}", pageNo, requestStatus);

    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    List<SchemaRequest> schemaReqs =
        manageDatabase
            .getHandleDbRequests()
            .getAllSchemaRequests(
                isApproval,
                userName,
                tenantId,
                requestOperationType,
                topic,
                env,
                requestStatus,
                search,
                isApproval
                    && !commonUtilsService.isNotAuthorizedUser(
                        getPrincipal(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS),
                isMyRequest);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    if (schemaReqs != null) {
      schemaReqs =
          schemaReqs.stream()
              .filter(request -> allowedEnvIdSet.contains(request.getEnvironment()))
              .collect(Collectors.toList());
    }

    Integer userTeamId = commonUtilsService.getTeamId(userName);
    List<UserInfo> userList = manageDatabase.getUsersPerTeamAndTenant(userTeamId, tenantId);

    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("CONNECTORS", tenantId);
    List<SchemaRequestsResponseModel> schemaRequestModels = new ArrayList<>();

    SchemaRequestsResponseModel schemaRequestModel;
    if (schemaReqs != null) {
      for (SchemaRequest schemaReq : schemaReqs) {
        schemaRequestModel = new SchemaRequestsResponseModel();
        schemaReq.setEnvironmentName(
            manageDatabase
                .getHandleDbRequests()
                .getEnvDetails(schemaReq.getEnvironment(), tenantId)
                .getName());
        copyProperties(schemaReq, schemaRequestModel);
        schemaRequestModel.setRequestStatus(RequestStatus.of(schemaReq.getRequestStatus()));
        schemaRequestModel.setRequestOperationType(
            RequestOperationType.of(schemaReq.getRequestOperationType()));

        // show approving info only before approvals
        if (RequestStatus.APPROVED != schemaRequestModel.getRequestStatus()) {
          schemaRequestModel.setApprovingTeamDetails(
              updateApproverInfo(
                  userList,
                  manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                  approverRoles,
                  schemaRequestModel.getRequestor()));
        }
        schemaRequestModels.add(setRequestorPermissions(schemaRequestModel, userName));
      }
    }

    schemaRequestModels = schemaRequestModels.stream().sorted(getPreferredOrder(order)).toList();

    return getSchemaRequestsPaged(schemaRequestModels, pageNo, currentPage, tenantId);
  }

  private Comparator<SchemaRequestsResponseModel> getPreferredOrder(Order order) {
    return switch (order) {
      case ASC_REQUESTED_TIME -> Comparator.comparing(BaseRequestsResponseModel::getRequesttime);
      case DESC_REQUESTED_TIME -> Collections.reverseOrder(
          Comparator.comparing(BaseRequestsResponseModel::getRequesttime));
    };
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

  private SchemaRequestsResponseModel setRequestorPermissions(
      SchemaRequestsResponseModel req, String userName) {

    if (RequestStatus.CREATED == req.getRequestStatus()
        && userName != null
        && userName.equals(req.getRequestor())) {
      req.setDeletable(true);
      req.setEditable(true);
    }

    return req;
  }

  private List<SchemaRequestsResponseModel> getSchemaRequestsPaged(
      List<SchemaRequestsResponseModel> schemaRequestModelList,
      String pageNo,
      String currentPage,
      int tenantId) {

    List<SchemaRequestsResponseModel> newList = new ArrayList<>();

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
        SchemaRequestsResponseModel schemaRequestModel = schemaRequestModelList.get(i);
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
      return ApiResponse.NOT_AUTHORIZED;
    }
    String userName = getUserName();
    try {
      String result =
          manageDatabase
              .getHandleDbRequests()
              .deleteSchemaRequest(
                  Integer.parseInt(avroSchemaId),
                  userName,
                  commonUtilsService.getTenantId(getUserName()));
      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
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
      return ApiResponse.NOT_AUTHORIZED;
    }

    SchemaRequest schemaRequest =
        manageDatabase
            .getHandleDbRequests()
            .getSchemaRequest(Integer.parseInt(avroSchemaId), tenantId);

    if (Objects.equals(schemaRequest.getRequestor(), userDetails))
      return ApiResponse.notOk(SCHEMA_ERR_101);

    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());

    if (!allowedEnvIdSet.contains(schemaRequest.getEnvironment())) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    ResponseEntity<ApiResponse> response =
        clusterApiService.postSchema(
            schemaRequest, schemaRequest.getEnvironment(), schemaRequest.getTopicname(), tenantId);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    ApiResponse apiResponse = response.getBody();
    Map<String, Object> registerSchemaCustomResponse = null;
    boolean schemaRegistered = false;
    if (apiResponse != null
        && apiResponse.getData() != null
        && apiResponse.getData() instanceof Map<?, ?>) {
      registerSchemaCustomResponse = (Map) apiResponse.getData();
      schemaRegistered = (Boolean) registerSchemaCustomResponse.get("schemaRegistered");
    }
    if (registerSchemaCustomResponse != null
        && (registerSchemaCustomResponse.containsKey("id"))
        && (registerSchemaCustomResponse.containsKey("compatibility"))) {
      try {
        if (schemaRegistered) {
          schemaRequest.setSchemaversion(registerSchemaCustomResponse.get("version") + "");
          schemaRequest.setSchemaId((Integer) registerSchemaCustomResponse.get("id"));
          schemaRequest.setCompatibility(
              (String) registerSchemaCustomResponse.get("compatibility"));
        }
        String responseDb = dbHandle.updateSchemaRequest(schemaRequest, userDetails);
        if (responseDb.equals(ApiResultStatus.SUCCESS.value)) {
          saveToTopicHistory(userDetails, tenantId, schemaRequest);
        }
        mailService.sendMail(
            schemaRequest.getTopicname(),
            null,
            "",
            schemaRequest.getRequestor(),
            schemaRequest.getApprover(),
            schemaRequest.getTeamId(),
            dbHandle,
            SCHEMA_REQUEST_APPROVED,
            commonUtilsService.getLoginUrl());
        return ApiResultStatus.SUCCESS.value.equals(responseDb)
            ? ApiResponse.ok(responseDb)
            : ApiResponse.notOk(responseDb);
      } catch (Exception e) {
        log.error("Exception:", e);
        throw new KlawException(e.getMessage());
      }

    } else {
      String errStr = response.getBody().getMessage();
      if (errStr.length() > 100) {
        errStr = errStr.substring(0, 98) + "...";
      }
      return ApiResponse.notOk(String.format(SCHEMA_ERR_102, errStr));
    }
  }

  private void saveToTopicHistory(String userDetails, int tenantId, SchemaRequest schemaRequest) {
    manageDatabase.getKafkaEnvList(tenantId).stream()
        .filter(
            kafkaEnv -> {
              if (kafkaEnv.getAssociatedEnv() != null) {
                return kafkaEnv.getAssociatedEnv().getId().equals(schemaRequest.getEnvironment());
              }
              return false;
            })
        .findFirst()
        .ifPresent(
            env ->
                commonUtilsService.saveTopicHistory(
                    schemaRequest.getRequestOperationType(),
                    schemaRequest.getTopicname(),
                    env.getId(),
                    schemaRequest.getRequestor(),
                    schemaRequest.getRequesttime(),
                    schemaRequest.getTeamId(),
                    userDetails,
                    tenantId,
                    RequestEntityType.SCHEMA.name(),
                    RequestEntityType.SCHEMA.name()
                        + " "
                        + schemaRequest.getRequestOperationType()
                        + " Version : "
                        + schemaRequest.getSchemaversion()));
  }

  public ApiResponse execSchemaRequestsDecline(String avroSchemaId, String reasonForDecline)
      throws KlawException {
    log.info("execSchemaRequestsDecline {}", avroSchemaId);
    String userDetails = getUserName();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.APPROVE_SCHEMAS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    SchemaRequest schemaRequest =
        dbHandle.getSchemaRequest(Integer.parseInt(avroSchemaId), tenantId);
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getUserName());

    if (!allowedEnvIdSet.contains(schemaRequest.getEnvironment())) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    try {
      String responseDb = dbHandle.updateSchemaRequestDecline(schemaRequest, userDetails);
      mailService.sendMail(
          schemaRequest.getTopicname(),
          null,
          reasonForDecline,
          schemaRequest.getRequestor(),
          schemaRequest.getApprover(),
          schemaRequest.getTeamId(),
          dbHandle,
          SCHEMA_REQUEST_DENIED,
          commonUtilsService.getLoginUrl());
      return ApiResultStatus.SUCCESS.value.equals(responseDb)
          ? ApiResponse.ok(responseDb)
          : ApiResponse.notOk(responseDb);
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse promoteSchema(SchemaPromotion schemaPromotion) throws Exception {
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SCHEMAS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    Integer userTeamId = commonUtilsService.getTeamId(userDetails);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (!userAndTopicOwnerAreOnTheSameTeam(schemaPromotion.getTopicName(), userTeamId, tenantId)) {
      return ApiResponse.notOk(SCHEMA_ERR_103);
    }

    SchemaRequestModel schemaRequest = buildSchemaRequestFromPromotionRequest(schemaPromotion);
    Optional<Env> optionalEnv = getSchemaEnvFromKafkaEnvId(schemaPromotion.getSourceEnvironment());

    if (optionalEnv.isEmpty()) {
      return ApiResponse.notOk(SCHEMA_ERR_104);
    }
    Env schemaSourceEnv = optionalEnv.get();
    SortedMap<Integer, Map<String, Object>> schemaObjects =
        getSchemasFromTopicName(schemaPromotion.getTopicName(), tenantId, schemaSourceEnv);
    log.debug(
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
    List<Topic> topicsSearchList = commonUtilsService.getTopicsForTopicName(topicName, tenantId);

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

    return clusterApiService.getAvroSchema(
        kwClusters.getBootstrapServers(),
        kwClusters.getProtocol(),
        kwClusters.getClusterName() + kwClusters.getClusterId(),
        topicName,
        tenantId);
  }

  private Optional<Env> getSchemaEnvFromKafkaEnvId(String envId) {
    return manageDatabase
        .getSchemaRegEnvList(commonUtilsService.getTenantId(getUserName()))
        .stream()
        .filter(
            env -> env.getAssociatedEnv() != null && env.getAssociatedEnv().getId().equals(envId))
        .findFirst();
  }

  private SchemaRequestModel buildSchemaRequestFromPromotionRequest(
      SchemaPromotion schemaPromotion) {
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    // setup schema Request
    schemaRequest.setRemarks(schemaPromotion.getRemarks());

    schemaRequest.setSchemaversion(schemaPromotion.getSchemaVersion());
    schemaRequest.setTopicname(schemaPromotion.getTopicName());
    schemaRequest.setForceRegister(schemaPromotion.isForceRegister());
    Optional<Env> env = getSchemaEnvFromKafkaEnvId(schemaPromotion.getTargetEnvironment());
    schemaRequest.setEnvironment(env.isPresent() ? env.get().getId() : null);
    return schemaRequest;
  }

  public ApiResponse uploadSchema(SchemaRequestModel schemaRequest) throws KlawException {
    log.info("uploadSchema {}", schemaRequest);
    String userName = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SCHEMAS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    // If force register is not set validate the schema
    if (validateCompatiblityOnSave
        && (schemaRequest.getForceRegister() == null || !schemaRequest.getForceRegister())) {
      // check if Schema is valid
      ApiResponse isValid = validateSchema(schemaRequest);
      if (!isValid.isSuccess()) {
        // Return on Failure response
        return isValid;
      }
    }

    try {
      new ObjectMapper().readValue(schemaRequest.getSchemafull(), Object.class);
    } catch (IOException e) {
      log.error("Exception:", e);
      return ApiResponse.notOk(SCHEMA_ERR_105);
    }

    Integer userTeamId = commonUtilsService.getTeamId(userName);
    schemaRequest.setTeamId(userTeamId);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (!userAndTopicOwnerAreOnTheSameTeam(schemaRequest.getTopicname(), userTeamId, tenantId)) {
      return ApiResponse.notOk(SCHEMA_ERR_106);
    }

    List<SchemaRequest> schemaReqs =
        manageDatabase
            .getHandleDbRequests()
            .getAllSchemaRequests(
                false, userName, tenantId, null, null, null, null, null, false, false);

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
                      "created".equals(schemaRequest1.getRequestStatus())
                          && Objects.equals(
                              schemaRequest1.getTopicname(), schemaRequest.getTopicname()))
              .collect(Collectors.toList());
      if (schemaReqs.size() > 0) {
        return ApiResponse.notOk(SCHEMA_ERR_107);
      }
    }

    schemaRequest.setRequestor(userName);
    SchemaRequest schemaRequestDao = new SchemaRequest();
    copyProperties(schemaRequest, schemaRequestDao);
    schemaRequestDao.setRequestOperationType(RequestOperationType.CREATE.value);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    schemaRequestDao.setTenantId(tenantId);
    try {
      String responseDb = dbHandle.requestForSchema(schemaRequestDao);
      mailService.sendMail(
          schemaRequest.getTopicname(),
          null,
          "",
          schemaRequest.getRequestor(),
          null,
          schemaRequest.getTeamId(),
          dbHandle,
          SCHEMA_REQUESTED,
          commonUtilsService.getLoginUrl());

      return ApiResultStatus.SUCCESS.value.equals(responseDb)
          ? ApiResponse.ok(responseDb)
          : ApiResponse.notOk(responseDb);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse validateSchema(SchemaRequestModel schemaRequest) throws KlawException {
    log.info("validateSchema {}", schemaRequest);
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    try {
      return clusterApiService
          .validateSchema(
              schemaRequest.getSchemafull(),
              schemaRequest.getEnvironment(),
              schemaRequest.getTopicname(),
              tenantId)
          .getBody();
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
