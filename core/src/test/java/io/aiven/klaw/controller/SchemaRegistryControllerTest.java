package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.requests.SchemaPromotion;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.response.SchemaRequestsResponseModel;
import io.aiven.klaw.service.SchemaRegistryControllerService;
import java.util.List;
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
public class SchemaRegistryControllerTest {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  @MockBean private SchemaRegistryControllerService schemaRegistryControllerService;

  private SchemaRegistryController schemaRegistryController;

  private UtilMethods utilMethods;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    schemaRegistryController = new SchemaRegistryController();
    mvc = MockMvcBuilders.standaloneSetup(schemaRegistryController).dispatchOptions(true).build();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(
        schemaRegistryController,
        "schemaRegistryControllerService",
        schemaRegistryControllerService);
  }

  @Test
  @Order(1)
  public void getSchemaRequests() throws Exception {
    List<SchemaRequestsResponseModel> schRequests = utilMethods.getSchemaRequestsResponse();

    when(schemaRegistryControllerService.getSchemaRequests(
            anyString(),
            anyString(),
            anyString(),
            eq(null),
            anyBoolean(),
            eq(null),
            eq(null),
            eq(null),
            eq(io.aiven.klaw.model.enums.Order.OLDEST_FIRST),
            anyBoolean()))
        .thenReturn(schRequests);

    mvc.perform(
            MockMvcRequestBuilders.get("/getSchemaRequests")
                .param("pageNo", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @Order(2)
  public void deleteSchemaRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(schemaRegistryControllerService.deleteSchemaRequests(anyString())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/deleteSchemaRequests")
                .param("req_no", "1001")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(3)
  public void execSchemaRequests() throws Exception {
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(schemaRegistryControllerService.execSchemaRequests(anyString())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/execSchemaRequests")
                .param("avroSchemaReqId", "1001")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(4)
  public void uploadSchema() throws Exception {
    SchemaRequestModel schemaRequest = utilMethods.getSchemaRequests().get(0);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(schemaRequest);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(schemaRegistryControllerService.uploadSchema(any())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/uploadSchema")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  @Order(5)
  public void promoteSchema() throws Exception {
    SchemaPromotion schemaPromotion = utilMethods.getSchemaPromotion().get(0);
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(schemaPromotion);
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    when(schemaRegistryControllerService.promoteSchema(any())).thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/promote/schema")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.result", is(ApiResultStatus.SUCCESS.value)));
  }
}
