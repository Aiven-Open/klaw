package io.aiven.klaw.dao.migration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.dao.test.MigrationTestData2x1x0;
import io.aiven.klaw.dao.test.MigrationTestData2x2x0;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.UpdateDataJdbc;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MigrateData2x2x0Test {

  private MigrateData2x2x0 migrateData2x2x0;

  @Mock private SelectDataJdbc selectDataJdbc;

  @Mock private UpdateDataJdbc updateDataJdbc;

  @Bean
  public MigrationTestData2x1x0 MigrateTestData2x1x0() {
    return new MigrationTestData2x1x0();
  }

  @Bean
  public MigrationTestData2x2x0 MigrationTestData2x1x0() {
    return new MigrationTestData2x2x0();
  }

  @BeforeEach
  public void setUp() {
    migrateData2x2x0 = new MigrateData2x2x0(selectDataJdbc, updateDataJdbc);
  }

  @Test
  public void givenNoTenantsDoNotMigrateAnyData() {

    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(0));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(0));

    boolean success = migrateData2x2x0.migrate();
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    // No other calls made
    verify(selectDataJdbc, times(0)).selectAllTeams(anyInt());
    verify(selectDataJdbc, times(0))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(0))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantButNoClaimsRequestsDoNotMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfTopicRequests(0, 0, "0"));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfConnectorRequests(0, 0, "0"));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(1))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    // No Claim Requests so no saving updates back to DB
    verify(updateDataJdbc, times(0)).updateTopicRequest(any());
    verify(updateDataJdbc, times(0)).updateConnectorRequest(any());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithReqeuestsButNoClaimsRequestsDoNotMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfTopicRequests(8, 0, "0"));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfConnectorRequests(8, 0, "0"));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(1))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    // No Claim Requests so no saving updates back to DB
    verify(updateDataJdbc, times(0)).updateTopicRequest(any());
    verify(updateDataJdbc, times(0)).updateConnectorRequest(any());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithReqeuestsAndOneClaimsRequestsMigrateOneData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfTopicRequests(8, 0, "0"));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfConnectorRequests(8, 1, "1"));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(1))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    // No Claim Requests so no saving updates back to DB
    verify(updateDataJdbc, times(0)).updateTopicRequest(any());
    // One Claim requests ensure it is saved back.
    verify(updateDataJdbc, times(1)).updateConnectorRequest(any());
    assertThat(success).isTrue();
  }

  @Test
  public void
      givenOneTenantWithReqeuestsAndOneClaimsRequestsButDataInDescriptionIsNotTeamIdDoNotMigrateOneData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfTopicRequests(8, 0, "0"));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfConnectorRequests(8, 1, "A description"));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(1))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    // No Claim Requests so no saving updates back to DB
    verify(updateDataJdbc, times(0)).updateTopicRequest(any());
    // One Claim requests but description is text not number so it wont update.
    verify(updateDataJdbc, times(0)).updateConnectorRequest(any());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithReqeuestsAndAlreadyMigratedClaimsRequestsDoNotMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(setTopicApprovingTeamId(getListOfTopicRequests(33, 3, "2")));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(setConnectorApprovingTeamId(getListOfConnectorRequests(18, 8, "1")));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(1))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    // No Claim Requests so no saving updates back to DB
    verify(updateDataJdbc, times(0)).updateTopicRequest(any());
    // One Claim requests but description is text not number so it wont update.
    verify(updateDataJdbc, times(0)).updateConnectorRequest(any());

    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantWithReqeuestsAndNoMigratedClaimsRequestsMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfTopicRequests(33, 3, "2"));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfConnectorRequests(18, 8, "1"));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(1))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));
    // No Claim Requests so no saving updates back to DB
    verify(updateDataJdbc, times(3)).updateTopicRequest(any());
    // One Claim requests but description is text not number so it wont update.
    verify(updateDataJdbc, times(8)).updateConnectorRequest(any());

    assertThat(success).isTrue();
  }

  @Test
  public void givenThreeTenantsWithAMixOfRequestsToMigrateAndNotMigrateMigrateData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(3));
    when(selectDataJdbc.selectAllTeams(anyInt())).thenReturn(getTeams(2));
    when(selectDataJdbc.selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfTopicRequests(33, 3, "2"))
        .thenReturn(setTopicApprovingTeamId(getListOfTopicRequests(33, 30, "2")))
        .thenReturn(getListOfTopicRequests(13, 8, "2"));
    when(selectDataJdbc.selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false)))
        .thenReturn(getListOfConnectorRequests(18, 8, "1"))
        .thenReturn(setConnectorApprovingTeamId(getListOfConnectorRequests(18, 8, "1")))
        .thenReturn(getListOfConnectorRequests(43, 13, "2"));

    // Execute
    boolean success = migrateData2x2x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(3))
        .selectFilteredTopicRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(null),
            eq(null),
            eq(false));
    verify(selectDataJdbc, times(3))
        .selectFilteredKafkaConnectorRequests(
            anyBoolean(),
            eq("superadmin"),
            eq(null),
            eq(null),
            eq(true),
            anyInt(),
            eq(null),
            eq(null),
            eq(false));

    // Topic Requests & Connector Requests have a mix of allready updated and non updated claims.
    // So we expect 11 topic reqs and 21 connector requests
    verify(updateDataJdbc, times(11)).updateTopicRequest(any());
    verify(updateDataJdbc, times(21)).updateConnectorRequest(any());

    assertThat(success).isTrue();
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
