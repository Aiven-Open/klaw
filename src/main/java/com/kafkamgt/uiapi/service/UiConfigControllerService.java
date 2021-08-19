package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.UserInfoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;


@Service
@Slf4j
public class UiConfigControllerService {

    @Value("${kafkawize.syncdata.cluster:DEV}")
    private String syncCluster;

    @Value("${kafkawize.request.topics.envs}")
    private String requestTopicsEnvs;

    @Value("${kafkawize.envs.order}")
    private String orderOfEnvs;

    @Autowired
    private UtilService utilService;

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    private ClusterApiService clusterApiService;

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    private static String baseClusterDropDownStr = " (Base Sync cluster)";

    @Autowired
    public UiConfigControllerService(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @Autowired
    public void setServices(ClusterApiService clusterApiService, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    @PostConstruct
    public void initialLoadClusters(){
        log.info("Loading clusters");
        getEnvs(true);
    }

    public Env getClusterApiStatus(){
        Env env = new Env();
        env.setHost(clusterApiService.getClusterApiUrl());
        env.setEnvStatus(clusterApiService.getClusterApiStatus());
        return env;
    }

    class TopicEnvComparator implements Comparator<String> {
        List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));

        @Override
        public int compare(String topicEnv1, String topicEnv2) {
            if(orderedEnv.indexOf(topicEnv1) > orderedEnv.indexOf(topicEnv2))
                return 1;
            else
                return -1;
        }
    }

