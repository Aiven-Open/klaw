package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.RegisterUserInfo;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.KwTenantModel;
import com.kafkamgt.uiapi.model.RegisterSaasUserInfoModel;
import com.kafkamgt.uiapi.model.RegisterUserInfoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.kafkamgt.uiapi.service.KwConstants.*;
import static com.kafkamgt.uiapi.service.KwConstants.USER_ROLE;
import static org.springframework.beans.BeanUtils.copyProperties;

@Service
@Slf4j
public class SaasService {

    @Value("${kafkawize.installation.type:onpremise}")
    private String kwInstallationType;

    @Autowired
    private ValidateCaptchaService validateCaptchaService;

    @Autowired
    private DefaultDataService defaultDataService;

    @Autowired
    private CommonUtilsService commonUtilsService;

    @Autowired
    private MailUtils mailService;

    @Autowired
    private UsersTeamsControllerService usersTeamsControllerService;

    @Autowired
    private EnvsClustersTenantsControllerService envsClustersTenantsControllerService;

    @Autowired
    ManageDatabase manageDatabase;

    public HashMap<String, String> approveUserSaas(RegisterUserInfoModel newUser) throws Exception{
        log.info("approveUserSaas {} / {}", newUser.getFullname(), newUser.getMailid());
        HashMap<Integer, String> tenantMap = manageDatabase.getTenantMap();

        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("result","failure");

        // check if user exists
        List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersAllTenants();
        if (userList.stream().anyMatch(user -> user.getUsername().equals(newUser.getMailid()))) {
            resultMap.put("error", "User already exists. You may login.");
            return resultMap;
        }

        try {
            String newTenantName;
            Integer tenantId = 0;

            // create tenant, team
            if (!tenantMap.containsValue(newUser.getTenantName())) {
                KwTenantModel kwTenantModel = new KwTenantModel();
                newTenantName = usersTeamsControllerService.generateRandomWord(20);
                kwTenantModel.setTenantName(newTenantName);
                kwTenantModel.setTenantDesc("");
                kwTenantModel.setContactPerson(newUser.getFullname());
                kwTenantModel.setInTrialPhase(true);
                kwTenantModel.setActiveTenant(true);
                HashMap<String, String> addTenantResult = envsClustersTenantsControllerService.addTenantId(kwTenantModel, false);

                // create INFRATEAM and STAGINGTEAM
                if (addTenantResult.get("result").equals("success")) {
                    tenantId = Integer.parseInt(addTenantResult.get("tenantId"));

                    HashMap<String, String> teamAddMap = usersTeamsControllerService
                            .addTwoDefaultTeams(newUser.getFullname(), newTenantName, tenantId);

                    if (teamAddMap.get("team1result").contains("success") && teamAddMap.get("team2result").contains("success")) {
                        // approve user

                        String resultApproveUser = usersTeamsControllerService
                                .approveNewUserRequests(newUser.getUsername(), false, tenantId, INFRATEAM);
                        if(resultApproveUser.contains("success"))
                            updateStaticData(newUser, tenantId);
                        else{
                            resultMap.put("error","Something went wrong. Please try again.");
                            return resultMap;
                        }

                    }else{
                        resultMap.put("error","Something went wrong. Please try again.");
                        return resultMap;
                    }
                }else{
                    resultMap.put("error", "Failure :" + addTenantResult.get("result"));
                    return resultMap;
                }
            }

            resultMap.put("result", "success");
            return resultMap;
        }catch(Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            resultMap.put("error","Something went wrong. Please try again.");
            return resultMap;
        }
    }

    // TO DO transactions
    public HashMap<String, String> registerUserSaas(RegisterSaasUserInfoModel newUser) throws Exception{
        log.info("registerUserSaas {} / {}", newUser.getFullname(), newUser.getMailid());
        HashMap<Integer, String> tenantMap = manageDatabase.getTenantMap();

        HashMap<String, String> resultMap = new HashMap<>();
        resultMap.put("result","failure");

        try {
            if (handleValidations(newUser, tenantMap, resultMap)) return resultMap;

            RegisterUserInfoModel newUserTarget = new RegisterUserInfoModel();
            copyProperties(newUser, newUserTarget);

            String userName = newUser.getMailid();

            newUserTarget.setUsername(userName);
            String pwd = usersTeamsControllerService.generateRandomWord(10);
            newUserTarget.setPwd(pwd);

            if (newUser.getTenantName() == null || newUser.getTenantName().equals("")) {
                // new user
                if (createNewUserForActivation(resultMap, newUserTarget)) return resultMap;
            } else if (!tenantMap.containsValue(newUser.getTenantName())) {
                resultMap.put("error", "Tenant does not exist.");
                return resultMap;
            } else {
                // create user for existing tenant
                if (createUserForExistingTenant(newUser, tenantMap, resultMap, newUserTarget)) return resultMap;
            }

            resultMap.put("result", "success");
            return resultMap;
        }catch(Exception e){
            e.printStackTrace();
            log.error(e.getMessage());
            resultMap.put("error","Something went wrong. Please try again.");
            return resultMap;
        }
    }

