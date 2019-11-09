package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Team;
import com.kafkamgt.uiapi.dao.TeamPK;
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
