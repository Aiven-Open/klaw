package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.*;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.beans.BeanUtils.copyProperties;
import static com.kafkamgt.uiapi.service.KwConstants.*;

@Service
@Slf4j
public class UsersTeamsControllerService {

    @Value("${kafkawize.login.authentication.type}")
    private String authenticationType;

    @Value("${kafkawize.enable.authorization.ad:false}")
    private String adAuthRoleEnabled;

    @Value("${kafkawize.jasypt.encryptor.secretkey}")
    private String encryptorSecretKey;

    @Value("${kafkawize.installation.type:onpremise}")
    private String kwInstallationType;

    @Autowired
    private MailUtils mailService;

    @Autowired
    private CommonUtilsService commonUtilsService;

    @Autowired
    ManageDatabase manageDatabase;

    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    @Autowired
    public UsersTeamsControllerService(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    public UserInfoModel getUserInfoDetails(String userId) {
        UserInfoModel userInfoModel = new UserInfoModel();
        UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
        if(userInfo != null) {
            copyProperties(userInfo, userInfoModel);
            userInfoModel.setTeam(manageDatabase.getTeamNameFromTeamId(userInfo.getTenantId(),
                    userInfoModel.getTeamId()));
            userInfoModel.setUserPassword("*******");
            return userInfoModel;
        }
        else return null;
    }

    public HashMap<String, String> updateProfile(UserInfoModel updateUserObj) {
        log.info("updateProfile {}", updateUserObj);
        HashMap<String, String> updateProfileResult = new HashMap<>();
        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

        UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(getUserName());
        userInfo.setFullname(updateUserObj.getFullname());
        userInfo.setMailid(updateUserObj.getMailid());

        updateProfileResult.put("result",dbHandle.updateUser(userInfo));
        return updateProfileResult;
    }

    public String updateUser(UserInfoModel newUser) {
        log.info("updateUser {}", newUser.getUsername());

        if(commonUtilsService.isNotAuthorizedUser(getUserName(), PermissionType.ADD_EDIT_DELETE_USERS))
            return "{\"result\":\"Not Authorized\"}";

        UserInfo existingUserInfo = manageDatabase.getHandleDbRequests().getUsersInfo(newUser.getUsername());
        int tenantId = commonUtilsService.getTenantId(getUserName());
        List<String> permissions = manageDatabase.getRolesPermissionsPerTenant(tenantId).get(existingUserInfo.getRole());
        if(permissions!=null && permissions.contains(PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES.name())) {
            if(!getUserName().equals(newUser.getUsername())) // should be able to update same user
                return "{\"result\":\"Not Authorized to update another SUPERADMIN user.\"}";
        }

        String pwdUpdated = newUser.getUserPassword();
        String existingPwd;

        if(pwdUpdated.equals("*******") && authenticationType.equals("db")) {
            existingPwd = existingUserInfo.getPwd();
            if(!existingPwd.equals(""))
                newUser.setUserPassword(decodePwd(existingPwd));
        }

        try {
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();

            if(authenticationType.equals("db")){
                if(inMemoryUserDetailsManager.userExists(newUser.getUsername())){
                    inMemoryUserDetailsManager.updateUser(User.withUsername(newUser.getUsername())
                            .password(encoder.encode(newUser.getUserPassword()))
                            .roles(newUser.getRole()).build());
                }else {
                    inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername())
                            .password(encoder.encode(newUser.getUserPassword()))
                            .roles(newUser.getRole()).build());
                }
                newUser.setUserPassword(encodePwd(newUser.getUserPassword()));
            }

            HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
            UserInfo userInfo = new UserInfo();

            copyProperties(newUser, userInfo);
            userInfo.setPwd(newUser.getUserPassword());
            userInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, newUser.getTeam()));
            userInfo.setTenantId(tenantId);

            String result = dbHandle.updateUser(userInfo);

            return "{\"result\":\""+result+"\"}";
        }catch(Exception e){
            log.error("Error from updateUser {}", e.toString());
            return "{\"result\":\"Failure "+e.getMessage()+"\"}";
        }
    }

    public TeamModel getTeamDetails(Integer teamId, String tenantName) {
        log.debug("getTeamDetails {} {}", teamId, tenantName);

        int tenantId = commonUtilsService.getTenantId(getUserName());

        Team teamDao = manageDatabase.getHandleDbRequests()
                .selectTeamDetails(teamId, tenantId);
        if(teamDao != null) {
            TeamModel teamModel = new TeamModel();
            copyProperties(teamDao, teamModel);
            if (teamDao.getRequestTopicsEnvs() != null)
                teamModel.setEnvList(Arrays.asList(teamDao.getRequestTopicsEnvs().split("\\s*,\\s*")));
            teamModel.setTenantName(tenantName);
            teamModel.setTenantId(getTenantId(tenantName));
            return teamModel;
        } return null;
    }

    private int getTenantId(String tenantName) {
        return manageDatabase.getTenantMap().entrySet().stream()
                    .filter(obj -> obj.getValue().equals(tenantName))
                    .findFirst().get().getKey();
    }

    String generateRandomWord(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghi"
                +"jklmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    public HashMap<String, String> resetPassword(String username) {
        log.info("resetPassword {}", username);
        HashMap<String, String> userMap = new HashMap<>();
        UserInfoModel userInfoModel = getUserInfoDetails(username);
        userMap.put("passwordSent", "false");
        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

        if(userInfoModel == null)
            userMap.put("userFound","false");
        else {
            userMap.put("userFound", "true");
            String newGeneratedPwd = generateRandomWord(15);
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();

            UserDetails updatePwdUserDetails = inMemoryUserDetailsManager.loadUserByUsername(username);
            inMemoryUserDetailsManager.updatePassword(updatePwdUserDetails, encoder.encode(newGeneratedPwd));
            String pwdUpdated = dbHandle.updatePassword(username, encodePwd(newGeneratedPwd));
            if(pwdUpdated.equals("success")) {
                userMap.put("passwordSent", "true");
                mailService.sendMailResetPwd(username, newGeneratedPwd, dbHandle, userInfoModel.getTenantId(), commonUtilsService.getLoginUrl());
            }
        }
        return userMap;
    }




    private List<TeamModel> getTeamModels(List<Team> teams) {
        List<TeamModel> teamModels = new ArrayList<>();
        TeamModel teamModel;
        List<String> allList = new ArrayList<>();
        allList.add("ALL");
        List<String> tmpConvertedList;

        for (Team team : teams) {
            teamModel = new TeamModel();
            copyProperties(team, teamModel);
            if(team.getRequestTopicsEnvs() == null || team.getRequestTopicsEnvs().length() == 0)
                teamModel.setEnvList(allList);
            else {
                teamModel.setEnvList(Arrays.asList(team.getRequestTopicsEnvs().split("\\s*,\\s*")));
                tmpConvertedList = new ArrayList<>();
                try {
                    for (String s : teamModel.getEnvList()) {
                        tmpConvertedList.add(getEnvDetailsFromId(s).getName());
                    }
                } catch (Exception exception) {
                    log.error("No environments/clusters found.");
                }
                teamModel.setEnvList(tmpConvertedList);
            }

            teamModels.add(teamModel);
        }
        return teamModels;
    }

    public List<TeamModel> getAllTeamsSUFromRegisterUsers() {
        int tenantId = commonUtilsService.getTenantId(getUserName());
        List<TeamModel> teamModels = getTeamModels(manageDatabase.getHandleDbRequests().selectAllTeams(tenantId));

        teamModels.forEach(teamModel -> teamModel
                .setTenantName(manageDatabase
                        .getTenantMap()
                        .get(teamModel.getTenantId())));

        return teamModels;
    }

    public List<TeamModel> getAllTeamsSU() {
        int tenantId = commonUtilsService.getTenantId(getUserName());
        List<TeamModel> teamModels = getTeamModels(manageDatabase.getHandleDbRequests().selectAllTeams(tenantId));

        if(!commonUtilsService.isNotAuthorizedUser(getUserName(), PermissionType.ADD_EDIT_DELETE_TEAMS)) {
            teamModels.forEach(teamModel -> {
                teamModel.setShowDeleteTeam(manageDatabase.getHandleDbRequests()
                        .findAllComponentsCountForTeam(teamModel.getTeamId(), tenantId) <= 0);
            });
        }

        teamModels.forEach(teamModel -> teamModel
                .setTenantName(manageDatabase
                        .getTenantMap()
                        .get(teamModel.getTenantId())));

        return teamModels;
    }

    // tenant filtering
    public List<String> getAllTeamsSUOnly() {
        String userDetails = getUserName();
        int tenantId = commonUtilsService.getTenantId(getUserName());
        String myTeamName = manageDatabase.
                getHandleDbRequests().
                selectAllTeamsOfUsers(userDetails, tenantId).
                get(0).
                getTeamname();

        List<String> teams = new ArrayList<>();

        // tenant filtering
        List<TeamModel> teamsList = getAllTeamsSU();

        teams.add("All teams"); // team id 1
        teams.add(myTeamName);

        for(TeamModel team: teamsList) {
            if(!team.getTeamname().equals(myTeamName))
                teams.add(team.getTeamname());
        }

        return teams;
    }


    public String deleteTeam(Integer teamId){
        log.info("deleteTeam {}", teamId);
        String userDetails = getUserName();

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_TEAMS))
            return "{\"result\":\"Not Authorized\"}";

        String envAddResult = "{\"result\":\"Team cannot be deleted.\"}";

        int tenantId = commonUtilsService.getTenantId(getUserName());

        if(manageDatabase.getHandleDbRequests()
                .findAllComponentsCountForTeam(teamId, tenantId) > 0)
        {
            return "{\"result\":\"Not allowed to delete this team, as there are associated topics/acls/requests..\"}";
        }

        // own team cannot be deleted
        if(getMyTeamId(userDetails).equals(teamId))
            return envAddResult;

        try {
            String result = manageDatabase.getHandleDbRequests()
                    .deleteTeamRequest(teamId, commonUtilsService.getTenantId(getUserName()));

            if(result.equals("success")) {
                commonUtilsService.updateMetadata(tenantId, EntityType.TEAM, MetadataOperationType.DELETE);
            }

            return "{\"result\":\"" + result +"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String deleteUser(String userId, boolean isExternal){
        log.info("deleteUser {}", userId);
        String userDetails = getUserName();

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS))
            return "{\"result\":\"Not Authorized\"}";

        UserInfo existingUserInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
        List<String> permissions = manageDatabase.getRolesPermissionsPerTenant(commonUtilsService
                .getTenantId(getUserName())).get(existingUserInfo.getRole());
        if(permissions != null && permissions.contains(PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES.name()))
            return "{\"result\":\"Not Authorized. Cannot delete a user with SUPERADMIN access.\"}";

        String envAddResult = "{\"result\":\"User cannot be deleted\"}";

        if(userDetails.equals(userId) && isExternal)
            return envAddResult;

        inMemoryUserDetailsManager.deleteUser(userId);

        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteUserRequest(userId)+"\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    private String encodePwd(String pwd){
        return getJasyptEncryptor().encrypt(pwd);
    }
    private String decodePwd(String pwd){
        if(pwd != null)
            return getJasyptEncryptor().decrypt(pwd);
        else
            return "";
    }

    private BasicTextEncryptor getJasyptEncryptor(){
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

        return textEncryptor;
    }

    public HashMap<String, String> addNewUser(UserInfoModel newUser, boolean isExternal) throws KafkawizeException {
        HashMap<String, String> resMap = new HashMap<>();
        log.info("addNewUser {} {} {}", newUser.getUsername(), newUser.getTeam(), newUser.getRole());

        if(kwInstallationType.equals("saas")){
            String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(newUser.getUsername());
            if(!m.matches()){
                resMap.put("result", "Invalid mail id");
                return resMap;
            }
        }else{
            String regex = "^[a-zA-Z0-9]{3,}$";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(newUser.getUsername());
            if(!m.matches()){
                resMap.put("result", "Invalid username");
                return resMap;
            }
        }

        int tenantId;
        if(isExternal){
            if(newUser.getTenantId() == 0)
                tenantId = commonUtilsService.getTenantId(getUserName());
            else
                tenantId = newUser.getTenantId();

            newUser.setTenantId(tenantId);

            if(newUser.getTeamId() == null) {
                newUser.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, newUser.getTeam()));
            }
        }

        if(isExternal && commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS)){
            resMap.put("result", "Not Authorized");
            return resMap;
        }

        if(authenticationType.equals("ad") && adAuthRoleEnabled.equals("true"))
            newUser.setRole("NA");

        try {
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();

            if(authenticationType.equals("db")){
                inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername())
                        .password(encoder.encode(newUser.getUserPassword()))
                        .roles(newUser.getRole()).build());
                newUser.setUserPassword(encodePwd(newUser.getUserPassword()));
            }
            else
                inMemoryUserDetailsManager.createUser(User.withUsername(newUser.getUsername())
                        .password("")
                        .roles(newUser.getRole()).build());

            HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
            UserInfo userInfo = new UserInfo();
            copyProperties(newUser, userInfo);
            userInfo.setPwd(newUser.getUserPassword());
            String result = dbHandle.addNewUser(userInfo);

