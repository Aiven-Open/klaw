package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.AclRequestID;
import com.kafkamgt.uiapi.dao.AclRequests;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AclRequestsRepo extends CrudRepository<AclRequests, AclRequestID> {
    Optional<AclRequests> findById(AclRequestID aclRequestID);
    List<AclRequests> findAllByAclstatusAndTenantId(String topicStatus, int tenantId);

    @Query(value ="select count(*) from kwaclrequests where env = :envId and tenantid = :tenantId and topicstatus='created'", nativeQuery = true)
    List<Object[]> findAllAclRequestsCountForEnv(@Param("envId") String envId, @Param("tenantId") Integer tenantId);

    @Query(value ="select count(*) from kwaclrequests where teamid = :teamId and tenantid = :tenantId and topicstatus='created'",
            nativeQuery = true)
    List<Object[]> findAllRecordsCountForTeamId(@Param("teamId") Integer teamId, @Param("tenantId") Integer tenantId);

    @Query(value ="select max(aclid) from kwaclrequests where tenantid = :tenantId", nativeQuery = true)
    Integer getNextAclRequestId(@Param("tenantId") Integer tenantId);

    List<AclRequests>  findAllByTenantId(int tenantId);
}
