package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.response.RequestEntityStatusCount;
import io.aiven.klaw.model.response.RequestStatusCount;
import io.aiven.klaw.model.response.RequestsCountOverview;
import io.aiven.klaw.model.response.RequestsOperationTypeCount;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RequestStatisticsService {

  @Autowired ManageDatabase manageDatabase;
  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired MailUtils mailService;

  public RequestsCountOverview getRequestsCountOverview(RequestMode requestMode) {
    RequestsCountOverview requestsCountOverview = new RequestsCountOverview();
    Set<RequestEntityStatusCount> requestEntityStatusCountSet = new HashSet<>();
    final String userName = getUserName();
    // get topics count and update requestsCountOverview
    Map<String, Map<String, Long>> topicRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getTopicRequestsCounts(
                commonUtilsService.getTeamId(userName),
                requestMode,
                commonUtilsService.getTenantId(userName),
                userName);
    updateRequestsCountOverview(
        topicRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.TOPIC);

    // get acls count and update requestsCountOverview
    Map<String, Map<String, Long>> aclsRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getAclRequestsCounts(
                commonUtilsService.getTeamId(userName),
                requestMode,
                commonUtilsService.getTenantId(userName),
                userName);
    updateRequestsCountOverview(
        aclsRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.ACL);

    // get schema reqs count and update requestsCountOverview
    Map<String, Map<String, Long>> schemasRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getSchemaRequestsCounts(
                commonUtilsService.getTeamId(userName),
                requestMode,
                commonUtilsService.getTenantId(userName),
                userName);
    updateRequestsCountOverview(
        schemasRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.SCHEMA);

    // get connector reqs count and update requestsCountOverview
    Map<String, Map<String, Long>> connectorRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getConnectorRequestsCounts(
                commonUtilsService.getTeamId(userName),
                requestMode,
                commonUtilsService.getTenantId(userName),
                userName);
    updateRequestsCountOverview(
        connectorRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.CONNECTOR);

    requestsCountOverview.setRequestEntityStatistics(requestEntityStatusCountSet);
    return requestsCountOverview;
  }

  private void updateRequestsCountOverview(
      Map<String, Map<String, Long>> topicRequestCountsMap,
      Set<RequestEntityStatusCount> requestEntityStatusCountSet,
      RequestEntityType requestEntityType) {
    Map<String, Long> opCounts = topicRequestCountsMap.get("OPERATION_TYPE_COUNTS");
    Map<String, Long> stCounts = topicRequestCountsMap.get("STATUS_COUNTS");

    Set<RequestStatusCount> requestStatusCountSet = new HashSet<>();
    Set<RequestsOperationTypeCount> requestsOperationTypeCountsSet = new HashSet<>();

    for (String key : stCounts.keySet()) {
      RequestStatusCount requestStatusCount =
          RequestStatusCount.builder()
              .requestStatus(RequestStatus.of(key))
              .count(stCounts.getOrDefault(key, 0L))
              .build();
      requestStatusCountSet.add(requestStatusCount);
    }

    for (String key : opCounts.keySet()) {
      RequestsOperationTypeCount requestsOperationTypeCount =
          RequestsOperationTypeCount.builder()
              .requestOperationType(RequestOperationType.of(key))
              .count(opCounts.getOrDefault(key, 0L))
              .build();
      requestsOperationTypeCountsSet.add(requestsOperationTypeCount);
    }

    RequestEntityStatusCount requestEntityTopicStatusCount = new RequestEntityStatusCount();
    requestEntityTopicStatusCount.setRequestEntityType(requestEntityType);
    requestEntityTopicStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityTopicStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityTopicStatusCount);
  }

  private String getUserName() {
    return mailService.getUserName(commonUtilsService.getPrincipal());
  }
}
