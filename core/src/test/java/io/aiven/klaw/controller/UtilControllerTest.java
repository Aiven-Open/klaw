package io.aiven.klaw.controller;

import static io.aiven.klaw.controller.TopicControllerTest.OBJECT_MAPPER;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.requests.ResetEntityCache;
import io.aiven.klaw.model.response.AuthenticationInfo;
import io.aiven.klaw.model.response.RequestsCountOverview;
import io.aiven.klaw.service.RequestStatisticsService;
import io.aiven.klaw.service.UtilControllerService;
import java.nio.charset.StandardCharsets;
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

  UtilMethods utilMethods;
  @MockBean private UtilControllerService utilControllerService;

  @MockBean private RequestStatisticsService requestStatisticsService;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    utilMethods = new UtilMethods();
    UtilController utilController = new UtilController();
    mvc = MockMvcBuilders.standaloneSetup(utilController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(utilController, "utilControllerService", utilControllerService);
    ReflectionTestUtils.setField(
        utilController, "requestStatisticsService", requestStatisticsService);
  }

  @Test
  @Order(1)
  public void getAuth() throws Exception {
    AuthenticationInfo authenticationInfo = new AuthenticationInfo();
    authenticationInfo.setCoralEnabled("true");
    when(utilControllerService.getAuth()).thenReturn(authenticationInfo);

    mvc.perform(
            MockMvcRequestBuilders.get("/getAuth")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.coralEnabled", is("true")));
  }

  @Test
  @Order(2)
  public void getLogoutPage() throws Exception {
    mvc.perform(MockMvcRequestBuilders.post("/logout")).andExpect(status().isOk());
  }

  @Test
  @Order(3)
  public void getRequestStatistics() throws Exception {
    RequestsCountOverview requestsCountOverview = utilMethods.getRequestStatisticsOverview();
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

  @Test
  public void resetMemoryCache() throws Exception {
    ResetEntityCache resetEntityCache = utilMethods.getResetEntityCache();
    String jsonReq = OBJECT_MAPPER.writer().writeValueAsString(resetEntityCache);
    ApiResponse apiResponse = ApiResponse.builder().message(ApiResultStatus.SUCCESS.value).build();
    when(utilControllerService.resetCache(eq(resetEntityCache))).thenReturn(apiResponse);
    mvc.perform(
            MockMvcRequestBuilders.post("/resetMemoryCache")
                .content(jsonReq)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", is(ApiResultStatus.SUCCESS.value)));
  }
}
