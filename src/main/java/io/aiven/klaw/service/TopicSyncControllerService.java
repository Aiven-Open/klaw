package io.aiven.klaw.service;

import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclType;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.KafkaClustersType;
import io.aiven.klaw.model.PermissionType;
import io.aiven.klaw.model.RequestOperationType;
import io.aiven.klaw.model.RequestStatus;
import io.aiven.klaw.model.SyncBackTopics;
import io.aiven.klaw.model.SyncTopicUpdates;
import io.aiven.klaw.model.SyncTopicsBulk;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.TopicRequestTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@EnableScheduling
@Service
@Slf4j
public class TopicSyncControllerService {

  @Autowired private ClusterApiService clusterApiService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  private int topicCounter = 0;

  // default at 7 am everyday
  //    @Scheduled(cron = "${klaw.recontopics.fixedtime:0 7 * * ?}")
  public void getReconTopicsScheduledAsync() {
    CompletableFuture.runAsync(this::getReconTopicsScheduled);
  }

  public void getReconTopicsScheduled() {
    Map<Integer, List<String>> envTenantMap = manageDatabase.getEnvsOfTenantsMap();
    Map<Integer, String> tenantMap = manageDatabase.getTenantMap();
    List<Integer> tenants = new ArrayList<>(envTenantMap.keySet());

    for (Integer tenantId : tenants) {
      List<String> envsStrList = envTenantMap.get(tenantId);
      StringBuilder reconStr = new StringBuilder();
      reconStr.append("Tenant : ").append(tenantMap.get(tenantId)).append("\n");

      for (String envStr : envsStrList) {
        reconStr = new StringBuilder();
        try {
          @SuppressWarnings("unchecked")
          List<TopicRequestModel> results =
              (List<TopicRequestModel>)
                  getReconTopics(envStr, "-1", "", null, "false", false).get("resultSet");

          for (TopicRequestModel topicRequestModel : results) {
            reconStr
                .append(topicRequestModel.getTopicname())
                .append(topicRequestModel.getEnvironmentName())
                .append(topicRequestModel.getRemarks())
                .append("\n");
          }

          if (!results.isEmpty()) {
            mailService.sendReconMailToAdmin(
                "Reconciliation of Topics",
                reconStr.toString(),
                tenantMap.get(tenantId),
                tenantId,
                commonUtilsService.getLoginUrl());
          }
        } catch (Exception e) {
          log.error("Exception:", e);
        }
      }
    }
  }

  public Map<String, Object> getReconTopics(
      String envId,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      String showAllTopics,
      boolean isBulkOption)
      throws Exception {
    Map<String, Object> syncTopicsObjectMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    List<TopicRequestModel> topicRequestModelList =
        (List<TopicRequestModel>)
            getSyncTopics(envId, pageNo, currentPage, topicNameSearch, showAllTopics, isBulkOption)
                .get("resultSet");

    topicRequestModelList =
        topicRequestModelList.stream()
            .filter(
                topicRequestModel ->
                    topicRequestModel != null
                        && ("DELETED".equals(topicRequestModel.getRemarks())
                            || topicRequestModel.getTeamname() == null
                            || topicRequestModel.getTeamname().equals("")))
            .collect(Collectors.toList());

    int allTopicsCount = topicRequestModelList.size();

    topicRequestModelList.forEach(
        topicReq -> {
          topicReq.setEnvironmentName(getEnvDetails(envId).getName());
          topicReq.setTopicstatus("ON_CLUSTER");
        });
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (!"-1".equals(pageNo)) // scheduler call
    topicRequestModelList =
          getPagedTopicReqModels(pageNo, currentPage, topicRequestModelList, tenantId);

    syncTopicsObjectMap.put("resultSet", topicRequestModelList);
    syncTopicsObjectMap.put("allTopicsCount", allTopicsCount);

    return syncTopicsObjectMap;
  }

