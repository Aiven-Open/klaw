package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.ActivityLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityLogRepo extends CrudRepository<ActivityLog, String> {
    Optional<ActivityLog> findById(String req_no);
    List<ActivityLog> findAllByEnv(String env);
    List<ActivityLog> findAllByEnvAndTeam(String env, String team);

    @Query(value ="select date(activitytime), count(*) from activitylog where" +
            " team = :teamnameVar group by date(activitytime) order by date(activitytime) asc", nativeQuery = true)
    List<Object[]> findActivityLogForTeam(@Param("teamnameVar") String teamnameVar);

}
