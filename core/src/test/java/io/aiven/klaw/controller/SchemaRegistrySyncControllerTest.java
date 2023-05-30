package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncSchemaUpdates;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.response.SchemaDetailsResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import io.aiven.klaw.service.SchemaRegistrySyncControllerService;
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
public class SchemaRegistrySyncControllerTest {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @MockBean private SchemaRegistrySyncControllerService schemaRegistrySyncControllerService;

  private SchemaRegistrySyncController schemaRegistrySyncController;

  private UtilMethods utilMethods;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    schemaRegistrySyncController = new SchemaRegistrySyncController();
    mvc =
        MockMvcBuilders.standaloneSetup(schemaRegistrySyncController).dispatchOptions(true).build();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(
        schemaRegistrySyncController,
        "schemaRegistrySyncControllerService",
        schemaRegistrySyncControllerService);
  }

  @Test
  @Order(1)
  public void getSchemasInfoOfAnEnvironment() throws Exception {
    SyncSchemasList schemasInfoOfClusterResponse = utilMethods.getSchemasSyncInfoOfEnv();
    when(schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            anyString(), anyString(), anyString(), any(), anyBoolean(), anyString(), anyInt()))
        .thenReturn(schemasInfoOfClusterResponse);

    mvc.perform(
            MockMvcRequestBuilders.get("/schemas")
                .param("envId", "1")
                .param("pageNo", "1")
                .param("showAllTopics", "false")
                .param("source", "cluster")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schemaSubjectInfoResponseList", hasSize(2)));
  }

  @Test
  @Order(2)
  public void updateSyncSchemas() throws Exception {
    ApiResponse apiResponse =
        ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
    SyncSchemaUpdates syncSchemaUpdates = new SyncSchemaUpdates();
    syncSchemaUpdates.setSourceKafkaEnvSelected("1");
    syncSchemaUpdates.setTopicList(List.of("Topic01"));
    syncSchemaUpdates.setTypeOfSync("SYNC_SCHEMAS");
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(syncSchemaUpdates);

    when(schemaRegistrySyncControllerService.updateSyncSchemas(eq(syncSchemaUpdates)))
        .thenReturn(apiResponse);

    mvc.perform(
            MockMvcRequestBuilders.post("/schemas")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success", is(true)));
  }

  @Test
  @Order(3)
  public void getSchemaOfTopic() throws Exception {
    String schemaVersion = "1";
    String topicName = "testtopic";
    String topicEnv = "DEV";
    String schemaContent = "{name : schema}";

    SchemaDetailsResponse schemaDetailsResponse = new SchemaDetailsResponse();
    schemaDetailsResponse.setSchemaVersion(schemaVersion);
    schemaDetailsResponse.setSchemaContent(schemaContent);
    schemaDetailsResponse.setEnvName(topicEnv);
    schemaDetailsResponse.setTopicName(topicName);

    when(schemaRegistrySyncControllerService.getSchemaOfTopicFromSource(
            anyString(), anyString(), anyInt(), anyString()))
        .thenReturn(schemaDetailsResponse);

    mvc.perform(
            MockMvcRequestBuilders.get(
                    "/schemas/source/cluster/kafkaEnv/1/topic/testtopic/schemaVersion/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.topicName", is(topicName)))
        .andExpect(jsonPath("$.schemaContent", is(schemaContent)));
  }

  @Test
  @Order(3)
  public void getSchemaOfTopicNoContent() throws Exception {
    SchemaDetailsResponse schemaDetailsResponse = new SchemaDetailsResponse();

    when(schemaRegistrySyncControllerService.getSchemaOfTopicFromSource(
            anyString(), anyString(), anyInt(), anyString()))
        .thenReturn(schemaDetailsResponse);

    mvc.perform(
            MockMvcRequestBuilders.get(
                    "/schemas/source/cluster/kafkaEnv/1/topic/testtopic/schemaVersion/2")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.schemaContent", is(nullValue())));
  }
}
