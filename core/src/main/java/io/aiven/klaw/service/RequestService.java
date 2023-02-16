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

  @Autowired private SchemaRegstryControllerService schemaRegstryControllerService;

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
          return schemaRegstryControllerService.execSchemaRequests(reqId);
        case CONNECTOR:
          return kafkaConnectControllerService.approveConnectorRequests(reqId);
        default:
          return ApiResponse.builder()
              .result("Failure Unable to determine target resource.")
              .build();
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

  private ApiResponse processDeclineRequests(
      String reqId, String reason, RequestEntityType requestEntityType) {
    try {
      switch (requestEntityType) {
        case TOPIC:
          return topicControllerService.declineTopicRequests(reqId, reason);
        case ACL:
          return aclControllerService.declineAclRequests(reqId, reason);
        case SCHEMA:
          return schemaRegstryControllerService.execSchemaRequestsDecline(reqId, reason);
        case CONNECTOR:
          return kafkaConnectControllerService.declineConnectorRequests(reqId, reason);
        default:
          return ApiResponse.builder()
              .result("Failure Unable to determine target resource.")
              .build();
      }

    } catch (Exception ex) {

      return ApiResponse.builder().result("Failure unable to approve requestId " + reqId).build();
    }
  }
}
