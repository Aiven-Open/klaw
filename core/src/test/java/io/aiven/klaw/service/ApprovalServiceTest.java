package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.Approval;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.model.enums.MailType;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class ApprovalServiceTest {

  public static final String JAMES = "James";
  public static final String JACKIE = "Jackie";
  public static final String JOHN = "John";
  public static final String OCTOPUS = "Octopus";
  public static final String ALICE = "ALICE";
  public static final String SUPPORT_TEAM = "SUPPORT_TEAM";
  @Mock ManageDatabase manageDatabase;

  @Mock private MailUtils mailService;
  @Mock private CommonUtilsService commonUtilsService;

  @Mock private HandleDbRequestsJdbc handleDbRequestsJdbc;

  @InjectMocks private ApprovalService approvalService;

  @BeforeEach
  public void setUp() throws Exception {
    ReflectionTestUtils.setField(approvalService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(approvalService, "mailService", mailService);
    ReflectionTestUtils.setField(approvalService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(approvalService, "allowMultiApproval", true);

    when(manageDatabase.selectAllCachedUserInfo())
        .thenReturn(List.of(createUser(JOHN, 10), createUser(JAMES, 11), createUser(JACKIE, 13)));
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(11))).thenReturn(OCTOPUS);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(10))).thenReturn(ALICE);
    when(manageDatabase.getTeamNameFromTeamId(eq(101), eq(13))).thenReturn(SUPPORT_TEAM);

    // reverse get teamId From TEamName
    when(manageDatabase.getTeamIdFromTeamName(eq(101), eq(OCTOPUS))).thenReturn(11);
    when(manageDatabase.getTeamIdFromTeamName(eq(101), eq(ALICE))).thenReturn(10);
    when(manageDatabase.getTeamIdFromTeamName(eq(101), eq(SUPPORT_TEAM))).thenReturn(13);

    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequestsJdbc);

    // setup team emails
    when(handleDbRequestsJdbc.getTeamDetails(eq(10), eq(101))).thenReturn(createTeam(ALICE, 10));
    when(handleDbRequestsJdbc.getTeamDetails(eq(11), eq(101))).thenReturn(createTeam(OCTOPUS, 11));
    when(handleDbRequestsJdbc.getTeamDetails(eq(13), eq(101)))
        .thenReturn(createTeam(SUPPORT_TEAM, 13));
  }

  @Test
  public void addAResourceOwnerApproval() throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(
                OCTOPUS, "Create-11-1", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(
                OCTOPUS, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JAMES, 11, null);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(0).getApproverName()).isEqualTo(JAMES);
    assertThat(approvalAdded.get(0).getApproverTeamId()).isEqualTo(11);
    assertThat(approvalAdded.get(0).getApproverTeamName()).isEqualTo(OCTOPUS);
    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addAResourceOwnerApproval_rejectDifferentTeams() throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(
                OCTOPUS, "Create-11-1", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(
                OCTOPUS, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JOHN, 11, null);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(0).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(0).getApproverTeamName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addAResourceOwnerApproval_rejectUserAlreadyApproved() throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(
                OCTOPUS, "Create-11-1", ApprovalType.RESOURCE_TEAM_OWNER, JAMES, OCTOPUS, 11),
            createApproval(
                OCTOPUS, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JAMES, 11, null);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(0).getApproverName()).isEqualTo(JAMES);
    assertThat(approvalAdded.get(0).getApproverTeamId()).isEqualTo(11);
    assertThat(approvalAdded.get(0).getApproverTeamName()).isEqualTo(OCTOPUS);
    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addAnACLOwnerApproval_WhereDifferentTeamOwnsResource() throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(
                ALICE, "Create-11-1", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(OCTOPUS, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JOHN, 11, 10);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(1).getApproverName()).isEqualTo(JOHN);
    assertThat(approvalAdded.get(1).getApproverTeamId()).isEqualTo(10);
    assertThat(approvalAdded.get(1).getApproverTeamName()).isEqualTo(ALICE);
    assertThat(approvalAdded.get(0).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(0).getApproverTeamName()).isNullOrEmpty();

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addAnACLOwnerApproval_SameTeamOwnsSubscriptionAndResourceTeams()
      throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(
                ALICE, "Create-11-1", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(OCTOPUS, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JOHN, 10, 10);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(1).getApproverName()).isEqualTo(JOHN);
    assertThat(approvalAdded.get(1).getApproverTeamId()).isEqualTo(10);
    assertThat(approvalAdded.get(1).getApproverTeamName()).isEqualTo(ALICE);
    assertThat(approvalAdded.get(1).getApprovalType()).isEqualTo(ApprovalType.ACL_TEAM_OWNER);
    assertThat(approvalAdded.get(0).getApproverName()).isEqualTo(JOHN);
    assertThat(approvalAdded.get(0).getApproverTeamId()).isEqualTo(10);
    assertThat(approvalAdded.get(0).getApproverTeamName()).isEqualTo(ALICE);

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isTrue();
  }

  @Test
  public void addAnACLOwnerApproval_SameTeamOwnsSubscriptionAndResourceTeams_NoApproval()
      throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(
                ALICE, "Create-11-1", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(OCTOPUS, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JAMES, 10, 10);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();
    assertThat(approvalAdded.get(0).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(0).getApproverTeamName()).isNullOrEmpty();

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addATeamOwnerApprovalToAcl_AddTeamApproval() throws KlawException {

    List<Approval> approvals =
        List.of(
            createApproval(SUPPORT_TEAM, "Create-11-1", ApprovalType.TEAM, null, null, null),
            createApproval(OCTOPUS, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JACKIE, 10, 11);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(0).getApproverName()).isEqualTo(JACKIE);
    assertThat(approvalAdded.get(0).getApproverTeamId()).isEqualTo(13);
    assertThat(approvalAdded.get(0).getApproverTeamName()).isEqualTo(SUPPORT_TEAM);
    assertThat(approvalAdded.get(0).getApprovalType()).isEqualTo(ApprovalType.TEAM);
    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();
    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addATeamOwnerApproval_AddTeamApproval() throws KlawException {

    List<Approval> approvals =
        List.of(
            createApproval(SUPPORT_TEAM, "Create-11-1", ApprovalType.TEAM, null, null, null),
            createApproval(
                OCTOPUS, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JACKIE, 10, null);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(0).getApproverName()).isEqualTo(JACKIE);
    assertThat(approvalAdded.get(0).getApproverTeamId()).isEqualTo(13);
    assertThat(approvalAdded.get(0).getApproverTeamName()).isEqualTo(SUPPORT_TEAM);
    assertThat(approvalAdded.get(0).getApprovalType()).isEqualTo(ApprovalType.TEAM);
    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();
    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void addATeamOwnerApproval_rejectDifferentTeams_NoApprovalsAdded() throws KlawException {
    List<Approval> approvals =
        List.of(
            createApproval(ALICE, "Create-11-1", ApprovalType.TEAM, null, null, null),
            createApproval(OCTOPUS, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null));
    List<Approval> approvalAdded = approvalService.addApproval(approvals, JACKIE, 10, 10);

    assertThat(approvalAdded.size()).isEqualTo(2);

    assertThat(approvalAdded.get(1).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(1).getApproverTeamName()).isNullOrEmpty();
    assertThat(approvalAdded.get(0).getApproverName()).isNullOrEmpty();
    assertThat(approvalAdded.get(0).getApproverTeamName()).isNullOrEmpty();

    // IsNotFullyApproved They should only be allowed to approve one approval slot of the same type
    // in this case RESOURCE_TEAM_OWNER
    assertThat(approvalService.isRequestFullyApproved(approvalAdded)).isFalse();
  }

  @Test
  public void getEmailAddresses() {
    List<Approval> approvals =
        List.of(
            createApproval(ALICE, "Create-11-1", ApprovalType.TEAM, null, null, null),
            createApproval(
                OCTOPUS, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));

    Set<String> emails = approvalService.getApproverEmails(approvals, 101);
    // two seperate email addresses expected
    assertThat(emails.size()).isEqualTo(2);
  }

  @Test
  public void getEmailAddresses_DuplicateTeamsOnlyOneEmailAddressReturned() {

    List<Approval> approvals =
        List.of(
            createApproval(ALICE, "Create-11-1", ApprovalType.TEAM, null, null, null),
            createApproval(
                ALICE, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(ALICE, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null),
            createApproval(
                ALICE, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));

    Set<String> emails = approvalService.getApproverEmails(approvals, 101);
    // All The same Team so we only expect to get one email so we can send once.
    assertThat(emails.size()).isEqualTo(1);
  }

  @Test
  public void sendEmailToApprovers() {
    List<Approval> approvals =
        List.of(
            createApproval(ALICE, "Create-11-1", ApprovalType.TEAM, null, null, null),
            createApproval(
                ALICE, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null),
            createApproval(ALICE, "Create-11-2", ApprovalType.ACL_TEAM_OWNER, null, null, null),
            createApproval(
                ALICE, "Create-11-2", ApprovalType.RESOURCE_TEAM_OWNER, null, null, null));

    approvalService.sendEmailToApprovers(
        JOHN, "SimpleTopic", null, null, MailType.TOPIC_CREATE_REQUESTED, approvals, 101);
  }

  private UserInfo createUser(String userName, int teamId) {
    UserInfo user = new UserInfo();
    user.setUsername(userName);
    user.setRole("USER");
    user.setTeamId(teamId);
    user.setMailid(userName + "@klaw.mailid");
    user.setTenantId(101);

    return user;
  }

  private Approval createApproval(
      String requiredTeamName,
      String approvalId,
      ApprovalType type,
      String userName,
      String approverTeamName,
      Integer approvingTeamId) {
    Approval approval = new Approval();
    approval.setApproverName(userName);
    approval.setApprovalId(approvalId);
    approval.setApprovalType(type);
    approval.setApproverTeamName(approverTeamName);
    approval.setRequiredApprovingTeamName(requiredTeamName);
    if (approvingTeamId != null) {
      approval.setApproverTeamId(approvingTeamId);
    }

    return approval;
  }

  private static Team createTeam(String teamName, int teamId) {
    Team t = new Team();
    t.setTeamId(teamId);
    t.setTeammail(teamName + ".klaw@mailid");
    t.setTeamname(teamName);
    return t;
  }
}
