package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.KwPropertiesID;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface KwPropertiesRepo extends CrudRepository<KwProperties, KwPropertiesID> {
  KwProperties findFirstByKwKeyAndTenantIdOrderByTenantId(String kwKey, int tenantId);

  @Query(
      value = "SELECT * FROM kwproperties WHERE tenantid IN :tenantIds ORDER BY tenantId",
      nativeQuery = true)
  List<KwProperties> findAllByTenantIdsOrdered(@Param("tenantIds") Collection<Integer> tenantIds);

  List<KwProperties> findAllByTenantId(int tenantId);

  void deleteByTenantId(int tenantId);
}
