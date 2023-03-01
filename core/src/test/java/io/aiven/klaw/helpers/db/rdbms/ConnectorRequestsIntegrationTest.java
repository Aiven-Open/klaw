package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
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
public class ConnectorRequestsIntegrationTest {

  @Autowired private KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;
  @Autowired private UserInfoRepo userInfoRepo;

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
        100); // Team1
    generateData(
        3,
        101,
        101,
        "firsttopic2",
        "dev",
        RequestStatus.APPROVED,
        RequestOperationType.CREATE,
        200); // Team1
    generateData(
        7,
        101,
        101,
        "firsttopic3",
        "dev",
        RequestStatus.DECLINED,
        RequestOperationType.CREATE,
        300); // Team1
    generateData(
        2,
        101,
        101,
        "firsttopic4",
        "dev",
        RequestStatus.DELETED,
        RequestOperationType.DELETE,
        400); // Team1
    generateData(
        4,
        103,
        101,
        "firsttopic5",
        "dev",
        RequestStatus.CREATED,
        RequestOperationType.DELETE,
        500); // Team2

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
    ReflectionTestUtils.setField(
        selectDataJdbc, "kafkaConnectorRequestsRepo", kafkaConnectorRequestsRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "userInfoRepo", userInfoRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void getConnectorRequestsCountsForMyRequestsTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(101, RequestMode.MY_REQUESTS, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(5L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(3L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(7L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(2L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(15L);
  }

  @Test
  @Order(2)
  public void getConnectorRequestsCountsForApproveForTeam1() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(101, RequestMode.TO_APPROVE, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(5L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(3L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(7L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(2L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(15L);
  }

  @Test
  @Order(3)
  public void getConnectorRequestsCountsForApproveForTeam2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(103, RequestMode.TO_APPROVE, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(4L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
  }

  @Test
  @Order(3)
  public void getConnectorRequestsCountsForMyRequestsForTeam2() {
    Map<String, Map<String, Long>> results =
        selectDataJdbc.getConnectorRequestsCounts(103, RequestMode.MY_REQUESTS, 101, "James");

    Map<String, Long> statsCount = results.get("STATUS_COUNTS");
    Map<String, Long> operationTypeCount = results.get("OPERATION_TYPE_COUNTS");

    assertThat(results.size()).isEqualTo(2);
    assertThat(statsCount.get(RequestStatus.CREATED.value)).isEqualTo(4L);
    assertThat(statsCount.get(RequestStatus.APPROVED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DECLINED.value)).isEqualTo(0L);
    assertThat(statsCount.get(RequestStatus.DELETED.value)).isEqualTo(0L);
    assertThat(operationTypeCount.get(RequestOperationType.CREATE.value)).isEqualTo(0L);
  }

  private void generateData(
      int number,
      int teamId,
      int tenantId,
      String topicName,
      String env,
      RequestStatus requestStatus,
      RequestOperationType requestOperationType,
      int topicIdentifier) {

    for (int i = 0; i < number; i++) {
      KafkaConnectorRequest connectorRequest = new KafkaConnectorRequest();
      connectorRequest.setTenantId(tenantId);
      connectorRequest.setConnectorName(topicName);
      connectorRequest.setTeamId(teamId);
      connectorRequest.setEnvironment(env);
      connectorRequest.setRequestStatus(requestStatus.value);
      connectorRequest.setRequestOperationType(requestOperationType.value);
      connectorRequest.setConnectorId(topicIdentifier + i);
      connectorRequest.setConnectorConfig("{config}");

      entityManager.persistAndFlush(connectorRequest);
    }
  }
}
