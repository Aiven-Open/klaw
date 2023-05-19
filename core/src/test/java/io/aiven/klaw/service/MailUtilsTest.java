package io.aiven.klaw.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.PermissionType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class MailUtilsTest {

  public static final String LOGIN_URL = "https://localhost:9097";
  @Mock UserDetails userDetails;

  //  @Mock HandleDbRequests handleDbRequests;

  @Mock HandleDbRequestsJdbc handleDbRequestsJdbc;

  @Mock EmailService emailService;

  @Mock ManageDatabase manageDatabase;

  @InjectMocks private MailUtils mailService;

  @BeforeEach
  public void setUp() throws Exception {
    //    mailService = new MailUtils();
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);
    when(handleDbRequestsJdbc.getUsersInfo(eq("james")))
        .thenReturn(createUserInfo("James", "USER"));
    when(manageDatabase.getRolesPermissionsPerTenant(eq(101)))
        .thenReturn(getRolesToPermissionsMap());
  }

  @Test
  public void getUserDetails() {}

  @Test
  public void resetPasswordEmail_noCCTeam() throws InterruptedException {

    String username = "Octopus";
    UserInfo info = createUserInfo(username, "USER");
    when(manageDatabase.selectAllCachedUserInfo()).thenReturn(List.of(info));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.passwordreset.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    mailService.sendMailResetPwd(username, "KlawPassword", handleDbRequestsJdbc, 101, LOGIN_URL);

    Thread.sleep(1000);
    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(info.getMailid()), eq(null), anyString(), anyString(), eq(101), eq(LOGIN_URL));
  }

  @Test
  public void resetPasswordEmail_noSuchUser() {

    String username = "Octopus";
    UserInfo info = createUserInfo("Octi", "USER");
    when(manageDatabase.selectAllCachedUserInfo()).thenReturn(List.of(info));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.passwordreset.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    mailService.sendMailResetPwd(username, "KlawPassword", handleDbRequestsJdbc, 101, LOGIN_URL);
    Mockito.verify(emailService, timeout(1000).times(0))
        .sendSimpleMessage(
            eq(info.getMailid()), eq(null), anyString(), anyString(), eq(101), eq(LOGIN_URL));
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"USER"})
  public void sendMailWith_NoBCC() {
    String username = "James";
    when(manageDatabase.selectAllCachedUserInfo())
        .thenReturn(List.of(createUserInfo(username, "USER"), createUserInfo("Tom", "ADMIN")));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.aclrequestapproval.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_ACLREQUESTAPPROVAL_CONTENT);

    mailService.sendMail(
        "TopicOne",
        "AclOne",
        null,
        username,
        handleDbRequestsJdbc,
        MailType.ACL_REQUEST_APPROVED,
        LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            anyString(), eq(null), eq(null), anyString(), anyString(), eq(101), eq(LOGIN_URL));
  }

  @Test
  @WithMockUser(
      username = "james",
      authorities = {"USER"})
  public void sendMailWith_BCC() {

    String username = "James";
    when(manageDatabase.selectAllCachedUserInfo())
        .thenReturn(List.of(createUserInfo(username, "USER"), createUserInfo("Tom", "ADMIN")));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicrequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_TOPICREQUEST_CONTENT);

    mailService.sendMail(
        "TopicOne",
        "AclOne",
        null,
        username,
        handleDbRequestsJdbc,
        MailType.TOPIC_CREATE_REQUESTED,
        LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            anyString(),
            eq(null),
            eq(List.of("Tom.klaw@mailid")),
            anyString(),
            anyString(),
            eq(101),
            eq(LOGIN_URL));
  }

  private Map<String, List<String>> getRolesToPermissionsMap() {

    List<String> user = List.of(PermissionType.APPROVE_TOPICS.name());

    List<String> admin =
        List.of(
            PermissionType.APPROVE_TOPICS.name(), PermissionType.APPROVE_ALL_REQUESTS_TEAMS.name());

    return new HashMap<>() {
      {
        put("USER", user);
        put("ADMIN", admin);
      }
    };
  }

  private static UserInfo createUserInfo(String username, String role) {
    UserInfo info = new UserInfo();
    info.setUsername(username);
    info.setMailid(username + ".klaw@mailid");
    info.setTenantId(101);
    info.setTeamId(10);
    info.setRole(role);
    return info;
  }
}
