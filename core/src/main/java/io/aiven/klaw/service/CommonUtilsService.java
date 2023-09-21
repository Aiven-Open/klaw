package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
import static io.aiven.klaw.helpers.KwConstants.REQUEST_TOPICS_OF_ENVS;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.UtilMethods;
import io.aiven.klaw.model.KwMetadataUpdates;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.ResourceHistory;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.Options;
import io.aiven.klaw.model.charts.Title;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.requests.ResetEntityCache;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
@Slf4j
public class CommonUtilsService {

  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final TypeReference<List<ResourceHistory>> VALUE_TYPE_REF =
      new TypeReference<>() {};

  @Value("${klaw.enable.authorization.ad:false}")
  private boolean enableUserAuthorizationFromAD;

  @Value("${klaw.ad.username.attribute:preferred_username}")
  private String preferredUsernameAttribute;

  @Value("${klaw.ad.email.attribute:email}")
  private String emailAttribute;

  @Value("${klaw.saas.ssl.clientcerts.location:./tmp/}")
  private String clientCertsLocation;

  @Value("${klaw.saas.ssl.clusterapi.truststore:./tmp}")
  private String trustStore;

  @Value("${klaw.saas.ssl.clusterapi.truststore.pwd:./tmp}")
  private String trustStorePwd;

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Autowired ManageDatabase manageDatabase;

  @Autowired HARestMessagingService HARestMessagingService;

  @Value("${klaw.uiapi.servers:server1,server2}")
  private String uiApiServers;

  @Value("${server.servlet.context-path:}")
  private String kwContextPath;

  @Autowired(required = false)
  private InMemoryUserDetailsManager inMemoryUserDetailsManager;

  private RestTemplate getRestTemplate() {
    return HARestMessagingService.getRestTemplate();
  }

