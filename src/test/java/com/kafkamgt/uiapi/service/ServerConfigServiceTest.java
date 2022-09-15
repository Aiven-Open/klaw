package com.kafkamgt.uiapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.kafkamgt.uiapi.model.ServerConfigProperties;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ServerConfigServiceTest {

  ServerConfigService serverConfigService;

  private Environment env;

  @BeforeEach
  public void setUp() {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    this.env = context.getEnvironment();

    serverConfigService = new ServerConfigService(env);
  }

  @Test
  public void getAllProps() {
    serverConfigService.getAllProperties();
    List<ServerConfigProperties> list = serverConfigService.getAllProps();
    assertEquals(list.size() > 0, true);
  }
}
