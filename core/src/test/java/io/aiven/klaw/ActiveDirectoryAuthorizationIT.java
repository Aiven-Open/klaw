package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

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
// true. klaw.enable.authorization.ad=true means role and team are retrieved from AD token
@TestPropertySource(locations = "classpath:test-application-rdbms-ad-authorization.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class ActiveDirectoryAuthorizationIT {

  @Autowired private MockMvc mvc;

  // Register a user with a no role and no team in AD authorities/attributes - deny signup with
  // error
  @Test
  @Order(1)
  public void adRegisterUserWithNoRoleAndNoTeamSignupDeny() {
    String errorCode = "AD101";
    try {
      MockHttpServletResponse response =
          mvc.perform(
                  MockMvcRequestBuilders.get("/")
                      .with(
                          oidcLogin()
                              .oidcUser(
                                  getOidcUser(
                                      "newuser", "NONE"))) // oidc register with no role and team
                      // authorities
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      assertThat(response.getRedirectedUrl()).contains("login?errorCode=" + errorCode); //
      // dashboard details
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Register a user with a valid role and no team in AD authorities/attributes - deny signup with
  // error
  @Test
  @Order(1)
  public void adRegisterUserWithValidRoleAndNoTeamSignupDeny() {
    String errorCode = "AD102";
    try {
      MockHttpServletResponse response =
          mvc.perform(
                  MockMvcRequestBuilders.get("/")
                      .with(
                          oidcLogin()
                              .oidcUser(
                                  getOidcUser(
                                      "newuser",
                                      "VALID"))) // oidc register with valid role and no team
                      // authorities
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      assertThat(response.getRedirectedUrl()).contains("login?errorCode=" + errorCode); //
      // dashboard details
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private OidcUser getOidcUser(String username, String authorityType) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("groups", "ROLE_USER");
    claims.put("sub", 123);
    claims.put("preferred_username", username); // existing user with default installation
    OidcIdToken idToken =
        new OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims);
    Collection<GrantedAuthority> authorities = getAuthorities(authorityType);
    return new DefaultOidcUser(authorities, idToken);
  }

  private static Collection<GrantedAuthority> getAuthorities(String authorityType) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    if (authorityType.equals("VALID")) authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    return authorities;
  }
}
