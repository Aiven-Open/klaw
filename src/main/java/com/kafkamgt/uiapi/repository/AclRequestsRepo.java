package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.AclRequests;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface AclRequestsRepo extends CrudRepository<AclRequests, String> {
    Optional<AclRequests> findById(String req_no);
    List<AclRequests>  findAllByEnvironment(String env);
    List<AclRequests> findAllByAclstatus(String topicStatus);
}
