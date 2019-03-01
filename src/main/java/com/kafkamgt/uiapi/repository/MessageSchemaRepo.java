package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.MessageSchema;
import com.kafkamgt.uiapi.dao.MessageSchemaPK;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface MessageSchemaRepo extends CrudRepository<MessageSchema, MessageSchemaPK> {
    Optional<MessageSchema> findById(MessageSchemaPK messageSchemaPK);
}