//            log.info("pwd : "+decodePwd(newUser.getUserPassword()));
            if(isExternal) {
                if(newUser.getUserPassword().equals(""))
                    mailService.sendMail(newUser.getUsername(), newUser.getUserPassword(),
                        dbHandle, commonUtilsService.getLoginUrl());
                else
                    mailService.sendMail(newUser.getUsername(), decodePwd(newUser.getUserPassword()),
                            dbHandle, commonUtilsService.getLoginUrl());
            }
            resMap.put("result", result);
            return resMap;
        }catch(Exception e){
            log.error("Error {}",e.getMessage());
            inMemoryUserDetailsManager.deleteUser(newUser.getUsername());
            if(e.getMessage().contains("should not exist")){
                resMap.put("result", "Failure. User already exists.");
                return resMap;
            }
            else
                throw new KafkawizeException("Unable to create the user.");
        }
    }

    public String addNewTeam(TeamModel newTeam, boolean isExternal){
        log.info("addNewTeam {}", newTeam);

        if(isExternal && commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_TEAMS))
            return "{\"result\":\"Not Authorized\"}";

        int tenantId;
        if(isExternal){
           tenantId = commonUtilsService.getTenantId(getUserName());
           newTeam.setTenantId(tenantId);
        }
        else
            tenantId = newTeam.getTenantId();

        Team team = new Team();
        copyProperties(newTeam, team);

        String envListStrCommaSeperated;
        if(newTeam.getEnvList() != null && newTeam.getEnvList().size() > 0) {
            envListStrCommaSeperated = String.join(",", newTeam.getEnvList().toArray(new String[0]));
            team.setRequestTopicsEnvs(envListStrCommaSeperated);
        }
        try {
            String res = manageDatabase.getHandleDbRequests().addNewTeam(team);
            if(res.equals("success")) {
                commonUtilsService.updateMetadata(tenantId, EntityType.TEAM, MetadataOperationType.CREATE);
            }
            return "{\"result\":\"" + res + "\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String updateTeam(TeamModel updatedTeam) {
        log.info("updateTeam {}", updatedTeam);

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_TEAMS))
            return "{\"result\":\"Not Authorized\"}";

        int tenantId = commonUtilsService.getTenantId(getUserName());
        Team team = new Team();
        copyProperties(updatedTeam, team);
        team.setTenantId(tenantId);

        String envListStrCommaSeperated;
        if(updatedTeam.getEnvList() != null && updatedTeam.getEnvList().size() > 0) {
            envListStrCommaSeperated = String.join(",", updatedTeam.getEnvList().toArray(new String[0]));
            team.setRequestTopicsEnvs(envListStrCommaSeperated);
        }

        try {
            String res = manageDatabase.getHandleDbRequests().updateTeam(team);
            commonUtilsService.updateMetadata(tenantId, EntityType.TEAM, MetadataOperationType.UPDATE);
            return "{\"result\":\"" + res + "\"}";
        }catch (Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public String changePwd(String changePwd){
        if(authenticationType.equals("ldap") || authenticationType.equals("ad"))
        {
            return "{\"result\":\" Password cannot be updated in ldap/ad authentication mode. \"}";
        }
        String userDetails = getUserName();

        GsonJsonParser jsonParser = new GsonJsonParser();
        Map<String, Object> pwdMap  = jsonParser.parseMap(changePwd);

        String pwdChange = (String)pwdMap.get("pwd");

        try {
            PasswordEncoder encoder =
                    PasswordEncoderFactories.createDelegatingPasswordEncoder();

            UserDetails updatePwdUserDetails = inMemoryUserDetailsManager.loadUserByUsername(userDetails);
            inMemoryUserDetailsManager.updatePassword(updatePwdUserDetails, encoder.encode(pwdChange));

            return "{\"result\":\"" + manageDatabase.getHandleDbRequests()
                    .updatePassword(userDetails, encodePwd(pwdChange)) + "\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.getMessage()+"\"}";
        }
    }

    public List<UserInfoModel> showUsers(String teamName, String userSearchStr, String pageNo){

        List<UserInfoModel> userInfoModels = new ArrayList<>();
        int tenantId = commonUtilsService.getTenantId(getUserName());
        List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersInfo(tenantId);

        if(userSearchStr != null && !userSearchStr.equals("")){
            userList = userList.stream().filter(user -> user.getUsername().contains(userSearchStr)).collect(Collectors.toList());
        }

        userList.forEach(userListItem -> {
            UserInfoModel userInfoModel = new UserInfoModel();
            copyProperties(userListItem, userInfoModel);
            if(teamName != null && !teamName.equals("")) {
                if (manageDatabase.getTeamNameFromTeamId(tenantId, userInfoModel.getTeamId()).equals(teamName))
                    userInfoModels.add(userInfoModel);
            }
            else
                userInfoModels.add(userInfoModel);
        });
        userInfoModels.forEach(userInfoModel -> {
            userInfoModel.setTeam(manageDatabase.getTeamNameFromTeamId(tenantId, userInfoModel.getTeamId()));
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


    public UserInfoModel getMyProfileInfo(){
        String userDetails = getUserName();
        UserInfoModel userInfoModel = new UserInfoModel();
        UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userDetails);
        copyProperties(userInfo, userInfoModel);
        return userInfoModel;
    }

    private String getUserName(){
        return mailService.getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }



    HashMap<String, String> addTwoDefaultTeams(String teamContactPerson, String newTenantName, Integer tenantId) {
        HashMap<String, String> teamAddMap = new HashMap<>();

        TeamModel teamModel = new TeamModel();
        teamModel.setTenantId(tenantId);
        teamModel.setTenantName(newTenantName);
        teamModel.setTeamname(INFRATEAM);
        teamModel.setContactperson(teamContactPerson);

        String addTeamRes = addNewTeam(teamModel, false);
        teamAddMap.put("team1result", addTeamRes);

        TeamModel teamModel1 = new TeamModel();
        teamModel1.setTenantId(tenantId);
        teamModel1.setTenantName(newTenantName);
        teamModel1.setTeamname(STAGINGTEAM);
        teamModel1.setContactperson(teamContactPerson);

        String addTeamRes2 = addNewTeam(teamModel1, false);
        teamAddMap.put("team2result", addTeamRes2);
        return teamAddMap;
    }

    public HashMap<String, String> registerUser(RegisterUserInfoModel newUser, boolean isExternal) throws Exception{
        log.info("registerUser {}", newUser.getUsername());
        HashMap<String, String> result = new HashMap<>();
        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

        // check if user exists
        List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersAllTenants();
        if (userList.stream().anyMatch(user -> user.getUsername().equals(newUser.getMailid()))) {
            throw new KafkawizeException("User already exists.");
        }
        else if (userList.stream().anyMatch(user -> user.getUsername().equals(newUser.getUsername()))) {
            throw new KafkawizeException("User already exists.");
        }

        // check if registration exists
        List<RegisterUserInfo> registerUserInfoList = manageDatabase.getHandleDbRequests().selectAllRegisterUsersInfo();
        if (registerUserInfoList.stream().anyMatch(user -> user.getUsername().equals(newUser.getMailid()))) {
            throw new KafkawizeException("User already exists.");
        }else if (registerUserInfoList.stream().anyMatch(user -> user.getUsername().equals(newUser.getUsername()))) {
            throw new KafkawizeException("User already exists.");
        }

        try {
            newUser.setStatus("PENDING");
            newUser.setRegisteredTime(new Timestamp(System.currentTimeMillis()));

            if(isExternal){ // not saas
                newUser.setTeam(STAGINGTEAM);
                newUser.setRole(USER_ROLE);
            }

            RegisterUserInfo registerUserInfo = new RegisterUserInfo();
            copyProperties(newUser, registerUserInfo);
            registerUserInfo.setPwd(encodePwd(registerUserInfo.getPwd()));

            if(!isExternal) { // from saas
                if(registerUserInfo.getTenantId() != 0){
                    registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(registerUserInfo.getTenantId(), registerUserInfo.getTeam()));
                }else
                    registerUserInfo.setTeamId(0);
            }
            else {
                if(newUser.getTenantName() == null || newUser.getTenantName().equals("")){ // join default tenant
                    registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(DEFAULT_TENANT_ID, newUser.getTeam()));
                    registerUserInfo.setTenantId(DEFAULT_TENANT_ID);
                }else{ // join existing tenant
                    try {
                        int tenantId = getTenantId(newUser.getTenantName());
                        registerUserInfo.setTenantId(tenantId);

                        // new tenant, no users exist
                        if(userList.stream().noneMatch(userInfo -> userInfo.getTenantId() == tenantId))
                        {
                            addTwoDefaultTeams(newUser.getFullname(), newUser.getTenantName(), tenantId);
                            registerUserInfo.setTeam(INFRATEAM);
                            registerUserInfo.setRole(SUPERADMIN_ROLE);
                            registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, registerUserInfo.getTeam()));
                        }
                        else{
                            registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, newUser.getTeam()));
                        }
                    }catch (Exception e){
                        throw new KafkawizeException("Invalid tenant provided.");
                    }
                }
            }

            String resultRegister = dbHandle.registerUser(registerUserInfo);

            if(resultRegister.contains("Failure"))
                throw new KafkawizeException("Registration already exists.");

            result.put("result", resultRegister);

            if(isExternal) {
                mailService.sendMailRegisteredUser(registerUserInfo, dbHandle, commonUtilsService.getLoginUrl());
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new KafkawizeException("Failure. Something went wrong. Please try later.");
        }
        return result;
    }

    public List<RegisterUserInfoModel> getNewUserRequests() {
        int tenantId = commonUtilsService.getTenantId(getUserName());
        List<RegisterUserInfo> registerUserInfoList;

        if(kwInstallationType.equals("saas"))
            registerUserInfoList = manageDatabase.getHandleDbRequests().selectAllRegisterUsersInfoForTenant(tenantId);
        else
            registerUserInfoList = manageDatabase.getHandleDbRequests().selectAllRegisterUsersInfo();

        List<RegisterUserInfoModel> registerUserInfoModels = new ArrayList<>();
        RegisterUserInfoModel registerUserInfoModel;
        for (RegisterUserInfo registerUserInfo : registerUserInfoList) {
            registerUserInfoModel = new RegisterUserInfoModel();
            copyProperties(registerUserInfo, registerUserInfoModel);
            registerUserInfoModel.setTeam(manageDatabase.getTeamNameFromTeamId(tenantId, registerUserInfo.getTeamId()));
            registerUserInfoModel.setPwd("");
            registerUserInfoModel.setTenantName(manageDatabase.getTenantMap().get(registerUserInfoModel.getTenantId()));
            registerUserInfoModels.add(registerUserInfoModel);
        }

        return registerUserInfoModels;
    }

    public String approveNewUserRequests(String username, boolean isExternal, int tenantId, String teamName) {
        log.info("approveNewUserRequests {}", username);
        String userDetails = getUserName();

        if(isExternal && commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS))
            return "{\"result\":\"Not Authorized\"}";

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        try {
            RegisterUserInfo registerUserInfo = dbHandle.getRegisterUsersInfo(username);
            if(!isExternal) //from saas new user requests for tenant owners
            {
                registerUserInfo.setTenantId(tenantId);
                registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, teamName));
            }
