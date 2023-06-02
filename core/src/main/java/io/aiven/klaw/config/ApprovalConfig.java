package io.aiven.klaw.config;

import io.aiven.klaw.error.KlawConfigurationException;
import io.aiven.klaw.model.Approval;
import io.aiven.klaw.model.enums.ApprovalType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.service.ApprovalService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Data
@Configuration
@EnableConfigurationProperties
@PropertySource("/approval.properties")
public class ApprovalConfig {

  public static final String ALL_ENTITY_TYPES = "ALL";
  public static final String LIST_SEPERATOR = ",";

  @ConfigurationProperties(prefix = "topic")
  @Bean
  public Properties getTopicProperties() {
    return new Properties();
  }

  @ConfigurationProperties(prefix = "acl")
  @Bean
  public Properties getACLProperties() {
    return new Properties();
  }

  @ConfigurationProperties(prefix = "connect")
  @Bean
  public Properties getConnectProperties() {
    return new Properties();
  }

  @ConfigurationProperties(prefix = "schema")
  @Bean
  public Properties getSchemaProperties() {
    return new Properties();
  }

  @Bean
  public ApprovalService approvalService() throws KlawConfigurationException {
    log.info("Start Approval Config set up");
    return new ApprovalService(
        flattenAllRequestsToKeys(getTopicProperties(), RequestEntityType.TOPIC),
        flattenAllRequestsToKeys(getACLProperties(), RequestEntityType.ACL),
        flattenAllRequestsToKeys(getSchemaProperties(), RequestEntityType.SCHEMA),
        flattenAllRequestsToKeys(getConnectProperties(), RequestEntityType.CONNECTOR));
  }

  private Map<String, List<Approval>> flattenAllRequestsToKeys(
      Properties properties, RequestEntityType type) throws KlawConfigurationException {
    Map<String, List<Approval>> entityToApprovers = createDefaultApprovalMap(type);

    for (String key : getOrderedKeyListAndValidateKeys(properties)) {
      // Key values are in the format
      // RequestOperationType.Env (The Entity Type ACL/TOPIC etc is already stripped out)
      String keyStr = String.valueOf(key);
      if (keyStr.toUpperCase().startsWith(ALL_ENTITY_TYPES)) {
        // This suffix will include the "." seperator if it is shown here.
        String suffix = keyStr.toUpperCase().substring(ALL_ENTITY_TYPES.length());
        setAllEntityTypesApproves(
            suffix,
            processApprovalsFromSettings(type, splitPropertyConfigValues(properties, keyStr)),
            entityToApprovers);
      } else {
        entityToApprovers.put(
            keyStr.toUpperCase(),
            processApprovalsFromSettings(type, splitPropertyConfigValues(properties, keyStr)));
      }
    }

    return entityToApprovers;
  }

  private Set<String> getOrderedKeyListAndValidateKeys(Properties properties)
      throws KlawConfigurationException {

    // put all the "ALL" first
    Set<Object> unfilteredKeySet = properties.keySet();
    SortedSet<String> orderedSet = new TreeSet<>();
    // Check that each key has a maximum of 1 seperator '.'
    // Also Order so all 'ALL' keys are processed first

    for (Object key : unfilteredKeySet) {
      String strKey = (String) key;
      if (strKey.chars().filter(ch -> ch == '.').count() > 1) {
        throw new KlawConfigurationException("Approval key " + strKey + " has too many parts.");
      } else if (strKey.startsWith(ALL_ENTITY_TYPES)) {
        if (!orderedSet.add(strKey)) {
          throw new KlawConfigurationException("Dupliate approval key " + strKey);
        }
      }
    }
    // Add specific override keys after the ALL keys
    for (Object key : unfilteredKeySet) {
      String strKey = (String) key;
      if (!strKey.startsWith(ALL_ENTITY_TYPES)) {
        if (!orderedSet.add(strKey)) {
          throw new KlawConfigurationException("Duplicate approval key " + strKey);
        }
      }
    }

    return orderedSet;
  }

  private String[] splitPropertyConfigValues(Properties properties, String keyStr) {
    return properties.getProperty(keyStr) != null
        ? properties.getProperty(keyStr).split(LIST_SEPERATOR)
        : new String[0];
  }

  private Map<String, List<Approval>> createDefaultApprovalMap(RequestEntityType type) {
    Map<String, List<Approval>> entityToApprovers = new HashMap<>();
    setAllEntityTypesApproves(
        null,
        processApprovalsFromSettings(type, ApprovalType.RESOURCE_TEAM_OWNER.name()),
        entityToApprovers);
    if (type.equals(RequestEntityType.ACL)) {
      // If ACL by default add the additional ACL approval on the CLAIM
      entityToApprovers
          .get(RequestOperationType.CLAIM.name())
          .add(createDefaultApproval(ApprovalType.ACL_TEAM_OWNER));
      // If ACL remove the RESOURCE TEAM OWNER and Add the ACL TEAM OWNER
      entityToApprovers.get(RequestOperationType.DELETE.name()).clear();
      entityToApprovers
          .get(RequestOperationType.DELETE.name())
          .add(createDefaultApproval(ApprovalType.ACL_TEAM_OWNER));
    }

    return entityToApprovers;
  }

  private void setAllEntityTypesApproves(
      String suffix, List<Approval> approvers, Map<String, List<Approval>> entityToApprovers) {
    for (RequestOperationType value : RequestOperationType.values()) {
      entityToApprovers.put(
          suffix == null ? value.name() : value.name() + suffix.toUpperCase(),
          new ArrayList(approvers));
    }
  }

  private List<Approval> processApprovalsFromSettings(
      RequestEntityType type, String... configuredApprovals) {
    List<Approval> approvals = new ArrayList<>();

    for (String str : configuredApprovals) {

      if (str.equalsIgnoreCase(ApprovalType.RESOURCE_TEAM_OWNER.name())) {
        approvals.add(createDefaultApproval(ApprovalType.RESOURCE_TEAM_OWNER));
      } else if (str.equalsIgnoreCase(ApprovalType.ACL_TEAM_OWNER.name())
          && type.equals(RequestEntityType.ACL)) {
        approvals.add(createDefaultApproval(ApprovalType.ACL_TEAM_OWNER));
      } else {
        Approval teamApproval = createDefaultApproval(ApprovalType.TEAM);
        teamApproval.setRequiredApprovingTeamName(str);
        approvals.add(teamApproval);
      }
    }

    return approvals;
  }

  private static Approval createDefaultApproval(ApprovalType approvalType) {
    Approval app = new Approval();
    app.setApprovalType(approvalType);
    return app;
  }
}
