package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.RegisterUserInfo;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.MailType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailUtils {

  @Value("${klaw.admin.mailid}")
  private String kwSaasAdminMailId;

  @Value("${spring.security.oauth2.client.provider.klaw.user-name-attribute:preferred_username}")
  private String preferredUsername;

  private static final String SUPERUSER_MAILID_KEY = "klaw.superuser.mailid";
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
  private static final String REGISTER_USER_KEY = "klaw.mail.registeruser.content";
  private static final String REGISTER_USER_SAAS_KEY = "klaw.mail.registeruser.saas.content";
  private static final String REGISTER_USER_TOUSER_KEY = "klaw.mail.registerusertouser.content";
  private static final String REGISTER_USER_SAAS_TOUSER_KEY =
      "klaw.mail.registerusertouser.saas.content";
  private static final String REGISTER_USER_SAASADMIN_TOUSER_KEY =
      "klaw.mail.registerusertouser.saasadmin.content";
  private static final String RECONCILIATION_TOPICS_KEY = "klaw.mail.recontopics.content";

  private String superUserMailId;
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
    if (principal instanceof DefaultOAuth2User) {
      DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) principal;
      return (String) defaultOAuth2User.getAttributes().get(preferredUsername);
    } else if (principal instanceof String) {
      return (String) principal;
    } else {
      return ((UserDetails) principal).getUsername();
    }
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
      case TOPIC_CREATE_REQUESTED:
        formattedStr = String.format(topicRequestMail, "'" + topicName + "'");
        subject = "Create Topic Request";
        break;
      case TOPIC_DELETE_REQUESTED:
        formattedStr = String.format(topicDeleteRequestMail, "'" + topicName + "'");
        subject = "Delete Topic Request";
        break;
      case TOPIC_CLAIM_REQUESTED:
        formattedStr = String.format(topicClaimRequestMail, "'" + topicName + "'");
        subject = "Claim Topic Request";
        break;
      case TOPIC_REQUEST_APPROVED:
        formattedStr = String.format(topicRequestApproved, "'" + topicName + "'");
        subject = "Topic Request Approved";
        break;
      case TOPIC_REQUEST_DENIED:
        formattedStr =
            String.format(topicRequestDenied, "'" + topicName + "'", "'" + reasonToDecline + "'");
        subject = "Topic Request Denied";
        break;
      case ACL_REQUESTED:
        formattedStr = String.format(aclRequestMail, "'" + acl + "'", "'" + topicName + "'");
        subject = "New Acl Request";
        break;
      case ACL_DELETE_REQUESTED:
        formattedStr = String.format(aclDeleteRequestMail, "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Delete Request";
        break;
      case ACL_REQUEST_APPROVED:
        formattedStr = String.format(aclRequestApproved, "'" + acl + "'", "'" + topicName + "'");
        subject = "Acl Request Approved";
        break;
      case ACL_REQUEST_DENIED:
        formattedStr =
            String.format(
                aclRequestDenied,
                "'" + acl + "'",
                "'" + topicName + "'",
                "'" + reasonToDecline + "'");
        subject = "Acl Request Denied";
        break;
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
      String username, String pwd, HandleDbRequests dbHandle, int tenantId, String loginUrl) {
    String formattedStr, subject;
    String passwordReset = manageDatabase.getKwPropertyValue(PWD_RESET_KEY, tenantId);
    formattedStr = String.format(passwordReset, username, pwd);
    subject = "Klaw Access - Password reset requested";

    sendMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
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

    subject = "New User Registration request";
    if (!Objects.equals(
        registerUserInfo.getMailid(),
        manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId)))
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

    subject = "Klaw User Registration request";
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

      subject = "New User Registration request";
      if (!Objects.equals(
          registerUserInfo.getMailid(),
          manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId)))
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

      subject = "Klaw Registration request";
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
    this.superUserMailId = manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId);
    String reconMailContent =
        manageDatabase.getKwPropertyValue(RECONCILIATION_TOPICS_KEY, tenantId);
    String formattedStr = String.format(reconMailContent, tenantName);

    try {
      CompletableFuture.runAsync(
              () -> {
                if (superUserMailId != null) {
                  emailService.sendSimpleMessage(
                      superUserMailId, null, subject, formattedStr, tenantId, loginUrl);
                }
              })
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Exception:", e);
    }
  }

  public void sendMailToAdmin(String subject, String mailContent, int tenantId, String loginUrl) {
    this.superUserMailId = manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId);
    CompletableFuture.runAsync(
        () -> {
          if (superUserMailId != null) {
            emailService.sendSimpleMessage(
                superUserMailId, null, subject, mailContent, tenantId, loginUrl);
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
    this.superUserMailId = manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId);

    CompletableFuture.runAsync(
        () -> {
          String emailId;

          String emailIdTeam;
          try {
            if (registrationRequest) emailId = otherMailId;
            else emailId = dbHandle.getUsersInfo(username).getMailid();

            try {
              emailIdTeam = dbHandle.selectAllTeamsOfUsers(username, tenantId).get(0).getTeammail();
            } catch (Exception e) {
              log.error("Exception:", e);
              emailIdTeam = null;
            }

            if (emailId != null) {
              emailService.sendSimpleMessage(
                  emailId, emailIdTeam, subject, formattedStr, tenantId, loginUrl);
            } else log.error("Email id not found. Notification not sent !!");
          } catch (Exception e) {
            log.error("Email id not found. Notification not sent !! ", e);
          }
        });
  }

  public String getEnvProperty(Integer tenantId, String envPropertyType) {
    try {
      KwTenantConfigModel tenantModel = manageDatabase.getTenantConfig().get(tenantId);
      if (tenantModel == null) {
        return "";
      }
      List<Integer> intOrderEnvsList = new ArrayList<>();

      switch (envPropertyType) {
        case "ORDER_OF_ENVS":
          tenantModel
              .getOrderOfTopicPromotionEnvsList()
              .forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          break;
        case "REQUEST_TOPICS_OF_ENVS":
          tenantModel
              .getRequestTopicsEnvironmentsList()
              .forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          break;
        case "ORDER_OF_KAFKA_CONNECT_ENVS":
          tenantModel
              .getOrderOfConnectorsPromotionEnvsList()
              .forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          break;
        case "REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS":
          tenantModel
              .getRequestConnectorsEnvironmentsList()
              .forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          break;
      }

      return intOrderEnvsList.stream().map(String::valueOf).collect(Collectors.joining(","));
    } catch (Exception e) {
      log.error("Exception:", e);
      return "";
    }
  }

  public String sendMailToSaasAdmin(int tenantId, String userName, String period, String loginUrl) {
    String mailtext =
        "Tenant extension : Tenant " + tenantId + " username " + userName + " period " + period;
    emailService.sendSimpleMessage(
        userName, kwSaasAdminMailId, "Tenant Extension", mailtext, tenantId, loginUrl);
    return "success";
  }
}
