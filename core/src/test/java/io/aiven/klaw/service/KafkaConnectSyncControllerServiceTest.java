package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.Collections;
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

    test.setId("2");
    test.setName("TST");
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
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
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
    assertThat(result.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }
}
