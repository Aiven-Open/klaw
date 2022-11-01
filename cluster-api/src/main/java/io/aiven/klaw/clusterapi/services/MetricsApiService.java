package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.utils.MetricsUtils;
import java.util.HashMap;
import javax.management.*;
import javax.management.remote.JMXConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsApiService {

  @Autowired MetricsUtils metricsUtils;

  //    public void getMetrics(){
  //        getMetrics("service:jmx:rmi:///jndi/rmi://localhost:9996/jmxrmi",
  //                "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec");
  //    }

  public HashMap<String, String> getMetrics(String jmxUrl, String objectName) throws Exception {
    HashMap<String, String> metricsMap = new HashMap<>();
    try {
      JMXConnector jmxc = metricsUtils.getJmxConnector(jmxUrl);
      MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

      //            String[] domains = mbsc.getDomains();
      //            System.out.println(Arrays.toString(domains));

      // kafka.server:name=MessagesInPerSec,topic=topicName,type=BrokerTopicMetrics
      ObjectName mxbeanName = new ObjectName(objectName);
      MBeanAttributeInfo[] attributes = mbsc.getMBeanInfo(mxbeanName).getAttributes();

      for (MBeanAttributeInfo attr : attributes) {
        metricsMap.put(attr.getName(), "" + mbsc.getAttribute(mxbeanName, attr.getName()));
        log.debug(attr.getName() + " " + mbsc.getAttribute(mxbeanName, attr.getName()));
      }
    } catch (Exception e) {
      log.error("Error {}", e.toString());
      throw e;
    }
    return metricsMap;
  }
}
