package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.model.RequestsCountOverview;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.service.RequestStatisticsService;
import io.aiven.klaw.service.UtilControllerService;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
}
