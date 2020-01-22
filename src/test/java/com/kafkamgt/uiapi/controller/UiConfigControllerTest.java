package com.kafkamgt.uiapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.service.UiConfigControllerService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class UiConfigControllerTest {

    @MockBean
    private UiConfigControllerService uiConfigControllerService;

    private UiConfigController uiConfigController;

    private UtilMethods utilMethods;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        uiConfigController = new UiConfigController();
        mvc = MockMvcBuilders
                .standaloneSetup(uiConfigController)
                .dispatchOptions(true)
                .build();
        utilMethods = new UtilMethods();
        ReflectionTestUtils.setField(uiConfigController, "uiConfigControllerService", uiConfigControllerService);
    }

    @Test
    public void getEnvs() throws Exception {
        List<Env> envList = utilMethods.getEnvList();
        when(uiConfigControllerService.getEnvs(eq(true))).thenReturn(envList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getEnvs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Env> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getEnvsStatus() throws Exception {
        List<Env> envList = utilMethods.getEnvList();
        when(uiConfigControllerService.getEnvs(eq(false))).thenReturn(envList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getEnvsStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Env> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getClusterApiStatus() throws Exception {
        Env env = utilMethods.getEnvList().get(0);
        when(uiConfigControllerService.getClusterApiStatus()).thenReturn(env);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getClusterApiStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Env response = new ObjectMapper().readValue(res, Env.class);
        assertEquals("DEV", response.getName());
    }

    @Test
    public void getSchemaRegEnvs() throws Exception {
        List<Env> envList = utilMethods.getEnvList();
        when(uiConfigControllerService.getSchemaRegEnvs()).thenReturn(envList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getSchemaRegEnvs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Env> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getSchemaRegEnvsStatus() throws Exception {
        List<Env> envList = utilMethods.getEnvList();
        when(uiConfigControllerService.getSchemaRegEnvsStatus()).thenReturn(envList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getSchemaRegEnvsStatus")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Env> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getAllTeams() throws Exception {
        List<Team> teamList = utilMethods.getTeams();
        when(uiConfigControllerService.getAllTeams()).thenReturn(teamList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getAllTeams")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Team> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getAllTeamsSU() throws Exception {
        List<Team> teamList = utilMethods.getTeams();
        when(uiConfigControllerService.getAllTeamsSU()).thenReturn(teamList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getAllTeamsSU")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Team> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void addNewEnv() throws Exception {
        Env env = utilMethods.getEnvList().get(0);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(env);
        when(uiConfigControllerService.addNewEnv(any())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewEnv")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void deleteCluster() throws Exception {
        when(uiConfigControllerService.deleteCluster(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteClusterRequest")
                .param("clusterId", "clusterId101")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void deleteTeam() throws Exception {
        when(uiConfigControllerService.deleteTeam(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteTeamRequest")
                .param("teamId", "Team101")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void deleteUser() throws Exception {
        when(uiConfigControllerService.deleteUser(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteUserRequest")
                .param("userId", "uiuser1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void addNewUser() throws Exception {
        UserInfo userInfo = utilMethods.getUserInfoMock();
        String jsonReq = new ObjectMapper().writer().writeValueAsString(userInfo);
        when(uiConfigControllerService.addNewUser(any())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewUser")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void addNewTeam() throws Exception {
        Team team = utilMethods.getTeams().get(0);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(team);
        String result =  "{ \"status\": \"" + "success" + "\" }";
        when(uiConfigControllerService.addNewTeam(any())).thenReturn(result);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/addNewTeam")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    @Test
    public void changePwd() throws Exception {
        when(uiConfigControllerService.changePwd(any())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/chPwd")
                .param("changePwd","newpasswd")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void showUsers() throws Exception {
        List<UserInfo> userList = utilMethods.getUserInfoList("uiuser","ADMIN");
        when(uiConfigControllerService.showUsers()).thenReturn(userList);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/showUserList")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<UserInfo> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getMyProfileInfo() throws Exception {
        UserInfo userInfo = utilMethods.getUserInfoMock();
        when(uiConfigControllerService.getMyProfileInfo()).thenReturn(userInfo);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getMyProfileInfo")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserInfo response = new ObjectMapper().readValue(res, UserInfo.class);
        assertEquals("Team1", response.getTeam());
    }

    @Test
    public void showActivityLog() throws Exception {
        List<ActivityLog> activityLogs = utilMethods.getLogs();
        when(uiConfigControllerService.showActivityLog(anyString(), anyString())).thenReturn(activityLogs);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/activityLog")
                .param("env","DEV")
                .param("pageNo","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ActivityLog> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }
}