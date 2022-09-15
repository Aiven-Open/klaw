package com.kafkamgt.uiapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.service.UtilControllerService;
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
    hMap.put("status", "Authorized");
    when(utilControllerService.getAuth()).thenReturn(hMap);

    String res =
        mvc.perform(
                MockMvcRequestBuilders.get("/getAuth")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    HashMap<String, String> response = new ObjectMapper().readValue(res, HashMap.class);
    assertEquals("Authorized", response.get("status"));
  }

  @Test
  @Order(2)
  public void getLogoutPage() throws Exception {

    mvc.perform(MockMvcRequestBuilders.post("/logout"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }
}
