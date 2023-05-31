package io.aiven.klaw.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.aiven.klaw.model.Approval;
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
// test-custom-config has a number of changes to customise specific approvals
@TestPropertySource(locations = "/test-custom-approval.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ApprovalConfig.class)
class ApprovalCustomConfigurationTest {

  @MockBean MailUtils mailUtils;
  @MockBean CommonUtilsService commonUtilsService;
  @MockBean ManageDatabase manageDatabase;

  @Autowired ApprovalConfig config;

  @Test
  public void returnCustomEnvironmentSpecificProperties() throws Exception {

    ApprovalService defaultList = config.createTopicService();

    List<Approval> aclClaimApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.CLAIM, "PRD", 10, null, 101);

    List<Approval> aclDeleteApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.CLAIM, "UAT", 10, null, 101);

    List<Approval> topicClaimApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.CLAIM, "PRD", 10, null, 101);

    List<Approval> topicApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.CREATE, "PRD", 10, null, 101);

    List<Approval> topicDeleteApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.TOPIC, RequestOperationType.DELETE, null, 10, null, 101);

    List<Approval> schemaApprovals =
        defaultList.getApprovalsForRequest(
            RequestEntityType.SCHEMA, RequestOperationType.DELETE, "PRD", 10, null, 101);
    List<Approval> schemaApprovalsDev =
        defaultList.getApprovalsForRequest(
            RequestEntityType.SCHEMA, RequestOperationType.DELETE, "DEV", 10, null, 101);

    assertThat(topicClaimApprovals.size()).isEqualTo(2);
    for (Approval a : topicClaimApprovals) {
      // All entries should be Resource Team Owner
      assertThat(a.getApprovalType()).isEqualTo(ApprovalType.RESOURCE_TEAM_OWNER);
    }

    assertThat(topicApprovals.size()).isEqualTo(2);
    for (Approval a : topicApprovals) {
      // All entries should be Resource Team Owner
      assertThat(a.getApprovalType()).isEqualTo(ApprovalType.RESOURCE_TEAM_OWNER);
    }

    assertThat(schemaApprovals.size()).isEqualTo(1);
    assertThat(schemaApprovals.get(0).getApprovalType())
        .isEqualTo(ApprovalType.RESOURCE_TEAM_OWNER);

    assertThat(schemaApprovalsDev.size()).isEqualTo(2);

    for (Approval a : schemaApprovalsDev) {
      // All entries should be Resource Team Owner
      assertThat(a.getApprovalType()).isIn(ApprovalType.RESOURCE_TEAM_OWNER, ApprovalType.TEAM);
    }
    // previous check makes sure that both approval types are resource team owner or team this one
    // makes sure that they aren't the same.
    assertThat(schemaApprovalsDev.get(0).getApprovalType())
        .isNotEqualTo(schemaApprovalsDev.get(1).getApprovalType());

    for (Approval a : topicDeleteApprovals) {
      // All entries should be Resource Team Owner
      assertThat(a.getApprovalType()).isIn(ApprovalType.RESOURCE_TEAM_OWNER, ApprovalType.TEAM);
    }
    // previous check makes sure that both approval types are resource team owner or team this one
    // makes sure that they aren't the same.
    assertThat(topicDeleteApprovals.get(0).getApprovalType())
        .isNotEqualTo(topicDeleteApprovals.get(1).getApprovalType());
  }
}
