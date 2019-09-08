package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.service.ManageTopics;
import com.kafkamgt.uiapi.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/")
public class UiConfigController {

    //private static Logger LOG = LoggerFactory.getLogger(UiConfigController.class);

    @Autowired
    private UtilService utilService;

    @Autowired
    private ManageTopics manageTopics;

    @RequestMapping(value = "/getEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getEnvs() {
        return new ResponseEntity<>(manageTopics.selectAllKafkaEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSchemaRegEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getSchemaRegEnvs() {
        return new ResponseEntity<>(manageTopics.selectAllSchemaRegEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeams", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Team>> getAllTeams() {
        return new ResponseEntity<>(manageTopics.selectAllTeamsOfUsers(utilService.getUserName()), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeamsSU", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Team>> getAllTeamsSU() {

        return new ResponseEntity<>(manageTopics.selectAllTeams(), HttpStatus.OK);
    }

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public UiConfigController(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @PostMapping(value = "/addNewEnv")
    public ResponseEntity<String> addNewEnv(@RequestBody Env newEnv){

        if(!utilService.checkAuthorizedSU())
            return new ResponseEntity<>("{ \"result\": \"Not Authorized\" }", HttpStatus.OK);

        newEnv.setTrustStorePwd("");
        newEnv.setKeyPwd("");
        newEnv.setKeyStorePwd("");
        newEnv.setTrustStoreLocation("");
        newEnv.setKeyStoreLocation("");
        String execRes = manageTopics.addNewEnv(newEnv);

        String envAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<>(envAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/deleteClusterRequest")
    public ResponseEntity<String> deleteCluster(@RequestParam ("clusterId") String clusterId){

        if(!utilService.checkAuthorizedSU())
            return new ResponseEntity<>("{ \"result\": \"Not Authorized\" }", HttpStatus.OK);

        String execRes = manageTopics.deleteClusterRequest(clusterId);

        String envAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<>(envAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/deleteTeamRequest")
    public ResponseEntity<String> deleteTeam(@RequestParam ("teamId") String teamId){

        if(!utilService.checkAuthorizedSU())
            return new ResponseEntity<>("{ \"result\": \"Not Authorized\" }", HttpStatus.OK);

        String envAddResult = "{\"result\":\"Your team cannot be deleted. Try deleting other team.\"}";

        if(manageTopics.getUsersInfo(utilService.getUserName()).getTeam().equals(teamId))
            return new ResponseEntity<>(envAddResult, HttpStatus.OK);

        String execRes = manageTopics.deleteTeamRequest(teamId);
        envAddResult = "{\"result\":\""+execRes+"\"}";

        return new ResponseEntity<>(envAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/deleteUserRequest")
    public ResponseEntity<String> deleteUser(@RequestParam ("userId") String userId){

        if(!utilService.checkAuthorizedSU())
            return new ResponseEntity<>("{ \"result\": \"Not Authorized\" }", HttpStatus.OK);

        String envAddResult = "{\"result\":\"User cannot be deleted\"}";

        if(userId.equals("superuser") || utilService.getUserName().equals(userId))
            return new ResponseEntity<>(envAddResult, HttpStatus.OK);

        String execRes = manageTopics.deleteUserRequest(userId);
        envAddResult = "{\"result\":\""+execRes+"\"}";

        return new ResponseEntity<>(envAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/addNewUser")
    public ResponseEntity<String> addNewUser(@RequestBody UserInfo newUser){

        if(!utilService.checkAuthorizedSU())
            return new ResponseEntity<>("{ \"result\": \"Not Authorized\" }", HttpStatus.OK);

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername()).password(encoder.encode(newUser.getPwd()))
                .roles(newUser.getRole()).build());

        String execRes = manageTopics.addNewUser(newUser);

        String userAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<>(userAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/addNewTeam")
    public ResponseEntity<String> addNewTeam(@RequestBody Team newTeam){

        if(!utilService.checkAuthorizedSU())
            return new ResponseEntity<>("{ \"result\": \"Not Authorized\" }", HttpStatus.OK);

        String execRes = manageTopics.addNewTeam(newTeam);

        String teamAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<>(teamAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/chPwd")
    public ResponseEntity<String> changePwd(@RequestParam ("changePwd") String changePwd){

        UserDetails userDetails = utilService.getUserDetails();

        GsonJsonParser jsonParser = new GsonJsonParser();
        Map<String, Object> pwdMap  = jsonParser.parseMap(changePwd);

        String pwdChange = (String)pwdMap.get("pwd");

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserDetails ud = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return userDetails.getAuthorities();
            }

            @Override
            public String getPassword() {
                return encoder.encode(pwdChange);
            }

            @Override
            public String getUsername() {
                return userDetails.getUsername();
            }

            @Override
            public boolean isAccountNonExpired() {
                return userDetails.isAccountNonExpired();
            }

            @Override
            public boolean isAccountNonLocked() {
                return userDetails.isAccountNonLocked();
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return userDetails.isCredentialsNonExpired();
            }

            @Override
            public boolean isEnabled() {
                return userDetails.isEnabled();
            }
        };

        inMemoryUserDetailsManager.updateUser(ud);

        String execRes = manageTopics.updatePassword(userDetails.getUsername(),pwdChange);

        String pwdChResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<>(pwdChResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/showUserList", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<UserInfo>> showUsers(){

        List<UserInfo> userList = manageTopics.selectAllUsersInfo();

        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    @RequestMapping(value = "/getMyProfileInfo", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserInfo> getMyProfileInfo(){

        UserInfo userList = manageTopics.getUsersInfo(utilService.getUserName());

        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ActivityLog>> showActivityLog(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo){

        List<ActivityLog> origActivityList = manageTopics.selectActivityLog(utilService.getUserName(), env);

        int totalRecs = origActivityList.size();
        int recsPerPage = 20;

        int requestPageNo = Integer.parseInt(pageNo);
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        List<ActivityLog> newList = new ArrayList<>();

        List<String> numList = new ArrayList<>();
        for (int k = 1; k <= totalPages; k++) {
            numList.add("" + k);
        }
         for(int i=0;i<totalRecs;i++){
             ActivityLog activityLog = origActivityList.get(i);
            if(i>=startVar && i<lastVar) {
                activityLog.setAllPageNos(numList);
                activityLog.setTotalNoPages("" + totalPages);

                newList.add(activityLog);
            }
        }
        return new ResponseEntity<>(newList, HttpStatus.OK);
    }


}
