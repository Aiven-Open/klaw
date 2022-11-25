package io.aiven.klaw.validation;

import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.TopicRequestTypes;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.TopicControllerService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class TopicRequestValidatorImpl
    implements ConstraintValidator<TopicRequestValidator, TopicRequestModel> {

  @Autowired private CommonUtilsService commonUtilsService;
  @Autowired private MailUtils mailService;
  @Autowired private TopicControllerService topicControllerService;

  private PermissionType permissionType;

  @Override
  public void initialize(TopicRequestValidator constraintAnnotation) {
    this.permissionType = constraintAnnotation.getPermissionType();
  }

  @Override
  public boolean isValid(
      TopicRequestModel topicRequestModel, ConstraintValidatorContext constraintValidatorContext) {

    // Verify if user has access to request for topics
    if (commonUtilsService.isNotAuthorizedUser(
        topicControllerService.getPrincipal(), this.permissionType)) {
      updateConstraint(constraintValidatorContext, ApiResultStatus.NOT_AUTHORIZED.value);
      return false;
    }

    // tenant filtering
    if (!commonUtilsService
        .getEnvsFromUserId(topicControllerService.getUserName())
        .contains(topicRequestModel.getEnvironment())) {
      updateConstraint(
          constraintValidatorContext,
          "Failure. Not authorized to request topic for this environment.");
      return false;
    }

    // check for null topic name
    if (topicRequestModel.getTopicname() == null) {
      updateConstraint(constraintValidatorContext, "Failure. Please fill in topic name.");
      return false;
    }

    // check for empty/whitespaces on topic name
    if (topicRequestModel.getTopicname().isBlank()) {
      updateConstraint(constraintValidatorContext, "Failure. Please fill in a valid topic name.");
      return false;
    }

    // verify tenant config exists
    int tenantId = commonUtilsService.getTenantId(topicControllerService.getUserName());
    String syncCluster;
    try {
      syncCluster = topicControllerService.getSyncCluster(tenantId);
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      updateConstraint(
          constraintValidatorContext,
          "Failure. Tenant configuration in Server config is missing. Please configure.");
      return false;
    }

    // Check if topic is owned by a different team, applicable on update requests
    List<Topic> topics =
        topicControllerService.getTopicFromName(topicRequestModel.getTopicname(), tenantId);
    if (topics != null
        && !topics.isEmpty()
        && !Objects.equals(
            topics
                .get(0) // as there could be only one owner team for topic, with topic name being
                // unique for tenant, getting the first element.
                .getTeamId(),
            commonUtilsService.getTeamId(topicControllerService.getUserName()))) {
      updateConstraint(
          constraintValidatorContext, "Failure. This topic is owned by a different team.");
      return false;
    }

    // validation on promotion of a topic
    if (!checkIfPromotionOfTopic(
        topics, tenantId, topicRequestModel, syncCluster, constraintValidatorContext)) return false;

    if (!validateTopicConfigParameters(topicRequestModel, constraintValidatorContext)) return false;

    // Verify if topic request already exists
    if (topics != null) {
      if (!topicControllerService.getExistingTopicRequests(topicRequestModel, tenantId).isEmpty()) {
        updateConstraint(constraintValidatorContext, "Failure. A topic request already exists.");
        return false;
      }
    }

    // Check if topic exists on cluster
    // Ignore topic exists check if Update request
    if (!TopicRequestTypes.Update.name().equals(topicRequestModel.getTopictype())) {
      boolean topicExists = false;
      if (topics != null) {
        topicExists =
            topics.stream()
                .anyMatch(
                    topicEx ->
                        Objects.equals(
                            topicEx.getEnvironment(), topicRequestModel.getEnvironment()));
      }
      if (topicExists) {
        updateConstraint(
            constraintValidatorContext,
            "Failure. This topic already exists in the selected cluster.");
        return false;
      }
    }

    return true;
  }

  private boolean checkIfPromotionOfTopic(
      List<Topic> topics,
      int tenantId,
      TopicRequestModel topicRequestModel,
      String syncCluster,
      ConstraintValidatorContext constraintValidatorContext) {
    String orderOfEnvs = mailService.getEnvProperty(tenantId, "ORDER_OF_ENVS");
    boolean promotionOrderCheck = false;
    if (null != orderOfEnvs) {
      promotionOrderCheck = checkInPromotionOrder(topicRequestModel.getEnvironment(), orderOfEnvs);
    }

    if (topics != null && !topics.isEmpty()) {
      if (promotionOrderCheck) {
        int devTopicFound =
            (int)
                topics.stream()
                    .filter(topic -> Objects.equals(topic.getEnvironment(), syncCluster))
                    .count();
        if (devTopicFound != 1) {
          if (topicControllerService.getEnvDetails(syncCluster) == null) {
            updateConstraint(
                constraintValidatorContext, "Failure. Base cluster is not configured.");
          } else {
            updateConstraint(
                constraintValidatorContext,
                "Failure. This topic does not exist in "
                    + topicControllerService.getEnvDetails(syncCluster).getName()
                    + " cluster.");
          }
          return false;
        }
      }
    } else if (!Objects.equals(topicRequestModel.getEnvironment(), syncCluster)) {
      if (promotionOrderCheck) {
        updateConstraint(
            constraintValidatorContext,
            "Failure. Please request for a topic first in "
                + topicControllerService.getEnvDetails(syncCluster).getName()
                + " cluster.");
        return false;
      }
    }

    return true;
  }

  private boolean validateTopicConfigParameters(
      TopicRequestModel topicRequestReq, ConstraintValidatorContext constraintValidatorContext) {
    log.debug("Into validateTopicConfigParameters");

    String topicPrefix = null, topicSuffix = null;
    String otherParams =
        topicControllerService.getEnvDetails(topicRequestReq.getEnvironment()).getOtherParams();
    String[] params;
    try {
      if (otherParams != null) {
        params = otherParams.split(",");
        for (String param : params) {
          if (param.startsWith("topic.prefix")) {
            topicPrefix = param.substring(param.indexOf("=") + 1);
          } else if (param.startsWith("topic.suffix")) {
            topicSuffix = param.substring(param.indexOf("=") + 1);
          }
        }
      }
    } catch (Exception e) {
      log.error("Unable to set topic partitions, setting default from properties.", e);
    }

    try {
      if (topicPrefix != null
          && !topicPrefix.isBlank()
          && !topicRequestReq.getTopicname().startsWith(topicPrefix)) {
        log.error(
            "Topic prefix {} does not match. {}", topicPrefix, topicRequestReq.getTopicname());
        updateConstraint(
            constraintValidatorContext,
            "Topic prefix does not match. " + topicRequestReq.getTopicname());
        return false;
      }

      if (topicSuffix != null
          && !topicSuffix.isBlank()
          && !topicRequestReq.getTopicname().endsWith(topicSuffix)) {
        log.error(
            "Topic suffix {} does not match. {}", topicSuffix, topicRequestReq.getTopicname());
        updateConstraint(
            constraintValidatorContext,
            "Topic suffix does not match. " + topicRequestReq.getTopicname());
        return false;
      }
    } catch (Exception e) {
      log.error("Unable to set topic partitions, setting default from properties.", e);
      updateConstraint(
          constraintValidatorContext, "Cluster default parameters config missing/incorrect.");
      return false;
    }

    return true;
  }

  private void updateConstraint(
      ConstraintValidatorContext constraintValidatorContext, String errorMessage) {
    constraintValidatorContext
        .buildConstraintViolationWithTemplate(errorMessage)
        .addConstraintViolation()
        .disableDefaultConstraintViolation();
  }

  private boolean checkInPromotionOrder(String envId, String orderOfEnvs) {
    List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));
    return orderedEnv.contains(envId);
  }
}
