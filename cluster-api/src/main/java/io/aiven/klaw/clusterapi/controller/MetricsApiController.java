package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.services.MetricsApiService;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/metrics")
@Slf4j
public class MetricsApiController {

  @Autowired MetricsApiService metricsApiService;

  @PostMapping(value = "/getMetrics")
  public ResponseEntity<HashMap<String, String>> getMetrics(
      @RequestBody MultiValueMap<String, String> metricsRequest) throws Exception {
    String metricsObjectName = "kafka.server:type=BrokerTopicMetrics,name=MessagesInPerSec";
    String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9996/jmxrmi";
    HashMap<String, String> metrics = metricsApiService.getMetrics(jmxUrl, metricsObjectName);
    return new ResponseEntity<>(metrics, HttpStatus.OK);
  }
}
