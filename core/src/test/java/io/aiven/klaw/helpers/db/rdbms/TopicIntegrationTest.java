package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.repository.EnvRepo;
import io.aiven.klaw.repository.TopicRepo;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class TopicIntegrationTest {

  @Autowired TestEntityManager entityManager;
  @Autowired TopicRepo topicRepo;
  @Autowired EnvRepo envRepo;

  private SelectDataJdbc selectDataJdbc;

  private UtilMethods utilMethods;

  public void loadData() {
    generateData(10, 101, "firsttopic", "dev", 1, 2, 1);
    generateData(10, 103, "firsttopic", "dev", 1, 1, 11);
    generateData(10, 101, "secondtopic", "dev", 4, 2, 21);
    generateData(10, 101, "secondtopic", "tst", 4, 4, 31);
    generateData(1, 101, "secondtopic", "tst", 2, 1, 41);
    UserInfo user = new UserInfo();
    user.setTenantId(101);
    user.setTeamId(101);
    user.setRole("USER");
    user.setUsername("res1");
    UserInfo user2 = new UserInfo();
    user2.setTenantId(103);
    user2.setTeamId(103);
    user2.setRole("USER");
    user2.setUsername("res2");
    entityManager.persistAndFlush(user);
    entityManager.persistAndFlush(user2);

    Env e1 = new Env();
    e1.setId("dev");
    e1.setTenantId(101);
    e1.setName("dev");
    e1.setType("kafka");
    Env e2 = new Env();
    e2.setId("tst");
    e2.setTenantId(101);
    e2.setName("tst");
    e2.setType("kafka");
    Env e3 = new Env();
    e3.setId("dev");
    e3.setTenantId(103);
    e3.setName("dev");
    e3.setType("kafka");

    entityManager.persistAndFlush(e1);
    entityManager.persistAndFlush(e2);
    entityManager.persistAndFlush(e3);
  }

  @BeforeEach
  public void setUp() {
    selectDataJdbc = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectDataJdbc, "topicRepo", topicRepo);
    ReflectionTestUtils.setField(selectDataJdbc, "envRepo", envRepo);
    loadData();
  }

  @Test
  @Order(1)
  public void selectTopicsCountByEnv() {

    List<Map<String, String>> results = selectDataJdbc.selectTopicsCountByEnv(101);
    assertThat(results.size()).isEqualTo(2);
  }

  @Test
  @Order(2)
  public void selectTopicsCountByEnv_MultiTenant() {

    List<Map<String, String>> res1 = selectDataJdbc.selectTopicsCountByEnv(101);
    List<Map<String, String>> res2 = selectDataJdbc.selectTopicsCountByEnv(103);

    assertThat(res1.size()).isEqualTo(2);
    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(3)
  public void selectPartitionsCountByEnv() {

    List<Map<String, String>> res1 = selectDataJdbc.selectPartitionsCountByEnv(101, 101);
    List<Map<String, String>> res2 = selectDataJdbc.selectPartitionsCountByEnv(103, 103);

    assertThat(res1.size()).isEqualTo(2);
    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(4)
  public void selectPartitionsCountByEnv_TeamIdNull() {

    List<Map<String, String>> res1 = selectDataJdbc.selectPartitionsCountByEnv(null, 101);
    List<Map<String, String>> res2 = selectDataJdbc.selectPartitionsCountByEnv(null, 103);

    assertThat(res1.size()).isEqualTo(2);
    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(5)
  public void selectTopicsCountByTeams_TeamIdNull() {

    List<Map<String, String>> res1 = selectDataJdbc.selectTopicsCountByTeams(null, 101);
    List<Map<String, String>> res2 = selectDataJdbc.selectTopicsCountByTeams(null, 103);

    assertThat(res1.size()).isEqualTo(1);
    assertThat(res1.size()).isEqualTo(1);
  }

  @Test
  @Order(6)
  public void selectTopicsCountByTeams() {

    List<Map<String, String>> res1 = selectDataJdbc.selectTopicsCountByTeams(101, 101);
    List<Map<String, String>> res2 = selectDataJdbc.selectTopicsCountByTeams(103, 103);

    assertThat(res1.size()).isEqualTo(1);
    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(7)
  public void selectAllTopicsForTeamGroupByEnv() {

    List<Map<String, String>> res1 = selectDataJdbc.selectAllTopicsForTeamGroupByEnv(101, 101);
    List<Map<String, String>> res2 = selectDataJdbc.selectAllTopicsForTeamGroupByEnv(103, 103);

    assertThat(res1.size()).isEqualTo(2);
    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(8)
  public void selectAllTopicsForTeamGroupByEnv_TeamIdNull() {
    List<Map<String, String>> res1 = selectDataJdbc.selectAllTopicsForTeamGroupByEnv(null, 101);
    List<Map<String, String>> res2 = selectDataJdbc.selectAllTopicsForTeamGroupByEnv(null, 103);

    assertThat(res1.size()).isEqualTo(0);
    assertThat(res2.size()).isEqualTo(0);
  }

  @Test
  @Order(9)
  public void selectTopicDetails() {

    List<Topic> res1 = selectDataJdbc.selectTopicDetails("firsttopic0", 101);
    List<Topic> res2 = selectDataJdbc.selectTopicDetails("firsttopic0", 103);

    assertThat(res1.size()).isEqualTo(1);

    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(10)
  public void getTopicFromId() {
    Optional<Topic> res1 = selectDataJdbc.getTopicFromId(1, 101);
    Optional<Topic> res2 = selectDataJdbc.getTopicFromId(11, 103);

    Optional<Topic> res3 = selectDataJdbc.getTopicFromId(1, 103);

    assertThat(res1.isPresent()).isTrue();
    assertThat(res1.get().getTopicid()).isEqualTo(1);
    assertThat(res1.get().getTopicname()).isEqualTo("firsttopic0");
    assertThat(res2.isPresent()).isTrue();
    assertThat(res2.get().getTopicid()).isEqualTo(11);
    assertThat(res2.get().getTopicname()).isEqualTo("firsttopic0");
    assertThat(res3.isPresent()).isFalse();
  }

  @Test
  @Order(11)
  public void getDashboardInfo() {
    Map<String, String> res1 = selectDataJdbc.getDashboardInfo(101, 101);
    Map<String, String> res2 = selectDataJdbc.getDashboardInfo(103, 103);

    assertThat(res1.size()).isEqualTo(1);
    assertThat(res2.size()).isEqualTo(1);
  }

  @Test
  @Order(10)
  public void getTopics() {
    // Get all topics from tenant
    List<Topic> res1 = selectDataJdbc.getTopics(null, true, 101);
    // Get specific Topic from tenant.
    List<Topic> res2 = selectDataJdbc.getTopics("firsttopic0", false, 103);

    assertThat(res1.size()).isEqualTo(31);

    assertThat(res2.size()).isEqualTo(1);
  }

  private void generateData(
      int number,
      int tenantId,
      String topicName,
      String env,
      int numberOfPartitions,
      int replicationFactor,
      int id) {

    for (int i = 0; i < number; i++) {
      Topic topic = new Topic();
      topic.setTopicid(id++);
      topic.setTenantId(tenantId);
      topic.setTeamId(tenantId);
      topic.setNoOfPartitions(numberOfPartitions);
      topic.setNoOfReplcias(String.valueOf(replicationFactor));
      topic.setTopicname(topicName + i);
      topic.setEnvironment(env);
      entityManager.persistAndFlush(topic);
    }
  }
}
