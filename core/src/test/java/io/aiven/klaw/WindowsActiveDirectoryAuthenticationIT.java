package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
// Config file with property klaw.login.authentication.type set to 'ad' and spring.ad.* set
@TestPropertySource(locations = "classpath:test-application-rdbms-windows-ad.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class WindowsActiveDirectoryAuthenticationIT {

  @Autowired private MockMvc mvc;

  // Login with user profile with success
  @Test
  @Order(1)
  public void invokeRootPageWithAdUserLoginSuccess() {
    try {
      MockHttpServletResponse response =
          mvc.perform(
                  MockMvcRequestBuilders.get("/")
                      .with(user("superadmin").password("superAdminPwd")) // active directory login
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

  // In AD context, submitting plain username/password credentials. User doesn't exist in klaw, so
  // redirect user to register
  @Test
  @Order(2)
  public void invokeRootPageWithADUserRegistration() {
    try {
      MockHttpServletResponse response =
          mvc.perform(
                  MockMvcRequestBuilders.get("/")
                      .with(user("testuser").password("testpwd")) // User doesn't exist in klaw
                      // authentication
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andReturn()
              .getResponse();
      assertThat(response.getContentAsString())
          .doesNotContain(
              "dashboardApp"); // after invalid login, user returned to login.html and not dashboard
      assertThat(response.getRedirectedUrl())
          .contains("register"); // after login, user returned to register page, as user is ad
      // authenticated, but does not exist in klaw
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
