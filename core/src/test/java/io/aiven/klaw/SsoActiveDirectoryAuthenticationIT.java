package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
// Config file with property klaw.login.authentication.type set to 'ad' and klaw.enable.sso set to
// true
@TestPropertySource(locations = "classpath:test-application-rdbms-sso-ad.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class SsoActiveDirectoryAuthenticationIT {

  @Autowired private MockMvc mvc;

  // Login with Oidc profile with success
  @Test
  @Order(1)
  public void invokeRootPageWithOidcLoginSuccess() {
    try {
      MockHttpServletResponse response =
          mvc.perform(
                  MockMvcRequestBuilders.get("/")
                      .with(
                          oidcLogin()
                              .oidcUser(
                                  getOidcUser())) // oidc login with valid preferredName/claims and
                      // authorities
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      assertThat(response.getContentAsString())
          .contains(
              "dashboardApp"); // after successful login, returned index.html with dashboard details
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // In AD/SSO context, submitting plain username/password credentials would fail to process, and
  // user returned to login page
  @Test
  @Order(2)
  public void invokeRootPageWithBasicLoginFailure() {
    try {
      MockHttpServletResponse response =
          mvc.perform(
                  MockMvcRequestBuilders.get("/")
                      .with(
                          user("superadmin")
                              .password("superAdminPwd")) // Invalid login for AD context
                      // authentication
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      assertThat(response.getContentAsString())
          .doesNotContain(
              "dashboardApp"); // after invalid login, user returned to login.html and not dashboard
      assertThat(response.getContentAsString())
          .contains("loginSaasApp"); // after invalid login, user returned to login.html
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    ;
  }

  private OidcUser getOidcUser() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("groups", "ROLE_USER");
    claims.put("sub", 123);
    claims.put("preferred_username", "superadmin"); // existing user with default installation
    OidcIdToken idToken =
        new OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims);
    Collection<GrantedAuthority> authorities = getAuthorities();
    return new DefaultOidcUser(authorities, idToken);
  }

  private static Collection<GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    return authorities;
  }
}