  public Map<String, Object> getSyncTopics(
      String env,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      String showAllTopics,
      boolean isBulkOption)
      throws Exception {
    boolean isReconciliation = !Boolean.parseBoolean(showAllTopics);
    Map<String, Object> syncTopicsObjectMap = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    log.info("getSyncTopics {} {} {}", env, pageNo, topicNameSearch);

    if (!"-1".equals(pageNo)) { // ignore check for scheduler
      if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_TOPICS))
        return null;
    }

    List<Map<String, String>> topicFilteredList = getTopicsFromKafkaCluster(env, topicNameSearch);
    List<Map<String, String>> topicsList;

    topicsList =
        topicFilteredList.stream()
            .sorted(new TopicControllerService.TopicNameSyncComparator())
            .collect(Collectors.toList());

    List<TopicRequestModel> deletedTopicsFromClusterList = new ArrayList<>();

    List<Integer> sizeOfTopics = new ArrayList<>();

    if (isReconciliation) {
      syncTopicsObjectMap.put(
          "resultSet",
          getSyncTopicListRecon(
              topicsList, deletedTopicsFromClusterList, pageNo, env, isBulkOption, tenantId));
      syncTopicsObjectMap.put("allTopicsCount", topicsList.size());
    } else {
      syncTopicsObjectMap.put(
          "resultSet",
          getSyncTopicList(
              topicsList,
              deletedTopicsFromClusterList,
              pageNo,
              currentPage,
              env,
              isBulkOption,
              sizeOfTopics,
              tenantId));
      syncTopicsObjectMap.put("allTopicsCount", sizeOfTopics.get(0));
    }

    return syncTopicsObjectMap;
  }

  private List<TopicRequestModel> getSyncTopicList(
      List<Map<String, String>> topicsList,
      List<TopicRequestModel> deletedTopicsFromClusterList,
      String pageNo,
      String currentPage,
      String env,
      boolean isBulkOption,
      List<Integer> sizeOfTopics,
      int tenantId) {
    // Get Sync topics
    List<Topic> topicsFromSOT =
        manageDatabase.getHandleDbRequests().getSyncTopics(env, null, tenantId);

    // tenant filtering
    topicsFromSOT = getFilteredTopicsForTenant(topicsFromSOT);
    int counterInc;
    List<String> teamList = new ArrayList<>();
    teamList = tenantFilterTeams(teamList);

    if (!isBulkOption)
      updateClusterDeletedTopicsList(
          topicsList, deletedTopicsFromClusterList, topicsFromSOT, teamList, tenantId);

    List<TopicRequest> topicsListMap = new ArrayList<>();

    for (int i = 0; i < topicsList.size(); i++) {
      counterInc = counterIncrement();
      TopicRequest mp = new TopicRequest();
      if (createTopicRequest(topicsList, topicsFromSOT, teamList, i, counterInc, mp, tenantId))
        topicsListMap.add(mp);
    }
    // topics which exist on cluster and not in kw, with no recon option.
    List<TopicRequestModel> topicRequestModelList =
        getTopicRequestModels(topicsListMap, false, tenantId);
    topicRequestModelList.addAll(deletedTopicsFromClusterList);

    sizeOfTopics.add(topicRequestModelList.size());
    return getPagedTopicReqModels(pageNo, currentPage, topicRequestModelList, tenantId);
  }

  private List<TopicRequestModel> getPagedTopicReqModels(
      String pageNo, String currentPage, List<TopicRequestModel> topicRequestModels, int tenantId) {
    List<TopicRequestModel> topicRequestModelList = new ArrayList<>();

    int totalRecs = topicRequestModels.size();
    int recsPerPage = 20;

    int totalPages =
        topicRequestModels.size() / recsPerPage
            + (topicRequestModels.size() % recsPerPage > 0 ? 1 : 0);

    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);
    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < totalRecs; i++) {

      if (i >= startVar && i < lastVar) {
        TopicRequestModel mp = topicRequestModels.get(i);

        mp.setTotalNoPages(totalPages + "");
        mp.setAllPageNos(numList);
        mp.setCurrentPage(pageNo);
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, mp.getTeamId()));
        topicRequestModelList.add(mp);
      }
    }
    return topicRequestModelList;
  }

  private List<TopicRequestModel> getSyncTopicListRecon(
      List<Map<String, String>> clusterTopicsList,
      List<TopicRequestModel> deletedTopicsFromClusterList,
      String pageNo,
      String env,
      boolean isBulkOption,
      int tenantId) {
    // Get Sync topics
    List<Topic> topicsFromSOT =
        manageDatabase.getHandleDbRequests().getSyncTopics(env, null, tenantId);

    // tenant filtering
    topicsFromSOT = getFilteredTopicsForTenant(topicsFromSOT);
    List<TopicRequest> topicsListMap = new ArrayList<>();

    List<String> teamList = new ArrayList<>();
    teamList = tenantFilterTeams(teamList);
    int counterInc;

    if (!isBulkOption)
      updateClusterDeletedTopicsList(
          clusterTopicsList, deletedTopicsFromClusterList, topicsFromSOT, teamList, tenantId);

    for (int i = 0; i < clusterTopicsList.size(); i++) {
      counterInc = counterIncrement();
      TopicRequest mp = new TopicRequest();
      if (createTopicRequest(
          clusterTopicsList, topicsFromSOT, teamList, i, counterInc, mp, tenantId))
        if (mp.getTeamId().equals(0) || mp.getTeamId() == null) topicsListMap.add(mp);
    }

    // topics which exist in cluster and not in kw.
    List<TopicRequestModel> topicRequestModelList =
        getTopicRequestModels(topicsListMap, false, tenantId);
    topicRequestModelList.addAll(deletedTopicsFromClusterList);

    return topicRequestModelList;
  }

  private List<TopicRequestModel> getTopicRequestModels(
      List<TopicRequest> topicsList, boolean fromSyncTopics, int tenantId) {
    List<TopicRequestModel> topicRequestModelList = new ArrayList<>();
    TopicRequestModel topicRequestModel;
    Integer userTeamId = getMyTeamId(getUserName());
    List<String> approverRoles =
        rolesPermissionsControllerService.getApproverRoles("CONNECTORS", tenantId);
    List<UserInfo> userList =
        manageDatabase.getHandleDbRequests().selectAllUsersInfoForTeam(userTeamId, tenantId);

    for (TopicRequest topicReq : topicsList) {
      topicRequestModel = new TopicRequestModel();
      copyProperties(topicReq, topicRequestModel);

      if (fromSyncTopics) {
        // show approving info only before approvals
        if (!RequestStatus.approved.name().equals(topicRequestModel.getTopicstatus())) {
          if (topicRequestModel.getTopictype() != null
              && TopicRequestTypes.Claim.name().equals(topicRequestModel.getTopictype())) {
            List<Topic> topics = getTopicFromName(topicRequestModel.getTopicname(), tenantId);
            topicRequestModel.setApprovingTeamDetails(
                updateApproverInfo(
                    manageDatabase
                        .getHandleDbRequests()
                        .selectAllUsersInfoForTeam(topics.get(0).getTeamId(), tenantId),
                    manageDatabase.getTeamNameFromTeamId(tenantId, topics.get(0).getTeamId()),
                    approverRoles,
                    topicRequestModel.getRequestor()));
          } else
            topicRequestModel.setApprovingTeamDetails(
                updateApproverInfo(
                    userList,
                    manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId),
                    approverRoles,
                    topicRequestModel.getRequestor()));
        }
      }

      topicRequestModelList.add(topicRequestModel);
    }
    return topicRequestModelList;
  }

  private String updateApproverInfo(
      List<UserInfo> userList, String teamName, List<String> approverRoles, String requestor) {
    StringBuilder approvingInfo = new StringBuilder("Team : " + teamName + ", Users : ");

    for (UserInfo userInfo : userList) {
      if (approverRoles.contains(userInfo.getRole())
          && !Objects.equals(requestor, userInfo.getUsername()))
        approvingInfo.append(userInfo.getUsername()).append(",");
    }

    return String.valueOf(approvingInfo);
  }

  private boolean createTopicRequest(
      List<Map<String, String>> topicsList,
      List<Topic> topicsFromSOT,
      List<String> teamList,
      int i,
      int counterInc,
      TopicRequest mp,
      int tenantId) {
    Map<String, String> topicMap;
    mp.setSequence(counterInc + "");

    topicMap = topicsList.get(i);
    final String tmpTopicName = topicMap.get("topicName");

    mp.setTopicname(tmpTopicName);
    mp.setTopicpartitions(Integer.parseInt(topicMap.get("partitions")));
    mp.setReplicationfactor(topicMap.get("replicationFactor"));

    String teamUpdated = null;

    try {
      Optional<Topic> teamUpdatedFirst =
          topicsFromSOT.stream()
              .filter(a -> Objects.equals(a.getTopicname(), tmpTopicName))
              .findFirst();

      if (teamUpdatedFirst.isPresent())
        teamUpdated =
            manageDatabase.getTeamNameFromTeamId(tenantId, teamUpdatedFirst.get().getTeamId());
    } catch (Exception e) {
      log.error("Error from getSyncTopicList ", e);
    }

    if (teamUpdated != null && !teamUpdated.equals("undefined")) {
      mp.setPossibleTeams(teamList);
      if (teamList.contains(teamUpdated))
        mp.setTeamId(manageDatabase.getTeamIdFromTeamName(tenantId, teamUpdated));
      else return false; // belongs to different tenant
    } else {
      mp.setPossibleTeams(teamList);
      mp.setTeamId(0);
      mp.setRemarks("ADDED");
    }

    return true;
  }

  private void updateClusterDeletedTopicsList(
      List<Map<String, String>> clusterTopicsList,
      List<TopicRequestModel> deletedTopicsFromClusterList,
      List<Topic> topicsFromSOT,
      List<String> teamList,
      int tenantId) {
    try {
      List<String> clusterTopicStringList = new ArrayList<>();
      clusterTopicsList.forEach(
          hashMapTopicObj -> clusterTopicStringList.add(hashMapTopicObj.get("topicName")));

      Map<String, TopicRequestModel> sotTopics = new HashMap<>();

      List<String> sotTopicStringList = new ArrayList<>();
      for (Topic topicObj : topicsFromSOT) {
        List<String> possibleTeams = new ArrayList<>();
        possibleTeams.add(manageDatabase.getTeamNameFromTeamId(tenantId, topicObj.getTeamId()));
        possibleTeams.add("REMOVE FROM KLAW");

        sotTopicStringList.add(topicObj.getTopicname());
        TopicRequestModel topicRequestModel = new TopicRequestModel();
        topicRequestModel.setTopicname(topicObj.getTopicname());
        topicRequestModel.setEnvironment(topicObj.getEnvironment());
        topicRequestModel.setTopicpartitions(topicObj.getNoOfPartitions());
        topicRequestModel.setReplicationfactor(topicObj.getNoOfReplcias());
        topicRequestModel.setTeamId(topicObj.getTeamId());
        topicRequestModel.setTeamname(
            manageDatabase.getTeamNameFromTeamId(tenantId, topicObj.getTeamId()));
        topicRequestModel.setPossibleTeams(possibleTeams);
        topicRequestModel.setSequence("" + topicObj.getTopicid());
        topicRequestModel.setRemarks("DELETED");

        // tenant teams
        if (teamList.contains(manageDatabase.getTeamNameFromTeamId(tenantId, topicObj.getTeamId())))
          sotTopics.put(topicObj.getTopicname(), topicRequestModel);
      }

      List<String> deletedTopicsFromClusterListTmp =
          sotTopicStringList.stream()
              .filter(topicName -> !clusterTopicStringList.contains(topicName))
              .collect(Collectors.toList());
      deletedTopicsFromClusterListTmp.forEach(
          topicName -> deletedTopicsFromClusterList.add(sotTopics.get(topicName)));
    } catch (Exception e) {
      log.error("Error from updateClusterDeletedTopicsList ", e);
    }
  }

  private List<String> tenantFilterTeams(List<String> teamList) {
    if (!commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.SYNC_BACK_SUBSCRIPTIONS)
        || !commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_TOPICS)
        || !commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.SYNC_SUBSCRIPTIONS)
        || !commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.SYNC_BACK_TOPICS)) {
      // tenant filtering
      int tenantId = commonUtilsService.getTenantId(getUserName());

      List<Team> teams = manageDatabase.getHandleDbRequests().selectAllTeams(tenantId);

      List<String> teamListUpdated = new ArrayList<>();
      for (Team teamsItem : teams) {
        teamListUpdated.add(teamsItem.getTeamname());
      }
      teamList = teamListUpdated;
    }
    return teamList;
  }

  public ApiResponse updateSyncBackTopics(SyncBackTopics syncBackTopics) {
    log.info("updateSyncBackTopics {}", syncBackTopics);
    Map<String, List<String>> resultMap = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    List<String> logArray = new ArrayList<>();

    logArray.add("Source Environment " + getEnvDetails(syncBackTopics.getSourceEnv()).getName());
    logArray.add("Target Environment " + getEnvDetails(syncBackTopics.getTargetEnv()).getName());
    logArray.add("Type of Sync " + syncBackTopics.getTypeOfSync());

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_BACK_TOPICS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    List<String> resultStatus = new ArrayList<>();
    resultStatus.add(ApiResultStatus.SUCCESS.value);
    resultMap.put("result", resultStatus);

    if ("SELECTED_TOPICS".equals(syncBackTopics.getTypeOfSync())) {
      for (String topicId : syncBackTopics.getTopicIds()) {
        Optional<Topic> topicFoundOptional =
            manageDatabase
                .getHandleDbRequests()
                .getTopicFromId(Integer.parseInt(topicId), tenantId);
        Topic topicFound;
        if (topicFoundOptional.isPresent()) {
          topicFound = topicFoundOptional.get();
          approveSyncBackTopics(syncBackTopics, resultMap, logArray, topicFound, tenantId);
        }
      }
    } else {
      List<Topic> topics =
          manageDatabase
              .getHandleDbRequests()
              .getTopicsFromEnv(syncBackTopics.getSourceEnv(), tenantId);
      for (Topic topicFound : topics) {
        approveSyncBackTopics(syncBackTopics, resultMap, logArray, topicFound, tenantId);
      }
    }

    return ApiResponse.builder().result(resultMap.get("result").get(0)).data(logArray).build();
  }

  private void approveSyncBackTopics(
      SyncBackTopics syncBackTopics,
      Map<String, List<String>> resultMap,
      List<String> logUpdateSyncBackTopics,
      Topic topicFound,
      int tenantId) {
    try {
      ResponseEntity<ApiResponse> response =
          clusterApiService.approveTopicRequests(
              topicFound.getTopicname(),
              RequestOperationType.CREATE.value,
              topicFound.getNoOfPartitions(),
              topicFound.getNoOfReplcias(),
              syncBackTopics.getTargetEnv(),
              tenantId);

      if (!Objects.equals(
          Objects.requireNonNull(response.getBody()).getResult(), ApiResultStatus.SUCCESS.value)) {
        log.error("Error in creating topic {} {}", topicFound, response.getBody());
        if (Objects.requireNonNull(response.getBody()).getResult().contains("TopicExistsException"))
          logUpdateSyncBackTopics.add(
              "Error in Topic creation. Topic:"
                  + topicFound.getTopicname()
                  + " already exists. TopicExistsException");
        else
          logUpdateSyncBackTopics.add(
              "Error in Topic creation. Topic:"
                  + topicFound.getTopicname()
                  + " "
                  + response.getBody());
      } else {
        logUpdateSyncBackTopics.add("Topic created " + topicFound.getTopicname());
        if (!Objects.equals(syncBackTopics.getSourceEnv(), syncBackTopics.getTargetEnv()))
          createAndApproveTopicRequest(syncBackTopics, topicFound, tenantId);
      }
    } catch (KlawException e) {
      log.error("Error in creating topic {}", topicFound, e);
      List<String> resultStatus = new ArrayList<>();
      resultStatus.add("Error :" + e.getMessage());
      resultMap.put("result", resultStatus);
    }
  }

  private void createAndApproveTopicRequest(
      SyncBackTopics syncBackTopics, Topic topicFound, int tenantId) {
    List<Topic> topics = getTopicFromName(topicFound.getTopicname(), tenantId);
    Integer teamName;
    if (topics != null && topics.size() > 0) {
      teamName = topics.get(0).getTeamId();

      TopicRequest topicRequest;
      topicRequest = new TopicRequest();
      topicRequest.setTopictype(RequestOperationType.CREATE.value);
      topicRequest.setTopicname(topicFound.getTopicname());
      topicRequest.setEnvironment(syncBackTopics.getTargetEnv());
      topicRequest.setTopicpartitions(topicFound.getNoOfPartitions());
      topicRequest.setReplicationfactor(topicFound.getNoOfReplcias());
      topicRequest.setRequestor(getUserName());
      topicRequest.setUsername(getUserName());
      topicRequest.setTeamId(teamName);
      topicRequest.setTenantId(tenantId);
      // Create request
      Map<String, String> createResult =
          manageDatabase.getHandleDbRequests().requestForTopic(topicRequest);
      // Approve request
      if (createResult.get("topicId") != null) {
        topicRequest.setTopicid(Integer.parseInt(createResult.get("topicId")));
        manageDatabase.getHandleDbRequests().updateTopicRequest(topicRequest, getUserName());
      }
    }
  }

  public List<TopicInfo> getTopicsRowView(
      String env,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      String teamName,
      String topicType) {
    log.info("getTopicsRowView {}", topicNameSearch);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    List<TopicInfo> topicListUpdated =
        getTopicsPaginated(
            env, pageNo, currentPage, topicNameSearch, teamName, topicType, tenantId);

    if (topicListUpdated != null && topicListUpdated.size() > 0) return topicListUpdated;

    return new ArrayList<>();
  }

  private List<TopicInfo> getTopicsPaginated(
      String env,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      String teamName,
      String topicType,
      int tenantId) {
    if (topicNameSearch != null) topicNameSearch = topicNameSearch.trim();

    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();

    // To get Producer or Consumer topics, first get all topics based on acls and then filter
    List<Topic> producerConsumerTopics = new ArrayList<>();
    if ((AclType.PRODUCER.value.equals(topicType) || AclType.CONSUMER.value.equals(topicType))
        && teamName != null) {
      producerConsumerTopics =
          handleDbRequests.selectAllTopicsByTopictypeAndTeamname(
              topicType, manageDatabase.getTeamIdFromTeamName(tenantId, teamName), tenantId);

      // tenant filtering, not really necessary though, as based on team is searched.
      producerConsumerTopics = getFilteredTopicsForTenant(producerConsumerTopics);

      // select all topics and then filter
      env = "ALL";
      teamName = null;
    }

    // Get Sync topics
    List<Topic> topicsFromSOT =
        handleDbRequests.getSyncTopics(
            env, manageDatabase.getTeamIdFromTeamName(tenantId, teamName), tenantId);
    topicsFromSOT = getFilteredTopicsForTenant(topicsFromSOT);

    // tenant filtering
    List<Env> listAllEnvs = manageDatabase.getKafkaEnvList(tenantId);
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");

    topicsFromSOT = groupTopicsByEnv(topicsFromSOT);
    List<Topic> filterProducerConsumerList = new ArrayList<>();
    String tmpTopicFull, tmpTopicSub;

    if (producerConsumerTopics.size() > 0) {
      for (Topic topicInfo : topicsFromSOT) {
        for (Topic producerConsumerTopic : producerConsumerTopics) {
          tmpTopicFull = producerConsumerTopic.getTopicname();

          if (tmpTopicFull.endsWith("--PREFIXED--")) { // has prefixed acl
            tmpTopicSub = tmpTopicFull.replaceAll("--PREFIXED--", "");
            if (topicInfo.getTopicname().startsWith(tmpTopicSub)
                && topicInfo
                    .getEnvironmentsList()
                    .contains(producerConsumerTopic.getEnvironment())) {
              topicInfo.setEnvironmentsList(producerConsumerTopic.getEnvironmentsList());
              filterProducerConsumerList.add(topicInfo);
            }
          } else if (Objects.equals(producerConsumerTopic.getTopicname(), topicInfo.getTopicname())
              && topicInfo.getEnvironmentsList().contains(producerConsumerTopic.getEnvironment())) {
            topicInfo.setEnvironmentsList(producerConsumerTopic.getEnvironmentsList());
            filterProducerConsumerList.add(topicInfo);
          }
        }
      }
      topicsFromSOT = filterProducerConsumerList;
    }

    List<Topic> topicFilteredList = topicsFromSOT;
    // Filter topics on topic name for search
    if (topicNameSearch != null && topicNameSearch.length() > 0) {
      final String topicSearchFilter = topicNameSearch;
      topicFilteredList =
          topicsFromSOT.stream()
              .filter(topic -> topic.getTopicname().contains(topicSearchFilter))
              .collect(Collectors.toList());

      // searching documentation
      List<Topic> searchDocList =
          topicsFromSOT.stream()
              .filter(
                  topic ->
                      (topic.getDocumentation() != null
                          && topic.getDocumentation().contains(topicSearchFilter)))
              .collect(Collectors.toList());

      topicFilteredList.addAll(searchDocList);
      topicFilteredList =
          new ArrayList<>(
              topicFilteredList.stream()
                  .collect(
                      Collectors.toConcurrentMap(
                          Topic::getTopicname, Function.identity(), (p, q) -> p))
                  .values());
    }

    topicsFromSOT =
        topicFilteredList.stream()
            .sorted(new TopicControllerService.TopicNameComparator())
            .collect(Collectors.toList());

    return getTopicInfoList(topicsFromSOT, pageNo, currentPage, listAllEnvs, orderOfEnvs, tenantId);
  }

  private List<TopicInfo> getTopicInfoList(
      List<Topic> topicsFromSOT,
      String pageNo,
      String currentPage,
      List<Env> listAllEnvs,
      String orderOfEnvs,
      int tenantId) {
    int totalRecs = topicsFromSOT.size();
    int recsPerPage = 21;

    int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);
    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);

    List<TopicInfo> topicsListMap = null;
    if (totalRecs > 0) topicsListMap = new ArrayList<>();

    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < topicsFromSOT.size(); i++) {
      int counterInc = counterIncrement();
      if (i >= startVar && i < lastVar) {
        TopicInfo mp = new TopicInfo();
        mp.setSequence(counterInc + "");
        Topic topicSOT = topicsFromSOT.get(i);

        List<String> envList = topicSOT.getEnvironmentsList();
        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        mp.setTopicid(topicSOT.getTopicid());
        mp.setCluster(topicSOT.getEnvironment());
        mp.setEnvironmentsList(getConvertedEnvs(listAllEnvs, envList));
        mp.setTopicName(topicSOT.getTopicname());
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topicSOT.getTeamId()));

        mp.setNoOfReplcias(topicSOT.getNoOfReplcias());
        mp.setNoOfPartitions(topicSOT.getNoOfPartitions());
        mp.setDescription(topicSOT.getDescription());

        mp.setTotalNoPages(totalPages + "");
        mp.setCurrentPage(pageNo);
        mp.setAllPageNos(numList);

        if (topicsListMap != null) {
          topicsListMap.add(mp);
        }
      }
    }

    return topicsListMap;
  }

  private List<String> getConvertedEnvs(List<Env> allEnvs, List<String> selectedEnvs) {
    List<String> newEnvList = new ArrayList<>();
    for (String env : selectedEnvs) {
      for (Env env1 : allEnvs) {
        if (Objects.equals(env, env1.getId())) {
          newEnvList.add(env1.getName());
          break;
        }
      }
    }

    return newEnvList;
  }

  private int counterIncrement() {
    topicCounter++;
    return topicCounter;
  }

  private List<Topic> groupTopicsByEnv(List<Topic> topicsFromSOT) {
    List<Topic> tmpTopicList = new ArrayList<>();

    Map<String, List<Topic>> groupedList =
        topicsFromSOT.stream().collect(Collectors.groupingBy(Topic::getTopicname));
    groupedList.forEach(
        (k, v) -> {
          Topic t = v.get(0);
          List<String> tmpEnvList = new ArrayList<>();
          for (Topic topic : v) {
            tmpEnvList.add(topic.getEnvironment());
          }
          t.setEnvironmentsList(tmpEnvList);
          tmpTopicList.add(t);
        });
    return tmpTopicList;
  }

  public ApiResponse updateSyncTopicsBulk(SyncTopicsBulk syncTopicsBulk) throws KlawException {
    log.info("updateSyncTopicsBulk {}", syncTopicsBulk);
    //    Map<String, List<String>> resultMap = new HashMap<>();

    List<String> logArray = new ArrayList<>();

    logArray.add("Source Environment " + getEnvDetails(syncTopicsBulk.getSourceEnv()).getName());
    logArray.add("Assigned to Team " + syncTopicsBulk.getSelectedTeam());
    logArray.add("Type of Sync " + syncTopicsBulk.getTypeOfSync());

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_TOPICS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    //    List<String> resultStatus = new ArrayList<>();
    //    resultStatus.add(ApiResultStatus.SUCCESS.value);
    //
    //    resultMap.put("result", resultStatus);

    if ("SELECTED_TOPICS".equals(syncTopicsBulk.getTypeOfSync())) {
      Object[] topicMap = syncTopicsBulk.getTopicDetails();
      Map<String, Map<String, Object>> hashMap = new HashMap<>();
      Map<String, Object> subObj;
      for (Object o : topicMap) {
        subObj = (Map<String, Object>) o;
        hashMap.put((String) subObj.get("topicName"), subObj);
      }

      for (String topicName : syncTopicsBulk.getTopicNames()) {
        invokeUpdateSync(syncTopicsBulk, logArray, hashMap, topicName);
      }
    } else {
      try {
        List<Map<String, String>> topicsMap =
            getTopicsFromKafkaCluster(
                syncTopicsBulk.getSourceEnv(), syncTopicsBulk.getTopicSearchFilter());
        for (Map<String, String> hashMap : topicsMap) {
          invokeUpdateSyncAllTopics(syncTopicsBulk, logArray, hashMap);
        }
      } catch (Exception e) {
        log.error("Could not retrieve topics ", e);
        throw new KlawException(e.getMessage());
      }
    }

    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).data(logArray).build();
  }

  private void invokeUpdateSyncAllTopics(
      SyncTopicsBulk syncTopicsBulk, List<String> logArray, Map<String, String> hashMap) {
    SyncTopicUpdates syncTopicUpdates;
    List<SyncTopicUpdates> updatedSyncTopicsList = new ArrayList<>();

    syncTopicUpdates = new SyncTopicUpdates();
    syncTopicUpdates.setTeamSelected(syncTopicsBulk.getSelectedTeam());
    syncTopicUpdates.setTopicName(hashMap.get("topicName"));
    syncTopicUpdates.setEnvSelected(syncTopicsBulk.getSourceEnv());
    syncTopicUpdates.setPartitions(Integer.parseInt(hashMap.get("partitions")));
    syncTopicUpdates.setReplicationFactor(hashMap.get("replicationFactor"));

    updatedSyncTopicsList.add(syncTopicUpdates);
    try {
      logArray.add(
          "Topic status :"
              + hashMap.get("topicName")
              + " "
              + updateSyncTopics(updatedSyncTopicsList).getResult());
    } catch (Exception e) {
      logArray.add("Topic update failed :" + hashMap.get("topicName") + " " + e.toString());
      log.error("Exception:", e);
    }
  }

  private List<Map<String, String>> getTopicsFromKafkaCluster(String env, String topicNameSearch)
      throws Exception {
    if (topicNameSearch != null) topicNameSearch = topicNameSearch.trim();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    Env envSelected = getEnvDetails(env);
    String bootstrapHost =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA.value, tenantId)
            .get(envSelected.getClusterId())
            .getBootstrapServers();

    List<Map<String, String>> topicsList =
        clusterApiService.getAllTopics(
            bootstrapHost,
            manageDatabase
                .getClusters(KafkaClustersType.KAFKA.value, tenantId)
                .get(envSelected.getClusterId())
                .getProtocol(),
            manageDatabase
                .getClusters(KafkaClustersType.KAFKA.value, tenantId)
                .get(envSelected.getClusterId())
                .getClusterName(),
            tenantId);

    topicCounter = 0;

    List<Map<String, String>> topicFilteredList = topicsList;
    // Filter topics on topic name for search

    if (topicNameSearch != null && topicNameSearch.length() > 0) {
      final String topicSearchFilter = topicNameSearch;
      topicFilteredList =
          topicsList.stream()
              .filter(topic -> topic.get("topicName").contains(topicSearchFilter))
              .collect(Collectors.toList());
    }
    return topicFilteredList;
  }

  private void invokeUpdateSync(
      SyncTopicsBulk syncTopicsBulk,
      List<String> logArray,
      Map<String, Map<String, Object>> hashMap,
      String topicName) {
    SyncTopicUpdates syncTopicUpdates;
    List<SyncTopicUpdates> updatedSyncTopicsList = new ArrayList<>();

    syncTopicUpdates = new SyncTopicUpdates();
    syncTopicUpdates.setTeamSelected(syncTopicsBulk.getSelectedTeam());
    syncTopicUpdates.setTopicName(topicName);
    syncTopicUpdates.setEnvSelected(syncTopicsBulk.getSourceEnv());
    syncTopicUpdates.setPartitions((Integer) hashMap.get(topicName).get("topicPartitions"));
    syncTopicUpdates.setReplicationFactor(
        hashMap.get(topicName).get("topicReplicationFactor") + "");

    updatedSyncTopicsList.add(syncTopicUpdates);
    try {
      logArray.add(
          "Topic status :" + topicName + " " + updateSyncTopics(updatedSyncTopicsList).getResult());
    } catch (Exception e) {
      log.error("Exception:", e);
      logArray.add("Topic update failed :" + topicName + " " + e);
    }
  }

  public ApiResponse updateSyncTopics(List<SyncTopicUpdates> updatedSyncTopics)
      throws KlawException {
    log.info("updateSyncTopics {}", updatedSyncTopics);
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_TOPICS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    // tenant filtering
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");

    List<Topic> existingTopics;
    List<Topic> listTopics = new ArrayList<>();
    Topic t;

    StringBuilder erroredTopics = new StringBuilder();
    boolean topicsWithDiffTeams = false;

    StringBuilder erroredTopicsExist = new StringBuilder();
    boolean topicsDontExistInMainCluster = false;
    int topicId = manageDatabase.getHandleDbRequests().getNextTopicRequestId("TOPIC_ID", tenantId);

    // remove duplicates
    updatedSyncTopics =
        updatedSyncTopics.stream()
            .filter(
                topicUpdate ->
                    topicUpdate.getTeamSelected() != null
                        && !topicUpdate.getTeamSelected().equals(""))
            .collect(Collectors.toList());

    List<Integer> updatedSyncTopicsDelete = new ArrayList<>();
    updatedSyncTopics = handleTopicDeletes(updatedSyncTopics, updatedSyncTopicsDelete, tenantId);

    if (updatedSyncTopics.size() > 0) {
      for (SyncTopicUpdates topicUpdate : updatedSyncTopics) {
        // tenant filtering
        if (!getEnvsFromUserId(userDetails).contains(topicUpdate.getEnvSelected())) {
          return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
        }
        existingTopics = getTopicFromName(topicUpdate.getTopicName(), tenantId);

        if (existingTopics != null) {
          for (Topic existingTopic : existingTopics) {
            if (existingTopic.getEnvironment().equals(syncCluster)) {
              if (!manageDatabase
                      .getTeamNameFromTeamId(tenantId, existingTopic.getTeamId())
                      .equals(topicUpdate.getTeamSelected())
                  && !topicUpdate.getEnvSelected().equals(syncCluster)) {
                erroredTopics.append(topicUpdate.getTopicName()).append(" ");
                topicsWithDiffTeams = true;
              }
              break;
            }
          }
        } else if (!Objects.equals(syncCluster, topicUpdate.getEnvSelected())) {
          erroredTopicsExist.append(topicUpdate.getTopicName()).append(" ");
          if (checkInPromotionOrder(
              topicUpdate.getTopicName(), topicUpdate.getEnvSelected(), orderOfEnvs))
            topicsDontExistInMainCluster = true;
        }

        boolean topicAdded = false;
        if (existingTopics == null) {
          t = new Topic();

          topicId = topicId + 1;
          t.setTopicid(topicId);
          t.setTopicname(topicUpdate.getTopicName());
          t.setNoOfPartitions(topicUpdate.getPartitions());
          t.setNoOfReplcias(topicUpdate.getReplicationFactor());
          t.setEnvironment(topicUpdate.getEnvSelected());
          t.setTeamId(
              manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
          t.setDescription("Topic description");
          t.setExistingTopic(false);
          t.setTenantId(tenantId);

          listTopics.add(t);
        } else {
          for (Topic existingTopic : existingTopics) {
            if (Objects.equals(existingTopic.getEnvironment(), topicUpdate.getEnvSelected())) {
              t = existingTopic;
              t.setTeamId(
                  manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
              t.setTopicname(topicUpdate.getTopicName());
              t.setEnvironment(existingTopic.getEnvironment());
              t.setExistingTopic(true);
              t.setTenantId(tenantId);
              listTopics.add(t);
              topicAdded = true;
            } else if (!Objects.equals(
                manageDatabase.getTeamNameFromTeamId(tenantId, existingTopic.getTeamId()),
                topicUpdate.getTeamSelected())) {
              t = existingTopic;
              t.setTeamId(
                  manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
              t.setTopicname(topicUpdate.getTopicName());
              t.setEnvironment(existingTopic.getEnvironment());
              t.setExistingTopic(true);
              t.setTenantId(tenantId);
              listTopics.add(t);
              topicAdded = true;
            }
          }
        }

        boolean envFound = false;
        if (existingTopics != null) {
          for (Topic existingTopic : existingTopics) {
            if (Objects.equals(existingTopic.getEnvironment(), topicUpdate.getEnvSelected())) {
              envFound = true;
              break;
            }
          }
          if (!envFound && !topicAdded) {
            t = new Topic();
            topicId = topicId + 1;
            t.setTopicid(topicId);
            t.setTopicname(topicUpdate.getTopicName());
            t.setNoOfPartitions(topicUpdate.getPartitions());
            t.setNoOfReplcias(topicUpdate.getReplicationFactor());
            t.setEnvironment(topicUpdate.getEnvSelected());
            t.setTeamId(
                manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
            t.setDescription("Topic description");
            t.setExistingTopic(false);
            t.setTenantId(tenantId);

            listTopics.add(t);
          }
        }
      }
    }

    if (updatedSyncTopics.size() == 0 && updatedSyncTopicsDelete.size() > 0) {
      return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    }

    if (topicsDontExistInMainCluster) {
      return ApiResponse.builder()
          .result(
              "Failure. Please sync up the team of the following topic(s) first in"
                  + " main Sync cluster (klaw.syncdata.cluster)"
                  + " :"
                  + syncCluster
                  + ". \n Topics : "
                  + erroredTopicsExist)
          .build();
    }

    if (topicsWithDiffTeams) {
      return ApiResponse.builder()
          .result(
              "Failure. The following topics are being synchronized with"
                  + " a different team, when compared to main Sync cluster (klaw.syncdata.cluster)"
                  + " :"
                  + syncCluster
                  + ". \n Topics : "
                  + erroredTopics)
          .build();
    }

    if (listTopics.size() > 0) {
      try {
        return ApiResponse.builder()
            .result(manageDatabase.getHandleDbRequests().addToSynctopics(listTopics))
            .build();
      } catch (Exception e) {
        log.error(e.getMessage());
        throw new KlawException(e.getMessage());
      }
    } else {
      return ApiResponse.builder().result("No record updated.").build();
    }
  }

  private List<SyncTopicUpdates> handleTopicDeletes(
      List<SyncTopicUpdates> updatedSyncTopics,
      List<Integer> updatedSyncTopicsDelete,
      int tenantId) {
    List<SyncTopicUpdates> updatedSyncTopicsUpdated = new ArrayList<>();
    for (SyncTopicUpdates updatedSyncTopic : updatedSyncTopics) {
      if ("REMOVE FROM KLAW".equals(updatedSyncTopic.getTeamSelected())) {
        updatedSyncTopicsDelete.add(Integer.parseInt(updatedSyncTopic.getSequence()));
      } else updatedSyncTopicsUpdated.add(updatedSyncTopic);
    }

    // delete topic
    for (Integer topicId : updatedSyncTopicsDelete) {
      manageDatabase.getHandleDbRequests().deleteTopic(topicId, tenantId);
    }

    return updatedSyncTopicsUpdated;
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public List<Topic> getTopicFromName(String topicName, int tenantId) {
    List<Topic> topics = manageDatabase.getHandleDbRequests().getTopicTeam(topicName, tenantId);

    // tenant filtering
    topics = getFilteredTopicsForTenant(topics);

    return topics;
  }

  public Env getEnvDetails(String envId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(commonUtilsService.getTenantId(getUserName())).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  private List<String> getEnvsFromUserId(String userDetails) {
    Integer userTeamId = getMyTeamId(userDetails);
    return manageDatabase.getTeamsAndAllowedEnvs(
        userTeamId, commonUtilsService.getTenantId(userDetails));
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }

  private List<Topic> getFilteredTopicsForTenant(List<Topic> topicsFromSOT) {
    // tenant filtering
    try {
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      if (topicsFromSOT != null)
        topicsFromSOT =
            topicsFromSOT.stream()
                .filter(topic -> allowedEnvIdList.contains(topic.getEnvironment()))
                .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return topicsFromSOT;
  }

  private boolean checkInPromotionOrder(String topicname, String envId, String orderOfEnvs) {
    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
    return orderedEnv.contains(envId);
  }
}
