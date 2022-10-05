package io.aiven.klaw.service;

import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.KwTenantModel;
import io.aiven.klaw.model.RegisterSaasUserInfoModel;
import io.aiven.klaw.model.RegisterUserInfoModel;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SaasService {

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Autowired private ValidateCaptchaService validateCaptchaService;

  @Autowired private DefaultDataService defaultDataService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired private MailUtils mailService;

  @Autowired private UsersTeamsControllerService usersTeamsControllerService;

  @Autowired private EnvsClustersTenantsControllerService envsClustersTenantsControllerService;

  @Autowired ManageDatabase manageDatabase;

  public Map<String, String> approveUserSaas(RegisterUserInfoModel newUser) throws Exception {
    log.info("approveUserSaas {} / {}", newUser.getFullname(), newUser.getMailid());
    Map<Integer, String> tenantMap = manageDatabase.getTenantMap();

    Map<String, String> resultMap = new HashMap<>();
    resultMap.put("result", ApiResultStatus.FAILURE.value);

    // check if user exists
    List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersAllTenants();
    if (userList.stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), newUser.getMailid()))) {
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
        ApiResponse addTenantResult =
            envsClustersTenantsControllerService.addTenantId(kwTenantModel, false);

        // create INFRATEAM and STAGINGTEAM
        if (ApiResultStatus.SUCCESS.value.equals(addTenantResult.getResult())) {
          tenantId = Integer.parseInt((String) addTenantResult.getData());

          Map<String, String> teamAddMap =
              usersTeamsControllerService.addTwoDefaultTeams(
                  newUser.getFullname(), newTenantName, tenantId);

          if (teamAddMap.get("team1result").contains(ApiResultStatus.SUCCESS.value)
              && teamAddMap.get("team2result").contains(ApiResultStatus.SUCCESS.value)) {
            // approve user

            ApiResponse resultApproveUser =
                usersTeamsControllerService.approveNewUserRequests(
                    newUser.getUsername(), false, tenantId, KwConstants.INFRATEAM);
            if (resultApproveUser.getResult().contains(ApiResultStatus.SUCCESS.value))
              updateStaticData(newUser, tenantId);
            else {
              resultMap.put("error", "Something went wrong. Please try again.");
              return resultMap;
            }

          } else {
            resultMap.put("error", "Something went wrong. Please try again.");
            return resultMap;
          }
        } else {
          resultMap.put("error", "Failure :" + addTenantResult.getResult());
          return resultMap;
        }
      }

      resultMap.put("result", ApiResultStatus.SUCCESS.value);
      return resultMap;
    } catch (Exception e) {
      log.error("Exception:", e);
      resultMap.put("error", "Something went wrong. Please try again.");
      return resultMap;
    }
  }

  // TO DO transactions
  public ApiResponse registerUserSaas(RegisterSaasUserInfoModel newUser) throws Exception {
    log.info("registerUserSaas {} / {}", newUser.getFullname(), newUser.getMailid());
    Map<Integer, String> tenantMap = manageDatabase.getTenantMap();
    Map<String, String> resultMap = new HashMap<>();

    try {
      if (handleValidations(newUser, tenantMap, resultMap))
        return ApiResponse.builder().result(resultMap.get("result")).build();

      RegisterUserInfoModel newUserTarget = new RegisterUserInfoModel();
      copyProperties(newUser, newUserTarget);
      String userName = newUser.getMailid();
      newUserTarget.setUsername(userName);
      String pwd = usersTeamsControllerService.generateRandomWord(10);
      newUserTarget.setPwd(pwd);

      if (newUser.getTenantName() == null || newUser.getTenantName().equals("")) {
        // new user
        if (createNewUserForActivation(resultMap, newUserTarget))
          return ApiResponse.builder().result(resultMap.get("error")).build();
      } else if (!tenantMap.containsValue(newUser.getTenantName())) {
        resultMap.put("error", "Tenant does not exist.");
        return ApiResponse.builder()
            .result("Tenant does not exist.")
            .message("Tenant does not exist.")
            .build();
      } else {
        // create user for existing tenant
        if (createUserForExistingTenant(newUser, tenantMap, resultMap, newUserTarget))
          return ApiResponse.builder().result(resultMap.get("error")).build();
      }

      return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private boolean createNewUserForActivation(
      Map<String, String> resultMap, RegisterUserInfoModel newUserTarget) throws Exception {
    newUserTarget.setTenantId(0);
    String randomId = UUID.randomUUID().toString();
    newUserTarget.setRole(KwConstants.SUPERADMIN_ROLE);
    newUserTarget.setTeam(KwConstants.INFRATEAM);
    newUserTarget.setRegistrationId(randomId);
    newUserTarget.setRegisteredTime(new Timestamp(System.currentTimeMillis()));
    ApiResponse userRegMap = usersTeamsControllerService.registerUser(newUserTarget, false);

    if (!ApiResultStatus.SUCCESS.value.equals(userRegMap.getResult())) {
      resultMap.put("error", "Something went wrong. Please try again.");
      return true;
    }
    String activationUrl =
        commonUtilsService.getBaseUrl()
            + "/userActivation?activationId="
            + newUserTarget.getRegistrationId();

    //        log.info(activationUrl);
    RegisterUserInfo registerUserInfo = new RegisterUserInfo();
    copyProperties(newUserTarget, registerUserInfo);

    mailService.sendMailRegisteredUserSaas(
        registerUserInfo,
        manageDatabase.getHandleDbRequests(),
        "",
        KwConstants.DEFAULT_TENANT_ID,
        newUserTarget.getTeam(),
        activationUrl,
        commonUtilsService.getLoginUrl());
    return false;
  }

  private boolean createUserForExistingTenant(
      RegisterSaasUserInfoModel newUser,
      Map<Integer, String> tenantMap,
      Map<String, String> resultMap,
      RegisterUserInfoModel newUserTarget)
      throws Exception {
    String newTenantName = newUser.getTenantName();
    Integer tenantId;
    // register user
    tenantId =
        tenantMap.entrySet().stream()
            .filter(obj -> Objects.equals(obj.getValue(), newTenantName))
            .findFirst()
            .get()
            .getKey();

    newUserTarget.setTenantId(tenantId);
    newUserTarget.setTeam(KwConstants.STAGINGTEAM);
    newUserTarget.setRole(KwConstants.USER_ROLE);
    ApiResponse userRegMap = usersTeamsControllerService.registerUser(newUserTarget, false);

    if (!ApiResultStatus.SUCCESS.value.equals(userRegMap.getResult())) {
      resultMap.put("error", "Something went wrong. Please try again.");
      return true;
    } else {
      RegisterUserInfo registerUserInfo = new RegisterUserInfo();
      copyProperties(newUserTarget, registerUserInfo);
      mailService.sendMailRegisteredUserSaas(
          registerUserInfo,
          manageDatabase.getHandleDbRequests(),
          newTenantName,
          tenantId,
          newUserTarget.getTeam(),
          "activationUrl",
          commonUtilsService.getLoginUrl());
    }
    return false;
  }

  private boolean handleValidations(
      RegisterSaasUserInfoModel newUser,
      Map<Integer, String> tenantMap,
      Map<String, String> resultMap) {
    if (!validateCaptchaService.validateCaptcha(newUser.getRecaptchaStr())) {
      resultMap.put("error", " Verify Captcha.");
      return true;
    }

    // check if user exists
    List<UserInfo> userList = manageDatabase.getHandleDbRequests().selectAllUsersAllTenants();
    if (userList.stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), newUser.getMailid()))) {
      resultMap.put("error", "User already exists. You may login.");
      return true;
    }

    List<RegisterUserInfo> registerUserInfoList =
        manageDatabase.getHandleDbRequests().selectAllRegisterUsersInfo();
    if (registerUserInfoList.stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), newUser.getMailid()))) {
      resultMap.put("error", "Registration already exists. You may login.");
      return true;
    }

    if ("default"
        .equals(newUser.getTenantName())) { // don't allow users to be created on default tenant
      resultMap.put("error", "You cannot request users for default tenant.");
      return true;
    }

    return false;
  }

  private void updateStaticData(RegisterUserInfoModel newUserTarget, Integer tenantId) {
    manageDatabase
        .getHandleDbRequests()
        .insertDefaultKwProperties(
            defaultDataService.createDefaultProperties(tenantId, newUserTarget.getUsername()));
    manageDatabase
        .getHandleDbRequests()
        .insertDefaultRolesPermissions(
            defaultDataService.createDefaultRolesPermissions(tenantId, false, kwInstallationType));

    manageDatabase.loadRolesPermissionsOneTenant(null, tenantId);
    manageDatabase.loadKwPropsPerOneTenant(null, tenantId);
  }

  // approve users
  public Map<String, String> getActivationInfo(String activationId) {
    Map<String, String> resultMap = new HashMap<>();
    RegisterUserInfoModel registerUserInfoModel =
        usersTeamsControllerService.getRegistrationInfoFromId(activationId, "");

    if (registerUserInfoModel == null) {
      resultMap.put("result", ApiResultStatus.FAILURE.value);
      return resultMap;
    } else if ("APPROVED".equals(registerUserInfoModel.getStatus())) {
      resultMap.put("result", "already_activated");
      return resultMap;
    } else if ("PENDING".equals(registerUserInfoModel.getStatus())) {
      Map<String, String> result;
      try {
        result = approveUserSaas(registerUserInfoModel);
        if (ApiResultStatus.SUCCESS.value.equals(result.get("result"))) {
          resultMap.put("result", ApiResultStatus.SUCCESS.value);
        } else resultMap.put("result", "othererror");
      } catch (Exception e) {
        log.error("Exception:", e);
      }
    }
    return resultMap;
  }
}
