package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.RegisterUserInfo;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.KwTenantConfigModel;
import com.kafkamgt.uiapi.model.MailType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.kafkamgt.uiapi.service.KwConstants.INFRATEAM;

@Service
@Slf4j
public class MailUtils {

    @Value("${kafkawize.admin.mailid}")
    private
    String kwSaasAdminMailId;

    @Value("${spring.security.oauth2.client.provider.kafkawize.user-name-attribute:preferred_username}")
    private
    String preferredUsername;

    private static final String SUPERUSER_MAILID_KEY = "kafkawize.superuser.mailid";
    private static final String TOPIC_REQ_KEY = "kafkawize.mail.topicrequest.content";
    private static final String TOPIC_REQ_DEL_KEY = "kafkawize.mail.topicdeleterequest.content";
    private static final String TOPIC_REQ_CLAIM_KEY = "kafkawize.mail.topicclaimrequest.content";
    private static final String TOPIC_REQ_APPRVL_KEY = "kafkawize.mail.topicrequestapproval.content";
    private static final String TOPIC_REQ_DENY_KEY = "kafkawize.mail.topicrequestdenial.content";
    private static final String ACL_REQ_KEY = "kafkawize.mail.aclrequest.content";
    private static final String ACL_DELETE_REQ_KEY = "kafkawize.mail.aclrequestdelete.content";
    private static final String ACL_REQ_APPRVL_KEY = "kafkawize.mail.aclrequestapproval.content";
    private static final String ACL_REQ_DENY_KEY = "kafkawize.mail.aclrequestdenial.content";
    private static final String NEW_USER_ADDED_KEY = "kafkawize.mail.newuseradded.content";
    private static final String PWD_RESET_KEY = "kafkawize.mail.passwordreset.content";
    private static final String REGISTER_USER_KEY = "kafkawize.mail.registeruser.content";
    private static final String REGISTER_USER_SAAS_KEY = "kafkawize.mail.registeruser.saas.content";
    private static final String REGISTER_USER_TOUSER_KEY = "kafkawize.mail.registerusertouser.content";
    private static final String REGISTER_USER_SAAS_TOUSER_KEY = "kafkawize.mail.registerusertouser.saas.content";
    private static final String REGISTER_USER_SAASADMIN_TOUSER_KEY = "kafkawize.mail.registerusertouser.saasadmin.content";
    private static final String RECONCILIATION_TOPICS_KEY = "kafkawize.mail.recontopics.content";

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

    private void loadKwProps(int tenantId){
        this.topicRequestMail = manageDatabase.getKwPropertyValue(TOPIC_REQ_KEY ,tenantId);
        this.topicDeleteRequestMail = manageDatabase.getKwPropertyValue(TOPIC_REQ_DEL_KEY ,tenantId);
        this.topicClaimRequestMail = manageDatabase.getKwPropertyValue(TOPIC_REQ_CLAIM_KEY ,tenantId);
        this.topicRequestApproved = manageDatabase.getKwPropertyValue(TOPIC_REQ_APPRVL_KEY ,tenantId);
        this.topicRequestDenied = manageDatabase.getKwPropertyValue(TOPIC_REQ_DENY_KEY ,tenantId);
        this.aclRequestMail = manageDatabase.getKwPropertyValue(ACL_REQ_KEY ,tenantId);
        this.aclDeleteRequestMail = manageDatabase.getKwPropertyValue(ACL_DELETE_REQ_KEY ,tenantId);
        this.aclRequestApproved = manageDatabase.getKwPropertyValue(ACL_REQ_APPRVL_KEY ,tenantId);
        this.aclRequestDenied = manageDatabase.getKwPropertyValue(ACL_REQ_DENY_KEY ,tenantId);
    }

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    private EmailService emailService;

