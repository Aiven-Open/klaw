package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_ERR_108;
import static io.aiven.klaw.service.MailUtils.MailType.ACL_REQUESTED;
import static io.aiven.klaw.service.MailUtils.MailType.ACL_REQUEST_APPROVAL_ADDED;
import static io.aiven.klaw.service.MailUtils.MailType.ACL_REQUEST_APPROVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.ServiceAccounts;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.ServiceAccountDetails;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclControllerServiceTest {

  public static final int TENANT_ID = 101;
  private UtilMethods utilMethods;
  @Mock private UserDetails userDetails;
  @Mock private ClusterApiService clusterApiService;
  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private ManageDatabase manageDatabase;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private RolesPermissionsControllerService rolesPermissionsControllerService;
  @Mock private MailUtils mailService;
  @Mock private UserInfo userInfo;
  @Mock private Pager pager;
  @Mock private ApprovalService approvalService;

  private AclControllerService aclControllerService;

  @Captor private ArgumentCaptor<AclRequests> aclRequestsCapture;

  @Captor private ArgumentCaptor<Team> teamCapture;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.aclControllerService = new AclControllerService(clusterApiService, mailService);

    Env env = new Env();
    env.setName("DEV");
    env.setId("1");
    env.setClusterId(1);
    ReflectionTestUtils.setField(aclControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(aclControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(aclControllerService, "approvalService", approvalService);
    ReflectionTestUtils.setField(
        aclControllerService,
        "rolesPermissionsControllerService",
        rolesPermissionsControllerService);
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    when(commonUtilsService.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    loginMock();
  }

  private void mockKafkaFlavor() {
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    KwClusters kwClusters = new KwClusters();
    kwClusters.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA.value);
    kwClusters.setProjectName("project");
    kwClusters.setServiceName("service");
    kwClusters.setClusterId(1);
    kwClustersMap.put(1, kwClusters);
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
  }

  private void mockKafkaFlavorAiven() {
    Map<Integer, KwClusters> kwClustersMap = new HashMap<>();
    KwClusters kwClusters = new KwClusters();
    kwClusters.setKafkaFlavor(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value);
    kwClusters.setProjectName("project");
    kwClusters.setServiceName("service");
    kwClusters.setClusterId(1);
    kwClustersMap.put(1, kwClusters);
    when(manageDatabase.getClusters(any(), anyInt())).thenReturn(kwClustersMap);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @Order(1)
  public void createAclProducer() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequestProducer();
    copyProperties(aclRequests, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);
    Env env = new Env();
    env.setClusterId(1);
    when(commonUtilsService.getEnvDetails(anyString(), anyInt())).thenReturn(env);
    stubUserInfo();
    mockKafkaFlavor();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequests);
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void createAclConsumer() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequestConsumer();
    copyProperties(aclRequests, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);

    mockKafkaFlavor();
    stubUserInfo();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequests);
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(3)
  public void createAclConsumerThrowError() {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequestConsumer();
    copyProperties(aclRequests, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();
    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.requestForAcl(any()))
        .thenThrow(new RuntimeException("Failure in creating request"));
    stubUserInfo();
    mockKafkaFlavor();

    KlawException thrown =
        Assertions.assertThrows(
            KlawException.class, () -> aclControllerService.createAcl(aclRequests));
    assertThat(thrown.getMessage()).isEqualTo("Failure in creating request");
  }

  @Test
  @Order(4)
  public void createAclNotAuthorized() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequestProducer();
    copyProperties(aclRequests, aclRequestsDao);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    stubUserInfo();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequests);
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(5)
  public void createAclTopicNotFound() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequests = getAclRequestProducer();
    copyProperties(aclRequests, aclRequestsDao);
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(Collections.emptyList());
    stubUserInfo();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequests);
    assertThat(resultResp.getMessage())
        .isEqualTo("Failure : Topic not found on target environment.");
  }

  @Test
  @Order(6)
  public void createAclInvalidPattern() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequestsModel = getAclRequestConsumer();
    aclRequestsModel.setAclPatternType(AclPatternType.PREFIXED.value);
    List<Topic> topicList = utilMethods.getTopics();
    copyProperties(aclRequestsModel, aclRequestsDao);
    when(handleDbRequests.getTopics(anyString(), anyInt())).thenReturn(topicList);
    stubUserInfo();
    mockKafkaFlavor();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequestsModel);
    assertThat(resultResp.getMessage())
        .isEqualTo("Failure : Please change the pattern to LITERAL for topic type.");
  }

  @Test
  @Order(7)
  public void createAclConsumerFailure() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequestsModel = getAclRequestConsumer();
    copyProperties(aclRequestsModel, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();
    Acl acl = new Acl();
    acl.setConsumergroup(aclRequestsModel.getConsumergroup());

    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.validateIfConsumerGroupUsedByAnotherTeam(anyInt(), anyInt(), anyString()))
        .thenReturn(true);
    stubUserInfo();
    mockKafkaFlavor();

    ApiResponse resultResp = aclControllerService.createAcl(aclRequestsModel);
    assertThat(resultResp.getMessage())
        .isEqualTo(
            "Failure : Consumer group "
                + aclRequestsModel.getConsumergroup()
                + " used by another team.");
  }

  @Test
  @Order(8)
  public void createAclProducerEmptyTxnId() throws KlawException {
    AclRequests aclRequestsDao = new AclRequests();
    AclRequestsModel aclRequestsModel = getAclRequestProducer();
    aclRequestsModel.setTransactionalId("    "); // empty spaces
    copyProperties(aclRequestsModel, aclRequestsDao);
    List<Topic> topicList = utilMethods.getTopics();
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);
    stubUserInfo();
    mockKafkaFlavor();

    aclControllerService.createAcl(aclRequestsModel);
    assertThat(aclRequestsModel.getTransactionalId()).isEmpty();
  }

  @Test
  @Order(9)
  public void createAclMultipleAcls() {
    AclRequestsModel aclRequestsModel = getAclRequestProducer();
    AclRequests aclRequestsDao = new AclRequests();

    aclControllerService.handleIpAddressAndCNString(aclRequestsModel, aclRequestsDao);
    assertThat(aclRequestsDao.getAcl_ip())
        .isEqualTo(
            aclRequestsModel.getAcl_ip().get(0) + "<ACL>" + aclRequestsModel.getAcl_ip().get(1));
    assertThat(aclRequestsDao.getAcl_ssl()).isEqualTo("User:*");

    aclRequestsModel.setAclIpPrincipleType(AclIPPrincipleType.PRINCIPAL);
    aclRequestsModel.setAcl_ssl(new ArrayList<>(List.of("CN=abc", "CN=def")));
    aclControllerService.handleIpAddressAndCNString(aclRequestsModel, aclRequestsDao);
    assertThat(aclRequestsDao.getAcl_ssl())
        .isEqualTo(
            aclRequestsModel.getAcl_ssl().get(0) + "<ACL>" + aclRequestsModel.getAcl_ssl().get(1));
    assertThat(aclRequestsDao.getAcl_ip()).isNull();
  }

  @Test
  @Order(10)
  public void getAclRequestsFirstAndSecondPage() {
    String teamName = "teamname";
    List<Topic> topicList = getTopicList();
    List<UserInfo> userList = getUserInfoList();

    stubUserInfo();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getAllAclRequests(
            anyBoolean(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false),
            anyInt()))
        .thenReturn(getAclRequests("testtopic", 15));
    when(rolesPermissionsControllerService.getApproverRoles(anyString(), anyInt()))
        .thenReturn(Collections.singleton("USER"));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn(teamName);
    when(commonUtilsService.getTopicsForTopicName(anyString(), anyInt())).thenReturn(topicList);
    when(handleDbRequests.getAllUsersInfoForTeam(anyInt(), anyInt())).thenReturn(userList);

    List<AclRequestsResponseModel> aclReqs =
        aclControllerService.getAclRequests(
            "1",
            "1",
            "all",
            null,
            null,
            null,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME,
            false);
    assertThat(aclReqs).hasSize(10);
    assertThat(aclReqs.get(0).getAcl_ip()).hasSize(3);
    assertThat(aclReqs.get(0).getTeamname()).isEqualTo(teamName);

    aclReqs =
        aclControllerService.getAclRequests(
            "2",
            "2",
            "all",
            null,
            null,
            null,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME,
            false);
    assertThat(aclReqs).hasSize(5);
    assertThat(aclReqs.get(0).getApprovingTeamDetails()).contains(userList.get(0).getUsername());
    assertThat(aclReqs.get(0).getApprovingTeamDetails()).contains(userList.get(1).getUsername());
  }

  @Test
  @Order(11)
  public void getCreatedAclRequests() {
    String teamName = "teamname";
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedAclRequestsByStatus(
            anyString(),
            anyString(),
            anyBoolean(),
            eq(RequestOperationType.CREATE),
            any(),
            any(),
            any(),
            any(),
            anyInt()))
        .thenReturn(getAclRequests("testtopic", 16));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn(teamName);

    List<AclRequestsResponseModel> listReqs =
        aclControllerService.getAclRequestsForApprover(
            "1",
            "1",
            "",
            null,
            null,
            RequestOperationType.CREATE,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME);
    assertThat(listReqs).hasSize(10);
  }

  @Test
  @Order(12)
  public void getCreatedAclRequestsNotAuthorizedForAllTeams() {
    String teamName = "teamname";
    stubUserInfo();
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedAclRequestsByStatus(
            anyString(),
            anyString(),
            anyBoolean(),
            eq(RequestOperationType.CREATE),
            any(),
            any(),
            any(),
            any(),
            anyInt()))
        .thenReturn(getAclRequests("testtopic", 16));
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn(teamName);

    List<AclRequestsResponseModel> listReqs =
        aclControllerService.getAclRequestsForApprover(
            "1",
            "1",
            "",
            null,
            null,
            RequestOperationType.CREATE,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME);
    assertThat(listReqs).hasSize(10);
  }

  @Test
  @Order(13)
  public void deleteAclRequests() throws KlawException {
    String req_no = "1001";
    when(mailService.getCurrentUserName()).thenReturn("testuser");
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(handleDbRequests.deleteAclRequest(anyInt(), anyString(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse result = aclControllerService.deleteAclRequests(req_no);
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(14)
  public void deleteAclRequestsNotAuthorized() throws KlawException {
    String req_no = "1001";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse result = aclControllerService.deleteAclRequests(req_no);
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(14)
  public void deleteAclRequestsNotRequestOwner() throws KlawException {
    String req_no = "1001";
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse result = aclControllerService.deleteAclRequests(req_no);
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(15)
  public void deleteAclRequestsFailure() {
    String req_no = "1001";
    when(mailService.getCurrentUserName()).thenReturn("testuser");
    when(handleDbRequests.deleteAclRequest(anyInt(), anyString(), anyInt()))
        .thenThrow(new RuntimeException("failure in deleting request"));
    KlawException thrown =
        Assertions.assertThrows(
            KlawException.class, () -> aclControllerService.deleteAclRequests(req_no));
    assertThat(thrown.getMessage()).isEqualTo("failure in deleting request");
  }

  @Test
  @Order(16)
  public void approveAclRequests() throws KlawException, KlawBadRequestException {
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);

    ApiResponse apiResponse = ApiResponse.SUCCESS;
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyMap(), anyBoolean()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    Topic t1 = new Topic();
    t1.setTopicname("testtopic");
    t1.setEnvironment("1");
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(List.of(t1));

    ApiResponse apiResp = aclControllerService.approveAclRequests("112");
    assertThat(apiResp.isSuccess()).isTrue();
  }

  @Test
  @Order(17)
  public void approveAclRequestsWithAivenAcl() throws KlawException, KlawBadRequestException {
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);

    Map<String, String> dataObj = new HashMap<>();
    String aivenAclIdKey = "aivenaclid";
    dataObj.put(aivenAclIdKey, "abcdef"); // any test key

    ApiResponse apiResponse =
        ApiResponse.builder()
            .success(true)
            .message(ApiResultStatus.SUCCESS.value)
            .data(dataObj)
            .build();
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyMap(), anyBoolean()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    Topic t1 = new Topic();
    t1.setTopicname("testtopic");
    t1.setEnvironment("1");
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(List.of(t1));

    ApiResponse apiResp = aclControllerService.approveAclRequests("112");
    assertThat(apiResp.isSuccess()).isTrue();
  }

  @Test
  @Order(18)
  public void approveAclRequestsNotAuthorized() throws KlawException, KlawBadRequestException {
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse apiResp = aclControllerService.approveAclRequests("112");
    assertThat(apiResp.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(19)
  public void approveAclRequestsOwnRequest() throws KlawException, KlawBadRequestException {
    stubUserInfo();
    AclRequests aclReq = getAclRequestDao();
    aclReq.setRequestor("kwusera");
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    ApiResponse apiResp = aclControllerService.approveAclRequests("112");
    assertThat(apiResp.getMessage())
        .isEqualTo("You are not allowed to approve your own subscription requests.");
  }

  @Test
  @Order(20)
  public void approveAclRequestsFailure1() throws KlawException, KlawBadRequestException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();
    stubUserInfo();
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    Topic t1 = new Topic();
    t1.setTopicname("testtopic");
    t1.setEnvironment("1");
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(List.of(t1));

    ApiResponse apiResponse = ApiResponse.notOk("failure");
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getMessage()).isEqualTo("failure");
  }

  @Test
  @Order(21)
  public void approveAclRequestsFailure2() throws KlawException, KlawBadRequestException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    Topic t1 = new Topic();
    t1.setTopicname("testtopic");
    t1.setEnvironment("1");
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(List.of(t1));

    ApiResponse apiResponse = ApiResponse.SUCCESS;
    when(clusterApiService.approveAclRequests(any(), anyInt()))
        .thenReturn(new ResponseEntity<>(apiResponse, HttpStatus.OK));
    when(handleDbRequests.updateAclRequest(any(), any(), anyMap(), anyBoolean()))
        .thenThrow(new RuntimeException("Error"));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getMessage()).isEqualTo("failure");
  }

  @Test
  @Order(22)
  public void approveAclRequestsFailure3() throws KlawException, KlawBadRequestException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();
    aclReq.setRequestStatus(RequestStatus.APPROVED.value);

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getMessage()).isEqualTo("This request does not exist anymore.");
  }

  @Test
  @Order(23)
  public void declineAclRequests() throws KlawException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.declineAclRequest(any(), any()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse resultResp = aclControllerService.declineAclRequests(req_no, "");
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(24)
  public void declineAclRequestsFailure() {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    when(handleDbRequests.declineAclRequest(any(), anyString()))
        .thenThrow(new RuntimeException("failure in declining request"));
    KlawException thrown =
        Assertions.assertThrows(
            KlawException.class, () -> aclControllerService.declineAclRequests(req_no, "Reason"));
    assertThat(thrown.getMessage()).isEqualTo("failure in declining request");
  }

  @Test
  @Order(25)
  public void createDeleteAclSubscriptionRequest() throws KlawException {
    String reqNo = "101";
    stubUserInfo();
    Acl acl = utilMethods.getAllAcls().get(1);

    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getSyncAclsFromReqNo(anyInt(), anyInt())).thenReturn(acl);
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);

    ApiResponse resultResp = aclControllerService.createDeleteAclSubscriptionRequest(reqNo);
    assertThat(resultResp.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(26)
  public void getAivenServiceAccountDetails() throws KlawException {
    String reqNo = "101";
    stubUserInfo();
    mockKafkaFlavor();
    ServiceAccountDetails serviceAccountDetails = new ServiceAccountDetails();
    serviceAccountDetails.setPassword("password");
    serviceAccountDetails.setUsername("username");
    serviceAccountDetails.setAccountFound(true);

    Acl acl = utilMethods.getAllAcls().get(1);

    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getSyncAclsFromReqNo(anyInt(), anyInt())).thenReturn(acl);
    when(clusterApiService.getAivenServiceAccountDetails(
            anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(serviceAccountDetails);

    ServiceAccountDetails resultResp =
        aclControllerService.getAivenServiceAccountDetails("1", "testtopic", "service", reqNo);

    assertThat(resultResp.isAccountFound()).isTrue();
  }

  @Test
  @Order(27)
  public void getAivenServiceAccountDetailsAccountDoesNotExist() throws KlawException {
    String reqNo = "101";
    stubUserInfo();
    mockKafkaFlavor();
    ServiceAccountDetails serviceAccountDetails = new ServiceAccountDetails();
    serviceAccountDetails.setAccountFound(false);

    Acl acl = utilMethods.getAllAcls().get(1);

    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getSyncAclsFromReqNo(anyInt(), anyInt())).thenReturn(acl);
    when(clusterApiService.getAivenServiceAccountDetails(
            anyString(), anyString(), anyString(), anyInt()))
        .thenReturn(serviceAccountDetails);

    ServiceAccountDetails resultResp =
        aclControllerService.getAivenServiceAccountDetails("1", "testtopic", "service", reqNo);

    assertThat(resultResp.isAccountFound()).isFalse();
  }

  @Test
  @Order(28)
  public void getAivenServiceAccounts() throws KlawException {
    stubUserInfo();
    mockKafkaFlavor();
    Set<String> serviceAccountInfoSet = new HashSet<>();
    serviceAccountInfoSet.add("user1");
    serviceAccountInfoSet.add("user2");

    ApiResponse apiResponse =
        ApiResponse.builder()
            .message(ApiResultStatus.SUCCESS.value)
            .data(serviceAccountInfoSet)
            .build();

    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(manageDatabase.getTeamObjForTenant(anyInt())).thenReturn(utilMethods.getTeams());

    when(clusterApiService.getAivenServiceAccounts(anyString(), anyString(), anyInt()))
        .thenReturn(apiResponse);

    Set<String> resultObj = aclControllerService.getAivenServiceAccounts("1");
    assertThat(resultObj).hasSize(2);
  }

  @Test
  @Order(29)
  public void getAivenServiceAccountsDontExist() throws KlawException {
    stubUserInfo();
    mockKafkaFlavor();
    Set<String> serviceAccountInfoSet = new HashSet<>();

    ApiResponse apiResponse =
        ApiResponse.builder()
            .message(ApiResultStatus.SUCCESS.value)
            .data(serviceAccountInfoSet)
            .build();

    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    when(clusterApiService.getAivenServiceAccounts(anyString(), anyString(), anyInt()))
        .thenReturn(apiResponse);

    Set<String> resultObj = aclControllerService.getAivenServiceAccounts("1");
    assertThat(resultObj).isEmpty();
  }

  @Test
  @Order(30)
  public void verifyServiceAccountsOfTeam() {
    AclRequestsModel aclRequestsModel = getAclRequestProducer();
    aclRequestsModel.setRequestingteam(TENANT_ID);
    aclRequestsModel.setAcl_ssl(new ArrayList<>(List.of("user1", "user2")));
    Set<String> serviceAccountInfoSet = new HashSet<>();
    serviceAccountInfoSet.add("user1");

    stubUserInfo();
    mockKafkaFlavorAiven();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(manageDatabase.getTeamObjForTenant(anyInt())).thenReturn(utilMethods.getTeams());
    when(manageDatabase.getAllServiceAccounts(TENANT_ID)).thenReturn(serviceAccountInfoSet);

    boolean serviceAccountsCheck =
        aclControllerService.verifyServiceAccountsOfTeam(aclRequestsModel, TENANT_ID);
    assertThat(serviceAccountsCheck).isFalse();
  }

  @Test
  @Order(31)
  public void verifyServiceAccountsOfTeamOtherTeamOwnsAccount() {
    AclRequestsModel aclRequestsModel = getAclRequestProducer();
    aclRequestsModel.setRequestingteam(TENANT_ID);
    aclRequestsModel.setAcl_ssl(new ArrayList<>(List.of("user1", "user2")));
    Set<String> serviceAccountInfoSet = new HashSet<>();
    serviceAccountInfoSet.add("user1");

    stubUserInfo();
    mockKafkaFlavorAiven();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    List<Team> teamList = utilMethods.getTeams();
    teamList.get(0).setServiceAccounts(null);
    when(manageDatabase.getTeamObjForTenant(anyInt())).thenReturn(teamList);
    when(manageDatabase.getAllServiceAccounts(TENANT_ID)).thenReturn(serviceAccountInfoSet);

    boolean serviceAccountsCheck =
        aclControllerService.verifyServiceAccountsOfTeam(aclRequestsModel, TENANT_ID);
    assertThat(serviceAccountsCheck).isTrue();
  }

  @Test
  @Order(32)
  public void verifyServiceAccountsOfTeamNewAccount() {
    AclRequestsModel aclRequestsModel = getAclRequestProducer();
    aclRequestsModel.setRequestingteam(TENANT_ID);
    aclRequestsModel.setAcl_ssl(new ArrayList<>(List.of("user3", "user4")));
    Set<String> serviceAccountInfoSet = new HashSet<>();
    serviceAccountInfoSet.add("user1");

    stubUserInfo();
    mockKafkaFlavorAiven();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    List<Team> teamList = utilMethods.getTeams();
    when(manageDatabase.getTeamObjForTenant(anyInt())).thenReturn(teamList);
    when(manageDatabase.getAllServiceAccounts(TENANT_ID)).thenReturn(serviceAccountInfoSet);

    boolean serviceAccountsCheck =
        aclControllerService.verifyServiceAccountsOfTeam(aclRequestsModel, TENANT_ID);
    assertThat(serviceAccountsCheck).isFalse();
  }

  @Test
  @Order(33)
  public void verifyServiceAccountsOfTeamNewAccountOtherTeamOwnsAccount() {
    AclRequestsModel aclRequestsModel = getAclRequestProducer();
    aclRequestsModel.setRequestingteam(TENANT_ID);
    aclRequestsModel.setAcl_ssl(new ArrayList<>(List.of("user3")));
    Set<String> serviceAccountInfoSet = new HashSet<>();
    serviceAccountInfoSet.add("user3");

    stubUserInfo();
    mockKafkaFlavorAiven();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    List<Team> teamList = utilMethods.getTeams();
    when(manageDatabase.getTeamObjForTenant(anyInt())).thenReturn(teamList);
    when(manageDatabase.getAllServiceAccounts(TENANT_ID)).thenReturn(serviceAccountInfoSet);

    boolean serviceAccountsCheck =
        aclControllerService.verifyServiceAccountsOfTeam(aclRequestsModel, TENANT_ID);
    assertThat(serviceAccountsCheck).isTrue();
  }

  @Test
  @Order(34)
  public void getAclRequests_OrderBy_NEWEST_FIRST() {
    stubUserInfo();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getAllAclRequests(
            anyBoolean(),
            anyString(),
            anyString(),
            eq(null),
            anyBoolean(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false),
            anyInt()))
        .thenReturn(getAclRequests("", 30));

    List<AclRequestsResponseModel> ordered_response =
        aclControllerService.getAclRequests(
            "1",
            "1",
            null,
            null,
            null,
            null,
            null,
            null,
            io.aiven.klaw.model.enums.Order.DESC_REQUESTED_TIME,
            false);

    assertThat(ordered_response).hasSize(10);
    Timestamp origReqTime = ordered_response.get(0).getRequesttime();

    for (AclRequestsResponseModel req : ordered_response) {

      // assert That each new Request time is older than or equal to the previous request
      assertThat(origReqTime.compareTo(req.getRequesttime())).isGreaterThanOrEqualTo(0);
      origReqTime = req.getRequesttime();
    }
  }

  @Test
  @Order(35)
  public void getAclRequests_OrderBy_OLDEST_FIRST() {
    stubUserInfo();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getAllAclRequests(
            anyBoolean(),
            anyString(),
            anyString(),
            eq(null),
            anyBoolean(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false),
            anyInt()))
        .thenReturn(getAclRequests("", 30));

    List<AclRequestsResponseModel> ordered_response =
        aclControllerService.getAclRequests(
            "1",
            "1",
            null,
            null,
            null,
            null,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME,
            false);

    assertThat(ordered_response).hasSize(10);
    Timestamp origReqTime = ordered_response.get(0).getRequesttime();

    for (AclRequestsResponseModel req : ordered_response) {
      // assert That each new Request time is newer than or equal to the previous request
      assertThat(origReqTime.compareTo(req.getRequesttime())).isLessThanOrEqualTo(0);
      origReqTime = req.getRequesttime();
    }
  }

  @ParameterizedTest
  @CsvSource(value = {"CREATE", "DELETE", "PROMOTE", "UPDATE"})
  @Order(35)
  public void getAclRequestsForApprover_RequestOperationType(RequestOperationType operationType) {
    String teamName = "teamname";
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TENANT_ID);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.getCreatedAclRequestsByStatus(
            anyString(),
            anyString(),
            anyBoolean(),
            eq(operationType),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            anyInt()))
        .thenReturn(getAclRequests("testtopic", 16));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn(teamName);

    List<AclRequestsResponseModel> listReqs =
        aclControllerService.getAclRequestsForApprover(
            "1",
            "1",
            "",
            null,
            null,
            operationType,
            null,
            null,
            io.aiven.klaw.model.enums.Order.ASC_REQUESTED_TIME);
    assertThat(listReqs).hasSize(10);
    verify(handleDbRequests, times(1))
        .getCreatedAclRequestsByStatus(
            anyString(),
            eq(""),
            eq(true),
            eq(operationType),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(TENANT_ID));
  }

  @Test
  @Order(36)
  public void createDeleteAclSubscriptionRequestFailure() throws KlawException {
    String reqNo = "101";
    stubUserInfo();
    Acl acl = utilMethods.getAllAcls().get(1);

    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(TENANT_ID);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getSyncAclsFromReqNo(anyInt(), anyInt())).thenReturn(acl);
    Map<String, String> hashMap = new HashMap<>();
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.requestForAcl(any())).thenReturn(hashMap);

    when(handleDbRequests.getAllAclRequests(
            anyBoolean(),
            anyString(),
            anyString(),
            anyString(),
            anyBoolean(),
            any(),
            anyString(),
            anyString(),
            any(),
            any(),
            anyBoolean(),
            anyInt()))
        .thenReturn(Collections.singletonList(getAclRequestDao()));

    ApiResponse resultResp = aclControllerService.createDeleteAclSubscriptionRequest(reqNo);
    assertThat(resultResp.getMessage()).isEqualTo(ACL_ERR_107);
    assertThat(resultResp.isSuccess()).isFalse();
  }

  @Test
  @Order(37)
  public void approveAclRequestsFailure4() throws KlawException, KlawBadRequestException {
    String req_no = "1001";
    AclRequests aclReq = getAclRequestDao();

    stubUserInfo();
    when(handleDbRequests.getAclRequest(anyInt(), anyInt())).thenReturn(aclReq);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));

    Topic t1 = new Topic();
    t1.setTopicname("testtopic1"); // non-existing topic
    t1.setEnvironment("1");
    when(manageDatabase.getTopicsForTenant(anyInt())).thenReturn(List.of(t1));

    ApiResponse apiResp = aclControllerService.approveAclRequests(req_no);
    assertThat(apiResp.getMessage()).isEqualTo(ACL_ERR_101);
    assertThat(apiResp.isSuccess()).isFalse();
  }

  @Test
  @Order(38)
  public void claimAcl_NotAuthorized() throws KlawException, KlawBadRequestException {
    int aclId = 224;

    stubUserInfo();

    when(commonUtilsService.isNotAuthorizedUser(
            any(), eq(PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)))
        .thenReturn(true);
    ApiResponse apiResp = aclControllerService.claimAcl(aclId);

    assertThat(apiResp.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
    assertThat(apiResp.isSuccess()).isFalse();
  }

  @Order(39)
  @Test
  public void claimAcl_AclDoesNotExist() throws KlawException {
    int aclId = 224;
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(
            any(), eq(PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(handleDbRequests.getAcl(eq(aclId), anyInt())).thenReturn(Optional.empty());
    ApiResponse apiResp = aclControllerService.claimAcl(aclId);

    assertThat(apiResp.getMessage()).isEqualTo("Acl does not exist.");
    assertThat(apiResp.isSuccess()).isFalse();
  }

  @Order(40)
  @Test
  public void claimAcl_TopicDoesNotExistOnACL() throws KlawException {
    int aclId = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(
            any(), eq(PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(handleDbRequests.getAcl(eq(aclId), anyInt())).thenReturn(Optional.of(acl));
    when(handleDbRequests.getTopics(eq(acl.getTopicname()), eq(TENANT_ID)))
        .thenReturn(new ArrayList<>());
    ApiResponse apiResp = aclControllerService.claimAcl(aclId);

    assertThat(apiResp.getMessage()).isEqualTo("Unable to find the topic related to this ACL.");
    assertThat(apiResp.isSuccess()).isFalse();
  }

  @Order(41)
  @Test
  public void claimAcl_claimAlreadyExists() throws KlawException {
    int aclId = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(
            any(), eq(PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(handleDbRequests.getAcl(eq(aclId), anyInt())).thenReturn(Optional.of(acl));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(handleDbRequests.getTopics(eq(acl.getTopicname()), eq(TENANT_ID))).thenReturn(topics);
    when(manageDatabase
            .getHandleDbRequests()
            .existsSpecificAclRequest(
                eq(acl.getTopicname()),
                eq(RequestStatus.CREATED.value),
                eq(acl.getEnvironment()),
                eq(TENANT_ID),
                eq(aclId)))
        .thenReturn(true);
    ApiResponse apiResp = aclControllerService.claimAcl(aclId);

    assertThat(apiResp.getMessage()).isEqualTo(ACL_ERR_108);
    assertThat(apiResp.isSuccess()).isFalse();
  }

  @Order(42)
  @Test
  public void claimAcl_createClaimRequest() throws KlawException {
    int aclId = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(
            any(), eq(PermissionType.REQUEST_CREATE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    when(handleDbRequests.getAcl(eq(aclId), anyInt())).thenReturn(Optional.of(acl));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(handleDbRequests.getTopics(eq(acl.getTopicname()), eq(TENANT_ID))).thenReturn(topics);
    when(manageDatabase
            .getHandleDbRequests()
            .existsSpecificAclRequest(
                eq(acl.getTopicname()),
                eq(RequestStatus.CREATED.value),
                eq(acl.getEnvironment()),
                eq(TENANT_ID),
                eq(aclId)))
        .thenReturn(false);
    when(approvalService.getApprovalsForRequest(
            eq(RequestEntityType.ACL),
            eq(RequestOperationType.CLAIM),
            eq(acl.getEnvironment()),
            eq(topics.get(0).getTeamId()),
            eq(acl.getTeamId()),
            eq(TENANT_ID)))
        .thenReturn(getDefaultAclApprovals("Octopus", "Crab"));
    when(handleDbRequests.requestForAcl(any()))
        .thenReturn(
            new HashMap<>() {
              {
                put("result", ApiResultStatus.SUCCESS.value);
              }
            });
    ApiResponse apiResp = aclControllerService.claimAcl(aclId);
    verify(approvalService, times(1))
        .sendEmailToApprovers(
            any(), eq("testtopic"), eq(""), eq(null), eq(ACL_REQUESTED), any(), eq(TENANT_ID));
    verify(handleDbRequests, times(1)).requestForAcl(aclRequestsCapture.capture());
    AclRequests request = aclRequestsCapture.getValue();
    assertThat(apiResp.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
    assertThat(apiResp.isSuccess()).isTrue();
    assertThat(request.getAcl_ip()).isEqualTo(acl.getAclip());
    assertThat(request.getAcl_ssl()).isEqualTo(acl.getAclssl());
    assertThat(request.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
    assertThat(request.getRequestOperationType()).isEqualTo(RequestOperationType.CLAIM.value);
    assertThat(request.getApprovals()).hasSize(2);
  }

  @Order(43)
  @Test
  public void claimAcl_approveClaim_NotAuthorized() throws KlawException, KlawBadRequestException {
    String reqNum = "224";
    stubUserInfo();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.APPROVE_SUBSCRIPTIONS)))
        .thenReturn(true);

    ApiResponse apiResp = aclControllerService.approveAclRequests(reqNum);
    assertThat(apiResp.isSuccess()).isFalse();
    assertThat(apiResp.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Order(44)
  @Test
  public void claimAcl_approveClaim_transferOwnership()
      throws KlawException, KlawBadRequestException {
    int reqNum = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.APPROVE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    AclRequests aclReq = getAclClaimRequestDao(reqNum);
    when(handleDbRequests.getAclRequest(eq(reqNum), eq(TENANT_ID)))
        .thenReturn(getAclClaimRequestDao(reqNum));
    when(handleDbRequests.getAcl(eq(aclReq.getAssociatedAclId()), eq(TENANT_ID)))
        .thenReturn((Optional.of(createClaimAcl())));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(manageDatabase.getTopicsForTenant(TENANT_ID)).thenReturn(topics);
    when(approvalService.isRequestFullyApproved(any())).thenReturn(true);
    // No acl_ssl service name left owned by the team.
    when(manageDatabase
            .getHandleDbRequests()
            .existsAclSslInTeam(aclReq.getTeamId(), aclReq.getTenantId(), aclReq.getAcl_ssl()))
        .thenReturn(false);
    when(manageDatabase.getTeamObjForTenant(eq(TENANT_ID)))
        .thenReturn(getTeamsListWithServiceAccounts(aclReq));

    ApiResponse apiResp = aclControllerService.approveAclRequests(String.valueOf(reqNum));

    verify(handleDbRequests, times(2)).updateTeam(teamCapture.capture());
    List<Team> teamsCaptured = teamCapture.getAllValues();
    for (Team team : teamsCaptured) {
      if (team.getTeamId().equals(aclReq.getRequestingteam())) {
        // Requesting team gets ownership
        assertThat(team.getServiceAccounts().getServiceAccountsList().contains(aclReq.getAcl_ssl()))
            .isTrue();
      } else if (team.getTeamId().equals(aclReq.getTeamId())) {
        // Previous team only had one ACl so they do not get to keep the service Account in their
        // list
        assertThat(team.getServiceAccounts().getServiceAccountsList()).isEmpty();
      }
    }
    verify(handleDbRequests).claimAclRequest(any(), eq(RequestStatus.APPROVED));
    verify(mailService)
        .sendMail(
            eq(aclReq.getTopicname()),
            eq(aclReq.getAclType()),
            eq(""),
            eq(aclReq.getRequestor()),
            eq(aclReq.getApprover()),
            eq(aclReq.getTeamId()),
            any(),
            eq(ACL_REQUEST_APPROVED),
            any());
  }

  @Order(45)
  @Test
  public void claimAcl_approveClaim_transferOwnership_AddToNewowner_doNotRemoveFromPrevious()
      throws KlawException, KlawBadRequestException {
    int reqNum = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.APPROVE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    AclRequests aclReq = getAclClaimRequestDao(reqNum);
    when(handleDbRequests.getAclRequest(eq(reqNum), eq(TENANT_ID)))
        .thenReturn(getAclClaimRequestDao(reqNum));
    when(handleDbRequests.getAcl(eq(aclReq.getAssociatedAclId()), eq(TENANT_ID)))
        .thenReturn((Optional.of(createClaimAcl())));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(manageDatabase.getTopicsForTenant(TENANT_ID)).thenReturn(topics);
    when(approvalService.isRequestFullyApproved(any())).thenReturn(true);
    // Another acl_ssl (service acc) is owned by the team.
    when(manageDatabase
            .getHandleDbRequests()
            .existsAclSslInTeam(aclReq.getTeamId(), aclReq.getTenantId(), aclReq.getAcl_ssl()))
        .thenReturn(true);
    when(manageDatabase.getTeamObjForTenant(eq(TENANT_ID)))
        .thenReturn(getTeamsListWithServiceAccounts(aclReq));

    ApiResponse apiResp = aclControllerService.approveAclRequests(String.valueOf(reqNum));

    verify(handleDbRequests, times(1)).updateTeam(teamCapture.capture());
    List<Team> teamsCaptured = teamCapture.getAllValues();
    assertThat(teamsCaptured).hasSize(1);
    for (Team team : teamsCaptured) {
      if (team.getTeamId().equals(aclReq.getRequestingteam())) {
        // Requesting team gets ownership added
        assertThat(team.getServiceAccounts().getServiceAccountsList().contains(aclReq.getAcl_ssl()))
            .isTrue();
      }
      // The existing owner team doesnt have it removed as they have another acl that uses that
      // accounr.
    }
    verify(handleDbRequests).claimAclRequest(any(), eq(RequestStatus.APPROVED));
    verify(mailService)
        .sendMail(
            eq(aclReq.getTopicname()),
            eq(aclReq.getAclType()),
            eq(""),
            eq(aclReq.getRequestor()),
            eq(aclReq.getApprover()),
            eq(aclReq.getTeamId()),
            any(),
            eq(ACL_REQUEST_APPROVED),
            any());
  }

  @Order(46)
  @Test
  public void claimAcl_approveClaim_Not_fullyApproved_doNot_transferOwnership()
      throws KlawException, KlawBadRequestException {
    int reqNum = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.APPROVE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    AclRequests aclReq = getAclClaimRequestDao(reqNum);
    when(handleDbRequests.getAclRequest(eq(reqNum), eq(TENANT_ID)))
        .thenReturn(getAclClaimRequestDao(reqNum));
    when(handleDbRequests.getAcl(eq(aclReq.getAssociatedAclId()), eq(TENANT_ID)))
        .thenReturn((Optional.of(createClaimAcl())));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(manageDatabase.getTopicsForTenant(TENANT_ID)).thenReturn(topics);
    when(approvalService.isRequestFullyApproved(any())).thenReturn(false);

    ApiResponse apiResp = aclControllerService.approveAclRequests(String.valueOf(reqNum));

    verify(handleDbRequests, times(0)).updateTeam(any());
    verify(handleDbRequests).claimAclRequest(any(), eq(RequestStatus.CREATED));
    verify(mailService)
        .sendMail(
            eq(aclReq.getTopicname()),
            eq(aclReq.getAclType()),
            eq(""),
            eq(aclReq.getRequestor()),
            eq(aclReq.getApprover()),
            eq(aclReq.getTeamId()),
            any(),
            eq(ACL_REQUEST_APPROVAL_ADDED),
            any());
  }

  @Order(47)
  @Test
  public void
      claimAcl_approveClaim_transferOwnership_AddToNewowner_NullServiceAccountShouldNotThrowError()
          throws KlawException, KlawBadRequestException {
    int reqNum = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.APPROVE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    AclRequests aclReq = getAclClaimRequestDao(reqNum);
    when(handleDbRequests.getAclRequest(eq(reqNum), eq(TENANT_ID)))
        .thenReturn(getAclClaimRequestDao(reqNum));
    when(handleDbRequests.getAcl(eq(aclReq.getAssociatedAclId()), eq(TENANT_ID)))
        .thenReturn((Optional.of(createClaimAcl())));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(manageDatabase.getTopicsForTenant(TENANT_ID)).thenReturn(topics);
    when(approvalService.isRequestFullyApproved(any())).thenReturn(true);
    List<Team> existingTeams = getTeamsListWithServiceAccounts(aclReq);
    for (Team team : existingTeams) {
      if (team.getTeamId().equals(aclReq.getRequestingteam())) {
        team.getServiceAccounts().setServiceAccountsList(null);
      }
    }
    // Another acl_ssl (service acc) is owned by the team.
    when(manageDatabase
            .getHandleDbRequests()
            .existsAclSslInTeam(aclReq.getTeamId(), aclReq.getTenantId(), aclReq.getAcl_ssl()))
        .thenReturn(true);
    when(manageDatabase.getTeamObjForTenant(eq(TENANT_ID))).thenReturn(existingTeams);

    ApiResponse apiResp = aclControllerService.approveAclRequests(String.valueOf(reqNum));

    verify(handleDbRequests, times(1)).updateTeam(teamCapture.capture());
    List<Team> teamsCaptured = teamCapture.getAllValues();
    assertThat(teamsCaptured).hasSize(1);
    for (Team team : teamsCaptured) {
      if (team.getTeamId().equals(aclReq.getRequestingteam())) {
        // Requesting team gets ownership added
        assertThat(team.getServiceAccounts().getServiceAccountsList().contains(aclReq.getAcl_ssl()))
            .isTrue();
      }
      // The existing owner team doesnt have it removed as they have another acl that uses that
      // account.
    }
    verify(handleDbRequests).claimAclRequest(any(), eq(RequestStatus.APPROVED));
    verify(mailService)
        .sendMail(
            eq(aclReq.getTopicname()),
            eq(aclReq.getAclType()),
            eq(""),
            eq(aclReq.getRequestor()),
            eq(aclReq.getApprover()),
            eq(aclReq.getTeamId()),
            any(),
            eq(ACL_REQUEST_APPROVED),
            any());
  }

  @Order(48)
  @Test
  public void
      claimAcl_approveClaim_transferOwnership_AddToNewowner_removeFromPreviousOwner_NullServiceAccountShouldNotThrowError()
          throws KlawException, KlawBadRequestException {
    int reqNum = 224;
    stubUserInfo();
    Acl acl = createAcl();
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.APPROVE_SUBSCRIPTIONS)))
        .thenReturn(false);
    when(commonUtilsService.getTenantId(any())).thenReturn(TENANT_ID);
    AclRequests aclReq = getAclClaimRequestDao(reqNum);
    when(handleDbRequests.getAclRequest(eq(reqNum), eq(TENANT_ID)))
        .thenReturn(getAclClaimRequestDao(reqNum));
    when(handleDbRequests.getAcl(eq(aclReq.getAssociatedAclId()), eq(TENANT_ID)))
        .thenReturn((Optional.of(createClaimAcl())));
    ArrayList<Topic> topics = new ArrayList<>();
    topics.add(createTopic());
    when(manageDatabase.getTopicsForTenant(TENANT_ID)).thenReturn(topics);
    when(approvalService.isRequestFullyApproved(any())).thenReturn(true);
    List<Team> existingTeams = getTeamsListWithServiceAccounts(aclReq);
    for (Team team : existingTeams) {
      // Set all service accounts to null
      team.getServiceAccounts().setServiceAccountsList(null);
    }
    // Another acl_ssl (service acc) is owned by the team.
    when(manageDatabase
            .getHandleDbRequests()
            .existsAclSslInTeam(aclReq.getTeamId(), aclReq.getTenantId(), aclReq.getAcl_ssl()))
        .thenReturn(false);
    when(manageDatabase.getTeamObjForTenant(eq(TENANT_ID))).thenReturn(existingTeams);

    ApiResponse apiResp = aclControllerService.approveAclRequests(String.valueOf(reqNum));

    verify(handleDbRequests, times(2)).updateTeam(teamCapture.capture());
    List<Team> teamsCaptured = teamCapture.getAllValues();
    assertThat(teamsCaptured).hasSize(2);
    for (Team team : teamsCaptured) {
      if (team.getTeamId().equals(aclReq.getRequestingteam())) {
        // Requesting team gets ownership added
        assertThat(team.getServiceAccounts().getServiceAccountsList().contains(aclReq.getAcl_ssl()))
            .isTrue();
      } else if (team.getTeamId().equals(aclReq.getTeamId())) {
        assertThat(team.getServiceAccounts().getServiceAccountsList()).isNullOrEmpty();
      }
      // The existing owner team doesnt have it removed as they have another acl that uses that
      // account.
    }
    verify(handleDbRequests).claimAclRequest(any(), eq(RequestStatus.APPROVED));
    verify(mailService)
        .sendMail(
            eq(aclReq.getTopicname()),
            eq(aclReq.getAclType()),
            eq(""),
            eq(aclReq.getRequestor()),
            eq(aclReq.getApprover()),
            eq(aclReq.getTeamId()),
            any(),
            eq(ACL_REQUEST_APPROVED),
            any());
  }

  // Add the principal name to the original owning team so it can then be removed and added to the
  // other
  private static List<Team> getTeamsListWithServiceAccounts(AclRequests aclReq) {
    List<Team> teams = getTeamsList(aclReq.getTeamId(), aclReq.getRequestingteam());
    for (Team team : teams) {
      if (team.getTeamId().equals(aclReq.getTeamId())) {
        team.getServiceAccounts().getServiceAccountsList().add(aclReq.getAcl_ssl());
      }
    }
    return teams;
  }

  private static Topic createTopic() {
    Topic topic = new Topic();
    topic.setTeamId(1008);
    topic.setTopicname("testtopic");
    topic.setEnvironment("Dev");
    return topic;
  }

  private Acl createAcl() {
    Acl acl = new Acl();
    acl.setTopicname("testtopic");
    acl.setAclType(AclType.PRODUCER.value);
    acl.setEnvironment("1");
    acl.setAclPatternType(AclPatternType.LITERAL.value);
    acl.setAclip(new ArrayList<>(List.of("1.1.1.1", "2.2.2.2")).toString());
    acl.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
    return acl;
  }

  private AclRequestsModel getAclRequestProducer() {
    AclRequestsModel aclReq = new AclRequestsModel();
    aclReq.setTopicname("testtopic");
    aclReq.setAclType(AclType.PRODUCER);
    aclReq.setRequestingteam(1);
    aclReq.setEnvironment("1");
    aclReq.setAclPatternType(AclPatternType.LITERAL.value);
    aclReq.setAcl_ip(new ArrayList<>(List.of("1.1.1.1", "2.2.2.2")));
    aclReq.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
    return aclReq;
  }

  private AclRequestsModel getAclRequestConsumer() {
    AclRequestsModel aclReq = new AclRequestsModel();
    aclReq.setTopicname("testtopic");
    aclReq.setAclType(AclType.CONSUMER);
    aclReq.setRequestingteam(1);
    aclReq.setEnvironment("1");
    aclReq.setAclPatternType(AclPatternType.LITERAL.value);
    aclReq.setConsumergroup("testconsumergroup");
    return aclReq;
  }

  private AclRequests getAclRequestDao() {
    AclRequests aclReq = new AclRequests();
    aclReq.setTopicname("testtopic");
    aclReq.setAclType(AclType.PRODUCER.value);
    aclReq.setRequestingteam(1);
    aclReq.setReq_no(112);
    aclReq.setEnvironment("1");
    aclReq.setRequestor("kwuserb");
    aclReq.setTeamId(1001);
    aclReq.setRequestStatus(RequestStatus.CREATED.value);
    aclReq.setAcl_ip("1.2.3.4");
    aclReq.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
    aclReq.setRequestOperationType(RequestOperationType.CREATE.value);
    return aclReq;
  }

  private AclRequests getAclClaimRequestDao(int reqNum) {
    AclRequests aclReq = new AclRequests();
    aclReq.setTopicname("testtopic");
    aclReq.setAclType(AclType.PRODUCER.value);
    aclReq.setRequestingteam(1);
    aclReq.setReq_no(reqNum);
    aclReq.setEnvironment("1");
    aclReq.setRequestor("kwuserb");
    aclReq.setTeamId(1001);
    aclReq.setRequestStatus(RequestStatus.CREATED.value);
    aclReq.setAclIpPrincipleType(AclIPPrincipleType.PRINCIPAL);
    aclReq.setRequestOperationType(RequestOperationType.CLAIM.value);
    aclReq.setAssociatedAclId(1000001);
    aclReq.setTenantId(TENANT_ID);
    return aclReq;
  }

  private Acl createClaimAcl() {
    Acl acl = new Acl();
    acl.setReq_no(1000001);
    acl.setTopicname("testtopic");
    acl.setAclType(AclType.PRODUCER.value);
    acl.setEnvironment("1");
    acl.setAclPatternType(AclPatternType.LITERAL.value);
    acl.setAclssl("Alice");
    acl.setAclIpPrincipleType(AclIPPrincipleType.PRINCIPAL);
    acl.setTeamId(1003);
    return acl;
  }

  private List<AclRequests> getAclRequests(String topicPrefix, int size) {
    List<AclRequests> listReqs = new ArrayList<>();
    AclRequests aclReq;

    for (int i = 0; i < size; i++) {
      aclReq = new AclRequests();
      aclReq.setEnvironment("1");
      aclReq.setTopicname(topicPrefix + i);
      aclReq.setAclType(AclType.PRODUCER.value);
      aclReq.setRequestingteam(1);
      aclReq.setTeamId(1);
      aclReq.setAcl_ip("1.2.3.4<ACL>3.2.4.5<ACL>11.22.33.44");
      aclReq.setReq_no(100 + i);
      aclReq.setRequesttime(new Timestamp(System.currentTimeMillis() - (3600000 * i)));
      aclReq.setRequestor("testuser");
      listReqs.add(aclReq);
    }
    return listReqs;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(TENANT_ID);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    when(mailService.getCurrentUserName()).thenReturn("kwusera");
  }

  private static List<Topic> getTopicList() {
    Topic topic = new Topic();
    topic.setTeamId(1);
    topic.setEnvironment("1");
    return new ArrayList<>(List.of(topic));
  }

  private static List<UserInfo> getUserInfoList() {
    UserInfo userInfo1 = new UserInfo();
    userInfo1.setUsername("firstuser");
    userInfo1.setTeamId(1);
    userInfo1.setRole("USER");
    UserInfo userInfo2 = new UserInfo();
    userInfo2.setUsername("seconduser");
    userInfo2.setTeamId(1);
    userInfo2.setRole("USER");
    return new ArrayList<>(List.of(userInfo1, userInfo2));
  }

  private List<Approval> getDefaultAclApprovals(
      String topicOwnerTeamName, String aclOwnerTeamName) {
    return List.of(
        createApproval(topicOwnerTeamName, 1, ApprovalType.TOPIC_TEAM_OWNER, null, null, null),
        createApproval(aclOwnerTeamName, 2, ApprovalType.ACL_TEAM_OWNER, null, null, null));
  }

  private Approval createApproval(
      String requiredTeamName,
      Integer approvalId,
      ApprovalType type,
      String userName,
      String approverTeamName,
      Integer approvingTeamId) {
    Approval approval = new Approval();
    approval.setApproverName(userName);
    approval.setApprovalId(approvalId);
    approval.setApprovalType(type);
    approval.setApproverTeamName(approverTeamName);
    approval.setRequiredApprover(requiredTeamName);
    if (approvingTeamId != null) {
      approval.setApproverTeamId(approvingTeamId);
    }

    return approval;
  }

  private static List<Team> getTeamsList(int... teamIds) {
    List<Team> teams = new ArrayList<>();
    for (int teamId : teamIds) {
      Team team = new Team();
      team.setTeamId(teamId);
      team.setTenantId(TENANT_ID);
      ServiceAccounts acc = new ServiceAccounts();
      acc.setServiceAccountsList(new HashSet<>());
      team.setServiceAccounts(acc);
      teams.add(team);
    }
    return teams;
  }
}
