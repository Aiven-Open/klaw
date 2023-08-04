package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.KAFKA_CONNECT_SYNC_102;
import static io.aiven.klaw.error.KlawErrorMessages.KAFKA_CONNECT_SYNC_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.KAFKA_CONNECT_SYNC_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.KAFKA_CONNECT_SYNC_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.SYNC_102;
import static io.aiven.klaw.error.KlawErrorMessages.SYNC_ERR_101;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncConnectorUpdates;
import io.aiven.klaw.model.cluster.ConnectorState;
import io.aiven.klaw.model.cluster.ConnectorsStatus;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.KafkaConnectorModelResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaConnectSyncControllerService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired ManageDatabase manageDatabase;

  public ApiResponse getConnectorDetails(String connectorName, String envId) throws KlawException {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA_CONNECT, tenantId)
            .get(getKafkaConnectorEnvDetails(envId).getClusterId());

    Map<String, Object> res =
        clusterApiService.getConnectorDetails(
            connectorName,
            kwClusters.getBootstrapServers(),
            kwClusters.getProtocol(),
            kwClusters.getClusterName() + kwClusters.getClusterId(),
            tenantId);

    try {
      String schemaOfObj = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(res);
      return ApiResponse.ok(schemaOfObj);
    } catch (JsonProcessingException e) {
      log.error("Exception:", e);
      return ApiResponse.notOk(e.getMessage());
    }
  }

  public ApiResponse updateSyncConnectors(List<SyncConnectorUpdates> updatedSyncTopics)
      throws KlawException {
    log.info("updateSyncConnectors {}", updatedSyncTopics);
    String userName = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_CONNECTORS)) {
      return ApiResponse.notOk(ApiResultStatus.NOT_AUTHORIZED.value);
    }

    // tenant filtering
    int tenantId = commonUtilsService.getTenantId(userName);
    String syncCluster =
        manageDatabase.getTenantConfig().get(tenantId).getBaseSyncKafkaConnectCluster();
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");

    List<KwKafkaConnector> existingTopics;
    List<KwKafkaConnector> kafkaConnectorList = new ArrayList<>();
    KwKafkaConnector t;

    StringBuilder erroredTopics = new StringBuilder();
    boolean topicsWithDiffTeams = false;

    StringBuilder erroredTopicsExist = new StringBuilder();
    boolean topicsDontExistInMainCluster = false;
    int topicId =
        manageDatabase.getHandleDbRequests().getNextConnectorRequestId("CONNECTOR_ID", tenantId);

    List<Integer> updatedSyncTopicsDelete = new ArrayList<>();
    updatedSyncTopics = handleConnectorDeletes(updatedSyncTopics, updatedSyncTopicsDelete);

    if (updatedSyncTopics.size() > 0) {
      for (SyncConnectorUpdates topicUpdate : updatedSyncTopics) {
        // tenant filtering
        if (!commonUtilsService
            .getEnvsFromUserId(userName)
            .contains(topicUpdate.getEnvSelected())) {
          return ApiResponse.notOk(ApiResultStatus.NOT_AUTHORIZED.value);
        }
        existingTopics = getConnectorsFromName(topicUpdate.getConnectorName(), tenantId);

        if (existingTopics != null) {
          for (KwKafkaConnector existingTopic : existingTopics) {
            if (Objects.equals(existingTopic.getEnvironment(), syncCluster)) {
              if (!Objects.equals(
                      manageDatabase.getTeamNameFromTeamId(tenantId, existingTopic.getTeamId()),
                      topicUpdate.getTeamSelected())
                  && !Objects.equals(topicUpdate.getEnvSelected(), syncCluster)) {
                erroredTopics.append(topicUpdate.getConnectorName()).append(" ");
                topicsWithDiffTeams = true;
              }
              break;
            }
          }
        } else if (!Objects.equals(topicUpdate.getEnvSelected(), syncCluster)) {
          erroredTopicsExist.append(topicUpdate.getConnectorName()).append(" ");
          if (checkInPromotionOrder(topicUpdate.getEnvSelected(), orderOfEnvs))
            topicsDontExistInMainCluster = true;
        }

        String connectorConfig;
        try {
          connectorConfig =
              getConnectorConfiguration(
                  topicUpdate.getConnectorName(), topicUpdate.getEnvSelected(), tenantId);
        } catch (KlawException | JsonProcessingException e) {
          log.error("Exception:", e);
          return ApiResponse.notOk(
              String.format(KAFKA_CONNECT_SYNC_ERR_101, topicUpdate.getConnectorName()));
        }

        boolean topicAdded = false;
        if (existingTopics == null) {
          t = new KwKafkaConnector();

          topicId = topicId + 1;
          t.setConnectorId(topicId);
          t.setConnectorName(topicUpdate.getConnectorName());
          t.setConnectorConfig(connectorConfig);
          t.setEnvironment(topicUpdate.getEnvSelected());
          t.setTeamId(
              manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
          t.setDescription(KAFKA_CONNECT_SYNC_102);
          t.setExistingConnector(false);
          t.setTenantId(tenantId);

          kafkaConnectorList.add(t);
        } else {
          for (KwKafkaConnector existingTopic : existingTopics) {
            if (Objects.equals(existingTopic.getEnvironment(), topicUpdate.getEnvSelected())) {
              t = existingTopic;
              t.setTeamId(
                  manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
              t.setConnectorName(topicUpdate.getConnectorName());
              t.setConnectorConfig(connectorConfig);
              t.setEnvironment(existingTopic.getEnvironment());
              t.setExistingConnector(true);
              t.setTenantId(tenantId);
              kafkaConnectorList.add(t);
              topicAdded = true;
            } else if (!Objects.equals(
                existingTopic.getTeamId(),
                manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()))) {
              t = existingTopic;
              t.setTeamId(
                  manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
              t.setConnectorName(topicUpdate.getConnectorName());
              t.setConnectorConfig(connectorConfig);
              t.setEnvironment(existingTopic.getEnvironment());
              t.setExistingConnector(true);
              t.setTenantId(tenantId);
              kafkaConnectorList.add(t);
              topicAdded = true;
            }
          }
        }

        boolean envFound = false;
        if (existingTopics != null) {
          for (KwKafkaConnector existingTopic : existingTopics) {
            if (Objects.equals(existingTopic.getEnvironment(), topicUpdate.getEnvSelected())) {
              envFound = true;
              break;
            }
          }
          if (!envFound && !topicAdded) {
            t = new KwKafkaConnector();
            topicId = topicId + 1;
            t.setConnectorId(topicId);
            t.setConnectorName(topicUpdate.getConnectorName());
            t.setConnectorConfig(connectorConfig);
            t.setEnvironment(topicUpdate.getEnvSelected());
            t.setTeamId(
                manageDatabase.getTeamIdFromTeamName(tenantId, topicUpdate.getTeamSelected()));
            t.setDescription(KAFKA_CONNECT_SYNC_102);
            t.setExistingConnector(false);
            t.setTenantId(tenantId);

            kafkaConnectorList.add(t);
          }
        }
      }
    }

    if (updatedSyncTopics.size() == 0 && updatedSyncTopicsDelete.size() > 0) {
      return ApiResponse.SUCCESS;
    }

    if (topicsDontExistInMainCluster) {
      return ApiResponse.notOk(
          KAFKA_CONNECT_SYNC_ERR_102 + " :" + syncCluster + ". \n Topics : " + erroredTopicsExist);
    }

    if (topicsWithDiffTeams) {
      return ApiResponse.notOk(
          KAFKA_CONNECT_SYNC_ERR_103 + " :" + syncCluster + ". \n Topics : " + erroredTopics);
    }

    if (kafkaConnectorList.size() > 0) {
      try {
        String result =
            manageDatabase.getHandleDbRequests().addToSyncConnectors(kafkaConnectorList);
        return ApiResultStatus.SUCCESS.value.equals(result)
            ? ApiResponse.ok(result)
            : ApiResponse.notOk(result);
      } catch (Exception e) {
        throw new KlawException(e.getMessage());
      }
    } else {
      return ApiResponse.notOk(SYNC_ERR_101);
    }
  }

  private String getConnectorConfiguration(String connectorName, String environmentId, int tenantId)
      throws KlawException, JsonProcessingException {
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA_CONNECT, tenantId)
            .get(getKafkaConnectorEnvDetails(environmentId).getClusterId());

    Object configMap =
        clusterApiService
            .getConnectorDetails(
                connectorName,
                kwClusters.getBootstrapServers(),
                kwClusters.getProtocol(),
                kwClusters.getClusterName() + kwClusters.getClusterId(),
                tenantId)
            .get("config");
    return WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(configMap);
  }

  private List<SyncConnectorUpdates> handleConnectorDeletes(
      List<SyncConnectorUpdates> updatedSyncTopics, List<Integer> updatedSyncConnectorsDelete) {
    List<SyncConnectorUpdates> updatedSyncTopicsUpdated = new ArrayList<>();
    for (SyncConnectorUpdates updatedSyncConn : updatedSyncTopics) {
      if (SYNC_102.equals(updatedSyncConn.getTeamSelected())) {
        updatedSyncConnectorsDelete.add(Integer.parseInt(updatedSyncConn.getConnectorId()));
      } else {
        updatedSyncTopicsUpdated.add(updatedSyncConn);
      }
    }

    // delete topic
    for (Integer connectorId : updatedSyncConnectorsDelete) {
      manageDatabase
          .getHandleDbRequests()
          .deleteConnector(connectorId, commonUtilsService.getTenantId(getUserName()));
    }

    return updatedSyncTopicsUpdated;
  }

  private boolean checkInPromotionOrder(String envId, String orderOfEnvs) {
    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
    return orderedEnv.contains(envId);
  }

  public List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId) {
    return manageDatabase.getHandleDbRequests().getConnectorsFromName(connectorName, tenantId);
  }

  public List<KafkaConnectorModelResponse> getSyncConnectors(
      String envId,
      String pageNo,
      String currentPage,
      String connectorNameSearch,
      boolean getConnectorsStatuses) {
    Env envSelected = getKafkaConnectorEnvDetails(envId);
    List<String> teamList = new ArrayList<>();
    teamList = tenantFilterTeams(teamList);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    // get from metastore
    List<KafkaConnectorModelResponse> kafkaConnectorModelSourceList =
        getSyncConnectorsList(envId, teamList, tenantId);
    if (connectorNameSearch != null && connectorNameSearch.length() > 0) {
      final String topicSearchFilter = connectorNameSearch;
      kafkaConnectorModelSourceList =
          kafkaConnectorModelSourceList.stream()
              .filter(topic -> topic.getConnectorName().contains(topicSearchFilter))
              .collect(Collectors.toList());
    }
    List<String> allSyncConnectors = new ArrayList<>();
    for (KafkaConnectorModelResponse kafkaConnectorModel : kafkaConnectorModelSourceList) {
      allSyncConnectors.add(kafkaConnectorModel.getConnectorName());
    }

    // get from cluster
    List<KafkaConnectorModelResponse> kafkaConnectorModelClusterList = new ArrayList<>();
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA_CONNECT, tenantId)
            .get(envSelected.getClusterId());
    String bootstrapHost = kwClusters.getBootstrapServers();
    try {
      ConnectorsStatus allConnectors =
          clusterApiService.getAllKafkaConnectors(
              bootstrapHost,
              kwClusters.getProtocol().getName(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              tenantId,
              getConnectorsStatuses);
      List<ConnectorState> connectorsList = allConnectors.getConnectorStateList();
      if (connectorNameSearch != null && connectorNameSearch.length() > 0) {
        final String topicSearchFilter = connectorNameSearch;
        connectorsList =
            connectorsList.stream()
                .filter(
                    connectorState -> connectorState.getConnectorName().contains(topicSearchFilter))
                .toList();
      }

      for (ConnectorState connectorState : connectorsList) {
        KafkaConnectorModelResponse kafkaConnectorModel = new KafkaConnectorModelResponse();
        kafkaConnectorModel.setConnectorName(connectorState.getConnectorName());
        kafkaConnectorModel.setRunningTasks(connectorState.getRunningTasks());
        kafkaConnectorModel.setConnectorStatus(connectorState.getConnectorStatus());
        kafkaConnectorModel.setFailedTasks(connectorState.getFailedTasks());
        kafkaConnectorModel.setEnvironmentId(envId);
        kafkaConnectorModel.setEnvironmentName(getKafkaConnectorEnvDetails(envId).getName());
        kafkaConnectorModel.setPossibleTeams(teamList);

        kafkaConnectorModelClusterList.add(kafkaConnectorModel);
      }

      // remove if any which already exist in metastore
      kafkaConnectorModelClusterList.removeIf(
          p -> !allSyncConnectors.isEmpty() && allSyncConnectors.contains(p.getConnectorName()));

      for (KafkaConnectorModelResponse kafkaConnectorModelResponse :
          kafkaConnectorModelSourceList) {
        kafkaConnectorModelResponse.setRemarks("IN_SYNC"); // default
        Optional<ConnectorState> optionalConnectorState =
            connectorsList.stream()
                .filter(
                    connectorState ->
                        connectorState
                            .getConnectorName()
                            .equals(kafkaConnectorModelResponse.getConnectorName()))
                .findFirst();
        if (optionalConnectorState.isPresent()) {
          ConnectorState connectorState = optionalConnectorState.get();
          kafkaConnectorModelResponse.setConnectorStatus(connectorState.getConnectorStatus());
          kafkaConnectorModelResponse.setFailedTasks(connectorState.getFailedTasks());
          kafkaConnectorModelResponse.setRunningTasks(connectorState.getRunningTasks());
        } else {
          kafkaConnectorModelResponse.setConnectorStatus("NOT_KNOWN");
        }
      }

      kafkaConnectorModelClusterList.addAll(kafkaConnectorModelSourceList);

      for (KafkaConnectorModelResponse kafkaConnectorModel : kafkaConnectorModelSourceList) {
        if (connectorsList.stream()
            .noneMatch(
                connectorState ->
                    connectorState
                        .getConnectorName()
                        .equals(kafkaConnectorModel.getConnectorName()))) {
          for (KafkaConnectorModelResponse kafkaConnectorModelCluster :
              kafkaConnectorModelClusterList) {
            if (Objects.equals(
                kafkaConnectorModelCluster.getConnectorName(),
                kafkaConnectorModel.getConnectorName())) {
              kafkaConnectorModelCluster.setRemarks("DELETED");
              // Remove the other team options added and replace with existing team and option to
              // remove it.
              List<String> possibleTeams = new ArrayList<>();
              possibleTeams.add(kafkaConnectorModelCluster.getTeamName());
              possibleTeams.add(SYNC_102);
              kafkaConnectorModelCluster.setPossibleTeams(possibleTeams);
            }
          }
        }
      }

      // set sequence
      int i = 0;
      for (KafkaConnectorModelResponse kafkaConnectorModel : kafkaConnectorModelClusterList) {
        kafkaConnectorModel.setSequence(i);
        i++;

        if (kafkaConnectorModel.getTeamName() == null
            || kafkaConnectorModel.getTeamName().equals("")) {
          kafkaConnectorModel.setRemarks("ADDED");
          kafkaConnectorModel.setTeamName("");
        }
      }

      // don't show connectors which are deleted on cluster
      if (getConnectorsStatuses) {
        kafkaConnectorModelClusterList =
            kafkaConnectorModelClusterList.stream()
                .filter(connectorList -> connectorList.getRemarks().equals("IN_SYNC"))
                .toList();
      }

      // pagination
      kafkaConnectorModelClusterList =
          getConnectorsPaged(kafkaConnectorModelClusterList, pageNo, currentPage);
      return kafkaConnectorModelClusterList;

    } catch (KlawException e) {
      log.error("Exception:", e);
    }

    return new ArrayList<>();
  }

  private List<KafkaConnectorModelResponse> getSyncConnectorsList(
      String envId, List<String> teamList, int tenantId) {
    //         Get Sync connectors
    List<KwKafkaConnector> connectorsFromSOT =
        manageDatabase.getHandleDbRequests().getSyncConnectors(envId, null, tenantId);

    List<KafkaConnectorModelResponse> kafkaConnectorModelSourceList = new ArrayList<>();

    for (KwKafkaConnector kwKafkaConnector : connectorsFromSOT) {
      KafkaConnectorModelResponse kafkaConnectorModel = new KafkaConnectorModelResponse();
      kafkaConnectorModel.setEnvironmentName(
          getKafkaConnectorEnvDetails(kwKafkaConnector.getEnvironment()).getName());
      kafkaConnectorModel.setEnvironmentId(kwKafkaConnector.getEnvironment());
      kafkaConnectorModel.setConnectorName(kwKafkaConnector.getConnectorName());
      kafkaConnectorModel.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, kwKafkaConnector.getTeamId()));
      kafkaConnectorModel.setConnectorId(kwKafkaConnector.getConnectorId());
      kafkaConnectorModel.setPossibleTeams(teamList);

      kafkaConnectorModelSourceList.add(kafkaConnectorModel);
    }

    return kafkaConnectorModelSourceList;
  }

  private ArrayList<KafkaConnectorModelResponse> getConnectorsPaged(
      List<KafkaConnectorModelResponse> origActivityList, String pageNo, String currentPage) {

    ArrayList<KafkaConnectorModelResponse> newList = new ArrayList<>();

    if (origActivityList != null && origActivityList.size() > 0) {
      int totalRecs = origActivityList.size();
      int recsPerPage = 20;
      int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);

      pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
      int requestPageNo = Integer.parseInt(pageNo);
      int startVar = (requestPageNo - 1) * recsPerPage;
      int lastVar = (requestPageNo) * (recsPerPage);

      List<String> numList = new ArrayList<>();
      commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

      for (int i = 0; i < totalRecs; i++) {
        KafkaConnectorModelResponse activityLog = origActivityList.get(i);
        if (i >= startVar && i < lastVar) {
          activityLog.setAllPageNos(numList);
          activityLog.setTotalNoPages("" + totalPages);
          activityLog.setCurrentPage(pageNo);
          newList.add(activityLog);
        }
      }
    }

    return newList;
  }

  private List<String> tenantFilterTeams(List<String> teamList) {
    if (!commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_CONNECTORS)) {
      // tenant filtering
      int tenantId = commonUtilsService.getTenantId(getUserName());
      List<Team> teams = manageDatabase.getHandleDbRequests().getAllTeams(tenantId);

      List<String> teamListUpdated = new ArrayList<>();
      for (Team teamsItem : teams) {
        teamListUpdated.add(teamsItem.getTeamname());
      }
      teamList = teamListUpdated;
    }
    return teamList;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public Env getKafkaConnectorEnvDetails(String envId) {
    Optional<Env> envFound =
        manageDatabase
            .getKafkaConnectEnvList(commonUtilsService.getTenantId(getUserName()))
            .stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  public List<KafkaConnectorModelResponse> getConnectorsToManage(
      String envId, String pageNo, String currentPage, String connectorNameSearch) {
    return null;
  }
}
