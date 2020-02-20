package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class UiConfigControllerService {

    @Autowired
    private UtilService utilService;

    //private HandleDbRequests manageDatabase.getHandleDbRequests() = ManageDatabase.manageDatabase.getHandleDbRequests();

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    private ClusterApiService clusterApiService;

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public UiConfigControllerService(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @Autowired
    public void setServices(ClusterApiService clusterApiService, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    public Env getClusterApiStatus(){
        Env env = new Env();
        env.setHost(clusterApiService.getClusterApiUrl());
        env.setEnvStatus(clusterApiService.getClusterApiStatus());
        return env;
    }

    public List<Env> getEnvs(boolean envStatus) {

        if(envStatus)
            return manageDatabase.getHandleDbRequests().selectAllKafkaEnvs();

        List<Env> listEnvs = manageDatabase.getHandleDbRequests().selectAllKafkaEnvs();
        List<Env> newListEnvs = new ArrayList<>();
        for(Env oneEnv: listEnvs){
            String status;
            if(oneEnv.getProtocol().equalsIgnoreCase("plain"))
                status = clusterApiService.getKafkaClusterStatus(oneEnv.getHost()+":"+oneEnv.getPort());
            else
                status = "NOT_KNOWN";
            oneEnv.setEnvStatus(status);
            newListEnvs.add(oneEnv);
        }

        return newListEnvs;
    }

    public List<Env> getSchemaRegEnvs() {
        return manageDatabase.getHandleDbRequests().selectAllSchemaRegEnvs();
    }

    public List<Env> getSchemaRegEnvsStatus() {
        List<Env> listEnvs = manageDatabase.getHandleDbRequests().selectAllSchemaRegEnvs();
        List<Env> newListEnvs = new ArrayList<>();
        for(Env oneEnv: listEnvs){
            String status = null;
           if (oneEnv.getProtocol().equalsIgnoreCase("plain"))
                status = clusterApiService.getSchemaClusterStatus(oneEnv.getHost() + ":" + oneEnv.getPort());
            else
                status = "NOT_KNOWN";
            oneEnv.setEnvStatus(status);
            newListEnvs.add(oneEnv);
        }

        return newListEnvs;
    }

    public List<Team> getAllTeams() {
        return manageDatabase.getHandleDbRequests().selectAllTeamsOfUsers(utilService.getUserName());
    }

    public List<Team> getAllTeamsSU() {
        return manageDatabase.getHandleDbRequests().selectAllTeams();
    }

    public String addNewEnv(Env newEnv){

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        newEnv.setTrustStorePwd("");
        newEnv.setKeyPwd("");
        newEnv.setKeyStorePwd("");
        newEnv.setTrustStoreLocation("");
        newEnv.setKeyStoreLocation("");
        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().addNewEnv(newEnv)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String deleteCluster(String clusterId){

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteClusterRequest(clusterId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String deleteTeam(String teamId){

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        String envAddResult = "{\"result\":\"Your team cannot be deleted. Try deleting other team.\"}";

        if(manageDatabase.getHandleDbRequests().getUsersInfo(utilService.getUserName()).getTeam().equals(teamId))
            return envAddResult;

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteTeamRequest(teamId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String deleteUser(String userId){

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        String envAddResult = "{\"result\":\"User cannot be deleted\"}";

        if(userId.equals("superuser") || utilService.getUserName().equals(userId))
            return envAddResult;

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteUserRequest(userId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String addNewUser(UserInfo newUser){

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        try {
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();

            inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername()).password(encoder.encode(newUser.getPwd()))
                    .roles(newUser.getRole()).build());

           return "{\"result\":\""+manageDatabase.getHandleDbRequests().addNewUser(newUser)+"\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String addNewTeam(Team newTeam){

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        try {
            return "{\"result\":\"" + manageDatabase.getHandleDbRequests().addNewTeam(newTeam) + "\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String changePwd(String changePwd){

        UserDetails userDetails = utilService.getUserDetails();

        GsonJsonParser jsonParser = new GsonJsonParser();
        Map<String, Object> pwdMap  = jsonParser.parseMap(changePwd);

        String pwdChange = (String)pwdMap.get("pwd");

        try {
            UserDetails userDetailsUpdated = getUserDetails(userDetails,
                    PasswordEncoderFactories.createDelegatingPasswordEncoder(),
                    pwdChange);
            inMemoryUserDetailsManager.updateUser(userDetailsUpdated);

            return "{\"result\":\"" + manageDatabase.getHandleDbRequests().updatePassword(userDetails.getUsername(), pwdChange) + "\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public List<UserInfo> showUsers(){

        return manageDatabase.getHandleDbRequests().selectAllUsersInfo();
    }

    public UserInfo getMyProfileInfo(){

        return manageDatabase.getHandleDbRequests().getUsersInfo(utilService.getUserName());
    }

    public List<ActivityLog> showActivityLog(String env, String pageNo){

        List<ActivityLog> origActivityList = manageDatabase.getHandleDbRequests().selectActivityLog(utilService.getUserName(), env);
        List<ActivityLog> newList = new ArrayList<>();

        if(origActivityList!=null && origActivityList.size() > 0) {
            int totalRecs = origActivityList.size();
            int recsPerPage = 20;

            int requestPageNo = Integer.parseInt(pageNo);
            int startVar = (requestPageNo - 1) * recsPerPage;
            int lastVar = (requestPageNo) * (recsPerPage);

            int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);

            List<String> numList = new ArrayList<>();
            for (int k = 1; k <= totalPages; k++) {
                numList.add("" + k);
            }
            for (int i = 0; i < totalRecs; i++) {
                ActivityLog activityLog = origActivityList.get(i);
                if (i >= startVar && i < lastVar) {
                    activityLog.setAllPageNos(numList);
                    activityLog.setTotalNoPages("" + totalPages);

                    newList.add(activityLog);
                }
            }
        }
        return newList;
    }

    private UserDetails getUserDetails(UserDetails userDetails, PasswordEncoder encoder, String pwdChange){
        return new UserDetails() {
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
    }
}
