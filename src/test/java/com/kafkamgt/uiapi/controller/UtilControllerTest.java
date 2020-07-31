package com.kafkamgt.uiapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.service.UtilControllerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class UtilControllerTest {

    @MockBean
    private UtilControllerService utilControllerService;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        UtilController utilController = new UtilController();
        mvc = MockMvcBuilders
                .standaloneSetup(utilController)
                .dispatchOptions(true)
                .build();
        ReflectionTestUtils.setField(utilController, "utilControllerService", utilControllerService);
    }

    @Test
    public void getAuth() throws Exception {
        HashMap<String, String> hMap = new HashMap<>();
        hMap.put("status","Authorized");
        when(utilControllerService.getAuth()).thenReturn(hMap);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getAuth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        HashMap<String, String> response = new ObjectMapper().readValue(res, HashMap.class);
        assertEquals("Authorized", response.get("status"));
    }

    @Test
    public void getExecAuth() throws Exception {
        String result =  "{ \"status\": \"" + "Authorized" + "\" }";

        when(utilControllerService.getExecAuth()).thenReturn(result);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getExecAuth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        HashMap<String, String> response = new ObjectMapper().readValue(res, HashMap.class);
        assertEquals("Authorized", response.get("status"));
    }

    @Test
    public void getLogoutPage() throws Exception {

        mvc.perform(MockMvcRequestBuilders
                .get("/logout"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}