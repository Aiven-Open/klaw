package io.aiven.klaw.helpers.db.rdbms;

import com.google.common.collect.Lists;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.AclRepo;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.ActivityLogRepo;
import io.aiven.klaw.repository.EnvRepo;
import io.aiven.klaw.repository.KwClusterRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.KwMetricsRepo;
import io.aiven.klaw.repository.KwPropertiesRepo;
import io.aiven.klaw.repository.KwRolesPermsRepo;
import io.aiven.klaw.repository.MessageSchemaRepo;
import io.aiven.klaw.repository.ProductDetailsRepo;
import io.aiven.klaw.repository.RegisterInfoRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TenantRepo;
import io.aiven.klaw.repository.TopicRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SelectDataJdbc {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

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

  public List<AclRequests> selectAclRequests(
      boolean allReqs,
      String requestor,
      String role,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId) {
    return selectAclRequests(
        allReqs, requestor, role, status, showRequestsOfAllTeams, null, null, null, tenantId);
  }

  public List<AclRequests> selectAclRequests(
      boolean allReqs,
      String requestor,
      String role,
      String status,
      boolean showRequestsOfAllTeams,
      String topic,
      String environment,
      AclType aclType,
      int tenantId) {
    log.debug("selectAclRequests {}", requestor);
    List<AclRequests> aclList = new ArrayList<>();
    List<AclRequests> aclListSub;

    aclListSub =
        Lists.newArrayList(findAclRequestsByExample(topic, environment, aclType, status, tenantId));
    if (allReqs) {
      // Only filter when returning to approvers view.
      // in the acl request the username is mapped to the requestor column in the database.
      aclListSub =
          aclListSub.stream()
              .filter(req -> !req.getUsername().equals(requestor))
              .collect(Collectors.toList());
    }
    Integer teamSelected = selectUserInfo(requestor).getTeamId();

    for (AclRequests row : aclListSub) {
      Integer teamName;
      String rowAclType = row.getAclType();
      if (allReqs) {
        if ("requestor_subscriptions".equals(role)) {
          teamName = row.getRequestingteam();
        } else {
          teamName = row.getTeamId();
        }

        if (RequestOperationType.DELETE.value.equals(rowAclType)) {
          teamName = row.getRequestingteam();
        }

      } else {
        teamName = row.getRequestingteam();
      }

      if (showRequestsOfAllTeams) { // show all requests of all teams
        aclList.add(row);
      } else if (teamSelected != null && teamSelected.equals(teamName)) {
        aclList.add(row);
      }

      try {
        row.setRequesttimestring(
            (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                .format((row.getRequesttime()).getTime()));
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
   * @param status created/declined/approved
   * @param tenantId The tenantId
   * @return
   */
  public Iterable<AclRequests> findAclRequestsByExample(
      String topic, String environment, AclType aclType, String status, int tenantId) {

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
    if (status != null && !status.equalsIgnoreCase("all")) {
      request.setAclstatus(status);
    }
    // check if debug is enabled so the logger doesnt waste resources converting object request to a
    // string
    if (log.isDebugEnabled()) {
      log.debug("find By topic etc example {}", request);
    }
    return aclRequestsRepo.findAll(Example.of(request));
  }

  public List<SchemaRequest> selectSchemaRequests(boolean allReqs, String requestor, int tenantId) {
    log.debug("selectSchemaRequests {}", requestor);
    List<SchemaRequest> schemaList = new ArrayList<>();
    List<SchemaRequest> schemaListSub;

    if (allReqs) {
      schemaListSub = schemaRequestRepo.findAllByTopicstatusAndTenantId("created", tenantId);
    } else {
      schemaListSub = Lists.newArrayList(schemaRequestRepo.findAllByTenantId(tenantId));
    }

    for (SchemaRequest row : schemaListSub) {
      Integer teamId = row.getTeamId();
      Integer teamSelected = selectUserInfo(requestor).getTeamId();
      try {
        row.setRequesttimestring(
            (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                .format((row.getRequesttime()).getTime()));
      } catch (Exception ignored) {
      }

      if (teamSelected != null && teamSelected.equals(teamId)) {
        schemaList.add(row);
      }
    }

    return schemaList;
  }

  public SchemaRequest selectSchemaRequest(int avroSchemaId, int tenantId) {
    log.debug("selectSchemaRequest {}", avroSchemaId);
    SchemaRequestID schemaRequestID = new SchemaRequestID(avroSchemaId, tenantId);
    Optional<SchemaRequest> sR = schemaRequestRepo.findById(schemaRequestID);
    return sR.orElse(null);
  }

  public List<Topic> selectTopicDetails(String topic, int tenantId) {
    List<Topic> topicOpt = topicRepo.findAllByTopicnameAndTenantId(topic, tenantId);

    if (topicOpt.size() > 0) {
      return topicOpt;
    } else {
      return new ArrayList<>();
    }
  }

  public List<KwKafkaConnector> selectConnectorDetails(String connectorName, int tenantId) {
    List<KwKafkaConnector> topicOpt =
        kafkaConnectorRepo.findAllByConnectorNameAndTenantId(connectorName, tenantId);

    if (topicOpt.size() > 0) {
      return topicOpt;
    } else {
      return new ArrayList<>();
    }
  }

  // "All teams"
  // teamId 1 is All teams
  public List<Topic> selectSyncTopics(String env, Integer teamId, int tenantId) {
    log.debug("selectSyncTopics {}", teamId);
    if (teamId == null || teamId.equals(1)) {
      if (env == null || env.equals("ALL")) {
        return topicRepo.findAllByTenantId(tenantId);
      } else {
        return topicRepo.findAllByEnvironmentAndTenantId(env, tenantId);
      }
    } else {
      if ("ALL".equals(env)) {
        return topicRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
      } else {
        return topicRepo.findAllByEnvironmentAndTeamIdAndTenantId(env, teamId, tenantId);
      }
    }
  }

  public Map<String, String> getDashboardStats(Integer teamId, int tenantId) {
    Map<String, String> dashboardMap = new HashMap<>();
    int countProducers = 0, countConsumers = 0;
    List<Acl> acls =
        aclRepo.findAllByTopictypeAndTeamIdAndTenantId(AclType.PRODUCER.value, teamId, tenantId);
    List<String> topicList = new ArrayList<>();
    if (acls != null) {
      acls.forEach(a -> topicList.add(a.getTopicname()));
      countProducers = (int) topicList.stream().distinct().count();
    }
    dashboardMap.put("producerCount", "" + countProducers);

    acls = aclRepo.findAllByTopictypeAndTeamIdAndTenantId(AclType.CONSUMER.value, teamId, tenantId);
    List<String> topicListCons = new ArrayList<>();
    if (acls != null) {
      acls.forEach(a -> topicListCons.add(a.getTopicname()));
      countConsumers = (int) topicListCons.stream().distinct().count();
    }
    dashboardMap.put("consumerCount", "" + countConsumers);

    List<UserInfo> allUsers = userInfoRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
    dashboardMap.put("teamMembersCount", "" + allUsers.size());

    return dashboardMap;
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

    List<Acl> acls = aclRepo.findAllByTopictypeAndTeamIdAndTenantId(topicType, teamId, tenantId);
    Topic t;
    Map<String, List<String>> topicEnvMap = new HashMap<>();
    String tmpTopicName;
    List<String> envList;

    for (Acl acl : acls) {
      t = new Topic();
      tmpTopicName = acl.getTopicname();
      aclPatternType = acl.getAclPatternType();

      if (aclPatternType != null && aclPatternType.equals(AclPatternType.PREFIXED.value))
        tmpTopicName = tmpTopicName + "--" + AclPatternType.PREFIXED.value + "--";

      t.setEnvironment(acl.getEnvironment());
      t.setTopicname(tmpTopicName);

      if (topicEnvMap.containsKey(tmpTopicName)) {
        envList = topicEnvMap.get(tmpTopicName);
        if (!envList.contains(acl.getEnvironment())) {
          envList.add(acl.getEnvironment());
          topicEnvMap.put(tmpTopicName, envList);
        }
      } else {
        envList = new ArrayList<>();
        envList.add(acl.getEnvironment());
        topicEnvMap.put(tmpTopicName, envList);
      }

      topics.add(t);
    }

    for (Topic topic : topics) {
      topic.setEnvironmentsList(topicEnvMap.get(topic.getTopicname()));
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

  public List<TopicRequest> selectTopicRequestsByStatus(
      boolean allReqs,
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      String teamName,
      String env,
      String wildcardSearch) {
    log.debug(
        "selectTopicRequests {} {} {} {} {}", requestor, status, teamName, env, wildcardSearch);

    Integer teamSelected = selectUserInfo(requestor).getTeamId();
    List<TopicRequest> topicRequests = new ArrayList<>();
    List<TopicRequest> topicRequestListSub;

    if (allReqs) { // approvers

      List<TopicRequest> claimTopicReqs =
          Lists.newArrayList(
              findTopicRequestsByExample(
                  RequestOperationType.CLAIM.name(),
                  teamName,
                  env,
                  status,
                  tenantId,
                  String.valueOf(teamSelected)));

      topicRequestListSub =
          Lists.newArrayList(
              findTopicRequestsByExample(null, teamName, env, status, tenantId, null));

      // Only execute just before adding the separate claim list as this will make sure only the
      // claim topics this team is able to approve will be returned.
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(
                  topicRequest ->
                      !RequestOperationType.CLAIM.name().equals(topicRequest.getTopictype()))
              .collect(Collectors.toList());

      topicRequestListSub.addAll(claimTopicReqs);
      // remove users own requests to approve/show in the list
      // Placed here as it should only apply for approvers.
      topicRequestListSub =
          topicRequestListSub.stream()
              .filter(topicRequest -> !topicRequest.getRequestor().equals(requestor))
              .collect(Collectors.toList());
    } else {
      topicRequestListSub =
          Lists.newArrayList(findTopicRequestsByExample(null, null, null, null, tenantId, null));
    }

    boolean wildcardFilter = (allReqs && wildcardSearch != null && !wildcardSearch.isEmpty());

    for (TopicRequest row : topicRequestListSub) {
      Integer teamId = row.getTeamId();
      try {
        row.setRequesttimestring(
            (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                .format((row.getRequesttime()).getTime()));
      } catch (Exception ignored) {
      }

      if (showRequestsOfAllTeams) {
        filterAndAddTopicRequest(
            topicRequests, row, wildcardSearch, wildcardFilter); // no team filter
      } else if (teamSelected != null
          && (teamSelected.equals(teamId) || ("" + teamSelected).equals(row.getDescription()))) {
        filterAndAddTopicRequest(topicRequests, row, wildcardSearch, wildcardFilter);
      }
    }

    return topicRequests;
  }

  private void filterAndAddTopicRequest(
      List<TopicRequest> topicRequestList,
      TopicRequest row,
      String wildcardSearch,
      boolean applyWildcardFilter) {
    if (!applyWildcardFilter || row.getTopicname().contains(wildcardSearch)) {
      topicRequestList.add(row);
    }
  }

  /**
   * Query the TopicRequestsRepo by supplying optional search parameters any given search parameters
   * will be utilised in the search.
   *
   * @param requestType The type of topic request Create/claim etc
   * @param teamName The identifier of the team
   * @param environment the environment
   * @param status created/declined/approved
   * @param tenantId The tenantId
   * @return An Iterable of all TopicRequests that match the parameters that have been supplied
   */
  private Iterable<TopicRequest> findTopicRequestsByExample(
      String requestType,
      String teamName,
      String environment,
      String status,
      int tenantId,
      String description) {

    TopicRequest request = new TopicRequest();
    request.setTenantId(tenantId);

    if (requestType != null) {
      request.setTopictype(requestType);
    }
    if (environment != null) {
      request.setEnvironment(environment);
    }
    if (teamName != null) {
      List<Team> team = teamRepo.findAllByTenantIdAndTeamname(tenantId, teamName);
      if (team.size() == 1) {
        request.setTeamId(team.get(0).getTeamId());
      } else {
        log.warn(
            "findTopicRequestsByExample({},{},{},{},{}) returned {} number of teams, so the filtering parameter was not applied.",
            requestType,
            teamName,
            environment,
            status,
            tenantId,
            team.size());
      }
    }
    if (description != null && !description.isEmpty()) {
      request.setDescription(description);
    }

    if (status != null && !status.equalsIgnoreCase("all")) {
      request.setTopicstatus(status);
    }
    // check if debug is enabled so the logger doesn't waste resources converting object request to
    // a
    // string
    if (log.isDebugEnabled()) {
      log.debug("find By topic etc example {}", request);
    }

    return topicRequestsRepo.findAll(Example.of(request));
  }

  public List<KafkaConnectorRequest> selectConnectorRequestsByStatus(
      boolean allReqs,
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId) {
    log.debug("selectConnectorRequestsByStatus {} {}", requestor, status);
    List<KafkaConnectorRequest> topicRequestList = new ArrayList<>();

    List<KafkaConnectorRequest> topicRequestListSub;
    Integer teamSelected = selectUserInfo(requestor).getTeamId();

    if (allReqs) { // approvers
      List<KafkaConnectorRequest> claimTopicReqs =
          kafkaConnectorRequestsRepo.findAllByConnectortypeAndTenantId(
              RequestOperationType.CLAIM.value, tenantId);

      if ("all".equals(status)) {
        topicRequestListSub =
            Lists.newArrayList(kafkaConnectorRequestsRepo.findAllByTenantId(tenantId));
        topicRequestListSub =
            topicRequestListSub.stream()
                .filter(
                    topicRequest ->
                        !RequestOperationType.CLAIM.value.equals(topicRequest.getConnectortype()))
                .collect(Collectors.toList());

        claimTopicReqs =
            claimTopicReqs.stream()
                .filter(
                    claimTopicReq ->
                        Objects.equals(claimTopicReq.getDescription(), "" + teamSelected))
                .collect(Collectors.toList());
      } else {
        topicRequestListSub =
            kafkaConnectorRequestsRepo.findAllByConnectorStatusAndTenantId(status, tenantId);
        topicRequestListSub =
            topicRequestListSub.stream()
                .filter(
                    topicRequest ->
                        !RequestOperationType.CLAIM.value.equals(topicRequest.getConnectortype()))
                .collect(Collectors.toList());

        claimTopicReqs =
            claimTopicReqs.stream()
                .filter(
                    claimTopicReq ->
                        Objects.equals(claimTopicReq.getDescription(), "" + teamSelected)
                            && Objects.equals(claimTopicReq.getConnectorStatus(), status))
                .collect(Collectors.toList());
      }

      topicRequestListSub.addAll(claimTopicReqs);
    } else {
      topicRequestListSub =
          Lists.newArrayList(kafkaConnectorRequestsRepo.findAllByTenantId(tenantId));
    }

    for (KafkaConnectorRequest row : topicRequestListSub) {
      Integer teamId = row.getTeamId();
      try {
        row.setRequesttimestring(
            (new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                .format((row.getRequesttime()).getTime()));
      } catch (Exception ignored) {
      }

      if (showRequestsOfAllTeams) {
        topicRequestList.add(row); // no team filter
      } else if (teamSelected != null
          && (Objects.equals(teamSelected, teamId)
              || Objects.equals("" + teamSelected, row.getDescription()))) {
        topicRequestList.add(row);
      }
    }

    return topicRequestList;
  }

  public TopicRequest selectTopicRequestsForTopic(int topicId, int tenantId) {
    log.debug("selectTopicRequestsForTopic {}", topicId);
    TopicRequestID topicRequestID = new TopicRequestID();
    topicRequestID.setTenantId(tenantId);
    topicRequestID.setTopicid(topicId);
    Optional<TopicRequest> topicReq = topicRequestsRepo.findById(topicRequestID);
    return topicReq.orElse(null);
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
    return userInfoRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
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
          DATE_TIME_FORMATTER.format(row.getActivityTime().toLocalDateTime()));
    }

    return activityList;
  }

  public List<Team> selectTeamsOfUsers(String username, int tenantId) {
    log.debug("selectTeamsOfUsers {}", username);
    List<Team> allTeams = selectAllTeams(tenantId);

    List<Team> teamList = new ArrayList<>();
    List<UserInfo> userInfoList = Lists.newArrayList(userInfoRepo.findAllByTenantId(tenantId));
    Integer teamId;

    for (UserInfo row : userInfoList) {
      teamId = row.getTeamId();

      Integer finalTeamId = teamId;
      Optional<Team> teamSel =
          allTeams.stream().filter(a -> Objects.equals(a.getTeamId(), finalTeamId)).findFirst();

      if (Objects.equals(username, row.getUsername())) {
        teamSel.ifPresent(teamList::add);
      }
    }

    return teamList;
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

  public List<RegisterUserInfo> selectAllRegisterUsersInfoForTenant(int tenantId) {
    return registerInfoRepo.findAllByStatusAndTenantId("PENDING", tenantId);
  }

  public List<RegisterUserInfo> selectAllRegisterUsersInfo() {
    return registerInfoRepo.findAllByStatus("PENDING");
  }

  public List<RegisterUserInfo> selectAllStagingRegisterUsersInfo(String userId) {
    return registerInfoRepo.findAllByUsernameAndStatus(userId, "STAGING");
  }

  public RegisterUserInfo selectRegisterUsersInfo(String username) {
    Optional<RegisterUserInfo> registerUserRec = registerInfoRepo.findById(username);
    return registerUserRec.orElse(null);
  }

  public List<Acl> getUniqueConsumerGroups(int tenantId) {
    return aclRepo.findAllByTenantId(tenantId);
  }

  public Team selectTeamDetails(Integer teamId, int tenantId) {
    TeamID teamID = new TeamID(teamId, tenantId);
    Optional<Team> teamList = teamRepo.findById(teamID);
    return teamList.orElse(null);
  }

  public Team selectTeamDetailsFromName(String teamName, int tenantId) {
    List<Team> teamList = teamRepo.findAllByTenantIdAndTeamname(tenantId, teamName);
    if (!teamList.isEmpty()) {
      return teamList.get(0);
    } else {
      return null;
    }
  }

  public List<Map<String, String>> selectActivityLogByTeam(
      Integer teamId, int numberOfDays, int tenantId) {
    List<Map<String, String>> totalActivityLogCount = new ArrayList<>();
    try {
      List<Object[]> activityCount = activityLogRepo.findActivityLogForTeamId(teamId, tenantId);
      if (activityCount.size() > numberOfDays)
        activityCount = activityCount.subList(0, numberOfDays - 1);
      Map<String, String> hashMap;
      for (Object[] actvty : activityCount) {
        hashMap = new HashMap<>();
        hashMap.put("dateofactivity", "" + actvty[0]);
        hashMap.put("activitycount", "" + ((Long) actvty[1]).intValue());

        totalActivityLogCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error selectActivityLogForLastDays ", e);
    }
    return totalActivityLogCount;
  }

  public List<Map<String, String>> selectActivityLogForLastDays(
      int numberOfDays, String[] envIdList, int tenantId) {
    List<Map<String, String>> totalActivityLogCount = new ArrayList<>();
    try {
      List<Object[]> activityCount =
          activityLogRepo.findActivityLogForLastDays(envIdList, tenantId);
      if (activityCount.size() > numberOfDays) {
        activityCount = activityCount.subList(0, numberOfDays - 1);
      }
      Map<String, String> hashMap;
      for (Object[] actvty : activityCount) {
        hashMap = new HashMap<>();
        hashMap.put("dateofactivity", "" + actvty[0]);
        hashMap.put("activitycount", "" + ((Long) actvty[1]).intValue());

        totalActivityLogCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error selectActivityLogForLastDays ", e);
    }
    return totalActivityLogCount;
  }

  public List<Map<String, String>> selectTopicsCountByEnv(Integer tenantId) {
    List<Map<String, String>> totalTopicCount = new ArrayList<>();
    try {
      List<Object[]> topics = topicRepo.findAllTopicsGroupByEnv(tenantId);

      Map<String, String> hashMap;
      for (Object[] topic : topics) {
        hashMap = new HashMap<>();
        hashMap.put("cluster", (String) topic[0]);
        hashMap.put("topicscount", "" + ((Long) topic[1]).intValue());

        totalTopicCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error selectTopicsCountByEnv ", e);
    }
    return totalTopicCount;
  }

  public List<Map<String, String>> selectPartitionsCountByEnv(Integer teamId, Integer tenantId) {
    List<Map<String, String>> totalPartitionsCount = new ArrayList<>();
    try {
      List<Object[]> topics;
      if (teamId != null) {
        topics = topicRepo.findAllPartitionsForTeamGroupByEnv(teamId, tenantId);
      } else {
        topics = topicRepo.findAllPartitionsGroupByEnv(tenantId);
      }

      Map<String, String> hashMap;
      for (Object[] topic : topics) {
        hashMap = new HashMap<>();
        hashMap.put("cluster", (String) topic[0]);
        hashMap.put("partitionscount", "" + topic[1]);

        totalPartitionsCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error from selectPartitionsCountByEnv ", e);
    }
    return totalPartitionsCount;
  }

  public List<Map<String, String>> selectAclsCountByEnv(Integer teamId, Integer tenantId) {
    List<Map<String, String>> totalAclsCount = new ArrayList<>();
    try {
      List<Object[]> topics;
      if (teamId != null) {
        topics = aclRepo.findAllAclsforTeamGroupByEnv(teamId, tenantId);
      } else {
        topics = aclRepo.findAllAclsGroupByEnv(tenantId);
      }

      Map<String, String> hashMap;
      for (Object[] topic : topics) {
        hashMap = new HashMap<>();
        hashMap.put("cluster", (String) topic[0]);
        hashMap.put("aclscount", "" + ((Long) topic[1]).intValue());

        totalAclsCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error selectAclsCountByEnv", e);
    }
    return totalAclsCount;
  }

  public List<Map<String, String>> selectTopicsCountByTeams(Integer teamId, int tenantId) {
    List<Map<String, String>> totalTopicCount = new ArrayList<>();
    try {
      List<Object[]> topics;
      if (teamId != null) {
        topics = topicRepo.findAllTopicsForTeam(teamId, tenantId);
      } else {
        topics = topicRepo.findAllTopicsGroupByTeamId(tenantId);
      }

      Map<String, String> hashMap;
      for (Object[] topic : topics) {
        hashMap = new HashMap<>();
        if (teamId != null) {
          hashMap.put("teamid", "" + teamId);
          hashMap.put("topicscount", "" + ((Long) topic[0]).intValue());
        } else {
          hashMap.put("teamid", "" + topic[0]);
          hashMap.put("topicscount", "" + ((Long) topic[1]).intValue());
        }

        totalTopicCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error from selectTopicsCountByTeams ", e);
    }
    return totalTopicCount;
  }

  public List<Map<String, String>> selectAclsCountByTeams(
      String aclType, Integer teamId, Integer tenantId) {
    List<Map<String, String>> totalAclCount = new ArrayList<>();
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

      Map<String, String> hashMap;
      for (Object[] topic : acls) {
        hashMap = new HashMap<>();
        if (teamId != null) {
          hashMap.put("teamid", "" + teamId);
          hashMap.put("aclscount", "" + ((Long) topic[0]).intValue());
        } else {
          hashMap.put("teamid", "" + topic[0]);
          hashMap.put("aclscount", "" + ((Long) topic[1]).intValue());
        }

        totalAclCount.add(hashMap);
      }
    } catch (Exception e) {
      log.error("Error from selectAclsCountByTeams ", e);
    }
    return totalAclCount;
  }

  public List<Map<String, String>> selectAllTopicsForTeamGroupByEnv(Integer teamId, int tenantId) {
    List<Map<String, String>> totalTopicCount = new ArrayList<>();
    try {
      List<Object[]> topics = topicRepo.findAllTopicsForTeamGroupByEnv(teamId, tenantId);

      Map<String, String> hashMap;
      for (Object[] topic : topics) {
        hashMap = new HashMap<>();
        if (selectEnvDetails((String) topic[0], tenantId) != null) {
          hashMap.put("cluster", selectEnvDetails((String) topic[0], tenantId).getName());
          hashMap.put("topicscount", "" + ((Long) topic[1]).intValue());
          totalTopicCount.add(hashMap);
        } else {
          log.error("Error: Environment not found for env {}", topic[0]);
        }
      }
    } catch (Exception e) {
      log.error("Error from selectAllTopicsForTeamGroupByEnv ", e);
    }
    return totalTopicCount;
  }

  public List<Map<String, String>> selectAllMetrics(
      String metricsType, String metricsName, String env) {
    List<Map<String, String>> metricsCount = new ArrayList<>();
    try {
      List<Object[]> metrics =
          kwMetricsRepo.findAllByEnvAndMetricsTypeAndMetricsName(env, metricsType, metricsName);

      Map<String, String> hashMap;
      for (Object[] kwMetrics : metrics) {
        hashMap = new HashMap<>();
        hashMap.put("datetime", (String) kwMetrics[0]);
        hashMap.put("messagescount", (String) kwMetrics[1]);
        metricsCount.add(hashMap);
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

    for (KwTenants tenant : tenants) {
      List<KwProperties> kwProps = kwPropertiesRepo.findAllByTenantId(tenant.getTenantId());

      Map<String, Map<String, String>> fullMap = new HashMap<>();
      for (KwProperties kwProp : kwProps) {
        mapProps = new HashMap<>();
        mapProps.put("kwkey", kwProp.getKwKey());
        mapProps.put("kwvalue", kwProp.getKwValue());
        mapProps.put("kwdesc", kwProp.getKwDesc());
        mapProps.put("kwtenantid", kwProp.getTenantId() + "");

        fullMap.put(kwProp.getKwKey(), mapProps);
      }
      tenantProps.put(tenant.getTenantId(), fullMap);
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

  public KwClusters getClusterDetails(int id, int tenantId) {
    KwClusterID kwClusterID = new KwClusterID();
    kwClusterID.setClusterId(id);
    kwClusterID.setTenantId(tenantId);

    Optional<KwClusters> kwCluster = kwClusterRepo.findById(kwClusterID);
    return kwCluster.orElse(null);
  }

  public List<TopicRequest> selectTopicRequests(
      String topicName, String envId, String status, int tenantId) {
    return topicRequestsRepo.findAllByTopicstatusAndTopicnameAndEnvironmentAndTenantId(
        status, topicName, envId, tenantId);
  }

  public List<KafkaConnectorRequest> selectConnectorRequests(
      String connectorName, String envId, String status, int tenantId) {
    return kafkaConnectorRequestsRepo
        .findAllByConnectorStatusAndConnectorNameAndEnvironmentAndTenantId(
            status, connectorName, envId, tenantId);
  }

  public String getRegistrationId(String userId) {
    List<RegisterUserInfo> registerInfoList =
        registerInfoRepo.findAllByUsernameAndStatus(userId, "STAGING");
    List<RegisterUserInfo> registerInfoList1 =
        registerInfoRepo.findAllByUsernameAndStatus(userId, "PENDING");
    if (registerInfoList.size() > 0) {
      return registerInfoList.get(0).getRegistrationId();
    } else if (registerInfoList1.size() > 0) {
      return "PENDING_ACTIVATION";
    } else {
      return null;
    }
  }

  public RegisterUserInfo getRegistrationDetails(String registrationId, String status) {
    List<RegisterUserInfo> registerUserInfoList;
    if ("".equals(status)) {
      registerUserInfoList = registerInfoRepo.findAllByRegistrationId(registrationId);
    } else {
      registerUserInfoList =
          registerInfoRepo.findAllByRegistrationIdAndStatus(registrationId, status);
    }
    if (registerUserInfoList.size() == 1) {
      return registerUserInfoList.get(0);
    }
    return null;
  }

  public List<Topic> getTopicsforTeam(Integer teamId, int tenantId) {
    return topicRepo.findAllByTeamIdAndTenantId(teamId, tenantId);
  }

  public List<Acl> getConsumerGroupsforTeam(Integer teamId, int tenantId) {
    return aclRepo.findAllByTopictypeAndTeamIdAndTenantId(
        AclType.CONSUMER.name(), teamId, tenantId);
  }

  public List<Acl> getAllConsumerGroups(int tenantId) {
    return aclRepo.findAllByTopictypeAndTenantId(AclType.CONSUMER.name(), tenantId);
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
      if ("ALL".equals(env)) {
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

  public int findAllKafkaComponentsCountForEnv(String env, int tenantId) {
    return ((Long) topicRepo.findAllTopicsCountForEnv(env, tenantId).get(0)[0]).intValue()
        + ((Long) topicRequestsRepo.findAllTopicRequestsCountForEnv(env, tenantId).get(0)[0])
            .intValue()
        + ((Long) aclRepo.findAllAclsCountForEnv(env, tenantId).get(0)[0]).intValue()
        + ((Long) aclRequestsRepo.findAllAclRequestsCountForEnv(env, tenantId).get(0)[0])
            .intValue();
  }

  public int findAllConnectorComponentsCountForEnv(String env, int tenantId) {
    return ((Long) kafkaConnectorRepo.findAllConnectorCountForEnv(env, tenantId).get(0)[0])
            .intValue()
        + ((Long)
                kafkaConnectorRequestsRepo.findAllConnectorRequestsCountForEnv(env, tenantId)
                    .get(0)[0])
            .intValue();
  }

  public int findAllSchemaComponentsCountForEnv(String env, int tenantId) {
    return ((Long) schemaRequestRepo.findAllSchemaRequestsCountForEnv(env, tenantId).get(0)[0])
            .intValue()
        + ((Long) messageSchemaRepo.findAllSchemaCountForEnv(env, tenantId).get(0)[0]).intValue();
  }

  public int findAllComponentsCountForTeam(Integer teamId, int tenantId) {
    return ((Long) schemaRequestRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0])
            .intValue()
        + ((Long) messageSchemaRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0])
            .intValue()
        + ((Long) kafkaConnectorRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0])
            .intValue()
        + ((Long)
                kafkaConnectorRequestsRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0])
            .intValue()
        + ((Long) topicRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0]).intValue()
        + ((Long) topicRequestsRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0])
            .intValue()
        + ((Long) aclRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0]).intValue()
        + ((Long) aclRequestsRepo.findAllRecordsCountForTeamId(teamId, tenantId).get(0)[0])
            .intValue();
  }

  public int getAllTopicsCountInAllTenants() {
    return ((Long) topicRepo.findAllTopicsCount().get(0)[0]).intValue();
  }

  // teamId is requestedBy. For 'topics' all the requests are assigned to the same team, except
  // claim requests
  public Map<String, Map<String, Long>> getTopicRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId) {
    Map<String, Map<String, Long>> allCountsMap = new HashMap<>();

    Map<String, Long> operationTypeCountsMap = new HashMap<>();
    Map<String, Long> statusCountsMap = new HashMap<>();

    if (RequestMode.MY_REQUESTS == requestMode) {
      List<Object[]> topicRequestsOperationTypObj =
          topicRequestsRepo.findAllTopicRequestsGroupByOperationType(teamId, tenantId);
      updateMap(operationTypeCountsMap, topicRequestsOperationTypObj);

      List<Object[]> topicRequestsStatusObj =
          topicRequestsRepo.findAllTopicRequestsGroupByStatus(teamId, tenantId);
      updateMap(statusCountsMap, topicRequestsStatusObj);
    } else if (RequestMode.TO_APPROVE == requestMode) {
      List<Object[]> topicRequestsStatusObj =
          topicRequestsRepo.findAllTopicRequestsGroupByStatus(teamId, tenantId);
      updateMap(statusCountsMap, topicRequestsStatusObj);

      long assignedToClaimReqs =
          topicRequestsRepo.countAllTopicRequestsByDescriptionAndTopictype(
              tenantId, "" + teamId, RequestOperationType.CLAIM.value);
      List<Object[]> topicRequestsOperationTypObj =
          topicRequestsRepo.findAllTopicRequestsGroupByOperationType(teamId, tenantId);
      updateMap(operationTypeCountsMap, topicRequestsOperationTypObj);

      operationTypeCountsMap.put(RequestOperationType.CLAIM.value, assignedToClaimReqs);
    }

    for (RequestStatus requestStatus : RequestStatus.values()) {
      if (!statusCountsMap.containsKey(requestStatus.value))
        statusCountsMap.put(requestStatus.value, 0L);
    }
    for (RequestOperationType requestOperationType : RequestOperationType.values()) {
      if (!operationTypeCountsMap.containsKey(requestOperationType.value))
        operationTypeCountsMap.put(requestOperationType.value, 0L);
    }

    allCountsMap.put("STATUS_COUNTS", statusCountsMap);
    allCountsMap.put("OPERATION_TYPE_COUNTS", operationTypeCountsMap);

    return allCountsMap;
  }

  private static void updateMap(Map<String, Long> countsMap, List<Object[]> topicRequestsObj) {
    topicRequestsObj.forEach(
        topicReqObjs -> countsMap.put((String) topicReqObjs[0], (Long) topicReqObjs[1]));
  }
}
