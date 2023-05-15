package io.aiven.klaw.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MailUtilsTest {

  public static final String LOGIN_URL = "https://localhost:9097";
  @Mock UserDetails userDetails;

  @Mock HandleDbRequests handleDbRequests;

  @Mock EmailService emailService;

  @Mock ManageDatabase manageDatabase;

  @InjectMocks private MailUtils mailService;

  @BeforeEach
  public void setUp() throws Exception {
    //    mailService = new MailUtils();
  }

  @Test
  public void getUserDetails() {}

  @Test
  public void resetPasswordEmail_noCCTeam() throws InterruptedException {

    String username = "Octopus";
    UserInfo info = new UserInfo();
    info.setUsername(username);
    info.setMailid("Octopus.klaw@mailid");
    when(manageDatabase.selectAllCachedUserInfo()).thenReturn(List.of(info));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.passwordreset.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    mailService.sendMailResetPwd(username, "KlawPassword", handleDbRequests, 101, LOGIN_URL);

    Thread.sleep(1000);
    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(info.getMailid()), eq(null), anyString(), anyString(), eq(101), eq(LOGIN_URL));
  }

  @Test
  public void resetPasswordEmail_noSuchUser() {

    String username = "Octopus";
    UserInfo info = new UserInfo();
    info.setUsername("Octi");
    info.setMailid("Octi.klaw@mailid");
    when(manageDatabase.selectAllCachedUserInfo()).thenReturn(List.of(info));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.passwordreset.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    mailService.sendMailResetPwd(username, "KlawPassword", handleDbRequests, 101, LOGIN_URL);
    Mockito.verify(emailService, timeout(1000).times(0))
        .sendSimpleMessage(
            eq(info.getMailid()), eq(null), anyString(), anyString(), eq(101), eq(LOGIN_URL));
  }
}
