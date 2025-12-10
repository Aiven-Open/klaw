package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterKeyIdentifier;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.LoadTopicsResponse;
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
import org.apache.kafka.clients.admin.AlterConfigOp;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApacheKafkaTopicService {
  private final ClusterApiUtils clusterApiUtils;

  private final SchemaService schemaService;

  private static Map<ClusterKeyIdentifier, Set<TopicConfig>> cachedTopics = new HashMap<>();

  private static Set<ClusterKeyIdentifier> topicCacheKeySets = new HashSet<>();

  private static Map<ClusterKeyIdentifier, Boolean> topicsLoadingStatusOfClusters = new HashMap<>();

  public ApacheKafkaTopicService(ClusterApiUtils clusterApiUtils, SchemaService schemaService) {
    this.clusterApiUtils = clusterApiUtils;
    this.schemaService = schemaService;
  }

  public synchronized LoadTopicsResponse loadTopics(
      String environment,
      KafkaSupportedProtocol protocol,
      String clusterIdentification,
      boolean resetCache)
      throws Exception {
    log.info("loadTopics {} {}", environment, protocol);
    AdminClient client =
        clusterApiUtils.getAdminClient(environment, protocol, clusterIdentification);
    Set<TopicConfig> topics = new HashSet<>();
    if (client == null) {
      throw new Exception("Cannot connect to cluster.");
    }

    ClusterKeyIdentifier clusterKeyIdentifier =
        new ClusterKeyIdentifier(environment, protocol, clusterIdentification);

    boolean topicsLoadingStatus = false;
    if (topicsLoadingStatusOfClusters.containsKey(clusterKeyIdentifier)) {
      topicsLoadingStatus = topicsLoadingStatusOfClusters.get(clusterKeyIdentifier);
    } else {
      topicsLoadingStatusOfClusters.put(clusterKeyIdentifier, false);
    }

    if (topicsLoadingStatus) {
      return LoadTopicsResponse.builder()
          .loadingInProgress(true)
          .topicConfigSet(new HashSet<>())
          .build();
    } else {
      topicsLoadingStatusOfClusters.put(clusterKeyIdentifier, true);
    }

    if (resetCache || !cachedTopics.containsKey(clusterKeyIdentifier)) {
      loadTopicsForCache(client, topics, clusterKeyIdentifier);
    } else {
      topics = cachedTopics.get(clusterKeyIdentifier);
    }

    topicsLoadingStatusOfClusters.put(clusterKeyIdentifier, false);
    return LoadTopicsResponse.builder().loadingInProgress(false).topicConfigSet(topics).build();
  }

  private void loadTopicsForCache(
      AdminClient client, Set<TopicConfig> topics, ClusterKeyIdentifier clusterKeyIdentifier) {
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
      updateCache(clusterKeyIdentifier, topics);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Exception:", e);
    }
  }

  private void updateCache(
      ClusterKeyIdentifier clusterKeyIdentifier, Set<TopicConfig> topicConfigSet) {
    cachedTopics.put(clusterKeyIdentifier, topicConfigSet);
    topicCacheKeySets.add(clusterKeyIdentifier);
  }

  private Map<String, TopicDescription> loadTopicDescriptionsMap(AdminClient client)
      throws InterruptedException, ExecutionException, TimeoutException {
    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions = listTopicsOptions.listInternal(false);

    ListTopicsResult topicsResult = client.listTopics(listTopicsOptions);
    DescribeTopicsResult describeTopicsResult =
        client.describeTopics(new ArrayList<>(topicsResult.names().get()));

    return describeTopicsResult
        .allTopicNames()
        .get(clusterApiUtils.getAdminClientProperties().getTopicsTimeoutSecs(), TimeUnit.SECONDS);
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
          .get(clusterApiUtils.getAdminClientProperties().getTopicsTimeoutSecs(), TimeUnit.SECONDS);
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
        describeTopicsResult.allTopicNames().get().get(clusterTopicRequest.getTopicName());
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

    // Describe topic
    DescribeTopicsResult describeTopicsResult =
        client.describeTopics(Collections.singleton(clusterTopicRequest.getTopicName()));

    TopicDescription topicDescription =
        describeTopicsResult
            .allTopicNames()
            .get(
                clusterApiUtils.getAdminClientProperties().getTopicsTimeoutSecs(), TimeUnit.SECONDS)
            .get(clusterTopicRequest.getTopicName());

    int currentPartitions = topicDescription.partitions().size();
    int requestedPartitions = clusterTopicRequest.getPartitions();

    // If partitions need to decrease, delete and recreate topic
    if (currentPartitions > requestedPartitions) {
      deleteTopic(clusterTopicRequest);
      createTopic(clusterTopicRequest);
    } else {
      // Increase partitions if needed
      if (currentPartitions < requestedPartitions) {
        Map<String, NewPartitions> newPartitionSet = new HashMap<>();
        newPartitionSet.put(
            clusterTopicRequest.getTopicName(), NewPartitions.increaseTo(requestedPartitions));
        client.createPartitions(newPartitionSet).all().get();
      }

      // Update advanced topic configuration using incrementalAlterConfigs
      Map<String, String> advancedConfig = clusterTopicRequest.getAdvancedTopicConfiguration();
      if (!advancedConfig.isEmpty()) {
        ConfigResource configResource =
            new ConfigResource(ConfigResource.Type.TOPIC, clusterTopicRequest.getTopicName());

        Collection<AlterConfigOp> ops = new ArrayList<>();
        for (Map.Entry<String, String> entry : advancedConfig.entrySet()) {
          ops.add(
              new AlterConfigOp(
                  new ConfigEntry(entry.getKey(), entry.getValue()), AlterConfigOp.OpType.SET));
        }

        Map<ConfigResource, Collection<AlterConfigOp>> updateOps = new HashMap<>();
        updateOps.put(configResource, ops);

        client.incrementalAlterConfigs(updateOps).all().get();
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
          .topicNameValues()
          .get(clusterTopicRequest.getTopicName())
          .get(clusterApiUtils.getAdminClientProperties().getTopicsTimeoutSecs(), TimeUnit.SECONDS);

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

  @Async("resetTopicsCacheTaskExecutor")
  @Scheduled(
      cron = "${klaw.topics.cron.expression:0 0 0 * * ?}",
      zone = "${klaw.topics.cron.expression.timezone:UTC}")
  public void resetTopicsCacheScheduler() {
    topicCacheKeySets.forEach(
        clusterKeyIdentifier -> {
          try {
            log.info("Loading topics {}", clusterKeyIdentifier);
            loadTopics(
                clusterKeyIdentifier.getBootstrapServers(),
                clusterKeyIdentifier.getProtocol(),
                clusterKeyIdentifier.getClusterIdentification(),
                true);
          } catch (Exception e) {
            log.error("Error while loading topics {}", clusterKeyIdentifier);
          }
        });
  }
}
