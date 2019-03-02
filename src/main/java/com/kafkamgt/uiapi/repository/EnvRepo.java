package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Env;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface EnvRepo extends CrudRepository<Env, String> {
    Optional<Env> findById(String name);
    List<Env> findAllByType(String type);
    Optional<Env> findByName(String environment);
}
