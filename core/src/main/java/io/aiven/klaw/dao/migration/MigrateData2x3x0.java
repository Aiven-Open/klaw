package io.aiven.klaw.dao.migration;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.response.EnvParams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@DataMigration(version = "2.3.0", order = 1)
@Slf4j
@Configuration // Spring will automatically scan and instantiate this class for retrieval.
public class MigrateData2x3x0 {

  @Autowired private SelectDataJdbc selectDataJdbc;

  @Autowired private InsertDataJdbc insertDataJdbc;

  public MigrateData2x3x0() {}

  public MigrateData2x3x0(SelectDataJdbc selectDataJdbc, InsertDataJdbc insertDataJdbc) {
    this.selectDataJdbc = selectDataJdbc;
    this.insertDataJdbc = insertDataJdbc;
  }

  @MigrationRunner()
  public boolean migrate() {
    List<UserInfo> allUserInfo = selectDataJdbc.selectAllUsersAllTenants();
    Set<Integer> tenantIds =
        allUserInfo.stream().map(UserInfo::getTenantId).collect(Collectors.toSet());

    for (int tenantId : tenantIds) {
      migrateKafkaEnvironments(tenantId);
    }

    return true;
  }

  private void migrateKafkaEnvironments(Integer tenantId) {
    int numberOfRequests = 0, numberOfRequestsUpdated = 0;
    List<Env> envs = selectDataJdbc.selectAllEnvs(KafkaClustersType.KAFKA, tenantId);

    for (Env env : envs) {
      try {
        numberOfRequests++;
        // Only migrate data that doesn't already have the parameters set.
        if (env.getParams() == null) {
          EnvParams params = new EnvParams();
          String envParams = env.getOtherParams();

          String[] stringParams = envParams.split(",");
          String defaultPartitions = null, defaultRf = null;
          for (String param : stringParams) {
            if (param.startsWith("default.partitions")) {
              defaultPartitions = param.substring(param.indexOf("=") + 1);
              params.setDefaultPartitions(getValueAsList(param));
            } else if (param.startsWith("max.partitions")) {
              List<String> partitions = generateMaxList(defaultPartitions, param);
              params.setPartitionsList(partitions);
            } else if (param.startsWith("default.replication.factor")) {
              defaultRf = param.substring(param.indexOf("=") + 1);
              params.setDefaultRepFactor(getValueAsList(param));
            } else if (param.startsWith("max.replication.factor")) {
              List<String> rf = generateMaxList(defaultRf, param);
              params.setReplicationFactorList(rf);
            } else if (param.startsWith("topic.prefix")) {

              params.setTopicPrefix(getValueAsList(param));
            } else if (param.startsWith("topic.suffix")) {

              params.setTopicSuffix(getValueAsList(param));
            }
          }
          params.setTopicRegex(Collections.EMPTY_LIST);
          params.setAdvancedTopicConfiguration(List.of("false"));
          env.setParams(params);
          insertDataJdbc.addNewEnv(env);
          numberOfRequestsUpdated++;
        }
      } catch (Exception ex) {
        log.error(
            String.format(
                "Exception caught on envId %s and envName %s and tenantId: %s ,",
                env.getId(), env.getName(), env.getTenantId()),
            ex);
      }
    }
    log.info(
        "Data Migration 2.3.0 Completed for TenantId : {} with {} environments updated out of {} environments",
        tenantId,
        numberOfRequestsUpdated,
        numberOfRequests);
  }

  private static List<String> generateMaxList(String defaultValue, String param) {
    int maxSize = Integer.parseInt(param.substring(param.indexOf("=") + 1));
    List<String> rf = new ArrayList<>();

    for (int i = 1; i < maxSize + 1; i++) {
      if (defaultValue != null && defaultValue.equals(i + "")) {
        rf.add(i + " (default)");
      } else {
        rf.add(i + "");
      }
    }
    return rf;
  }

  private static List<String> getValueAsList(String param) {
    String paramName = param.substring(param.indexOf("=") + 1);
    List<String> paramList = new ArrayList<>();
    paramList.add(paramName);
    return paramList;
  }
}
