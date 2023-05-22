package io.aiven.klaw.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.response.SyncSchemasList;
import io.aiven.klaw.service.SchemaRegistrySyncControllerService;
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
            anyString(), anyString(), anyString()))
        .thenReturn(schemasInfoOfClusterResponse);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/schemas")
                    .param("envId", "1")
                    .param("pageNo", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    SyncSchemasList resp = new ObjectMapper().readValue(res, new TypeReference<>() {});
    assertThat(resp.getSchemaSubjectInfoResponseList().size()).isEqualTo(2);
  }
}
