package io.aiven.klaw.repository;

import io.aiven.klaw.dao.UserInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserInfoRepo extends CrudRepository<UserInfo, String> {
  Optional<UserInfo> findById(String userid);

  Optional<UserInfo> findByUsernameIgnoreCase(String username);

  List<UserInfo> findAllByTenantId(int tenantId);

  List<UserInfo> findAllByTenantIdAndUsername(int tenantId, String username);

  List<UserInfo> findAllByTeamIdAndTenantId(Integer teamId, int tenantId);

  boolean existsByTeamIdAndTenantId(Integer teamId, int tenantId);

  void deleteByTenantId(int tenantId);
}
