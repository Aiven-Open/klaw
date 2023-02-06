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
    updateRequestsCountOverviewForTopics(requestEntityStatusCountSet, requestMode);

    requestsCountOverview.setRequestEntityStatistics(requestEntityStatusCountSet);
    return requestsCountOverview;
  }

  private void updateRequestsCountOverviewForTopics(
      Set<RequestEntityStatusCount> requestEntityStatusCountSet, RequestMode requestMode) {
    Map<String, Map<String, Long>> topicRequestCountsMap =
        manageDatabase
            .getHandleDbRequests()
            .getTopicRequestsCounts(
                commonUtilsService.getTeamId(getUserName()),
                requestMode,
                commonUtilsService.getTenantId(getUserName()));
    Map<String, Long> opCounts = topicRequestCountsMap.get("OPERATION_TYPE_COUNTS");
    Map<String, Long> stCounts = topicRequestCountsMap.get("STATUS_COUNTS");

    Set<RequestStatusCount> requestStatusCountSet = new HashSet<>();
    Set<RequestsOperationTypeCount> requestsOperationTypeCountsSet = new HashSet<>();

    for (String key : stCounts.keySet()) {
      RequestStatusCount requestStatusCount =
          RequestStatusCount.builder()
              .requestStatus(RequestStatus.valueOf(key))
              .count(stCounts.get(key))
              .build();
      requestStatusCountSet.add(requestStatusCount);
    }

    for (String key : opCounts.keySet()) {
      RequestsOperationTypeCount requestsOperationTypeCount =
          RequestsOperationTypeCount.builder()
              .requestOperationType(RequestOperationType.valueOf(key))
              .count(opCounts.get(key))
              .build();
      requestsOperationTypeCountsSet.add(requestsOperationTypeCount);
    }

    RequestEntityStatusCount requestEntityTopicStatusCount = new RequestEntityStatusCount();
    requestEntityTopicStatusCount.setRequestEntityType(RequestEntityType.TOPIC);
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
