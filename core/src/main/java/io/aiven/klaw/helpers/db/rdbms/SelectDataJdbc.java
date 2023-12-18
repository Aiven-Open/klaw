package io.aiven.klaw.helpers.db.rdbms;

import static io.aiven.klaw.helpers.KwConstants.*;
import static io.aiven.klaw.helpers.KwConstants.DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER;
import static io.aiven.klaw.helpers.KwConstants.REQUESTOR_SUBSCRIPTIONS;

import com.google.common.collect.Lists;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.response.DashboardStats;
import io.aiven.klaw.repository.*;
import io.aiven.klaw.service.CommonUtilsService;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SelectDataJdbc {

  @Autowired(required = false)
  private UserInfoRepo userInfoRepo;

  @Autowired(required = false)
  private KwPropertiesRepo kwPropertiesRepo;

  @Autowired(required = false)
  private RegisterInfoRepo registerInfoRepo;

  @Autowired(required = false)
  private TeamRepo teamRepo;

  @Autowired(required = false)
  private EnvRepo envRepo;

  @Autowired(required = false)
  private ActivityLogRepo activityLogRepo;

  @Autowired(required = false)
  private AclRequestsRepo aclRequestsRepo;

  @Autowired(required = false)
  private OperationalRequestsRepo operationalRequestsRepo;

  @Autowired(required = false)
  private TopicRepo topicRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRepo kafkaConnectorRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

  @Autowired(required = false)
  private AclRepo aclRepo;

  @Autowired(required = false)
  private TopicRequestsRepo topicRequestsRepo;

  @Autowired(required = false)
  private SchemaRequestRepo schemaRequestRepo;

  @Autowired(required = false)
  private MessageSchemaRepo messageSchemaRepo;

  @Autowired(required = false)
  private TenantRepo tenantRepo;

  @Autowired(required = false)
  private KwRolesPermsRepo kwRolesPermsRepo;

  @Autowired(required = false)
  private KwClusterRepo kwClusterRepo;

  @Autowired(required = false)
  private KwMetricsRepo kwMetricsRepo;

  @Autowired(required = false)
  private ProductDetailsRepo productDetailsRepo;

  @Autowired(required = false)
  private KwEntitySequenceRepo kwEntitySequenceRepo;

  public boolean existsAclRequest(
      String topicName, String requestStatus, String env, int tenantId) {
    return aclRequestsRepo.existsByTenantIdAndEnvironmentAndRequestStatusAndTopicname(
        tenantId, env, requestStatus, topicName);
  }

  public List<AclRequests> selectFilteredAclRequests(
      boolean isApproval,
      String requestor,
      String role,
      String status,
      RequestOperationType requestOperationType,
      boolean showRequestsOfAllTeams,
      String topic,
      String environment,
      String wildcardSearch,
      AclType aclType,
      boolean isMyRequest,
      int tenantId) {
    log.debug("selectAclRequests {}", requestor);
    List<AclRequests> aclList = new ArrayList<>();
    List<AclRequests> aclListSub;

    aclListSub =
        Lists.newArrayList(
            findAclRequestsByExample(
                topic,
                environment,
                aclType,
                status,
                isMyRequest ? requestor : null,
                tenantId,
                requestOperationType));
    if (isApproval) {
      // Only filter when returning to approvers view.
      // in the acl request the username is mapped to the requestor column in the database.
      aclListSub =
          aclListSub.stream()
              .filter(req -> !req.getRequestor().equals(requestor))
              .collect(Collectors.toList());
    }
    Integer teamSelected = selectUserInfo(requestor).getTeamId();
    if (wildcardSearch != null && !wildcardSearch.isEmpty()) {
      aclListSub =
          aclListSub.stream()
              .filter(
                  req -> req.getTopicname().toLowerCase().contains(wildcardSearch.toLowerCase()))
              .collect(Collectors.toList());
    }

    for (AclRequests row : aclListSub) {

      Integer teamId;
      String rowRequestOperationType = row.getRequestOperationType();
      if (isApproval) {
        teamSelected = showRequestsOfAllTeams ? null : teamSelected;
        if (REQUESTOR_SUBSCRIPTIONS.equals(role)) {
          teamId = row.getRequestingteam();
        } else {
          teamId = row.getTeamId();
        }

        if (RequestOperationType.DELETE.value.equals(rowRequestOperationType)) {
          teamId = row.getRequestingteam();
        }

      } else {
        teamId = row.getRequestingteam();
      }

      if (showRequestsOfAllTeams) { // show all requests of all teams
        aclList.add(row);
      } else if (teamSelected != null && teamSelected.equals(teamId)) {
        aclList.add(row);
      }

      try {
        row.setRequesttimestring(
            DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(row.getRequesttime().toLocalDateTime()));
      } catch (Exception ignored) {
      }
    }

    return aclList;
  }

  /**
   * Query the AclRequestsRepo by supplying optional search parameters any given search parameters
   * will be utilised in the search.
   *
   * @param topic The topic Name
   * @param environment the environment
   * @param aclType Producer or consumer
   * @param requestStatus created/declined/approved
   * @param requestor the name of the user who created the request in klaw
   * @param tenantId The tenantId
   * @return
   */
  public Iterable<AclRequests> findAclRequestsByExample(
      String topic,
      String environment,
      AclType aclType,
      String requestStatus,
      String requestor,
      int tenantId,
      RequestOperationType requestOperationType) {

    AclRequests request = new AclRequests();

    request.setTenantId(tenantId);
    if (aclType != null) {
      request.setAclType(aclType.value);
    }
    if (environment != null) {
      request.setEnvironment(environment);
    }
    if (topic != null && !topic.isEmpty()) {
      request.setTopicname(topic);
    }
    if (requestStatus != null && !requestStatus.equalsIgnoreCase("all")) {
      request.setRequestStatus(requestStatus);
    }

    if (requestor != null && !requestor.isEmpty()) {
      request.setRequestor(requestor);
    }
    if (requestOperationType != null && requestOperationType != RequestOperationType.ALL) {
      request.setRequestOperationType(requestOperationType.value);
    }
    // check if debug is enabled so the logger doesnt waste resources converting object request to a
    // string
    if (log.isDebugEnabled()) {
      log.debug("find By topic etc example {}", request);
    }
    return aclRequestsRepo.findAll(Example.of(request));
  }

  public List<SchemaRequest> selectFilteredSchemaRequests(
      boolean isApproval,
      String requestor,
      int tenantId,
      RequestOperationType requestOperationType,
      String topic,
      String env,
      String status,
      String wildcardSearch,
      boolean showRequestsOfAllTeams,
      boolean isMyRequest) {
    if (log.isDebugEnabled()) {
      log.debug(
          "selectSchemaRequests isApproval {} Requestor: {} , tenantIf:{} , topic: {}, env: {}, status: {}, wildcardSearch: {}, showRequestsOfAllTeams {}, isMyRequest: {}",
          isApproval,
          requestor,
          tenantId,
          topic,
          env,
          status,
          wildcardSearch,
          showRequestsOfAllTeams,
          isMyRequest);
    }
    List<SchemaRequest> schemaList = new ArrayList<>();
    List<SchemaRequest> schemaListSub;
    Integer teamSelected = selectUserInfo(requestor).getTeamId();
    if (isApproval) {
      schemaListSub =
          Lists.newArrayList(
              findSchemaRequestsByExample(
                  topic,
                  env,
                  status != null ? status : RequestStatus.CREATED.value,
                  showRequestsOfAllTeams ? null : teamSelected,
                  tenantId,
                  isMyRequest ? requestor : null,
                  requestOperationType));

      // Placed here as it should only apply for approvers.
      schemaListSub =
          schemaListSub.stream()
              .filter(request -> !request.getRequestor().equals(requestor))
              .collect(Collectors.toList());
    } else {
      // Previously Status was being filtered by the calling method but it is more efficient to have
      // the database do this.
      // Similarily Team Selected now included in the database call as previously they were all
      // being filtered out after the data was returned.
      schemaListSub =
          Lists.newArrayList(
              findSchemaRequestsByExample(
                  topic,
                  env,
                  status,
                  teamSelected,
                  tenantId,
                  isMyRequest ? requestor : null,
                  requestOperationType));
    }

    boolean wildcardFilter = (wildcardSearch != null && !wildcardSearch.isEmpty());
    for (SchemaRequest row : schemaListSub) {

      try {
        row.setRequesttimestring(
            DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(row.getRequesttime().toLocalDateTime()));
      } catch (Exception ignored) {
      }
      if (!wildcardFilter
          || row.getTopicname().toLowerCase().contains(wildcardSearch.toLowerCase())) {
        schemaList.add(row);
      }
    }

    return schemaList;
  }

  public Iterable<SchemaRequest> findSchemaRequestsByExample(
      String topic,
      String environment,
      String status,
      Integer teamId,
      int tenantId,
      String userName,
      RequestOperationType requestOperationType) {

    SchemaRequest request = new SchemaRequest();

    request.setTenantId(tenantId);

    if (environment != null) {
      request.setEnvironment(environment);
    }
    if (topic != null && !topic.isEmpty()) {
      request.setTopicname(topic);
    }
    if (status != null && !status.equalsIgnoreCase("all")) {
      request.setRequestStatus(status);
    }
    if (teamId != null) {
      request.setTeamId(teamId);
    }
    if (userName != null && !userName.isEmpty()) {
      request.setRequestor(userName);
    }
    if (requestOperationType != null && requestOperationType != RequestOperationType.ALL) {
      request.setRequestOperationType(requestOperationType.value);
    }

    request.setForceRegister(null);
    // check if debug is enabled so the logger doesnt waste resources converting object request to a
    // string
    if (log.isDebugEnabled()) {
      log.debug("find By topic etc example {}", request);
    }
    return schemaRequestRepo.findAll(Example.of(request));
  }

  public SchemaRequest selectSchemaRequest(int avroSchemaId, int tenantId) {
    log.debug("selectSchemaRequest {}", avroSchemaId);
    SchemaRequestID schemaRequestID = new SchemaRequestID(avroSchemaId, tenantId);
    Optional<SchemaRequest> sR = schemaRequestRepo.findById(schemaRequestID);
    return sR.orElse(null);
  }

  public List<Topic> selectTopicDetails(String topic, int tenantId) {
    List<Topic> topicOpt = topicRepo.findAllByTopicnameAndTenantId(topic, tenantId);
    return topicOpt.isEmpty() ? Collections.emptyList() : topicOpt;
  }

  public List<KwKafkaConnector> selectConnectorDetails(String connectorName, int tenantId) {
    List<KwKafkaConnector> topicOpt =
        kafkaConnectorRepo.findAllByConnectorNameAndTenantId(connectorName, tenantId);
    return topicOpt.isEmpty() ? Collections.emptyList() : topicOpt;
  }

  // "All teams"
  // teamId 1 is All teams
  public List<Topic> selectSyncTopics(String env, Integer teamId, int tenantId) {
    log.debug("selectSyncTopics {}", teamId);
    if (teamId == null || teamId.equals(1)) {
      if (env == null || env.equals("ALL")) {
        return topicRepo.findAllByTenantId(tenantId);
      } else {
        List<String> topics = topicRepo.findAllTopicNamesForEnv(env, tenantId);
        return topicRepo.findAllByTenantIdAndTopicnameIn(tenantId, topics);
      }
    } else {
      if (env == null || "ALL".equals(env)) {
        return topicRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
      } else {
        List<String> topics = topicRepo.findAllTopicNamesForEnvAndTeam(env, teamId, tenantId);
        return topicRepo.findAllByTenantIdAndTopicnameIn(tenantId, topics);
      }
    }
  }

  public DashboardStats getDashboardStats(Integer teamId, int tenantId) {
    DashboardStats dashboardStats = new DashboardStats();
    int countProducers = 0, countConsumers = 0;
    List<Acl> acls =
        aclRepo.findAllByAclTypeAndTeamIdAndTenantId(AclType.PRODUCER.value, teamId, tenantId);
    List<String> topicList = new ArrayList<>();
    if (acls != null) {
      acls.forEach(a -> topicList.add(a.getTopicname()));
      countProducers = (int) topicList.stream().distinct().count();
    }
    dashboardStats.setProducerCount(countProducers);

    acls = aclRepo.findAllByAclTypeAndTeamIdAndTenantId(AclType.CONSUMER.value, teamId, tenantId);
    List<String> topicListCons = new ArrayList<>();
    if (acls != null) {
      acls.forEach(a -> topicListCons.add(a.getTopicname()));
      countConsumers = (int) topicListCons.stream().distinct().count();
    }
    dashboardStats.setConsumerCount(countConsumers);

    dashboardStats.setTeamMembersCount(userInfoRepo.countByTeamIdAndTenantId(teamId, tenantId));

    return dashboardStats;
  }

  public List<Topic> selectAllTopicsByTopictypeAndTeamname(
      String isProducerConsumer, Integer teamId, int tenantId) {
    log.debug("selectAllByTopictypeAndTeamname {} {}", isProducerConsumer, teamId);
    List<Topic> topics = new ArrayList<>();
    String topicType, aclPatternType;
    if (isProducerConsumer != null && isProducerConsumer.equals(AclType.PRODUCER.value)) {
      topicType = AclType.PRODUCER.value;
    } else {
      topicType = AclType.CONSUMER.value;
    }

    List<Acl> acls = aclRepo.findAllByAclTypeAndTeamIdAndTenantId(topicType, teamId, tenantId);
    Topic t;
    Map<String, Set<String>> topicEnvMap = new HashMap<>();
    String tmpTopicName;
    Set<String> envSet;

    for (Acl acl : acls) {
      t = new Topic();
      tmpTopicName = acl.getTopicname();
      aclPatternType = acl.getAclPatternType();

      if (aclPatternType != null && aclPatternType.equals(AclPatternType.PREFIXED.value))
        tmpTopicName = tmpTopicName + "--" + AclPatternType.PREFIXED.value + "--";

      t.setEnvironment(acl.getEnvironment());
      t.setTopicname(tmpTopicName);

      if (topicEnvMap.containsKey(tmpTopicName)) {
        envSet = topicEnvMap.get(tmpTopicName);
        if (!envSet.contains(acl.getEnvironment())) {
          envSet.add(acl.getEnvironment());
          topicEnvMap.put(tmpTopicName, envSet);
        }
      } else {
        envSet = new HashSet<>();
        envSet.add(acl.getEnvironment());
        topicEnvMap.put(tmpTopicName, envSet);
      }

      topics.add(t);
    }

    for (Topic topic : topics) {
      topic.setEnvironmentsSet(topicEnvMap.get(topic.getTopicname()));
    }

    topics =
        topics.stream()
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toCollection(
                        () -> new TreeSet<>(Comparator.comparing(Topic::getTopicname))),
                    ArrayList::new));

    return topics;
  }

  public List<Acl> selectSyncAcls(String env, int tenantId) {
    return aclRepo.findAllByEnvironmentAndTenantId(env, tenantId);
  }

  public List<Acl> getPrefixedAclsSOT(String env, int tenantId) {
    return aclRepo.findAllByEnvironmentAndAclPatternTypeAndTenantId(
        env, AclPatternType.PREFIXED.value, tenantId);
  }

  /**
   * @param isApproval boolean should all requests be returned (true if requesting for an approvers
   *     view)
   * @param requestor The UserName of the person who is requesting the list of topics
   * @param status Filter the response by the status of the requests e.g.
   *     all,created,declined,deleted,approved
   * @param showRequestsOfAllTeams boolean true if a superadmin with the permission to view
   *     requests. otherwise false
   * @param tenantId The identifier of the tenancy with which the requestor is from.
   * @param teamId filter the results by the name of the team who created the request
   * @param env filter the results by the environment identifier e.g. 1,2,3,4. Applied only when
   *     allReqs is true
   * @param wildcardSearch filter the results by a wildcard search of the topicnames. Applied only
   *     when allReqs is true
   * @return A list of TopicRequests that meet the filtered criteria. Applied only when allReqs is
   *     true
   */
  public List<TopicRequest> selectFilteredTopicRequests(
      boolean isApproval,
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      Integer teamId,
      RequestOperationType requestOperationType,
      String env,
      String wildcardSearch,
      boolean isMyRequest) {
    if (log.isDebugEnabled()) {
      log.debug(
          "selectTopicRequests {} {} {} {} {} {}",
          showRequestsOfAllTeams,
          requestor,
          status,
          teamId,
          env,
          wildcardSearch);
    }
    Integer teamSelected = selectUserInfo(requestor).getTeamId();
    List<TopicRequest> topicRequests = new ArrayList<>();
    List<TopicRequest> topicRequestListSub;
    if (isApproval) { // approvers
      // On Approvals this should always be filtered by the users current team
      topicRequestListSub =
          Lists.newArrayList(
              findTopicRequestsByExample(
                  requestOperationType != null ? requestOperationType.value : null,
                  showRequestsOfAllTeams ? null : teamSelected,
                  env,
                  status,
                  tenantId,
                  null,
                  isMyRequest ? requestor : null));

      // Only execute just before adding the separate claim list as this will make sure only the
      // claim topics this team is able to approve will be returned.
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(
                  topicRequest ->
                      !RequestOperationType.CLAIM.value.equals(
                          topicRequest.getRequestOperationType()))
              .collect(Collectors.toList());

      if (requestOperationType == null
          || requestOperationType.equals(RequestOperationType.CLAIM)
          || requestOperationType.equals(RequestOperationType.ALL)) {
        // Team Name is null here as it is the team who created the requested
        // Approving Team includes the teamId as that is how Claim topics know who is the owning
        // team.
        List<TopicRequest> claimTopicReqs =
            Lists.newArrayList(
                findTopicRequestsByExample(
                    RequestOperationType.CLAIM.value,
                    null,
                    env,
                    status,
                    tenantId,
                    showRequestsOfAllTeams ? null : String.valueOf(teamSelected),
                    isMyRequest ? requestor : null));
        topicRequestListSub.addAll(claimTopicReqs);
      }

      // remove users own requests to approve/show in the list
      // Placed here as it should only apply for approvers.
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(topicRequest -> !topicRequest.getRequestor().equals(requestor))
              .toList();
    } else {
      if (showRequestsOfAllTeams) {
        topicRequestListSub =
            Lists.newArrayList(
                findTopicRequestsByExample(
                    requestOperationType != null ? requestOperationType.value : null,
                    null,
                    env,
                    status,
                    tenantId,
                    null,
                    isMyRequest ? requestor : null));
      } else {

        topicRequestListSub =
            Lists.newArrayList(
                findTopicRequestsByExample(
                    requestOperationType != null ? requestOperationType.value : null,
                    teamSelected,
                    env,
                    status,
                    tenantId,
                    null,
                    isMyRequest ? requestor : null));
      }
    }

    if (teamId != null) {
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(topicRequest -> topicRequest.getTeamId().equals(teamId))
              .toList();
    }

    boolean wildcardFilter = (wildcardSearch != null && !wildcardSearch.isEmpty());

    for (TopicRequest row : topicRequestListSub) {

      try {
        row.setRequesttimestring(
            DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(row.getRequesttime().toLocalDateTime()));
      } catch (Exception ignored) {
      }
      filterAndAddTopicRequest(topicRequests, row, wildcardSearch, wildcardFilter);
    }

    return topicRequests;
  }

  private void filterAndAddTopicRequest(
      List<TopicRequest> topicRequestList,
      TopicRequest row,
      String wildcardSearch,
      boolean applyWildcardFilter) {
    if (!applyWildcardFilter
        || row.getTopicname().toLowerCase().contains(wildcardSearch.toLowerCase())) {
      topicRequestList.add(row);
    }
  }

  private void filterAndAddOperationalRequest(
      List<OperationalRequest> operationalRequestList,
      OperationalRequest row,
      String wildcardSearch,
      boolean applyWildcardFilter) {
    if (!applyWildcardFilter
        || row.getTopicname()
            .toLowerCase(Locale.ROOT)
            .contains(wildcardSearch.toLowerCase(Locale.ROOT))) {
      operationalRequestList.add(row);
    }
  }

  /**
   * Query the TopicRequestsRepo by supplying optional search parameters any given search parameters
   * will be utilised in the search.
   *
   * @param requestOperationType The type of topic request Create/claim etc
   * @param teamId The identifier of the team
   * @param environment the environment
   * @param status created/declined/approved
   * @param tenantId The tenantId @Param approvingTeam The team who is able to approve this request
   *     will be filled in the case of claim requests. @Param userName of the person who created the
   *     request
   * @return An Iterable of all TopicRequests that match the parameters that have been supplied
   */
  private Iterable<TopicRequest> findTopicRequestsByExample(
      String requestOperationType,
      Integer teamId,
      String environment,
      String status,
      int tenantId,
      String approvingTeam,
      String userName) {

    TopicRequest request = new TopicRequest();
    request.setTenantId(tenantId);

    if (requestOperationType != null
        && !requestOperationType.equals(RequestOperationType.ALL.value)) {
      request.setRequestOperationType(requestOperationType);
    }
    if (environment != null) {
      request.setEnvironment(environment);
    }
    if (teamId != null) {
      request.setTeamId(teamId);
    }

    if (approvingTeam != null && !approvingTeam.isEmpty()) {
      request.setApprovingTeamId(approvingTeam);
    }

    if (status != null && !status.equalsIgnoreCase("all")) {
      request.setRequestStatus(status);
    }
    // userName is transient and so not available in the database to be queried.
    if (userName != null && !userName.isEmpty()) {
      request.setRequestor(userName);
    }

    // check if debug is enabled so the logger doesn't waste resources converting object request to
    // a
    // string
    if (log.isDebugEnabled()) {
      log.debug("find By topic etc example {}", request);
    }

    return topicRequestsRepo.findAll(Example.of(request));
  }

  public List<KafkaConnectorRequest> selectFilteredKafkaConnectorRequests(
      boolean isApproval,
      String requestor,
      String status,
      RequestOperationType requestOperationType,
      boolean showRequestsOfAllTeams,
      int tenantId,
      String env,
      String search,
      boolean isMyRequest) {
    log.debug("selectConnectorRequestsByStatus {} {}", requestor, status);
    List<KafkaConnectorRequest> topicRequestList = new ArrayList<>();

    List<KafkaConnectorRequest> topicRequestListSub;
    Integer teamSelected = selectUserInfo(requestor).getTeamId();

    if (isApproval) { // approvers

      // Team Name is null here as it is the team who created the requested
      // Approving Team includes the teamId as that is how Claim topics know who is the owning team.
      List<KafkaConnectorRequest> claimTopicReqs =
          Lists.newArrayList(
              findKafkaConnectorRequestsByExample(
                  RequestOperationType.CLAIM.value,
                  null,
                  env,
                  status,
                  tenantId,
                  showRequestsOfAllTeams ? null : String.valueOf(teamSelected),
                  isMyRequest ? requestor : null));

      topicRequestListSub =
          Lists.newArrayList(
              findKafkaConnectorRequestsByExample(
                  requestOperationType != null ? requestOperationType.value : null,
                  showRequestsOfAllTeams ? null : teamSelected,
                  env,
                  status,
                  tenantId,
                  null,
                  isMyRequest ? requestor : null));

      // Only execute just before adding the separate claim list as this will make sure only the
      // claim topics this team is able to approve will be returned.
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(
                  request ->
                      !RequestOperationType.CLAIM.value.equals(request.getRequestOperationType()))
              .collect(Collectors.toList());

      topicRequestListSub.addAll(claimTopicReqs);
      // remove users own requests to approve/show in the list
      // Placed here as it should only apply for approvers.
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(topicRequest -> !topicRequest.getRequestor().equals(requestor))
              .collect(Collectors.toList());
    } else {
      // show my teams requests
      topicRequestListSub =
          Lists.newArrayList(
              findKafkaConnectorRequestsByExample(
                  requestOperationType != null ? requestOperationType.value : null,
                  showRequestsOfAllTeams ? null : teamSelected,
                  env,
                  status,
                  tenantId,
                  null,
                  isMyRequest ? requestor : null));
    }

    boolean wildcardSearch = search != null && !search.isEmpty();

    for (KafkaConnectorRequest row : topicRequestListSub) {
      try {
        row.setRequesttimestring(
            DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(row.getRequesttime().toLocalDateTime()));
      } catch (Exception ignored) {
      }
      if (!wildcardSearch || row.getConnectorName().toLowerCase().contains(search.toLowerCase()))
        topicRequestList.add(row); // no team filter
    }

    return topicRequestList;
  }

  public List<Topic> getTopicsByTopicNameAndTeamId(String topicName, int teamId, int tenantId) {

    return topicRepo.findAllByTopicnameAndTeamIdAndTenantId(topicName, teamId, tenantId);
  }

  /**
   * Query the KafkaConnectorRequestsRepo by supplying optional search parameters any given search
   * parameters will be utilised in the search.
   *
   * @param requestOperationType The type of topic request Create/claim etc
   * @param teamId The identifier of the team
   * @param environment the environment
   * @param status created/declined/approved
   * @param tenantId The tenantId @Param approvingTeam The team who is able to approve this request
   *     will be filled in the case of claim requests.
   * @return An Iterable of all TopicRequests that match the parameters that have been supplied
   */
  private Iterable<KafkaConnectorRequest> findKafkaConnectorRequestsByExample(
      String requestOperationType,
      Integer teamId,
      String environment,
      String status,
      int tenantId,
      String approvingTeam,
      String requestor) {

    KafkaConnectorRequest request = new KafkaConnectorRequest();
    request.setTenantId(tenantId);

    if (requestOperationType != null
        && !requestOperationType.equals(RequestOperationType.ALL.value)) {
      request.setRequestOperationType(requestOperationType);
    }
    if (environment != null) {
      request.setEnvironment(environment);
    }
    if (teamId != null) {
      request.setTeamId(teamId);
    }

    if (requestor != null) {
      request.setRequestor(requestor);
    }

    if (approvingTeam != null && !approvingTeam.isEmpty()) {
      request.setApprovingTeamId(approvingTeam);
    }

    if (status != null && !status.equalsIgnoreCase("all")) {
      request.setRequestStatus(status);
    }

    // check if debug is enabled so the logger doesn't waste resources converting object request to
    // a
    // string
    if (log.isDebugEnabled()) {
      log.debug("find By topic etc example {}", request);
    }

    return kafkaConnectorRequestsRepo.findAll(Example.of(request));
  }

  public TopicRequest selectTopicRequestsForTopic(int topicId, int tenantId) {
    log.debug("selectTopicRequestsForTopic {}", topicId);
    TopicRequestID topicRequestID = new TopicRequestID();
    topicRequestID.setTenantId(tenantId);
    topicRequestID.setTopicid(topicId);
    Optional<TopicRequest> topicReq = topicRequestsRepo.findById(topicRequestID);
    return topicReq.orElse(null);
  }

  public OperationalRequest selectOperationalRequestsForId(int reqId, int tenantId) {
    log.debug("selectOperationalRequestsForId {}", reqId);
    OperationalRequestID operationalRequestID = new OperationalRequestID();
    operationalRequestID.setTenantId(tenantId);
    operationalRequestID.setReqId(reqId);
    Optional<OperationalRequest> operationalRequest =
        operationalRequestsRepo.findById(operationalRequestID);
    return operationalRequest.orElse(null);
  }

  public KafkaConnectorRequest selectConnectorRequestsForConnector(int connectorId, int tenantId) {
    log.debug("selectConnectorRequestsForConnector {}", connectorId);
    KafkaConnectorRequestID kafkaConnectorRequestID = new KafkaConnectorRequestID();
    kafkaConnectorRequestID.setConnectorId(connectorId);
    kafkaConnectorRequestID.setTenantId(tenantId);

    Optional<KafkaConnectorRequest> topicReq =
        kafkaConnectorRequestsRepo.findById(kafkaConnectorRequestID);
    return topicReq.orElse(null);
  }

  public List<Team> selectAllTeams(int tenantId) {
    return teamRepo.findAllByTenantId(tenantId);
  }

  public List<Team> selectAllTeamsByTenantIdAndTeamId(int tenantId, int teamId) {
    return teamRepo.findAllByTenantIdAndTeamId(tenantId, teamId);
  }

  public AclRequests selectAcl(int req_no, int tenantId) {
    log.debug("selectAcl {}", req_no);
    AclRequestID aclRequestID = new AclRequestID();
    aclRequestID.setReq_no(req_no);
    aclRequestID.setTenantId(tenantId);

    Optional<AclRequests> aclReq = aclRequestsRepo.findById(aclRequestID);
    return aclReq.orElse(null);
  }

  public List<UserInfo> selectAllUsersInfo(int tenantId) {
    return userInfoRepo.findAllByTenantId(tenantId);
  }

  public List<UserInfo> selectAllUsersInfoForTeam(Integer teamId, int tenantId) {
    if (log.isDebugEnabled()) {
      List<UserInfo> team = userInfoRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
      log.debug("For team {} Team Details {} with size {}", teamId, team, team.size());
      return team;
    }
    return userInfoRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
  }

  public boolean existsUsersInfoForTeam(Integer teamId, int tenantId) {
    return userInfoRepo.existsByTeamIdAndTenantId(teamId, tenantId);
  }

  public List<Env> selectAllEnvs(KafkaClustersType type, int tenantId) {
    if (KafkaClustersType.ALL == type) {
      return envRepo.findAllByTenantId(tenantId);
    } else {
      return envRepo.findAllByTypeAndTenantId(type.value, tenantId);
    }
  }

  public Env selectEnvDetails(String environmentId, int tenantId) {
    EnvID envID = new EnvID();
    envID.setId(environmentId);
    envID.setTenantId(tenantId);
    Optional<Env> env = envRepo.findById(envID);
    return env.orElse(null);
  }

  public Iterable<Env> selectEnvsDetails(Collection<String> envIds, int tenantId) {
    List<EnvID> ids = new ArrayList<>(envIds.size());
    for (var env : envIds) {
      EnvID envID = new EnvID();
      envID.setId(env);
      envID.setTenantId(tenantId);
      ids.add(envID);
    }
    return envRepo.findAllById(ids);
  }

  public UserInfo selectUserInfo(String username) {
    Optional<UserInfo> userRec = userInfoRepo.findByUsernameIgnoreCase(username);
    return userRec.orElse(null);
  }

  public List<ActivityLog> selectActivityLog(
      String username, String env, boolean allReqs, int tenantId) {
    log.debug("selectActivityLog {}", username);
    List<ActivityLog> activityList;

    if (allReqs) {
      if (env == null || env.isBlank()) {
        activityList = Lists.newArrayList(activityLogRepo.findAllByTenantId(tenantId));
      } else {
        activityList = activityLogRepo.findAllByEnvAndTenantId(env, tenantId);
      }
    } else {
      final UserInfo userInfo = selectUserInfo(username);
      if (env == null || env.isBlank()) {
        activityList = activityLogRepo.findAllByTeamIdAndTenantId(userInfo.getTeamId(), tenantId);
      } else {
        activityList =
            activityLogRepo.findAllByEnvAndTeamIdAndTenantId(env, userInfo.getTeamId(), tenantId);
      }
    }

    for (ActivityLog row : activityList) {
      row.setActivityTimeString(
          DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(row.getActivityTime().toLocalDateTime()));
    }

    return activityList;
  }

  public List<Team> selectTeamsOfUsers(String username, int tenantId) {

    Optional<UserInfo> userInfoOpt =
        userInfoRepo.findFirstByTenantIdAndUsername(tenantId, username);
    if (userInfoOpt.isEmpty()) {
      return Collections.emptyList();
    }

    Integer teamId = userInfoOpt.get().getTeamId();

    log.debug("selectTeamsOfUsers {}", username);
    return selectAllTeamsByTenantIdAndTeamId(tenantId, teamId);
  }

  public Map<String, String> getDashboardInfo(Integer teamId, int tenantId) {
    log.debug("getDashboardInfo {}", teamId);
    Map<String, String> dashboardInfo = new HashMap<>();
    Integer topicListSize = topicRepo.findDistinctCountTopicnameByTeamId(teamId, tenantId);
    dashboardInfo.put("myteamtopics", "" + topicListSize);

    return dashboardInfo;
  }

  Acl selectSyncAclsFromReqNo(int reqNo, int tenantId) {
    AclID aclID = new AclID();
    aclID.setReq_no(reqNo);
    aclID.setTenantId(tenantId);

    Optional<Acl> aclRec = aclRepo.findById(aclID);
    return aclRec.orElse(null);
  }

  public List<Topic> getTopics(String topicName, boolean allTopics, int tenantId) {
    log.debug("getTopics {} {}", topicName, allTopics);
    if (allTopics) {
      return Lists.newArrayList(topicRepo.findAllByTenantId(tenantId));
    } else {
      return topicRepo.findAllByTopicnameAndTenantId(topicName, tenantId);
    }
  }

  public List<KwKafkaConnector> getConnectors(String topicName, boolean allTopics, int tenantId) {
    log.debug("getConnectors {} {}", topicName, allTopics);
    if (allTopics) {
      return Lists.newArrayList(kafkaConnectorRepo.findAllByTenantId(tenantId));
    } else {
      return kafkaConnectorRepo.findAllByConnectorNameAndTenantId(topicName, tenantId);
    }
  }

  public Optional<Topic> getTopicFromId(int topicId, int tenantId) {
    TopicID topicID = new TopicID();
    topicID.setTenantId(tenantId);
    topicID.setTopicid(topicId);
    return topicRepo.findById(topicID);
  }

  public List<Topic> getTopicsFromEnv(String envId, int tenantId) {
    return topicRepo.findAllByEnvironmentAndTenantId(envId, tenantId);
  }

  public List<Acl> selectSyncAcls(String env, String topic, int tenantId) {
    return aclRepo.findAllByEnvironmentAndTopicnameAndTenantId(env, topic, tenantId);
  }

  public List<Acl> selectSyncAcls(
      String env, String topic, int teamId, String consumerGroup, int tenantId) {
    return aclRepo.findAllByEnvironmentAndTopicnameAndTeamIdAndConsumergroupAndTenantId(
        env, topic, teamId, consumerGroup, tenantId);
  }

  public List<RegisterUserInfo> selectAllRegisterUsersInfoForTenant(int tenantId) {
    return registerInfoRepo.findAllByStatusAndTenantId("PENDING", tenantId);
  }

  public int countRegisterUsersInfoForTenant(int tenantId) {
    return registerInfoRepo.countByStatusAndTenantId("PENDING", tenantId);
  }

  public List<RegisterUserInfo> selectAllRegisterUsersInfo() {
    return registerInfoRepo.findAllByStatus("PENDING");
  }

  public RegisterUserInfo selectFirstStagingRegisterUsersInfo(String userId) {
    return registerInfoRepo.findFirstByUsernameAndStatus(userId, "STAGING");
  }

  public RegisterUserInfo selectRegisterUsersInfo(String username) {
    Optional<RegisterUserInfo> registerUserRec = registerInfoRepo.findById(username);
    return registerUserRec.orElse(null);
  }

  public List<Acl> getUniqueConsumerGroups(int tenantId) {
    return aclRepo.findAllByTenantId(tenantId);
  }

  public boolean validateIfConsumerGroupUsedByAnotherTeam(
      Integer teamId, int tenantId, String consumerGroup) {
    return aclRepo.existsByTeamIdNotAndTenantIdAndConsumergroup(teamId, tenantId, consumerGroup);
  }

  public Team selectTeamDetails(Integer teamId, int tenantId) {
    TeamID teamID = new TeamID(teamId, tenantId);
    Optional<Team> teamList = teamRepo.findById(teamID);
    return teamList.orElse(null);
  }

  public Team selectTeamDetailsFromName(String teamName, int tenantId) {
    return teamRepo.findFirstByTenantIdAndTeamnameOrderByTenantId(tenantId, teamName);
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> selectActivityLogByTeam(
      Integer teamId, int numberOfDays, int tenantId) {
    try {
      List<CommonUtilsService.ChartsOverviewItem<String, Integer>> res = new ArrayList<>();
      List<Pair<String, Integer>> fromDb =
          gatherActivityList(
              activityLogRepo.findActivityLogForTeamIdForLastNDays(teamId, tenantId, numberOfDays));
      for (Pair<String, Integer> elem : fromDb) {
        res.add(CommonUtilsService.ChartsOverviewItem.of(elem.getKey(), elem.getValue()));
      }
      return res;
    } catch (Exception e) {
      log.error("Error selectActivityLogForLastDays ", e);
    }
    return Collections.emptyList();
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> selectActivityLogForLastDays(
      int numberOfDays, String[] envIdList, int tenantId) {
    try {
      List<CommonUtilsService.ChartsOverviewItem<String, Integer>> res = new ArrayList<>();
      List<Pair<String, Integer>> fromDb =
          gatherActivityList(
              activityLogRepo.findActivityLogForLastNDays(envIdList, tenantId, numberOfDays));
      for (Pair<String, Integer> elem : fromDb) {
        res.add(CommonUtilsService.ChartsOverviewItem.of(elem.getKey(), elem.getValue()));
      }
      return res;
    } catch (Exception e) {
      log.error("Error selectActivityLogForLastDays ", e);
    }
    return Collections.emptyList();
  }

  static class ActivityCountItem {
    private String dateOfActivity;
    private int activityCount;

    private ActivityCountItem(String dateOfActivity, int activityCount) {
      this.dateOfActivity = dateOfActivity;
      this.activityCount = activityCount;
    }

    public static ActivityCountItem of(String dateOfActivity, int activityCount) {
      return new ActivityCountItem(dateOfActivity, activityCount);
    }
  }

  private List<Pair<String, Integer>> gatherActivityList(List<Object[]> activityCount) {
    List<Pair<String, Integer>> res = new ArrayList<>(activityCount.size());
    for (Object[] activity : activityCount) {

      res.add(
          new ImmutablePair<>(
              DATE_DDMMMYYYY_FORMATTER.format(((java.sql.Date) activity[0]).toLocalDate()),
              ((Long) activity[1]).intValue()));
    }
    return res;
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> selectTopicsCountByEnv(
      Integer tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> totalTopicCount =
        new ArrayList<>();
    try {
      List<Object[]> topics = topicRepo.findAllTopicsGroupByEnv(tenantId);

      for (Object[] topic : topics) {
        totalTopicCount.add(
            CommonUtilsService.ChartsOverviewItem.of(
                (String) topic[0], ((Long) topic[1]).intValue()));
      }
    } catch (Exception e) {
      log.error("Error selectTopicsCountByEnv ", e);
    }
    return totalTopicCount;
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> selectPartitionsCountByEnv(
      Integer teamId, Integer tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> totalPartitionsCount =
        new ArrayList<>();
    try {
      List<Object[]> topics;
      if (teamId != null) {
        topics = topicRepo.findAllPartitionsForTeamGroupByEnv(teamId, tenantId);
      } else {
        topics = topicRepo.findAllPartitionsGroupByEnv(tenantId);
      }

      for (Object[] topic : topics) {
        totalPartitionsCount.add(
            CommonUtilsService.ChartsOverviewItem.of(
                String.valueOf(topic[0]), ((Long) topic[1]).intValue()));
      }
    } catch (Exception e) {
      log.error("Error from selectPartitionsCountByEnv ", e);
    }
    return totalPartitionsCount;
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> selectAclsCountByEnv(
      Integer teamId, Integer tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> totalAclsCount = new ArrayList<>();
    try {
      List<Object[]> topics;
      if (teamId != null) {
        topics = aclRepo.findAllAclsforTeamGroupByEnv(teamId, tenantId);
      } else {
        topics = aclRepo.findAllAclsGroupByEnv(tenantId);
      }

      for (Object[] topic : topics) {
        totalAclsCount.add(
            CommonUtilsService.ChartsOverviewItem.of((String) topic[0], (Integer) topic[1]));
      }
    } catch (Exception e) {
      log.error("Error selectAclsCountByEnv", e);
    }
    return totalAclsCount;
  }

  public List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> selectTopicsCountByTeams(
      Integer teamId, int tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> totalTopicCount =
        new ArrayList<>();
    try {
      List<Object[]> topics;
      if (teamId != null) {
        topics = topicRepo.findAllTopicsForTeam(teamId, tenantId);
      } else {
        topics = topicRepo.findAllTopicsGroupByTeamId(tenantId);
      }

      for (Object[] topic : topics) {
        if (teamId == null) {
          totalTopicCount.add(
              CommonUtilsService.ChartsOverviewItem.of(
                  (Integer) topic[0], ((Long) topic[1]).intValue()));
        } else {
          totalTopicCount.add(
              CommonUtilsService.ChartsOverviewItem.of(teamId, ((Long) topic[0]).intValue()));
        }
      }
    } catch (Exception e) {
      log.error("Error from selectTopicsCountByTeams ", e);
    }
    return totalTopicCount;
  }

  public List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> selectAclsCountByTeams(
      String aclType, Integer teamId, Integer tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> totalAclCount = new ArrayList<>();
    try {
      List<Object[]> acls;
      if (AclType.PRODUCER.value.equals(aclType)) {
        if (teamId != null) {
          acls = aclRepo.findAllProducerAclsForTeamId(teamId, tenantId);
        } else {
          acls = aclRepo.findAllProducerAclsGroupByTeamId(tenantId);
        }
      } else {
        if (teamId != null) {
          acls = aclRepo.findAllConsumerAclsForTeamId(teamId, tenantId);
        } else {
          acls = aclRepo.findAllConsumerAclsGroupByTeamId(tenantId);
        }
      }
      for (Object[] topic : acls) {
        if (teamId == null) {
          totalAclCount.add(
              CommonUtilsService.ChartsOverviewItem.of(
                  ((Long) topic[0]).intValue(), ((Long) topic[1]).intValue()));
        } else {
          totalAclCount.add(
              CommonUtilsService.ChartsOverviewItem.of(teamId, ((Long) topic[0]).intValue()));
        }
      }
    } catch (Exception e) {
      log.error("Error from selectAclsCountByTeams ", e);
    }
    return totalAclCount;
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>>
      selectAllTopicsForTeamGroupByEnv(Integer teamId, int tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> totalTopicCount =
        new ArrayList<>();
    try {
      List<Object[]> topics = topicRepo.findAllTopicsForTeamGroupByEnv(teamId, tenantId);
      Map<String, Integer> envIds =
          topics.stream()
              .collect(Collectors.toMap(e -> (String) e[0], e -> ((Long) e[1]).intValue()));

      Map<String, Env> name2envsFromDb =
          StreamSupport.stream(selectEnvsDetails(envIds.keySet(), tenantId).spliterator(), false)
              .collect(Collectors.toMap(Env::getId, Function.identity()));

      for (var envIdEntry : envIds.entrySet()) {
        if (!name2envsFromDb.containsKey(envIdEntry.getKey())) {
          log.error("Error: Environment not found for env {}", envIdEntry.getKey());
        } else {
          totalTopicCount.add(
              CommonUtilsService.ChartsOverviewItem.of(
                  name2envsFromDb.get(envIdEntry.getKey()).getName(), envIdEntry.getValue()));
        }
      }
    } catch (Exception e) {
      log.error("Error from selectAllTopicsForTeamGroupByEnv ", e);
    }
    return totalTopicCount;
  }

  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> selectAllMetrics(
      String metricsType, String metricsName, String env) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> metricsCount = new ArrayList<>();
    try {
      List<Object[]> metrics =
          kwMetricsRepo.findAllByEnvAndMetricsTypeAndMetricsName(env, metricsType, metricsName);

      for (Object[] kwMetrics : metrics) {
        metricsCount.add(
            CommonUtilsService.ChartsOverviewItem.of(
                (String) kwMetrics[0], Integer.parseInt(kwMetrics[1].toString())));
      }
    } catch (Exception e) {
      log.error("Error from selectAllMetrics ", e);
    }
    return metricsCount;
  }

  public Map<Integer, Map<String, Map<String, String>>> selectAllKwProperties() {
    Map<Integer, Map<String, Map<String, String>>> tenantProps = new HashMap<>();

    Map<String, String> mapProps;
    List<KwTenants> tenants = getTenants();

    Map<Integer, List<KwProperties>> tenantId2kwPropsMap =
        kwPropertiesRepo
            .findAllByTenantIdsOrdered(tenants.stream().map(KwTenants::getTenantId).toList())
            .stream()
            .collect(Collectors.groupingBy(KwProperties::getTenantId));
    for (var entry : tenantId2kwPropsMap.entrySet()) {
      Map<String, Map<String, String>> fullMap = new HashMap<>();
      for (KwProperties kwProp : entry.getValue()) {
        mapProps = new HashMap<>();
        mapProps.put("kwkey", kwProp.getKwKey());
        mapProps.put("kwvalue", kwProp.getKwValue());
        mapProps.put("kwdesc", kwProp.getKwDesc());
        mapProps.put("kwtenantid", kwProp.getTenantId() + "");

        fullMap.put(kwProp.getKwKey(), mapProps);
      }
      tenantProps.put(entry.getKey(), fullMap);
    }

    return tenantProps;
  }

  public List<KwProperties> selectAllKwPropertiesPerTenant(int tenantId) {
    return kwPropertiesRepo.findAllByTenantId(tenantId);
  }

  public Integer getNextTopicRequestId(String idType, int tenantId) {
    Integer topicId;
    if (idType.equals("TOPIC_REQ_ID")) {
      topicId = topicRequestsRepo.getNextTopicRequestId(tenantId);
    } else if (idType.equals("TOPIC_ID")) {
      topicId = topicRepo.getNextTopicRequestId(tenantId);
    } else {
      topicId = null;
    }

    if (topicId == null) {
      return 1001;
    } else {
      return topicId + 1;
    }
  }

  public Integer getNextConnectorRequestId(String idType, int tenantId) {
    Integer connectorId = null;
    if ("CONNECTOR_REQ_ID".equals(idType)) {
      connectorId = kafkaConnectorRequestsRepo.getNextConnectorRequestId(tenantId);
    } else if ("CONNECTOR_ID".equals(idType)) {
      connectorId = kafkaConnectorRepo.getNextConnectorRequestId(tenantId);
    }
    return (connectorId == null) ? 1001 : connectorId + 1;
  }

  public List<KwTenants> getTenants() {
    return Lists.newArrayList(tenantRepo.findAll());
  }

  public Optional<KwTenants> getMyTenants(int tenantId) {
    return tenantRepo.findById(tenantId);
  }

  public List<KwRolesPermissions> getRolesPermissions() {
    return Lists.newArrayList(kwRolesPermsRepo.findAll());
  }

  public List<KwRolesPermissions> getRolesPermissionsPerTenant(int tenantId) {
    return kwRolesPermsRepo.findAllByTenantId(tenantId);
  }

  public List<KwClusters> getAllClusters(KafkaClustersType typeOfCluster, int tenantId) {
    if (KafkaClustersType.ALL == typeOfCluster) {
      return kwClusterRepo.findAllByTenantId(tenantId);
    } else {
      return kwClusterRepo.findAllByClusterTypeAndTenantId(typeOfCluster.value, tenantId);
    }
  }

  public boolean existsClusters(KafkaClustersType typeOfCluster, int tenantId) {
    return kwClusterRepo.existsByClusterTypeAndTenantId(typeOfCluster.value, tenantId);
  }

  public KwClusters getClusterDetails(int id, int tenantId) {
    KwClusterID kwClusterID = new KwClusterID();
    kwClusterID.setClusterId(id);
    kwClusterID.setTenantId(tenantId);

    Optional<KwClusters> kwCluster = kwClusterRepo.findById(kwClusterID);
    return kwCluster.orElse(null);
  }

  public List<TopicRequest> selectTopicRequests(
      String topicName, String envId, String status, int tenantId) {
    return topicRequestsRepo.findAllByRequestStatusAndTopicnameAndEnvironmentAndTenantId(
        status, topicName, envId, tenantId);
  }

  public boolean existsTopicRequests(String topicName, String envId, String status, int tenantId) {
    return topicRequestsRepo.existsByRequestStatusAndTopicnameAndEnvironmentAndTenantId(
        status, topicName, envId, tenantId);
  }

  public List<KafkaConnectorRequest> selectConnectorRequests(
      String connectorName, String envId, String status, int tenantId) {
    return kafkaConnectorRequestsRepo
        .findAllByRequestStatusAndConnectorNameAndEnvironmentAndTenantId(
            status, connectorName, envId, tenantId);
  }

  public String getRegistrationId(String userId) {
    RegisterUserInfo registerInfoStaging =
        registerInfoRepo.findFirstByUsernameAndStatus(userId, "STAGING");
    if (registerInfoStaging != null) {
      return registerInfoStaging.getRegistrationId();
    }
    boolean pendingArePresent =
        registerInfoRepo.existsRegisterUserInfoByUsernameAndStatus(userId, "PENDING");
    if (pendingArePresent) {
      return "PENDING_ACTIVATION";
    }
    return null;
  }

  public RegisterUserInfo getRegistrationDetails(String registrationId, String status) {
    if ("".equals(status)) {
      return registerInfoRepo.findFirstByRegistrationId(registrationId);
    } else {
      return registerInfoRepo.findFirstByRegistrationIdAndStatus(registrationId, status);
    }
  }

  public List<Topic> getTopicsforTeam(Integer teamId, int tenantId) {
    return topicRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
  }

  public List<Acl> getConsumerGroupsforTeam(Integer teamId, int tenantId) {
    return aclRepo.findAllByAclTypeAndTeamIdAndTenantId(AclType.CONSUMER.name(), teamId, tenantId);
  }

  public List<Acl> getAllConsumerGroups(int tenantId) {
    return aclRepo.findAllByAclTypeAndTenantId(AclType.CONSUMER.name(), tenantId);
  }

  public List<Topic> getTopicDetailsPerEnv(String topicName, String envId, int tenantId) {
    log.debug("getTopicDetailsPerEnv {} {}", topicName, envId);
    return topicRepo.findAllByTopicnameAndEnvironmentAndTenantId(topicName, envId, tenantId);
  }

  // All teams
  public List<KwKafkaConnector> selectSyncConnectors(String env, Integer teamId, int tenantId) {
    log.debug("selectSyncConnectors {}", teamId);
    if (teamId == null || teamId.equals(1)) {
      if (env == null || env.equals("ALL")) {
        return kafkaConnectorRepo.findAllByTenantId(tenantId);
      } else {
        return kafkaConnectorRepo.findAllByEnvironmentAndTenantId(env, tenantId);
      }
    } else {
      if (env == null || "ALL".equals(env)) {
        return kafkaConnectorRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
      }
      return kafkaConnectorRepo.findAllByEnvironmentAndTeamIdAndTenantId(env, teamId, tenantId);
    }
  }

  public List<UserInfo> selectAllUsersAllTenants() {
    return Lists.newArrayList(userInfoRepo.findAll());
  }

  public Optional<ProductDetails> selectProductDetails(String name) {
    return productDetailsRepo.findById(name);
  }

  public boolean existsKafkaComponentsForEnv(String env, int tenantId) {
    return Stream.<Supplier<Boolean>>of(
            () -> topicRepo.existsByEnvironmentAndTenantId(env, tenantId),
            () ->
                topicRequestsRepo.existsByTenantIdAndEnvironmentAndRequestStatus(
                    tenantId, env, RequestStatus.CREATED.value),
            () -> aclRepo.existsByEnvironmentAndTenantId(env, tenantId),
            () ->
                aclRequestsRepo.existsByTenantIdAndEnvironmentAndRequestStatus(
                    tenantId, env, RequestStatus.CREATED.value))
        .anyMatch(Supplier::get);
  }

  public boolean existsConnectorComponentsForEnv(String env, int tenantId) {
    return Stream.<Supplier<Boolean>>of(
            () -> kafkaConnectorRepo.existsByEnvironmentAndTenantId(env, tenantId),
            () ->
                kafkaConnectorRequestsRepo.existsConnectorRequestsForEnvTenantIdAndCreatedStatus(
                    env, tenantId))
        .anyMatch(Supplier::get);
  }

  public boolean existsSchemaComponentsForEnv(String env, int tenantId) {
    return Stream.<Supplier<Boolean>>of(
            () -> schemaRequestRepo.existsSchemaRequestByEnvironmentAndTenantId(env, tenantId),
            () -> messageSchemaRepo.existsMessageSchemaByEnvironmentAndTenantId(env, tenantId))
        .anyMatch(Supplier::get);
  }

  public boolean existsComponentsCountForTeam(Integer teamId, int tenantId) {
    return Stream.<Supplier<Boolean>>of(
            () -> {
              boolean res =
                  schemaRequestRepo.existsByTeamIdAndTenantIdAndRequestStatus(
                      teamId, tenantId, RequestStatus.CREATED.value);
              log.debug("For team {} Active Schema Requests {}", teamId, res);
              return res;
            },
            () -> {
              boolean res = messageSchemaRepo.existsByTeamIdAndTenantId(teamId, tenantId);
              log.debug("For team {} number of Schemas in DB {}", teamId, res);
              return res;
            },
            () -> {
              boolean res = kafkaConnectorRepo.existsByTeamIdAndTenantId(teamId, tenantId);
              log.debug("For team {} Active Connector Requests {}", teamId, res);
              return res;
            },
            () -> {
              boolean res =
                  kafkaConnectorRequestsRepo.existsByTeamIdAndTenantIdAndRequestStatus(
                      teamId, tenantId, RequestStatus.CREATED.value);
              log.debug("For team {} number of Connector in DB {}", teamId, res);
              return res;
            },
            () -> {
              boolean res = topicRepo.existsByTeamIdAndTenantId(teamId, tenantId);
              log.debug("For team {} Active Topic Requests {}", teamId, res);
              return res;
            },
            () -> {
              boolean res =
                  topicRequestsRepo.existsByTeamIdAndTenantIdAndRequestStatus(
                      teamId, tenantId, RequestStatus.CREATED.value);
              log.debug("For team {} number of Topic in DB {}", teamId, res);
              return res;
            },
            () -> {
              boolean res = aclRepo.existsByTeamIdAndTenantId(teamId, tenantId);
              log.debug("For team {} Active ACL Requests {}", teamId, res);
              return res;
            },
            () -> {
              boolean res =
                  aclRequestsRepo.existsByTeamIdAndTenantIdAndRequestStatus(
                      teamId, tenantId, RequestStatus.CREATED.value);
              log.debug("For team {} number of ACL in DB {}", teamId, res);
              return res;
            })
        .anyMatch(Supplier::get);
  }

  public boolean existsComponentsCountForUser(String userId, int tenantId) {
    return Stream.<Supplier<Boolean>>of(
            () ->
                schemaRequestRepo.existsByRequestorAndTenantIdAndRequestStatus(
                    userId, tenantId, RequestStatus.CREATED.value),
            () ->
                kafkaConnectorRequestsRepo.existsByRequestorAndTenantIdAndRequestStatus(
                    userId, tenantId, RequestStatus.CREATED.value),
            () ->
                topicRequestsRepo.existsByRequestorAndTenantIdAndRequestStatus(
                    userId, tenantId, RequestStatus.CREATED.value),
            () ->
                aclRequestsRepo.existsByRequestorAndTenantIdAndRequestStatus(
                    userId, tenantId, RequestStatus.CREATED.value))
        .anyMatch(Supplier::get);
  }

  public int getAllTopicsCountInAllTenants() {
    return topicRepo.findAllTopicsCount();
  }

  // teamId is requestedBy. For 'topics' all the requests are assigned to the same team, except
  // claim requests
  public Map<String, Map<String, Long>> getTopicRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    Map<String, Long> operationTypeCountsMap =
        topicRequestsRepo.getCountPerTopicType(teamId, tenantId);
    Map<String, Long> statusCountsMap = topicRequestsRepo.getCountPerTopicStatus(teamId, tenantId);

    if (RequestMode.TO_APPROVE == requestMode || RequestMode.MY_APPROVALS == requestMode) {
      long assignedToClaimReqs =
          topicRequestsRepo.countAllTopicRequestsByApprovingTeamAndTopictype(
              tenantId, "" + teamId, RequestOperationType.CLAIM.value);

      operationTypeCountsMap.put(RequestOperationType.CLAIM.value, assignedToClaimReqs);
      if (RequestMode.MY_APPROVALS == requestMode) {
        // Make sure to remove any requests which the requestor can not approve
        // Using Integer.toString() as it seems to be the fastest method of changing it.
        Long topicApprovalCount =
            topicRequestsRepo.countRequestorsTopicRequestsGroupByStatusType(
                teamId, Integer.toString(teamId), tenantId, requestor, RequestStatus.CREATED.value);

        statusCountsMap.put(
            RequestStatus.CREATED.value, topicApprovalCount == null ? 0L : topicApprovalCount);
      }
    }

    return Map.of(
        "STATUS_COUNTS", statusCountsMap, "OPERATION_TYPE_COUNTS", operationTypeCountsMap);
  }

  // Acl requests can be submitted by any team. your team or other teams on topics.
  // For requests raised by your team, 'requestingteam' is the column in aclrequests table
  // For requests assigned to your team for approval, 'teamid' is the column in aclrequests table
  public Map<String, Map<String, Long>> getAclRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    Map<String, Map<String, Long>> allCountsMap = new HashMap<>();
    Map<String, Long> operationTypeCountsMap = new HashMap<>();
    Map<String, Long> statusCountsMap = new HashMap<>();

    if (RequestMode.MY_REQUESTS == requestMode) {
      List<Object[]> aclRequestsOperationTypObj =
          aclRequestsRepo.findAllAclRequestsGroupByOperationTypeMyTeam(teamId, tenantId);
      updateMap(operationTypeCountsMap, aclRequestsOperationTypObj);

      List<Object[]> aclRequestsStatusObj =
          aclRequestsRepo.findAllAclRequestsGroupByStatusMyTeam(teamId, tenantId);
      updateMap(statusCountsMap, aclRequestsStatusObj);
    } else if (RequestMode.TO_APPROVE == requestMode || RequestMode.MY_APPROVALS == requestMode) {
      List<Object[]> aclRequestsOperationTypObj =
          aclRequestsRepo.findAllAclRequestsGroupByOperationTypeAssignedToTeam(teamId, tenantId);
      updateMap(operationTypeCountsMap, aclRequestsOperationTypObj);

      List<Object[]> aclRequestsStatusObj =
          aclRequestsRepo.findAllAclRequestsGroupByStatusAssignedToTeam(teamId, tenantId);
      updateMap(statusCountsMap, aclRequestsStatusObj);
      if (RequestMode.MY_APPROVALS == requestMode) {
        // Make sure to remove any requests which the requestor can not approve
        Long aclApprovalCount =
            aclRequestsRepo.countRequestorsAclRequestsGroupByStatusType(
                teamId, tenantId, requestor, RequestStatus.CREATED.value);

        statusCountsMap.put(
            RequestStatus.CREATED.value, aclApprovalCount == null ? 0L : aclApprovalCount);
      }
    }
    // update with 0L if requests don't exist
    updateCountsForNonExistingRequestTypes(operationTypeCountsMap, statusCountsMap);

    allCountsMap.put("STATUS_COUNTS", statusCountsMap);
    allCountsMap.put("OPERATION_TYPE_COUNTS", operationTypeCountsMap);

    return allCountsMap;
  }

  // teamId is requestedBy. For 'schemas' all the requests are assigned to the same team
  public Map<String, Map<String, Long>> getSchemaRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    Map<String, Map<String, Long>> allCountsMap = new HashMap<>();

    Map<String, Long> operationTypeCountsMap = new HashMap<>();
    Map<String, Long> statusCountsMap = new HashMap<>();

    if (RequestMode.MY_REQUESTS == requestMode
        || RequestMode.TO_APPROVE == requestMode
        || RequestMode.MY_APPROVALS == requestMode) {
      List<Object[]> schemaRequestsOperationTypObj =
          schemaRequestRepo.findAllSchemaRequestsGroupByOperationType(teamId, tenantId);
      updateMap(operationTypeCountsMap, schemaRequestsOperationTypObj);

      List<Object[]> schemaRequestsStatusObj =
          schemaRequestRepo.findAllSchemaRequestsGroupByStatus(teamId, tenantId);
      updateMap(statusCountsMap, schemaRequestsStatusObj);
    }
    if (RequestMode.MY_APPROVALS == requestMode) {
      // Make sure to remove any requests which the requestor can not approve
      Long schemaApprovalCount =
          schemaRequestRepo.countRequestorsSchemaRequestsGroupForStatusType(
              teamId, tenantId, requestor, RequestStatus.CREATED.value);
      statusCountsMap.put(
          RequestStatus.CREATED.value, schemaApprovalCount == null ? 0L : schemaApprovalCount);
    }

    // update with 0L if requests don't exist
    updateCountsForNonExistingRequestTypes(operationTypeCountsMap, statusCountsMap);

    allCountsMap.put("STATUS_COUNTS", statusCountsMap);
    allCountsMap.put("OPERATION_TYPE_COUNTS", operationTypeCountsMap);

    return allCountsMap;
  }

  public Map<String, Set<String>> getTopicAndVersionsForEnvAndTenantId(String envId, int tenantId) {
    Map<String, Set<String>> topicSchemaVersionsMap = new HashMap<>();
    List<Object[]> topicSchemaData =
        messageSchemaRepo.findTopicAndVersionsForEnvAndTenantId(envId, tenantId);
    for (Object[] topicSchemaDatum : topicSchemaData) {
      String topicName = (String) topicSchemaDatum[0];

      if (topicSchemaVersionsMap.containsKey(topicName)) {
        topicSchemaVersionsMap.get(topicName).add((String) topicSchemaDatum[1]);
      } else {
        Set<String> versionSet = new HashSet<>();
        versionSet.add((String) topicSchemaDatum[1]);
        topicSchemaVersionsMap.put(topicName, versionSet);
      }
    }

    return topicSchemaVersionsMap;
  }

  // teamId is requestedBy. For 'connectors' all the requests are assigned to the same team
  public Map<String, Map<String, Long>> getConnectorRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    Map<String, Map<String, Long>> allCountsMap = new HashMap<>();

    Map<String, Long> operationTypeCountsMap = new HashMap<>();
    Map<String, Long> statusCountsMap = new HashMap<>();

    if (RequestMode.MY_REQUESTS == requestMode
        || RequestMode.TO_APPROVE == requestMode
        || RequestMode.MY_APPROVALS == requestMode) {
      List<Object[]> connectorRequestsOperationTypObj =
          kafkaConnectorRequestsRepo.findAllConnectorRequestsGroupByOperationType(teamId, tenantId);
      updateMap(operationTypeCountsMap, connectorRequestsOperationTypObj);

      List<Object[]> connectorRequestsStatusObj =
          kafkaConnectorRequestsRepo.findAllConnectorRequestsGroupByStatus(teamId, tenantId);
      updateMap(statusCountsMap, connectorRequestsStatusObj);
    }

    if (RequestMode.MY_APPROVALS == requestMode) {
      // Make sure to remove any requests which the requestor can not approve
      Long connectorApprovalCount =
          kafkaConnectorRequestsRepo.countRequestorsConnectorRequestsGroupByStatusType(
              teamId, Integer.toString(teamId), tenantId, requestor, RequestStatus.CREATED.value);

      statusCountsMap.put(
          RequestStatus.CREATED.value,
          connectorApprovalCount == null ? 0L : connectorApprovalCount);
    }

    // update with 0L if requests don't exist
    updateCountsForNonExistingRequestTypes(operationTypeCountsMap, statusCountsMap);

    allCountsMap.put("STATUS_COUNTS", statusCountsMap);
    allCountsMap.put("OPERATION_TYPE_COUNTS", operationTypeCountsMap);

    return allCountsMap;
  }

  public MessageSchema getTeamIdFromSchemaTopicNameAndEnvAndTenantId(
      String schemaTopicName, String envId, int tenantId) {
    List<MessageSchema> schema =
        messageSchemaRepo.findAllByTenantIdAndTopicnameAndEnvironment(
            tenantId, schemaTopicName, envId);
    if (schema.isEmpty()) {
      return null;
    } else {
      return schema.get(0);
    }
  }

  private void updateCountsForNonExistingRequestTypes(
      Map<String, Long> operationTypeCountsMap, Map<String, Long> statusCountsMap) {
    for (RequestStatus requestStatus : RequestStatus.values()) {
      if (!statusCountsMap.containsKey(requestStatus.value))
        statusCountsMap.put(requestStatus.value, 0L);
    }
    for (RequestOperationType requestOperationType : RequestOperationType.values()) {
      if (!operationTypeCountsMap.containsKey(requestOperationType.value))
        operationTypeCountsMap.put(requestOperationType.value, 0L);
    }
  }

  private void updateMap(Map<String, Long> countsMap, List<Object[]> requestObjList) {
    requestObjList.forEach(reqObj -> countsMap.put((String) reqObj[0], (Long) reqObj[1]));
  }

  public List<TopicRequest> getAllTopicRequestsByTenantId(int tenantId) {
    return Lists.newArrayList(topicRequestsRepo.findAllByTenantId(tenantId));
  }

  public List<KafkaConnectorRequest> getAllConnectorRequestsByTenantId(int tenantId) {
    return Lists.newArrayList(kafkaConnectorRequestsRepo.findAllByTenantId(tenantId));
  }

  public Optional<MessageSchema> getFirstSchemaForTenantAndEnvAndTopicAndVersion(
      int tenantId, String schemaEnvId, String topicName, String schemaVersion) {
    return messageSchemaRepo.findFirstByTenantIdAndEnvironmentAndTopicnameAndSchemaversion(
        tenantId, schemaEnvId, topicName, schemaVersion);
  }

  public List<MessageSchema> getSchemaForTenantAndEnvAndTopic(
      int tenantId, String schemaEnvId, String topicName) {
    return messageSchemaRepo.findAllByTenantIdAndTopicnameAndEnvironment(
        tenantId, topicName, schemaEnvId);
  }

  public List<KwClusters> getClusters() {
    return Lists.newArrayList(kwClusterRepo.findAll());
  }

  public List<Env> selectEnvs() {
    return Lists.newArrayList(envRepo.findAll());
  }

  public List<Team> selectTeams() {
    return Lists.newArrayList(teamRepo.findAll());
  }

  public List<KwProperties> selectKwProperties() {
    return Lists.newArrayList(kwPropertiesRepo.findAll());
  }

  public List<Topic> getAllTopics() {
    return Lists.newArrayList(topicRepo.findAll());
  }

  public List<Acl> getAllSubscriptions() {
    return Lists.newArrayList(aclRepo.findAll());
  }

  public List<MessageSchema> selectAllSchemas() {
    return Lists.newArrayList(messageSchemaRepo.findAll());
  }

  public List<KwKafkaConnector> getAllConnectors() {
    return Lists.newArrayList(kafkaConnectorRepo.findAll());
  }

  public List<ActivityLog> getAllActivityLog() {
    return Lists.newArrayList(activityLogRepo.findAll());
  }

  public List<TopicRequest> getAllTopicRequests() {
    return Lists.newArrayList(topicRequestsRepo.findAll());
  }

  public List<AclRequests> getAllAclRequests() {
    return Lists.newArrayList(aclRequestsRepo.findAll());
  }

  public List<SchemaRequest> getAllSchemaRequests() {
    return Lists.newArrayList(schemaRequestRepo.findAll());
  }

  public List<KafkaConnectorRequest> getAllConnectorRequests() {
    return Lists.newArrayList(kafkaConnectorRequestsRepo.findAll());
  }

  public Integer getNextClusterId(int tenantId) {
    return kwClusterRepo.getNextClusterId(tenantId);
  }

  public Integer getNextEnvId(int tenantId) {
    return envRepo.getNextId(tenantId);
  }

  public Integer getNextTeamId(int tenantId) {
    return teamRepo.getNextTeamId(tenantId);
  }

  public long getDataFromKwEntitySequences() {
    return kwEntitySequenceRepo.count();
  }

  public boolean existsSchemaRequest(
      String topicName, String requestStatus, String env, int tenantId) {
    return schemaRequestRepo.existsByTenantIdAndEnvironmentAndRequestStatusAndTopicname(
        tenantId, env, requestStatus, topicName);
  }

  public boolean existsSchemaRequest(
      String topicName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId) {

    return schemaRequestRepo
        .existsByTenantIdAndEnvironmentAndRequestStatusAndRequestOperationTypeAndTopicname(
            tenantId, env, requestStatus, requestOperationType, topicName);
  }

  public boolean existsTopicRequest(
      String topicName, String requestStatus, String env, int tenantId) {
    return topicRequestsRepo.existsByTenantIdAndEnvironmentAndRequestStatusAndTopicname(
        tenantId, env, requestStatus, topicName);
  }

  public boolean existsTopicRequest(
      String topicName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId) {
    return topicRequestsRepo
        .existsByTenantIdAndEnvironmentAndRequestStatusAndRequestOperationTypeAndTopicname(
            tenantId, env, requestStatus, requestOperationType, topicName);
  }

  public boolean existsClaimTopicRequest(
      String topicName, String requestStatus, String requestOperationType, int tenantId) {
    return topicRequestsRepo.existsByTenantIdAndRequestStatusAndRequestOperationTypeAndTopicname(
        tenantId, requestStatus, requestOperationType, topicName);
  }

  public boolean existsConnectorRequest(
      String connectorName, String requestStatus, String env, int tenantId) {
    return kafkaConnectorRequestsRepo
        .existsByTenantIdAndEnvironmentAndRequestStatusAndConnectorName(
            tenantId, env, requestStatus, connectorName);
  }

  public boolean existsConnectorRequest(
      String connectorName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId) {
    return kafkaConnectorRequestsRepo
        .existsByTenantIdAndEnvironmentAndRequestStatusAndRequestOperationTypeAndConnectorName(
            tenantId, env, requestStatus, requestOperationType, connectorName);
  }

  public boolean existsConnectorRequestOnAnyEnv(
      String connectorName, String requestStatus, int tenantId) {
    return kafkaConnectorRequestsRepo.existsByTenantIdAndRequestStatusAndConnectorName(
        tenantId, requestStatus, connectorName);
  }

  public boolean existsTopicRequestOnAnyEnv(String topicName, String requestStatus, int tenantId) {
    return topicRequestsRepo.existsByTenantIdAndRequestStatusAndTopicname(
        tenantId, requestStatus, topicName);
  }

  public boolean existsClaimConnectorRequest(
      String connectorName, String requestStatus, int tenantId) {
    return kafkaConnectorRequestsRepo
        .existsByTenantIdAndRequestStatusAndRequestOperationTypeAndConnectorName(
            tenantId, requestStatus, RequestOperationType.CLAIM.value, connectorName);
  }

  public boolean existsSchemaForTopic(String topicName, String env, int tenantId) {
    return messageSchemaRepo.existsByTenantIdAndTopicnameAndEnvironment(tenantId, topicName, env);
  }

  public OperationalRequest selectOperationalRequest(int reqId, int tenantId) {
    log.debug("selectOperationalRequest {}", reqId);
    OperationalRequestID operationalRequestID = new OperationalRequestID();
    operationalRequestID.setReqId(reqId);
    operationalRequestID.setTenantId(tenantId);

    Optional<OperationalRequest> operationalRequest =
        operationalRequestsRepo.findById(operationalRequestID);
    return operationalRequest.orElse(null);
  }

  public List<OperationalRequest> selectFilteredOperationalRequests(
      boolean isApproval,
      String requestor,
      String requestStatus,
      boolean showRequestsOfAllTeams,
      int tenantId,
      Integer teamId,
      OperationalRequestType operationalRequestType,
      String env,
      String topicName,
      String consumerGroup,
      String wildcardSearch,
      boolean isMyRequest) {
    if (log.isDebugEnabled()) {
      log.debug(
          "selectFilteredOperationalRequests {} {} {} {} {} {} {} {}",
          showRequestsOfAllTeams,
          requestor,
          requestStatus,
          teamId,
          env,
          topicName,
          consumerGroup,
          wildcardSearch);
    }
    Integer teamSelected = selectUserInfo(requestor).getTeamId();
    List<OperationalRequest> operationalRequests = new ArrayList<>();
    List<OperationalRequest> operationRequestListSub;
    if (isApproval) { // approvers
      // On Approvals this should always be filtered by the users current team
      operationRequestListSub =
          Lists.newArrayList(
              findOperationalRequestsByExample(
                  operationalRequestType,
                  showRequestsOfAllTeams ? null : teamSelected,
                  env,
                  topicName,
                  consumerGroup,
                  requestStatus,
                  tenantId,
                  null,
                  isMyRequest ? requestor : null));

      // remove users own requests to approve/show in the list
      // Placed here as it should only apply for approvers.
      operationRequestListSub =
          operationRequestListSub.stream()
              .filter(topicRequest -> !topicRequest.getRequestor().equals(requestor))
              .toList();
    } else {
      if (showRequestsOfAllTeams) {
        operationRequestListSub =
            Lists.newArrayList(
                findOperationalRequestsByExample(
                    operationalRequestType,
                    null,
                    env,
                    topicName,
                    consumerGroup,
                    requestStatus,
                    tenantId,
                    null,
                    isMyRequest ? requestor : null));
      } else {
        operationRequestListSub =
            Lists.newArrayList(
                findOperationalRequestsByExample(
                    operationalRequestType,
                    teamSelected,
                    env,
                    topicName,
                    consumerGroup,
                    requestStatus,
                    tenantId,
                    null,
                    isMyRequest ? requestor : null));
      }
    }

    if (teamId != null) {
      operationRequestListSub =
          operationRequestListSub.stream()
              .filter(topicRequest -> topicRequest.getRequestingTeamId().equals(teamId))
              .toList();
    }

    boolean wildcardFilter = (wildcardSearch != null && !wildcardSearch.isEmpty());

    for (OperationalRequest row : operationRequestListSub) {
      try {
        row.setRequesttimestring(
            DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(row.getRequesttime().toLocalDateTime()));
      } catch (Exception ignored) {
      }
      filterAndAddOperationalRequest(operationalRequests, row, wildcardSearch, wildcardFilter);
    }

    return operationalRequests;
  }

  private Iterable<OperationalRequest> findOperationalRequestsByExample(
      OperationalRequestType operationalRequestType,
      Integer teamId,
      String environment,
      String topicName,
      String consumerGroup,
      String requestStatus,
      int tenantId,
      String approvingTeam,
      String userName) {

    OperationalRequest request = new OperationalRequest();
    request.setTenantId(tenantId);
    if (topicName != null) {
      request.setTopicname(topicName);
    }
    if (consumerGroup != null) {
      request.setConsumerGroup(consumerGroup);
    }
    if (operationalRequestType != null) {
      request.setOperationalRequestType(operationalRequestType);
    }
    if (environment != null) {
      request.setEnvironment(environment);
    }
    if (teamId != null) {
      request.setRequestingTeamId(teamId);
    }

    if (approvingTeam != null && !approvingTeam.isEmpty()) {
      request.setApprovingTeamId(approvingTeam);
    }

    if (requestStatus != null && !requestStatus.equalsIgnoreCase("all")) {
      request.setRequestStatus(requestStatus);
    }
    // userName is transient and so not available in the database to be queried.
    if (userName != null && !userName.isEmpty()) {
      request.setRequestor(userName);
    }

    // check if debug is enabled so the logger doesn't waste resources converting object request to
    // a
    // string
    if (log.isDebugEnabled()) {
      log.debug("findOperationalRequestsByExample {}", request);
    }

    return operationalRequestsRepo.findAll(Example.of(request));
  }
}