//            if(kwInstallationType.equals("saas")){ //from saas new user requests for not tenant owners - join thru tenant id
//                tenantId = registerUserInfo.getTenantId();
//                registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, STAGINGTEAM));
//            }

            tenantId = registerUserInfo.getTenantId();

            UserInfoModel userInfo = new UserInfoModel();
            userInfo.setUsername(username);
            userInfo.setFullname(registerUserInfo.getFullname());
            userInfo.setTeam(manageDatabase.getTeamNameFromTeamId(tenantId, registerUserInfo.getTeamId()));
            userInfo.setTeamId(registerUserInfo.getTeamId());
            userInfo.setRole(registerUserInfo.getRole());
            userInfo.setTenantId(tenantId);

            if(authenticationType.equals("db"))
                userInfo.setUserPassword(decodePwd(registerUserInfo.getPwd()));
            else
                userInfo.setUserPassword("");
            userInfo.setMailid(registerUserInfo.getMailid());

            HashMap<String, String> result = addNewUser(userInfo, isExternal);
            if(result.get("result").contains("success"))
                dbHandle.updateNewUserRequest(username, userDetails, true);
            else
                return "{\"result\":\"failure\"}";
            return "{\"result\":\"success\"}";
        }catch(Exception e){
            return "{\"result\":\"failure\"}";
        }
    }

    public String declineNewUserRequests(String username) {
        log.info("declineNewUserRequests {}", username);
        String userDetails = getUserName();

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_USERS))
            return "{\"result\":\"Not Authorized\"}";

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        try {
            dbHandle.updateNewUserRequest(username, userDetails, false);
            return "{\"result\":\"success\"}";
        }catch(Exception e){
            return "{\"result\":\"failure\"}";
        }
    }

    public RegisterUserInfoModel getRegistrationInfoFromId(String registrationId, String status) {
        RegisterUserInfo registerUserInfo = manageDatabase.getHandleDbRequests().getRegistrationDetails(registrationId, status);

        if(registerUserInfo != null){
            RegisterUserInfoModel registerUserInfoModel = new RegisterUserInfoModel();
            copyProperties(registerUserInfo, registerUserInfoModel);
            return registerUserInfoModel;
        }
        else return null;
    }

    public Env getEnvDetailsFromId(String envId){
        Optional<Env> envFound = manageDatabase
                .getKafkaEnvList(commonUtilsService.getTenantId(getUserName()))
                .stream().filter(env->env.getId().equals(envId)).findFirst();
        return envFound.orElse(null);
    }

    private Object getPrincipal(){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Integer getMyTeamId(String userName){
        return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
    }
}
