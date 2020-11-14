package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.UserInfoModel;
import com.kafkamgt.uiapi.service.UiConfigControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/")
public class UiConfigController {

    @Autowired
    private UiConfigControllerService uiConfigControllerService;

    @RequestMapping(value = "/getEnvsBaseCluster", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<String>> getEnvsBaseCluster() {
        return new ResponseEntity<>(uiConfigControllerService.getEnvsBaseCluster(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getEnvs() {
        return new ResponseEntity<>(uiConfigControllerService.getEnvs(true), HttpStatus.OK);
    }

    @RequestMapping(value = "/getOtherEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<String>> getOtherEnvs() {
        return new ResponseEntity<>(uiConfigControllerService.getOtherEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getEnvsOnly", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<String>> getEnvsOnly() {
        return new ResponseEntity<>(uiConfigControllerService.getEnvsOnly(true), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncEnv", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<HashMap<String,String>>> getSyncEnv() {
        return new ResponseEntity<>(uiConfigControllerService.getSyncEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getEnvsStatus", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getEnvsStatus() {
        return new ResponseEntity<>(uiConfigControllerService.getEnvs(false), HttpStatus.OK);
    }

    @RequestMapping(value = "/getEnvParams", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, List<String>>> getEnvParams(@RequestParam(value="envSelected") String envSelected)
            throws KafkawizeException {
        return new ResponseEntity<>(uiConfigControllerService.getEnvParams(envSelected), HttpStatus.OK);
    }

    @RequestMapping(value = "/getClusterApiStatus", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Env> getClusterApiStatus() {
        return new ResponseEntity<>(uiConfigControllerService.getClusterApiStatus(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSchemaRegEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getSchemaRegEnvs() {
        return new ResponseEntity<>(uiConfigControllerService.getSchemaRegEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSchemaRegEnvsStatus", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getSchemaRegEnvsStatus() {
        return new ResponseEntity<>(uiConfigControllerService.getSchemaRegEnvsStatus(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeams", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Team>> getAllTeams() {
        return new ResponseEntity<>(uiConfigControllerService.getAllTeams(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeamsSU", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Team>> getAllTeamsSU() {
        return new ResponseEntity<>(uiConfigControllerService.getAllTeamsSU(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeamsSUOnly", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<String>> getAllTeamsSUOnly() {
        return new ResponseEntity<>(uiConfigControllerService.getAllTeamsSUOnly(), HttpStatus.OK);
    }

    @PostMapping(value = "/addNewEnv")
    public ResponseEntity<String> addNewEnv(@RequestBody Env newEnv){
        return new ResponseEntity<>(uiConfigControllerService.addNewEnv(newEnv), HttpStatus.OK);
    }

    @PostMapping(value = "/deleteClusterRequest")
    public ResponseEntity<String> deleteCluster(@RequestParam ("clusterId") String clusterId){

        return new ResponseEntity<>(uiConfigControllerService.deleteCluster(clusterId), HttpStatus.OK);
    }

    @PostMapping(value = "/deleteTeamRequest")
    public ResponseEntity<String> deleteTeam(@RequestParam ("teamId") String teamId){

        return new ResponseEntity<>(uiConfigControllerService.deleteTeam(teamId), HttpStatus.OK);
    }

    @PostMapping(value = "/deleteUserRequest")
    public ResponseEntity<String> deleteUser(@RequestParam ("userId") String userId){

        return new ResponseEntity<>(uiConfigControllerService.deleteUser(userId), HttpStatus.OK);
    }

    @PostMapping(value = "/addNewUser")
    public ResponseEntity<String> addNewUser(@RequestBody UserInfo newUser){
        return new ResponseEntity<>(uiConfigControllerService.addNewUser(newUser), HttpStatus.OK);
    }

    @PostMapping(value = "/addNewTeam")
    public ResponseEntity<String> addNewTeam(@RequestBody Team newTeam){
        return new ResponseEntity<>(uiConfigControllerService.addNewTeam(newTeam), HttpStatus.OK);
    }

    @PostMapping(value = "/chPwd")
    public ResponseEntity<String> changePwd(@RequestParam ("changePwd") String changePwd){
        return new ResponseEntity<>(uiConfigControllerService.changePwd(changePwd), HttpStatus.OK);
    }

    @RequestMapping(value = "/showUserList", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<UserInfoModel>> showUsers(){
        return new ResponseEntity<>(uiConfigControllerService.showUsers(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getMyProfileInfo", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserInfo> getMyProfileInfo(){
        return new ResponseEntity<>(uiConfigControllerService.getMyProfileInfo(), HttpStatus.OK);
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ActivityLog>> showActivityLog(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo){
        return new ResponseEntity<>(uiConfigControllerService.showActivityLog(env, pageNo), HttpStatus.OK);
    }


}
