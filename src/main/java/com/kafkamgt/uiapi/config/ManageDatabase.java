package com.kafkamgt.uiapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.helpers.db.rdbms.HandleDbRequestsJdbc;
import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import com.kafkamgt.uiapi.model.*;
import com.kafkamgt.uiapi.service.DefaultDataService;
import com.kafkamgt.uiapi.service.MailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import static com.kafkamgt.uiapi.service.KwConstants.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ManageDatabase  implements ApplicationContextAware {

    private HandleDbRequests handleDbRequests;

    private static HashMap<Integer, HashMap<String, HashMap<String, List<String>>>> envParamsMapPerTenant;

    private static HashMap<Integer, HashMap<String, HashMap<String, String>>> kwPropertiesMapPerTenant;

    // key is tenant id, value is list of envs
    private static HashMap<Integer, List<String>> envsOfTenantsMap;

    // key is tenantid id, value is hashmap of teamId and allowed envs
    private static HashMap<Integer, HashMap<Integer, List<String>>> teamsAndAllowedEnvsPerTenant;

    // key is tenantid id, value is hashmap of team Id as key and teamname as value
    private static HashMap<Integer, HashMap<Integer, String>> teamIdAndNamePerTenant;

    // EnvModel lists for status
    private static HashMap<Integer, List<EnvModel>> envModelsClustersStatus;

    // key tenantId, value tenant name
    private static HashMap<Integer, String> tenantMap;

    // key tenantId, value tenant full config
    private static HashMap<Integer, KwTenants> tenantFullMap;

    // key rolename, value list of permissions per tenant
    private static HashMap<Integer, HashMap<String, List<String>>> rolesPermsMapPerTenant;

    // key tenantId, sub key clusterid Pertenant
    private static HashMap<Integer, HashMap<Integer, KwClusters>> kwAllClustersPertenant;

    // key tenantId, sub key clusterid Pertenant
    private static HashMap<Integer, HashMap<Integer, KwClusters>> kwKafkaClustersPertenant;

    // key tenantId, sub key clusterid Pertenant
    private static HashMap<Integer, HashMap<Integer, KwClusters>> kwSchemaRegClustersPertenant;

    // key tenantId, sub key clusterid Pertenant
    private static HashMap<Integer, HashMap<Integer, KwClusters>> kwKafkaConnectClustersPertenant;

//    private static List<Env> kafkaEnvList;

//    private static List<Env> kafkaConnectEnvList;
//
//    private static List<Env> schemaEnvList;

    private static HashMap<Integer, List<Env>> kafkaEnvListPerTenant = new HashMap<>();
    private static HashMap<Integer, List<Env>> schemaRegEnvListPerTenant = new HashMap<>();
    private static HashMap<Integer, List<Env>> kafkaConnectEnvListPerTenant = new HashMap<>();
    private static HashMap<Integer, List<Env>> allEnvListPerTenant = new HashMap<>();

    private static HashMap<Integer, KwTenantConfigModel> tenantConfig = new HashMap<>();;

    private static List<String> reqStatusList;

    private static boolean isTrialLicense;

    @Autowired
    private MailUtils utils;

    @Autowired
    private DefaultDataService defaultDataService;

    @Value("${kafkawize.org.name}")
    private
    String orgName;

    @Value("${kafkawize.login.authentication.type}")
    private String authenticationType;

    @Value("${kafkawize.enable.sso:false}")
    private String ssoEnabled;

    @Value("${kafkawize.admin.mailid}")
    private
    String kwAdminMailId;

    @Value("${kafkawize.superadmin.defaultpassword}")
    private String superAdminDefaultPwd;

    @Value("${kafkawize.version:1.0.0}")
    private String kwVersion;

    @Value("${kafkawize.jasypt.encryptor.secretkey}")
    private String encryptorSecretKey;

    @Value("${kafkawize.installation.type:onpremise}")
    private String kwInstallationType;

    private ApplicationContext contextApp;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.contextApp = applicationContext;
    }

    private void shutdownApp(){
        ((ConfigurableApplicationContext) contextApp).close();
    }
    
    @PostConstruct
    public void loadDb() throws Exception {

        handleDbRequests = handleJdbc();
        handleDbRequests.connectToDb("licenseKey");
        loadStaticDataToDb();
        updateStaticDataToMemory();
        checkSSOAuthentication();
    }

    private void loadStaticDataToDb() {
        // add tenant
        Optional<KwTenants> kwTenants = handleDbRequests.getMyTenants(DEFAULT_TENANT_ID);
        if(kwTenants.isEmpty())
            handleDbRequests.addNewTenant(defaultDataService.getDefaultTenant(DEFAULT_TENANT_ID));

        // add teams
        String infraTeam = "INFRATEAM", stagingTeam = "STAGINGTEAM";
        Team team1 = handleDbRequests.selectTeamDetailsFromName(infraTeam, DEFAULT_TENANT_ID);
        Team team2 = handleDbRequests.selectTeamDetailsFromName(stagingTeam, DEFAULT_TENANT_ID);

        if(team1 == null && team2 == null){
            handleDbRequests.addNewTeam(defaultDataService.getTeam(DEFAULT_TENANT_ID, infraTeam));
            handleDbRequests.addNewTeam(defaultDataService.getTeam(DEFAULT_TENANT_ID, stagingTeam));
        }

        // add user
        String userName = "superadmin";
        UserInfo userExists = handleDbRequests.getUsersInfo(userName);
        if(userExists == null) {
            handleDbRequests.addNewUser(defaultDataService.getUser(DEFAULT_TENANT_ID, superAdminDefaultPwd,
                    "SUPERADMIN", handleDbRequests.selectTeamDetailsFromName(infraTeam, DEFAULT_TENANT_ID).getTeamId(),
                    kwAdminMailId, userName, encryptorSecretKey));
        }

        // add props
        List<KwProperties> kwProps = handleDbRequests.selectAllKwPropertiesPerTenant(DEFAULT_TENANT_ID);
        List<KwRolesPermissions> kwRolesPerms = handleDbRequests.getRolesPermissionsPerTenant(DEFAULT_TENANT_ID);
        if(kwProps == null || kwProps.isEmpty()){
            handleDbRequests.insertDefaultKwProperties(defaultDataService.createDefaultProperties(DEFAULT_TENANT_ID, kwAdminMailId));
        }

        // add roles, permissions
        if(kwRolesPerms == null || kwRolesPerms.isEmpty()){
            handleDbRequests.insertDefaultRolesPermissions(defaultDataService.createDefaultRolesPermissions(DEFAULT_TENANT_ID, true, kwInstallationType));
        }

        // product details
        String productName = "Kafkawize";
        Optional<ProductDetails> productDetails = handleDbRequests.selectProductDetails(productName);
        if(productDetails.isPresent()){
            if(!productDetails.get().getVersion().equals(kwVersion))
                handleDbRequests.insertProductDetails(defaultDataService.getProductDetails(productName, kwVersion));
        }else
            handleDbRequests.insertProductDetails(defaultDataService.getProductDetails(productName, kwVersion));
    }

