package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicPK;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicRepo extends CrudRepository<Topic, TopicPK> {
    Optional<Topic> findById(TopicPK topicPK);
    List<Topic> findAllByTopicPKEnvironment(String env);
    List<Topic> findAllByTeamname(String teamName);
    List<Topic> findAllByTopicPKEnvironmentAndTeamname(String env, String teamName);
    List<Topic> findAllByTopicPKEnvironmentAndTopicPKTopicname(String env, String topicName);

    List<Topic> findAllByTopicPKTopicname(String topicName);

    @Query(value ="select env, count(*) from kwtopics where teamname = :teamnameVar group by env", nativeQuery = true)
    List<Object[]> findAllTopicsForTeamGroupByEnv(@Param("teamnameVar") String teamnameVar);
}
