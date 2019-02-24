package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.SchemaRequest;
import com.kafkamgt.uiapi.entities.SchemaRequestPK;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SchemaRequestRepo extends CrudRepository<SchemaRequest, SchemaRequestPK> {
    Optional<SchemaRequest> findById(SchemaRequestPK schemaRequestPK);
    List<SchemaRequest> findAllByTopicstatus(String topicStatus);
}
