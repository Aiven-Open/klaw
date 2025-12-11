package io.aiven.klaw;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
@TestPropertySource(locations = "classpath:test-application-rdbms1.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
class SecurityConfigNoSSOIT {

  @Autowired private MockMvc mockMvc;

  // CSRF should prevent unauthorized POSTs to secure endpoints
  @Test
  void testCsrfProtection() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.post("/updateProfile"))
        .andExpect(status().isForbidden());
  }

  // Logout should be allowed without CSRF
  @Test
  void testCsrfIgnoredForLogout() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.post("/logout")).andExpect(status().is3xxRedirection());
  }

  // Logout is not allowed as it's secured
  @Test
  void testLogout() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.post("/logout").with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
  }

  // Static resources should be permitted without login
  @Test
  void testStaticResourcesAreAccessible() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/assets/css/style.css")).andExpect(status().isOk());
  }

  // Should be accessible to un-authenticated users
  @Test
  void testEndpointWithoutAuthentication() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/getRoles")).andExpect(status().isOk());
  }

  // Should be accessible to authenticated users
  @Test
  @WithMockUser
  void testAuthenticatedEndpoint() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/getRequestTypeStatuses"))
        .andExpect(status().isOk());
  }

  // Redirect to login for secured end point
  @Test
  void testUnauthorizedAccess() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/getRequestTypeStatuses"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }
}
