package io.aiven.klaw.repository;

import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.ActivityLogID;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepo extends CrudRepository<ActivityLog, ActivityLogID> {
  Optional<ActivityLog> findById(ActivityLogID activityLogID);

  List<ActivityLog> findAllByEnvAndTenantId(String env, int tenantId);

  List<ActivityLog> findAllByEnvAndTeamIdAndTenantId(String env, Integer teamId, int tenantId);

  List<ActivityLog> findAllByTeamIdAndTenantId(Integer team, int tenantId);

  @Query(
      value =
          "select date(activitytime), count(*) from kwactivitylog where "
              + " env in :envId and tenantid = :tenantId"
              + " group by date(activitytime) order by date(activitytime) asc limit :limit",
      nativeQuery = true)
  List<Object[]> findActivityLogForLastNDays(
      @Param("envId") String[] envId,
      @Param("tenantId") Integer tenantId,
      @Param("limit") Integer limit);

  @Query(
      value = "select max(kwreqno) from kwactivitylog where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextActivityLogRequestId(@Param("tenantId") Integer tenantId);

  List<ActivityLog> findAllByTenantId(int tenantId);

  @Query(
      value =
          "select date(activitytime), count(*) from kwactivitylog where"
              + " teamid = :teamIdVar and tenantid = :tenantId and activityTime > :activityTimeAfter group by date(activitytime) order by date(activitytime) asc",
      nativeQuery = true)
  List<Object[]> findActivityLogForTeamIdAfter(
      @Param("teamIdVar") Integer teamIdVar,
      @Param("tenantId") Integer tenantId,
      @Param("activityTimeAfter") Timestamp activityTime);

  List<ActivityLog> findByTeamIdAndTenantIdAndActivityTimeAfter(
      Integer teamId, int tenantId, Timestamp activityTime);
}
