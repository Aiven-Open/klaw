package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwRolesPermissions;
import io.aiven.klaw.model.KwRolesPermissionsModel;
import io.aiven.klaw.model.PermissionType;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RolesPermissionsControllerService {

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ManageDatabase manageDatabase;

  @Autowired private MailUtils mailService;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  public List<String> getRoles() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    try {
      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES))
        return Arrays.asList(
            manageDatabase.getKwPropertyValue("klaw.adduser.roles", tenantId).split(","));
      else return new ArrayList<>(manageDatabase.getRolesPermissionsPerTenant(tenantId).keySet());
    } catch (Exception e) {
      log.error("Exception:", e);
      return Arrays.asList(
          manageDatabase.getKwPropertyValue("klaw.adduser.roles", tenantId).split(","));
    }
  }

  public List<String> getRolesFromDb() {
    return new ArrayList<>(
        manageDatabase
            .getRolesPermissionsPerTenant(commonUtilsService.getTenantId(getUserName()))
            .keySet());
  }

  public Map<String, List<Map<String, Boolean>>> getPermissions(boolean isExternalCall) {

    if (isExternalCall
        && commonUtilsService.isNotAuthorizedUser(
            getPrincipal(), PermissionType.UPDATE_PERMISSIONS)) {
      return null;
    }

    PermissionType[] listPerms = PermissionType.values();
    List<String> permsList = new ArrayList<>();
    for (PermissionType permissionType : listPerms) {
      permsList.add(permissionType.name());
    }

    Collections.sort(permsList);

    Map<String, List<String>> existingPerms =
        manageDatabase.getRolesPermissionsPerTenant(commonUtilsService.getTenantId(getUserName()));
    Map<String, List<Map<String, Boolean>>> finalMap = new HashMap<>();
    List<Map<String, Boolean>> mapList;
    List<String> roles = new ArrayList<>(existingPerms.keySet());
    Collections.sort(roles);

    for (String role : roles) {
      mapList = new ArrayList<>();
      for (String perm : permsList) {
        Map<String, Boolean> perMap = new HashMap<>();
        if ("saas".equals(kwInstallationType) && "ADD_TENANT".equals(perm)) {
          // do nothing
        } else {
          perMap.put(perm, existingPerms.get(role).contains(perm));
          mapList.add(perMap);
        }
      }
      finalMap.put(role, mapList);
    }

    return finalMap;
  }

  public Map<String, String> getPermissionDescriptions() {
    Map<String, String> hMap = new HashMap<>();
    for (PermissionType value : PermissionType.values()) {
      hMap.put(value.name(), value.getDescription());
    }
    return hMap;
  }

  public Map<String, String> updatePermissions(KwRolesPermissionsModel[] permissionsSet) {
    Map<String, String> updatedPermsMapStatus = new HashMap<>();

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.UPDATE_PERMISSIONS)) {
      updatedPermsMapStatus.put("result", "Not Authorized");
      return updatedPermsMapStatus;
    }

    Map<String, String> uniqueMap = new HashMap<>();
    for (KwRolesPermissionsModel kwRolesPermissionsModel : permissionsSet) {
      uniqueMap.put(
          kwRolesPermissionsModel.getRolePermission(),
          kwRolesPermissionsModel.getPermissionEnabled());
    }

    List<KwRolesPermissions> kwRolesPermissionsAdd = new ArrayList<>();
    List<KwRolesPermissions> kwRolesPermissionsDelete = new ArrayList<>();
    KwRolesPermissions tmpKwRolePermModel;

    String delimiter = "-----";
    int indexOfDelimiter;
    String isPermEnabled;

    for (String permKey : uniqueMap.keySet()) {
      isPermEnabled = uniqueMap.get(permKey);
      indexOfDelimiter = permKey.indexOf(delimiter);

      tmpKwRolePermModel = new KwRolesPermissions();
      tmpKwRolePermModel.setRoleId(permKey.substring(0, indexOfDelimiter));
      tmpKwRolePermModel.setPermission(permKey.substring(indexOfDelimiter + 5));

      if ("true".equals(isPermEnabled)) kwRolesPermissionsAdd.add(tmpKwRolePermModel);
      else if ("false".equals(isPermEnabled)) kwRolesPermissionsDelete.add(tmpKwRolePermModel);
    }

    if (kwRolesPermissionsAdd.size() > 0)
      manageDatabase.getHandleDbRequests().updatePermissions(kwRolesPermissionsAdd, "ADD");
    if (kwRolesPermissionsDelete.size() > 0)
      manageDatabase.getHandleDbRequests().updatePermissions(kwRolesPermissionsDelete, "DELETE");

    if (kwRolesPermissionsAdd.size() > 0 || kwRolesPermissionsDelete.size() > 0)
      manageDatabase.loadRolesForAllTenants();

    updatedPermsMapStatus.put("result", "success");
    return updatedPermsMapStatus;
  }

  public Map<String, String> deleteRole(String roleId) {
    Map<String, String> deleteRoleStatus = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ROLES)) {
      deleteRoleStatus.put("result", "Not Authorized");
      return deleteRoleStatus;
    }

    if (KwConstants.USER_ROLE.equals(roleId) || KwConstants.SUPERADMIN_ROLE.equals(roleId)) {
      deleteRoleStatus.put("result", "Not Allowed");
      return deleteRoleStatus;
    }

    String status =
        manageDatabase
            .getHandleDbRequests()
            .deleteRole(roleId, commonUtilsService.getTenantId(getUserName()));
    deleteRoleStatus.put("result", status);
    manageDatabase.loadRolesForAllTenants();
    return deleteRoleStatus;
  }

  public Map<String, String> addRoleId(String roleId) {
    Map<String, String> addRoleStatus = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ROLES)) {
      addRoleStatus.put("result", "Not Authorized");
      return addRoleStatus;
    }

    List<KwRolesPermissions> kwRolesPermissionsAdd = new ArrayList<>();
    KwRolesPermissions tmpKwRolePermModel = new KwRolesPermissions();
    tmpKwRolePermModel.setRoleId(roleId.trim().toUpperCase());
    tmpKwRolePermModel.setPermission(PermissionType.VIEW_TOPICS.name());
    tmpKwRolePermModel.setTenantId(commonUtilsService.getTenantId(getUserName()));

    kwRolesPermissionsAdd.add(tmpKwRolePermModel);

    String status =
        manageDatabase.getHandleDbRequests().updatePermissions(kwRolesPermissionsAdd, "ADD");
    addRoleStatus.put("result", status);
    manageDatabase.loadRolesForAllTenants();
    return addRoleStatus;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  protected List<String> getApproverRoles(String requestType, int tenantId) {
    Map<String, List<Map<String, Boolean>>> existingPermissions = getPermissions(false);
    List<String> approverRoles = new ArrayList<>();
    for (Map.Entry<String, List<Map<String, Boolean>>> permissionsListEntry :
        existingPermissions.entrySet()) {
      List<Map<String, Boolean>> entryDets = permissionsListEntry.getValue();
      for (Map<String, Boolean> entryDet : entryDets) {
        for (Map.Entry<String, Boolean> stringBooleanEntry : entryDet.entrySet()) {
          if (Objects.equals(stringBooleanEntry.getValue(), true)) {
            if ("SUBSCRIPTIONS".equals(requestType)
                && Objects.equals(
                    stringBooleanEntry.getKey(), PermissionType.APPROVE_SUBSCRIPTIONS.name())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            } else if ("TOPICS".equals(requestType)
                && Objects.equals(
                    stringBooleanEntry.getKey(), PermissionType.APPROVE_TOPICS.name())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            } else if ("SCHEMAS".equals(requestType)
                && Objects.equals(
                    stringBooleanEntry.getKey(), PermissionType.APPROVE_SCHEMAS.name())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            } else if ("CONNECTORS".equals(requestType)
                && Objects.equals(
                    stringBooleanEntry.getKey(), PermissionType.APPROVE_SCHEMAS.name())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            }
          }
        }
      }
    }

    return approverRoles.stream().distinct().collect(Collectors.toList());
  }
}
