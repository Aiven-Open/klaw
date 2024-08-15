package io.aiven.klaw.clusterapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = KafkaClusterApiApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@EmbeddedKafka(
    brokerProperties = {"listeners=PLAINTEXT://" + TopicContentsControllerIT.BOOTSTRAP_SERVER},
    partitions = 1,
    topics = {TopicContentsControllerIT.TEST_TOPIC_NAME})
@Slf4j
public class TopicContentsControllerIT {

  public static final String CUSTOM_SELECTION = "custom";
  public static final String RANGE_SELECTION = "range";
  public static final String BOOTSTRAP_SERVER = "localhost:9092";
  public static final String TEST_TOPIC_NAME = "test-topic";
  public static final int TOTAL_TEST_RECORDS = 10;
  public static final String KWCLUSTERAPIUSER = "kwclusterapiuser";
  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired private MockMvc mvc;

  @Value("${klaw.clusterapi.access.base64.secret}")
  private String clusterAccessSecret;

  private static int totalRecordsProduced = 0;
  private final UtilMethods utilMethods = new UtilMethods();

  @Test
  @Order(1)
  void getTopicContentsWhenSelectedNumberOfOffsetsLessThanTotalRecords() throws Exception {
    produceRecords(TOTAL_TEST_RECORDS);

    int totalOffsets = 5;
    String url =
        createUrl(
            String.valueOf(totalOffsets), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, totalOffsets, totalOffsets, TOTAL_TEST_RECORDS - 1);
  }

  @Test
  @Order(2)
  void getTopicContentsWhenSelectedNumberOfOffsetsMoreThanTotalRecords() throws Exception {
    int totalOffsets = TOTAL_TEST_RECORDS + 10;
    String url =
        createUrl(
            String.valueOf(totalOffsets), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, TOTAL_TEST_RECORDS, 0, TOTAL_TEST_RECORDS - 1);
  }

  @Test
  @Order(3)
  void getTopicContentsWhenSelectedNumberOfOffsetsIsNegative() throws Exception {
    String url =
        createUrl(String.valueOf(-1), Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    assertThat(response).isEmpty();
  }

  @Test
  @Order(4)
  void getTopicContentsWhenCustomAndOffsetsLessThanTotalRecords() throws Exception {
    int totalOffsets = 5;
    String url = createUrl(CUSTOM_SELECTION, totalOffsets, Integer.MAX_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, totalOffsets, totalOffsets, TOTAL_TEST_RECORDS - 1);
  }

  @Test
  @Order(5)
  void getTopicContentsWhenCustomAndOffsetsMoreThanTotalRecords() throws Exception {
    int totalOffsets = TOTAL_TEST_RECORDS + 10;
    String url = createUrl(CUSTOM_SELECTION, totalOffsets, Integer.MAX_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, TOTAL_TEST_RECORDS, 0, TOTAL_TEST_RECORDS - 1);
  }

  @Test
  @Order(6)
  void getTopicContentsWhenCustomAndOffsetAndOffsetIsNegative() throws Exception {
    String url = createUrl(CUSTOM_SELECTION, -1, Integer.MAX_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    assertThat(response).isEmpty();
  }

  @Test
  @Order(7)
  void getTopicContentsWhenRangeAndValidBounds() throws Exception {
    int start = 4;
    int end = 7;
    String url = createUrl(RANGE_SELECTION, Integer.MAX_VALUE, start, end);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, 4, start, end);
  }

  @Test
  @Order(8)
  void getTopicContentsWhenRangeAndInvalidLowerBound() throws Exception {
    int end = 7;
    String url = createUrl(RANGE_SELECTION, Integer.MAX_VALUE, Integer.MIN_VALUE, end);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, 8, 0, end);
  }

  @Test
  @Order(9)
  void getTopicContentsWhenRangeAndInvalidUpperBound() throws Exception {
    int start = 4;
    String url = createUrl(RANGE_SELECTION, Integer.MAX_VALUE, start, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, 6, start, 9);
  }

  @Test
  @Order(10)
  void getTopicContentsWhenRangeAndInvalidUpperLowerBounds() throws Exception {
    String url =
        createUrl(RANGE_SELECTION, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, TOTAL_TEST_RECORDS, 0, TOTAL_TEST_RECORDS - 1);
  }

  @Test
  @Order(11)
  void getTopicContentsWhenRangeAndLowerBoundBiggerThanUpperBound() throws Exception {
    String url = createUrl(RANGE_SELECTION, Integer.MAX_VALUE, 6, 3);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    assertThat(response).isEmpty();
  }

  @Test
  @Order(12)
  void getTopicContentsWhenRangeAndTotalOffsetsLargerThanMax() throws Exception {
    produceRecords(200);
    String url = createUrl(RANGE_SELECTION, Integer.MAX_VALUE, 20, 180);

    Map<Integer, String> response = getTopicContentsPerformMockRequest(url);

    getTopicContentsVerifyResponse(response, 100, 20, 119);
  }

  private String createUrl(
      String offsetType, int customNumberOfOffsets, int rangeStart, int rangeEnd) {
    return String.format(
        "/topics/getTopicContents/%s/PLAINTEXT/notdefined/test-topic/%s/partitionId/0/selectedNumberOfOffsets/%d/DEV_CLUSTER1/rangeOffsets/%d/%d",
        BOOTSTRAP_SERVER, offsetType, customNumberOfOffsets, rangeStart, rangeEnd);
  }

  private void produceRecords(int number) {
    Properties properties = new Properties();
    properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    properties.setProperty(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.setProperty(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

    KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

    ProducerRecord<String, String> producerRecord;
    for (int x = 0; x < number; x++) {
      producerRecord =
          new ProducerRecord<>("test-topic", String.format("value%d", totalRecordsProduced++));
      kafkaProducer.send(producerRecord);
    }

    kafkaProducer.flush();
    kafkaProducer.close();
  }

  private Map<Integer, String> getTopicContentsPerformMockRequest(String url) throws Exception {
    MockHttpServletResponse response =
        mvc.perform(
                MockMvcRequestBuilders.get(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                        AUTHORIZATION,
                        BEARER_PREFIX
                            + utilMethods.generateToken(KWCLUSTERAPIUSER, clusterAccessSecret, 3L))
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

    MapType mapType =
        TypeFactory.defaultInstance().constructMapType(HashMap.class, Integer.class, String.class);
    return OBJECT_MAPPER.readValue(response.getContentAsString(), mapType);
  }

  private void getTopicContentsVerifyResponse(
      Map<Integer, String> response, int responseSize, int offsetIdStart, int offsetIdEnd) {
    assertThat(response.size()).isEqualTo(responseSize);
    for (int x = offsetIdStart; x <= offsetIdEnd; x++) {
      assertThat(response.containsKey(x)).isTrue();
      assertThat(response.get(x)).isEqualTo(String.format("value%d", x));
    }
  }
}
