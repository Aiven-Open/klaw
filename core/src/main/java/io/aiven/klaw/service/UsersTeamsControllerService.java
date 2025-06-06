package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.*;
import static io.aiven.klaw.helpers.KwConstants.SUPERADMIN_ROLE;
import static io.aiven.klaw.helpers.KwConstants.USER_DELETION_MAIL_TEXT;
import static io.aiven.klaw.helpers.KwConstants.USER_DELETION_TEXT;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.enums.AuthenticationType.LDAP;
import static io.aiven.klaw.service.PasswordService.BCRYPT_ENCODING_ID;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.NewUserStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.ChangePasswordRequestModel;
import io.aiven.klaw.model.requests.ProfileModel;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import io.aiven.klaw.model.requests.TeamModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.RegisterUserInfoModelResponse;
import io.aiven.klaw.model.response.ResetPasswordInfo;
import io.aiven.klaw.model.response.TeamModelResponse;
import io.aiven.klaw.model.response.UserInfoModelResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UsersTeamsControllerService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String MASKED_PWD = "********";
  public static final String UNUSED_PASSWD = "unusedpasswd";
  private static final Pattern emailUsernamePattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${klaw.enable.sso:false}")
  private boolean ssoEnabled;

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired(required = false)
  private InMemoryUserDetailsManager inMemoryUserDetailsManager;

  @Autowired private PasswordService passwordService;
  // pattern for simple username/mailid

  // pattern for simple username
  private static final Pattern defaultPattern = Pattern.compile("^[a-zA-Z0-9]{3,}$");

  public UserInfoModelResponse getUserInfoDetails(String userId) {
    UserInfoModelResponse userInfoModel = new UserInfoModelResponse();
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
    if (userInfo != null) {
      copyProperties(userInfo, userInfoModel);
      updateSwitchTeamsList(userInfoModel, userInfo, -1, false);
      userInfoModel.setTeam(
          manageDatabase.getTeamNameFromTeamId(userInfo.getTenantId(), userInfoModel.getTeamId()));
      userInfoModel.setUserPassword(MASKED_PWD);
      return userInfoModel;
    } else {
      return null;
    }
  }

  private void updateSwitchTeamsList(
      UserInfoModelResponse userInfoModel,
      UserInfo userInfo,
      int tenantId,
      boolean updateTeamName) {
    if (userInfo.isSwitchTeams()) {
      Set<Integer> switchAllowedTeamIds = userInfo.getSwitchAllowedTeamIds();
      Set<String> switchAllowedTeamNames = new HashSet<>();

      if (updateTeamName) {
        switchAllowedTeamIds.forEach(
            switchAllowedTeamId ->
                switchAllowedTeamNames.add(
                    manageDatabase.getTeamNameFromTeamId(tenantId, switchAllowedTeamId)));
        userInfoModel.setSwitchAllowedTeamNames(switchAllowedTeamNames);
      }
    }
  }

  public ApiResponse updateProfile(ProfileModel profileModel) throws KlawException {
    log.debug("updateProfile {}", profileModel);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    UserInfo userInfo = dbHandle.getUsersInfo(getUserName());
    userInfo.setFullname(profileModel.getFullname());
    userInfo.setMailid(profileModel.getMailid());
    try {
      String result = dbHandle.updateUser(userInfo);
      commonUtilsService.updateMetadata(
          commonUtilsService.getTenantId(getUserName()),
          EntityType.USERS,
          MetadataOperationType.UPDATE,
          userInfo.getUsername());
      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse updateUser(UserInfoModel newUser) throws KlawException {
    log.info("updateUser {}", newUser.getUsername());

    if (commonUtilsService.isNotAuthorizedUser(
        getUserName(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    if (newUser.getTeamId() == null) {
      return ApiResponse.notOk(TEAMS_ERR_101);
    }

    UserInfo existingUserInfo =
        manageDatabase.getHandleDbRequests().getUsersInfo(newUser.getUsername());
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Set<String> permissions =
        manageDatabase
            .getRolesPermissionsPerTenant(tenantId)
            .getOrDefault(existingUserInfo.getRole(), Collections.emptySet());
    if (permissions != null
        && permissions.contains(PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES.name())) {
      if (!Objects.equals(
          getUserName(), newUser.getUsername())) { // should be able to update same user
        return ApiResponse.notOk(TEAMS_ERR_102);
      }
    }

    // Only a user with permission FULL_ACCESS_USERS_TEAMS_ROLES or UPDATE_PERMISSIONS
    // should be able to update other user who has permissions FULL_ACCESS_USERS_TEAMS_ROLES or
    // UPDATE_PERMISSIONS or role SUPERADMIN_ROLE
    if (verifySuperAdminAccess(newUser, permissions)) {
      return ApiResponse.notOk(TEAMS_ERR_102);
    }

    return getApiResponseUpdateUser(newUser, existingUserInfo, tenantId);
  }

  private ApiResponse getApiResponseUpdateUser(
      UserInfoModel newUser, UserInfo existingUserInfo, int tenantId) throws KlawException {
    String pwdUpdated = newUser.getUserPassword();
    String existingPwd;
    if (StringUtils.isEmpty(pwdUpdated) && DATABASE.value.equals(authenticationType)) {
      existingPwd = existingUserInfo.getPwd();
      if (!"".equals(existingPwd)) {
        newUser.setUserPassword(passwordService.getBcryptPassword(existingPwd));
      }
    } else if (!StringUtils.isEmpty(pwdUpdated) && DATABASE.value.equals(authenticationType)) {
      newUser.setUserPassword(passwordService.encodePwd(pwdUpdated));
    }

    try {
      if (DATABASE.value.equals(authenticationType)) {
        if (inMemoryUserDetailsManager.userExists(newUser.getUsername())) {
          inMemoryUserDetailsManager.updateUser(
              User.withUsername(newUser.getUsername())
                  .password(newUser.getUserPassword())
                  .roles(newUser.getRole())
                  .build());
        } else {
          inMemoryUserDetailsManager.createUser(
              User.withUsername(newUser.getUsername())
                  .password(newUser.getUserPassword())
                  .roles(newUser.getRole())
                  .build());
        }
        newUser.setUserPassword(newUser.getUserPassword());
      }

      HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

      copyProperties(newUser, existingUserInfo);
      existingUserInfo.setPwd(newUser.getUserPassword());
      existingUserInfo.setTenantId(tenantId);
      Pair<Boolean, String> switchTeamsCheck = validateSwitchTeams(newUser);
      if (switchTeamsCheck.getLeft()) {
        return ApiResponse.notOk(switchTeamsCheck.getRight());
      }

      String result = dbHandle.updateUser(existingUserInfo);
      if (result.equals(ApiResultStatus.SUCCESS.value)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.USERS, MetadataOperationType.UPDATE, newUser.getUsername());
      }
      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      log.error("Error from updateUser ", e);
      throw new KlawException(e.getMessage());
    }
  }

  public TeamModelResponse getTeamDetails(Integer teamId, String tenantName) {
    log.debug("getTeamDetails {} {}", teamId, tenantName);

    int tenantId = commonUtilsService.getTenantId(getUserName());
    Team teamDao = manageDatabase.getHandleDbRequests().getTeamDetails(teamId, tenantId);
    if (teamDao != null) {
      TeamModelResponse teamModel = new TeamModelResponse();
      copyProperties(teamDao, teamModel);
      if (teamDao.getRequestTopicsEnvs() != null) {
        teamModel.setEnvList(Arrays.asList(teamDao.getRequestTopicsEnvs().split("\\s*,\\s*")));
      }
      teamModel.setTenantName(tenantName);
      teamModel.setTenantId(getTenantId(tenantName));
      return teamModel;
    }
    return null;
  }

  private int getTenantId(String tenantName) {
    return manageDatabase.getTenantMap().entrySet().stream()
        .filter(obj -> Objects.equals(obj.getValue(), tenantName))
        .findFirst()
        .get()
        .getKey();
  }

  public ResetPasswordInfo resetPasswordGenerateToken(String username) {
    log.info("resetPasswordGenerateToken {}", username);
    ResetPasswordInfo resetPasswordInfo = new ResetPasswordInfo();
    UserInfoModelResponse userInfoModel = getUserInfoDetails(username);
    resetPasswordInfo.setTokenSent(false);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    if (userInfoModel == null) {
      resetPasswordInfo.setUserFound(false);
    } else {
      resetPasswordInfo.setUserFound(true);
      String resetToken = dbHandle.generatePasswordResetToken(username);
      if (!ApiResultStatus.FAILURE.value.equals(resetToken)) {
        resetPasswordInfo.setTokenSent(true);
        mailService.sendMailResetPwd(
            username,
            resetToken,
            dbHandle,
            userInfoModel.getTenantId(),
            commonUtilsService.getLoginUrl());
      }
    }
    return resetPasswordInfo;
  }

  public ResetPasswordInfo resetPassword(String username, String password, String resetToken)
      throws KlawNotAuthorizedException {
    log.info("resetPassword {}", username);
    ResetPasswordInfo resetPasswordInfo = new ResetPasswordInfo();
    UserInfoModelResponse userInfoModel = getUserInfoDetails(username);
    resetPasswordInfo.setTokenSent(false);
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    if (userInfoModel == null) {
      resetPasswordInfo.setUserFound(false);
    } else {
      resetPasswordInfo.setUserFound(true);

      String pwdUpdated =
          dbHandle.resetPassword(username, resetToken, passwordService.encodePwd(password));

      if (ApiResultStatus.SUCCESS.value.equals(pwdUpdated)) {
        UserDetails updatePwdUserDetails = inMemoryUserDetailsManager.loadUserByUsername(username);
        inMemoryUserDetailsManager.updatePassword(
            updatePwdUserDetails, passwordService.encodePwd(password));
        resetPasswordInfo.setTokenSent(true);
        mailService.sendMailPwdChanged(
            username, dbHandle, userInfoModel.getTenantId(), commonUtilsService.getLoginUrl());
      }
    }
    return resetPasswordInfo;
  }

  private List<TeamModelResponse> getTeamModels(List<Team> teams) {
    List<TeamModelResponse> teamModels = new ArrayList<>();
    TeamModelResponse teamModel;
    List<String> allList = new ArrayList<>();
    allList.add("ALL");
    List<String> tmpConvertedList;

    for (Team team : teams) {
      teamModel = new TeamModelResponse();
      copyProperties(team, teamModel);
      if (team.getRequestTopicsEnvs() == null || team.getRequestTopicsEnvs().length() == 0) {
        teamModel.setEnvList(allList);
      } else {
        teamModel.setEnvList(Arrays.asList(team.getRequestTopicsEnvs().split("\\s*,\\s*")));
        tmpConvertedList = new ArrayList<>();
        try {
          for (String s : teamModel.getEnvList()) {
            tmpConvertedList.add(getEnvDetailsFromId(s).getName());
          }
        } catch (Exception e) {
          log.error("No environments/clusters found.", e);
        }
        teamModel.setEnvList(tmpConvertedList);
      }

      teamModels.add(teamModel);
    }
    return teamModels;
  }

  public List<TeamModelResponse> getAllTeamsSUFromRegisterUsers() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<TeamModelResponse> teamModels =
        getTeamModels(manageDatabase.getHandleDbRequests().getAllTeams(tenantId));

    teamModels.forEach(
        teamModel ->
            teamModel.setTenantName(manageDatabase.getTenantMap().get(teamModel.getTenantId())));

    return teamModels;
  }

  public List<TeamModelResponse> getAllTeamsSU() {
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    List<TeamModelResponse> teamModels =
        getTeamModels(manageDatabase.getTeamObjForTenant(tenantId));

    if (!commonUtilsService.isNotAuthorizedUser(userName, PermissionType.ADD_EDIT_DELETE_TEAMS)) {
      List<UserInfo> allUsersInfo = manageDatabase.getHandleDbRequests().getAllUsersInfo(tenantId);
      teamModels.forEach(
          teamModel -> {
            teamModel.setShowDeleteTeam(
                (!manageDatabase
                        .getHandleDbRequests()
                        .existsComponentsCountForTeam(teamModel.getTeamId(), tenantId))
                    && (!manageDatabase
                        .getHandleDbRequests()
                        .existsUsersInfoForTeam(teamModel.getTeamId(), tenantId))
                    && allUsersInfo.stream()
                        .noneMatch(
                            userInfo ->
                                userInfo
                                    .getSwitchAllowedTeamIds()
                                    .contains(teamModel.getTeamId())));
          });
    }

    teamModels.forEach(
        teamModel ->
            teamModel.setTenantName(manageDatabase.getTenantMap().get(teamModel.getTenantId())));

    return teamModels;
  }

  // tenant filtering
  public List<String> getAllTeamsSUOnly() {
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String myTeamName =
        manageDatabase
            .getHandleDbRequests()
            .getAllTeamsOfUsers(userDetails, tenantId)
            .get(0)
            .getTeamname();

    List<String> teams = new ArrayList<>();
    // tenant filtering
    List<TeamModelResponse> teamsList = getAllTeamsSU();
    teams.add("All teams"); // team id 1
    teams.add(myTeamName);

    for (TeamModelResponse team : teamsList) {
      if (!team.getTeamname().equals(myTeamName)) {
        teams.add(team.getTeamname());
      }
    }

    return teams;
  }

  public ApiResponse deleteTeam(Integer teamId) throws KlawException {
    log.info("deleteTeam {}", teamId);
    String userName = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_TEAMS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (manageDatabase.getHandleDbRequests().existsUsersInfoForTeam(teamId, tenantId)) {
      return ApiResponse.notOk(TEAMS_ERR_103);
    }

    if (manageDatabase.getHandleDbRequests().existsComponentsCountForTeam(teamId, tenantId)) {
      return ApiResponse.notOk(TEAMS_ERR_104);
    }

    // own team cannot be deleted
    if (Objects.equals(commonUtilsService.getTeamId(userName), teamId)) {
      return ApiResponse.notOk(TEAMS_ERR_105);
    }

    try {
      String result =
          manageDatabase
              .getHandleDbRequests()
              .deleteTeamRequest(teamId, commonUtilsService.getTenantId(getUserName()));

      if (ApiResultStatus.SUCCESS.value.equals(result)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.TEAM, MetadataOperationType.DELETE, null);
      }

      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse deleteUser(String userIdToDelete, boolean isExternal) throws KlawException {
    log.info("deleteUser {}", userIdToDelete);
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    // check permissions of user to be deleted
    UserInfo existingUserInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userIdToDelete);
    Set<String> permissions =
        manageDatabase
            .getRolesPermissionsPerTenant(commonUtilsService.getTenantId(getUserName()))
            .getOrDefault(existingUserInfo.getRole(), Collections.emptySet());
    if (permissions != null
        && permissions.contains(PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES.name())) {
      // user to be deleted has superuser permissions.
      // check if you (logged in user who is deleting) have superuser permissions to delete user
      if (commonUtilsService.isNotAuthorizedUser(
          commonUtilsService.getPrincipal(), PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES)) {
        return ApiResponse.notOk(TEAMS_ERR_106);
      }
    }

    if (manageDatabase
        .getHandleDbRequests()
        .existsComponentsCountForUser(userIdToDelete, tenantId)) {
      return ApiResponse.notOk(TEAMS_ERR_107);
    }

    if (Objects.equals(userName, userIdToDelete) && isExternal) {
      return ApiResponse.notOk(TEAMS_ERR_108);
    }

    try {
      if (inMemoryUserDetailsManager != null
          && inMemoryUserDetailsManager.userExists(userIdToDelete)) {
        inMemoryUserDetailsManager.deleteUser(userIdToDelete);
      }
      String result = manageDatabase.getHandleDbRequests().deleteUserRequest(userIdToDelete);
      if (result.equals(ApiResultStatus.SUCCESS.value)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.USERS, MetadataOperationType.DELETE, userIdToDelete);
        manageDatabase
            .getHandleDbRequests()
            .insertIntoActivityLog(
                RequestEntityType.USER.value,
                tenantId,
                RequestOperationType.DELETE.value,
                commonUtilsService.getTeamId(userName),
                String.format(USER_DELETION_TEXT, userIdToDelete, userName),
                "-NA-",
                userName);
        mailService.sendMail(
            userName,
            manageDatabase.getHandleDbRequests(),
            String.format(USER_DELETION_MAIL_TEXT, userIdToDelete, userName),
            "USER DELETION",
            false,
            false,
            mailService.getEmailAddressFromUsername(userIdToDelete),
            tenantId,
            commonUtilsService.getLoginUrl(),
            false);
      }
      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse addNewUser(UserInfoModel newUser, boolean isExternal) throws KlawException {
    log.info("addNewUser {} {} {}", newUser.getUsername(), newUser.getTeamId(), newUser.getRole());
    boolean userNamePatternCheck = userNamePatternValidation(newUser.getUsername());
    if (!userNamePatternCheck) {
      return ApiResponse.notOk(TEAMS_ERR_109);
    }

    int tenantId;
    if (isExternal) {
      if (newUser.getTenantId() == 0) {
        tenantId = commonUtilsService.getTenantId(getUserName());
      } else {
        tenantId = newUser.getTenantId();
      }
      newUser.setTenantId(tenantId);
    } else {
      tenantId = commonUtilsService.getTenantId(getUserName());
    }

    // Only a user with permission FULL_ACCESS_USERS_TEAMS_ROLES or UPDATE_PERMISSIONS
    // should be able to update other user who has permissions FULL_ACCESS_USERS_TEAMS_ROLES or
    // UPDATE_PERMISSIONS or
    // role SUPERADMIN_ROLE
    Set<String> permissions =
        manageDatabase
            .getRolesPermissionsPerTenant(tenantId)
            .getOrDefault(newUser.getRole(), Collections.emptySet());

    if (verifySuperAdminAccess(newUser, permissions)) {
      return ApiResponse.notOk(TEAMS_ERR_102);
    }

    if (isExternal
        && commonUtilsService.isNotAuthorizedUser(
            commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    try {
      if (DATABASE.value.equals(authenticationType)) {
        // If the newUser is coming from the UI directly the password will not be encrypted yet, if
        // it is coming from the register users it will already be encrypted so check before
        // encrypting.
        if (!newUser.getUserPassword().startsWith(BCRYPT_ENCODING_ID)) {
          newUser.setUserPassword(passwordService.encodePwd(newUser.getUserPassword()));
        }
        inMemoryUserDetailsManager.createUser(
            User.withUsername(newUser.getUsername())
                .password(newUser.getUserPassword())
                .roles(newUser.getRole())
                .build());
      }

      HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
      UserInfo userInfo = new UserInfo();
      copyProperties(newUser, userInfo);

      Pair<Boolean, String> switchTeamsCheck = validateSwitchTeams(newUser);
      if (switchTeamsCheck.getLeft()) return ApiResponse.notOk(switchTeamsCheck.getRight());

      userInfo.setPwd(newUser.getUserPassword());
      String result = dbHandle.addNewUser(userInfo);

      if (result.equals(ApiResultStatus.SUCCESS.value)) {

        commonUtilsService.updateMetadata(
            tenantId, EntityType.USERS, MetadataOperationType.CREATE, newUser.getUsername());
      }

      if (isExternal) {

        if ("".equals(newUser.getUserPassword())
            || ACTIVE_DIRECTORY.value.equals(authenticationType)) {
          mailService.sendMail(newUser.getUsername(), dbHandle, commonUtilsService.getLoginUrl());
        } else {
          mailService.sendMail(newUser.getUsername(), dbHandle, commonUtilsService.getLoginUrl());
        }
      }

      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      try {
        if (inMemoryUserDetailsManager != null
            && inMemoryUserDetailsManager.userExists(newUser.getUsername())) {
          inMemoryUserDetailsManager.deleteUser(newUser.getUsername());
        }
      } catch (Exception e1) {
        log.error("Try deleting user");
      }
      if (StringUtils.isNotBlank(e.getMessage()) && e.getMessage().contains("should not exist")) {
        return ApiResponse.notOk(TEAMS_ERR_110);
      } else {
        log.error("Error ", e);
        throw new KlawException(TEAMS_ERR_111);
      }
    }
  }

  // Only a user with permission FULL_ACCESS_USERS_TEAMS_ROLES or UPDATE_PERMISSIONS
  // should be able to update other user who has permissions FULL_ACCESS_USERS_TEAMS_ROLES or
  // UPDATE_PERMISSIONS or role SUPERADMIN_ROLE
  private boolean verifySuperAdminAccess(UserInfoModel newUser, Set<String> permissions) {
    return (newUser.getRole().equals(SUPERADMIN_ROLE)
            || (permissions != null
                && (permissions.contains(PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES.name())
                    || permissions.contains(PermissionType.UPDATE_PERMISSIONS.name()))))
        && (commonUtilsService.isNotAuthorizedUser(
                getUserName(), PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES)
            && commonUtilsService.isNotAuthorizedUser(
                getUserName(), PermissionType.UPDATE_PERMISSIONS));
  }

  private Pair<Boolean, String> validateSwitchTeams(UserInfoModel sourceUser) {
    if (sourceUser.isSwitchTeams()) {
      Set<Integer> switchAllowedTeamIds = sourceUser.getSwitchAllowedTeamIds();
      if (switchAllowedTeamIds == null
          || switchAllowedTeamIds.isEmpty()
          || switchAllowedTeamIds.size() < 2) { // make sure at least 2 teams are selected to switch
        return Pair.of(Boolean.TRUE, TEAMS_ERR_112);
      } else if (!switchAllowedTeamIds.contains(sourceUser.getTeamId())) {
        return Pair.of(Boolean.TRUE, TEAMS_ERR_113);
      }
    }
    return Pair.of(Boolean.FALSE, "");
  }

  public ApiResponse addNewTeam(TeamModel newTeam, boolean isExternal) throws KlawException {
    log.info("addNewTeam {}", newTeam);

    if (isExternal
        && commonUtilsService.isNotAuthorizedUser(
            commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_TEAMS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    int tenantId;
    if (isExternal) {
      tenantId = commonUtilsService.getTenantId(getUserName());
      newTeam.setTenantId(tenantId);
    } else {
      tenantId = newTeam.getTenantId();
    }

    // teamname check if already exists
    if (manageDatabase.getTeamNamesForTenant(tenantId).stream()
        .anyMatch(newTeam.getTeamname()::equalsIgnoreCase)) {
      return ApiResponse.notOk(TEAMS_ERR_119);
    }

    Team team = new Team();
    copyProperties(newTeam, team);
    String envListStrCommaSeperated;

    if (newTeam.getEnvList() != null && newTeam.getEnvList().size() > 0) {
      envListStrCommaSeperated = String.join(",", newTeam.getEnvList().toArray(new String[0]));
      team.setRequestTopicsEnvs(envListStrCommaSeperated);
    }
    try {
      String res = manageDatabase.getHandleDbRequests().addNewTeam(team);
      if (ApiResultStatus.SUCCESS.value.equals(res)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.TEAM, MetadataOperationType.CREATE, null);
      }
      return ApiResultStatus.SUCCESS.value.equals(res)
          ? ApiResponse.ok(res)
          : ApiResponse.notOk(res);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse updateTeam(TeamModel updatedTeam) throws KlawException {
    log.info("updateTeam {}", updatedTeam);

    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_TEAMS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    // teamname check if already exists
    if (manageDatabase.getTeamObjForTenant(tenantId).stream()
        .filter(a -> !a.getTeamId().equals(updatedTeam.getTeamId()))
        .map(Team::getTeamname)
        .anyMatch(updatedTeam.getTeamname()::equalsIgnoreCase)) {
      return ApiResponse.notOk(TEAMS_ERR_119);
    }

    Team team = new Team();
    copyProperties(updatedTeam, team);
    team.setTenantId(tenantId);

    String envListStrCommaSeperated;
    if (updatedTeam.getEnvList() != null && updatedTeam.getEnvList().size() > 0) {
      envListStrCommaSeperated = String.join(",", updatedTeam.getEnvList().toArray(new String[0]));
      team.setRequestTopicsEnvs(envListStrCommaSeperated);
    }

    try {
      String res = manageDatabase.getHandleDbRequests().updateTeam(team);
      commonUtilsService.updateMetadata(
          tenantId, EntityType.TEAM, MetadataOperationType.UPDATE, null);
      return ApiResultStatus.SUCCESS.value.equals(res)
          ? ApiResponse.ok(res)
          : ApiResponse.notOk(res);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse changePwd(ChangePasswordRequestModel changePasswordRequestModel)
      throws KlawException {
    if (LDAP.value.equals(authenticationType)
        || ACTIVE_DIRECTORY.value.equals(authenticationType)) {
      return ApiResponse.notOk(TEAMS_ERR_114);
    }

    if (!changePasswordRequestModel.getPwd().equals(changePasswordRequestModel.getRepeatPwd())) {
      return ApiResponse.notOk(TEAMS_ERR_120);
    }

    String userDetails = getUserName();
    String pwdChange = changePasswordRequestModel.getPwd();

    try {

      UserDetails updatePwdUserDetails = inMemoryUserDetailsManager.loadUserByUsername(userDetails);
      inMemoryUserDetailsManager.updatePassword(
          updatePwdUserDetails, passwordService.encodePwd(pwdChange));

      String result =
          manageDatabase
              .getHandleDbRequests()
              .updatePassword(userDetails, passwordService.encodePwd(pwdChange));
      return ApiResultStatus.SUCCESS.value.equals(result)
          ? ApiResponse.ok(result)
          : ApiResponse.notOk(result);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public List<UserInfoModelResponse> showUsers(
      Integer teamId, String userSearchStr, String pageNo) {

    List<UserInfoModelResponse> userInfoModels = new ArrayList<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<UserInfo> userList = manageDatabase.getHandleDbRequests().getAllUsersInfo(tenantId);

    if (userSearchStr != null && !userSearchStr.equals("")) {
      userList =
          userList.stream()
              .filter(user -> user.getUsername().contains(userSearchStr))
              .collect(Collectors.toList());
    }

    userList.forEach(
        userListItem -> {
          UserInfoModelResponse userInfoModel = new UserInfoModelResponse();
          copyProperties(userListItem, userInfoModel);
          updateSwitchTeamsList(userInfoModel, userListItem, tenantId, true);
          if (teamId != null) {
            if (Objects.equals(userInfoModel.getTeamId(), teamId)) {
              userInfoModels.add(userInfoModel);
            }
          } else {
            userInfoModels.add(userInfoModel);
          }
        });
    userInfoModels.forEach(
        userInfoModel -> {
          userInfoModel.setTeam(
              manageDatabase.getTeamNameFromTeamId(tenantId, userInfoModel.getTeamId()));
        });
    userInfoModels.sort(Comparator.comparing(UserInfoModelResponse::getUsername));

    return Pager.getItemsList(
        pageNo,
        "",
        userInfoModels,
        (pageContext, mp) -> {
          mp.setTotalNoPages(pageContext.getTotalPages());
          List<String> numList = new ArrayList<>();
          int totalPages = Integer.parseInt(pageContext.getTotalPages());
          for (int k = 1; k <= totalPages; k++) {
            numList.add("" + k);
          }
          mp.setAllPageNos(numList);
          return mp;
        });
  }

  public UserInfoModelResponse getMyProfileInfo() {
    String userDetails = getUserName();
    UserInfoModelResponse userInfoModel = new UserInfoModelResponse();
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userDetails);
    copyProperties(userInfo, userInfoModel);
    updateSwitchTeamsList(userInfoModel, userInfo, userInfo.getTenantId(), true);
    userInfoModel.setTeam(
        manageDatabase.getTeamNameFromTeamId(
            commonUtilsService.getTenantId(userDetails), userInfo.getTeamId()));
    return userInfoModel;
  }

  private String getUserName() {
    return mailService.getUserName(commonUtilsService.getPrincipal());
  }

  Map<String, String> addTwoDefaultTeams(
      String teamContactPerson, String newTenantName, Integer tenantId) throws KlawException {
    Map<String, String> teamAddMap = new HashMap<>();

    TeamModel teamModel = new TeamModel();
    teamModel.setTenantId(tenantId);
    teamModel.setTenantName(newTenantName);
    teamModel.setTeamname(KwConstants.INFRATEAM);
    teamModel.setContactperson(teamContactPerson);

    ApiResponse addTeamRes = addNewTeam(teamModel, false);
    teamAddMap.put("team1result", addTeamRes.getMessage());

    TeamModel teamModel1 = new TeamModel();
    teamModel1.setTenantId(tenantId);
    teamModel1.setTenantName(newTenantName);
    teamModel1.setTeamname(KwConstants.STAGINGTEAM);
    teamModel1.setContactperson(teamContactPerson);

    ApiResponse addTeamRes2 = addNewTeam(teamModel1, false);
    teamAddMap.put("team2result", addTeamRes2.getMessage());
    return teamAddMap;
  }

  public ApiResponse registerUser(RegisterUserInfoModel newUser, boolean isExternal)
      throws KlawException {
    log.info("registerUser {}", newUser.getUsername());
    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

    // check if user exists
    List<UserInfo> userList = manageDatabase.getHandleDbRequests().getAllUsersAllTenants();
    if (userList.stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), newUser.getMailid()))) {
      return ApiResponse.notOk(TEAMS_ERR_115);
    } else if (userList.stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), newUser.getUsername()))) {
      return ApiResponse.notOk(TEAMS_ERR_115);
    }

    // check if registration exists
    List<RegisterUserInfo> registerUserInfoList =
        manageDatabase.getHandleDbRequests().getAllRegisterUsersInformation();
    if (registerUserInfoList.stream()
        .anyMatch(
            user -> {
              if (user.getUsername().equals(newUser.getUsername())
                  || user.getUsername().equals(newUser.getMailid())) {
                // if not equal to pending it has been previously declined or should have been
                // caught by the above check if the user already exists.
                return !Objects.equals(user.getStatus(), NewUserStatus.PENDING);
              }
              return false;
            })) {
      return ApiResponse.notOk(TEAMS_ERR_117);
    }
    // get the user details from db
    RegisterUserInfo stagingRegisterUserInfo =
        manageDatabase
            .getHandleDbRequests()
            .getFirstStagingRegisterUsersInfo(newUser.getUsername());

    // enrich user info
    if (stagingRegisterUserInfo != null) {
      newUser.setTeamId(stagingRegisterUserInfo.getTeamId());
      newUser.setTeam(
          manageDatabase.getTeamNameFromTeamId(
              stagingRegisterUserInfo.getTenantId(), stagingRegisterUserInfo.getTeamId()));
      newUser.setRole(stagingRegisterUserInfo.getRole());
      newUser.setTenantId(stagingRegisterUserInfo.getTenantId());
    }

    try {
      newUser.setStatus(NewUserStatus.PENDING.value);
      newUser.setRegisteredTime(new Timestamp(System.currentTimeMillis()));

      if (isExternal) {
        newUser.setTeam(Objects.requireNonNullElse(newUser.getTeam(), KwConstants.STAGINGTEAM));
        newUser.setRole(Objects.requireNonNullElse(newUser.getRole(), KwConstants.USER_ROLE));
      }

      RegisterUserInfo registerUserInfo = new RegisterUserInfo();
      copyProperties(newUser, registerUserInfo);
      registerUserInfo.setPwd(passwordService.encodePwd(registerUserInfo.getPwd()));

      if (newUser.getTenantName() == null
          || newUser.getTenantName().equals("")) { // join default tenant
        if (null == registerUserInfo.getTeamId()) {
          registerUserInfo.setTeamId(
              manageDatabase.getTeamIdFromTeamName(
                  KwConstants.DEFAULT_TENANT_ID, newUser.getTeam()));
        }
        if (registerUserInfo.getTenantId() == 0) { // check for tenantId if 0
          registerUserInfo.setTenantId(KwConstants.DEFAULT_TENANT_ID);
        }
      } else { // join existing tenant
        try {
          int tenantId = getTenantId(newUser.getTenantName());
          registerUserInfo.setTenantId(tenantId);

          // new tenant, no users exist
          if (userList.stream().noneMatch(userInfo -> userInfo.getTenantId() == tenantId)) {
            addTwoDefaultTeams(newUser.getFullname(), newUser.getTenantName(), tenantId);
            registerUserInfo.setTeam(KwConstants.INFRATEAM);
            registerUserInfo.setRole(KwConstants.SUPERADMIN_ROLE);
            registerUserInfo.setTeamId(
                manageDatabase.getTeamIdFromTeamName(tenantId, registerUserInfo.getTeam()));
          } else {
            registerUserInfo.setTeamId(
                manageDatabase.getTeamIdFromTeamName(tenantId, newUser.getTeam()));
          }
        } catch (Exception e) {
          log.error("Exception:", e);
          return ApiResponse.notOk(TEAMS_ERR_116);
        }
      }

      String resultRegister = dbHandle.registerUser(registerUserInfo);
      if (resultRegister.contains("Failure")) {
        return ApiResponse.notOk(TEAMS_ERR_117);
      }

      if (isExternal) {
        mailService.sendMailRegisteredUser(
            registerUserInfo, dbHandle, commonUtilsService.getLoginUrl());
      }

      return ApiResultStatus.SUCCESS.value.equals(resultRegister)
          ? ApiResponse.ok(resultRegister)
          : ApiResponse.notOk(resultRegister);
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(TEAMS_ERR_118);
    }
  }

  public List<RegisterUserInfoModelResponse> getNewUserRequests()
      throws KlawNotAuthorizedException {
    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      throw new KlawNotAuthorizedException("You are not authorized to view this information.");
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<RegisterUserInfo> registerUserInfoList;

    registerUserInfoList = manageDatabase.getHandleDbRequests().getAllRegisterUsersInformation();

    List<RegisterUserInfoModelResponse> registerUserInfoModels = new ArrayList<>();
    RegisterUserInfoModelResponse registerUserInfoModel;
    for (RegisterUserInfo registerUserInfo : registerUserInfoList) {
      registerUserInfoModel = new RegisterUserInfoModelResponse();
      copyProperties(registerUserInfo, registerUserInfoModel);
      registerUserInfoModel.setTeam(
          manageDatabase.getTeamNameFromTeamId(tenantId, registerUserInfo.getTeamId()));
      registerUserInfoModel.setPwd("");
      registerUserInfoModel.setTenantName(
          manageDatabase.getTenantMap().get(registerUserInfoModel.getTenantId()));
      registerUserInfoModels.add(registerUserInfoModel);
    }

    return registerUserInfoModels;
  }

  public ApiResponse approveNewUserRequests(
      String username, boolean isExternal, int tenantId, String teamName) throws KlawException {
    log.info("approveNewUserRequests {}", username);
    String userDetails = getUserName();

    if (isExternal
        && commonUtilsService.isNotAuthorizedUser(
            commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    try {
      RegisterUserInfo registerUserInfo = dbHandle.getPendingRegisterUsersInfo(username);

      tenantId = registerUserInfo.getTenantId();

      UserInfoModel userInfo = new UserInfoModel();
      userInfo.setUsername(username);
      userInfo.setFullname(registerUserInfo.getFullname());
      userInfo.setTeamId(registerUserInfo.getTeamId());
      userInfo.setRole(registerUserInfo.getRole());
      userInfo.setTenantId(tenantId);

      if (DATABASE.value.equals(authenticationType)) {
        userInfo.setUserPassword(passwordService.getBcryptPassword(registerUserInfo.getPwd()));
      } else {
        userInfo.setUserPassword(UNUSED_PASSWD);
      }
      userInfo.setMailid(registerUserInfo.getMailid());

      ApiResponse resultResp = addNewUser(userInfo, isExternal);
      if (resultResp.getMessage().contains(ApiResultStatus.SUCCESS.value)) {
        dbHandle.updateNewUserRequest(username, userDetails, true);
      } else {
        return ApiResponse.FAILURE;
      }
      return ApiResponse.SUCCESS;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse declineNewUserRequests(String username) throws KlawException {
    log.info("declineNewUserRequests {}", username);
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
    try {
      dbHandle.updateNewUserRequest(username, userDetails, false);
      return ApiResponse.SUCCESS;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public RegisterUserInfoModelResponse getRegistrationInfoFromId(
      String registrationId, String status) {
    RegisterUserInfo registerUserInfo =
        manageDatabase.getHandleDbRequests().getRegistrationDetails(registrationId, status);

    if (registerUserInfo != null) {
      RegisterUserInfoModelResponse registerUserInfoModel = new RegisterUserInfoModelResponse();
      copyProperties(registerUserInfo, registerUserInfoModel);
      return registerUserInfoModel;
    } else {
      return null;
    }
  }

  public Env getEnvDetailsFromId(String envId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(commonUtilsService.getTenantId(getUserName())).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  private boolean userNamePatternValidation(String userName) {
    Matcher m1 = emailUsernamePattern.matcher(userName);
    Matcher m2 = defaultPattern.matcher(userName);
    return m1.matches() || m2.matches();
  }

  public List<TeamModelResponse> getSwitchTeams(String userId) {
    List<TeamModelResponse> teamModelList = new ArrayList<>();
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (userInfo.isSwitchTeams()) {
      Set<Integer> teamIds = userInfo.getSwitchAllowedTeamIds();
      if (teamIds != null) {
        for (Integer switchAllowedTeamId : teamIds) {
          TeamModelResponse teamModel = new TeamModelResponse();
          teamModel.setTeamId(switchAllowedTeamId);
          teamModel.setTeamname(
              manageDatabase.getTeamNameFromTeamId(tenantId, switchAllowedTeamId));
          teamModelList.add(teamModel);
        }
      }
    }

    return teamModelList;
  }

  public ApiResponse updateUserTeamFromSwitchTeams(UserInfoModel userProfileDetails) {
    String userIdToBeUpdated = userProfileDetails.getUsername();
    Integer newTeamId = userProfileDetails.getTeamId();
    log.info("updateProfileTeam {}", userIdToBeUpdated);

    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userIdToBeUpdated);
    boolean authorizedUser = true;

    if (!userInfo.isSwitchTeams()) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    Set<Integer> teamIds = userInfo.getSwitchAllowedTeamIds();
    if (teamIds != null) {
      if (teamIds.isEmpty()) {
        authorizedUser = false;
      } else if (!teamIds.contains(userProfileDetails.getTeamId())) {
        authorizedUser = false;
      }
    } else {
      authorizedUser = false;
    }

    if (!authorizedUser) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    String result =
        manageDatabase.getHandleDbRequests().updateUserTeam(userIdToBeUpdated, newTeamId);
    if (ApiResultStatus.SUCCESS.value.equals(result)) {
      manageDatabase.loadUsersForAllTenants();
    }
    return ApiResultStatus.SUCCESS.value.equals(result)
        ? ApiResponse.ok(result)
        : ApiResponse.notOk(result);
  }
}
