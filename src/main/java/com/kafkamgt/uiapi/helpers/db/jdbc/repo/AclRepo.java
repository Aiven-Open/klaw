package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.Acl;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AclRepo extends CrudRepository<Acl, String> {
    Optional<Acl> findById(String req_no);
    List<Acl> findAllByEnvironment(String environment);
    Optional<Acl> findByTopicnameAndEnvironmentAndTopictypeAndConsumergroupAndAclipAndAclssl(String topicName,
                                                                                               String env,
                                                                                               String topicType,
                                                                                               String consumerGroup,
                                                                                               String aclIp,
                                                                                               String aclSsl
    );
}
