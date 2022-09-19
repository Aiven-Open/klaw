package io.aiven.klaw.controller;

import io.aiven.klaw.model.RegisterSaasUserInfoModel;
import io.aiven.klaw.model.RegisterUserInfoModel;
import io.aiven.klaw.model.TeamModel;
import io.aiven.klaw.model.UserInfoModel;
import io.aiven.klaw.service.SaasService;
import io.aiven.klaw.service.UsersTeamsControllerService;
import java.util.HashMap;
import java.util.List;
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
  public ResponseEntity<String> deleteTeam(@RequestParam("teamId") Integer teamId) {

    return new ResponseEntity<>(usersTeamsControllerService.deleteTeam(teamId), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteUserRequest")
  public ResponseEntity<String> deleteUser(@RequestParam("userId") String userId) {

    return new ResponseEntity<>(
        usersTeamsControllerService.deleteUser(userId, true), HttpStatus.OK);
  }

  @PostMapping(value = "/updateUser")
  public ResponseEntity<String> updateUser(@Valid @RequestBody UserInfoModel updateUserObj) {
    return new ResponseEntity<>(
        usersTeamsControllerService.updateUser(updateUserObj), HttpStatus.OK);
  }

  @PostMapping(value = "/updateProfile")
  public ResponseEntity<HashMap<String, String>> updateProfile(
      @Valid @RequestBody UserInfoModel updateUserObj) {
    return new ResponseEntity<>(
        usersTeamsControllerService.updateProfile(updateUserObj), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewUser")
  public ResponseEntity<HashMap<String, String>> addNewUser(
      @Valid @RequestBody UserInfoModel newUser) {

    try {
      HashMap<String, String> response = usersTeamsControllerService.addNewUser(newUser, true);
      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      HashMap<String, String> resMap = new HashMap<>();
      resMap.put("result", "Failure. Unable to create the user.");
      return new ResponseEntity<>(resMap, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(value = "/registerUser")
  public ResponseEntity<HashMap<String, String>> registerUser(
      @Valid @RequestBody RegisterUserInfoModel newUser) throws Exception {
    try {
      return new ResponseEntity<>(
          usersTeamsControllerService.registerUser(newUser, true), HttpStatus.OK);
    } catch (Exception e) {
      HashMap<String, String> resMap = new HashMap<>();
      resMap.put("result", "Failure. " + e.getMessage());
      return new ResponseEntity<>(resMap, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(value = "/registerUserSaas")
  public ResponseEntity<HashMap<String, String>> registerUserSaas(
      @Valid @RequestBody RegisterSaasUserInfoModel newUser) throws Exception {
    try {
      return new ResponseEntity<>(saasService.registerUserSaas(newUser), HttpStatus.OK);
    } catch (Exception e) {
      HashMap<String, String> resMap = new HashMap<>();
      resMap.put("result", "Failure. Something went wrong. Please try later.");
      return new ResponseEntity<>(resMap, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(
      value = "/getActivationInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getActivationInfo(
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
  public ResponseEntity<String> approveNewUserRequests(@RequestParam("username") String username) {
    return new ResponseEntity<>(
        usersTeamsControllerService.approveNewUserRequests(username, true, 0, ""), HttpStatus.OK);
  }

  @PostMapping(value = "/execNewUserRequestDecline")
  public ResponseEntity<String> declineNewUserRequests(@RequestParam("username") String username) {
    return new ResponseEntity<>(
        usersTeamsControllerService.declineNewUserRequests(username), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewTeam")
  public ResponseEntity<String> addNewTeam(@Valid @RequestBody TeamModel newTeam) {
    return new ResponseEntity<>(
        usersTeamsControllerService.addNewTeam(newTeam, true), HttpStatus.OK);
  }

  @PostMapping(value = "/updateTeam")
  public ResponseEntity<String> updateTeam(@Valid @RequestBody TeamModel updateTeam) {
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
  public ResponseEntity<String> changePwd(@RequestParam("changePwd") String changePwd) {
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
  public ResponseEntity<HashMap<String, String>> resetPassword(
      @RequestParam("username") String username) {
    return new ResponseEntity<>(usersTeamsControllerService.resetPassword(username), HttpStatus.OK);
  }
}
