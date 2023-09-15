package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.*;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AclRequestsIntegrationTest {

  @Autowired private AclRequestsRepo repo;

  @Autowired private SchemaRequestRepo schemaRequestRepo;

  @Autowired private KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

  @Autowired private TopicRequestsRepo topicRequestsRepo;

  @Autowired private UserInfoRepo userInfoRepo;

  @Autowired TestEntityManager entityManager;

  private SelectDataJdbc selectDataJdbc;

  private UtilMethods utilMethods;

  public void loadData() {
    generateData(
        10,
        101,
        101,
        101,
        "firsttopic",
        "dev",
        RequestOperationType.CREATE,
        AclType.CONSUMER,
        RequestStatus.CREATED.value,
        1,
        "Jackie");
    generateData(
        10,
        103,
        103,
        103,
        "firsttopic",
        "dev",
        RequestOperationType.CREATE,
        AclType.CONSUMER,
        RequestStatus.CREATED.value,
        11,
        "Jackie");
    generateData(
        10,
        101,
        101,
        103,
        "secondtopic",
        "dev",
        RequestOperationType.CREATE,
        AclType.CONSUMER,
        RequestStatus.CREATED.value,
        21,
        "Jackie");
    generateData(
        10,
        101,
        101,
        103,
        "secondtopic",
        "test",
        RequestOperationType.CREATE,
        AclType.CONSUMER,
        RequestStatus.DECLINED.value,
        31,
        "Jackie");
    generateData(
        1,
        101,
        103,
        101,
        "secondtopic",
        "test",
        RequestOperationType.DELETE,
        AclType.PRODUCER,
        RequestStatus.CREATED.value,
        41,
        "Jackie");
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
    user3.setTenantId(101);
    user3.setTeamId(101);
    user3.setRole("USER");
    user3.setUsername("Jackie");
    entityManager.persistAndFlush(user);
    entityManager.persistAndFlush(user2);
    entityManager.persistAndFlush(user3);

    UserInfo user4 = new UserInfo();
    user4.setTenantId(104);
    user4.setTeamId(104);
    user4.setRole("USER");
    user4.setUsername("Gorph");
    entityManager.persistAndFlush(user4);
    UserInfo user5 = new UserInfo();
    user5.setTenantId(104);
    user5.setTeamId(104);
    user5.setRole("USER");
    user5.setUsername("Turf");
    entityManager.persistAndFlush(user5);

    generateData(
        10,
        104,
        104,
        104,
        "firsttopic",
        "dev",
        RequestOperationType.CREATE,
        AclType.CONSUMER,
        RequestStatus.CREATED.value,
        1,
        "Gorph");
    generateData(
        10,
        104,
        104,
        104,
        "firsttopic",
        "dev",
        RequestOperationType.CREATE,
        AclType.PRODUCER,
        RequestStatus.CREATED.value,
        11,
        "Gorph");
  }

  @BeforeEach
  public void setUp() {
    selectDataJdbc = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectDataJdbc, "aclRequestsRepo", repo);
    ReflectionTestUtils.setField(selectDataJdbc, "schemaRequestRepo", schemaRequestRepo);
    ReflectionTestUtils.setField(
        selectDataJdbc, "kafkaConnectorRequestsRepo", kafkaConnectorRequestsRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "topicRequestsRepo", topicRequestsRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "userInfoRepo", userInfoRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void givenNewQueryMethodAssertThatOldAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<AclRequests> aclListSub = Lists.newArrayList(repo.findAllByTenantId(101));

    List<AclRequests> results =
        selectDataJdbc.selectFilteredAclRequests(
            true, "James", "USER", "ALL", null, true, null, null, null, null, false, 101);

    assertThat(aclListSub.size()).isEqualTo(results.size());
    assertThat(aclListSub).isEqualTo(results);
  }

  @Test
  @Order(2)
  public void getAllRequestsFilteredByTenantId() {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true, "James", "USER", "ALL", null, true, null, null, null, null, false, 101);
    List<AclRequests> john =
        selectDataJdbc.selectFilteredAclRequests(
            true, "John", "USER", "ALL", null, true, null, null, null, null, false, 103);

    assertThat(james.size()).isEqualTo(31);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(10);
    for (AclRequests req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(3)
  public void getAllRequestsFilteredByEnvironment() {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "James",
            "USER",
            RequestStatus.CREATED.value,
            null,
            true,
            null,
            "dev",
            null,
            null,
            false,
            101);
    List<AclRequests> john =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "John",
            "USER",
            RequestStatus.DECLINED.value,
            null,
            true,
            null,
            "dev",
            null,
            null,
            false,
            103);

    assertThat(james.size()).isEqualTo(20);
    for (AclRequests req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(RequestStatus.CREATED.value);
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(4)
  public void getAllRequestsFilteredByAclType() {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "James",
            "USER",
            "ALL",
            null,
            true,
            null,
            null,
            null,
            AclType.PRODUCER,
            false,
            101);
    List<AclRequests> james2 =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "James",
            "USER",
            "ALL",
            null,
            true,
            null,
            null,
            null,
            AclType.CONSUMER,
            false,
            101);
    List<AclRequests> john =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "John",
            "USER",
            "ALL",
            null,
            true,
            null,
            null,
            null,
            AclType.CONSUMER,
            false,
            103);

    assertThat(james.size()).isEqualTo(1);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getAclType()).isEqualTo(AclType.PRODUCER.value);
    }
    assertThat(john.size()).isEqualTo(10);
    assertThat(james2.size()).isEqualTo(30);
    for (AclRequests req : james2) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getAclType()).isEqualTo(AclType.CONSUMER.value);
    }
  }

  @Test
  @Order(5)
  public void getAllRequestsFilteredByTopic() {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true, "James", "USER", "ALL", null, true, "firsttopic", null, null, null, false, 101);

    assertThat(james.size()).isEqualTo(10);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Test
  @Order(6)
  public void getAllRequestsFilteredByTopicbyEnvironment() {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "James",
            "USER",
            "ALL",
            null,
            true,
            "secondtopic",
            "test",
            null,
            null,
            false,
            101);

    assertThat(james.size()).isEqualTo(11);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Test
  @Order(7)
  public void getAllRequestsFilteredByTopicbyEnvironmentByAclType() {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "James",
            "USER",
            "ALL",
            null,
            true,
            "secondtopic",
            "test",
            null,
            AclType.CONSUMER,
            false,
            101);

    assertThat(james.size()).isEqualTo(10);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Test
  @Order(8)
  public void getAllRequestsFilteredByMisSpeltTopicbyEnvironmentByAclType_ReturnNothing() {
    // topic name is case sensitive.
    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true,
            "James",
            "USER",
            "ALL",
            null,
            true,
            "Secondtopic",
            "test",
            null,
            AclType.CONSUMER,
            false,
            101);
    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(9)
  public void getAllRequestsDontReturnOwnRequestsForApproval() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            true, "Jackie", "USER", "ALL", null, true, null, null, null, null, false, 101);
    assertThat(jackie.size()).isEqualTo(0);
  }

  @Test
  @Order(10)
  public void getAllRequestsAndReturnOwnRequests() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false, "Jackie", "USER", "ALL", null, true, null, null, null, null, false, 101);
    assertThat(jackie.size()).isEqualTo(31);
  }

  @Test
  @Order(11)
  public void getAclRequestsCountsForMyRequestsTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(101, RequestMode.MY_REQUESTS, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(20L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(30L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.DELETE.value)).isEqualTo(0L);
  }

  @Test
  @Order(12)
  public void getAclRequestsCountsForApproveForTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(101, RequestMode.TO_APPROVE, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(11L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(10L);
    assertThat(operationTypeCount.get(RequestOperationType.UPDATE.value)).isEqualTo(0L);
  }

  // note tenantId is 101 here
  @Test
  @Order(13)
  public void getAclRequestsCountsForApproveForTeam2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.TO_APPROVE, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(20L);
    assertThat(operationTypeCount.get(RequestOperationType.DELETE.value)).isEqualTo(0L);
  }

  // note tenantId is 101
  @Test
  @Order(14)
  public void getAclRequestsCountsForMyRequestsForTeam2Tenant1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.MY_REQUESTS, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(1L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.DELETE.value)).isEqualTo(1L);
  }

  // note tenantId is 103
  @Test
  @Order(15)
  public void getAclRequestsCountsForMyRequestsForTeam2Tenant2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.MY_REQUESTS, 103, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(10L);
    assertThat(operationTypeCount.get(RequestOperationType.DELETE.value)).isEqualTo(0L);
  }

  @Test
  @Order(16)
  public void getAllRequestsOnlyReturnMyRequests() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false, "Jackie", "USER", "ALL", null, true, null, null, null, null, true, 101);
    assertThat(jackie.size()).isEqualTo(31);
    for (AclRequests req : jackie) {
      assertThat(req.getRequestor()).isEqualTo("Jackie");
    }
  }

  @Test
  @Order(17)
  public void getAllRequestsOnlyReturnMyRequestsFromDevEnv() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false, "Jackie", "USER", "ALL", null, true, null, "dev", null, null, true, 101);
    assertThat(jackie.size()).isEqualTo(20);
    for (AclRequests req : jackie) {
      assertThat(req.getRequestor()).isEqualTo("Jackie");
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
  }

  @Test
  @Order(18)
  public void getAllRequestsOnlyReturnMyRequestsOfProducerAclType() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false,
            "Jackie",
            "USER",
            "ALL",
            null,
            true,
            null,
            null,
            null,
            AclType.PRODUCER,
            true,
            101);
    assertThat(jackie.size()).isEqualTo(1);
    for (AclRequests req : jackie) {
      assertThat(req.getRequestor()).isEqualTo("Jackie");
      assertThat(req.getAclType()).isEqualTo(AclType.PRODUCER.value);
    }
  }

  @Test
  @Order(19)
  public void getAllRequestsOnlyReturnMyRequestsOfTopicFirstTopic() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false, "Jackie", "USER", "ALL", null, true, "firsttopic", null, null, null, true, 101);
    assertThat(jackie.size()).isEqualTo(10);
    for (AclRequests req : jackie) {
      assertThat(req.getRequestor()).isEqualTo("Jackie");
      assertThat(req.getTopicname()).isEqualTo("firsttopic");
    }
  }

  @Test
  @Order(20)
  public void getAllRequestsOnlyReturnWhereEnvisTestMyRequestsOfTopicFirstTopic() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false, "Jackie", "USER", "ALL", null, true, null, "test", null, null, true, 101);
    assertThat(jackie.size()).isEqualTo(11);
    for (AclRequests req : jackie) {
      assertThat(req.getRequestor()).isEqualTo("Jackie");
      assertThat(req.getTopicname()).isEqualTo("secondtopic");
      assertThat(req.getEnvironment()).isEqualTo("test");
    }
  }

  @Test
  @Order(21)
  public void
      getAllRequestsOnlyReturnWhereEnvisTestAndAclTypeConsumerMyRequestsOfTopicFirstTopic() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false,
            "Jackie",
            "USER",
            "ALL",
            null,
            true,
            null,
            "test",
            null,
            AclType.CONSUMER,
            true,
            101);
    assertThat(jackie.size()).isEqualTo(10);
    for (AclRequests req : jackie) {
      assertThat(req.getRequestor()).isEqualTo("Jackie");
      assertThat(req.getTopicname()).isEqualTo("secondtopic");
      assertThat(req.getEnvironment()).isEqualTo("test");
      assertThat(req.getAclType()).isEqualTo(AclType.CONSUMER.value);
    }
  }

  @Test
  @Order(21)
  public void getAllRequestsReturnAllRequests() {
    List<AclRequests> jackie =
        selectDataJdbc.selectFilteredAclRequests(
            false, "Jackie", "USER", "ALL", null, true, null, null, null, null, false, 101);
    assertThat(jackie.size()).isEqualTo(31);
  }

  @Test
  @Order(22)
  public void getAclRequestsCountsForMyApprovals() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.MY_APPROVALS, 101, "Jackie");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);
    // Jackie created all the requests so there should be none returned for Jackie.
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(20L);
    assertThat(operationTypeCount.get(RequestOperationType.DELETE.value)).isEqualTo(0L);
  }

  @Test
  @Order(23)
  public void getAclRequestsCountsForMyApprovalsJohnCreatedNone() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.MY_APPROVALS, 101, "John");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results).hasSize(2);

    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(10L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(20L);
    assertThat(operationTypeCount.get(RequestOperationType.DELETE.value)).isEqualTo(0L);
  }

  @Test
  @Order(24)
  public void getRequestsExistenceForCreatedStatus() {
    assertThat(selectDataJdbc.existsComponentsCountForUser("Jackie", 101)).isTrue();
    assertThat(selectDataJdbc.existsComponentsCountForUser("Jackie", 103)).isTrue();
  }

  @Order(24)
  @ParameterizedTest
  @CsvSource({
    "FIRSTTOPIC1,firsttopic1",
    "TOPIC1,firsttopic1",
    "FirstTopic1,firsttopic1",
    "firSToPic1,firsttopic1"
  })
  public void getAclRequestsForTeamViewFilteredbySearchTerm(
      String searchCriteria, String expectedTopicName) {

    List<AclRequests> john =
        selectDataJdbc.selectFilteredAclRequests(
            false, "John", null, null, null, false, null, null, searchCriteria, null, false, 101);

    for (AclRequests req : john) {
      assertThat(req.getTopicname()).isEqualTo(expectedTopicName);
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Order(25)
  @ParameterizedTest
  @CsvSource({
    "Claim,Claim,0",
    "Delete,Delete,0",
    "Update,Update,0",
    "Promote,Promote,0",
    "Create,Create,30"
  })
  public void getAclRequestsForTeamViewFilteredbyRequestOperationType(
      String requestOperationType, String expectedTopicName, String number) {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            false,
            "James",
            null,
            null,
            RequestOperationType.of(requestOperationType),
            false,
            null,
            null,
            null,
            null,
            false,
            101);

    for (AclRequests req : james) {
      assertThat(req.getRequestOperationType()).isEqualTo(expectedTopicName);
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(james).hasSize(Integer.valueOf(number));
  }

  @Order(26)
  @ParameterizedTest
  @CsvSource({"created,21", "deleted,0", "declined,10", "approved,0"})
  public void getAclRequestsForTeamViewFilteredbyRequestStatus(
      String requestStatus, String number) {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            false, "James", null, requestStatus, null, true, null, null, null, null, false, 101);

    for (AclRequests req : james) {
      assertThat(req.getRequestStatus()).isEqualTo(requestStatus);
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(james).hasSize(Integer.valueOf(number));
  }

  @Order(27)
  @ParameterizedTest
  @CsvSource({"James,31", "Jackie,0", "John,31"})
  public void getAclRequests_GetAllTeamsRequestsInTenantcy(String requestor, String number) {

    List<AclRequests> james =
        selectDataJdbc.selectFilteredAclRequests(
            true, requestor, null, null, null, true, null, null, null, null, false, 101);

    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(james).hasSize(Integer.valueOf(number));
  }

  @Order(28)
  @Test
  public void getAclRequests_GetAllTeamsRequestsInTenantcyNewTenant() {

    int tenantId = 104;
    List<AclRequests> requests =
        selectDataJdbc.selectFilteredAclRequests(
            true, "Turf", null, null, null, true, null, null, null, null, false, tenantId);

    for (AclRequests req : requests) {
      assertThat(req.getTenantId()).isEqualTo(tenantId);
    }

    assertThat(requests).hasSize(Integer.valueOf(20));
  }

  private void generateData(
      int number,
      int tenantId,
      int requestingTeamId,
      int assignedToTeamId,
      String topicName,
      String env,
      RequestOperationType requestOperationType,
      AclType aclType,
      String status,
      int requestNumber,
      String requestor) {

    for (int i = 0; i < number; i++) {
      AclRequests acl = new AclRequests();
      acl.setTenantId(tenantId);
      acl.setTeamId(assignedToTeamId);
      acl.setRequestingteam(requestingTeamId);
      acl.setReq_no(requestNumber++);
      acl.setTopicname(topicName);
      acl.setEnvironment(env);
      acl.setRequestor(requestor);
      acl.setRequestOperationType(aclType.value);
      acl.setRequestOperationType(requestOperationType.value); // Create/Delete ..
      acl.setAclType(aclType.value);
      acl.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
      if (status != null) {
        acl.setRequestStatus(status);
      }
      entityManager.persistAndFlush(acl);
    }
  }
}
