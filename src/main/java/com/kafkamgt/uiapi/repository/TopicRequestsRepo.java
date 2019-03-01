package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.dao.TopicRequestPK;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRequestsRepo extends CrudRepository<TopicRequest, TopicRequestPK> {
    Optional<TopicRequest> findById(TopicRequestPK topicRequestPK);
    List<TopicRequest> findAllByTopicstatus(String topicStatus);
    Optional<TopicRequest> findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(String topicname, String env);
}
