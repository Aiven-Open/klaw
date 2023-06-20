package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;
import static io.aiven.klaw.helpers.KwConstants.REQUEST_TOPICS_OF_ENVS;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.UtilMethods;
import io.aiven.klaw.model.KwMetadataUpdates;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.Options;
import io.aiven.klaw.model.charts.Title;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.PermissionType;
import java.io.*;
import java.net.InetAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

  public static final String BASE_URL_ADDRESS = "BASE_URL_ADDRESS";
  public static final String BASE_URL_NAME = "BASE_URL_NAME";

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

  @Autowired Environment environment;

  @Autowired ManageDatabase manageDatabase;

  private static Map<String, String> baseUrlsMap;

  private static HttpComponentsClientHttpRequestFactory requestFactory =
      ClusterApiService.requestFactory;

  @Value("${klaw.uiapi.servers:server1,server2}")
  private String uiApiServers;

  @Value("${server.servlet.context-path:}")
  private String kwContextPath;

  @Autowired(required = false)
  private InMemoryUserDetailsManager inMemoryUserDetailsManager;

  private RestTemplate getRestTemplate() {
    if (uiApiServers.toLowerCase().startsWith("https")) return new RestTemplate(requestFactory);
    else return new RestTemplate();
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

  public String deriveCurrentPage(String pageNo, String currentPage, int totalPages) {
    switch (pageNo) {
      case ">" -> pageNo = (Integer.parseInt(currentPage) + 1) + "";
      case ">>" -> pageNo = totalPages + "";
      case "<" -> pageNo = (Integer.parseInt(currentPage) - 1) + "";
      case "<<" -> pageNo = "1";
    }
    return pageNo;
  }

  public void getAllPagesList(
      String pageNo, String currentPage, int totalPages, List<String> numList) {
    if (currentPage != null
        && !currentPage.equals("")
        && !currentPage.equals(pageNo)
        && Integer.parseInt(pageNo) > 1
        && totalPages > 1) {
      numList.add("<<");
      numList.add("<");
    } else if (currentPage != null
        && currentPage.equals(pageNo)
        && Integer.parseInt(pageNo) > 1
        && totalPages > 1) {
      numList.add("<<");
      numList.add("<");
    }

    if (totalPages > Integer.parseInt(pageNo)) {
      numList.add(pageNo);
      numList.add(">");
      numList.add(">>");
    } else if (totalPages == Integer.parseInt(pageNo)) {
      numList.add(pageNo);
    }
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
            User.withUsername(encoder.encode(decodePwd(userInfo.getUsername())))
                .password(userInfo.getPwd())
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

  private BasicTextEncryptor getJasyptEncryptor() {
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

  public Map<String, String> getBaseIpUrlFromEnvironment() {
    if (baseUrlsMap != null && !baseUrlsMap.isEmpty()) {
      return baseUrlsMap;
    } else {
      baseUrlsMap = new HashMap<>();
      String hostAddress = InetAddress.getLoopbackAddress().getHostAddress();
      String hostName = InetAddress.getLoopbackAddress().getHostName();
      int port = Integer.parseInt(Objects.requireNonNull(environment.getProperty("server.port")));

      baseUrlsMap.put(BASE_URL_ADDRESS, hostAddress + ":" + port);
      baseUrlsMap.put(BASE_URL_NAME, hostName + ":" + port);
      return baseUrlsMap;
    }
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

          Map<String, String> baseUrlsFromEnv = getBaseIpUrlFromEnvironment();

          // ignore metadata cache reset on local.
          if (baseUrlsFromEnv != null && !baseUrlsFromEnv.isEmpty()) {
            if (baseUrlsFromEnv.containsKey(BASE_URL_ADDRESS)
                && basePath.contains(baseUrlsFromEnv.get(BASE_URL_ADDRESS))) {
              continue;
            }
            if (baseUrlsFromEnv.containsKey(BASE_URL_NAME)
                && basePath.contains(baseUrlsFromEnv.get(BASE_URL_NAME))) {
              continue;
            }
          }

          if (kwMetadataUpdates.getEntityValue() == null) {
            kwMetadataUpdates.setEntityValue("na");
          }

          String uri =
              basePath
                  + "/resetMemoryCache/"
                  + kwMetadataUpdates.getTenantId()
                  + "/"
                  + kwMetadataUpdates.getEntityType()
                  + "/"
                  + kwMetadataUpdates.getEntityValue()
                  + "/"
                  + kwMetadataUpdates.getOperationType();
          RestTemplate restTemplate = getRestTemplate();

          HttpHeaders headers = new HttpHeaders();
          headers.setContentType(MediaType.APPLICATION_JSON);

          headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
          HttpEntity<String> entity = new HttpEntity<>(headers);

          restTemplate.exchange(uri, HttpMethod.POST, entity, String.class);
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

  public void loadAllUsers(Properties globalUsers, List<UserInfo> users) {
    Iterator<UserInfo> iter = users.iterator();
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    UserInfo userInfo;
    while (iter.hasNext()) {
      userInfo = iter.next();
      try {
        String secPwd = userInfo.getPwd();
        if (secPwd != null && secPwd.equals("")) {
          secPwd = "gfGF%64GFDd766hfgfHFD$%#453";
        } else {
          secPwd = decodePwd(secPwd);
        }
        globalUsers.put(
            userInfo.getUsername(), encoder.encode(secPwd) + "," + userInfo.getRole() + ",enabled");
      } catch (Exception e) {
        log.error("Error : User not loaded {}. Check password.", userInfo.getUsername(), e);
      }
    }
  }
}
