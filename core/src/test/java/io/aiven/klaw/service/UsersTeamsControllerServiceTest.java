package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_109;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_115;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_117;
import static io.aiven.klaw.error.KlawErrorMessages.TEAMS_ERR_119;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.NewUserStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.requests.ProfileModel;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import io.aiven.klaw.model.requests.TeamModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.ResetPasswordInfo;
import io.aiven.klaw.model.response.TeamModelResponse;
import io.aiven.klaw.model.response.UserInfoModelResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
  private UtilMethods utilMethods;

  @Mock InMemoryUserDetailsManager inMemoryUserDetailsManager;
  @Mock private MailUtils mailService;

  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private UserDetails userDetails;
  @Mock private ManageDatabase manageDatabase;

  private UsersTeamsControllerService usersTeamsControllerService;
  private UserInfo userInfo;
  private ArgumentCaptor<Team> teamCaptor;

  @BeforeEach
  void setUp() {
    utilMethods = new UtilMethods();
    usersTeamsControllerService = new UsersTeamsControllerService();
    teamCaptor = ArgumentCaptor.forClass(Team.class);
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "inMemoryUserDetailsManager", inMemoryUserDetailsManager);
    ReflectionTestUtils.setField(usersTeamsControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(usersTeamsControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "encryptorSecretKey", "Testsecretkey");
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    userInfo = utilMethods.getUserInfoMockDao();
    loginMock();
  }

  @Test
  void getUserInfoDetails() {
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
  void updateProfile() throws KlawException {
    ProfileModel userInfoModel = utilMethods.getUserInfoToUpdateMock();
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    when(handleDbRequests.updateUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.updateProfile(userInfoModel);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void updateProfileFailureDbUpdate() {
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
  void updateUserNotAuthorized() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    ApiResponse apiResponse = usersTeamsControllerService.updateUser(userInfoModel);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  void updateUserNotAuthorizedToUpdateSuperAdmin() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
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
  void getTeamDetails() {
    final String testUserName = "testUserName";
    Team teamDaoMock = utilMethods.getTeamDaoMock();
    Map<Integer, String> tenantMapMock = utilMethods.getTenantMapMock();

    when(manageDatabase.getTenantMap()).thenReturn(tenantMapMock);
    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_ID)).thenReturn(teamDaoMock);

    TeamModelResponse response = usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME);

    getTeamDetailsVerifyResponse(response, teamDaoMock, tenantMapMock);
    assertThat(response.getEnvList()).isEqualTo(List.of(teamDaoMock.getRequestTopicsEnvs().split("\\s*,\\s*")));
  }

  @Test
  void getTeamDetailsNoRequestTopicsEnvs() {
    final String testUserName = "testUserName";
    Team teamDaoMock = utilMethods.getTeamDaoMock();
    teamDaoMock.setRequestTopicsEnvs(null);
    Map<Integer, String> tenantMapMock = utilMethods.getTenantMapMock();

    when(manageDatabase.getTenantMap()).thenReturn(tenantMapMock);
    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_ID)).thenReturn(teamDaoMock);

    TeamModelResponse response = usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME);

    getTeamDetailsVerifyResponse(response, teamDaoMock, tenantMapMock);
    assertThat(response.getEnvList()).isNull();
  }

  @Test
  void getTeamDetailsTeamDoesNotExist() {
    final String testUserName = "testUserName";
    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_ID)).thenReturn(null);

    TeamModelResponse response = usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME);

    assertThat(response).isNull();
  }

  @Test
  void getTeamDetailsInvalidTenantName() {
    final String testUserName = "testUserName";
    Team teamDaoMock = utilMethods.getTeamDaoMock();

    when(mailService.getUserName(any())).thenReturn(testUserName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(handleDbRequests.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_ID)).thenReturn(teamDaoMock);
    when(manageDatabase.getTenantMap()).thenReturn(Collections.emptyMap());

    assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> usersTeamsControllerService.getTeamDetails(TEST_TEAM_ID, TEST_TENANT_NAME))
            .withMessage("No value present");
  }

  @Test
  void resetPassword() {}

  @Test
  void getAllTeamsSUFromRegisterUsers() {}

  @Test
  void getAllTeamsSU() {}

  @Test
  void getAllTeamsSUOnly() {
    int tenantId = 101;
    int teamId = 101;
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(commonUtilsService.getTenantId(anyString())).thenReturn(tenantId);
    when(manageDatabase.getTeamObjForTenant(tenantId)).thenReturn(utilMethods.getTeams());
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequests.existsComponentsCountForTeam(teamId, tenantId)).thenReturn(false);
    when(handleDbRequests.existsUsersInfoForTeam(teamId, tenantId)).thenReturn(false);
    List<TeamModelResponse> teams = usersTeamsControllerService.getAllTeamsSU();
    assertThat(teams.get(0).isShowDeleteTeam()).isTrue();

    when(handleDbRequests.existsComponentsCountForTeam(teamId, tenantId)).thenReturn(true);
    teams = usersTeamsControllerService.getAllTeamsSU();
    assertThat(teams.get(0).isShowDeleteTeam()).isFalse();
  }

  @Test
  void deleteTeamFailure() throws KlawException {
    int teamId = 101;
    int tenantId = 101;
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(tenantId);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    when(handleDbRequests.existsUsersInfoForTeam(teamId, tenantId)).thenReturn(true);
    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(teamId);
    assertThat(apiResponse.getMessage())
        .isEqualTo("Not allowed to delete this team, as there are associated users.");
  }

  @Test
  void deleteUserFailureHasRequests() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt())).thenReturn(new HashMap<>());
    when(handleDbRequests.existsComponentsCountForUser("testuser", 101)).thenReturn(true);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage())
        .isEqualTo(
            "Not allowed to delete this user, as there are associated requests in the metadata.");
  }

  @Test
  void deleteUserFailureisAdmin() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    when(handleDbRequests.deleteUserRequest(anyString())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void deleteUserSuccessNormalUser() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForNormalUser());
    when(handleDbRequests.deleteUserRequest(anyString())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void deleteUserFailureNoSuperUserPermission() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class)))
        .thenReturn(false, true);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_106);
  }

  @Test
  void deleteUserFailureNoDeletionPermission() throws KlawException {
    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMapForSuperuser());
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.NOT_AUTHORIZED.getMessage());
  }

  @Test
  void addNewUserAlphaNumeric() throws KlawException {
    UserInfoModel newUser = utilMethods.getUserInfoMock();
    when(handleDbRequests.addNewUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.addNewUser(newUser, false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
  }

  @Test
  void addNewUserEmail() throws KlawException {
    UserInfoModel newUser = utilMethods.getUserInfoMock();
    newUser.setUsername("test@test.com"); // email pattern
    when(handleDbRequests.addNewUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.addNewUser(newUser, false);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResponse.SUCCESS.getMessage());
  }

  @Test
  void addNewUserInvalidPatternFailure() throws KlawException {
    UserInfoModel newUser = utilMethods.getUserInfoMock();
    newUser.setUsername("abc123$%$"); // invalid pattern
    ApiResponse apiResponse = usersTeamsControllerService.addNewUser(newUser, false);
    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_109);
  }

  @Test
  void addNewTeamSuccess() throws KlawException {
    int tenantId = 101;
    String userName = "testuser";
    String existingTeamName = "existingTeamName";
    TeamModel teamModel = utilMethods.getTeamModelMock();

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(tenantId);
    when(manageDatabase.getTeamNamesForTenant(tenantId)).thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture())).thenReturn(ApiResultStatus.SUCCESS.value);

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, true);
    Team capturedTeam = teamCaptor.getValue();

    assertThat(capturedTeam.getRequestTopicsEnvs()).isEqualTo(
            String.join(",", teamModel.getEnvList().toArray(new String[0])));
    assertThat(capturedTeam.getTeamname()).isEqualTo(teamModel.getTeamname());
    assertThat(capturedTeam.getTeamphone()).isEqualTo(teamModel.getTeamphone());
    assertThat(capturedTeam.getContactperson()).isEqualTo(teamModel.getContactperson());
    assertThat(capturedTeam.getTenantId()).isEqualTo(tenantId);
    assertThat(capturedTeam.getTeamId()).isEqualTo(teamModel.getTeamId());

    assertThat(apiResponse.isSuccess()).isTrue();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(commonUtilsService).updateMetadata(tenantId, EntityType.TEAM, MetadataOperationType.CREATE, null);
  }

  @Test
  void addNewTeamWithApiFailureFromDB() throws KlawException {
    int tenantId = 101;
    String userName = "testuser";
    String existingTeamName = "existingTeamName";
    TeamModel teamModel = utilMethods.getTeamModelMock();

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(tenantId);
    when(manageDatabase.getTeamNamesForTenant(tenantId)).thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture())).thenReturn(ApiResultStatus.FAILURE.value);

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, true);
    Team capturedTeam = teamCaptor.getValue();

    assertThat(capturedTeam.getRequestTopicsEnvs()).isEqualTo(
            String.join(",", teamModel.getEnvList().toArray(new String[0])));
    assertThat(capturedTeam.getTeamname()).isEqualTo(teamModel.getTeamname());
    assertThat(capturedTeam.getTeamphone()).isEqualTo(teamModel.getTeamphone());
    assertThat(capturedTeam.getContactperson()).isEqualTo(teamModel.getContactperson());
    assertThat(capturedTeam.getTenantId()).isEqualTo(tenantId);
    assertThat(capturedTeam.getTeamId()).isEqualTo(teamModel.getTeamId());
    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.FAILURE.value);
    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  void addNewTeamFailureWithException() {
    int tenantId = 101;
    String userName = "testuser";
    String existingTeamName = "existingTeamName";
    String errorMessage = "Failure. Team already exists";
    TeamModel teamModel = utilMethods.getTeamModelMock();

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(tenantId);
    when(manageDatabase.getTeamNamesForTenant(tenantId)).thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture())).thenThrow(new RuntimeException(errorMessage));

    assertThatExceptionOfType(KlawException.class)
            .isThrownBy(() -> usersTeamsControllerService.addNewTeam(teamModel, true))
            .withMessage(errorMessage);

    Team capturedTeam = teamCaptor.getValue();

    assertThat(capturedTeam.getRequestTopicsEnvs()).isEqualTo(
            String.join(",", teamModel.getEnvList().toArray(new String[0])));
    assertThat(capturedTeam.getTeamname()).isEqualTo(teamModel.getTeamname());
    assertThat(capturedTeam.getTeamphone()).isEqualTo(teamModel.getTeamphone());
    assertThat(capturedTeam.getContactperson()).isEqualTo(teamModel.getContactperson());
    assertThat(capturedTeam.getTeamId()).isEqualTo(teamModel.getTeamId());
    assertThat(capturedTeam.getTenantId()).isEqualTo(tenantId);

    verify(commonUtilsService, never()).updateMetadata(anyInt(), any(), any(), anyString());
  }

  @Test
  void addNewTeamFailureWithExistingTeamName() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    when(manageDatabase.getTeamNamesForTenant(teamModel.getTenantId())).thenReturn(List.of(teamModel.getTeamname()));

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, false);

    assertThat(apiResponse.isSuccess()).isFalse();
    assertThat(apiResponse.getMessage()).isEqualTo(TEAMS_ERR_119);
  }

  @Test
  void addNewTeamFailureWithUnAuthorizedUser() throws KlawException {
    TeamModel teamModel = utilMethods.getTeamModelMock();

    when(commonUtilsService.isNotAuthorizedUser(any(), any(PermissionType.class))).thenReturn(true);

    ApiResponse apiResponse = usersTeamsControllerService.addNewTeam(teamModel, true);
    assertThat(apiResponse).isSameAs(ApiResponse.NOT_AUTHORIZED);
  }

  @Test
  void updateTeam() {}

  @Test
  void changePwd() {}

  @Test
  void showUsers() {}

  @Test
  void getMyProfileInfo() {}

  @Test
  void addTwoDefaultTeamsSuccess() throws KlawException {
    String userName = "testuser";
    String existingTeamName = "existingTeamName";
    String contactPerson = "contactPerson";

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(manageDatabase.getTeamNamesForTenant(TEST_TENANT_ID)).thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture())).thenReturn(ApiResultStatus.SUCCESS.value);

    Map<String, String> teamMap = usersTeamsControllerService.addTwoDefaultTeams(contactPerson, "newTenant", TEST_TENANT_ID);

    assertThat(teamMap.size()).isEqualTo(2);
    assertThat(teamMap.get("team1result")).isEqualTo("success");
    assertThat(teamMap.get("team2result")).isEqualTo("success");

    addTwoDefaultTeamsVerifyCapturedTeams(contactPerson, 2);
  }

  @Test
  void addTwoDefaultTeamsWithApiFailureForFirstTeam() throws KlawException {
    String userName = "testuser";
    String existingTeamName = "existingTeamName";
    String contactPerson = "contactPerson";

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(manageDatabase.getTeamNamesForTenant(TEST_TENANT_ID)).thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture()))
            .thenReturn(ApiResultStatus.FAILURE.value).thenReturn(ApiResultStatus.SUCCESS.value);

    Map<String, String> teamMap = usersTeamsControllerService.addTwoDefaultTeams(contactPerson, "newTenant", TEST_TENANT_ID);

    assertThat(teamMap.size()).isEqualTo(2);
    assertThat(teamMap.get("team1result")).isEqualTo("failure");
    assertThat(teamMap.get("team2result")).isEqualTo("success");

    addTwoDefaultTeamsVerifyCapturedTeams(contactPerson, 1);
  }

  @Test
  void addTwoDefaultTeamsWithApiFailureForSecondTeam() throws KlawException {
    String userName = "testuser";
    String existingTeamName = "existingTeamName";
    String contactPerson = "contactPerson";

    when(mailService.getUserName(any())).thenReturn(userName);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(TEST_TENANT_ID);
    when(manageDatabase.getTeamNamesForTenant(TEST_TENANT_ID)).thenReturn(List.of(existingTeamName));
    when(handleDbRequests.addNewTeam(teamCaptor.capture()))
            .thenReturn(ApiResultStatus.SUCCESS.value).thenReturn(ApiResultStatus.FAILURE.value);

    Map<String, String> teamMap = usersTeamsControllerService.addTwoDefaultTeams(contactPerson, "newTenant", TEST_TENANT_ID);

    assertThat(teamMap.size()).isEqualTo(2);
    assertThat(teamMap.get("team1result")).isEqualTo("success");
    assertThat(teamMap.get("team2result")).isEqualTo("failure");

    addTwoDefaultTeamsVerifyCapturedTeams(contactPerson, 1);
  }

  @ParameterizedTest
  @MethodSource
  void registerUserInternal(
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

  @Test
  void getNewUserRequests() {}

  @Test
  void approveNewUserRequests() {}

  @Test
  void declineNewUserRequests() {}

  @Test
  void getRegistrationInfoFromId() {}

  @Test
  void getEnvDetailsFromId() {}

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
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

  private void getTeamDetailsVerifyResponse(TeamModelResponse response, Team teamDaoMock, Map<Integer, String> tenantMapMock) {
    assertThat(response.getTeamname()).isEqualTo(teamDaoMock.getTeamname());
    assertThat(response.getTeamphone()).isEqualTo(teamDaoMock.getTeamphone());
    assertThat(response.getContactperson()).isEqualTo(teamDaoMock.getContactperson());
    assertThat(response.getTeamId()).isEqualTo(teamDaoMock.getTeamId());
    assertThat(response.getTenantId()).isEqualTo(tenantMapMock.entrySet().stream()
            .filter(obj -> Objects.equals(obj.getValue(), TEST_TENANT_NAME))
            .findFirst()
            .get()
            .getKey());
    assertThat(response.getTenantName()).isEqualTo(TEST_TENANT_NAME);
    assertThat(response.getTeammail()).isEqualTo(teamDaoMock.getTeammail());
    assertThat(response.getApp()).isEqualTo(teamDaoMock.getApp());
    assertThat(response.getServiceAccounts()).isEqualTo(teamDaoMock.getServiceAccounts());
  }

  private void addTwoDefaultTeamsVerifyCapturedTeams(String contactPerson, int expectedUpdateMetaDataInvocations) {
    List<Team> capturedTeams = teamCaptor.getAllValues();

    assertThat(capturedTeams.size()).isEqualTo(2);
    assertThat(capturedTeams.get(0).getTenantId()).isEqualTo(TEST_TENANT_ID);
    assertThat(capturedTeams.get(0).getTeamname()).isEqualTo(KwConstants.INFRATEAM);
    assertThat(capturedTeams.get(0).getContactperson()).isEqualTo(contactPerson);
    assertThat(capturedTeams.get(1).getTenantId()).isEqualTo(TEST_TENANT_ID);
    assertThat(capturedTeams.get(1).getTeamname()).isEqualTo(KwConstants.STAGINGTEAM);
    assertThat(capturedTeams.get(1).getContactperson()).isEqualTo(contactPerson);

    verify(commonUtilsService, times(expectedUpdateMetaDataInvocations)).updateMetadata(
            TEST_TENANT_ID, EntityType.TEAM, MetadataOperationType.CREATE, null);
  }
}
