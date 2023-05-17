package io.aiven.klaw.repository;

import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.TopicRequestID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface TopicRequestsRepo
    extends CrudRepository<TopicRequest, TopicRequestID>, QueryByExampleExecutor<TopicRequest> {
  Optional<TopicRequest> findById(TopicRequestID topicRequestId);

  List<TopicRequest> findAllByTenantId(int tenantId);

  List<TopicRequest> findAllByRequestStatusAndTopicnameAndEnvironmentAndTenantId(
      String topicStatus, String topicName, String envId, int tenantId);

  @Query(
      value = "select count(*) from kwtopicrequests where env = :envId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllTopicRequestsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwtopicrequests where teamid = :teamId and tenantid = :tenantId",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwtopicrequests where (requestor = :userId) and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForUserId(
      @Param("userId") String userId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select max(topicid) from kwtopicrequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextTopicRequestId(@Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select topictype, count(*) from kwtopicrequests where tenantid = :tenantId"
              + " and teamid = :teamId group by topictype",
      nativeQuery = true)
  List<Object[]> findAllTopicRequestsGroupByOperationType(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select topicstatus, count(*) from kwtopicrequests where tenantid = :tenantId"
              + " and teamid = :teamId group by topicstatus",
      nativeQuery = true)
  List<Object[]> findAllTopicRequestsGroupByStatus(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwtopicrequests where tenantid = :tenantId"
              + " and approvingteamId = :approvingteamId and topictype = :topictype",
      nativeQuery = true)
  long countAllTopicRequestsByApprovingTeamAndTopictype(
      @Param("tenantId") Integer tenantId,
      @Param("approvingteamId") String approvingteamId,
      @Param("topictype") String requestOperationType);

  @Query(
      value =
          "select count(*) from kwtopicrequests where tenantid = :tenantId"
              + " and (teamid = :teamId or approvingteamid = :teamId) and requestor != :requestor "
              + "and topicstatus = :topicStatus  group by topicstatus",
      nativeQuery = true)
  Long countRequestorsTopicRequestsGroupByStatusType(
      @Param("teamId") Integer teamId,
      @Param("tenantId") Integer tenantId,
      @Param("requestor") String requestor,
      @Param("topicStatus") String topicStatus);
}
