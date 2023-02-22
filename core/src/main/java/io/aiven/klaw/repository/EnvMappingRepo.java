package io.aiven.klaw.repository;

import io.aiven.klaw.dao.EnvID;
import io.aiven.klaw.dao.EnvMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface EnvMappingRepo extends CrudRepository<EnvMapping, EnvID> {
  Optional<EnvMapping> findById(EnvID id);

  List<EnvMapping> findAllByTenantId(int tenantId);
}
