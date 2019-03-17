package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicPK;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepo extends CrudRepository<Topic, TopicPK> {
    Optional<Topic> findById(TopicPK topicPK);
    List<Topic> findAllByTopicPKEnvironment(String env);
    Optional<Topic> findByTopicPKEnvironmentAndTopicPKTopicname(String env, String topicName);
}
