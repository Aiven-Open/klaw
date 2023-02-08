package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.AclRequestsRepo;
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
public class AclRequestsIntegrationTest {

  @Autowired private AclRequestsRepo repo;
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
        1);
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
        11);
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
        21);
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
        31);
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
        41);
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
    entityManager.persistAndFlush(user);
    entityManager.persistAndFlush(user2);
  }

  @BeforeEach
  public void setUp() {
    selectDataJdbc = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectDataJdbc, "aclRequestsRepo", repo);
    ReflectionTestUtils.setField(selectDataJdbc, "userInfoRepo", userInfoRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void givenNewQueryMethodAssertThatOldAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<AclRequests> aclListSub = Lists.newArrayList(repo.findAllByTenantId(101));

    List<AclRequests> results =
        selectDataJdbc.selectAclRequests(true, "James", "USER", "ALL", true, 101);

    assertThat(aclListSub.size()).isEqualTo(results.size());
    assertThat(aclListSub).isEqualTo(results);
  }

  @Test
  @Order(2)
  public void getAllRequestsFilteredByTenantId() {

    List<AclRequests> james =
        selectDataJdbc.selectAclRequests(true, "James", "USER", "ALL", true, null, null, null, 101);
    List<AclRequests> john =
        selectDataJdbc.selectAclRequests(true, "John", "USER", "ALL", true, null, null, null, 103);

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
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "created", true, null, "dev", null, 101);
    List<AclRequests> john =
        selectDataJdbc.selectAclRequests(
            true, "John", "USER", "declined", true, null, "dev", null, 103);

    assertThat(james.size()).isEqualTo(20);
    for (AclRequests req : james) {
      assertThat(req.getAclstatus()).isEqualTo("created");
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(4)
  public void getAllRequestsFilteredByAclType() {

    List<AclRequests> james =
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "ALL", true, null, null, AclType.PRODUCER, 101);
    List<AclRequests> james2 =
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "ALL", true, null, null, AclType.CONSUMER, 101);
    List<AclRequests> john =
        selectDataJdbc.selectAclRequests(
            true, "John", "USER", "ALL", true, null, null, AclType.CONSUMER, 103);

    assertThat(james.size()).isEqualTo(1);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getTopictype()).isEqualTo(AclType.PRODUCER.value);
    }
    assertThat(john.size()).isEqualTo(10);
    assertThat(james2.size()).isEqualTo(30);
    for (AclRequests req : james2) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getTopictype()).isEqualTo(AclType.CONSUMER.value);
    }
  }

  @Test
  @Order(5)
  public void getAllRequestsFilteredByTopic() {

    List<AclRequests> james =
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "ALL", true, "firsttopic", null, null, 101);

    assertThat(james.size()).isEqualTo(10);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Test
  @Order(6)
  public void getAllRequestsFilteredByTopicbyEnvironment() {

    List<AclRequests> james =
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "ALL", true, "secondtopic", "test", null, 101);

    assertThat(james.size()).isEqualTo(11);
    for (AclRequests req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
  }

  @Test
  @Order(7)
  public void getAllRequestsFilteredByTopicbyEnvironmentByAclType() {

    List<AclRequests> james =
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "ALL", true, "secondtopic", "test", AclType.CONSUMER, 101);

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
        selectDataJdbc.selectAclRequests(
            true, "James", "USER", "ALL", true, "Secondtopic", "test", AclType.CONSUMER, 101);
    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(9)
  public void getAclRequestsCountsForMyRequestsTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(101, RequestMode.MY_REQUESTS, 101);

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
  @Order(10)
  public void getAclRequestsCountsForApproveForTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(101, RequestMode.TO_APPROVE, 101);

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
  @Order(11)
  public void getAclRequestsCountsForApproveForTeam2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.TO_APPROVE, 101);

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
  @Order(12)
  public void getAclRequestsCountsForMyRequestsForTeam2Tenant1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.MY_REQUESTS, 101);

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
  @Order(13)
  public void getAclRequestsCountsForMyRequestsForTeam2Tenant2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getAclRequestsCounts(103, RequestMode.MY_REQUESTS, 103);

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
      int requestNumber) {

    for (int i = 0; i < number; i++) {
      AclRequests acl = new AclRequests();
      acl.setTenantId(tenantId);
      acl.setTeamId(assignedToTeamId);
      acl.setRequestingteam(requestingTeamId);
      acl.setReq_no(requestNumber++);
      acl.setTopicname(topicName);
      acl.setEnvironment(env);
      acl.setAclType(requestOperationType.value); // Create/Delete ..
      acl.setTopictype(aclType.value);
      if (status != null) {
        acl.setAclstatus(status);
      }
      entityManager.persistAndFlush(acl);
    }
  }
}
