package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.SchemaPromotion;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.response.SchemaRequestsResponseModel;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class SchemaRegistryControllerServiceTest {

  public static final String TESTTOPIC = "topic-1";
  @Mock private UserDetails userDetails;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock private MailUtils mailService;

  @Mock private ManageDatabase manageDatabase;

  @Mock private UserInfo userInfo;

  @Mock private ClusterApiService clusterApiService;

  @Mock CommonUtilsService commonUtilsService;

  @Mock RolesPermissionsControllerService rolesPermissionsControllerService;

  private SchemaRegistryControllerService schemaRegistryControllerService;

  private ObjectMapper mapper = new ObjectMapper();

  private Env env;

  @Captor private ArgumentCaptor<SchemaRequest> schemaRequestCaptor;

  @BeforeEach
  public void setUp() throws Exception {
    this.env = new Env();
    env.setId("1");
    env.setName("DEV");

    schemaRegistryControllerService =
        new SchemaRegistryControllerService(clusterApiService, mailService);
    ReflectionTestUtils.setField(schemaRegistryControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(schemaRegistryControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        schemaRegistryControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        schemaRegistryControllerService,
        "rolesPermissionsControllerService",
        rolesPermissionsControllerService);

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
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
  public void getSchemaRequests() {
    stubUserInfo();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(handleDbRequests.getAllSchemaRequests(
            anyBoolean(),
            anyString(),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq("all"),
            eq(null),
            eq(false)))
        .thenReturn(getSchemasReqs());
    when(rolesPermissionsControllerService.getApproverRoles(anyString(), anyInt()))
        .thenReturn(List.of(""));
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.selectAllUsersInfoForTeam(anyInt(), anyInt()))
        .thenReturn(List.of(userInfo));
    when(handleDbRequests.selectEnvDetails(anyString(), anyInt())).thenReturn(this.env);
    when(commonUtilsService.deriveCurrentPage(anyString(), anyString(), anyInt())).thenReturn("1");
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("teamname");

    List<SchemaRequestsResponseModel> listReqs =
        schemaRegistryControllerService.getSchemaRequests(
            "1", "", "all", null, true, null, null, null, false);
    assertThat(listReqs).hasSize(2);
  }

  @Test
  @Order(2)
  public void deleteSchemaRequestsSuccess() throws KlawException {
    int schemaReqId = 1001;

    stubUserInfo();
    when(handleDbRequests.deleteSchemaRequest(anyInt(), anyString(), anyInt()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse resultResp = schemaRegistryControllerService.deleteSchemaRequests("" + schemaReqId);
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(3)
  public void deleteSchemaRequestsFailure() {
    int schemaReqId = 1001;

    stubUserInfo();
    when(handleDbRequests.deleteSchemaRequest(anyInt(), anyString(), anyInt()))
        .thenThrow(new RuntimeException("Error from Schema upload"));
    try {
      schemaRegistryControllerService.deleteSchemaRequests("" + schemaReqId);
    } catch (KlawException e) {
      assertThat(e.getMessage()).contains("Error from Schema upload");
    }
  }

  @Test
  @Order(4)
  public void execSchemaRequestsSuccess() throws KlawException {
    int schemaReqId = 1001;

    ApiResponse apiResponse = ApiResponse.builder().result("Schema registered id\": 215").build();

    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("schema..");
    schemaRequest.setRequestor("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");

    stubUserInfo();
    when(handleDbRequests.selectSchemaRequest(anyInt(), anyInt())).thenReturn(schemaRequest);
    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(response);
    when(handleDbRequests.updateSchemaRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse resultResp = schemaRegistryControllerService.execSchemaRequests("" + schemaReqId);
    assertThat(resultResp.getResult()).contains(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(5)
  public void execSchemaRequestsFailure1() throws KlawException {
    int schemaReqId = 1001;

    ApiResponse apiResponse = ApiResponse.builder().result("Schema not registered").build();
    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("schema..");
    schemaRequest.setRequestor("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");

    stubUserInfo();
    when(handleDbRequests.selectSchemaRequest(anyInt(), anyInt())).thenReturn(schemaRequest);
    when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
        .thenReturn(response);
    when(handleDbRequests.updateSchemaRequest(any(), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    ApiResponse resultResp = schemaRegistryControllerService.execSchemaRequests("" + schemaReqId);
    assertThat(resultResp.getResult()).contains("Schema not registered");
  }

  @Test()
  @Order(6)
  public void execSchemaRequestsFailure2() {
    int schemaReqId = 1001;

    ApiResponse apiResponse = ApiResponse.builder().result("Schema registered id\": 215").build();
    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);

    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setSchemafull("schema..");
    schemaRequest.setRequestor("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");

    stubUserInfo();
    when(handleDbRequests.selectSchemaRequest(anyInt(), anyInt())).thenReturn(schemaRequest);
    try {
      when(clusterApiService.postSchema(any(), anyString(), anyString(), anyInt()))
          .thenReturn(response);
    } catch (KlawException e) {
      throw new RuntimeException(e);
    }
    when(handleDbRequests.updateSchemaRequest(any(), anyString()))
        .thenThrow(new RuntimeException("Error in registering"));
    when(manageDatabase.getTeamsAndAllowedEnvs(anyInt(), anyInt()))
        .thenReturn(Collections.singletonList("1"));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);

    try {
      schemaRegistryControllerService.execSchemaRequests("" + schemaReqId);
    } catch (KlawException e) {
      assertThat(e.getMessage()).contains("Error in registering");
    }
  }

  @Test
  @Order(7)
  public void uploadSchemaSuccess() throws KlawException {
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    schemaRequest.setSchemafull("{}");
    schemaRequest.setRequestor("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");
    Topic topic = createTopic();

    stubUserInfo();
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.requestForSchema(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(List.of(topic));
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(List.of(topic));

    ApiResponse resultResp = schemaRegistryControllerService.uploadSchema(schemaRequest);
    assertThat(resultResp.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(8)
  public void uploadSchemaFailure() {
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    schemaRequest.setSchemafull("{}");
    schemaRequest.setRequestor("kwuserb");
    schemaRequest.setEnvironment("1");
    schemaRequest.setTopicname("topic");
    Topic topic = createTopic();

    stubUserInfo();
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.requestForSchema(any()))
        .thenThrow(new RuntimeException("Error from schema upload"));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(List.of(topic));
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(List.of(topic));

    try {
      schemaRegistryControllerService.uploadSchema(schemaRequest);
    } catch (KlawException e) {
      assertThat(e.getMessage()).contains("Error from schema upload");
    }
  }

  @Test
  @Order(9)
  public void promoteSchemaNotAuthorized() throws Exception {
    // Make user unauthorized
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.REQUEST_CREATE_SCHEMAS)))
        .thenReturn(true);
    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "1"));
    assertThat(returnedValue.getResult()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(10)
  public void promoteSchemaCanNotFindSourceEnvironmentSchema() throws Exception {
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(List.of(createTopic()));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(List.of(createTopic()));
    when(commonUtilsService.getTeamId(any())).thenReturn(101);
    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "1"));
    assertThat(returnedValue.getResult())
        .isEqualTo("Unable to find or access the source Schema Registry");
  }

  @Test
  @Order(12)
  public void promoteSchemaSuccess() throws Exception {
    mockGetEnvironment();
    mockSchema();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    mockSchemaCreation();

    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "1"));
    assertThat(returnedValue.getResult())
        .isNotEqualTo("Unable to find or access the source Schema Registry");
    assertThat(returnedValue.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(13)
  public void promoteSchemaEnsureCorrectSchemaSelectedV1Success() throws Exception {
    mockGetEnvironment();
    mockSchema();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    mockSchemaCreation();

    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "1"));
    assertThat(returnedValue.getResult())
        .isNotEqualTo("Unable to find or access the source Schema Registry");
    assertThat(returnedValue.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(1)).requestForSchema(schemaRequestCaptor.capture());
    assertThat(schemaRequestCaptor.getValue().getSchemaversion()).isEqualTo("1");
    JsonNode json = mapper.readTree(schemaRequestCaptor.getValue().getSchemafull());
    assertThat(json.get("name").asText()).isEqualTo("klawTestAvroV1");
  }

  @Test
  @Order(14)
  public void promoteSchemaEnsureCorrectSchemaSelectedV2Success() throws Exception {
    mockGetEnvironment();
    mockSchema();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    mockSchemaCreation();

    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "2"));
    assertThat(returnedValue.getResult())
        .isNotEqualTo("Unable to find or access the source Schema Registry");
    assertThat(returnedValue.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(1)).requestForSchema(schemaRequestCaptor.capture());
    assertThat(schemaRequestCaptor.getValue().getSchemaversion()).isEqualTo("2");
    JsonNode json = mapper.readTree(schemaRequestCaptor.getValue().getSchemafull());
    assertThat(json.get("name").asText()).isEqualTo("klawTestAvroV2");
  }

  @Test
  @Order(15)
  public void promoteSchemaEnsureCorrectSchemaSelectedV3Success() throws Exception {
    mockGetEnvironment();
    mockSchema();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    mockSchemaCreation();

    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "3"));
    assertThat(returnedValue.getResult())
        .isNotEqualTo("Unable to find or access the source Schema Registry");
    assertThat(returnedValue.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);

    verify(handleDbRequests, times(1)).requestForSchema(schemaRequestCaptor.capture());
    assertThat(schemaRequestCaptor.getValue().getSchemaversion()).isEqualTo("3");
    JsonNode json = mapper.readTree(schemaRequestCaptor.getValue().getSchemafull());
    assertThat(json.get("name").asText()).isEqualTo("klawTestAvroV3");
  }

  @Test
  @Order(16)
  public void promoteSchemaEnsureCorrectSchemaSelectedV4Success() throws Exception {
    mockGetEnvironment();
    mockSchema();
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    mockSchemaCreation();

    ApiResponse returnedValue =
        schemaRegistryControllerService.promoteSchema(buildPromoteSchemaRequest(false, "4"));
    assertThat(returnedValue.getResult())
        .isNotEqualTo("Unable to find or access the source Schema Registry");
    assertThat(returnedValue.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(handleDbRequests, times(1)).requestForSchema(schemaRequestCaptor.capture());
    assertThat(schemaRequestCaptor.getValue().getSchemaversion()).isEqualTo("4");
    JsonNode json = mapper.readTree(schemaRequestCaptor.getValue().getSchemafull());
    assertThat(json.get("name").asText()).isEqualTo("klawTestAvroV4");
  }

  private void mockSchemaCreation() {
    Topic topic = createTopic();

    stubUserInfo();
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt())).thenReturn(List.of(topic));
    when(commonUtilsService.getFilteredTopicsForTenant(any())).thenReturn(List.of(topic));

    when(handleDbRequests.requestForSchema(any())).thenReturn(ApiResultStatus.SUCCESS.value);
  }

  private static Topic createTopic() {
    Topic topic = new Topic();
    topic.setEnvironment("1");
    topic.setTeamId(101);
    return topic;
  }

  private void mockGetEnvironment() {

    when(manageDatabase.getSchemaRegEnvList(anyInt())).thenReturn(createEnvList(8));
  }

  private void mockSchema() throws Exception {
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(manageDatabase.getClusters(KafkaClustersType.SCHEMA_REGISTRY, 101))
        .thenReturn(createClusterMap(8));
    when(clusterApiService.getAvroSchema(any(), any(), any(), eq(TESTTOPIC), eq(101)))
        .thenReturn(createSchemaList())
        .thenReturn(null);
  }

  private TreeMap<Integer, Map<String, Object>> createSchemaList() throws JsonProcessingException {
    String schemav1 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"1\", \"id\":\"2\", \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvroV1\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";
    String schemav2 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"2\", \"id\":\"3\", \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvroV2\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";

    String schemav3 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"3\", \"id\":\"3\", \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvroV3\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";
    String schemav4 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"4\", \"id\":\"2\", \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvroV4\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";

    TreeMap<Integer, Map<String, Object>> allVersionSchemas =
        new TreeMap<>(Collections.reverseOrder());
    allVersionSchemas.put(1, mapper.readValue(schemav1, Map.class));
    allVersionSchemas.put(2, mapper.readValue(schemav2, Map.class));
    allVersionSchemas.put(3, mapper.readValue(schemav3, Map.class));
    allVersionSchemas.put(4, mapper.readValue(schemav4, Map.class));

    return allVersionSchemas;
  }

  private Map<Integer, KwClusters> createClusterMap(int numberOfClusters) {
    Map<Integer, KwClusters> map = new HashMap<>();

    for (int i = 0; i < numberOfClusters; i++) {
      map.put(i + 1, createCluster(KafkaClustersType.SCHEMA_REGISTRY));
    }
    return map;
  }

  private KwClusters createCluster(KafkaClustersType clusterType) {
    KwClusters cluster = new KwClusters();
    cluster.setClusterId(1);
    cluster.setClusterType(clusterType.value);
    cluster.setBootstrapServers("server:8081");
    cluster.setTenantId(101);
    return cluster;
  }

  private static List<Env> createEnvList(int number) {
    List<Env> envs = new ArrayList<>();
    for (int i = 1; i <= number; i++) {
      Env e = new Env();
      e.setId(String.valueOf(i));
      e.setName("Dev-" + i);
      e.setClusterId(i);
      e.setTenantId(101);
      e.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
      envs.add(e);
    }
    return envs;
  }

  private SchemaPromotion buildPromoteSchemaRequest(boolean isForceRegister, String SchemaVersion) {
    SchemaPromotion schema = new SchemaPromotion();
    schema.setAppName("App");
    schema.setSchemaVersion(SchemaVersion);
    schema.setRemarks("Promote Schema.");
    schema.setForceRegister(isForceRegister);
    schema.setSchemaFull("{'name':'tester'}");
    schema.setSourceEnvironment("1");
    schema.setTargetEnvironment("9");
    schema.setTopicName(TESTTOPIC);
    return schema;
  }

  private List<SchemaRequest> getSchemasReqs() {
    List<SchemaRequest> schList = new ArrayList<>();
    SchemaRequest schReq = new SchemaRequest();
    schReq.setSchemafull("<Schema>");
    schReq.setEnvironment("1");
    schReq.setRequestStatus(RequestStatus.CREATED.value);
    schReq.setTeamId(101);
    schReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
    schList.add(schReq);

    schReq = new SchemaRequest();
    schReq.setSchemafull("<Schema1>");
    schReq.setEnvironment("1");
    schReq.setRequestStatus(RequestStatus.CREATED.value);
    schReq.setTeamId(102);
    schReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
    schList.add(schReq);

    return schList;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(userInfo.getRole()).thenReturn("USER");
    when(mailService.getUserName(any())).thenReturn("kwusera");
  }
}
