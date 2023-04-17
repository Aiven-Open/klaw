package io.aiven.klaw.config;

import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.KwRolesPermissions;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.ProductDetails;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.TenantConfig;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.service.DefaultDataService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ManageDatabase implements ApplicationContextAware, InitializingBean, DisposableBean {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Autowired HandleDbRequestsJdbc handleDbRequests;

  private static Map<Integer, Map<String, EnvParams>> envParamsMapPerTenant;

  private static Map<Integer, Map<String, Map<String, String>>> kwPropertiesMapPerTenant;

  private static Map<Integer, List<Team>> teamsPerTenant;

  private static Map<Integer, List<UserInfo>> usersPerTenant;

  private static List<UserInfo> allUsersAllTenants;

  private static Set<String> serviceAccounts;

  // key is tenant id, value is list of envs
  private static Map<Integer, List<String>> envsOfTenantsMap;

  // key is tenantid id, value is hashmap of teamId and allowed envs
  private static Map<Integer, Map<Integer, List<String>>> teamsAndAllowedEnvsPerTenant;

  // key is tenantid id, value is hashmap of team Id as key and teamname as value
  private static Map<Integer, Map<Integer, String>> teamIdAndNamePerTenant;

  // EnvModel lists for status
  private static Map<Integer, List<EnvModel>> envModelsClustersStatus;

  // key tenantId, value tenant name
  private static Map<Integer, String> tenantMap;

  // key tenantId, value tenant full config
  private static Map<Integer, KwTenants> tenantFullMap;

  // key rolename, value list of permissions per tenant
  private static Map<Integer, Map<String, List<String>>> rolesPermsMapPerTenant;

  // key tenantId, sub key clusterid Pertenant
  private static Map<Integer, Map<Integer, KwClusters>> kwAllClustersPertenant;

  // key tenantId, sub key clusterid Pertenant
  private static Map<Integer, Map<Integer, KwClusters>> kwKafkaClustersPertenant;

  // key tenantId, sub key clusterid Pertenant
  private static Map<Integer, Map<Integer, KwClusters>> kwSchemaRegClustersPertenant;

  // key tenantId, sub key clusterid Pertenant
  private static Map<Integer, Map<Integer, KwClusters>> kwKafkaConnectClustersPertenant;

  private static Map<Integer, List<Env>> kafkaEnvListPerTenant = new HashMap<>();
  private static Map<Integer, List<Env>> schemaRegEnvListPerTenant = new HashMap<>();
  private static Map<Integer, List<Env>> kafkaConnectEnvListPerTenant = new HashMap<>();
  private static Map<Integer, List<Env>> allEnvListPerTenant = new HashMap<>();

  private static Map<Integer, KwTenantConfigModel> tenantConfig = new HashMap<>();

  private static Map<Integer, List<Topic>> topicsPerTenant = new HashMap<>();

  private static List<String> reqStatusList;

  @Autowired private DefaultDataService defaultDataService;

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${klaw.enable.sso:false}")
  private String ssoEnabled;

  @Value("${klaw.admin.mailid}")
  private String kwAdminMailId;

  @Value("${klaw.superadmin.default.password}")
  private String superAdminDefaultPwd;

  @Value("${klaw.version}")
  private String kwVersion;

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${klaw.superadmin.default.username}")
  private String superAdminDefaultUserName;

  private ApplicationContext contextApp;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.contextApp = applicationContext;
  }

  private void shutdownApp() {
    ((ConfigurableApplicationContext) contextApp).close();
  }

  @Override
  public void afterPropertiesSet() throws KlawException {
    loadDb();
  }

  @Override
  public void destroy() throws Exception {
    shutdownApp();
  }

  public void loadDb() throws KlawException {
    try {
      loadStaticDataToDb();
      updateStaticDataToMemory();
      checkSSOAuthentication();
    } catch (Exception e) {
      log.error("Error in starting the application. ", e);
      throw new KlawException(e.getMessage());
    }
  }

  public HandleDbRequestsJdbc getHandleDbRequests() {
    return handleDbRequests;
  }

  private void loadStaticDataToDb() throws KlawException {
    // add tenant
    Optional<KwTenants> kwTenants = handleDbRequests.getMyTenants(KwConstants.DEFAULT_TENANT_ID);
    if (kwTenants.isEmpty()) {
      handleDbRequests.addNewTenant(
          defaultDataService.getDefaultTenant(KwConstants.DEFAULT_TENANT_ID));
    }

    // add teams
    String infraTeam = "INFRATEAM", stagingTeam = "STAGINGTEAM";
    Team team1 =
        handleDbRequests.selectTeamDetailsFromName(infraTeam, KwConstants.DEFAULT_TENANT_ID);
    Team team2 =
        handleDbRequests.selectTeamDetailsFromName(stagingTeam, KwConstants.DEFAULT_TENANT_ID);

    if (team1 == null && team2 == null) {
      handleDbRequests.addNewTeam(
          defaultDataService.getTeam(KwConstants.DEFAULT_TENANT_ID, infraTeam));
      handleDbRequests.addNewTeam(
          defaultDataService.getTeam(KwConstants.DEFAULT_TENANT_ID, stagingTeam));
    }

    // check if default superadmin is configured or blank.
    // in case of blank, there should be atleast one other user with SUPERADMIN role
    if (superAdminDefaultUserName.isBlank()) {
      if (!validateUsersBeforeAdding()) {
        String errorMsg =
            "Please configure klaw.superadmin.default.username, as there are no other superadmins in the system.";
        log.error(errorMsg);
        throw new KlawException(errorMsg);
      }
    } else {
      // verify and add user with superadmin role
      if (superAdminDefaultPwd.isBlank()) {
        String errorMsg =
            "Please configure klaw.superadmin.default.password with a valid password.";
        log.error(errorMsg);
        throw new KlawException(errorMsg);
      }
      UserInfo userExists = handleDbRequests.getUsersInfo(superAdminDefaultUserName);
      if (userExists == null) {
        log.info("Adding user {}", superAdminDefaultUserName);
        handleDbRequests.addNewUser(
            defaultDataService.getUser(
                KwConstants.DEFAULT_TENANT_ID,
                superAdminDefaultPwd,
                KwConstants.SUPERADMIN_ROLE,
                handleDbRequests
                    .selectTeamDetailsFromName(infraTeam, KwConstants.DEFAULT_TENANT_ID)
                    .getTeamId(),
                kwAdminMailId,
                superAdminDefaultUserName,
                encryptorSecretKey));
      }
    }

    // add props
    List<KwProperties> kwProps =
        handleDbRequests.selectAllKwPropertiesPerTenant(KwConstants.DEFAULT_TENANT_ID);
    List<KwRolesPermissions> kwRolesPerms =
        handleDbRequests.getRolesPermissionsPerTenant(KwConstants.DEFAULT_TENANT_ID);
    if (kwProps == null || kwProps.isEmpty()) {
      handleDbRequests.insertDefaultKwProperties(
          defaultDataService.createDefaultProperties(KwConstants.DEFAULT_TENANT_ID, kwAdminMailId));
    }

    // add roles, permissions
    if (kwRolesPerms == null || kwRolesPerms.isEmpty()) {
      handleDbRequests.insertDefaultRolesPermissions(
          defaultDataService.createDefaultRolesPermissions(
              KwConstants.DEFAULT_TENANT_ID, true, kwInstallationType));
    }

    // product details
    String productName = "Klaw";
    Optional<ProductDetails> productDetails = handleDbRequests.selectProductDetails(productName);
    if (productDetails.isPresent()) {
      if (!Objects.equals(productDetails.get().getVersion(), kwVersion)) {
        handleDbRequests.insertProductDetails(
            defaultDataService.getProductDetails(productName, kwVersion));
      }
    } else {
      handleDbRequests.insertProductDetails(
          defaultDataService.getProductDetails(productName, kwVersion));
    }
  }

  // verify if there is atleast one user with superadmin access in default tenant
  private boolean validateUsersBeforeAdding() {
    List<UserInfo> allUsers = handleDbRequests.selectAllUsersInfo(KwConstants.DEFAULT_TENANT_ID);
    return allUsers.stream()
        .anyMatch(userInfo -> userInfo.getRole().equals(KwConstants.SUPERADMIN_ROLE));
  }

  private void checkSSOAuthentication() {
    if (DATABASE.value.equals(authenticationType) && "true".equals(ssoEnabled)) {
      log.error(
          "Error : Please configure authentication type to ad, if SSO is enabled. Shutting down..");
      shutdownApp();
    }
  }

  public List<UserInfo> selectAllUsersInfo() {
    return handleDbRequests.selectAllUsersAllTenants();
  }

  public List<Env> getKafkaEnvListAllTenants(int tenantId) {
    return kafkaEnvListPerTenant.get(tenantId);
  }

  public Integer getAllTeamsSize() {
    int allTeamSize = 0;
    for (Integer tenantId : tenantMap.keySet()) {
      allTeamSize += teamIdAndNamePerTenant.get(tenantId).size();
      allTeamSize = allTeamSize - 1; // removing "All teams"
    }
    return allTeamSize;
  }

  public Integer getAllClustersSize() {
    int allClustersSize = 0;
    for (Integer tenantId : tenantMap.keySet()) {
      allClustersSize += kwKafkaClustersPertenant.get(tenantId).size();
    }
    return allClustersSize;
  }

  public List<Env> getKafkaEnvList(int tenantId) {
    if (kafkaEnvListPerTenant.get(tenantId).isEmpty()) {
      return new ArrayList<>();
    }
    return kafkaEnvListPerTenant.get(tenantId);
  }

  public List<Env> getSchemaRegEnvList(int tenantId) {
    if (schemaRegEnvListPerTenant.get(tenantId).isEmpty()) {
      return new ArrayList<>();
    }
    return schemaRegEnvListPerTenant.get(tenantId);
  }

  public List<Env> getKafkaConnectEnvList(int tenantId) {
    if (kafkaConnectEnvListPerTenant.get(tenantId).isEmpty()) {
      return new ArrayList<>();
    }
    return kafkaConnectEnvListPerTenant.get(tenantId);
  }

  public List<Env> getAllEnvList(int tenantId) {
    if (allEnvListPerTenant.get(tenantId).isEmpty()) {
      return new ArrayList<>();
    }
    return allEnvListPerTenant.get(tenantId);
  }

  private Integer getTenantIdFromName(String tenantName) {
    return tenantMap.entrySet().stream()
        .filter(obj -> Objects.equals(obj.getValue(), tenantName))
        .findFirst()
        .get()
        .getKey();
  }

  public Map<String, EnvParams> getEnvParamsMap(Integer tenantId) {
    return envParamsMapPerTenant.get(tenantId);
  }

  public List<String> getTeamsAndAllowedEnvs(Integer teamId, int tenantId) {
    return teamsAndAllowedEnvsPerTenant.get(tenantId).get(teamId);
  }

  // return team ids
  public Set<Integer> getTeamsForTenant(int tenantId) {
    return teamsAndAllowedEnvsPerTenant.get(tenantId).keySet();
  }

  // return teams
  public List<Team> getTeamObjForTenant(int tenantId) {
    return teamsPerTenant.get(tenantId);
  }

  public Set<String> getAllServiceAccounts(int tenantId) {
    return serviceAccounts;
  }

  private void updateAllServiceAccounts(int tenantId) {
    serviceAccounts = new HashSet<>();
    getTeamObjForTenant(tenantId)
        .forEach(
            a -> {
              if (a.getServiceAccounts() != null
                  && a.getServiceAccounts().getServiceAccountsList() != null) {
                serviceAccounts.addAll(a.getServiceAccounts().getServiceAccountsList());
              }
            });
  }

  public List<String> getTeamNamesForTenant(int tenantId) {
    return teamsPerTenant.get(tenantId).stream().map(Team::getTeamname).toList();
  }

  public Integer getTeamIdFromTeamName(int tenantId, String teamName) {
    Optional<Map.Entry<Integer, String>> optionalTeam;
    if (teamName != null) {
      optionalTeam =
          teamIdAndNamePerTenant.get(tenantId).entrySet().stream()
              .filter(a -> Objects.equals(a.getValue(), teamName))
              .findFirst();
    } else {
      return null;
    }

    // unknown team
    return optionalTeam.map(Map.Entry::getKey).orElse(null);
  }

  public String getTeamNameFromTeamId(int tenantId, int teamId) {
    return teamIdAndNamePerTenant
        .getOrDefault(tenantId, Collections.emptyMap())
        .getOrDefault(teamId, ""); // empty string in case of unknown team
  }

  public Map<Integer, List<String>> getEnvsOfTenantsMap() {
    return envsOfTenantsMap;
  }

  public Map<Integer, String> getTenantMap() {
    return tenantMap;
  }

  public Map<Integer, KwClusters> getClusters(KafkaClustersType clusterType, int tenantId) {
    return switch (clusterType) {
      case SCHEMA_REGISTRY -> kwSchemaRegClustersPertenant.get(tenantId);
      case KAFKA_CONNECT -> kwKafkaConnectClustersPertenant.get(tenantId);
      case KAFKA -> kwKafkaClustersPertenant.get(tenantId);
      default -> kwAllClustersPertenant.get(tenantId);
    };
  }

  public Map<String, Map<String, String>> getKwPropertiesMap(int tenantId) {
    return kwPropertiesMapPerTenant.get(tenantId);
  }

  public String getKwPropertyValue(String kwKey, int tenantId) {
    if (kwPropertiesMapPerTenant.get(tenantId) != null) {
      return kwPropertiesMapPerTenant.get(tenantId).get(kwKey).get("kwvalue");
    } else {
      return "";
    }
  }

  public List<UserInfo> selectAllCachedUserInfo() {
    return allUsersAllTenants;
  }

  private void loadEnvsForAllTenants() {
    envsOfTenantsMap = new HashMap<>(); // key is tenantid, value is list of envs
    for (Integer tenantId : tenantMap.keySet()) {
      loadEnvsForOneTenant(tenantId);
    }
  }

  public void loadEnvsForOneTenant(Integer tenantId) {
    List<Env> kafkaEnvList =
        handleDbRequests.selectAllKafkaEnvs(tenantId).stream()
            .filter(env -> "true".equals(env.getEnvExists()))
            .toList();
    List<Env> schemaEnvList =
        handleDbRequests.selectAllSchemaRegEnvs(tenantId).stream()
            .filter(env -> "true".equals(env.getEnvExists()))
            .toList();
    List<Env> kafkaConnectEnvList =
        handleDbRequests.selectAllKafkaConnectEnvs(tenantId).stream()
            .filter(env -> "true".equals(env.getEnvExists()))
            .toList();

    List<String> envList1 = kafkaEnvList.stream().map(Env::getId).collect(Collectors.toList());
    List<String> envList2 = schemaEnvList.stream().map(Env::getId).toList();
    List<String> envList3 = kafkaConnectEnvList.stream().map(Env::getId).toList();
    envList1.addAll(envList2);
    envList1.addAll(envList3);

    envsOfTenantsMap.put(tenantId, envList1);
  }

  private void loadTenantTeamsUsersForAllTenants() {
    teamsAndAllowedEnvsPerTenant = new HashMap<>();
    teamIdAndNamePerTenant = new HashMap<>();
    teamsPerTenant = new HashMap<>();
    usersPerTenant = new HashMap<>();
    allUsersAllTenants = new ArrayList<>();

    List<Team> allTeams;

    for (Integer tenantId : tenantMap.keySet()) {
      allTeams = handleDbRequests.selectAllTeams(tenantId);
      teamsPerTenant.put(tenantId, allTeams);
      loadTenantTeamsForOneTenant(allTeams, tenantId);
    }

    loadUsersForAllTenants();
  }

  public void loadUsersForAllTenants() {
    List<UserInfo> allUsers;
    allUsersAllTenants = new ArrayList<>();
    for (Integer tenantId : tenantMap.keySet()) {
      allUsers = handleDbRequests.selectAllUsersInfo(tenantId);
      usersPerTenant.put(tenantId, allUsers);
      allUsersAllTenants.addAll(allUsers);
    }
  }

  public void loadTopicsForAllTenants() {
    for (Integer tenantId : tenantMap.keySet()) {
      topicsPerTenant.put(tenantId, handleDbRequests.getAllTopics(tenantId));
    }
  }

  public void loadTopicsForOneTenant(int tenantId) {
    topicsPerTenant.put(tenantId, handleDbRequests.getAllTopics(tenantId));
  }

  public List<Topic> getTopicsForTenant(int tenantId) {
    return topicsPerTenant.get(tenantId);
  }

  public void loadTenantTeamsForOneTenant(List<Team> allTeams, Integer tenantId) {
    if (allTeams == null) {
      allTeams = handleDbRequests.selectAllTeams(tenantId);
      teamsPerTenant.put(tenantId, allTeams);
    }

    Map<Integer, List<String>> teamsAndAllowedEnvs = new HashMap<>();
    Map<Integer, String> teamsAndNames = new HashMap<>();

    List<Team> teamList =
        allTeams.stream().filter(team -> Objects.equals(team.getTenantId(), tenantId)).toList();

    for (Team team : teamList) {
      teamsAndAllowedEnvs.put(team.getTeamId(), envsOfTenantsMap.get(tenantId));
      teamsAndNames.put(team.getTeamId(), team.getTeamname());
    }
    teamsAndNames.put(1, "All teams");

    teamsAndAllowedEnvsPerTenant.put(tenantId, teamsAndAllowedEnvs);
    teamIdAndNamePerTenant.put(tenantId, teamsAndNames);
    updateAllServiceAccounts(tenantId);
  }

  public Map<Integer, KwTenantConfigModel> getTenantConfig() {
    return tenantConfig;
  }

  public void setTenantConfig(TenantConfig config) {
    KwTenantConfigModel tenantModel = config.getTenantModel();
    if (tenantModel != null) {
      tenantConfig.put(getTenantIdFromName(tenantModel.getTenantName()), tenantModel);
    }
  }

  private void loadKwPropertiesforAllTenants() {
    Map<Integer, Map<String, Map<String, String>>> kwPropertiesMap =
        handleDbRequests.selectAllKwProperties();
    if (kwPropertiesMap.size() == 0) {
      log.info("Klaw Properties not loaded into database. Shutting down !!");
      shutdownApp();
    }
    kwPropertiesMapPerTenant = new HashMap<>();
    for (Integer tenantId : tenantMap.keySet()) {
      loadKwPropsPerOneTenant(kwPropertiesMap, tenantId);
    }
  }

  public void loadKwPropsPerOneTenant(
      Map<Integer, Map<String, Map<String, String>>> kwPropertiesMap, Integer tenantId) {
    if (kwPropertiesMap == null) {
      kwPropertiesMap = handleDbRequests.selectAllKwProperties();
    }

    kwPropertiesMapPerTenant.put(tenantId, kwPropertiesMap.get(tenantId));
    updateKwTenantConfigPerTenant(tenantId);
  }

  private void loadTenants() {
    List<KwTenants> tenants = handleDbRequests.getTenants();
    tenantMap = new HashMap<>();
    tenantFullMap = new HashMap<>();
    tenants.forEach(tenant -> tenantMap.put(tenant.getTenantId(), tenant.getTenantName()));
    tenants.forEach(tenant -> tenantFullMap.put(tenant.getTenantId(), tenant));
  }

  public void loadOneTenant(int tenantId) {
    Optional<KwTenants> tenants = handleDbRequests.getMyTenants(tenantId);
    tenants.ifPresent(
        kwTenants -> {
          tenantFullMap.put(kwTenants.getTenantId(), kwTenants);
          tenantMap.put(tenantId, kwTenants.getTenantName());
        });
  }

  public KwTenants getTenantFullConfig(int tenantId) {
    return tenantFullMap.get(tenantId);
  }

  private void loadClustersForAllTenants() {
    List<KwClusters> kafkaClusters;
    List<KwClusters> schemaRegistryClusters;
    List<KwClusters> kafkaConnectClusters;

    kwKafkaClustersPertenant = new HashMap<>();
    kwSchemaRegClustersPertenant = new HashMap<>();
    kwKafkaConnectClustersPertenant = new HashMap<>();
    kwAllClustersPertenant = new HashMap<>();

    for (Integer tenantId : tenantMap.keySet()) {
      kafkaClusters = handleDbRequests.getAllClusters(KafkaClustersType.KAFKA, tenantId);
      schemaRegistryClusters =
          handleDbRequests.getAllClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId);
      kafkaConnectClusters =
          handleDbRequests.getAllClusters(KafkaClustersType.KAFKA_CONNECT, tenantId);

      loadClustersForOneTenant(
          kafkaClusters, schemaRegistryClusters, kafkaConnectClusters, tenantId);
    }
  }

  public void loadClustersForOneTenant(
      List<KwClusters> kafkaClusters,
      List<KwClusters> schemaRegistryClusters,
      List<KwClusters> kafkaConnectClusters,
      Integer tenantId) {
    if (kafkaClusters == null) {
      kafkaClusters = handleDbRequests.getAllClusters(KafkaClustersType.KAFKA, tenantId);
    }
    if (schemaRegistryClusters == null) {
      schemaRegistryClusters =
          handleDbRequests.getAllClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId);
    }
    if (kafkaConnectClusters == null) {
      kafkaConnectClusters =
          handleDbRequests.getAllClusters(KafkaClustersType.KAFKA_CONNECT, tenantId);
    }

    Map<Integer, KwClusters> kwKafkaClusters = new HashMap<>();
    Map<Integer, KwClusters> kwSchemaRegClusters = new HashMap<>();
    Map<Integer, KwClusters> kwKafkaConnectClusters = new HashMap<>();
    Map<Integer, KwClusters> kwAllClusters = new HashMap<>();

    kafkaClusters.forEach(
        cluster -> {
          kwKafkaClusters.put(cluster.getClusterId(), cluster);
          kwAllClusters.put(cluster.getClusterId(), cluster);
        });
    kwKafkaClustersPertenant.put(tenantId, kwKafkaClusters);

    schemaRegistryClusters.forEach(
        cluster -> {
          kwSchemaRegClusters.put(cluster.getClusterId(), cluster);
          kwAllClusters.put(cluster.getClusterId(), cluster);
        });
    kwSchemaRegClustersPertenant.put(tenantId, kwSchemaRegClusters);

    kafkaConnectClusters.forEach(
        cluster -> {
          kwKafkaConnectClusters.put(cluster.getClusterId(), cluster);
          kwAllClusters.put(cluster.getClusterId(), cluster);
        });
    kwKafkaConnectClustersPertenant.put(tenantId, kwKafkaConnectClusters);

    kwAllClustersPertenant.put(tenantId, kwAllClusters);
  }

  public void updateStaticDataForTenant(int tenantId) {
    loadOneTenant(tenantId);
    loadKwPropsPerOneTenant(null, tenantId);
    loadRolesPermissionsOneTenant(null, tenantId);
    loadClustersForOneTenant(null, null, null, tenantId);
    loadEnvMapForOneTenant(tenantId);
    loadEnvsForOneTenant(tenantId);
    loadTenantTeamsForOneTenant(null, tenantId);
    loadUsersForAllTenants();

    loadTopicsForOneTenant(tenantId);
  }

  private void updateStaticDataToMemory() {
    log.info("updateStaticData Loading all config.");
    loadTenants();
    loadKwPropertiesforAllTenants();
    loadRolesForAllTenants();
    loadClustersForAllTenants();
    loadRequestTypeStatuses();
    loadEnvironmentsMapForAllTenants();
    loadEnvsForAllTenants();
    loadTenantTeamsUsersForAllTenants();

    loadTopicsForAllTenants();
  }

  private void loadRequestTypeStatuses() {
    reqStatusList = new ArrayList<>();
    for (RequestStatus requestStatus : RequestStatus.values()) {
      reqStatusList.add(requestStatus.name());
    }
  }

  private void loadEnvironmentsMapForAllTenants() {
    envParamsMapPerTenant = new HashMap<>();

    for (Integer tenantId : tenantMap.keySet()) {
      loadEnvMapForOneTenant(tenantId);
    }
    log.info("Finished loading cluster parameters.");
  }

  public void loadEnvMapForOneTenant(Integer tenantId) {
    List<Env> kafkaEnvList =
        handleDbRequests.selectAllKafkaEnvs(tenantId).stream()
            .filter(env -> "true".equals(env.getEnvExists()))
            .collect(Collectors.toList());
    List<Env> schemaEnvList =
        handleDbRequests.selectAllSchemaRegEnvs(tenantId).stream()
            .filter(env -> "true".equals(env.getEnvExists()))
            .collect(Collectors.toList());
    List<Env> kafkaConnectEnvList =
        handleDbRequests.selectAllKafkaConnectEnvs(tenantId).stream()
            .filter(env -> "true".equals(env.getEnvExists()))
            .collect(Collectors.toList());
    List<Env> allEnvList = new ArrayList<>();
    allEnvList.addAll(kafkaEnvList);
    allEnvList.addAll(schemaEnvList);
    allEnvList.addAll(kafkaConnectEnvList);

    kafkaEnvListPerTenant.put(tenantId, kafkaEnvList);
    schemaRegEnvListPerTenant.put(tenantId, schemaEnvList);
    kafkaConnectEnvListPerTenant.put(tenantId, kafkaConnectEnvList);
    allEnvListPerTenant.put(tenantId, allEnvList);

    List<Env> kafkaEnvTenantList =
        kafkaEnvList.stream()
            .filter(kafkaEnv -> Objects.equals(kafkaEnv.getTenantId(), tenantId))
            .toList();
    Map<String, EnvParams> envParamsMap = new HashMap<>();

    for (Env env : kafkaEnvTenantList) {
      EnvParams oneEnvParamsObj = new EnvParams();
      String envParams = env.getOtherParams();

      String[] params = envParams.split(",");
      String defaultPartitions = null, defaultRf = null;
      for (String param : params) {
        if (param.startsWith("default.partitions")) {
          defaultPartitions = param.substring(param.indexOf("=") + 1);
          oneEnvParamsObj.setDefaultPartitions(getParamAsList(param));
        } else if (param.startsWith("max.partitions")) {
          String maxPartitions = param.substring(param.indexOf("=") + 1);
          int maxPartitionsInt = Integer.parseInt(maxPartitions);
          List<String> partitions = new ArrayList<>();
          createMaxEntry(defaultPartitions, maxPartitionsInt, partitions);
          oneEnvParamsObj.setPartitionsList(partitions);
        } else if (param.startsWith("default.replication.factor")) {
          defaultRf = param.substring(param.indexOf("=") + 1);
          oneEnvParamsObj.setDefaultRepFactor(getParamAsList(param));
        } else if (param.startsWith("max.replication.factor")) {
          String maxRf = param.substring(param.indexOf("=") + 1);
          int maxRfInt = Integer.parseInt(maxRf);
          List<String> rf = new ArrayList<>();
          createMaxEntry(defaultRf, maxRfInt, rf);
          oneEnvParamsObj.setReplicationFactorList(rf);
        } else if (param.startsWith("topic.prefix")) {
          oneEnvParamsObj.setTopicPrefix(getParamAsList(param));
        } else if (param.startsWith("topic.suffix")) {
          oneEnvParamsObj.setTopicSuffix(getParamAsList(param));
        } else if (param.startsWith("topic.regex")) {
          oneEnvParamsObj.setTopicSuffix(getParamAsList(param));
        } else if (param.startsWith("topic.advanced.config")) {
          oneEnvParamsObj.setAdvancedTopicConfiguration(getParamAsList(param));
        }
      }

      envParamsMap.put(env.getId(), oneEnvParamsObj);
    }
    envParamsMapPerTenant.put(tenantId, envParamsMap);
  }

  private static void createMaxEntry(String defaultEntry, int maxInt, List<String> listOfEntries) {
    for (int i = 1; i < maxInt + 1; i++) {
      if (defaultEntry != null && defaultEntry.equals(i + "")) {
        listOfEntries.add(i + " (default)");
      } else {
        listOfEntries.add(i + "");
      }
    }
  }

  private static List<String> getParamAsList(String param) {
    String paramPrefix = param.substring(param.indexOf("=") + 1);
    List<String> paramList = new ArrayList<>();
    paramList.add(paramPrefix);
    return paramList;
  }

  private static void setTopicNamingConstraint(
      String param, Map<String, List<String>> oneEnvParamsMap, String topicConventionName) {
    List<String> topicConventionNamingList = getParamAsList(param);
    oneEnvParamsMap.put(topicConventionName, topicConventionNamingList);
  }

  public Map<String, List<String>> getRolesPermissionsPerTenant(int tenantId) {
    return rolesPermsMapPerTenant.get(tenantId);
  }

  public void loadRolesForAllTenants() {
    log.info("Load roles and permissions.");
    rolesPermsMapPerTenant = new HashMap<>();
    List<KwRolesPermissions> rolesPermissions = handleDbRequests.getRolesPermissions();
    for (Integer tenantId : tenantMap.keySet()) {
      loadRolesPermissionsOneTenant(rolesPermissions, tenantId);
    }
  }

  public void loadRolesPermissionsOneTenant(
      List<KwRolesPermissions> rolesPermissions, Integer tenantId) {
    if (rolesPermissions == null) {
      rolesPermissions = handleDbRequests.getRolesPermissions();
    }

    List<KwRolesPermissions> rolesPermsList =
        rolesPermissions.stream().filter(rolePerms -> rolePerms.getTenantId() == tenantId).toList();
    List<String> tmpList;
    Map<String, List<String>> rolesPermsMap = new HashMap<>();
    for (KwRolesPermissions rolesPermission : rolesPermsList) {
      if (!rolesPermsMap.containsKey(rolesPermission.getRoleId())) {
        tmpList = new ArrayList<>();
      } else {
        tmpList = rolesPermsMap.get(rolesPermission.getRoleId());
      }
      tmpList.add(rolesPermission.getPermission());
      rolesPermsMap.put(rolesPermission.getRoleId(), tmpList);
    }
    rolesPermsMapPerTenant.put(tenantId, rolesPermsMap);
  }

  public Map<Integer, List<EnvModel>> getEnvModelsClustersStatusAllTenants() {
    return envModelsClustersStatus;
  }

  public List<String> getRequestStatusList() {
    return reqStatusList;
  }

  public void updateKwTenantConfigPerTenant(Integer tenantId) {
    try {
      String TENANT_CONFIG = "klaw.tenant.config";
      if (kwPropertiesMapPerTenant.get(tenantId) != null) {
        if (kwPropertiesMapPerTenant.get(tenantId).get(TENANT_CONFIG) != null) {
          String kwTenantConfig =
              kwPropertiesMapPerTenant.get(tenantId).get(TENANT_CONFIG).get("kwvalue");
          TenantConfig dynamicObj;
          OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

          dynamicObj = OBJECT_MAPPER.readValue(kwTenantConfig, TenantConfig.class);
          setTenantConfig(dynamicObj);
        }
      }
    } catch (IOException e) {
      log.info("Error loading tenant config ", e);
    }
  }

  public void deleteCluster(int tenantId) {
    loadClustersForOneTenant(null, null, null, tenantId);
    loadEnvMapForOneTenant(tenantId);
    loadEnvsForOneTenant(tenantId);
  }

  // delete users from both users tables of tenant
  // delete teams of tenant
  // delete users of tenant
  // delete envs of tenant
  // delete rolesperms
  // delete kwprops
  // delete clusters of tenant
  // delete tenant
  public String deleteTenant(int tenantId) {
    tenantMap.remove(tenantId);
    teamsAndAllowedEnvsPerTenant.remove(tenantId);
    usersPerTenant.remove(tenantId);
    kwPropertiesMapPerTenant.remove(tenantId);
    rolesPermsMapPerTenant.remove(tenantId);
    envParamsMapPerTenant.remove(tenantId);

    kwKafkaClustersPertenant.remove(tenantId);
    kwSchemaRegClustersPertenant.remove(tenantId);
    kwKafkaConnectClustersPertenant.remove(tenantId);
    kwAllClustersPertenant.remove(tenantId);

    return ApiResultStatus.SUCCESS.value;
  }
}
