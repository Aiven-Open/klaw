package io.aiven.klaw.repository;

import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.SchemaRequestID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface SchemaRequestRepo
    extends CrudRepository<SchemaRequest, SchemaRequestID>, QueryByExampleExecutor<SchemaRequest> {
  Optional<SchemaRequest> findById(SchemaRequestID schemaRequestId);

  List<SchemaRequest> findAllByTopicstatusAndTenantId(String topicStatus, int tenantId);

  @Query(
      value = "select max(avroschemaid) from kwschemarequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextSchemaRequestId(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwschemarequests where env = :envId and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllSchemaRequestsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwschemarequests where teamid = :teamId and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  List<SchemaRequest> findAllByTenantId(int tenantId);
}