  public Authentication getAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication();
  }

  String getAuthority(Object principal) {
    if (enableUserAuthorizationFromAD) {
      if (principal instanceof DefaultOAuth2User) {
        DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) principal;
        String userName = extractUserNameFromOAuthUser(defaultOAuth2User);
        return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getRole();
      } else if (principal instanceof String) {
        return manageDatabase.getHandleDbRequests().getUsersInfo((String) principal).getRole();
      } else if (principal instanceof UserDetails) {
        Object[] authorities = ((UserDetails) principal).getAuthorities().toArray();
        if (authorities.length > 0) {
          SimpleGrantedAuthority sag = (SimpleGrantedAuthority) authorities[0];
          return sag.getAuthority();
        } else {
          return "";
        }
      } else {
        return "";
      }
    } else {
      UserInfo userInfo = manageDatabase.getHandleDbRequests().getUsersInfo(getUserName(principal));
      if (userInfo != null) {
        return userInfo.getRole();
      } else {
        return null;
      }
    }
  }

  public String extractUserNameFromOAuthUser(DefaultOAuth2User defaultOAuth2User) {
    String preferredUsername =
        (String) defaultOAuth2User.getAttributes().get(preferredUsernameAttribute);
    String email = (String) defaultOAuth2User.getAttributes().get(emailAttribute);
    String userName = null;
    if (preferredUsername != null) {
      userName = preferredUsername;
    } else if (email != null) {
      userName = email;
    }
    return userName;
  }

  public String getUserName(Object principal) {
    return UtilMethods.getUserName(principal, preferredUsernameAttribute);
  }

  public String getCurrentUserName() {
    return UtilMethods.getUserName(preferredUsernameAttribute);
  }

  public boolean isNotAuthorizedUser(Object principal, PermissionType permissionType) {
    try {
      return !manageDatabase
          .getRolesPermissionsPerTenant(getTenantId(getUserName(principal)))
          .get(getAuthority(principal))
          .contains(permissionType.name());
    } catch (Exception e) {
      log.debug(
          "Error isNotAuthorizedUser / Check if role exists. {} {} {}",
          getUserName(principal),
          permissionType.name(),
          getAuthority(getUserName(principal)),
          e);
      return true;
    }
  }

  public ChartsJsOverview getChartsJsOverview(
      List<Map<String, String>> activityCountList,
      String title,
      String yaxisCount,
      String xaxisLabel,
      String xAxisLabelConstant,
      String yAxisLabelConstant,
      int tenantId) {
    ChartsJsOverview chartsJsOverview = new ChartsJsOverview();
    List<Integer> data = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    List<String> colors = new ArrayList<>();

    data.add(0);
    labels.add("");

    int totalCount = 0;

    if (activityCountList != null) {
      for (Map<String, String> hashMap : activityCountList) {
        totalCount += Integer.parseInt(hashMap.get(yaxisCount));
        data.add(Integer.parseInt(hashMap.get(yaxisCount)));

        if ("teamid".equals(xaxisLabel)) {
          labels.add(
              manageDatabase.getTeamNameFromTeamId(
                  tenantId, Integer.parseInt(hashMap.get(xaxisLabel))));
        } else {
          labels.add(hashMap.get(xaxisLabel));
        }

        colors.add("Green");
      }
    }
    chartsJsOverview.setData(data);
    chartsJsOverview.setLabels(labels);
    chartsJsOverview.setColors(colors);

    Options options = new Options();
    Title title1 = new Title();
    title1.setDisplay(true);
    title1.setText(title + " (Total " + totalCount + ")");
    title1.setPosition("bottom");
    title1.setFontColor("red");

    options.setTitle(title1);
    chartsJsOverview.setOptions(options);
    chartsJsOverview.setTitleForReport(title);

    chartsJsOverview.setXAxisLabel(xAxisLabelConstant);
    chartsJsOverview.setYAxisLabel(yAxisLabelConstant);

    return chartsJsOverview;
  }

  public boolean addPublicKeyToTrustStore(String fileName, Integer tenantId) {
    KeyStore ks;
    fileName = fileName + tenantId + ".pem";
    try {
      ks = KeyStore.getInstance("JKS");
      FileInputStream trustStoreStream = new FileInputStream(trustStore);

      ks.load(trustStoreStream, trustStorePwd.toCharArray());

      FileInputStream fis = new FileInputStream(clientCertsLocation + "/" + fileName);
      BufferedInputStream bis = new BufferedInputStream(fis);
      // I USE x.509 BECAUSE THAT'S WHAT keytool CREATES

      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      Certificate cert;
      while (bis.available() > 0) {
        cert = cf.generateCertificate(bis);
        ks.setCertificateEntry(fileName, cert);
      }

      ks.store(new FileOutputStream(trustStore), trustStorePwd.toCharArray());
      return true;

    } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
      log.error(
          "Unable to load public key to trust store clusterName: "
              + fileName
              + " Tenant "
              + tenantId
              + "-",
          e);
      return false;
    }
  }

  public void updateMetadata(
      int tenantId,
      EntityType entityType,
      MetadataOperationType operationType,
      String entityValue) {
    KwMetadataUpdates kwMetadataUpdates =
        KwMetadataUpdates.builder()
            .tenantId(tenantId)
            .entityType(entityType.name())
            .entityValue(entityValue)
            .operationType(operationType.name())
            .createdTime(new Timestamp(System.currentTimeMillis()))
            .build();
    updateMetadataCache(kwMetadataUpdates, true);

    try {
      CompletableFuture.runAsync(
              () -> {
                resetCacheOnOtherServers(kwMetadataUpdates);
              })
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Exception:", e);
    }
  }

  public synchronized void updateMetadataCache(
      KwMetadataUpdates kwMetadataUpdates, boolean isLocal) {
    final EntityType entityType = EntityType.of(kwMetadataUpdates.getEntityType());
    if (entityType == null) {
      return;
    }
    final MetadataOperationType operationType =
        MetadataOperationType.of(kwMetadataUpdates.getOperationType());
    if (entityType == EntityType.USERS) {
      manageDatabase.loadUsersForAllTenants();
      if (DATABASE.value.equals(authenticationType) && !isLocal) {
        updateInMemoryAuthenticationManager(kwMetadataUpdates, operationType);
      }
    } else if (entityType == EntityType.TEAM) {
      manageDatabase.loadEnvsForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadTenantTeamsForOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.CLUSTER && operationType == MetadataOperationType.DELETE) {
      manageDatabase.deleteCluster(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.CLUSTER && operationType == MetadataOperationType.CREATE) {
      manageDatabase.loadClustersForOneTenant(null, null, null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.ENVIRONMENT
        && operationType == MetadataOperationType.CREATE) {
      manageDatabase.loadEnvsForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadEnvMapForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadTenantTeamsForOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.ENVIRONMENT
        && operationType == MetadataOperationType.DELETE) {
      manageDatabase.loadEnvMapForOneTenant(kwMetadataUpdates.getTenantId());
      manageDatabase.loadEnvsForOneTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TENANT && operationType == MetadataOperationType.CREATE) {
      manageDatabase.updateStaticDataForTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TENANT && operationType == MetadataOperationType.DELETE) {
      manageDatabase.deleteTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TENANT && operationType == MetadataOperationType.UPDATE) {
      manageDatabase.loadOneTenant(kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.ROLES_PERMISSIONS) {
      manageDatabase.loadRolesPermissionsOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.PROPERTIES) {
      manageDatabase.loadKwPropsPerOneTenant(null, kwMetadataUpdates.getTenantId());
    } else if (entityType == EntityType.TOPICS) {
      manageDatabase.loadTopicsForOneTenant(kwMetadataUpdates.getTenantId());
    }
  }

  private void updateInMemoryAuthenticationManager(
      KwMetadataUpdates kwMetadataUpdates, MetadataOperationType operationType) {
    UserInfo userInfo =
        manageDatabase.getHandleDbRequests().getUsersInfo(kwMetadataUpdates.getEntityValue());
    try {
      PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      if (operationType == MetadataOperationType.CREATE) {
        inMemoryUserDetailsManager.createUser(
            User.withUsername(userInfo.getUsername())
                .password(encoder.encode(decodePwd(userInfo.getPwd())))
                .roles(userInfo.getRole())
                .build());
      } else if (operationType == MetadataOperationType.UPDATE) {
        inMemoryUserDetailsManager.updateUser(
            User.withUsername(userInfo.getUsername())
                .password(encoder.encode(decodePwd(userInfo.getPwd())))
                .roles(userInfo.getRole())
                .build());
      } else if (operationType == MetadataOperationType.DELETE) {
        inMemoryUserDetailsManager.deleteUser(kwMetadataUpdates.getEntityValue());
      }
    } catch (Exception e) {
      log.error("ERROR : Ignore the error while updating user in inMemory authentication manager");
    }
  }

  private String decodePwd(String pwd) {
    if (pwd != null) {
      return getJasyptEncryptor().decrypt(pwd);
    } else {
      return "";
    }
  }

  public BasicTextEncryptor getJasyptEncryptor() {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

    return textEncryptor;
  }

  public String getLoginUrl() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/login";
  }

  public String getBaseUrl() {
    if ("".equals(kwContextPath))
      return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    else
      return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
          + "/"
          + kwContextPath;
  }

  public void resetCacheOnOtherServers(KwMetadataUpdates kwMetadataUpdates) {
    log.info("invokeResetEndpoints");
    try {
      if (uiApiServers != null && uiApiServers.length() > 0) {
        String[] servers = uiApiServers.split(",");
        String basePath;
        for (String server : servers) {

          if ("".equals(kwContextPath)) {
            basePath = server;
          } else {
            basePath = server + "/" + kwContextPath;
          }

          // ignore metadata cache reset on local.
          if (HARestMessagingService.isLocalServerUrl(basePath)) {
            continue;
          }

          if (kwMetadataUpdates.getEntityValue() == null) {
            kwMetadataUpdates.setEntityValue("na");
          }

          String uri = basePath + "/resetMemoryCache/";

          ResetEntityCache resetEntityCache =
              ResetEntityCache.builder()
                  .tenantId(kwMetadataUpdates.getTenantId())
                  .entityType(kwMetadataUpdates.getEntityType())
                  .entityValue(kwMetadataUpdates.getEntityValue())
                  .operationType(kwMetadataUpdates.getOperationType())
                  .build();

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);
          headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);

          HttpEntity<ResetEntityCache> request = new HttpEntity<>(resetEntityCache, headers);
          ResponseEntity<Object> response =
              getRestTemplate()
                  .exchange(uri, HttpMethod.POST, request, new ParameterizedTypeReference<>() {});
          log.info("Response from invokeResetEndpoints" + response);
        }
      }
    } catch (Exception e) {
      log.error("Error from invokeResetEndpoints ", e);
    }
  }

  protected List<Topic> getFilteredTopicsForTenant(List<Topic> topicsFromSOT) {
    List<Topic> filteredList = new ArrayList<>();
    // tenant filtering
    try {
      final Set<String> allowedEnvIdSet = getEnvsFromUserId(getUserName(getPrincipal()));
      if (topicsFromSOT != null) {
        filteredList =
            topicsFromSOT.stream()
                .filter(topic -> allowedEnvIdSet.contains(topic.getEnvironment()))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // this situation cannot happen, as every topic has an assigned team and this flow is
      // triggered on topic overview, which means topic has an owner
      log.error("No environments/clusters found.", e);
    }
    return filteredList;
  }

  public Set<String> getEnvsFromUserId(String userName) {
    return new HashSet<>(
        manageDatabase.getTeamsAndAllowedEnvs(getTeamId(userName), getTenantId(userName)));
  }

  public int getTenantId(String userId) {
    return manageDatabase.selectAllCachedUserInfo().stream()
        .filter(userInfo -> userInfo.getUsername().equals(userId))
        .findFirst()
        .map(UserInfo::getTenantId)
        .orElse(0);
  }

  public Integer getTeamId(String userName) {
    return manageDatabase.selectAllCachedUserInfo().stream()
        .filter(userInfo -> userInfo.getUsername().equals(userName))
        .findFirst()
        .map(UserInfo::getTeamId)
        .orElse(0);
  }

  public Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  List<Topic> groupTopicsByEnv(List<Topic> topicsFromSOT) {
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

  public String getEnvProperty(Integer tenantId, String envPropertyType) {
    try {
      KwTenantConfigModel tenantModel = manageDatabase.getTenantConfig().get(tenantId);
      if (tenantModel == null) {
        return "";
      }
      List<Integer> intOrderEnvsList = new ArrayList<>();

      switch (envPropertyType) {
        case ORDER_OF_TOPIC_ENVS -> {
          List<String> orderOfTopicPromotionEnvsList =
              tenantModel.getOrderOfTopicPromotionEnvsList();
          if (null != orderOfTopicPromotionEnvsList && !orderOfTopicPromotionEnvsList.isEmpty()) {
            orderOfTopicPromotionEnvsList.forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          }
        }
        case REQUEST_TOPICS_OF_ENVS -> {
          List<String> requestTopics = tenantModel.getRequestTopicsEnvironmentsList();
          if (requestTopics != null && !requestTopics.isEmpty()) {
            requestTopics.forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          }
        }
        case "ORDER_OF_KAFKA_CONNECT_ENVS" -> {
          List<String> orderOfConn = tenantModel.getOrderOfConnectorsPromotionEnvsList();
          if (orderOfConn != null && !orderOfConn.isEmpty()) {
            orderOfConn.forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          }
        }
        case "REQUEST_CONNECTORS_OF_KAFKA_CONNECT_ENVS" -> {
          List<String> requestConn = tenantModel.getRequestConnectorsEnvironmentsList();
          if (requestConn != null && !requestConn.isEmpty()) {
            requestConn.forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          }
        }
        case "REQUEST_SCHEMA_OF_ENVS" -> {
          List<String> requestSchema = tenantModel.getRequestSchemaEnvironmentsList();
          if (requestSchema != null && !requestSchema.isEmpty()) {
            requestSchema.forEach(a -> intOrderEnvsList.add(Integer.parseInt(a)));
          }
        }
      }

      return intOrderEnvsList.stream().map(String::valueOf).collect(Collectors.joining(","));
    } catch (Exception e) {
      log.error("Exception:", e);
      return "";
    }
  }

  protected String getSchemaPromotionEnvsFromKafkaEnvs(int tenantId) {
    String kafkaEnvs = getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
    String[] kafkaEnvIdsList = kafkaEnvs.split(",");
    StringBuilder orderOfSchemaEnvs = new StringBuilder();

    List<Env> kafkaEnvsList = manageDatabase.getKafkaEnvList(tenantId);

    if (kafkaEnvIdsList.length > 0) {
      for (String kafkaEnvId : kafkaEnvIdsList) {
        kafkaEnvsList.stream()
            .filter(env -> env.getId().equals(kafkaEnvId))
            .findFirst()
            .ifPresent(
                env -> {
                  if (env.getAssociatedEnv() != null) {
                    orderOfSchemaEnvs.append(env.getAssociatedEnv().getId()).append(",");
                  }
                });
      }
    }

    return orderOfSchemaEnvs.toString();
  }

  public List<Topic> getTopicsForTopicName(String topicName, int tenantId) {
    if (topicName != null) {
      return manageDatabase.getTopicsForTenant(tenantId).stream()
          .filter(topic -> topic.getTopicname().equals(topicName))
          .toList();
    } else {
      return manageDatabase.getTopicsForTenant(tenantId);
    }
  }

  public List<Topic> getTopics(String env, Integer teamId, int tenantId) {
    log.debug("getSyncTopics {} {}", env, teamId);
    List<Topic> allTopicsList = manageDatabase.getTopicsForTenant(tenantId);
    if (teamId == null || teamId.equals(1)) {
      if (env == null || env.equals("ALL")) {
        return allTopicsList;
      } else {
        Set<String> uniqueTopicNamesList =
            new HashSet<>(
                allTopicsList.stream()
                    .filter(
                        topic -> {
                          return topic.getEnvironment().equals(env);
                        })
                    .map(Topic::getTopicname)
                    .toList());
        return getSubTopics(allTopicsList, uniqueTopicNamesList);
      }
    } else {
      if (env == null || "ALL".equals(env)) {
        return allTopicsList.stream().filter(topic -> topic.getTeamId().equals(teamId)).toList();
      } else {
        Set<String> uniqueTopicNamesList =
            new HashSet<>(
                allTopicsList.stream()
                    .filter(
                        topic -> {
                          return topic.getEnvironment().equals(env)
                              && topic.getTeamId().equals(teamId);
                        })
                    .map(Topic::getTopicname)
                    .toList());
        return getSubTopics(allTopicsList, uniqueTopicNamesList);
      }
    }
  }

  private List<Topic> getSubTopics(List<Topic> allTopicsList, Set<String> uniqueTopicNamesList) {
    List<Topic> subTopicsList = new ArrayList<>();
    uniqueTopicNamesList.forEach(
        topicName -> {
          allTopicsList.forEach(
              topic -> {
                if (topic.getTopicname().equals(topicName)) {
                  subTopicsList.add(topic);
                }
              });
        });
    return subTopicsList;
  }

  public List<ResourceHistory> saveTopicHistory(
      String requestOperationType,
      String topicName,
      String topicEnvironment,
      String requestor,
      Date requestedTime,
      int ownerTeamId,
      String userName,
      int tenantId,
      String entityType,
      String remarks) {
    List<ResourceHistory> topicHistoryList = new ArrayList<>();
    try {
      AtomicReference<String> existingHistory = new AtomicReference<>("");
      List<ResourceHistory> existingTopicHistory;
      List<Topic> existingTopicList = new ArrayList<>();
      // topic requests (not create) or any acl/schema requests
      if ((!RequestOperationType.CREATE.value.equals(requestOperationType)
              && entityType.equals(RequestEntityType.TOPIC.name()))
          || entityType.equals(RequestEntityType.ACL.name())
          || entityType.equals(RequestEntityType.SCHEMA.name())) {
        existingTopicList =
            getFilteredTopicsForTenant(getTopicsForTopicName(topicName, tenantId)).stream()
                .filter(topic -> Objects.equals(topic.getEnvironment(), topicEnvironment))
                .toList();
        existingTopicList.stream().findFirst().ifPresent(a -> existingHistory.set(a.getHistory()));
        try {
          existingTopicHistory = OBJECT_MAPPER.readValue(existingHistory.get(), VALUE_TYPE_REF);
          topicHistoryList.addAll(existingTopicHistory);
        } catch (Exception e) {
          log.error("Error in parsing existing history {} {}", topicName, topicEnvironment);
        }
      }

      ResourceHistory topicHistory = new ResourceHistory();
      topicHistory.setTeamName(manageDatabase.getTeamNameFromTeamId(tenantId, ownerTeamId));
      topicHistory.setEnvironmentName(getEnvDetails(topicEnvironment, tenantId).getName());
      topicHistory.setRequestedBy(requestor);
      topicHistory.setRequestedTime(DATE_TIME_FORMATTER.format(requestedTime.toInstant()));
      topicHistory.setApprovedBy(userName);
      topicHistory.setApprovedTime(DATE_TIME_FORMATTER.format(Instant.now()));
      topicHistory.setRemarks(remarks);
      topicHistoryList.add(topicHistory);

      try {
        if (!existingTopicList.isEmpty()
            && (entityType.equals(RequestEntityType.ACL.name())
                || entityType.equals(RequestEntityType.SCHEMA.name()))) {
          Topic topic = existingTopicList.get(0);
          topic.setHistory(OBJECT_MAPPER.writer().writeValueAsString(topicHistoryList));
          topic.setExistingTopic(true);
          manageDatabase.getHandleDbRequests().addToSynctopics(existingTopicList);
        }
      } catch (JsonProcessingException e) {
        log.error("Could not save history : ", e);
      }

    } catch (Exception e) {
      log.error("Exception: ", e);
    }
    return topicHistoryList;
  }

  public boolean existsSchemaForTopic(String topicName, String topicEnvId, int tenantId) {

    Optional<String> schemaEnvId =
        manageDatabase.getAssociatedSchemaEnvIdFromTopicId(topicEnvId, tenantId);
    return schemaEnvId
        .filter(
            envId ->
                manageDatabase
                    .getHandleDbRequests()
                    .existsSchemaForTopic(topicName, envId, tenantId))
        .isPresent();
  }

  public boolean isCreateNewSchemaAllowed(String schemaEnvId, int tenantId) {
    KwTenantConfigModel tenantModel = manageDatabase.getTenantConfig().get(tenantId);
    List<String> reqSchemaEnvs =
        tenantModel == null ? new ArrayList<>() : tenantModel.getRequestSchemaEnvironmentsList();

    return reqSchemaEnvs != null ? reqSchemaEnvs.contains(schemaEnvId) : false;
  }

  public Env getEnvDetails(String envId, int tenantId) {
    return manageDatabase.getKafkaEnv(tenantId, Integer.valueOf(envId)).orElse(null);
  }
}
