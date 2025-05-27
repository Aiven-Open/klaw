package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.*;
import static io.aiven.klaw.helpers.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.RolesType.SUPERADMIN;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.constants.UriConstants;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UiControllerLoginService {

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${spring.ad.domain:#{null}}")
  private String adDomain;

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

  public String getReturningPage(String uri, String userName) {
    try {
      if (userName != null) {
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
        UserInfo userInfo = reqsHandle.getUsersInfo(userName);

        if (userInfo == null) {
          SecurityContextHolder.getContext().setAuthentication(null);

          if (ACTIVE_DIRECTORY.value.equals(authenticationType)) {
            return UriConstants.REGISTER_LDAP_PAGE;
          }
          return UriConstants.REGISTER_PAGE;
        }

        log.debug("Authenticated user : {}", userName);
        if (UriConstants.LOGIN_PAGE.equals(uri)
            || UriConstants.HOME_PAGE.equals(uri)
            || UriConstants.REGISTER_PAGE.equals(uri)
            || uri.contains(UriConstants.REGISTRATION_REVIEW)
            || UriConstants.FORGOT_PASSWORD_PAGE.equals(uri)
            || UriConstants.NEW_AD_USER_PAGE.equals(uri)) {
          return UriConstants.INDEX_PAGE;
        }
        return uri;
      }

      return UriConstants.DEFAULT_PAGE;

    } catch (Exception e) {
      log.error("Exception:", e);
      if (UriConstants.LOGIN_PAGE.equals(uri)
          || UriConstants.REGISTER_PAGE.equals(uri)
          || UriConstants.REGISTER_LDAP_PAGE.equals(uri)
          || uri.contains(UriConstants.REGISTRATION_REVIEW)
          || UriConstants.FORGOT_PASSWORD_PAGE.equals(uri)
          || UriConstants.NEW_AD_USER_PAGE.equals(uri)
          || UriConstants.TERMS_PAGE.equals(uri)
          || UriConstants.FEEDBACK_PAGE.equals(uri)) return uri;

      return UriConstants.DEFAULT_PAGE;
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

    Object principal = abstractAuthenticationToken.getPrincipal();
    // Extract attributes for user verification/registration
    if (abstractAuthenticationToken instanceof OAuth2AuthenticationToken) {
      if (principal instanceof DefaultOAuth2User) {
        defaultOAuth2User = (DefaultOAuth2User) principal;
        nameAttribute = (String) defaultOAuth2User.getAttributes().get(nameAttribute);
        authorities = defaultOAuth2User.getAuthorities();
      }
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
            return UriConstants.OAUTH_LOGIN;
          }
        }
      }
      String extractedDomain = extractDomain(principal);
      return registerStagingUser(
          userName, nameAttribute, roleValidationPair.getRight(), extractedDomain);
    }

    if (abstractAuthenticationToken.isAuthenticated()) {
      return uri;
    } else {
      return UriConstants.OAUTH_LOGIN;
    }
  }

  public String extractDomain(Object principal) {
    try {
      if (principal instanceof LdapUserDetailsImpl) {
        String distinguishedName = ((LdapUserDetailsImpl) principal).getDn();
        return getLdapName(distinguishedName);
      } else if (principal instanceof String distinguishedName
          && ((String) principal).contains("DC=")) {
        return getLdapName(distinguishedName);
      } else if (principal instanceof DefaultOAuth2User defaultOAuth2User) {
        Object email = defaultOAuth2User.getAttributes().get("email");
        if (email instanceof String emailStr && emailStr.contains("@")) {
          return emailStr.substring(emailStr.indexOf("@") + 1);
        }
      }
    } catch (Exception e) {
      log.error("Could not extract domain from principal");
    }
    return null;
  }

  private static String getLdapName(String distinguishedName) {
    try {
      LdapName ldapName = new LdapName(distinguishedName);
      List<String> domainComponents = new ArrayList<>();
      for (Rdn rdn : ldapName.getRdns()) {
        if (rdn.getType().equalsIgnoreCase("DC")) {
          domainComponents.add(rdn.getValue().toString());
        }
      }
      if (!domainComponents.isEmpty()) {
        Collections.reverse(domainComponents); // Always reverse to get correct domain order
        return String.join(".", domainComponents);
      }
    } catch (Exception e) {
      log.error("Could not extract domain from principal");
    }
    return null;
  }

  // redirect the user to login page with error display
  private static void sendResponse(
      HttpServletResponse response, Pair<Boolean, String> validationPair) {
    try {
      // Display error to the user based on error code
      response.sendRedirect(UriConstants.LOGIN_ERROR_CODE + validationPair.getRight());
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
      userName = commonUtilsService.getUserName(defaultOAuth2User);
    } else if (abstractAuthenticationToken instanceof UsernamePasswordAuthenticationToken) {
      userName =
          ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
              .getUsername();
    }

    if (UriConstants.TENANTS_PAGE.equals(uri)) {
      if (SUPERADMIN
              .name()
              .equals(manageDatabase.getHandleDbRequests().getUsersInfo(userName).getRole())
          && commonUtilsService.getTenantId(userName) == DEFAULT_TENANT_ID) {
        return getReturningPage(uri, userName);
      } else {
        uri = UriConstants.INDEX;
      }
    }

    if ("true".equals(ssoEnabled)) {
      if (uri.contains(UriConstants.REGISTER)
          || uri.equals(UriConstants.REGISTRATION_REVIEW_PAGE)
          || uri.equals(UriConstants.FORGOT_PASSWORD)
          || uri.equals(UriConstants.FORGOT_PASSWORD_PAGE)) {
        return uri;
      } else {
        if (abstractAuthenticationToken instanceof OAuth2AuthenticationToken) {
          return checkAnonymousLogin(uri, abstractAuthenticationToken, response, userName);
        } else {
          return UriConstants.OAUTH_LOGIN;
        }
      }
    } else {
      if (uri.contains(UriConstants.REGISTER)
          || uri.equals(UriConstants.REGISTRATION_REVIEW_PAGE)
          || uri.equals(UriConstants.FORGOT_PASSWORD)
          || uri.equals(UriConstants.FORGOT_PASSWORD_PAGE)) {
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
  public String registerStagingUser(
      String userName, Object fullName, String roleFromAD, String adDomainFromPrincipal) {
    try {
      log.info("User found in SSO/AD and not in Klaw db :{}", userName);
      String existingRegistrationId =
          manageDatabase.getHandleDbRequests().getRegistrationId(userName);

      if (existingRegistrationId != null) {
        if ("PENDING_ACTIVATION".equals(existingRegistrationId)) {
          return UriConstants.REDIRECT + UriConstants.REGISTRATION_REVIEW;
        } else {
          return UriConstants.REDIRECT
              + UriConstants.REGISTER_USER_REGISTRATION_ID
              + existingRegistrationId;
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

        if (adDomainFromPrincipal != null && !adDomainFromPrincipal.isBlank()) {
          registerUserInfoModel.setMailid(userName + "@" + adDomainFromPrincipal);
        } else if (adDomain != null && !adDomain.isBlank()) {
          registerUserInfoModel.setMailid(userName + "@" + adDomain);
        }

        registerUserInfoModel.setUsername(userName);
        registerUserInfoModel.setPwd("");
        if (fullName != null) {
          String fullNameStr =
              ((String) fullName).replace(",", ""); // Look for other characters in names
          registerUserInfoModel.setFullname(fullNameStr);
        }

        RegisterUserInfo registerUserInfo = new RegisterUserInfo();
        copyProperties(registerUserInfoModel, registerUserInfo);
        registerUserInfo.setTeamId(
            manageDatabase.getTeamIdFromTeamName(DEFAULT_TENANT_ID, registerUserInfo.getTeam()));
        registerUserInfo.setTenantId(DEFAULT_TENANT_ID);

        String result = manageDatabase.getHandleDbRequests().registerUserForAD(registerUserInfo);
        if (result.equals(ApiResultStatus.SUCCESS.value))
          return UriConstants.REDIRECT + UriConstants.REGISTER_USER_REGISTRATION_ID + randomId;
        else return "";
      }
    } catch (Exception e) {
      log.error("Unable to find mail/name fields.", e);
      return UriConstants.REGISTER_LDAP_PAGE;
    }
  }
}
