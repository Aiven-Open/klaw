package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicRepo extends CrudRepository<Topic, TopicID> {
    Optional<Topic> findById(TopicID topicId);

    List<Topic> findAllByTenantId(int tenantId);

    List<Topic> findAllByEnvironmentAndTenantId(String env, int tenantId);

    List<Topic> findAllByTeamIdAndTenantId(Integer teamId, int tenantId);

    List<Topic> findAllByEnvironmentAndTeamIdAndTenantId(String env, Integer teamId, int tenantId);

    List<Topic> findAllByTopicnameAndTenantId(String topicName, int tenantId);

    List<Topic> findAllByTopicnameAndEnvironmentAndTenantId(String topicName, String env, int tenantId);

    @Query(value ="select count(distinct(topicname)) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId", nativeQuery = true)
    Integer findDistinctCountTopicnameByTeamId(@Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

    @Query(value ="select teamid, count(*) from kwtopics where tenantid = :tenantId group by teamid", nativeQuery = true)
    List<Object[]> findAllTopicsGroupByTeamId(@Param("tenantId") Integer tenantId);

    @Query(value ="select count(*) from kwtopics", nativeQuery = true)
    List<Object[]> findAllTopicsCount();

    @Query(value ="select count(*) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId", nativeQuery = true)
    List<Object[]> findAllTopicsForTeam(@Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

    @Query(value ="select count(*) from kwtopics where env = :envId and tenantid = :tenantId", nativeQuery = true)
    List<Object[]> findAllTopicsCountForEnv(@Param("envId") String envId, @Param("tenantId") Integer tenantId);

    @Query(value ="select count(*) from kwtopics where teamid = :teamId and tenantid = :tenantId", nativeQuery = true)
    List<Object[]> findAllRecordsCountForTeamId(@Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

    @Query(value ="select env, count(*) from kwtopics " +
            "where env in (select id from kwenv where envtype='kafka' and tenantid = :tenantId) and tenantid = :tenantId group by env", nativeQuery = true)
    List<Object[]> findAllTopicsGroupByEnv(@Param("tenantId") Integer tenantId);

    @Query(value ="select env, sum(partitions) from kwtopics " +
            "where env in (select id from kwenv where envtype='kafka' and tenantid = :tenantId) and tenantid = :tenantId group by env", nativeQuery = true)
    List<Object[]> findAllPartitionsGroupByEnv(@Param("tenantId") Integer tenantId);

    @Query(value ="select env, sum(partitions) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId group by env", nativeQuery = true)
    List<Object[]> findAllPartitionsForTeamGroupByEnv(@Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

    @Query(value ="select env, count(*) from kwtopics where teamid = :teamIdVar and tenantid = :tenantId group by env", nativeQuery = true)
    List<Object[]> findAllTopicsForTeamGroupByEnv(@Param("teamIdVar") Integer teamIdVar, @Param("tenantId") Integer tenantId);

    @Query(value ="select max(topicid) from kwtopics where tenantid = :tenantId", nativeQuery = true)
    Integer getNextTopicRequestId(@Param("tenantId") Integer tenantId);
}
