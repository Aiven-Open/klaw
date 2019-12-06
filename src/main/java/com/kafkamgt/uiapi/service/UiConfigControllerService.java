package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.error.KafkawizeException;
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

    //private static Logger LOG = LoggerFactory.getLogger(UiConfigController.class);

    @Autowired
    private UtilService utilService;

    @Autowired
    private ManageTopics manageTopics;

    @Autowired
    private ClusterApiService clusterApiService;

    public Env getClusterApiStatus(){
        Env env = new Env();
        env.setHost(clusterApiService.clusterConnUrl);
        try {
            env.setEnvStatus(clusterApiService.getClusterApiStatus());
        } catch (KafkawizeException e) {
            e.printStackTrace();
            env.setEnvStatus("OFFLINE");
        }
        return env;
    }

    public List<Env> getEnvs(boolean envStatus) {

        if(envStatus)
            return manageTopics.selectAllKafkaEnvs();

        List<Env> listEnvs = manageTopics.selectAllKafkaEnvs();
        List<Env> newListEnvs = new ArrayList<>();
        for(Env oneEnv: listEnvs){
            String status = null;
            try {
                if(oneEnv.getProtocol().equalsIgnoreCase("plain"))
                    status = clusterApiService.getKafkaClusterStatus(oneEnv.getHost()+":"+oneEnv.getPort());
                else
                    status = "NOT_KNOWN";
            } catch (KafkawizeException e) {
                e.printStackTrace();
                return newListEnvs;
            }
            oneEnv.setEnvStatus(status);
            newListEnvs.add(oneEnv);
        }

        return newListEnvs;
    }

    public List<Env> getSchemaRegEnvs() {
        return manageTopics.selectAllSchemaRegEnvs();
    }

    public List<Env> getSchemaRegEnvsStatus() {
        List<Env> listEnvs = manageTopics.selectAllSchemaRegEnvs();
        List<Env> newListEnvs = new ArrayList<>();
        for(Env oneEnv: listEnvs){
            String status = null;
            if(oneEnv.getProtocol().equalsIgnoreCase("plain"))
                status = clusterApiService.getSchemaClusterStatus(oneEnv.getHost()+":"+oneEnv.getPort());
            else
                status = "NOT_KNOWN";
            oneEnv.setEnvStatus(status);
            newListEnvs.add(oneEnv);
        }

        return newListEnvs;
    }

    public List<Team> getAllTeams() {
        return manageTopics.selectAllTeamsOfUsers(utilService.getUserName());
    }

    public List<Team> getAllTeamsSU() {

        return manageTopics.selectAllTeams();
    }

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public UiConfigControllerService(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    public String addNewEnv(Env newEnv){

        if(!utilService.checkAuthorizedSU())
            return "{ \"result\": \"Not Authorized\" }";

        newEnv.setTrustStorePwd("");
        newEnv.setKeyPwd("");
        newEnv.setKeyStorePwd("");
        newEnv.setTrustStoreLocation("");
        newEnv.setKeyStoreLocation("");
        String execRes = manageTopics.addNewEnv(newEnv);

        return "{\"result\":\""+execRes+"\"}";
    }

    public String deleteCluster(String clusterId){

        if(!utilService.checkAuthorizedSU())
            return "{ \"result\": \"Not Authorized\" }";

        String execRes = manageTopics.deleteClusterRequest(clusterId);

        return "{\"result\":\""+execRes+"\"}";
    }

    public String deleteTeam(String teamId){

        if(!utilService.checkAuthorizedSU())
            return "{ \"result\": \"Not Authorized\" }";

        String envAddResult = "{\"result\":\"Your team cannot be deleted. Try deleting other team.\"}";

        if(manageTopics.getUsersInfo(utilService.getUserName()).getTeam().equals(teamId))
            return envAddResult;

        String execRes = manageTopics.deleteTeamRequest(teamId);
        envAddResult = "{\"result\":\""+execRes+"\"}";

        return envAddResult;
    }

    public String deleteUser(String userId){

        if(!utilService.checkAuthorizedSU())
            return "{ \"result\": \"Not Authorized\" }";

        String envAddResult = "{\"result\":\"User cannot be deleted\"}";

        if(userId.equals("superuser") || utilService.getUserName().equals(userId))
            return envAddResult;

        String execRes = manageTopics.deleteUserRequest(userId);
        envAddResult = "{\"result\":\""+execRes+"\"}";

        return envAddResult;
    }

    public String addNewUser(UserInfo newUser){

        if(!utilService.checkAuthorizedSU())
            return "{ \"result\": \"Not Authorized\" }";

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername()).password(encoder.encode(newUser.getPwd()))
                .roles(newUser.getRole()).build());

        String execRes = manageTopics.addNewUser(newUser);

        return "{\"result\":\""+execRes+"\"}";
    }

    public String addNewTeam(Team newTeam){

        if(!utilService.checkAuthorizedSU())
            return "{ \"result\": \"Not Authorized\" }";

        return "{\"result\":\""+manageTopics.addNewTeam(newTeam)+"\"}";
    }

    public String changePwd(String changePwd){

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

        return "{\"result\":\""+execRes+"\"}";
    }

    public List<UserInfo> showUsers(){

        return manageTopics.selectAllUsersInfo();
    }

    public UserInfo getMyProfileInfo(){

        return manageTopics.getUsersInfo(utilService.getUserName());
    }

    public List<ActivityLog> showActivityLog(String env, String pageNo){

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
        return newList;
    }


}
