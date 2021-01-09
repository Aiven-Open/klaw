package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilControllerServiceTest {

    @Mock
    private
    HandleDbRequests handleDbRequests;

    @Mock
    private
    UtilService utilService;

    @Mock
    private
    UserDetails userDetails;

    @Mock
    private
    UserInfo userInfo;

    @Mock
    private
    HttpServletRequest httpServletRequest;

    @Mock
    private
    HttpServletResponse httpServletResponse;

    @Mock
    private
    ManageDatabase manageDatabase;

    @Mock
    private
    Authentication authentication;

    private UtilControllerService utilControllerService;

    @Before
    public void setUp() {
        this.utilControllerService = new UtilControllerService(utilService);
        ReflectionTestUtils.setField(utilControllerService, "manageDatabase", manageDatabase);
        when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    }

    private void loginMock(){
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void getAuth1() {
        loginMock();
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Octopus");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_USER");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getAllRequestsToBeApproved(any(),anyString())).thenReturn(getCounts(true));

        HashMap<String, String> actualResult = utilControllerService.getAuth();

        HashMap<String, String> expectedResult = new HashMap<>();

        expectedResult.put("status","Authorized");
        expectedResult.put("username","uiuser1");
        expectedResult.put("teamname","Octopus");
        expectedResult.put("companyinfo","null");
        expectedResult.put("kafkawizeversion","null");
        expectedResult.put("notifications","2");
        expectedResult.put("notificationsAcls","2");
        expectedResult.put("notificationsSchemas","1");
        expectedResult.put("statusauthexectopics_su","NotAuthorized");
        expectedResult.put("statusauthexectopics","NotAuthorized");

        assertEquals(expectedResult.get("status"),actualResult.get("status"));
    }

    @Test
    public void getAuth2() {
        loginMock();
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Octopus");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_ADMIN");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getAllRequestsToBeApproved(any(), anyString())).thenReturn(getCounts(false));

        HashMap<String, String> actualResult = utilControllerService.getAuth();
        HashMap<String, String> expectedResult = new HashMap<>();

        expectedResult.put("status","Authorized");
        expectedResult.put("username","uiuser1");
        expectedResult.put("teamname","Octopus");
        expectedResult.put("companyinfo","null");
        expectedResult.put("kafkawizeversion","null");
        expectedResult.put("notifications","0");
        expectedResult.put("notificationsAcls","0");
        expectedResult.put("notificationsSchemas","2");
        expectedResult.put("statusauthexectopics_su","NotAuthorized");
        expectedResult.put("statusauthexectopics","Authorized");
        assertEquals(expectedResult.get("status"),actualResult.get("status"));
    }

    @Test
    public void getAuth3() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        HashMap<String, String> actualResult = utilControllerService.getAuth();
        assertEquals(null,actualResult);
    }

    @Test
    public void getExecAuth1() {

        loginMock();
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Octopus");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_ADMIN");

        String actualResult = utilControllerService.getExecAuth();
        String expectedResult = "{ \"status\": \"Authorized\" ," +
                "  \"companyinfo\": \"null\"," +
                " \"teamname\": \"Octopus\"," +
                "\"username\":\"uiuser1\" }";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void getExecAuth2() {
        loginMock();
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Octopus");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_USER");

        String actualResult = utilControllerService.getExecAuth();
        String expectedResult = "{ \"status\": \"NotAuthorized\" ," +
                "  \"companyinfo\": \"null\"," +
                " \"teamname\": \"Octopus\"," +
                "\"username\":\"uiuser1\" }";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void getLogoutPage() {
        when(utilService.getAuthentication()).thenReturn(authentication);
        utilControllerService.getLogoutPage(httpServletRequest, httpServletResponse);
    }

    private HashMap<String, String> getCounts(boolean greater){
        HashMap<String, String> countList = new HashMap<>();

        if(greater) {
            countList.put("topics", "2");
            countList.put("acls", "1");
            countList.put("schemas", "1");
            countList.put("users", "0");
        }else{
            countList.put("topics", "0");
            countList.put("acls", "0");
            countList.put("schemas", "2");
            countList.put("users", "0");
        }
        return countList;
    }
}