package io.aiven.klaw.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.RolesType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class ManageDatabaseTest {

  public static final int TENANT_ID = 101;
  private ManageDatabase manageDatabase = new ManageDatabase();
  @Mock private HandleDbRequestsJdbc handleDbRequests;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(manageDatabase, "handleDbRequests", handleDbRequests);
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

  @Test
  public void setDefaultEntitySequencesForTenantForExistingIdsNoSeqs() {
    when(handleDbRequests.getNextClusterId(anyInt())).thenReturn(1);
    when(handleDbRequests.getNextEnvId(anyInt())).thenReturn(1);
    when(handleDbRequests.getNextTeamId(anyInt())).thenReturn(1001);

    when(handleDbRequests.hasSequence(EntityType.CLUSTER.name(), 101)).thenReturn(false);
    when(handleDbRequests.hasSequence(EntityType.ENVIRONMENT.name(), 101)).thenReturn(false);
    when(handleDbRequests.hasSequence(EntityType.TEAM.name(), 101)).thenReturn(false);

    manageDatabase.setDefaultEntitySequencesForTenant(101);

    verify(handleDbRequests, times(1))
        .insertIntoKwEntitySequence(EntityType.CLUSTER.name(), 2, 101);
    verify(handleDbRequests, times(1))
        .insertIntoKwEntitySequence(EntityType.ENVIRONMENT.name(), 2, 101);
    verify(handleDbRequests, times(1))
        .insertIntoKwEntitySequence(EntityType.TEAM.name(), 1002, 101);
  }

  @Test
  public void setDefaultEntitySequencesForTenantForExistingIdsAndSeqs() {
    when(handleDbRequests.getNextClusterId(anyInt())).thenReturn(1);
    when(handleDbRequests.getNextEnvId(anyInt())).thenReturn(1);
    when(handleDbRequests.getNextTeamId(anyInt())).thenReturn(1001);

    when(handleDbRequests.hasSequence(EntityType.CLUSTER.name(), 101)).thenReturn(true);
    when(handleDbRequests.hasSequence(EntityType.ENVIRONMENT.name(), 101)).thenReturn(true);
    when(handleDbRequests.hasSequence(EntityType.TEAM.name(), 101)).thenReturn(true);

    manageDatabase.setDefaultEntitySequencesForTenant(101);

    verify(handleDbRequests, times(0))
        .insertIntoKwEntitySequence(EntityType.CLUSTER.name(), 2, 101);
    verify(handleDbRequests, times(0))
        .insertIntoKwEntitySequence(EntityType.ENVIRONMENT.name(), 2, 101);
    verify(handleDbRequests, times(0))
        .insertIntoKwEntitySequence(EntityType.TEAM.name(), 1002, 101);
  }

  @Test
  public void setDefaultEntitySequencesForTenantForNoIds() {
    when(handleDbRequests.getNextClusterId(anyInt())).thenReturn(null);
    when(handleDbRequests.getNextEnvId(anyInt())).thenReturn(null);
    when(handleDbRequests.getNextTeamId(anyInt())).thenReturn(null);

    manageDatabase.setDefaultEntitySequencesForTenant(101);

    verify(handleDbRequests, times(1))
        .insertIntoKwEntitySequence(EntityType.CLUSTER.name(), 1, 101);
    verify(handleDbRequests, times(1))
        .insertIntoKwEntitySequence(EntityType.ENVIRONMENT.name(), 1, 101);
    verify(handleDbRequests, times(1))
        .insertIntoKwEntitySequence(EntityType.TEAM.name(), 1001, 101);
  }

  @Test
  public void getUserInfoMap() {
    UserInfo info1 = buildUserInfo();
    UserInfo info2 = buildUserInfo();
    info2.setTenantId(102);
    UserInfo info3 = buildUserInfo();
    info3.setTenantId(103);
    when(handleDbRequests.getAllUsersAllTenants(RolesType.SUPERADMIN))
        .thenReturn(List.of(info1, info2, info3));

    Map<Integer, UserInfo> result = manageDatabase.getUserInfoMap(RolesType.SUPERADMIN);
    assertThat(result.size()).isEqualTo(3);
    assertThat(result.get(TENANT_ID).getTenantId()).isEqualTo(TENANT_ID);
    assertThat(result.get(102).getTenantId()).isEqualTo(102);
    assertThat(result.get(103).getTenantId()).isEqualTo(103);
  }

  private List<Topic> getTopicFromCache(int tenantId, int topicId) {
    return manageDatabase.getTopicsForTenant(tenantId).stream()
        .filter(entry -> entry.getTopicid().equals(topicId))
        .collect(Collectors.toList());
  }

  private UserInfo buildUserInfo() {
    UserInfo info = new UserInfo();
    info.setTenantId(TENANT_ID);
    info.setRole(RolesType.SUPERADMIN.name());
    return info;
  }
}
