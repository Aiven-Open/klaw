package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.TOPIC_OVW_ERR_101;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.helpers.KlawResourceUtils;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.ResourceHistory;
import io.aiven.klaw.model.TopicConfigurationRequest;
import io.aiven.klaw.model.TopicOverviewInfo;
import io.aiven.klaw.model.enums.AclGroupBy;
import io.aiven.klaw.model.enums.PromotionStatusType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.response.AclOverviewInfo;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.PromotionStatus;
import io.aiven.klaw.model.response.TopicOverview;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TopicOverviewService extends BaseOverviewService {

  public TopicOverviewService(MailUtils mailService) {
    super(mailService);
  }

  public TopicOverview getTopicOverview(
      String topicName, String environmentId, AclGroupBy groupBy) {
    log.debug("getTopicOverview {}", topicName);

    if (topicName != null) {
      topicName = topicName.trim();
    } else {
      return null;
    }

    String userName = getUserName();
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(userName);

    Integer loggedInUserTeam = commonUtilsService.getTeamId(userName);
    List<Topic> topics = commonUtilsService.getTopicsForTopicName(topicName, tenantId);

    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(userName);
    topics =
        topics.stream()
            .filter(topicObj -> allowedEnvIdSet.contains(topicObj.getEnvironment()))
            .collect(Collectors.toList());

    List<Topic> originalSetTopics = new ArrayList<>(topics);
    TopicOverview topicOverview = new TopicOverview();

    if (topics.size() == 0) {
      topicOverview.setTopicExists(false);
      return topicOverview;
    } else {
      topicOverview.setTopicExists(true);
    }

    Pair<String, List<Topic>> topicsPair =
        filterByEnvIdParameter(environmentId, tenantId, topics, topicOverview);
    environmentId = topicsPair.getKey();
    topics = topicsPair.getValue();

    KwTenantConfigModel kwTenantConfigModel = manageDatabase.getTenantConfig().get(tenantId);
    String syncCluster =
        kwTenantConfigModel == null ? null : kwTenantConfigModel.getBaseSyncEnvironment();

    List<TopicOverviewInfo> topicInfoList = new ArrayList<>();
    List<ResourceHistory> topicHistoryList = new ArrayList<>();
    enrichTopicOverview(
        tenantId, topics, topicOverview, syncCluster, topicInfoList, topicHistoryList, topicName);
    List<AclOverviewInfo> aclInfo = new ArrayList<>();
    List<AclOverviewInfo> prefixedAclsInfo = new ArrayList<>();
    List<Topic> topicsSearchList = commonUtilsService.getTopicsForTopicName(topicName, tenantId);
    // tenant filtering
    Integer topicOwnerTeamId =
        commonUtilsService.getFilteredTopicsForTenant(topicsSearchList).get(0).getTeamId();

    enrichTopicInfoList(
        topicName,
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
        topicName,
        tenantId,
        loggedInUserTeam,
        originalSetTopics,
        topicOverview,
        topicInfoList,
        aclInfo,
        topicOwnerTeamId,
        environmentId);

    return topicOverview;
  }

  private Pair<String, List<Topic>> filterByEnvIdParameter(
      String environmentId, int tenantId, List<Topic> topics, TopicOverview topicOverview) {
    List<EnvIdInfo> availableEnvs = new ArrayList<>();
    List<EnvIdInfo> availableEnvsNotInPromotionOrder = new ArrayList<>();
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
    List<String> orderOfEnvsArrayList = KlawResourceUtils.getOrderedEnvsList(orderOfEnvs);
    topics.forEach(
        topic -> {
          EnvIdInfo envIdInfo = new EnvIdInfo();
          envIdInfo.setId(topic.getEnvironment());
          envIdInfo.setName(
              manageDatabase.getKafkaEnvList(tenantId).stream()
                  .filter(env -> env.getId().equals(topic.getEnvironment()))
                  .map(Env::getName)
                  .findFirst()
                  .orElse("ENV_NOT_FOUND"));
          if (orderOfEnvsArrayList.contains(envIdInfo.getId())) {
            availableEnvs.add(envIdInfo);
          } else {
            availableEnvsNotInPromotionOrder.add(envIdInfo);
          }
        });

    availableEnvs.sort(
        Comparator.comparingInt(
            topicEnv -> Objects.requireNonNull(orderOfEnvs).indexOf(topicEnv.getId())));
    availableEnvs.addAll(availableEnvsNotInPromotionOrder);
    topicOverview.setAvailableEnvironments(availableEnvs);

    if (Objects.equals(environmentId, "")) {
      environmentId = availableEnvs.get(0).getId();
    }
    String finalEnvironmentId = environmentId;
    topics =
        topics.stream().filter(topic -> topic.getEnvironment().equals(finalEnvironmentId)).toList();
    return Pair.of(environmentId, topics);
  }

  private void enrichTopicInfoList(
      String topicNameSearch,
      HandleDbRequests handleDb,
      int tenantId,
      Integer loggedInUserTeam,
      List<TopicOverviewInfo> topicInfoList,
      List<AclOverviewInfo> aclInfo,
      List<AclOverviewInfo> prefixedAclsInfo,
      Integer topicOwnerTeamId) {
    List<Acl> prefixedAcls = new ArrayList<>();
    List<Acl> aclsFromSOT = new ArrayList<>();
    List<Acl> allPrefixedAcls;
    List<AclOverviewInfo> tmpAclPrefixed;
    List<AclOverviewInfo> tmpAcl;

    for (TopicOverviewInfo topicInfo : topicInfoList) {
      aclsFromSOT.addAll(getAclsFromSOT(topicInfo.getEnvId(), topicNameSearch, false, tenantId));

      tmpAcl =
          applyFiltersAclsForSOT(loggedInUserTeam, aclsFromSOT, tenantId).stream()
              .collect(Collectors.groupingBy(AclOverviewInfo::getTopicname))
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
      setHasAcl(aclInfo, topicInfo);
      // show edit button only forenv owned by your team
      if (Objects.equals(topicOwnerTeamId, loggedInUserTeam)) {
        topicInfo.setShowEditTopic(true);
        topicInfo.setTopicOwner(true);
      }
    }
  }

  private void setHasOpenRequestBooleans(
      TopicOverviewInfo topicInfo, String topicName, String envId, int tenantId) {
    topicInfo.setHasOpenACLRequest(isACLRequestOpen(topicName, envId, tenantId));
    topicInfo.setHasOpenTopicRequest(isTopicRequestOpen(topicName, envId, tenantId));
    topicInfo.setHasOpenSchemaRequest(isSchemaRequestOpen(topicName, envId, tenantId));
    topicInfo.setHasOpenClaimRequest(isClaimTopicRequestOpen(topicName, tenantId));
    topicInfo.setHasOpenRequest(
        topicInfo.isHasOpenACLRequest()
            || topicInfo.isHasOpenSchemaRequest()
            || topicInfo.isHasOpenTopicRequest()
            || topicInfo.isHasOpenClaimRequest());
  }

  private void setHasOpenRequestOnly(
      TopicOverviewInfo topicInfo, String topicName, String envId, int tenantId) {
    topicInfo.setHasOpenRequest(
        isACLRequestOpen(topicName, envId, tenantId)
            || isSchemaRequestOpen(topicName, envId, tenantId)
            || isTopicRequestOpen(topicName, envId, tenantId)
            || isClaimTopicRequestOpen(topicName, tenantId));
  }

  private void setHasSchema(
      TopicOverviewInfo topicInfo, String topicName, String envId, int tenantId) {
    topicInfo.setHasSchema(commonUtilsService.existsSchemaForTopic(topicName, envId, tenantId));
  }

  private void setHasAcl(List<AclOverviewInfo> aclInfo, TopicOverviewInfo topicInfo) {
    topicInfo.setHasACL(
        aclInfo.stream()
            .anyMatch(aclItem -> Objects.equals(aclItem.getEnvironment(), topicInfo.getEnvId())));
  }

  private void enrichTopicOverview(
      int tenantId,
      List<Topic> topics,
      TopicOverview topicOverview,
      String syncCluster,
      List<TopicOverviewInfo> topicInfoList,
      List<ResourceHistory> topicHistoryList,
      String topicName) {
    ArrayList<ResourceHistory> topicHistoryFromTopic;
    for (Topic topic : topics) {
      TopicOverviewInfo topicInfo = new TopicOverviewInfo();
      topicInfo.setTopicName(topicName);
      Env topicEnv = getEnvDetails(topic.getEnvironment(), tenantId);
      topicInfo.setEnvName(topicEnv.getName());
      topicInfo.setClusterId(topicEnv.getClusterId());
      topicInfo.setEnvId(topic.getEnvironment());
      topicInfo.setNoOfPartitions(topic.getNoOfPartitions());
      topicInfo.setNoOfReplicas(topic.getNoOfReplicas());
      topicInfo.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, topic.getTeamId()));
      topicInfo.setTeamId(topic.getTeamId());
      String topicJsonParams = topic.getJsonParams();
      if (topicJsonParams != null) {
        TopicConfigurationRequest topicConfigurationRequest;
        try {
          topicConfigurationRequest =
              OBJECT_MAPPER.readValue(topicJsonParams, TopicConfigurationRequest.class);
          topicInfo.setAdvancedTopicConfiguration(
              topicConfigurationRequest.getAdvancedTopicConfiguration());
        } catch (JsonProcessingException e) {
          log.error("Unable to parse topic advanced config {}", topicName);
        }
      }

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
      List<Topic> originalSetTopics,
      TopicOverview topicOverview,
      List<TopicOverviewInfo> topicInfoList,
      List<AclOverviewInfo> aclInfo,
      Integer topicOwnerTeam,
      String environmentId) {
    try {
      if (Objects.equals(topicOwnerTeam, loggedInUserTeam)) {
        topicOverview.setTopicPromotionDetails(
            getTopicPromotionEnv(topicNameSearch, originalSetTopics, tenantId, environmentId));

        if (topicInfoList.size() > 0) {
          TopicOverviewInfo lastItem = topicInfoList.get(topicInfoList.size() - 1);
          lastItem.setTopicDeletable(
              aclInfo.stream()
                  .noneMatch(
                      aclItem -> Objects.equals(aclItem.getEnvironment(), lastItem.getEnvId())));

          // Available environments is ordered from lowest to highest environment. See line
          // filterByEnvIdParameter
          lastItem.setHighestEnv(
              Objects.equals(
                  topicOverview
                      .getAvailableEnvironments()
                      .get(topicOverview.getAvailableEnvironments().size() - 1)
                      .getId(),
                  lastItem.getEnvId()));
          setHasOpenRequestBooleans(lastItem, topicNameSearch, environmentId, tenantId);
          setHasSchema(lastItem, topicNameSearch, environmentId, tenantId);
          lastItem.setShowDeleteTopic(
              lastItem.isTopicDeletable()
                  && lastItem.isHighestEnv()
                  && !lastItem.isHasOpenRequest());

          topicOverview.setSchemaExists(
              commonUtilsService.existsSchemaForTopic(topicNameSearch, environmentId, tenantId));
        }
        Env env = commonUtilsService.getEnvDetails(environmentId, tenantId);
        if (env != null && env.getAssociatedEnv() != null) {
          topicOverview.setCreateSchemaAllowed(
              commonUtilsService.isCreateNewSchemaAllowed(
                  env.getAssociatedEnv().getId(), tenantId));
        }
      } else {
        PromotionStatus promotionStatus = new PromotionStatus();
        promotionStatus.setStatus(PromotionStatusType.NOT_AUTHORIZED);
        topicOverview.setTopicPromotionDetails(promotionStatus);
        if (topicInfoList.size() > 0) {
          topicInfoList
              .get(0)
              .setHasOpenClaimRequest(isClaimTopicRequestOpen(topicNameSearch, tenantId));
          if (topicInfoList.get(0).isHasOpenClaimRequest()) {
            topicInfoList.get(0).setHasOpenRequest(true);
          } else {
            // only make call to db if it is required.
            setHasOpenRequestOnly(topicInfoList.get(0), topicNameSearch, environmentId, tenantId);
          }
        }
      }
    } catch (Exception e) {
      PromotionStatus promotionStatus = new PromotionStatus();
      promotionStatus.setStatus(PromotionStatusType.NOT_AUTHORIZED);
      topicOverview.setTopicPromotionDetails(promotionStatus);
    }
  }

  private boolean isTopicRequestOpen(String topicName, String environmentId, int tenantId) {
    return manageDatabase
        .getHandleDbRequests()
        .existsTopicRequest(topicName, RequestStatus.CREATED.value, environmentId, tenantId);
  }

  private boolean isClaimTopicRequestOpen(String topicName, int tenantId) {
    return manageDatabase
        .getHandleDbRequests()
        .existsClaimTopicRequest(topicName, RequestStatus.CREATED.value, tenantId);
  }

  private boolean isTopicPromoteRequestOpen(String topicName, String environmentId, int tenantId) {
    return manageDatabase
        .getHandleDbRequests()
        .existsTopicRequest(
            topicName,
            RequestStatus.CREATED.value,
            RequestOperationType.PROMOTE.value,
            environmentId,
            tenantId);
  }

  private boolean isACLRequestOpen(String topicName, String environmentId, int tenantId) {

    return manageDatabase
        .getHandleDbRequests()
        .existsAclRequest(topicName, RequestStatus.CREATED.value, environmentId, tenantId);
  }

  private boolean isSchemaRequestOpen(String topicName, String envId, int tenantId) {
    return manageDatabase
        .getAssociatedSchemaEnvIdFromTopicId(envId, tenantId)
        .filter(
            s ->
                manageDatabase
                    .getHandleDbRequests()
                    .existsSchemaRequest(topicName, RequestStatus.CREATED.value, s, tenantId))
        .isPresent();
  }

  private PromotionStatus getTopicPromotionEnv(
      String topicSearch, List<Topic> topics, int tenantId, String environmentId) {
    PromotionStatus promotionStatus = new PromotionStatus();
    try {
      if (topics == null) {
        topics = manageDatabase.getHandleDbRequests().getTopics(topicSearch, tenantId);
      }
      promotionStatus.setTopicName(topicSearch);
      String orderEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
      List<String> envOrderList = KlawResourceUtils.getOrderedEnvsList(orderEnvs);

      if (topics != null && topics.size() > 0) {
        List<String> envList =
            topics.stream().map(Topic::getEnvironment).collect(Collectors.toList());

        generatePromotionDetails(tenantId, promotionStatus, envList, orderEnvs);
        // Ex : If topic exists in D, T, then promotion to A is displayed when topic overview is for
        // T env
        if (promotionStatus.getTargetEnvId() != null) {
          String targetEnvId = promotionStatus.getTargetEnvId();
          if (!((envOrderList.indexOf(targetEnvId) - envOrderList.indexOf(environmentId)) == 1)
              || !envOrderList.contains(environmentId)) {
            promotionStatus.setStatus(PromotionStatusType.NO_PROMOTION);
          } else if (isTopicPromoteRequestOpen(
              topicSearch, promotionStatus.getTargetEnvId(), tenantId)) {
            promotionStatus.setStatus(PromotionStatusType.REQUEST_OPEN);
          }
        }

        return promotionStatus;
      }
    } catch (Exception e) {
      log.error("getTopicPromotionEnv error ", e);
      promotionStatus.setStatus(PromotionStatusType.FAILURE);
      promotionStatus.setError(TOPIC_OVW_ERR_101);
    }

    return promotionStatus;
  }
}
