package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Topic;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommonUtilsServiceTest {

  private UtilMethods utilMethods;

  @Mock private ManageDatabase manageDatabase;

  private CommonUtilsService commonUtilsService;

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    commonUtilsService = new CommonUtilsService();
    ReflectionTestUtils.setField(commonUtilsService, "manageDatabase", manageDatabase);
  }

  @Test
  public void getTopicsForTopicName() {
    List<Topic> topicList1 = utilMethods.getMultipleTopics("test1", 20, "1", 101);
    List<Topic> topicList2 = utilMethods.getMultipleTopics("test2", 20, "2", 101);
    topicList1.addAll(topicList2);
    when(manageDatabase.getTopicsForTenant(1)).thenReturn(topicList1);
    List<Topic> topicList = commonUtilsService.getTopicsForTopicName("test10", 1);
    assertThat(topicList).hasSize(1);
  }

  @Test
  public void getSyncTopicsAll() {
    List<Topic> topicList1 = utilMethods.getMultipleTopics("test1", 20, "1", 101);
    List<Topic> topicList2 = utilMethods.getMultipleTopics("test2", 20, "2", 101);
    topicList1.addAll(topicList2);
    when(manageDatabase.getTopicsForTenant(1)).thenReturn(topicList1);
    List<Topic> topicList = commonUtilsService.getTopics(null, null, 1);
    assertThat(topicList).hasSize(40);
  }

  @Test
  public void getSyncTopicsFilterEnvAll() {
    String env = "1";
    when(manageDatabase.getTopicsForTenant(1))
        .thenReturn(utilMethods.getMultipleTopics("test", 20, "1", 101));
    List<Topic> topicList = commonUtilsService.getTopics(env, null, 1);
    assertThat(topicList).hasSize(20);
  }

  @Test
  public void getSyncTopicsFilterEnvNone() {
    String env = "1";
    when(manageDatabase.getTopicsForTenant(1))
        .thenReturn(utilMethods.getMultipleTopics("test", 20, "2", 101));
    List<Topic> topicList = commonUtilsService.getTopics(env, null, 1);
    assertThat(topicList).hasSize(0);
  }

  @Test
  public void getSyncTopicsFilterTeam() {
    when(manageDatabase.getTopicsForTenant(1))
        .thenReturn(utilMethods.getMultipleTopics("test", 20, "2", 102));
    List<Topic> topicList = commonUtilsService.getTopics(null, 101, 1);
    assertThat(topicList).hasSize(0);
  }

  @Test
  public void getSyncTopicsFilterDifferentTeam() {
    List<Topic> topicList1 = utilMethods.getMultipleTopics("test", 20, "1", 101);
    List<Topic> topicList2 = utilMethods.getMultipleTopics("test", 5, "2", 102);
    topicList1.addAll(topicList2);
    when(manageDatabase.getTopicsForTenant(1)).thenReturn(topicList1);
    List<Topic> topicList = commonUtilsService.getTopics(null, 102, 1);
    assertThat(topicList).hasSize(5);
  }

  @Test
  public void getSyncTopicsFilterTeamEnv() {
    List<Topic> topicList1 = utilMethods.getMultipleTopics("test1", 20, "1", 101);
    List<Topic> topicList2 = utilMethods.getMultipleTopics("test2", 5, "2", 102);
    List<Topic> topicList3 = utilMethods.getMultipleTopics("test3", 10, "3", 102);
    topicList1.addAll(topicList2);
    topicList1.addAll(topicList3);
    when(manageDatabase.getTopicsForTenant(1)).thenReturn(topicList1);
    List<Topic> topicList = commonUtilsService.getTopics("3", 102, 1);
    assertThat(topicList).hasSize(10);
  }

  @Test
  public void getSyncTopicsFilterTeamMultipleEnv() {
    List<Topic> topicList1 = utilMethods.getMultipleTopics("test1", 20, "1", 101);
    List<Topic> topicList2 = utilMethods.getMultipleTopics("test2", 5, "2", 102);
    List<Topic> topicList3 = utilMethods.getMultipleTopics("test2", 10, "3", 102);
    topicList1.addAll(topicList2);
    topicList1.addAll(topicList3);
    when(manageDatabase.getTopicsForTenant(1)).thenReturn(topicList1);
    List<Topic> topicList = commonUtilsService.getTopics("3", 102, 1);
    assertThat(topicList).hasSize(15);
  }
}
