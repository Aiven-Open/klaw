package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.cluster.ConnectorState;
import io.aiven.klaw.model.cluster.ConnectorsStatus;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.KafkaConnectorModelResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaConnectSyncControllerServiceTest {
  public static final int TENANT_ID = 1;
  public static final String USERNAME = "kwusera";
  public static final String REMOVE_FROM_KLAW = "REMOVE FROM KLAW";
  public static final String IN_SYNC = "IN_SYNC";
  public static final String DELETED = "DELETED";
  public static final String ADDED = "ADDED";
  public static final String TEAM_NAME_1 = "Octopus";
  @Mock private ClusterApiService clusterApiService;

  @Mock private UserDetails userDetails;

  @Mock private UserInfo userInfo;

  @Mock private Map<Integer, KwClusters> clustersHashMap;

  @Mock private KwClusters kwClusters;

  @Mock private ManageDatabase manageDatabase;

  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock CommonUtilsService commonUtilsService;

  @Mock private MailUtils mailService;
  private KafkaConnectSyncControllerService kafkaConnectSyncControllerService;

  @Mock Map<Integer, KwTenantConfigModel> tenantConfig;

  @Mock KwTenantConfigModel tenantConfigModel;

  private UtilMethods utilMethods;
  private Env env = new Env();
  private Env test = new Env();

  @BeforeEach
  public void setUp() throws Exception {
    this.kafkaConnectSyncControllerService = new KafkaConnectSyncControllerService();
    utilMethods = new UtilMethods();
    environmentSetUp();

    ReflectionTestUtils.setField(
        kafkaConnectSyncControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(kafkaConnectSyncControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        kafkaConnectSyncControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        kafkaConnectSyncControllerService, "clusterApiService", clusterApiService);

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  private void environmentSetUp() {
    env.setId("1");
    env.setName("DEV");
    env.setClusterId(1);

    test.setId("2");
    test.setName("TST");
    test.setClusterId(2);
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(List.of(env, test));
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn(USERNAME);
    when(commonUtilsService.getTenantId(eq(USERNAME))).thenReturn(TENANT_ID);
  }

  @Test
  @Order(1)
  public void updateSyncConnectorsSuccess() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getTenantConfig()).thenReturn(tenantConfig);
    when(tenantConfig.get(anyInt())).thenReturn(tenantConfigModel);
    when(tenantConfigModel.getBaseSyncEnvironment()).thenReturn("1");
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getKafkaConnectEnvList(anyInt())).thenReturn(List.of(env));
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA_CONNECT), anyInt()))
        .thenReturn(clustersHashMap);
    when(clustersHashMap.get(any())).thenReturn(kwClusters);
    when(handleDbRequests.addToSyncConnectors(any())).thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse result =
        kafkaConnectSyncControllerService.updateSyncConnectors(
            utilMethods.getSyncConnectorUpdates());
    assertThat(result.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(2)
  public void getSync_WithDeleted_Connectors() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getKafkaConnectEnvList(anyInt())).thenReturn(List.of(env, test));
    when(manageDatabase.getTeamNameFromTeamId(eq(1), eq(10))).thenReturn(TEAM_NAME_1);
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA_CONNECT), eq(1)))
        .thenReturn(getConnectlusters(2));
    when(manageDatabase.getHandleDbRequests().getSyncConnectors(eq("2"), eq(null), eq(1)))
        .thenReturn(getConnectorsFromDb(5));
    when(clusterApiService.getAllKafkaConnectors(
            anyString(),
            eq(KafkaSupportedProtocol.PLAINTEXT.getValue()),
            anyString(),
            eq(1),
            anyBoolean()))
        .thenReturn(getConnectorStatus(3));
    List<KafkaConnectorModelResponse> result =
        kafkaConnectSyncControllerService.getSyncConnectors("2", "1", "1", null, false);

    assertThat(result).hasSize(5);
    int added = 0, deleted = 0, synced = 0;
    for (KafkaConnectorModelResponse entry : result) {

      if (entry.getRemarks().equalsIgnoreCase(ADDED)) {
        added++;
      } else if (entry.getRemarks().equalsIgnoreCase(DELETED)) {
        deleted++;
        assertThat(entry.getPossibleTeams()).contains(REMOVE_FROM_KLAW);
      } else if (entry.getRemarks().equalsIgnoreCase(IN_SYNC)) {
        synced++;
      }
    }
    assertThat(added).isEqualTo(0);
    assertThat(deleted).isEqualTo(2);
    assertThat(synced).isEqualTo(3);
  }

  @Test
  @Order(2)
  public void getSync_WithAddedOnly_Connectors() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getKafkaConnectEnvList(anyInt())).thenReturn(List.of(env, test));
    when(manageDatabase.getTeamNameFromTeamId(eq(1), eq(10))).thenReturn(TEAM_NAME_1);
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA_CONNECT), eq(1)))
        .thenReturn(getConnectlusters(2));
    when(manageDatabase.getHandleDbRequests().getSyncConnectors(eq("2"), eq(null), eq(1)))
        .thenReturn(getConnectorsFromDb(0));
    when(clusterApiService.getAllKafkaConnectors(
            anyString(),
            eq(KafkaSupportedProtocol.PLAINTEXT.getValue()),
            anyString(),
            eq(1),
            anyBoolean()))
        .thenReturn(getConnectorStatus(5));
    List<KafkaConnectorModelResponse> result =
        kafkaConnectSyncControllerService.getSyncConnectors("2", "1", "1", null, false);

    assertThat(result).hasSize(5);
    int added = 0, deleted = 0, synced = 0;
    for (KafkaConnectorModelResponse entry : result) {

      if (entry.getRemarks().equalsIgnoreCase(ADDED)) {
        added++;
      } else if (entry.getRemarks().equalsIgnoreCase(DELETED)) {
        deleted++;
        assertThat(entry.getPossibleTeams()).contains(REMOVE_FROM_KLAW);
      } else if (entry.getRemarks().equalsIgnoreCase(IN_SYNC)) {
        synced++;
      }
    }
    assertThat(added).isEqualTo(5);
    assertThat(deleted).isEqualTo(0);
    assertThat(synced).isEqualTo(0);
  }

  @Test
  @Order(2)
  public void getSync_WithDeletedOnly_Connectors() throws KlawException {
    stubUserInfo();
    when(manageDatabase.getKafkaConnectEnvList(anyInt())).thenReturn(List.of(env, test));
    when(manageDatabase.getTeamNameFromTeamId(eq(1), eq(10))).thenReturn(TEAM_NAME_1);
    when(manageDatabase.getClusters(eq(KafkaClustersType.KAFKA_CONNECT), eq(1)))
        .thenReturn(getConnectlusters(2));
    when(manageDatabase.getHandleDbRequests().getSyncConnectors(eq("2"), eq(null), eq(1)))
        .thenReturn(getConnectorsFromDb(5));
    when(clusterApiService.getAllKafkaConnectors(
            anyString(),
            eq(KafkaSupportedProtocol.PLAINTEXT.getValue()),
            anyString(),
            eq(1),
            anyBoolean()))
        .thenReturn(getConnectorStatus(0));
    List<KafkaConnectorModelResponse> result =
        kafkaConnectSyncControllerService.getSyncConnectors("2", "1", "1", null, false);

    assertThat(result).hasSize(5);
    int added = 0, deleted = 0, synced = 0;
    for (KafkaConnectorModelResponse entry : result) {

      if (entry.getRemarks().equalsIgnoreCase(ADDED)) {
        added++;
      } else if (entry.getRemarks().equalsIgnoreCase(DELETED)) {
        deleted++;
        assertThat(entry.getPossibleTeams()).contains(REMOVE_FROM_KLAW);
      } else if (entry.getRemarks().equalsIgnoreCase(IN_SYNC)) {
        synced++;
      }
    }
    assertThat(added).isEqualTo(0);
    assertThat(deleted).isEqualTo(5);
    assertThat(synced).isEqualTo(0);
  }

  private List<KwKafkaConnector> getConnectorsFromDb(int number) {
    List<KwKafkaConnector> list = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      KwKafkaConnector conn = new KwKafkaConnector();
      conn.setConnectorId(i);
      conn.setConnectorName("Connector" + i);
      conn.setEnvironment("1");
      conn.setTenantId(1);
      conn.setTeamId(10);
      list.add(conn);
    }
    return list;
  }

  private ConnectorsStatus getConnectorStatus(int number) {
    ConnectorsStatus status = new ConnectorsStatus();
    status.setConnectorStateList(getConnectorStateList(number));
    return status;
  }

  private List<ConnectorState> getConnectorStateList(int number) {
    List<ConnectorState> list = new ArrayList<>();

    for (int i = 0; i < number; i++) {

      ConnectorState state = new ConnectorState();
      state.setConnectorName("Connector" + i);
      state.setFailedTasks(0);
      state.setRunningTasks(1);
      state.setConnectorStatus("RUNNING");
      list.add(state);
    }
    return list;
  }

  private Map<Integer, KwClusters> getConnectlusters(int number) {
    Map<Integer, KwClusters> retval = new HashMap<>();
    for (int i = 1; i <= number; i++) {
      KwClusters clusters = new KwClusters();
      clusters.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
      clusters.setClusterId(i);
      clusters.setClusterType(KafkaClustersType.KAFKA_CONNECT.value);
      clusters.setClusterName(i + "");
      clusters.setBootstrapServers("localhost:9092");
      retval.put(i, clusters);
    }

    return retval;
  }
}
