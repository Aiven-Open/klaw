package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.KwKafkaConnector;
import com.kafkamgt.uiapi.dao.KwKafkaConnectorID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface KwKafkaConnectorRepo extends CrudRepository<KwKafkaConnector, KwKafkaConnectorID> {
  Optional<KwKafkaConnector> findById(KwKafkaConnectorID topicId);

  List<KwKafkaConnector> findAllByTenantId(int tenantId);

  List<KwKafkaConnector> findAllByEnvironmentAndTenantId(String env, int tenantId);

  List<KwKafkaConnector> findAllByTeamIdAndTenantId(Integer teamId, int tenantId);

  List<KwKafkaConnector> findAllByEnvironmentAndTeamIdAndTenantId(
      String env, Integer teamId, int tenantId);

  List<KwKafkaConnector> findAllByConnectorNameAndTenantId(String connectorName, int tenantId);

  List<KwKafkaConnector> findAllByConnectorNameAndEnvironmentAndTenantId(
      String connectorName, String env, int tenantId);

  @Query(
      value = "select count(*) from kwkafkaconnector where env = :envId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllConnectorCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwkafkaconnector where teamid = :teamId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select max(connectorid) from kwkafkaconnector where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextConnectorRequestId(@Param("tenantId") Integer tenantId);
}
