package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.enums.AclIPPrincipleType;
import io.aiven.klaw.clusterapi.models.enums.AclPatternType;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.RequestOperationType;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApacheKafkaAclService {

  private static final long TIME_OUT_SECS_FOR_ACLS = 5;

  private final ClusterApiUtils clusterApiUtils;

  public ApacheKafkaAclService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public synchronized Set<Map<String, String>> loadAcls(
      String environment, KafkaSupportedProtocol protocol, String clusterName) throws Exception {
    log.info("loadAcls {} {}", environment, protocol);
    Set<Map<String, String>> acls = new HashSet<>();

    AdminClient client = clusterApiUtils.getAdminClient(environment, protocol, clusterName);
    if (client == null) {
      throw new Exception("Cannot connect to cluster.");
    }

    try {
      AclBindingFilter aclBindingFilter = AclBindingFilter.ANY;
      DescribeAclsResult aclsResult = client.describeAcls(aclBindingFilter);

      aclsResult
          .values()
          .get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS)
          .forEach(aclBinding -> filterAndUpdateAclBindings(acls, aclBinding));
    } catch (Exception e) {
      log.error("Exception: ", e);
    }

    return acls;
  }

  private static void filterAndUpdateAclBindings(
      Set<Map<String, String>> acls, AclBinding aclBinding) {
    if (aclBinding.pattern().patternType().name().equals(AclPatternType.LITERAL.value)) {
      Map<String, String> aclbindingMap = new HashMap<>();
      AccessControlEntry accessControlEntry = aclBinding.entry();
      aclbindingMap.put("host", accessControlEntry.host());
      aclbindingMap.put("principle", accessControlEntry.principal());
      aclbindingMap.put("operation", accessControlEntry.operation().toString());
      aclbindingMap.put("permissionType", accessControlEntry.permissionType().toString());
      aclbindingMap.put("resourceType", aclBinding.pattern().resourceType().toString());
      aclbindingMap.put("resourceName", aclBinding.pattern().name());

      if (!aclBinding.pattern().resourceType().toString().equals("CLUSTER")) {
        if (accessControlEntry.operation().toString().equals("WRITE")
            || accessControlEntry.operation().toString().equals("READ")) {
          acls.add(aclbindingMap);
        }
      }
    }
  }

  public synchronized String updateProducerAcl(ClusterAclRequest clusterAclRequest) {
    log.info("updateProducerAclRequest {}", clusterAclRequest);
    AdminClient client;
    try {
      PatternType patternType;
      if (clusterAclRequest.isPrefixAcl()) {
        patternType = PatternType.PREFIXED;
      } else {
        patternType = PatternType.LITERAL;
      }

      client =
          clusterApiUtils.getAdminClient(
              clusterAclRequest.getEnv(),
              clusterAclRequest.getProtocol(),
              clusterAclRequest.getClusterName());
      if (client == null) {
        return ApiResultStatus.FAILURE.value;
      }

      String host,
          principal,
          aclSsl = clusterAclRequest.getAclSsl(),
          aclIp = clusterAclRequest.getAclIp();
      if (clusterAclRequest.getAclSsl() != null
          && clusterAclRequest.getAclSsl().trim().length() > 0) {
        aclSsl = aclSsl.trim();
        if (AclIPPrincipleType.PRINCIPAL.name().equals(clusterAclRequest.getAclIpPrincipleType())) {
          host = "*";
          principal = "User:" + aclSsl;

          if (RequestOperationType.CREATE.equals(clusterAclRequest.getRequestOperationType())) {
            if (updateTopicProducerWriteAcls(
                clusterAclRequest.getTopicName(), client, patternType, host, principal)) {
              return "Acl already exists. success";
            }
          } else {
            processOtherRequests(clusterAclRequest, client, patternType, host, principal);
          }
          updateTransactionalIdAclsForProducer(
              clusterAclRequest.getTransactionalId(),
              client,
              patternType,
              host,
              principal,
              clusterAclRequest.getRequestOperationType().value);
        }
      }

      if (aclIp != null && aclIp.trim().length() > 0) {
        aclIp = aclIp.trim();
        host = aclIp;
        principal = "User:*";

        if (clusterAclRequest.getRequestOperationType().equals(RequestOperationType.CREATE)) {
          if (updateTopicProducerWriteAcls(
              clusterAclRequest.getTopicName(), client, patternType, host, principal)) {
            return "Acl already exists. success";
          }
        } else {
          processOtherRequests(clusterAclRequest, client, patternType, host, principal);
        }
        // Update transactional id acls
        updateTransactionalIdAclsForProducer(
            clusterAclRequest.getTransactionalId(),
            client,
            patternType,
            host,
            principal,
            clusterAclRequest.getRequestOperationType().value);
      }

    } catch (Exception e) {
      log.error("Exception: ", e);
      return ApiResultStatus.FAILURE.value;
    }

    return ApiResultStatus.SUCCESS.value;
  }

  private void processOtherRequests(
      ClusterAclRequest clusterAclRequest,
      AdminClient client,
      PatternType patternType,
      String host,
      String principal)
      throws InterruptedException, ExecutionException, TimeoutException {
    List<AclBindingFilter> aclListArray = new ArrayList<>();

    ResourcePatternFilter resourcePattern =
        new ResourcePatternFilter(
            ResourceType.TOPIC, clusterAclRequest.getTopicName(), patternType);
    AccessControlEntryFilter aclEntry =
        new AccessControlEntryFilter(principal, host, AclOperation.WRITE, AclPermissionType.ALLOW);
    AclBindingFilter aclBinding1 = new AclBindingFilter(resourcePattern, aclEntry);
    aclListArray.add(aclBinding1);

    aclEntry =
        new AccessControlEntryFilter(
            principal, host, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
    AclBindingFilter aclBinding2 = new AclBindingFilter(resourcePattern, aclEntry);
    aclListArray.add(aclBinding2);

    client.deleteAcls(aclListArray).all().get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS);
  }

  private boolean updateTopicProducerWriteAcls(
      String topicName, AdminClient client, PatternType patternType, String host, String principal)
      throws InterruptedException, ExecutionException, TimeoutException {
    List<AclBinding> aclListArray = new ArrayList<>();

    ResourcePattern resourcePattern =
        new ResourcePattern(ResourceType.TOPIC, topicName, patternType);
    AccessControlEntry aclEntry =
        new AccessControlEntry(principal, host, AclOperation.WRITE, AclPermissionType.ALLOW);
    AclBinding aclBinding1 = new AclBinding(resourcePattern, aclEntry);
    aclListArray.add(aclBinding1);

    boolean acl1Exists = aclExists(client, aclBinding1.toFilter());

    aclEntry =
        new AccessControlEntry(principal, host, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
    AclBinding aclBinding2 = new AclBinding(resourcePattern, aclEntry);
    aclListArray.add(aclBinding2);

    boolean acl2Exists = aclExists(client, aclBinding2.toFilter());

    if (acl1Exists && acl2Exists) {
      return true;
    }
    client.createAcls(aclListArray).all().get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS);
    return false;
  }

  private boolean aclExists(AdminClient client, AclBindingFilter aclBindingFilter) {
    DescribeAclsResult aclsResult = client.describeAcls(aclBindingFilter);
    try {
      if (aclsResult.values().get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS).size() == 1) {
        return true;
      }
    } catch (InterruptedException | TimeoutException | ExecutionException e) {
      log.error("Exception:", e);
    }
    return false;
  }

  private void updateTransactionalIdAclsForProducer(
      String transactionalId,
      AdminClient client,
      PatternType patternType,
      String host,
      String principal,
      String aclOperation)
      throws InterruptedException, ExecutionException, TimeoutException {

    List<AclBinding> aclListArray = new ArrayList<>();
    // Adding transactional id acls
    if (transactionalId != null) {
      transactionalId = transactionalId.trim();
    }

    if (transactionalId != null && transactionalId.length() > 0) {
      ResourcePattern resourcePatternTxn =
          new ResourcePattern(ResourceType.TRANSACTIONAL_ID, transactionalId, patternType);
      AccessControlEntry aclEntryTxn =
          new AccessControlEntry(principal, host, AclOperation.WRITE, AclPermissionType.ALLOW);
      AclBinding aclBinding1Txn = new AclBinding(resourcePatternTxn, aclEntryTxn);
      aclListArray.add(aclBinding1Txn);

      if (aclOperation.equals("Create")) {
        client.createAcls(aclListArray);
      } else {
        List<AclBindingFilter> aclListArrayDel = new ArrayList<>();

        ResourcePatternFilter resourcePattern =
            new ResourcePatternFilter(ResourceType.TRANSACTIONAL_ID, transactionalId, patternType);
        AccessControlEntryFilter aclEntry =
            new AccessControlEntryFilter(
                principal, host, AclOperation.WRITE, AclPermissionType.ALLOW);
        AclBindingFilter aclBinding1 = new AclBindingFilter(resourcePattern, aclEntry);
        aclListArrayDel.add(aclBinding1);
        client.deleteAcls(aclListArrayDel).all().get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS);
      }
    }
  }

  public synchronized String updateConsumerAcl(ClusterAclRequest clusterAclRequest) {
    log.info("updateConsumerAclRequest {} ", clusterAclRequest);
    AdminClient client;
    String resultStr = "";
    try {
      PatternType patternType;
      patternType = PatternType.LITERAL;

      client =
          clusterApiUtils.getAdminClient(
              clusterAclRequest.getEnv(),
              clusterAclRequest.getProtocol(),
              clusterAclRequest.getClusterName());
      if (client == null) {
        return ApiResultStatus.FAILURE.value;
      }

      String host = null,
          principal = null,
          aclSsl = clusterAclRequest.getAclSsl(),
          aclIp = clusterAclRequest.getAclIp();
      boolean isValidParam = false;

      if (aclSsl != null && aclSsl.trim().length() > 0 && !aclSsl.equals("User:*")) {
        aclSsl = aclSsl.trim();

        if (AclIPPrincipleType.PRINCIPAL.name().equals(clusterAclRequest.getAclIpPrincipleType())) {
          host = "*";
          principal = "User:" + aclSsl;
          isValidParam = true;
        }

        if (RequestOperationType.CREATE.equals(clusterAclRequest.getRequestOperationType())
            && isValidParam) {
          List<AclBinding> aclListArray = new ArrayList<>();

          AccessControlEntry aclEntry =
              new AccessControlEntry(principal, host, AclOperation.READ, AclPermissionType.ALLOW);
          ResourcePattern resourcePattern =
              new ResourcePattern(
                  ResourceType.TOPIC, clusterAclRequest.getTopicName(), patternType);

          resultStr =
              processAclBindings(
                  clusterAclRequest,
                  client,
                  patternType,
                  host,
                  principal,
                  aclListArray,
                  aclEntry,
                  resourcePattern);

        } else if (isValidParam) {
          List<AclBindingFilter> aclListArray = new ArrayList<>();

          AccessControlEntryFilter aclEntry =
              new AccessControlEntryFilter(
                  principal, host, AclOperation.READ, AclPermissionType.ALLOW);
          ResourcePatternFilter resourcePattern =
              new ResourcePatternFilter(
                  ResourceType.TOPIC, clusterAclRequest.getTopicName(), patternType);

          resultStr =
              processOtherAclBindings(
                  clusterAclRequest,
                  client,
                  patternType,
                  host,
                  principal,
                  aclListArray,
                  aclEntry,
                  resourcePattern);
        }
      }

      if (aclIp != null && aclIp.trim().length() > 0) {
        aclIp = aclIp.trim();
        host = aclIp;
        principal = "User:*";

        if (RequestOperationType.CREATE.equals(clusterAclRequest.getRequestOperationType())) {
          List<AclBinding> aclListArray = new ArrayList<>();

          ResourcePattern resourcePattern =
              new ResourcePattern(
                  ResourceType.TOPIC, clusterAclRequest.getTopicName(), patternType);
          AccessControlEntry aclEntry =
              new AccessControlEntry(principal, host, AclOperation.READ, AclPermissionType.ALLOW);
          resultStr =
              processAclBindings(
                  clusterAclRequest,
                  client,
                  patternType,
                  host,
                  principal,
                  aclListArray,
                  aclEntry,
                  resourcePattern);

        } else {
          List<AclBindingFilter> aclListArray = new ArrayList<>();

          ResourcePatternFilter resourcePattern =
              new ResourcePatternFilter(
                  ResourceType.TOPIC, clusterAclRequest.getTopicName(), patternType);
          AccessControlEntryFilter aclEntry =
              new AccessControlEntryFilter(
                  principal, host, AclOperation.READ, AclPermissionType.ALLOW);
          resultStr =
              processOtherAclBindings(
                  clusterAclRequest,
                  client,
                  patternType,
                  host,
                  principal,
                  aclListArray,
                  aclEntry,
                  resourcePattern);
        }
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      return ApiResultStatus.FAILURE.value;
    }

    return resultStr;
  }

  private String processOtherAclBindings(
      ClusterAclRequest clusterAclRequest,
      AdminClient client,
      PatternType patternType,
      String host,
      String principal,
      List<AclBindingFilter> aclListArray,
      AccessControlEntryFilter aclEntry,
      ResourcePatternFilter resourcePattern)
      throws InterruptedException, ExecutionException, TimeoutException {
    String resultStr;
    AclBindingFilter aclBinding1 = new AclBindingFilter(resourcePattern, aclEntry);
    aclListArray.add(aclBinding1);

    aclEntry =
        new AccessControlEntryFilter(
            principal, host, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
    AclBindingFilter aclBinding2 = new AclBindingFilter(resourcePattern, aclEntry);
    aclListArray.add(aclBinding2);

    resourcePattern =
        new ResourcePatternFilter(
            ResourceType.GROUP, clusterAclRequest.getConsumerGroup(), patternType);
    aclEntry =
        new AccessControlEntryFilter(principal, host, AclOperation.READ, AclPermissionType.ALLOW);
    AclBindingFilter aclBinding3 = new AclBindingFilter(resourcePattern, aclEntry);
    aclListArray.add(aclBinding3);

    client.deleteAcls(aclListArray).all().get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS);
    resultStr = ApiResultStatus.SUCCESS.value;
    return resultStr;
  }

  private String processAclBindings(
      ClusterAclRequest clusterAclRequest,
      AdminClient client,
      PatternType patternType,
      String host,
      String principal,
      List<AclBinding> aclListArray,
      AccessControlEntry aclEntry,
      ResourcePattern resourcePattern)
      throws InterruptedException, ExecutionException, TimeoutException {
    String resultStr;
    AclBinding aclBinding1 = new AclBinding(resourcePattern, aclEntry);
    aclListArray.add(aclBinding1);

    boolean acl1Exists = aclExists(client, aclBinding1.toFilter());

    aclEntry =
        new AccessControlEntry(principal, host, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
    AclBinding aclBinding2 = new AclBinding(resourcePattern, aclEntry);
    aclListArray.add(aclBinding2);

    boolean acl2Exists = aclExists(client, aclBinding2.toFilter());

    resourcePattern =
        new ResourcePattern(ResourceType.GROUP, clusterAclRequest.getConsumerGroup(), patternType);
    aclEntry = new AccessControlEntry(principal, host, AclOperation.READ, AclPermissionType.ALLOW);
    AclBinding aclBinding3 = new AclBinding(resourcePattern, aclEntry);
    aclListArray.add(aclBinding3);

    boolean acl3Exists = aclExists(client, aclBinding3.toFilter());
    if (acl1Exists && acl2Exists && acl3Exists) {
      resultStr = "Acl already exists. success";
    } else {
      client.createAcls(aclListArray).all().get(TIME_OUT_SECS_FOR_ACLS, TimeUnit.SECONDS);
      resultStr = ApiResultStatus.SUCCESS.value;
    }
    return resultStr;
  }
}
