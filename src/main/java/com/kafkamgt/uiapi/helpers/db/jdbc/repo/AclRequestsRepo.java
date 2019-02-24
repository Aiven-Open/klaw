package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.AclRequests;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AclRequestsRepo extends CrudRepository<AclRequests, String> {
    Optional<AclRequests> findById(String req_no);
    List<AclRequests>  findAllByEnvironment(String env);
    List<AclRequests> findAllByAclstatus(String topicStatus);
}
