package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
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
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApacheKafkaTopicService {

  private static final long TIME_OUT_SECS_FOR_TOPICS = 5;

  private final ClusterApiUtils clusterApiUtils;

  public ApacheKafkaTopicService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public synchronized Set<Map<String, String>> loadTopics(
      String environment, KafkaSupportedProtocol protocol, String clusterName) throws Exception {
    log.info("loadTopics {} {}", environment, protocol);
    AdminClient client = clusterApiUtils.getAdminClient(environment, protocol, clusterName);
    Set<Map<String, String>> topics = new HashSet<>();
    if (client == null) {
      throw new Exception("Cannot connect to cluster.");
    }

    ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
    listTopicsOptions = listTopicsOptions.listInternal(false);

    ListTopicsResult topicsResult = client.listTopics(listTopicsOptions);

    try {
      DescribeTopicsResult s = client.describeTopics(new ArrayList<>(topicsResult.names().get()));
      Map<String, TopicDescription> topicDesc =
          s.all().get(TIME_OUT_SECS_FOR_TOPICS, TimeUnit.SECONDS);
      Set<String> keySet = topicDesc.keySet();
      keySet.remove("_schemas");
      List<String> lstK = new ArrayList<>(keySet);
      Map<String, String> hashMap;
      for (String topicName : lstK) {
        if (topicName.startsWith("_confluent")) {
          continue;
        }
        hashMap = new HashMap<>();
        hashMap.put("topicName", topicName);
        hashMap.put(
            "replicationFactor",
            "" + topicDesc.get(topicName).partitions().get(0).replicas().size());
        hashMap.put("partitions", "" + topicDesc.get(topicName).partitions().size());
        topics.add(hashMap);
      }

    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Exception:", e);
    }
    return topics;
  }

  public synchronized ApiResponse createTopic(ClusterTopicRequest clusterTopicRequest)
      throws Exception {
    log.info("createTopic {}", clusterTopicRequest);
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
    } catch (ExecutionException | InterruptedException e) {
      String errorMessage;
      if (e instanceof ExecutionException) {
        errorMessage = e.getCause().getMessage();
      } else {
        Thread.currentThread().interrupt();
        errorMessage = e.getMessage();
      }
      log.error("Unable to create topic {}, {}", clusterTopicRequest.getTopicName(), errorMessage);
      throw e;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw e;
    }

    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
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
      Map<String, NewPartitions> newPartitionSet = new HashMap<>();
      newPartitionSet.put(
          clusterTopicRequest.getTopicName(),
          NewPartitions.increaseTo(clusterTopicRequest.getPartitions()));

      client.createPartitions(newPartitionSet);
    }

    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
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
      return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
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
      log.error("Unable to delete topic {}, {}", clusterTopicRequest.getTopicName(), errorMessage);
      throw e;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw e;
    }
  }
}
