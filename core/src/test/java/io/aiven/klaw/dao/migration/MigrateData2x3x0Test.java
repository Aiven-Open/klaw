package io.aiven.klaw.dao.migration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.dao.test.MigrationTestData2x1x0;
import io.aiven.klaw.dao.test.MigrationTestData2x2x0;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.response.EnvParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MigrateData2x3x0Test {

  private MigrateData2x3x0 migrateData2x3x0;

  @Mock private SelectDataJdbc selectDataJdbc;

  @Mock private InsertDataJdbc insertDataJdbc;

  @Mock private ManageDatabase manageDatabase;

  @Bean
  public MigrationTestData2x1x0 MigrateTestData2x1x0() {
    return new MigrationTestData2x1x0();
  }

  @Bean
  public MigrationTestData2x2x0 MigrationTestData2x1x0() {
    return new MigrationTestData2x2x0();
  }

  @Captor ArgumentCaptor<Env> envCaptor;

  @BeforeEach
  public void setUp() {
    migrateData2x3x0 = new MigrateData2x3x0(selectDataJdbc, insertDataJdbc, manageDatabase);
  }

  @Test
  public void givenNoTenantsDoNotMigrateAnyData() {

    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(0));

    boolean success = migrateData2x3x0.migrate();
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    // No other calls made
    verify(insertDataJdbc, times(0)).addNewEnv(any(Env.class));
    verify(selectDataJdbc, times(0)).selectAllEnvs(any(KafkaClustersType.class), anyInt());
    //    called once per tenant
    verify(manageDatabase, times(0)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantButNoEnvsDoNotMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));

    when(selectDataJdbc.selectAllEnvs(any(), anyInt())).thenReturn(Collections.EMPTY_LIST);

    // Execute
    boolean success = migrateData2x3x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1)).selectAllEnvs(any(KafkaClustersType.class), anyInt());

    verify(insertDataJdbc, times(0)).addNewEnv(any(Env.class));
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithEnvsMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));

    when(selectDataJdbc.selectAllEnvs(any(), anyInt())).thenReturn(createListOfEnvs(2));

    // Execute
    boolean success = migrateData2x3x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllEnvs(any(KafkaClustersType.class), anyInt());

    verify(insertDataJdbc, times(2)).addNewEnv(any(Env.class));
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithEnvsAlreadyMigratedDoNotMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    List<Env> envs = createListOfEnvs(2);
    for (Env env : envs) {
      // Set EnvParams introduced in 2.3.0 so they wont be re migrated.
      env.setParams(new EnvParams());
    }

    when(selectDataJdbc.selectAllEnvs(any(), anyInt())).thenReturn(envs);

    // Execute
    boolean success = migrateData2x3x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();

    verify(insertDataJdbc, times(0)).addNewEnv(any(Env.class));
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithTwoEnvsAndOneIsNotProcessableAnExceptionMigrateOneData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    List<Env> envs = createListOfEnvs(2);
    // Set EnvParams introduced in 2.3.0 so they wont be re migrated.
    envs.get(1).setOtherParams(null);

    when(selectDataJdbc.selectAllEnvs(any(), anyInt())).thenReturn(envs);

    // Execute
    boolean success = migrateData2x3x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(insertDataJdbc, times(1)).addNewEnv(envCaptor.capture());
    Env env = envCaptor.getValue();
    // verify data is correct
    assertThat(env.getParams().getDefaultPartitions().get(0)).isEqualTo("1");
    assertThat(env.getParams().getDefaultPartitions().get(0)).isEqualTo("1");
    assertThat(env.getParams().getPartitionsList().size()).isEqualTo(2);
    assertThat(env.getParams().getReplicationFactorList().size()).isEqualTo(8);
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenTwoTenantWithTwoEnvsAndOneAlreadyMigratedMigrateOneTenant() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(2));
    List<Env> envs = createListOfEnvs(2);
    // Set EnvParams introduced in 2.3.0 so they wont be re migrated.
    envs.get(0).setParams(new EnvParams());
    envs.get(1).setParams(new EnvParams());
    when(selectDataJdbc.selectAllEnvs(any(), anyInt()))
        .thenReturn(envs)
        .thenReturn(createListOfEnvs(2));

    // Execute
    boolean success = migrateData2x3x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(2)).selectAllEnvs(any(), anyInt());
    verify(insertDataJdbc, times(2)).addNewEnv(any(Env.class));
    //    called once per tenant
    verify(manageDatabase, times(2)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenThreeTenantsWithAMixOfRequestsToMigrateAndNotMigrateMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(3));
    List<Env> envs = createListOfEnvs(2);
    // Set EnvParams introduced in 2.3.0 so they wont be re migrated.
    envs.get(0).setParams(new EnvParams());
    envs.get(1).setParams(new EnvParams());
    when(selectDataJdbc.selectAllEnvs(any(), anyInt()))
        .thenReturn(envs)
        .thenReturn(createListOfEnvs(2))
        .thenReturn(createListOfEnvs(2));

    // Execute
    boolean success = migrateData2x3x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(3)).selectAllEnvs(any(), anyInt());
    verify(insertDataJdbc, times(4)).addNewEnv(any(Env.class));
    //    called once per tenant
    verify(manageDatabase, times(3)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  private List<Env> createListOfEnvs(int numberOfEnvs) {
    List<Env> envs = new ArrayList<>();
    for (int i = 0; i < numberOfEnvs; i++) {
      Env env = new Env();
      env.setName("Env1");
      env.setId("1");
      env.setTenantId(1);
      env.setOtherParams(
          "default.partitions=1,max.partitions=2,default.replication.factor=1,max.replication.factor=8,topic.prefix=,topic.suffix=");
      envs.add(env);
    }
    return envs;
  }

  private List<TopicRequest> getListOfTopicRequests(
      int numberOfRequests, int numberOfClaimRequests, String desc) {
    List<TopicRequest> requests = new ArrayList<>();
    for (int i = 0; i < numberOfRequests; i++) {
      TopicRequest request = new TopicRequest();
      request.setTenantId(101 + i);
      request.setTeamId(1 + i);
      request.setDescription(desc);
      request.setRequestor("User" + i);
      if (i < numberOfClaimRequests) {
        request.setRequestOperationType(RequestOperationType.CLAIM.value);
      } else {
        request.setRequestOperationType(RequestOperationType.CREATE.value);
      }

      requests.add(request);
    }

    return requests;
  }

  private List<KafkaConnectorRequest> getListOfConnectorRequests(
      int numberOfRequests, int numberOfClaimRequests, String desc) {
    List<KafkaConnectorRequest> requests = new ArrayList<>();
    for (int i = 0; i < numberOfRequests; i++) {
      KafkaConnectorRequest request = new KafkaConnectorRequest();
      request.setTenantId(101 + i);
      request.setTeamId(1 + i);
      request.setDescription(desc);
      request.setRequestor("User" + i);
      if (i < numberOfClaimRequests) {
        request.setRequestOperationType(RequestOperationType.CLAIM.value);
      } else {
        request.setRequestOperationType(RequestOperationType.CREATE.value);
      }

      requests.add(request);
    }

    return requests;
  }

  private List<TopicRequest> setTopicApprovingTeamId(List<TopicRequest> requests) {

    return requests.stream()
        .map(
            req -> {
              if (req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
                req.setApprovingTeamId(req.getDescription());
              }
              return req;
            })
        .collect(Collectors.toList());
  }

  private List<KafkaConnectorRequest> setConnectorApprovingTeamId(
      List<KafkaConnectorRequest> requests) {

    return requests.stream()
        .map(
            req -> {
              if (req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
                req.setApprovingTeamId(req.getDescription());
              }
              return req;
            })
        .collect(Collectors.toList());
  }

  private List<UserInfo> getUserAndTenantInfo(int numberOfEntries) {
    List<UserInfo> users = new ArrayList<>();
    for (int i = 0; i < numberOfEntries; i++) {
      UserInfo info = new UserInfo();
      info.setTenantId(101 + i);
      info.setTeamId(1 + i);
      info.setRole("User");
      info.setUsername("User" + i);
      info.setFullname("User" + i + " LastName");
      info.setSwitchTeams(false);
      users.add(info);
    }

    return users;
  }

  private List<Team> getTeams(int numberOfEntries) {
    List<Team> teams = new ArrayList<>();
    for (int i = 0; i < numberOfEntries; i++) {
      Team info = new Team();
      info.setTenantId(101 + i);
      info.setTeamId(1 + i);
      teams.add(info);
    }

    return teams;
  }
}
