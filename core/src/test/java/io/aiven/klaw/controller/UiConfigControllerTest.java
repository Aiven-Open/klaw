package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.KafkaClustersType;
import io.aiven.klaw.model.TeamModel;
import io.aiven.klaw.model.UserInfoModel;
import io.aiven.klaw.service.EnvsClustersTenantsControllerService;
import io.aiven.klaw.service.UiConfigControllerService;
import io.aiven.klaw.service.UsersTeamsControllerService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UiConfigControllerTest {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  @MockBean private UiConfigControllerService uiConfigControllerService;

  @MockBean private EnvsClustersTenantsControllerService envsClustersTenantsControllerService;

  @MockBean private UsersTeamsControllerService usersTeamsControllerService;

  private UiConfigController uiConfigController;

  private UsersTeamsController usersTeamsController;

  private EnvsClustersTenantsController envsClustersTenantsController;

  private UtilMethods utilMethods;

  private MockMvc mvc, mvcUserTeams, mvcEnvs;

  @BeforeEach
  public void setUp() {
    utilMethods = new UtilMethods();

    uiConfigController = new UiConfigController();
    mvc = MockMvcBuilders.standaloneSetup(uiConfigController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(
        uiConfigController, "uiConfigControllerService", uiConfigControllerService);

    usersTeamsController = new UsersTeamsController();
    mvcUserTeams =
        MockMvcBuilders.standaloneSetup(usersTeamsController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(
        usersTeamsController, "usersTeamsControllerService", usersTeamsControllerService);

    envsClustersTenantsController = new EnvsClustersTenantsController();
    mvcEnvs =
        MockMvcBuilders.standaloneSetup(envsClustersTenantsController)
            .dispatchOptions(true)
            .build();
    ReflectionTestUtils.setField(
        envsClustersTenantsController,
        "envsClustersTenantsControllerService",
        envsClustersTenantsControllerService);
  }

  @Test
  @Order(1)
  public void getEnvs() throws Exception {
    List<EnvModel> envList = utilMethods.getEnvList();
    when(envsClustersTenantsControllerService.getKafkaEnvs()).thenReturn(envList);

    mvcEnvs
        .perform(
            MockMvcRequestBuilders.get("/getEnvs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(2)
  public void getSyncEnv() throws Exception {
    List<Map<String, String>> envList = utilMethods.getSyncEnv();
    when(envsClustersTenantsControllerService.getSyncEnvs()).thenReturn(envList);

    mvcEnvs
        .perform(
            MockMvcRequestBuilders.get("/getSyncEnv")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].*", hasSize(2)));
  }

  @Test
  @Order(4)
  public void getSchemaRegEnvs() throws Exception {
    List<EnvModel> envList = utilMethods.getEnvList();
    when(envsClustersTenantsControllerService.getSchemaRegEnvs()).thenReturn(envList);

    mvcEnvs
        .perform(
            MockMvcRequestBuilders.get("/getSchemaRegEnvs")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(5)
  public void getAllTeamsSU() throws Exception {
    List<TeamModel> teamList = utilMethods.getTeamsModel();
    when(usersTeamsControllerService.getAllTeamsSU()).thenReturn(teamList);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.get("/getAllTeamsSU")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(6)
  public void getAllTeamsSUOnly() throws Exception {
    List<String> teamList = utilMethods.getAllTeamsSUOnly();
    when(usersTeamsControllerService.getAllTeamsSUOnly()).thenReturn(teamList);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.get("/getAllTeamsSUOnly")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @Order(7)
  public void addNewEnv() throws Exception {
    EnvModel env = utilMethods.getEnvList().get(0);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(env);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(envsClustersTenantsControllerService.addNewEnv(any())).thenReturn(apiResponse);

    mvcEnvs
        .perform(
            MockMvcRequestBuilders.post("/addNewEnv")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(8)
  public void deleteEnv() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(envsClustersTenantsControllerService.deleteEnvironment(anyString(), anyString()))
        .thenReturn(apiResponse);

    mvcEnvs
        .perform(
            MockMvcRequestBuilders.post("/deleteEnvironmentRequest")
                .param("envId", "101")
                .param("envType", KafkaClustersType.KAFKA.value)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(9)
  public void deleteTeam() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(usersTeamsControllerService.deleteTeam(any())).thenReturn(apiResponse);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.post("/deleteTeamRequest")
                .param("teamId", "101")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(10)
  public void deleteUser() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(usersTeamsControllerService.deleteUser(anyString(), anyBoolean())).thenReturn(apiResponse);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.post("/deleteUserRequest")
                .param("userId", "uiuser1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(11)
  public void addNewUser() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    UserInfoModel userInfo = utilMethods.getUserInfoMock();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(userInfo);
    when(usersTeamsControllerService.addNewUser(any(), anyBoolean())).thenReturn(apiResponse);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.post("/addNewUser")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(12)
  public void addNewTeam() throws Exception {
    Team team = utilMethods.getTeams().get(0);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(team);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(usersTeamsControllerService.addNewTeam(any(), anyBoolean())).thenReturn(apiResponse);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.post("/addNewTeam")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(13)
  public void changePwd() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(usersTeamsControllerService.changePwd(any())).thenReturn(apiResponse);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.post("/chPwd")
                .param("changePwd", "newpasswd")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(14)
  public void showUsers() throws Exception {
    List<UserInfoModel> userList = utilMethods.getUserInfoListModel("uiuser", "ADMIN");
    when(usersTeamsControllerService.showUsers(any(), any(), any())).thenReturn(userList);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.get("/showUserList")
                .param("teamName", "")
                .param("pageNo", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(15)
  public void getMyProfileInfo() throws Exception {
    UserInfoModel userInfo = utilMethods.getUserInfoMock();
    when(usersTeamsControllerService.getMyProfileInfo()).thenReturn(userInfo);

    mvcUserTeams
        .perform(
            MockMvcRequestBuilders.get("/getMyProfileInfo")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.team", is("Seahorses")));
  }

  @Test
  @Order(16)
  public void showActivityLog() throws Exception {
    List<ActivityLog> activityLogs = utilMethods.getLogs();
    when(uiConfigControllerService.showActivityLog(anyString(), anyString(), anyString()))
        .thenReturn(activityLogs);

    mvc.perform(
            MockMvcRequestBuilders.get("/getActivityLogPerEnv")
                .param("env", "1")
                .param("pageNo", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }
}
