package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.KafkaConnectorRequestID;
import io.aiven.klaw.model.enums.RequestStatus;
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

  boolean existsByTenantIdAndEnvironmentAndRequestStatusAndConnectorName(
      int tenantId, String environment, String requestStatus, String connectorName);

  boolean existsByTenantIdAndEnvironmentAndRequestStatusAndRequestOperationTypeAndConnectorName(
      int tenantId,
      String environment,
      String requestStatus,
      String requestOperationType,
      String connectorName);

  List<KafkaConnectorRequest> findAllByTenantId(int tenantId);

  default boolean existsConnectorRequestsForEnvTenantIdAndCreatedStatus(
      String envId, Integer tenantId) {
    return existsByEnvironmentAndTenantIdAndRequestStatus(
        envId, tenantId, RequestStatus.CREATED.value);
  }

  boolean existsByEnvironmentAndTenantIdAndRequestStatus(
      @Param("envId") String envId,
      @Param("tenantId") Integer tenantId,
      @Param("requestStatus") String requestStatus);

  @Query(
      value = "select max(connectorid) from kwkafkaconnectorrequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextConnectorRequestId(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select exists(select 1 from kwkafkaconnectorrequests where teamid = :teamId and tenantid = :tenantId and connectorstatus='created')",
      nativeQuery = true)
  boolean existsRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select exists(select 1 from kwkafkaconnectorrequests where (requestor = :userId) and tenantid = :tenantId and connectorstatus='created')",
      nativeQuery = true)
  boolean existsRecordsCountForUserId(
      @Param("userId") String userId, @Param("tenantId") Integer tenantId);

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

  @Query(
      value =
          "select count(*) from kwkafkaconnectorrequests where tenantid = :tenantId"
              + " and (teamid = :teamId or approvingteamid = :approvingTeamid) and requestor != :requestor and connectorstatus = :connectorStatus group by connectorstatus",
      nativeQuery = true)
  Long countRequestorsConnectorRequestsGroupByStatusType(
      @Param("teamId") Integer teamId,
      @Param("approvingTeamid") String approvingTeamid,
      @Param("tenantId") Integer tenantId,
      @Param("requestor") String requestor,
      @Param("connectorStatus") String connectorStatus);

  void deleteByTenantId(int tenantId);
}
