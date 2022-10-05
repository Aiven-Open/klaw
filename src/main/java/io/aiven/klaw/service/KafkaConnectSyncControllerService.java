package io.aiven.klaw.service;

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
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.KafkaClustersType;
import io.aiven.klaw.model.KafkaConnectorModel;
import io.aiven.klaw.model.PermissionType;
import io.aiven.klaw.model.SyncConnectorUpdates;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

  public Map<String, String> getConnectorDetails(String connectorName, String envId)
      throws KlawException {
    Map<String, String> response = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA_CONNECT.value, tenantId)
            .get(getKafkaConnectorEnvDetails(envId).getClusterId());

    Map<String, Object> res =
        clusterApiService.getConnectorDetails(
            connectorName, kwClusters.getBootstrapServers(), kwClusters.getProtocol(), tenantId);

    try {
      String schemaOfObj = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(res);
      response.put("result", schemaOfObj);
      return response;
    } catch (JsonProcessingException e) {
      log.error("Exception:", e);
    }

    response.put("result", res.toString());
    return response;
  }

  public ApiResponse updateSyncConnectors(List<SyncConnectorUpdates> updatedSyncTopics)
      throws KlawException {
    log.info("updateSyncConnectors {}", updatedSyncTopics);
    String userDetails = getUserName();

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_CONNECTORS)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    // tenant filtering
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String syncCluster =
        manageDatabase.getTenantConfig().get(tenantId).getBaseSyncKafkaConnectCluster();
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_KAFKA_CONNECT_ENVS");

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
        if (!getEnvsFromUserId(userDetails).contains(topicUpdate.getEnvSelected())) {
          return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
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
          if (checkInPromotionOrder(
              topicUpdate.getConnectorName(), topicUpdate.getEnvSelected(), orderOfEnvs))
            topicsDontExistInMainCluster = true;
        }

        String connectorConfig;
        try {
          connectorConfig =
              getConnectorConfiguration(
                  topicUpdate.getConnectorName(), topicUpdate.getEnvSelected(), tenantId);
        } catch (KlawException | JsonProcessingException e) {
          log.error("Exception:", e);
          return ApiResponse.builder()
              .result(topicUpdate.getConnectorName() + " Connector config could not be retrieved.")
              .build();
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
          t.setDescription("Connector description");
          t.setExistingConnector(false);

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
            t.setDescription("Connector description");
            t.setExistingConnector(false);

            kafkaConnectorList.add(t);
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
              "Failure. Please sync up the team of the following connector(s) first in"
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
              "Failure. The following connectors are being synchronized with"
                  + " a different team, when compared to main Sync cluster (klaw.syncdata.cluster)"
                  + " :"
                  + syncCluster
                  + ". \n Topics : "
                  + erroredTopics)
          .build();
    }

    if (kafkaConnectorList.size() > 0) {
      try {
        String result =
            manageDatabase.getHandleDbRequests().addToSyncConnectors(kafkaConnectorList);
        return ApiResponse.builder().result(result).build();
      } catch (Exception e) {
        throw new KlawException(e.getMessage());
      }
    } else {
      return ApiResponse.builder().result("No record updated.").build();
    }
  }

  private String getConnectorConfiguration(String connectorName, String environmentId, int tenantId)
      throws KlawException, JsonProcessingException {
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA_CONNECT.value, tenantId)
            .get(getKafkaConnectorEnvDetails(environmentId).getClusterId());

    Object configMap =
        clusterApiService
            .getConnectorDetails(
                connectorName, kwClusters.getBootstrapServers(), kwClusters.getProtocol(), tenantId)
            .get("config");
    return WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(configMap);
  }

  private List<SyncConnectorUpdates> handleConnectorDeletes(
      List<SyncConnectorUpdates> updatedSyncTopics, List<Integer> updatedSyncTopicsDelete) {
    List<SyncConnectorUpdates> updatedSyncTopicsUpdated = new ArrayList<>();
    for (SyncConnectorUpdates updatedSyncTopic : updatedSyncTopics) {
      if ("REMOVE FROM KLAW".equals(updatedSyncTopic.getTeamSelected())) {
        updatedSyncTopicsDelete.add(Integer.parseInt(updatedSyncTopic.getSequence()));
      } else updatedSyncTopicsUpdated.add(updatedSyncTopic);
    }

    // delete topic
    for (Integer topicId : updatedSyncTopicsDelete) {
      manageDatabase
          .getHandleDbRequests()
          .deleteConnector(topicId, commonUtilsService.getTenantId(getUserName()));
    }

    return updatedSyncTopicsUpdated;
  }

  private boolean checkInPromotionOrder(String topicname, String envId, String orderOfEnvs) {
    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
    return orderedEnv.contains(envId);
  }

  public List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId) {
    List<KwKafkaConnector> connectors =
        manageDatabase.getHandleDbRequests().getConnectorsFromName(connectorName, tenantId);
    return connectors;
  }

  public List<KafkaConnectorModel> getSyncConnectors(
      String envId,
      String pageNo,
      String currentPage,
      String connectorNameSearch,
      boolean parseBoolean) {
    Env envSelected = getKafkaConnectorEnvDetails(envId);
    List<String> teamList = new ArrayList<>();
    teamList = tenantFilterTeams(teamList);
    int tenantId = commonUtilsService.getTenantId(getUserName());

    // get from metastore
    List<KafkaConnectorModel> kafkaConnectorModelSourceList =
        getSyncConnectorsList(envId, teamList, tenantId);
    if (connectorNameSearch != null && connectorNameSearch.length() > 0) {
      final String topicSearchFilter = connectorNameSearch;
      kafkaConnectorModelSourceList =
          kafkaConnectorModelSourceList.stream()
              .filter(topic -> topic.getConnectorName().contains(topicSearchFilter))
              .collect(Collectors.toList());
    }
    List<String> allSyncConnectors = new ArrayList<>();
    for (KafkaConnectorModel kafkaConnectorModel : kafkaConnectorModelSourceList) {
      allSyncConnectors.add(kafkaConnectorModel.getConnectorName());
    }

    // get from cluster
    List<KafkaConnectorModel> kafkaConnectorModelClusterList = new ArrayList<>();
    String bootstrapHost =
        manageDatabase
            .getClusters(KafkaClustersType.KAFKA_CONNECT.value, tenantId)
            .get(envSelected.getClusterId())
            .getBootstrapServers();
    try {
      List<String> allConnectors = clusterApiService.getAllKafkaConnectors(bootstrapHost, tenantId);

      if (connectorNameSearch != null && connectorNameSearch.length() > 0) {
        final String topicSearchFilter = connectorNameSearch;
        allConnectors =
            allConnectors.stream()
                .filter(topic -> topic.contains(topicSearchFilter))
                .collect(Collectors.toList());
      }

      for (String allConnector : allConnectors) {
        KafkaConnectorModel kafkaConnectorModel = new KafkaConnectorModel();
        kafkaConnectorModel.setConnectorName(allConnector);
        kafkaConnectorModel.setEnvironmentId(envId);
        kafkaConnectorModel.setEnvironmentName(getKafkaConnectorEnvDetails(envId).getName());
        kafkaConnectorModel.setPossibleTeams(teamList);

        kafkaConnectorModelClusterList.add(kafkaConnectorModel);
      }

      // remove if any which already exist in metastore
      kafkaConnectorModelClusterList.removeIf(
          p -> !allSyncConnectors.isEmpty() && allSyncConnectors.contains(p.getConnectorName()));

      kafkaConnectorModelClusterList.addAll(kafkaConnectorModelSourceList);

      for (KafkaConnectorModel kafkaConnectorModel : kafkaConnectorModelSourceList) {
        if (!allConnectors.contains(kafkaConnectorModel.getConnectorName())) {
          for (KafkaConnectorModel kafkaConnectorModelCluster : kafkaConnectorModelClusterList) {
            if (Objects.equals(
                kafkaConnectorModelCluster.getConnectorName(),
                kafkaConnectorModel.getConnectorName())) {
              kafkaConnectorModelCluster.setRemarks("DELETED");
              //                            kafkaConnectorModelCluster.setPossibleTeams(teamList);
              //                            possibleTeams.add("REMOVE FROM KLAW");
            }
          }
        }
      }

      // set sequence
      int i = 0;
      for (KafkaConnectorModel kafkaConnectorModel : kafkaConnectorModelClusterList) {
        kafkaConnectorModel.setSequence(i);
        i++;

        if (kafkaConnectorModel.getTeamName() == null
            || kafkaConnectorModel.getTeamName().equals("")) {
          kafkaConnectorModel.setRemarks("ADDED");
          kafkaConnectorModel.setTeamName("");
        }
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

  private List<KafkaConnectorModel> getSyncConnectorsList(
      String envId, List<String> teamList, int tenantId) {
    //         Get Sync connectors
    List<KwKafkaConnector> connectorsFromSOT =
        manageDatabase.getHandleDbRequests().getSyncConnectors(envId, null, tenantId);

    List<KafkaConnectorModel> kafkaConnectorModelSourceList = new ArrayList<>();

    for (KwKafkaConnector kwKafkaConnector : connectorsFromSOT) {
      KafkaConnectorModel kafkaConnectorModel = new KafkaConnectorModel();
      kafkaConnectorModel.setEnvironmentName(
          getKafkaConnectorEnvDetails(kwKafkaConnector.getEnvironment()).getName());
      kafkaConnectorModel.setEnvironmentId(kwKafkaConnector.getEnvironment());
      kafkaConnectorModel.setConnectorName(kwKafkaConnector.getConnectorName());
      kafkaConnectorModel.setTeamName(
          manageDatabase.getTeamNameFromTeamId(tenantId, kwKafkaConnector.getTeamId()));
      kafkaConnectorModel.setPossibleTeams(teamList);

      kafkaConnectorModelSourceList.add(kafkaConnectorModel);
    }

    return kafkaConnectorModelSourceList;
  }

  private ArrayList<KafkaConnectorModel> getConnectorsPaged(
      List<KafkaConnectorModel> origActivityList, String pageNo, String currentPage) {

    ArrayList<KafkaConnectorModel> newList = new ArrayList<>();

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
        KafkaConnectorModel activityLog = origActivityList.get(i);
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
      List<Team> teams = manageDatabase.getHandleDbRequests().selectAllTeams(tenantId);

      List<String> teamListUpdated = new ArrayList<>();
      for (Team teamsItem : teams) {
        teamListUpdated.add(teamsItem.getTeamname());
      }
      teamList = teamListUpdated;
    }
    return teamList;
  }

  private List<KwKafkaConnector> getFilteredConnectorsForTenant(
      List<KwKafkaConnector> connectorsFromSOT) {
    // tenant filtering
    try {
      List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
      if (connectorsFromSOT != null)
        connectorsFromSOT =
            connectorsFromSOT.stream()
                .filter(connector -> allowedEnvIdList.contains(connector.getEnvironment()))
                .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      return new ArrayList<>();
    }
    return connectorsFromSOT;
  }

  private List<String> getEnvsFromUserId(String userDetails) {
    Integer userTeamId = getMyTeamId(userDetails);
    return manageDatabase.getTeamsAndAllowedEnvs(
        userTeamId, commonUtilsService.getTenantId(userDetails));
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
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

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }
}
