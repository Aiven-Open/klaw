package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UtilComponentsService {

  Environment env;

  ClusterApiUtils clusterApiUtils;

  SchemaService schemaService;

  KafkaConnectService kafkaConnectService;

  ConfluentCloudApiService confluentCloudApiService;

  public UtilComponentsService() {}

  public UtilComponentsService(Environment env, ClusterApiUtils clusterApiUtils) {
    this.env = env;
    this.clusterApiUtils = clusterApiUtils;
  }

  @Autowired
  public UtilComponentsService(
      Environment env,
      ClusterApiUtils clusterApiUtils,
      SchemaService schemaService,
      KafkaConnectService kafkaConnectService,
      ConfluentCloudApiService confluentCloudApiService) {
    this.env = env;
    this.clusterApiUtils = clusterApiUtils;
    this.schemaService = schemaService;
    this.kafkaConnectService = kafkaConnectService;
    this.confluentCloudApiService = confluentCloudApiService;
  }

  //    public String reloadTruststore(String protocol, String clusterName){
  //        getAdminClient.removeSSLElementFromAdminClientMap(protocol, clusterName);
  //        return ApiResultStatus.SUCCESS.value;
  //    }

  public ClusterStatus getStatus(
      String environment,
      KafkaSupportedProtocol protocol,
      String clusterIdentification,
      String clusterType,
      String kafkaFlavor) {
    log.info("getStatus {} {}", environment, protocol);
    return switch (clusterType) {
      case "kafka" -> getStatusKafka(environment, protocol, clusterIdentification, kafkaFlavor);
      case "schemaregistry" -> schemaService.getSchemaRegistryStatus(
          environment, protocol, clusterIdentification);
      case "kafkaconnect" -> kafkaConnectService.getKafkaConnectStatus(
          environment, protocol, clusterIdentification);
      default -> ClusterStatus.OFFLINE;
    };
  }

  private ClusterStatus getStatusKafka(
      String environment, KafkaSupportedProtocol protocol, String clusterName, String kafkaFlavor) {
    try {
      if (kafkaFlavor != null && kafkaFlavor.equals("Confluent Cloud")) {
        if (confluentCloudApiService.listTopics(environment, protocol, clusterName).size() >= 0)
          return ClusterStatus.ONLINE;
      } else {
        AdminClient client = clusterApiUtils.getAdminClient(environment, protocol, clusterName);
        if (client != null) {
          return ClusterStatus.ONLINE;
        }
      }

      return ClusterStatus.OFFLINE;
    } catch (Exception e) {
      log.error("Exception:", e);
      return ClusterStatus.OFFLINE;
    }
  }
}
