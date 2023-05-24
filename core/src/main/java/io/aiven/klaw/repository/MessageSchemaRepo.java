package io.aiven.klaw.repository;

import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.MessageSchemaID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MessageSchemaRepo extends CrudRepository<MessageSchema, MessageSchemaID> {
  Optional<MessageSchema> findById(MessageSchemaID avroSchemaId);

  List<MessageSchema> findAllByTenantId(int tenantId);

  List<MessageSchema> findAllByTenantIdAndTopicnameAndEnvironment(
      int tenantId, String topicName, String environmentId);

  List<MessageSchema> findAllByTenantIdAndTopicnameAndSchemaversionAndEnvironment(
      int tenantId, String topicName, String schemaVersion, String environmentId);

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

  @Query(
      value =
          "select topicname, versionschema from kwavroschemas where env = :envId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findTopicAndVersionsForEnvAndTenantId(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);
}
