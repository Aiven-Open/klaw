package io.aiven.klaw.validation;

import static io.aiven.klaw.error.KlawErrorMessages.*;
import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;

import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.TopicRequestModel;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.TopicControllerService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class TopicRequestValidatorImpl
    implements ConstraintValidator<TopicRequestValidator, TopicRequestModel> {

  @Autowired private CommonUtilsService commonUtilsService;
  @Autowired private MailUtils mailService;
  @Autowired private TopicControllerService topicControllerService;

  @Value("${klaw.validation.min.size.topicName:3}")
  private int minimumTopicNameSize;

  private PermissionType permissionType;

  @Override
  public void initialize(TopicRequestValidator constraintAnnotation) {
    this.permissionType = constraintAnnotation.getPermissionType();
  }

  @Override
  public boolean isValid(
      TopicRequestModel topicRequestModel, ConstraintValidatorContext constraintValidatorContext) {

    String userName = topicControllerService.getUserName();
    // Verify if user has access to request for topics
    if (commonUtilsService.isNotAuthorizedUser(
        topicControllerService.getPrincipal(), this.permissionType)) {
      updateConstraint(constraintValidatorContext, ApiResultStatus.NOT_AUTHORIZED.value);
      return false;
    }

    if (permissionType.equals(PermissionType.REQUEST_CREATE_TOPICS)) {
      // Verify if topic request type is Create/Promote
      if (RequestOperationType.CREATE != topicRequestModel.getRequestOperationType()
          && RequestOperationType.PROMOTE != topicRequestModel.getRequestOperationType()
          && topicRequestModel.getRequestId() == null) {
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_101);
        return false;
      }
    } else if (permissionType.equals(PermissionType.REQUEST_EDIT_TOPICS)) {
      if (RequestOperationType.UPDATE != topicRequestModel.getRequestOperationType()) {
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_102);
        return false;
      }
    } else {
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_103);
      return false;
    }

    // tenant filtering
    if (!commonUtilsService.getEnvsFromUserId(userName).contains(topicRequestModel.getEnvironment())
        && RequestOperationType.PROMOTE != topicRequestModel.getRequestOperationType()) {
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_104);
      return false;
    }

    // check for null topic name
    if (topicRequestModel.getTopicname() == null) {
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_105);
      return false;
    }

    // check for empty/whitespaces on topic name
    if (topicRequestModel.getTopicname().isBlank()) {
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_106);
      return false;
    }

    // verify tenant config exists
    int tenantId = commonUtilsService.getTenantId(userName);
    String syncCluster;
    try {
      syncCluster = topicControllerService.getSyncCluster(tenantId);
    } catch (Exception e) {
      log.error("Tenant Configuration not found. " + tenantId, e);
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_107);
      return false;
    }

    // Verify if topic requesting team exists
    Integer teamId = commonUtilsService.getTeamId(userName);
    if (null == teamId || teamId == 0) {
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_108);
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
            commonUtilsService.getTeamId(userName))) {
      updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_109);
      return false;
    }

    // validation on promotion of a topic
    if (!checkIfPromotionOfTopic(
        topics, tenantId, topicRequestModel, syncCluster, constraintValidatorContext)) return false;

    if (!validateTopicConfigParameters(topicRequestModel, constraintValidatorContext)) return false;

    // Verify if topic request already exists
    if (topics != null && topicRequestModel.getRequestId() == null) {
      if (!topicControllerService.getExistingTopicRequests(topicRequestModel, tenantId).isEmpty()) {
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_110);
        return false;
      }
    } else {
      // Editing an existing topic request of type DELETE/CLAIM is not possible.
      if (topicRequestModel.getRequestOperationType().equals(RequestOperationType.CLAIM)
          || topicRequestModel.getRequestOperationType().equals(RequestOperationType.DELETE)) {
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_122);
        return false;
      }

      // only requests in created state can be edited
      if (!topicControllerService
          .getTopicRequestFromTopicId(topicRequestModel.getRequestId(), tenantId)
          .getRequestStatus()
          .equals(RequestStatus.CREATED.value)) {
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_123);
        return false;
      }
    }

    // Check if topic exists on cluster
    // Ignore topic exists check if Update request
    if (RequestOperationType.UPDATE != topicRequestModel.getRequestOperationType()) {
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
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_111);
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
    String orderOfEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
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
            updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_112);
          } else {
            updateConstraint(
                constraintValidatorContext,
                String.format(
                    TOPICS_VLD_ERR_113,
                    topicControllerService.getEnvDetails(syncCluster).getName()));
          }
          return false;
        }
      }
    } else if (!Objects.equals(topicRequestModel.getEnvironment(), syncCluster)) {
      if (promotionOrderCheck) {
        updateConstraint(
            constraintValidatorContext,
            String.format(
                TOPICS_VLD_ERR_114, topicControllerService.getEnvDetails(syncCluster).getName()));
        return false;
      }
    }

    return true;
  }

  private boolean validateTopicConfigParameters(
      TopicRequestModel topicRequestReq, ConstraintValidatorContext constraintValidatorContext) {

    EnvParams params =
        topicControllerService.getEnvDetails(topicRequestReq.getEnvironment()).getParams();
    if (params != null) {
      String topicPrefix = getValueOrDefault(params.getTopicPrefix(), "");
      String topicSuffix = getValueOrDefault(params.getTopicSuffix(), "");
      String topicRegex = getValueOrDefault(params.getTopicRegex(), "");
      String topicName = topicRequestReq.getTopicname();
      try {
        if (!params.isApplyRegex()) {
          if (topicPrefix != null && !topicPrefix.isBlank() && !topicName.startsWith(topicPrefix)) {
            log.error(
                "Topic prefix {} does not match. {}", topicPrefix, topicRequestReq.getTopicname());
            updateConstraint(
                constraintValidatorContext,
                String.format(TOPICS_VLD_ERR_115, topicRequestReq.getTopicname()));
            return false;
          }

          if (topicSuffix != null && !topicSuffix.isBlank() && !topicName.endsWith(topicSuffix)) {
            log.error(
                "Topic suffix {} does not match. {}", topicSuffix, topicRequestReq.getTopicname());
            updateConstraint(
                constraintValidatorContext,
                String.format(TOPICS_VLD_ERR_116, topicRequestReq.getTopicname()));
            return false;
          }
          // It will return before here if any other validation is not met.
          topicName = StringUtils.removeStart(topicName, topicPrefix);
          // if the prefix is set it will be removed by now, so we just need to see if there was any
          // over lap in the prefix.
          if (topicPrefix != null
              && topicSuffix != null
              && topicName.length() < topicSuffix.length()) {
            log.error(
                "Topic Suffix and Topic Prefix overlap there is a requirement for {} characters minimum to be unique between the prefix and suffix.",
                topicRequestReq.getTopicname(),
                minimumTopicNameSize);
            updateConstraint(
                constraintValidatorContext,
                String.format(
                    TOPICS_VLD_ERR_120, topicRequestReq.getTopicname(), minimumTopicNameSize));

            return false;
          }
          topicName = StringUtils.removeEnd(topicName, topicSuffix);

          // Check topic name without prefix or suffix meets minimum length requirements
          if (topicName.length() < minimumTopicNameSize) {
            log.error(
                "Topic name: {} is not long enough when prefix and suffix's are excluded. {} characters minimum are required to be unique.",
                topicRequestReq.getTopicname(),
                minimumTopicNameSize);
            updateConstraint(
                constraintValidatorContext,
                String.format(
                    TOPICS_VLD_ERR_119, topicRequestReq.getTopicname(), minimumTopicNameSize));
            return false;
          }

        } else {
          if (topicRegex != null
              && !topicRegex.isBlank()
              && !isRegexAMatch(topicRequestReq, topicRegex)) {

            log.error(
                "Topic Regex {} does not match. {}", topicRegex, topicRequestReq.getTopicname());
            updateConstraint(
                constraintValidatorContext,
                String.format(TOPICS_VLD_ERR_118, topicRequestReq.getTopicname()));
            return false;
          }
        }

      } catch (Exception e) {
        log.error("Unable to set topic partitions, setting default from properties.", e);
        updateConstraint(constraintValidatorContext, TOPICS_VLD_ERR_117);
        return false;
      }
    }
    return true;
  }

  // TODO Review rej and see if this would provide a better experience for Klaw.
  private boolean isRegexAMatch(TopicRequestModel topicRequestReq, String topicRegex) {
    Pattern p = Pattern.compile(topicRegex);
    Matcher m = p.matcher(topicRequestReq.getTopicname());
    return m.matches();
  }

  private static String getValueOrDefault(List<String> params, String defaultValue) {
    return (params != null && params.size() > 0) ? params.get(0) : defaultValue;
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
