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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
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

    switch (mailType) {
      case TOPIC_CREATE_REQUESTED -> {
        formattedStr = String.format(topicRequestMail, "'" + topicName + "'");
        subject = "Create Topic Request";
      }
      case TOPIC_DELETE_REQUESTED -> {
        formattedStr = String.format(topicDeleteRequestMail, "'" + topicName + "'");
        subject = "Delete Topic Request";
      }
      case TOPIC_CLAIM_REQUESTED -> {
        formattedStr = String.format(topicClaimRequestMail, "'" + topicName + "'");
        subject = "Claim Topic Request";
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
      }
      case ACL_DELETE_REQUESTED -> {
        formattedStr = String.format(aclDeleteRequestMail, "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Delete Request";
      }
      case ACL_REQUEST_APPROVED -> {
        formattedStr = String.format(aclRequestApproved, "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Request Approved";
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
    }

    sendMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
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

    sendMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
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
    if (!Objects.equals(registerUserInfo.getMailid(), kwAdminMailId))
      sendMailToAdmin(subject, formattedStr, tenantId, loginUrl);

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
      if (!Objects.equals(registerUserInfo.getMailid(), kwAdminMailId))
        sendMailToAdmin(subject, formattedStr, tenantId, loginUrl);

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
    String formattedStr = String.format(reconMailContent, tenantName);

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
      String otherMailId,
      int tenantId,
      String loginUrl) {

    CompletableFuture.runAsync(
        () -> {
          String emailId;

          String emailIdTeam = null;
          try {
            if (registrationRequest) {
              emailId = otherMailId;
            } else {
              emailId = getEmailAddressFromUsername(username);
            }

            try {
              List<Team> allTeams = dbHandle.getAllTeamsOfUsers(username, tenantId);
              if (!allTeams.isEmpty()) emailIdTeam = allTeams.get(0).getTeammail();
            } catch (Exception e) {
              log.error("Exception :", e);
            }

            if (emailId != null) {
              emailService.sendSimpleMessage(
                  emailId, emailIdTeam, subject, formattedStr, tenantId, loginUrl);
            } else {
              log.error("Email id not found. Notification not sent !!");
            }
          } catch (Exception e) {
            log.error("Email id not found. Notification not sent !! ", e);
          }
        });
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
    if (user.isPresent()) {
      return user.get().getMailid();
    } else {
      return null;
    }
  }

  public String sendMailToSaasAdmin(int tenantId, String userName, String period, String loginUrl) {
    String mailtext =
        "Tenant extension : Tenant " + tenantId + " username " + userName + " period " + period;
    emailService.sendSimpleMessage(
        userName, kwAdminMailId, "Tenant Extension", mailtext, tenantId, loginUrl);
    return ApiResultStatus.SUCCESS.value;
  }
}
