package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KwTenants;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepo extends CrudRepository<KwTenants, Integer> {
  @Query(value = "select max(tenantid) from kwtenants", nativeQuery = true)
  Integer getMaxTenantId();
}
