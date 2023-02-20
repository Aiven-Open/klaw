package io.aiven.klaw.repository;

import io.aiven.klaw.dao.AclRequestID;
import io.aiven.klaw.dao.AclRequests;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface AclRequestsRepo
    extends CrudRepository<AclRequests, AclRequestID>, QueryByExampleExecutor<AclRequests> {
  Optional<AclRequests> findById(AclRequestID aclRequestID);

  List<AclRequests> findAllByTenantId(int tenantId);

  @Query(
      value =
          "select count(*) from kwaclrequests where env = :envId and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllAclRequestsCountForEnv(
      @Param("envId") String envId, @Param("tenantId") Integer tenantId);

  @Query(
      value =
          "select count(*) from kwaclrequests where teamid = :teamId and tenantid = :tenantId and topicstatus='created'",
      nativeQuery = true)
  List<Object[]> findAllRecordsCountForTeamId(
      @Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

  @Query(
      value = "select max(aclid) from kwaclrequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextAclRequestId(@Param("tenantId") Integer tenantId);

  // requests raised by my team
  @Query(
      value =
          "select acltype, count(*) from kwaclrequests where tenantid = :tenantId"
              + " and requestingteam = :requestingTeamId group by acltype",
      nativeQuery = true)
  List<Object[]> findAllAclRequestsGroupByOperationTypeMyTeam(
      @Param("requestingTeamId") Integer requestingTeamId, @Param("tenantId") Integer tenantId);

  // requests raised by my team
  @Query(
      value =
          "select topicstatus, count(*) from kwaclrequests where tenantid = :tenantId"
              + " and requestingteam = :requestingTeamId group by topicstatus",
      nativeQuery = true)
  List<Object[]> findAllAclRequestsGroupByStatusMyTeam(
      @Param("requestingTeamId") Integer requestingTeamId, @Param("tenantId") Integer tenantId);

  // requests assigned to my team
  @Query(
      value =
          "select acltype, count(*) from kwaclrequests where tenantid = :tenantId"
              + " and teamid = :assignedToTeamId group by acltype",
      nativeQuery = true)
  List<Object[]> findAllAclRequestsGroupByOperationTypeAssignedToTeam(
      @Param("assignedToTeamId") Integer assignedToTeamId, @Param("tenantId") Integer tenantId);

  // requests assigned to my team
  @Query(
      value =
          "select topicstatus, count(*) from kwaclrequests where tenantid = :tenantId"
              + " and teamid = :assignedToTeamId group by topicstatus",
      nativeQuery = true)
  List<Object[]> findAllAclRequestsGroupByStatusAssignedToTeam(
      @Param("assignedToTeamId") Integer assignedToTeamId, @Param("tenantId") Integer tenantId);
}
