package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.constants.TestConstants;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TopicContentsServiceTest {
  @Mock private ClusterApiUtils clusterApiUtils;
  private TopicContentsService topicContentsService;

  @BeforeEach
  void setup() {
    topicContentsService = new TopicContentsService(clusterApiUtils);
  }

  @Test
  void readEvents() {
    String protocol = "SSL";
    int offsetPosition = 0;
    String readMessagesType = "OFFSET_ID";
    String bootstrapServers = "localhost:9092";

    Mockito.when(clusterApiUtils.getSslConfig(TestConstants.CLUSTER_IDENTIFICATION))
        .thenReturn(new Properties());

    Map<Long, String> actual =
        topicContentsService.readEvents(
            bootstrapServers,
            protocol,
            TestConstants.CLUSTER_NAME,
            TestConstants.CONSUMER_GROUP_ID,
            TestConstants.TOPIC_NAME,
            offsetPosition,
            readMessagesType,
            TestConstants.CLUSTER_IDENTIFICATION);

    Map<Long, String> expected = Collections.emptyMap();
    Assertions.assertThat(actual).isEqualTo(expected);
  }
}
