package com.kafkamgt.uiapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.service.SchemaRegstryControllerService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
public class SchemaRegstryControllerTest {

    @MockBean
    private SchemaRegstryControllerService schemaRegstryControllerService;

    private SchemaRegstryController schemaRegstryController;

    private UtilMethods utilMethods;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        schemaRegstryController = new SchemaRegstryController();
        mvc = MockMvcBuilders
                .standaloneSetup(schemaRegstryController)
                .dispatchOptions(true)
                .build();
        utilMethods = new UtilMethods();
        ReflectionTestUtils.setField(schemaRegstryController, "schemaRegstryControllerService", schemaRegstryControllerService);

    }

    @Test
    public void getSchemaRequests() throws Exception {
        List<SchemaRequest> schRequests = utilMethods.getSchemaRequests();

        when(schemaRegstryControllerService.getSchemaRequests()).thenReturn(schRequests);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getSchemaRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<SchemaRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void getCreatedSchemaRequests() throws Exception {
        List<SchemaRequest> schRequests = utilMethods.getSchemaRequests();

        when(schemaRegstryControllerService.getCreatedSchemaRequests()).thenReturn(schRequests);

        String res = mvc.perform(MockMvcRequestBuilders
                .get("/getCreatedSchemaRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<SchemaRequest> response = new ObjectMapper().readValue(res, List.class);
        assertEquals(1, response.size());
    }

    @Test
    public void deleteSchemaRequests() throws Exception {
        when(schemaRegstryControllerService.deleteSchemaRequests(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/deleteSchemaRequests")
                .param("topicName","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    @Test
    public void execSchemaRequests() throws Exception {
        when(schemaRegstryControllerService.execSchemaRequests(anyString())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/execSchemaRequests")
                .param("topicName","testtopic")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }

    @Test
    public void uploadSchema() throws Exception {
        SchemaRequest schemaRequest = utilMethods.getSchemaRequests().get(0);
        String jsonReq = new ObjectMapper().writer().writeValueAsString(schemaRequest);

        when(schemaRegstryControllerService.uploadSchema(any())).thenReturn("success");

        String response = mvc.perform(MockMvcRequestBuilders
                .post("/uploadSchema")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response, CoreMatchers.containsString("success"));
    }
}