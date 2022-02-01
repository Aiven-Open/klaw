package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.TeamID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepo extends CrudRepository<Team, TeamID> {

    @Override
    Optional<Team> findById(TeamID teamId);

    List<Team> findAllByTenantId(int tenantId);

    List<Team> findAllByTenantIdAndTeamname(int tenantId, String teamName);

    List<Team> findAllByTenantIdAndTeamId(int tenantId, Integer teamId);

    @Query(value ="select max(teamid) from kwteams where tenantid = :tenantId", nativeQuery = true)
    Integer getNextTeamId(@Param("tenantId") Integer tenantId);
}
