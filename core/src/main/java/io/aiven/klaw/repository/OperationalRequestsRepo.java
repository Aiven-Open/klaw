package io.aiven.klaw.repository;

import io.aiven.klaw.dao.OperationalRequest;
import io.aiven.klaw.dao.OperationalRequestID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public interface OperationalRequestsRepo
    extends CrudRepository<OperationalRequest, OperationalRequestID>,
        QueryByExampleExecutor<OperationalRequest> {
  Optional<OperationalRequest> findById(OperationalRequestID operationalRequestID);

  List<OperationalRequest> findAllByTenantId(int tenantId);

  @Query(
      value = "select max(reqid) from kwoperationalrequests where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextOperationalRequestId(@Param("tenantId") Integer tenantId);
}
