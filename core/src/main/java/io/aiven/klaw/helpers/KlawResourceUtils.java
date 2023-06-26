package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.model.response.EnvIdInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KlawResourceUtils {

  public static List<EnvIdInfo> getConvertedEnvs(List<Env> allEnvs, List<String> selectedEnvs) {
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
}
