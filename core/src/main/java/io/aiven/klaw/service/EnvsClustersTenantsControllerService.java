package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_110;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_108;
import static io.aiven.klaw.helpers.CompareUtils.isEqual;
import static io.aiven.klaw.helpers.CompareUtils.isFalse;
import static io.aiven.klaw.helpers.KwConstants.DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER;
import static io.aiven.klaw.helpers.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_KAFKA_CONNECT_ENVS;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
import static io.aiven.klaw.helpers.KwConstants.REQUEST_TOPICS_OF_ENVS;
import static io.aiven.klaw.model.enums.PermissionType.ADD_EDIT_DELETE_CLUSTERS;
import static io.aiven.klaw.model.enums.PermissionType.ADD_EDIT_DELETE_ENVS;
import static io.aiven.klaw.model.enums.PermissionType.ADD_TENANT;
import static io.aiven.klaw.model.enums.PermissionType.UPDATE_DELETE_MY_TENANT;
import static io.aiven.klaw.model.enums.RolesType.SUPERADMIN;
import static io.aiven.klaw.service.UsersTeamsControllerService.MASKED_PWD;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawValidationException;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.KwClustersModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.ClusterInfo;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.EnvModelResponse;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.model.response.EnvUpdatedStatus;
import io.aiven.klaw.model.response.KwClustersModelResponse;
import io.aiven.klaw.model.response.SupportedProtocolInfo;
import io.aiven.klaw.model.response.TenantInfo;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
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

  @Autowired private ManageDatabase manageDatabase;

  @Autowired private ClusterApiService clusterApiService;

  @Autowired private UsersTeamsControllerService usersTeamsControllerService;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${klaw.max.tenants:1000}")
  private int maxNumberOfTenantsCanBeCreated;

  @Autowired private DefaultDataService defaultDataService;

  @Autowired
  public void setServices(ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  private boolean isAuthorizedFor(PermissionType type) {
    return !commonUtilsService.isNotAuthorizedUser(commonUtilsService.getPrincipal(), type);
  }

  public synchronized EnvModelResponse getEnvDetails(String envSelected, String clusterType) {
    String userName = getUserName();
    log.debug("getEnvDetails {}", envSelected);
    int tenantId = commonUtilsService.getTenantId(userName);
    if (!isAuthorizedFor(ADD_EDIT_DELETE_ENVS)
        && !commonUtilsService.getEnvsFromUserId(userName).contains(envSelected)) {
      return null;
    }

    Env env = manageDatabase.getHandleDbRequests().getEnvDetails(envSelected, tenantId);
    if (env == null || isFalse(env.getEnvExists())) {
      return null;
    }
    KwClusters clusters =
        manageDatabase
            .getClusters(KafkaClustersType.of(clusterType), tenantId)
            .get(env.getClusterId());
    String tenantName = manageDatabase.getTenantMap().get(tenantId);

    EnvModelResponse envModel = new EnvModelResponse();
    copyProperties(env, envModel);
    envModel.setClusterName(clusters.getClusterName());
    envModel.setTenantName(tenantName);

    log.debug("Return env model {}", envModel);
    return envModel;
  }

  public UserInfoModel getUserDetails(String userId) {
    UserInfoModel userInfoModel = new UserInfoModel();
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
    if (userInfo != null) {
      copyProperties(userInfo, userInfoModel);
      userInfoModel.setUserPassword(MASKED_PWD);
    }
    return userInfo == null ? null : userInfoModel;
  }

  public List<KwClustersModelResponse> getClusters(String typeOfCluster) {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<KwClusters> clusters =
        new ArrayList<>(
            manageDatabase.getClusters(KafkaClustersType.of(typeOfCluster), tenantId).values());
    List<KwClustersModelResponse> clustersModels = new ArrayList<>();
    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    for (KwClusters cluster : clusters) {
      KwClustersModelResponse clusterModel = new KwClustersModelResponse(cluster);
      int clusterId = clusterModel.getClusterId();
      clusterModel.setShowDeleteCluster(true);
      // set only for authorized users
      if (isAuthorizedFor(ADD_EDIT_DELETE_CLUSTERS)) {
        boolean linkedToEnv =
            allEnvList.stream().anyMatch(env -> Objects.equals(env.getClusterId(), clusterId));
        clusterModel.setShowDeleteCluster(!linkedToEnv);
      }
      clustersModels.add(clusterModel);
    }

    return clustersModels;
  }

  public List<KwClustersModelResponse> getClustersPaginated(
      KafkaClustersType typeOfCluster, String clusterId, String pageNo, String searchClusterParam) {

    String clusterTypeValue = typeOfCluster.value;
    List<KwClustersModelResponse> kwClustersModelList = getClusters(clusterTypeValue);

    if (clusterId != null && !clusterId.equals("")) {
      kwClustersModelList =
          kwClustersModelList.stream()
              .filter(cluster -> isEqual(cluster.getClusterId(), clusterId))
              .collect(toList());
    }

    if (searchClusterParam != null && !searchClusterParam.equals("")) {
      kwClustersModelList =
          kwClustersModelList.stream()
              .filter(
                  model ->
                      Stream.of(
                              model.getClusterName(),
                              model.getBootstrapServers(),
                              model.getProtocol().getName())
                          .filter(Objects::nonNull)
                          .map(String::toLowerCase)
                          .anyMatch(s -> s.contains(searchClusterParam.toLowerCase())))
              .sorted(Comparator.comparingInt(KwClustersModelResponse::getClusterId))
              .collect(toList());
    }
    return Pager.getItemsList(
        pageNo, "", 10, kwClustersModelList, (pageContext, mp) -> mp.loadPageContext(pageContext));
  }

  public List<EnvIdInfo> getSyncEnvs() {
    log.debug("getSyncEnvs");
    Integer tenantId = getUserDetails(getUserName()).getTenantId();
    String syncCluster;
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      return new ArrayList<>();
    }

    // return empty list, when sync cluster is not configured
    if (syncCluster == null) {
      return new ArrayList<>();
    }

    List<EnvIdInfo> envsOnly = new ArrayList<>();
    List<EnvModelResponse> envList = getKafkaEnvs();
    for (EnvModelResponse env : envList) {
      EnvIdInfo envIdInfo = new EnvIdInfo();
      envIdInfo.setId(env.getId());
      String baseClusterDropDownStr = " (Base Sync cluster)";
      boolean isSyncEnv = Objects.equals(syncCluster, env.getId());
      String envName = isSyncEnv ? env.getName() + baseClusterDropDownStr : env.getName();
      envIdInfo.setName(envName);
      envsOnly.add(envIdInfo);
    }

    return envsOnly;
  }

  public List<EnvModelResponse> getEnvsForRequestTopicsCluster() {
    int tenantId = getUserDetails(getUserName()).getTenantId();

    String requestTopicsEnvs = commonUtilsService.getEnvProperty(tenantId, REQUEST_TOPICS_OF_ENVS);
    if (requestTopicsEnvs == null) {
      return new ArrayList<>();
    }
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
    String[] reqTopicsEnvs = requestTopicsEnvs.split(",");
    List<Env> listEnvs = manageDatabase.getKafkaEnvList(tenantId);
    List<EnvModelResponse> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA, tenantId);

    envModelList = filterEnvironmentModelList(reqTopicsEnvs, envModelList);
    if (orderOfEnvs == null) {
      return envModelList;
    }
    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public List<EnvModelResponse> getEnvsForRequestTopicsClusterFiltered() {
    return getKafkaEnvs();
  }

  public List<EnvModelResponse> getKafkaEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
    List<Env> listEnvs = manageDatabase.getKafkaEnvList(tenantId);
    List<EnvModelResponse> envModelList = getEnvModels(listEnvs, KafkaClustersType.KAFKA, tenantId);

    // set deletable only to authorized, and check for count
    if (isAuthorizedFor(ADD_EDIT_DELETE_ENVS)) {
      envModelList.forEach(
          envModel -> {
            envModel.setShowDeleteEnv(
                !manageDatabase
                    .getHandleDbRequests()
                    .existsKafkaComponentsForEnv(envModel.getId(), tenantId));
          });
    }

    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public List<EnvModelResponse> getConnectorEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_KAFKA_CONNECT_ENVS);
    List<Env> listEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);
    List<EnvModelResponse> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT, tenantId);

    envModelList.sort(Comparator.comparingInt(topicEnv -> orderOfEnvs.indexOf(topicEnv.getId())));
    return envModelList;
  }

  public List<EnvModelResponse> getEnvsPaginated(
      KafkaClustersType type, String envId, String pageNo, String searchEnvParam) {
    List<EnvModelResponse> envListMap;
    envListMap =
        switch (type) {
          case KAFKA:
            yield getKafkaEnvs();
          case SCHEMA_REGISTRY:
            yield getSchemaRegEnvs();
          case KAFKA_CONNECT:
            yield getKafkaConnectEnvs();
          case ALL:
            yield Stream.of(getKafkaEnvs(), getSchemaRegEnvs(), getKafkaConnectEnvs())
                .flatMap(Collection::stream)
                .toList();
        };

    if (envId != null && !envId.equals("")) {
      envListMap =
          envListMap.stream().filter(env -> Objects.equals(env.getId(), envId)).collect(toList());
    }

    if (searchEnvParam != null && !searchEnvParam.equals("")) {
      envListMap =
          envListMap.stream()
              .filter(
                  env ->
                      Stream.of(
                              env.getName(),
                              env.getClusterName(),
                              env.getOtherParams(),
                              env.getTenantName())
                          .filter(Objects::nonNull)
                          .map(String::toLowerCase)
                          .anyMatch(s -> s.contains(searchEnvParam.toLowerCase())))
              .collect(toList());
    }

    return Pager.getItemsList(
        pageNo, "", 10, envListMap, (pageContext, mp) -> mp.loadPageContext(pageContext));
  }

  private List<EnvModelResponse> getEnvModels(
      List<Env> listEnvs, KafkaClustersType clusterType, int tenantId) {
    List<EnvModelResponse> envModelList = new ArrayList<>();
    String tenantName = manageDatabase.getTenantMap().get(tenantId);
    for (Env listEnv : listEnvs) {
      log.debug("Params {} for env {}", listEnv.getParams(), listEnv.getName());
      KwClusters kwCluster =
          manageDatabase.getClusters(clusterType, tenantId).get(listEnv.getClusterId());
      if (kwCluster != null) {
        EnvModelResponse envModel = new EnvModelResponse();
        copyProperties(listEnv, envModel);
        envModel.setClusterName(kwCluster.getClusterName());
        envModel.setTenantName(tenantName);
        envModelList.add(envModel);
      } else {
        log.error("Error : Environment/cluster not loaded :{}", listEnv);
      }
    }
    return envModelList;
  }

  public EnvParams getEnvParams(Integer targetEnv) {
    return manageDatabase.getEnvParams(commonUtilsService.getTenantId(getUserName()), targetEnv);
  }

  public List<EnvModelResponse> getSchemaRegEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    List<Env> listEnvs = manageDatabase.getSchemaRegEnvList(tenantId);

    List<EnvModelResponse> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.SCHEMA_REGISTRY, tenantId);

    // set deletable only to authorized, and check for count
    if (isAuthorizedFor(ADD_EDIT_DELETE_ENVS)) {
      envModelList.forEach(
          envModel -> {
            envModel.setShowDeleteEnv(
                !manageDatabase
                    .getHandleDbRequests()
                    .existsSchemaComponentsForEnv(envModel.getId(), tenantId));
          });
    }

    return envModelList;
  }

  private List<EnvModelResponse> filterEnvironmentModelList(
      String[] reqEnvs, List<EnvModelResponse> envModelList) {
    envModelList =
        envModelList.stream()
            .filter(env -> Arrays.asList(reqEnvs).contains(env.getId()))
            .collect(toList());
    return envModelList;
  }

  public List<EnvModelResponse> getKafkaConnectEnvs() {
    String userName = getUserName();
    int tenantId = getUserDetails(userName).getTenantId();
    List<Env> listEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);

    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), ADD_EDIT_DELETE_ENVS)) {
      final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
      listEnvs =
          listEnvs.stream().filter(env -> allowedEnvIdSet.contains(env.getId())).collect(toList());
    }

    List<EnvModelResponse> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT, tenantId);

    // set deletable only to authorized, and check for count
    if (isAuthorizedFor(ADD_EDIT_DELETE_ENVS)) {
      envModelList.forEach(
          envModel -> {
            envModel.setShowDeleteEnv(
                !manageDatabase
                    .getHandleDbRequests()
                    .existsConnectorComponentsForEnv(envModel.getId(), tenantId));
          });
    }

    return envModelList;
  }

  public ApiResponse addNewEnv(EnvModel newEnv) throws KlawException, KlawValidationException {
    log.info("addNewEnv {}", newEnv);
    int tenantId = getUserDetails(getUserName()).getTenantId();
    if (!isAuthorizedFor(ADD_EDIT_DELETE_ENVS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    newEnv.setTenantId(tenantId);

    newEnv.setName(newEnv.getName().toUpperCase());
    if (KafkaClustersType.KAFKA.value.equalsIgnoreCase(newEnv.getType())) {
      newEnv
          .getParams()
          .setPartitionsList(
              buildListWithDefault(
                  newEnv.getParams().getDefaultPartitions(),
                  newEnv.getParams().getMaxPartitions()));
      newEnv
          .getParams()
          .setReplicationFactorList(
              buildListWithDefault(
                  newEnv.getParams().getDefaultRepFactor(), newEnv.getParams().getMaxRepFactor()));
    }

    List<Env> envActualList = manageDatabase.getHandleDbRequests().getAllEnvs(tenantId);
    List<Env> kafkaEnvs = manageDatabase.getKafkaEnvList(tenantId);
    List<Env> schemaEnvs = manageDatabase.getSchemaRegEnvList(tenantId);
    List<Integer> kafkaClusterIds = kafkaEnvs.stream().map(Env::getClusterId).toList();
    List<Integer> schemaClusterIds = schemaEnvs.stream().map(Env::getClusterId).toList();

    if (newEnv.getId() == null || newEnv.getId().length() == 0) {
      if (validateConnectedClusters(newEnv, kafkaClusterIds, schemaClusterIds)) {
        return ApiResponse.notOk(ENV_CLUSTER_TNT_110);
      }
      Integer id =
          manageDatabase
              .getHandleDbRequests()
              .getNextSeqIdAndUpdate(EntityType.ENVIRONMENT.name(), tenantId);
      if (id != null) {
        newEnv.setId(String.valueOf(id));
      }
    } else {
      // modify env
      envActualList =
          envActualList.stream()
              .filter(env -> !env.getId().equals(newEnv.getId()))
              .collect(toList());
    }

    // Same name per type (kafka, kafkaconnect) in tenant not posssible.
    boolean envNameAlreadyPresent =
        envActualList.stream()
            .anyMatch(
                en ->
                    Objects.equals(en.getName().toLowerCase(), newEnv.getName().toLowerCase())
                        && Objects.equals(en.getType(), newEnv.getType())
                        && Objects.equals(en.getTenantId(), newEnv.getTenantId())
                        && Objects.equals(en.getEnvExists(), "true"));
    if (envNameAlreadyPresent) {
      return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_101);
    }

    Env env = new Env();
    copyProperties(newEnv, env);
    env.setEnvExists("true");

    try {
      EnvTag envTag =
          addEnvironmentMapping(
              env.getAssociatedEnv(), env.getId(), env.getName(), env.getTenantId(), env.getType());
      env.setAssociatedEnv(envTag);
      String result = manageDatabase.getHandleDbRequests().addNewEnv(env);
      if (result.equals(ApiResultStatus.SUCCESS.value)) {
        manageDatabase.addEnvToCache(tenantId, env, false);
        return ApiResponse.ok(result);
      } else {
        return ApiResponse.notOk(result);
      }
    } catch (KlawValidationException ex) {
      log.error("KlawValidationException:", ex);
      throw ex;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private List<String> buildListWithDefault(String defaultNumber, String maxNumber) {
    int defaultNum = Integer.parseInt(defaultNumber);
    int maxNum = Integer.parseInt(maxNumber);
    List<String> parameterList = new ArrayList();
    for (int i = 1; i <= maxNum; i++) {
      String value = i == defaultNum ? i + " (default)" : String.valueOf(i);
      parameterList.add(value);
    }
    return parameterList;
  }

  private boolean validateConnectedClusters(
      EnvModel newEnv, List<Integer> kafkaClusterIds, List<Integer> schemaClusterIds) {
    KafkaClustersType type = KafkaClustersType.of(newEnv.getType());
    switch (type) {
      case KAFKA -> {
        // don't allow same cluster id be assigned to another kafka env, if regex is not defined
        return kafkaClusterIds.contains(newEnv.getClusterId())
            && newEnv.getParams().getTopicPrefix() == null
            && newEnv.getParams().getTopicSuffix() == null;
      }
      case SCHEMA_REGISTRY -> {
        return schemaClusterIds.contains(newEnv.getClusterId());
      }
      default -> {
        return false;
      }
    }
  }

  public ApiResponse addNewCluster(KwClustersModel kwClustersModel) {
    log.info("addNewCluster {}", kwClustersModel);

    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (!isAuthorizedFor(ADD_EDIT_DELETE_CLUSTERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    AtomicBoolean clusterNameAlreadyExists = new AtomicBoolean(false);
    if (kwClustersModel.getClusterId() == null) {
      manageDatabase
          .getClusters(KafkaClustersType.ALL, tenantId)
          .forEach(
              (k, v) -> {
                if (Objects.equals(v.getClusterName(), kwClustersModel.getClusterName())
                    && Objects.equals(v.getClusterType(), kwClustersModel.getClusterType().value)) {
                  clusterNameAlreadyExists.set(true);
                }
              });

      if (clusterNameAlreadyExists.get()) {
        return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_102);
      }
    }
    KwClusters kwCluster = new KwClusters();
    copyProperties(kwClustersModel, kwCluster);
    kwCluster.setTenantId(tenantId);
    kwCluster.setClusterName(kwClustersModel.getClusterName().toUpperCase());
    kwCluster.setKafkaFlavor(kwClustersModel.getKafkaFlavor().value);
    kwCluster.setClusterType(kwClustersModel.getClusterType().value);

    // only for new cluster requests
    String result = manageDatabase.getHandleDbRequests().addNewCluster(kwCluster);

    if (result.equals(ApiResultStatus.SUCCESS.value)) {
      commonUtilsService.updateMetadata(
          tenantId, EntityType.CLUSTER, MetadataOperationType.CREATE, null);
      return ApiResponse.SUCCESS;
    } else {
      return ApiResponse.FAILURE;
    }
  }

  public ApiResponse deleteCluster(String clusterId) throws KlawException {
    log.info("deleteCluster {}", clusterId);
    if (!isAuthorizedFor(ADD_EDIT_DELETE_CLUSTERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    if (allEnvList.stream().anyMatch(env -> isEqual(env.getClusterId(), clusterId))) {
      return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_104);
    }

    try {
      String result =
          manageDatabase.getHandleDbRequests().deleteCluster(Integer.parseInt(clusterId), tenantId);
      if (result.equals(ApiResultStatus.SUCCESS.value)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.CLUSTER, MetadataOperationType.DELETE, null);
        return ApiResponse.ok(result);
      } else {
        return ApiResponse.notOk(result);
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse deleteEnvironment(String envId, String envType) throws KlawException {
    log.info("deleteEnvironment {}", envId);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (!isAuthorizedFor(ADD_EDIT_DELETE_ENVS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    HandleDbRequestsJdbc jdbc = manageDatabase.getHandleDbRequests();
    KafkaClustersType type = KafkaClustersType.of(envType);
    switch (type) {
      case KAFKA:
        if (jdbc.existsKafkaComponentsForEnv(envId, tenantId)) {
          return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_105);
        }
        break;
      case KAFKA_CONNECT:
        if (jdbc.existsConnectorComponentsForEnv(envId, tenantId)) {
          return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_106);
        }
        break;
      case SCHEMA_REGISTRY:
        if (jdbc.existsSchemaComponentsForEnv(envId, tenantId)) {
          return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_107);
        }
        break;
    }

    try {
      removeAssociatedKafkaOrSchemaEnvironment(envId, tenantId, envType);
      String result = jdbc.deleteEnvironmentRequest(envId, tenantId);
      if (result.equals(ApiResultStatus.SUCCESS.value)) {
        manageDatabase.removeEnvFromCache(tenantId, Integer.valueOf(envId), false);
        return ApiResponse.ok(result);
      } else {
        return ApiResponse.notOk(result);
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private void removeAssociatedKafkaOrSchemaEnvironment(
      String envId, int tenantId, String envType) {

    if (KafkaClustersType.KAFKA.value.equals(envType)
        || KafkaClustersType.SCHEMA_REGISTRY.value.equals(envType)) {
      Env env = manageDatabase.getHandleDbRequests().getEnvDetails(envId, tenantId);
      if (env != null && env.getAssociatedEnv() != null) {
        Env linkedEnv =
            manageDatabase
                .getHandleDbRequests()
                .getEnvDetails(env.getAssociatedEnv().getId(), tenantId);
        linkedEnv.setAssociatedEnv(null);
        manageDatabase.getHandleDbRequests().addNewEnv(linkedEnv);
      }
    }
  }

  private EnvTag addEnvironmentMapping(
      EnvTag envTag, String envId, String envName, int tenantId, String envType)
      throws KlawValidationException {
    KafkaClustersType type = KafkaClustersType.of(envType);
    switch (type) {
      case SCHEMA_REGISTRY -> {
        // only assignable on a schema registry
        log.debug("Env Tag supplied = {}", envTag);
        if (envTag != null && !envTag.getId().isEmpty()) {
          associateWithKafkaEnv(envTag, envId, envName, tenantId);
          // remove existing association if it exists
          removeAssociationWithKafkaEnv(envTag, envId, tenantId);
        } else {
          // envTag is always null here
          removeAssociationWithKafkaEnv(null, envId, tenantId);
        }
      }
      case KAFKA -> envTag = getKafkaAssociation(envTag, envId, tenantId);
    }

    return envTag;
  }

  private EnvTag getKafkaAssociation(EnvTag kafkaEnvTag, String kafkaEnvId, int tenantId) {
    if (kafkaEnvTag == null) {
      Env existing = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
      kafkaEnvTag = existing != null ? existing.getAssociatedEnv() : null;
    }
    return kafkaEnvTag;
  }

  private void removeAssociationWithKafkaEnv(EnvTag envTag, String envId, int tenantId) {
    Env existingEnv = manageDatabase.getHandleDbRequests().getEnvDetails(envId, tenantId);
    // envTag is equal to null we don't need to check if the associated env is the same as the
    // passed env tag because this is actualy an operation to remove an association.
    if (existingEnv != null
        && existingEnv.getAssociatedEnv() != null
        && (envTag == null || !existingEnv.getAssociatedEnv().getId().equals(envTag.getId()))) {
      Env linkedEnv =
          manageDatabase
              .getHandleDbRequests()
              .getEnvDetails(existingEnv.getAssociatedEnv().getId(), tenantId);
      linkedEnv.setAssociatedEnv(null);
      manageDatabase.getHandleDbRequests().addNewEnv(linkedEnv);
      // add to cache
      manageDatabase.addEnvToCache(tenantId, linkedEnv, false);
    }
  }

  private void associateWithKafkaEnv(EnvTag envTag, String envId, String envName, int tenantId)
      throws KlawValidationException {
    Env linkedEnv = manageDatabase.getHandleDbRequests().getEnvDetails(envTag.getId(), tenantId);

    if (linkedEnv.getAssociatedEnv() != null
        && !linkedEnv.getAssociatedEnv().getId().equals(envId)) {
      throw new KlawValidationException(
          "Target Environment "
              + linkedEnv.getName()
              + " is already assigned to env "
              + linkedEnv.getAssociatedEnv().getName());
    }
    linkedEnv.setAssociatedEnv(new EnvTag(envId, envName));
    manageDatabase.getHandleDbRequests().addNewEnv(linkedEnv);
    // add update to cache
    manageDatabase.addEnvToCache(tenantId, linkedEnv, false);
  }

  private String getUserName() {
    return mailService.getUserName(commonUtilsService.getPrincipal());
  }

  private Boolean isUserSuperAdmin() {
    return SUPERADMIN
        .name()
        .equals(manageDatabase.getHandleDbRequests().getUsersInfo(getUserName()).getRole());
  }

  public List<KwTenantModel> getAllTenants() {
    boolean allowToRetrieve =
        isUserSuperAdmin() && commonUtilsService.getTenantId(getUserName()) == DEFAULT_TENANT_ID;

    if (!allowToRetrieve) {
      return new ArrayList<>();
    }

    List<KwTenants> tenants = manageDatabase.getHandleDbRequests().getTenants();

    Map<Integer, UserInfo> superAdminMap = manageDatabase.getUserInfoMap(SUPERADMIN);

    List<KwTenantModel> tenantModels =
        tenants.stream()
            .map(
                tenant -> {
                  KwTenantModel kwTenantModel = new KwTenantModel(tenant);
                  UserInfo superAdmin = superAdminMap.get(tenant.getTenantId());
                  kwTenantModel.setEmailId(superAdmin == null ? null : superAdmin.getMailid());
                  return kwTenantModel;
                })
            .collect(Collectors.toList());

    return tenantModels;
  }

  public KwClustersModelResponse getClusterDetails(String clusterId) {
    try {
      int tenantId = commonUtilsService.getTenantId(getUserName());
      KwClusters kwClusters =
          manageDatabase
              .getHandleDbRequests()
              .getClusterDetails(Integer.parseInt(clusterId), tenantId);

      if (kwClusters == null) {
        return null;
      }

      KwClustersModelResponse kwClustersModel = new KwClustersModelResponse(kwClusters);
      return kwClustersModel;
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
      return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_108);
    }

    if (isExternal && !isAuthorizedFor(ADD_TENANT)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    KwTenants kwTenants = new KwTenants();
    copyProperties(kwTenantModel, kwTenants);
    Boolean active = isExternal || kwTenantModel.isActiveTenant();
    kwTenants.setIsActive(active.toString());

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

      commonUtilsService.updateMetadata(
          tenantId, EntityType.TENANT, MetadataOperationType.CREATE, null);
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
            tenantId, EntityType.ROLES_PERMISSIONS, MetadataOperationType.CREATE, null);
        commonUtilsService.updateMetadata(
            tenantId, EntityType.PROPERTIES, MetadataOperationType.CREATE, null);
      }
      return ApiResponse.builder()
          .success(true)
          .message(addNewTenantStatus)
          .data("" + tenantId)
          .build();
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public KwTenantModel getMyTenantInfo() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Optional<KwTenants> tenant = manageDatabase.getHandleDbRequests().getMyTenants(tenantId);
    KwTenantModel kwTenantModel = new KwTenantModel();
    if (tenant.isPresent()) {
      kwTenantModel = new KwTenantModel(tenant.get());
      kwTenantModel.setAuthorizedToDelete(isAuthorizedFor(UPDATE_DELETE_MY_TENANT));
    }
    return kwTenantModel;
  }

  public ApiResponse deleteTenant() throws KlawException {
    if (!isAuthorizedFor(UPDATE_DELETE_MY_TENANT)) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (tenantId == DEFAULT_TENANT_ID) {
      return ApiResponse.NOT_AUTHORIZED;
    }
    String tenantName = manageDatabase.getTenantMap().get(tenantId);

    List<UserInfo> allUsers = manageDatabase.getHandleDbRequests().getAllUsersInfo(tenantId);
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
            tenantId, EntityType.TENANT, MetadataOperationType.DELETE, null);
        SecurityContextHolder.getContext().setAuthentication(null);
        return ApiResponse.builder().success(true).message(result).data(tenantName).build();
      } else {
        return ApiResponse.notOk(result);
      }

    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse updateTenant(KwTenantModel kwTenantModel) throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(
        commonUtilsService.getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    KwTenants kwTenants = new KwTenants();
    copyProperties(kwTenantModel, kwTenants);

    try {
      String result = manageDatabase.getHandleDbRequests().addNewTenant(kwTenants);

      if (ApiResultStatus.SUCCESS.value.equals(result)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.TENANT, MetadataOperationType.UPDATE, null);
        return ApiResponse.ok(result);
      } else {
        return ApiResponse.notOk(result);
      }
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  public EnvUpdatedStatus getUpdateEnvStatus(String envId) throws KlawBadRequestException {

    EnvUpdatedStatus envUpdatedStatus = new EnvUpdatedStatus();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<Env> allEnvs = manageDatabase.getAllEnvList(tenantId);
    Optional<Env> optionalEnv =
        allEnvs.stream()
            .filter(e -> e.getId().equals(envId) && e.getTenantId().equals(tenantId))
            .findFirst();

    if (optionalEnv.isEmpty()) {
      throw new KlawBadRequestException("No Such environment.");
    }

    ClusterStatus status;
    Env env = optionalEnv.get();
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.of(env.getType()), tenantId)
            .get(env.getClusterId());
    try {
      status =
          clusterApiService.getKafkaClusterStatus(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              env.getType(),
              kwClusters.getKafkaFlavor(),
              tenantId);

    } catch (Exception e) {
      status = ClusterStatus.OFFLINE;
      log.error("Error from getUpdateEnvStatus ", e);
    }
    LocalDateTime statusTime = LocalDateTime.now(ZoneOffset.UTC);
    env.setEnvStatus(status);
    env.setEnvStatusTime(statusTime);
    env.setEnvStatusTimeString(DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(statusTime));

    // Is this required can we remove it?
    kwClusters.setClusterStatus(status);
    manageDatabase.getHandleDbRequests().addNewCluster(kwClusters);

    manageDatabase.addEnvToCache(tenantId, env, false);

    envUpdatedStatus.setResult(ApiResultStatus.SUCCESS.value);
    envUpdatedStatus.setEnvStatus(status);
    envUpdatedStatus.setEnvStatusTime(statusTime);
    envUpdatedStatus.setEnvStatusTimeString(
        DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(statusTime));

    return envUpdatedStatus;
  }

  @Cacheable(cacheNames = "tenantsinfo")
  public TenantInfo getTenantsInfo() {
    TenantInfo tenantsInfo = new TenantInfo();
    tenantsInfo.setTenants(manageDatabase.getTenantMap().size());
    tenantsInfo.setTeams(manageDatabase.getAllTeamsSize());
    tenantsInfo.setClusters(manageDatabase.getAllClustersSize());
    tenantsInfo.setTopics(manageDatabase.getHandleDbRequests().getAllTopicsCountInAllTenants());

    return tenantsInfo;
  }

  public ClusterInfo getClusterInfoFromEnv(String envSelected, String clusterType) {
    //    Map<String, String> clusterInfo = new HashMap<>();
    ClusterInfo clusterInfo = new ClusterInfo();
    log.debug("getClusterInfoFromEnv {}", envSelected);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (!isAuthorizedFor(ADD_EDIT_DELETE_ENVS)
        && !commonUtilsService.getEnvsFromUserId(getUserName()).contains(envSelected)) {
      return null;
    }

    Env env = manageDatabase.getHandleDbRequests().getEnvDetails(envSelected, tenantId);
    KwClusters kwClusters =
        manageDatabase.getHandleDbRequests().getClusterDetails(env.getClusterId(), tenantId);

    clusterInfo.setAivenCluster(
        KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value.equals(kwClusters.getKafkaFlavor()));
    return clusterInfo;
  }

  public List<SupportedProtocolInfo> getSupportedKafkaProtocols() {
    List<SupportedProtocolInfo> supportedProtocols = new ArrayList<>();
    for (KafkaSupportedProtocol kafkaSupportedProtocol : KafkaSupportedProtocol.values()) {
      SupportedProtocolInfo supportedProtocolInfo = new SupportedProtocolInfo();
      supportedProtocolInfo.setName(kafkaSupportedProtocol.getName());
      supportedProtocolInfo.setValue(kafkaSupportedProtocol.getValue());
      supportedProtocols.add(supportedProtocolInfo);
    }

    return supportedProtocols;
  }
}
