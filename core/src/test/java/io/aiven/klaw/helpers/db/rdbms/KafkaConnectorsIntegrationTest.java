package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.TopicRequestTypes;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.List;
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
    generateData(10, 101, "firstconn", "dev", TopicRequestTypes.Claim, "created", 1);
    generateData(10, 103, "firstconn", "dev", TopicRequestTypes.Create, "created", 11);
    generateData(10, 101, "secondconn", "dev", TopicRequestTypes.Create, "created", 21);
    generateData(10, 101, "secondconn", "test", TopicRequestTypes.Delete, "declined", 31);
    generateData(1, 101, "secondconn", "test", TopicRequestTypes.Update, "created", 41);
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
        selectDataJdbc.selectConnectorRequestsByStatus(true, "James", "all", true, 101);

    assertThat(KafkaConnectorRequestList.size()).isEqualTo(results.size());

    assertThat(results.containsAll(KafkaConnectorRequestList)).isTrue();
  }

  @Test
  @Order(2)
  public void getallRequestsFilteredByTenantId() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectConnectorRequestsByStatus(true, "James", "all", true, 101);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectConnectorRequestsByStatus(true, "James", "all", true, 103);

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
        selectDataJdbc.selectConnectorRequestsByStatus(true, "James", "created", true, 101);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectConnectorRequestsByStatus(true, "John", "declined", true, 103);

    assertThat(james.size()).isEqualTo(21);
    for (KafkaConnectorRequest req : james) {
      assertThat(req.getConnectorStatus()).isEqualTo("created");
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(4)
  public void
      getallRequestsFilteredByAdditionalStatus_NotAllReqs_IgnoreStatus_ExpectTeamFiltering() {

    List<KafkaConnectorRequest> james =
        selectDataJdbc.selectConnectorRequestsByStatus(false, "James", "deleted", false, 103);
    List<KafkaConnectorRequest> james2 =
        selectDataJdbc.selectConnectorRequestsByStatus(false, "James", "declined", true, 101);
    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectConnectorRequestsByStatus(false, "John", "created", true, 103);

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
        selectDataJdbc.selectConnectorRequestsByStatus(true, "James", "all", false, 101);

    List<KafkaConnectorRequest> john =
        selectDataJdbc.selectConnectorRequestsByStatus(true, "John", "all", false, 103);

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
        selectDataJdbc.selectConnectorRequestsByStatus(true, "James", "ALL", true, 101);
    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(7)
  public void getNonApproversRequestsFilteredByTenantId_willIgnoreOtherPassedParameters() {

    List<KafkaConnectorRequest> tenant1 =
        selectDataJdbc.selectConnectorRequestsByStatus(false, "James", null, true, 101);

    List<KafkaConnectorRequest> tenant2 =
        selectDataJdbc.selectConnectorRequestsByStatus(false, "John", null, true, 103);

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

  private void generateData(
      int number,
      int tenantId,
      String topicName,
      String env,
      TopicRequestTypes connectorType,
      String status,
      int id) {

    for (int i = 0; i < number; i++) {
      KafkaConnectorRequest kc = new KafkaConnectorRequest();
      kc.setConnectorId(id++);
      kc.setTenantId(tenantId);
      kc.setTeamId(tenantId);

      kc.setConnectorName(topicName);
      kc.setEnvironment(env);
      kc.setConnectortype(connectorType.name());
      if (status != null) {
        kc.setConnectorStatus(status);
      }

      if (connectorType.name().equals("Claim")) {
        kc.setDescription(String.valueOf(tenantId));
      }

      entityManager.persistAndFlush(kc);
    }
  }
}
