package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_109;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_111;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_114;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_115;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_117;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_119;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_120;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawErrorMessages;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.AuthenticationType;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.NewUserStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.requests.ChangePasswordRequestModel;
import io.aiven.klaw.model.requests.ProfileModel;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import io.aiven.klaw.model.requests.TeamModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.RegisterUserInfoModelResponse;
import io.aiven.klaw.model.response.ResetPasswordInfo;
import io.aiven.klaw.model.response.TeamModelResponse;
import io.aiven.klaw.model.response.UserInfoModelResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersTeamsControllerServiceTest {

  public static final String OCTOPUS = "octopus";
  private static final int TEST_TENANT_ID = 101;
  private static final int TEST_TEAM_ID = 3;
  private static final String TEST_TENANT_NAME = "testTenantName";
  private static final String TEST_NEW_USER_UNAME = "newUserUname";
  private static final String ENCRYPTOR_SECRET_KEY = "encryptorSecretKey";
  private static final String TEST_NEW_USER_PWD_PLAIN_TEXT = "newUserPwd";
  private static final String TEST_AUTHENTICATED_USER_UNAME = "authenticatedUserName";
  private static final String TEST_LOGIN_URL = "http://klaw.com/login";
  private UtilMethods utilMethods;

  @Mock InMemoryUserDetailsManager inMemoryUserDetailsManager;
  @Mock private MailUtils mailService;

  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private UserDetails userDetails;
  @Mock private ManageDatabase manageDatabase;

  private UsersTeamsControllerService usersTeamsControllerService;
  private UserInfo userInfo;
  private RegisterUserInfo testNewRegUser;
  private ArgumentCaptor<Team> teamCaptor;
  private ArgumentCaptor<UserDetails> userDetailsArgCaptor;
  private ArgumentCaptor<UserInfo> userInfoArgCaptor;
  private ArgumentCaptor<String> stringArgCaptor;

  @BeforeEach
  public void setUp() {
    utilMethods = new UtilMethods();
    usersTeamsControllerService = new UsersTeamsControllerService();
    teamCaptor = ArgumentCaptor.forClass(Team.class);
    userDetailsArgCaptor = ArgumentCaptor.forClass(UserDetails.class);
    userInfoArgCaptor = ArgumentCaptor.forClass(UserInfo.class);
    stringArgCaptor = ArgumentCaptor.forClass(String.class);
    testNewRegUser =
        utilMethods.getRegisterUserInfoMock(
            TEST_NEW_USER_UNAME, encodePwd(TEST_NEW_USER_PWD_PLAIN_TEXT));
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "inMemoryUserDetailsManager", inMemoryUserDetailsManager);
    ReflectionTestUtils.setField(usersTeamsControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(usersTeamsControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "encryptorSecretKey", ENCRYPTOR_SECRET_KEY);
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    userInfo = utilMethods.getUserInfoMockDao();
    when(commonUtilsService.getPrincipal()).thenReturn(userDetails);
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
  }

  @Test
  public void getUserInfoDetails() {
    String userId = "testuser";
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("testteam");
    UserInfoModelResponse userInfoModel = usersTeamsControllerService.getUserInfoDetails(userId);
    assertThat(userInfoModel.getTeamId()).isEqualTo(userInfo.getTeamId());
    assertThat(userInfoModel.getUsername()).isEqualTo(userInfo.getUsername());
    assertThat(userInfoModel.getRole()).isEqualTo(userInfo.getRole());
    assertThat(userInfoModel.getTeam()).isEqualTo("testteam");
  }

  @Test
  public void updateProfile() throws KlawException {
    ProfileModel userInfoModel = utilMethods.getUserInfoToUpdateMock();
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    when(handleDbRequests.updateUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.updateProfile(userInfoModel);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateProfileFailureDbUpdate() {
    ProfileModel userInfoModel = utilMethods.getUserInfoToUpdateMock();
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    when(handleDbRequests.updateUser(any())).thenThrow(new RuntimeException("Db update failed"));
    KlawException thrown =
        Assertions.assertThrows(
            KlawException.class, () -> usersTeamsControllerService.updateProfile(userInfoModel));
    assertThat(thrown.getMessage()).isEqualTo("Db update failed");
  }

  @Test
  public void updateUserNotAuthorized() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    ApiResponse apiResponse = usersTeamsControllerService.updateUser(userInfoModel);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  public void updateUserNotAuthorizedToUpdateSuperAdmin() throws KlawException {
    final String userName = "testUser";
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(userDetails)).thenReturn(userName);
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    ApiResponse apiResponse = usersTeamsControllerService.updateUser(userInfoModel);
    assertThat(apiResponse.getMessage())
        .isEqualTo("Not Authorized to update another SUPERADMIN user.");
  }

  @Test
  public void resetPassword_withSuccess() throws KlawException, KlawNotAuthorizedException {
    String newPW = "newPW";
    String resetToken = UUID.randomUUID().toString();
    when(handleDbRequests.getUsersInfo(eq(OCTOPUS))).thenReturn(generateUser(OCTOPUS));
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Octo");
    when(handleDbRequests.resetPassword(eq(OCTOPUS), eq(resetToken), anyString()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(inMemoryUserDetailsManager.loadUserByUsername(eq(OCTOPUS)))
        .thenReturn(userDetails(OCTOPUS, newPW));
    ResetPasswordInfo passwordReset =
        usersTeamsControllerService.resetPassword(OCTOPUS, newPW, resetToken);

    verify(handleDbRequests, times(1)).resetPassword(eq(OCTOPUS), eq(resetToken), anyString());
    verify(inMemoryUserDetailsManager, times(1))
        .updatePassword(any(UserDetails.class), anyString());
    assertThat(passwordReset.isUserFound()).isEqualTo(true);
    assertThat(passwordReset.isTokenSent()).isEqualTo(true);
  }

  @Test
  public void resetPassword_withFailure() throws KlawNotAuthorizedException {
    String newPW = "newPW";
    String resetToken = UUID.randomUUID().toString();
    when(handleDbRequests.getUsersInfo(eq(OCTOPUS))).thenReturn(generateUser(OCTOPUS));
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Octo");
    when(handleDbRequests.resetPassword(eq(OCTOPUS), eq(resetToken), anyString()))
        .thenReturn(ApiResultStatus.FAILURE.value);
    when(inMemoryUserDetailsManager.loadUserByUsername(eq(OCTOPUS)))
        .thenReturn(userDetails(OCTOPUS, newPW));
    ResetPasswordInfo passwordReset =
        usersTeamsControllerService.resetPassword(OCTOPUS, newPW, resetToken);
    verify(handleDbRequests, times(1)).resetPassword(eq(OCTOPUS), eq(resetToken), anyString());
    verify(inMemoryUserDetailsManager, times(0))
        .updatePassword(any(UserDetails.class), anyString());
    assertThat(passwordReset.isUserFound()).isTrue();
    assertThat(passwordReset.isTokenSent()).isFalse();
  }

  @Test
  public void resetPassword_noUser() throws KlawNotAuthorizedException {
    String newPW = "newPW";
    String resetToken = UUID.randomUUID().toString();
    when(handleDbRequests.getUsersInfo(eq(OCTOPUS))).thenReturn(null);

    ResetPasswordInfo passwordReset =
        usersTeamsControllerService.resetPassword(OCTOPUS, newPW, resetToken);

    assertThat(passwordReset.isUserFound()).isEqualTo(false);
    assertThat(passwordReset.isTokenSent()).isEqualTo(false);
  }

  @Test
  public void resetPasswordGenerateToken_noUser() throws KlawException {
    String newPW = "newPW";
    String resetToken = UUID.randomUUID().toString();
    when(handleDbRequests.getUsersInfo(eq(OCTOPUS))).thenReturn(null);

    ResetPasswordInfo passwordReset =
        usersTeamsControllerService.resetPasswordGenerateToken(OCTOPUS);

    assertThat(passwordReset.isUserFound()).isEqualTo(false);
    assertThat(passwordReset.isTokenSent()).isEqualTo(false);
  }

  @Test
  public void resetPasswordGenerateToken_withSuccess() throws KlawException {
    String resetToken = UUID.randomUUID().toString();
    when(handleDbRequests.getUsersInfo(eq(OCTOPUS))).thenReturn(generateUser(OCTOPUS));
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Octo");
    when(handleDbRequests.generatePasswordResetToken(OCTOPUS)).thenReturn(resetToken);

    ResetPasswordInfo resetInfo = usersTeamsControllerService.resetPasswordGenerateToken(OCTOPUS);
    assertThat(resetInfo.isUserFound()).isEqualTo(true);
    assertThat(resetInfo.isTokenSent()).isEqualTo(true);
  }

  @Test
  public void resetPasswordGenerateToken_withFailure() throws KlawException {

    when(handleDbRequests.getUsersInfo(eq(OCTOPUS))).thenReturn(generateUser(OCTOPUS));
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn("Octo");
    when(handleDbRequests.generatePasswordResetToken(OCTOPUS))
        .thenReturn(ApiResultStatus.FAILURE.value);

    ResetPasswordInfo resetInfo = usersTeamsControllerService.resetPasswordGenerateToken(OCTOPUS);
    assertThat(resetInfo.isUserFound()).isEqualTo(true);
    assertThat(resetInfo.isTokenSent()).isEqualTo(false);
  }

  @Test
  public void getTeamDetails() {
    Team teamDaoMock = utilMethods.getTeamDaoMock();
    Map<Integer, String> tenantMapMock = utilMethods.getTenantMapMock();

    getTeamDetailsSetupTest(tenantMapMock, teamDaoMock, "testUserName");

    TeamModelResponse response =
        usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME);

    getTeamDetailsVerifyResponse(response, teamDaoMock, tenantMapMock);
    assertThat(response.getEnvList())
        .isEqualTo(List.of(teamDaoMock.getRequestTopicsEnvs().split("\\s*,\\s*")));
  }

  @Test
  public void getTeamDetailsNoRequestTopicsEnvs() {
    Team teamDaoMock = utilMethods.getTeamDaoMock();
    teamDaoMock.setRequestTopicsEnvs(null);
    Map<Integer, String> tenantMapMock = utilMethods.getTenantMapMock();

    getTeamDetailsSetupTest(tenantMapMock, teamDaoMock, "testUserName");

    TeamModelResponse response =
        usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME);

    getTeamDetailsVerifyResponse(response, teamDaoMock, tenantMapMock);
    assertThat(response.getEnvList()).isNull();
  }

  @Test
  public void getTeamDetailsTeamDoesNotExist() {
    final String testUserName = "testUserName";
    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_ID)).thenReturn(null);

    TeamModelResponse response =
        usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME);

    assertThat(response).isNull();
  }

  @Test
  public void getTeamDetailsInvalidTenantName() {
    Team teamDaoMock = utilMethods.getTeamDaoMock();

    getTeamDetailsSetupTest(Collections.emptyMap(), teamDaoMock, "testUserName");

    assertThatExceptionOfType(NoSuchElementException.class)
        .isThrownBy(
            () -> usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME))
        .withMessage("No value present");
  }

  @Test
  public void getAllTeamsSUFromRegisterUsers() {
    Team team = utilMethods.getTeamDaoMock();
    List<Env> envs = utilMethods.getEnvLists();
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();

    getAllTeamsSUFromRegisteredUsersSetupTest(List.of(team), envs, tenantMap);

    List<TeamModelResponse> teamModelResponses =
        usersTeamsControllerService.getAllTeamsSUFromRegisterUsers();

    assertThat(teamModelResponses.size()).isEqualTo(1);
    TeamModelResponse teamModelResponse = teamModelResponses.get(0);

    getAllTeamsSUFromRegisteredUsersVerifyResponse(teamModelResponse, team, envs);
    verify(manageDatabase, times(3)).getKafkaEnvList(TEST_TENANT_ID);
  }

  @Test
  public void getAllTeamsSUFromRegisterUsersNoEnvs() {
    Team team = utilMethods.getTeamDaoMock();
    team.setRequestTopicsEnvs(null);

    List<Env> envs = utilMethods.getEnvLists();
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();

    getAllTeamsSUFromRegisteredUsersSetupTest(List.of(team), envs, tenantMap);

    List<TeamModelResponse> teamModelResponses =
        usersTeamsControllerService.getAllTeamsSUFromRegisterUsers();

    assertThat(teamModelResponses.size()).isEqualTo(1);
    TeamModelResponse teamModelResponse = teamModelResponses.get(0);

    getAllTeamsSUFromRegisteredUsersVerifyResponse(teamModelResponse, team, null);
    verify(manageDatabase, never()).getKafkaEnvList(anyInt());
  }

  @Test
  public void getAllTeamsSUFromRegisterUsersEnvInvalidForTenant() {
    List<Team> teams = utilMethods.getTeamsForTenant();
    teams.get(0).setRequestTopicsEnvs("1, X, 3");
    teams.get(1).setRequestTopicsEnvs("X, 1, 2");
    teams.get(2).setRequestTopicsEnvs("1, 2, X");

    List<Env> envs = utilMethods.getEnvLists();
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();

    getAllTeamsSUFromRegisteredUsersSetupTest(teams, envs, tenantMap);

    List<TeamModelResponse> teamModelResponses =
        usersTeamsControllerService.getAllTeamsSUFromRegisterUsers();

    assertThat(teamModelResponses.size()).isEqualTo(3);

    getAllTeamsSUFromRegisteredUsersVerifyResponse(
        teamModelResponses.get(0), teams.get(0), List.of(envs.get(0)));
    getAllTeamsSUFromRegisteredUsersVerifyResponse(
        teamModelResponses.get(1), teams.get(1), Collections.emptyList());
    getAllTeamsSUFromRegisteredUsersVerifyResponse(
        teamModelResponses.get(2), teams.get(2), List.of(envs.get(0), envs.get(1)));

    verify(manageDatabase, times(6)).getKafkaEnvList(TEST_TENANT_ID);
  }

  @Test
  public void getAllTeamsSUFromRegisterUsersNoUsersForTenant() {
    getAllTeamsSUFromRegisteredUsersSetupTest(
        Collections.emptyList(), utilMethods.getEnvLists(), utilMethods.getTenantMapMock());

    List<TeamModelResponse> teamModelResponses =
        usersTeamsControllerService.getAllTeamsSUFromRegisterUsers();

    assertThat(teamModelResponses.size()).isEqualTo(0);
    verify(manageDatabase, never()).getKafkaEnvList(anyInt());
  }

  @Test
  public void getAllTeamsSUOnlyWhenNoMatchingTeamName() {
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();
    List<Team> teamsForTenant = utilMethods.getTeamsForTenant();
    List<Team> teamsForUser = List.of(utilMethods.getTeamDaoMock());
    teamsForUser.get(0).setTeamname("diffName");
    List<UserInfo> userInfoList = utilMethods.getUserInfoList(1, "testUser");

    getAllTeamsSUSetupTest(teamsForTenant, userInfoList, tenantMap, false);
    when(handleDbRequests.getAllTeamsOfUsers(TEST_AUTHENTICATED_USER_UNAME, TEST_TENANT_ID))
        .thenReturn(teamsForUser);

    List<String> resultTeamNames = usersTeamsControllerService.getAllTeamsSUOnly();

    assertThat(resultTeamNames.size()).isEqualTo(teamsForTenant.size() + teamsForUser.size() + 1);
    assertThat(resultTeamNames.get(0)).isEqualTo("All teams");
    assertThat(resultTeamNames.get(1)).isEqualTo(teamsForUser.get(0).getTeamname());
    for (int i = 0; i < teamsForTenant.size(); i++) {
      assertThat(resultTeamNames.get(i + 2)).isEqualTo(teamsForTenant.get(i).getTeamname());
    }
  }

  @Test
  public void getAllTeamsSUOnlyWhenMatchingTeamName() {
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();
    List<Team> teamsForTenant = utilMethods.getTeamsForTenant();
    List<Team> teamsForUser = List.of(utilMethods.getTeamDaoMock());
    List<UserInfo> userInfoList = utilMethods.getUserInfoList(1, "testUser");

    getAllTeamsSUSetupTest(teamsForTenant, userInfoList, tenantMap, false);
    when(handleDbRequests.getAllTeamsOfUsers(TEST_AUTHENTICATED_USER_UNAME, TEST_TENANT_ID))
        .thenReturn(teamsForUser);

    List<String> resultTeamNames = usersTeamsControllerService.getAllTeamsSUOnly();

    assertThat(resultTeamNames.size()).isEqualTo(teamsForTenant.size() + teamsForUser.size());
    assertThat(resultTeamNames.get(0)).isEqualTo("All teams");
    for (int i = 0; i < teamsForTenant.size(); i++) {
      assertThat(resultTeamNames.get(i + 1)).isEqualTo(teamsForTenant.get(i).getTeamname());
    }
  }

  @Test
  public void getAllTeamsSUMultipleTeams() {
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();
    List<Team> teamList = utilMethods.getTeamsForTenant();
    List<UserInfo> userInfoList = utilMethods.getUserInfoList(1, "testUser");
    userInfoList.get(0).setSwitchAllowedTeamIds(Set.of(Integer.MAX_VALUE));

    getAllTeamsSUSetupTest(teamList, userInfoList, tenantMap, false);

    for (Team team : teamList) {
      when(handleDbRequests.existsComponentsCountForTeam(team.getTeamId(), TEST_TENANT_ID))
          .thenReturn(false);
      when(handleDbRequests.existsUsersInfoForTeam(team.getTeamId(), TEST_TENANT_ID))
          .thenReturn(false);
    }

    List<TeamModelResponse> teamModelResponses = usersTeamsControllerService.getAllTeamsSU();

    assertThat(teamModelResponses.size()).isEqualTo(teamList.size());
    getAllTeamSUVerifyResponse(teamModelResponses, teamList, tenantMap);

    for (TeamModelResponse teamModelResponse : teamModelResponses) {
      assertThat(teamModelResponse.isShowDeleteTeam()).isTrue();
    }

    verify(handleDbRequests, times(3)).existsComponentsCountForTeam(anyInt(), anyInt());
    verify(handleDbRequests, times(3)).existsUsersInfoForTeam(anyInt(), anyInt());
  }

  @Test
  public void getAllTeamsSUNoTeams() {
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();
    List<UserInfo> userInfoList = utilMethods.getUserInfoList(1, "testUser");

    getAllTeamsSUSetupTest(Collections.emptyList(), userInfoList, tenantMap, false);

    List<TeamModelResponse> teamModelResponses = usersTeamsControllerService.getAllTeamsSU();

    assertThat(teamModelResponses).isEmpty();
    verify(handleDbRequests, never()).existsComponentsCountForTeam(anyInt(), anyInt());
    verify(handleDbRequests, never()).existsUsersInfoForTeam(anyInt(), anyInt());
  }

  @ParameterizedTest
  @MethodSource
  public void getAllTeamsSUShowDeleteTeam(
      boolean existComponentsCountForTeam,
      boolean existUsersInfoForTeam,
      boolean switchAllowedTeamMatch,
      boolean unAuthorizedUser) {
    Map<Integer, String> tenantMap = utilMethods.getTenantMapMock();
    List<Team> teamList = utilMethods.getTeams();
    teamList.get(0).setTeamId(TEST_TEAM_ID);

    List<UserInfo> userInfoList = utilMethods.getUserInfoList(1, "testUser");
    if (switchAllowedTeamMatch) {
      userInfoList.get(0).setSwitchAllowedTeamIds(Set.of(TEST_TEAM_ID));
    } else {
      userInfoList.get(0).setSwitchAllowedTeamIds(Set.of(TEST_TEAM_ID + 1));
    }

    getAllTeamsSUSetupTest(teamList, userInfoList, tenantMap, unAuthorizedUser);
    when(handleDbRequests.existsComponentsCountForTeam(TEST_TEAM_ID, TEST_TENANT_ID))
        .thenReturn(existComponentsCountForTeam);
    when(handleDbRequests.existsUsersInfoForTeam(TEST_TEAM_ID, TEST_TENANT_ID))
        .thenReturn(existUsersInfoForTeam);

    List<TeamModelResponse> teamModelResponses = usersTeamsControllerService.getAllTeamsSU();

    assertThat(teamModelResponses.size()).isEqualTo(1);
    getAllTeamSUVerifyResponse(teamModelResponses, teamList, tenantMap);

    if (!unAuthorizedUser) {
      if (existComponentsCountForTeam || existUsersInfoForTeam || switchAllowedTeamMatch) {
        assertThat(teamModelResponses.get(0).isShowDeleteTeam()).isFalse();
      } else {
        assertThat(teamModelResponses.get(0).isShowDeleteTeam()).isTrue();
      }
    } else {
      assertThat(teamModelResponses.get(0).isShowDeleteTeam()).isFalse();
      verify(handleDbRequests, never()).existsComponentsCountForTeam(anyInt(), anyInt());
      verify(handleDbRequests, never()).existsUsersInfoForTeam(anyInt(), anyInt());
    }
  }

  public static Stream<Arguments> getAllTeamsSUShowDeleteTeam() {
    return Stream.of(
        Arguments.of(false, false, false, false),
        Arguments.of(true, false, false, false),
        Arguments.of(false, true, false, false),
        Arguments.of(false, false, true, false),
        Arguments.of(false, false, false, true));
  }

  @Test
  public void deleteTeamSuccess() throws KlawException {
    deleteTeamSetup(ApiResultStatus.SUCCESS, false, false, false, false, TEST_TEAM_ID + 1);

    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(TEST_TEAM_ID);

    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
    verify(commonUtilsService)
        .updateMetadata(TEST_TENANT_ID, EntityType.TEAM, MetadataOperationType.DELETE, null);
  }

  @Test
  public void deleteTeamFailureUnAuthorizedUser() throws KlawException {
    deleteTeamSetup(null, true, false, false, false, Integer.MAX_VALUE);

    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(TEST_TEAM_ID);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.NOT_AUTHORIZED.getMessage());
    verifyNoInteractions(manageDatabase);
    verifyNoInteractions(handleDbRequests);
    verify(commonUtilsService, never()).getTenantId(anyString());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void deleteTeamFailureUserInfoForTeamExists() throws KlawException {
    deleteTeamSetup(null, false, true, false, false, TEST_TEAM_ID);

    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(TEST_TEAM_ID);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(KlawErrorMessages.TEAMS_ERR_103);
    verify(handleDbRequests, never()).existsComponentsCountForTeam(anyInt(), anyInt());
    verify(handleDbRequests, never()).deleteTeamRequest(anyInt(), anyInt());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void deleteTeamFailureComponentCountForTeamExists() throws KlawException {
    deleteTeamSetup(null, false, false, true, false, TEST_TEAM_ID);

    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(TEST_TEAM_ID);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(KlawErrorMessages.TEAMS_ERR_104);
    verify(handleDbRequests, never()).deleteTeamRequest(anyInt(), anyInt());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void deleteTeamFailureDeletingOwnTeam() throws KlawException {
    deleteTeamSetup(null, false, false, false, false, TEST_TEAM_ID);

    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(TEST_TEAM_ID);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(KlawErrorMessages.TEAMS_ERR_105);
    verify(handleDbRequests, never()).deleteTeamRequest(anyInt(), anyInt());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void deleteTeamFailureDbApi() throws KlawException {
    deleteTeamSetup(ApiResultStatus.FAILURE, false, false, false, false, TEST_TEAM_ID + 1);

    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(TEST_TEAM_ID);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void deleteTeamFailureDbException() {
    deleteTeamSetup(null, false, false, false, true, TEST_TEAM_ID + 1);

    assertThatExceptionOfType(KlawException.class)
        .isThrownBy(() -> usersTeamsControllerService.deleteTeam(TEST_TEAM_ID))
        .withMessage("Db exception");

    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void deleteUserFailureHasRequests() throws KlawException {
    String userName = "testuser";
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(mailService.getUserName(userDetails)).thenReturn(userName);
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt())).thenReturn(new HashMap<>());
    when(handleDbRequests.existsComponentsCountForUser("testuser", TEST_TENANT_ID))
        .thenReturn(true);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage())
        .isEqualTo(
            "Not allowed to delete this user, as there are associated requests in the metadata.");
  }

  @Test
  public void deleteUserFailureisAdmin() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(commonUtilsService.isNotAuthorizedUser(
            userDetails, PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    when(handleDbRequests.deleteUserRequest(anyString())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteUserSuccessNormalUser() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForNormalUser());
    when(handleDbRequests.deleteUserRequest(anyString())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteUserFailureNoSuperUserPermission() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_106);
  }

  @Test
  public void deleteUserFailureNoDeletionPermission() throws KlawException {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.NOT_AUTHORIZED.getMessage());
  }

  @Test
  public void addNewUserAlphaNumeric() throws KlawException {
    UserInfoModel newUser = utilMethods.getUserInfoMock();
    when(handleDbRequests.addNewUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.addNewUser(newUser, false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
  }

  @Test
  public void addNewUserEmail() throws KlawException {
    UserInfoModel newUser = utilMethods.getUserInfoMock();
    newUser.setUsername("test@test.com"); // email pattern
    when(handleDbRequests.addNewUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.addNewUser(newUser, false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
  }

  @Test
  public void addNewUserInvalidPatternFailure() throws KlawException {
    UserInfoModel newUser = utilMethods.getUserInfoMock();
    newUser.setUsername("abc123$%$"); // invalid pattern
    ApiResponse apiResponse = usersTeamsControllerService.addNewUser(newUser, false);
    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_109);
  }

  @Test
  public void addNewTeamSuccess() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    addNewTeamSetupTest("testuser", "existingTeamName");
    when(handleDbRequests.addNewTeam(teamCaptor.capture()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, true);

    addNewTeamVerifyCapturedTeam(teamModel);

    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(commonUtilsService)
        .updateMetadata(TEST_TENANT_ID, EntityType.TEAM, MetadataOperationType.CREATE, null);
  }

  @Test
  public void addNewTeamWithApiFailureFromDB() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    addNewTeamSetupTest("testuser", "existingTeamName");
    when(handleDbRequests.addNewTeam(teamCaptor.capture()))
        .thenReturn(ApiResultStatus.FAILURE.value);

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, true);

    addNewTeamVerifyCapturedTeam(teamModel);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void addNewTeamFailureWithException() {
    String errorMessage = "Failure. Team already exists";
    TeamModel teamModel = utilMethods.getTeamModelMock();

    addNewTeamSetupTest("testuser", "existingTeamName");
    when(handleDbRequests.addNewTeam(teamCaptor.capture()))
        .thenThrow(new RuntimeException(errorMessage));

    assertThatExceptionOfType(KlawException.class)
        .isThrownBy(() -> usersTeamsControllerService.addNewTeam(teamModel, true))
        .withMessage(errorMessage);

    addNewTeamVerifyCapturedTeam(teamModel);

    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void addNewTeamFailureWithExistingTeamName() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    when(manageDatabase.getTeamNamesForTenant(teamModel.getTenantId()))
        .thenReturn(List.of(teamModel.getTeamname()));

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, false);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_119);
  }

  @Test
  public void addNewTeamFailureWithUnAuthorizedUser() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, true);
    assertThat(apiResponse).isSameAs(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  public void updateTeamSuccess() throws KlawException {
    TeamModel teamModel = updateTeamSetupTest("newTeamName", ApiResultStatus.SUCCESS);

    ApiResponse apiResponse = usersTeamsControllerService.updateTeam(teamModel);

    updateTeamVerifySuccess(apiResponse, teamModel, true);
  }

  @Test
  public void updateTeamSuccessWhenNoEnvList() throws KlawException {
    TeamModel teamModel = updateTeamSetupTest("newTeamName", ApiResultStatus.SUCCESS);
    teamModel.setEnvList(null);

    ApiResponse apiResponse = usersTeamsControllerService.updateTeam(teamModel);

    updateTeamVerifySuccess(apiResponse, teamModel, false);
  }

  @Test
  public void updateTeamWhenUnAuthorizedUser() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    ApiResponse apiResponse = usersTeamsControllerService.updateTeam(teamModel);

    assertThat(apiResponse).isSameAs(ApiResponse.NOT_AUTHORIZED);
    verifyNoInteractions(manageDatabase);
    verifyNoInteractions(handleDbRequests);
  }

  @Test
  public void updateTeamWhenTeamNameExists() throws KlawException {
    TeamModel teamModel = updateTeamSetupTest("Seahorses", ApiResultStatus.SUCCESS);

    ApiResponse apiResponse = usersTeamsControllerService.updateTeam(teamModel);

    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_119);
    assertThat(apiResponse.isSuccess()).isFalse();
    verifyNoInteractions(handleDbRequests);
  }

  @Test
  public void updateTeamWhenDBApiError() throws KlawException {
    TeamModel teamModel = updateTeamSetupTest("newTeamName", ApiResultStatus.FAILURE);

    ApiResponse apiResponse = usersTeamsControllerService.updateTeam(teamModel);

    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.FAILURE.getMessage());
    assertThat(apiResponse.isSuccess()).isFalse();
    verify(commonUtilsService)
        .updateMetadata(TEST_TENANT_ID, EntityType.TEAM, MetadataOperationType.UPDATE, null);

    updateTeamVerifyCapturedTeam(teamModel, true);
  }

  @Test
  public void updateTeamWhenDBExceptionError() {
    TeamModel teamModel = updateTeamSetupTest("newTeamName", null);

    assertThatExceptionOfType(KlawException.class)
        .isThrownBy(() -> usersTeamsControllerService.updateTeam(teamModel))
        .withMessage("DB Error");

    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  public void changePwd() throws KlawException {
    UserDetails updatePwdUserDetails = mock(UserDetails.class);
    ChangePasswordRequestModel changePwdRequestModel = utilMethods.getChangePwdRequestModelMock();
    changePwdSetupTest(updatePwdUserDetails, ApiResultStatus.SUCCESS);

    ApiResponse apiResponse = usersTeamsControllerService.changePwd(changePwdRequestModel);

    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(apiResponse.isSuccess()).isTrue();

    changePwdEncodedParams(updatePwdUserDetails, changePwdRequestModel);
  }

  @Test
  public void changePwdWhenDbApiFailure() throws KlawException {
    UserDetails updatePwdUserDetails = mock(UserDetails.class);
    ChangePasswordRequestModel changePwdRequestModel = utilMethods.getChangePwdRequestModelMock();
    changePwdSetupTest(updatePwdUserDetails, ApiResultStatus.FAILURE);

    ApiResponse apiResponse = usersTeamsControllerService.changePwd(changePwdRequestModel);

    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);
    assertThat(apiResponse.isSuccess()).isFalse();

    changePwdEncodedParams(updatePwdUserDetails, changePwdRequestModel);
  }

  @Test
  public void changePwdWhenDbExceptionFailure() {
    changePwdSetupTest(mock(UserDetails.class), null);

    assertThatExceptionOfType(KlawException.class)
        .isThrownBy(
            () -> usersTeamsControllerService.changePwd(utilMethods.getChangePwdRequestModelMock()))
        .withMessage("DB Error");
  }

  @ParameterizedTest
  @MethodSource
  public void changePwdWhenNonDBAuth(AuthenticationType authType) throws KlawException {
    ReflectionTestUtils.setField(usersTeamsControllerService, "authenticationType", authType.value);

    ApiResponse apiResponse = usersTeamsControllerService.changePwd(null);

    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_114);
    assertThat(apiResponse.isSuccess()).isFalse();
    changePwdVerifyNoInteractionsWithAnyServices();
  }

  public static Stream<Arguments> changePwdWhenNonDBAuth() {
    return Stream.of(
        Arguments.of(AuthenticationType.LDAP), Arguments.of(AuthenticationType.ACTIVE_DIRECTORY));
  }

  @Test
  public void changePwdWhenWhenRepeatPwdDoesNotMatch() throws KlawException {
    ChangePasswordRequestModel changePwdRequestModel = utilMethods.getChangePwdRequestModelMock();
    changePwdRequestModel.setRepeatPwd(String.format("%s%s", changePwdRequestModel.getPwd(), "1"));

    ApiResponse apiResponse = usersTeamsControllerService.changePwd(changePwdRequestModel);

    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_120);
    assertThat(apiResponse.isSuccess()).isFalse();
    changePwdVerifyNoInteractionsWithAnyServices();
  }

  @Test
  public void showUsersWithNoFilterAndSwitchEnabled() {
    List<UserInfo> userInfosMock = showUsersSetupTest("testuser", 2, 0, "Octopus", 1, "Seahorses");

    List<UserInfoModelResponse> userInfos = usersTeamsControllerService.showUsers(null, null, "1");

    assertThat(userInfos.size()).isEqualTo(2);
    showUsersValidateUserInfo(userInfos.get(0), userInfosMock.get(0).getTeamId(), "Seahorses");
    showUsersValidateUserInfo(userInfos.get(1), userInfosMock.get(1).getTeamId(), "Octopus");
  }

  @Test
  public void showUsersWithNoFilterAndOneSwitchDisable() {
    List<UserInfo> userInfosMock = showUsersSetupTest("testuser", 2, 0, "Octopus", 1, "Seahorses");
    userInfosMock.get(1).setSwitchTeams(false);

    List<UserInfoModelResponse> userInfos = usersTeamsControllerService.showUsers(null, null, "1");

    assertThat(userInfos.size()).isEqualTo(2);
    showUsersValidateUserInfo(userInfos.get(0), userInfosMock.get(0).getTeamId(), "Seahorses");
    assertThat(userInfos.get(1).getTeamId()).isEqualTo(userInfosMock.get(1).getTeamId());
    assertThat(userInfos.get(1).getSwitchAllowedTeamNames()).isNull();
  }

  @Test
  public void showUsersWithNoFilterAndSwitchTeamExistsButUserNotMember() {
    List<UserInfo> userInfosMock =
        showUsersSetupTest(
            "testuser", 1, 1, "Seahorses", Integer.MAX_VALUE, "Not Member of this Team");

    List<UserInfoModelResponse> userInfos = usersTeamsControllerService.showUsers(null, null, "1");

    assertThat(userInfos.size()).isEqualTo(1);
    showUsersValidateUserInfo(userInfos.get(0), userInfosMock.get(0).getTeamId(), "Seahorses");
  }

  @Test
  public void showUsersWithNoFilterAndSwitchToTeamThatDoesNotExist() {
    String userName = "testuser";
    List<UserInfo> userInfosMock = utilMethods.getUserInfoList(1, "testUser");

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(userName)).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getAllUsersInfo(TEST_TENANT_ID)).thenReturn(userInfosMock);

    List<UserInfoModelResponse> userInfos = usersTeamsControllerService.showUsers(null, null, "1");

    assertThat(userInfos.size()).isEqualTo(1);
    showUsersValidateUserInfo(userInfos.get(0), userInfosMock.get(0).getTeamId(), null);
  }

  @Test
  public void showUsersWithValidTeamIdFilter() {
    int filterUserIndex = 1;
    List<UserInfo> userInfosMock = showUsersSetupTest("testuser", 2, 0, "Octopus", 1, "Seahorses");

    List<UserInfoModelResponse> userInfos =
        usersTeamsControllerService.showUsers(filterUserIndex, null, "1");

    assertThat(userInfos.size()).isEqualTo(1);
    showUsersValidateUserInfo(
        userInfos.get(0), userInfosMock.get(filterUserIndex).getTeamId(), "Octopus");
  }

  @Test
  public void showUsersWithInValidTeamIdFilter() {
    showUsersSetupTest("testuser", 2, 0, "Octopus", 1, "Seahorses");

    List<UserInfoModelResponse> userInfos =
        usersTeamsControllerService.showUsers(Integer.MAX_VALUE, null, "1");

    assertThat(userInfos.size()).isEqualTo(0);
  }

  @Test
  public void showUsersWithValidUserSearchStrFilter() {
    List<UserInfo> userInfosMock = showUsersSetupTest("testuser", 2, 0, "Octopus", 1, "Seahorses");

    List<UserInfoModelResponse> userInfos = usersTeamsControllerService.showUsers(null, "0", "1");

    assertThat(userInfos.size()).isEqualTo(1);
    showUsersValidateUserInfo(userInfos.get(0), userInfosMock.get(0).getTeamId(), "Seahorses");
  }

  @Test
  public void showUsersWithInValidUserSearchStrFilter() {
    showUsersSetupTest("testuser", 2, 0, "Octopus", 1, "Seahorses");

    List<UserInfoModelResponse> userInfos =
        usersTeamsControllerService.showUsers(null, "invalid", "1");

    assertThat(userInfos.size()).isEqualTo(0);
  }

  @Test
  public void showUsersWithMultiplePages() {
    String userName = "testuser";
    List<UserInfo> userInfosMock = utilMethods.getUserInfoList(30, "testUser");

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(userName)).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getAllUsersInfo(TEST_TENANT_ID)).thenReturn(userInfosMock);

    List<UserInfoModelResponse> userInfos = usersTeamsControllerService.showUsers(null, null, "1");
    assertThat(userInfos.size()).isEqualTo(20);

    userInfos = usersTeamsControllerService.showUsers(null, null, "2");
    assertThat(userInfos.size()).isEqualTo(10);

    userInfos = usersTeamsControllerService.showUsers(null, null, "3");
    assertThat(userInfos.size()).isEqualTo(0);
  }

  @Test
  public void getMyProfileInfoWithValidSwitchTeam() {
    String userTeamName = "Seahorse";
    String switchTeamName = "Octopus";
    getMyProfileInfoSetupTest(true, userTeamName, switchTeamName);

    UserInfoModelResponse userInfoModelResponse = usersTeamsControllerService.getMyProfileInfo();

    getMyProfileInfoVerifyUserInfoModelResponse(userInfoModelResponse, userTeamName);
    getMyProfileInfoVerifySwitchTeam(userInfoModelResponse, switchTeamName);
  }

  @Test
  public void getMyProfileInfoWithSwitchTeamDisabled() {
    String userTeamName = "Seahorse";
    getMyProfileInfoSetupTest(false, userTeamName, null);

    UserInfoModelResponse userInfoModelResponse = usersTeamsControllerService.getMyProfileInfo();

    getMyProfileInfoVerifyUserInfoModelResponse(userInfoModelResponse, userTeamName);
    assertThat(userInfoModelResponse.isSwitchTeams()).isFalse();
    assertThat(userInfoModelResponse.getSwitchAllowedTeamNames()).isNull();

    verify(manageDatabase, never())
        .getTeamNameFromTeamId(userInfo.getTenantId(), userInfo.getTeamId() - 1);
  }

  @Test
  public void getMyProfileInfoWithSwitchTeamExistsButUserNotMember() {
    String userTeamName = "Seahorse";
    String switchTeamName = "Octopus";
    getMyProfileInfoSetupTest(true, userTeamName, switchTeamName);

    when(manageDatabase.getTeamNameFromTeamId(userInfo.getTenantId(), Integer.MAX_VALUE))
        .thenReturn("User not member of this team");

    UserInfoModelResponse userInfoModelResponse = usersTeamsControllerService.getMyProfileInfo();

    getMyProfileInfoVerifyUserInfoModelResponse(userInfoModelResponse, userTeamName);
    getMyProfileInfoVerifySwitchTeam(userInfoModelResponse, switchTeamName);
  }

  @Test
  public void getMyProfileInfoWithSwitchToTeamThatDoesNotExist() {
    String userTeamName = "Seahorse";
    String userName = "testUser";
    userInfo.setSwitchAllowedTeamIds(Set.of(userInfo.getTeamId() - 1));
    userInfo.setSwitchTeams(true);

    when(mailService.getUserName(any())).thenReturn(userName);
    when(handleDbRequests.getUsersInfo(userName)).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(userName)).thenReturn(userInfo.getTenantId());
    when(manageDatabase.getTeamNameFromTeamId(userInfo.getTenantId(), userInfo.getTeamId()))
        .thenReturn("Seahorse");

    UserInfoModelResponse userInfoModelResponse = usersTeamsControllerService.getMyProfileInfo();

    getMyProfileInfoVerifyUserInfoModelResponse(userInfoModelResponse, userTeamName);
    getMyProfileInfoVerifySwitchTeam(userInfoModelResponse, null);
  }

  @Test
  public void addTwoDefaultTeamsSuccess() throws KlawException {
    String contactPerson = "contactPerson";

    addTwoDefaultTeamsSetupTest(
        "testuser",
        "existingTeamName",
        ApiResultStatus.SUCCESS.value,
        ApiResultStatus.SUCCESS.value);

    addTwoDefaultTeamsVerifyCapturedTeams(
        contactPerson,
        2,
        usersTeamsControllerService.addTwoDefaultTeams(contactPerson, "newTenant", TEST_TENANT_ID),
        "success",
        "success");
  }

  @Test
  public void addTwoDefaultTeamsWithApiFailureForFirstTeam() throws KlawException {
    String contactPerson = "contactPerson";

    addTwoDefaultTeamsSetupTest(
        "testuser",
        "existingTeamName",
        ApiResultStatus.FAILURE.value,
        ApiResultStatus.SUCCESS.value);

    addTwoDefaultTeamsVerifyCapturedTeams(
        contactPerson,
        1,
        usersTeamsControllerService.addTwoDefaultTeams(contactPerson, "newTenant", TEST_TENANT_ID),
        "failure",
        "success");
  }

  @Test
  public void addTwoDefaultTeamsWithApiFailureForSecondTeam() throws KlawException {
    String contactPerson = "contactPerson";

    addTwoDefaultTeamsSetupTest(
        "testuser",
        "existingTeamName",
        ApiResultStatus.SUCCESS.value,
        ApiResultStatus.FAILURE.value);

    addTwoDefaultTeamsVerifyCapturedTeams(
        contactPerson,
        1,
        usersTeamsControllerService.addTwoDefaultTeams(contactPerson, "newTenant", TEST_TENANT_ID),
        "success",
        "failure");
  }

  @Test
  public void updateUserTeamFromSwitchTeamsSuccess() {
    UserInfoModel userInfoModel = updateUserTeamFromSwitchTeamsSetupTest();

    when(handleDbRequests.updateUserTeam(userInfoModel.getUsername(), userInfoModel.getTeamId()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse apiResponse =
        usersTeamsControllerService.updateUserTeamFromSwitchTeams(userInfoModel);

    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(manageDatabase).loadUsersForAllTenants();
  }

  @Test
  public void updateUserTeamFromSwitchTeamsWithDBApiFailure() {
    UserInfoModel userInfoModel = updateUserTeamFromSwitchTeamsSetupTest();

    when(handleDbRequests.updateUserTeam(userInfoModel.getUsername(), userInfoModel.getTeamId()))
        .thenReturn(ApiResultStatus.FAILURE.value);

    ApiResponse apiResponse =
        usersTeamsControllerService.updateUserTeamFromSwitchTeams(userInfoModel);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);
    verify(manageDatabase, never()).loadUsersForAllTenants();
  }

  @Test
  public void updateUserTeamFromSwitchTeamsWithDBExceptionFailure() {
    UserInfoModel userInfoModel = updateUserTeamFromSwitchTeamsSetupTest();
    String errorMessage = "Failure Team doesn't exist";

    when(handleDbRequests.updateUserTeam(userInfoModel.getUsername(), userInfoModel.getTeamId()))
        .thenThrow(new RuntimeException(errorMessage));

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> usersTeamsControllerService.updateUserTeamFromSwitchTeams(userInfoModel))
        .withMessage(errorMessage);

    verify(manageDatabase, never()).loadUsersForAllTenants();
  }

  @ParameterizedTest
  @MethodSource
  public void updateUserTeamFromSwitchTeamsWithUnAuthorizedUserFailure(
      boolean setSwitchTeams, Set<Integer> teamIds, Integer userProfileTeamId) {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    userInfoModel.setTeamId(userProfileTeamId);

    userInfo.setSwitchTeams(setSwitchTeams);
    userInfo.setSwitchAllowedTeamIds(teamIds);

    when(handleDbRequests.getUsersInfo(userInfoModel.getUsername())).thenReturn(userInfo);

    ApiResponse apiResponse =
        usersTeamsControllerService.updateUserTeamFromSwitchTeams(userInfoModel);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.NOT_AUTHORIZED.getMessage());
    verify(handleDbRequests, never()).updateUserTeam(anyString(), anyInt());
  }

  public static Stream<Arguments> updateUserTeamFromSwitchTeamsWithUnAuthorizedUserFailure() {
    return Stream.of(
        Arguments.of(false, Set.of(1, 2), 1),
        Arguments.of(true, null, 1),
        Arguments.of(true, Collections.emptySet(), 1),
        Arguments.of(true, Set.of(1, 2), 3));
  }

  @ParameterizedTest
  @MethodSource
  public void getSwitchTeams(String userId, Set<Integer> teamIds, boolean setSwitchTeams) {
    String testUserName = "testUserName";

    userInfo.setSwitchTeams(setSwitchTeams);
    userInfo.setSwitchAllowedTeamIds(teamIds);

    when(handleDbRequests.getUsersInfo(userId)).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(testUserName)).thenReturn(TEST_TENANT_ID);
    for (Integer teamId : teamIds) {
      when(manageDatabase.getTeamNameFromTeamId(TEST_TENANT_ID, teamId))
          .thenReturn(String.format("team%d", teamId));
    }

    List<TeamModelResponse> responses = usersTeamsControllerService.getSwitchTeams(userId);

    if (userId.equals("testuser")) {
      assertThat(responses).isEmpty();
    } else {
      assertThat(responses).isNotEmpty();
      assertThat(responses.size()).isEqualTo(teamIds.size());

      responses.sort(Comparator.comparing(TeamModelResponse::getTeamId));

      assertThat(responses.get(0).getTeamId()).isEqualTo(1);
      assertThat(responses.get(0).getTeamname()).isEqualTo("team1");

      if (userId.equals("testuser2")) {
        assertThat(responses.get(1).getTeamId()).isEqualTo(2);
        assertThat(responses.get(1).getTeamname()).isEqualTo("team2");
      }
    }
  }

  public static Stream<Arguments> getSwitchTeams() {
    return Stream.of(
        Arguments.of("testuser", Collections.emptySet(), false),
        Arguments.of("testuser1", Set.of(1), true),
        Arguments.of("testuser2", Set.of(1, 2), true));
  }

  @ParameterizedTest
  @MethodSource
  public void registerUserInternal(
      String userName, String mailId, ApiResponse expectedResult, String responseMsg)
      throws KlawException {

    if (responseMsg.equals(TEAMS_ERR_115)) {
      UserInfo user = new UserInfo();
      user.setUsername(userName);
      user.setMailid(mailId);
      when(handleDbRequests.getAllUsersAllTenants()).thenReturn(List.of(user));
    } else if (responseMsg.equals(TEAMS_ERR_117)) {
      // Empty list no users currently exist
      when(handleDbRequests.getAllUsersAllTenants()).thenReturn(new ArrayList<>());
      RegisterUserInfo regUser = new RegisterUserInfo();
      regUser.setUsername(userName);
      regUser.setStatus(NewUserStatus.PENDING.value);
      regUser.setMailid(mailId);
      when(handleDbRequests.getAllRegisterUsersInformation()).thenReturn(List.of(regUser));
    } else {
      // Empty list no users currently exist
      when(handleDbRequests.getAllUsersAllTenants()).thenReturn(new ArrayList<>());
      when(handleDbRequests.getAllRegisterUsersInformation()).thenReturn(new ArrayList<>());
      when(handleDbRequests.registerUser(any())).thenReturn(responseMsg);
    }

    RegisterUserInfoModel model = new RegisterUserInfoModel();
    model.setTeam("Octopus");
    model.setRole("USER");
    model.setPwd("XXXXXXX");
    model.setFullname(userName);
    model.setUsername(userName);
    model.setMailid(mailId);

    ApiResponse result = usersTeamsControllerService.registerUser(model, false);
    assertThat(result.getMessage()).isEqualTo(expectedResult.getMessage());
    assertThat(result.isSuccess()).isEqualTo(expectedResult.isSuccess());
  }

  public static Stream<Arguments> registerUserInternal() {
    return Stream.of(
        Arguments.of(
            "Octopus",
            "octopus@klaw-project.io",
            ApiResponse.SUCCESS,
            ApiResultStatus.SUCCESS.value),
        Arguments.of(
            "octopus@klaw-project.io",
            "octopus@klaw-project.io",
            ApiResponse.FAILURE,
            ApiResultStatus.FAILURE.value),
        Arguments.of(
            "octopus@klaw-project.io",
            "octopus@klaw-project.io",
            ApiResponse.notOk(TEAMS_ERR_117),
            "Failure. Registration already exists"),
        Arguments.of(
            "Octopus2", "octopus@klaw-project.io", ApiResponse.notOk(TEAMS_ERR_117), TEAMS_ERR_117),
        Arguments.of(
            "Octopus3",
            "octopus@klaw-project.io",
            ApiResponse.notOk(TEAMS_ERR_115),
            TEAMS_ERR_115));
  }

  @ParameterizedTest
  @MethodSource
  public void getNewUserRequestsSuccess(int count) throws KlawNotAuthorizedException {
    int authenticatedUserTenantId = TEST_TENANT_ID;
    Map<Integer, String> tenantMapMock = utilMethods.getTenantMapMock();
    List<RegisterUserInfo> regUserList =
        utilMethods.getRegisterUserInfoList(tenantMapMock, count, "testUser");

    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME))
        .thenReturn(authenticatedUserTenantId);
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(handleDbRequests.getAllRegisterUsersInformation()).thenReturn(regUserList);
    when(manageDatabase.getTenantMap()).thenReturn(tenantMapMock);

    for (RegisterUserInfo regUser : regUserList) {
      when(manageDatabase.getTeamNameFromTeamId(authenticatedUserTenantId, regUser.getTeamId()))
          .thenReturn(String.format("%s%s", OCTOPUS, regUser.getTeamId()));
    }

    List<RegisterUserInfoModelResponse> responseList =
        usersTeamsControllerService.getNewUserRequests();

    assertThat(responseList.size()).isEqualTo(count);
    for (int i = 0; i < responseList.size(); i++) {
      assertThat(responseList.get(i).getUsername()).isEqualTo(regUserList.get(i).getUsername());
      assertThat(responseList.get(i).getMailid()).isEqualTo(regUserList.get(i).getMailid());
      assertThat(responseList.get(i).getTeam())
          .isEqualTo(String.format("%s%s", OCTOPUS, regUserList.get(i).getTeamId()));
      assertThat(responseList.get(i).getStatus()).isEqualTo(regUserList.get(i).getStatus());
      assertThat(responseList.get(i).getTenantId()).isEqualTo(regUserList.get(i).getTenantId());
      assertThat(responseList.get(i).getTenantName())
          .isEqualTo(tenantMapMock.get(regUserList.get(i).getTenantId()));
      assertThat(StringUtils.isBlank(responseList.get(i).getPwd())).isTrue();
    }
  }

  public static Stream<Arguments> getNewUserRequestsSuccess() {
    return Stream.of(Arguments.of(1), Arguments.of(2), Arguments.of(0));
  }

  @Test
  public void getNewUserRequestsWithNoRequestsInDBNullPointerException() {
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getAllRegisterUsersInformation()).thenReturn(null);

    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> usersTeamsControllerService.getNewUserRequests());

    verify(manageDatabase, never()).getTenantMap();
    verify(manageDatabase, never()).getTeamNameFromTeamId(anyInt(), anyInt());
  }

  @Test
  public void getNewUserRequestsWithUnAuthorizedUser() {
    assertThatExceptionOfType(KlawNotAuthorizedException.class)
        .isThrownBy(() -> usersTeamsControllerService.getNewUserRequests())
        .withMessage("You are not authorized to view this information.");

    verify(commonUtilsService, never()).getTenantId(anyString());
  }

  @ParameterizedTest
  @MethodSource
  public void approveNewUserRequestSuccess(AuthenticationType authType, boolean isExternal)
      throws KlawException {
    when(handleDbRequests.addNewUser(userInfoArgCaptor.capture()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    approveNewUserRequestsSetupTest(authType);

    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), isExternal, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isTrue();
    approveNewUserRequestsVerifyServiceInteractions(authType, isExternal, 1);
    approveNewUserRequestsValidateCapturedUserInfo(authType);
    if (authType == AuthenticationType.DATABASE) {
      approveNewUserRequestsValidateCapturedUserDetails();
    }
  }

  public static Stream<Arguments> approveNewUserRequestSuccess() {
    return Stream.of(
        Arguments.of(AuthenticationType.DATABASE, true),
        Arguments.of(AuthenticationType.DATABASE, false),
        Arguments.of(AuthenticationType.ACTIVE_DIRECTORY, true),
        Arguments.of(AuthenticationType.ACTIVE_DIRECTORY, false),
        Arguments.of(AuthenticationType.LDAP, false));
  }

  @Test
  public void approveNewUserRequestSuccessWithEmailAsUname() throws KlawException {
    testNewRegUser.setUsername("newuser@klaw.com");
    when(handleDbRequests.addNewUser(userInfoArgCaptor.capture()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    approveNewUserRequestsSetupTest(AuthenticationType.DATABASE);

    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isTrue();
    approveNewUserRequestsVerifyServiceInteractions(AuthenticationType.DATABASE, true, 1);
    approveNewUserRequestsValidateCapturedUserInfo(AuthenticationType.DATABASE);
    approveNewUserRequestsValidateCapturedUserDetails();
  }

  @Test
  public void approveNewUserRequestsSuccessWithTenantIDNotSet() throws KlawException {
    when(handleDbRequests.addNewUser(userInfoArgCaptor.capture()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    testNewRegUser.setTenantId(0);
    approveNewUserRequestsSetupTest(AuthenticationType.DATABASE);

    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isTrue();
    testNewRegUser.setTenantId(TEST_TENANT_ID);
    approveNewUserRequestsVerifyServiceInteractions(AuthenticationType.DATABASE, true, 2);
    approveNewUserRequestsValidateCapturedUserInfo(AuthenticationType.DATABASE);
    approveNewUserRequestsValidateCapturedUserDetails();
  }

  @Test
  public void approveNewUserRequestsFailureWithLDAPAuth() {
    when(handleDbRequests.addNewUser(userInfoArgCaptor.capture()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    approveNewUserRequestsSetupTest(AuthenticationType.LDAP);

    assertThatExceptionOfType(KlawException.class)
        .isThrownBy(
            () ->
                usersTeamsControllerService.approveNewUserRequests(
                    testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null))
        .withMessage(TEAMS_ERR_111);

    verify(commonUtilsService)
        .updateMetadata(
            TEST_TENANT_ID,
            EntityType.USERS,
            MetadataOperationType.CREATE,
            testNewRegUser.getUsername());
    verify(commonUtilsService).getTenantId(TEST_AUTHENTICATED_USER_UNAME);
    verify(inMemoryUserDetailsManager, never()).createUser(any());
    verify(handleDbRequests, never()).updateNewUserRequest(anyString(), anyString(), anyBoolean());
    verify(mailService, never()).sendMail(anyString(), anyString(), any(), anyString());

    approveNewUserRequestsValidateCapturedUserInfo(AuthenticationType.LDAP);
  }

  @Test
  public void approveNewUserRequestsFailureWithUnAuthorizedUser() throws KlawException {
    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getMessage()).isEqualTo(ApiResponse.NOT_AUTHORIZED.getMessage());
    verifyNoInteractions(handleDbRequests);
    verifyNoInteractions(inMemoryUserDetailsManager);
    verify(commonUtilsService, never()).getTenantId(anyString());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
    verify(commonUtilsService, never()).getLoginUrl();
    verify(mailService, never()).sendMail(anyString(), anyString(), any(), anyString());
  }

  @Test
  public void approveNewUserRequestWithDBApiFailure() throws KlawException {
    when(handleDbRequests.addNewUser(userInfoArgCaptor.capture()))
        .thenReturn(ApiResultStatus.FAILURE.value);
    approveNewUserRequestsSetupTest(AuthenticationType.DATABASE);

    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);

    verify(inMemoryUserDetailsManager).createUser(userDetailsArgCaptor.capture());
    verify(mailService)
        .sendMail(
            testNewRegUser.getUsername(),
            TEST_NEW_USER_PWD_PLAIN_TEXT,
            handleDbRequests,
            TEST_LOGIN_URL);
    verify(commonUtilsService, never()).getTenantId(anyString());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
    verify(handleDbRequests, never()).updateNewUserRequest(anyString(), anyString(), anyBoolean());
    verify(inMemoryUserDetailsManager, never()).deleteUser(anyString());

    approveNewUserRequestsValidateCapturedUserInfo(AuthenticationType.DATABASE);
    approveNewUserRequestsValidateCapturedUserDetails();
  }

  @Test
  public void approveNewUserRequestWithDBExceptionFailure() throws KlawException {
    when(handleDbRequests.addNewUser(userInfoArgCaptor.capture()))
        .thenThrow(new RuntimeException("This User should not exist"));
    approveNewUserRequestsSetupTest(AuthenticationType.DATABASE);

    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getMessage()).isEqualTo(ApiResponse.FAILURE.getMessage());

    verify(inMemoryUserDetailsManager).createUser(userDetailsArgCaptor.capture());
    verify(inMemoryUserDetailsManager).deleteUser(testNewRegUser.getUsername());
    verify(mailService, never()).sendMail(anyString(), anyString(), any(), anyString());
    verify(commonUtilsService, never()).getTenantId(anyString());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
    verify(handleDbRequests, never()).updateNewUserRequest(anyString(), anyString(), anyBoolean());

    approveNewUserRequestsValidateCapturedUserInfo(AuthenticationType.DATABASE);
    approveNewUserRequestsValidateCapturedUserDetails();
  }

  @ParameterizedTest
  @MethodSource
  public void approveNewUserRequestWithInvalidUnameFailure(String invalidUname)
      throws KlawException {
    testNewRegUser.setUsername(invalidUname);
    approveNewUserRequestsSetupTest(AuthenticationType.DATABASE);

    ApiResponse response =
        usersTeamsControllerService.approveNewUserRequests(
            testNewRegUser.getUsername(), true, Integer.MIN_VALUE, null);

    assertThat(response.isSuccess()).isFalse();
    verify(commonUtilsService, never()).getTenantId(anyString());
    verify(inMemoryUserDetailsManager, never()).createUser(any());
    verify(mailService, never()).sendMail(anyString(), anyString(), any(), anyString());
    verify(handleDbRequests, never()).addNewUser(any());
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
    verify(inMemoryUserDetailsManager, never()).deleteUser(anyString());
    verify(commonUtilsService, never()).getLoginUrl();
    verify(handleDbRequests, never()).updateNewUserRequest(anyString(), anyString(), anyBoolean());
  }

  public static Stream<Arguments> approveNewUserRequestWithInvalidUnameFailure() {
    return Stream.of(Arguments.of("ss"), Arguments.of("sdflk&&klsjdf"));
  }

  @Test
  public void declineNewUserRequests() throws KlawException {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);

    ApiResponse response = usersTeamsControllerService.declineNewUserRequests(TEST_NEW_USER_UNAME);

    assertThat(response.isSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
    verify(handleDbRequests)
        .updateNewUserRequest(TEST_NEW_USER_UNAME, TEST_AUTHENTICATED_USER_UNAME, false);
  }

  @Test
  public void declineNewUserRequestsDBFailure() {
    String errMsg = "DB Error";
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    doThrow(new RuntimeException(errMsg))
        .when(handleDbRequests)
        .updateNewUserRequest(TEST_NEW_USER_UNAME, TEST_AUTHENTICATED_USER_UNAME, false);

    assertThatExceptionOfType(KlawException.class)
        .isThrownBy(() -> usersTeamsControllerService.declineNewUserRequests(TEST_NEW_USER_UNAME))
        .withMessage(errMsg);
  }

  @Test
  public void declineNewUserRequestsAuthenticationFailure() throws KlawException {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);

    ApiResponse response = usersTeamsControllerService.declineNewUserRequests(TEST_NEW_USER_UNAME);

    assertThat(response.isSuccess()).isFalse();
    assertThat(response.getMessage()).isEqualTo(ApiResponse.NOT_AUTHORIZED.getMessage());
    verify(handleDbRequests, never()).updateNewUserRequest(anyString(), anyString(), anyBoolean());
  }

  @Test
  public void getRegistrationInfoFromId() {
    String testRegId = "testRegId";
    String testStatus = "status";
    when(handleDbRequests.getRegistrationDetails(testRegId, testStatus)).thenReturn(testNewRegUser);

    RegisterUserInfoModelResponse response =
        usersTeamsControllerService.getRegistrationInfoFromId(testRegId, testStatus);

    assertThat(response.getUsername()).isEqualTo(testNewRegUser.getUsername());
    assertThat(response.getMailid()).isEqualTo(testNewRegUser.getMailid());
    assertThat(response.getTeam()).isEqualTo(testNewRegUser.getTeam());
    assertThat(response.getStatus()).isEqualTo(testNewRegUser.getStatus());
    assertThat(response.getTenantId()).isEqualTo(testNewRegUser.getTenantId());
    assertThat(response.getFullname()).isEqualTo(testNewRegUser.getFullname());
    assertThat(response.getRole()).isEqualTo(testNewRegUser.getRole());
    assertThat(response.getPwd()).isEqualTo(testNewRegUser.getPwd());
  }

  @Test
  public void getRegistrationInfoFromIdNoRegistrationInfo() {
    String testRegId = "testRegId";
    String testStatus = "status";
    when(handleDbRequests.getRegistrationDetails(testRegId, testStatus)).thenReturn(null);

    RegisterUserInfoModelResponse response =
        usersTeamsControllerService.getRegistrationInfoFromId(testRegId, testStatus);

    assertThat(response).isNull();
  }

  public UserDetails userDetails(String username, String password) {

    return new UserDetails() {
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
      }

      @Override
      public String getPassword() {
        return password;
      }

      @Override
      public String getUsername() {
        return username;
      }

      @Override
      public boolean isAccountNonExpired() {
        return false;
      }

      @Override
      public boolean isAccountNonLocked() {
        return false;
      }

      @Override
      public boolean isCredentialsNonExpired() {
        return false;
      }

      @Override
      public boolean isEnabled() {
        return false;
      }
    };
  }

  public UserInfo generateUser(String username) {
    UserInfo info = new UserInfo();
    info.setUsername(username);
    info.setMailid(username);
    info.setTenantId(101);
    info.setTeamId(10);
    info.setRole("User");
    return info;
  }

  private BasicTextEncryptor getJasyptEncryptor() {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPasswordCharArray(ENCRYPTOR_SECRET_KEY.toCharArray());

    return textEncryptor;
  }

  private String encodePwd(String pwd) {
    return getJasyptEncryptor().encrypt(pwd);
  }

  private String decodePwd(String pwd) {
    if (pwd != null) {
      return getJasyptEncryptor().decrypt(pwd);
    } else {
      return "";
    }
  }

  private void getTeamDetailsSetupTest(
      Map<Integer, String> tenantMapMock, Team teamDaoMock, String testUserName) {
    when(manageDatabase.getTenantMap()).thenReturn(tenantMapMock);
    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_ID)).thenReturn(teamDaoMock);
  }

  private void getTeamDetailsVerifyResponse(
      TeamModelResponse response, Team teamDaoMock, Map<Integer, String> tenantMapMock) {
    assertThat(response.getTeamname()).isEqualTo(teamDaoMock.getTeamname());
    assertThat(response.getTeamphone()).isEqualTo(teamDaoMock.getTeamphone());
    assertThat(response.getContactperson()).isEqualTo(teamDaoMock.getContactperson());
    assertThat(response.getTeamId()).isEqualTo(teamDaoMock.getTeamId());
    assertThat(response.getTenantId())
        .isEqualTo(
            tenantMapMock.entrySet().stream()
                .filter(obj -> Objects.equals(obj.getValue(), TEST_TENANT_NAME))
                .findFirst()
                .get()
                .getKey());
    assertThat(response.getTenantName()).isEqualTo(TEST_TENANT_NAME);
    assertThat(response.getTeammail()).isEqualTo(teamDaoMock.getTeammail());
    assertThat(response.getApp()).isEqualTo(teamDaoMock.getApp());
    assertThat(response.getServiceAccounts()).isEqualTo(teamDaoMock.getServiceAccounts());
  }

  private void addTwoDefaultTeamsSetupTest(
      String userName, String existingTeamName, String firstApiResult, String secondApiResult) {
    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(manageDatabase.getTeamNamesForTenant(TEST_TENANT_ID))
        .thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture()))
        .thenReturn(firstApiResult)
        .thenReturn(secondApiResult);
  }

  private void addTwoDefaultTeamsVerifyCapturedTeams(
      String contactPerson,
      int expectedUpdateMetaDataInvocations,
      Map<String, String> teamMap,
      String team1Result,
      String team2Result) {
    List<Team> capturedTeams = teamCaptor.getAllValues();

    assertThat(capturedTeams.size()).isEqualTo(2);
    assertThat(capturedTeams.get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
    assertThat(capturedTeams.get(0).getTeamname()).isEqualTo(KwConstants.INFRATEAM);
    assertThat(capturedTeams.get(0).getContactperson()).isEqualTo(contactPerson);
    assertThat(capturedTeams.get(1).getTenantId()).isEqualTo(TEST_TENANT_ID);
    assertThat(capturedTeams.get(1).getTeamname()).isEqualTo(KwConstants.STAGINGTEAM);
    assertThat(capturedTeams.get(1).getContactperson()).isEqualTo(contactPerson);

    assertThat(teamMap.size()).isEqualTo(2);
    assertThat(teamMap.get("team1result")).isEqualTo(team1Result);
    assertThat(teamMap.get("team2result")).isEqualTo(team2Result);

    verify(commonUtilsService, times(expectedUpdateMetaDataInvocations))
        .updateMetadata(TEST_TENANT_ID, EntityType.TEAM, MetadataOperationType.CREATE, null);
  }

  private void addNewTeamSetupTest(String userName, String existingTeamName) {
    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_TEAMS))
        .thenReturn(false);
    when(manageDatabase.getTeamNamesForTenant(TEST_TENANT_ID))
        .thenReturn(List.of(existingTeamName));
  }

  private void addNewTeamVerifyCapturedTeam(TeamModel teamModel) {
    Team capturedTeam = teamCaptor.getValue();

    assertThat(capturedTeam.getRequestTopicsEnvs())
        .isEqualTo(String.join(",", teamModel.getEnvList().toArray(new String[0])));
    assertThat(capturedTeam.getTeamname()).isEqualTo(teamModel.getTeamname());
    assertThat(capturedTeam.getTeamphone()).isEqualTo(teamModel.getTeamphone());
    assertThat(capturedTeam.getContactperson()).isEqualTo(teamModel.getContactperson());
    assertThat(capturedTeam.getTeamId()).isEqualTo(teamModel.getTeamId());
    assertThat(capturedTeam.getTenantId()).isEqualTo(TEST_TENANT_ID);
  }

  private List<UserInfo> showUsersSetupTest(
      String userName,
      int totalMocks,
      int switchTeam1ID,
      String switchTeam1Name,
      int switchTeam2ID,
      String switchTeam2Name) {
    List<UserInfo> userInfosMock = utilMethods.getUserInfoList(totalMocks, userName);

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(userName)).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getAllUsersInfo(TEST_TENANT_ID)).thenReturn(userInfosMock);
    when(manageDatabase.getTeamNameFromTeamId(TEST_TENANT_ID, switchTeam1ID))
        .thenReturn(switchTeam1Name);
    when(manageDatabase.getTeamNameFromTeamId(TEST_TENANT_ID, switchTeam2ID))
        .thenReturn(switchTeam2Name);

    return userInfosMock;
  }

  private void showUsersValidateUserInfo(
      UserInfoModelResponse userInfoResponse, int teamId, String switchTeamName) {
    assertThat(userInfoResponse.getTeamId()).isEqualTo(teamId);
    assertThat(userInfoResponse.getSwitchAllowedTeamNames().size()).isEqualTo(1);
    assertThat(userInfoResponse.getSwitchAllowedTeamNames().contains(switchTeamName)).isTrue();
  }

  private UserInfoModel updateUserTeamFromSwitchTeamsSetupTest() {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();

    userInfo.setSwitchTeams(true);
    userInfo.setSwitchAllowedTeamIds(Set.of(userInfoModel.getTeamId()));

    when(handleDbRequests.getUsersInfo(userInfoModel.getUsername())).thenReturn(userInfo);

    return userInfoModel;
  }

  private void getMyProfileInfoSetupTest(
      boolean switchTeamsEnabled, String userTeamName, String switchTeamName) {
    userInfo.setSwitchAllowedTeamIds(Set.of(userInfo.getTeamId() - 1));
    userInfo.setSwitchTeams(switchTeamsEnabled);

    when(mailService.getUserName(any())).thenReturn("testuser");
    when(handleDbRequests.getUsersInfo("testuser")).thenReturn(userInfo);
    when(commonUtilsService.getTenantId("testuser")).thenReturn(userInfo.getTenantId());
    when(manageDatabase.getTeamNameFromTeamId(userInfo.getTenantId(), userInfo.getTeamId()))
        .thenReturn("Seahorse");
    if (switchTeamsEnabled) {
      when(manageDatabase.getTeamNameFromTeamId(userInfo.getTenantId(), userInfo.getTeamId() - 1))
          .thenReturn("Octopus");
    }
  }

  private void getMyProfileInfoVerifyUserInfoModelResponse(
      UserInfoModelResponse userInfoModelResponse, String userTeamName) {
    assertThat(userInfoModelResponse.getTeamId()).isEqualTo(userInfo.getTeamId());
    assertThat(userInfoModelResponse.getRole()).isEqualTo(userInfo.getRole());
    assertThat(userInfoModelResponse.getTenantId()).isEqualTo(userInfo.getTenantId());
    assertThat(userInfoModelResponse.getUsername()).isEqualTo(userInfo.getUsername());
    assertThat(userInfoModelResponse.getTeam()).isEqualTo(userTeamName);
  }

  private void getMyProfileInfoVerifySwitchTeam(
      UserInfoModelResponse userInfoModelResponse, String switchTeamName) {
    assertThat(userInfoModelResponse.isSwitchTeams()).isTrue();
    assertThat(userInfoModelResponse.getSwitchAllowedTeamNames().size()).isEqualTo(1);
    assertThat(userInfoModelResponse.getSwitchAllowedTeamNames().contains(switchTeamName)).isTrue();
  }

  private void approveNewUserRequestsSetupTest(AuthenticationType authType) {
    ReflectionTestUtils.setField(usersTeamsControllerService, "authenticationType", authType.value);
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(handleDbRequests.getPendingRegisterUsersInfo(testNewRegUser.getUsername()))
        .thenReturn(testNewRegUser);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(TEST_TENANT_ID);
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_USERS))
        .thenReturn(false);
    when(commonUtilsService.getLoginUrl()).thenReturn(TEST_LOGIN_URL);
  }

  private void approveNewUserRequestsVerifyServiceInteractions(
      AuthenticationType authType, boolean isExternal, int getTenantIdCallCount) {
    String sentMailPwd;
    if (authType == AuthenticationType.DATABASE) {
      verify(inMemoryUserDetailsManager).createUser(userDetailsArgCaptor.capture());
      sentMailPwd = TEST_NEW_USER_PWD_PLAIN_TEXT;
    } else {
      verify(inMemoryUserDetailsManager, never()).createUser(any());
      sentMailPwd = UsersTeamsControllerService.UNUSED_PASSWD;
    }
    verify(handleDbRequests)
        .updateNewUserRequest(testNewRegUser.getUsername(), TEST_AUTHENTICATED_USER_UNAME, true);
    verify(commonUtilsService)
        .updateMetadata(
            TEST_TENANT_ID,
            EntityType.USERS,
            MetadataOperationType.CREATE,
            testNewRegUser.getUsername());
    verify(commonUtilsService, times(getTenantIdCallCount))
        .getTenantId(TEST_AUTHENTICATED_USER_UNAME);
    verify(inMemoryUserDetailsManager, never()).deleteUser(anyString());
    if (isExternal) {
      verify(mailService)
          .sendMail(testNewRegUser.getUsername(), sentMailPwd, handleDbRequests, TEST_LOGIN_URL);
    } else {
      verify(commonUtilsService, never()).isNotAuthorizedUser(any(), any(PermissionType.class));
      verify(commonUtilsService, never()).getLoginUrl();
      verify(mailService, never()).sendMail(anyString(), anyString(), any(), anyString());
    }
  }

  private void approveNewUserRequestsValidateCapturedUserInfo(AuthenticationType authType) {
    UserInfo capturedUserInfo = userInfoArgCaptor.getValue();
    assertThat(capturedUserInfo.getUsername()).isEqualTo(testNewRegUser.getUsername());
    assertThat(capturedUserInfo.getMailid()).isEqualTo(testNewRegUser.getMailid());
    assertThat(capturedUserInfo.getTenantId()).isEqualTo(testNewRegUser.getTenantId());
    assertThat(capturedUserInfo.getTeamId()).isEqualTo(testNewRegUser.getTeamId());
    assertThat(capturedUserInfo.getRole()).isEqualTo(testNewRegUser.getRole());
    assertThat(capturedUserInfo.getFullname()).isEqualTo(testNewRegUser.getFullname());
    if (authType == AuthenticationType.DATABASE) {
      assertThat(decodePwd(capturedUserInfo.getPwd())).isEqualTo(TEST_NEW_USER_PWD_PLAIN_TEXT);
    } else {
      assertThat(capturedUserInfo.getPwd()).isEqualTo(UsersTeamsControllerService.UNUSED_PASSWD);
    }
  }

  private void approveNewUserRequestsValidateCapturedUserDetails() {
    UserDetails capturedUserDetails = userDetailsArgCaptor.getValue();
    assertThat(capturedUserDetails.getUsername()).isEqualTo(testNewRegUser.getUsername());
    assertThat(capturedUserDetails.getAuthorities().size()).isEqualTo(1);
    assertThat(capturedUserDetails.getAuthorities().iterator().next().getAuthority())
        .isEqualTo(String.format("ROLE_%s", testNewRegUser.getRole()));
    assertThat(
            PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .matches(TEST_NEW_USER_PWD_PLAIN_TEXT, capturedUserDetails.getPassword()))
        .isTrue();
  }

  private TeamModel updateTeamSetupTest(String teamName, ApiResultStatus dbResultStatus) {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(TEST_TENANT_ID);
    when(manageDatabase.getTeamObjForTenant(TEST_TENANT_ID)).thenReturn(utilMethods.getTeams());
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_TEAMS))
        .thenReturn(false);
    if (dbResultStatus != null) {
      when(handleDbRequests.updateTeam(teamCaptor.capture())).thenReturn(dbResultStatus.value);
    } else {
      doThrow(new RuntimeException("DB Error")).when(handleDbRequests).updateTeam(any());
    }
    TeamModel teamModel = utilMethods.getTeamModelMock();
    teamModel.setTeamname(teamName);
    return teamModel;
  }

  private void updateTeamVerifySuccess(
      ApiResponse apiResponse, TeamModel requestModel, boolean withEnvList) {
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
    assertThat(apiResponse.isSuccess()).isTrue();
    verify(commonUtilsService)
        .updateMetadata(TEST_TENANT_ID, EntityType.TEAM, MetadataOperationType.UPDATE, null);
    updateTeamVerifyCapturedTeam(requestModel, withEnvList);
  }

  private void updateTeamVerifyCapturedTeam(TeamModel requestModel, boolean withEnvList) {
    Team team = teamCaptor.getValue();
    assertThat(team.getTeamname()).isEqualTo(requestModel.getTeamname());
    assertThat(team.getContactperson()).isEqualTo(requestModel.getContactperson());
    assertThat(team.getTeamphone()).isEqualTo(requestModel.getTeamphone());
    assertThat(team.getTenantId()).isEqualTo(requestModel.getTenantId());
    assertThat(team.getTeamId()).isEqualTo(requestModel.getTeamId());

    if (withEnvList) {
      assertThat(team.getRequestTopicsEnvs())
          .isEqualTo(String.join(",", requestModel.getEnvList().toArray(new String[0])));
    } else {
      assertThat(team.getRequestTopicsEnvs()).isNull();
    }
  }

  private void changePwdEncodedParams(
      UserDetails updatePwdUserDetails, ChangePasswordRequestModel changePwdRequestModel) {
    verify(inMemoryUserDetailsManager)
        .updatePassword(userDetailsArgCaptor.capture(), stringArgCaptor.capture());
    verify(handleDbRequests)
        .updatePassword(eq(TEST_AUTHENTICATED_USER_UNAME), stringArgCaptor.capture());

    assertThat(userDetailsArgCaptor.getValue()).isSameAs(updatePwdUserDetails);

    assertThat(
            PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .matches(changePwdRequestModel.getPwd(), stringArgCaptor.getAllValues().get(0)))
        .isTrue();
    assertThat(decodePwd(stringArgCaptor.getAllValues().get(1)))
        .isEqualTo(changePwdRequestModel.getPwd());
  }

  private void changePwdSetupTest(UserDetails updatePwdUserDetails, ApiResultStatus dbApiResult) {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(inMemoryUserDetailsManager.loadUserByUsername(TEST_AUTHENTICATED_USER_UNAME))
        .thenReturn(updatePwdUserDetails);
    if (dbApiResult != null) {
      when(handleDbRequests.updatePassword(eq(TEST_AUTHENTICATED_USER_UNAME), anyString()))
          .thenReturn(dbApiResult.value);
    } else {
      when(handleDbRequests.updatePassword(eq(TEST_AUTHENTICATED_USER_UNAME), anyString()))
          .thenThrow(new RuntimeException("DB Error"));
    }
  }

  private void changePwdVerifyNoInteractionsWithAnyServices() {
    verifyNoInteractions(inMemoryUserDetailsManager);
    verifyNoInteractions(manageDatabase);
    verifyNoInteractions(handleDbRequests);
    verifyNoInteractions(mailService);
  }

  private void getAllTeamsSUFromRegisteredUsersSetupTest(
      List<Team> teams, List<Env> envs, Map<Integer, String> tenantMap) {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getAllTeams(TEST_TENANT_ID)).thenReturn(teams);
    when(manageDatabase.getTenantMap()).thenReturn(tenantMap);
    when(manageDatabase.getKafkaEnvList(TEST_TENANT_ID)).thenReturn(envs);
  }

  private void getAllTeamsSUFromRegisteredUsersVerifyResponse(
      TeamModelResponse teamModelResponse, Team team, List<Env> envs) {
    assertThat(teamModelResponse.getTeamname()).isEqualTo(team.getTeamname());
    assertThat(teamModelResponse.getTeamId()).isEqualTo(team.getTeamId());
    assertThat(teamModelResponse.getContactperson()).isEqualTo(team.getContactperson());
    assertThat(teamModelResponse.getTenantId()).isEqualTo(team.getTenantId());
    assertThat(teamModelResponse.getTeamphone()).isEqualTo(team.getTeamphone());
    assertThat(teamModelResponse.getTeammail()).isEqualTo(team.getTeammail());
    assertThat(teamModelResponse.getApp()).isEqualTo(team.getApp());
    assertThat(teamModelResponse.getServiceAccounts()).isEqualTo(team.getServiceAccounts());

    if (envs != null) {
      assertThat(teamModelResponse.getEnvList().size()).isEqualTo(envs.size());
      for (int i = 0; i < teamModelResponse.getEnvList().size(); i++) {
        assertThat(teamModelResponse.getEnvList().get(i)).isEqualTo(envs.get(i).getName());
      }
    } else {
      assertThat(teamModelResponse.getEnvList().size()).isEqualTo(1);
      assertThat(teamModelResponse.getEnvList().get(0)).isEqualTo("ALL");
    }
  }

  private void getAllTeamsSUSetupTest(
      List<Team> teamList,
      List<UserInfo> userInfoList,
      Map<Integer, String> tenantMap,
      boolean userUnAuthorized) {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(TEST_TENANT_ID);
    when(manageDatabase.getTeamObjForTenant(TEST_TENANT_ID)).thenReturn(teamList);
    when(commonUtilsService.isNotAuthorizedUser(
            TEST_AUTHENTICATED_USER_UNAME, PermissionType.ADD_EDIT_DELETE_TEAMS))
        .thenReturn(userUnAuthorized);
    when(handleDbRequests.getAllUsersInfo(TEST_TENANT_ID)).thenReturn(userInfoList);
    when(manageDatabase.getTenantMap()).thenReturn(tenantMap);
  }

  private void getAllTeamSUVerifyResponse(
      List<TeamModelResponse> teamModelResponses,
      List<Team> teams,
      Map<Integer, String> tenantMap) {
    for (int i = 0; i < teamModelResponses.size(); i++) {
      TeamModelResponse teamModelResponse = teamModelResponses.get(i);
      Team team = teams.get(i);
      assertThat(teamModelResponse.getTeamId()).isEqualTo(team.getTeamId());
      assertThat(teamModelResponse.getTeamname()).isEqualTo(team.getTeamname());
      assertThat(teamModelResponse.getTeamphone()).isEqualTo(team.getTeamphone());
      assertThat(teamModelResponse.getContactperson()).isEqualTo(team.getContactperson());
      assertThat(teamModelResponse.getTenantId()).isEqualTo(team.getTenantId());
      assertThat(teamModelResponse.getTenantName()).isEqualTo(tenantMap.get(team.getTenantId()));
      assertThat(teamModelResponse.getTeammail()).isEqualTo(team.getTeammail());
      assertThat(teamModelResponse.getServiceAccounts()).isEqualTo(team.getServiceAccounts());
    }
  }

  private void deleteTeamSetup(
      ApiResultStatus dbApiResult,
      boolean unAuthorizedUser,
      boolean userInfoForTeamExists,
      boolean componentsCountForTeamExists,
      boolean dbFailureException,
      int userTeamId) {
    when(mailService.getUserName(userDetails)).thenReturn(TEST_AUTHENTICATED_USER_UNAME);
    when(commonUtilsService.isNotAuthorizedUser(userDetails, PermissionType.ADD_EDIT_DELETE_TEAMS))
        .thenReturn(unAuthorizedUser);
    when(commonUtilsService.getTenantId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.existsUsersInfoForTeam(TEST_TEAM_ID, TEST_TENANT_ID))
        .thenReturn(userInfoForTeamExists);
    when(handleDbRequests.existsComponentsCountForTeam(TEST_TEAM_ID, TEST_TENANT_ID))
        .thenReturn(componentsCountForTeamExists);
    when(commonUtilsService.getTeamId(TEST_AUTHENTICATED_USER_UNAME)).thenReturn(userTeamId);
    if (dbFailureException) {
      doThrow(new RuntimeException("Db exception"))
          .when(handleDbRequests)
          .deleteTeamRequest(TEST_TEAM_ID, TEST_TENANT_ID);
    } else {
      when(handleDbRequests.deleteTeamRequest(TEST_TEAM_ID, TEST_TENANT_ID))
          .thenReturn(dbApiResult != null ? dbApiResult.value : null);
    }
  }
}
