package io.aiven.klaw.repository;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvID;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface EnvRepo extends CrudRepository<Env, EnvID> {
  Optional<Env> findById(EnvID id);

  List<Env> findAllByTypeAndTenantId(String type, int tenantId);

  List<Env> findAllByType(String type);

  List<Env> findAllByClusterIdAndTenantId(Integer clusterId, int tenantId);

  List<Env> findAllByTenantId(int tenantId);

  @Query(
          value = "select max(id) from kwenv where tenantid = :tenantId",
          nativeQuery = true)
  Integer getNextId(@Param("tenantId") Integer tenantId);
}
