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

  List<AclRequests> findAllByTenantId(int tenantId);
}
