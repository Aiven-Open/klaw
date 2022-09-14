package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.dao.TopicRequestID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TopicRequestsRepo extends CrudRepository<TopicRequest, TopicRequestID> {
  Optional<TopicRequest> findById(TopicRequestID topicRequestId);

  List<TopicRequest> findAllByTopicstatusAndTenantId(String topicStatus, int tenantId);

  List<TopicRequest> findAllByTenantId(int tenantId);

  List<TopicRequest> findAllByTopictypeAndTenantId(String topicType, int tenantId);

  List<TopicRequest> findAllByTopicstatusAndTopicnameAndEnvironmentAndTenantId(
      String topicStatus, String topicName, String envId, int tenantId);

  @Query(
      value =
          "select count(*) from kwtopicrequests where env = :envId and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllTopicRequestsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwtopicrequests where teamid = :teamId and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select max(topicid) from kwtopicrequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextTopicRequestId(@Param("tenantId") Integer tenantId);
}
