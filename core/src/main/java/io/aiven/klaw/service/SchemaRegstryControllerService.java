package io.aiven.klaw.service;

import static io.aiven.klaw.model.enums.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaRequestModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.enums.TopicRequestTypes;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
      String pageNo, String currentPage, String requestsType) {
    log.debug("getSchemaRequests page {} requestsType {}", pageNo, requestsType);

    int tenantId = commonUtilsService.getTenantId(getUserName());
    String userDetails = getUserName();
    List<SchemaRequest> schemaReqs =
        manageDatabase.getHandleDbRequests().getAllSchemaRequests(false, userDetails, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(getUserName());
    if (schemaReqs != null) {
      schemaReqs =
          schemaReqs.stream()
              .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
              .collect(Collectors.toList());
    }

    // request status filtering
    if (!"all".equals(requestsType) && EnumUtils.isValidEnum(RequestStatus.class, requestsType)) {
      if (schemaReqs != null) {
        schemaReqs =
            schemaReqs.stream()
                .filter(
                    schemaRequest -> Objects.equals(schemaRequest.getTopicstatus(), requestsType))
                .collect(Collectors.toList());
      }
    }

    Integer userTeamId = getMyTeamId(userDetails);
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
        if (!RequestStatus.approved.name().equals(schemaRequestModel.getTopicstatus())) {
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

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
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

    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(getUserName());

    if (!allowedEnvIdList.contains(schemaRequest.getEnvironment())) {
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
    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(getUserName());

    if (!allowedEnvIdList.contains(schemaRequest.getEnvironment())) {
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

    Integer userTeamId = getMyTeamId(userDetails);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<Topic> topicsSearchList =
        manageDatabase.getHandleDbRequests().getTopicTeam(schemaRequest.getTopicname(), tenantId);

    // tenant filtering
    Integer topicOwnerTeam =
        commonUtilsService.getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();

    if (!Objects.equals(userTeamId, topicOwnerTeam)) {
      return ApiResponse.builder()
          .result("No topic selected Or Not authorized to register schema for this topic.")
          .build();
    }

    List<SchemaRequest> schemaReqs =
        manageDatabase.getHandleDbRequests().getAllSchemaRequests(false, userDetails, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(getUserName());
    if (schemaReqs != null)
      schemaReqs =
          schemaReqs.stream()
              .filter(request -> allowedEnvIdList.contains(request.getEnvironment()))
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
    schemaRequestDao.setRequesttype(TopicRequestTypes.Create.name());
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

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
