package com.kafkamgt.uiapi.service;

import static com.kafkamgt.uiapi.model.RolesType.SUPERADMIN;
import static com.kafkamgt.uiapi.service.KwConstants.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.*;
import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnvsClustersTenantsControllerService {

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private ClusterApiService clusterApiService;

  @Autowired private UsersTeamsControllerService usersTeamsControllerService;

  @Value("${kafkawize.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${kafkawize.saas.ssl.aclcommand:acl}")
  private String aclCommandSsl;

  @Value("${kafkawize.saas.plaintext.aclcommand:acl}")
  private String aclCommandPlaintext;

  @Value("${kafkawize.prizelist.pertenant}")
  private String extensionPeriods;

  @Value("${kafkawize.saas.ssl.pubkey:pubkey.zip}")
  private String kwPublicKey;

  @Value("${kafkawize.saas.ssl.clientcerts.location:./tmp/}")
  private String clientCertsLocation;

  @Value("${kafkawize.max.tenants:1000}")
  private int maxNumberOfTenantsCanBeCreated;

  @Autowired private DefaultDataService defaultDataService;

  @Autowired
  public void setServices(ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public synchronized EnvModel getEnvDetails(String envSelected, String clusterType) {
    log.debug("getEnvDetails {}", envSelected);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      // tenant filtering
      if (!getEnvsFromUserId().contains(envSelected)) return null;
    }

    Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envSelected, tenantId);
    if (env != null && env.getEnvExists().equals("false")) {
      return null;
    }

    if (env != null) {
      EnvModel envModel = new EnvModel();
      copyProperties(env, envModel);
      envModel.setClusterName(
          manageDatabase
              .getClusters(clusterType, tenantId)
              .get(envModel.getClusterId())
              .getClusterName());
      envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId()));

      extractKwEnvParameters(envModel);
      return envModel;
    }
    return null;
  }

  private void extractKwEnvParameters(EnvModel env) {
    String defPartns = "",
        defMaxPartns = "",
        defaultRf = "",
        maxRf = "",
        topicPrefix = "",
        topicSuffix = "";
    String otherParams = env.getOtherParams();
    String[] params;
    try {
      if (otherParams != null) {
        params = otherParams.split(",");
        for (String param : params) {
          if (param.startsWith("default.partitions")) {
            defPartns = param.substring(param.indexOf("=") + 1);
          } else if (param.startsWith("max.partitions")) {
            defMaxPartns = param.substring(param.indexOf("=") + 1);
          } else if (param.startsWith("default.replication.factor")) {
            defaultRf = param.substring(param.indexOf("=") + 1);
          } else if (param.startsWith("max.replication.factor")) {
            maxRf = param.substring(param.indexOf("=") + 1);
          } else if (param.startsWith("topic.prefix")) {
            topicPrefix = param.substring(param.indexOf("=") + 1);
          } else if (param.startsWith("topic.suffix")) {
            topicSuffix = param.substring(param.indexOf("=") + 1);
          }
        }
        env.setDefaultPartitions(defPartns);
        env.setMaxPartitions(defMaxPartns);
        env.setDefaultReplicationFactor(defaultRf);
        env.setMaxReplicationFactor(maxRf);
        env.setTopicprefix(topicPrefix);
        env.setTopicsuffix(topicSuffix);
      }
    } catch (Exception e) {
      log.error("Unable to set topic partitions, setting default from properties.", e);
    }
  }

  public UserInfoModel getUserDetails(String userId) {
    UserInfoModel userInfoModel = new UserInfoModel();
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
    if (userInfo != null) {
      copyProperties(userInfo, userInfoModel);
      userInfoModel.setUserPassword("*******");
      return userInfoModel;
    } else return null;
  }

  public List<KwClustersModel> getClusters(String typeOfCluster) {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<KwClusters> clusters =
        new ArrayList<>(manageDatabase.getClusters(typeOfCluster, tenantId).values());
    List<KwClustersModel> clustersModels = new ArrayList<>();
    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    KwClustersModel tmpClusterModel;
    for (KwClusters cluster : clusters) {
      tmpClusterModel = new KwClustersModel();
      copyProperties(cluster, tmpClusterModel);
      KwClustersModel finalTmpClusterModel = tmpClusterModel;
      tmpClusterModel.setShowDeleteCluster(true);
      // set only for authorized users
      if (!commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
        if (allEnvList.stream()
            .anyMatch(env -> env.getClusterId().equals(finalTmpClusterModel.getClusterId()))) {
          tmpClusterModel.setShowDeleteCluster(false);
        }
      }

      clustersModels.add(tmpClusterModel);
    }

    return clustersModels;
  }

  public List<KwClustersModel> getClustersPaginated(
      String typeOfCluster, String clusterId, String pageNo, String searchClusterParam) {
    List<KwClustersModel> kwClustersModelList = getClusters("all");

    if (clusterId != null && !clusterId.equals("")) {
      kwClustersModelList =
          kwClustersModelList.stream()
              .filter(env -> (env.getClusterId() + "").toLowerCase().equals(clusterId))
              .collect(Collectors.toList());
    }

    if (searchClusterParam != null && !searchClusterParam.equals("")) {
      List<KwClustersModel> envListMap1 =
          kwClustersModelList.stream()
              .filter(
                  env ->
                      env.getClusterName().toLowerCase().contains(searchClusterParam.toLowerCase()))
              .collect(Collectors.toList());
      List<KwClustersModel> envListMap2 =
          kwClustersModelList.stream()
              .filter(
                  env ->
                      env.getBootstrapServers()
                          .toLowerCase()
                          .contains(searchClusterParam.toLowerCase()))
              .collect(Collectors.toList());
      List<KwClustersModel> envListMap3 =
          kwClustersModelList.stream()
              .filter(
                  env -> env.getProtocol().toLowerCase().contains(searchClusterParam.toLowerCase()))
              .collect(Collectors.toList());
      envListMap1.addAll(envListMap2);
      envListMap1.addAll(envListMap3);

      // remove duplicates
      kwClustersModelList =
          envListMap1.stream()
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.toCollection(
                          () -> new TreeSet<>(Comparator.comparing(KwClustersModel::getClusterId))),
                      ArrayList::new));
    }
    return getClustersModelsPaginated(pageNo, kwClustersModelList);
  }

  private List<KwClustersModel> getClustersModelsPaginated(
      String pageNo, List<KwClustersModel> envListMap) {
    List<KwClustersModel> envListMapUpdated = new ArrayList<>();

    int totalRecs = envListMap.size();
    int recsPerPage = 10;

    int totalPages =
        envListMap.size() / recsPerPage + (envListMap.size() % recsPerPage > 0 ? 1 : 0);

    int requestPageNo = Integer.parseInt(pageNo);
    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    for (int i = 0; i < totalRecs; i++) {

      if (i >= startVar && i < lastVar) {
        KwClustersModel mp = envListMap.get(i);

        mp.setTotalNoPages(totalPages + "");
        List<String> numList = new ArrayList<>();
        for (int k = 1; k <= totalPages; k++) {
          numList.add("" + k);
        }
        mp.setAllPageNos(numList);
        mp.setPublicKey(""); // remove public key from here
        envListMapUpdated.add(mp);
      }
    }
    return envListMapUpdated;
  }

  public List<HashMap<String, String>> getSyncEnvs() {
    log.debug("getSyncEnvs");
    Integer tenantId = getUserDetails(getUserName()).getTenantId();
    String syncCluster;
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      return new ArrayList<>();
    }

    HashMap<String, String> hMap;
    List<HashMap<String, String>> envsOnly = new ArrayList<>();
    List<EnvModel> envList = getKafkaEnvs();
    for (EnvModel env : envList) {
      hMap = new HashMap<>();
      hMap.put("id", env.getId());
      String baseClusterDropDownStr = " (Base Sync cluster)";
      if (syncCluster.equals(env.getId())) hMap.put("name", env.getName() + baseClusterDropDownStr);
      else hMap.put("name", env.getName());

      envsOnly.add(hMap);
    }

    return envsOnly;
  }

  public List<EnvModel> getEnvsForRequestTopicsCluster() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");
    String requestTopicsEnvs = mailService.getEnvProperty(tenantId, "REQUEST_TOPICS_OF_ENVS");

    String[] reqTopicsEnvs = requestTopicsEnvs.split(",");
    List<Env> listEnvs = manageDatabase.getKafkaEnvList(tenantId);
    List<EnvModel> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA.value, tenantId);

    envModelList =
        envModelList.stream()
            .filter(
                env -> {
                  boolean found = false;
                  for (String reqTopicEnv : reqTopicsEnvs) {
                    if (env.getId().equals(reqTopicEnv)) {
                      found = true;
                      break;
                    }
                  }
                  return found;
                })
            .collect(Collectors.toList());

    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public List<EnvModel> getEnvsForRequestTopicsClusterFiltered() {
    //        List<EnvModel> envModelList = getEnvsForRequestTopicsCluster();
    //        String userDetails = getUserDetails();
    //        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
    //
    //        if(userDetails!=null) {
    //            String teamName = reqsHandle.getUsersInfo(userDetails).getTeam();
    //            List<String> teamEnvList = getTeamDetails(teamName).getEnvList();
    //            if(teamEnvList != null && teamEnvList.size() > 0) {
    //                envModelList = envModelList.stream()
    //                        .filter(a -> teamEnvList.contains(a.getId()))
    //                        .collect(Collectors.toList());
    //            }
    //        }
    //        return envModelList;
    return getKafkaEnvs();
  }

  public List<EnvModel> getKafkaEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");

    List<Env> listEnvs = manageDatabase.getKafkaEnvList(tenantId);

    List<EnvModel> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA.value, tenantId);
    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    // set deletable only to authorized, and check for count
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      envModelList.forEach(
          envModel -> {
            envModel.setShowDeleteEnv(
                manageDatabase
                        .getHandleDbRequests()
                        .findAllKafkaComponentsCountForEnv(envModel.getId(), tenantId)
                    <= 0);
          });
    }

    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public List<EnvModel> getConnectorEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");

    List<Env> listEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);

    //        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(),
    // PermissionType.VIEW_EDIT_ALL_ENVS_CLUSTERS_TENANTS)){
    //            List<String> allowedEnvIdList = getEnvsFromUserId();
    //            listEnvs = listEnvs.stream()
    //                    .filter(env -> allowedEnvIdList.contains(env.getId()))
    //                    .collect(Collectors.toList());
    //        }

    List<EnvModel> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT.value, tenantId);

    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public HashMap<String, List<EnvModel>> getEnvsStatus() {
    Integer tenantId = getUserDetails(getUserName()).getTenantId();
    //        Integer tenantId =
    // getTeamDetails(getUserDetails(getUserDetails()).getTeam()).getTenantId();
    HashMap<Integer, List<EnvModel>> allTenantsEnvModels =
        manageDatabase.getEnvModelsClustersStatusAllTenants();
    HashMap<String, List<EnvModel>> allTenantsEnvModelsUpdated = new HashMap<>();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      allTenantsEnvModelsUpdated.put(
          manageDatabase.getTenantMap().get(tenantId), allTenantsEnvModels.get(tenantId));
    } else {
      for (Integer tenantIdInt : allTenantsEnvModels.keySet()) {
        allTenantsEnvModelsUpdated.put(
            manageDatabase.getTenantMap().get(tenantIdInt), allTenantsEnvModels.get(tenantIdInt));
      }
    }

    return allTenantsEnvModelsUpdated;
  }

  public List<EnvModel> getEnvsPaginated(String envId, String pageNo, String searchEnvParam) {

    List<EnvModel> envListMap = getKafkaEnvs();

    if (envId != null && !envId.equals("")) {
      envListMap =
          envListMap.stream().filter(env -> env.getId().equals(envId)).collect(Collectors.toList());
    }

    if (searchEnvParam != null && !searchEnvParam.equals("")) {
      List<EnvModel> envListMap1 =
          envListMap.stream()
              .filter(env -> env.getName().toLowerCase().contains(searchEnvParam.toLowerCase()))
              .collect(Collectors.toList());
      List<EnvModel> envListMap2 =
          envListMap.stream()
              .filter(
                  env -> env.getClusterName().toLowerCase().contains(searchEnvParam.toLowerCase()))
              .collect(Collectors.toList());
      List<EnvModel> envListMap3 =
          envListMap.stream()
              .filter(
                  env -> env.getOtherParams().toLowerCase().contains(searchEnvParam.toLowerCase()))
              .collect(Collectors.toList());
      List<EnvModel> envListMap4 =
          envListMap.stream()
              .filter(
                  env ->
                      manageDatabase
                          .getTenantMap()
                          .get(env.getTenantId())
                          .toLowerCase()
                          .contains(searchEnvParam.toLowerCase()))
              .collect(Collectors.toList());
      envListMap1.addAll(envListMap2);
      envListMap1.addAll(envListMap3);
      envListMap1.addAll(envListMap4);

      // remove duplicates
      envListMap =
          envListMap1.stream()
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.toCollection(
                          () -> new TreeSet<>(Comparator.comparing(EnvModel::getId))),
                      ArrayList::new));
    }

    return getEnvModelsPaginated(pageNo, envListMap);
  }

  private List<EnvModel> getEnvModelsPaginated(String pageNo, List<EnvModel> envListMap) {
    List<EnvModel> envListMapUpdated = new ArrayList<>();

    int totalRecs = envListMap.size();
    int recsPerPage = 10;

    int totalPages =
        envListMap.size() / recsPerPage + (envListMap.size() % recsPerPage > 0 ? 1 : 0);

    int requestPageNo = Integer.parseInt(pageNo);
    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    for (int i = 0; i < totalRecs; i++) {

      if (i >= startVar && i < lastVar) {
        EnvModel mp = envListMap.get(i);

        mp.setTotalNoPages(totalPages + "");
        List<String> numList = new ArrayList<>();
        for (int k = 1; k <= totalPages; k++) {
          numList.add("" + k);
        }
        mp.setAllPageNos(numList);
        envListMapUpdated.add(mp);
      }
    }
    return envListMapUpdated;
  }

  private List<EnvModel> getEnvModels(List<Env> listEnvs, String clusterType, int tenantId) {
    List<EnvModel> envModelList = new ArrayList<>();
    EnvModel envModel;
    KwClusters kwCluster;
    for (Env listEnv : listEnvs) {
      kwCluster = manageDatabase.getClusters(clusterType, tenantId).get(listEnv.getClusterId());
      if (kwCluster != null) {
        envModel = new EnvModel();
        copyProperties(listEnv, envModel);
        envModel.setClusterName(
            manageDatabase
                .getClusters(clusterType, tenantId)
                .get(envModel.getClusterId())
                .getClusterName());
        envModelList.add(envModel);
      } else log.error("Error : Environment/cluster not loaded :{}", listEnv);
    }
    return envModelList;
  }

  public HashMap<String, List<String>> getEnvParams(String targetEnv) {
    return manageDatabase
        .getEnvParamsMap(commonUtilsService.getTenantId(getUserName()))
        .get(targetEnv);
  }

  public List<EnvModel> getSchemaRegEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    List<Env> listEnvs = manageDatabase.getSchemaRegEnvList(tenantId);

    List<EnvModel> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.SCHEMA_REGISTRY.value, tenantId);

    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    // set deletable only to authorized, and check for count
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      envModelList.forEach(
          envModel -> {
            envModel.setShowDeleteEnv(
                manageDatabase
                        .getHandleDbRequests()
                        .findAllConnectorComponentsCountForEnv(envModel.getId(), tenantId)
                    <= 0);
          });
    }

    return envModelList;
  }

  public List<EnvModel> getKafkaConnectEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    List<Env> listEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      List<String> allowedEnvIdList = getEnvsFromUserId();
      listEnvs =
          listEnvs.stream()
              .filter(env -> allowedEnvIdList.contains(env.getId()))
              .collect(Collectors.toList());
    }

    List<EnvModel> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT.value, tenantId);

    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    // set deletable only to authorized, and check for count
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      envModelList.forEach(
          envModel -> {
            envModel.setShowDeleteEnv(
                manageDatabase
                        .getHandleDbRequests()
                        .findAllConnectorComponentsCountForEnv(envModel.getId(), tenantId)
                    <= 0);
          });
    }

    return envModelList;
  }

  public List<EnvModel> getSchemaRegEnvsStatus() {
    List<Env> listEnvs =
        manageDatabase
            .getHandleDbRequests()
            .selectAllSchemaRegEnvs(commonUtilsService.getTenantId(getUserName()));
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<String> allowedEnvIdList = getEnvsFromUserId();
    listEnvs =
        listEnvs.stream()
            .filter(env -> allowedEnvIdList.contains(env.getId()))
            .collect(Collectors.toList());

    List<Env> newListEnvs = new ArrayList<>();
    for (Env oneEnv : listEnvs) {
      String status;

      if (manageDatabase
          .getClusters(KafkaClustersType.SCHEMA_REGISTRY.value, tenantId)
          .get(oneEnv.getClusterId())
          .getProtocol()
          .equalsIgnoreCase("plaintext"))
        status =
            clusterApiService.getSchemaClusterStatus(
                manageDatabase
                    .getClusters(KafkaClustersType.SCHEMA_REGISTRY.value, tenantId)
                    .get(oneEnv.getClusterId())
                    .getBootstrapServers(),
                tenantId);
      else status = "NOT_KNOWN";
      oneEnv.setEnvStatus(status);
      newListEnvs.add(oneEnv);
    }

    return getEnvModels(newListEnvs, KafkaClustersType.SCHEMA_REGISTRY.value, tenantId);
  }

  public String addNewEnv(EnvModel newEnv) {
    log.info("addNewEnv {}", newEnv);
    int tenantId = getUserDetails(getUserName()).getTenantId();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS))
      return "{\"result\":\"Not Authorized\"}";

    newEnv.setTenantId(tenantId);

    if (newEnv.getClusterId() == null) return "{\"result\":\"Please select a cluster.\"}";

    if (newEnv.getName().length() > 3 && newEnv.getType().equals(KafkaClustersType.KAFKA.value))
      newEnv.setName(newEnv.getName().substring(0, 3));

    newEnv.setName(newEnv.getName().toUpperCase());
    String envIdAlreadyExistsInDeleteStatus = "";

    if (newEnv.getId() == null || newEnv.getId().length() == 0) {
      List<Env> kafkaEnvs = manageDatabase.getKafkaEnvList(tenantId);
      List<Env> schemaEnvs = manageDatabase.getSchemaRegEnvList(tenantId);
      List<Env> kafkaConnectEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);

      List<Integer> idListInts = new ArrayList<>();

      kafkaEnvs.forEach(a -> idListInts.add(Integer.valueOf(a.getId())));
      schemaEnvs.forEach(a -> idListInts.add(Integer.valueOf(a.getId())));
      kafkaConnectEnvs.forEach(a -> idListInts.add(Integer.valueOf(a.getId())));

      Optional<Integer> updatedList = idListInts.stream().max(Comparator.naturalOrder());
      if (updatedList.isPresent()) {
        int nextId = updatedList.get() + 1;
        newEnv.setId(String.valueOf(nextId));
      } else newEnv.setId("1");

      // Same name per type (kafka, kafkaconnect) in tenant not posssible.
      List<Env> envActualList = manageDatabase.getHandleDbRequests().selectAllEnvs(tenantId);
      boolean envNameAlreadyPresent =
          envActualList.stream()
              .anyMatch(
                  en ->
                      en.getName().equals(newEnv.getName())
                          && en.getType().equals(newEnv.getType())
                          && en.getTenantId().equals(newEnv.getTenantId())
                          && en.getEnvExists().equals("true")); // 504 change
      if (envNameAlreadyPresent) {
        return "{\"result\":\"Failure "
            + "Please choose a different name. This environment name already exists."
            + "\"}";
      } else if (envActualList.stream()
          .anyMatch(
              en ->
                  en.getName().equals(newEnv.getName())
                      && en.getType().equals(newEnv.getType())
                      && en.getTenantId().equals(newEnv.getTenantId())
                      && en.getEnvExists().equals("false"))) {
        Optional<Env> envAlreadyExistsInDeleted =
            envActualList.stream()
                .filter(
                    en ->
                        en.getName().equals(newEnv.getName())
                            && en.getType().equals(newEnv.getType())
                            && en.getTenantId().equals(newEnv.getTenantId())
                            && en.getEnvExists().equals("false"))
                .findFirst();
        if (envAlreadyExistsInDeleted.isPresent())
          envIdAlreadyExistsInDeleteStatus = envAlreadyExistsInDeleted.get().getId();
      }
    }

    Env env = new Env();
    copyProperties(newEnv, env);
    if (!envIdAlreadyExistsInDeleteStatus.equals("")) env.setId(envIdAlreadyExistsInDeleteStatus);
    env.setEnvExists("true");

    try {
      String result = manageDatabase.getHandleDbRequests().addNewEnv(env);
      commonUtilsService.updateMetadata(
          tenantId, EntityType.ENVIRONMENT, MetadataOperationType.CREATE);
      return "{\"result\":\"" + result + "\"}";
    } catch (Exception e) {
      log.error("Exception:", e);
      return "{\"result\":\"failure " + e.getMessage() + "\"}";
    }
  }

  public HashMap<String, String> addNewCluster(KwClustersModel kwClustersModel) {
    log.info("addNewCluster {}", kwClustersModel.getClusterName());
    HashMap<String, String> resultMap = new HashMap<>();

    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }

    AtomicBoolean clusterNameAlreadyExists = new AtomicBoolean(false);
    if (kwClustersModel.getClusterId() == null) {
      manageDatabase
          .getClusters("all", tenantId)
          .forEach(
              (k, v) -> {
                if (v.getClusterName().equals(kwClustersModel.getClusterName())
                    && v.getClusterType().equals(kwClustersModel.getClusterType())) {
                  clusterNameAlreadyExists.set(true);
                }
              });

      if (clusterNameAlreadyExists.get()) {
        resultMap.put(
            "result", "Failure. Please choose a different name. This cluster name already exists.");
        return resultMap;
      }
    }
    KwClusters kwCluster = new KwClusters();
    copyProperties(kwClustersModel, kwCluster);
    kwCluster.setTenantId(tenantId);
    kwCluster.setClusterName(kwCluster.getClusterName().toUpperCase());

    // only for new cluster requests on saas
    if (kwCluster.getProtocol().equals("SSL")
        && kwCluster.getClusterId() == null
        && kwInstallationType.equals("saas")) {
      if (!savePublicKey(kwClustersModel, resultMap, tenantId, kwCluster)) {
        resultMap.put("result", "Failure. Unable to save public key.");
        return resultMap;
      }
    }

    String result = manageDatabase.getHandleDbRequests().addNewCluster(kwCluster);
    if (result.equals("success")) {
      commonUtilsService.updateMetadata(tenantId, EntityType.CLUSTER, MetadataOperationType.CREATE);
    }
    resultMap.put("result", result);
    return resultMap;
  }

  private boolean savePublicKey(
      KwClustersModel kwClustersModel,
      HashMap<String, String> resultMap,
      int tenantId,
      KwClusters kwCluster) {
    try {
      kwCluster.setPublicKey(kwClustersModel.getPublicKey());
      boolean pubKeyFileCreated =
          createPublicKeyFile(
              kwCluster.getClusterName().toUpperCase(), tenantId, kwClustersModel.getPublicKey());
      if (pubKeyFileCreated) {
        return commonUtilsService.addPublicKeyToTrustStore(
            kwCluster.getClusterName().toUpperCase(), tenantId);
      } else return false;
    } catch (Exception e) {
      log.error(
          tenantId + " tenantId. Error in adding cluster --" + kwClustersModel.getClusterName(), e);
      return false;
    }
  }

  private boolean createPublicKeyFile(String clusterName, int tenantId, String publicKey) {
    File clientCertFile;
    String fileName = clusterName + tenantId + ".pem";
    FileOutputStream outputStream = null;
    try {
      clientCertFile = new File(clientCertsLocation + "/" + fileName);

      if (clientCertFile.exists()) clientCertFile.delete();

      outputStream = new FileOutputStream(clientCertFile);
      outputStream.write(Base64.getDecoder().decode(publicKey));
    } catch (Exception e) {
      log.error(
          "Unable to create public key file "
              + clusterName
              + " tenant id "
              + commonUtilsService.getTenantId(getUserName()),
          e);
      return false;
    } finally {
      try {
        if (outputStream != null) outputStream.close();
      } catch (Exception ex) {
        log.error("Error in closing the BufferedWriter ", ex);
      }
    }
    return true;
  }

  public String deleteCluster(String clusterId) {
    log.info("deleteCluster {}", clusterId);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS))
      return "{\"result\":\"Not Authorized\"}";

    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    if (allEnvList.stream()
        .anyMatch(env -> env.getClusterId().equals(Integer.parseInt(clusterId)))) {
      return "{\"result\":\"Not allowed to delete this cluster, as there are associated environments.\"}";
    }

    try {
      String result =
          manageDatabase.getHandleDbRequests().deleteCluster(Integer.parseInt(clusterId), tenantId);
      commonUtilsService.updateMetadata(tenantId, EntityType.CLUSTER, MetadataOperationType.DELETE);
      return "{\"result\":\"" + result + "\"}";
    } catch (Exception e) {
      log.error("Exception:", e);
      return "{\"result\":\"failure " + e.getMessage() + "\"}";
    }
  }

  public HashMap<String, String> deleteEnvironment(String envId, String envType) {
    HashMap<String, String> resultMap = new HashMap<>();

    log.info("deleteEnvironment {}", envId);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }

    switch (envType) {
      case "kafka":
        if (manageDatabase.getHandleDbRequests().findAllKafkaComponentsCountForEnv(envId, tenantId)
            > 0) {
          String notAllowed =
              "Not allowed to delete this environment, as there are associated topics/acls/requests.";
          resultMap.put("result", notAllowed);
          return resultMap;
        }
        break;
      case "kafkaconnect":
        if (manageDatabase
                .getHandleDbRequests()
                .findAllConnectorComponentsCountForEnv(envId, tenantId)
            > 0) {
          String notAllowed =
              "Not allowed to delete this environment, as there are associated connectors/requests.";
          resultMap.put("result", notAllowed);
          return resultMap;
        }
        break;
      case "schemaregistry":
        if (manageDatabase.getHandleDbRequests().findAllSchemaComponentsCountForEnv(envId, tenantId)
            > 0) {
          String notAllowed =
              "Not allowed to delete this environment, as there are associated schemaregistry/requests.";
          resultMap.put("result", notAllowed);
          return resultMap;
        }
        break;
    }

    try {
      String result =
          manageDatabase.getHandleDbRequests().deleteEnvironmentRequest(envId, tenantId);
      commonUtilsService.updateMetadata(
          tenantId, EntityType.ENVIRONMENT, MetadataOperationType.DELETE);

      resultMap.put("result", result);
      return resultMap;
    } catch (Exception e) {
      log.error("Exception:", e);
      resultMap.put("result", "failure " + e.getMessage());
      return resultMap;
    }
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  public List<KwTenantModel> getAllTenants() {
    if (manageDatabase
            .getHandleDbRequests()
            .getUsersInfo(getUserName())
            .getRole()
            .equals(SUPERADMIN.name())
        && commonUtilsService.getTenantId(getUserName()) == DEFAULT_TENANT_ID) {
      HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
      List<KwTenants> tenants = dbHandle.getTenants();
      List<KwTenantModel> tenantModels = new ArrayList<>();

      List<UserInfo> allUsers = dbHandle.selectAllUsersAllTenants();

      KwTenantModel kwTenantModel;
      for (KwTenants tenant : tenants) {
        kwTenantModel = new KwTenantModel();
        copyProperties(tenant, kwTenantModel);

        Optional<UserInfo> userFound =
            allUsers.stream()
                .filter(
                    userInfo ->
                        userInfo.getTenantId() == tenant.getTenantId()
                            && userInfo.getRole().equals(SUPERADMIN_ROLE))
                .findFirst();
        if (userFound.isPresent()) {
          kwTenantModel.setEmailId(userFound.get().getMailid());
        }

        kwTenantModel.setLicenseExpiryDate(tenant.getLicenseExpiry() + "");
        kwTenantModel.setActiveTenant(Boolean.parseBoolean(tenant.getIsActive()));
        kwTenantModel.setInTrialPhase(Boolean.parseBoolean(tenant.getInTrial()));

        tenantModels.add(kwTenantModel);
      }
      return tenantModels;
    } else {
      return new ArrayList<>();
    }
  }

  // tenant filtering
  private List<String> getEnvsFromUserId() {
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    Integer userTeamId = getMyTeamId(userDetails);
    List<String> listEnvs = manageDatabase.getTeamsAndAllowedEnvs(userTeamId, tenantId);
    if (listEnvs == null) return new ArrayList<>();
    return listEnvs;
  }

  public KwClustersModel getClusterDetails(String clusterId) {
    try {
      int tenantId = commonUtilsService.getTenantId(getUserName());
      KwClusters kwClusters =
          manageDatabase
              .getHandleDbRequests()
              .getClusterDetails(Integer.parseInt(clusterId), tenantId);
      if (kwClusters != null) {
        KwClustersModel kwClustersModel = new KwClustersModel();
        copyProperties(kwClusters, kwClustersModel);

        return kwClustersModel;
      }
      return null;
    } catch (Exception e) {
      log.error("Exception:", e);
      return null;
    }
  }

  public List<String> getStandardEnvNames() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String standardNames =
        manageDatabase.getKwPropertyValue("kafkawize.envs.standardnames", tenantId);
    List<String> envList = Arrays.asList(standardNames.split(","));
    Collections.sort(envList);
    return envList;
  }

  public HashMap<String, String> addTenantId(KwTenantModel kwTenantModel, boolean isExternal) {

    HashMap<String, String> addTenantStatus = new HashMap<>();

    if (manageDatabase.getHandleDbRequests().getTenants().size()
        >= maxNumberOfTenantsCanBeCreated) {
      addTenantStatus.put("result", "Maximum tenants reached.");
      return addTenantStatus;
    }

    if (isExternal
        && commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_TENANT)) {
      addTenantStatus.put("result", "Not Authorized");
      return addTenantStatus;
    }

    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantName(kwTenantModel.getTenantName());
    kwTenants.setTenantDesc(kwTenantModel.getTenantDesc());
    kwTenants.setInTrial(kwTenantModel.isInTrialPhase() + "");
    kwTenants.setContactPerson(kwTenantModel.getContactPerson());
    kwTenants.setOrgName("Our Organization");
    if (isExternal) kwTenantModel.setActiveTenant(true);

    if (kwTenantModel.isActiveTenant()) kwTenants.setIsActive("true");
    else kwTenants.setIsActive("false");

    if (kwInstallationType.equals("saas"))
      kwTenants.setLicenseExpiry(
          new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DAYS_TRIAL_PERIOD)));
    else
      kwTenants.setLicenseExpiry(
          new Timestamp(
              System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DAYS_EXPIRY_DEFAULT_TENANT)));

    addTenantStatus.put("result", manageDatabase.getHandleDbRequests().addNewTenant(kwTenants));
    int tenantId =
        manageDatabase.getHandleDbRequests().getTenants().stream()
            .filter(kwTenant -> kwTenant.getTenantName().equals(kwTenantModel.getTenantName()))
            .findFirst()
            .get()
            .getTenantId();

    addTenantStatus.put("tenantId", "" + tenantId);

    commonUtilsService.updateMetadata(tenantId, EntityType.TENANT, MetadataOperationType.CREATE);

    if (isExternal) {
      manageDatabase
          .getHandleDbRequests()
          .insertDefaultKwProperties(defaultDataService.createDefaultProperties(tenantId, ""));
      manageDatabase
          .getHandleDbRequests()
          .insertDefaultRolesPermissions(
              defaultDataService.createDefaultRolesPermissions(
                  tenantId, false, kwInstallationType));

      commonUtilsService.updateMetadata(
          tenantId, EntityType.ROLES_PERMISSIONS, MetadataOperationType.CREATE);
      commonUtilsService.updateMetadata(
          tenantId, EntityType.PROPERTIES, MetadataOperationType.CREATE);
    }

    return addTenantStatus;
  }

  public KwTenantModel getMyTenantInfo() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Optional<KwTenants> tenant = manageDatabase.getHandleDbRequests().getMyTenants(tenantId);
    KwTenantModel kwTenantModel = new KwTenantModel();
    if (tenant.isPresent()) {
      kwTenantModel.setTenantName(tenant.get().getTenantName());
      kwTenantModel.setLicenseExpiryDate(
          ((new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
              .format(tenant.get().getLicenseExpiry().getTime())));
      kwTenantModel.setContactPerson(tenant.get().getContactPerson());
      kwTenantModel.setInTrialPhase(tenant.get().getInTrial().equals("true"));
      long timeInMilliSeconds =
          tenant.get().getLicenseExpiry().getTime() - System.currentTimeMillis();
      long hours = TimeUnit.MILLISECONDS.toHours(timeInMilliSeconds);

      int days = (int) (hours / 24);
      int hoursN = (int) hours % 24;

      kwTenantModel.setNumberOfDays("" + days);
      kwTenantModel.setNumberOfHours("" + hoursN);
      kwTenantModel.setActiveTenant(tenant.get().getIsActive().equals("true"));
      kwTenantModel.setOrgName(tenant.get().getOrgName());

      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
        kwTenantModel.setAuthorizedToDelete(false);
      } else kwTenantModel.setAuthorizedToDelete(true);
    }
    return kwTenantModel;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public HashMap<String, String> deleteTenant() {
    HashMap<String, String> resultMap = new HashMap<>();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (tenantId == DEFAULT_TENANT_ID) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }
    String tenantName = manageDatabase.getTenantMap().get(tenantId);

    List<UserInfo> allUsers = manageDatabase.getHandleDbRequests().selectAllUsersInfo(tenantId);
    for (UserInfo userInfo : allUsers) {
      usersTeamsControllerService.deleteUser(userInfo.getUsername(), false); // internal delete
    }
    manageDatabase.getHandleDbRequests().deleteAllUsers(tenantId);

    manageDatabase.getHandleDbRequests().deleteAllTeams(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllEnvs(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllClusters(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllRolesPerms(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllKwProps(tenantId);
    manageDatabase.getHandleDbRequests().deleteTxnData(tenantId);

    String result = manageDatabase.getHandleDbRequests().disableTenant(tenantId);

    if (result.equals("success")) {
      commonUtilsService.updateMetadata(tenantId, EntityType.TENANT, MetadataOperationType.DELETE);
      resultMap.put("result", "success");
      resultMap.put("tenantId", tenantName);
      SecurityContextHolder.getContext().setAuthentication(null);
    } else {
      resultMap.put("result", "failure");
    }
    return resultMap;
  }

  public HashMap<String, String> updateTenant(String orgName) {
    HashMap<String, String> resultMap = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String result = manageDatabase.getHandleDbRequests().updateTenant(tenantId, orgName);

    if (result.equals("success")) {
      resultMap.put("result", "success");
      commonUtilsService.updateMetadata(tenantId, EntityType.TENANT, MetadataOperationType.UPDATE);
    } else {
      resultMap.put("result", "failure");
    }
    return resultMap;
  }

  public List<String> getExtensionPeriods() {
    return Arrays.asList(extensionPeriods.split(","));
    // return Arrays.asList("1 month (7$)", "2 months (14$)", "3 months (20$)", "6 months", "1
    // year", "2 years", "3 years", "5 years");
  }

  public HashMap<String, String> udpateTenantExtension(String selectedTenantExtensionPeriod) {
    // send mail

    HashMap<String, String> resultMap = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      resultMap.put("result", "Not Authorized");
      return resultMap;
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    log.info("Into tenant extension : " + tenantId + "  from " + getUserName());

    String result =
        mailService.sendMailToSaasAdmin(
            tenantId,
            getUserName(),
            selectedTenantExtensionPeriod,
            commonUtilsService.getLoginUrl());

    if (result.equals("success")) {
      resultMap.put("result", "success");
    } else {
      resultMap.put("result", "failure");
    }
    return resultMap;
  }

  public HashMap<String, String> getAclCommands() {
    HashMap<String, String> res = new HashMap<>();
    res.put("result", "success");
    res.put("aclCommandSsl", aclCommandSsl);
    res.put("aclCommandPlaintext", aclCommandPlaintext);
    return res;
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }

  public HashMap<String, String> getPublicKey() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    log.info("getPublicKey download " + tenantId);

    HashMap<String, String> kwPublicKeyMap = new HashMap<>();
    File file = new File(kwPublicKey);
    try {
      byte[] arr = FileUtils.readFileToByteArray(file);
      String str = Base64.getEncoder().encodeToString(arr);

      kwPublicKeyMap.put("data", str);
      kwPublicKeyMap.put("filename", file.getName());

      return kwPublicKeyMap;
    } catch (IOException e) {
      log.error("Exception:", e);
    }
    return kwPublicKeyMap;
  }

  public HashMap<String, String> getUpdateEnvStatus(String envId) {
    HashMap<String, String> envUpdatedStatus = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envId, tenantId);

    String status;
    try {
      status =
          clusterApiService.getKafkaClusterStatus(
              manageDatabase
                  .getClusters(env.getType(), tenantId)
                  .get(env.getClusterId())
                  .getBootstrapServers(),
              manageDatabase
                  .getClusters(env.getType(), tenantId)
                  .get(env.getClusterId())
                  .getProtocol(),
              manageDatabase
                  .getClusters(env.getType(), tenantId)
                  .get(env.getClusterId())
                  .getClusterName(),
              env.getType(),
              tenantId);
    } catch (Exception e) {
      status = "OFFLINE";
      log.error("Error from getUpdateEnvStatus ", e);
    }
    env.setEnvStatus(status);
    manageDatabase.getHandleDbRequests().addNewEnv(env);
    manageDatabase.loadEnvMapForOneTenant(tenantId);

    envUpdatedStatus.put("result", "success");
    envUpdatedStatus.put("envstatus", status);

    return envUpdatedStatus;
  }

  @Cacheable(cacheNames = "tenantsinfo")
  public HashMap<String, Integer> getTenantsInfo() {
    HashMap<String, Integer> tenantsInfo = new HashMap<>();
    tenantsInfo.put("tenants", manageDatabase.getTenantMap().size());
    tenantsInfo.put("teams", manageDatabase.getAllTeamsSize());
    tenantsInfo.put("clusters", manageDatabase.getAllClustersSize());
    tenantsInfo.put("topics", manageDatabase.getHandleDbRequests().getAllTopicsCountInAllTenants());

    return tenantsInfo;
  }

  public Map<String, String> getClusterInfoFromEnv(String envSelected, String clusterType) {
    Map<String, String> clusterInfo = new HashMap<>();

    log.debug("getEnvDetails {}", envSelected);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      // tenant filtering
      if (!getEnvsFromUserId().contains(envSelected)) return null;
    }

    Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envSelected, tenantId);
    KwClusters kwClusters =
        manageDatabase.getHandleDbRequests().getClusterDetails(env.getClusterId(), tenantId);

    clusterInfo.put(
        "aivenCluster",
        "" + KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value.equals(kwClusters.getKafkaFlavor()));

    return clusterInfo;
  }
}
