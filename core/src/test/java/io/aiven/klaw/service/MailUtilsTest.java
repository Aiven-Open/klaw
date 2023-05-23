package io.aiven.klaw.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.PermissionType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
    when(handleDbRequestsJdbc.getAllTeamsOfUsers(eq("James"), eq(101)))
        .thenReturn(List.of(createTeam("OpenSource", 5), createTeam("Octopus", 11)));
    when(manageDatabase.getRolesPermissionsPerTenant(eq(101)))
        .thenReturn(getRolesToPermissionsMap());

    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.passwordreset.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicrequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicdeleterequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicclaimrequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicrequestapproval.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicrequestdenial.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.aclrequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.aclrequestdelete.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.aclrequestapproval.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.aclrequestdenial.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_PASSWORDRESET_CONTENT);
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
        null,
        null,
        handleDbRequestsJdbc,
        MailType.ACL_REQUEST_APPROVED,
        LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(List.of("James.klaw@mailid")),
            eq(List.of("OpenSource.klaw@mailid")),
            eq(Collections.emptyList()),
            anyString(),
            anyString(),
            eq(101),
            eq(LOGIN_URL));
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
        null,
        null,
        handleDbRequestsJdbc,
        MailType.TOPIC_CREATE_REQUESTED,
        LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(List.of("OpenSource.klaw@mailid")), // to is to the approver team
            eq(List.of("James.klaw@mailid")), // cc is to the requester
            eq(List.of("Tom.klaw@mailid")), // bcc is to the user with the permission to approve all
            // requests.
            anyString(),
            anyString(),
            eq(101),
            eq(LOGIN_URL));
  }

  /** Tests all emails being sent for request for new resources */
  @WithMockUser(
      username = "james",
      authorities = {"USER"})
  @ParameterizedTest
  @MethodSource(value = "requestMailType")
  public void sendRequestEmail(MailType mailType) {

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
        null,
        null,
        handleDbRequestsJdbc,
        mailType,
        LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(List.of("OpenSource.klaw@mailid")), // to is to the approver team
            eq(List.of("James.klaw@mailid")), // cc is to the requester
            eq(List.of("Tom.klaw@mailid")), // bcc is to the user with the permission to approve all
            // requests.
            anyString(),
            anyString(),
            eq(101),
            eq(LOGIN_URL));
  }

  /** Test all emails being sent for notification of approvals */
  @WithMockUser(
      username = "james",
      authorities = {"USER"})
  @ParameterizedTest()
  @MethodSource(value = "approvedMailType")
  public void sendRequestApprovedEmail(MailType mailType) {
    String username = "James";
    when(manageDatabase.selectAllCachedUserInfo())
        .thenReturn(
            List.of(
                createUserInfo(username, "USER"),
                createUserInfo("Rob", "USER"),
                createUserInfo("Tom", "ADMIN")));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicrequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_TOPICREQUEST_CONTENT);

    mailService.sendMail(
        "TopicOne", "AclOne", "", username, "Rob", null, handleDbRequestsJdbc, mailType, LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(List.of("James.klaw@mailid")), // to is to the requester & the requestors team
            eq(List.of("OpenSource.klaw@mailid", "Rob.klaw@mailid")), // cc is to the approver team
            eq(Collections.emptyList()), // bcc is empty
            // requests.
            anyString(),
            anyString(),
            eq(101),
            eq(LOGIN_URL));
  }

  /***
   * Tests all emails sent to claim a resource
   */
  @WithMockUser(
      username = "james",
      authorities = {"USER"})
  @ParameterizedTest()
  @MethodSource(value = "claimMailType")
  public void sendClaimRequestEmail(MailType mailType) {

    String username = "James";
    when(manageDatabase.selectAllCachedUserInfo())
        .thenReturn(
            List.of(
                createUserInfo(username, "USER"),
                createUserInfo("Rob", "USER"),
                createUserInfo("Tom", "ADMIN")));
    when(manageDatabase.getKwPropertyValue(eq("klaw.mail.topicrequest.content"), eq(101)))
        .thenReturn(KwConstants.MAIL_TOPICREQUEST_CONTENT);

    when(handleDbRequestsJdbc.getTeamDetails(eq(11), eq(101)))
        .thenReturn(createTeam("Octopus", 11));

    mailService.sendMail(
        "TopicOne",
        "AclOne",
        "",
        username,
        "Rob",
        Integer.parseInt("11"),
        handleDbRequestsJdbc,
        mailType,
        LOGIN_URL);

    Mockito.verify(emailService, timeout(1000).times(1))
        .sendSimpleMessage(
            eq(
                List.of(
                    "Octopus.klaw@mailid")), // to is to the approver team and if there is a contact
            // assigned to the resource
            eq(
                List.of(
                    "OpenSource.klaw@mailid",
                    "James.klaw@mailid")), // cc is to the requester & the requestors team
            eq(List.of("Tom.klaw@mailid")), // bcc has thohse with special approval permissions.
            // requests.
            anyString(),
            anyString(),
            eq(101),
            eq(LOGIN_URL));
  }

  /** Tests all emails sent for notification of claim approval */
  private static Stream<MailType> requestMailType() {
    return Stream.of(
        MailType.CONNECTOR_CREATE_REQUESTED,
        MailType.CONNECTOR_DELETE_REQUESTED,
        MailType.SCHEMA_REQUESTED,
        MailType.TOPIC_DELETE_REQUESTED,
        MailType.TOPIC_CREATE_REQUESTED,
        MailType.ACL_REQUESTED,
        MailType.ACL_DELETE_REQUESTED);
  }

  private static Stream<MailType> approvedMailType() {
    return Stream.of(
        MailType.CONNECTOR_REQUEST_APPROVED,
        MailType.CONNECTOR_REQUEST_DENIED,
        MailType.SCHEMA_REQUEST_APPROVED,
        MailType.SCHEMA_REQUEST_DENIED,
        MailType.TOPIC_REQUEST_APPROVED,
        MailType.TOPIC_REQUEST_DENIED,
        MailType.ACL_REQUEST_DENIED,
        MailType.ACL_REQUEST_APPROVED,
        MailType.ACL_REQUEST_FAILURE);
  }

  private static Stream<MailType> claimMailType() {
    return Stream.of(MailType.CONNECTOR_CLAIM_REQUESTED, MailType.TOPIC_CLAIM_REQUESTED);
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

  private static Team createTeam(String teamName, int teamId) {
    Team t = new Team();
    t.setTeamId(teamId);
    t.setTeammail(teamName + ".klaw@mailid");
    t.setTeamname(teamName);
    return t;
  }
}
