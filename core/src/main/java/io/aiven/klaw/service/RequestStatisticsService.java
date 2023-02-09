package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.model.RequestEntityStatusCount;
import io.aiven.klaw.model.RequestStatusCount;
import io.aiven.klaw.model.RequestsCountOverview;
import io.aiven.klaw.model.RequestsOperationTypeCount;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // get topics count and update requestsCountOverview
    Map<String, Map<String, Long>> topicRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getTopicRequestsCounts(
                commonUtilsService.getTeamId(getUserName()),
                requestMode,
                commonUtilsService.getTenantId(getUserName()));
    updateRequestsCountOverview(
        topicRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.TOPIC);

    // get acls count and update requestsCountOverview
    Map<String, Map<String, Long>> aclsRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getAclRequestsCounts(
                commonUtilsService.getTeamId(getUserName()),
                requestMode,
                commonUtilsService.getTenantId(getUserName()));
    updateRequestsCountOverview(
        aclsRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.ACL);

    // get schema reqs count and update requestsCountOverview
    Map<String, Map<String, Long>> schemasRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getSchemaRequestsCounts(
                commonUtilsService.getTeamId(getUserName()),
                requestMode,
                commonUtilsService.getTenantId(getUserName()));
    updateRequestsCountOverview(
        schemasRequestCountsMap, requestEntityStatusCountSet, RequestEntityType.SCHEMA);

    // get connector reqs count and update requestsCountOverview
    Map<String, Map<String, Long>> connectorRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getConnectorRequestsCounts(
                commonUtilsService.getTeamId(getUserName()),
                requestMode,
                commonUtilsService.getTenantId(getUserName()));
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
              .count(stCounts.get(key))
              .build();
      requestStatusCountSet.add(requestStatusCount);
    }

    for (String key : opCounts.keySet()) {
      RequestsOperationTypeCount requestsOperationTypeCount =
          RequestsOperationTypeCount.builder()
              .requestOperationType(RequestOperationType.of(key))
              .count(opCounts.get(key))
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
    return mailService.getUserName(getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
