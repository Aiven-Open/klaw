package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.AclRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
  @Autowired private AclRepo aclRepo;

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

    Acl acl1 = new Acl();
    acl1.setReq_no(1);
    acl1.setTeamId(101);
    acl1.setTenantId(101);
    acl1.setConsumergroup("test");
    Acl acl2 = new Acl();
    acl2.setReq_no(2);
    acl2.setTeamId(101);
    acl2.setTenantId(101);
    acl2.setConsumergroup("test2");
    Acl acl3 = new Acl();
    acl3.setReq_no(3);
    acl3.setTeamId(103);
    acl3.setTenantId(101);
    acl3.setConsumergroup("test");
    entityManager.persistAndFlush(acl1);
    entityManager.persistAndFlush(acl2);
    entityManager.persistAndFlush(acl3);

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
    ReflectionTestUtils.setField(selectDataJdbc, "aclRepo", aclRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void givenNewQueryMethodAssertThatDirectQueryAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<KafkaConnectorRequest> KafkaConnectorRequestList =
        Lists.newArrayList(repo.findAllByTenantId(101));

    List<KafkaConnectorRequest> results =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", "all", null, true, 101, null, null, false);

    assertThat(KafkaConnectorRequestList).hasSameSizeAs(results);

    assertThat(results).containsAll(KafkaConnectorRequestList);
  }

  @Test
  @Order(2)
  public void getallRequestsFilteredByTenantId() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", "all", null, false, 101, null, null, false);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", "all", null, true, 103, null, null, false);

    assertThat(james).hasSize(21);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john).hasSize(10);
    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(3)
  public void getallRequestsFilteredByTenantByStatus() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.CREATED.value, null, false, 101, null, null, false);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.DECLINED.value, null, false, 103, null, null, false);

    assertThat(james).hasSize(11);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john).isEmpty();
  }

  @Test
  @Order(4)
  public void
      getallRequestsFilteredByAdditionalStatus_NotAllReqs_DoNotIgnoreStatus_ExpectTeamFiltering() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", "deleted", null, false, 103, null, null, false);
    List<KafkaConnectorRequest> james2 =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", "declined", null, true, 101, null, null, false);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "John", "created", null, true, 103, null, null, false);

    assertThat(james).isEmpty();

    assertThat(john).hasSize(10);
    assertThat(james2).hasSize(10);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", "all", null, false, 101, null, null, false);

    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", "all", null, false, 103, null, null, false);

    assertThat(james).hasSize(21);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getTeamId()).isEqualTo(101);
    }

    assertThat(john).hasSize(10);
    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
      assertThat(req.getTeamId()).isEqualTo(103);
    }
  }

  @Test
  @Order(6)
  public void getallRequestsFilteredByMisSpeltStatus_ReturnNothing() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", "ALT", null, true, 101, null, null, false);
    assertThat(james).isEmpty();
  }

  @Test
  @Order(7)
  public void getNonApproversRequestsFilteredByTenantId_willIgnoreOtherPassedParameters() {

    List<KafkaConnectorRequest> tenant1 =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", null, null, true, 101, null, null, false);

    List<KafkaConnectorRequest> tenant2 =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "John", null, null, true, 103, null, null, false);

    assertThat(tenant1).hasSize(31);
    for (KafkaConnectorRequest req : tenant1) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(tenant2).hasSize(10);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", "all", null, true, 101, "test", null, false);

    assertThat(jackie).hasSize(11);

    for (KafkaConnectorRequest req : jackie) {
      assertThat(req.getEnvironment()).isEqualTo("test");
      assertThat(req.getRequestor()).isEqualTo("Jackie");
    }
  }

  @Test
  @Order(10)
  public void getAllRequestsAndFilterByApproversViewAndTestEnvFilterAllJackiesRequestsReturnNone() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "Jackie", "all", null, true, 101, "test", null, false);

    assertThat(jackie).isEmpty();
  }

  @Test
  @Order(11)
  public void getNonApproversRequestsFilteredByStatusByWildcard_StatusIsNotIgnored() {

    List<KafkaConnectorRequest> tenant1 =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", "deleted", null, true, 101, null, "conn", false);

    List<KafkaConnectorRequest> tenant2 =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "John", "created", null, true, 103, null, "conn", false);

    assertThat(tenant1).isEmpty();

    assertThat(tenant2).hasSize(10);
  }

  @Test
  @Order(12)
  public void givennonExistentTenantId_ReturnNothing() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", "all", null, true, 99, null, null, false);

    assertThat(james).isEmpty();
  }

  @Test
  @Order(13)
  public void givenNewQueryMethodAssertThatOldAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<KafkaConnectorRequest> KafkaConnectorRequestList =
        Lists.newArrayList(repo.findAllByTenantId(101));

    List<KafkaConnectorRequest> results =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", null, null, true, 101, null, null, false);

    assertThat(KafkaConnectorRequestList).hasSameSizeAs(results);

    assertThat(results).containsAll(KafkaConnectorRequestList);
  }

  @Test
  @Order(14)
  public void getAllRequestsFilteredByTenantId() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", null, null, false, 101, null, null, false);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", null, null, false, 103, null, null, false);

    assertThat(james).hasSize(21);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john).hasSize(10);
    for (KafkaConnectorRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(15)
  public void getAllRequestsFilteredByEnvironmentByStatus() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.CREATED.value, null, false, 101, "dev", null, false);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.DECLINED.value, null, false, 103, "test", null, false);

    assertThat(james).hasSize(10);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
    assertThat(john).isEmpty();
  }

  @Test
  @Order(16)
  public void getAllRequestsDontReturnOwnRequestsAsAllReqsIsTrue() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "Jackie", "all", null, true, 101, null, null, false);

    assertThat(jackie).isEmpty();
  }

  @Test
  @Order(17)
  public void getAllRequestsAndReturnOwnRequestsAsAllReqsIsTrue() {

    List<KafkaConnectorRequest> jackie =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "Jackie", "all", null, true, 101, null, null, false);

    assertThat(jackie).hasSize(31);
  }

  @Test
  @Order(18)
  public void getAllCreatedStatusRequestsFromTenant() {

    List<KafkaConnectorRequest> joan =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "Joan", RequestStatus.CREATED.value, null, true, 104, null, null, false);

    assertThat(joan).hasSize(7);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "Joan", RequestStatus.DELETED.value, null, true, 104, null, null, false);

    assertThat(joan).hasSize(6);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.ALL.value, null, false, 101, null, null, false);

    assertThat(resultSet).hasSize(21);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", RequestStatus.ALL.value, null, false, 101, null, null, false);

    assertThat(resultSet).hasSize(31);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.ALL.value, null, true, 101, null, null, false);

    assertThat(resultSet).hasSize(31);
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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "John", RequestStatus.ALL.value, null, false, 103, null, null, false);
    assertThat(resultSet).hasSize(10);

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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "John", RequestStatus.ALL.value, null, false, 101, null, "first", false);

    assertThat(resultSet).hasSize(10);

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
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", RequestStatus.ALL.value, null, false, 103, null, "lots", false);
    assertThat(resultSet).isEmpty();
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
    assertThat(statsCount).containsEntry(RequestStatus.CREATED.value, 0L);
    assertThat(operationTypeCount)
        .containsEntry(RequestOperationType.CREATE.value, 10L)
        .containsEntry(RequestOperationType.CLAIM.value, 10L);
    assertThat(statsCount)
        .containsEntry(RequestStatus.APPROVED.value, 0L)
        .containsEntry(RequestStatus.DECLINED.value, 10L)
        .containsEntry(RequestStatus.DELETED.value, 0L);
    assertThat(operationTypeCount).containsEntry(RequestOperationType.UPDATE.value, 1L);
  }

  @Test
  @Order(27)
  public void getConnectorRequestsCountsForMyApprovalsJohnCreatedNone() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(101, RequestMode.MY_APPROVALS, 101, "John");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);
    assertThat(statsCount).containsEntry(RequestStatus.CREATED.value, 21L);
    assertThat(operationTypeCount)
        .containsEntry(RequestOperationType.CREATE.value, 10L)
        .containsEntry(RequestOperationType.CLAIM.value, 10L);
    assertThat(statsCount)
        .containsEntry(RequestStatus.APPROVED.value, 0L)
        .containsEntry(RequestStatus.DECLINED.value, 10L)
        .containsEntry(RequestStatus.DELETED.value, 0L);
    assertThat(operationTypeCount).containsEntry(RequestOperationType.UPDATE.value, 1L);
  }

  @Test
  @Order(28)
  public void getConnectorRequestsWhereRequestOperationIsCreate() {

    List<KafkaConnectorRequest> createRequests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "James", null, RequestOperationType.CREATE, false, 101, null, null, false);

    assertThat(createRequests).hasSize(10);
  }

  @Order(26)
  @ParameterizedTest
  @CsvSource({
    "FIRSTTOPIC1,firsttopic1",
    "TOPIC1,firsttopic1",
    "FirstTopic1,firsttopic1",
    "firSToPic1,firsttopic1"
  })
  public void getConnectorRequestsForTeamViewFilteredbySearchTerm(
      String searchCriteria, String expectedTopicName) {

    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "John", null, null, false, 101, null, searchCriteria, false);

    for (KafkaConnectorRequest req : john) {
      assertThat(req.getConnectorName()).isEqualTo(expectedTopicName);
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Order(27)
  @ParameterizedTest
  @CsvSource({
    "Claim,Claim,10",
    "Delete,Delete,10",
    "Update,Update,1",
    "Promote,Promote,0",
    "Create,Create,10"
  })
  public void getConnectorRequestsForTeamViewFilteredbyRequestOperationType(
      String requestOperationType, String expectedTopicName, String number) {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false,
            "James",
            null,
            RequestOperationType.of(requestOperationType),
            false,
            101,
            null,
            null,
            false);

    for (KafkaConnectorRequest req : james) {
      assertThat(req.getRequestOperationType()).isEqualTo(expectedTopicName);
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(james).hasSize(Integer.parseInt(number));
  }

  @Order(28)
  @ParameterizedTest
  @CsvSource({"created,11", "deleted,0", "declined,10", "approved,0"})
  public void getConnectorRequestsForTeamViewFilteredbyRequestStatus(
      String requestStatus, String number) {
    // allreqs true so only requests from your team will be returned.
    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, "James", requestStatus, null, false, 101, null, null, false);

    for (KafkaConnectorRequest req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(requestStatus);
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(james).hasSize(Integer.parseInt(number));
  }

  @Order(29)
  @ParameterizedTest
  @CsvSource({"James,0", "Jackie,0", "John,0"})
  public void getConnectorRequestsForApprovalIsMyRequestTrueNonReturned(
      String requestor, String number) {
    // allreqs true so only requests from your team will be returned.
    List<KafkaConnectorRequest> requests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            true, requestor, null, null, false, 101, null, null, true);

    for (KafkaConnectorRequest req : requests) {
      assertThat(req.getRequestor()).isEqualTo(requestor);
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(requests).hasSize(Integer.parseInt(number));
  }

  @Order(30)
  @ParameterizedTest
  @CsvSource({"James,101,0", "Jackie,103,10", "John,103,0"})
  public void getConnectorRequestsForTeamViewIsMyRequestTrueRequestorsRequestsOnlyReturned(
      String requestor, String teamId, String number) {
    // allreqs true so only requests from your team will be returned.
    List<KafkaConnectorRequest> requests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, requestor, null, null, false, 103, null, null, true);

    for (KafkaConnectorRequest req : requests) {
      assertThat(req.getRequestor()).isEqualTo(requestor);
      assertThat(req.getTeamId()).isEqualTo(Integer.valueOf(teamId));
    }

    assertThat(requests).hasSize(Integer.parseInt(number));
  }

  @Order(31)
  @ParameterizedTest
  @CsvSource({"James,0", "Jackie,31", "John,0"})
  public void getConnectorRequestsForTeamViewAllTeamsIsMyRequestTrueRequestorsRequestsOnlyReturned(
      String requestor, String number) {
    // allreqs true so only requests from your team will be returned.
    List<KafkaConnectorRequest> requests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, requestor, null, null, true, 101, null, null, true);

    for (KafkaConnectorRequest req : requests) {
      assertThat(req.getRequestor()).isEqualTo(requestor);
    }

    assertThat(requests).hasSize(Integer.parseInt(number));
  }

  @Order(32)
  @ParameterizedTest
  @CsvSource({"James,31", "Jackie,31", "John,31"})
  public void getConnectorRequests_GetAllTeamsRequestsInTenantcy(String requestor, String number) {
    // allreqs true so only requests from your team will be returned.
    List<KafkaConnectorRequest> requests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, requestor, null, null, true, 101, null, null, false);

    assertThat(requests).hasSize(Integer.parseInt(number));
  }

  @Order(33)
  @Test
  public void getConnectorRequests_GetAllTeamsRequestsInTenantcy() {
    // allreqs true so only requests from your team will be returned.
    List<KafkaConnectorRequest> requests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, "Jackie", null, null, true, 103, null, null, false);

    assertThat(requests).hasSize(10);
  }

  @Order(34)
  @Test
  public void testValidateIfConsumerGroupUsedByAnotherTeam() {
    assertThat(selectDataJdbc.validateIfConsumerGroupUsedByAnotherTeam(101, 101, "test")).isTrue();
    assertThat(selectDataJdbc.validateIfConsumerGroupUsedByAnotherTeam(101, 101, "test2"))
        .isFalse();
    assertThat(selectDataJdbc.validateIfConsumerGroupUsedByAnotherTeam(101, 101, "non existing"))
        .isFalse();
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
      kc.setRequestOperationType(connectorType.value);
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
