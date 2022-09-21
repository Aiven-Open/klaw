package io.aiven.klaw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MailUtilsTest {

  @Mock UserDetails userDetails;

  private MailUtils mailService;

  @BeforeEach
  public void setUp() throws Exception {
    mailService = new MailUtils();
  }

  @Test
  public void getUserDetails() {}
}
