package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.RequestEntityStatusCount;
import io.aiven.klaw.model.RequestsCountOverview;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RequestStatisticsServiceTest {

  private UtilMethods utilMethods;

  @Mock private CommonUtilsService commonUtilsService;
  @Mock private ManageDatabase manageDatabase;
  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private UserDetails userDetails;
  @Mock private UserInfo userInfo;
  @Mock private MailUtils mailService;

  private RequestStatisticsService requestStatisticsService;

  @BeforeEach
  void setUp() {
    utilMethods = new UtilMethods();
    this.requestStatisticsService = new RequestStatisticsService();
    ReflectionTestUtils.setField(requestStatisticsService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(
        requestStatisticsService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(requestStatisticsService, "mailService", mailService);
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  @Test
  public void getRequestsCountOverview() {
    stubUserInfo();
    when(commonUtilsService.getTenantId(userDetails.getUsername())).thenReturn(1);
    when(handleDbRequests.getTopicRequestsCounts(
            anyInt(), eq(RequestMode.MY_REQUESTS), anyInt(), anyString()))
        .thenReturn(utilMethods.getRequestCounts());
    when(handleDbRequests.getAclRequestsCounts(
            anyInt(), eq(RequestMode.MY_REQUESTS), anyInt(), anyString()))
        .thenReturn(utilMethods.getRequestCounts());
    when(handleDbRequests.getSchemaRequestsCounts(
            anyInt(), eq(RequestMode.MY_REQUESTS), anyInt(), anyString()))
        .thenReturn(utilMethods.getRequestCounts());
    when(handleDbRequests.getConnectorRequestsCounts(
            anyInt(), eq(RequestMode.MY_REQUESTS), anyInt(), anyString()))
        .thenReturn(utilMethods.getRequestCounts());
    RequestsCountOverview requestsCountOverview =
        requestStatisticsService.getRequestsCountOverview(RequestMode.MY_REQUESTS);
    Set<RequestEntityStatusCount> requestEntityStatistics =
        requestsCountOverview.getRequestEntityStatistics();
    List<RequestEntityStatusCount> requestEntityStatusCountArrayList =
        new ArrayList<>(requestEntityStatistics);

    assertThat(requestEntityStatusCountArrayList).hasSize(4);

    boolean topicEntityFound = false,
        aclEntityFound = false,
        schemaEntityFound = false,
        connectorEntityFound = false;
    for (RequestEntityStatusCount requestEntityStatusCount : requestEntityStatusCountArrayList) {
      if (requestEntityStatusCount.getRequestEntityType() == RequestEntityType.TOPIC) {
        topicEntityFound = true;
      }
      if (requestEntityStatusCount.getRequestEntityType() == RequestEntityType.ACL) {
        aclEntityFound = true;
      }
      if (requestEntityStatusCount.getRequestEntityType() == RequestEntityType.SCHEMA) {
        schemaEntityFound = true;
      }
      if (requestEntityStatusCount.getRequestEntityType() == RequestEntityType.CONNECTOR) {
        connectorEntityFound = true;
      }
    }

    assertThat(topicEntityFound).isTrue();
    assertThat(requestEntityStatusCountArrayList.get(0).getRequestStatusCountSet()).hasSize(2);
    assertThat(requestEntityStatusCountArrayList.get(0).getRequestsOperationTypeCountSet())
        .hasSize(2);

    assertThat(aclEntityFound).isTrue();
    assertThat(requestEntityStatusCountArrayList.get(1).getRequestStatusCountSet()).hasSize(2);
    assertThat(requestEntityStatusCountArrayList.get(1).getRequestsOperationTypeCountSet())
        .hasSize(2);

    assertThat(schemaEntityFound).isTrue();
    assertThat(requestEntityStatusCountArrayList.get(2).getRequestStatusCountSet()).hasSize(2);
    assertThat(requestEntityStatusCountArrayList.get(2).getRequestsOperationTypeCountSet())
        .hasSize(2);

    assertThat(connectorEntityFound).isTrue();
    assertThat(requestEntityStatusCountArrayList.get(3).getRequestStatusCountSet()).hasSize(2);
    assertThat(requestEntityStatusCountArrayList.get(3).getRequestsOperationTypeCountSet())
        .hasSize(2);
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(101);
    when(mailService.getUserName(any())).thenReturn("kwusera");
    when(mailService.getCurrentUserName()).thenReturn("kwusera");
  }
}
