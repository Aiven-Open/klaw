package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_108;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_109;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_110;
import static io.aiven.klaw.error.KlawErrorMessages.SCHEMA_ERR_111;
import static io.aiven.klaw.helpers.UtilMethods.updateEnvStatus;
import static io.aiven.klaw.model.enums.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.*;
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

    Set<String> approverRoles =
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

    return Pager.getItemsList(
        pageNo,
        currentPage,
        10,
        schemaRequestModels,
        (pageContext, schemaRequestModel1) -> {
          schemaRequestModel1.setAllPageNos(pageContext.getAllPageNos());
          schemaRequestModel1.setTotalNoPages(pageContext.getTotalPages());
          schemaRequestModel1.setCurrentPage(pageContext.getPageNo());
          schemaRequestModel1.setTeamname(
              manageDatabase.getTeamNameFromTeamId(tenantId, schemaRequestModel1.getTeamId()));
          return schemaRequestModel1;
        });
  }

  private Comparator<SchemaRequestsResponseModel> getPreferredOrder(Order order) {
    return switch (order) {
      case ASC_REQUESTED_TIME -> Comparator.comparing(BaseRequestsResponseModel::getRequesttime);
      case DESC_REQUESTED_TIME -> Collections.reverseOrder(
          Comparator.comparing(BaseRequestsResponseModel::getRequesttime));
    };
  }

  private String updateApproverInfo(
      List<UserInfo> userList, String teamName, Set<String> approverRoles, String requestor) {
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
    updateEnvStatus(response, manageDatabase, tenantId, schemaRequest.getEnvironment());
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
          updateAuditAndHistory(
              userDetails, tenantId, schemaRequest, dbHandle, registerSchemaCustomResponse);
        }

        // send mail to producers and consumers
        notifySubscribers(schemaRequest, tenantId);

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

  private void updateAuditAndHistory(
      String userDetails,
      int tenantId,
      SchemaRequest schemaRequest,
      HandleDbRequests dbHandle,
      Map<String, Object> registerSchemaCustomResponse) {
    saveToTopicHistory(userDetails, tenantId, schemaRequest);
    dbHandle.insertIntoActivityLog(
        RequestEntityType.SCHEMA.value,
        tenantId,
        schemaRequest.getRequestOperationType(),
        schemaRequest.getTeamId(),
        "Topic : "
            + schemaRequest.getTopicname()
            + " version : "
            + registerSchemaCustomResponse.get("version")
            + " id : "
            + registerSchemaCustomResponse.get("id"),
        schemaRequest.getEnvironment(),
        schemaRequest.getRequestor());
  }

  public void notifySubscribers(SchemaRequest schemaRequest, int tenantId) {
    String topic = schemaRequest.getTopicname();
    String schemaRequestEnvironment = schemaRequest.getEnvironment();

    Optional<Env> optSchemaEnv =
        manageDatabase.getEnv(tenantId, Integer.valueOf(schemaRequestEnvironment));
    Optional<Env> optionalKafkaEnv = Optional.empty();
    // add null pointer checks.
    if (optSchemaEnv.isPresent()) {
      optionalKafkaEnv =
          manageDatabase.getEnv(
              tenantId, Integer.valueOf(optSchemaEnv.get().getAssociatedEnv().getId()));
    }

    // get all producer and consumer acls for topic, schemaRequestEnvironment
    List<Acl> acls = new ArrayList<>();
    if (optionalKafkaEnv.isPresent()) {
      acls =
          manageDatabase
              .getHandleDbRequests()
              .getSyncAcls(optionalKafkaEnv.get().getId(), topic, tenantId);
    }

    // get all teams to be notified based on above acls
    List<Integer> teamIdsToBeNotified = acls.stream().map(Acl::getTeamId).toList();
    List<String> teamsToBeNotified =
        manageDatabase.getTeamObjForTenant(tenantId).stream()
            .filter(team -> teamIdsToBeNotified.contains(team.getTeamId()))
            .map(Team::getTeammail)
            .toList();

    Optional<Team> optionalOwnerTeam =
        manageDatabase.getTeamObjForTenant(tenantId).stream()
            .filter(team -> Objects.equals(team.getTeamId(), schemaRequest.getTeamId()))
            .findFirst();

    // send notifications
    if (optionalOwnerTeam.isPresent() && optionalKafkaEnv.isPresent()) {
      mailService.notifySubscribersOnSchemaChange(
          SCHEMA_APPROVED_NOTIFY_SUBSCRIBERS,
          topic,
          optionalKafkaEnv.get().getName(),
          optionalOwnerTeam.get().getTeamname(),
          teamsToBeNotified,
          optionalOwnerTeam.get().getTeammail(),
          tenantId,
          commonUtilsService.getLoginUrl());
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
    // sending request operation type along with function call
    return uploadSchema(schemaRequest, RequestOperationType.PROMOTE);
  }

  private boolean userAndTopicOwnerAreOnTheSameTeam(
      String topicName, Integer userTeamId, Integer tenantId) {
    List<Topic> topicsSearchList = commonUtilsService.getTopicsForTopicName(topicName, tenantId);

    // tenant filtering
    Integer topicOwnerTeam = topicsSearchList.get(0).getTeamId();
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
    schemaRequest.setEnvironment(schemaPromotion.getTargetEnvironment());
    return schemaRequest;
  }

  public ApiResponse uploadSchema(
      SchemaRequestModel schemaRequest, RequestOperationType requestOperationType)
      throws KlawException {
    log.info("uploadSchema {}, requestOperationType {}", schemaRequest, requestOperationType);
    String userName = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.REQUEST_CREATE_SCHEMAS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    schemaRequest.setRequestor(userName);

    int tenantId = commonUtilsService.getTenantId(getUserName());
    Optional<Env> schemaEnv = getSchemaEnvFromKafkaEnvId(schemaRequest.getEnvironment());
    if (schemaEnv.isPresent()) {
      schemaRequest.setEnvironment(schemaEnv.get().getId());
      // Check to see if the schema Registry exists
      Optional<Env> schemaReg =
          manageDatabase.getEnv(
              schemaEnv.get().getTenantId(), Integer.valueOf(schemaEnv.get().getId()));
      if (schemaReg.isEmpty()) {
        return ApiResponse.notOk(SCHEMA_ERR_109);
      }

    } else {
      return ApiResponse.notOk(SCHEMA_ERR_108);
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
    if (schemaReqs != null && schemaRequest.getRequestId() == null) {
      schemaReqs =
          schemaReqs.stream()
              .filter(
                  schemaRequest1 ->
                      RequestStatus.CREATED.value.equals(schemaRequest1.getRequestStatus())
                          && Objects.equals(
                              schemaRequest1.getTopicname(), schemaRequest.getTopicname()))
              .collect(Collectors.toList());
      if (schemaReqs.size() > 0) {
        return ApiResponse.notOk(SCHEMA_ERR_107);
      }
    } else if (schemaReqs != null && schemaRequest.getRequestId() != null) {
      // edit schema request
      Optional<SchemaRequest> optionalSchemaRequest =
          schemaReqs.stream()
              .filter(schemaReq -> schemaReq.getReq_no().equals(schemaRequest.getRequestId()))
              .findFirst();

      if (optionalSchemaRequest.isPresent()) {
        // verify if request is in CREATED state
        if (!optionalSchemaRequest.get().getRequestStatus().equals(RequestStatus.CREATED.value)) {
          return ApiResponse.notOk(SCHEMA_ERR_110);
        }

        // verify if the edit request is being submitted by request owner
        if (!optionalSchemaRequest.get().getRequestor().equals(schemaRequest.getRequestor())) {
          return ApiResponse.notOk(SCHEMA_ERR_111);
        }
      }
    }

    SchemaRequest schemaRequestDao = new SchemaRequest();
    copyProperties(schemaRequest, schemaRequestDao);
    schemaRequestDao.setReq_no(schemaRequest.getRequestId());
    schemaRequestDao.setRequestOperationType(requestOperationType.value);
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

  public SchemaRequestsResponseModel getSchemaRequest(Integer schemaReqId) {
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    SchemaRequest schemaRequest =
        manageDatabase.getHandleDbRequests().getSchemaRequest(schemaReqId, tenantId);

    if (schemaRequest == null) {
      return null;
    } else {
      SchemaRequestsResponseModel schemaRequestsResponseModel = new SchemaRequestsResponseModel();
      copyProperties(schemaRequest, schemaRequestsResponseModel);
      schemaRequestsResponseModel.setRequestStatus(
          RequestStatus.of(schemaRequest.getRequestStatus()));
      schemaRequestsResponseModel.setRequestOperationType(
          RequestOperationType.of(schemaRequest.getRequestOperationType()));
      // Set kafka env name, id
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
              env -> {
                schemaRequestsResponseModel.setEnvironmentName(env.getName());
                schemaRequestsResponseModel.setEnvironment(env.getId());
              });

      schemaRequestsResponseModel.setTeamname(
          manageDatabase.getTeamNameFromTeamId(tenantId, schemaRequest.getTeamId()));

      return schemaRequestsResponseModel;
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
