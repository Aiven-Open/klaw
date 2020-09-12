package com.kafkamgt.uiapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.model.AclInfo;
import com.kafkamgt.uiapi.model.SyncAclUpdates;
import com.kafkamgt.uiapi.model.TopicOverview;
import com.kafkamgt.uiapi.service.AclControllerService;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class AclControllerTest {

    @MockBean
    private AclControllerService aclControllerService;

    private UtilMethods utilMethods;

    private MockMvc mvc;

    private AclController aclController;

    @Before
    public void setup() {
        aclController = new AclController();
        mvc = MockMvcBuilders
                .standaloneSetup(aclController)
                .dispatchOptions(true)
                .build();
        utilMethods = new UtilMethods();
        ReflectionTestUtils.setField(aclController, "aclControllerService", aclControllerService);
    }

    @Test
    public void createAcl() throws Exception {
        AclRequests addAclRequest = utilMethods.getAclRequest("testtopic");
        String jsonReq = new ObjectMapper().writer().writeValueAsString(addAclRequest);
        when(aclControllerService.createAcl(any())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/createAcl")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void updateSyncAcls() throws Exception {
        List<SyncAclUpdates> syncUpdates = utilMethods.getSyncAclsUpdates();

        String jsonReq = new ObjectMapper().writer().writeValueAsString(syncUpdates);
        HashMap<String, String> result = new HashMap<>();
        result.put("result","success");
        when(aclControllerService.updateSyncAcls(any())).thenReturn(result);

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/updateSyncAcls")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        HashMap<String, String> actualResult = new ObjectMapper().readValue(response, new TypeReference<HashMap<String,String>>(){});

        assertEquals("success", actualResult.get("result"));
    }

    @Test
    public void getAclRequests() throws Exception {

        List<AclRequests> aclRequests = utilMethods.getAclRequests();

        when(aclControllerService.getAclRequests("1")).thenReturn(aclRequests);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getAclRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNo","1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclRequests> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getCreatedAclRequests() throws Exception {

        List<List<AclRequests>> aclRequests = utilMethods.getAclRequestsList();

        when(aclControllerService.getCreatedAclRequests()).thenReturn(aclRequests);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getCreatedAclRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<List<AclRequests>> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void deleteAclRequests() throws Exception {
        when(aclControllerService.deleteAclRequests(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .get("/deleteAclRequests")
                .param("req_no","fsda32FSDw")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void approveAclRequests() throws Exception {
        when(aclControllerService.approveAclRequests(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequest")
                .param("req_no","reqno")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void declineAclRequests() throws Exception {
        when(aclControllerService.declineAclRequests(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execAclRequestDecline")
                .param("req_no","reqno")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("success", response);
    }

    @Test
    public void getAcls1() throws Exception {
        TopicOverview topicOverview = utilMethods.getTopicOverview();

        when(aclControllerService.getAcls("testtopic"))
                .thenReturn(topicOverview);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getAcls")
                .param("topicnamesearch","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TopicOverview response = new ObjectMapper().readValue(res, TopicOverview.class);
        assertEquals(1, response.getAclInfoList().size());
    }

    @Test
    public void getAcls2() throws Exception {
        TopicOverview topicOverview = utilMethods.getTopicOverview();

        when(aclControllerService.getAcls( null))
                .thenReturn(topicOverview);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getAcls")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void getSyncAcls() throws Exception {
        List<AclInfo> aclInfo = utilMethods.getAclInfoList();

        when(aclControllerService.getSyncAcls(anyString(), anyString(), any()))
                .thenReturn(aclInfo);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getSyncAcls")
                .param("env","DEV")
                .param("pageNo","1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<AclInfo> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }
}