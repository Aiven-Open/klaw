package com.kafkamgt.integrationtests.rdbms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.integrationtests.rdbms.utils.MockMethods;
import com.kafkamgt.uiapi.UiapiApplication;
import com.kafkamgt.uiapi.model.TeamModel;
import com.kafkamgt.uiapi.model.UserInfoModel;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes=UiapiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations="classpath:test-application-rdbms.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class UsersTeamsControllerIT {

    private static final String INFRATEAM_ID = "1001";

    private static MockMethods mockMethods;

    @Autowired
    private MockMvc mvc;

    private static String superAdmin = "superadmin";
    private static String superAdminPwd = "kwsuperadmin123$$";
    private static String user1 = "kwusera", user2="kwuserb";
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
        String jsonReq = new ObjectMapper().writer().writeValueAsString(userInfoModel);

        String response = mvc.perform(MockMvcRequestBuilders
                        .post("/addNewUser").with(user(superAdmin).password(superAdminPwd))
                        .content(jsonReq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

//        assertThat(response, CoreMatchers.containsString("success"));

        userInfoModel = mockMethods.getUserInfoModel(user2, role, "INFRATEAM");
        jsonReq = new ObjectMapper().writer().writeValueAsString(userInfoModel);

        response = mvc.perform(MockMvcRequestBuilders
                        .post("/addNewUser").with(user(superAdmin).password(superAdminPwd))
                        .content(jsonReq)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

//        assertThat(response, CoreMatchers.containsString("success"));
    }

    // Create team success
    @Test
    @Order(1)
    public void createTeamSuccess() throws Exception {
        TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(teamModelRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewTeam").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getTeamDetails").with(user(superAdmin).password(superAdminPwd))
                .param("teamId","1003")
                .param("tenantName","default")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TeamModel teamModel = new ObjectMapper().readValue(response, TeamModel.class);
        assertEquals(teamName, teamModel.getTeamname());
    }

    // Create same team again, failure
    @Test
    @Order(2)
    public void createSameTeamAgainFailure() throws Exception {
        TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(teamModelRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewTeam").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("Failure. Team already exists"));
    }

    // Create team failure, invalid team mail id
    @Test
    @Order(3)
    public void createTeamWithInvalidEmailId() throws Exception {
        TeamModel teamModelRequest = mockMethods.getTeamModelFailure(teamName);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(teamModelRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewTeam").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())// validation error
                .andReturn().getResponse().getContentAsString();
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

        String jsonReq = new ObjectMapper().writer().writeValueAsString(teamModelRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/updateTeam").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getTeamDetails").with(user(superAdmin).password(superAdmin))
                .param("teamId","1003")
                .param("tenantName","default")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TeamModel teamModel = new ObjectMapper().readValue(response, TeamModel.class);
        assertEquals(emailId, teamModel.getTeammail());
    }


    // Create team failure, not authorized
    @Test
    @Order(6)
    public void createTeamFailureNotAuthorized() throws Exception {
        TeamModel teamModelRequest = mockMethods.getTeamModel(teamName);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(teamModelRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewTeam").with(user(user1).password(userPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("Not Authorized"));
    }

    // Delete team success
    @Test
    @Order(7)
    public void deleteTeamSuccess() throws Exception {
        String newTeam = "Testteam";
        TeamModel teamModelRequest = mockMethods.getTeamModel(newTeam);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(teamModelRequest);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewTeam").with(user(superAdmin).password(superAdminPwd))
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteTeamRequest").with(user(superAdmin).password(superAdminPwd))
                .param("teamId","1004")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getTeamDetails").with(user(superAdmin).password(superAdminPwd))
                .param("teamId","1004")
                .param("tenantName","default")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("", response);
    }

    // Delete team failure
    @Test
    @Order(8)
    public void deleteTeamFailure() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteTeamRequest").with(user(superAdmin).password(superAdminPwd))
                .param("teamId",INFRATEAM_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("Not allowed to delete this team"));
    }

    // Delete user with USER role success
    @Test
    @Order(9)
    public void deleteUserSuccess() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteUserRequest").with(user(superAdmin).password(superAdminPwd))
                .param("userId",user1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));

        response = mvc.perform(MockMvcRequestBuilders
                .get("/getUserDetails").with(user(superAdmin).password(superAdminPwd))
                .param("userId",user1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("", response);
    }

    // Get teams getAllTeamsSU - for superadmin gets all teams in all tenants
    @Test
    @Order(10)
    public void getAllTeams() throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders
                .get("/getAllTeamsSU").with(user(superAdmin).password(superAdminPwd))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<TeamModel> teamModels = new ObjectMapper().readValue(response, List.class);
        assertEquals(3, teamModels.size());
    }
}