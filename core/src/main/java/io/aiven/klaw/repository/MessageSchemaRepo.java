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

  boolean existsByTenantIdAndTopicnameAndEnvironment(
      int tenantId, String topicName, String environmentId);

  List<MessageSchema> findAllByTenantId(int tenantId);

  List<MessageSchema> findAllByTenantIdAndTopicnameAndEnvironment(
      int tenantId, String topicName, String environmentId);

  List<MessageSchema> findAllByTenantIdAndEnvironmentAndTopicnameAndSchemaversion(
      int tenantId, String environmentId, String topicName, String schemaVersion);

  List<MessageSchema> findAllByTenantIdAndTopicnameAndSchemaversionAndEnvironment(
      int tenantId, String topicName, String schemaVersion, String environmentId);

  boolean existsMessageSchemaByEnvironmentAndTenantId(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select exists(select 1 from kwavroschemas where teamid = :teamId and tenantid = :tenantId)",
      nativeQuery = true)
  boolean existsRecordsCountForTeamId(
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

  void deleteByTenantId(int tenantId);

  void deleteByTenantIdAndTopicnameAndEnvironment(
      int tenantId, String topicName, String environmentId);
}
