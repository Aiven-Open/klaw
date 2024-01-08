package io.aiven.klaw.repository;

import io.aiven.klaw.dao.AclApproval;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface AclApprovalsRepo
    extends CrudRepository<AclApproval, String>, QueryByExampleExecutor<AclApproval> {}
