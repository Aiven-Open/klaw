package io.aiven.klaw.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.cluster.SchemaInfoOfTopic;
import io.aiven.klaw.model.cluster.SchemasInfoOfClusterResponse;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.SchemaSubjectInfoResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchemaRegistrySyncControllerService {
  @Autowired ManageDatabase manageDatabase;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  public SchemaRegistrySyncControllerService(
      ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  public SyncSchemasList getSchemasOfEnvironment(
      String kafkaEnvId, String pageNo, String currentPage) throws Exception {
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_SCHEMAS)) {
      return null;
    }
    SyncSchemasList syncSchemasList = new SyncSchemasList();
    List<SchemaSubjectInfoResponse> schemaSubjectInfoResponseList = new ArrayList<>();

    Env kafkaEnv = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
    Env schemaEnvSelected =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(kafkaEnv.getAssociatedEnv().getId(), tenantId);
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
            .get(schemaEnvSelected.getClusterId());
    try {
      SchemasInfoOfClusterResponse schemasInfoOfClusterResponse =
          clusterApiService.getSchemasFromCluster(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              tenantId);

      List<Topic> topicsFromSOT =
          manageDatabase.getHandleDbRequests().getSyncTopics(kafkaEnvId, null, tenantId);

      List<SchemaInfoOfTopic> schemaInfoOfTopicList =
          schemasInfoOfClusterResponse.getSchemaInfoOfTopicList();
      for (SchemaInfoOfTopic schemaInfoOfTopic : schemaInfoOfTopicList) {
        SchemaSubjectInfoResponse schemaSubjectInfoResponse = new SchemaSubjectInfoResponse();
        schemaSubjectInfoResponse.setSchemaVersions(schemaInfoOfTopic.getSchemaVersions());
        schemaSubjectInfoResponse.setTopic(schemaInfoOfTopic.getTopic());
        schemaSubjectInfoResponseList.add(schemaSubjectInfoResponse);
      }

      schemaSubjectInfoResponseList =
          filterTopicsNotInDb(schemaSubjectInfoResponseList, topicsFromSOT);
      syncSchemasList.setSchemaSubjectInfoResponseList(
          getPagedResponse(pageNo, currentPage, schemaSubjectInfoResponseList, tenantId));
      return syncSchemasList;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private List<SchemaSubjectInfoResponse> filterTopicsNotInDb(
      List<SchemaSubjectInfoResponse> schemaSubjectInfoResponseList, List<Topic> topicsFromSOT) {
    List<SchemaSubjectInfoResponse> updatedList = new ArrayList<>();
    for (SchemaSubjectInfoResponse schemaSubjectInfoResponse : schemaSubjectInfoResponseList) {
      Optional<Topic> optionalTopic =
          topicsFromSOT.stream()
              .filter(t -> t.getTopicname().equals(schemaSubjectInfoResponse.getTopic()))
              .findAny();
      if (optionalTopic.isPresent()) {
        schemaSubjectInfoResponse.setTeamId(optionalTopic.get().getTeamId());
        updatedList.add(schemaSubjectInfoResponse);
      }
    }
    return updatedList;
  }

  private List<SchemaSubjectInfoResponse> getPagedResponse(
      String pageNo,
      String currentPage,
      List<SchemaSubjectInfoResponse> schemaInfoOfTopicList,
      int tenantId) {
    List<SchemaSubjectInfoResponse> pagedTopicSyncList = new ArrayList<>();

    int totalRecs = schemaInfoOfTopicList.size();
    int recsPerPage = 20;

    int totalPages =
        schemaInfoOfTopicList.size() / recsPerPage
            + (schemaInfoOfTopicList.size() % recsPerPage > 0 ? 1 : 0);

    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);
    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < totalRecs; i++) {

      if (i >= startVar && i < lastVar) {
        SchemaSubjectInfoResponse mp = schemaInfoOfTopicList.get(i);

        mp.setTotalNoPages(totalPages + "");
        mp.setAllPageNos(numList);
        mp.setCurrentPage(pageNo);
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, mp.getTeamId()));
        pagedTopicSyncList.add(mp);
      }
    }
    return pagedTopicSyncList;
  }

  private String prettyPrintUglyJsonString(String json) {
    ObjectMapper mapper = new ObjectMapper();

    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(json));
    } catch (JsonProcessingException e) {
      log.error("Unable to pretty print json : ", e);
    }
    return json;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
