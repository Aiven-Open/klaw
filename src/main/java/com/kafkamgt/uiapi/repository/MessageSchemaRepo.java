package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.MessageSchema;
import com.kafkamgt.uiapi.dao.MessageSchemaID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MessageSchemaRepo extends CrudRepository<MessageSchema, MessageSchemaID> {
  Optional<MessageSchema> findById(MessageSchemaID avroSchemaId);

  List<MessageSchema> findAllByTenantId(int tenantId);

  @Query(
      value = "select count(*) from kwavroschemas where env = :envId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllSchemaCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select count(*) from kwavroschemas where teamid = :teamId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select max(avroschemaid) from kwavroschemas where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextSchemaId(@Param("tenantId") Integer tenantId);
}
