package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.TopicRequestTypes;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
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
public class TopicRequestsIntegrationTest {

  @Autowired private TopicRequestsRepo repo;
  @Autowired private UserInfoRepo userInfoRepo;

  @Autowired private TeamRepo teamRepo;

  @Autowired TestEntityManager entityManager;

  private SelectDataJdbc selectDataJdbc;

  private UtilMethods utilMethods;

  public void loadData() {
    generateData(10, 101, "firsttopic", "dev", TopicRequestTypes.Claim, "created", 1);
    generateData(10, 103, "firsttopic", "dev", TopicRequestTypes.Create, "created", 11);
    generateData(10, 101, "secondtopic", "dev", TopicRequestTypes.Create, "created", 21);
    generateData(10, 101, "secondtopic", "test", TopicRequestTypes.Delete, "declined", 31);
    generateData(1, 101, "secondtopic", "test", TopicRequestTypes.Update, "created", 41);
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

    Team t3 = new Team();
    t3.setTeamId(104);
    t3.setTenantId(101);
    t3.setTeamname("trust");
    Team t4 = new Team();
    t4.setTeamname("trust");
    t4.setTeamId(105);
    t4.setTenantId(101);
    entityManager.persistAndFlush(t3);
    entityManager.persistAndFlush(t4);
  }

  @BeforeEach
  public void setUp() {
    selectDataJdbc = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectDataJdbc, "topicRequestsRepo", repo);
    ReflectionTestUtils.setField(selectDataJdbc, "userInfoRepo", userInfoRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "teamRepo", teamRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void givenNewQueryMethodAssertThatOldAndNewQueriesReturnTheSameResultSet() {
    // Old way of getting all requests
    List<TopicRequest> topicRequestList = Lists.newArrayList(repo.findAllByTenantId(101));

    List<TopicRequest> results =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", null, true, 101, null, null, null);

    assertThat(topicRequestList.size()).isEqualTo(results.size());

    assertThat(results.containsAll(topicRequestList)).isTrue();
  }

  @Test
  @Order(2)
  public void getAllRequestsFilteredByTenantId() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", null, true, 101, null, null, null);
    List<TopicRequest> john =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", null, true, 103, null, null, null);

    assertThat(james.size()).isEqualTo(31);
    for (TopicRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }
    assertThat(john.size()).isEqualTo(10);
    for (TopicRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(3)
  public void getAllRequestsFilteredByEnvironmentByStatus() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "created", true, 101, null, "dev", null);
    List<TopicRequest> john =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "John", "declined", true, 103, null, "test", null);

