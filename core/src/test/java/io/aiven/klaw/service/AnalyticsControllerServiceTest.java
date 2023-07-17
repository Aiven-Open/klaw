package io.aiven.klaw.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.constants.TestConstants;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.TeamOverview;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.AclsCountPerEnv;
import io.aiven.klaw.model.response.TopicsCountPerEnv;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerServiceTest {
  @Mock private ManageDatabase manageDatabase;
  @Mock private CommonUtilsService commonUtilsService;
  @Spy @InjectMocks private AnalyticsControllerService analyticsControllerService;

  @Mock private HandleDbRequestsJdbc handleDbRequestsJdbc;
  @Mock private UserDetails userDetails;

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void getEnvName() {
    Env env = new Env();
    env.setName(TestConstants.ENV_NAME);
    env.setId(TestConstants.ENV_ID);

    Mockito.when(manageDatabase.getKafkaEnvList(TestConstants.TENANT_ID)).thenReturn(List.of(env));
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);

    String actual = analyticsControllerService.getEnvName(TestConstants.ENV_ID);

    assertEquals(TestConstants.ENV_NAME, actual);
  }

  @Test
  public void getEnvName_NoEnvNameFound() {
    Mockito.when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(List.of());

    String actual = analyticsControllerService.getEnvName(TestConstants.ENV_ID);

    assertNull(actual);
  }

  @Test
  public void getAclsCountPerEnv() {
    Mockito.when(commonUtilsService.getTenantId(any())).thenReturn(TestConstants.TENANT_ID);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getAclsCountByEnv(null, TestConstants.TENANT_ID))
        .thenReturn(TestConstants.ACLS_COUNT_BY_ENV_ID);
    Mockito.when(manageDatabase.getEnvsOfTenantsMap())
        .thenReturn(Map.of(TestConstants.TENANT_ID, List.of(TestConstants.ENV_ID)));

    AclsCountPerEnv actual = analyticsControllerService.getAclsCountPerEnv(TestConstants.ENV_ID);

    Assertions.assertEquals(ApiResultStatus.SUCCESS.value, actual.getStatus());
    Assertions.assertEquals(TestConstants.ACLS_COUNT, actual.getAclsCount());
  }

  @Test
  public void getTopicsCountPerEnv() {
    Mockito.when(commonUtilsService.getTenantId(any())).thenReturn(TestConstants.TENANT_ID);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getTopicsCountByEnv(TestConstants.TENANT_ID))
        .thenReturn(TestConstants.TOPICS_COUNT_BY_ENV_ID);
    Mockito.when(commonUtilsService.getEnvsFromUserId(any()))
        .thenReturn(Set.of(TestConstants.ENV_ID));

    TopicsCountPerEnv actual =
        analyticsControllerService.getTopicsCountPerEnv(TestConstants.ENV_ID);

    Assertions.assertEquals(ApiResultStatus.SUCCESS.value, actual.getStatus());
    Assertions.assertEquals(TestConstants.TOPICS_COUNT, actual.getTopicsCount());
  }

  @Test
  public void getProducerAclsTeamsOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getAclsCountByTeams(
                AclType.PRODUCER.value, TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(Collections.emptyList());
    Mockito.when(
            manageDatabase.getTeamNameFromTeamId(TestConstants.TENANT_ID, TestConstants.TEAM_ID))
        .thenReturn(TestConstants.TEAM_NAME);
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                eq(Collections.emptyList()),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getProducerAclsTeamsOverview(
            TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getProducerAclsTeamsOverview_TeamIdNull() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getAclsCountByTeams(
                AclType.PRODUCER.value, null, TestConstants.TENANT_ID))
        .thenReturn(Collections.emptyList());
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                eq(Collections.emptyList()),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getProducerAclsTeamsOverview(null, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getConsumerAclsTeamsOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getAclsCountByTeams(
                AclType.CONSUMER.value, TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(Collections.emptyList());
    Mockito.when(
            manageDatabase.getTeamNameFromTeamId(TestConstants.TENANT_ID, TestConstants.TEAM_ID))
        .thenReturn(TestConstants.TEAM_NAME);
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                eq(Collections.emptyList()),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getConsumerAclsTeamsOverview(
            TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getConsumerAclsTeamsOverview_TeamIdNull() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getAclsCountByTeams(
                AclType.CONSUMER.value, null, TestConstants.TENANT_ID))
        .thenReturn(Collections.emptyList());
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                eq(Collections.emptyList()),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getConsumerAclsTeamsOverview(null, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getTopicsTeamsOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getTopicsCountByTeams(
                TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(Collections.emptyList());
    Mockito.when(
            manageDatabase.getTeamNameFromTeamId(TestConstants.TENANT_ID, TestConstants.TEAM_ID))
        .thenReturn(TestConstants.TEAM_NAME);
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                eq(Collections.emptyList()),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getTopicsTeamsOverview(
            TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getTopicsTeamsOverview_TeamIdNull() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getTopicsCountByTeams(null, TestConstants.TENANT_ID))
        .thenReturn(Collections.emptyList());
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                eq(Collections.emptyList()),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getTopicsTeamsOverview(null, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getTopicsEnvOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getTopicsCountByEnv(TestConstants.TENANT_ID))
        .thenReturn(UtilMethods.convertImmutableToMutable(TestConstants.TOPICS_COUNT_BY_ENV_ID));
    Mockito.when(commonUtilsService.getEnvsFromUserId(any()))
        .thenReturn(Set.of(TestConstants.ENV_ID));
    Mockito.when(analyticsControllerService.getEnvName(TestConstants.ENV_ID))
        .thenReturn(TestConstants.ENV_NAME);
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getTopicsEnvOverview(
            TestConstants.TENANT_ID, Mockito.mock(PermissionType.class));

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getTopicsPerTeamEnvOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);
    Mockito.when(commonUtilsService.getTeamId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getAllTopicsForTeamGroupByEnv(
                TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(TestConstants.TOPICS_COUNT_BY_ENV_ID);
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getTopicsPerTeamEnvOverview(TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getPartitionsEnvOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getPartitionsCountByEnv(
                TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(UtilMethods.convertImmutableToMutable(TestConstants.TOPICS_COUNT_BY_ENV_ID));
    Mockito.when(
            manageDatabase.getTeamNameFromTeamId(TestConstants.TENANT_ID, TestConstants.TEAM_ID))
        .thenReturn(TestConstants.TEAM_NAME);
    Mockito.when(manageDatabase.getEnvsOfTenantsMap())
        .thenReturn(Map.of(TestConstants.TENANT_ID, List.of(TestConstants.ENV_ID)));
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getPartitionsEnvOverview(
            TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getAclsEnvOverview() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getAclsCountByEnv(TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(UtilMethods.convertImmutableToMutable(TestConstants.ACLS_COUNT_BY_ENV_ID));
    Mockito.when(
            manageDatabase.getTeamNameFromTeamId(TestConstants.TENANT_ID, TestConstants.TEAM_ID))
        .thenReturn(TestConstants.TEAM_NAME);
    Mockito.when(manageDatabase.getEnvsOfTenantsMap())
        .thenReturn(Map.of(TestConstants.TENANT_ID, List.of(TestConstants.ENV_ID)));
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getAclsEnvOverview(
            TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getActivityLogOverview_TeamIdNull() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(manageDatabase.getEnvsOfTenantsMap())
        .thenReturn(Map.of(TestConstants.TENANT_ID, List.of(TestConstants.ENV_ID)));
    Mockito.when(
            handleDbRequestsJdbc.getActivityLogForLastDays(
                eq(30), any(), eq(TestConstants.TENANT_ID)))
        .thenReturn(List.of(Map.of()));
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getActivityLogOverview(null, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getActivityLogOverview_TeamIdNotNull() {
    ChartsJsOverview expected = new ChartsJsOverview();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getActivityLogByTeam(
                TestConstants.TEAM_ID, 30, TestConstants.TENANT_ID))
        .thenReturn(UtilMethods.convertImmutableToMutable(TestConstants.ACLS_COUNT_BY_ENV_ID));
    Mockito.when(
            manageDatabase.getTeamNameFromTeamId(TestConstants.TENANT_ID, TestConstants.TEAM_ID))
        .thenReturn(TestConstants.TEAM_NAME);
    Mockito.when(
            commonUtilsService.getChartsJsOverview(
                anyList(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                eq(TestConstants.TENANT_ID)))
        .thenReturn(expected);

    ChartsJsOverview actual =
        analyticsControllerService.getActivityLogOverview(
            TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void getTeamsOverview_UnauthorizedUser() {
    ChartsJsOverview chartsJsOverview = new ChartsJsOverview();
    TeamOverview teamOverview = new TeamOverview();
    teamOverview.setProducerAclsPerTeamsOverview(chartsJsOverview);
    teamOverview.setConsumerAclsPerTeamsOverview(chartsJsOverview);
    teamOverview.setTopicsPerEnvOverview(chartsJsOverview);
    teamOverview.setPartitionsPerEnvOverview(chartsJsOverview);
    teamOverview.setActivityLogOverview(chartsJsOverview);
    teamOverview.setAclsPerEnvOverview(chartsJsOverview);
    teamOverview.setTopicsPerTeamsOverview(chartsJsOverview);
    List<TeamOverview> expected = List.of(teamOverview);

    loginMock();
    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);
    Mockito.when(commonUtilsService.getTeamId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.when(
            commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.ALL_TEAMS_REPORTS)))
        .thenReturn(true);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getProducerAclsTeamsOverview(TestConstants.TEAM_ID, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getConsumerAclsTeamsOverview(TestConstants.TEAM_ID, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getTopicsPerTeamEnvOverview(TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getPartitionsEnvOverview(TestConstants.TEAM_ID, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getActivityLogOverview(TestConstants.TEAM_ID, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getAclsEnvOverview(TestConstants.TEAM_ID, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getTopicsTeamsOverview(TestConstants.TEAM_ID, TestConstants.TENANT_ID);

    List<TeamOverview> actual = analyticsControllerService.getTeamsOverview("");

    UtilMethods.assertEqualsList(actual, expected);
  }

  @Test
  public void getTeamsOverview_AuthorizedUser() {
    ChartsJsOverview chartsJsOverview = new ChartsJsOverview();
    TeamOverview teamOverview = new TeamOverview();
    teamOverview.setProducerAclsPerTeamsOverview(chartsJsOverview);
    teamOverview.setConsumerAclsPerTeamsOverview(chartsJsOverview);
    teamOverview.setTopicsPerEnvOverview(chartsJsOverview);
    teamOverview.setPartitionsPerEnvOverview(chartsJsOverview);
    teamOverview.setActivityLogOverview(chartsJsOverview);
    teamOverview.setAclsPerEnvOverview(chartsJsOverview);
    teamOverview.setTopicsPerTeamsOverview(chartsJsOverview);
    List<TeamOverview> expected = List.of(teamOverview);

    loginMock();
    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);
    Mockito.when(commonUtilsService.getTeamId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.when(
            commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.ALL_TEAMS_REPORTS)))
        .thenReturn(false);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getProducerAclsTeamsOverview(null, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getConsumerAclsTeamsOverview(null, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getTopicsEnvOverview(TestConstants.TENANT_ID, PermissionType.ALL_TEAMS_REPORTS);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getPartitionsEnvOverview(null, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getActivityLogOverview(null, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getAclsEnvOverview(null, TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getTopicsTeamsOverview(null, TestConstants.TENANT_ID);

    List<TeamOverview> actual = analyticsControllerService.getTeamsOverview("");

    UtilMethods.assertEqualsList(actual, expected);
  }

  @Test
  public void getActivityLogForTeamOverview() {
    ChartsJsOverview chartsJsOverview = new ChartsJsOverview();
    TeamOverview expected = new TeamOverview();
    expected.setTopicsPerTeamPerEnvOverview(chartsJsOverview);
    expected.setActivityLogOverview(chartsJsOverview);

    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.when(commonUtilsService.getTeamId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getTopicsPerTeamEnvOverview(TestConstants.TENANT_ID);
    Mockito.doReturn(chartsJsOverview)
        .when(analyticsControllerService)
        .getActivityLogOverview(TestConstants.TEAM_ID, 101);

    TeamOverview actual = analyticsControllerService.getActivityLogForTeamOverview("true");
    Assertions.assertEquals(expected, actual);
  }

  @Test
  public void generateReport_UnauthorizedUser() {
    TeamOverview teamOverview = UtilMethods.getDummyTeamOverview();

    List<TeamOverview> listTeamsOverview = List.of(teamOverview);
    Topic topic = UtilMethods.getDummyTopic();
    Acl acl = UtilMethods.getDummyAcl();

    loginMock();
    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.when(
            manageDatabase.getKwPropertyValue(
                KwConstants.KW_REPORTS_TMP_LOCATION_KEY, TestConstants.TENANT_ID))
        .thenReturn(TestConstants.KW_REPORTS_LOCATION);
    Mockito.doReturn(listTeamsOverview).when(analyticsControllerService).getTeamsOverview(null);
    Mockito.when(commonUtilsService.getEnvsFromUserId(any()))
        .thenReturn(Set.of(TestConstants.ENV_ID));
    Mockito.doReturn(TestConstants.ENV_NAME)
        .when(analyticsControllerService)
        .getEnvName(TestConstants.ENV_ID);
    Mockito.when(
            commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.ALL_TEAMS_REPORTS)))
        .thenReturn(true);
    Mockito.when(commonUtilsService.getTeamId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(
            handleDbRequestsJdbc.getTopicsforTeam(TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(List.of(topic));
    Mockito.when(
            handleDbRequestsJdbc.getConsumerGroupsforTeam(
                TestConstants.TEAM_ID, TestConstants.TENANT_ID))
        .thenReturn(List.of(acl));

    File actual = analyticsControllerService.generateReport();
    Assertions.assertNotNull(actual);
  }

  @Test
  public void generateReport_AuthorizedUser() {
    TeamOverview teamOverview = UtilMethods.getDummyTeamOverview();

    List<TeamOverview> listTeamsOverview = List.of(teamOverview);
    Topic topic = UtilMethods.getDummyTopic();
    Acl acl = UtilMethods.getDummyAcl();

    loginMock();
    Mockito.when(commonUtilsService.getCurrentUserName()).thenReturn(TestConstants.USERNAME);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.when(
            manageDatabase.getKwPropertyValue(
                KwConstants.KW_REPORTS_TMP_LOCATION_KEY, TestConstants.TENANT_ID))
        .thenReturn(TestConstants.KW_REPORTS_LOCATION);
    Mockito.doReturn(listTeamsOverview).when(analyticsControllerService).getTeamsOverview(null);
    Mockito.when(commonUtilsService.getEnvsFromUserId(any()))
        .thenReturn(Set.of(TestConstants.ENV_ID));
    Mockito.doReturn(TestConstants.ENV_NAME)
        .when(analyticsControllerService)
        .getEnvName(TestConstants.ENV_ID);
    Mockito.when(
            commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.ALL_TEAMS_REPORTS)))
        .thenReturn(false);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getAllTopics(TestConstants.TENANT_ID))
        .thenReturn(List.of(topic));
    Mockito.when(handleDbRequestsJdbc.getAllConsumerGroups(TestConstants.TENANT_ID))
        .thenReturn(List.of(acl));

    File actual = analyticsControllerService.generateReport();
    Assertions.assertNotNull(actual);
  }
}
