package io.aiven.klaw.service;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.EnvParams;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DefaultDataService {

  @Value("${klaw.quickstart.enabled:false}")
  private boolean quickStartEnabled;

  @Value("${klaw.docker.clusterapi.host:host.docker.internal:9343}")
  private String dockerClusterApiHost;

  @Value("${klaw.docker.kafka.cluster:host.docker.internal:9092}")
  private String dockerKafkaHost;

  @Value("${klaw.docker.sr.cluster:host.docker.internal:8081}")
  private String dockerSRHost;

  @Value("${klaw.quickstart.default.user1:william}")
  private String quickStartUser1;

  @Value("${klaw.quickstart.default.user2:jennifer}")
  private String quickStartUser2;

  @Value("${klaw.quickstart.default.pwd:welcome}")
  private String quickStartUserPwd;

  public UserInfo getUser(
      int tenantId,
      String password,
      String role,
      Integer teamId,
      String mailId,
      String userName,
      String secretKey) {
    UserInfo userInfo = new UserInfo();
    userInfo.setPwd(getJasyptEncryptor(secretKey).encrypt(password));
    userInfo.setRole(role);
    userInfo.setTeamId(teamId);
    userInfo.setMailid(mailId);
    userInfo.setUsername(userName);
    userInfo.setTenantId(tenantId);
    userInfo.setFullname("Super Admin");

    return userInfo;
  }

  private BasicTextEncryptor getJasyptEncryptor(String secretKey) {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPasswordCharArray(secretKey.toCharArray());

    return textEncryptor;
  }

  public Team getTeam(int tenantId, String teamName) {
    Team team = new Team();
    team.setTenantId(tenantId);
    team.setTeamname(teamName);
    team.setContactperson("Kw Admin");
    return team;
  }

  public KwTenants getDefaultTenant(int tenantId) {
    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantId(tenantId);
    kwTenants.setTenantName("default");
    kwTenants.setTenantDesc("default");
    kwTenants.setInTrial("false");
    kwTenants.setContactPerson("Klaw Administrator");
    kwTenants.setOrgName("Default Organization");
    kwTenants.setLicenseExpiry(
        new Timestamp(
            System.currentTimeMillis()
                + TimeUnit.DAYS.toMillis(KwConstants.DAYS_EXPIRY_DEFAULT_TENANT)));
    kwTenants.setIsActive("true");

    return kwTenants;
  }

  public List<KwProperties> createDefaultProperties(int tenantId, String mailId) {
    List<KwProperties> kwPropertiesList = new ArrayList<>();

    KwProperties kwProperties1 =
        new KwProperties(
            "klaw.mail.topicrequest.content",
            tenantId,
            KwConstants.MAIL_TOPICREQUEST_CONTENT,
            "Email notification body for a new Topic request");
    kwPropertiesList.add(kwProperties1);

    KwProperties kwProperties2 =
        new KwProperties(
            "klaw.mail.topicdeleterequest.content",
            tenantId,
            KwConstants.MAIL_TOPICDELETEREQUEST_CONTENT,
            "Email notification body for Topic delete request");
    kwPropertiesList.add(kwProperties2);

    KwProperties kwProperties3 =
        new KwProperties(
            "klaw.mail.topicclaimrequest.content",
            tenantId,
            KwConstants.MAIL_TOPICCLAIMREQUEST_CONTENT,
            "Email notification body for a new claim request");
    kwPropertiesList.add(kwProperties3);

    KwProperties kwProperties4 =
        new KwProperties(
            "klaw.mail.topicrequestapproval.content",
            tenantId,
            KwConstants.MAIL_TOPICREQUESTAPPROVAL_CONTENT,
            "Email notification body for Topic request approval");
    kwPropertiesList.add(kwProperties4);

    KwProperties kwProperties5 =
        new KwProperties(
            "klaw.mail.topicrequestdenial.content",
            tenantId,
            KwConstants.MAIL_TOPICREQUESTDENIAL_CONTENT,
            "Email notification body for Topic request decline");
    kwPropertiesList.add(kwProperties5);

    KwProperties kwProperties6 =
        new KwProperties(
            "klaw.mail.aclrequest.content",
            tenantId,
            KwConstants.MAIL_ACLREQUEST_CONTENT,
            "Email notification body for new Acl request");
    kwPropertiesList.add(kwProperties6);

    KwProperties kwProperties7 =
        new KwProperties(
            "klaw.mail.aclrequestdelete.content",
            tenantId,
            KwConstants.MAIL_ACLREQUESTDELETE_CONTENT,
            "Email notification body for Acl delete request");
    kwPropertiesList.add(kwProperties7);

    KwProperties kwProperties8 =
        new KwProperties(
            "klaw.mail.aclrequestapproval.content",
            tenantId,
            KwConstants.MAIL_ACLREQUESTAPPROVAL_CONTENT,
            "Email notification body for Acl request approval");
    kwPropertiesList.add(kwProperties8);

    KwProperties kwProperties9 =
        new KwProperties(
            "klaw.mail.aclrequestdenial.content",
            tenantId,
            KwConstants.MAIL_ACLREQUESTDENIAL_CONTENT,
            "Email notification body for Acl request decline");
    kwPropertiesList.add(kwProperties9);

    KwProperties kwProperties10 =
        new KwProperties(
            "klaw.mail.registeruser.content",
            tenantId,
            KwConstants.MAIL_REGISTERUSER_CONTENT,
            "Email notification body for new user request to be approved.");
    kwPropertiesList.add(kwProperties10);

    KwProperties kwProperties11 =
        new KwProperties(
            "klaw.mail.registeruser.saas.content",
            tenantId,
            KwConstants.MAIL_REGISTERUSER_SAAS_CONTENT,
            "Email notification body for new user request");
    kwPropertiesList.add(kwProperties11);

    KwProperties kwProperties12 =
        new KwProperties(
            "klaw.mail.registerusertouser.content",
            tenantId,
            KwConstants.MAIL_REGISTERUSERTOUSER_CONTENT,
            "Email notification body for new user request to actual user.");
    kwPropertiesList.add(kwProperties12);

    KwProperties kwProperties13 =
        new KwProperties(
            "klaw.mail.registerusertouser.saas.content",
            tenantId,
            KwConstants.MAIL_REGISTERUSERTOUSER_SAAS_CONTENT,
            "Email notification body for new SaaS user request to actual user.");
    kwPropertiesList.add(kwProperties13);

    KwProperties kwProperties14 =
        new KwProperties(
            "klaw.mail.recontopics.content",
            tenantId,
            KwConstants.MAIL_RECONTOPICS_CONTENT,
            "Email notification body for reconciliation of topics.");
    kwPropertiesList.add(kwProperties14);

    KwProperties kwProperties15 =
        new KwProperties(
            "klaw.mail.newuseradded.content",
            tenantId,
            KwConstants.MAIL_NEWUSERADDED_CONTENT,
            "Email notification body after a new user is added");
    kwPropertiesList.add(kwProperties15);

    KwProperties kwProperties16 =
        new KwProperties(
            "klaw.mail.passwordreset.content",
            tenantId,
            KwConstants.MAIL_PASSWORDRESET_CONTENT,
            "Email notification body for password reset");
    kwPropertiesList.add(kwProperties16);

    KwProperties kwProperties18 =
        new KwProperties(
            "klaw.reports.location",
            tenantId,
            KwConstants.REPORTS_LOCATION,
            "Temporary location of reports");
    kwPropertiesList.add(kwProperties18);

    KwProperties kwProperties20 =
        new KwProperties(
            "klaw.getschemas.enable",
            tenantId,
            KwConstants.GETSCHEMAS_ENABLE,
            "Enable View or retrieve schemas");
    kwPropertiesList.add(kwProperties20);

    handleQuickStartKwProperties(tenantId, kwPropertiesList);

    KwProperties kwProperties23 =
        new KwProperties(
            "klaw.adduser.roles",
            tenantId,
            KwConstants.ADDUSER_ROLES,
            "Allowed roles when adding a new user.");
    kwPropertiesList.add(kwProperties23);

    KwProperties kwProperties24 =
        new KwProperties(
            "klaw.envs.standardnames",
            tenantId,
            KwConstants.ENVS_STANDARDNAMES,
            "Standard names of environments");
    kwPropertiesList.add(kwProperties24);

    KwProperties kwProperties19 =
        new KwProperties(
            "klaw.mail.notifications.enable",
            tenantId,
            KwConstants.MAIL_NOTIFICATIONS_ENABLE,
            "Smtp Config Enable email notifications");
    kwPropertiesList.add(kwProperties19);

    KwProperties kwProperties33 =
        new KwProperties(
            "klaw.broadcast.text", tenantId, "", "Broadcast text to all your tenant users");
    kwPropertiesList.add(kwProperties33);

    KwProperties kwProperties34 =
        new KwProperties(
            "klaw.mail.registerusertouser.saasadmin.content",
            tenantId,
            KwConstants.MAIL_REGISTERUSERTOUSER_SAAS_ADMIN_CONTENT,
            "Email notification body for new SaaS user request to actual user.");
    kwPropertiesList.add(kwProperties34);
    KwProperties kwProperties35 =
        new KwProperties(
            "klaw.mail.passwordchanged.content",
            tenantId,
            KwConstants.MAIL_PASSWORDCHANGED_CONTENT,
            "Email notification body for password update.");
    kwPropertiesList.add(kwProperties35);

    KwProperties kwProperties36 =
        new KwProperties(
            "klaw.mail.topicpromotionrequest.content",
            tenantId,
            KwConstants.MAIL_TOPICPROMOTIONREQUEST_CONTENT,
            "Email notification body for a new Topic Promotion Request");
    kwPropertiesList.add(kwProperties36);

    KwProperties kwProperties37 =
        new KwProperties(
            "klaw.mail.topicupdaterequest.content",
            tenantId,
            KwConstants.MAIL_TOPICUPDATEREQUEST_CONTENT,
            "Email notification body for a new Topic Update Request");
    kwPropertiesList.add(kwProperties37);
    return kwPropertiesList;
  }

  private void handleQuickStartKwProperties(int tenantId, List<KwProperties> kwPropertiesList) {
    KwProperties kwProperties21;
    KwProperties kwProperties22;
    if (quickStartEnabled) {
      kwProperties21 =
          new KwProperties(
              "klaw.clusterapi.url", tenantId, dockerClusterApiHost, "Cluster Api URL");
      kwProperties22 =
          new KwProperties(
              "klaw.tenant.config",
              tenantId,
              KwConstants.TENANT_CONFIG_QUICK_START,
              "Base sync cluster, order of topic promotion environments, topic request envs");
    } else {
      kwProperties21 =
          new KwProperties(
              "klaw.clusterapi.url", tenantId, KwConstants.CLUSTERAPI_URL, "Cluster Api URL");
      kwProperties22 =
          new KwProperties(
              "klaw.tenant.config",
              tenantId,
              KwConstants.TENANT_CONFIG,
              "Base sync cluster, order of topic promotion environments, topic request envs");
    }
    kwPropertiesList.add(kwProperties21);
    kwPropertiesList.add(kwProperties22);
  }

  public List<KwRolesPermissions> createDefaultRolesPermissions(
      int tenantId, boolean isSuperAdmin, String kwInstallationType) {
    Map<String, String> defaultUserPermissionsList = getDefaultUserPermissionsList();
    Map<String, String> defaultAdminPermissionsList = getDefaultAdminPermissionsList();
    Map<String, String> superAdminPermissionsList = getDefaultSuperAdminPermissionsList();

    if (!"saas".equals(kwInstallationType)) // on premise
    {
      defaultAdminPermissionsList.put("ADD_TENANT", PermissionType.ADD_TENANT.getDescription());
    }

    List<KwRolesPermissions> kwRolesPermissionsList = new ArrayList<>();
    for (Map.Entry<String, String> rolePermEntry : defaultUserPermissionsList.entrySet()) {
      KwRolesPermissions kwRolesPermissions =
          new KwRolesPermissions(
              0, tenantId, KwConstants.USER_ROLE, rolePermEntry.getKey(), rolePermEntry.getValue());
      kwRolesPermissionsList.add(kwRolesPermissions);
    }
    for (Map.Entry<String, String> rolePermEntry : defaultAdminPermissionsList.entrySet()) {
      KwRolesPermissions kwRolesPermissions =
          new KwRolesPermissions(
              0,
              tenantId,
              KwConstants.SUPERADMIN_ROLE,
              rolePermEntry.getKey(),
              rolePermEntry.getValue());
      kwRolesPermissionsList.add(kwRolesPermissions);
    }
    if (isSuperAdmin) {
      for (Map.Entry<String, String> rolePermEntry : superAdminPermissionsList.entrySet()) {
        KwRolesPermissions kwRolesPermissions =
            new KwRolesPermissions(
                0,
                tenantId,
                KwConstants.SUPERADMIN_ROLE,
                rolePermEntry.getKey(),
                rolePermEntry.getValue());
        kwRolesPermissionsList.add(kwRolesPermissions);
      }
    }

    return kwRolesPermissionsList;
  }

  private static Map<String, String> getDefaultUserPermissionsList() {
    Map<String, String> defaultUserPermissionsList = new HashMap<>();

    defaultUserPermissionsList.put(
        "REQUEST_CREATE_TOPICS", PermissionType.REQUEST_CREATE_TOPICS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_CREATE_SUBSCRIPTIONS",
        PermissionType.REQUEST_CREATE_SUBSCRIPTIONS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_DELETE_TOPICS", PermissionType.REQUEST_DELETE_TOPICS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_EDIT_TOPICS", PermissionType.REQUEST_EDIT_TOPICS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_DELETE_SUBSCRIPTIONS",
        PermissionType.REQUEST_DELETE_SUBSCRIPTIONS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_CREATE_SCHEMAS", PermissionType.REQUEST_CREATE_SCHEMAS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_DELETE_SCHEMAS", PermissionType.REQUEST_DELETE_SCHEMAS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_CREATE_CONNECTORS", PermissionType.REQUEST_CREATE_CONNECTORS.getDescription());
    defaultUserPermissionsList.put(
        "REQUEST_DELETE_CONNECTORS", PermissionType.REQUEST_DELETE_CONNECTORS.getDescription());
    defaultUserPermissionsList.put(
        "APPROVE_TOPICS", PermissionType.APPROVE_TOPICS.getDescription());
    defaultUserPermissionsList.put(
        "APPROVE_SUBSCRIPTIONS", PermissionType.APPROVE_SUBSCRIPTIONS.getDescription());
    defaultUserPermissionsList.put(
        "APPROVE_SCHEMAS", PermissionType.APPROVE_SCHEMAS.getDescription());
    defaultUserPermissionsList.put(
        "APPROVE_CONNECTORS", PermissionType.APPROVE_CONNECTORS.getDescription());
    defaultUserPermissionsList.put(
        "VIEW_CONNECTORS", PermissionType.VIEW_CONNECTORS.getDescription());
    defaultUserPermissionsList.put("VIEW_TOPICS", PermissionType.VIEW_TOPICS.getDescription());

    return defaultUserPermissionsList;
  }

  private static Map<String, String> getDefaultAdminPermissionsList() {
    Map<String, String> defaultAdminPermissionsList = new HashMap<>();

    defaultAdminPermissionsList.put(
        "ADD_EDIT_DELETE_TEAMS", PermissionType.ADD_EDIT_DELETE_TEAMS.getDescription());
    defaultAdminPermissionsList.put(
        "ADD_EDIT_DELETE_USERS", PermissionType.ADD_EDIT_DELETE_USERS.getDescription());
    defaultAdminPermissionsList.put(
        "ADD_EDIT_DELETE_CLUSTERS", PermissionType.ADD_EDIT_DELETE_CLUSTERS.getDescription());
    defaultAdminPermissionsList.put(
        "ADD_EDIT_DELETE_ENVS", PermissionType.ADD_EDIT_DELETE_ENVS.getDescription());
    defaultAdminPermissionsList.put(
        "ADD_EDIT_DELETE_ROLES", PermissionType.ADD_EDIT_DELETE_ROLES.getDescription());

    defaultAdminPermissionsList.put("SYNC_TOPICS", PermissionType.SYNC_TOPICS.getDescription());
    defaultAdminPermissionsList.put("SYNC_SCHEMAS", PermissionType.SYNC_SCHEMAS.getDescription());
    defaultAdminPermissionsList.put(
        "SYNC_SUBSCRIPTIONS", PermissionType.SYNC_SUBSCRIPTIONS.getDescription());
    defaultAdminPermissionsList.put(
        "SYNC_BACK_TOPICS", PermissionType.SYNC_BACK_TOPICS.getDescription());
    defaultAdminPermissionsList.put(
        "SYNC_BACK_SCHEMAS", PermissionType.SYNC_BACK_SCHEMAS.getDescription());
    defaultAdminPermissionsList.put(
        "SYNC_BACK_SUBSCRIPTIONS", PermissionType.SYNC_BACK_SUBSCRIPTIONS.getDescription());
    defaultAdminPermissionsList.put(
        "SYNC_CONNECTORS", PermissionType.SYNC_CONNECTORS.getDescription());
    defaultAdminPermissionsList.put(
        "MANAGE_CONNECTORS", PermissionType.MANAGE_CONNECTORS.getDescription());

    defaultAdminPermissionsList.put(
        "UPDATE_PERMISSIONS", PermissionType.UPDATE_PERMISSIONS.getDescription());
    defaultAdminPermissionsList.put(
        "UPDATE_SERVERCONFIG", PermissionType.UPDATE_SERVERCONFIG.getDescription());
    defaultAdminPermissionsList.put(
        "ALL_TEAMS_REPORTS", PermissionType.ALL_TEAMS_REPORTS.getDescription());
    defaultAdminPermissionsList.put(
        "APPROVE_ALL_REQUESTS_TEAMS", PermissionType.APPROVE_ALL_REQUESTS_TEAMS.getDescription());
    defaultAdminPermissionsList.put("VIEW_TOPICS", PermissionType.VIEW_TOPICS.getDescription());
    defaultAdminPermissionsList.put(
        "VIEW_CONNECTORS", PermissionType.VIEW_CONNECTORS.getDescription());
    defaultAdminPermissionsList.put(
        "UPDATE_DELETE_MY_TENANT", PermissionType.UPDATE_DELETE_MY_TENANT.getDescription());

    return defaultAdminPermissionsList;
  }

  public Map<String, String> getDefaultSuperAdminPermissionsList() {
    Map<String, String> permissionsList = new HashMap<>();

    permissionsList.put("SHUTDOWN_KLAW", PermissionType.SHUTDOWN_KLAW.getDescription());
    return permissionsList;
  }

  public ProductDetails getProductDetails(String name, String version) {
    ProductDetails productDetails = new ProductDetails();
    productDetails.setName(name);
    productDetails.setVersion(version);

    return productDetails;
  }

  public void handleQuickStartData(
      HandleDbRequestsJdbc handleDbRequests, String encryptorSecretKey, String infraTeam) {
    if (quickStartEnabled) {
      // verify if quick start data already exists tbd
      List<KwClusters> kwClusters =
          handleDbRequests.getAllClusters(KafkaClustersType.KAFKA, KwConstants.DEFAULT_TENANT_ID);
      if (!kwClusters.isEmpty()) {
        return;
      }

      handleDbRequests.addNewUser(
          getUser(
              KwConstants.DEFAULT_TENANT_ID,
              quickStartUserPwd,
              KwConstants.USER_ROLE,
              handleDbRequests
                  .getTeamDetailsFromName(infraTeam, KwConstants.DEFAULT_TENANT_ID)
                  .getTeamId(),
              "",
              quickStartUser1,
              encryptorSecretKey));
      handleDbRequests.addNewUser(
          getUser(
              KwConstants.DEFAULT_TENANT_ID,
              quickStartUserPwd,
              KwConstants.USER_ROLE,
              handleDbRequests
                  .getTeamDetailsFromName(infraTeam, KwConstants.DEFAULT_TENANT_ID)
                  .getTeamId(),
              "",
              quickStartUser2,
              encryptorSecretKey));
      // Add kafka cluster
      KwClusters kwClusterKafka = new KwClusters();
      kwClusterKafka.setClusterName("STG");
      kwClusterKafka.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA.value);
      kwClusterKafka.setBootstrapServers(dockerKafkaHost);
      kwClusterKafka.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
      kwClusterKafka.setClusterType(KafkaClustersType.KAFKA.value);
      kwClusterKafka.setTenantId(KwConstants.DEFAULT_TENANT_ID);
      handleDbRequests.addNewCluster(kwClusterKafka);

      // Add sr cluster
      KwClusters kwClusterSchemaRegistry = new KwClusters();
      kwClusterSchemaRegistry.setClusterName("STG");
      kwClusterSchemaRegistry.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA.value);
      kwClusterSchemaRegistry.setBootstrapServers(dockerSRHost);
      kwClusterSchemaRegistry.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
      kwClusterSchemaRegistry.setClusterType(KafkaClustersType.SCHEMA_REGISTRY.value);
      kwClusterSchemaRegistry.setTenantId(KwConstants.DEFAULT_TENANT_ID);
      handleDbRequests.addNewCluster(kwClusterSchemaRegistry);

      // Add kafka environment
      kwClusters =
          handleDbRequests.getAllClusters(KafkaClustersType.KAFKA, KwConstants.DEFAULT_TENANT_ID);
      Env envKafka = new Env();
      envKafka.setClusterId(kwClusters.get(0).getClusterId());
      envKafka.setName("STG");
      envKafka.setType(KafkaClustersType.KAFKA.value);
      EnvParams envParams = new EnvParams();
      envParams.setDefaultPartitions("1");
      envParams.setMaxPartitions("1");
      envParams.setDefaultRepFactor("1");
      envParams.setMaxRepFactor("1");
      envParams.setReplicationFactorList(Collections.singletonList("1"));
      envParams.setPartitionsList(List.of("1", "2"));
      envParams.setTopicPrefix(Collections.emptyList());
      envParams.setTopicSuffix(Collections.emptyList());
      envParams.setTopicRegex(Collections.emptyList());
      envKafka.setParams(envParams);
      envKafka.setTenantId(KwConstants.DEFAULT_TENANT_ID);
      envKafka.setEnvExists("true");
      handleDbRequests.addNewEnv(envKafka);

      // Add sr environment
      List<KwClusters> kwClustersSR =
          handleDbRequests.getAllClusters(
              KafkaClustersType.SCHEMA_REGISTRY, KwConstants.DEFAULT_TENANT_ID);
      Env envSR = new Env();
      envSR.setClusterId(kwClustersSR.get(0).getClusterId());
      envSR.setName("STG");
      envSR.setType(KafkaClustersType.SCHEMA_REGISTRY.value);
      EnvParams envParamsSR = new EnvParams();
      envSR.setParams(envParamsSR);
      envSR.setTenantId(KwConstants.DEFAULT_TENANT_ID);
      EnvTag envTag = new EnvTag();
      envTag.setId(handleDbRequests.getAllEnvs(KwConstants.DEFAULT_TENANT_ID).get(0).getId());
      envSR.setAssociatedEnv(envTag);
      envSR.setEnvExists("true");
      handleDbRequests.addNewEnv(envSR);
    }
  }
}
