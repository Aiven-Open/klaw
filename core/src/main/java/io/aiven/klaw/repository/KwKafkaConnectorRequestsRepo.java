package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.KafkaConnectorRequestID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface KwKafkaConnectorRequestsRepo
    extends CrudRepository<KafkaConnectorRequest, KafkaConnectorRequestID>,
        QueryByExampleExecutor<KafkaConnectorRequest> {
  Optional<KafkaConnectorRequest> findById(KafkaConnectorRequestID connectorRequestId);

  List<KafkaConnectorRequest> findAllByRequestStatusAndConnectorNameAndEnvironmentAndTenantId(
      String connectorStatus, String connectorName, String envId, int tenantId);

  List<KafkaConnectorRequest> findAllByTenantId(int tenantId);

  @Query(
      value =
          "select count(*) from kwkafkaconnectorrequests where env = :envId and tenantid = :tenantId and connectorstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllConnectorRequestsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select max(connectorid) from kwkafkaconnectorrequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextConnectorRequestId(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwkafkaconnectorrequests where teamid = :teamId and tenantid = :tenantId and connectorstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select connectortype, count(*) from kwkafkaconnectorrequests where tenantid = :tenantId"
              + " and teamid = :teamId group by connectortype",
      nativeQuery = true)
  List<Object[]> findAllConnectorRequestsGroupByOperationType(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select connectorstatus, count(*) from kwkafkaconnectorrequests where tenantid = :tenantId"
              + " and teamid = :teamId group by connectorstatus",
      nativeQuery = true)
  List<Object[]> findAllConnectorRequestsGroupByStatus(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);
}
