package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.KwRolesPermissions;
import com.kafkamgt.uiapi.dao.KwRolesPermissionsID;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KwRolesPermsRepo extends CrudRepository<KwRolesPermissions, KwRolesPermissionsID> {
    Optional<KwRolesPermissions> findById(KwRolesPermissionsID rolesPermissionsID);
    List<KwRolesPermissions> findAllByTenantId(int tenantId);

    List<KwRolesPermissions> findAllByRoleIdAndPermissionAndTenantId(String roleId, String permission, int tenantId);

    List<KwRolesPermissions> findAllByRoleIdAndTenantId(String roleId, int tenantId);

    @Query(value ="select max(id) from kwrolespermissions where tenantid = :tenantId", nativeQuery = true)
    Integer getMaxRolePermissionId(@Param("tenantId") Integer tenantId);
}
