package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.RegisterSaasUserInfoModel;
import io.aiven.klaw.model.RegisterUserInfoModel;
import io.aiven.klaw.model.TeamModel;
import io.aiven.klaw.model.UserInfoModel;
import io.aiven.klaw.service.SaasService;
import io.aiven.klaw.service.UsersTeamsControllerService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UsersTeamsController {

  @Autowired private UsersTeamsControllerService usersTeamsControllerService;

  @Autowired private SaasService saasService;

  @RequestMapping(
      value = "/getAllTeamsSU",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamModel>> getAllTeamsSU() {
    return new ResponseEntity<>(usersTeamsControllerService.getAllTeamsSU(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAllTeamsSUFromRegisterUsers",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamModel>> getAllTeamsSUFromRegisterUsers() {
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

  @PostMapping(value = "/deleteTeamRequest")
  public ResponseEntity<ApiResponse> deleteTeam(@RequestParam("teamId") Integer teamId)
      throws KlawException {
    return new ResponseEntity<>(usersTeamsControllerService.deleteTeam(teamId), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteUserRequest")
  public ResponseEntity<ApiResponse> deleteUser(@RequestParam("userId") String userId)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.deleteUser(userId, true), HttpStatus.OK);
  }

  @PostMapping(value = "/updateUser")
  public ResponseEntity<ApiResponse> updateUser(@Valid @RequestBody UserInfoModel updateUserObj)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.updateUser(updateUserObj), HttpStatus.OK);
  }

  @PostMapping(value = "/updateProfile")
  public ResponseEntity<ApiResponse> updateProfile(@Valid @RequestBody UserInfoModel updateUserObj)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.updateProfile(updateUserObj), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewUser")
  public ResponseEntity<ApiResponse> addNewUser(@Valid @RequestBody UserInfoModel newUser)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.addNewUser(newUser, true), HttpStatus.OK);
  }

  @PostMapping(value = "/registerUser")
  public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterUserInfoModel newUser)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.registerUser(newUser, true), HttpStatus.OK);
  }

  @PostMapping(value = "/registerUserSaas")
  public ResponseEntity<ApiResponse> registerUserSaas(
      @Valid @RequestBody RegisterSaasUserInfoModel newUser) throws Exception {
    return new ResponseEntity<>(saasService.registerUserSaas(newUser), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getActivationInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getActivationInfo(
      @RequestParam("userActivationId") String userActivationId) {
    return new ResponseEntity<>(saasService.getActivationInfo(userActivationId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getNewUserRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<RegisterUserInfoModel>> getNewUserRequests() {
    return new ResponseEntity<>(usersTeamsControllerService.getNewUserRequests(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUserInfoFromRegistrationId",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<RegisterUserInfoModel> getRegistrationInfoFromId(
      @RequestParam("userRegistrationId") String userRegistrationId) {
    return new ResponseEntity<>(
        usersTeamsControllerService.getRegistrationInfoFromId(userRegistrationId, "STAGING"),
        HttpStatus.OK);
  }

  @PostMapping(value = "/execNewUserRequestApprove")
  public ResponseEntity<ApiResponse> approveNewUserRequests(
      @RequestParam("username") String username) throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.approveNewUserRequests(username, true, 0, ""), HttpStatus.OK);
  }

  @PostMapping(value = "/execNewUserRequestDecline")
  public ResponseEntity<ApiResponse> declineNewUserRequests(
      @RequestParam("username") String username) throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.declineNewUserRequests(username), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewTeam")
  public ResponseEntity<ApiResponse> addNewTeam(@Valid @RequestBody TeamModel newTeam)
      throws KlawException {
    return new ResponseEntity<>(
        usersTeamsControllerService.addNewTeam(newTeam, true), HttpStatus.OK);
  }

  @PostMapping(value = "/updateTeam")
  public ResponseEntity<ApiResponse> updateTeam(@Valid @RequestBody TeamModel updateTeam)
      throws KlawException {
    return new ResponseEntity<>(usersTeamsControllerService.updateTeam(updateTeam), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTeamDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TeamModel> getTeamDetails(
      @RequestParam(value = "teamId") Integer teamId,
      @RequestParam(value = "tenantName") String tenantName) {
    return new ResponseEntity<>(
        usersTeamsControllerService.getTeamDetails(teamId, tenantName), HttpStatus.OK);
  }

  @PostMapping(value = "/chPwd")
  public ResponseEntity<ApiResponse> changePwd(@RequestParam("changePwd") String changePwd)
      throws KlawException {
    return new ResponseEntity<>(usersTeamsControllerService.changePwd(changePwd), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/showUserList",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<UserInfoModel>> showUsers(
      @RequestParam("teamName") String teamName,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "searchUserParam", defaultValue = "") String searchUserParam) {
    return new ResponseEntity<>(
        usersTeamsControllerService.showUsers(teamName, searchUserParam, pageNo), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getMyProfileInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<UserInfoModel> getMyProfileInfo() {
    return new ResponseEntity<>(usersTeamsControllerService.getMyProfileInfo(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUserDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<UserInfoModel> getUserDetails(
      @RequestParam(value = "userId") String userId) {
    return new ResponseEntity<>(
        usersTeamsControllerService.getUserInfoDetails(userId), HttpStatus.OK);
  }

  @PostMapping(value = "/resetPassword")
  public ResponseEntity<Map<String, String>> resetPassword(
      @RequestParam("username") String username) {
    return new ResponseEntity<>(usersTeamsControllerService.resetPassword(username), HttpStatus.OK);
  }
}
