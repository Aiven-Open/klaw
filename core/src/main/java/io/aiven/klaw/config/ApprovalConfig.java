package io.aiven.klaw.config;

import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.error.KlawConfigurationException;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Approval Config consumes an approval.properties file from the classpath this is the used to allow
 * for creation of complex rules for approving resources. In the first release (2.8) this is only
 * being used by Claim Acl's where multiple Approvals are required to transfer ownership of an ACL.
 * The approval config is flattened and moved into a map which is then queried based on the
 * operation type and then environment and it allows increased control of who can approve what
 * operations across teams and environments.
 */
@Data
@Configuration
@EnableConfigurationProperties
@PropertySource("classpath:approval.properties")
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
  @Qualifier("approvalService")
  public ApprovalService approvalService() throws KlawConfigurationException {
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
            processApprovalsFromSettings(splitPropertyConfigValues(properties, keyStr)),
            entityToApprovers);
      } else {
        entityToApprovers.put(
            keyStr.toUpperCase(),
            processApprovalsFromSettings(splitPropertyConfigValues(properties, keyStr)));
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
          throw new KlawConfigurationException("Duplicate approval key " + strKey);
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
    setAllEntityTypesApproves(null, processApprovalsFromSettings(type.name()), entityToApprovers);
    // Set all Defaults.
    if (type.equals(RequestEntityType.ACL)) {
      // By Default ACl will also have the Topic Team Owner as an approver
      setAllEntityTypesApproves(
          null,
          processApprovalsFromSettings(ApprovalType.TOPIC_TEAM_OWNER.name()),
          entityToApprovers);
      // If ACL by default add the additional ACL approval on the CLAIM
      entityToApprovers
          .get(RequestOperationType.CLAIM.name())
          .add(createDefaultApproval(ApprovalType.ACL_TEAM_OWNER));
      // If ACL remove the RESOURCE TEAM OWNER and Add the ACL TEAM OWNER
      entityToApprovers.get(RequestOperationType.DELETE.name()).clear();
      entityToApprovers
          .get(RequestOperationType.DELETE.name())
          .add(createDefaultApproval(ApprovalType.ACL_TEAM_OWNER));
    } else if (type.equals(RequestEntityType.TOPIC) || type.equals(RequestEntityType.SCHEMA)) {
      setAllEntityTypesApproves(
          null,
          processApprovalsFromSettings(ApprovalType.TOPIC_TEAM_OWNER.name()),
          entityToApprovers);
    } else if (type.equals(RequestEntityType.CONNECTOR)) {
      setAllEntityTypesApproves(
          null,
          processApprovalsFromSettings(ApprovalType.CONNECTOR_TEAM_OWNER.name()),
          entityToApprovers);
    }

    return entityToApprovers;
  }

  private void setAllEntityTypesApproves(
      String suffix, List<Approval> approvers, Map<String, List<Approval>> entityToApprovers) {
    for (RequestOperationType value : RequestOperationType.values()) {
      entityToApprovers.put(
          suffix == null ? value.name() : value.name() + suffix.toUpperCase(),
          new ArrayList<>(approvers));
    }
  }

  private List<Approval> processApprovalsFromSettings(String... configuredApprovals) {
    List<Approval> approvals = new ArrayList<>();

    for (String str : configuredApprovals) {

      if (ApprovalType.isApprovalType(str)) {
        // TODO If this is expanded to allow multi approval to be used with connectors and topics
        // add a check to make sure a CONNECTOR_TEAM_OWNER OR ACL_TEAM_OWNER can't be added to a
        // TOPIC or SCHEMA Approval etc.
        approvals.add(createDefaultApproval(ApprovalType.of(str)));
      } else {
        Approval teamApproval = createDefaultApproval(ApprovalType.TEAM);
        teamApproval.setRequiredApprover(str);
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
