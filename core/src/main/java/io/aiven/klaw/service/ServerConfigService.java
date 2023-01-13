package io.aiven.klaw.service;

import static org.springframework.beans.BeanUtils.copyProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.model.*;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.PermissionType;
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

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();
  @Autowired private Environment env;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private MailUtils mailService;

  @Autowired private ClusterApiService clusterApiService;

  private final CommonUtilsService commonUtilsService;

  private static List<ServerConfigProperties> listProps;

  public ServerConfigService(
      Environment env,
      CommonUtilsService commonUtilsService,
      MailUtils mailService,
      ManageDatabase manageDatabase) {
    this.env = env;
    this.commonUtilsService = commonUtilsService;
    this.mailService = mailService;
    this.manageDatabase = manageDatabase;
  }

  @PostConstruct
  public void getAllProperties() {

    log.info("All server properties being loaded");

    List<ServerConfigProperties> listProps = new ArrayList<>();
    List<String> allowedKeys = Arrays.asList("spring.", "klaw.");

    if (env instanceof ConfigurableEnvironment) {
      for (PropertySource<?> propertySource :
          ((ConfigurableEnvironment) env).getPropertySources()) {
        if (propertySource instanceof EnumerablePropertySource) {
          for (String key : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {

            ServerConfigProperties props = new ServerConfigProperties();
            props.setKey(key);
            if (key.contains("password")
                || key.contains("license")
                || key.contains("pwd")
                || key.contains("cert")
                || key.contains("secret")) {
              props.setValue("*******");
            } else {
              props.setValue(WordUtils.wrap(propertySource.getProperty(key) + "", 125, "\n", true));
            }
            if (!checkPropertyExists(listProps, key)
                && !key.toLowerCase().contains("path")
                && !key.contains("secretkey")
                && !key.contains("password")
                && !key.contains("username")) {
              if (allowedKeys.stream().anyMatch(key::startsWith)) {
                listProps.add(props);
              }
            }
          }
        }
      }
    }
    ServerConfigService.listProps = listProps;
  }

  public List<ServerConfigProperties> getAllProps() {
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_SERVERCONFIG)) {
      return new ArrayList<>();
    }
    return listProps;
  }

  private boolean checkPropertyExists(List<ServerConfigProperties> props, String key) {
    for (ServerConfigProperties serverProps : props) {
      if (Objects.equals(serverProps.getKey(), key)) {
        return true;
      }
    }
    return false;
  }

  public List<Map<String, String>> getAllEditableProps() {
    List<Map<String, String>> listMap = new ArrayList<>();
    Map<String, String> resultMap = new HashMap<>();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_SERVERCONFIG)) {
      resultMap.put("result", ApiResultStatus.NOT_AUTHORIZED.value);
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
        TenantConfig dynamicObj;
        try {
          dynamicObj = OBJECT_MAPPER.readValue(kwVal, TenantConfig.class);
          updateEnvNameValues(dynamicObj, tenantId);
          kwVal = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(dynamicObj);
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

    if (tenantId != KwConstants.DEFAULT_TENANT_ID) {
      return listMap.stream()
          .filter(item -> KwConstants.allowConfigForAdmins.contains(item.get("kwkey")))
          .collect(Collectors.toList());
    } else {
      return listMap;
    }
  }

  public ApiResponse updateKwCustomProperty(KwPropertiesModel kwPropertiesModel)
      throws KlawException {
    log.info("updateKwCustomProperty {}", kwPropertiesModel);
    int tenantId = commonUtilsService.getTenantId(getUserName());
    String kwKey = kwPropertiesModel.getKwKey();
    String kwVal = kwPropertiesModel.getKwValue().trim();

    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.UPDATE_SERVERCONFIG)) {
      return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
    }

    // SUPERADMINS filter
    if (tenantId != KwConstants.DEFAULT_TENANT_ID) {
      if (!KwConstants.allowConfigForAdmins.contains(kwKey)) {
        return ApiResponse.builder().result(ApiResultStatus.NOT_AUTHORIZED.value).build();
      }
    }

    try {
      if (KwConstants.TENANT_CONFIG_PROPERTY.equals(kwKey)) {
        TenantConfig dynamicObj;
        try {
          dynamicObj = OBJECT_MAPPER.readValue(kwVal, TenantConfig.class);
          if (validateTenantConfig(dynamicObj, tenantId)) {
            updateEnvIdValues(dynamicObj);
            kwPropertiesModel.setKwValue(OBJECT_MAPPER.writeValueAsString(dynamicObj));
          } else {
            return ApiResponse.builder()
                .result(
                    "Failure. Invalid json / incorrect name values. Check tenant and env details.")
                .build();
          }
        } catch (IOException e) {
          log.error("Exception:", e);
          return ApiResponse.builder()
              .result("Failure. Invalid json values. Please check if tenant/environments exist.")
              .build();
        }
      }
    } catch (KlawException klawException) {
      return ApiResponse.builder().result("Failure. " + klawException.getMessage()).build();

    } catch (Exception e) {
      log.error("Exception:", e);
      return ApiResponse.builder()
          .result("Failure. Please check if the environment names exist.")
          .build();
    }

    try {
      KwProperties kwProperties = new KwProperties();
      copyProperties(kwPropertiesModel, kwProperties);
      String res = manageDatabase.getHandleDbRequests().updateKwProperty(kwProperties, tenantId);
      if (ApiResultStatus.SUCCESS.value.equals(res)) {
        commonUtilsService.updateMetadata(
            tenantId, EntityType.PROPERTIES, MetadataOperationType.CREATE);
        return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).data(kwKey).build();
      } else {
        return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
      }
    } catch (Exception e) {
      throw new KlawException(e.getMessage());
    }
  }

  private void updateEnvNameValues(TenantConfig dynamicObj, int tenantId) {
    if (dynamicObj.getTenantModel() != null) {
      KwTenantConfigModel tenant = dynamicObj.getTenantModel();

      // syncClusterName update
      if (tenant.getBaseSyncEnvironment() != null) {
        tenant.setBaseSyncEnvironment(
            getEnvDetails(tenant.getBaseSyncEnvironment(), tenantId).getName());
      }

      // syncClusterKafkaConnectName update
      if (tenant.getBaseSyncKafkaConnectCluster() != null) {
        tenant.setBaseSyncKafkaConnectCluster(
            getKafkaConnectEnvDetails(tenant.getBaseSyncKafkaConnectCluster(), tenantId).getName());
      }

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
                  if (getKafkaConnectEnvDetails(a, tenantId) != null) {
                    tmpOrderList1.add(getKafkaConnectEnvDetails(a, tenantId).getName());
                  }
                });
        tenant.setOrderOfConnectorsPromotionEnvsList(tmpOrderList1);
      }
      // The Schema Entry is saved in the DB IN ORDER as provided by the user with the Schema ID
      // Here we must translate the Schema ID back to the Schema Name for human readablity while also maintaining the provided order.
      // The schema promotion list is processed if it is not null and cycled through matching individual schema IDs against a list of Schema Envs retrieved from the DB.
      if (tenant.getOrderOfSchemaPromotionEnvsList() != null) {
        List<String> tmpSchemaOrderList1 = new ArrayList<>();
        tenant
            .getOrderOfSchemaPromotionEnvsList()
            .forEach(
                a -> {
                  if (getSchemaEnvDetails(a, tenantId) != null) {
                    tmpSchemaOrderList1.add(getSchemaEnvDetails(a, tenantId).getName());
                  }
                });
        tenant.setOrderOfSchemaPromotionEnvsList(tmpSchemaOrderList1);
      }

      // kafka
      if (tenant.getRequestTopicsEnvironmentsList() != null) {
        List<String> tmpReqTopicList = new ArrayList<>();
        tenant
            .getRequestTopicsEnvironmentsList()
            .forEach(
                a -> {
                  if (getEnvDetails(a, tenantId) != null) {
                    tmpReqTopicList.add(getEnvDetails(a, tenantId).getName());
                  }
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
                  if (getKafkaConnectEnvDetails(a, tenantId) != null) {
                    tmpReqTopicList1.add(getKafkaConnectEnvDetails(a, tenantId).getName());
                  }
                });
        tenant.setRequestConnectorsEnvironmentsList(tmpReqTopicList1);
      }

      // The Schema Entry is saved in the DB with the Schema ID
      // Here we must translate the Schema ID back to the Schema Name for human readablity.
      // The schema Request list is processed if it is not null and cycled through matching individual schema IDs against a list of Schema Envs retrieved from the DB.
      if (tenant.getRequestSchemaEnvironmentsList() != null) {
        List<String> tmpSchemaRequest = new ArrayList<>();
        tenant
            .getRequestSchemaEnvironmentsList()
            .forEach(
                a -> {
                  if (getSchemaEnvDetails(a, tenantId) != null) {
                    tmpSchemaRequest.add(getSchemaEnvDetails(a, tenantId).getName());
                  }
                });
        tenant.setRequestSchemaEnvironmentsList(tmpSchemaRequest);
      }
    }
  }

  private void updateEnvIdValues(TenantConfig dynamicObj) {
    if (dynamicObj.getTenantModel() != null) {
      KwTenantConfigModel tenantModel = dynamicObj.getTenantModel();

      // syncClusterName update
      if (tenantModel.getBaseSyncEnvironment() != null) {
        tenantModel.setBaseSyncEnvironment(
            getEnvDetailsFromName(
                    tenantModel.getBaseSyncEnvironment(),
                    getTenantIdFromName(tenantModel.getTenantName()))
                .getId());
      }

      // syncClusterKafkaConnectName update
      if (tenantModel.getBaseSyncKafkaConnectCluster() != null) {
        tenantModel.setBaseSyncKafkaConnectCluster(
            getKafkaConnectEnvDetailsFromName(
                    tenantModel.getBaseSyncKafkaConnectCluster(),
                    getTenantIdFromName(tenantModel.getTenantName()))
                .getId());
      }

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

      // The Tenant Model is updated where the Schema Name is changed to the Schema ID when saved in the DB.
      // Here we check the list is not null and cycle through each name mapping them to the correct Schema ENV ID so it can be stored correctly.
      // In this case the order is maintained as it is provided by the user for promotion purposes.
      if (tenantModel.getOrderOfSchemaPromotionEnvsList() != null) {
        List<String> tmpSchemaOrderList = new ArrayList<>();
        tenantModel
            .getOrderOfSchemaPromotionEnvsList()
            .forEach(
                a ->
                    tmpSchemaOrderList.add(
                        getSchemaEnvDetailsFromName(
                                a, getTenantIdFromName(tenantModel.getTenantName()))
                            .getId()));
        tenantModel.setOrderOfSchemaPromotionEnvsList(tmpSchemaOrderList);
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

      // Schema Registry
      // The Tenant Model is updated where the Schema Name is changed to the Schema ID when saved in the DB.
      //Here we check the list is not null and cycle through each name mapping them to the correct Schema ENV ID so it can be stored correctly.
      if (tenantModel.getRequestSchemaEnvironmentsList() != null) {
        List<String> tmpReqSchemaList = new ArrayList<>();
        tenantModel
            .getRequestSchemaEnvironmentsList()
            .forEach(
                a ->
                    tmpReqSchemaList.add(
                        getSchemaEnvDetailsFromName(
                                a, getTenantIdFromName(tenantModel.getTenantName()))
                            .getId()));
        tenantModel.setRequestSchemaEnvironmentsList(tmpReqSchemaList);
      }
    }
  }

  private boolean validateTenantConfig(TenantConfig dynamicObj, int tenantId) throws KlawException {
    Map<Integer, String> tenantMap = manageDatabase.getTenantMap();
    List<Env> envList = manageDatabase.getKafkaEnvList(tenantId);
    List<Env> envKafkaConnectList = manageDatabase.getKafkaConnectEnvList(tenantId);
    List<Env> schemaList = manageDatabase.getSchemaRegEnvList(tenantId);

    List<String> envListStr = new ArrayList<>();
    envList.forEach(a -> envListStr.add(a.getName()));

    List<String> envListKafkaConnectStr = new ArrayList<>();
    envKafkaConnectList.forEach(a -> envListKafkaConnectStr.add(a.getName()));

    List<String> envListSchemaRegistryStr = new ArrayList<>();
    schemaList.forEach(a -> envListSchemaRegistryStr.add(a.getName()));

    boolean tenantCheck;
    try {
      tenantCheck = tenantMap.containsValue(dynamicObj.getTenantModel().getTenantName());

      if (tenantCheck) {
        KwTenantConfigModel tenantModel = dynamicObj.getTenantModel();
        // syncClusterCheck
        isBaseSyncValid(tenantModel.getBaseSyncEnvironment(), envListStr);
        // syncClusterKafkaConnectCheck
        isBaseSyncValid(tenantModel.getBaseSyncKafkaConnectCluster(), envListKafkaConnectStr);
        // orderOfenvs check
        isResourceAlreadyCreated(tenantModel.getOrderOfTopicPromotionEnvsList(), envListStr);
        // check that all Kafka Connectors in the ordered list already exist as resources.
        isResourceAlreadyCreated(
            tenantModel.getOrderOfConnectorsPromotionEnvsList(), envListKafkaConnectStr);
        // Check that all schema registries exist that have been defined in the config.
        isResourceAlreadyCreated(
            tenantModel.getOrderOfSchemaPromotionEnvsList(), envListSchemaRegistryStr);
        // requestTopic Check
        isResourceAlreadyCreated(tenantModel.getRequestTopicsEnvironmentsList(), envListStr);
        // requestConnectors check
        isResourceAlreadyCreated(
            tenantModel.getRequestConnectorsEnvironmentsList(), envListKafkaConnectStr);
        // Schema Check
        isResourceAlreadyCreated(
            tenantModel.getRequestSchemaEnvironmentsList(), envListSchemaRegistryStr);
      }
    } catch (Exception e) {
      log.error(dynamicObj + "", e);
      throw e;
    }

    return tenantCheck;
  }

  private void isBaseSyncValid(String baseSync, List<String> existingResources)
      throws KlawException {
    if (baseSync != null && !existingResources.contains(baseSync)) {
      throw new KlawException(
          "Base Sync Resource "
              + baseSync
              + " must be created before being added to the Tenant Model");
    }
  }

  private void isResourceAlreadyCreated(List<String> namedResources, List<String> existingResources)
      throws KlawException {

    if (namedResources != null) {
      for (String res : namedResources) {
        if (!existingResources.contains(res)) {
          throw new KlawException(
              "Resource " + res + " must be created before being added to the Tenant Model");
        }
      }
    }
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

  public Env getSchemaEnvDetails(String envId, int tenantId) {
    Optional<Env> envFound =
        manageDatabase.getSchemaRegEnvList(tenantId).stream()
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

  public Env getSchemaEnvDetailsFromName(String envName, Integer tenantId) {
    Optional<Env> envFound =
        manageDatabase.getSchemaRegEnvList(tenantId).stream()
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
    if ("ONLINE".equals(clusterApiStatus)) {
      clusterApiStatus = "successful.";
    } else {
      clusterApiStatus = "failure.";
    }
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
    return mailService.getUserName(getPrincipal());
  }
}