    class TopicEnvClassComparator implements Comparator<Env> {
        List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));

        @Override
        public int compare(Env topicEnv1, Env topicEnv2) {
            if(orderedEnv.indexOf(topicEnv1.getName()) > orderedEnv.indexOf(topicEnv2.getName()))
                return 1;
            else
                return -1;
        }
    }

    public List<String> getEnvsOnly(boolean envStatus){
        List<String> envsOnly = new ArrayList<>();
        List<Env> envList = getEnvs(envStatus);

        for (Env env : envList) {
            envsOnly.add(env.getName());
        }
        envsOnly = envsOnly.stream().sorted(new TopicEnvComparator()).collect(Collectors.toList());

        return envsOnly;
    }

    public List<HashMap<String,String>> getSyncEnvs() {

        HashMap<String,String> hMap;
        List<HashMap<String,String>> envsOnly = new ArrayList<>();
        List<Env> envList = getEnvs(true);
        for (Env env : envList) {
            hMap = new HashMap<>();
            hMap.put("key", env.getName());
            if(syncCluster.equals(env.getName()))
                hMap.put("name", env.getName() + baseClusterDropDownStr);
            else
                hMap.put("name", env.getName());

            envsOnly.add(hMap);
        }

        return envsOnly;
    }

    public List<String> getEnvsBaseCluster() {
        return Arrays.asList(requestTopicsEnvs.split(","));
    }

    public List<String> getOtherEnvs() {

        List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
        List<Env> listEnvs = manageDatabase.getHandleDbRequests().selectAllKafkaEnvs();

        List<String> newList = new ArrayList();
        boolean envFound;

        for (String env : orderedEnv) {
            envFound = false;
            for (Env listEnv : listEnvs) {
                if(env.equals(listEnv.getName())) {
                    envFound = true;
                    break;
                }
            }
            if(!envFound)
                newList.add(env);
        }

        return newList;
    }

    public List<Env> getEnvs(boolean envStatus) {
        List<Env> listEnvs = manageDatabase.getHandleDbRequests().selectAllKafkaEnvs();

        if(envStatus){
            return listEnvs.stream().sorted(new TopicEnvClassComparator()).collect(Collectors.toList());
        }

        List<Env> newListEnvs = new ArrayList<>();
        for(Env oneEnv: listEnvs){
            String status;
            status = clusterApiService.getKafkaClusterStatus(oneEnv.getHost(),
                    oneEnv.getProtocol());
            oneEnv.setEnvStatus(status);
            newListEnvs.add(oneEnv);
        }

        return newListEnvs.stream().sorted(new TopicEnvClassComparator()).collect(Collectors.toList());
    }

    public HashMap<String, List<String>> getEnvParams(String targetEnv) {
        return ManageDatabase.envParamsMap.get(targetEnv);
    }

    public List<Env> getSchemaRegEnvs() {
        return manageDatabase.getHandleDbRequests().selectAllSchemaRegEnvs();
    }

    public List<Env> getSchemaRegEnvsStatus() {
        List<Env> listEnvs = manageDatabase.getHandleDbRequests().selectAllSchemaRegEnvs();
        List<Env> newListEnvs = new ArrayList<>();
        for(Env oneEnv: listEnvs){
            String status = null;
           if (oneEnv.getProtocol().equalsIgnoreCase("plaintext"))
                status = clusterApiService.getSchemaClusterStatus(oneEnv.getHost());
            else
                status = "NOT_KNOWN";
            oneEnv.setEnvStatus(status);
            newListEnvs.add(oneEnv);
        }

        return newListEnvs;
    }

    public List<Team> getAllTeams() {
        UserDetails userDetails = getUserDetails();
        return manageDatabase.getHandleDbRequests().selectAllTeamsOfUsers(userDetails.getUsername());
    }

    public List<Team> getAllTeamsSU() {
        return manageDatabase.getHandleDbRequests().selectAllTeams();
    }

    public List<String> getAllTeamsSUOnly() {
        UserDetails userDetails = getUserDetails();
        String myTeamName = manageDatabase.
                getHandleDbRequests().
                selectAllTeamsOfUsers(userDetails.getUsername()).
                get(0).
                getTeamname();

        List<String> teams = new ArrayList<>();
        List<Team> teamsList = getAllTeamsSU();
        teams.add("All teams");
        teams.add(myTeamName);

        for(Team team: teamsList) {
            if(!team.getTeamname().equals(myTeamName))
                teams.add(team.getTeamname());
        }

        return teams;
    }

    public String addNewEnv(Env newEnv){
        if(newEnv.getName().length() > 3 && newEnv.getType().equals("kafka"))
            newEnv.setName(newEnv.getName().substring(0,3));
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
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
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteClusterRequest(clusterId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String deleteTeam(String teamId){
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        String envAddResult = "{\"result\":\"Your team cannot be deleted. Try deleting other team.\"}";

        if(manageDatabase.getHandleDbRequests().getUsersInfo(userDetails.getUsername()).getTeam().equals(teamId))
            return envAddResult;

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteTeamRequest(teamId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String deleteUser(String userId){
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        String envAddResult = "{\"result\":\"User cannot be deleted\"}";

        if(userId.equals("superuser") || userDetails.getUsername().equals(userId))
            return envAddResult;

        inMemoryUserDetailsManager.deleteUser(User.withUsername(userId).toString());

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteUserRequest(userId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    private String base64EncodePwd(String pwd){
        return Base64.getEncoder().encodeToString(pwd.getBytes());
    }

    public String addNewUser(UserInfo newUser){
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        try {
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();
                inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername())
                        .password(encoder.encode(newUser.getPwd()))
                        .roles(newUser.getRole()).build());
                newUser.setPwd(base64EncodePwd(newUser.getPwd()));

            HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

            String result = dbHandle.addNewUser(newUser);

           return "{\"result\":\""+result+"\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String addNewTeam(Team newTeam){
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        try {
            return "{\"result\":\"" + manageDatabase.getHandleDbRequests().addNewTeam(newTeam) + "\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String changePwd(String changePwd){

        UserDetails userDetails = getUserDetails();

        GsonJsonParser jsonParser = new GsonJsonParser();
        Map<String, Object> pwdMap  = jsonParser.parseMap(changePwd);

        String pwdChange = (String)pwdMap.get("pwd");

        try {
            UserDetails userDetailsUpdated = getUserDetails(userDetails,
                    PasswordEncoderFactories.createDelegatingPasswordEncoder(),
                    pwdChange);
            inMemoryUserDetailsManager.updateUser(userDetailsUpdated);

            return "{\"result\":\"" + manageDatabase.getHandleDbRequests()
                    .updatePassword(userDetails.getUsername(), base64EncodePwd(pwdChange)) + "\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public List<UserInfoModel> showUsers(String teamName, String pageNo){

        List<UserInfoModel> userInfoModels = new ArrayList<>();

        List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersInfo();
        userList.forEach(userListItem -> {
            UserInfoModel userInfoModel = new UserInfoModel();
            copyProperties(userListItem,userInfoModel);
            if(teamName!=null && !teamName.equals("")) {
                if (userInfoModel.getTeam().equals(teamName))
                    userInfoModels.add(userInfoModel);
            }
            else
                userInfoModels.add(userInfoModel);
        });

        userInfoModels.sort(Comparator.comparing(UserInfoModel::getTeam));

        return getPagedUsers(pageNo, userInfoModels);
    }

    private List<UserInfoModel> getPagedUsers(String pageNo, List<UserInfoModel> userListMap){
        List<UserInfoModel> aclListMapUpdated = new ArrayList<>();

        int totalRecs = userListMap.size();
        int recsPerPage = 20;

        int totalPages = userListMap.size()/recsPerPage + (userListMap.size()%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        for(int i=0;i<totalRecs;i++) {

            if(i>=startVar && i<lastVar) {
                UserInfoModel mp = userListMap.get(i);

                mp.setTotalNoPages(totalPages + "");
                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setAllPageNos(numList);
                aclListMapUpdated.add(mp);
            }
        }
        return aclListMapUpdated;
    }

    public UserInfo getMyProfileInfo(){
        UserDetails userDetails = getUserDetails();
        return manageDatabase.getHandleDbRequests().getUsersInfo(userDetails.getUsername());
    }

    public List<ActivityLog> showActivityLog(String env, String pageNo){
        UserDetails userDetails = getUserDetails();
        List<ActivityLog> origActivityList = manageDatabase.getHandleDbRequests().selectActivityLog(userDetails.getUsername(), env);
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
       newList = newList.stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(ActivityLog::getActivityTime)))
                .collect(Collectors.toList());
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

    private UserDetails getUserDetails(){
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
