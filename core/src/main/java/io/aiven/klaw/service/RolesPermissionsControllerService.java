package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ROLE_PRM_ERR_101;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwRolesPermissions;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.requests.KwRolesPermissionsModel;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RolesPermissionsControllerService {

  private final CommonUtilsService commonUtilsService;

  final ManageDatabase manageDatabase;

  private final MailUtils mailService;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  public RolesPermissionsControllerService(
      CommonUtilsService commonUtilsService, ManageDatabase manageDatabase, MailUtils mailService) {
    this.commonUtilsService = commonUtilsService;
    this.manageDatabase = manageDatabase;
    this.mailService = mailService;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  public List<String> getRoles() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    try {
      if (commonUtilsService.isNotAuthorizedUser(
          getPrincipal(), PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES))
        return Arrays.asList(
            manageDatabase.getKwPropertyValue("klaw.adduser.roles", tenantId).split(","));
      else {
        return new ArrayList<>(manageDatabase.getRolesPermissionsPerTenant(tenantId).keySet());
      }
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

  public ApiResponse updatePermissions(KwRolesPermissionsModel[] permissionsSet)
      throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.UPDATE_PERMISSIONS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
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
    int tenantId = commonUtilsService.getTenantId(getUserName());

    try {
      for (String permKey : uniqueMap.keySet()) {
        isPermEnabled = uniqueMap.get(permKey);
        indexOfDelimiter = permKey.indexOf(delimiter);

        tmpKwRolePermModel = new KwRolesPermissions();
        tmpKwRolePermModel.setRoleId(permKey.substring(0, indexOfDelimiter));
        tmpKwRolePermModel.setPermission(permKey.substring(indexOfDelimiter + 5));
        tmpKwRolePermModel.setTenantId(tenantId);

        if ("true".equals(isPermEnabled)) {
          kwRolesPermissionsAdd.add(tmpKwRolePermModel);
        } else if ("false".equals(isPermEnabled)) {
          kwRolesPermissionsDelete.add(tmpKwRolePermModel);
        }
      }

      if (kwRolesPermissionsAdd.size() > 0) {
        manageDatabase.getHandleDbRequests().updatePermissions(kwRolesPermissionsAdd, "ADD");
      }
      if (kwRolesPermissionsDelete.size() > 0) {
        manageDatabase.getHandleDbRequests().updatePermissions(kwRolesPermissionsDelete, "DELETE");
      }

      if (kwRolesPermissionsAdd.size() > 0 || kwRolesPermissionsDelete.size() > 0) {
        manageDatabase.loadRolesForAllTenants();
      }
      return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse deleteRole(String roleId) throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ROLES)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    if (KwConstants.USER_ROLE.equals(roleId) || KwConstants.SUPERADMIN_ROLE.equals(roleId)) {
      return ApiResponse.builder().success(false).message(ROLE_PRM_ERR_101).build();
    }

    try {
      String status =
          manageDatabase
              .getHandleDbRequests()
              .deleteRole(roleId, commonUtilsService.getTenantId(getUserName()));
      manageDatabase.loadRolesForAllTenants();
      return ApiResponse.builder()
          .success((status.equals(ApiResultStatus.SUCCESS.value)))
          .message(status)
          .build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
  }

  public ApiResponse addRoleId(String roleId) throws KlawException {
    if (commonUtilsService.isNotAuthorizedUser(
        getPrincipal(), PermissionType.ADD_EDIT_DELETE_ROLES)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }

    List<KwRolesPermissions> kwRolesPermissionsAdd = new ArrayList<>();
    KwRolesPermissions tmpKwRolePermModel = new KwRolesPermissions();
    tmpKwRolePermModel.setRoleId(roleId.trim().toUpperCase());
    tmpKwRolePermModel.setPermission(PermissionType.VIEW_TOPICS.name());
    tmpKwRolePermModel.setTenantId(commonUtilsService.getTenantId(getUserName()));

    kwRolesPermissionsAdd.add(tmpKwRolePermModel);

    try {
      String status =
          manageDatabase.getHandleDbRequests().updatePermissions(kwRolesPermissionsAdd, "ADD");
      manageDatabase.loadRolesForAllTenants();
      return ApiResponse.builder()
          .success((status.equals(ApiResultStatus.SUCCESS.value)))
          .message(status)
          .build();
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new KlawException(e.getMessage());
    }
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
          if (Boolean.TRUE.equals(stringBooleanEntry.getValue())) {
            if ("SUBSCRIPTIONS".equals(requestType)
                && PermissionType.APPROVE_SUBSCRIPTIONS
                    .name()
                    .equals(stringBooleanEntry.getKey())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            } else if ("TOPICS".equals(requestType)
                && PermissionType.APPROVE_TOPICS.name().equals(stringBooleanEntry.getKey())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            } else if ("SCHEMAS".equals(requestType)
                && PermissionType.APPROVE_SCHEMAS.name().equals(stringBooleanEntry.getKey())) {
              approverRoles.add(permissionsListEntry.getKey());
              break;
            } else if ("CONNECTORS".equals(requestType)
                && PermissionType.APPROVE_SCHEMAS.name().equals(stringBooleanEntry.getKey())) {
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
