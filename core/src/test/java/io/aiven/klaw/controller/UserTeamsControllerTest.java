package io.aiven.klaw.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.requests.UserUpdateInfoModel;
import io.aiven.klaw.model.response.ResetPasswordInfo;
import io.aiven.klaw.service.UsersTeamsControllerService;
import java.nio.charset.StandardCharsets;
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
import org.thymeleaf.util.StringUtils;

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
    "invalidpwd, 400", // Invalid password
    "Invalidpwd321@, 200", // Valid password
    ", 200", // Valid no password supplied
    "******, 400", // Invalid password
  })
  public void updateUserPasswordTest(String password, int expectedStatus) throws Exception {
    ResetPasswordInfo passwordReset = new ResetPasswordInfo();
    ApiResponse response;
    if (expectedStatus >= 200 && expectedStatus <= 299) {
      response = ApiResponse.SUCCESS;
    } else {
      response = ApiResponse.FAILURE;
    }
    when(usersTeamsControllerService.updateUser(any(UserInfoModel.class))).thenReturn(response);

    UserUpdateInfoModel user = new UserUpdateInfoModel();

    user.setUsername("octopus");
    user.setFullname("Octopus User");
    user.setSwitchTeams(false);
    user.setTeamId(1001);
    user.setRole("USER");
    user.setTenantId(101);
    user.setMailid("user@klaw-project.io");
    user.setUserPassword(StringUtils.isEmpty(password) ? null : password);

    mvc.perform(
            MockMvcRequestBuilders.post("/updateUser")
                .param("token", "token")
                .content(new ObjectMapper().writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().is(expectedStatus));
  }
}
