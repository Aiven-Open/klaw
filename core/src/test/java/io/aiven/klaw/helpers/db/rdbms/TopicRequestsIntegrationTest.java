package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TopicRequestsIntegrationTest {

  @Autowired private TopicRequestsRepo repo;
  @Autowired private TopicRequestsRepo topicRequestsRepo;
  @Autowired private UserInfoRepo userInfoRepo;
  @Autowired private TeamRepo teamRepo;

  @Autowired TestEntityManager entityManager;

  private SelectDataJdbc selectDataJdbc;

  private UtilMethods utilMethods;

  public void loadData() {
    generateData(
        5,
        101,
        101,
        "firsttopic1",
        "dev",
        RequestStatus.CREATED,
        RequestOperationType.CREATE,
        100,
        null); // Team1
    generateData(
        8,
        101,
        101,
        "firsttopic1",
        "dev",
        RequestStatus.CREATED,
        RequestOperationType.PROMOTE,
        150,
        null); // Team1
    generateData(
        3,
        101,
        101,
        "firsttopic2",
        "dev",
        RequestStatus.APPROVED,
        RequestOperationType.CREATE,
        200,
        null); // Team1
    generateData(
        7,
        101,
        101,
        "firsttopic3",
        "dev",
        RequestStatus.DECLINED,
        RequestOperationType.CLAIM,
        300,
        "103"); // Team1
    generateData(
        2,
        101,
        101,
        "firsttopic4",
        "dev",
        RequestStatus.DELETED,
        RequestOperationType.UPDATE,
        400,
        null); // Team1
    generateData(
        4,
        103,
        101,
        "firsttopic5",
        "dev",
        RequestStatus.CREATED,
        RequestOperationType.CLAIM,
        500,
        "101"); // Team2

    generateData(
        10,
        101,
        101,
        "secondtopic",
        "test",
        RequestStatus.DECLINED,
        RequestOperationType.DELETE,
        31,
        null);
    generateData(
        1,
        103,
        103,
        "secondtopic",
        "test",
        RequestStatus.APPROVED,
        RequestOperationType.UPDATE,
        41,
        null);

    UserInfo user = new UserInfo();
    user.setTenantId(101);
    user.setTeamId(101);
    user.setRole("USER");
    user.setUsername("James");
    UserInfo user2 = new UserInfo();
    user2.setTenantId(101);
    user2.setTeamId(103);
    user2.setRole("USER");
    user2.setUsername("John");
    UserInfo user3 = new UserInfo();
    user3.setTenantId(103);
    user3.setTeamId(103);
    user3.setRole("USER");
    user3.setUsername("Jackie");

    UserInfo user4 = new UserInfo();
    user4.setTenantId(104);
    user4.setTeamId(104);
    user4.setRole("USER");
    user4.setUsername("Joan");
    entityManager.persistAndFlush(user);
    entityManager.persistAndFlush(user2);
    entityManager.persistAndFlush(user3);
    entityManager.persistAndFlush(user4);

    Team t1 = new Team();
    t1.setTeamId(101);
    t1.setTenantId(101);
    t1.setTeamname("octopus");
    Team t2 = new Team();
    t2.setTeamname("pirates");
    t2.setTeamId(103);
    t2.setTenantId(103);

    Team t4 = new Team();
    t4.setTeamname("plank");
    t4.setTeamId(104);
    t4.setTenantId(101);

    entityManager.persistAndFlush(t1);
    entityManager.persistAndFlush(t2);
    entityManager.persistAndFlush(t4);

    //
    generateData(
        5,
        104,
        104,
        "thirdtopic",
        "test",
        RequestStatus.CREATED,
        RequestOperationType.CREATE,
        50,
        null);
    generateData(
        1,
        104,
        104,
        "thirdtopic",
        "test",
        RequestStatus.DECLINED,
        RequestOperationType.CLAIM,
        55,
        "101");
    generateData(
        2,
        104,
        104,
        "thirdtopic",
        "test",
        RequestStatus.APPROVED,
        RequestOperationType.UPDATE,
        57,
        null);
    generateData(
        2,
        104,
        104,
        "thirdtopic",
        "test",
        RequestStatus.CREATED,
        RequestOperationType.DELETE,
        59,
        null);

    generateData(
        6,
        104,
        104,
        "thirdtopic",
        "test",
        RequestStatus.DELETED,
        RequestOperationType.CREATE,
        61,
        null);
  }

  @BeforeEach
  public void setUp() {
    selectDataJdbc = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectDataJdbc, "topicRequestsRepo", topicRequestsRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "userInfoRepo", userInfoRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "teamRepo", teamRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void getTopicRequestsCountsForMyRequestsTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getTopicRequestsCounts(101, RequestMode.MY_REQUESTS, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(13L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(3L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(17L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(2L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(8L);
    assertThat(operationTypeCount.get(RequestOperationType.PROMOTE.value)).isEqualTo(8L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(7L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(2L);
  }

  @Test
  @Order(2)
  public void getTopicRequestsCountsForApproveForTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getTopicRequestsCounts(101, RequestMode.TO_APPROVE, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(13L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(8L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(4L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(3L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(17L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(2L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(2L);
  }

  @Test
  @Order(3)
  public void getTopicRequestsCountsForApproveForTeam2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getTopicRequestsCounts(103, RequestMode.TO_APPROVE, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(4L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(7L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  @Test
  @Order(3)
  public void getTopicRequestsCountsForMyRequestsForTeam2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getTopicRequestsCounts(103, RequestMode.MY_REQUESTS, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(4L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(4L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  @Test
  @Order(4)
  public void getAllRequestsFilteredByStatus() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "deleted", true, 101, null, null, null, null, false);
    List<TopicRequest> james2 =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "declined", true, 101, null, null, null, null, false);
    List<TopicRequest> john =
        selectDataJdbc.getFilteredTopicRequests(
            true, "John", "created", true, 103, null, null, null, null, false);

    assertThat(james.size()).isEqualTo(2);

    assertThat(john.size()).isEqualTo(0);
    assertThat(james2.size()).isEqualTo(10);
    for (TopicRequest req : james2) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.DECLINED.value);
    }

    for (TopicRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
    }
  }

  @Test
  @Order(5)
  public void getAllRequestsFilteredByTopicWildcard() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "ALL", true, 101, null, null, null, "first", false);

    List<TopicRequest> john =
        selectDataJdbc.getFilteredTopicRequests(
            true, "John", "ALL", true, 101, null, null, null, "second", false);

    List<TopicRequest> all =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "ALL", true, 101, null, null, null, "topic", false);

    assertThat(james.size()).isEqualTo(22);
    for (TopicRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(john.size()).isEqualTo(10);

    assertThat(all.size()).isEqualTo(32);
  }

  @Test
  @Order(6)
  public void getAllRequestsFilteredByTopicByEnvironment() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "ALL", true, 101, null, null, "test", "second", false);

    assertThat(james.size()).isEqualTo(10);
    for (TopicRequest req : james) {
      assertThat(req.getTopicname()).contains("secondtopic");
      assertThat(req.getEnvironment()).isEqualTo("test");
    }
  }

  @Test
  @Order(7)
  public void getAllRequestsFilteredByTeamName() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "ALL", true, 101, Integer.valueOf(101), null, null, null, false);

    List<TopicRequest> john =
        selectDataJdbc.getFilteredTopicRequests(
            true, "John", "ALL", true, 103, Integer.valueOf(103), null, null, null, false);

    assertThat(james.size()).isEqualTo(32);
    for (TopicRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
      if (!req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
        assertThat(req.getTeamId()).isEqualTo(101);
      } else {
        assertThat(req.getTeamId()).isNotEqualTo(101);
      }
    }

    assertThat(john.size()).isEqualTo(1);
    for (TopicRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
      assertThat(req.getTeamId()).isEqualTo(103);
    }
  }

  @Test
  @Order(8)
  public void getAllRequestsFilteredByMisSpeltEnvironment_ReturnNothing() {
    // topic name is case sensitive.
    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "ALL", true, 101, null, null, "tsst", null, false);
    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(9)
  public void getNonApproversRequestsFilteredByTenantId_willIgnoreOtherPassedParameters() {

    List<TopicRequest> tenant1 =
        selectDataJdbc.getFilteredTopicRequests(
            false, "James", null, true, 101, null, null, null, null, false);

    List<TopicRequest> tenant2 =
        selectDataJdbc.getFilteredTopicRequests(
            false, "John", null, true, 103, null, null, null, null, false);

    assertThat(tenant1.size()).isEqualTo(39);
    for (TopicRequest req : tenant1) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(tenant2.size()).isEqualTo(1);
    for (TopicRequest req : tenant2) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(10)
  public void getNonApproversRequestsFilteredByEnvironmentByStatus() {

    List<TopicRequest> tenant1 =
        selectDataJdbc.getFilteredTopicRequests(
            false, "James", "created", true, 101, null, null, "dev", null, false);
    List<TopicRequest> tenant2 =
        selectDataJdbc.getFilteredTopicRequests(
            false, "John", "approved", true, 103, null, null, "test", null, false);

    assertThat(tenant1.size()).isEqualTo(17);
    for (TopicRequest req : tenant1) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(tenant2.size()).isEqualTo(1);
    for (TopicRequest req : tenant2) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(11)
  public void getNonApproversRequestsFilteredByStatusByWildcard_willUseWildcardParameter() {

    List<TopicRequest> tenant1 =
        selectDataJdbc.getFilteredTopicRequests(
            false, "James", "deleted", true, 101, null, null, null, "One", false);

    List<TopicRequest> tenant2 =
        selectDataJdbc.getFilteredTopicRequests(
            false, "John", "approved", true, 103, null, null, null, "two", false);

    assertThat(tenant1.size()).isEqualTo(0);

    assertThat(tenant2.size()).isEqualTo(0);
  }

  @Test
  @Order(12)
  public void givennonExistentTeamId_ReturnNothing() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", "declined", true, 101, Integer.valueOf(99), null, null, null, false);

    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(13)
  public void givenNewQueryMethodAssertThatOldAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<TopicRequest> topicRequestList = Lists.newArrayList(repo.findAllByTenantId(101));

    List<TopicRequest> results =
        selectDataJdbc.getFilteredTopicRequests(
            false, "James", null, true, 101, null, null, null, null, false);

    assertThat(topicRequestList.size()).isEqualTo(results.size());

    assertThat(results.containsAll(topicRequestList)).isTrue();
  }

  @Test
  @Order(14)
  public void getAllRequestsFilteredByTenantId() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", null, true, 101, null, null, null, null, false);
    List<TopicRequest> john =
        selectDataJdbc.getFilteredTopicRequests(
            true, "John", null, true, 103, null, null, null, null, false);

    assertThat(james.size()).isEqualTo(32);
    for (TopicRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(1);
    for (TopicRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(15)
  public void getAllRequestsFilteredByEnvironmentByStatus() {

    List<TopicRequest> james =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", RequestStatus.CREATED.value, true, 101, null, null, "dev", null, false);
    List<TopicRequest> john =
        selectDataJdbc.getFilteredTopicRequests(
            true, "John", RequestStatus.DECLINED.value, true, 103, null, null, "test", null, false);

    assertThat(james.size()).isEqualTo(17);
    for (TopicRequest req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(16)
  public void getAllRequestsDontReturnOwnRequestsAsAllReqsIsTrue() {

    List<TopicRequest> jackie =
        selectDataJdbc.getFilteredTopicRequests(
            true, "Jackie", "all", true, 101, null, null, null, null, false);

    assertThat(jackie.size()).isEqualTo(0);
  }

  @Test
  @Order(17)
  public void getAllRequestsAndReturnOwnRequestsAsAllReqsIsTrue() {

    List<TopicRequest> jackie =
        selectDataJdbc.getFilteredTopicRequests(
            false, "Jackie", "all", true, 101, null, null, null, null, false);

    assertThat(jackie.size()).isEqualTo(39);
  }

  @Test
  @Order(18)
  public void getAllCreatedStatusRequestsFromTenant() {

    List<TopicRequest> joan =
        selectDataJdbc.getFilteredTopicRequests(
            true, "Joan", RequestStatus.CREATED.value, true, 104, null, null, null, null, false);

    assertThat(joan.size()).isEqualTo(7);
    for (TopicRequest req : joan) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(104);
      assertThat(req.getRequestor()).isNotEqualTo("Joan");
    }
  }

  @Test
  @Order(19)
  public void getAllDELETEDStatusRequestsFromTenant() {

    List<TopicRequest> joan =
        selectDataJdbc.getFilteredTopicRequests(
            true, "Joan", RequestStatus.DELETED.value, true, 104, null, null, null, null, false);

    assertThat(joan.size()).isEqualTo(6);
    for (TopicRequest req : joan) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.DELETED.value);
      assertThat(req.getTenantId()).isEqualTo(104);
      assertThat(req.getRequestor()).isNotEqualTo("Joan");
    }
  }

  @Test
  @Order(20)
  public void getAllClaimStatusRequestsForApprovalTeam1() {

    List<TopicRequest> resultSet =
        selectDataJdbc.getFilteredTopicRequests(
            true, "James", RequestStatus.ALL.value, true, 101, null, null, null, null, false);

    assertThat(resultSet.size()).isEqualTo(32);
    for (TopicRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      if (req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
        assertThat(req.getTeamId()).isEqualTo(103);
        assertThat(req.getApprovingTeamId()).isEqualTo("101");
      } else {
        assertThat(req.getTeamId()).isEqualTo(101);
      }
    }
  }

  @Test
  @Order(21)
  public void getAllClaimStatusRequestsForMyRequestsViewCreatedByMyTeam() {

    List<TopicRequest> resultSet =
        selectDataJdbc.getFilteredTopicRequests(
            false, "James", RequestStatus.ALL.value, false, 101, null, null, null, null, false);

    assertThat(resultSet.size()).isEqualTo(35);
    for (TopicRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      if (req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
        assertThat(req.getApprovingTeamId()).isNotEqualTo("101");
      }
    }
  }

  @Test
  @Order(22)
  public void getAllClaimStatusRequestsForApprovalTeam2() {

    List<TopicRequest> resultSet =
        selectDataJdbc.getFilteredTopicRequests(
            true, "John", RequestStatus.ALL.value, true, 101, null, null, null, null, false);

    assertThat(resultSet.size()).isEqualTo(35);
    for (TopicRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      if (req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
        assertThat(req.getTeamId()).isEqualTo(101);
        assertThat(req.getApprovingTeamId()).isEqualTo("103");
      } else {
        assertThat(req.getTeamId()).isEqualTo(101);
      }
    }
  }

  @Test
  @Order(23)
  public void getAllClaimStatusRequestsForMyRequestsViewCreatedByMyTeamTeam2() {

    List<TopicRequest> resultSet =
        selectDataJdbc.getFilteredTopicRequests(
            false, "John", RequestStatus.ALL.value, false, 101, null, null, null, null, false);

    assertThat(resultSet.size()).isEqualTo(4);
    // MyTeamsRequests only
    for (TopicRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      assertThat(req.getTeamId()).isEqualTo(103);
      if (req.getRequestOperationType().equals(RequestOperationType.CLAIM.value)) {
        assertThat(req.getApprovingTeamId()).isNotEqualTo("103");
      }
    }
  }

  @Test
  @Order(24)
  public void getAllRequestsAndReturnOwnRequestsOnly() {

    List<TopicRequest> jackie =
        selectDataJdbc.getFilteredTopicRequests(
            false, "Jackie", "all", true, 101, null, null, null, null, true);

    assertThat(jackie.size()).isEqualTo(39);
    for (TopicRequest req : jackie) {
      assertThat(req.getRequestor().equals("Jackie"));
    }
  }

  @Test
  @Order(25)
  public void getAllRequestsAndReturnOwnRequestsOnlyFromDevEnv() {

    List<TopicRequest> jackie =
        selectDataJdbc.getFilteredTopicRequests(
            false, "Jackie", "all", true, 101, null, null, "dev", null, true);

    assertThat(jackie.size()).isEqualTo(29);
    for (TopicRequest req : jackie) {
      assertThat(req.getRequestor().equals("Jackie"));
      assertThat(req.getEnvironment().equals("dev"));
    }
  }

  @Test
  @Order(26)
  public void getAllRequestsAndReturnOwnRequestsOnlyCreatedStatusFromDevEnv() {

    List<TopicRequest> jackie =
        selectDataJdbc.getFilteredTopicRequests(
            false, "Jackie", "created", true, 101, null, null, "dev", null, true);

    assertThat(jackie.size()).isEqualTo(17);
    for (TopicRequest req : jackie) {
      assertThat(req.getRequestor().equals("Jackie"));
      assertThat(req.getEnvironment().equals("dev"));
      assertThat(req.getRequestStatus().equals("created"));
    }
  }

  @Test
  @Order(27)
  public void getRequestsAndReturnOwnRequestsOnlyCreatedStatusFromDevEnv() {

    List<TopicRequest> jackie =
        selectDataJdbc.getFilteredTopicRequests(
            false, "Jackie", "created", false, 101, null, null, "dev", null, true);

    assertThat(jackie.size()).isEqualTo(4);
    for (TopicRequest req : jackie) {
      assertThat(req.getRequestor().equals("Jackie"));
      assertThat(req.getEnvironment().equals("dev"));
      assertThat(req.getRequestStatus().equals("created"));
    }
  }

  @Test
  @Order(28)
  public void getRequestsRequestorsOnlyNoneReturned() {

    List<TopicRequest> john =
        selectDataJdbc.getFilteredTopicRequests(
            false, "John", "created", false, 101, null, null, "dev", null, true);

    assertThat(john.size()).isEqualTo(0);
    for (TopicRequest req : john) {
      assertThat(req.getApprovingTeamId().equals("John"));
    }
  }

  @Test
  @Order(29)
  public void getTopicRequestsCountsForMyApprovals() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getTopicRequestsCounts(103, RequestMode.MY_APPROVALS, 101, "Jackie");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);
    // Jackie created all the requests so we expect 0 for created status which is approval status
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(7L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  @Test
  @Order(30)
  public void getTopicRequestsCountsForMyApprovalsJohnCreatedNone() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getTopicRequestsCounts(103, RequestMode.MY_APPROVALS, 101, "John");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(4L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(7L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  private void generateData(
      int number,
      int teamId,
      int tenantId,
      String topicName,
      String env,
      RequestStatus requestStatus,
      RequestOperationType requestOperationType,
      int topicIdentifier,
      String claimOwner) {

    for (int i = 0; i < number; i++) {
      TopicRequest topicRequest = new TopicRequest();
      topicRequest.setTenantId(tenantId);
      topicRequest.setTeamId(teamId);
      topicRequest.setTopicname(topicName + "" + i);
      topicRequest.setEnvironment(env);
      topicRequest.setRequestor("Jackie");
      topicRequest.setRequestStatus(requestStatus.value);
      topicRequest.setRequestOperationType(requestOperationType.value);
      topicRequest.setTopicpartitions(1);
      topicRequest.setReplicationfactor("1");
      topicRequest.setTopicid(topicIdentifier + i);
      if (RequestOperationType.CLAIM == requestOperationType) {
        topicRequest.setApprovingTeamId("" + claimOwner);
      }

      entityManager.persistAndFlush(topicRequest);
    }
  }
}
