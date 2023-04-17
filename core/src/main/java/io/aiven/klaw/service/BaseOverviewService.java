package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.BASE_OVERVIEW_101;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.TopicHistory;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.enums.AclGroupBy;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.response.TopicOverview;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class BaseOverviewService {

  private static final Comparator<String> nullSafeStringComparator =
      Comparator.nullsLast(String::compareToIgnoreCase);

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
  public static final TypeReference<ArrayList<TopicHistory>> VALUE_TYPE_REF =
      new TypeReference<>() {};
  private static final String MASKED_FOR_SECURITY = BASE_OVERVIEW_101;
  @Autowired protected ManageDatabase manageDatabase;
  @Autowired protected ClusterApiService clusterApiService;
  @Autowired protected CommonUtilsService commonUtilsService;
  protected final MailUtils mailService;

  public BaseOverviewService(MailUtils mailService) {
    this.mailService = mailService;
  }

  protected List<AclInfo> getAclInfoList(
      int tenantId,
      TopicOverview topicOverview,
      List<TopicInfo> topicInfoList,
      List<AclInfo> aclInfo,
      List<AclInfo> prefixedAclsInfo,
      AclGroupBy groupBy) {
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

    topicOverview.setAclInfoList(sortAclInfo(aclInfo, groupBy));
    if (prefixedAclsInfo.size() > 0) {
      topicOverview.setPrefixedAclInfoList(prefixedAclsInfo);
      topicOverview.setPrefixAclsExists(true);
    }
    if (transactionalAcls.size() > 0) {
      topicOverview.setTransactionalAclInfoList(transactionalAcls);
      topicOverview.setTxnAclsExists(true);
    }

    topicOverview.setTopicInfoList(topicInfoList);
    return topicOverview.getAclInfoList();
  }

  private List<AclInfo> sortAclInfo(List<AclInfo> aclInfo, AclGroupBy groupBy) {
    // merge reduce
    if (groupBy.equals(AclGroupBy.TEAM)) {
      aclInfo = mergeReduceAclInfo(aclInfo);
      aclInfo.sort(
          Comparator.comparing(AclInfo::getTeamname, nullSafeStringComparator)
              .thenComparing(AclInfo::getEnvironmentName, nullSafeStringComparator)
              .thenComparing(AclInfo::getConsumergroup, nullSafeStringComparator));
    }

    return aclInfo;
  }

  /**
   * mergeReduceAclInfo reduces the Acls to a smaller subset by merging Ips and proncipals by Team
   * -> Env -> Consumer Group
   *
   * @param aclInfo List of AclInfo supplied to be reduced and merged
   * @return The processed and reduced AclInfo List
   */
  private List<AclInfo> mergeReduceAclInfo(List<AclInfo> aclInfo) {
    // recursively get Env Per Team
    return mergeReduceByTeam(aclInfo);
  }

  private List<AclInfo> mergeReduceByTeam(List<AclInfo> aclInfo) {
    List<AclInfo> organisedList = new ArrayList<>();
    Map<String, List<AclInfo>> teams = new HashMap<>();

    aclInfo.forEach(acl -> reduceAclByProperty(teams, acl.getTeamname(), acl));

    // recursively merge and reduce By Environment.
    teams.values().forEach(acls -> organisedList.addAll(mergeReduceByEnv(acls)));
    return organisedList;
  }

  private List<AclInfo> mergeReduceByEnv(List<AclInfo> aclInfo) {
    List<AclInfo> organisedList = new ArrayList<>();
    Map<String, List<AclInfo>> processedAcls = new HashMap<>();

    aclInfo.forEach(acl -> reduceAclByProperty(processedAcls, acl.getEnvironmentName(), acl));
    // recursively  merge and reduce By ConsumerGroup
    processedAcls.values().forEach(acls -> organisedList.addAll(mergeReduceByConsumerGroups(acls)));
    return organisedList;
  }

  private List<AclInfo> mergeReduceByConsumerGroups(List<AclInfo> aclInfo) {
    List<AclInfo> organisedList = new ArrayList<>();
    Map<String, List<AclInfo>> processedAcls = new HashMap<>();

    aclInfo.forEach(acl -> reduceAclByProperty(processedAcls, acl.getConsumergroup(), acl));
    // Merge Reduce All ACls with the same Team/env/Consumer group together.
    processedAcls.values().forEach(acls -> organisedList.addAll(mergeReduceAcls(acls)));
    return organisedList;
  }

  private void reduceAclByProperty(
      Map<String, List<AclInfo>> processedAcls, String aclProperty, AclInfo acl) {
    if (processedAcls.containsKey(aclProperty)) {
      processedAcls.get(aclProperty).add(acl);
    } else {
      processedAcls.put(aclProperty, new ArrayList<>());
      processedAcls.get(aclProperty).add(acl);
    }
  }

  private List<AclInfo> mergeReduceAcls(List<AclInfo> aclInfo) {
    AclInfo producerIp = null, producerPrincipal = null;
    AclInfo consumerIp = null, consumerPrincipal = null;

    for (AclInfo info : aclInfo) {
      if (info.getTopictype().equals(AclType.PRODUCER.value)) {
        if (info.getAcl_ip() != null && !info.getAcl_ip().isEmpty()) {
          producerIp = initializeAclInfo(producerIp, info);
          producerIp.getAcl_ips().add(info.getAcl_ip());
        } else if (info.getAcl_ssl() != null && !info.getAcl_ssl().isEmpty()) {
          producerPrincipal = initializeAclInfo(producerPrincipal, info);
          producerPrincipal.getAcl_ssls().add(info.getAcl_ssl());
        }
      } else if (info.getTopictype().equals(AclType.CONSUMER.value)) {
        if (info.getAcl_ip() != null && !info.getAcl_ip().isEmpty()) {
          consumerIp = initializeAclInfo(consumerIp, info);
          consumerIp.getAcl_ips().add(info.getAcl_ip());
        } else if (info.getAcl_ssl() != null && !info.getAcl_ssl().isEmpty()) {
          consumerPrincipal = initializeAclInfo(consumerPrincipal, info);
          consumerPrincipal.getAcl_ssls().add(info.getAcl_ssl());
        }
      }
    }

    return addToListIfNotNull(producerIp, producerPrincipal, consumerIp, consumerPrincipal);
  }

  private List<AclInfo> addToListIfNotNull(
      AclInfo producerIp,
      AclInfo producerPrincipal,
      AclInfo consumerIp,
      AclInfo consumerPrincipal) {
    List<AclInfo> buildList = new ArrayList<>();
    if (producerIp != null) {
      buildList.add(producerIp);
    }
    if (consumerIp != null) {
      buildList.add(consumerIp);
    }
    if (producerPrincipal != null) {
      buildList.add(producerPrincipal);
    }
    if (consumerPrincipal != null) {
      buildList.add(consumerPrincipal);
    }
    return buildList;
  }

  private AclInfo initializeAclInfo(AclInfo newAcl, AclInfo info) {
    if (newAcl == null) {
      newAcl = new AclInfo();
      copyProperties(info, newAcl);
      newAcl.setAcl_ips(new HashSet<>());
      newAcl.setAcl_ssls(new HashSet<>());
      // remove old information from the copy properties that is no longer needed to avoid
      // confusion.
      newAcl.setAcl_ip(null);
      newAcl.setAcl_ssl(null);
    }
    return newAcl;
  }

  protected List<AclInfo> applyFiltersAclsForSOT(
      Integer loggedInUserTeam, List<Acl> aclsFromSOT, int tenantId) {

    List<AclInfo> aclList = new ArrayList<>();
    AclInfo mp;

    for (Acl aclSotItem : aclsFromSOT) {
      if (aclSotItem.getAclip() != null || aclSotItem.getAclssl() != null) {
        mp = new AclInfo();
        mp.setEnvironment(aclSotItem.getEnvironment());
        Env envDetails = getEnvDetails(aclSotItem.getEnvironment(), tenantId);
        mp.setEnvironmentName(envDetails.getName());
        mp.setTopicname(aclSotItem.getTopicname());

        mp.setTransactionalId(aclSotItem.getTransactionalId());
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, aclSotItem.getTeamId()));
        mp.setConsumergroup(aclSotItem.getConsumergroup());
        mp.setTopictype(aclSotItem.getAclType());
        mp.setAclPatternType(aclSotItem.getAclPatternType());
        mp.setReq_no(aclSotItem.getReq_no() + "");
        mp.setKafkaFlavorType(
            KafkaFlavors.of(
                manageDatabase
                    .getClusters(KafkaClustersType.KAFKA, tenantId)
                    .get(envDetails.getClusterId())
                    .getKafkaFlavor()));
        if (aclSotItem.getTeamId() != null && aclSotItem.getTeamId().equals(loggedInUserTeam)) {
          mp.setShowDeleteAcl(true);
          // Only add ACL info if it is the owning team that is requesting it
          mp.setAcl_ip(aclSotItem.getAclip());
          mp.setAcl_ssl(aclSotItem.getAclssl());
        } else {
          mp.setAcl_ip(MASKED_FOR_SECURITY);
          mp.setAcl_ssl(MASKED_FOR_SECURITY);
        }

        aclList.add(mp);
      }
    }
    return aclList;
  }

  protected List<Acl> getAclsFromSOT(
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

  protected void generatePromotionDetails(
      int tenantId, Map<String, String> hashMap, List<String> envList, String orderOfEnvs) {
    if (envList != null && envList.size() > 0) {
      // tenant filtering
      envList.sort(Comparator.comparingInt(orderOfEnvs::indexOf));

      String lastEnv = envList.get(envList.size() - 1);
      List<String> orderedEnvs = Arrays.asList(orderOfEnvs.split(","));

      if (orderedEnvs.indexOf(lastEnv) == orderedEnvs.size() - 1) {
        hashMap.put("status", "NO_PROMOTION"); // PRD
      } else {
        hashMap.put("status", ApiResultStatus.SUCCESS.value);
        hashMap.put("sourceEnv", lastEnv);
        String targetEnv = orderedEnvs.get(orderedEnvs.indexOf(lastEnv) + 1);
        hashMap.put("targetEnv", getEnvDetails(targetEnv, tenantId).getName());
        hashMap.put("targetEnvId", targetEnv);
      }
    }
  }

  protected String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  protected Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  protected Env getEnvDetails(String envId, int tenantId) {
    Optional<Env> envFound =
        manageDatabase.getAllEnvList(tenantId).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }
}
