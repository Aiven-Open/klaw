package io.aiven.klaw.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.constants.TestConstants;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EnvControllerServiceTest {
  @Mock private ClusterApiService clusterApiService;
  @Mock private ManageDatabase manageDatabase;
  @Mock private HandleDbRequestsJdbc handleDbRequestsJdbc;

  @InjectMocks private EnvControllerService envControllerService;

  @Test
  void loadTenantActiveStatus() {
    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantId(TestConstants.TENANT_ID);
    kwTenants.setTenantName(TestConstants.TENANT_NAME);
    kwTenants.setLicenseExpiry(new Timestamp(System.currentTimeMillis() - 1000L));

    Mockito.when(manageDatabase.getTenantMap())
        .thenReturn(Map.of(TestConstants.TENANT_ID, TestConstants.TENANT_NAME));
    Mockito.when(manageDatabase.getTenantFullConfig(TestConstants.TENANT_ID)).thenReturn(kwTenants);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);

    envControllerService.loadTenantActiveStatus();

    Mockito.verify(handleDbRequestsJdbc).setTenantActivestatus(TestConstants.TENANT_ID, false);
    Mockito.verify(manageDatabase).loadOneTenant(TestConstants.TENANT_ID);
  }

  @Test
  void loadEnvsWithStatus() {
    Env env = new Env();
    env.setType(KafkaClustersType.ALL.value);
    env.setClusterId(TestConstants.CLUSTER_ID);
    KwClusters kwClusters = new KwClusters();

    Mockito.when(manageDatabase.getTenantMap())
        .thenReturn(Map.of(TestConstants.TENANT_ID, TestConstants.TENANT_NAME));
    Mockito.when(manageDatabase.getKafkaEnvListAllTenants(TestConstants.TENANT_ID))
        .thenReturn(List.of(env));
    Mockito.when(manageDatabase.getSchemaRegEnvList(TestConstants.TENANT_ID))
        .thenReturn(List.of(env));
    Mockito.when(manageDatabase.getKafkaConnectEnvList(TestConstants.TENANT_ID))
        .thenReturn(List.of(env));
    Mockito.when(manageDatabase.getClusters(KafkaClustersType.ALL, TestConstants.TENANT_ID))
        .thenReturn(Map.of(TestConstants.CLUSTER_ID, kwClusters));
    Mockito.when(
            clusterApiService.getKafkaClusterStatus(any(), any(), any(), any(), any(), anyInt()))
        .thenReturn(ClusterStatus.ONLINE);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);

    envControllerService.loadEnvsWithStatus();

    Mockito.verify(handleDbRequestsJdbc, Mockito.times(3)).addNewEnv(env);
    Mockito.verify(manageDatabase, Mockito.times(3))
        .loadEnvMapForOneTenant(TestConstants.TENANT_ID);
    Assertions.assertEquals(env.getEnvStatus(), ClusterStatus.ONLINE);
  }
}
