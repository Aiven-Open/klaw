package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.AclType;
import io.aiven.klaw.clusterapi.models.AclsNativeType;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterStatus;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.AivenApiService;
import io.aiven.klaw.clusterapi.services.ApacheKafkaAclService;
import io.aiven.klaw.clusterapi.services.ApacheKafkaTopicService;
import io.aiven.klaw.clusterapi.services.MonitoringService;
import io.aiven.klaw.clusterapi.services.SchemaService;
import io.aiven.klaw.clusterapi.services.UtilComponentsService;
import java.util.*;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
@Slf4j
@AllArgsConstructor
public class ClusterApiController {

  UtilComponentsService utilComponentsService;

  ApacheKafkaAclService apacheKafkaAclService;

  ApacheKafkaTopicService apacheKafkaTopicService;

  SchemaService schemaService;

  MonitoringService monitoringService;

  AivenApiService aivenApiService;

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
      value = "/getStatus/{bootstrapServers}/{protocol}/{clusterName}/{clusterType}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ClusterStatus> getStatus(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName,
      @PathVariable String clusterType) {
    return new ResponseEntity<>(
        utilComponentsService.getStatus(bootstrapServers, protocol, clusterName, clusterType),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopics/{bootstrapServers}/{protocol}/{clusterName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Set<Map<String, String>>> getTopics(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName)
      throws Exception {
    Set<Map<String, String>> topics =
        apacheKafkaTopicService.loadTopics(bootstrapServers, protocol, clusterName);
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
    } else {
      acls = aivenApiService.listAcls(projectName, serviceName);
    }
    return new ResponseEntity<>(acls, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSchema/{bootstrapServers}/{protocol}/{clusterIdentification}/{topicName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<Integer, Map<String, Object>>> getSchema(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String topicName,
      @PathVariable String clusterIdentification) {
    Map<Integer, Map<String, Object>> schema =
        schemaService.getSchema(bootstrapServers, protocol, clusterIdentification, topicName);
    return new ResponseEntity<>(schema, HttpStatus.OK);
  }

  @RequestMapping(
      value =
          "/getConsumerOffsets/{bootstrapServers}/{protocol}/{clusterName}/{consumerGroupId}/{topicName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<Map<String, String>>> getConsumerOffsets(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName,
      @PathVariable String consumerGroupId,
      @PathVariable String topicName)
      throws Exception {
    List<Map<String, String>> consumerOffsetDetails =
        monitoringService.getConsumerGroupDetails(
            consumerGroupId, topicName, bootstrapServers, protocol, clusterName);

    return new ResponseEntity<>(consumerOffsetDetails, HttpStatus.OK);
  }

  @PostMapping(value = "/createTopics")
  public ResponseEntity<ApiResponse> createTopics(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      log.info("createTopics clusterTopicRequest {}", clusterTopicRequest);
      return new ResponseEntity<>(
          apacheKafkaTopicService.createTopic(clusterTopicRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(value = "/updateTopics")
  public ResponseEntity<ApiResponse> updateTopics(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      log.info("updateTopics clusterTopicRequest {}", clusterTopicRequest);
      return new ResponseEntity<>(
          apacheKafkaTopicService.updateTopic(clusterTopicRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(value = "/deleteTopics")
  public ResponseEntity<ApiResponse> deleteTopics(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      return new ResponseEntity<>(
          apacheKafkaTopicService.deleteTopic(clusterTopicRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(value = "/createAcls")
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
        return new ResponseEntity<>(ApiResponse.builder().result(result).build(), HttpStatus.OK);
      } else if (AclsNativeType.AIVEN.name().equals(clusterAclRequest.getAclNativeType())) {
        resultMap = aivenApiService.createAcls(clusterAclRequest);
        return new ResponseEntity<>(
            ApiResponse.builder().result(resultMap.get("result")).data(resultMap).build(),
            HttpStatus.OK);
      }
    } catch (Exception e) {
      resultMap.put("result", "failure " + e.getMessage());
      return new ResponseEntity<>(
          ApiResponse.builder().result("failure " + e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    resultMap.put("result", "Not a valid request");
    return new ResponseEntity<>(
        ApiResponse.builder().result("Not a valid request").build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PostMapping(value = "/deleteAcls")
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

        return new ResponseEntity<>(ApiResponse.builder().result(result).build(), HttpStatus.OK);
      } else if (AclsNativeType.AIVEN.name().equals(clusterAclRequest.getAclNativeType())) {
        result = aivenApiService.deleteAcls(clusterAclRequest);

        return new ResponseEntity<>(ApiResponse.builder().result(result).build(), HttpStatus.OK);
      }

    } catch (Exception e) {
      return handleException(e);
    }
    return new ResponseEntity<>(
        ApiResponse.builder().result("Not a valid request").build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @PostMapping(value = "/postSchema")
  public ResponseEntity<ApiResponse> postSchema(
      @RequestBody @Valid ClusterSchemaRequest clusterSchemaRequest) {
    try {
      return new ResponseEntity<>(
          schemaService.registerSchema(clusterSchemaRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  private static ResponseEntity<ApiResponse> handleException(Exception e) {
    log.error("Exception:", e);
    return new ResponseEntity<>(
        ApiResponse.builder().result(e.getMessage()).build(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