    assertThat(james.size()).isEqualTo(20);
    for (TopicRequest req : james) {
      assertThat(req.getTopicstatus()).isEqualTo("created");
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getEnvironment()).isEqualTo("dev");
    }
    assertThat(john.size()).isEqualTo(0);
  }

  @Test
  @Order(4)
  public void getAllRequestsFilteredByStatus() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "deleted", true, 101, null, null, null);
    List<TopicRequest> james2 =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "declined", true, 101, null, null, null);
    List<TopicRequest> john =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "John", "created", true, 103, null, null, null);

    assertThat(james.size()).isEqualTo(0);

    assertThat(john.size()).isEqualTo(10);
    assertThat(james2.size()).isEqualTo(10);
    for (TopicRequest req : james2) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getTopictype()).isEqualTo(TopicRequestTypes.Delete.name());
    }

    for (TopicRequest req : john) {
      assertThat(req.getTenantId()).isEqualTo(103);
      assertThat(req.getTopictype()).isEqualTo(TopicRequestTypes.Create.name());
    }
  }

  @Test
  @Order(5)
  public void getAllRequestsFilteredByTopicWildcard() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "ALL", true, 101, null, null, "first");

    List<TopicRequest> john =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "John", "ALL", true, 101, null, null, "second");

    List<TopicRequest> all =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "ALL", true, 101, null, null, "topic");

    assertThat(james.size()).isEqualTo(10);
    for (TopicRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(john.size()).isEqualTo(21);
    for (TopicRequest req : john) {
      assertThat(req.getTopicname()).isEqualTo("secondtopic");
    }

    assertThat(all.size()).isEqualTo(31);
  }

  @Test
  @Order(6)
  public void getAllRequestsFilteredByTopicByEnvironment() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "ALL", true, 101, null, "test", "second");

    assertThat(james.size()).isEqualTo(11);
    for (TopicRequest req : james) {
      assertThat(req.getTopicname()).isEqualTo("secondtopic");
      assertThat(req.getEnvironment()).isEqualTo("test");
    }
  }

  @Test
  @Order(7)
  public void getAllRequestsFilteredByTeamName() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "ALL", true, 101, "octopus", null, null);

    List<TopicRequest> john =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "John", "ALL", true, 103, "pirate", null, null);

    assertThat(james.size()).isEqualTo(31);
    for (TopicRequest req : james) {
      assertThat(req.getTenantId()).isEqualTo(101);
      assertThat(req.getTeamId()).isEqualTo(101);
    }

    assertThat(john.size()).isEqualTo(10);
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
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "ALL", true, 101, null, "tsst", null);
    assertThat(james.size()).isEqualTo(0);
  }

  @Test
  @Order(9)
  public void getNonApproversRequestsFilteredByTenantId_willIgnoreOtherPassedParameters() {

    List<TopicRequest> tenant1 =
        selectDataJdbc.selectTopicRequestsByStatus(
            false, "James", null, true, 101, null, null, null);

    List<TopicRequest> tenant2 =
        selectDataJdbc.selectTopicRequestsByStatus(
            false, "John", null, true, 103, null, null, null);

    assertThat(tenant1.size()).isEqualTo(31);
    for (TopicRequest req : tenant1) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(tenant2.size()).isEqualTo(10);
    for (TopicRequest req : tenant2) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(10)
  public void
      getNonApproversRequestsFilteredByEnvironmentByStatus_willIgnoreOtherPassedParameters() {

    List<TopicRequest> tenant1 =
        selectDataJdbc.selectTopicRequestsByStatus(
            false, "James", "created", true, 101, null, "dev", null);
    List<TopicRequest> tenant2 =
        selectDataJdbc.selectTopicRequestsByStatus(
            false, "John", "declined", true, 103, null, "test", null);

    assertThat(tenant1.size()).isEqualTo(31);
    for (TopicRequest req : tenant1) {
      assertThat(req.getTenantId()).isEqualTo(101);
    }

    assertThat(tenant2.size()).isEqualTo(10);
    for (TopicRequest req : tenant2) {
      assertThat(req.getTenantId()).isEqualTo(103);
    }
  }

  @Test
  @Order(11)
  public void getNonApproversRequestsFilteredByStatusByWildcard_willIgnoreOtherPassedParameters() {

    List<TopicRequest> tenant1 =
        selectDataJdbc.selectTopicRequestsByStatus(
            false, "James", "deleted", true, 101, null, null, "One");

    List<TopicRequest> tenant2 =
        selectDataJdbc.selectTopicRequestsByStatus(
            false, "John", "created", true, 103, null, null, "two");

    assertThat(tenant1.size()).isEqualTo(31);

    assertThat(tenant2.size()).isEqualTo(10);
  }

  @Test
  @Order(12)
  public void givenTeamNameWithMultipleResponses_doNotFilterByTeam() {

    List<TopicRequest> james =
        selectDataJdbc.selectTopicRequestsByStatus(
            true, "James", "declined", true, 101, "trust", null, null);

    assertThat(james.size()).isEqualTo(10);
  }

  private void generateData(
      int number,
      int tenantId,
      String topicName,
      String env,
      TopicRequestTypes topicType,
      String status,
      int id) {

    for (int i = 0; i < number; i++) {
      TopicRequest tr = new TopicRequest();
      tr.setTopicid(id++);
      tr.setTenantId(tenantId);
      tr.setTeamId(tenantId);

      tr.setTopicname(topicName);
      tr.setEnvironment(env);
      tr.setTopictype(topicType.name());
      if (status != null) {
        tr.setTopicstatus(status);
      }
      if (topicType.equals(TopicRequestTypes.Claim)) {
        tr.setDescription(String.valueOf(tenantId));
      }
      entityManager.persistAndFlush(tr);
    }
  }
}
