package io.aiven.klaw.service;

import java.util.Arrays;
import java.util.List;

public class KwConstants {
  public static final List<String> allowConfigForAdmins =
      Arrays.asList(
          "klaw.adduser.roles",
          "klaw.superuser.mailid",
          "klaw.tenant.config",
          "klaw.envs.standardnames",
          "klaw.broadcast.text",
          "klaw.getschemas.enable");

  public static final int DEFAULT_TENANT_ID = 101;
  public static final String TENANT_CONFIG_PROPERTY = "klaw.tenant.config";
  public static final String broadCastTextProperty = "klaw.broadcast.text";
  public static final String RETRIEVE_SCHEMAS_KEY = "klaw.getschemas.enable";
  public static final String KW_REPORTS_TMP_LOCATION_KEY = "klaw.reports.location";
  public static final String CLUSTER_CONN_URL_KEY = "klaw.clusterapi.url";
  public static final String EMAIL_NOTIFICATIONS_ENABLED_KEY = "klaw.mail.notifications.enable";

  public static final String USER_ROLE = "USER";
  public static final String SUPERADMIN_ROLE = "SUPERADMIN";

  public static final String INFRATEAM = "INFRATEAM";
  public static final String STAGINGTEAM = "STAGINGTEAM";

  public static final String INVALID_KEY_MESSAGE =
      "Invalid License !! Please request from https://klaw-project.io for a license key.";
  public static final String MAIL_TOPICREQUEST_CONTENT =
      "Dear User, \\nA request for a create topic %s has been requested in Klaw.";
  public static final String MAIL_TOPICDELETEREQUEST_CONTENT =
      "Dear User, \\nA request for a delete topic %s has been requested in Klaw.";
  public static final String MAIL_TOPICCLAIMREQUEST_CONTENT =
      "Dear User, \\nA request for a clam topic %s has been requested in Klaw.";
  public static final String MAIL_TOPICREQUESTAPPROVAL_CONTENT =
      "Dear User, \\nA request for a new topic %s has been approved.";
  public static final String MAIL_TOPICREQUESTDENIAL_CONTENT =
      "Dear User, \\nA request for a new topic %s has been declined.\\nReason : %s";
  public static final String MAIL_ACLREQUEST_CONTENT =
      "Dear User, \\nA request for a new acl %s for topic %s has been requested in Klaw.";
  public static final String MAIL_ACLREQUESTDELETE_CONTENT =
      "Dear User, \\nA request for a Delete acl %s for topic %s has been requested in Klaw.";
  public static final String MAIL_ACLREQUESTAPPROVAL_CONTENT =
      "Dear User, \\nA request for a new acl %s for topic %s has been approved.";
  public static final String MAIL_ACLREQUESTDENIAL_CONTENT =
      "Dear User, \\nA request for a new acl %s for topic %s has been declined.\\nReason : %s";
  public static final String MAIL_REGISTERUSER_CONTENT =
      "Dear Super User, \\nYou have a new pending user registration request from : \\n\\nUser name : %s \\n\\nName : %s \\n\\nTeam : %s\\n\\nRole : %s";
  public static final String MAIL_REGISTERUSER_SAAS_CONTENT =
      "Dear Super User, \\nThere is a new user registration request from : \\n\\nUser name : %s \\n\\nName : %s ";
  public static final String MAIL_REGISTERUSERTOUSER_CONTENT =
      "Dear User, \\nThankyou for registering in Klaw. Below here are your details. \\n\\nUser name : %s \\nName : %s \\nTeam : %s\\nRole : %s \\n\\n Account pending Activation.";
  public static final String MAIL_REGISTERUSERTOUSER_SAAS_CONTENT =
      "Dear User, \\nThankyou for registering in Klaw. Below here are your registration details. \\n\\nUser name : %s \\nPassword : %s \\nName : %s \\nTenant : %s\\nTeam : %s\\nRole : %s \\n\\n  Account pending Activation.";
  public static final String MAIL_REGISTERUSERTOUSER_SAAS_ADMIN_CONTENT =
      "Dear User, \\nThankyou for registering in Klaw. Below here are your registration details. \\n\\nUser name : %s \\nPassword : %s \\nName : %s \\nTeam : %s\\nRole : %s \\n\\n ACTIVATE : Please click %s to activate.";
  public static final String MAIL_RECONTOPICS_CONTENT =
      "Dear User, \\n  Below are the topics which need to be reconciled. \\n\\n Tenant : %s \\n\\n Topics : \\n\\n %s";
  public static final String MAIL_NEWUSERADDED_CONTENT =
      "Dear User, \\nYou have been given access to Klaw. \\n\\nUser name : %s \\nPassword : %s";
  public static final String MAIL_PASSWORDRESET_CONTENT =
      "Dear User, \\nYou have requested for reset password on your Klaw account. \\n\\nUser name : %s \\nYour new Password : %s ";

  public static final String REPORTS_LOCATION = "/tmp/";
  public static final String MAIL_NOTIFICATIONS_ENABLE = "true";
  public static final String GETSCHEMAS_ENABLE = "false";
  public static final String CLUSTERAPI_URL = "http://localhost:9343"; //
  public static final String TENANT_CONFIG = "{}";
  public static final String ADDUSER_ROLES = "USER";
  public static final String ENVS_STANDARDNAMES =
      "DEV,TST,SIT,STG,QAE,ACC,E2E,IOE,DRE,PEE,PRD,PRE,UAT";
  public static final String MAIL_PROTOCOL = "smtp";
  public static final String MAIL_HOST = "smtphost";
  public static final String MAIL_PORT = "22";
  public static final String MAIL_USERNAME = "username";
  public static final String MAIL_PASSWORD = "password";
  public static final String MAIL_SMTP_AUTH = "true";
  public static final String MAIL_SMTP_TLS = "true";
  public static final String MAIL_DEBUG = "false";

  public static final int DAYS_EXPIRY_DEFAULT_TENANT = 365 * 10;
  public static final int DAYS_TRIAL_PERIOD = 7;
}
