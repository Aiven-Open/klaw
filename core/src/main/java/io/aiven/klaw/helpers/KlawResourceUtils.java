package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.AclApproval;
import io.aiven.klaw.dao.Approval;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.model.response.EnvIdInfo;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KlawResourceUtils {

  private static final LinkedHashSet<String> EMPTY_LINKED_HASH_SET = new LinkedHashSet<>(0);

  public static List<EnvIdInfo> getConvertedEnvs(List<Env> allEnvs, Set<String> selectedEnvs) {
    Set<EnvIdInfo> newEnvSet = new LinkedHashSet<>();
    for (Env env1 : allEnvs) {
      if (selectedEnvs.contains(env1.getId())) {
        newEnvSet.add(new EnvIdInfo(env1.getId(), env1.getName()));
        break;
      }
    }

    return new ArrayList<>(newEnvSet);
  }

  public static LinkedHashSet<String> getOrderedEnvsSet(String orderOfEnvs) {
    if (orderOfEnvs == null || orderOfEnvs.isEmpty()) {
      return EMPTY_LINKED_HASH_SET;
    }
    return Stream.of(orderOfEnvs.split(",")).collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public static List<Approval> aclApprovalsToApprovalsList(List<AclApproval> aclApps) {
    return aclApps.stream().map(Approval::new).toList();
  }

  public static List<AclApproval> approvalsToAclApprovalsList(List<Approval> apps) {
    return apps.stream().map(AclApproval::new).toList();
  }
}
