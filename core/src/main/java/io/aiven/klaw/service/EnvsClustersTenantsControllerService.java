package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_110;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_105;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_106;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_107;
import static io.aiven.klaw.error.KlawErrorMessages.ENV_CLUSTER_TNT_ERR_108;
import static io.aiven.klaw.helpers.KwConstants.DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER;
import static io.aiven.klaw.helpers.KwConstants.DEFAULT_TENANT_ID;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_KAFKA_CONNECT_ENVS;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
import static io.aiven.klaw.helpers.KwConstants.REQUEST_TOPICS_OF_ENVS;
import static io.aiven.klaw.helpers.KwConstants.SUPERADMIN_ROLE;
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
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.Pager;
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
import java.util.*;
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

  @Autowired ManageDatabase manageDatabase;

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

  public synchronized EnvModelResponse getEnvDetails(String envSelected, String clusterType) {
    String userName = getUserName();
    log.debug("getEnvDetails {}", envSelected);
    int tenantId = commonUtilsService.getTenantId(userName);
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      // tenant filtering
      if (!commonUtilsService.getEnvsFromUserId(userName).contains(envSelected)) {
        return null;
      }
    }

    Env env = manageDatabase.getHandleDbRequests().getEnvDetails(envSelected, tenantId);
    if (env != null && "false".equals(env.getEnvExists())) {
      return null;
    }

    if (env != null) {
      EnvModelResponse envModel = new EnvModelResponse();
      copyProperties(env, envModel);
      envModel.setClusterName(
          manageDatabase
              .getClusters(KafkaClustersType.of(clusterType), tenantId)
              .get(envModel.getClusterId())
              .getClusterName());
      envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId()));

      log.debug("Return env model {}", envModel);
      return envModel;
    }
    return null;
  }

  public UserInfoModel getUserDetails(String userId) {
    UserInfoModel userInfoModel = new UserInfoModel();
    UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(userId);
    if (userInfo != null) {
      copyProperties(userInfo, userInfoModel);
      userInfoModel.setUserPassword(MASKED_PWD);
      return userInfoModel;
    } else {
      return null;
    }
  }

  public List<KwClustersModelResponse> getClusters(String typeOfCluster) {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<KwClusters> clusters =
        new ArrayList<>(
            manageDatabase.getClusters(KafkaClustersType.of(typeOfCluster), tenantId).values());
    List<KwClustersModelResponse> clustersModels = new ArrayList<>();
    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    KwClustersModelResponse tmpClusterModel;
    for (KwClusters cluster : clusters) {
      tmpClusterModel = new KwClustersModelResponse();
      copyProperties(cluster, tmpClusterModel);
      tmpClusterModel.setKafkaFlavor(KafkaFlavors.of(cluster.getKafkaFlavor()));
      tmpClusterModel.setClusterType(KafkaClustersType.of(cluster.getClusterType()));

      KwClustersModelResponse finalTmpClusterModel = tmpClusterModel;
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

  public List<KwClustersModelResponse> getClustersPaginated(
      KafkaClustersType typeOfCluster, String clusterId, String pageNo, String searchClusterParam) {

    String clusterTypeValue = typeOfCluster.value;
    List<KwClustersModelResponse> kwClustersModelList = getClusters(clusterTypeValue);

    if (clusterId != null && !clusterId.equals("")) {
      kwClustersModelList =
          kwClustersModelList.stream()
              .filter(env -> Objects.equals((env.getClusterId() + "").toLowerCase(), clusterId))
              .collect(toList());
    }

    if (searchClusterParam != null && !searchClusterParam.equals("")) {
      kwClustersModelList =
          kwClustersModelList.stream()
              .filter(
                  env ->
                      env.getClusterName().toLowerCase().contains(searchClusterParam.toLowerCase())
                          || env.getBootstrapServers()
                              .toLowerCase()
                              .contains(searchClusterParam.toLowerCase())
                          || env.getProtocol()
                              .getName()
                              .toLowerCase()
                              .contains(searchClusterParam.toLowerCase()))
              .collect(
                  Collectors.collectingAndThen(
                      Collectors.toCollection(
                          () ->
                              new TreeSet<>(
                                  Comparator.comparing(KwClustersModelResponse::getClusterId))),
                      ArrayList::new));
    }
    return Pager.getItemsList(
        pageNo,
        "",
        10,
        kwClustersModelList,
        (pageContext, mp) -> {
          mp.setTotalNoPages(pageContext.getTotalPages());
          List<String> numList = new ArrayList<>();
          int totalPages = Integer.parseInt(pageContext.getTotalPages());
          for (int k = 1; k <= totalPages; k++) {
            numList.add("" + k);
          }
          mp.setAllPageNos(numList);
          mp.setCurrentPage(pageContext.getPageNo());
          return mp;
        });
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
      if (Objects.equals(syncCluster, env.getId())) {
        envIdInfo.setName(env.getName() + baseClusterDropDownStr);
      } else {
        envIdInfo.setName(env.getName());
      }

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
    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    // set deletable only to authorized, and check for count
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
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

    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

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
                      env.getName().toLowerCase().contains(searchEnvParam.toLowerCase())
                          || env.getClusterName()
                              .toLowerCase()
                              .contains(searchEnvParam.toLowerCase())
                          || (env.getOtherParams() != null
                              && env.getOtherParams()
                                  .toLowerCase()
                                  .contains(searchEnvParam.toLowerCase()))
                          || manageDatabase
                              .getTenantMap()
                              .get(env.getTenantId())
                              .toLowerCase()
                              .contains(searchEnvParam.toLowerCase()))
              .toList();
    }

    return Pager.getItemsList(
        "",
        pageNo,
        10,
        envListMap,
        (pageContext, mp) -> {
          mp.setTotalNoPages(pageContext.getTotalPages());
          List<String> numList = new ArrayList<>();
          int totalPages = Integer.parseInt(pageContext.getTotalPages());
          for (int k = 1; k <= totalPages; k++) {
            numList.add("" + k);
          }
          mp.setAllPageNos(numList);
          mp.setCurrentPage(pageContext.getPageNo());
          mp.setTotalRecs(pageContext.getTotalRecs());
          return mp;
        });
  }

  private List<EnvModelResponse> getEnvModels(
      List<Env> listEnvs, KafkaClustersType clusterType, int tenantId) {
    List<EnvModelResponse> envModelList = new ArrayList<>();
    EnvModelResponse envModel;
    KwClusters kwCluster;
    for (Env listEnv : listEnvs) {
      log.debug("Params {} for env {}", listEnv.getParams(), listEnv.getName());
      kwCluster = manageDatabase.getClusters(clusterType, tenantId).get(listEnv.getClusterId());
      if (kwCluster != null) {
        envModel = new EnvModelResponse();
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

  public EnvParams getEnvParams(Integer targetEnv) {
    return manageDatabase.getEnvParams(commonUtilsService.getTenantId(getUserName()), targetEnv);
  }

  public List<EnvModelResponse> getSchemaRegEnvs() {
    int tenantId = getUserDetails(getUserName()).getTenantId();
    List<Env> listEnvs = manageDatabase.getSchemaRegEnvList(tenantId);

    List<EnvModelResponse> envModelList =
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
            .filter(
                env -> {
                  boolean found = false;
                  for (String reqEnv : reqEnvs) {
                    if (Objects.equals(env.getId(), reqEnv)) {
                      found = true;
                      break;
                    }
                  }
                  return found;
                })
            .collect(toList());
    return envModelList;
  }

  public List<EnvModelResponse> getKafkaConnectEnvs() {
    String userName = getUserName();
    int tenantId = getUserDetails(userName).getTenantId();
    List<Env> listEnvs = manageDatabase.getKafkaConnectEnvList(tenantId);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
      listEnvs =
          listEnvs.stream().filter(env -> allowedEnvIdSet.contains(env.getId())).collect(toList());
    }

    List<EnvModelResponse> envModelList =
        getEnvModels(listEnvs, KafkaClustersType.KAFKA_CONNECT, tenantId);

    envModelList.forEach(
        envModel ->
            envModel.setTenantName(manageDatabase.getTenantMap().get(envModel.getTenantId())));

    // set deletable only to authorized, and check for count
    if (!commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
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
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
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
    for (int i = 0; i < maxNum; i++) {
      if ((i + 1) == defaultNum) {
        parameterList.add(i, String.valueOf(i + 1) + " (default)");
      } else {
        parameterList.add(i, String.valueOf(i + 1));
      }
    }
    return parameterList;
  }

  private boolean validateConnectedClusters(
      EnvModel newEnv, List<Integer> kafkaClusterIds, List<Integer> schemaClusterIds) {
    if (newEnv.getType().equals(KafkaClustersType.KAFKA.value)) {
      if (kafkaClusterIds.contains(newEnv.getClusterId())) {
        // don't allow same cluster id be assigned to another kafka env, if regex is not defined
        return newEnv.getParams().getTopicPrefix() == null
            && newEnv.getParams().getTopicSuffix() == null;
      }
    } else if (newEnv.getType().equals(KafkaClustersType.SCHEMA_REGISTRY.value)) {
      // don't allow same cluster id be assigned to another schema env
      return schemaClusterIds.contains(newEnv.getClusterId());
    }
    return false;
  }

  public ApiResponse addNewCluster(KwClustersModel kwClustersModel) {
    log.info("addNewCluster {}", kwClustersModel);
    Map<String, String> resultMap = new HashMap<>();

    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
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
    kwCluster.setClusterName(kwCluster.getClusterName().toUpperCase());
    kwCluster.setKafkaFlavor(kwClustersModel.getKafkaFlavor().value);
    kwCluster.setClusterType(kwClustersModel.getClusterType().value);

    // only for new cluster requests
    String result = manageDatabase.getHandleDbRequests().addNewCluster(kwCluster);
    if (result.equals(ApiResultStatus.SUCCESS.value)) {
      commonUtilsService.updateMetadata(
          tenantId, EntityType.CLUSTER, MetadataOperationType.CREATE, null);
      return ApiResponse.SUCCESS;
    }
    return ApiResponse.FAILURE;
  }

  public ApiResponse deleteCluster(String clusterId) throws KlawException {
    log.info("deleteCluster {}", clusterId);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_CLUSTERS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    List<Env> allEnvList = manageDatabase.getAllEnvList(tenantId);
    if (allEnvList.stream()
        .anyMatch(env -> Objects.equals(env.getClusterId(), Integer.parseInt(clusterId)))) {
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
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    switch (envType) {
      case "kafka":
        if (manageDatabase.getHandleDbRequests().existsKafkaComponentsForEnv(envId, tenantId)) {
          return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_105);
        }
        break;
      case "kafkaconnect":
        if (manageDatabase.getHandleDbRequests().existsConnectorComponentsForEnv(envId, tenantId)) {
          return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_106);
        }
        break;
      case "schemaregistry":
        if (manageDatabase.getHandleDbRequests().existsSchemaComponentsForEnv(envId, tenantId)) {
          return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_107);
        }
        break;
    }

    try {
      removeAssociatedKafkaOrSchemaEnvironment(envId, tenantId, envType);
      String result =
          manageDatabase.getHandleDbRequests().deleteEnvironmentRequest(envId, tenantId);
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

  private void removeAssociatedKafkaOrSchemaEnvironment(String envId, int tenantId, String envType)
      throws KlawException {

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
    // only assignable on a schema registry
    if (KafkaClustersType.SCHEMA_REGISTRY.value.equals(envType)) {
      log.debug("Env Tag supplied = {}", envTag);
      if (envTag != null && !envTag.getId().isEmpty()) {

        associateWithKafkaEnv(envTag, envId, envName, tenantId);
        // remove existing association if it exists
        removeAssociationWithKafkaEnv(envTag, envId, tenantId);

      } else {
        // envTag is always null here
        removeAssociationWithKafkaEnv(null, envId, tenantId);
      }
    } else if (KafkaClustersType.KAFKA.value.equals(envType)) {
      envTag = getKafkaAssociation(envTag, envId, tenantId);
    }

    return envTag;
  }

  private EnvTag getKafkaAssociation(EnvTag KafkaEnvTag, String kafkaEnvId, int tenantId) {
    if (KafkaEnvTag == null) {
      Env existing = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
      KafkaEnvTag = existing != null ? existing.getAssociatedEnv() : KafkaEnvTag;
    }
    return KafkaEnvTag;
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
    return mailService.getUserName(getPrincipal());
  }

  public List<KwTenantModel> getAllTenants() {
    if (SUPERADMIN
            .name()
            .equals(manageDatabase.getHandleDbRequests().getUsersInfo(getUserName()).getRole())
        && commonUtilsService.getTenantId(getUserName()) == DEFAULT_TENANT_ID) {
      HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
      List<KwTenants> tenants = dbHandle.getTenants();
      List<KwTenantModel> tenantModels = new ArrayList<>();

      List<UserInfo> allUsers = dbHandle.getAllUsersAllTenants();

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

        kwTenantModel.setActiveTenant(Boolean.parseBoolean(tenant.getIsActive()));
        tenantModels.add(kwTenantModel);
      }
      return tenantModels;
    } else {
      return new ArrayList<>();
    }
  }

  public KwClustersModelResponse getClusterDetails(String clusterId) {
    try {
      int tenantId = commonUtilsService.getTenantId(getUserName());
      KwClusters kwClusters =
          manageDatabase
              .getHandleDbRequests()
              .getClusterDetails(Integer.parseInt(clusterId), tenantId);
      if (kwClusters != null) {
        KwClustersModelResponse kwClustersModel = new KwClustersModelResponse();
        copyProperties(kwClusters, kwClustersModel);
        kwClustersModel.setKafkaFlavor(KafkaFlavors.of(kwClusters.getKafkaFlavor()));
        kwClustersModel.setClusterType(KafkaClustersType.of(kwClusters.getClusterType()));

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
      return ApiResponse.notOk(ENV_CLUSTER_TNT_ERR_108);
    }

    if (isExternal
        && commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ADD_TENANT)) {
      return ApiResponse.NOT_AUTHORIZED;
    }

    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantName(kwTenantModel.getTenantName());
    kwTenants.setTenantDesc(kwTenantModel.getTenantDesc());
    kwTenants.setContactPerson(kwTenantModel.getContactPerson());
    kwTenants.setOrgName(kwTenantModel.getOrgName());

    if (isExternal) {
      kwTenantModel.setActiveTenant(true);
    }

    if (kwTenantModel.isActiveTenant()) {
      kwTenants.setIsActive("true");
    } else {
      kwTenants.setIsActive("false");
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
      kwTenantModel.setTenantName(tenant.get().getTenantName());
      kwTenantModel.setContactPerson(tenant.get().getContactPerson());
      kwTenantModel.setActiveTenant("true".equals(tenant.get().getIsActive()));
      kwTenantModel.setOrgName(tenant.get().getOrgName());
      kwTenantModel.setTenantDesc(tenant.get().getTenantDesc());
      kwTenantModel.setTenantId(tenant.get().getTenantId());

      kwTenantModel.setAuthorizedToDelete(
          !commonUtilsService.isNotAuthorizedUser(
              getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT));
    }
    return kwTenantModel;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public ApiResponse deleteTenant() throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
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
        getPrincipal(), PermissionType.UPDATE_DELETE_MY_TENANT)) {
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
    Optional<Env> env =
        allEnvs.stream()
            .filter(e -> e.getId().equals(envId) && e.getTenantId().equals(tenantId))
            .findFirst();

    if (env.isEmpty()) {
      throw new KlawBadRequestException("No Such environment.");
    }

    ClusterStatus status;
    KwClusters kwClusters = null;
    kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.of(env.get().getType()), tenantId)
            .get(env.get().getClusterId());
    try {
      status =
          clusterApiService.getKafkaClusterStatus(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              env.get().getType(),
              kwClusters.getKafkaFlavor(),
              tenantId);

    } catch (Exception e) {
      status = ClusterStatus.OFFLINE;
      log.error("Error from getUpdateEnvStatus ", e);
    }
    LocalDateTime statusTime = LocalDateTime.now(ZoneOffset.UTC);
    env.get().setEnvStatus(status);
    env.get().setEnvStatusTime(statusTime);
    env.get().setEnvStatusTimeString(DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(statusTime));

    // Is this required can we remove it?
    kwClusters.setClusterStatus(status);
    manageDatabase.getHandleDbRequests().addNewCluster(kwClusters);

    manageDatabase.addEnvToCache(tenantId, env.get(), false);

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
    log.debug("getEnvDetails {}", envSelected);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ENVS)) {
      // tenant filtering
      if (!commonUtilsService.getEnvsFromUserId(getUserName()).contains(envSelected)) {
        return null;
      }
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
