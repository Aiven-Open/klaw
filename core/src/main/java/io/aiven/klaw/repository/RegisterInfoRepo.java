package io.aiven.klaw.repository;

import io.aiven.klaw.dao.RegisterUserInfo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RegisterInfoRepo extends CrudRepository<RegisterUserInfo, String> {
  Optional<RegisterUserInfo> findById(String userid);

  List<RegisterUserInfo> findAllByStatusAndTenantId(String status, int tenantId);

  List<RegisterUserInfo> findAllByStatus(String status);

  List<RegisterUserInfo> findAllByTenantId(int tenantId);

  List<RegisterUserInfo> findAllByRegistrationIdAndStatus(String registrationId, String status);

  RegisterUserInfo findFirstByUsernameAndStatus(String userId, String status);

  boolean existsRegisterUserInfoByUsernameAndStatus(String userId, String status);

  List<RegisterUserInfo> findAllByRegistrationId(String registrationId);

  void deleteByTenantId(int tenantId);
}
