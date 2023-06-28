package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.TopicConfig;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApacheKafkaTopicService {

  private static final long TIME_OUT_SECS_FOR_TOPICS = 5;

  private final ClusterApiUtils clusterApiUtils;

  private final SchemaService schemaService;

  public ApacheKafkaTopicService(ClusterApiUtils clusterApiUtils, SchemaService schemaService) {
    this.clusterApiUtils = clusterApiUtils;
    this.schemaService = schemaService;
  }

  public synchronized Set<TopicConfig> loadTopics(
      String environment, KafkaSupportedProtocol protocol, String clusterIdentification)
      throws Exception {
    log.info("loadTopics {} {}", environment, protocol);
    AdminClient client =
        clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification);
    Set<TopicConfig> topics = new HashSet<>();
    if (client == null) {
      throw new Exception("Cannot connect to cluster.");
    }

    try {
      Map<String, TopicDescription> topicDescriptionsPerAdminClient =
          loadTopicDescriptionsMap(client);

      Set<String> keySet = topicDescriptionsPerAdminClient.keySet();
      keySet.remove("_schemas");
      List<String> lstK = new ArrayList<>(keySet);
      TopicConfig topicConfig;
      for (String topicName : lstK) {
        if (topicName.startsWith("_confluent") || topicName.startsWith("__connect")) {
          continue;
        }
        topicConfig = new TopicConfig();
        topicConfig.setTopicName(topicName);
        TopicDescription topicDescription = topicDescriptionsPerAdminClient.get(topicName);
        topicConfig.setReplicationFactor(
            "" + topicDescription.partitions().get(0).replicas().size());
        topicConfig.setPartitions("" + topicDescription.partitions().size());
        topics.add(topicConfig);
      }

    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Exception:", e);
    }
    return topics;
  }

  private Map<String, TopicDescription> loadTopicDescriptionsMap(AdminClient client)
      throws InterruptedException, ExecutionException, TimeoutException {
    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions = listTopicsOptions.listInternal(false);

    ListTopicsResult topicsResult = client.listTopics(listTopicsOptions);
    DescribeTopicsResult describeTopicsResult =
        client.describeTopics(new ArrayList<>(topicsResult.names().get()));

    return describeTopicsResult.all().get(TIME_OUT_SECS_FOR_TOPICS, TimeUnit.SECONDS);
  }

  public synchronized ApiResponse createTopic(ClusterTopicRequest clusterTopicRequest)
      throws Exception {
    log.info("createTopic {}", clusterTopicRequest);
    AdminClient client =
        clusterApiUtils.getAdminClient(
            clusterTopicRequest.getEnv(),
            clusterTopicRequest.getProtocol(),
            clusterTopicRequest.getClusterName());
    if (client == null) {
      throw new Exception("Cannot connect to cluster.");
    }
    try {
      NewTopic topic =
          new NewTopic(
                  clusterTopicRequest.getTopicName(),
                  clusterTopicRequest.getPartitions(),
                  clusterTopicRequest.getReplicationFactor())
              .configs(clusterTopicRequest.getAdvancedTopicConfiguration());

      CreateTopicsResult result = client.createTopics(Collections.singletonList(topic));
      result
          .values()
          .get(clusterTopicRequest.getTopicName())
          .get(TIME_OUT_SECS_FOR_TOPICS, TimeUnit.SECONDS);
    } catch (KafkaException e) {
      log.error("Invalid properties: ", e);
      throw e;
    } catch (NumberFormatException e) {
      log.error("Invalid replica assignment string", e);
      throw e;
    } catch (ExecutionException e) {
      log.error(
          "Unable to create topic {}, {}",
          clusterTopicRequest.getTopicName(),
          e.getCause().getMessage());
      // TopicExistsException is wrapped in ExecutionException so we have to dig into the lower
      // exception.
      if (e.getMessage().contains("TopicExistsException")) {
        log.warn(
            "Topic: {} already exists in {}",
            clusterTopicRequest.getTopicName(),
            clusterTopicRequest.getEnv());

        if (checkIfTopicExistsWithSameConfig(clusterTopicRequest, client)) {
          return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
        }

        return ApiResponse.builder().success(false).message(e.getMessage()).build();
      }
      throw e;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error(
          "Unable to create topic {}, {}", clusterTopicRequest.getTopicName(), e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw e;
    }

    return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
  }

  // check if topic exists with same configuration as request
  private boolean checkIfTopicExistsWithSameConfig(
      ClusterTopicRequest clusterTopicRequest, AdminClient adminClient)
      throws ExecutionException, InterruptedException, TimeoutException {
    DescribeTopicsResult describeTopicsResult =
        adminClient.describeTopics(Collections.singletonList(clusterTopicRequest.getTopicName()));

    TopicDescription topicDescription =
        describeTopicsResult.all().get().get(clusterTopicRequest.getTopicName());
    return topicDescription.partitions().size() == clusterTopicRequest.getPartitions()
        && topicDescription.partitions().get(0).replicas().size()
            == clusterTopicRequest.getReplicationFactor();
  }

  public synchronized ApiResponse updateTopic(ClusterTopicRequest clusterTopicRequest)
      throws Exception {
    log.info("updateTopic Name: {}", clusterTopicRequest);

    AdminClient client =
        clusterApiUtils.getAdminClient(
            clusterTopicRequest.getEnv(),
            clusterTopicRequest.getProtocol(),
            clusterTopicRequest.getClusterName());

    if (client == null) {
      throw new Exception("Cannot connect to cluster.");
    }

    DescribeTopicsResult describeTopicsResult =
        client.describeTopics(Collections.singleton(clusterTopicRequest.getTopicName()));
    TopicDescription result =
        describeTopicsResult
            .all()
            .get(TIME_OUT_SECS_FOR_TOPICS, TimeUnit.SECONDS)
            .get(clusterTopicRequest.getTopicName());

    if (result.partitions().size() > clusterTopicRequest.getPartitions()) {
      // delete topic and recreate
      deleteTopic(clusterTopicRequest);
      createTopic(clusterTopicRequest);
    } else {
      // Update partitions
      Map<String, NewPartitions> newPartitionSet = new HashMap<>();
      newPartitionSet.put(
          clusterTopicRequest.getTopicName(),
          NewPartitions.increaseTo(clusterTopicRequest.getPartitions()));
      if (result.partitions().size() != clusterTopicRequest.getPartitions()) {
        client.createPartitions(newPartitionSet);
      }

      // Update advanced config
      ConfigResource configResource =
          new ConfigResource(ConfigResource.Type.TOPIC, clusterTopicRequest.getTopicName());
      Map<ConfigResource, Config> updateConfig = new HashMap<>();

      Map<String, String> advancedConfig = clusterTopicRequest.getAdvancedTopicConfiguration();
      Collection<ConfigEntry> entries = new ArrayList<>();
      for (String key : advancedConfig.keySet()) {
        ConfigEntry configEntry = new ConfigEntry(key, advancedConfig.get(key));
        entries.add(configEntry);
      }

      if (!advancedConfig.isEmpty()) {
        updateConfig.put(configResource, new Config(entries));
        client.alterConfigs(updateConfig);
      }
    }

    return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
  }

  public synchronized ApiResponse deleteTopic(ClusterTopicRequest clusterTopicRequest)
      throws Exception {
    log.info("deleteTopic Topic {}", clusterTopicRequest);

    AdminClient client;
    try {
      client =
          clusterApiUtils.getAdminClient(
              clusterTopicRequest.getEnv(),
              clusterTopicRequest.getProtocol(),
              clusterTopicRequest.getClusterName());
      if (client == null) {
        throw new Exception("Cannot connect to cluster.");
      }

      DeleteTopicsResult result =
          client.deleteTopics(Collections.singletonList(clusterTopicRequest.getTopicName()));
      result
          .values()
          .get(clusterTopicRequest.getTopicName())
          .get(TIME_OUT_SECS_FOR_TOPICS, TimeUnit.SECONDS);

      // delete associated schema if requested
      String schemaDeletionStatus = "";
      if (clusterTopicRequest.getDeleteAssociatedSchema()) {
        schemaDeletionStatus = schemaService.deleteSchema(clusterTopicRequest).getMessage();
        log.info("Schema deletion status : {}", schemaDeletionStatus);
      }
      return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
    } catch (KafkaException e) {
      log.error("Invalid properties: ", e);
      throw e;
    } catch (ExecutionException | InterruptedException e) {
      String errorMessage;
      if (e instanceof ExecutionException) {
        errorMessage = e.getCause().getMessage();
      } else {
        Thread.currentThread().interrupt();
        errorMessage = e.getMessage();
      }
      if ((e.getMessage().contains("UnknownTopicOrPartition"))) {
        return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
      }
      log.error("Unable to delete topic {}, {}", clusterTopicRequest.getTopicName(), errorMessage);
      throw e;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw e;
    }
  }
}