    private boolean createNewUserForActivation(HashMap<String, String> resultMap, RegisterUserInfoModel newUserTarget) throws Exception {
        newUserTarget.setTenantId(0);
        String randomId = UUID.randomUUID().toString();
        newUserTarget.setRole(SUPERADMIN_ROLE);
        newUserTarget.setTeam(INFRATEAM);
        newUserTarget.setRegistrationId(randomId);
        newUserTarget.setRegisteredTime(new Timestamp(System.currentTimeMillis()));
        HashMap<String, String> userRegMap = usersTeamsControllerService.registerUser(newUserTarget, false);

        if(!userRegMap.get("result").equals("success")){
            resultMap.put("error","Something went wrong. Please try again.");
            return true;
        }
        String activationUrl = commonUtilsService.getBaseUrl() + "/userActivation?activationId=" + newUserTarget.getRegistrationId();

        RegisterUserInfo registerUserInfo = new RegisterUserInfo();
        copyProperties(newUserTarget, registerUserInfo);

        mailService.sendMailRegisteredUserSaas(registerUserInfo, manageDatabase.getHandleDbRequests(), "",
                DEFAULT_TENANT_ID, newUserTarget.getTeam(), activationUrl, commonUtilsService.getLoginUrl());
        return false;
    }

    private boolean createUserForExistingTenant(RegisterSaasUserInfoModel newUser, HashMap<Integer, String> tenantMap, HashMap<String, String> resultMap, RegisterUserInfoModel newUserTarget) throws Exception {
        String newTenantName =  newUser.getTenantName();
        Integer tenantId;
        // register user
        String finalNewTenantName = newTenantName;
        tenantId = tenantMap.entrySet().stream()
                .filter(obj -> obj.getValue().equals(finalNewTenantName))
                .findFirst().get().getKey();

        newUserTarget.setTenantId(tenantId);
        newUserTarget.setTeam(STAGINGTEAM);
        newUserTarget.setRole(USER_ROLE);
        HashMap<String, String> userRegMap = usersTeamsControllerService.registerUser(newUserTarget, false);

        if(!userRegMap.get("result").equals("success")){
            resultMap.put("error","Something went wrong. Please try again.");
            return true;
        }else{
            RegisterUserInfo registerUserInfo = new RegisterUserInfo();

            copyProperties(newUserTarget, registerUserInfo);

            mailService.sendMailRegisteredUserSaas(registerUserInfo, manageDatabase.getHandleDbRequests(), newTenantName,
                    tenantId, newUserTarget.getTeam(), "activationUrl", commonUtilsService.getLoginUrl());

//            String resultApproveUser = usersTeamsControllerService
//                    .approveNewUserRequests(newUserTarget.getUsername(), false, tenantId, STAGINGTEAM);
//            if(resultApproveUser.contains("success")){
//                RegisterUserInfo registerUserInfo = new RegisterUserInfo();
//                copyProperties(newUserTarget, registerUserInfo);
//
//                mailService.sendMailRegisteredUserSaas(registerUserInfo, manageDatabase.getHandleDbRequests(), newTenantName,
//                        tenantId, newUserTarget.getTeam(), "activationUrl", commonUtilsService.getLoginUrl());
//            }
        }

        return false;
    }

    private boolean handleValidations(RegisterSaasUserInfoModel newUser, HashMap<Integer, String> tenantMap, HashMap<String, String> resultMap) {
        if(!validateCaptchaService.validateCaptcha(newUser.getRecaptchaStr())){
            resultMap.put("error", " Verify Captcha.");
            return true;
        }

        // check if user exists
        List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersAllTenants();
        if (userList.stream().anyMatch(user -> user.getUsername().equals(newUser.getMailid()))) {
            resultMap.put("error", "User already exists. You may login.");
            return true;
        }

        List<RegisterUserInfo> registerUserInfoList = manageDatabase.getHandleDbRequests().selectAllRegisterUsersInfo();
        if (registerUserInfoList.stream().anyMatch(user -> user.getUsername().equals(newUser.getMailid()))) {
            resultMap.put("error", "Registration already exists. You may login.");
            return true;
        }

        if(newUser.getTenantName().equals("default")){ // don't allow users to be created on default tenant
            resultMap.put("error", "You cannot request users for default tenant.");
            return true;
        }


        return false;
    }

    private void updateStaticData(RegisterUserInfoModel newUserTarget, Integer tenantId) {
        manageDatabase.getHandleDbRequests()
                .insertDefaultKwProperties(defaultDataService
                        .createDefaultProperties(tenantId, newUserTarget.getUsername()));
        manageDatabase.getHandleDbRequests()
                .insertDefaultRolesPermissions(defaultDataService
                        .createDefaultRolesPermissions(tenantId, false, kwInstallationType));

        manageDatabase.loadRolesPermissionsOneTenant(null, tenantId);
        manageDatabase.loadKwPropsPerOneTenant(null, tenantId);
    }

    // approve users
    public HashMap<String, String> getActivationInfo(String activationId) {
        HashMap<String, String> resultMap = new HashMap<>();
        RegisterUserInfoModel registerUserInfoModel = usersTeamsControllerService.getRegistrationInfoFromId(activationId, "");

        if(registerUserInfoModel == null){
            resultMap.put("result", "failure");
            return resultMap;
        }
        else if(registerUserInfoModel.getStatus().equals("APPROVED")){
            resultMap.put("result","already_activated");
            return resultMap;
        }else if(registerUserInfoModel.getStatus().equals("PENDING")){
            HashMap<String, String> result ;
            try {
                result = approveUserSaas(registerUserInfoModel);
                if(result.get("result").equals("success")){
                    resultMap.put("result","success");
                }else
                    resultMap.put("result","othererror");
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
            return resultMap;
    }
}
