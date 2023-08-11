package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.OffsetDetails;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.offsets.ResetConsumerGroupOffsetsRequest;
import io.aiven.klaw.clusterapi.services.ConsumerGroupService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Slf4j
@AllArgsConstructor
public class ConsumerGroupsController {
  ConsumerGroupService consumerGroupService;

  /**
   * Reset offsets of a consumer group provided topic name, consumer group, reset type(earliest,
   * latest), and timestamp if applicable
   *
   * @param bootstrapServers environment param
   * @param protocol SSL or plaintext
   * @param clusterIdentification cluster id
   * @param consumerGroupOffsetsRequest request object containing topic, group and reset type
   * @return ApiResponse with OffsetReset data object
   * @throws Exception any exception while retrieving or resetting offsets
   */
  @PostMapping(
      value = "/consumerGroupOffsets/reset/{bootstrapServers}/{protocol}/{clusterIdentification}",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ApiResponse resetConsumerOffsets(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterIdentification,
      @Valid ResetConsumerGroupOffsetsRequest consumerGroupOffsetsRequest)
      throws Exception {
    return consumerGroupService.resetConsumerGroupOffsets(
        bootstrapServers, protocol, clusterIdentification, consumerGroupOffsetsRequest);
  }

  /**
   * @param bootstrapServers env param
   * @param protocol ssl or plaintext protocol
   * @param clusterName cluster id
   * @param consumerGroupId consumer group
   * @param topicName topic name
   * @return current offset positions
   * @throws Exception any exception while retrieving offsets
   */
  @RequestMapping(
      value =
          "/getConsumerOffsets/{bootstrapServers}/{protocol}/{clusterName}/{consumerGroupId}/{topicName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<OffsetDetails>> getConsumerOffsets(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterName,
      @PathVariable String consumerGroupId,
      @PathVariable String topicName)
      throws Exception {
    List<OffsetDetails> consumerOffsetDetails =
        consumerGroupService.getConsumerGroupDetails(
            consumerGroupId, topicName, bootstrapServers, protocol, clusterName);

    return new ResponseEntity<>(consumerOffsetDetails, HttpStatus.OK);
  }
}
