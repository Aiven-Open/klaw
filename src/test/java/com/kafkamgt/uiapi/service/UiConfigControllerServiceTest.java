package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.UserInfoModel;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UiConfigControllerServiceTest {

    @Mock
    private
    HandleDbRequests handleDbRequests;

    @Mock
    private
    ClusterApiService clusterApiService;

    @Mock
    private
    UtilService utilService;

    @Mock
    private
    UserInfo userInfo;

    @Mock
    private
    UserDetails userDetails;

    @Mock
    private
    ManageDatabase manageDatabase;

    @Mock
    private
    InMemoryUserDetailsManager inMemoryUserDetailsManager;

    private Env env;

    private UiConfigControllerService uiConfigControllerService;

    @Before
    public void setUp() throws Exception {
        uiConfigControllerService = new UiConfigControllerService(inMemoryUserDetailsManager);
        uiConfigControllerService.setServices(clusterApiService, utilService);

        this.env = new Env();
        env.setHost("101.10.11.11:9092");
        env.setName("DEV");
        ReflectionTestUtils.setField(uiConfigControllerService, "manageDatabase", manageDatabase);
        ReflectionTestUtils.setField(uiConfigControllerService, "orderOfEnvs", "DEV,TST,ACC,PRD");
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
        loginMock();
    }

    @After
    public void tearDown() throws Exception {
    }

    private void loginMock(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void getClusterApiStatus1() throws KafkawizeException {
        when(clusterApiService.getClusterApiUrl()).thenReturn("http://localhost:9343");
        when(clusterApiService.getClusterApiStatus()).thenReturn("ONLINE");

        Env result = uiConfigControllerService.getClusterApiStatus();
        assertEquals("ONLINE", result.getEnvStatus());
    }

    @Test
    public void getClusterApiStatus2() throws KafkawizeException {
        when(clusterApiService.getClusterApiUrl()).thenReturn("http://localhost:9343");
        when(clusterApiService.getClusterApiStatus()).thenReturn("OFFLINE");

        Env result = uiConfigControllerService.getClusterApiStatus();
        assertEquals("OFFLINE", result.getEnvStatus());
    }

    @Test
    public void getEnvs1() {
        when(handleDbRequests.selectAllKafkaEnvs()).thenReturn(getAllEnvs());
        List<Env> envsList = uiConfigControllerService.getEnvs(true);

        assertEquals(2, envsList.size());
        assertEquals(null, envsList.get(0).getEnvStatus());
    }

    @Test
    public void getEnvs2() throws KafkawizeException {
        when(handleDbRequests.selectAllKafkaEnvs()).thenReturn(getAllEnvs());
        when(clusterApiService.getKafkaClusterStatus(any(), eq("PLAINTEXT"))).thenReturn("ONLINE");
        List<Env> envsList = uiConfigControllerService.getEnvs(false);

        assertEquals(2, envsList.size());
        assertEquals("ONLINE", envsList.get(0).getEnvStatus());
        assertEquals("ONLINE", envsList.get(1).getEnvStatus());
    }

    @Test
    public void getEnvs3() throws KafkawizeException {
        when(handleDbRequests.selectAllKafkaEnvs()).thenReturn(getAllEnvs());
        when(clusterApiService.getKafkaClusterStatus(any(), eq("PLAINTEXT"))).thenReturn("OFFLINE");
        List<Env> envsList = uiConfigControllerService.getEnvs(false);

        assertEquals(2, envsList.size());
        assertEquals("OFFLINE", envsList.get(0).getEnvStatus());
        assertEquals("OFFLINE", envsList.get(1).getEnvStatus());
    }

    @Test
    public void getSchemaRegEnvs() {
        when(handleDbRequests.selectAllSchemaRegEnvs()).thenReturn(getAllSchemaEnvs());
        List<Env> envsList = uiConfigControllerService.getSchemaRegEnvs();

        assertEquals(1, envsList.size());
        assertEquals(null, envsList.get(0).getEnvStatus());
    }

    @Test
    public void getSchemaRegEnvsStatus1() {
        when(handleDbRequests.selectAllSchemaRegEnvs()).thenReturn(getAllSchemaEnvs());
        when(clusterApiService.getSchemaClusterStatus(any())).thenReturn("ONLINE");

        List<Env> envsList = uiConfigControllerService.getSchemaRegEnvsStatus();
        assertEquals(1, envsList.size());
        assertEquals("ONLINE", envsList.get(0).getEnvStatus());
    }

    @Test
    public void getSchemaRegEnvsStatus2() {
        when(handleDbRequests.selectAllSchemaRegEnvs()).thenReturn(getAllSchemaEnvs());
        when(clusterApiService.getSchemaClusterStatus(any())).thenReturn("OFFLINE");

        List<Env> envsList = uiConfigControllerService.getSchemaRegEnvsStatus();
        assertEquals(1, envsList.size());
        assertEquals("OFFLINE", envsList.get(0).getEnvStatus());
    }

    @Test
    public void getAllTeams() {
        when(handleDbRequests.selectAllTeamsOfUsers(any())).thenReturn(getAvailableTeams());
        when(userDetails.getUsername()).thenReturn("uiuser1");

        List<Team> teamsList = uiConfigControllerService.getAllTeams();

        assertEquals(1, teamsList.size());
    }

    @Test
    public void getAllTeamsSU() {
        when(handleDbRequests.selectAllTeams()).thenReturn(getAvailableTeamsSU());
        List<Team> teamsList = uiConfigControllerService.getAllTeamsSU();
        assertEquals(3, teamsList.size());
    }

    @Test
    public void addNewEnv1() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);
        String result = uiConfigControllerService.addNewEnv(this.env);
        assertEquals("{\"result\":\"Not Authorized\"}",result);
    }

    @Test
    public void addNewEnv2() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addNewEnv(any())).thenReturn("success");
        String result = uiConfigControllerService.addNewEnv(this.env);
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void addNewEnv3() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addNewEnv(any())).thenThrow(new RuntimeException("Error"));
        String result = uiConfigControllerService.addNewEnv(this.env);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void deleteCluster1() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);
        String result = uiConfigControllerService.deleteCluster("clusterId");
        assertEquals("{\"result\":\"Not Authorized\"}", result);
    }

    @Test
    public void deleteCluster2() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.deleteClusterRequest(any())).thenReturn("success");
        String result = uiConfigControllerService.deleteCluster("clusterId");
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void deleteCluster3() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.deleteClusterRequest(any())).thenThrow(new RuntimeException("Error"));
        String result = uiConfigControllerService.deleteCluster("clusterId");
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void deleteTeam1() {
        String teamId = "Team1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);

        String result = uiConfigControllerService.deleteTeam(teamId);
        assertEquals("{\"result\":\"Not Authorized\"}", result);
    }

    @Test
    public void deleteTeam2() {
        String teamId = "Team1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn(teamId);

        String result = uiConfigControllerService.deleteTeam(teamId);
        assertThat(result, CoreMatchers.containsString("Your team cannot be deleted"));
    }

    @Test
    public void deleteTeam3() {
        String teamId = "Team1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Team2");
        when(handleDbRequests.deleteTeamRequest(teamId)).thenReturn("success");

        String result = uiConfigControllerService.deleteTeam(teamId);
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void deleteTeam4() {
        String teamId = "Team1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Team2");
        when(handleDbRequests.deleteTeamRequest(teamId)).thenThrow(new RuntimeException("Error"));

        String result = uiConfigControllerService.deleteTeam(teamId);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void deleteUser1() {
        String userId = "Team1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);

        String result = uiConfigControllerService.deleteUser(userId);
        assertEquals("{\"result\":\"Not Authorized\"}", result);
    }

    @Test
    public void deleteUser2() {
        String userId = "uiuser1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn(userId);

        String result = uiConfigControllerService.deleteUser(userId);
        assertThat(result, CoreMatchers.containsString("User cannot be deleted"));
    }

    @Test
    public void deleteUser3() {
        String userId = "uiuser1";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser3");
        when(handleDbRequests.deleteUserRequest(userId)).thenReturn("success");

        String result = uiConfigControllerService.deleteUser(userId);
        assertEquals("{\"result\":\"success\"}",result);
    }

    @Test
    public void deleteUser4() {
        String userId = "superuser";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);

        String result = uiConfigControllerService.deleteUser(userId);
        assertThat(result, CoreMatchers.containsString("User cannot be deleted"));
    }

    @Test
    public void deleteUser5() {
        String userId = "uiuser2";
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(userDetails.getUsername()).thenReturn("uiuser3");
        when(handleDbRequests.deleteUserRequest(userId)).thenThrow(new RuntimeException("Error"));

        String result = uiConfigControllerService.deleteUser(userId);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void addNewUser1() {
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);
        String result = uiConfigControllerService.addNewUser(userInfo);
        assertEquals("{\"result\":\"Not Authorized\"}", result);
    }

    @Test
    public void addNewUser2() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("uiuser1");
        userInfo.setRole("USER");
        userInfo.setPwd("pwd");
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addNewUser(userInfo)).thenReturn("success");

        String result = uiConfigControllerService.addNewUser(userInfo);
        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void addNewUser3() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("uiuser1");
        userInfo.setRole("USER");
        userInfo.setPwd("pwd");
        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addNewUser(userInfo)).thenThrow(new RuntimeException("Error"));

        String result = uiConfigControllerService.addNewUser(userInfo);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void addNewTeam1() {
        Team team1 = new Team();
        team1.setTeamname("Team1");

        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(false);

        String result = uiConfigControllerService.addNewTeam(team1);
        assertEquals("{\"result\":\"Not Authorized\"}", result);
    }

    @Test
    public void addNewTeam2() {
        Team team1 = new Team();
        team1.setTeamname("Team1");

        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addNewTeam(team1)).thenReturn("success");

        String result = uiConfigControllerService.addNewTeam(team1);
        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void addNewTeam3() {
        Team team1 = new Team();
        team1.setTeamname("Team1");

        when(utilService.checkAuthorizedSU(userDetails)).thenReturn(true);
        when(handleDbRequests.addNewTeam(team1)).thenThrow(new RuntimeException("Error"));

        String result = uiConfigControllerService.addNewTeam(team1);
        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void changePwd1() {

        String pwdUpdate = "{\"pwd\":\"newpasswd\",\"repeatpwd\":\"newpasswd\"}";
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.updatePassword(eq("uiuser1"), any())).thenReturn("success");

        String result = uiConfigControllerService.changePwd(pwdUpdate);

        assertEquals("{\"result\":\"success\"}", result);
    }

    @Test
    public void changePwd2() {

        String pwdUpdate = "{\"pwd\":\"newpasswd\",\"repeatpwd\":\"newpasswd\"}";
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.updatePassword(eq("uiuser1"), any())).
                thenThrow(new RuntimeException("Error"));

        String result = uiConfigControllerService.changePwd(pwdUpdate);

        assertThat(result, CoreMatchers.containsString("failure"));
    }

    @Test
    public void showUsers() {
        when(handleDbRequests.selectAllUsersInfo()).thenReturn(getUsernfoList());
        List<UserInfoModel> userInfoList = uiConfigControllerService.showUsers(null,"1");
        assertEquals(1,userInfoList.size());
    }

    @Test
    public void getMyProfileInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("uiuser1");
        userInfo.setRole("USER");
        userInfo.setPwd("pwd");

        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getUsersInfo("uiuser1")).thenReturn(userInfo);
        UserInfo userInfoActual = uiConfigControllerService.getMyProfileInfo();
        assertEquals(userInfo.getUsername(), userInfoActual.getUsername());
    }

    @Test
    public void showActivityLog1() {
        String envSel = "DEV";
        String pageNo = "1";

        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectActivityLog("uiuser1", envSel)).thenReturn(getAcitivityList(2));

        List<ActivityLog> actList = uiConfigControllerService.showActivityLog(envSel, pageNo);
        assertEquals(2, actList.size());
        assertEquals(actList.get(0).getAllPageNos().get(0),"1");
    }

    @Test
    public void showActivityLog2() {
        String envSel = "DEV";
        String pageNo = "1";

        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.selectActivityLog("uiuser1", envSel)).thenReturn(getAcitivityList(0));

        List<ActivityLog> actList = uiConfigControllerService.showActivityLog(envSel, pageNo);
        assertEquals(0, actList.size());
    }

    private List<ActivityLog> getAcitivityList(int size) {
        List<ActivityLog> actList = new ArrayList<>();

        if(size>0) {

            ActivityLog actLog1 = new ActivityLog();
            actLog1.setEnv("DEV");
            actLog1.setActivityTime(new Timestamp(System.currentTimeMillis()));
            actList.add(actLog1);

            ActivityLog actLog2 = new ActivityLog();
            actLog2.setEnv("DEV");
            actLog2.setActivityTime(new Timestamp(System.currentTimeMillis()));
            actList.add(actLog2);
        }
        return actList;
    }

    private List<UserInfo> getUsernfoList(){
        List<UserInfo> listUsersInfo = new ArrayList<>();

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("user1");

        listUsersInfo.add(userInfo);

        return listUsersInfo;
    }

    private List<Team> getAvailableTeams(){

        Team team1 = new Team();
        team1.setTeamname("Team1");

        List<Team> teamList = new ArrayList<>();
        teamList.add(team1);

        return teamList;
    }

    private List<Team> getAvailableTeamsSU(){

        Team team1 = new Team();
        team1.setTeamname("Team1");

        Team team2 = new Team();
        team2.setTeamname("Team2");

        Team team3 = new Team();
        team3.setTeamname("Team3");

        List<Team> teamList = new ArrayList<>();
        teamList.add(team1);
        teamList.add(team2);
        teamList.add(team3);

        return teamList;
    }

    private List<Env> getAllSchemaEnvs(){
        List<Env> listEnvs = new ArrayList<>();

        Env env = new Env();
        env.setHost("localhost:8081");
        env.setName("DEV");
        env.setProtocol("PLAINTEXT");
        listEnvs.add(env);

        return listEnvs;
    }

    private List<Env> getAllEnvs(){
        List<Env> listEnvs = new ArrayList<>();

        Env env = new Env();
        env.setHost("localhost:9092");
        env.setName("DEV");
        env.setProtocol("PLAINTEXT");
        listEnvs.add(env);

        env = new Env();
        env.setHost("10.22.34.121:9092");
        env.setName("TST");
        env.setProtocol("PLAINTEXT");
        listEnvs.add(env);

        return listEnvs;
    }
}