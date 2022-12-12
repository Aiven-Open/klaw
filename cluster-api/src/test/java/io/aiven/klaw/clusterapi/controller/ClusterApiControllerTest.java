package io.aiven.klaw.clusterapi.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.config.JwtRequestFilter;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.AivenApiService;
import io.aiven.klaw.clusterapi.services.ApacheKafkaAclService;
import io.aiven.klaw.clusterapi.services.ApacheKafkaTopicService;
import io.aiven.klaw.clusterapi.services.MonitoringService;
import io.aiven.klaw.clusterapi.services.SchemaService;
import io.aiven.klaw.clusterapi.services.UtilComponentsService;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(controllers = ClusterApiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClusterApiControllerTest {

  @MockBean private UtilComponentsService utilComponentsService;
  @MockBean private ApacheKafkaAclService apacheKafkaAclService;
  @MockBean private ApacheKafkaTopicService apacheKafkaTopicService;
  @MockBean private SchemaService schemaService;
  @MockBean private MonitoringService monitoringService;
  @MockBean private AivenApiService aivenApiService;
  @MockBean private JwtRequestFilter jwtRequestFilter;

  @Autowired private MockMvc mvc;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
  }

  @Test
  public void getApiStatus() throws Exception {
    mvc.perform(get("/topics/getApiStatus"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("\"ONLINE\""));
  }

  @Test
  public void getStatus() throws Exception {
    String clusterName = "testCluster";
    String clusterType = "sampleType";
    String bootstrapServers = "localhost:9092";

    when(utilComponentsService.getStatus(
            bootstrapServers, KafkaSupportedProtocol.PLAINTEXT, clusterName, clusterType))
        .thenReturn(ClusterStatus.ONLINE);

    String urlTemplate =
        String.join(
            "/",
            "/topics",
            "getStatus",
            bootstrapServers,
            KafkaSupportedProtocol.PLAINTEXT.getValue(),
            clusterName,
            clusterType);
    mvc.perform(
            MockMvcRequestBuilders.get(urlTemplate)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("\"ONLINE\""));
  }

  @Test
  public void getTopics() throws Exception {
    String clusterName = "testCluster";
    String bootstrapServers = "localhost:9092";

    when(apacheKafkaTopicService.loadTopics(
            bootstrapServers, KafkaSupportedProtocol.PLAINTEXT, clusterName))
        .thenReturn(utilMethods.getTopics());

    String urlTemplate =
        String.join(
            "/",
            "/topics",
            "getTopics",
            bootstrapServers,
            KafkaSupportedProtocol.PLAINTEXT.getValue(),
            clusterName);
    mvc.perform(get(urlTemplate))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void getAcls() throws Exception {
    String clusterName = "testCluster";
    String bootstrapServers = "localhost:9092";
    String aclsNativeType = AclsNativeType.NATIVE.name();
    String projectName = "projectName";
    String serviceName = "serviceName";

    when(apacheKafkaAclService.loadAcls(
            bootstrapServers, KafkaSupportedProtocol.PLAINTEXT, clusterName))
        .thenReturn(utilMethods.getAcls());

    String urlTemplate =
        String.join(
            "/",
            "/topics",
            "getAcls",
            bootstrapServers,
            aclsNativeType,
            KafkaSupportedProtocol.PLAINTEXT.getValue(),
            clusterName,
            projectName,
            serviceName);
    mvc.perform(get(urlTemplate))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void createTopics() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getTopicRequest());
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();

    when(apacheKafkaTopicService.createTopic(any(ClusterTopicRequest.class)))
        .thenReturn(apiResponse);

    mvc.perform(
            post("/topics/createTopics")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void createAclsProducer() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.PRODUCER.value);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterAclRequest);

    when(apacheKafkaAclService.updateProducerAcl(any(ClusterAclRequest.class)))
        .thenReturn(ApiResultStatus.SUCCESS.value);

    mvc.perform(
            post("/topics/createAcls")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void createAclsConsumer() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterAclRequest);

    when(apacheKafkaAclService.updateConsumerAcl(any(ClusterAclRequest.class)))
        .thenReturn("success1");

    mvc.perform(
            post("/topics/createAcls")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString(ApiResultStatus.SUCCESS.value)));
  }

  @Test
  public void createAclsConsumerFail() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    String jsonReq = new ObjectMapper().writer().writeValueAsString(clusterAclRequest);

    when(apacheKafkaAclService.updateConsumerAcl(any(ClusterAclRequest.class)))
        .thenThrow(new RuntimeException("Error creating acls"));

    mvc.perform(post("/topics/createAcls").content(jsonReq).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is5xxServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.result", containsString("Error creating acls")));
  }

  @Test
  public void postSchema() throws Exception {
    String jsonReq = new ObjectMapper().writer().writeValueAsString(utilMethods.getSchema());
    ApiResponse apiResponse = ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
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
}
