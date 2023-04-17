package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.TOPIC_OVW_ERR_101;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.enums.AclGroupBy;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.response.TopicOverview;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TopicOverviewService extends BaseOverviewService {

  public TopicOverviewService(MailUtils mailService) {
    super(mailService);
  }

  public TopicOverview getTopicOverview(String topicNameSearch, AclGroupBy groupBy) {
    log.debug("getAcls {}", topicNameSearch);

    if (topicNameSearch != null) {
      topicNameSearch = topicNameSearch.trim();
    } else {
      return null;
    }

    String userName = getUserName();
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(userName);

    Integer loggedInUserTeam = commonUtilsService.getTeamId(userName);
    List<Topic> topics = commonUtilsService.getTopicsForTopicName(topicNameSearch, tenantId);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    topics =
        topics.stream()
            .filter(topicObj -> allowedEnvIdSet.contains(topicObj.getEnvironment()))
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
      String requestTopicsEnvs =
          commonUtilsService.getEnvProperty(tenantId, "REQUEST_TOPICS_OF_ENVS");
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
        commonUtilsService.getTopicsForTopicName(topicNameSearch, tenantId);
    // tenant filtering
    Integer topicOwnerTeamId =
        commonUtilsService.getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();

    enrichTopicInfoList(
        topicNameSearch,
        handleDb,
        tenantId,
        loggedInUserTeam,
        topicInfoList,
        aclInfo,
        prefixedAclsInfo,
        topicOwnerTeamId);

    aclInfo =
        getAclInfoList(tenantId, topicOverview, topicInfoList, aclInfo, prefixedAclsInfo, groupBy);

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

  private void enrichTopicInfoList(
      String topicNameSearch,
      HandleDbRequests handleDb,
      int tenantId,
      Integer loggedInUserTeam,
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
      aclsFromSOT.addAll(getAclsFromSOT(topicInfo.getEnvId(), topicNameSearch, false, tenantId));

      tmpAcl =
          applyFiltersAclsForSOT(loggedInUserTeam, aclsFromSOT, tenantId).stream()
              .collect(Collectors.groupingBy(AclInfo::getTopicname))
              .get(topicNameSearch);

      if (tmpAcl != null) {
        aclInfo.addAll(tmpAcl);
      }

      allPrefixedAcls = handleDb.getPrefixedAclsSOT(topicInfo.getEnvId(), tenantId);
      if (allPrefixedAcls != null && allPrefixedAcls.size() > 0) {
        for (Acl allPrefixedAcl : allPrefixedAcls) {
          if (topicNameSearch.startsWith(allPrefixedAcl.getTopicname())) {
            prefixedAcls.add(allPrefixedAcl);
          }
        }
        tmpAclPrefixed = applyFiltersAclsForSOT(loggedInUserTeam, prefixedAcls, tenantId);
        prefixedAclsInfo.addAll(tmpAclPrefixed);
      }

      // show edit button only forenv owned by your team
      if (Objects.equals(topicOwnerTeamId, loggedInUserTeam)) {
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
      topicInfo.setEnvName(getEnvDetails(topic.getEnvironment(), tenantId).getName());
      topicInfo.setEnvId(topic.getEnvironment());
      topicInfo.setNoOfPartitions(topic.getNoOfPartitions());
      topicInfo.setNoOfReplicas(topic.getNoOfReplicas());
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
        topicOverview.setTopicPromotionDetails(
            getTopicPromotionEnv(topicNameSearch, topics, tenantId));

        if (topicInfoList.size() > 0) {
          TopicInfo lastItem = topicInfoList.get(topicInfoList.size() - 1);
          lastItem.setTopicDeletable(
              aclInfo.stream()
                  .noneMatch(
                      aclItem -> Objects.equals(aclItem.getEnvironment(), lastItem.getEnvName())));
          lastItem.setShowDeleteTopic(true);
        }
      } else {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("status", ApiResultStatus.NOT_AUTHORIZED.value);
        topicOverview.setTopicPromotionDetails(hashMap);
      }
    } catch (Exception e) {
      Map<String, String> hashMap = new HashMap<>();
      hashMap.put("status", ApiResultStatus.NOT_AUTHORIZED.value);
      topicOverview.setTopicPromotionDetails(hashMap);
    }
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
        generatePromotionDetails(
            tenantId,
            hashMap,
            envList,
            commonUtilsService.getEnvProperty(tenantId, "ORDER_OF_ENVS"));
        return hashMap;
      }
    } catch (Exception e) {
      log.error("getTopicPromotionEnv error ", e);
      hashMap.put("status", ApiResultStatus.FAILURE.value);
      hashMap.put("error", TOPIC_OVW_ERR_101);
    }

    return hashMap;
  }
}
