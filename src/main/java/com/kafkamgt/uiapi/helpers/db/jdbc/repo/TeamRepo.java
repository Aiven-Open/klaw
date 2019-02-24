package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.Team;
import com.kafkamgt.uiapi.entities.TeamPK;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepo extends CrudRepository<Team, TeamPK> {

    @Override
    Optional<Team> findById(TeamPK teamname);

    @Override
    List<Team> findAll();
}
