package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.RegisterUserInfo;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.RegisterUserInfoModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.kafkamgt.uiapi.model.RolesType.SUPERADMIN;
import static com.kafkamgt.uiapi.service.KwConstants.DEFAULT_TENANT_ID;
import static org.springframework.beans.BeanUtils.copyProperties;

@Service
@Slf4j
public class UiControllerLoginService {

    @Value("${kafkawize.login.authentication.type}")
    private String authenticationType;

    @Value("${kafkawize.enable.sso:false}")
    private String ssoEnabled;

    @Value("${kafkawize.installation.type:onpremise}")
    private String kwInstallationType;

    @Value("${kafkawize.sso.client.registration.id:kwregid}")
    private String ssoClientRegistrationId;

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    CommonUtilsService commonUtilsService;

    @Autowired(required = false)
    private OAuth2AuthorizedClientService authorizedClientService;

    private static final String defaultPage = "login.html";
    private static final String defaultPageSaas = "loginSaas.html";
    private static final String indexPage = "index.html";
    private static final String oauthLoginPage = "oauthLogin";

    public String getReturningPage(String uri){
        try {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails != null) {

                HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
                UserInfo userInfo = reqsHandle.getUsersInfo(userDetails.getUsername());

                if(userInfo == null) {
                    SecurityContextHolder.getContext().setAuthentication(null);
                    if(kwInstallationType.equals("saas")) {
                        return "registerSaas.html";
                    }
                    if(authenticationType.equals("ad"))
                        return "registerLdap.html";
                    return "register.html";
                }

                if(kwInstallationType.equals("saas")) {
                    int tenantId = commonUtilsService.getTenantId(userDetails.getUsername());
                    if(!manageDatabase.getTenantFullConfig(tenantId).getIsActive().equals("true")){
                        return "tenantInfo.html";
                    }
                }

                log.debug("Authenticated user : {}", userDetails.getUsername());
                if(uri.equals("login.html") || uri.equals("loginSaas.html") || uri.equals("home.html")
                        || uri.equals("register.html") || uri.contains("registrationReview") || uri.contains("userActivation")
                        || uri.equals("forgotPassword.html") || uri.equals("newADUser.html"))
                    return indexPage;
                return uri;
            }
            if(authenticationType.equals("db") && kwInstallationType.equals("saas"))
                return defaultPageSaas;
            else
                return defaultPage;
        }catch (Exception e){
            log.error("Exception:", e);
            if(uri.equals("login.html") || uri.equals("register.html") || uri.equals("registerSaas.html") ||
                    uri.equals("registerLdap.html") || uri.contains("registrationReview") || uri.contains("userActivation")
                    || uri.equals("forgotPassword.html") || uri.equals("newADUser.html") || uri.equals("terms.html")
            || uri.equals("feedback.html"))
                return uri;

            if(authenticationType.equals("db") && kwInstallationType.equals("saas"))
                return defaultPageSaas;
            else
                return defaultPage;
        }
    }

    public String checkAnonymousLogin(String uri, HttpServletRequest request, HttpServletResponse response){
        try {
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();

            // if user does not exist in db
            if(manageDatabase.getHandleDbRequests()
                    .getUsersInfo((String)defaultOAuth2User.getAttributes().get("preferred_username")) == null) {

                return registerStagingUser((String)defaultOAuth2User.getAttributes().get("preferred_username"),
                        defaultOAuth2User.getAttributes().get("name"));
            }

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(ssoClientRegistrationId,
                    (String) defaultOAuth2User.getAttributes().get("preferred_username"));
            if(client == null)
                return oauthLoginPage;
            else
                return uri;
        }catch (Exception e)
        {
            log.error("Exception:", e);
            return oauthLoginPage;
        }
    }

    public String checkAuth(String uri, HttpServletRequest request, HttpServletResponse response){
        if(uri.equals("tenants.html"))
        {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if(manageDatabase.getHandleDbRequests().getUsersInfo(userDetails.getUsername()).getRole().equals(SUPERADMIN.name())
                    && commonUtilsService.getTenantId(userDetails.getUsername()) == DEFAULT_TENANT_ID){
                return getReturningPage(uri);
            }
            else uri = "index";
        }

        if(ssoEnabled.equals("true")) {
            if(uri.contains("register") || uri.equals("registrationReview.html"))
                return uri;
            else
                return checkAnonymousLogin(uri, request, response);
        }
        else{
            return getReturningPage(uri);
        }
    }

    public String registerStagingUser(String userName, Object fullName){
        try{
            log.info("User found in SSO and not in Kafkawize db :{}", userName);
            String existingRegistrationId = manageDatabase.getHandleDbRequests()
                    .getRegistrationId(userName);

            if(existingRegistrationId != null){
                if(existingRegistrationId.equals("PENDING_ACTIVATION"))
                    return  "redirect:" + "registrationReview";
                else
                    return  "redirect:" + "register?userRegistrationId=" + existingRegistrationId;
            }
            else{
                String randomId = UUID.randomUUID().toString();

                RegisterUserInfoModel registerUserInfoModel = new RegisterUserInfoModel();
                registerUserInfoModel.setRegistrationId(randomId);
                registerUserInfoModel.setStatus("STAGING");
                registerUserInfoModel.setTeam("STAGINGTEAM");
                registerUserInfoModel.setRegisteredTime(new Timestamp(System.currentTimeMillis()));
                registerUserInfoModel.setUsername(userName);
                registerUserInfoModel.setPwd("");
                if(fullName != null)
                    registerUserInfoModel.setFullname((String)fullName);

                RegisterUserInfo registerUserInfo = new RegisterUserInfo();
                copyProperties(registerUserInfoModel, registerUserInfo);
                registerUserInfo.setTeamId(manageDatabase.getTeamIdFromTeamName(DEFAULT_TENANT_ID, registerUserInfo.getTeam()));

                manageDatabase.getHandleDbRequests().registerUserForAD(registerUserInfo);

                return  "redirect:" + "register?userRegistrationId=" + randomId;
            }
        }catch(Exception e){
            log.error("Unable to find mail/name fields.", e);
            return "registerLdap.html";
        }
    }

}
