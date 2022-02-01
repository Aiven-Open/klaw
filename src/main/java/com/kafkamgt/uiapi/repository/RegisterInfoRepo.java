package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.RegisterUserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RegisterInfoRepo extends CrudRepository<RegisterUserInfo, String> {
    Optional<RegisterUserInfo> findById(String userid);
    List<RegisterUserInfo> findAllByStatusAndTenantId(String status, int tenantId);
    List<RegisterUserInfo> findAllByStatus(String status);
    List<RegisterUserInfo> findAllByTenantId(int tenantId);
    List<RegisterUserInfo> findAllByRegistrationIdAndStatus(String registrationId, String status);
    List<RegisterUserInfo> findAllByUsernameAndStatus(String userId, String status);

    List<RegisterUserInfo> findAllByRegistrationId(String registrationId);
}
