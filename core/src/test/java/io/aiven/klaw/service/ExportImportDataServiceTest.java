package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.metadata.KwAdminConfig;
import io.aiven.klaw.dao.metadata.KwData;
import io.aiven.klaw.dao.metadata.KwRequests;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExportImportDataServiceTest {

  @Mock private ManageDatabase manageDatabase;

  private UtilMethods utilMethods;

  @Mock private HandleDbRequestsJdbc handleDbRequests;
  private ExportImportDataService exportImportDataService;

  @BeforeEach
  public void setUp() throws Exception {
    exportImportDataService = new ExportImportDataService();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(exportImportDataService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(exportImportDataService, "encryptorSecretKey", "testkey");
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
  }

  @Test
  public void getAdminConfig() {
    when(handleDbRequests.getAllUsersAllTenants()).thenReturn(utilMethods.getUserInfoList("", ""));
    when(handleDbRequests.getClusters())
        .thenReturn(Collections.singletonList(utilMethods.getKwClusters()));
    KwAdminConfig kwAdminConfig = exportImportDataService.getAdminConfig(handleDbRequests, "");
    assertThat(kwAdminConfig.getUsers().size()).isEqualTo(1);
    assertThat(kwAdminConfig.getClusters().size()).isEqualTo(1);
  }

  @Test
  public void getKwData() {
    when(handleDbRequests.getAllTopics())
        .thenReturn(utilMethods.getMultipleTopics("test", 10, null, 101));
    when(handleDbRequests.getAllConnectors())
        .thenReturn(Collections.singletonList(new KwKafkaConnector()));
    KwData kwData = exportImportDataService.getKwData(handleDbRequests, "");
    assertThat(kwData.getTopics().size()).isEqualTo(10);
    assertThat(kwData.getKafkaConnectors().size()).isEqualTo(1);
  }

  @Test
  public void getRequestsData() {
    when(handleDbRequests.getAllTopicRequests()).thenReturn(utilMethods.getTopicRequests());
    when(handleDbRequests.getAllAclRequests()).thenReturn(utilMethods.getAclRequests());
    KwRequests kwRequests = exportImportDataService.getRequestsData(handleDbRequests, "");
    assertThat(kwRequests.getTopicRequests().size()).isEqualTo(1);
    assertThat(kwRequests.getSubscriptionRequests().size()).isEqualTo(1);
  }
}
