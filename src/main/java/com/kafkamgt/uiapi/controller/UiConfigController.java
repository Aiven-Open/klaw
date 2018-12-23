package com.kafkamgt.uiapi.controller;


import com.google.gson.Gson;
import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.ManageTopics;
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

    private static Logger LOG = LoggerFactory.getLogger(UiConfigController.class);

    @Autowired
    ManageTopics manageTopics;

    @RequestMapping(value = "/getEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getEnvs() {
        return new ResponseEntity<List<Env>>(manageTopics.selectAllKafkaEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSchemaRegEnvs", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Env>> getSchemaRegEnvs() {
        return new ResponseEntity<List<Env>>(manageTopics.selectAllSchemaRegEnvs(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeams", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Team>> getAllTeams() {
        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return new ResponseEntity<List<Team>>(manageTopics.selectAllTeamsOfUsers(userDetails.getUsername()), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllTeamsSU", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Team>> getAllTeamsSU() {

        return new ResponseEntity<List<Team>>(manageTopics.selectAllTeams(), HttpStatus.OK);
    }

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public UiConfigController(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @PostMapping(value = "/addNewEnv")
    public ResponseEntity<String> addNewEnv(@RequestParam ("addNewEnv") String addNewEnv){

        LOG.info("*********"+addNewEnv);

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        String json = "";
        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"result\": \"Not Authorized\" }";
            return new ResponseEntity<String>(json, HttpStatus.OK);
        }

        Gson gson = new Gson();

        Env newEnv = gson.fromJson(addNewEnv, Env.class);

        newEnv.setTruststorepwd("");
        newEnv.setKeypwd("");
        newEnv.setKeystorepwd("");
        newEnv.setTruststorelocation("");
        newEnv.setKeystorelocation("");
        String execRes = manageTopics.addNewEnv(newEnv);

        String envAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(envAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/addNewUser")
    public ResponseEntity<String> addNewUser(@RequestParam ("addNewUser") String addNewUser){

        LOG.info("*********"+addNewUser);
        Gson gson = new Gson();

        UserInfo newUser = gson.fromJson(addNewUser, UserInfo.class);

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername()).password(encoder.encode(newUser.getPwd()))
                .roles(newUser.getRole()).build());

        String execRes = manageTopics.addNewUser(newUser);

        String userAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(userAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/addNewTeam")
    public ResponseEntity<String> addNewTeam(@RequestParam ("addNewTeam") String addNewTeam){

        LOG.info("*********"+addNewTeam);
        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        String json = "";
        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"result\": \"Not Authorized\" }";
            return new ResponseEntity<String>(json, HttpStatus.OK);
        }

        Gson gson = new Gson();

        Team newTeam = gson.fromJson(addNewTeam, Team.class);

        String execRes = manageTopics.addNewTeam(newTeam);

        String teamAddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(teamAddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/chPwd")
    public ResponseEntity<String> changePwd(@RequestParam ("changePwd") String changePwd){

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        GsonJsonParser jsonParser = new GsonJsonParser();
        Map<String, Object> pwdMap  = jsonParser.parseMap(changePwd);

        String pwdChange = (String)pwdMap.get("pwd");

//        String oldPwd = inMemoryUserDetailsManager.loadUserByUsername(userDetails.getUsername()).getPassword();

//        LOG.info(oldPwd+"-----"+inMemoryUserDetailsManager.userExists("uiuser5")+
//                "---"+userDetails.getUsername()+"-----"+pwdChange);
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
        return new ResponseEntity<String>(pwdChResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/showUserList", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<UserInfo>> showUsers(){

        List<UserInfo> userList = manageTopics.selectAllUsersInfo();

        //LOG.info(userList + " --- userList ");

        return new ResponseEntity<List<UserInfo>>(userList, HttpStatus.OK);
    }

    @RequestMapping(value = "/getMyProfileInfo", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<UserInfo> getMyProfileInfo(){

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserInfo userList = manageTopics.getUsersInfo(userDetails.getUsername());

        LOG.info(userList + " --- userList ");

        return new ResponseEntity<UserInfo>(userList, HttpStatus.OK);
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ActivityLog>> showActivityLog(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo){

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<ActivityLog> origActivityList = manageTopics.selectActivityLog(userDetails.getUsername(), env);

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
        return new ResponseEntity<List<ActivityLog>>(newList, HttpStatus.OK);
    }
}
