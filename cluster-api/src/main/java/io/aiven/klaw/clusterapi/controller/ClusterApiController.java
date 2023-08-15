package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.ServiceAccountDetails;
import io.aiven.klaw.clusterapi.models.TopicConfig;
import io.aiven.klaw.clusterapi.models.enums.AclType;
import io.aiven.klaw.clusterapi.models.enums.AclsNativeType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.AivenApiService;
import io.aiven.klaw.clusterapi.services.ApacheKafkaAclService;
import io.aiven.klaw.clusterapi.services.ApacheKafkaTopicService;
import io.aiven.klaw.clusterapi.services.ConfluentCloudApiService;
import io.aiven.klaw.clusterapi.services.UtilComponentsService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Slf4j
@AllArgsConstructor
public class ClusterApiController {

  UtilComponentsService utilComponentsService;

  ApacheKafkaAclService apacheKafkaAclService;

  ApacheKafkaTopicService apacheKafkaTopicService;

  AivenApiService aivenApiService;

  ConfluentCloudApiService confluentCloudApiService;

  @RequestMapping(
      value = "/getApiStatus",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ClusterStatus> getApiStatus() {
    return new ResponseEntity<>(ClusterStatus.ONLINE, HttpStatus.OK);
  }

  //    @RequestMapping(value = "/reloadTruststore/{protocol}/{clusterName}", method =
  // RequestMethod.GET,produces = {MediaType.APPLICATION_JSON_VALUE})
  //    public ResponseEntity<String> reloadTruststore(@PathVariable String protocol,
  //                                                   @PathVariable String clusterName){
  //        return new ResponseEntity<>(manageKafkaComponents.reloadTruststore(protocol,
  // clusterName), HttpStatus.OK);
  //    }

  @RequestMapping(
      value =
          "/getStatus/{bootstrapServers}/{protocol}/{clusterName}/{clusterType}/kafkaFlavor/{kafkaFlavor}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ClusterStatus> getStatus(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName,
      @PathVariable String clusterType,
      @PathVariable String kafkaFlavor) {
    return new ResponseEntity<>(
        utilComponentsService.getStatus(
            bootstrapServers, protocol, clusterName, clusterType, kafkaFlavor),
        HttpStatus.OK);
  }

  @RequestMapping(
      value =
          "/getTopics/{bootstrapServers}/{protocol}/{clusterName}/topicsNativeType/{aclsNativeType}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Set<TopicConfig>> getTopics(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName,
      @PathVariable String aclsNativeType)
      throws Exception {
    Set<TopicConfig> topics;
    if (AclsNativeType.CONFLUENT_CLOUD.name().equals(aclsNativeType)) {
      topics = confluentCloudApiService.listTopics(bootstrapServers, protocol, clusterName);
    } else {
      topics = apacheKafkaTopicService.loadTopics(bootstrapServers, protocol, clusterName);
    }
    return new ResponseEntity<>(topics, HttpStatus.OK);
  }

  @RequestMapping(
      value =
          "/getAcls/{bootstrapServers}/{aclsNativeType}/{protocol}/{clusterName}/{projectName}/{serviceName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Set<Map<String, String>>> getAcls(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName,
      @PathVariable String aclsNativeType,
      @PathVariable String projectName,
      @PathVariable String serviceName)
      throws Exception {
    Set<Map<String, String>> acls;
    if (AclsNativeType.NATIVE.name().equals(aclsNativeType)) {
      acls = apacheKafkaAclService.loadAcls(bootstrapServers, protocol, clusterName);
    } else if (AclsNativeType.CONFLUENT_CLOUD.name().equals(aclsNativeType)) {
      acls = confluentCloudApiService.listAcls(bootstrapServers, protocol, clusterName);
    } else {
      acls = aivenApiService.listAcls(projectName, serviceName);
    }
    return new ResponseEntity<>(acls, HttpStatus.OK);
  }

  /*
  Based on the project, service and username, service account details (in Aiven system) are retrieved.
   */
  @RequestMapping(
      value = "/serviceAccountDetails/project/{projectName}/service/{serviceName}/user/{userName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceAccountDetails> getServiceAccountCredentials(
      @PathVariable String projectName,
      @PathVariable String serviceName,
      @PathVariable String userName) {
    return new ResponseEntity<>(
        aivenApiService.getServiceAccountDetails(projectName, serviceName, userName),
        HttpStatus.OK);
  }

  /*
  Based on the project, service, user service accounts (in Aiven system) are retrieved.
   */
  @RequestMapping(
      value = "/serviceAccounts/project/{projectName}/service/{serviceName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> getServiceAccounts(
      @PathVariable String projectName, @PathVariable String serviceName) {
    ApiResponse apiResponse =
        ApiResponse.builder()
            .data(aivenApiService.getServiceAccountUsers(projectName, serviceName))
            .message(ApiResultStatus.SUCCESS.value)
            .success(true)
            .build();
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @PostMapping(
      value = "/createTopics",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createTopics(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      log.info("createTopics clusterTopicRequest {}", clusterTopicRequest);
      if (AclsNativeType.CONFLUENT_CLOUD == clusterTopicRequest.getAclsNativeType()) {
        return new ResponseEntity<>(
            confluentCloudApiService.createTopic(clusterTopicRequest), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(
            apacheKafkaTopicService.createTopic(clusterTopicRequest), HttpStatus.OK);
      }

    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(
      value = "/updateTopics",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateTopics(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      log.info("updateTopics clusterTopicRequest {}", clusterTopicRequest);
      if (AclsNativeType.CONFLUENT_CLOUD == clusterTopicRequest.getAclsNativeType()) {
        return new ResponseEntity<>(
            confluentCloudApiService.updateTopic(clusterTopicRequest), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(
            apacheKafkaTopicService.updateTopic(clusterTopicRequest), HttpStatus.OK);
      }
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(
      value = "/deleteTopics",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteTopics(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      if (AclsNativeType.CONFLUENT_CLOUD == clusterTopicRequest.getAclsNativeType()) {
        return new ResponseEntity<>(
            confluentCloudApiService.deleteTopic(clusterTopicRequest), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(
            apacheKafkaTopicService.deleteTopic(clusterTopicRequest), HttpStatus.OK);
      }
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(
      value = "/createAcls",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createAcls(
      @RequestBody @Valid ClusterAclRequest clusterAclRequest) {

    Map<String, String> resultMap = new HashMap<>();
    String result;
    try {
      if (AclsNativeType.NATIVE.name().equals(clusterAclRequest.getAclNativeType())) {
        if (AclType.PRODUCER.value.equals(clusterAclRequest.getAclType())) {
          result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);
        } else {
          result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);
        }
        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(result.equals(ApiResultStatus.SUCCESS.value))
                .message(result)
                .build(),
            HttpStatus.OK);
      } else if (AclsNativeType.AIVEN.name().equals(clusterAclRequest.getAclNativeType())) {
        resultMap = aivenApiService.createAcls(clusterAclRequest);
        String resultStatus = resultMap.get("result");
        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(resultStatus.equals(ApiResultStatus.SUCCESS.value))
                .message(resultStatus)
                .data(resultMap)
                .build(),
            HttpStatus.OK);
      } else if (AclsNativeType.CONFLUENT_CLOUD
          .name()
          .equals(clusterAclRequest.getAclNativeType())) {
        resultMap = confluentCloudApiService.createAcls(clusterAclRequest);
        String resultStatus = resultMap.get("result");
        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(resultStatus.equals(ApiResultStatus.SUCCESS.value))
                .message(resultStatus)
                .data(resultMap)
                .build(),
            HttpStatus.OK);
      }
    } catch (Exception e) {
      resultMap.put("result", "failure " + e.getMessage());
      return new ResponseEntity<>(
          ApiResponse.builder().success(false).message("failure " + e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    resultMap.put("result", "Not a valid request");
    return new ResponseEntity<>(
        ApiResponse.builder().success(false).message("Not a valid request").build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PostMapping(
      value = "/deleteAcls",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteAcls(
      @RequestBody @Valid ClusterAclRequest clusterAclRequest) {
    String result;
    try {
      if (AclsNativeType.NATIVE.name().equals(clusterAclRequest.getAclNativeType())) {

        if (AclType.PRODUCER.value.equals(clusterAclRequest.getAclType())) {
          result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);
        } else {
          result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);
        }

        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(result.equals(ApiResultStatus.SUCCESS.value))
                .message(result)
                .build(),
            HttpStatus.OK);
      } else if (AclsNativeType.AIVEN.name().equals(clusterAclRequest.getAclNativeType())) {
        result = aivenApiService.deleteAcls(clusterAclRequest);

        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(result.equals(ApiResultStatus.SUCCESS.value))
                .message(result)
                .build(),
            HttpStatus.OK);
      } else if (AclsNativeType.CONFLUENT_CLOUD
          .name()
          .equals(clusterAclRequest.getAclNativeType())) {
        result = confluentCloudApiService.deleteAcls(clusterAclRequest);

        return new ResponseEntity<>(
            ApiResponse.builder()
                .success(result.equals(ApiResultStatus.SUCCESS.value))
                .message(result)
                .build(),
            HttpStatus.OK);
      }

    } catch (Exception e) {
      return handleException(e);
    }
    return new ResponseEntity<>(
        ApiResponse.builder().success(false).message("Not a valid request").build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static ResponseEntity<ApiResponse> handleException(Exception e) {
    log.error("Exception:", e);
    return new ResponseEntity<>(
        ApiResponse.builder().success(false).message(e.getMessage()).build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
