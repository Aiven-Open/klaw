package com.kafkamgt.uiapi.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.model.SchemaRequestModel;
import com.kafkamgt.uiapi.service.SchemaRegstryControllerService;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
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
public class SchemaRegstryControllerTest {

  @MockBean private SchemaRegstryControllerService schemaRegstryControllerService;

  private SchemaRegstryController schemaRegstryController;

  private UtilMethods utilMethods;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    schemaRegstryController = new SchemaRegstryController();
    mvc = MockMvcBuilders.standaloneSetup(schemaRegstryController).dispatchOptions(true).build();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(
        schemaRegstryController, "schemaRegstryControllerService", schemaRegstryControllerService);
  }

  @Test
  @Order(1)
  public void getSchemaRequests() throws Exception {
    List<SchemaRequestModel> schRequests = utilMethods.getSchemaRequests();

    when(schemaRegstryControllerService.getSchemaRequests(anyString(), anyString(), anyString()))
        .thenReturn(schRequests);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getSchemaRequests")
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    List<SchemaRequestModel> response = new ObjectMapper().readValue(res, List.class);
    assertEquals(1, response.size());
  }

  @Test
  @Order(2)
  public void deleteSchemaRequests() throws Exception {
    when(schemaRegstryControllerService.deleteSchemaRequests(anyString())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/deleteSchemaRequests")
                    .param("req_no", "1001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response, CoreMatchers.containsString("success"));
  }

  @Test
  @Order(3)
  public void execSchemaRequests() throws Exception {
    when(schemaRegstryControllerService.execSchemaRequests(anyString())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/execSchemaRequests")
                    .param("avroSchemaReqId", "1001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response, CoreMatchers.containsString("success"));
  }

  @Test
  @Order(4)
  public void uploadSchema() throws Exception {
    SchemaRequestModel schemaRequest = utilMethods.getSchemaRequests().get(0);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(schemaRequest);

    when(schemaRegstryControllerService.uploadSchema(any())).thenReturn("success");

    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/uploadSchema")
                    .content(jsonReq)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    assertThat(response, CoreMatchers.containsString("success"));
  }
}
