package io.aiven.klaw.service;

import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.model.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ServerConfigService {

  @Autowired private Environment env;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private MailUtils mailService;

  @Autowired private ClusterApiService clusterApiService;

  @Autowired private CommonUtilsService commonUtilsService;

  private static List<ServerConfigProperties> listProps;

  public ServerConfigService(Environment env) {
    this.env = env;
  }

  @PostConstruct
  public void getAllProperties() {

    log.info("All server properties being loaded");

    List<ServerConfigProperties> listProps = new ArrayList<>();
    List<String> allowedKeys =
        Arrays.asList(
            "spring.", "java.", "klaw.", "server.", "logging.", "management.", "endpoints.");

    if (env instanceof ConfigurableEnvironment) {
      for (PropertySource propertySource : ((ConfigurableEnvironment) env).getPropertySources()) {
        if (propertySource instanceof EnumerablePropertySource) {
          for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {

            ServerConfigProperties props = new ServerConfigProperties();
            props.setKey(key);
            if (key.contains("password") || key.contains("license")) props.setValue("*******");
            else
              props.setValue(WordUtils.wrap(propertySource.getProperty(key) + "", 125, "\n", true));

            if (!checkPropertyExists(listProps, key)
                && !key.toLowerCase().contains("path")
                && !key.contains("secretkey")
                && !key.contains("password")
                && !key.contains("username")) {
              if (allowedKeys.stream().anyMatch(key::startsWith)) listProps.add(props);
            }
          }
        }
      }
    }
    ServerConfigService.listProps = listProps;
  }

  public List<ServerConfigProperties> getAllProps() {
    return listProps;
  }

  private boolean checkPropertyExists(List<ServerConfigProperties> props, String key) {
    for (ServerConfigProperties serverProps : props) {
      if (Objects.equals(serverProps.getKey(), key)) return true;
    }
    return false;
  }

  public List<Map<String, String>> getAllEditableProps() {
    List<Map<String, String>> listMap = new ArrayList<>();
    Map<String, String> resultMap = new HashMap<>();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_SERVERCONFIG)) {
      resultMap.put("result", "Not Authorized.");
      listMap.add(resultMap);
      return listMap;
    }

    int tenantId = commonUtilsService.getTenantId(getUserName());
    Map<String, Map<String, String>> kwProps = manageDatabase.getKwPropertiesMap(tenantId);
    String kwVal, kwKey;

    for (Map.Entry<String, Map<String, String>> stringStringEntry : kwProps.entrySet()) {
      resultMap = new HashMap<>();
      kwKey = stringStringEntry.getKey();
      kwVal = stringStringEntry.getValue().get("kwvalue");
      resultMap.put("kwkey", kwKey);

      if (KwConstants.TENANT_CONFIG_PROPERTY.equals(kwKey)) {
        ObjectMapper objectMapper = new ObjectMapper();
        TenantConfig dynamicObj;
        try {
          dynamicObj = objectMapper.readValue(kwVal, TenantConfig.class);
          updateEnvNameValues(dynamicObj, tenantId);
          kwVal = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dynamicObj);
          resultMap.put("kwvalue", kwVal);
          resultMap.put("kwdesc", stringStringEntry.getValue().get("kwdesc"));

          listMap.add(resultMap);
        } catch (Exception ioe) {
          log.error("Error from getAllEditableProps {}", kwKey, ioe);
          log.error("No environments/clusters found. {}", kwKey);
          kwVal = "{}";
          resultMap.put("kwvalue", kwVal);
          resultMap.put("kwdesc", stringStringEntry.getValue().get("kwdesc"));
        }
      } else {
        resultMap.put("kwvalue", kwVal);
        resultMap.put("kwdesc", stringStringEntry.getValue().get("kwdesc"));

        listMap.add(resultMap);
      }
    }

    if (tenantId != KwConstants.DEFAULT_TENANT_ID)
      return listMap.stream()
          .filter(item -> KwConstants.allowConfigForAdmins.contains(item.get("kwkey")))
          .collect(Collectors.toList());
    else return listMap;
  }

  public Map<String, String> updateKwCustomProperty(KwPropertiesModel kwPropertiesModel) {
    log.info("updateKwCustomProperty {}", kwPropertiesModel);
    Map<String, String> response = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String kwKey = kwPropertiesModel.getKwKey();
    String kwVal = kwPropertiesModel.getKwValue().trim();
    response.put("resultkey", kwKey);

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_SERVERCONFIG)) {
      response.put("result", "Not Authorized.");
      return response;
    }

    // SUPERADMINS filter
    if (tenantId != KwConstants.DEFAULT_TENANT_ID) {
      if (!KwConstants.allowConfigForAdmins.contains(kwKey)) {
        response.put("result", "Not Authorized.");
        return response;
      }
    }

    try {
      if (KwConstants.TENANT_CONFIG_PROPERTY.equals(kwKey)) {
        ObjectMapper objectMapper = new ObjectMapper();
        TenantConfig dynamicObj;
        try {
          dynamicObj = objectMapper.readValue(kwVal, TenantConfig.class);
          if (validateTenantConfig(dynamicObj, tenantId)) {
            updateEnvIdValues(dynamicObj);
            kwPropertiesModel.setKwValue(objectMapper.writeValueAsString(dynamicObj));
          } else {
            response.put(
                "result",
                "Failure. Invalid json / incorrect name values. Check tenant and env details.");
            return response;
          }
        } catch (IOException e) {
          log.error("Exception:", e);
          response.put(
              "result", "Failure. Invalid json values. Please check if tenant/environments exist.");
          return response;
        }
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      response.put("result", "Failure. Please check if the environment names exist.");
      return response;
    }

    KwProperties kwProperties = new KwProperties();
    copyProperties(kwPropertiesModel, kwProperties);
    String res = manageDatabase.getHandleDbRequests().updateKwProperty(kwProperties, tenantId);
    if ("success".equals(res))
      commonUtilsService.updateMetadata(
          tenantId, EntityType.PROPERTIES, MetadataOperationType.CREATE);
    response.put("result", "success");
    return response;
  }

  private void updateEnvNameValues(TenantConfig dynamicObj, int tenantId) {
    if (dynamicObj.getTenantModel() != null) {
      KwTenantConfigModel tenant = dynamicObj.getTenantModel();

      // syncClusterName update
      if (tenant.getBaseSyncEnvironment() != null)
        tenant.setBaseSyncEnvironment(
            getEnvDetails(tenant.getBaseSyncEnvironment(), tenantId).getName());

      // syncClusterKafkaConnectName update
      if (tenant.getBaseSyncKafkaConnectCluster() != null)
        tenant.setBaseSyncKafkaConnectCluster(
            getKafkaConnectEnvDetails(tenant.getBaseSyncKafkaConnectCluster(), tenantId).getName());

      // kafka
      if (tenant.getOrderOfTopicPromotionEnvsList() != null) {
        List<String> tmpOrderList = new ArrayList<>();
        tenant
            .getOrderOfTopicPromotionEnvsList()
            .forEach(
                a -> {
                  if (getEnvDetails(a, tenantId) != null)
                    tmpOrderList.add(getEnvDetails(a, tenantId).getName());
                });
        tenant.setOrderOfTopicPromotionEnvsList(tmpOrderList);
      }

      // kafkaconnect
      if (tenant.getOrderOfConnectorsPromotionEnvsList() != null) {
        List<String> tmpOrderList1 = new ArrayList<>();
        tenant
            .getOrderOfConnectorsPromotionEnvsList()
            .forEach(
                a -> {
                  if (getKafkaConnectEnvDetails(a, tenantId) != null)
                    tmpOrderList1.add(getKafkaConnectEnvDetails(a, tenantId).getName());
                });
        tenant.setOrderOfConnectorsPromotionEnvsList(tmpOrderList1);
      }

      // kafka
      if (tenant.getRequestTopicsEnvironmentsList() != null) {
        List<String> tmpReqTopicList = new ArrayList<>();
        tenant
            .getRequestTopicsEnvironmentsList()
            .forEach(
                a -> {
                  if (getEnvDetails(a, tenantId) != null)
                    tmpReqTopicList.add(getEnvDetails(a, tenantId).getName());
                });
        tenant.setRequestTopicsEnvironmentsList(tmpReqTopicList);
      }

      // kafkaconnect
      if (tenant.getRequestConnectorsEnvironmentsList() != null) {
        List<String> tmpReqTopicList1 = new ArrayList<>();
        tenant
            .getRequestConnectorsEnvironmentsList()
            .forEach(
                a -> {
                  if (getKafkaConnectEnvDetails(a, tenantId) != null)
                    tmpReqTopicList1.add(getKafkaConnectEnvDetails(a, tenantId).getName());
                });
        tenant.setRequestConnectorsEnvironmentsList(tmpReqTopicList1);
      }
    }
  }

  private void updateEnvIdValues(TenantConfig dynamicObj) {
    if (dynamicObj.getTenantModel() != null) {
      KwTenantConfigModel tenantModel = dynamicObj.getTenantModel();

      // syncClusterName update
      if (tenantModel.getBaseSyncEnvironment() != null)
        tenantModel.setBaseSyncEnvironment(
            getEnvDetailsFromName(
                    tenantModel.getBaseSyncEnvironment(),
                    getTenantIdFromName(tenantModel.getTenantName()))
                .getId());

      // syncClusterKafkaConnectName update
      if (tenantModel.getBaseSyncKafkaConnectCluster() != null)
        tenantModel.setBaseSyncKafkaConnectCluster(
            getKafkaConnectEnvDetailsFromName(
                    tenantModel.getBaseSyncKafkaConnectCluster(),
                    getTenantIdFromName(tenantModel.getTenantName()))
                .getId());

      // kafka
      if (tenantModel.getOrderOfTopicPromotionEnvsList() != null) {
        List<String> tmpOrderList = new ArrayList<>();
        tenantModel
            .getOrderOfTopicPromotionEnvsList()
            .forEach(
                a ->
                    tmpOrderList.add(
                        getEnvDetailsFromName(a, getTenantIdFromName(tenantModel.getTenantName()))
                            .getId()));
        tenantModel.setOrderOfTopicPromotionEnvsList(tmpOrderList);
      }

      // kafkaconnect
      if (tenantModel.getOrderOfConnectorsPromotionEnvsList() != null) {
        List<String> tmpOrderList1 = new ArrayList<>();
        tenantModel
            .getOrderOfConnectorsPromotionEnvsList()
            .forEach(
                a ->
                    tmpOrderList1.add(
                        getKafkaConnectEnvDetailsFromName(
                                a, getTenantIdFromName(tenantModel.getTenantName()))
                            .getId()));
        tenantModel.setOrderOfConnectorsPromotionEnvsList(tmpOrderList1);
      }

      // kafka
      if (tenantModel.getRequestTopicsEnvironmentsList() != null) {
        List<String> tmpReqTopicList = new ArrayList<>();
        tenantModel
            .getRequestTopicsEnvironmentsList()
            .forEach(
                a ->
                    tmpReqTopicList.add(
                        getEnvDetailsFromName(a, getTenantIdFromName(tenantModel.getTenantName()))
                            .getId()));
        tenantModel.setRequestTopicsEnvironmentsList(tmpReqTopicList);
      }

      // kafkaconnect
      if (tenantModel.getRequestConnectorsEnvironmentsList() != null) {
        List<String> tmpReqTopicList1 = new ArrayList<>();
        tenantModel
            .getRequestConnectorsEnvironmentsList()
            .forEach(
                a ->
                    tmpReqTopicList1.add(
                        getKafkaConnectEnvDetailsFromName(
                                a, getTenantIdFromName(tenantModel.getTenantName()))
                            .getId()));
        tenantModel.setRequestConnectorsEnvironmentsList(tmpReqTopicList1);
      }
    }
  }

  private boolean validateTenantConfig(TenantConfig dynamicObj, int tenantId) {
    Map<Integer, String> tenantMap = manageDatabase.getTenantMap();
    List<Env> envList = manageDatabase.getKafkaEnvList(tenantId);
    List<Env> envKafkaConnectList = manageDatabase.getKafkaConnectEnvList(tenantId);

    List<String> envListStr = new ArrayList<>();
    envList.forEach(a -> envListStr.add(a.getName()));

    List<String> envListKafkaConnectStr = new ArrayList<>();
    envKafkaConnectList.forEach(a -> envListKafkaConnectStr.add(a.getName()));

    boolean tenantCheck;
    try {
      tenantCheck = tenantMap.containsValue(dynamicObj.getTenantModel().getTenantName());

      if (tenantCheck) {

        KwTenantConfigModel tenantModel = dynamicObj.getTenantModel();
        // syncClusterCheck
        if (tenantModel.getBaseSyncEnvironment() != null
            && !envListStr.contains(tenantModel.getBaseSyncEnvironment())) {
          return false;
        }

        // syncClusterKafkaConnectCheck
        if (tenantModel.getBaseSyncKafkaConnectCluster() != null
            && !envListKafkaConnectStr.contains(tenantModel.getBaseSyncKafkaConnectCluster())) {
          return false;
        }

        // orderOfenvs check
        if (tenantModel.getOrderOfTopicPromotionEnvsList() != null) {
          for (String orderOfTopicPromotionEnv : tenantModel.getOrderOfTopicPromotionEnvsList()) {
            if (!envListStr.contains(orderOfTopicPromotionEnv)) return false;
          }
        }

        // orderOfConnectEnvs check
        if (tenantModel.getOrderOfConnectorsPromotionEnvsList() != null) {
          for (String orderOfConnectorsPromotionEnv :
              tenantModel.getOrderOfConnectorsPromotionEnvsList()) {
            if (!envListKafkaConnectStr.contains(orderOfConnectorsPromotionEnv)) return false;
          }
        }

        // requestTopics check
        if (tenantModel.getRequestTopicsEnvironmentsList() != null) {
          for (String requestTopicEnvs : tenantModel.getRequestTopicsEnvironmentsList()) {
            if (!envListStr.contains(requestTopicEnvs)) return false;
          }
        }

        // requestConnectors check
        if (tenantModel.getRequestConnectorsEnvironmentsList() != null) {
          for (String requestConnectorEnvs : tenantModel.getRequestConnectorsEnvironmentsList()) {
            if (!envListKafkaConnectStr.contains(requestConnectorEnvs)) return false;
          }
        }
      }
    } catch (Exception e) {
      log.error(dynamicObj + "", e);
      return false;
    }

    return tenantCheck;
  }

  public Env getEnvDetails(String envId, int tenantId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(tenantId).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  public Env getKafkaConnectEnvDetails(String envId, int tenantId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaConnectEnvList(tenantId).stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.orElse(null);
  }

  public Env getEnvDetailsFromName(String envName, Integer tenantId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaEnvList(tenantId).stream()
            .filter(
                env ->
                    Objects.equals(env.getName(), envName)
                        && Objects.equals(env.getTenantId(), tenantId))
            .findFirst();
    return envFound.orElse(null);
  }

  public Env getKafkaConnectEnvDetailsFromName(String envName, Integer tenantId) {
    Optional<Env> envFound =
        manageDatabase.getKafkaConnectEnvList(tenantId).stream()
            .filter(env -> env.getName().equals(envName) && env.getTenantId().equals(tenantId))
            .findFirst();
    return envFound.orElse(null);
  }

  public Map<String, String> resetCache() {
    Map<String, String> hashMap = new HashMap<>();
    manageDatabase.updateStaticDataForTenant(commonUtilsService.getTenantId(getUserName()));
    return hashMap;
  }

  public Map<String, String> testClusterApiConnection(String clusterApiUrl) {
    Map<String, String> hashMap = new HashMap<>();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String clusterApiStatus = clusterApiService.getClusterApiStatus(clusterApiUrl, true, tenantId);
    if ("ONLINE".equals(clusterApiStatus)) clusterApiStatus = "successful.";
    else clusterApiStatus = "failure.";
    hashMap.put("result", clusterApiStatus);
    return hashMap;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  private Integer getTenantIdFromName(String tenantName) {
    return manageDatabase.getTenantMap().entrySet().stream()
        .filter(obj -> Objects.equals(obj.getValue(), tenantName))
        .findFirst()
        .get()
        .getKey();
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }
}
