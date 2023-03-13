package io.aiven.klaw.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawRestException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.RequestVerdict;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.service.AclControllerService;
import io.aiven.klaw.service.KafkaConnectControllerService;
import io.aiven.klaw.service.RequestService;
import io.aiven.klaw.service.SchemaRegstryControllerService;
import io.aiven.klaw.service.TopicControllerService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequestControllerTest {

  @Mock private SchemaRegstryControllerService schemaRegstryControllerService;

  @Mock private KafkaConnectControllerService kafkaConnectControllerService;

  @Mock private TopicControllerService topicControllerService;

  @Mock private AclControllerService aclControllerService;

  private RequestService service;

  private RequestController controller;

  @BeforeEach
  public void setUp() {
    service = new RequestService();
    controller = new RequestController();
    ReflectionTestUtils.setField(service, "topicControllerService", topicControllerService);
    ReflectionTestUtils.setField(service, "aclControllerService", aclControllerService);
    ReflectionTestUtils.setField(
        service, "kafkaConnectControllerService", kafkaConnectControllerService);
    ReflectionTestUtils.setField(
        service, "schemaRegstryControllerService", schemaRegstryControllerService);

    ReflectionTestUtils.setField(controller, "service", service);
  }

  @Order(1)
  @Test
  public void givenARequestToApproveCallCorrectServiceAndReturnSuccessOK() throws KlawException {
    when(topicControllerService.approveTopicRequests(eq("1001")))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(createRequestVerdict(RequestEntityType.TOPIC, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(topicControllerService, times(1)).approveTopicRequests(eq("1001"));
  }

  @Order(2)
  @Test
  public void givenARequestToApproveMulitpleCallTOPICCorrectServiceAndReturnSuccessOK()
      throws KlawException {
    when(topicControllerService.approveTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.TOPIC, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(topicControllerService, times(2)).approveTopicRequests(anyString());
  }

  @Order(3)
  @Test
  public void
      givenARequestToApproveMulitpleCallCorrectTOPICServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException {
    when(topicControllerService.approveTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.TOPIC, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(topicControllerService, times(2)).approveTopicRequests(anyString());
  }

  @Order(4)
  @Test
  public void givenARequestToApproveCallCorrectTOPICServiceAndReturnISEResponse()
      throws KlawException {
    when(topicControllerService.approveTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(createRequestVerdict(RequestEntityType.TOPIC, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(topicControllerService, times(1)).approveTopicRequests(anyString());
  }

  @Order(4)
  @Test
  public void givenMultipleRequestToApproveCallCorrectTOPICServiceAndReturnISEResponse()
      throws KlawException {
    when(topicControllerService.approveTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.TOPIC, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(topicControllerService, times(2)).approveTopicRequests(anyString());
  }

  @Order(5)
  @Test
  public void givenARequestToApproveMulitpleCallCorrectSCHEMAServiceAndReturnSuccessOK()
      throws KlawException {
    when(schemaRegstryControllerService.execSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(schemaRegstryControllerService, times(2)).execSchemaRequests(anyString());
  }

  @Order(6)
  @Test
  public void
      givenARequestToApproveMulitpleCallCorrectSCHEMAServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException {
    when(schemaRegstryControllerService.execSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(schemaRegstryControllerService, times(2)).execSchemaRequests(anyString());
  }

  @Order(7)
  @Test
  public void givenARequestToApproveCallCorrectSCHEMAServiceAndReturnISEResponse()
      throws KlawException {
    when(schemaRegstryControllerService.execSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(createRequestVerdict(RequestEntityType.SCHEMA, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(schemaRegstryControllerService, times(1)).execSchemaRequests(anyString());
  }

  @Order(8)
  @Test
  public void givenMultipleRequestToApproveCallCorrectSCHEMAServiceAndReturnISEResponse()
      throws KlawException {
    when(schemaRegstryControllerService.execSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(schemaRegstryControllerService, times(2)).execSchemaRequests(anyString());
  }

  @Order(9)
  @Test
  public void givenARequestToApproveMulitpleCallCorrectCONNECTORServiceAndReturnSuccessOK()
      throws KlawException {
    when(kafkaConnectControllerService.approveConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(kafkaConnectControllerService, times(2)).approveConnectorRequests(anyString());
  }

  @Order(10)
  @Test
  public void
      givenARequestToApproveMulitpleCallCorrectCONNECTORServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException {
    when(kafkaConnectControllerService.approveConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(kafkaConnectControllerService, times(2)).approveConnectorRequests(anyString());
  }

  @Order(11)
  @Test
  public void givenARequestToApproveCallCorrectCONNECTORServiceAndReturnISEResponse()
      throws KlawException {
    when(kafkaConnectControllerService.approveConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(kafkaConnectControllerService, times(1)).approveConnectorRequests(anyString());
  }

  @Order(12)
  @Test
  public void givenMultipleRequestToApproveCallCorrectCONNECTORServiceAndReturnISEResponse()
      throws KlawException {
    when(kafkaConnectControllerService.approveConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(kafkaConnectControllerService, times(2)).approveConnectorRequests(anyString());
  }

  @Order(13)
  @Test
  public void givenARequestToApproveMulitpleCallCorrectACLServiceAndReturnSuccessOK()
      throws KlawException {
    when(aclControllerService.approveAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.ACL, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(aclControllerService, times(2)).approveAclRequests(anyString());
  }

  @Order(14)
  @Test
  public void
      givenARequestToApproveMulitpleCallCorrectACLServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException {
    when(aclControllerService.approveAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.ACL, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(aclControllerService, times(2)).approveAclRequests(anyString());
  }

  @Order(15)
  @Test
  public void givenARequestToApproveCallCorrectACLServiceAndReturnISEResponse()
      throws KlawException {
    when(aclControllerService.approveAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(createRequestVerdict(RequestEntityType.ACL, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(1)).approveAclRequests(anyString());
  }

  @Order(16)
  @Test
  public void givenMultipleRequestToApproveCallCorrectACLServiceAndReturnISEResponse()
      throws KlawException {
    when(aclControllerService.approveAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.ACL, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(2)).approveAclRequests(anyString());
  }

  @Order(17)
  @Test
  public void givenMultipleRequestToApproveCallCorrectUSERServiceAndReturnISEResponse()
      throws KlawException {

    ResponseEntity<List<ApiResponse>> result =
        controller.approveRequest(
            createRequestVerdict(RequestEntityType.USER, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(0)).approveAclRequests(anyString());
    verify(kafkaConnectControllerService, times(0)).approveConnectorRequests(anyString());
    verify(schemaRegstryControllerService, times(0)).execSchemaRequests(anyString());
    verify(topicControllerService, times(0)).approveTopicRequests(anyString());
  }

  @Order(18)
  @Test
  public void givenARequestToDeclineCCallCorrectServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(topicControllerService.declineTopicRequests(eq("1001"), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.TOPIC, "TopicName Must Conform.", "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(topicControllerService, times(1)).declineTopicRequests(eq("1001"), anyString());
  }

  @Order(19)
  @Test
  public void givenMultipleRequestsToDeclineCallCorrectServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(topicControllerService.declineTopicRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(
                RequestEntityType.TOPIC, "TopicName Must Conform.", "1001", "2002"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(topicControllerService, times(2)).declineTopicRequests(anyString(), anyString());
  }

  @Order(20)
  @Test
  public void
      givenARequestToDeclineCMulitpleCallCorrectTOPICServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(topicControllerService.declineTopicRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(
                RequestEntityType.TOPIC, "TopicName Must Conform.", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(topicControllerService, times(2)).declineTopicRequests(anyString(), anyString());
  }

  @Order(21)
  @Test
  public void givenARequestToDeclineCCallCorrectTOPICServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(topicControllerService.declineTopicRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.TOPIC, "TopicName Must Conform.", "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(topicControllerService, times(1)).declineTopicRequests(anyString(), anyString());
  }

  @Order(22)
  @Test
  public void givenMultipleRequestToDeclineCCallCorrectTOPICServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(topicControllerService.declineTopicRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(
                RequestEntityType.TOPIC, "TopicName Must Conform.", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(topicControllerService, times(2)).declineTopicRequests(anyString(), anyString());
  }

  @Order(23)
  @Test
  public void givenARequestToDeclineCMulitpleCallCorrectSCHEMAServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.execSchemaRequestsDecline(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, "Schema is Invalid", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(schemaRegstryControllerService, times(2))
        .execSchemaRequestsDecline(anyString(), anyString());
  }

  @Order(24)
  @Test
  public void
      givenARequestToDeclineCMulitpleCallCorrectSCHEMAServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.execSchemaRequestsDecline(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, "Schema is Invalid", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(schemaRegstryControllerService, times(2))
        .execSchemaRequestsDecline(anyString(), anyString());
  }

  @Order(25)
  @Test
  public void givenARequestToDeclineCCallCorrectSCHEMAServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.execSchemaRequestsDecline(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, "Schema is Invalid", "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(schemaRegstryControllerService, times(1))
        .execSchemaRequestsDecline(anyString(), anyString());
  }

  @Order(26)
  @Test
  public void givenMultipleRequestToDeclineCCallCorrectSCHEMAServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.execSchemaRequestsDecline(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, "Schema is Invalid", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(schemaRegstryControllerService, times(2))
        .execSchemaRequestsDecline(anyString(), anyString());
  }

  @Order(27)
  @Test
  public void givenARequestToDeclineCMulitpleCallCorrectCONNECTORServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.declineConnectorRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, "What?", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(kafkaConnectControllerService, times(2))
        .declineConnectorRequests(anyString(), anyString());
  }

  @Order(28)
  @Test
  public void
      givenARequestToDeclineCMulitpleCallCorrectCONNECTORServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.declineConnectorRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, "What?", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(kafkaConnectControllerService, times(2))
        .declineConnectorRequests(anyString(), anyString());
  }

  @Order(29)
  @Test
  public void givenARequestToDeclineCCallCorrectCONNECTORServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.declineConnectorRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, "What?", "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(kafkaConnectControllerService, times(1))
        .declineConnectorRequests(anyString(), anyString());
  }

  @Order(30)
  @Test
  public void givenMultipleRequestToDeclineCCallCorrectCONNECTORServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.declineConnectorRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, "What?", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(kafkaConnectControllerService, times(2))
        .declineConnectorRequests(anyString(), anyString());
  }

  @Order(31)
  @Test
  public void givenARequestToDeclineCMulitpleCallCorrectACLServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(aclControllerService.declineAclRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.ACL, "No Access for you!", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(aclControllerService, times(2)).declineAclRequests(anyString(), anyString());
  }

  @Order(32)
  @Test
  public void
      givenARequestToDeclineCMulitpleCallCorrectACLServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(aclControllerService.declineAclRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.ACL, "No Access for you!", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(aclControllerService, times(2)).declineAclRequests(anyString(), anyString());
  }

  @Order(33)
  @Test
  public void givenARequestToDeclineCCallCorrectACLServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(aclControllerService.declineAclRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.ACL, "No Access for you!", "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(1)).declineAclRequests(anyString(), anyString());
  }

  @Order(34)
  @Test
  public void givenMultipleRequestToDeclineCCallCorrectACLServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(aclControllerService.declineAclRequests(anyString(), anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.ACL, "No Access for you!", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(2)).declineAclRequests(anyString(), anyString());
  }

  @Order(35)
  @Test
  public void givenMultipleRequestToDeclineCCCallCorrectUSERServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {

    ResponseEntity<List<ApiResponse>> result =
        controller.declineRequest(
            createRequestVerdict(RequestEntityType.USER, "No Access for you!", "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(0)).declineAclRequests(anyString(), anyString());
    verify(kafkaConnectControllerService, times(0))
        .declineConnectorRequests(anyString(), anyString());
    verify(schemaRegstryControllerService, times(0))
        .execSchemaRequestsDecline(anyString(), anyString());
    verify(topicControllerService, times(0)).declineTopicRequests(anyString(), anyString());
  }

  @Order(36)
  @Test
  public void givenARequestToDeleteCallCorrectServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(topicControllerService.deleteTopicRequests(eq("1001")))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.TOPIC, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(topicControllerService, times(1)).deleteTopicRequests(eq("1001"));
  }

  @Order(37)
  @Test
  public void givenMultipleRequestsToDeleteCallCorrectServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(topicControllerService.deleteTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.TOPIC, null, "1001", "2002"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(topicControllerService, times(2)).deleteTopicRequests(anyString());
  }

  @Order(38)
  @Test
  public void
      givenARequestToDeleteCMulitpleCallCorrectTOPICServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(topicControllerService.deleteTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.TOPIC, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(topicControllerService, times(2)).deleteTopicRequests(anyString());
  }

  @Order(39)
  @Test
  public void givenARequestToDeleteCallCorrectTOPICServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(topicControllerService.deleteTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.TOPIC, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(topicControllerService, times(1)).deleteTopicRequests(anyString());
  }

  @Order(40)
  @Test
  public void givenMultipleRequestToDeleteCallCorrectTOPICServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(topicControllerService.deleteTopicRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.TOPIC, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(topicControllerService, times(2)).deleteTopicRequests(anyString());
  }

  @Order(41)
  @Test
  public void givenARequestToDeleteMulitpleCallCorrectSCHEMAServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.deleteSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(schemaRegstryControllerService, times(2)).deleteSchemaRequests(anyString());
  }

  @Order(42)
  @Test
  public void
      givenARequestToDeleteMulitpleCallCorrectSCHEMAServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.deleteSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(schemaRegstryControllerService, times(2)).deleteSchemaRequests(anyString());
  }

  @Order(43)
  @Test
  public void givenARequestToDeleteCallCorrectSCHEMAServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.deleteSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.SCHEMA, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(schemaRegstryControllerService, times(1)).deleteSchemaRequests(anyString());
  }

  @Order(44)
  @Test
  public void givenMultipleRequestToDeleteCallCorrectSCHEMAServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(schemaRegstryControllerService.deleteSchemaRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.SCHEMA, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(schemaRegstryControllerService, times(2)).deleteSchemaRequests(anyString());
  }

  @Order(45)
  @Test
  public void givenARequestToDeleteMulitpleCallCorrectCONNECTORServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.deleteConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(kafkaConnectControllerService, times(2)).deleteConnectorRequests(anyString());
  }

  @Order(46)
  @Test
  public void
      givenARequestToDeleteMulitpleCallCorrectCONNECTORServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.deleteConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(kafkaConnectControllerService, times(2)).deleteConnectorRequests(anyString());
  }

  @Order(47)
  @Test
  public void givenARequestToDeleteCallCorrectCONNECTORServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.deleteConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(kafkaConnectControllerService, times(1)).deleteConnectorRequests(anyString());
  }

  @Order(48)
  @Test
  public void givenMultipleRequestToDeleteCallCorrectCONNECTORServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(kafkaConnectControllerService.deleteConnectorRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.CONNECTOR, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(kafkaConnectControllerService, times(2)).deleteConnectorRequests(anyString());
  }

  @Order(49)
  @Test
  public void givenARequestToDeleteMulitpleCallCorrectACLServiceAndReturnSuccessOK()
      throws KlawException, KlawRestException {
    when(aclControllerService.deleteAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.ACL, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
    verify(aclControllerService, times(2)).deleteAclRequests(anyString());
  }

  @Order(50)
  @Test
  public void
      givenARequestToDeleteMulitpleCallCorrectACLServiceAndReturnSuccessMultiStatusResponse()
          throws KlawException, KlawRestException {
    when(aclControllerService.deleteAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.SUCCESS))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.ACL, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(207));
    verify(aclControllerService, times(2)).deleteAclRequests(anyString());
  }

  @Order(51)
  @Test
  public void givenARequestToDeleteCallCorrectACLServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(aclControllerService.deleteAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.ACL, null, "1001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(1)).deleteAclRequests(anyString());
  }

  @Order(52)
  @Test
  public void givenMultipleRequestToDeleteCallCorrectACLServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {
    when(aclControllerService.deleteAclRequests(anyString()))
        .thenReturn(getApiResponse(ApiResultStatus.FAILURE));
    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(createRequestVerdict(RequestEntityType.ACL, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(2)).deleteAclRequests(anyString());
  }

  @Order(53)
  @Test
  public void givenMultipleRequestToDeleteCallCorrectUSERServiceAndReturnISEResponse()
      throws KlawException, KlawRestException {

    ResponseEntity<List<ApiResponse>> result =
        controller.deleteRequest(
            createRequestVerdict(RequestEntityType.USER, null, "1001", "2001"));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));
    verify(aclControllerService, times(0)).deleteAclRequests(anyString());
    verify(kafkaConnectControllerService, times(0)).deleteConnectorRequests(anyString());
    verify(schemaRegstryControllerService, times(0)).deleteSchemaRequests(anyString());
    verify(topicControllerService, times(0)).deleteTopicRequests(anyString());
  }

  private RequestVerdict createRequestVerdict(
      RequestEntityType type, String reason, String... reqIds) {
    RequestVerdict verdict = new RequestVerdict();
    verdict.setRequestEntityType(type);
    verdict.setReason(reason);
    verdict.setReqIds(Arrays.asList(reqIds));
    return verdict;
  }

  private ApiResponse getApiResponse(ApiResultStatus status) {
    return ApiResponse.builder().result(status.value).build();
  }
}
