package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.dao.SchemaRequestPK;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SchemaRequestRepo extends CrudRepository<SchemaRequest, SchemaRequestPK> {
    Optional<SchemaRequest> findById(SchemaRequestPK schemaRequestPK);
    List<SchemaRequest> findAllByTopicstatus(String topicStatus);
}
