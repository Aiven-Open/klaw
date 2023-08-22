package io.aiven.klaw.repository;

import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.TopicRequestID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface TopicRequestsRepo
    extends CrudRepository<TopicRequest, TopicRequestID>, QueryByExampleExecutor<TopicRequest> {
  Optional<TopicRequest> findById(TopicRequestID topicRequestId);

  List<TopicRequest> findAllByTenantId(int tenantId);

  boolean existsByTenantIdAndEnvironmentAndRequestStatusAndTopicname(
      int tenantId, String environment, String requestStatus, String topicname);

  boolean existsByTenantIdAndEnvironmentAndRequestStatusAndRequestOperationTypeAndTopicname(
      int tenantId,
      String environment,
      String requestStatus,
      String requestOperationType,
      String topicname);

  boolean existsByTenantIdAndRequestStatusAndRequestOperationTypeAndTopicname(
      int tenantId, String requestStatus, String requestOperationType, String topicname);

  boolean existsByTenantIdAndEnvironmentAndRequestStatus(
      int tenantId, String environment, String requestStatus);

  List<TopicRequest> findAllByRequestStatusAndTopicnameAndEnvironmentAndTenantId(
      String topicStatus, String topicName, String envId, int tenantId);

  boolean existsByEnvironmentAndTenantId(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select exists(select 1 from kwtopicrequests where teamid = :teamId and tenantid = :tenantId and topicstatus='created')",
      nativeQuery = true)
  boolean existsRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select exists(select 1 from kwtopicrequests where (requestor = :userId) and tenantid = :tenantId and topicstatus='created')",
      nativeQuery = true)
  boolean existsRecordsCountForUserId(
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

  default Map<String, Long> getCountPerTopicType(Integer teamId, Integer tenantId) {
    return deriveCountsFromRequests(findAllTopicRequestsGroupByOperationType(teamId, tenantId));
  }

  @Query(
      value =
          "select topicstatus, count(*) from kwtopicrequests where tenantid = :tenantId"
              + " and teamid = :teamId group by topicstatus",
      nativeQuery = true)
  List<Object[]> findCountPerTopicStatus(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  default Map<String, Long> getCountPerTopicStatus(Integer teamId, Integer tenantId) {
    return deriveCountsFromRequests(findCountPerTopicStatus(teamId, tenantId));
  }

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
              + " and (teamid = :teamId or approvingteamid = :approvingTeamId) and requestor != :requestor "
              + "and topicstatus = :topicStatus  group by topicstatus",
      nativeQuery = true)
  Long countRequestorsTopicRequestsGroupByStatusType(
      @Param("teamId") Integer teamId,
      @Param("approvingTeamId") String approvingTeamId,
      @Param("tenantId") Integer tenantId,
      @Param("requestor") String requestor,
      @Param("topicStatus") String topicStatus);

  void deleteByTenantId(int tenantId);

  private Map<String, Long> deriveCountsFromRequests(List<Object[]> list) {
    var result = new HashMap<String, Long>(list.size());
    for (var elem : list) {
      result.put((String) elem[0], (Long) elem[1]);
    }
    return result;
  }
}
