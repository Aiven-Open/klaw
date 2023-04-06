package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.REQ_SER_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.REQ_SER_ERR_102;
import static io.aiven.klaw.error.KlawErrorMessages.REQ_SER_ERR_103;
import static io.aiven.klaw.error.KlawErrorMessages.REQ_SER_ERR_104;

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
      return switch (requestEntityType) {
        case TOPIC -> topicControllerService.approveTopicRequests(reqId);
        case ACL -> aclControllerService.approveAclRequests(reqId);
        case SCHEMA -> schemaRegistryControllerService.execSchemaRequests(reqId);
        case CONNECTOR -> kafkaConnectControllerService.approveConnectorRequests(reqId);
        default -> undeterinableResource();
      };
    } catch (Exception ex) {
      return ApiResponse.builder()
          .success(false)
          .message(String.format(REQ_SER_ERR_101, reqId))
          .build();
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
      return switch (requestEntityType) {
        case TOPIC -> topicControllerService.deleteTopicRequests(reqId);
        case ACL -> aclControllerService.deleteAclRequests(reqId);
        case SCHEMA -> schemaRegistryControllerService.deleteSchemaRequests(reqId);
        case CONNECTOR -> kafkaConnectControllerService.deleteConnectorRequests(reqId);
        default -> undeterinableResource();
      };
    } catch (Exception ex) {
      return ApiResponse.builder()
          .success(false)
          .message(String.format(REQ_SER_ERR_102, reqId))
          .build();
    }
  }

  private static ApiResponse undeterinableResource() {
    return ApiResponse.builder().success(false).message(REQ_SER_ERR_103).build();
  }

  private ApiResponse processDeclineRequests(
      String reqId, String reason, RequestEntityType requestEntityType) {
    try {
      return switch (requestEntityType) {
        case TOPIC -> topicControllerService.declineTopicRequests(reqId, reason);
        case ACL -> aclControllerService.declineAclRequests(reqId, reason);
        case SCHEMA -> schemaRegistryControllerService.execSchemaRequestsDecline(reqId, reason);
        case CONNECTOR -> kafkaConnectControllerService.declineConnectorRequests(reqId, reason);
        default -> undeterinableResource();
      };
    } catch (Exception ex) {
      return ApiResponse.builder()
          .success(false)
          .message(String.format(REQ_SER_ERR_104, reqId))
          .build();
    }
  }
}
