package io.aiven.klaw.service;

import static io.aiven.klaw.model.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.RolesType.SUPERADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.RegisterUserInfoModel;
import java.sql.Timestamp;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UiControllerLoginService {

  private static final String SAAS = "saas";

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${klaw.enable.sso:false}")
  private String ssoEnabled;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${klaw.sso.client.registration.id:kwregid}")
  private String ssoClientRegistrationId;

  @Autowired ManageDatabase manageDatabase;

  @Autowired CommonUtilsService commonUtilsService;

  @Autowired(required = false)
  private OAuth2AuthorizedClientService authorizedClientService;

  private static final String defaultPage = "login.html";
  private static final String defaultPageSaas = "loginSaas.html";
  private static final String indexPage = "index.html";
  private static final String oauthLoginPage = "oauthLogin";

  public String getReturningPage(String uri) {
    try {
      UserDetails userDetails =
          (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String userName = userDetails.getUsername();

      if (userName != null) {
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
        UserInfo userInfo = reqsHandle.getUsersInfo(userName);

        if (userInfo == null) {
          SecurityContextHolder.getContext().setAuthentication(null);
          if (SAAS.equals(kwInstallationType)) {
            return "registerSaas.html";
          }

          if (ACTIVE_DIRECTORY.value.equals(authenticationType)) {
            return "registerLdap.html";
          }
          return "register.html";
        }

        if (SAAS.equals(kwInstallationType)) {
          int tenantId = commonUtilsService.getTenantId(userName);
          if (!"true".equals(manageDatabase.getTenantFullConfig(tenantId).getIsActive())) {
            return "tenantInfo.html";
          }
        }

        log.debug("Authenticated user : {}", userName);
        if ("login.html".equals(uri)
            || "loginSaas.html".equals(uri)
            || "home.html".equals(uri)
            || "register.html".equals(uri)
            || uri.contains("registrationReview")
            || uri.contains("userActivation")
            || "forgotPassword.html".equals(uri)
            || "newADUser.html".equals(uri)) {
          return indexPage;
        }
        return uri;
      }
      if (DATABASE.value.equals(authenticationType) && SAAS.equals(kwInstallationType)) {
        return defaultPageSaas;
      } else {
        return defaultPage;
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      if ("login.html".equals(uri)
          || "register.html".equals(uri)
          || "registerSaas.html".equals(uri)
          || "registerLdap.html".equals(uri)
          || uri.contains("registrationReview")
          || uri.contains("userActivation")
          || "forgotPassword.html".equals(uri)
          || "newADUser.html".equals(uri)
          || "terms.html".equals(uri)
          || "feedback.html".equals(uri)) return uri;

      if (DATABASE.value.equals(authenticationType) && SAAS.equals(kwInstallationType)) {
        return defaultPageSaas;
      } else {
        return defaultPage;
      }
    }
  }

  public String checkAnonymousLogin(
      String uri, OAuth2AuthenticationToken auth2AuthenticationToken) {
    try {
      DefaultOAuth2User defaultOAuth2User =
          (DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

      // if user does not exist in db
      if (manageDatabase
              .getHandleDbRequests()
              .getUsersInfo((String) defaultOAuth2User.getAttributes().get("preferred_username"))
          == null) {

        return registerStagingUser(
            (String) defaultOAuth2User.getAttributes().get("preferred_username"),
            defaultOAuth2User.getAttributes().get("name"));
      }

      if (auth2AuthenticationToken.isAuthenticated()) {
        return uri;
      } else {
        return oauthLoginPage;
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      return oauthLoginPage;
    }
  }

  public String checkAuth(
      String uri,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken authentication) {
    if ("tenants.html".equals(uri)) {
      UserDetails userDetails =
          (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

      if (SUPERADMIN
              .name()
              .equals(
                  manageDatabase
                      .getHandleDbRequests()
                      .getUsersInfo(userDetails.getUsername())
                      .getRole())
          && commonUtilsService.getTenantId(userDetails.getUsername())
              == KwConstants.DEFAULT_TENANT_ID) {
        return getReturningPage(uri);
      } else {
        uri = "index";
      }
    }

    if ("true".equals(ssoEnabled)) {
      if (uri.contains("register") || uri.equals("registrationReview.html")) {
        return uri;
      } else {
        return checkAnonymousLogin(uri, authentication);
      }
    } else {
      return getReturningPage(uri);
    }
  }

  public String registerStagingUser(String userName, Object fullName) {
    try {
      log.info("User found in SSO/AD and not in Klaw db :{}", userName);
      String existingRegistrationId =
          manageDatabase.getHandleDbRequests().getRegistrationId(userName);

      if (existingRegistrationId != null) {
        if ("PENDING_ACTIVATION".equals(existingRegistrationId)) {
          return "redirect:" + "registrationReview";
        } else {
          return "redirect:" + "register?userRegistrationId=" + existingRegistrationId;
        }
      } else {
        String randomId = UUID.randomUUID().toString();

        RegisterUserInfoModel registerUserInfoModel = new RegisterUserInfoModel();
        registerUserInfoModel.setRegistrationId(randomId);
        registerUserInfoModel.setStatus("STAGING");
        registerUserInfoModel.setTeam(KwConstants.STAGINGTEAM);
        registerUserInfoModel.setRegisteredTime(new Timestamp(System.currentTimeMillis()));
        registerUserInfoModel.setUsername(userName);
        registerUserInfoModel.setPwd("");
        if (fullName != null) registerUserInfoModel.setFullname((String) fullName);

        RegisterUserInfo registerUserInfo = new RegisterUserInfo();
        copyProperties(registerUserInfoModel, registerUserInfo);
        registerUserInfo.setTeamId(
            manageDatabase.getTeamIdFromTeamName(
                KwConstants.DEFAULT_TENANT_ID, registerUserInfo.getTeam()));

        manageDatabase.getHandleDbRequests().registerUserForAD(registerUserInfo);

        return "redirect:" + "register?userRegistrationId=" + randomId;
      }
    } catch (Exception e) {
      log.error("Unable to find mail/name fields.", e);
      return "registerLdap.html";
    }
  }
}
