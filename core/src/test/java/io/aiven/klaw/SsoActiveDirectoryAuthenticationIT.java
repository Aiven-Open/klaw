package io.aiven.klaw;

import static io.aiven.klaw.UsersTeamsControllerIT.OBJECT_MAPPER;
import static io.aiven.klaw.UsersTeamsControllerIT.superAdmin;
import static io.aiven.klaw.helpers.KwConstants.STAGINGTEAM;
import static io.aiven.klaw.model.enums.NewUserStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames.ID_TOKEN;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.requests.RegisterUserInfoModel;
import io.aiven.klaw.model.response.RegisterUserInfoModelResponse;
import io.aiven.klaw.model.response.UserInfoModelResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
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

  private static MockMethods mockMethods;

  @BeforeAll
  public static void setup() {
    mockMethods = new MockMethods();
  }

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
                                  getOidcUser(
                                      superAdmin))) // oidc login with valid preferredName/claims
                      // and authorities
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
                          user(superAdmin)
                              .password(
                                  "superAdminPwd")) // Invalid login for AD context authentication
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      assertThat(response.getContentAsString())
          .doesNotContain(
              "dashboardApp"); // after invalid login, user returned to login.html and not dashboard
      assertThat(response.getContentAsString())
          .contains("oauthLoginApp"); // after invalid login, user returned to login.html
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    ;
  }

  /*
   1. Login with valid AD user, but user doesn't exist in klaw
   2. A record is created in kwregisterusers table in STAGING state and User is routed to register page to submit all details and signup.
   3. After user registers, record in kwregisterusers table has an updated status of PENDING
   4. superadmin now approves and the user should exist in kwusers
  */
  @Test
  @Order(3)
  public void invokeRootPageWithOidcLoginFailure() {
    String nonExistingUserInKlaw = "testuser";
    try {
      // From browser, this triggers a user to be created in staging users table (kwregisterusers),
      // if successful login in SSO but user doesn't exist in klaw
      mvc.perform(
              MockMvcRequestBuilders.get("/")
                  .with(
                      oidcLogin()
                          .oidcUser(
                              getOidcUser(
                                  nonExistingUserInKlaw))) // oidc login with non existing user
                  .contentType(MediaType.APPLICATION_JSON)
                  .accept(MediaType.APPLICATION_JSON))
          .andReturn()
          .getResponse();
      RegisterUserInfoModel userInfoModel =
          mockMethods.getRegisterUserInfoModel(nonExistingUserInKlaw, "USER");
      // ad user should not have a password set
      userInfoModel.setPwd("");
      String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

      // Allow the user to signup
      MockHttpServletResponse response2 =
          mvc.perform(
                  MockMvcRequestBuilders.post("/registerUser")
                      .with(csrf())
                      .with(oidcLogin().oidcUser(getOidcUser(nonExistingUserInKlaw)))
                      .content(jsonReq)
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      ApiResponse objectResponse =
          new ObjectMapper().readValue(response2.getContentAsString(), ApiResponse.class);
      assertThat(objectResponse.isSuccess()).isTrue();

      // Allow the superadmin to fetch requests to approve
      MockHttpServletResponse response3 =
          mvc.perform(
                  MockMvcRequestBuilders.get("/getNewUserRequests")
                      .with(csrf())
                      .with(oidcLogin().oidcUser(getOidcUser(superAdmin)))
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();

      List<RegisterUserInfoModelResponse> userInfoModelActualList =
          new ObjectMapper().readValue(response3.getContentAsString(), new TypeReference<>() {});

      assertThat(userInfoModelActualList.get(0).getUsername()).isEqualTo(nonExistingUserInKlaw);
      assertThat(userInfoModelActualList.get(0).getStatus()).isEqualTo(PENDING.value);

      // Allow the superadmin to approve the user
      MockHttpServletResponse response4 =
          mvc.perform(
                  MockMvcRequestBuilders.post("/execNewUserRequestApprove")
                      .with(csrf())
                      .with(oidcLogin().oidcUser(getOidcUser(superAdmin)))
                      .param("username", nonExistingUserInKlaw)
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      ApiResponse objectResponse1 =
          new ObjectMapper().readValue(response4.getContentAsString(), ApiResponse.class);
      assertThat(objectResponse1.isSuccess()).isTrue();

      // Without csrf, forbidden to POST
      mvc.perform(
              MockMvcRequestBuilders.post("/execNewUserRequestApprove")
                  .with(oidcLogin().oidcUser(getOidcUser(superAdmin)))
                  .param("username", nonExistingUserInKlaw)
                  .contentType(MediaType.APPLICATION_JSON)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden());

      // Fetch and see if user is now created
      MockHttpServletRequestBuilder obj =
          MockMvcRequestBuilders.get("/getUserDetails")
              .with(oidcLogin().oidcUser(getOidcUser(superAdmin)))
              .param("userId", nonExistingUserInKlaw)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON);

      String userDetailsResponse =
          mvc.perform(
                  MockMvcRequestBuilders.get("/getUserDetails")
                      .with(oidcLogin().oidcUser(getOidcUser(superAdmin)))
                      .param("userId", nonExistingUserInKlaw)
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      UserInfoModelResponse userInfoModelActual =
          new ObjectMapper().readValue(userDetailsResponse, new TypeReference<>() {});
      assertThat(userInfoModelActual.getTeam()).isEqualTo(STAGINGTEAM);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private OidcUser getOidcUser(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("groups", "ROLE_USER");
    claims.put("sub", 123);
    claims.put("preferred_username", username); // existing user with default installation
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
