package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserInfoRepo extends CrudRepository<UserInfo, String> {
    Optional<UserInfo> findById(String userid);
    Optional<UserInfo> findByUsername(String username);
}
