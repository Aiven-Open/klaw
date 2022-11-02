package io.aiven.klaw.clusterapi.utils;

import java.util.HashMap;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsUtils {

  private final HashMap<String, JMXConnector> metricsClientsMap = new HashMap<>();

  public JMXConnector getJmxConnector(String jmxUrl) throws Exception {

    JMXConnector jmxConnector;

    try {
      if (!metricsClientsMap.containsKey(jmxUrl)) {
        JMXServiceURL url;
        log.info("Creating JMX connection {}", jmxUrl);
        url = new JMXServiceURL(jmxUrl);
        jmxConnector = JMXConnectorFactory.connect(url, null);
      } else {
        jmxConnector = metricsClientsMap.get(jmxUrl);
      }
    } catch (Exception exception) {
      log.error("Unable to create JMX Connector " + exception.getMessage(), exception);
      throw new Exception("Cannot connect to JMX Host. Please contact Administrator.");
    }

    if (jmxConnector == null) {
      log.error("Cannot create JMX Connector  {}", jmxUrl);
      throw new Exception("Cannot connect to JMX host. Please contact Administrator.");
    }

    try {
      if (!metricsClientsMap.containsKey(jmxUrl)) {
        metricsClientsMap.put(jmxUrl, jmxConnector);
      }
      return jmxConnector;
    } catch (Exception e) {
      metricsClientsMap.remove(jmxUrl);
      jmxConnector.close();
      log.error("Cannot create JMX Connector {}", jmxUrl, e);
      throw new Exception("Cannot connect to JMX host. Please contact Administrator.");
    }
  }
}
