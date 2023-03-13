package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class KafkaConnectorsIntegrationTest {

  @Autowired private KwKafkaConnectorRequestsRepo repo;
  @Autowired private UserInfoRepo userInfoRepo;

  @Autowired private TeamRepo teamRepo;

  @Autowired TestEntityManager entityManager;

  private SelectDataJdbc selectDataJdbc;

  private UtilMethods utilMethods;

  public void loadData() {
    generateData(10, 101, "firstconn", "dev", RequestOperationType.CLAIM, "created", 1, "103");
    generateData(10, 103, "firstconn", "dev", RequestOperationType.CREATE, "created", 11, null);
    generateData(10, 101, "secondconn", "dev", RequestOperationType.CREATE, "created", 21, null);
    generateData(10, 101, "secondconn", "test", RequestOperationType.DELETE, "declined", 31, null);
    generateData(1, 101, "secondconn", "test", RequestOperationType.UPDATE, "created", 41, null);
    UserInfo user = new UserInfo();
    user.setTenantId(101);
    user.setTeamId(101);
    user.setRole("USER");
    user.setUsername("James");
    UserInfo user2 = new UserInfo();
    user2.setTenantId(103);
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
    entityManager.persistAndFlush(t1);
    entityManager.persistAndFlush(t2);

    //
    generateData(
        5,
        104,
        "thirdtopic",
        "test",
        RequestOperationType.CREATE,
        RequestStatus.CREATED.value,
        50,
        null);
    generateData(
        1,
        104,
        "thirdtopic",
        "test",
        RequestOperationType.CLAIM,
        RequestStatus.DECLINED.value,
        55,
        "103");

    generateData(
        2,
        104,
        "thirdtopic",
        "test",
        RequestOperationType.UPDATE,
        RequestStatus.APPROVED.value,
        57,
        null);
    generateData(
        2,
        104,
        "thirdtopic",
        "test",
        RequestOperationType.DELETE,
        RequestStatus.CREATED.value,
        59,
        null);

    generateData(
        6,
        104,
        "thirdtopic",
        "test",
        RequestOperationType.CREATE,
        RequestStatus.DELETED.value,
        61,
        null);
  }

  @BeforeEach
  public void setUp() {
    selectDataJdbc = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectDataJdbc, "kafkaConnectorRequestsRepo", repo);
    ReflectionTestUtils.setField(selectDataJdbc, "userInfoRepo", userInfoRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "teamRepo", teamRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void givenNewQueryMethodAssertThatDirectQueryAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<KafkaConnectorRequest> KafkaConnectorRequestList =
        Lists.newArrayList(repo.findAllByTenantId(101));

    List<KafkaConnectorRequest> results =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "all", true, 101, null, null);

    assertThat(KafkaConnectorRequestList.size()).isEqualTo(results.size());

    assertThat(results.containsAll(KafkaConnectorRequestList)).isTrue();
  }

  @Test
  @Order(2)
  public void getallRequestsFilteredByTenantId() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "all", true, 101, null, null);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "all", true, 103, null, null);

    assertThat(james.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(10);
    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(3)
  public void getallRequestsFilteredByTenantByStatus() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.CREATED.value, true, 101, null, null);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.DECLINED.value, true, 103, null, null);

    assertThat(james.size()).isEqualTo(21);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(4)
  public void
      getallRequestsFilteredByAdditionalStatus_NotAllReqs_IgnoreStatus_ExpectTeamFiltering() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "James", "deleted", false, 103, null, null);
    List<KafkaConnectorRequest> james2 =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "James", "declined", true, 101, null, null);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "John", "created", true, 103, null, null);

    assertThat(james.size()).isEqualTo(0);

    assertThat(john.size()).isEqualTo(10);
    assertThat(james2.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : james2) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(5)
  public void getallRequestsFilteredByTeanantId() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "all", false, 101, null, null);

    List<KafkaConnectorRequest> john =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "John", "all", false, 103, null, null);

    assertThat(james.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getTeamId()).isEqualTo(101);
    }

    assertThat(john.size()).isEqualTo(10);
    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
      assertThat(req.getTeamId()).isEqualTo(103);
    }
  }

  @Test
  @Order(6)
  public void getallRequestsFilteredByMisSpeltStatus_ReturnNothing() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "ALT", true, 101, null, null);
    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(7)
  public void getNonApproversRequestsFilteredByTenantId_willIgnoreOtherPassedParameters() {

    List<KafkaConnectorRequest> tenant1 =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "James", null, true, 101, null, null);

    List<KafkaConnectorRequest> tenant2 =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "John", null, true, 103, null, null);

    assertThat(tenant1.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : tenant1) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(tenant2.size()).isEqualTo(10);
    for (KafkaConnectorRequest req : tenant2) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(8)
  public void getConnectors() {

    KafkaConnectorRequest tenant1 = selectDataJdbc.selectConnectorRequestsForConnector(1, 101);

    KafkaConnectorRequest tenant2 = selectDataJdbc.selectConnectorRequestsForConnector(11, 103);

    assertThat(tenant1.getTenantId()).isEqualTo(101);
    assertThat(tenant1.getConnectorName()).isEqualTo("firstconn");

    assertThat(tenant2.getTenantId()).isEqualTo(103);
    assertThat(tenant2.getConnectorName()).isEqualTo("firstconn");
  }

  @Test
  @Order(9)
  public void getAllRequestsAndFilterByApproversViewAndTestEnvReturnAll() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "all", true, 101, "test", null);

    assertThat(jackie.size()).isEqualTo(11);

    for (KafkaConnectorRequest req : jackie) {
      assertThat(req.getEnvironment()).isEqualTo("test");
      assertThat(req.getRequestor()).isEqualTo("Jackie");
    }
  }

  @Test
  @Order(10)
  public void getAllRequestsAndFilterByApproversViewAndTestEnvFilterAllJackiesRequestsReturnNone() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "Jackie", "all", true, 101, "test", null);

    assertThat(jackie.size()).isEqualTo(0);
  }

  @Test
  @Order(11)
  public void getNonApproversRequestsFilteredByStatusByWildcard_willIgnorePassedParameters() {

    List<KafkaConnectorRequest> tenant1 =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "James", "deleted", true, 101, null, "One");

    List<KafkaConnectorRequest> tenant2 =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "John", "created", true, 103, null, "two");

    assertThat(tenant1.size()).isEqualTo(31);

    assertThat(tenant2.size()).isEqualTo(10);
  }

  @Test
  @Order(12)
  public void givennonExistentTenantId_ReturnNothing() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", "all", true, 99, null, null);

    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(13)
  public void givenNewQueryMethodAssertThatOldAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<KafkaConnectorRequest> KafkaConnectorRequestList =
        Lists.newArrayList(repo.findAllByTenantId(101));

    List<KafkaConnectorRequest> results =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "James", null, true, 101, null, null);

    assertThat(KafkaConnectorRequestList.size()).isEqualTo(results.size());

    assertThat(results.containsAll(KafkaConnectorRequestList)).isTrue();
  }

  @Test
  @Order(14)
  public void getAllRequestsFilteredByTenantId() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", null, true, 101, null, null);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.getFilteredKafkaConnectorRequests(true, "John", null, true, 103, null, null);

    assertThat(james.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(10);
    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(15)
  public void getAllRequestsFilteredByEnvironmentByStatus() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.CREATED.value, true, 101, "dev", null);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.DECLINED.value, true, 103, "test", null);

    assertThat(james.size()).isEqualTo(20);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(16)
  public void getAllRequestsDontReturnOwnRequestsAsAllReqsIsTrue() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "Jackie", "all", true, 101, null, null);

    assertThat(jackie.size()).isEqualTo(0);
  }

  @Test
  @Order(17)
  public void getAllRequestsAndReturnOwnRequestsAsAllReqsIsTrue() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "Jackie", "all", true, 101, null, null);

    assertThat(jackie.size()).isEqualTo(31);
  }

  @Test
  @Order(18)
  public void getAllCreatedStatusRequestsFromTenant() {

    List<KafkaConnectorRequest> joan =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "Joan", RequestStatus.CREATED.value, true, 104, null, null);

    assertThat(joan.size()).isEqualTo(7);
    for (KafkaConnectorRequest req : joan) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(104);
      assertThat(req.getRequestor()).isNotEqualTo("Joan");
    }
  }

  @Test
  @Order(19)
  public void getAllDELETEDStatusRequestsFromTenant() {

    List<KafkaConnectorRequest> joan =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "Joan", RequestStatus.DELETED.value, true, 104, null, null);

    assertThat(joan.size()).isEqualTo(6);
    for (KafkaConnectorRequest req : joan) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.DELETED.value);
      assertThat(req.getTenantId()).isEqualTo(104);
      assertThat(req.getRequestor()).isNotEqualTo("Joan");
    }
  }

  @Test
  @Order(20)
  public void getAllClaimStatusRequestsForApprovalTeam1() {

    List<KafkaConnectorRequest> resultSet =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.ALL.value, true, 101, null, null);

    assertThat(resultSet.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      if (req.getRequestStatus().equals(RequestOperationType.CLAIM.value)) {
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

    List<KafkaConnectorRequest> resultSet =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "James", RequestStatus.ALL.value, false, 101, null, null);

    assertThat(resultSet.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      if (req.getRequestStatus().equals(RequestOperationType.CLAIM.value)) {
        assertThat(req.getApprovingTeamId()).isNotEqualTo("101");
      }
    }
  }

  @Test
  @Order(22)
  public void getAllClaimStatusRequestsForApprovalTeam2() {

    List<KafkaConnectorRequest> resultSet =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.ALL.value, true, 101, null, null);

    assertThat(resultSet.size()).isEqualTo(31);
    for (KafkaConnectorRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      if (req.getRequestStatus().equals(RequestOperationType.CLAIM.value)) {
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

    List<KafkaConnectorRequest> resultSet =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            false, "John", RequestStatus.ALL.value, false, 103, null, null);
    assertThat(resultSet.size()).isEqualTo(10);

    // MyTeamsRequests only
    for (KafkaConnectorRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(103);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      assertThat(req.getTeamId()).isEqualTo(103);
      if (req.getRequestStatus().equals(RequestOperationType.CLAIM.value)) {
        // only show my tems claim requests
        assertThat(req.getApprovingTeamId()).isNotEqualTo("103");
      }
    }
  }

  @Test
  @Order(24)
  public void getAllCRequestsForApprovalFilteredByWildcardSearch() {

    List<KafkaConnectorRequest> resultSet =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.ALL.value, false, 101, null, "first");

    assertThat(resultSet.size()).isEqualTo(10);

    // MyTeamsRequests only
    for (KafkaConnectorRequest req : resultSet) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getRequestor()).isNotEqualTo("James");
      assertThat(req.getTeamId()).isEqualTo(101);
      assertThat(req.getConnectorName()).isEqualTo("firstconn");
    }
  }

  @Test
  @Order(25)
  public void getAllCRequestsForApprovalFilteredByWildcardSearchNoMatching() {

    List<KafkaConnectorRequest> resultSet =
        selectDataJdbc.getFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.ALL.value, false, 103, null, "lots");
    assertThat(resultSet.size()).isEqualTo(0);
  }

  @Test
  @Order(26)
  public void getConnectorRequestsCountsForMyApprovals() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(101, RequestMode.MY_APPROVALS, 101, "Jackie");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);
    // Jackie created all the requests so we expect 0 for created status which is approval status
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  @Test
  @Order(27)
  public void getConnectorRequestsCountsForMyApprovalsJohnCreatedNone() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(101, RequestMode.MY_APPROVALS, 101, "John");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(21L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CLAIM.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  private void generateData(
      int number,
      int tenantId,
      String topicName,
      String env,
      RequestOperationType connectorType,
      String status,
      int id,
      String claimOwner) {

    for (int i = 0; i < number; i++) {
      KafkaConnectorRequest kc = new KafkaConnectorRequest();
      kc.setConnectorId(id++);
      kc.setTenantId(tenantId);
      kc.setTeamId(tenantId);
      kc.setRequestor("Jackie");
      kc.setConnectorName(topicName);
      kc.setEnvironment(env);
      kc.setRequestOperationType(connectorType.name());
      if (status != null) {
        kc.setRequestStatus(status);
      }

      if (connectorType.value.equals("Claim")) {
        kc.setApprovingTeamId(claimOwner);
      }

      entityManager.persistAndFlush(kc);
    }
  }
}
