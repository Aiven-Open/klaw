package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.services.MetricsApiService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metrics")
public class MetricsApiController {

  // JMX_HOST is hardcoded here below as this is work in progress and not totally integrated.
  public static final String JMX_HOST = "localhost:9996";
  @Autowired MetricsApiService metricsApiService;

  @PostMapping(value = "/getMetrics")
  public ResponseEntity<Map<String, String>> getMetrics(
      @RequestBody MultiValueMap<String, String> metricsRequest) throws Exception {
    String metricsObjectName = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
    String jmxUrl = "service:jmx:rmi:///jndi/rmi://" + JMX_HOST + "/jmxrmi";
    return new ResponseEntity<>(
        metricsApiService.getMetrics(jmxUrl, metricsObjectName), HttpStatus.OK);
  }
}
