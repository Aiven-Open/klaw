package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.UtilMethods;
import io.aiven.klaw.model.enums.PermissionType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailUtils {

  public static final String KLAW_ACCESS_PASSWORD_RESET_REQUESTED =
      "Klaw Access - Password reset requested";
  public static final String KLAW_ACCESS_PASSWORD_CHANGED =
      "Klaw Access - Password has been updated";
  public static final String NEW_USER_REGISTRATION_REQUEST = "New User Registration request";
  public static final String KLAW_USER_REGISTRATION_REQUEST = "Klaw User Registration request";
  public static final String KLAW_REGISTRATION_REQUEST = "Klaw Registration request";

  @Value("${klaw.admin.mailid}")
  private String kwAdminMailId;

  @Value("${klaw.ad.username.attribute:preferred_username}")
  private String preferredUsernameAttribute;

  @Value("${klaw.ad.email.attribute:email}")
  private String emailAttribute;

  private static final String TOPIC_REQ_KEY = "klaw.mail.topicrequest.content";
  private static final String TOPIC_PROMOTION_REQ_KEY = "klaw.mail.topicpromotionrequest.content";
  private static final String TOPIC_UPDATE_REQ_KEY = "klaw.mail.topicupdaterequest.content";
  private static final String TOPIC_REQ_DEL_KEY = "klaw.mail.topicdeleterequest.content";
  private static final String TOPIC_REQ_CLAIM_KEY = "klaw.mail.topicclaimrequest.content";
  private static final String TOPIC_REQ_APPRVL_KEY = "klaw.mail.topicrequestapproval.content";
  private static final String TOPIC_REQ_DENY_KEY = "klaw.mail.topicrequestdenial.content";
  private static final String ACL_REQ_KEY = "klaw.mail.aclrequest.content";
  private static final String ACL_DELETE_REQ_KEY = "klaw.mail.aclrequestdelete.content";
  private static final String ACL_REQ_APPRVL_KEY = "klaw.mail.aclrequestapproval.content";
  private static final String ACL_REQ_DENY_KEY = "klaw.mail.aclrequestdenial.content";
  public static final String NEW_USER_ADDED_V2_KEY = "klaw.mail.newuseradded.v2.content";
  private static final String PWD_RESET_KEY = "klaw.mail.passwordreset.content";

  private static final String PWD_CHANGED_KEY = "klaw.mail.passwordchanged.content";
  private static final String REGISTER_USER_KEY = "klaw.mail.registeruser.content";
  private static final String REGISTER_USER_TOUSER_KEY = "klaw.mail.registerusertouser.content";
  private static final String RECONCILIATION_TOPICS_KEY = "klaw.mail.recontopics.content";

  @Autowired ManageDatabase manageDatabase;

  @Autowired private EmailService emailService;

  public String getUserName(Object principal) {
    return UtilMethods.getUserName(principal, preferredUsernameAttribute, emailAttribute);
  }

  public String getCurrentUserName() {
    return UtilMethods.getUserName(preferredUsernameAttribute, emailAttribute);
  }

  void sendMail(
      String topicName,
      String acl,
      String reasonToDecline,
      String username,
      String approverUsername,
      Integer ownerTeamId,
      HandleDbRequests dbHandle,
      MailType mailType,
      String loginUrl) {
    int tenantId =
        manageDatabase
            .getHandleDbRequests()
            .getUsersInfo(
                getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
            .getTenantId();
    MailInfo mailInfo = MailInfo.of(topicName, acl, reasonToDecline, username, approverUsername);

    sendRequestMail(
        approverUsername,
        username,
        ownerTeamId,
        dbHandle,
        mailType.apply(manageDatabase, tenantId, mailInfo),
        mailType.getSubject(),
        mailType.isRequiresApproval(),
        tenantId,
        loginUrl);
  }

  void notifySubscribersOnSchemaChange(
      MailType mailType,
      String topicName,
      String envName,
      String teamName,
      List<String> toMailIds,
      String ccOwnerTeamMailId,
      int tenantId,
      String loginUrl) {
    if (mailType == MailType.SCHEMA_APPROVED_NOTIFY_SUBSCRIBERS) {
      CompletableFuture.runAsync(
          () ->
              emailService.sendSimpleMessage(
                  toMailIds,
                  Collections.singletonList(ccOwnerTeamMailId),
                  Collections.emptyList(),
                  mailType.subject,
                  mailType.apply(
                      manageDatabase,
                      tenantId,
                      MailInfo.of(topicName, envName, null, teamName, null)),
                  tenantId,
                  loginUrl));
    }
  }

  void sendMail(String username, HandleDbRequests dbHandle, String loginUrl) {
    String formattedStr, subject;
    int tenantId =
        manageDatabase
            .getHandleDbRequests()
            .getUsersInfo(
                getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
            .getTenantId();
    String newUserAdded = manageDatabase.getKwPropertyValue(NEW_USER_ADDED_V2_KEY, tenantId);
    formattedStr = String.format(newUserAdded, username);
    subject = "Access to Klaw";

    sendMail(
        username, dbHandle, formattedStr, subject, false, false, null, tenantId, loginUrl, false);
  }

  void sendMailResetPwd(
      String username,
      String resetToken,
      HandleDbRequests dbHandle,
      int tenantId,
      String loginUrl) {
    String formattedStr, subject;
    String passwordReset = manageDatabase.getKwPropertyValue(PWD_RESET_KEY, tenantId);
    formattedStr = String.format(passwordReset, resetToken);
    subject = KLAW_ACCESS_PASSWORD_RESET_REQUESTED;

    sendPwdResetMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
  }

  void sendMailPwdChanged(
      String username, HandleDbRequests dbHandle, int tenantId, String loginUrl) {
    String formattedStr, subject;
    String passwordChanged = manageDatabase.getKwPropertyValue(PWD_CHANGED_KEY, tenantId);
    formattedStr = String.format(passwordChanged, username);
    subject = KLAW_ACCESS_PASSWORD_CHANGED;

    sendPwdResetMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
  }

  void sendMailRegisteredUser(
      RegisterUserInfo registerUserInfo, HandleDbRequests dbHandle, String loginUrl) {
    try {
      String formattedStr, subject;
      int tenantId = registerUserInfo.getTenantId();
      // sending to super admin
      String registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_KEY, tenantId);
      formattedStr =
          String.format(
              registrationRequest,
              registerUserInfo.getUsername(),
              registerUserInfo.getFullname(),
              registerUserInfo.getTeam(),
              registerUserInfo.getRole());

      subject = NEW_USER_REGISTRATION_REQUEST;
      if (!Objects.equals(registerUserInfo.getMailid(), kwAdminMailId)) {
        sendMailToAdmin(subject, formattedStr, tenantId, loginUrl);
      }

      // Sending to user
      registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_TOUSER_KEY, tenantId);
      formattedStr =
          String.format(
              registrationRequest,
              registerUserInfo.getUsername(),
              registerUserInfo.getFullname(),
              registerUserInfo.getTeam(),
              registerUserInfo.getRole());

      subject = KLAW_REGISTRATION_REQUEST;
      sendMail(
          registerUserInfo.getUsername(),
          dbHandle,
          formattedStr,
          subject,
          true,
          false,
          registerUserInfo.getMailid(),
          tenantId,
          loginUrl,
          true);
    } catch (Exception e) {
      log.error(registerUserInfo.toString(), e);
    }
  }

  public void sendReconMailToAdmin(
      String subject, String reconTopicsContent, String tenantName, int tenantId, String loginUrl) {
    String reconMailContent =
        manageDatabase.getKwPropertyValue(RECONCILIATION_TOPICS_KEY, tenantId);
    String formattedStr = String.format(reconMailContent, tenantName) + "\n\n" + reconTopicsContent;

    try {
      CompletableFuture.runAsync(
              () -> {
                if (kwAdminMailId != null) {
                  emailService.sendSimpleMessage(
                      kwAdminMailId, null, subject, formattedStr, tenantId, loginUrl);
                }
              })
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Exception:", e);
    }
  }

  public void sendMailToAdmin(String subject, String mailContent, int tenantId, String loginUrl) {

    CompletableFuture.runAsync(
        () -> {
          if (kwAdminMailId != null) {
            emailService.sendSimpleMessage(
                kwAdminMailId, null, subject, mailContent, tenantId, loginUrl);
          }
        });
  }

  protected void sendMail(
      String username,
      HandleDbRequests dbHandle,
      String formattedStr,
      String subject,
      boolean registrationRequest,
      boolean requiresApproval,
      String otherMailId,
      int tenantId,
      String loginUrl,
      boolean copyTeam) {

    CompletableFuture.runAsync(
        () -> {
          List to = new ArrayList<>();
          List cc = new ArrayList<>();
          String emailId;

          String emailIdTeam = null;
          Integer teamId = null;
          List<String> allApprovers = null;
          try {
            if (registrationRequest) {
              emailId = otherMailId;
            } else {
              emailId = getEmailAddressFromUsername(username);
              CollectionUtils.addIgnoreNull(cc, otherMailId);
            }

            try {
              List<Team> allTeams = dbHandle.getAllTeamsOfUsers(username, tenantId);
              if (!allTeams.isEmpty()) {
                emailIdTeam = allTeams.get(0).getTeammail();
                teamId = allTeams.get(0).getTeamId();
              }
            } catch (Exception e) {
              log.error("Exception :", e);
            }
            if (requiresApproval) {
              allApprovers = getAllUsersWithPermissionToApproveRequest(tenantId, username, teamId);
            }
            if (emailId != null) {
              log.debug("emailId: {} Team: {}", emailId, emailIdTeam);

              CollectionUtils.addIgnoreNull(to, emailId);
              if (copyTeam) {
                CollectionUtils.addIgnoreNull(to, emailIdTeam);
              }
              emailService.sendSimpleMessage(
                  to, cc, allApprovers, subject, formattedStr, tenantId, loginUrl);
            } else {
              log.error("Email id not found. Notification not sent !!");
            }
          } catch (Exception e) {
            log.error("Email id not found. Notification not sent !! ", e);
          }
        });
  }

  private void sendRequestMail(
      String approverUsername,
      String requesterUsername,
      Integer resourceOwnerTeamId,
      HandleDbRequests dbHandle,
      String formattedStr,
      String subject,
      boolean requiresApproval,
      int tenantId,
      String loginUrl) {

    CompletableFuture.runAsync(
        () -> {
          String requesterEmail, approverEmail;

          String approverTeamEmail = null, requesterTeamEmail = null;
          Integer teamId = null;
          List<String> bcc = new ArrayList<>(), to = new ArrayList<>(), cc = new ArrayList<>();
          try {

            requesterEmail = getEmailAddressFromUsername(requesterUsername);
            approverEmail =
                approverUsername != null ? getEmailAddressFromUsername(approverUsername) : null;

            try {

              approverTeamEmail =
                  getApproverTeamEmail(dbHandle, resourceOwnerTeamId, approverUsername, tenantId);
              List<Team> requesterTeam = dbHandle.getAllTeamsOfUsers(requesterUsername, tenantId);

              if (!requesterTeam.isEmpty()) {
                requesterTeamEmail = requesterTeam.get(0).getTeammail();
                teamId = requesterTeam.get(0).getTeamId();
              }

              if (requesterTeamEmail != null
                      && requesterTeamEmail.equalsIgnoreCase(approverTeamEmail)
                  || (approverTeamEmail == null && requesterTeamEmail != null)) {
                approverTeamEmail = requesterTeamEmail;
                requesterTeamEmail = null;
              }
            } catch (Exception e) {
              log.error("Exception :", e);
            }

            if (requiresApproval) {
              bcc = getAllUsersWithPermissionToApproveRequest(tenantId, requesterUsername, teamId);
              CollectionUtils.addIgnoreNull(to, approverTeamEmail);

              CollectionUtils.addIgnoreNull(cc, requesterTeamEmail);
              CollectionUtils.addIgnoreNull(cc, requesterEmail);
            } else {
              CollectionUtils.addIgnoreNull(cc, approverTeamEmail);
              CollectionUtils.addIgnoreNull(cc, approverEmail);

              CollectionUtils.addIgnoreNull(to, requesterEmail);
              CollectionUtils.addIgnoreNull(to, requesterTeamEmail);
            }
            emailService.sendSimpleMessage(to, cc, bcc, subject, formattedStr, tenantId, loginUrl);
          } catch (Exception e) {
            log.error("Email id not found. Notification not sent !! ", e);
          }
        });
  }

  public String getApproverTeamEmail(
      HandleDbRequests dbHandle,
      Integer resourceOwnerTeamId,
      String approverUsername,
      int tenantId) {
    Team approverTeam = dbHandle.getTeamDetails(resourceOwnerTeamId, tenantId);
    if (approverTeam != null) {
      return approverTeam.getTeammail();
    } else {

      List<Team> approverTeamList = dbHandle.getAllTeamsOfUsers(approverUsername, tenantId);
      if (!approverTeamList.isEmpty()) {
        return approverTeamList.get(0).getTeammail();
      } else {
        return null;
      }
    }
  }

  private void sendPwdResetMail(
      String username,
      HandleDbRequests dbHandle,
      String formattedStr,
      String subject,
      boolean registrationRequest,
      String otherMailId,
      int tenantId,
      String loginUrl) {

    CompletableFuture.runAsync(
        () -> {
          String emailId;

          try {
            emailId = getEmailAddressFromUsername(username);

            if (emailId != null) {
              emailService.sendSimpleMessage(
                  emailId, null, subject, formattedStr, tenantId, loginUrl);
            } else {
              log.error("Email id not found. Notification not sent !!");
            }
          } catch (Exception e) {
            log.error("Email id not found. Notification not sent !! ", e);
          }
        });
  }

  public String getEmailAddressFromUsername(String username) {

    Optional<UserInfo> user =
        manageDatabase.selectAllCachedUserInfo().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst();
    return user.map(UserInfo::getMailid).orElse(null);
  }

  private List<String> getAllUsersWithPermissionToApproveRequest(
      int tenantId, String username, Integer teamId) {

    Map<String, Set<String>> rolesToPermissions =
        manageDatabase.getRolesPermissionsPerTenant(tenantId);

    Set<String> roles = new HashSet<>();

    rolesToPermissions.forEach(
        (k, v) -> {
          if (v.contains(PermissionType.APPROVE_ALL_REQUESTS_TEAMS.name())) {
            roles.add(k);
          }
        });

    // Prevent duplicates only show from the correct tenant, that is not the usernae of the
    // requestor and that is not on the same team as they have already received that email.
    return manageDatabase.selectAllCachedUserInfo().stream()
        .filter(
            user ->
                user.getTenantId() == tenantId
                    && !user.getUsername().equals(username)
                    && roles.contains(user.getRole())
                    && (teamId == null || !user.getTeamId().equals(teamId)))
        .map(u -> u.getMailid())
        .toList();
  }

  public enum MailType {
    TOPIC_CREATE_REQUESTED(
        "Create Topic Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_REQ_KEY, tenantId),
                "'" + mailInfo.topicName + "'"),
        true),
    TOPIC_PROMOTION_REQUESTED(
        "Topic Promotion Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_PROMOTION_REQ_KEY, tenantId),
                "'" + mailInfo.topicName + "'"),
        true),
    CONNECTOR_CREATE_REQUESTED(
        "New Connector Request", m -> "New Connector Request on " + m.topicName, true),
    TOPIC_DELETE_REQUESTED(
        "Delete Topic Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_REQ_DEL_KEY, tenantId),
                "'" + mailInfo.topicName + "'"),
        true),
    CONNECTOR_DELETE_REQUESTED(
        "New Connector Request", m -> "New Connector Request on " + m.topicName, true),
    TOPIC_CLAIM_REQUESTED(
        "Claim Topic Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_REQ_CLAIM_KEY, tenantId),
                "'" + mailInfo.topicName + "'"),
        true),
    CONNECTOR_CLAIM_REQUESTED(
        "New Connector Claim Request", m -> "New Claim on Connector " + m.topicName, true),
    TOPIC_REQUEST_APPROVED(
        "Topic Request Approved",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_REQ_APPRVL_KEY, tenantId),
                "'" + mailInfo.topicName + "'"),
        false),
    CONNECTOR_REQUEST_APPROVED(
        "Connector Request Approved",
        m -> "Connector Request on " + m.topicName + " approved by " + m.approverUsername),
    TOPIC_REQUEST_DENIED(
        "Topic Request Denied",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_REQ_DENY_KEY, tenantId),
                "'" + mailInfo.topicName + "'",
                "'" + mailInfo.reasonToDecline + "'"),
        false),
    CONNECTOR_REQUEST_DENIED(
        "Connector Request Denied",
        m ->
            "Connector Request on "
                + m.topicName
                + " denied by "
                + m.approverUsername
                + "because : "
                + m.reasonToDecline),
    ACL_REQUESTED(
        "New Acl Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(ACL_REQ_KEY, tenantId),
                "'" + mailInfo.acl + "'",
                "'" + mailInfo.topicName + "'"),
        true),
    ACL_DELETE_REQUESTED(
        "Acl Delete Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(ACL_DELETE_REQ_KEY, tenantId),
                "'" + mailInfo.acl + "'",
                "'" + mailInfo.topicName + "'"),
        true),
    ACL_REQUEST_APPROVED(
        "Acl Request Approved",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(ACL_REQ_APPRVL_KEY, tenantId),
                "'" + mailInfo.acl + "'",
                "'" + mailInfo.topicName + "'"),
        false),
    ACL_REQUEST_APPROVAL_ADDED(
        "Acl Request Approval Addded",
        m ->
            String.format(
                "Acl Request %s for topic %s has had an approval added to it.",
                "'" + m.acl + "'", "'" + m.topicName + "'")),

    ACL_REQUEST_DENIED(
        "Acl Request Denied",
        ((manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(ACL_REQ_DENY_KEY, tenantId),
                "'" + mailInfo.acl + "'",
                "'" + mailInfo.topicName + "'",
                "'" + mailInfo.reasonToDecline + "'")),
        false),
    ACL_REQUEST_FAILURE(
        "Request processing failed.",
        m -> "Acl Request processing failed : " + m.acl + ", " + m.topicName),
    SCHEMA_REQUESTED("New Schema Request", m -> "New Schema Request on " + m.topicName, true),
    SCHEMA_REQUEST_APPROVED(
        "Schema Request Approved",
        m -> "Schema Request on " + m.topicName + " approved by " + m.approverUsername),
    SCHEMA_REQUEST_DENIED(
        "Schema Request Denied",
        m ->
            "Schema Request on "
                + m.topicName
                + " denied by "
                + m.approverUsername
                + "because : "
                + m.reasonToDecline),
    TOPIC_UPDATE_REQUESTED(
        "Topic Update Request",
        (manageDatabase, tenantId, mailInfo) ->
            String.format(
                manageDatabase.getKwPropertyValue(TOPIC_UPDATE_REQ_KEY, tenantId),
                "'" + mailInfo.topicName + "'"),
        false),
    SCHEMA_PROMOTION_REQUESTED(
        "New Schema Promotion Request",
        m -> "New Schema Promotion Request on " + m.topicName,
        true),
    SCHEMA_APPROVED_NOTIFY_SUBSCRIBERS(
        "New schema on a topic",
        m ->
            "A schema has been uploaded on Topic :"
                + m.topicName
                + " Environment : "
                + m.acl
                + " by team : "
                + m.username),
    RESET_CONSUMER_OFFSET_REQUESTED(
        "Reset Consumer Offsets Request",
        m -> "Reset Consumer Offsets topic :" + m.topicName + "consumerGroup : " + m.acl,
        true),
    RESET_CONSUMER_OFFSET_APPROVED(
        "Reset Consumer Offsets Request Approved",
        m ->
            "Reset Consumer Offsets Request on "
                + m.topicName
                + " \n OffsetDetails : "
                + m.acl
                + "\napproved by "
                + m.approverUsername),
    RESET_CONSUMER_OFFSET_DENIED(
        "Reset Consumer Offsets Request Denied",
        m ->
            "Reset Consumer Offsets Request on "
                + m.topicName
                + " \n Consumer group : "
                + m.acl
                + "\ndenied by "
                + m.approverUsername);

    private final String subject;
    private final Function<MailInfo, String> function;
    private final TriFunction<ManageDatabase, Integer, MailInfo, String> triFunction;
    private final boolean requiresApproval;

    MailType(String subject, Function<MailInfo, String> function, boolean requiresApproval) {
      this.requiresApproval = requiresApproval;
      this.subject = subject;
      this.function = function;
      this.triFunction = null;
    }

    MailType(
        String subject,
        TriFunction<ManageDatabase, Integer, MailInfo, String> triFunction,
        boolean requiresApproval) {
      this.requiresApproval = requiresApproval;
      this.subject = subject;
      this.triFunction = triFunction;
      this.function = null;
    }

    MailType(String subject, Function<MailInfo, String> function) {
      this(subject, function, false);
    }

    MailType(String subject, TriFunction<ManageDatabase, Integer, MailInfo, String> triFunction) {
      this(subject, triFunction, false);
    }

    public String apply(ManageDatabase manageDatabase, int tenantId, MailInfo mailInfo) {
      if (triFunction != null) {
        return triFunction.apply(manageDatabase, tenantId, mailInfo);
      }
      return function.apply(mailInfo);
    }

    public String getSubject() {
      return subject;
    }

    public boolean isRequiresApproval() {
      return requiresApproval;
    }
  }

  public static class MailInfo {
    private final String topicName;
    private final String acl;
    private final String reasonToDecline;
    private final String username;
    private final String approverUsername;

    private MailInfo(
        String topicName,
        String acl,
        String reasonToDecline,
        String username,
        String approverUsername) {
      this.topicName = topicName;
      this.acl = acl;
      this.reasonToDecline = reasonToDecline;
      this.username = username;
      this.approverUsername = approverUsername;
    }

    public static MailInfo of(
        String topicName,
        String acl,
        String reasonToDecline,
        String username,
        String approverUsername) {
      return new MailInfo(topicName, acl, reasonToDecline, username, approverUsername);
    }

    public String getTopicName() {
      return topicName;
    }

    public String getAcl() {
      return acl;
    }

    public String getReasonToDecline() {
      return reasonToDecline;
    }

    public String getUsername() {
      return username;
    }

    public String getApproverUsername() {
      return approverUsername;
    }
  }
}
