package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.TeamModel;
import io.aiven.klaw.model.UserInfoModel;
import java.util.List;
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
public class UsersTeamsControllerIT {

  private static final String INFRATEAM_ID = "1001";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static MockMethods mockMethods;

  @Autowired private MockMvc mvc;

  private static String superAdmin = "superadmin";
  private static String superAdminPwd = "kwsuperadmin123$$";
  private static String user1 = "kwusera", user2 = "kwuserb";
  private static String teamName = "Octopus";
  private static String userPwd = "user";

  private static final String INFRATEAM = "INFRATEAM";
  private static final String PASSWORD = "user";

  @BeforeAll
  public static void setup() {
    mockMethods = new MockMethods();
  }

  // Create user with USER role success
  @Test
  @Order(1)
  public void createRequiredUsers() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel = mockMethods.getUserInfoModel(user1, role, INFRATEAM);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewUser")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    //        assertThat(response, CoreMatchers.containsString("success"));

    userInfoModel = mockMethods.getUserInfoModel(user2, role, "INFRATEAM");
    jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewUser")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    //        assertThat(response, CoreMatchers.containsString("success"));
  }

  // Create team success
  @Test
  @Order(1)
  public void createTeamSuccess() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(teamModelRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTeamDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("teamId", "1003")
                    .param("tenantName", "default")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TeamModel teamModel = OBJECT_MAPPER.readValue(response, TeamModel.class);
    assertThat(teamModel.getTeamname()).isEqualTo(teamName);
  }

  // Create same team again, failure
  @Test
  @Order(2)
  public void createSameTeamAgainFailure() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(teamModelRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("Failure. Team already exists");
  }

  // Create team failure, invalid team mail id
  @Test
  @Order(3)
  public void createTeamWithInvalidEmailId() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModelFailure(teamName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(teamModelRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().is4xxClientError()) // validation error
            .andReturn()
            .getResponse()
            .getContentAsString();
  }

  // Modify team success
  @Test
  @Order(4)
  public void modifyTeamSuccess() throws Exception {
    String emailId = "testteam@testteam.com";
    TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
    teamModelRequest.setTenantId(101);
    teamModelRequest.setTeamId(1003);
    teamModelRequest.setTeammail(emailId);

    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(teamModelRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/updateTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTeamDetails")
                    .with(user(superAdmin).password(superAdmin))
                    .param("teamId", "1003")
                    .param("tenantName", "default")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    TeamModel teamModel = OBJECT_MAPPER.readValue(response, TeamModel.class);
    assertThat(teamModel.getTeammail()).isEqualTo(emailId);
  }

  // Create team failure, not authorized
  @Test
  @Order(6)
  public void createTeamFailureNotAuthorized() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(teamModelRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewTeam")
                    .with(user(user1).password(userPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("Not Authorized");
  }

  // Delete team success
  @Test
  @Order(7)
  public void deleteTeamSuccess() throws Exception {
    String newTeam = "Testteam";
    TeamModel teamModelRequest = mockMethods.getTeamModel(newTeam);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(teamModelRequest);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/addNewTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteTeamRequest")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("teamId", "1004")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getTeamDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("teamId", "1004")
                    .param("tenantName", "default")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEmpty();
  }

  // Delete team failure
  @Test
  @Order(8)
  public void deleteTeamFailure() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteTeamRequest")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("teamId", INFRATEAM_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    //         TODO Assertion works on Windows and not in linux env. Need to take another look on
    // the order of execution.
    //         assertThat(response, CoreMatchers.containsString("Team cannot be deleted."));
  }

  // Delete user with USER role success
  @Test
  @Order(9)
  public void deleteUserSuccess() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteUserRequest")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("userId", user1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).contains("success");

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getUserDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("userId", user1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response).isEmpty();
  }

  // Get teams getAllTeamsSU - for superadmin gets all teams in all tenants
  @Test
  @Order(10)
  public void getAllTeams() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getAllTeamsSU")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<TeamModel> teamModels = OBJECT_MAPPER.readValue(response, List.class);
    assertThat(teamModels).hasSize(3);
  }
}
