package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.*;
import static io.aiven.klaw.helpers.KwConstants.*;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.enums.RolesType.SUPERADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.nimbusds.jose.shaded.json.JSONArray;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.model.RegisterUserInfoModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.NewUserStatus;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
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

  @Value("${klaw.ad.roles.attribute:roles}")
  private String rolesAttribute;

  @Value("${klaw.ad.teams.attribute:teams}")
  private String teamsAttribute;

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
      String uri,
      OAuth2AuthenticationToken auth2AuthenticationToken,
      HttpServletResponse response) {
    DefaultOAuth2User defaultOAuth2User =
        (DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // Extract attributes for user verification/registration
    String userName = commonUtilsService.extractUserNameFromOAuthUser(defaultOAuth2User);

    // if user does not exist in db
    if (manageDatabase.getHandleDbRequests().getUsersInfo(userName) == null) {
      Pair<Boolean, String> roleValidationPair = Pair.of(null, null);
      Pair<Boolean, String> teamValidationPair = Pair.of(null, null);
      // If enableUserAuthorizationFromAD is true, retrieve roles and teams from AD token and match
      // with klaw metadata
      if (enableUserAuthorizationFromAD) {
        Set<String> klawRoles =
            manageDatabase.getRolesPermissionsPerTenant(DEFAULT_TENANT_ID).keySet();
        Set<String> klawTeams = manageDatabase.getTeamNamesForTenant(DEFAULT_TENANT_ID);

        // extract role from AD token authorities
        roleValidationPair =
            getRoleFromTokenAuthorities(defaultOAuth2User, userName, klawRoles, response);
        if (!roleValidationPair.getLeft()) {
          sendResponse(response, roleValidationPair);
          return oauthLoginPage;
        }

        // extract team from AD attribute configured at klaw.ad.teams.attribute
        teamValidationPair =
            extractClaimsFromAD(defaultOAuth2User, userName, teamsAttribute, klawTeams, response);
        if (!teamValidationPair.getLeft()) {
          sendResponse(response, teamValidationPair);
          return oauthLoginPage;
        }
      }
      return registerStagingUser(
          userName,
          defaultOAuth2User.getAttributes().get(nameAttribute),
          roleValidationPair.getRight(),
          teamValidationPair.getRight());
    }

    if (auth2AuthenticationToken.isAuthenticated()) {
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
      DefaultOAuth2User defaultOAuth2User,
      String userName,
      Set<String> klawRoles,
      HttpServletResponse response) {
    String roleFromClaim = null;
    Collection<? extends GrantedAuthority> authorities = defaultOAuth2User.getAuthorities();
    int claimMatched = 0;
    for (GrantedAuthority authority : authorities) {
      String authorityRole = authority.getAuthority().substring(5); // ex : ROLE_USER
      if (klawRoles.stream().anyMatch(authorityRole::equalsIgnoreCase)) {
        claimMatched++;
        roleFromClaim = authorityRole;
      }
    }

    Pair<Boolean, String> claimValidationPair =
        validateClaims(claimMatched, userName, "roles", response);
    if (claimValidationPair.getLeft()) { // valid claim
      return Pair.of(Boolean.TRUE, roleFromClaim);
    } else {
      return claimValidationPair;
    }
  }

  private Pair<Boolean, String> extractClaimsFromAD(
      DefaultOAuth2User defaultOAuth2User,
      String userName,
      String claimKey,
      Set<String> klawRolesTeams,
      HttpServletResponse response) {
    String claimValue = null;
    int claimMatched = 0;
    try {
      JSONArray jsonArray = (JSONArray) defaultOAuth2User.getAttributes().get(claimKey);
      for (Object jsonObject : jsonArray) {
        String jsonElement = jsonObject.toString();
        if (klawRolesTeams.stream().anyMatch(jsonElement::equalsIgnoreCase)) {
          claimMatched++;
          claimValue = jsonElement;
        }
      }
    } catch (Exception e) {
      log.error("Error in retrieving claims, ignore");
    }
    // Multiple roles/teams matched : Klaw roles/teams against AD roles/teams. throw exception -
    // deny login
    Pair<Boolean, String> claimValidationPair =
        validateClaims(claimMatched, userName, "teams", response);
    if (claimValidationPair.getLeft()) {
      return Pair.of(Boolean.TRUE, claimValue);
    } else {
      return claimValidationPair;
    }
  }

  // if no claims matched, or mulitple claims matched, deny registering/login to the user. claimType
  // can be 'roles' or 'teams'.
  private Pair<Boolean, String> validateClaims(
      int claimMatched, String userName, String claimType, HttpServletResponse response) {
    String errorCode = "";
    Boolean validClaim = Boolean.TRUE;
    if (claimMatched == 0) {
      if (claimType.equals("roles")) {
        errorCode = ACTIVE_DIRECTORY_ERR_CODE_101;
        log.error(AD_ERROR_101_NO_MATCHING_ROLE + "{}", userName);
      } else {
        errorCode = ACTIVE_DIRECTORY_ERR_CODE_102;
        log.error(AD_ERROR_102_NO_MATCHING_TEAM + "{}", userName);
      }
      validClaim = Boolean.FALSE;
    } else if (claimMatched > 1) {
      if (claimType.equals("roles")) {
        errorCode = ACTIVE_DIRECTORY_ERR_CODE_103;
        log.error(AD_ERROR_103_MULTIPLE_MATCHING_ROLE + "{}", userName);
      } else {
        errorCode = ACTIVE_DIRECTORY_ERR_CODE_104;
        log.error(AD_ERROR_104_MULTIPLE_MATCHING_TEAM + "{}", userName);
      }
      validClaim = Boolean.FALSE;
    }
    return Pair.of(validClaim, errorCode);
  }

  public String checkAuth(
      String uri,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken authentication) {
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
          && commonUtilsService.getTenantId(userDetails.getUsername()) == DEFAULT_TENANT_ID) {
        return getReturningPage(uri);
      } else {
        uri = "index";
      }
    }

    if ("true".equals(ssoEnabled)) {
      if (uri.contains("register") || uri.equals("registrationReview.html")) {
        return uri;
      } else {
        if (authentication instanceof OAuth2AuthenticationToken) {
          return checkAnonymousLogin(uri, (OAuth2AuthenticationToken) authentication, response);
        } else {
          return oauthLoginPage;
        }
      }
    } else {
      return getReturningPage(uri);
    }
  }

  // register user with staging status, and forward to signup
  public String registerStagingUser(
      String userName, Object fullName, String roleFromAD, String teamFromAD) {
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
        registerUserInfoModel.setTeam(
            Objects.requireNonNullElse(teamFromAD, KwConstants.STAGINGTEAM));
        registerUserInfoModel.setRole(
            Objects.requireNonNullElse(roleFromAD, KwConstants.USER_ROLE));
        registerUserInfoModel.setRegisteredTime(new Timestamp(System.currentTimeMillis()));
        registerUserInfoModel.setUsername(userName);
        registerUserInfoModel.setPwd("");
        if (fullName != null) registerUserInfoModel.setFullname((String) fullName);

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