//    @Autowired
//    private CreateBulkTests createBulkTests;
//    private void runPerfTests() {
//        createBulkTests.createTopicRequests();
////        createBulkTests.approveTopicRequests();
////        createBulkTests.getTopics();
//    }

    private void checkSSOAuthentication(){
        if(authenticationType.equals("db") && ssoEnabled.equals("true")) {
            log.error("Error : Please configure authentication type to ad, if SSO is enabled. Shutting down..");
            shutdownApp();
        }
    }

    public HandleDbRequests getHandleDbRequests(){
        return handleDbRequests;
    }

    @Bean()
    @Conditional(JdbcDataSourceCondition.class)
    HandleDbRequestsJdbc handleJdbc() {
        return new HandleDbRequestsJdbc();
    }

    public List<UserInfo> selectAllUsersInfo(){
            return handleDbRequests.selectAllUsersAllTenants();
    }

    public List<Env> getKafkaEnvListAllTenants(int tenantId){
        return kafkaEnvListPerTenant.get(tenantId);
    }

    public Integer getAllTeamsSize(){
        int allTeamSize = 0;
        for (Integer tenantId : tenantMap.keySet()) {
            allTeamSize += teamIdAndNamePerTenant.get(tenantId).size();
            allTeamSize = allTeamSize - 1; // removing "All teams"
        }
        return allTeamSize;
    }

    public Integer getAllClustersSize(){
        int allClustersSize = 0;
        for (Integer tenantId : tenantMap.keySet()) {
            allClustersSize += kwKafkaClustersPertenant.get(tenantId).size();
        }
        return allClustersSize;
    }

    public List<Env> getKafkaEnvList(int tenantId){
        if(kafkaEnvListPerTenant.get(tenantId).isEmpty())
            return new ArrayList<>();
        return kafkaEnvListPerTenant.get(tenantId);
    }

    public List<Env> getSchemaRegEnvList(int tenantId){
        if(schemaRegEnvListPerTenant.get(tenantId).isEmpty())
            return new ArrayList<>();
        return schemaRegEnvListPerTenant.get(tenantId);
    }

    public List<Env> getKafkaConnectEnvList(int tenantId){
        if(kafkaConnectEnvListPerTenant.get(tenantId).isEmpty())
            return new ArrayList<>();
        return kafkaConnectEnvListPerTenant.get(tenantId);
    }

    public List<Env> getAllEnvList(int tenantId){
        if(allEnvListPerTenant.get(tenantId).isEmpty())
            return new ArrayList<>();
        return allEnvListPerTenant.get(tenantId);
    }

    public void setIsTrialLicense(boolean isTrialLicensed){
        isTrialLicense = isTrialLicensed;
    }

    public boolean getIsTrialLicense(){
        return isTrialLicense;
    }

    private Integer getTenantIdFromName(String tenantName){
        return tenantMap.entrySet().stream()
                .filter(obj -> obj.getValue().equals(tenantName))
                .findFirst().get().getKey();
    }

    public Map<String, HashMap<String, List<String>>> getEnvParamsMap(Integer tenantId){
        return envParamsMapPerTenant.get(tenantId);
    }

    public List<String> getTeamsAndAllowedEnvs(Integer teamId, int tenantId){
        return teamsAndAllowedEnvsPerTenant.get(tenantId).get(teamId);
    }

    // return team ids
    public Set<Integer> getTeamsForTenant(int tenantId){
        return teamsAndAllowedEnvsPerTenant.get(tenantId).keySet();
    }

    public Integer getTeamIdFromTeamName(int tenantId, String teamName){
        Optional<Map.Entry<Integer, String>> optionalTeam;
        if(teamName != null)
            optionalTeam =  teamIdAndNamePerTenant.get(tenantId).entrySet().stream()
                .filter(a -> a.getValue().equals(teamName))
                .findFirst();
        else
            return null;

        // unknown team
        return optionalTeam.map(Map.Entry::getKey).orElse(null);
    }

    public String getTeamNameFromTeamId(int tenantId, int teamId){
        Optional<Map.Entry<Integer, String>> optionalTeam =  teamIdAndNamePerTenant.get(tenantId).entrySet().stream()
                .filter(a -> a.getKey() == teamId)
                .findFirst();
        if(optionalTeam.isPresent())
            return optionalTeam.get().getValue();
        else
            return ""; // unknown team
    }

    public HashMap<Integer, List<String>> getEnvsOfTenantsMap(){
        return envsOfTenantsMap;
    }

    public HashMap<Integer, String> getTenantMap(){
        return tenantMap;
    }

    public HashMap<Integer, KwClusters> getClusters(String clusterType, int tenantId){
        switch (clusterType) {
            case "schemaregistry":
                return kwSchemaRegClustersPertenant.get(tenantId);
            case "kafkaconnect":
                return kwKafkaConnectClustersPertenant.get(tenantId);
            case "kafka":
                return kwKafkaClustersPertenant.get(tenantId);
            default:
                return kwAllClustersPertenant.get(tenantId);
        }
    }

    public HashMap<String, HashMap<String, String>> getKwPropertiesMap(int tenantId){
        return kwPropertiesMapPerTenant.get(tenantId);
    }

    public String getKwPropertyValue(String kwKey, int tenantId){
        if(kwPropertiesMapPerTenant.get(tenantId)!=null)
            return kwPropertiesMapPerTenant.get(tenantId).get(kwKey).get("kwvalue");
        else
            return "";
    }

    private void loadEnvsForAllTenants(){
        envsOfTenantsMap = new HashMap<>(); // key is tenantid, value is list of envs

        for (Integer tenantId : tenantMap.keySet()) {
            loadEnvsForOneTenant(tenantId);
        }
    }

    public void loadEnvsForOneTenant(Integer tenantId) {
        List<Env> kafkaEnvList = handleDbRequests.selectAllKafkaEnvs(tenantId)
                .stream().filter(env->env.getEnvExists().equals("true")).collect(Collectors.toList());
        List<Env> schemaEnvList = handleDbRequests.selectAllSchemaRegEnvs(tenantId)
                .stream().filter(env->env.getEnvExists().equals("true")).collect(Collectors.toList());
        List<Env> kafkaConnectEnvList = handleDbRequests.selectAllKafkaConnectEnvs(tenantId)
                .stream().filter(env->env.getEnvExists().equals("true")).collect(Collectors.toList());

        List<String> envList1 = kafkaEnvList.stream().map(Env::getId).collect(Collectors.toList());
        List<String> envList2 = schemaEnvList.stream().map(Env::getId).collect(Collectors.toList());
        List<String> envList3 = kafkaConnectEnvList.stream().map(Env::getId).collect(Collectors.toList());
        envList1.addAll(envList2);
        envList1.addAll(envList3);

        envsOfTenantsMap.put(tenantId, envList1);
    }

    private void loadTenantTeamsForAllTenants(){
        teamsAndAllowedEnvsPerTenant = new HashMap<>();
        teamIdAndNamePerTenant = new HashMap<>();
        List<Team> allTeams;

        for (Integer tenantId : tenantMap.keySet()) {
            allTeams =  handleDbRequests.selectAllTeams(tenantId);
            loadTenantTeamsForOneTenant(allTeams, tenantId);
        }
    }

    public void loadTenantTeamsForOneTenant(List<Team> allTeams, Integer tenantId) {
        if(allTeams == null)
            allTeams =  handleDbRequests.selectAllTeams(tenantId);

        HashMap<Integer, List<String>> teamsAndAllowedEnvs = new HashMap<>();
        HashMap<Integer, String> teamsAndNames = new HashMap<>();

        List<Team> teamList = allTeams.stream()
                .filter(team-> team.getTenantId().equals(tenantId))
                .collect(Collectors.toList());

        for (Team team : teamList) {
            teamsAndAllowedEnvs.put(team.getTeamId(), envsOfTenantsMap.get(tenantId));
            teamsAndNames.put(team.getTeamId(), team.getTeamname());
        }
        teamsAndNames.put(1, "All teams");

        teamsAndAllowedEnvsPerTenant.put(tenantId, teamsAndAllowedEnvs);
        teamIdAndNamePerTenant.put(tenantId, teamsAndNames);
    }

    public HashMap<Integer, KwTenantConfigModel> getTenantConfig(){
        return tenantConfig;
    }

    public void setTenantConfig(TenantConfig config){
        KwTenantConfigModel tenantModel = config.getTenantModel();
        if(tenantModel != null)
            tenantConfig.put(getTenantIdFromName(tenantModel.getTenantName()), tenantModel);
    }

    private void loadKwPropertiesforAllTenants(){
        HashMap<Integer, HashMap<String, HashMap<String, String>>> kwPropertiesMap = handleDbRequests.selectAllKwProperties();
        if(kwPropertiesMap.size() == 0)
        {
            log.info("Kafkawize Properties not loaded into database. Shutting down !!");
            shutdownApp();
        }
        kwPropertiesMapPerTenant = new HashMap<>();
        for (Integer tenantId : tenantMap.keySet()) {
            loadKwPropsPerOneTenant(kwPropertiesMap, tenantId);
        }
    }

    public void loadKwPropsPerOneTenant(HashMap<Integer, HashMap<String, HashMap<String, String>>> kwPropertiesMap, Integer tenantId) {
        if(kwPropertiesMap == null)
            kwPropertiesMap = handleDbRequests.selectAllKwProperties();

        kwPropertiesMapPerTenant.put(tenantId, kwPropertiesMap.get(tenantId));

        updateKwTenantConfigPerTenant(tenantId);
    }

    private void loadTenants(){
        List<KwTenants> tenants = handleDbRequests.getTenants();
        tenantMap = new HashMap<>();
        tenantFullMap = new HashMap<>();
        tenants.forEach(tenant -> tenantMap.put(tenant.getTenantId(), tenant.getTenantName()));
        tenants.forEach(tenant -> tenantFullMap.put(tenant.getTenantId(), tenant));
    }

    public void loadOneTenant(int tenantId){
        Optional<KwTenants> tenants = handleDbRequests.getTenants().stream().filter(tenant -> tenant.getTenantId() == tenantId).findFirst();
        tenants.ifPresent(kwTenants -> {
            tenantFullMap.put(kwTenants.getTenantId(), kwTenants);
            tenantMap.put(tenantId, kwTenants.getTenantName());
        });
    }

    public KwTenants getTenantFullConfig(int tenantId){
        return tenantFullMap.get(tenantId);
    }

    private void loadClustersForAllTenants(){
        List<KwClusters> kafkaClusters;
        List<KwClusters> schemaRegistryClusters;
        List<KwClusters> kafkaConnectClusters;

        kwKafkaClustersPertenant = new HashMap<>();
        kwSchemaRegClustersPertenant = new HashMap<>();
        kwKafkaConnectClustersPertenant = new HashMap<>();
        kwAllClustersPertenant = new HashMap<>();

        for (Integer tenantId : tenantMap.keySet()) {
            kafkaClusters = handleDbRequests.getAllClusters("kafka", tenantId);
            schemaRegistryClusters = handleDbRequests.getAllClusters("schemaregistry", tenantId);
            kafkaConnectClusters = handleDbRequests.getAllClusters("kafkaconnect", tenantId);

            loadClustersForOneTenant(kafkaClusters, schemaRegistryClusters, kafkaConnectClusters, tenantId);
        }
    }

    public void loadClustersForOneTenant(List<KwClusters> kafkaClusters, List<KwClusters> schemaRegistryClusters,
                                          List<KwClusters> kafkaConnectClusters, Integer tenantId) {
        if(kafkaClusters == null)
            kafkaClusters = handleDbRequests.getAllClusters("kafka", tenantId);
        if(schemaRegistryClusters == null)
            schemaRegistryClusters = handleDbRequests.getAllClusters("schemaregistry", tenantId);
        if(kafkaConnectClusters == null)
            kafkaConnectClusters = handleDbRequests.getAllClusters("kafkaconnect", tenantId);

        HashMap<Integer, KwClusters> kwKafkaClusters = new HashMap<>();
        HashMap<Integer, KwClusters> kwSchemaRegClusters = new HashMap<>();
        HashMap<Integer, KwClusters> kwKafkaConnectClusters = new HashMap<>();
        HashMap<Integer, KwClusters> kwAllClusters = new HashMap<>();

        kafkaClusters.forEach(cluster -> {
            kwKafkaClusters.put(cluster.getClusterId(), cluster);
            kwAllClusters.put(cluster.getClusterId(), cluster);
        });
        kwKafkaClustersPertenant.put(tenantId, kwKafkaClusters);

        schemaRegistryClusters.forEach(cluster -> {
            kwSchemaRegClusters.put(cluster.getClusterId(), cluster);
            kwAllClusters.put(cluster.getClusterId(), cluster);
        });
        kwSchemaRegClustersPertenant.put(tenantId, kwSchemaRegClusters);

        kafkaConnectClusters.forEach(cluster -> {
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
        loadTenantTeamsForAllTenants();
    }

    private void loadRequestTypeStatuses() {
        reqStatusList = new ArrayList<>();
        reqStatusList.add("all");
        for (RequestStatus value : RequestStatus.values()) {
            reqStatusList.add(value.name());
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
        List<Env> kafkaEnvList = handleDbRequests.selectAllKafkaEnvs(tenantId).stream().filter(env->env.getEnvExists().equals("true")).collect(Collectors.toList());
        List<Env> schemaEnvList = handleDbRequests.selectAllSchemaRegEnvs(tenantId).stream().filter(env->env.getEnvExists().equals("true")).collect(Collectors.toList());
        List<Env> kafkaConnectEnvList = handleDbRequests.selectAllKafkaConnectEnvs(tenantId).stream().filter(env->env.getEnvExists().equals("true")).collect(Collectors.toList());
        List<Env> allEnvList = new ArrayList<>();
        allEnvList.addAll(kafkaEnvList);
        allEnvList.addAll(schemaEnvList);
        allEnvList.addAll(kafkaConnectEnvList);

        kafkaEnvListPerTenant.put(tenantId, kafkaEnvList);
        schemaRegEnvListPerTenant.put(tenantId, schemaEnvList);
        kafkaConnectEnvListPerTenant.put(tenantId, kafkaConnectEnvList);
        allEnvListPerTenant.put(tenantId, allEnvList);

        List<Env> kafkaEnvTenantList = kafkaEnvList.stream().filter(kafkaEnv -> kafkaEnv.getTenantId().equals(tenantId)).collect(Collectors.toList());
        HashMap<String, HashMap<String, List<String>>> envParamsMap = new HashMap<>();

        HashMap<String, List<String>> oneEnvParamsMap;
        for (Env env : kafkaEnvTenantList) {
            oneEnvParamsMap = new HashMap<>();
            String envParams = env.getOtherParams();

            String[] params = envParams.split(",");
            String defaultPartitions = null, defaultRf = null;
            for (String param : params) {
                if (param.startsWith("default.partitions")) {
                    defaultPartitions = param.substring(param.indexOf("=") + 1);
                    List<String> defPartitionsList = new ArrayList<>();
                    defPartitionsList.add(defaultPartitions);
                    oneEnvParamsMap.put("defaultPartitions", defPartitionsList);
                } else if (param.startsWith("max.partitions")) {
                    String maxPartitions = param.substring(param.indexOf("=") + 1);
                    int maxPartitionsInt = Integer.parseInt(maxPartitions);
                    List<String> partitions = new ArrayList<>();

                    for (int i = 1; i < maxPartitionsInt + 1; i++) {
                        if (defaultPartitions != null && defaultPartitions.equals(i + ""))
                            partitions.add(i + " (default)");
                        else
                            partitions.add(i + "");
                    }
                    oneEnvParamsMap.put("partitionsList", partitions);
                } else if (param.startsWith("default.replication.factor")) {
                    defaultRf = param.substring(param.indexOf("=") + 1);
                    List<String> repFactorList = new ArrayList<>();
                    repFactorList.add(defaultRf);
                    oneEnvParamsMap.put("defaultRepFactor", repFactorList);
                }
                else if (param.startsWith("max.replication.factor")) {
                    String maxRf = param.substring(param.indexOf("=") + 1);
                    int maxRfInt = Integer.parseInt(maxRf);
                    List<String> rf = new ArrayList<>();

                    for (int i = 1; i < maxRfInt + 1; i++) {
                        if (defaultRf != null && defaultRf.equals(i + ""))
                            rf.add(i + " (default)");
                        else
                            rf.add(i + "");
                    }

                    oneEnvParamsMap.put("replicationFactorList", rf);
                }else if (param.startsWith("topic.prefix")) {
                    String topicPrefix = param.substring(param.indexOf("=") + 1);
                    List<String> topicPrefixList = new ArrayList<>();
                    topicPrefixList.add(topicPrefix);
                    oneEnvParamsMap.put("topicPrefix", topicPrefixList);
                } else if (param.startsWith("topic.suffix")) {
                    String topicSuffix = param.substring(param.indexOf("=") + 1);
                    List<String> topicSuffixList = new ArrayList<>();
                    topicSuffixList.add(topicSuffix);
                    oneEnvParamsMap.put("topicSuffix", topicSuffixList);
                }
            }

            envParamsMap.put(env.getId(), oneEnvParamsMap);
        }
        envParamsMapPerTenant.put(tenantId, envParamsMap);
    }

    public HashMap<String, List<String>> getRolesPermissionsPerTenant(int tenantId) {
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

    public void loadRolesPermissionsOneTenant(List<KwRolesPermissions> rolesPermissions, Integer tenantId) {
        if(rolesPermissions == null){
            rolesPermissions = handleDbRequests.getRolesPermissions();
        }

        List<KwRolesPermissions> rolesPermsList = rolesPermissions.stream()
                .filter(rolePerms -> rolePerms.getTenantId() == tenantId).collect(Collectors.toList());
        List<String> tmpList;
        HashMap<String, List<String>> rolesPermsMap = new HashMap<>();
        for (KwRolesPermissions rolesPermission : rolesPermsList) {
            if(!rolesPermsMap.containsKey(rolesPermission.getRoleId())){
                tmpList = new ArrayList<>();
            }
            else{
                tmpList = rolesPermsMap.get(rolesPermission.getRoleId());
            }
            tmpList.add(rolesPermission.getPermission());
            rolesPermsMap.put(rolesPermission.getRoleId(), tmpList);
        }
        rolesPermsMapPerTenant.put(tenantId, rolesPermsMap);
    }

    public HashMap<Integer, List<EnvModel>> getEnvModelsClustersStatusAllTenants(){
        return envModelsClustersStatus;
    }

    public List<String> getRequestStatusList(){
        return reqStatusList;
    }

    public void updateKwTenantConfigPerTenant(Integer tenantId) {
        try {
            String TENANT_CONFIG = "kafkawize.tenant.config";
            if(kwPropertiesMapPerTenant.get(tenantId)!=null){
                if(kwPropertiesMapPerTenant.get(tenantId).get(TENANT_CONFIG)!=null){
                    String kwTenantConfig =  kwPropertiesMapPerTenant.get(tenantId).get(TENANT_CONFIG).get("kwvalue");
                    ObjectMapper objectMapper = new ObjectMapper();
                    TenantConfig dynamicObj;

                    dynamicObj = objectMapper.readValue(kwTenantConfig, TenantConfig.class);
                    setTenantConfig(dynamicObj);
                }
            }
        } catch (IOException e) {
            log.info("Error loading tenant config {}",e.getMessage());
        }
    }

    public void deleteCluster(int tenantId) {
        loadClustersForOneTenant(null, null, null, tenantId);
        loadEnvMapForOneTenant(tenantId);
        loadEnvsForOneTenant(tenantId);
    }

    // delete users from both users tables of tenant
    // delete teams of tenant
    // delete envs of tenant
    // delete rolesperms
    // delete kwprops
    // delete clusters of tenant
    // delete tenant
    public String deleteTenant(int tenantId) {
        tenantMap.remove(tenantId);
        teamsAndAllowedEnvsPerTenant.remove(tenantId);
        kwPropertiesMapPerTenant.remove(tenantId);
        rolesPermsMapPerTenant.remove(tenantId);
        envParamsMapPerTenant.remove(tenantId);

        kwKafkaClustersPertenant.remove(tenantId);
        kwSchemaRegClustersPertenant.remove(tenantId);
        kwKafkaConnectClustersPertenant.remove(tenantId);
        kwAllClustersPertenant.remove(tenantId);
        return "success";
    }
}
