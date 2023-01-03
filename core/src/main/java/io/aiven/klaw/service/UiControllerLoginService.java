package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.enums.RolesType.SUPERADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.nimbusds.jose.shaded.json.JSONArray;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
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
    try {
      DefaultOAuth2User defaultOAuth2User =
          (DefaultOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      // Extract attributes for user verification/registration
      String userName = commonUtilsService.extractUserNameFromOAuthUser(defaultOAuth2User);

      // if user does not exist in db
      if (manageDatabase.getHandleDbRequests().getUsersInfo(userName) == null) {
        String roleFromClaim = null, teamFromClaim = null;
        if (enableUserAuthorizationFromAD) {
          roleFromClaim = getRoleFromTokenAuthorities(defaultOAuth2User, userName);
          Set<String> klawTeams = manageDatabase.getTeamNamesForTenant(DEFAULT_TENANT_ID);

          //          roleFromClaim =
          //              extractClaimsFromAD(defaultOAuth2User, userName, rolesAttribute,
          // klawRoles);
          teamFromClaim =
              extractClaimsFromAD(defaultOAuth2User, userName, teamsAttribute, klawTeams);
        }
        return registerStagingUser(
            userName,
            defaultOAuth2User.getAttributes().get(nameAttribute),
            roleFromClaim,
            teamFromClaim);
      }

      if (auth2AuthenticationToken.isAuthenticated()) {
        return uri;
      } else {
        return oauthLoginPage;
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      try {
        // Display error to the user based on error code
        response.sendRedirect("login?errorCode=AD101");
      } catch (IOException ex) {
        log.error("Ignore error from response redirect !");
      }
      return oauthLoginPage;
    }
  }

  private String getRoleFromTokenAuthorities(DefaultOAuth2User defaultOAuth2User, String userName)
      throws KlawException {

    String roleFromClaim = null;
    Set<String> klawRoles = manageDatabase.getRolesPermissionsPerTenant(DEFAULT_TENANT_ID).keySet();
    Collection<? extends GrantedAuthority> authorities = defaultOAuth2User.getAuthorities();
    int claimMatched = 0;
    for (GrantedAuthority authority : authorities) {
      String authorityRole = authority.getAuthority().substring(5); // ex : ROLE_USER
      if (klawRoles.stream().anyMatch(authorityRole::equalsIgnoreCase)) {
        claimMatched++;
        roleFromClaim = authorityRole;
      }
    }

    if (claimMatched == 0) {
      String errorTxt =
          "No roles in AD are configured for the user. Please make sure a matching"
              + " role is configured in AD. Denying login !! : ";
      log.error(errorTxt + "{}", userName);
      throw new KlawException(errorTxt + userName);
    } else if (claimMatched > 1) {
      String errorTxt =
          "Multiple roles in AD are configured for the user. Please make sure only one matching"
              + " role is configured in AD. Denying login !! : ";
      log.error(errorTxt + "{}", userName);
      throw new KlawException(errorTxt + userName);
    }
    return roleFromClaim;
  }

  private String extractClaimsFromAD(
      DefaultOAuth2User defaultOAuth2User,
      String userName,
      String claimKey,
      Set<String> klawRolesTeams)
      throws KlawException {
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
    if (claimMatched == 0) {
      String errorTxt =
          "No teams in AD are configured for the user. Please make sure a matching"
              + " team is configured in AD. Denying login !! : ";
      log.error(errorTxt + "{}", userName);
      throw new KlawException(errorTxt + userName);
    } else if (claimMatched > 1) {
      String errorTxt =
          "Multiple "
              + claimKey
              + " in AD are configured for the user. Please make sure only one matching"
              + " role or team is configured in AD. Denying login !! : ";
      log.error(errorTxt + "{}", userName);
      throw new KlawException(errorTxt + userName);
    }

    return claimValue;
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
