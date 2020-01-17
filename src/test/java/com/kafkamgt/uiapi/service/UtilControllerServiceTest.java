package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UtilControllerServiceTest {

    @Mock
    HandleDbRequests handleDbRequests;

    @Mock
    UtilService utilService;

    @Mock
    UserDetails userDetails;

    @Mock
    UserInfo userInfo;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    HttpServletResponse httpServletResponse;

    @Mock
    Authentication authentication;

    UtilControllerService utilControllerService;

    @Before
    public void setUp() {
        this.utilControllerService = new UtilControllerService(utilService);
        ReflectionTestUtils.setField(utilControllerService, "handleDbRequests", handleDbRequests);
    }

    @Test
    public void getAuth1() {

        when(utilService.getUserDetails()).thenReturn(userDetails);
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Team1");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_USER");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getAllRequestsToBeApproved((any()))).thenReturn(getCounts(true));

        String actualResult = utilControllerService.getAuth();
        String expectedResult = "{ \"status\": \"Authorized\" ," +
                " \"username\":\"uiuser1\"," +
                " \"teamname\": \"Team1\"," +
                " \"companyinfo\": \"null\"," +
                " \"kafkawizeversion\": \"null\"," +
                " \"notifications\": \"2\"," +
                " \"notificationsAcls\": \"1\"," +
                " \"statusauthexectopics\": \"NotAuthorized\" " +
                "}";
        assertEquals(expectedResult,actualResult);
    }

    @Test
    public void getAuth2() {

        when(utilService.getUserDetails()).thenReturn(userDetails);
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Team1");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_ADMIN");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(handleDbRequests.getAllRequestsToBeApproved((any()))).thenReturn(getCounts(false));

        String actualResult = utilControllerService.getAuth();
        String expectedResult = "{ \"status\": \"Authorized\" ," +
                " \"username\":\"uiuser1\"," +
                " \"teamname\": \"Team1\"," +
                " \"companyinfo\": \"null\"," +
                " \"kafkawizeversion\": \"null\"," +
                " \"notifications\": \"\"," +
                " \"notificationsAcls\": \"\"," +
                " \"statusauthexectopics\": \"Authorized\" " +
                "}";
        assertEquals(expectedResult,actualResult);
    }

    @Test
    public void getAuth3() {

        when(utilService.getUserDetails()).thenReturn(null);

        String actualResult = utilControllerService.getAuth();
        String expectedResult = null;
        assertEquals(expectedResult,actualResult);
    }

    @Test
    public void getExecAuth1() {

        when(utilService.getUserDetails()).thenReturn(userDetails);
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Team1");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_ADMIN");

        String actualResult = utilControllerService.getExecAuth();
        String expectedResult = "{ \"status\": \"Authorized\" ," +
                "  \"companyinfo\": \"null\"," +
                " \"teamname\": \"Team1\"," +
                "\"username\":\"uiuser1\" }";
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void getExecAuth2() {

        when(utilService.getUserDetails()).thenReturn(userDetails);
        when(handleDbRequests.getUsersInfo(any())).thenReturn(userInfo);
        when(userInfo.getTeam()).thenReturn("Team1");
        when(userDetails.getUsername()).thenReturn("uiuser1");
        when(utilService.getAuthority(userDetails)).thenReturn("ROLE_USER");

        String actualResult = utilControllerService.getExecAuth();
        String expectedResult = "{ \"status\": \"NotAuthorized\" ," +
                "  \"companyinfo\": \"null\"," +
                " \"teamname\": \"Team1\"," +
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
            countList.put("schemas", "");
        }else{
            countList.put("topics", "0");
            countList.put("acls", "0");
            countList.put("schemas", "");
        }
        return countList;
    }
}