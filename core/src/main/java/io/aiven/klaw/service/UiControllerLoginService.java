package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.*;
import static io.aiven.klaw.helpers.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.enums.RolesType.SUPERADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.NewUserStatus;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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

  @Value("${klaw.enable.authorization.ad:false}")
  private boolean enableUserAuthorizationFromAD;

  @Value("${klaw.ad.name.attribute:name}")
  private String nameAttribute;

  @Value("${klaw.enable.sso:false}")
  private String ssoEnabled;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Autowired ManageDatabase manageDatabase;

  @Autowired CommonUtilsService commonUtilsService;

  @Autowired(required = false)
  private OAuth2AuthorizedClientService authorizedClientService;

  private static final String defaultPage = "login.html";
  private static final String defaultPageSaas = "loginSaas.html";
  private static final String indexPage = "index.html";
  private static final String oauthLoginPage = "oauthLogin";

  public String getReturningPage(String uri, String userName) {
    try {
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
      String uri,
      AbstractAuthenticationToken abstractAuthenticationToken,
      HttpServletResponse response,
      String userName) {
    DefaultOAuth2User defaultOAuth2User = null;
    String nameAttribute = "name";
    Collection<? extends GrantedAuthority> authorities = null;

    // Extract attributes for user verification/registration
    if (abstractAuthenticationToken instanceof OAuth2AuthenticationToken) {
      defaultOAuth2User =
          (DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      nameAttribute = (String) defaultOAuth2User.getAttributes().get(nameAttribute);
      authorities = defaultOAuth2User.getAuthorities();
    } else if (abstractAuthenticationToken instanceof UsernamePasswordAuthenticationToken) {
      nameAttribute = abstractAuthenticationToken.getName();
      authorities = abstractAuthenticationToken.getAuthorities();
    }

    // if user does not exist in db
    if (manageDatabase.getHandleDbRequests().getUsersInfo(userName) == null) {
      Pair<Boolean, String> roleValidationPair = Pair.of(null, null);
      // If enableUserAuthorizationFromAD is true, retrieve roles from AD token and match
      // with klaw metadata
      if (enableUserAuthorizationFromAD) {
        Set<String> klawRoles =
            manageDatabase.getRolesPermissionsPerTenant(DEFAULT_TENANT_ID).keySet();

        // extract role from AD token authorities
        if ("true".equals(ssoEnabled) && defaultOAuth2User != null) {
          roleValidationPair =
              getRoleFromTokenAuthorities(authorities, userName, klawRoles, response);
          if (!roleValidationPair.getLeft()) {
            sendResponse(response, roleValidationPair);
            return oauthLoginPage;
          }
        }
      }
      return registerStagingUser(userName, nameAttribute, roleValidationPair.getRight());
    }

    if (abstractAuthenticationToken.isAuthenticated()) {
      return uri;
    } else {
      return oauthLoginPage;
    }
  }

  // redirect the user to login page with error display
  private static void sendResponse(
      HttpServletResponse response, Pair<Boolean, String> validationPair) {
    try {
      // Display error to the user based on error code
      response.sendRedirect("login?errorCode=" + validationPair.getRight());
    } catch (IOException ex) {
      log.error("Ignore error from response redirect !");
    }
  }

  private Pair<Boolean, String> getRoleFromTokenAuthorities(
      Collection<? extends GrantedAuthority> authorities,
      String userName,
      Set<String> klawRoles,
      HttpServletResponse response) {
    String roleFromClaim = null;
    int claimMatched = 0;
    for (GrantedAuthority authority : authorities) {
      String authorityStr = authority.getAuthority();
      int indexOfRoleSeparator = authorityStr.indexOf("_");
      if (indexOfRoleSeparator > 0) {
        String authorityRole = authorityStr.substring(indexOfRoleSeparator + 1); // ex : ROLE_USER
        if (klawRoles.stream().anyMatch(authorityRole::equalsIgnoreCase)) {
          claimMatched++;
          roleFromClaim = authorityRole;
        }
      }
    }

    Pair<Boolean, String> claimValidationPair = validateClaims(claimMatched, userName, "roles");
    if (claimValidationPair.getLeft()) { // valid claim
      return Pair.of(Boolean.TRUE, roleFromClaim);
    } else {
      return claimValidationPair;
    }
  }

  // if no claims matched, or multiple claims matched, deny registering/login to the user. claimType
  // can be 'roles' or 'teams'.
  private Pair<Boolean, String> validateClaims(
      int claimMatched, String userName, String claimType) {
    String errorCode = "";
    Boolean validClaim = Boolean.TRUE;
    if (claimMatched == 0) {
      if (claimType.equals("roles")) {
        errorCode = ACTIVE_DIRECTORY_ERR_CODE_101;
        log.error(AD_ERROR_101_NO_MATCHING_ROLE + "{}", userName);
      }
      validClaim = Boolean.FALSE;
    } else if (claimMatched > 1) {
      if (claimType.equals("roles")) {
        errorCode = ACTIVE_DIRECTORY_ERR_CODE_103;
        log.error(AD_ERROR_103_MULTIPLE_MATCHING_ROLE + "{}", userName);
      }
      validClaim = Boolean.FALSE;
    }
    return Pair.of(validClaim, errorCode);
  }

  public String checkAuth(
      String uri,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    String userName = null;
    if (abstractAuthenticationToken instanceof OAuth2AuthenticationToken) {
      DefaultOAuth2User defaultOAuth2User =
          (DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      userName = commonUtilsService.extractUserNameFromOAuthUser(defaultOAuth2User);
    } else if (abstractAuthenticationToken instanceof UsernamePasswordAuthenticationToken) {
      userName =
          ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
              .getUsername();
    }

    if ("tenants.html".equals(uri)) {
      if (SUPERADMIN
              .name()
              .equals(manageDatabase.getHandleDbRequests().getUsersInfo(userName).getRole())
          && commonUtilsService.getTenantId(userName) == DEFAULT_TENANT_ID) {
        return getReturningPage(uri, userName);
      } else {
        uri = "index";
      }
    }

    if ("true".equals(ssoEnabled)) {
      if (uri.contains("register")
          || uri.equals("registrationReview.html")
          || uri.equals("forgotPassword")
          || uri.equals("forgotPassword.html")) {
        return uri;
      } else {
        if (abstractAuthenticationToken instanceof OAuth2AuthenticationToken) {
          return checkAnonymousLogin(uri, abstractAuthenticationToken, response, userName);
        } else {
          return oauthLoginPage;
        }
      }
    } else {
      if (uri.contains("register")
          || uri.equals("registrationReview.html")
          || uri.equals("forgotPassword")
          || uri.equals("forgotPassword.html")) {
        return uri;
      } else {
        if (ACTIVE_DIRECTORY.value.equals(authenticationType)) {
          return checkAnonymousLogin(uri, abstractAuthenticationToken, response, userName);
        }
        return getReturningPage(uri, userName);
      }
    }
  }

  // register user with staging status, and forward to signup
  public String registerStagingUser(String userName, Object fullName, String roleFromAD) {
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
        registerUserInfoModel.setStatus(NewUserStatus.STAGING.value);
        registerUserInfoModel.setTeam(KwConstants.STAGINGTEAM);
        registerUserInfoModel.setRole(
            Objects.requireNonNullElse(roleFromAD, KwConstants.USER_ROLE));
        registerUserInfoModel.setRegisteredTime(new Timestamp(System.currentTimeMillis()));
        registerUserInfoModel.setUsername(userName);
        registerUserInfoModel.setPwd("");
        if (fullName != null) {
          String fullNameStr =
              ((String) fullName).replaceAll(",", ""); // Look for other characters in names
          registerUserInfoModel.setFullname(fullNameStr);
        }

        RegisterUserInfo registerUserInfo = new RegisterUserInfo();
        copyProperties(registerUserInfoModel, registerUserInfo);
        registerUserInfo.setTeamId(
            manageDatabase.getTeamIdFromTeamName(DEFAULT_TENANT_ID, registerUserInfo.getTeam()));
        registerUserInfo.setTenantId(DEFAULT_TENANT_ID);

        String result = manageDatabase.getHandleDbRequests().registerUserForAD(registerUserInfo);
        if (result.equals(ApiResultStatus.SUCCESS.value))
          return "redirect:" + "register?userRegistrationId=" + randomId;
        else return "";
      }
    } catch (Exception e) {
      log.error("Unable to find mail/name fields.", e);
      return "registerLdap.html";
    }
  }
}
