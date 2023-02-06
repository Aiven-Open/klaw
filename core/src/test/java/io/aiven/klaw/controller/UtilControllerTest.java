package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.RequestEntityStatusCount;
import io.aiven.klaw.model.RequestStatusCount;
import io.aiven.klaw.model.RequestsCountOverview;
import io.aiven.klaw.model.RequestsOperationTypeCount;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.service.RequestStatisticsService;
import io.aiven.klaw.service.UtilControllerService;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
public class UtilControllerTest {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  @MockBean private UtilControllerService utilControllerService;

  @MockBean private RequestStatisticsService requestStatisticsService;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    UtilController utilController = new UtilController();
    mvc = MockMvcBuilders.standaloneSetup(utilController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(utilController, "utilControllerService", utilControllerService);
    ReflectionTestUtils.setField(
        utilController, "requestStatisticsService", requestStatisticsService);
  }

  @Test
  @Order(1)
  public void getAuth() throws Exception {
    HashMap<String, String> hMap = new HashMap<>();
    hMap.put("status", ApiResultStatus.AUTHORIZED.value);
    when(utilControllerService.getAuth()).thenReturn(hMap);

    mvc.perform(
            MockMvcRequestBuilders.get("/getAuth")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("Authorized")));
  }

  @Test
  @Order(2)
  public void getLogoutPage() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/logout")).andExpect(status().isOk());
  }

  @Test
  @Order(3)
  public void getRequestStatistics() throws Exception {
    RequestsCountOverview requestsCountOverview = getRequestStatisticsOverview();
    when(requestStatisticsService.getRequestsCountOverview(any()))
        .thenReturn(requestsCountOverview);
    mvc.perform(
            MockMvcRequestBuilders.get("/requests/statistics")
                .contentType(MediaType.APPLICATION_JSON)
                .param("requestMode", RequestMode.MY_REQUESTS.toString())
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requestEntityStatistics", hasSize(5)));
  }

  private RequestsCountOverview getRequestStatisticsOverview() {
    RequestsCountOverview requestsCountOverview = new RequestsCountOverview();
    Set<RequestEntityStatusCount> requestEntityStatusCountSet = new HashSet<>();

    Map<String, Long> opCounts = new HashMap<>();
    Map<String, Long> stCounts = new HashMap<>();

    opCounts.put("CREATE", 2L);
    opCounts.put("UPDATE", 3L);
    stCounts.put("CREATED", 2L);
    stCounts.put("APPROVED", 4L);

    Set<RequestStatusCount> requestStatusCountSet = new HashSet<>();
    Set<RequestsOperationTypeCount> requestsOperationTypeCountsSet = new HashSet<>();

    for (String key : stCounts.keySet()) {
      RequestStatusCount requestStatusCount =
          RequestStatusCount.builder()
              .requestStatus(RequestStatus.valueOf(key))
              .count(stCounts.get(key))
              .build();
      requestStatusCountSet.add(requestStatusCount);
    }

    for (String key : opCounts.keySet()) {
      RequestsOperationTypeCount requestsOperationTypeCount =
          RequestsOperationTypeCount.builder()
              .requestOperationType(RequestOperationType.valueOf(key))
              .count(opCounts.get(key))
              .build();
      requestsOperationTypeCountsSet.add(requestsOperationTypeCount);
    }

    RequestEntityStatusCount requestEntityTopicStatusCount = new RequestEntityStatusCount();
    requestEntityTopicStatusCount.setRequestEntityType(RequestEntityType.TOPIC);
    requestEntityTopicStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityTopicStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityTopicStatusCount);

    RequestEntityStatusCount requestEntityAclStatusCount = new RequestEntityStatusCount();
    requestEntityAclStatusCount.setRequestEntityType(RequestEntityType.ACL);
    requestEntityAclStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityAclStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityAclStatusCount);

    RequestEntityStatusCount requestEntitySchemaStatusCount = new RequestEntityStatusCount();
    requestEntitySchemaStatusCount.setRequestEntityType(RequestEntityType.SCHEMA);
    requestEntitySchemaStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntitySchemaStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntitySchemaStatusCount);

    RequestEntityStatusCount requestEntityConnectStatusCount = new RequestEntityStatusCount();
    requestEntityConnectStatusCount.setRequestEntityType(RequestEntityType.CONNECTOR);
    requestEntityConnectStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityConnectStatusCount.setRequestsOperationTypeCountSet(
        requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityConnectStatusCount);

    RequestEntityStatusCount requestEntityUsersStatusCount = new RequestEntityStatusCount();
    requestEntityUsersStatusCount.setRequestEntityType(RequestEntityType.USER);
    requestEntityUsersStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityUsersStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityUsersStatusCount);

    requestsCountOverview.setRequestEntityStatistics(requestEntityStatusCountSet);

    return requestsCountOverview;
  }
}
