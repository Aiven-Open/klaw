package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import io.aiven.klaw.clusterapi.utils.MetricsUtils;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import org.assertj.core.api.AbstractThrowableAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetricsApiServiceTest {

  @Mock private MetricsUtils metricsUtils;
  @Mock private JMXConnector jmxConnector;
  @Mock private MBeanServerConnection mBeanServerConnection;
  @Mock private MBeanInfo mBeanInfo;
  @InjectMocks private MetricsApiService metricsApiService;

  @Test
  void getMetrics() throws Exception {
    String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9996/jmxrmi";
    String objectName = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
    MBeanAttributeInfo mBeanAttributeInfo =
        new MBeanAttributeInfo("name", "type", "description", true, true, false);
    MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[1];
    attributes[0] = mBeanAttributeInfo;

    Mockito.when(metricsUtils.getJmxConnector(jmxUrl)).thenReturn(jmxConnector);
    Mockito.when(jmxConnector.getMBeanServerConnection()).thenReturn(mBeanServerConnection);
    Mockito.when(mBeanServerConnection.getMBeanInfo(any(ObjectName.class))).thenReturn(mBeanInfo);
    Mockito.when(mBeanInfo.getAttributes()).thenReturn(attributes);
    Mockito.when(mBeanServerConnection.getAttribute(any(ObjectName.class), anyString()))
        .thenReturn("attribute");

    Map<String, String> actual = metricsApiService.getMetrics(jmxUrl, objectName);
    Map<String, String> expected = Map.of("name", "attribute");

    Assertions.assertThat(actual).isEqualTo(expected);
  }

  @Test
  void getMetrics_Failure() throws Exception {
    String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9996/jmxrmi";
    String objectName = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
    Exception expected = new Exception("Error while getting metrics.");

    Mockito.when(metricsUtils.getJmxConnector(jmxUrl)).thenThrow(expected);

    AbstractThrowableAssert<?, ? extends Throwable> exception =
        assertThatThrownBy(() -> metricsApiService.getMetrics(jmxUrl, objectName));

    exception.isEqualTo(expected);
  }
}
