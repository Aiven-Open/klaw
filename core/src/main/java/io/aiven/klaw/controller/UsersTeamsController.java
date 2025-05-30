package io.aiven.klaw.controller;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_REGEX;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.requests.ChangePasswordRequestModel;
import io.aiven.klaw.model.requests.ProfileModel;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import io.aiven.klaw.model.requests.TeamModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.requests.UserUpdateInfoModel;
import io.aiven.klaw.model.response.RegisterUserInfoModelResponse;
import io.aiven.klaw.model.response.ResetPasswordInfo;
import io.aiven.klaw.model.response.TeamModelResponse;
import io.aiven.klaw.model.response.UserInfoModelResponse;
import io.aiven.klaw.service.UsersTeamsControllerService;
import io.aiven.klaw.validation.PermissionAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UsersTeamsController {

  @Autowired private UsersTeamsControllerService usersTeamsControllerService;

  @RequestMapping(
      value = "/getAllTeamsSU",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamModelResponse>> getAllTeamsSU() {
    return new ResponseEntity<>(usersTeamsControllerService.getAllTeamsSU(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAllTeamsSUFromRegisterUsers",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamModelResponse>> getAllTeamsSUFromRegisterUsers() {
    return new ResponseEntity<>(
        usersTeamsControllerService.getAllTeamsSUFromRegisterUsers(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAllTeamsSUOnly",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getAllTeamsSUOnly() {
    return new ResponseEntity<>(usersTeamsControllerService.getAllTeamsSUOnly(), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.ADD_EDIT_DELETE_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/deleteTeamRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteTeam(@RequestParam("teamId") Integer teamId)
      throws KlawException {
    return new ResponseEntity<>(usersTeamsControllerService.deleteTeam(teamId), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.ADD_EDIT_DELETE_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/deleteUserRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteUser(@RequestParam("userId") String userId)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.deleteUser(userId, true), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.ADD_EDIT_DELETE_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/updateUser",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateUser(
      @Valid @RequestBody UserUpdateInfoModel updateUserObj) throws KlawException {
    UserInfoModel userInfoObj = new UserInfoModel();
    copyProperties(updateUserObj, userInfoObj);
    return new ResponseEntity<>(usersTeamsControllerService.updateUser(userInfoObj), HttpStatus.OK);
  }

  @PostMapping(
      value = "/updateProfile",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody ProfileModel updateUserObj)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.updateProfile(updateUserObj), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.ADD_EDIT_DELETE_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/addNewUser",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addNewUser(@Valid @RequestBody UserInfoModel newUser)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.addNewUser(newUser, true), HttpStatus.OK);
  }

  @PostMapping(
      value = "/registerUser",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterUserInfoModel newUser)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.registerUser(newUser, true), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.APPROVE_ALL_REQUESTS_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @RequestMapping(
      value = "/getNewUserRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<RegisterUserInfoModelResponse>> getNewUserRequests()
      throws KlawNotAuthorizedException {
    return new ResponseEntity<>(usersTeamsControllerService.getNewUserRequests(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUserInfoFromRegistrationId",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<RegisterUserInfoModelResponse> getRegistrationInfoFromId(
      @RequestParam("userRegistrationId") String userRegistrationId) {
    return new ResponseEntity<>(
        usersTeamsControllerService.getRegistrationInfoFromId(userRegistrationId, "STAGING"),
        HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.APPROVE_ALL_REQUESTS_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/execNewUserRequestApprove",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> approveNewUserRequests(
      @RequestParam("username") String username) throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.approveNewUserRequests(username, true, 0, ""), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.APPROVE_ALL_REQUESTS_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/execNewUserRequestDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> declineNewUserRequests(
      @RequestParam("username") String username) throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.declineNewUserRequests(username), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.ADD_EDIT_DELETE_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/addNewTeam",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addNewTeam(@Valid @RequestBody TeamModel newTeam)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.addNewTeam(newTeam, true), HttpStatus.OK);
  }

  @PermissionAllowed(
      permissionAllowed = {
        PermissionType.ADD_EDIT_DELETE_TEAMS,
        PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES
      })
  @PostMapping(
      value = "/updateTeam",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateTeam(@Valid @RequestBody TeamModel updateTeam)
      throws KlawException {
    return new ResponseEntity<>(usersTeamsControllerService.updateTeam(updateTeam), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTeamDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TeamModelResponse> getTeamDetails(
      @RequestParam(value = "teamId") Integer teamId,
      @RequestParam(value = "tenantName") String tenantName) {
    return new ResponseEntity<>(
        usersTeamsControllerService.getTeamDetails(teamId, tenantName), HttpStatus.OK);
  }

  @PostMapping(
      value = "/chPwd",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> changePwd(
      @Valid @RequestBody ChangePasswordRequestModel changePwdModel) throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.changePwd(changePwdModel), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/showUserList",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<UserInfoModelResponse>> showUsers(
      @RequestParam(value = "teamId", defaultValue = "") Integer teamId,
      @RequestParam(value = "pageNo", defaultValue = "1") String pageNo,
      @RequestParam(value = "searchUserParam", defaultValue = "") String searchUserParam) {
    return new ResponseEntity<>(
        usersTeamsControllerService.showUsers(teamId, searchUserParam, pageNo), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getMyProfileInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<UserInfoModelResponse> getMyProfileInfo() {
    return new ResponseEntity<>(usersTeamsControllerService.getMyProfileInfo(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUserDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<UserInfoModelResponse> getUserDetails(
      @RequestParam(value = "userId") String userId) {
    return new ResponseEntity<>(
        usersTeamsControllerService.getUserInfoDetails(userId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/reset/token",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ResetPasswordInfo> resetToken(@RequestParam("username") String username) {
    return new ResponseEntity<>(
        usersTeamsControllerService.resetPasswordGenerateToken(username), HttpStatus.OK);
  }

  @PostMapping(
      value = "/reset/password",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ResetPasswordInfo> resetPasswordWithToken(
      @RequestParam("token") String token,
      @Pattern(regexp = PASSWORD_REGEX) @RequestParam("password") String password,
      @RequestParam("username") String username)
      throws KlawNotAuthorizedException {
    return new ResponseEntity<>(
        usersTeamsControllerService.resetPassword(username, password, token), HttpStatus.OK);
  }

  /*
  Retrieve the list of teams which the user can swith between, if switch teams is enabled for the user
   */
  @RequestMapping(
      value = "/user/{userId}/switchTeamsList",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamModelResponse>> getSwitchTeams(
      @PathVariable(value = "userId") String userId) {
    return new ResponseEntity<>(usersTeamsControllerService.getSwitchTeams(userId), HttpStatus.OK);
  }

  /*
  Update base team for a user.
  Base team should be one of the switch teams.
   */
  @PostMapping(
      value = "/user/updateTeam",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateUserTeamFromSwitchTeams(
      @RequestBody UserInfoModel userInfoModel) {
    return new ResponseEntity<>(
        usersTeamsControllerService.updateUserTeamFromSwitchTeams(userInfoModel), HttpStatus.OK);
  }
}
