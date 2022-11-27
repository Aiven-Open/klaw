package io.aiven.klaw.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.enums.ApiResultStatus;
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

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  @MockBean private UtilControllerService utilControllerService;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    UtilController utilController = new UtilController();
    mvc = MockMvcBuilders.standaloneSetup(utilController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(utilController, "utilControllerService", utilControllerService);
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
}
