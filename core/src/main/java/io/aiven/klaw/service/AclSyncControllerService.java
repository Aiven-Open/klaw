package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ACL_SYNC_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_SYNC_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.ACL_SYNC_ERR_104;
import static io.aiven.klaw.error.KlawErrorMessages.SYNC_ERR_101;
import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncAclUpdates;
import io.aiven.klaw.model.SyncBackAcls;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclPermissionType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AclSyncControllerService {

  private int TOPIC_COUNTER = 0;

  private static String AIVEN_ACL_ID_KEY = "aivenaclid";
  @Autowired ManageDatabase manageDatabase;

  @Autowired private final MailUtils mailService;

  @Autowired private final ClusterApiService clusterApiService;

  @Autowired private CommonUtilsService commonUtilsService;

  AclSyncControllerService(ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public ApiResponse updateSyncAcls(List<SyncAclUpdates> syncAclUpdates) throws KlawException {
    log.info("updateSyncAcls {}", syncAclUpdates);
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_SUBSCRIPTIONS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    List<Acl> listTopics = new ArrayList<>();
    Acl t;

    if (syncAclUpdates != null && syncAclUpdates.size() > 0) {
      Map<String, SyncAclUpdates> stringSyncAclUpdatesHashMap = new HashMap<>();

      // remove duplicates
      for (SyncAclUpdates syncAclUpdateItem : syncAclUpdates) {
        stringSyncAclUpdatesHashMap.remove(syncAclUpdateItem.getSequence());
        stringSyncAclUpdatesHashMap.put(syncAclUpdateItem.getSequence(), syncAclUpdateItem);
      }

      for (Map.Entry<String, SyncAclUpdates> stringSyncAclUpdatesEntry :
          stringSyncAclUpdatesHashMap.entrySet()) {
        SyncAclUpdates syncAclUpdateItem = stringSyncAclUpdatesEntry.getValue();

        // tenant filtering
        if (!commonUtilsService
            .getEnvsFromUserId(userName)
            .contains(syncAclUpdateItem.getEnvSelected())) {
          return ApiResponse.builder()
              .success(false)
              .message(ApiResultStatus.NOT_AUTHORIZED.value)
              .build();
        }

        t = new Acl();

        if (syncAclUpdateItem.getReq_no() != null) {
          t.setReq_no(Integer.parseInt(syncAclUpdateItem.getReq_no()));
        }
        if (syncAclUpdateItem.getAclId() != null) {
          Map<String, String> jsonParams = new HashMap<>();
          jsonParams.put(AIVEN_ACL_ID_KEY, syncAclUpdateItem.getAclId());
          t.setJsonParams(jsonParams);
        }
        t.setTopicname(syncAclUpdateItem.getTopicName());
        t.setConsumergroup(syncAclUpdateItem.getConsumerGroup());
        t.setAclip(syncAclUpdateItem.getAclIp());
        t.setAclssl(syncAclUpdateItem.getAclSsl());
        t.setTeamId(
            manageDatabase.getTeamIdFromTeamName(tenantId, syncAclUpdateItem.getTeamSelected()));
        t.setEnvironment(syncAclUpdateItem.getEnvSelected());
        t.setAclType(syncAclUpdateItem.getAclType());
        t.setAclPatternType(AclPatternType.LITERAL.value);
        t.setTenantId(tenantId);
        if (syncAclUpdateItem.getAclSsl() != null && syncAclUpdateItem.getAclSsl().length() > 0) {
          t.setAclIpPrincipleType(AclIPPrincipleType.PRINCIPAL);
        } else {
          t.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
        }

        listTopics.add(t);
      }
    } else {
      return ApiResponse.builder().success(false).message(SYNC_ERR_101).build();
    }

    try {
      if (!listTopics.isEmpty()) {
        return ApiResponse.builder()
            .success(true)
            .message(manageDatabase.getHandleDbRequests().addToSyncacls(listTopics))
            .build();
      }
      return ApiResponse.builder().success(false).message(SYNC_ERR_101).build();
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse updateSyncBackAcls(SyncBackAcls syncBackAcls) throws KlawException {
    log.info("updateSyncBackAcls {}", syncBackAcls);
    Map<String, List<String>> resultMap = new HashMap<>();

    List<String> logArray = new ArrayList<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    logArray.add(
        "Source Environment " + getEnvDetails(syncBackAcls.getSourceEnv(), tenantId).getName());
    logArray.add(
        "Target Environment " + getEnvDetails(syncBackAcls.getTargetEnv(), tenantId).getName());
    logArray.add("Type of Sync " + syncBackAcls.getTypeOfSync());

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.SYNC_BACK_SUBSCRIPTIONS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    List<String> resultStatus = new ArrayList<>();
    resultStatus.add(ApiResultStatus.SUCCESS.value);

    resultMap.put("result", resultStatus);
    try {
      if ("SELECTED_ACLS".equals(syncBackAcls.getTypeOfSync())) {
        for (String aclId : syncBackAcls.getAclIds()) {
          Acl acl =
              manageDatabase
                  .getHandleDbRequests()
                  .getSyncAclsFromReqNo(Integer.parseInt(aclId), tenantId);
          if (acl != null) {
            approveSyncBackAcls(syncBackAcls, resultMap, logArray, acl, tenantId);
          }
        }
      } else {
        List<Acl> acls =
            manageDatabase.getHandleDbRequests().getSyncAcls(syncBackAcls.getSourceEnv(), tenantId);
        for (Acl acl : acls) {
          approveSyncBackAcls(syncBackAcls, resultMap, logArray, acl, tenantId);
        }
      }
    } catch (Exception e) {
      log.error("Error ", e);
      throw new KlawException(e.getMessage());
    }

    return ApiResponse.builder()
        .success(true)
        .message(ApiResultStatus.SUCCESS.value)
        .data(logArray)
        .build();
  }

  private void approveSyncBackAcls(
      SyncBackAcls syncBackAcls,
      Map<String, List<String>> resultMap,
      List<String> logUpdateSyncBackTopics,
      Acl aclFound,
      int tenantId) {
    String userName = getUserName();
    try {
      AclRequests aclReq = new AclRequests();
      copyProperties(aclFound, aclReq);
      aclReq.setReq_no(null);
      aclReq.setAcl_ip(aclFound.getAclip());
      aclReq.setAcl_ssl(aclFound.getAclssl());
      aclReq.setEnvironment(syncBackAcls.getTargetEnv());
      aclReq.setRequestingteam(aclFound.getTeamId());
      aclReq.setRequestOperationType(RequestOperationType.CREATE.value);
      aclReq.setRequestor(userName);
      aclReq.setTenantId(tenantId);

      ResponseEntity<ApiResponse> response = clusterApiService.approveAclRequests(aclReq, tenantId);

      ApiResponse responseBody = response.getBody();
      String resultAclNullCheck = Objects.requireNonNull(responseBody).getMessage();
      if (!Objects.requireNonNull(resultAclNullCheck).contains(ApiResultStatus.SUCCESS.value)) {
        log.error("Error in creating acl {} {}", aclFound, responseBody);
        logUpdateSyncBackTopics.add(
            String.format(ACL_SYNC_ERR_102, aclFound.getTopicname() + " " + resultAclNullCheck));
      } else if (resultAclNullCheck.contains("Acl already exists")) {
        logUpdateSyncBackTopics.add(String.format(ACL_SYNC_ERR_103, aclFound.getTopicname()));
      } else {
        Env env =
            manageDatabase
                .getHandleDbRequests()
                .getEnvDetails(syncBackAcls.getSourceEnv(), tenantId);
        KwClusters kwClusters =
            manageDatabase
                .getClusters(KafkaClustersType.of(env.getType()), tenantId)
                .get(env.getClusterId());
        // Update aivenaclid in klaw metadata
        if (kwClusters.getKafkaFlavor().equals(KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value)) {
          Map<String, String> jsonParams = new HashMap<>();
          if (Objects.requireNonNull(responseBody).isSuccess()) {
            Object responseData = responseBody.getData();
            if (responseData instanceof Map) {
              Map<String, String> dataMap = (Map<String, String>) responseData;
              if (dataMap.containsKey(AIVEN_ACL_ID_KEY)) {
                jsonParams = dataMap;
              }
            }
          }

          manageDatabase
              .getHandleDbRequests()
              .updateJsonParams(jsonParams, aclFound.getReq_no(), tenantId);
        }
        if (!Objects.equals(syncBackAcls.getSourceEnv(), syncBackAcls.getTargetEnv())) {
          logUpdateSyncBackTopics.add(String.format(ACL_SYNC_ERR_104, aclFound.getTopicname()));
          // Create request
          Map<String, String> resultMapReq =
              manageDatabase.getHandleDbRequests().requestForAcl(aclReq);
          if (resultMapReq.containsKey("aclId")) {
            Integer aclId = Integer.parseInt(resultMapReq.get("aclId"));
            aclReq.setReq_no(aclId);
            // Approve request
            Map<String, String> emptyJsonParams = new HashMap<>();
            manageDatabase
                .getHandleDbRequests()
                .updateAclRequest(aclReq, userName, emptyJsonParams, true);
          }
        }
      }
    } catch (KlawException e) {
      log.error("Error in creating acl {}", aclFound, e);
      List<String> resultStatus = new ArrayList<>();
      resultStatus.add("Error :" + e.getMessage());
      resultMap.put("result", resultStatus);
    }
  }

  private List<Map<String, String>> getAclListFromCluster(
      String bootstrapHost,
      Env envSelected,
      KafkaSupportedProtocol protocol,
      String clusterName,
      String topicNameSearch,
      int tenantId)
      throws KlawException {
    List<Map<String, String>> aclList;
    aclList = clusterApiService.getAcls(bootstrapHost, envSelected, protocol, tenantId);
    return updateConsumerGroups(groupAcls(aclList, topicNameSearch, true), aclList);
  }

  private List<Map<String, String>> updateConsumerGroups(
      List<Map<String, String>> groupedList, List<Map<String, String>> clusterAclList) {
    List<Map<String, String>> updateList = new ArrayList<>(groupedList);

    for (Map<String, String> hMapGroupItem : groupedList) {
      for (Map<String, String> hMapItem : clusterAclList) {
        if (AclPermissionType.READ.value.equals(hMapGroupItem.get("operation"))
            && AclPermissionType.READ.value.equals(hMapItem.get("operation"))
            && "GROUP".equals(hMapItem.get("resourceType"))) {
          if (Objects.equals(hMapItem.get("host"), hMapGroupItem.get("host"))
              && Objects.equals(hMapItem.get("principle"), hMapGroupItem.get("principle"))) {
            Map<String, String> hashMap = new HashMap<>(hMapGroupItem);
            hashMap.put("consumerGroup", hMapItem.get("resourceName"));
            updateList.add(hashMap);
            break;
          }
        }
      }
    }
    return updateList;
  }

  private List<Map<String, String>> groupAcls(
      List<Map<String, String>> aclList, String topicNameSearch, boolean isSync) {

    return aclList.stream()
        .filter(
            hItem -> {
              if (isSync) {
                if (topicNameSearch != null) {
                  return "TOPIC".equals(hItem.get("resourceType"))
                      && hItem.get("resourceName").contains(topicNameSearch);
                } else {
                  return "TOPIC".equals(hItem.get("resourceType"));
                }
              } else {
                return Objects.equals(hItem.get("resourceName"), topicNameSearch);
              }
            })
        .collect(Collectors.toList());
  }

  private List<Acl> getAclsFromSOT(
      String env, String topicNameSearch, boolean regex, int tenantId) {
    List<Acl> aclsFromSOT;
    if (!regex)
      aclsFromSOT =
          manageDatabase.getHandleDbRequests().getSyncAcls(env, topicNameSearch, tenantId);
    else {
      aclsFromSOT = manageDatabase.getHandleDbRequests().getSyncAcls(env, tenantId);
      List<Acl> topicFilteredList = aclsFromSOT;
      // Filter topics on topic name for search
      if (topicNameSearch != null && topicNameSearch.length() > 0) {
        final String topicSearchFilter = topicNameSearch;
        topicFilteredList =
            aclsFromSOT.stream()
                .filter(acl -> acl.getTopicname().contains(topicSearchFilter))
                .collect(Collectors.toList());
      }
      aclsFromSOT = topicFilteredList;
    }

    return aclsFromSOT;
  }

  public List<AclInfo> getSyncAcls(
      String env, String pageNo, String currentPage, String topicNameSearch, String showAllAcls)
      throws KlawException {
    log.info(
        "getSyncAcls env: {} topicNameSearch: {} showAllAcls:{}",
        env,
        topicNameSearch,
        showAllAcls);
    boolean isReconciliation = !Boolean.parseBoolean(showAllAcls);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (topicNameSearch != null) {
      topicNameSearch = topicNameSearch.trim();
    }

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_SUBSCRIPTIONS)) {
      return null;
    }

    List<Map<String, String>> aclList;

    Env envSelected = getEnvDetails(env, tenantId);
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA, tenantId)
            .get(envSelected.getClusterId());
    aclList =
        getAclListFromCluster(
            kwClusters.getBootstrapServers(),
            envSelected,
            kwClusters.getProtocol(),
            kwClusters.getClusterName(),
            topicNameSearch,
            tenantId);

    List<Acl> aclsFromSOT = getAclsFromSOT(env, topicNameSearch, true, tenantId);

    TOPIC_COUNTER = 0;
    return getAclsList(
        pageNo,
        currentPage,
        applyFiltersAcls(
            env, aclList, aclsFromSOT, isReconciliation, tenantId, kwClusters.getKafkaFlavor()));
  }

  public List<AclInfo> getSyncBackAcls(
      String envId, String pageNo, String currentPage, String topicNameSearch, String teamName) {
    log.info("getSyncBackAcls {} {} {} {}", envId, pageNo, topicNameSearch, teamName);
    String userName = getUserName();
    if (topicNameSearch != null) topicNameSearch = topicNameSearch.trim();

    int tenantId = commonUtilsService.getTenantId(userName);
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.SYNC_BACK_SUBSCRIPTIONS)) {
      return null;
    }

    List<Acl> aclsFromSOT;

    if (topicNameSearch != null && topicNameSearch.trim().length() > 0) {
      aclsFromSOT = getAclsFromSOT(envId, topicNameSearch, false, tenantId);
    } else {
      aclsFromSOT = getAclsFromSOT(envId, topicNameSearch, true, tenantId);
    }

    List<AclInfo> aclInfoList;
    Integer loggedInUserTeam = commonUtilsService.getTeamId(userName);
    aclInfoList =
        getAclsList(
            pageNo, currentPage, applyFiltersAclsForSOT(loggedInUserTeam, aclsFromSOT, tenantId));

    return aclInfoList;
  }

  private List<AclInfo> applyFiltersAclsForSOT(
      Integer loggedInUserTeam, List<Acl> aclsFromSOT, int tenantId) {

    List<AclInfo> aclList = new ArrayList<>();
    AclInfo mp;

    for (Acl aclSotItem : aclsFromSOT) {
      mp = new AclInfo();
      mp.setEnvironment(aclSotItem.getEnvironment());
      mp.setEnvironmentName(getEnvDetails(aclSotItem.getEnvironment(), tenantId).getName());
      mp.setTopicname(aclSotItem.getTopicname());
      mp.setAcl_ip(aclSotItem.getAclip());
      mp.setAcl_ssl(aclSotItem.getAclssl());
      mp.setTransactionalId(aclSotItem.getTransactionalId());
      mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, aclSotItem.getTeamId()));
      mp.setConsumergroup(aclSotItem.getConsumergroup());
      mp.setTopictype(aclSotItem.getAclType());
      mp.setAclPatternType(aclSotItem.getAclPatternType());
      mp.setReq_no(aclSotItem.getReq_no() + "");
      if (aclSotItem.getTeamId() != null && aclSotItem.getTeamId().equals(loggedInUserTeam))
        mp.setShowDeleteAcl(true);

      if (aclSotItem.getAclip() != null || aclSotItem.getAclssl() != null) {
        aclList.add(mp);
      }
    }
    return aclList;
  }

  private List<AclInfo> applyFiltersAcls(
      String env,
      List<Map<String, String>> aclList,
      List<Acl> aclsFromSOT,
      boolean isReconciliation,
      int tenantId,
      String kafkaFlavor) {

    List<AclInfo> aclListMap = new ArrayList<>();
    List<String> teamList = new ArrayList<>();
    teamList = tenantFiltering(teamList);

    for (Map<String, String> aclListItem : aclList) {
      AclInfo mp = new AclInfo();
      mp.setEnvironment(env);
      mp.setPossibleTeams(teamList);
      mp.setTeamname("");
      if (aclListItem.containsKey(AIVEN_ACL_ID_KEY)) {
        mp.setAclId(aclListItem.get(AIVEN_ACL_ID_KEY));
      }

      String tmpPermType = aclListItem.get("operation");

      if (AclPermissionType.WRITE.value.equals(tmpPermType)) {
        mp.setTopictype(AclType.PRODUCER.value);
      } else if (AclPermissionType.READ.value.equals(tmpPermType)) {
        mp.setTopictype(AclType.CONSUMER.value);
        if (aclListItem.get("consumerGroup") != null) {
          mp.setConsumergroup(aclListItem.get("consumerGroup"));
        } else {
          continue;
        }
      }

      if ("topic".equalsIgnoreCase(aclListItem.get("resourceType"))) {
        mp.setTopicname(aclListItem.get("resourceName"));
      }

      mp.setAcl_ip(aclListItem.get("host"));
      mp.setAcl_ssl(aclListItem.get("principle"));

      for (Acl aclSotItem : aclsFromSOT) {
        String acl_ssl = aclSotItem.getAclssl();
        String acl_host = aclSotItem.getAclip();

        if (acl_ssl == null || acl_ssl.equals("")) {
          acl_ssl = "User:*";
        } else {
          if (!KafkaFlavors.AIVEN_FOR_APACHE_KAFKA.value.equals(kafkaFlavor)
              && !"User:*".equals(acl_ssl)
              && !acl_ssl.startsWith("User:")) {
            acl_ssl = "User:" + acl_ssl;
          }
        }

        if (acl_host == null || acl_host.equals("")) {
          acl_host = "*";
        }

        if (aclSotItem.getTopicname() != null
            && Objects.equals(aclListItem.get("resourceName"), aclSotItem.getTopicname())
            && Objects.equals(aclListItem.get("host"), acl_host)
            && Objects.equals(aclListItem.get("principle"), acl_ssl)
            && Objects.equals(aclSotItem.getAclType(), mp.getTopictype())) {
          mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, aclSotItem.getTeamId()));
          mp.setTeamid(aclSotItem.getTeamId());
          mp.setReq_no(aclSotItem.getReq_no() + "");
          break;
        }
      }

      if (mp.getTeamname() == null) {
        mp.setTeamname("Unknown");
      }

      if (!verifyIfTopicExists(aclListItem, aclsFromSOT)) {
        continue;
      }

      if (isReconciliation) {
        if ("Unknown".equals(mp.getTeamname()) || "".equals(mp.getTeamname())) {
          aclListMap.add(mp);
        }
      } else {
        if (teamList.contains(mp.getTeamname())) aclListMap.add(mp);
        else if ("Unknown".equals(mp.getTeamname()) || "".equals(mp.getTeamname())) {
          aclListMap.add(mp);
        }
      }
    }
    return aclListMap;
  }

  private boolean verifyIfTopicExists(
      Map<String, String> aclListItemFromCluster, List<Acl> aclsFromLocalMetadata) {
    String topicName;
    if ("topic".equalsIgnoreCase(aclListItemFromCluster.get("resourceType"))) {
      topicName = aclListItemFromCluster.get("resourceName");
      String finalTopicName = topicName;
      return aclsFromLocalMetadata.stream()
          .anyMatch(acl -> acl.getTopicname().equals(finalTopicName));
    }

    return false;
  }

  private List<String> tenantFiltering(List<String> teamList) {
    if (!commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.SYNC_BACK_SUBSCRIPTIONS)
        || !commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_TOPICS)
        || !commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.SYNC_SUBSCRIPTIONS)
        || !commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.SYNC_BACK_TOPICS)) {
      // tenant filtering
      int tenantId = commonUtilsService.getTenantId(getUserName());
      List<Team> teams = manageDatabase.getHandleDbRequests().getAllTeams(tenantId);
      teams =
          teams.stream()
              .filter(t -> Objects.equals(t.getTenantId(), tenantId))
              .collect(Collectors.toList());
      List<String> teamListUpdated = new ArrayList<>();
      for (Team teamsItem : teams) {
        teamListUpdated.add(teamsItem.getTeamname());
      }
      teamList = teamListUpdated;
    }
    return teamList;
  }

  private List<AclInfo> getAclsList(String pageNo, String currentPage, List<AclInfo> aclListMap) {
    List<AclInfo> aclListMapUpdated = new ArrayList<>();

    int totalRecs = aclListMap.size();
    int recsPerPage = 20;
    int totalPages =
        aclListMap.size() / recsPerPage + (aclListMap.size() % recsPerPage > 0 ? 1 : 0);

    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);
    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < totalRecs; i++) {
      int counterInc = counterIncrement();
      if (i >= startVar && i < lastVar) {
        AclInfo mp = aclListMap.get(i);
        mp.setSequence(counterInc + "");

        mp.setTotalNoPages(totalPages + "");
        mp.setCurrentPage(pageNo);
        mp.setAllPageNos(numList);

        aclListMapUpdated.add(mp);
      }
    }
    return aclListMapUpdated;
  }

  private int counterIncrement() {
    TOPIC_COUNTER++;
    return TOPIC_COUNTER;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  public Env getEnvDetails(String envId, int tenantId) {

    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(tenantId).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
