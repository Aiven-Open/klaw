package io.aiven.klaw.repository;

import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TopicRepo extends CrudRepository<Topic, TopicID> {
  Optional<Topic> findById(TopicID topicId);

  List<Topic> findAllByTenantId(int tenantId);

  List<Topic> findAllByTenantIdAndTopicnameIn(int tenantId, List<String> topicsNamesList);

  List<Topic> findAllByEnvironmentAndTenantId(String env, int tenantId);

  List<Topic> findAllByTeamIdAndTenantId(Integer teamId, int tenantId);

  List<Topic> findAllByTopicnameAndTenantId(String topicName, int tenantId);

  List<Topic> findAllByTopicnameAndEnvironmentAndTenantId(
      String topicName, String env, int tenantId);

  List<Topic> findAllByTopicnameAndTeamIdAndTenantId(String topicName, int teamId, int tenantId);

  @Query(
      value =
          "select count(distinct(topicname)) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId",
      nativeQuery = true)
  Integer findDistinctCountTopicnameByTeamId(
      @Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select teamid, count(*) from kwtopics where tenantid = :tenantId group by teamid",
      nativeQuery = true)
  List<Object[]> findAllTopicsGroupByTeamId(@Param("tenantId") Integer tenantId);

  @Query(value = "select count(*) from kwtopics", nativeQuery = true)
  List<Object[]> findAllTopicsCount();

  @Query(
      value = "select count(*) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllTopicsForTeam(
      @Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select exists(select 1 from kwtopics where env = :envId and tenantid = :tenantId)",
      nativeQuery = true)
  boolean existsTopicsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select exists(select 1 from kwtopics where teamid = :teamId and tenantid = :tenantId)",
      nativeQuery = true)
  boolean existsRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select env, count(*) from kwtopics "
              + "where env in (select id from kwenv where envtype='kafka' and tenantid = :tenantId) and tenantid = :tenantId group by env",
      nativeQuery = true)
  List<Object[]> findAllTopicsGroupByEnv(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select env, sum(partitions) from kwtopics "
              + "where env in (select id from kwenv where envtype='kafka' and tenantid = :tenantId) and tenantid = :tenantId group by env",
      nativeQuery = true)
  List<Object[]> findAllPartitionsGroupByEnv(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select env, sum(partitions) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId group by env",
      nativeQuery = true)
  List<Object[]> findAllPartitionsForTeamGroupByEnv(
      @Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select env, count(*) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId group by env",
      nativeQuery = true)
  List<Object[]> findAllTopicsForTeamGroupByEnv(
      @Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

  @Query(value = "select max(topicid) from kwtopics where tenantid = :tenantId", nativeQuery = true)
  Integer getNextTopicRequestId(@Param("tenantId") Integer tenantId);

  @Query(
      value = "select topicname from kwtopics where env = :envId and tenantid = :tenantId",
      nativeQuery = true)
  List<String> findAllTopicNamesForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select topicname from kwtopics where env = :envId and teamid = :teamId and tenantid = :tenantId",
      nativeQuery = true)
  List<String> findAllTopicNamesForEnvAndTeam(
      @Param("envId") String envId,
      @Param("teamId") Integer teamId,
      @Param("tenantId") Integer tenantId);

  void deleteByTopicnameAndEnvironmentAndTenantId(String topicName, String env, int tenantId);

  void deleteByTenantId(int tenantId);
}
