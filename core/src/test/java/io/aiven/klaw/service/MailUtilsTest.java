package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class MailUtilsTest {

  public static final String LOGIN_URL = "https://localhost:9097";
  @Mock UserDetails userDetails;

  @Mock
  HandleDbRequests handleDbRequests;

  @Mock
  EmailService emailService;

  @Mock
  ManageDatabase manageDatabase;

  @InjectMocks
  private MailUtils mailService;


  @BeforeEach
  public void setUp() throws Exception {
//    mailService = new MailUtils();
  }

  @Test
  public void getUserDetails() {}


  @Test
  public void resetPasswordEmail_noCCTeam() {

    String username = "Octopus";
    UserInfo info = new UserInfo();
    info.setUsername(username);
    info.setMailid("Octopus.klaw@mailid");
    when(handleDbRequests.getUsersInfo(username)).thenReturn(info);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.passwordreset.content"),eq(101))).thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    mailService.sendMailResetPwd(username,"KlawPassword",handleDbRequests ,101, LOGIN_URL);
    Mockito.verify(emailService,times(1)).sendSimpleMessage(eq(info.getMailid()),eq(null),anyString(),anyString(),eq(101),eq(LOGIN_URL));

  }
}
