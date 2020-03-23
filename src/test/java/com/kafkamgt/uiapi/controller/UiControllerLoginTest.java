package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.service.UtilService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class UiControllerLoginTest {

    private UiControllerLogin uiControllerLogin;

    @Mock
    private UtilService utilService;

    @Mock
    UserDetails userDetails;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        uiControllerLogin = new UiControllerLogin();
        mvc = MockMvcBuilders
                .standaloneSetup(uiControllerLogin)
                .dispatchOptions(true)
                .build();
    }

    private void mvcPerformAndAssert(String uri, String fwdedUrl) throws Exception {
        String response = mvc.perform(MockMvcRequestBuilders
                .get(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getForwardedUrl();

        assertEquals(fwdedUrl, response);
    }

    @Test
    public void login1() throws Exception {
        mvcPerformAndAssert("/login", "loginnew.html");
    }

    @Test
    public void login2() throws Exception {
        ReflectionTestUtils.setField(uiControllerLogin, "utilService", utilService);

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        doNothing().when(utilService).setUserDetails(any());
        SecurityContextHolder.setContext(securityContext);

        mvcPerformAndAssert("/login", "index");
    }

    @Test
    public void addUsers()throws Exception {
        mvcPerformAndAssert("/addUsers", "addUser.html");
    }

    @Test
    public void envs() throws Exception{
        mvcPerformAndAssert("/envs", "envs.html");
    }

    @Test
    public void execAcls()throws Exception {
        mvcPerformAndAssert("/execAcls", "execAcls.html");
    }

    @Test
    public void execSchemas() throws Exception{
        mvcPerformAndAssert("/execSchemas", "execSchemas.html");
    }

    @Test
    public void execTopics()throws Exception {
        mvcPerformAndAssert("/execTopics", "execTopics.html");
    }

    @Test
    public void myTopicRequests()throws Exception {
        mvcPerformAndAssert("/execTopics", "execTopics.html");
    }

    @Test
    public void requestAcls()throws Exception {
        mvcPerformAndAssert("/requestAcls", "requestAcls.html");
    }

    @Test
    public void requestSchemaUpload() throws Exception{
        mvcPerformAndAssert("/requestSchema", "requestSchema.html");
    }

    @Test
    public void requestTopics()throws Exception {
        mvcPerformAndAssert("/requestTopics", "requestTopics.html");
    }

    @Test
    public void showUsers()throws Exception {
        mvcPerformAndAssert("/showUsers", "showUsers.html");
    }

    @Test
    public void myProfile()throws Exception {
        mvcPerformAndAssert("/myProfile", "myProfile.html");
    }

    @Test
    public void changePwd() throws Exception{
        mvcPerformAndAssert("/changePwd", "changePwd.html");
    }

    @Test
    public void synchronizeTopics() throws Exception{
        mvcPerformAndAssert("/synchronizeTopics", "synchronizeTopics.html");
    }

    @Test
    public void synchronizeAcls()throws Exception {
        mvcPerformAndAssert("/synchronizeAcls", "synchronizeAcls.html");
    }

    @Test
    public void showTeams() throws Exception{
        mvcPerformAndAssert("/showTeams", "showTeams.html");
    }

    @Test
    public void addTeam() throws Exception{
        mvcPerformAndAssert("/addTeam", "addTeam.html");
    }

    @Test
    public void addEnv()throws Exception {
        mvcPerformAndAssert("/addEnv", "addEnv.html");
    }

    @Test
    public void activityLog()throws Exception {
        mvcPerformAndAssert("/activityLog", "activityLog.html");
    }

    @Test
    public void browseAcls()throws Exception {
        mvcPerformAndAssert("/browseAcls", "browseAcls.html");
    }

    @Test
    public void serverConfig() throws Exception{
        mvcPerformAndAssert("/serverConfig", "serverConfig.html");
    }

    @Test
    public void notFound()throws Exception {
        mvcPerformAndAssert("/notFound", "index.html");
    }
}