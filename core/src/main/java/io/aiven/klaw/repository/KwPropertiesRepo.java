package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.KwPropertiesID;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface KwPropertiesRepo extends CrudRepository<KwProperties, KwPropertiesID> {
  KwProperties findFirstByKwKeyAndTenantIdOrderByTenantId(String kwKey, int tenantId);

  List<KwProperties> findAllByTenantId(int tenantId);

  void deleteByTenantId(int tenantId);
}