    public String getUserName(Object principal){
        if(principal instanceof DefaultOAuth2User){
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User)principal;
            return (String)defaultOAuth2User.getAttributes().get(preferredUsername);
        }
        else if(principal instanceof String){
            return (String)principal;
        }else{
            return ((UserDetails) principal).getUsername();
        }
    }

    void sendMail(String topicName, String acl, String reasonToDecline, String username, HandleDbRequests dbHandle, MailType mailType,
                  String loginUrl) {
        String formattedStr = null, subject = null;
        int tenantId = manageDatabase.getHandleDbRequests()
                .getUsersInfo(getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
                .getTenantId();
        loadKwProps(tenantId);

        switch(mailType) {
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
                formattedStr = String.format(topicRequestDenied, "'" + topicName + "'", "'" + reasonToDecline + "'");
                subject = "Topic Request Denied";
                break;
            case ACL_REQUESTED:
                formattedStr = String.format(aclRequestMail, "'" +acl + "'", "'" + topicName + "'");
                subject = "New Acl Request";
                break;
            case ACL_DELETE_REQUESTED:
                formattedStr = String.format(aclDeleteRequestMail, "'" +acl + "'", "'" + topicName + "'");
                subject = "Acl Delete Request";
                break;
            case ACL_REQUEST_APPROVED:
                formattedStr = String.format(aclRequestApproved, "'" +acl + "'", "'" + topicName + "'");
                subject = "Acl Request Approved";
                break;
            case ACL_REQUEST_DENIED:
                formattedStr = String.format(aclRequestDenied, "'" +acl + "'", "'" + topicName + "'", "'" + reasonToDecline + "'");
                subject = "Acl Request Denied";
                break;
        }

        sendMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
    }

    void sendMail(String username, String pwd, HandleDbRequests dbHandle, String loginUrl) {
        String formattedStr, subject;
        int tenantId = manageDatabase.getHandleDbRequests()
                .getUsersInfo(getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal()))
                .getTenantId();
        String newUserAdded = manageDatabase.getKwPropertyValue(NEW_USER_ADDED_KEY, tenantId);
        formattedStr = String.format(newUserAdded, username, pwd);
        subject = "Access to Kafkawize";

        sendMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
    }

    void sendMailResetPwd(String username, String pwd, HandleDbRequests dbHandle, int tenantId, String loginUrl) {
        String formattedStr, subject;
        String passwordReset = manageDatabase.getKwPropertyValue(PWD_RESET_KEY, tenantId);
        formattedStr = String.format(passwordReset, username, pwd);
        subject = "Kafkawize Access - Password reset requested";

        sendMail(username, dbHandle, formattedStr, subject, false, null, tenantId, loginUrl);
    }

    void sendMailRegisteredUserSaas(RegisterUserInfo registerUserInfo, HandleDbRequests dbHandle, String tenantName,
                                    int tenantId, String teamName, String activationUrl, String loginUrl) {
        String formattedStr , subject ;
        // sending to super admin
        String registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_SAAS_KEY, tenantId);
        formattedStr = String.format(registrationRequest, registerUserInfo.getUsername(),registerUserInfo.getFullname());

        subject = "New User Registration request";
        if(!registerUserInfo.getMailid().equals(manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId)))
            sendMailToAdmin(subject, formattedStr, tenantId, loginUrl);

        // Sending to user
        if(registerUserInfo.getTeam().equals(INFRATEAM)) {
            registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_SAASADMIN_TOUSER_KEY, tenantId);
            formattedStr = String.format(registrationRequest, registerUserInfo.getUsername(), registerUserInfo.getPwd(),
                    registerUserInfo.getFullname(), teamName, registerUserInfo.getRole(), activationUrl);
        }
        else {
            registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_SAAS_TOUSER_KEY, tenantId);
            formattedStr = String.format(registrationRequest, registerUserInfo.getUsername(), registerUserInfo.getPwd(),
                    registerUserInfo.getFullname(), tenantName, teamName, registerUserInfo.getRole());
        }

        subject = "Kafkawize User Registration request";
        sendMail(registerUserInfo.getUsername(), dbHandle, formattedStr, subject, true, registerUserInfo.getMailid(),
                tenantId, loginUrl);
    }

    void sendMailRegisteredUser(RegisterUserInfo registerUserInfo, HandleDbRequests dbHandle, String loginUrl) {
        try {
            String formattedStr , subject ;
            int tenantId = registerUserInfo.getTenantId();
            // sending to super admin
            String registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_KEY, tenantId);
            formattedStr = String.format(registrationRequest, registerUserInfo.getUsername(),registerUserInfo.getFullname()
            , registerUserInfo.getTeam(), registerUserInfo.getRole());

            subject = "New User Registration request";
            if(!registerUserInfo.getMailid().equals(manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId)))
                sendMailToAdmin(subject, formattedStr, tenantId, loginUrl);

            // Sending to user
            registrationRequest = manageDatabase.getKwPropertyValue(REGISTER_USER_TOUSER_KEY, tenantId);
            formattedStr = String.format(registrationRequest, registerUserInfo.getUsername(),registerUserInfo.getFullname()
                    ,registerUserInfo.getTeam(), registerUserInfo.getRole());

            subject = "Kafkawize Registration request";
            sendMail(registerUserInfo.getUsername(), dbHandle, formattedStr, subject, true, registerUserInfo.getMailid(), tenantId, loginUrl);
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error(registerUserInfo.toString());
        }
    }

    public void sendReconMailToAdmin(String subject, String reconTopicsContent, String tenantName, int tenantId, String loginUrl){
        this.superUserMailId = manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId);
        String reconMailContent = manageDatabase.getKwPropertyValue(RECONCILIATION_TOPICS_KEY, tenantId);
        String formattedStr = String.format(reconMailContent, tenantName);

        try {
            CompletableFuture.runAsync(() -> {
                if (superUserMailId != null) {
                    emailService.sendSimpleMessage(superUserMailId, null, subject, formattedStr, tenantId, loginUrl);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void sendMailToAdmin(String subject, String mailContent, int tenantId, String loginUrl){
        this.superUserMailId = manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId);
        CompletableFuture.runAsync(() -> {
            if (superUserMailId != null) {
                emailService.sendSimpleMessage(superUserMailId, null, subject, mailContent, tenantId, loginUrl);
            }
        });
    }

    private void sendMail(String username, HandleDbRequests dbHandle, String formattedStr, String subject,
                          boolean registrationRequest, String otherMailId, int tenantId, String loginUrl) {
        this.superUserMailId = manageDatabase.getKwPropertyValue(SUPERUSER_MAILID_KEY, tenantId);

        CompletableFuture.runAsync(() -> {
            String emailId;

            String emailIdTeam;
            try {
                if(registrationRequest)
                    emailId = otherMailId;
                else
                    emailId = dbHandle.getUsersInfo(username).getMailid();

                try {
                    emailIdTeam = dbHandle.selectAllTeamsOfUsers(username, tenantId).get(0).getTeammail();
                } catch (Exception exception) {
                    emailIdTeam = null;
                }

                if(emailId != null){
                    emailService.sendSimpleMessage(emailId, emailIdTeam, subject, formattedStr, tenantId, loginUrl);
                }
                else
                    log.error("Email id not found. Notification not sent !!");
            }catch (Exception e){
                log.error("Email id not found. Notification not sent !! {}", e.getMessage());
            }

        });
    }

    public String getEnvProperty(Integer tenantId, String envPropertyType){
        try {
            KwTenantConfigModel tenantModel = manageDatabase.getTenantConfig()
                    .get(tenantId);
            List<Integer> intOrderEnvsList = new ArrayList<>();

            switch (envPropertyType) {
                case "ORDER_OF_ENVS":
                    tenantModel.getOrderOfTopicPromotionEnvsList().forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
                    break;
                case "REQUEST_TOPICS_OF_ENVS":
                    tenantModel.getRequestTopicsEnvironmentsList().forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
                    break;
                case "ORDER_OF_KAFKA_CONNECT_ENVS":
                    tenantModel.getOrderOfConnectorsPromotionEnvsList().forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
                    break;
                case "REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS":
                    tenantModel.getRequestConnectorsEnvironmentsList().forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
                    break;
            }

            return intOrderEnvsList.stream().map(String::valueOf)
                    .collect(Collectors.joining(","));
        } catch (Exception e){
            return "";
        }
    }

    public String sendMailToSaasAdmin(int tenantId, String userName, String period, String loginUrl) {
        String mailtext = "Tenant extension : Tenant " + tenantId + " username " + userName + " period " + period;
        emailService.sendSimpleMessage(userName, kwSaasAdminMailId, "Tenant Extension", mailtext, tenantId, loginUrl);
        return "success";
    }
}
