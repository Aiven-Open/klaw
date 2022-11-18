package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.aiven.klaw.model.ServerConfigProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ServerConfigServiceTest {

  @Mock private CommonUtilsService commonUtilsService;
  ServerConfigService serverConfigService;

  @Mock private UserDetails userDetails;

  private Environment env;

  @BeforeEach
  public void setUp() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    this.env = context.getEnvironment();
    loginMock();

    serverConfigService = new ServerConfigService(env, commonUtilsService);
  }

  @Test
  public void getAllProps() {
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    serverConfigService.getAllProperties();
    List<ServerConfigProperties> list = serverConfigService.getAllProps();
    assertThat(list).isEmpty(); // filtering for spring. and klaw.
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }
}
