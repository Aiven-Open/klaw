package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.UtilMethods;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.MailType;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
  private String preferredUsername;

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
  private static final String NEW_USER_ADDED_KEY = "klaw.mail.newuseradded.content";
  private static final String PWD_RESET_KEY = "klaw.mail.passwordreset.content";

  private static final String PWD_CHANGED_KEY = "klaw.mail.passwordchanged.content";
  private static final String REGISTER_USER_KEY = "klaw.mail.registeruser.content";
  private static final String REGISTER_USER_SAAS_KEY = "klaw.mail.registeruser.saas.content";
  private static final String REGISTER_USER_TOUSER_KEY = "klaw.mail.registerusertouser.content";
  private static final String REGISTER_USER_SAAS_TOUSER_KEY =
      "klaw.mail.registerusertouser.saas.content";
  private static final String REGISTER_USER_SAASADMIN_TOUSER_KEY =
      "klaw.mail.registerusertouser.saasadmin.content";
  private static final String RECONCILIATION_TOPICS_KEY = "klaw.mail.recontopics.content";
  private String topicRequestMail;

  private String topicPromotionRequestMail;
  private String topicUpdateRequestMail;
  private String topicDeleteRequestMail;
  private String topicClaimRequestMail;
  private String topicRequestApproved;
  private String topicRequestDenied;
  private String aclRequestMail;
  private String aclDeleteRequestMail;
  private String aclRequestApproved;
  private String aclRequestDenied;

  private void loadKwProps(int tenantId) {
    this.topicRequestMail = manageDatabase.getKwPropertyValue(TOPIC_REQ_KEY, tenantId);

    this.topicUpdateRequestMail = manageDatabase.getKwPropertyValue(TOPIC_UPDATE_REQ_KEY, tenantId);
    this.topicPromotionRequestMail =
        manageDatabase.getKwPropertyValue(TOPIC_PROMOTION_REQ_KEY, tenantId);
    this.topicDeleteRequestMail = manageDatabase.getKwPropertyValue(TOPIC_REQ_DEL_KEY, tenantId);
    this.topicClaimRequestMail = manageDatabase.getKwPropertyValue(TOPIC_REQ_CLAIM_KEY, tenantId);
    this.topicRequestApproved = manageDatabase.getKwPropertyValue(TOPIC_REQ_APPRVL_KEY, tenantId);
    this.topicRequestDenied = manageDatabase.getKwPropertyValue(TOPIC_REQ_DENY_KEY, tenantId);
    this.aclRequestMail = manageDatabase.getKwPropertyValue(ACL_REQ_KEY, tenantId);
    this.aclDeleteRequestMail = manageDatabase.getKwPropertyValue(ACL_DELETE_REQ_KEY, tenantId);
    this.aclRequestApproved = manageDatabase.getKwPropertyValue(ACL_REQ_APPRVL_KEY, tenantId);
    this.aclRequestDenied = manageDatabase.getKwPropertyValue(ACL_REQ_DENY_KEY, tenantId);
  }

  @Autowired ManageDatabase manageDatabase;

  @Autowired private EmailService emailService;

  public String getUserName(Object principal) {
    return UtilMethods.getUserName(principal, preferredUsername);
  }

  public String getCurrentUserName() {
    return UtilMethods.getUserName(preferredUsername);
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
    String formattedStr = null, subject = null;
    int tenantId =
        manageDatabase
            .getHandleDbRequests()
            .getUsersInfo(
                getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
            .getTenantId();
    loadKwProps(tenantId);
    boolean requiresApproval = false;
    switch (mailType) {
      case TOPIC_CREATE_REQUESTED -> {
        formattedStr = String.format(topicRequestMail, "'" + topicName + "'");
        subject = "Create Topic Request";
        requiresApproval = true;
      }
      case TOPIC_PROMOTION_REQUESTED -> {
        formattedStr = String.format(topicPromotionRequestMail, "'" + topicName + "'");
        subject = "Topic Promotion Request";
        requiresApproval = true;
      }
      case TOPIC_UPDATE_REQUESTED -> {
        formattedStr = String.format(topicUpdateRequestMail, "'" + topicName + "'");
        subject = "Topic Update Request";
        requiresApproval = true;
      }
      case TOPIC_DELETE_REQUESTED -> {
        formattedStr = String.format(topicDeleteRequestMail, "'" + topicName + "'");
        subject = "Delete Topic Request";
        requiresApproval = true;
      }
      case TOPIC_CLAIM_REQUESTED -> {
        formattedStr = String.format(topicClaimRequestMail, "'" + topicName + "'");
        subject = "Claim Topic Request";
        requiresApproval = true;
      }
      case TOPIC_REQUEST_APPROVED -> {
        formattedStr = String.format(topicRequestApproved, "'" + topicName + "'");
        subject = "Topic Request Approved";
      }
      case TOPIC_REQUEST_DENIED -> {
        formattedStr =
            String.format(topicRequestDenied, "'" + topicName + "'", "'" + reasonToDecline + "'");
        subject = "Topic Request Denied";
      }
      case ACL_REQUESTED -> {
        formattedStr = String.format(aclRequestMail, "'" + acl + "'", "'" + topicName + "'");
        subject = "New Acl Request";
        requiresApproval = true;
      }
      case ACL_DELETE_REQUESTED -> {
        formattedStr = String.format(aclDeleteRequestMail, "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Delete Request";
        requiresApproval = true;
      }
      case ACL_REQUEST_APPROVED -> {
        formattedStr = String.format(aclRequestApproved, "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Request Approved";
      }
      case ACL_REQUEST_APPROVAL_ADDED -> {
        formattedStr =
            String.format(
                "Acl Request %s for topic %s has had an approval added to it.",
                "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Request Approval Addded";
      }
      case ACL_REQUEST_DENIED -> {
        formattedStr =
            String.format(
                aclRequestDenied,
                "'" + acl + "'",
                "'" + topicName + "'",
                "'" + reasonToDecline + "'");
        subject = "Acl Request Denied";
      }
      case ACL_REQUEST_FAILURE -> {
        formattedStr = "Acl Request processing failed : " + acl + ", " + topicName;
        subject = "Request processing failed.";
      }
      case SCHEMA_REQUESTED -> {
        requiresApproval = true;
        subject = "New Schema Request";
        formattedStr = "New Schema Request on " + topicName;
      }
      case SCHEMA_PROMOTION_REQUESTED -> {
        requiresApproval = true;
        subject = "New Schema Promotion Request";
        formattedStr = "New Schema Promotion Request on " + topicName;
      }
      case CONNECTOR_DELETE_REQUESTED, CONNECTOR_CREATE_REQUESTED -> {
        // all remaining requests that require approvals are grouped here.
        requiresApproval = true;
        subject = "New Connector Request";
        formattedStr = "New Connector Request on " + topicName;
      }
      case SCHEMA_REQUEST_APPROVED -> {
        subject = "Schema Request Approved";
        formattedStr = "Schema Request on " + topicName + " approved by " + approverUsername;
      }
      case SCHEMA_REQUEST_DENIED -> {
        subject = "Schema Request Denied";
        formattedStr =
            "Schema Request on "
                + topicName
                + " denied by "
                + approverUsername
                + "because : "
                + reasonToDecline;
      }
      case CONNECTOR_REQUEST_DENIED -> {
        subject = "Connector Request Denied";
        formattedStr =
            "Connector Request on "
                + topicName
                + " denied by "
                + approverUsername
                + "because : "
                + reasonToDecline;
      }
      case CONNECTOR_REQUEST_APPROVED -> {
        subject = "Connector Request Approved";
        formattedStr = "Connector Request on " + topicName + " approved by " + approverUsername;
      }
      case CONNECTOR_CLAIM_REQUESTED -> {
        requiresApproval = true;
        subject = "New Connector Claim Request";
        formattedStr = "New Claim on Connector " + topicName;
      }
      case RESET_CONSUMER_OFFSET_REQUESTED -> {
        requiresApproval = true;
        subject = "Reset Consumer Offsets Request";
        formattedStr = "Reset Consumer Offsets topic :" + topicName + "consumerGroup : " + acl;
      }
      case RESET_CONSUMER_OFFSET_APPROVED -> {
        subject = "Reset Consumer Offsets Request Approved";
        formattedStr =
            "Reset Consumer Offsets Request on "
                + topicName
                + " \n OffsetDetails : "
                + acl
                + "\napproved by "
                + approverUsername;
      }
      case RESET_CONSUMER_OFFSET_DENIED -> {
        subject = "Reset Consumer Offsets Request Denied";
        formattedStr =
            "Reset Consumer Offsets Request on "
                + topicName
                + " \n Consumer group : "
                + acl
                + "\ndenied by "
                + approverUsername;
      }
    }

    sendRequestMail(
        approverUsername,
        username,
        ownerTeamId,
        dbHandle,
        formattedStr,
        subject,
        requiresApproval,
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
    String subject, formattedStr;
    if (mailType == MailType.SCHEMA_APPROVED_NOTIFY_SUBSCRIBERS) {
      subject = "New schema on a topic";
      formattedStr =
          "A schema has been uploaded on Topic :"
              + topicName
              + " Environment : "
              + envName
              + " by team : "
              + teamName;
      String finalSubject = subject;
      String finalFormattedStr = formattedStr;
      CompletableFuture.runAsync(
          () -> {
            emailService.sendSimpleMessage(
                toMailIds,
                Collections.singletonList(ccOwnerTeamMailId),
                Collections.emptyList(),
                finalSubject,
                finalFormattedStr,
                tenantId,
                loginUrl);
          });
    }
  }

  void sendMail(String username, String pwd, HandleDbRequests dbHandle, String loginUrl) {
    String formattedStr, subject;
    int tenantId =
        manageDatabase
            .getHandleDbRequests()
            .getUsersInfo(
                getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
            .getTenantId();
    String newUserAdded = manageDatabase.getKwPropertyValue(NEW_USER_ADDED_KEY, tenantId);
    formattedStr = String.format(newUserAdded, username, pwd);
    subject = "Access to Klaw";

    sendMail(username, dbHandle, formattedStr, subject, false, false, null, tenantId, loginUrl);
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

  void sendMailRegisteredUserSaas(
      RegisterUserInfo registerUserInfo,
      HandleDbRequests dbHandle,
      String tenantName,
      int tenantId,
      String teamName,
      String activationUrl,
      String loginUrl) {
    String formattedStr, subject;
    // sending to super admin
    String registrationRequest =
        manageDatabase.getKwPropertyValue(REGISTER_USER_SAAS_KEY, tenantId);
    formattedStr =
        String.format(
            registrationRequest, registerUserInfo.getUsername(), registerUserInfo.getFullname());

    subject = NEW_USER_REGISTRATION_REQUEST;
    if (!Objects.equals(registerUserInfo.getMailid(), kwAdminMailId)) {
      sendMailToAdmin(subject, formattedStr, tenantId, loginUrl);
    }
    // Sending to user
    if (KwConstants.INFRATEAM.equals(registerUserInfo.getTeam())) {
      registrationRequest =
          manageDatabase.getKwPropertyValue(REGISTER_USER_SAASADMIN_TOUSER_KEY, tenantId);
      formattedStr =
          String.format(
              registrationRequest,
              registerUserInfo.getUsername(),
              registerUserInfo.getPwd(),
              registerUserInfo.getFullname(),
              teamName,
              registerUserInfo.getRole(),
              activationUrl);
    } else {
      registrationRequest =
          manageDatabase.getKwPropertyValue(REGISTER_USER_SAAS_TOUSER_KEY, tenantId);
      formattedStr =
          String.format(
              registrationRequest,
              registerUserInfo.getUsername(),
              registerUserInfo.getPwd(),
              registerUserInfo.getFullname(),
              tenantName,
              teamName,
              registerUserInfo.getRole());
    }

    subject = KLAW_USER_REGISTRATION_REQUEST;
    sendMail(
        registerUserInfo.getUsername(),
        dbHandle,
        formattedStr,
        subject,
        true,
        false,
        registerUserInfo.getMailid(),
        tenantId,
        loginUrl);
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
          loginUrl);
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

  private void sendMail(
      String username,
      HandleDbRequests dbHandle,
      String formattedStr,
      String subject,
      boolean registrationRequest,
      boolean requiresApproval,
      String otherMailId,
      int tenantId,
      String loginUrl) {

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
              CollectionUtils.addIgnoreNull(to, emailIdTeam);
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

    Map<String, List<String>> rolesToPermissions =
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

  public String sendMailToSaasAdmin(int tenantId, String userName, String period, String loginUrl) {
    String mailtext =
        "Tenant extension : Tenant " + tenantId + " username " + userName + " period " + period;
    emailService.sendSimpleMessage(
        userName, kwAdminMailId, "Tenant Extension", mailtext, tenantId, loginUrl);
    return ApiResultStatus.SUCCESS.value;
  }
}
