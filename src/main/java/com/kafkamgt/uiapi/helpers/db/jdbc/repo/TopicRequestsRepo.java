package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.TopicRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRequestsRepo extends CrudRepository<TopicRequest, String> {
    Optional<TopicRequest> findById(String topicname);
    List<TopicRequest> findAllByTopicstatus(String topicStatus);
    Optional<TopicRequest> findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(String topicname, String env);
}
