package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.UserInfoModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UsersTeamsControllerServiceTest {

  private UtilMethods utilMethods;

  @Mock InMemoryUserDetailsManager inMemoryUserDetailsManager;
  @Mock private MailUtils mailService;

  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private UserDetails userDetails;
  @Mock private ManageDatabase manageDatabase;

  private UsersTeamsControllerService usersTeamsControllerService;
  private UserInfo userInfo;

  @BeforeEach
  void setUp() {
    utilMethods = new UtilMethods();
    usersTeamsControllerService = new UsersTeamsControllerService();
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "inMemoryUserDetailsManager", inMemoryUserDetailsManager);
    ReflectionTestUtils.setField(usersTeamsControllerService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(usersTeamsControllerService, "mailService", mailService);
    ReflectionTestUtils.setField(
        usersTeamsControllerService, "commonUtilsService", commonUtilsService);
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    userInfo = utilMethods.getUserInfoMockDao();
    loginMock();
  }

  @Test
  void getUserInfoDetails() {
    String userId = "testuser";
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(manageDatabase.getTeamNameFromTeamId(anyInt(), anyInt())).thenReturn("testteam");
    UserInfoModel userInfoModel = usersTeamsControllerService.getUserInfoDetails(userId);
    assertThat(userInfoModel.getTeamId()).isEqualTo(userInfo.getTeamId());
    assertThat(userInfoModel.getUsername()).isEqualTo(userInfo.getUsername());
    assertThat(userInfoModel.getRole()).isEqualTo(userInfo.getRole());
    assertThat(userInfoModel.getTeam()).isEqualTo("testteam");
  }

  @Test
  void updateProfile() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    when(handleDbRequests.updateUser(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    ApiResponse apiResponse = usersTeamsControllerService.updateProfile(userInfoModel);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  void updateProfileFailureDbUpdate() {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
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
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(true);
    ApiResponse apiResponse = usersTeamsControllerService.updateUser(userInfoModel);
    assertThat(apiResponse.getMessage()).isEqualTo(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  void updateUserNotAuthorizedToUpdateSuperAdmin() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMap());
    ApiResponse apiResponse = usersTeamsControllerService.updateUser(userInfoModel);
    assertThat(apiResponse.getMessage())
        .isEqualTo("Not Authorized to update another SUPERADMIN user.");
  }

  @Test
  void getTeamDetails() {}

  @Test
  void resetPassword() {}

  @Test
  void getAllTeamsSUFromRegisterUsers() {}

  @Test
  void getAllTeamsSU() {}

  @Test
  void getAllTeamsSUOnly() {}

  @Test
  void deleteTeamFailure() throws KlawException {
    int teamId = 101;
    int tenantId = 101;
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMap());
    when(handleDbRequests.selectAllUsersInfoForTeam(teamId, tenantId))
        .thenReturn(Collections.singletonList(new UserInfo()));
    ApiResponse apiResponse = usersTeamsControllerService.deleteTeam(teamId);
    assertThat(apiResponse.getMessage())
        .isEqualTo("Not allowed to delete this team, as there are associated users.");
  }

  @Test
  void deleteUserFailureHasRequests() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt())).thenReturn(new HashMap<>());
    when(handleDbRequests.findAllComponentsCountForUser("testuser", 101)).thenReturn(1);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage())
        .isEqualTo(
            "Not allowed to delete this user, as there are associated requests in the metadata.");
  }

  @Test
  void deleteUserFailureisAdmin() throws KlawException {
    UserInfoModel userInfoModel = utilMethods.getUserInfoMock();
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(commonUtilsService.getTenantId(anyString())).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("testuser");
    when(manageDatabase.getRolesPermissionsPerTenant(anyInt()))
        .thenReturn(utilMethods.getRolesPermsMap());
    when(handleDbRequests.findAllComponentsCountForUser("testuser", 101)).thenReturn(1);
    ApiResponse apiResponse = usersTeamsControllerService.deleteUser("testuser", false);
    assertThat(apiResponse.getMessage())
        .isEqualTo("Not Authorized. Cannot delete a user with SUPERADMIN access.");
  }

  @Test
  void addNewUser() {}

  @Test
  void addNewTeam() {}

  @Test
  void updateTeam() {}

  @Test
  void changePwd() {}

  @Test
  void showUsers() {}

  @Test
  void getMyProfileInfo() {}

  @Test
  void addTwoDefaultTeams() {}

  @Test
  void registerUser() {}

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
}
