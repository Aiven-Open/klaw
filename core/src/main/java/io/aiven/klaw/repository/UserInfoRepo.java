package io.aiven.klaw.repository;

import io.aiven.klaw.dao.UserInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface UserInfoRepo extends CrudRepository<UserInfo, String> {
  Optional<UserInfo> findById(String userid);

  Optional<UserInfo> findByUsername(String username);

  List<UserInfo> findAllByTenantId(int tenantId);

  List<UserInfo> findAllByTeamIdAndTenantId(Integer teamId, int tenantId);
}
