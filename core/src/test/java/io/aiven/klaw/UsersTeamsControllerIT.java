package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.TeamModel;
import io.aiven.klaw.model.UserInfoModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
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
  public static final String STAGINGTEAM = "STAGINGTEAM";

  private static MockMethods mockMethods;

  @Autowired private MockMvc mvc;

  private static String superAdmin = "superadmin";
  private static String superAdminPwd = "kwsuperadmin123$$";
  private static String user1 = "kwusera",
      user2 = "kwuserb",
      switchUser1 = "kwuserc",
      switchUser2 = "kwuserd",
      switchUser3 = "kwusere";
  private static String octopusTeamName = "Octopus";
  private static String userPwd = "user";

  private static final String INFRATEAM = "INFRATEAM";

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
  }

  // Create team success
  @Test
  @Order(1)
  public void createTeamSuccess() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModel(octopusTeamName);
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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
    assertThat(teamModel.getTeamname()).isEqualTo(octopusTeamName);
  }

  // Create same team again, failure
  @Test
  @Order(2)
  public void createSameTeamAgainFailure() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModel(octopusTeamName);
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
    ApiResponse objectResponse = new ObjectMapper().readValue(response, ApiResponse.class);

    assertThat(objectResponse.getResult()).contains("Failure. Team already exists");
  }

  // Create team failure, invalid team mail id
  @Test
  @Order(3)
  public void createTeamWithInvalidEmailId() throws Exception {
    TeamModel teamModelRequest = mockMethods.getTeamModelFailure(octopusTeamName);
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
    TeamModel teamModelRequest = mockMethods.getTeamModel(octopusTeamName);
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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
    TeamModel teamModelRequest = mockMethods.getTeamModel(octopusTeamName);
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

    assertThat(response).contains(ApiResultStatus.NOT_AUTHORIZED.value);
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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

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

    List<TeamModel> teamModels = OBJECT_MAPPER.readValue(response, new TypeReference<>() {});
    assertThat(teamModels).hasSize(3);
  }

  // Create user with USER role, switch teams
  @Test
  @Order(11)
  public void createUserWithSwitchTeams() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel =
        mockMethods.getUserInfoModelSwitchTeams(
            switchUser1, role, INFRATEAM, 2); // switch teams 1001, 1002
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
    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(12)
  public void getSwitchTeamsOfUser() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/user/" + switchUser1 + "/switchTeamsList")
                    .with(user(superAdmin).password(superAdmin))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<TeamModel> teamModelList =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(teamModelList)
        .hasSize(2)
        .extracting(TeamModel::getTeamId)
        .containsExactlyInAnyOrder(1001, 1002);
    assertThat(teamModelList)
        .extracting(TeamModel::getTeamname)
        .containsExactlyInAnyOrder(INFRATEAM, STAGINGTEAM);
  }

  @Test
  @Order(13)
  public void createUserWithSwitchTeamsOwnTeamIsNotInSwitchTeamsFailure() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel =
        mockMethods.getUserInfoModelSwitchTeams(switchUser2, role, octopusTeamName, 2);
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
    assertThat(response).contains("Please select your own team, in the switch teams list.");
  }

  @Test
  @Order(14)
  public void createUserWithSwitchTeamsOnlyOneTeamSelectedFailure() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel =
        mockMethods.getUserInfoModelSwitchTeams(
            switchUser3, role, octopusTeamName, 1); // 1 switch team
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
    assertThat(response).contains("Please make sure atleast 2 teams are selected.");
  }

  @Test
  @Order(15)
  public void createUserWithSwitchTeamsNoTeamSelectedFailure() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel =
        mockMethods.getUserInfoModelSwitchTeams(
            "kwuserf", role, octopusTeamName, 0); // 0 switch teams
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
    assertThat(response).contains("Please make sure atleast 2 teams are selected.");
  }

  @Test
  @Order(16)
  public void updateUserTeam() throws Exception {
    UserInfoModel userInfoModel = new UserInfoModel();
    userInfoModel.setUsername(switchUser1); // base team : INFRATEAM 1001
    int newTeamId = 1002;
    userInfoModel.setTeamId(newTeamId); // 1002 is one of the switch teams for this user
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/user/updateTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).contains(ApiResultStatus.SUCCESS.value);

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getUserDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("userId", switchUser1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UserInfoModel userInfoModelActual =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(userInfoModelActual.getTeamId()).isEqualTo(newTeamId);
    assertThat(userInfoModelActual.getTeam()).isEqualTo(STAGINGTEAM);
    assertThat(userInfoModelActual.getSwitchAllowedTeamIds())
        .hasSize(2)
        .containsExactlyInAnyOrder(1001, 1002);
  }

  @Test
  @Order(17)
  public void updateUserTeamInvalidSwitchTeam() throws Exception {
    UserInfoModel userInfoModel = new UserInfoModel();
    userInfoModel.setUsername(
        switchUser1); // base team : STAGINGTEAM 1002, switch teams : 1001, 1002
    int newTeamId = 1003; // Octopus, not in switch teams
    userInfoModel.setTeamId(newTeamId);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/user/updateTeam")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).contains(ApiResultStatus.NOT_AUTHORIZED.value);

    response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getUserDetails")
                    .with(user(superAdmin).password(superAdminPwd))
                    .param("userId", switchUser1)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UserInfoModel userInfoModelActual =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(userInfoModelActual.getTeamId()).isEqualTo(1002); // no change
    assertThat(userInfoModelActual.getTeam()).isEqualTo(STAGINGTEAM); // no change, old team
  }

  // Update user with USER role, switch teams
  @Test
  @Order(18)
  public void updateUserWithSwitchTeams() throws Exception {
    String role = "USER";
    UserInfoModel userInfoModel =
        mockMethods.getUserInfoModelSwitchTeams(
            user2, role, INFRATEAM, 2); // add switch teams 1001, 1002
    userInfoModel.setTeamId(1001);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfoModel);

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/updateUser")
                    .with(user(superAdmin).password(superAdminPwd))
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(response).contains(ApiResultStatus.SUCCESS.value);
  }

  @Test
  @Order(19)
  public void getSwitchTeamsOfUpdatedUser() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/user/" + user2 + "/switchTeamsList")
                    .with(user(superAdmin).password(superAdmin))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<TeamModel> teamModelList =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(teamModelList)
        .hasSize(2)
        .extracting(TeamModel::getTeamId)
        .containsExactlyInAnyOrder(1001, 1002);
    assertThat(teamModelList)
        .extracting(TeamModel::getTeamname)
        .containsExactlyInAnyOrder(INFRATEAM, STAGINGTEAM);
  }

  @Test
  @Order(20)
  public void showUserList() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/showUserList")
                    .with(user(superAdmin).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    List<UserInfoModel> userInfoModelList =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(userInfoModelList).hasSizeBetween(3, 6); // superadmin, kwuserb, kwuserc
    assertThat(
            userInfoModelList.stream()
                .filter(userInfo -> userInfo.getUsername().equals(switchUser1))
                .findFirst()
                .get()
                .getSwitchAllowedTeamIds())
        .containsExactlyInAnyOrder(1001, 1002);
    assertThat(
            userInfoModelList.stream()
                .filter(userInfo -> userInfo.getUsername().equals(user2))
                .findFirst()
                .get()
                .getSwitchAllowedTeamIds())
        .containsExactlyInAnyOrder(1001, 1002);
  }

  @Test
  @Order(21)
  public void myProfileInfo() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.get("/getMyProfileInfo")
                    .with(user(switchUser1).password(superAdminPwd))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    UserInfoModel userInfoModelActual =
        new ObjectMapper().readValue(response, new TypeReference<>() {});
    assertThat(userInfoModelActual.getTeamId()).isEqualTo(1002); // no change
    assertThat(userInfoModelActual.getTeam()).isEqualTo(STAGINGTEAM); // no change, old team
    assertThat(userInfoModelActual.getSwitchAllowedTeamIds())
        .hasSize(2)
        .containsExactlyInAnyOrder(1001, 1002);
  }
}
