package io.aiven.klaw.clusterapi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.SchemaService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClientException;

@ExtendWith(SpringExtension.class)
public class SchemaRegistryControllerTest {

  @MockBean private SchemaService schemaService;

  private MockMvc mvc;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    SchemaRegistryController schemaRegistryController = new SchemaRegistryController(schemaService);
    mvc = MockMvcBuilders.standaloneSetup(schemaRegistryController).dispatchOptions(true).build();
  }

  @Test
  public void postSchema() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getSchema());
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(schemaService.registerSchema(any(ClusterSchemaRequest.class))).thenReturn(apiResponse);

    mvc.perform(
            post("/topics/postSchema")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void postSchemaFail() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getSchema());

    when(schemaService.registerSchema(any(ClusterSchemaRequest.class)))
        .thenThrow(new RuntimeException("Error registering schema"));

    mvc.perform(
            post("/topics/postSchema")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().is5xxServerError());
  }

  @Test
  public void validateSchemaCompaitbility_ReturnSuccess() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getSchema());

    when(schemaService.checkSchemaCompatibility(
            anyString(), anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString()))
        .thenReturn(
            ApiResponse.builder()
                .message(ApiResultStatus.SUCCESS.value + " Schema is compatible.")
                .build());

    mvc.perform(
            post("/topics/schema/validate/compatibility")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void validateSchemaCompaitbility_ReturnFailure() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getSchema());

    when(schemaService.checkSchemaCompatibility(
            anyString(), anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString()))
        .thenReturn(
            ApiResponse.builder()
                .message(ApiResultStatus.FAILURE.value + "  Schema is not compatible.")
                .build());

    mvc.perform(
            post("/topics/schema/validate/compatibility")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().string(containsString(ApiResultStatus.FAILURE.value)));
  }

  @Test
  public void validateSchemaCompaitbility_throwsError_ReturnInternalServerError() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getSchema());

    when(schemaService.checkSchemaCompatibility(
            anyString(), anyString(), any(KafkaSupportedProtocol.class), anyString(), anyString()))
        .thenThrow(RestClientException.class);

    mvc.perform(
            post("/topics/schema/validate/compatibility")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().is5xxServerError());
  }
}
