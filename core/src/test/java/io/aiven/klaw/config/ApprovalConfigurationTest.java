package io.aiven.klaw.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.service.ApprovalService;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.service.MailUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
// test-approval is a blank properties file testing when no custom config is given.
@TestPropertySource(locations = "/test-approval.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApprovalConfig.class)
class ApprovalConfigurationTest {

  public static final String PRD_ENV = "PRD";
  @MockBean MailUtils mailUtils;
  @MockBean CommonUtilsService commonUtilsService;
  @MockBean ManageDatabase manageDatabase;
  @Autowired ApprovalConfig config;

  @Test
  public void returnDefaulApprovalLists() throws Exception {

    ApprovalService defaultList = config.approvalService();

    List<Approval> aclApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.ACL, RequestOperationType.CLAIM, PRD_ENV, 10, 10, 101);

    List<Approval> topicApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.CREATE, PRD_ENV, 10, null, 101);

    List<Approval> schemaApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.SCHEMA, RequestOperationType.DELETE, PRD_ENV, 10, null, 101);

    List<Approval> connectorApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.CONNECTOR, RequestOperationType.UPDATE, PRD_ENV, 10, null, 101);

    assertThat(aclApprovals.size()).isEqualTo(2);
    assertThat(topicApprovals.size()).isEqualTo(1);
    assertThat(schemaApprovals.size()).isEqualTo(1);
    assertThat(connectorApprovals.size()).isEqualTo(1);

    assertThat(aclApprovals.get(0).getApprovalType()).isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
    assertThat(aclApprovals.get(1).getApprovalType()).isEqualTo(ApprovalType.ACL_TEAM_OWNER);
    assertThat(topicApprovals.get(0).getApprovalType()).isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
    assertThat(schemaApprovals.get(0).getApprovalType()).isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
    assertThat(connectorApprovals.get(0).getApprovalType())
        .isEqualTo(ApprovalType.CONNECTOR_TEAM_OWNER);
  }

  @Test
  public void returnDefaulApprovalLists_WhereEnvIsNull_return_ExpectedNumberOfApprovers()
      throws Exception {

    ApprovalService defaultList = config.approvalService();

    List<Approval> aclApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.ACL, RequestOperationType.CLAIM, null, 10, 10, 101);

    List<Approval> topicApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.CREATE, null, 10, null, 101);

    List<Approval> schemaApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.SCHEMA, RequestOperationType.DELETE, null, 10, null, 101);

    List<Approval> connectorApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.SCHEMA, RequestOperationType.UPDATE, null, 10, null, 101);

    assertThat(aclApprovals.size()).isEqualTo(2);
    assertThat(topicApprovals.size()).isEqualTo(1);
    assertThat(schemaApprovals.size()).isEqualTo(1);
    assertThat(connectorApprovals.size()).isEqualTo(1);

    assertThat(aclApprovals.get(0).getApprovalType()).isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
    assertThat(aclApprovals.get(1).getApprovalType()).isEqualTo(ApprovalType.ACL_TEAM_OWNER);
    assertThat(topicApprovals.get(0).getApprovalType()).isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
    assertThat(schemaApprovals.get(0).getApprovalType()).isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
    assertThat(connectorApprovals.get(0).getApprovalType())
        .isEqualTo(ApprovalType.TOPIC_TEAM_OWNER);
  }

  @Test
  public void returnDefaulApprovalLists_changeDataAndItShouldNotaffectTheOriginalService()
      throws Exception {

    ApprovalService defaultList = config.approvalService();

    List<Approval> aclApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.ACL, RequestOperationType.CLAIM, null, 10, 10, 101);
    aclApprovals.get(0).setApproverTeamId(1);
    aclApprovals.get(0).setApproverTeamName("Octopus");
    aclApprovals.get(0).setApproverTeamId(1);
    aclApprovals.get(0).setApproverName("George");
    assertThat(aclApprovals)
        .isNotEqualTo(
            defaultList.getApprovalsForRequest(
                RequestEntityType.ACL, RequestOperationType.CLAIM, null, 10, 10, 101));
  }
}
