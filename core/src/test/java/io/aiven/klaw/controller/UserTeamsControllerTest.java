package io.aiven.klaw.controller;

import static io.aiven.klaw.helpers.KwConstants.PASSWORD_UPDATE_REGEX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.response.ResetPasswordInfo;
import io.aiven.klaw.service.UsersTeamsControllerService;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTeamsControllerTest {

  @MockBean private UsersTeamsControllerService usersTeamsControllerService;

  private MockMvc mvc;

  @BeforeEach
  public void setUp() {
    UsersTeamsController usersTeamsController = new UsersTeamsController();
    mvc = MockMvcBuilders.standaloneSetup(usersTeamsController).dispatchOptions(true).build();
    ReflectionTestUtils.setField(
        usersTeamsController, "usersTeamsControllerService", usersTeamsControllerService);
  }

  @ParameterizedTest
  @Order(1)
  @CsvSource({
    "'invalidpwd', 400", // Invalid password -> Expect 4xx Client Error
    "'Invalidpwd321@', 200" // Valid password -> Expect 200 OK
  })
  public void resetPasswordWithTokenTest(String password, int expectedStatus) throws Exception {
    ResetPasswordInfo passwordReset = new ResetPasswordInfo();
    when(usersTeamsControllerService.resetPassword(anyString(), anyString(), anyString()))
        .thenReturn(passwordReset);

    mvc.perform(
            MockMvcRequestBuilders.post("/reset/password")
                .param("token", "token")
                .param("password", password)
                .param("username", "username")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().is(expectedStatus));
  }

  @ParameterizedTest
  @Order(2)
  @CsvSource({
    "invalidpwd, false", // Invalid password -> Expect 4xx Client Error
    "Invalidpwd321@, true", // Valid password -> Expect 200 OK
    "********, true",
    "******, false",
  })
  public void updateUserPasswordTest(String password, boolean expectedStatus) throws KlawException {
    Matcher matcher = Pattern.compile(PASSWORD_UPDATE_REGEX).matcher(password);
    assertThat(matcher.find()).isEqualTo(expectedStatus);
  }
}
