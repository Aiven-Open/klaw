package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.model.response.EnvIdInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KlawResourceUtils {

  private static final LinkedHashSet<String> EMPTY_LINKED_HASH_SET = new LinkedHashSet<>(0);

  public static List<EnvIdInfo> getConvertedEnvs(
      List<Env> allEnvs, Collection<String> selectedEnvs) {
    List<EnvIdInfo> newEnvList = new ArrayList<>();
    for (String env : selectedEnvs) {
      for (Env env1 : allEnvs) {
        if (Objects.equals(env, env1.getId())) {
          newEnvList.add(new EnvIdInfo(env1.getId(), env1.getName()));
          break;
        }
      }
    }

    return newEnvList;
  }

  public static LinkedHashSet<String> getOrderedEnvsSet(String orderOfEnvs) {
    if (orderOfEnvs == null || orderOfEnvs.isEmpty()) {
      return EMPTY_LINKED_HASH_SET;
    }
    return Stream.of(orderOfEnvs.split(",")).collect(Collectors.toCollection(LinkedHashSet::new));
  }
}
