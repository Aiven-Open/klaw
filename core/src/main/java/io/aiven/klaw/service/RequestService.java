package io.aiven.klaw.service;

import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.RequestVerdict;
import io.aiven.klaw.model.enums.RequestEntityType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequestService {

  @Autowired private SchemaRegistryControllerService schemaRegistryControllerService;

  @Autowired private KafkaConnectControllerService kafkaConnectControllerService;

  @Autowired private TopicControllerService topicControllerService;

  @Autowired private AclControllerService aclControllerService;

  public List<ApiResponse> processApprovalRequests(RequestVerdict requestVerdict) {
    return requestVerdict.getReqIds().stream()
        .map(req -> processApprovalRequests(req, requestVerdict.getRequestEntityType()))
        .collect(Collectors.toList());
  }

  private ApiResponse processApprovalRequests(String reqId, RequestEntityType requestEntityType) {
    try {
      switch (requestEntityType) {
        case TOPIC:
          return topicControllerService.approveTopicRequests(reqId);
        case ACL:
          return aclControllerService.approveAclRequests(reqId);
        case SCHEMA:
          return schemaRegistryControllerService.execSchemaRequests(reqId);
        case CONNECTOR:
          return kafkaConnectControllerService.approveConnectorRequests(reqId);
        default:
          return undeterinableResource();
      }
    } catch (Exception ex) {

      return ApiResponse.builder().result("Failure unable to approve requestId " + reqId).build();
    }
  }

  public List<ApiResponse> processDeclineRequests(RequestVerdict requestVerdict) {
    return requestVerdict.getReqIds().stream()
        .map(
            req ->
                processDeclineRequests(
                    req, requestVerdict.getReason(), requestVerdict.getRequestEntityType()))
        .collect(Collectors.toList());
  }

  public List<ApiResponse> processDeleteRequests(RequestVerdict requestVerdict) {
    return requestVerdict.getReqIds().stream()
        .map(req -> processDeleteRequest(req, requestVerdict.getRequestEntityType()))
        .collect(Collectors.toList());
  }

  private ApiResponse processDeleteRequest(String reqId, RequestEntityType requestEntityType) {
    try {
      switch (requestEntityType) {
        case TOPIC:
          return topicControllerService.deleteTopicRequests(reqId);
        case ACL:
          return aclControllerService.deleteAclRequests(reqId);
        case SCHEMA:
          return schemaRegistryControllerService.deleteSchemaRequests(reqId);
        case CONNECTOR:
          return kafkaConnectControllerService.deleteConnectorRequests(reqId);
        default:
          return undeterinableResource();
      }

    } catch (Exception ex) {
      return ApiResponse.builder().result("Failure unable to delete requestId " + reqId).build();
    }
  }

  private static ApiResponse undeterinableResource() {
    return ApiResponse.builder().result("Failure Unable to determine target resource.").build();
  }

  private ApiResponse processDeclineRequests(
      String reqId, String reason, RequestEntityType requestEntityType) {
    try {
      switch (requestEntityType) {
        case TOPIC:
          return topicControllerService.declineTopicRequests(reqId, reason);
        case ACL:
          return aclControllerService.declineAclRequests(reqId, reason);
        case SCHEMA:
          return schemaRegistryControllerService.execSchemaRequestsDecline(reqId, reason);
        case CONNECTOR:
          return kafkaConnectControllerService.declineConnectorRequests(reqId, reason);
        default:
          return undeterinableResource();
      }

    } catch (Exception ex) {

      return ApiResponse.builder().result("Failure unable to decline requestId " + reqId).build();
    }
  }
}
