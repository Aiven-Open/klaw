package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.Approval;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApprovalService {

  private Map<String, List<Approval>> topicApprovals;

  private Map<String, List<Approval>> aclApprovals;

  private Map<String, List<Approval>> schemaApprovals;

  private Map<String, List<Approval>> connectApprovals;

  @Autowired private MailUtils mailService;
  @Autowired private CommonUtilsService commonUtilsService;
  @Autowired ManageDatabase manageDatabase;

  @Value("${klaw.approvals.multiApproval:true}")
  private boolean allowMultiApproval;

  public ApprovalService(
      Map<String, List<Approval>> topicApprovals,
      Map<String, List<Approval>> aclApprovals,
      Map<String, List<Approval>> schemaApprovals,
      Map<String, List<Approval>> connectApprovals) {
    this.topicApprovals = topicApprovals;
    this.aclApprovals = aclApprovals;
    this.schemaApprovals = schemaApprovals;
    this.connectApprovals = connectApprovals;
  }

  private List<Approval> getApprovers(
      RequestEntityType entityType, RequestOperationType operationType, String envName)
      throws KlawException {

    Map<String, List<Approval>> requestToApprover = getEntityTypeApproverMap(entityType);
    List<Approval> approvals = null;

    if (envName != null) {
      approvals =
          requestToApprover.get(String.join(".", operationType.name(), envName.toUpperCase()));
    }
    if (approvals != null && !approvals.isEmpty()) {
      // Always return a deep copy

      return approvals.stream().map(Approval::new).toList();
    } else {
      // Env specific approvers do not exist so return all approvers for that type of operationType.
      // Always return a copy
      String key = operationType.name();
      approvals = requestToApprover.get(operationType.name());
      // Always return a deep copy
      return approvals.stream().map(Approval::new).toList();
    }
  }

  public List<Approval> getApprovalsForRequest(
      RequestEntityType entityType,
      RequestOperationType operationType,
      String envName,
      Integer resourceNameId,
      Integer aclOwnerId,
      int tenantId)
      throws KlawException {
    List<Approval> approvals = getApprovers(entityType, operationType, envName);
    for (Approval app : approvals) {
      if (app.getApprovalType().equals(ApprovalType.RESOURCE_TEAM_OWNER)) {
        app.setRequiredApprovingTeamName(
            manageDatabase.getTeamNameFromTeamId(tenantId, resourceNameId));
      } else if (app.getApprovalType().equals(ApprovalType.ACL_TEAM_OWNER)) {
        app.setRequiredApprovingTeamName(
            manageDatabase.getTeamNameFromTeamId(tenantId, aclOwnerId));
      }
    }
    return approvals;
  }

  private Map<String, List<Approval>> getEntityTypeApproverMap(RequestEntityType entityType)
      throws KlawException {
    switch (entityType) {
      case TOPIC:
        return topicApprovals;
      case ACL:
        return aclApprovals;
      case SCHEMA:
        return schemaApprovals;
      case CONNECTOR:
        return connectApprovals;
    }
    throw new KlawException("Unexpected Entity Type requested for Approval");
  }

  public void sendEmailToApprovers(
      String userName,
      String resourceName,
      String acl,
      String reasonToDecline,
      MailType mailType,
      List<Approval> approvals,
      int tenantId) {

    Set<Integer> approverIds = getApproverIds(approvals, tenantId);

    for (Integer approverId : approverIds) {

      mailService.sendMail(
          resourceName,
          acl,
          reasonToDecline,
          userName,
          userName,
          approverId,
          manageDatabase.getHandleDbRequests(),
          mailType,
          commonUtilsService.getLoginUrl());
    }
  }

  public boolean isRequestFullyApproved(List<Approval> approvals) throws KlawException {
    return getRemainingApprovals(approvals).isEmpty();
  }

  private static List<Approval> getRemainingApprovals(List<Approval> approvals) {
    return approvals.stream()
        .filter(
            app -> StringUtils.isBlank(app.getApproverName()) || app.getApproverName().isEmpty())
        .toList();
  }

  public List<Approval> addApproval(
      List<Approval> approvals, String userName, Integer resourceOwnerId, Integer aclOwnerId) {

    // If this user has already approved once they can not approve a second time.
    if (approvals.stream()
        .anyMatch(
            app ->
                !StringUtils.isBlank(app.getApproverName())
                    && app.getApproverName().equals(userName))) {
      return approvals;
    }

    List<Approval> remaining = getRemainingApprovals(approvals);

    Optional<UserInfo> user =
        manageDatabase.selectAllCachedUserInfo().stream()
            .filter(u -> u.getUsername().equals(userName))
            .findFirst();
    Set<ApprovalType> completedApprovalTypes = new HashSet<>();

    if (user.isPresent()) {
      for (Approval approval : remaining) {
        if (completedApprovalTypes.add(approval.getApprovalType()) && allowMultiApproval == true) {
          switch (approval.getApprovalType()) {
            case RESOURCE_TEAM_OWNER -> isResourceApprovalSatisfied(
                user.get(), resourceOwnerId, approval, approvals);

            case ACL_TEAM_OWNER -> isAclApprovalSatisfied(
                user.get(), aclOwnerId, approval, approvals);

            case TEAM -> isTeamApprovalSatisfied(user.get(), approval, approvals);
          }
        } else {
          log.info(
              "User {} has already provided one approval of type {} another approver is required to complete the approval process.",
              user.get().getUsername(),
              approval.getApprovalType());
        }
      }
    }
    return approvals;
  }

  private void isAclApprovalSatisfied(
      UserInfo user, int aclOwnerId, Approval approval, List<Approval> approvals) {
    if (user.getTeamId() == aclOwnerId) {
      addApproverToApprovalList(user, approval, approvals, user.getTenantId());
    }
  }

  private void isResourceApprovalSatisfied(
      UserInfo user, int resourceOwnerId, Approval approval, List<Approval> approvals) {
    if (user.getTeamId() == resourceOwnerId) {
      addApproverToApprovalList(user, approval, approvals, user.getTenantId());
    }
  }

  private void isTeamApprovalSatisfied(UserInfo user, Approval approval, List<Approval> approvals) {
    if (manageDatabase.getTeamNameFromTeamId(user.getTenantId(), user.getTeamId())
        == approval.getRequiredApprovingTeamName()) {
      addApproverToApprovalList(user, approval, approvals, user.getTenantId());
    }
  }

  private void addApproverToApprovalList(
      UserInfo user, Approval approval, List<Approval> approvals, int tenantId) {
    try {

      approvals.stream()
          .filter(app -> app.getApprovalId().equals(approval.getApprovalId()))
          .findFirst()
          .ifPresent(
              a -> {
                log.warn("THE DANGER ZONEEEEEEE");
                // Add the Approval Details
                a.setApproverName(user.getUsername());
                a.setApproverTeamId(user.getTeamId());
                a.setApproverTeamName(
                    manageDatabase.getTeamNameFromTeamId(tenantId, user.getTeamId()));
              });

    } catch (Exception ex) {
      log.error("Exception caught while adding approver to the approval list.");
    }
  }

  public Set<String> getApproverEmails(List<Approval> approvals, int tenantId) {

    return getApproverTeamEmails(
        manageDatabase.getHandleDbRequests(), getApproverIds(approvals, tenantId), tenantId);
  }

  public Set<Integer> getApproverIds(List<Approval> approvals, int tenantId) {
    Set<Integer> approverIds = new HashSet<>();

    for (Approval approver : approvals) {
      CollectionUtils.addIgnoreNull(
          approverIds,
          manageDatabase.getTeamIdFromTeamName(tenantId, approver.getRequiredApprovingTeamName()));
    }

    return approverIds;
  }

  public Set<String> getApproverTeamEmails(
      HandleDbRequests dbHandle, Set<Integer> teamIds, int tenantId) {
    Set<String> emailAddresses = new HashSet<>();
    for (Integer teamId : teamIds) {
      CollectionUtils.addIgnoreNull(emailAddresses, getTeammail(dbHandle, tenantId, teamId));
    }
    return emailAddresses;
  }

  private static String getTeammail(HandleDbRequests dbHandle, int tenantId, Integer teamId) {
    Team team = dbHandle.getTeamDetails(teamId, tenantId);
    return team != null ? team.getTeammail() : null;
  }
}
