package io.aiven.klaw.service;

import static io.aiven.klaw.model.RolesType.SUPERADMIN;
import static io.aiven.klaw.service.KwConstants.*;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.*;
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

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${klaw.saas.ssl.aclcommand:acl}")
  private String aclCommandSsl;

  @Value("${klaw.saas.plaintext.aclcommand:acl}")
  private String aclCommandPlaintext;

  @Value("${klaw.prizelist.pertenant}")
  private String extensionPeriods;

  @Value("${klaw.saas.ssl.pubkey:pubkey.zip}")
  private String kwPublicKey;

  @Value("${klaw.saas.ssl.clientcerts.location:./tmp/}")
  private String clientCertsLocation;

  @Value("${klaw.max.tenants:1000}")
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
      if (!getEnvsFromUserId().contains(envSelected)) {
        return null;
      }
    }

    Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envSelected, tenantId);
    if (env != null && "false".equals(env.getEnvExists())) {
      return null;
    }

    if (env != null) {
      EnvModel envModel = new EnvModel();
      copyProperties(env, envModel);
      envModel.setClusterName(
          manageDatabase
              .getClusters(KafkaClustersType.of(clusterType), tenantId)
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
    } else {
      return null;
    }
  }

  public List<KwClustersModel> getClusters(String typeOfCluster) {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<KwClusters> clusters =
        new ArrayList<>(
            manageDatabase.getClusters(KafkaClustersType.of(typeOfCluster), tenantId).values());
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
            .anyMatch(
                env -> Objects.equals(env.getClusterId(), finalTmpClusterModel.getClusterId()))) {
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
              .filter(env -> Objects.equals((env.getClusterId() + "").toLowerCase(), clusterId))
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
                  env ->
                      env.getProtocol()
                          .getName()
                          .toLowerCase()
                          .contains(searchClusterParam.toLowerCase()))
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

  public List<Map<String, String>> getSyncEnvs() {
    log.debug("getSyncEnvs");
    Integer tenantId = getUserDetails(getUserName()).getTenantId();
    String syncCluster;
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      return new ArrayList<>();
    }

    Map<String, String> hMap;
    List<Map<String, String>> envsOnly = new ArrayList<>();
    List<EnvModel> envList = getKafkaEnvs();
    for (EnvModel env : envList) {
      hMap = new HashMap<>();
      hMap.put("id", env.getId());
      String baseClusterDropDownStr = " (Base Sync cluster)";
      if (Objects.equals(syncCluster, env.getId())) {
        hMap.put("name", env.getName() + baseClusterDropDownStr);
      } else {
        hMap.put("name", env.getName());
      }

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
    List<EnvModel> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA, tenantId);

    envModelList =
        envModelList.stream()
            .filter(
                env -> {
                  boolean found = false;
                  for (String reqTopicEnv : reqTopicsEnvs) {
                    if (Objects.equals(env.getId(), reqTopicEnv)) {
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
    return getKafkaEnvs();
  }

  public List<EnvModel> getKafkaEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");
    List<Env> listEnvs = manageDatabase.getKafkaEnvList(tenantId);
    List<EnvModel> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA, tenantId);
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
    List<EnvModel> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT, tenantId);

    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public Map<String, List<EnvModel>> getEnvsStatus() {
    Integer tenantId = getUserDetails(getUserName()).getTenantId();
    Map<Integer, List<EnvModel>> allTenantsEnvModels =
        manageDatabase.getEnvModelsClustersStatusAllTenants();
    Map<String, List<EnvModel>> allTenantsEnvModelsUpdated = new HashMap<>();

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
          envListMap.stream()
              .filter(env -> Objects.equals(env.getId(), envId))
              .collect(Collectors.toList());
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

  private List<EnvModel> getEnvModels(
      List<Env> listEnvs, KafkaClustersType clusterType, int tenantId) {
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
      } else {
        log.error("Error : Environment/cluster not loaded :{}", listEnv);
      }
    }
    return envModelList;
  }

  public Map<String, List<String>> getEnvParams(String targetEnv) {
    return manageDatabase
        .getEnvParamsMap(commonUtilsService.getTenantId(getUserName()))
        .get(targetEnv);
  }

  public List<EnvModel> getSchemaRegEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    List<Env> listEnvs = manageDatabase.getSchemaRegEnvList(tenantId);

    List<EnvModel> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.SCHEMA_REGISTRY, tenantId);

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

    List<EnvModel> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT, tenantId);

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
          .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
          .get(oneEnv.getClusterId())
          .getProtocol()
          .equals(KafkaSupportedProtocol.PLAINTEXT))
        status =
            clusterApiService.getSchemaClusterStatus(
                manageDatabase
                    .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
                    .get(oneEnv.getClusterId())
                    .getBootstrapServers(),
                tenantId);
      else {
        status = "NOT_KNOWN";
      }
      oneEnv.setEnvStatus(status);
      newListEnvs.add(oneEnv);
    }

    return getEnvModels(newListEnvs, KafkaClustersType.SCHEMA_REGISTRY, tenantId);
  }

  public ApiResponse addNewEnv(EnvModel newEnv) throws KlawException {
    log.info("addNewEnv {}", newEnv);
    int tenantId = getUserDetails(getUserName()).getTenantId();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    newEnv.setTenantId(tenantId);

    if (newEnv.getClusterId() == null) {
      return ApiResponse.builder().result("Please select a cluster.").build();
    }

    if (newEnv.getName().length() > 3
        && Objects.equals(newEnv.getType(), KafkaClustersType.KAFKA.value)) {
      newEnv.setName(newEnv.getName().substring(0, 3));
    }

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
      } else {
        newEnv.setId("1");
      }

      // Same name per type (kafka, kafkaconnect) in tenant not posssible.
      List<Env> envActualList = manageDatabase.getHandleDbRequests().selectAllEnvs(tenantId);
      boolean envNameAlreadyPresent =
          envActualList.stream()
              .anyMatch(
                  en ->
                      Objects.equals(en.getName(), newEnv.getName())
                          && Objects.equals(en.getType(), newEnv.getType())
                          && Objects.equals(en.getTenantId(), newEnv.getTenantId())
                          && Objects.equals(en.getEnvExists(), "true")); // 504 change
      if (envNameAlreadyPresent) {
        return ApiResponse.builder()
            .result(
                "Failure. Please choose a different name. This environment name already exists.")
            .build();
      } else if (envActualList.stream()
          .anyMatch(
              en ->
                  Objects.equals(en.getName(), newEnv.getName())
                      && Objects.equals(en.getType(), newEnv.getType())
                      && Objects.equals(en.getTenantId(), newEnv.getTenantId())
                      && Objects.equals(en.getEnvExists(), "false"))) {
        Optional<Env> envAlreadyExistsInDeleted =
            envActualList.stream()
                .filter(
                    en ->
                        en.getName().equals(newEnv.getName())
                            && en.getType().equals(newEnv.getType())
                            && en.getTenantId().equals(newEnv.getTenantId())
                            && en.getEnvExists().equals("false"))
                .findFirst();
        if (envAlreadyExistsInDeleted.isPresent()) {
          envIdAlreadyExistsInDeleteStatus = envAlreadyExistsInDeleted.get().getId();
        }
      }
    }

    Env env = new Env();
    copyProperties(newEnv, env);
    if (!"".equals(envIdAlreadyExistsInDeleteStatus)) {
      env.setId(envIdAlreadyExistsInDeleteStatus);
    }
    env.setEnvExists("true");

    try {
      String result = manageDatabase.getHandleDbRequests().addNewEnv(env);
      commonUtilsService.updateMetadata(
          tenantId, EntityType.ENVIRONMENT, MetadataOperationType.CREATE);
      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse addNewCluster(KwClustersModel kwClustersModel) {
    log.info("addNewCluster {}", kwClustersModel);
    Map<String, String> resultMap = new HashMap<>();

    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    AtomicBoolean clusterNameAlreadyExists = new AtomicBoolean(false);
    if (kwClustersModel.getClusterId() == null) {
      manageDatabase
          .getClusters(KafkaClustersType.ALL, tenantId)
          .forEach(
              (k, v) -> {
                if (Objects.equals(v.getClusterName(), kwClustersModel.getClusterName())
                    && Objects.equals(v.getClusterType(), kwClustersModel.getClusterType())) {
                  clusterNameAlreadyExists.set(true);
                }
              });

      if (clusterNameAlreadyExists.get()) {
        return ApiResponse.builder()
            .result("Failure. Please choose a different name. This cluster name already exists.")
            .build();
      }
    }
    KwClusters kwCluster = new KwClusters();
    copyProperties(kwClustersModel, kwCluster);
    kwCluster.setTenantId(tenantId);
    kwCluster.setClusterName(kwCluster.getClusterName().toUpperCase());

    // only for new cluster requests on saas
    if (KafkaSupportedProtocol.SSL.equals(kwCluster.getProtocol())
        && kwCluster.getClusterId() == null
        && "saas".equals(kwInstallationType)) {
      if (!savePublicKey(kwClustersModel, resultMap, tenantId, kwCluster)) {
        return ApiResponse.builder().result("Failure. Unable to save public key.").build();
      }
    }

    String result = manageDatabase.getHandleDbRequests().addNewCluster(kwCluster);
    if (result.equals(ApiResultStatus.SUCCESS.value)) {
      commonUtilsService.updateMetadata(tenantId, EntityType.CLUSTER, MetadataOperationType.CREATE);
    }
    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
  }

  private boolean savePublicKey(
      KwClustersModel kwClustersModel,
      Map<String, String> resultMap,
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
      } else {
        return false;
      }
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

      if (clientCertFile.exists()) {
        clientCertFile.delete();
      }

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
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (Exception ex) {
        log.error("Error in closing the BufferedWriter ", ex);
      }
    }
    return true;
  }

  public ApiResponse deleteCluster(String clusterId) throws KlawException {
    log.info("deleteCluster {}", clusterId);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    if (allEnvList.stream()
        .anyMatch(env -> Objects.equals(env.getClusterId(), Integer.parseInt(clusterId)))) {
      return ApiResponse.builder()
          .result("Not allowed to delete this cluster, as there are associated environments.")
          .build();
    }

    try {
      String result =
          manageDatabase.getHandleDbRequests().deleteCluster(Integer.parseInt(clusterId), tenantId);
      commonUtilsService.updateMetadata(tenantId, EntityType.CLUSTER, MetadataOperationType.DELETE);
      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse deleteEnvironment(String envId, String envType) throws KlawException {
    log.info("deleteEnvironment {}", envId);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    switch (envType) {
      case "kafka":
        if (manageDatabase.getHandleDbRequests().findAllKafkaComponentsCountForEnv(envId, tenantId)
            > 0) {
          String notAllowed =
              "Not allowed to delete this environment, as there are associated topics/acls/requests.";
          return ApiResponse.builder().result(notAllowed).build();
        }
        break;
      case "kafkaconnect":
        if (manageDatabase
                .getHandleDbRequests()
                .findAllConnectorComponentsCountForEnv(envId, tenantId)
            > 0) {
          String notAllowed =
              "Not allowed to delete this environment, as there are associated connectors/requests.";
          return ApiResponse.builder().result(notAllowed).build();
        }
        break;
      case "schemaregistry":
        if (manageDatabase.getHandleDbRequests().findAllSchemaComponentsCountForEnv(envId, tenantId)
            > 0) {
          String notAllowed =
              "Not allowed to delete this environment, as there are associated schemaregistry/requests.";
          return ApiResponse.builder().result(notAllowed).build();
        }
        break;
    }

    try {
      String result =
          manageDatabase.getHandleDbRequests().deleteEnvironmentRequest(envId, tenantId);
      commonUtilsService.updateMetadata(
          tenantId, EntityType.ENVIRONMENT, MetadataOperationType.DELETE);

      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  public List<KwTenantModel> getAllTenants() {
    if (SUPERADMIN
            .name()
            .equals(manageDatabase.getHandleDbRequests().getUsersInfo(getUserName()).getRole())
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
                            && Objects.equals(userInfo.getRole(), SUPERADMIN_ROLE))
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
    String standardNames = manageDatabase.getKwPropertyValue("klaw.envs.standardnames", tenantId);
    List<String> envList = Arrays.asList(standardNames.split(","));
    Collections.sort(envList);
    return envList;
  }

  public ApiResponse addTenantId(KwTenantModel kwTenantModel, boolean isExternal)
      throws KlawException {
    if (manageDatabase.getHandleDbRequests().getTenants().size()
        >= maxNumberOfTenantsCanBeCreated) {
      return ApiResponse.builder().result("Maximum tenants reached.").build();
    }

    if (isExternal
        && commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_TENANT)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantName(kwTenantModel.getTenantName());
    kwTenants.setTenantDesc(kwTenantModel.getTenantDesc());
    kwTenants.setInTrial(kwTenantModel.isInTrialPhase() + "");
    kwTenants.setContactPerson(kwTenantModel.getContactPerson());
    kwTenants.setOrgName("Our Organization");
    if (isExternal) {
      kwTenantModel.setActiveTenant(true);
    }

    if (kwTenantModel.isActiveTenant()) {
      kwTenants.setIsActive("true");
    } else {
      kwTenants.setIsActive("false");
    }

    if ("saas".equals(kwInstallationType)) {
      kwTenants.setLicenseExpiry(
          new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DAYS_TRIAL_PERIOD)));
    } else {
      kwTenants.setLicenseExpiry(
          new Timestamp(
              System.currentTimeMillis() + TimeUnit.DAYS.toMillis(DAYS_EXPIRY_DEFAULT_TENANT)));
    }

    try {
      String addNewTenantStatus = manageDatabase.getHandleDbRequests().addNewTenant(kwTenants);
      int tenantId =
          manageDatabase.getHandleDbRequests().getTenants().stream()
              .filter(
                  kwTenant ->
                      Objects.equals(kwTenant.getTenantName(), kwTenantModel.getTenantName()))
              .findFirst()
              .get()
              .getTenantId();

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
      return ApiResponse.builder().result(addNewTenantStatus).data("" + tenantId).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
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
      kwTenantModel.setInTrialPhase("true".equals(tenant.get().getInTrial()));
      long timeInMilliSeconds =
          tenant.get().getLicenseExpiry().getTime() - System.currentTimeMillis();
      long hours = TimeUnit.MILLISECONDS.toHours(timeInMilliSeconds);

      int days = (int) (hours / 24);
      int hoursN = (int) hours % 24;

      kwTenantModel.setNumberOfDays("" + days);
      kwTenantModel.setNumberOfHours("" + hoursN);
      kwTenantModel.setActiveTenant("true".equals(tenant.get().getIsActive()));
      kwTenantModel.setOrgName(tenant.get().getOrgName());

      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
        kwTenantModel.setAuthorizedToDelete(false);
      } else {
        kwTenantModel.setAuthorizedToDelete(true);
      }
    }
    return kwTenantModel;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public ApiResponse deleteTenant() throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (tenantId == DEFAULT_TENANT_ID) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }
    String tenantName = manageDatabase.getTenantMap().get(tenantId);

    List<UserInfo> allUsers = manageDatabase.getHandleDbRequests().selectAllUsersInfo(tenantId);
    for (UserInfo userInfo : allUsers) {
      try {
        usersTeamsControllerService.deleteUser(userInfo.getUsername(), false); // internal delete
      } catch (KlawException e) {
        throw new RuntimeException(e);
      }
    }
    manageDatabase.getHandleDbRequests().deleteAllUsers(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllTeams(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllEnvs(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllClusters(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllRolesPerms(tenantId);
    manageDatabase.getHandleDbRequests().deleteAllKwProps(tenantId);
    manageDatabase.getHandleDbRequests().deleteTxnData(tenantId);

    try {
      String result = manageDatabase.getHandleDbRequests().disableTenant(tenantId);

      if (ApiResultStatus.SUCCESS.value.equals(result)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.TENANT, MetadataOperationType.DELETE);
        SecurityContextHolder.getContext().setAuthentication(null);
      }
      return ApiResponse.builder().result(result).data(tenantName).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse updateTenant(String orgName) throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    try {
      String result = manageDatabase.getHandleDbRequests().updateTenant(tenantId, orgName);

      if (ApiResultStatus.SUCCESS.value.equals(result)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.TENANT, MetadataOperationType.UPDATE);
      }

      return ApiResponse.builder().result(result).build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public List<String> getExtensionPeriods() {
    return Arrays.asList(extensionPeriods.split(","));
    // return Arrays.asList("1 month (7$)", "2 months (14$)", "3 months (20$)", "6 months", "1
    // year", "2 years", "3 years", "5 years");
  }

  public ApiResponse udpateTenantExtension(String selectedTenantExtensionPeriod) {
    // send mail
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    log.info("Into tenant extension : " + tenantId + "  from " + getUserName());

    String result =
        mailService.sendMailToSaasAdmin(
            tenantId,
            getUserName(),
            selectedTenantExtensionPeriod,
            commonUtilsService.getLoginUrl());

    return ApiResponse.builder().result(result).build();
  }

  public Map<String, String> getAclCommands() {
    Map<String, String> res = new HashMap<>();
    res.put("result", ApiResultStatus.SUCCESS.value);
    res.put("aclCommandSsl", aclCommandSsl);
    res.put("aclCommandPlaintext", aclCommandPlaintext);
    return res;
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }

  public Map<String, String> getPublicKey() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    log.info("getPublicKey download " + tenantId);

    Map<String, String> kwPublicKeyMap = new HashMap<>();
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

  public Map<String, String> getUpdateEnvStatus(String envId) {
    Map<String, String> envUpdatedStatus = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envId, tenantId);

    String status;
    try {
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.of(env.getType()), tenantId)
              .get(env.getClusterId());
      status =
          clusterApiService.getKafkaClusterStatus(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              env.getType(),
              tenantId);
    } catch (Exception e) {
      status = "OFFLINE";
      log.error("Error from getUpdateEnvStatus ", e);
    }
    env.setEnvStatus(status);
    manageDatabase.getHandleDbRequests().addNewEnv(env);
    manageDatabase.loadEnvMapForOneTenant(tenantId);

    envUpdatedStatus.put("result", ApiResultStatus.SUCCESS.value);
    envUpdatedStatus.put("envstatus", status);

    return envUpdatedStatus;
  }

  @Cacheable(cacheNames = "tenantsinfo")
  public Map<String, Integer> getTenantsInfo() {
    Map<String, Integer> tenantsInfo = new HashMap<>();
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
      if (!getEnvsFromUserId().contains(envSelected)) {
        return null;
      }
    }

    Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envSelected, tenantId);
    KwClusters kwClusters =
        manageDatabase.getHandleDbRequests().getClusterDetails(env.getClusterId(), tenantId);

    clusterInfo.put(
        "aivenCluster",
        "" + KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value.equals(kwClusters.getKafkaFlavor()));

    return clusterInfo;
  }

  public List<Map<String, String>> getSupportedKafkaProtocols() {
    List<Map<String, String>> supportedProtocols = new ArrayList<>();
    for (KafkaSupportedProtocol kafkaSupportedProtocol : KafkaSupportedProtocol.values()) {
      Map<String, String> protocolValues = new HashMap<>();
      protocolValues.put("name", kafkaSupportedProtocol.getName());
      protocolValues.put("value", kafkaSupportedProtocol.getValue());
      supportedProtocols.add(protocolValues);
    }

    return supportedProtocols;
  }
}
