package io.aiven.klaw.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.service.HighAvailabilityUtilsService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class ManageDatabaseTest {

  public static final int TENANT_ID = 101;
  private ManageDatabase manageDatabase = new ManageDatabase();
  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @Mock HighAvailabilityUtilsService highAvailabilityUtilsService;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(manageDatabase, "handleDbRequests", handleDbRequests);
    ReflectionTestUtils.setField(
        manageDatabase, "highAvailabilityUtilsService", highAvailabilityUtilsService);
    when(handleDbRequests.getAllTopics(eq(TENANT_ID))).thenReturn(new ArrayList<>());
    manageDatabase.loadTopicsForOneTenant(TENANT_ID);
  }

  @Test
  public void addTopicToCache() {
    Topic t = new Topic();
    int topicId = 1;
    t.setTopicid(topicId);
    t.setTopicname("FirstTopic");
    t.setTenantId(TENANT_ID);

    assertThat(getTopicFromCache(TENANT_ID, topicId)).isEmpty();
    manageDatabase.addTopicToCache(TENANT_ID, t);
    assertThat(getTopicFromCache(TENANT_ID, topicId)).isNotEmpty();
  }

  @Test
  public void addTopicToCacheThatAlreadyExistsOnlyOneEntry() {
    Topic t = new Topic();
    int topicId = 1;
    t.setTopicid(topicId);
    t.setTopicname("FirstTopic");
    t.setTenantId(TENANT_ID);

    assertThat(getTopicFromCache(TENANT_ID, topicId)).isEmpty();
    manageDatabase.addTopicToCache(TENANT_ID, t);
    assertThat(getTopicFromCache(TENANT_ID, topicId)).hasSize(1);
    manageDatabase.addTopicToCache(TENANT_ID, t);
    assertThat(getTopicFromCache(TENANT_ID, topicId)).hasSize(1);
  }

  private List<Topic> getTopicFromCache(int tenantId, int topicId) {
    return manageDatabase.getTopicsForTenant(tenantId).stream()
        .filter(entry -> entry.getTopicid().equals(topicId))
        .collect(Collectors.toList());
  }
}
