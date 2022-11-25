package io.aiven.klaw.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicOverview;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TopicOverviewService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
  public static final TypeReference<ArrayList<TopicHistory>> VALUE_TYPE_REF =
      new TypeReference<>() {};
  @Autowired ManageDatabase manageDatabase;
  @Autowired ClusterApiService clusterApiService;
  @Autowired private CommonUtilsService commonUtilsService;
  private final MailUtils mailService;

  public TopicOverviewService(MailUtils mailService) {
    this.mailService = mailService;
  }

  public TopicOverview getTopicOverview(String topicNameSearch) {
    log.debug("getAcls {}", topicNameSearch);

    if (topicNameSearch != null) {
      topicNameSearch = topicNameSearch.trim();
    } else {
      return null;
    }

    String userName = getUserName();
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(getUserName());

    Integer loggedInUserTeam = getMyTeamId(userName);
    List<Topic> topics = handleDb.getTopics(topicNameSearch, tenantId);

    // tenant filtering
    List<String> allowedEnvIdList = commonUtilsService.getEnvsFromUserId(userName);
    topics =
        topics.stream()
            .filter(topicObj -> allowedEnvIdList.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    TopicOverview topicOverview = new TopicOverview();

    if (topics.size() == 0) {
      topicOverview.setTopicExists(false);
      return topicOverview;
    } else {
      topicOverview.setTopicExists(true);
    }

    String syncCluster;
    String[] reqTopicsEnvs;
    Set<String> reqTopicsEnvsList = new HashSet<>();
    try {
      syncCluster = manageDatabase.getTenantConfig().get(tenantId).getBaseSyncEnvironment();
    } catch (Exception exception) {
      log.error("Exception while getting syncCluster. Ignored. ", exception);
      syncCluster = null;
    }

    try {
      String requestTopicsEnvs = mailService.getEnvProperty(tenantId, "REQUEST_TOPICS_OF_ENVS");
      reqTopicsEnvs = requestTopicsEnvs.split(",");
      reqTopicsEnvsList = new HashSet<>(Arrays.asList(reqTopicsEnvs));
    } catch (Exception exception) {
      log.error("Error in getting req topic envs", exception);
    }

    List<TopicInfo> topicInfoList = new ArrayList<>();
    List<TopicHistory> topicHistoryList = new ArrayList<>();
    enrichTopicOverview(
        tenantId, topics, topicOverview, syncCluster, topicInfoList, topicHistoryList);
    List<AclInfo> aclInfo = new ArrayList<>();
    List<AclInfo> prefixedAclsInfo = new ArrayList<>();
    List<Topic> topicsSearchList =
        manageDatabase.getHandleDbRequests().getTopicTeam(topicNameSearch, tenantId);
    // tenant filtering
    Integer topicOwnerTeamId =
        commonUtilsService.getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();

    enrichTopicInfoList(
        topicNameSearch,
        handleDb,
        tenantId,
        loggedInUserTeam,
        reqTopicsEnvsList,
        topicInfoList,
        aclInfo,
        prefixedAclsInfo,
        topicOwnerTeamId);

    aclInfo = getAclInfoList(tenantId, topicOverview, topicInfoList, aclInfo, prefixedAclsInfo);

    updateTopicOverviewItems(
        topicNameSearch,
        tenantId,
        loggedInUserTeam,
        topics,
        topicOverview,
        topicInfoList,
        aclInfo,
        topicOwnerTeamId);

    return topicOverview;
  }

  public TopicOverview getSchemaOfTopic(String topicNameSearch, String schemaVersionSearch) {
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    TopicOverview topicOverview = new TopicOverview();
    topicOverview.setTopicExists(true);
    boolean retrieveSchemas = true;
    updateAvroSchema(
        topicNameSearch, schemaVersionSearch, handleDb, retrieveSchemas, topicOverview, tenantId);
    return topicOverview;
  }

  private void updateAvroSchema(
      String topicNameSearch,
      String schemaVersionSearch,
      HandleDbRequests handleDb,
      boolean retrieveSchemas,
      TopicOverview topicOverview,
      int tenantId) {
    if (topicOverview.isTopicExists() && retrieveSchemas) {
      List<Map<String, String>> schemaDetails = new ArrayList<>();
      Map<String, String> schemaMap = new HashMap<>();
      List<Env> schemaEnvs = handleDb.selectAllSchemaRegEnvs(tenantId);
      Object dynamicObj;
      Map<String, Object> hashMapSchemaObj;
      String schemaOfObj;
      for (Env schemaEnv : schemaEnvs) {
        try {
          KwClusters kwClusters =
              manageDatabase
                  .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
                  .get(schemaEnv.getClusterId());
          SortedMap<Integer, Map<String, Object>> schemaObjects =
              clusterApiService.getAvroSchema(
                  kwClusters.getBootstrapServers(),
                  kwClusters.getProtocol(),
                  kwClusters.getClusterName(),
                  topicNameSearch,
                  tenantId);

          Integer latestSchemaVersion = schemaObjects.firstKey();
          Set<Integer> allVersions = schemaObjects.keySet();
          List<Integer> allVersionsList = new ArrayList<>(allVersions);

          try {
            if (schemaVersionSearch != null
                && latestSchemaVersion == Integer.parseInt(schemaVersionSearch)) {
              schemaVersionSearch = "";
            }
          } catch (NumberFormatException ignored) {
          }

          // get latest version
          if (schemaVersionSearch != null && schemaVersionSearch.equals("")) {
            hashMapSchemaObj = schemaObjects.get(latestSchemaVersion);
            schemaOfObj = (String) hashMapSchemaObj.get("schema");
            schemaMap.put("isLatest", "true");
            schemaMap.put("id", hashMapSchemaObj.get("id") + "");
            schemaMap.put("compatibility", hashMapSchemaObj.get("compatibility") + "");
            schemaMap.put("version", "" + latestSchemaVersion);

            if (schemaObjects.size() > 1) {
              schemaMap.put("showNext", "true");
              schemaMap.put("showPrev", "false");
              int indexOfVersion = allVersionsList.indexOf(latestSchemaVersion);
              schemaMap.put("nextVersion", "" + allVersionsList.get(indexOfVersion + 1));
            }
          } else {
            hashMapSchemaObj =
                schemaObjects.get(Integer.parseInt(Objects.requireNonNull(schemaVersionSearch)));
            schemaOfObj = (String) hashMapSchemaObj.get("schema");
            schemaMap.put("isLatest", "false");
            schemaMap.put("id", hashMapSchemaObj.get("id") + "");
            schemaMap.put("compatibility", hashMapSchemaObj.get("compatibility") + "");
            schemaMap.put("version", "" + schemaVersionSearch);

            if (schemaObjects.size() > 1) {
              int indexOfVersion = allVersionsList.indexOf(Integer.parseInt(schemaVersionSearch));
              if (indexOfVersion + 1 == allVersionsList.size()) {
                schemaMap.put("showNext", "false");
                schemaMap.put("showPrev", "true");
                schemaMap.put("prevVersion", "" + allVersionsList.get(indexOfVersion - 1));
              } else {
                schemaMap.put("showNext", "true");
                schemaMap.put("showPrev", "true");
                schemaMap.put("prevVersion", "" + allVersionsList.get(indexOfVersion - 1));
                schemaMap.put("nextVersion", "" + allVersionsList.get(indexOfVersion + 1));
              }
            }
          }

          schemaMap.put("env", schemaEnv.getName());
          dynamicObj = OBJECT_MAPPER.readValue(schemaOfObj, Object.class);
          schemaOfObj = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(dynamicObj);
          schemaMap.put("content", schemaOfObj);

          schemaDetails.add(schemaMap);
          topicOverview.setSchemaExists(true);
          log.info("Getting schema " + topicNameSearch);
        } catch (Exception e) {
          log.error("Error ", e);
        }
      }
      if (topicOverview.isSchemaExists()) topicOverview.setSchemaDetails(schemaDetails);
    }
  }

  private void updateTopicOverviewItems(
      String topicNameSearch,
      int tenantId,
      Integer loggedInUserTeam,
      List<Topic> topics,
      TopicOverview topicOverview,
      List<TopicInfo> topicInfoList,
      List<AclInfo> aclInfo,
      Integer topicOwnerTeam) {
    try {
      if (Objects.equals(topicOwnerTeam, loggedInUserTeam)) {
        topicOverview.setPromotionDetails(getTopicPromotionEnv(topicNameSearch, topics, tenantId));

        if (topicInfoList.size() > 0) {
          TopicInfo lastItem = topicInfoList.get(topicInfoList.size() - 1);
          lastItem.setTopicDeletable(
              aclInfo.stream()
                  .noneMatch(
                      aclItem -> Objects.equals(aclItem.getEnvironment(), lastItem.getCluster())));
          lastItem.setShowDeleteTopic(true);
        }
      } else {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("status", "not_authorized");
        topicOverview.setPromotionDetails(hashMap);
      }
    } catch (Exception e) {
      Map<String, String> hashMap = new HashMap<>();
      hashMap.put("status", "not_authorized");
      topicOverview.setPromotionDetails(hashMap);
    }
  }

  private List<AclInfo> getAclInfoList(
      int tenantId,
      TopicOverview topicOverview,
      List<TopicInfo> topicInfoList,
      List<AclInfo> aclInfo,
      List<AclInfo> prefixedAclsInfo) {
    aclInfo = aclInfo.stream().distinct().collect(Collectors.toList());
    List<AclInfo> transactionalAcls =
        aclInfo.stream()
            .filter(aclRec -> aclRec.getTransactionalId() != null)
            .collect(Collectors.toList());

    for (AclInfo aclInfo1 : aclInfo) {
      aclInfo1.setEnvironmentName(getEnvDetails(aclInfo1.getEnvironment(), tenantId).getName());
    }

    for (AclInfo aclInfo2 : prefixedAclsInfo) {
      aclInfo2.setEnvironmentName(getEnvDetails(aclInfo2.getEnvironment(), tenantId).getName());
    }

    topicOverview.setAclInfoList(aclInfo);
    if (prefixedAclsInfo.size() > 0) {
      topicOverview.setPrefixedAclInfoList(prefixedAclsInfo);
      topicOverview.setPrefixAclsExists(true);
    }
    if (transactionalAcls.size() > 0) {
      topicOverview.setTransactionalAclInfoList(transactionalAcls);
      topicOverview.setTxnAclsExists(true);
    }

    topicOverview.setTopicInfoList(topicInfoList);
    return aclInfo;
  }

  private void enrichTopicInfoList(
      String topicNameSearch,
      HandleDbRequests handleDb,
      int tenantId,
      Integer loggedInUserTeam,
      Set<String> reqTopicsEnvsList,
      List<TopicInfo> topicInfoList,
      List<AclInfo> aclInfo,
      List<AclInfo> prefixedAclsInfo,
      Integer topicOwnerTeamId) {
    List<Acl> prefixedAcls = new ArrayList<>();
    List<Acl> aclsFromSOT = new ArrayList<>();
    List<Acl> allPrefixedAcls;
    List<AclInfo> tmpAclPrefixed;
    List<AclInfo> tmpAcl;
    for (TopicInfo topicInfo : topicInfoList) {
      aclsFromSOT.addAll(
          getAclsFromSOT(topicInfo.getClusterId(), topicNameSearch, false, tenantId));

      tmpAcl =
          applyFiltersAclsForSOT(loggedInUserTeam, aclsFromSOT, tenantId).stream()
              .collect(Collectors.groupingBy(AclInfo::getTopicname))
              .get(topicNameSearch);

      if (tmpAcl != null) {
        aclInfo.addAll(tmpAcl);
      }

      allPrefixedAcls = handleDb.getPrefixedAclsSOT(topicInfo.getClusterId(), tenantId);
      if (allPrefixedAcls != null && allPrefixedAcls.size() > 0) {
        for (Acl allPrefixedAcl : allPrefixedAcls) {
          if (topicNameSearch.startsWith(allPrefixedAcl.getTopicname())) {
            prefixedAcls.add(allPrefixedAcl);
          }
        }
        tmpAclPrefixed = applyFiltersAclsForSOT(loggedInUserTeam, prefixedAcls, tenantId);
        prefixedAclsInfo.addAll(tmpAclPrefixed);
      }

      // show edit button only for restricted envs
      if (Objects.equals(topicOwnerTeamId, loggedInUserTeam)
          && reqTopicsEnvsList.contains(topicInfo.getClusterId())) {
        topicInfo.setShowEditTopic(true);
      }
    }
  }

  private void enrichTopicOverview(
      int tenantId,
      List<Topic> topics,
      TopicOverview topicOverview,
      String syncCluster,
      List<TopicInfo> topicInfoList,
      List<TopicHistory> topicHistoryList) {
    ArrayList<TopicHistory> topicHistoryFromTopic;
    for (Topic topic : topics) {
      TopicInfo topicInfo = new TopicInfo();
      topicInfo.setCluster(getEnvDetails(topic.getEnvironment(), tenantId).getName());
      topicInfo.setClusterId(topic.getEnvironment());
      topicInfo.setNoOfPartitions(topic.getNoOfPartitions());
      topicInfo.setNoOfReplcias(topic.getNoOfReplcias());
      topicInfo.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topic.getTeamId()));

      if (syncCluster != null && syncCluster.equals(topic.getEnvironment())) {
        topicOverview.setTopicDocumentation(topic.getDocumentation());
        topicOverview.setTopicIdForDocumentation(topic.getTopicid());
      }

      if (topic.getHistory() != null) {
        try {
          topicHistoryFromTopic = OBJECT_MAPPER.readValue(topic.getHistory(), VALUE_TYPE_REF);
          topicHistoryList.addAll(topicHistoryFromTopic);
        } catch (JsonProcessingException e) {
          log.error("Unable to parse topicHistory ", e);
        }
      }
      topicInfoList.add(topicInfo);
    }

    if (topicOverview.getTopicIdForDocumentation() == null) {
      topicOverview.setTopicDocumentation(topics.get(0).getDocumentation());
      topicOverview.setTopicIdForDocumentation(topics.get(0).getTopicid());
    }

    topicOverview.setTopicHistoryList(topicHistoryList);
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
      mp.setTopictype(aclSotItem.getTopictype());
      mp.setAclPatternType(aclSotItem.getAclPatternType());
      mp.setReq_no(aclSotItem.getReq_no() + "");
      if (aclSotItem.getTeamId() != null && aclSotItem.getTeamId().equals(loggedInUserTeam))
        mp.setShowDeleteAcl(true);

      if (aclSotItem.getAclip() != null || aclSotItem.getAclssl() != null) aclList.add(mp);
    }
    return aclList;
  }

  private List<Acl> getAclsFromSOT(
      String env, String topicNameSearch, boolean regex, int tenantId) {
    List<Acl> aclsFromSOT;
    if (!regex) {
      aclsFromSOT =
          manageDatabase.getHandleDbRequests().getSyncAcls(env, topicNameSearch, tenantId);
    } else {
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

  private Map<String, String> getTopicPromotionEnv(
      String topicSearch, List<Topic> topics, int tenantId) {
    Map<String, String> hashMap = new HashMap<>();
    try {
      if (topics == null) {
        topics = manageDatabase.getHandleDbRequests().getTopics(topicSearch, tenantId);
      }

      hashMap.put("topicName", topicSearch);

      if (topics != null && topics.size() > 0) {
        List<String> envList =
            topics.stream().map(Topic::getEnvironment).collect(Collectors.toList());

        // tenant filtering
        String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");
        envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

        String lastEnv = envList.get(envList.size() - 1);
        List<String> orderdEnvs = Arrays.asList(orderOfEnvs.split(","));

        if (orderdEnvs.indexOf(lastEnv) == orderdEnvs.size() - 1) {
          hashMap.put("status", "NO_PROMOTION"); // PRD
        } else {
          hashMap.put("status", ApiResultStatus.SUCCESS.value);
          hashMap.put("sourceEnv", lastEnv);
          String targetEnv = orderdEnvs.get(orderdEnvs.indexOf(lastEnv) + 1);
          hashMap.put("targetEnv", getEnvDetails(targetEnv, tenantId).getName());
          hashMap.put("targetEnvId", targetEnv);
        }

        return hashMap;
      }
    } catch (Exception e) {
      log.error("getTopicPromotionEnv error ", e);
      hashMap.put("status", ApiResultStatus.FAILURE.value);
      hashMap.put("error", "Topic does not exist in any environment.");
    }

    return hashMap;
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  private Integer getMyTeamId(String userName) {
    return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
  }

  public Env getEnvDetails(String envId, int tenantId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(tenantId).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }
}
