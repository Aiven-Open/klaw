package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.model.enums.MailType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ApprovalService {

  private final Map<String, List<Approval>> topicApprovals;

  private final Map<String, List<Approval>> aclApprovals;

  private final Map<String, List<Approval>> schemaApprovals;

  private final Map<String, List<Approval>> connectApprovals;

  @Autowired private MailUtils mailService;
  @Autowired private CommonUtilsService commonUtilsService;
  @Autowired ManageDatabase manageDatabase;

  @Value("${klaw.approvals.multiApproval:false}")
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
    if (approvals == null || approvals.isEmpty()) {
      // Env specific approvers do not exist so return all approvers for that type of operationType.
      // Always return a copy
      approvals = requestToApprover.get(operationType.name());
      // Always return a deep copy
    }
    return copyApprovalForReturn(approvals);
  }

  private static List<Approval> copyApprovalForReturn(List<Approval> approvals) {
    return approvals.stream().map(Approval::new).toList();
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
      if (app.getApprovalType() == ApprovalType.TOPIC_TEAM_OWNER
          || app.getApprovalType() == ApprovalType.CONNECTOR_TEAM_OWNER) {
        app.setRequiredApprover(manageDatabase.getTeamNameFromTeamId(tenantId, resourceNameId));
      } else if (app.getApprovalType() == ApprovalType.ACL_TEAM_OWNER) {
        app.setRequiredApprover(manageDatabase.getTeamNameFromTeamId(tenantId, aclOwnerId));
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

  /**
   * Check if a user has the ability to add an approvval to this list of approvals
   *
   * @param approvals The full list of approvals
   * @param username The username of the user to check
   * @return true if there is an approval that the user fits the requirements for to approve
   */
  public boolean isUserAQualifiedOutstandingApprover(List<Approval> approvals, String username) {
    Optional<UserInfo> user =
        manageDatabase.selectAllCachedUserInfo().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst();
    if (user.isEmpty()) {
      return false;
    }
    // check that user has not foundapproved any request before potentially as a member of another
    // team
    // Then check to see if their current team has the ability to approve any of the requests.
    return approvals.stream()
            .noneMatch(
                app ->
                    !StringUtils.isBlank(app.getApproverName())
                        && app.getApproverName().equals(username))
        && !approvals.stream()
            .filter(
                app ->
                    app.getRequiredApprover()
                        .equals(
                            manageDatabase.getTeamNameFromTeamId(
                                user.get().getTenantId(), user.get().getTeamId())))
            .toList()
            .isEmpty();
  }

  public List<Approval> addApproval(
      List<Approval> approvals, String userName, Integer resourceOwnerId, Integer aclOwnerId)
      throws KlawBadRequestException {

    // If this user has already approved once they can not approve a second time.
    if (approvals.stream()
        .anyMatch(
            app ->
                !StringUtils.isBlank(app.getApproverName())
                    && app.getApproverName().equals(userName))) {
      throw new KlawBadRequestException(
          String.format(
              "User %s has already provided one approval, another approver is required to complete the approval process.",
              userName));
    }

    List<Approval> remaining = getRemainingApprovals(approvals);

    Optional<UserInfo> user =
        manageDatabase.selectAllCachedUserInfo().stream()
            .filter(u -> u.getUsername().equals(userName))
            .findFirst();
    Set<ApprovalType> completedApprovalTypes = new HashSet<>();

    if (user.isPresent()) {
      for (Approval approval : remaining) {
        if ((completedApprovalTypes.isEmpty() || allowMultiApproval)
            && completedApprovalTypes.add(approval.getApprovalType())) {
          switch (approval.getApprovalType()) {
            case TOPIC_TEAM_OWNER, CONNECTOR_TEAM_OWNER -> isResourceApprovalSatisfied(
                user.get(), resourceOwnerId, approval, approvals);

            case ACL_TEAM_OWNER -> isAclApprovalSatisfied(
                user.get(), aclOwnerId, approval, approvals);

            case TEAM -> isTeamApprovalSatisfied(user.get(), approval, approvals);
          }
        } else {
          log.debug(
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
    log.info("isAclApprovalSatisfied {} == {}", user.getTeamId(), aclOwnerId);
    if (user.getTeamId() == aclOwnerId) {
      addApproverToApprovalList(user, approval, approvals, user.getTenantId());
    }
  }

  private void isResourceApprovalSatisfied(
      UserInfo user, int resourceOwnerId, Approval approval, List<Approval> approvals) {
    log.info("isResourceApprovalSatisfied {} == {}", user.getTeamId(), resourceOwnerId);
    if (user.getTeamId() == resourceOwnerId) {
      addApproverToApprovalList(user, approval, approvals, user.getTenantId());
    }
  }

  private void isTeamApprovalSatisfied(UserInfo user, Approval approval, List<Approval> approvals) {

    if (Objects.equals(
        manageDatabase.getTeamNameFromTeamId(user.getTenantId(), user.getTeamId()),
        approval.getRequiredApprover())) {
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
              approve -> {
                // Add the Approval Details
                approve.setApproverName(user.getUsername());
                approve.setApproverTeamId(user.getTeamId());
                approve.setApproverTeamName(
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
          manageDatabase.getTeamIdFromTeamName(tenantId, approver.getRequiredApprover()));
    }

    return approverIds;
  }

  public Set<String> getApproverTeamEmails(
      HandleDbRequests dbHandle, Set<Integer> teamIds, int tenantId) {
    Set<String> emailAddresses = new HashSet<>();
    for (Integer teamId : teamIds) {
      CollectionUtils.addIgnoreNull(emailAddresses, getTeamMail(dbHandle, tenantId, teamId));
    }
    return emailAddresses;
  }

  private static String getTeamMail(HandleDbRequests dbHandle, int tenantId, Integer teamId) {
    Team team = dbHandle.getTeamDetails(teamId, tenantId);
    return team != null ? team.getTeammail() : null;
  }
}
