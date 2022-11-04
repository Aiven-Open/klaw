package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KwClusterID;
import io.aiven.klaw.dao.KwClusters;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface KwClusterRepo extends CrudRepository<KwClusters, KwClusterID> {
  List<KwClusters> findAllByClusterTypeAndTenantId(String type, int tenantId);

  @Query(
      value = "select max(clusterid) from kwclusters where tenantid = :tenantId",
      nativeQuery = true)
  Integer getNextClusterId(@Param("tenantId") Integer tenantId);

  List<KwClusters> findAllByTenantId(int tenantId);
}
