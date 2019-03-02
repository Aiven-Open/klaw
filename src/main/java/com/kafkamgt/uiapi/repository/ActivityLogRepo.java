package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.ActivityLog;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityLogRepo extends CrudRepository<ActivityLog, String> {
    Optional<ActivityLog> findById(String req_no);
    List<ActivityLog> findAllByEnv(String env);
    List<ActivityLog> findAllByEnvAndTeam(String env, String team);
}
