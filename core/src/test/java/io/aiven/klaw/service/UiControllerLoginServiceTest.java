package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.constants.TestConstants;
import io.aiven.klaw.constants.UriConstants;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RolesType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.catalina.connector.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UiControllerLoginServiceTest {
  @Mock HandleDbRequestsJdbc handleDbRequestsJdbc;
  @Mock private ManageDatabase manageDatabase;
  @Mock private CommonUtilsService commonUtilsService;
  @InjectMocks @Spy private UiControllerLoginService uiControllerLoginService;
  @Mock private DefaultOAuth2User defaultOAuth2User;
  @Mock private UserDetails userDetails;
  @Mock Authentication authentication;

  @BeforeEach
  public void setUp() {
    ReflectionTestUtils.setField(uiControllerLoginService, "authenticationType", "db");
    ReflectionTestUtils.setField(uiControllerLoginService, "enableUserAuthorizationFromAD", false);
    ReflectionTestUtils.setField(uiControllerLoginService, "nameAttribute", "name");
    ReflectionTestUtils.setField(uiControllerLoginService, "ssoEnabled", "false");
    ReflectionTestUtils.setField(uiControllerLoginService, "kwInstallationType", "onpremise");
  }

  private void loginMock() {
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  public void getReturningPage_UserNameNull() {
    Assertions.assertEquals(
        UriConstants.DEFAULT_PAGE, uiControllerLoginService.getReturningPage("", null));
  }

  @Test
  public void getReturningPage_UserNameNull_SAAS() {
    ReflectionTestUtils.setField(uiControllerLoginService, "kwInstallationType", "saas");

    Assertions.assertEquals(
        UriConstants.DEFAULT_PAGE_SAAS, uiControllerLoginService.getReturningPage("", null));
  }

  @Test
  public void getReturningPage_UserNameNotNull_UserInfoNotNull() {
    UserInfo userInfo = new UserInfo();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(userInfo);

    Assertions.assertEquals(
        "", uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @Test
  public void getReturningPage_UserNameNotNull_UserInfoNotNull_SAAS() {
    ReflectionTestUtils.setField(uiControllerLoginService, "kwInstallationType", "saas");
    UserInfo userInfo = new UserInfo();
    KwTenants kwTenants = new KwTenants();
    kwTenants.setIsActive("false");

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(userInfo);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(TestConstants.TENANT_ID);
    Mockito.when(manageDatabase.getTenantFullConfig(TestConstants.TENANT_ID)).thenReturn(kwTenants);

    Assertions.assertEquals(
        UriConstants.TENANT_INFO_PAGE,
        uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        UriConstants.LOGIN_PAGE,
        UriConstants.LOGIN_SAAS_PAGE,
        UriConstants.HOME_PAGE,
        UriConstants.REGISTER_PAGE,
        UriConstants.FORGOT_PASSWORD_PAGE,
        UriConstants.NEW_AD_USER_PAGE,
        UriConstants.REGISTRATION_REVIEW,
        UriConstants.USER_ACTIVATION
      })
  public void getReturningPage_UserNameNotNull_UserInfoNotNull_IndexPage(String uri) {
    UserInfo userInfo = new UserInfo();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(userInfo);

    Assertions.assertEquals(
        UriConstants.INDEX_PAGE,
        uiControllerLoginService.getReturningPage(uri, TestConstants.USERNAME));
  }

  @Test
  public void getReturningPage_UserNameNotNull_UserInfoNull_SAAS() {
    ReflectionTestUtils.setField(uiControllerLoginService, "kwInstallationType", "saas");

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);

    Assertions.assertEquals(
        UriConstants.REGISTER_SAAS_PAGE,
        uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @Test
  public void getReturningPage_UserNameNotNull_UserInfoNull_ActiveDirectory() {
    ReflectionTestUtils.setField(uiControllerLoginService, "authenticationType", "ad");

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);

    Assertions.assertEquals(
        UriConstants.REGISTER_LDAP_PAGE,
        uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @Test
  public void getReturningPage_UserNameNotNull_UserInfoNull() {
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);

    Assertions.assertEquals(
        UriConstants.REGISTER_PAGE,
        uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        UriConstants.LOGIN_PAGE,
        UriConstants.REGISTER_PAGE,
        UriConstants.REGISTER_SAAS_PAGE,
        UriConstants.REGISTER_LDAP_PAGE,
        UriConstants.FORGOT_PASSWORD_PAGE,
        UriConstants.NEW_AD_USER_PAGE,
        UriConstants.TERMS_PAGE,
        UriConstants.FEEDBACK_PAGE,
        UriConstants.REGISTRATION_REVIEW,
        UriConstants.USER_ACTIVATION
      })
  public void getReturningPage_Failure(String uri) {
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME))
        .thenThrow(new RuntimeException("Error Occurred"));

    Assertions.assertEquals(
        uri, uiControllerLoginService.getReturningPage(uri, TestConstants.USERNAME));
  }

  @Test
  public void getReturningPage_Failure_SAAS() {
    ReflectionTestUtils.setField(uiControllerLoginService, "kwInstallationType", "saas");

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME))
        .thenThrow(new RuntimeException("Error Occurred"));

    Assertions.assertEquals(
        UriConstants.DEFAULT_PAGE_SAAS,
        uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @Test
  public void getReturningPage_Failure_NotSAAS() {
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME))
        .thenThrow(new RuntimeException("Error Occurred"));

    Assertions.assertEquals(
        UriConstants.DEFAULT_PAGE,
        uiControllerLoginService.getReturningPage("", TestConstants.USERNAME));
  }

  @Test
  public void checkAnonymousLogin_UserAuthorizationFromAdDisabled() {
    AbstractAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            TestConstants.USERNAME,
            TestConstants.PASSWORD,
            List.of(new SimpleGrantedAuthority("role")));
    HttpServletResponse response = new Response();

    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);
    Mockito.when(handleDbRequestsJdbc.getRegistrationId(TestConstants.USERNAME)).thenReturn(null);
    Mockito.when(
            manageDatabase.getTeamIdFromTeamName(
                KwConstants.DEFAULT_TENANT_ID, KwConstants.STAGINGTEAM))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.when(handleDbRequestsJdbc.registerUserForAD(ArgumentMatchers.any()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    String actual =
        uiControllerLoginService.checkAnonymousLogin(
            "", authenticationToken, response, TestConstants.USERNAME);

    Assertions.assertTrue(
        actual.contains(UriConstants.REDIRECT + UriConstants.REGISTER_USER_REGISTRATION_ID));
  }

  @Test
  public void checkAnonymousLogin_UserAuthorizationFromAdEnabled() {
    ReflectionTestUtils.setField(uiControllerLoginService, "enableUserAuthorizationFromAD", true);
    ReflectionTestUtils.setField(uiControllerLoginService, "ssoEnabled", "true");
    AbstractAuthenticationToken authenticationToken = Mockito.mock(OAuth2AuthenticationToken.class);
    HttpServletResponse response = new Response();

    loginMock();
      Mockito.when(authentication.getPrincipal()).thenReturn(defaultOAuth2User);
      Mockito.when(defaultOAuth2User.getAttributes()).thenReturn(Map.of("name", TestConstants.USERNAME));
    Mockito.when(defaultOAuth2User.getAuthorities())
        .thenReturn(
            (Collection)
                List.of(new SimpleGrantedAuthority(TestConstants.ROLE + "_" + TestConstants.ROLE)));
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);
    Mockito.when(manageDatabase.getRolesPermissionsPerTenant(KwConstants.DEFAULT_TENANT_ID))
        .thenReturn(Map.of(TestConstants.ROLE, List.of(TestConstants.PERMISSION)));
    Mockito.when(handleDbRequestsJdbc.getRegistrationId(TestConstants.USERNAME)).thenReturn(null);
    Mockito.when(
            manageDatabase.getTeamIdFromTeamName(
                KwConstants.DEFAULT_TENANT_ID, KwConstants.STAGINGTEAM))
        .thenReturn(TestConstants.TEAM_ID);
    Mockito.when(handleDbRequestsJdbc.registerUserForAD(ArgumentMatchers.any()))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    String actual =
        uiControllerLoginService.checkAnonymousLogin(
            "", authenticationToken, response, TestConstants.USERNAME);

    Assertions.assertTrue(
        actual.contains(UriConstants.REDIRECT + UriConstants.REGISTER_USER_REGISTRATION_ID));
  }

  @Test
  public void checkAnonymousLogin_UserAuthorizationFromAdEnabled_MultipleRoleMatched() {
    ReflectionTestUtils.setField(uiControllerLoginService, "enableUserAuthorizationFromAD", true);
    ReflectionTestUtils.setField(uiControllerLoginService, "ssoEnabled", "true");
    AbstractAuthenticationToken authenticationToken = Mockito.mock(OAuth2AuthenticationToken.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    loginMock();
      Mockito.when(authentication.getPrincipal()).thenReturn(defaultOAuth2User);
      Mockito.when(defaultOAuth2User.getAttributes()).thenReturn(Map.of("name", TestConstants.USERNAME));
    Mockito.when(defaultOAuth2User.getAuthorities())
        .thenReturn(
            (Collection)
                List.of(
                    new SimpleGrantedAuthority(TestConstants.ROLE + "_" + TestConstants.ROLE),
                    new SimpleGrantedAuthority(TestConstants.ROLE + "_" + TestConstants.ROLE)));
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);
    Mockito.when(manageDatabase.getRolesPermissionsPerTenant(KwConstants.DEFAULT_TENANT_ID))
        .thenReturn(Map.of(TestConstants.ROLE, List.of(TestConstants.PERMISSION)));

    String actual =
        uiControllerLoginService.checkAnonymousLogin(
            "", authenticationToken, response, TestConstants.USERNAME);

    Assertions.assertEquals(actual, UriConstants.OAUTH_LOGIN);
  }

  @Test
  public void checkAnonymousLogin_UserAuthorizationFromAdEnabled_NoRoleMatched() {
    ReflectionTestUtils.setField(uiControllerLoginService, "enableUserAuthorizationFromAD", true);
    ReflectionTestUtils.setField(uiControllerLoginService, "ssoEnabled", "true");
    AbstractAuthenticationToken authenticationToken = Mockito.mock(OAuth2AuthenticationToken.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    loginMock();
      Mockito.when(authentication.getPrincipal()).thenReturn(defaultOAuth2User);
      Mockito.when(defaultOAuth2User.getAttributes()).thenReturn(Map.of("name", TestConstants.USERNAME));
    Mockito.when(defaultOAuth2User.getAuthorities()).thenReturn(List.of());
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(null);
    Mockito.when(manageDatabase.getRolesPermissionsPerTenant(KwConstants.DEFAULT_TENANT_ID))
        .thenReturn(Map.of(TestConstants.ROLE, List.of(TestConstants.PERMISSION)));

    String actual =
        uiControllerLoginService.checkAnonymousLogin(
            "", authenticationToken, response, TestConstants.USERNAME);

    Assertions.assertEquals(actual, UriConstants.OAUTH_LOGIN);
  }

  @Test
  public void checkAuth_TenantsPage() {
    AbstractAuthenticationToken authenticationToken =
        new UsernamePasswordAuthenticationToken(
            TestConstants.USERNAME,
            TestConstants.PASSWORD,
            List.of(new SimpleGrantedAuthority("role")));
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    UserInfo userInfo = new UserInfo();
    userInfo.setRole(RolesType.SUPERADMIN.name());

    loginMock();
      Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
      Mockito.when(userDetails.getUsername()).thenReturn(TestConstants.USERNAME);
    Mockito.when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    Mockito.when(handleDbRequestsJdbc.getUsersInfo(TestConstants.USERNAME)).thenReturn(userInfo);
    Mockito.when(commonUtilsService.getTenantId(TestConstants.USERNAME))
        .thenReturn(KwConstants.DEFAULT_TENANT_ID);
    Mockito.doReturn(UriConstants.TENANTS_PAGE)
        .when(uiControllerLoginService)
        .getReturningPage(UriConstants.TENANTS_PAGE, TestConstants.USERNAME);

    String actual =
        uiControllerLoginService.checkAuth(
            UriConstants.TENANTS_PAGE, request, response, authenticationToken);
    Assertions.assertEquals(UriConstants.TENANTS_PAGE, actual);
  }

    @ParameterizedTest
    @CsvSource({
            UriConstants.REGISTER + ", true",
            UriConstants.REGISTRATION_REVIEW_PAGE + ", true",
            UriConstants.FORGOT_PASSWORD + ", true",
            UriConstants.FORGOT_PASSWORD_PAGE + ", true",
            UriConstants.REGISTER + ", false",
            UriConstants.REGISTRATION_REVIEW_PAGE + ", false",
            UriConstants.FORGOT_PASSWORD + ", false",
            UriConstants.FORGOT_PASSWORD_PAGE + ", false"
    })
    public void checkAuth(String url, String ssoEnabled) {
        ReflectionTestUtils.setField(uiControllerLoginService, "ssoEnabled", ssoEnabled);
        AbstractAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        TestConstants.USERNAME,
                        TestConstants.PASSWORD,
                        List.of(new SimpleGrantedAuthority("role")));
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setRole(RolesType.SUPERADMIN.name());

        loginMock();
        Mockito.when(authentication.getPrincipal()).thenReturn(userDetails);
        Mockito.when(userDetails.getUsername()).thenReturn(TestConstants.USERNAME);

        String actual =
                uiControllerLoginService.checkAuth(
                        url, request, response, authenticationToken);
        Assertions.assertEquals(url, actual);
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void checkAuth_OAuth2AuthenticationToken(String ssoEnabled) {
        ReflectionTestUtils.setField(uiControllerLoginService, "ssoEnabled", ssoEnabled);
        ReflectionTestUtils.setField(uiControllerLoginService, "authenticationType", "ad");
        AbstractAuthenticationToken authenticationToken = Mockito.mock(OAuth2AuthenticationToken.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setRole(RolesType.SUPERADMIN.name());

        loginMock();
        Mockito.when(authentication.getPrincipal()).thenReturn(defaultOAuth2User);
        Mockito.doReturn("")
                .when(uiControllerLoginService)
                .checkAnonymousLogin("", authenticationToken, response, null);
        String actual =
                uiControllerLoginService.checkAuth(
                        "", request, response, authenticationToken);
        Assertions.assertEquals("", actual);
    }


}
