package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.ApiResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test-application-rdbms.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class RolesPermissionsControllerIT {

  private static final String INFRATEAM_ID = "1001";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static MockMethods mockMethods;
  @Autowired private MockMvc mvc;
  private static final String superAdmin = "superadmin";
  private static final String superAdminPwd = "kwsuperadmin123$$";

  @BeforeAll
  public static void setup() {
    mockMethods = new MockMethods();
  }

  // Create a role
  @Test
  @Order(1)
  public void addNewRole() throws Exception {
    String testRole = "TESTROLE";
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addRoleId")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("roleId", testRole)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getRolesFromDb")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<String> rolesList = new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(rolesList).contains(testRole);
  }

  // Assign permissions to new role
  @Test
  @Order(2)
  public void addPermissionsToNewRole() throws Exception {
    String testRole = "TESTROLE";
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getPermissions")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Map<String, Boolean> firstEntry = new HashMap<>();
    firstEntry.put("VIEW_TOPICS", true);
    Map<String, Boolean> secondEntry = new HashMap<>();
    secondEntry.put("ADD_EDIT_DELETE_CLUSTERS", false);

    Map<String, List<Map<String, Boolean>>> permissionsList =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(permissionsList.get(testRole)).contains(firstEntry);
    assertThat(permissionsList.get(testRole)).contains(secondEntry);

    String jsonReq =
        OBJECT_MAPPER.writer().writeValueAsString(mockMethods.getPermissions(testRole));
    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/updatePermissions")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getPermissions")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    firstEntry = new HashMap<>();
    firstEntry.put("VIEW_TOPICS", false);
    secondEntry = new HashMap<>();
    secondEntry.put("ADD_EDIT_DELETE_CLUSTERS", true);

    permissionsList = new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(permissionsList.get(testRole)).contains(firstEntry);
    assertThat(permissionsList.get(testRole)).contains(secondEntry);
  }

  // Delete the role
  @Test
  @Order(3)
  public void deleteRole() throws Exception {
    String testRole = "TESTROLE";
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteRole")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("roleId", testRole)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiResponse response1 = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(response1.isSuccess()).isTrue();

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getRolesFromDb")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<String> rolesList = new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(rolesList).doesNotContain(testRole);
  }
}
