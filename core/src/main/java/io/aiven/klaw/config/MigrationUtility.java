package io.aiven.klaw.config;

import static io.aiven.klaw.error.KlawErrorMessages.MIGRATION_ERR_101;

import io.aiven.klaw.dao.DataVersion;
import io.aiven.klaw.dao.migration.DataMigration;
import io.aiven.klaw.dao.migration.MigrationRunner;
import io.aiven.klaw.error.KlawDataMigrationException;
import io.aiven.klaw.repository.DataVersionRepo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MigrationUtility {

  public static final int SUPPORTED_KLAW_VERSION_NUMBER_SYSTEM = 3;

  @Value("${klaw.data.migration.packageToScan:io.aiven.klaw.dao.migration}")
  private String packageToScan;

  @Value("${klaw.version}")
  private String currentKlawVersion;

  @Autowired private DataVersionRepo versionRepo;

  @Autowired private ApplicationContext context;

  @SchedulerLock(
      name = "TaskScheduler_MigrationUtility",
      lockAtLeastFor = "PT10M",
      lockAtMostFor = "PT60M")
  public void startMigration() throws Exception {
    // Find the latest version in DB
    String latestDataVersion = getLatestDataVersion();
    if (!isVersionGreaterThenCurrentVersion(latestDataVersion, currentKlawVersion)) {
      log.info(
          "Current Data Version {} is greater than the Klaw version {}, no action needed.",
          latestDataVersion,
          currentKlawVersion);
      return;
    }

    // Find all DataMigration annotated classes.// Migrate each one
    Reflections reflections =
        new Reflections(new ConfigurationBuilder().forPackages(packageToScan));
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(DataMigration.class);
    SortedMap<Integer, Pair<String, Class<?>>> orderedMapOfMigrationInstructions =
        orderApplicableMigrationInstructions(latestDataVersion, classes);

    // execute the migration
    executeMigrationInstructions(orderedMapOfMigrationInstructions);

    // Update the database with the version if there was no
    String postMigrationDataVersion = getLatestDataVersion();
    if (!isVersionGreaterThenCurrentVersion(postMigrationDataVersion, currentKlawVersion)) {
      updateDataVersionInDB(currentKlawVersion);
    }
  }

  private String getLatestDataVersion() {
    DataVersion latestDataVersion = versionRepo.findTopByOrderByIdDesc();
    if (latestDataVersion == null || latestDataVersion.getVersion() == null) {
      // If there is no data version we assume a clean system
      return "0.0.0";
    } else {
      return latestDataVersion.getVersion();
    }
  }

  private void updateDataVersionInDB(String version) {
    DataVersion latestDataVersion = new DataVersion();
    latestDataVersion.setVersion(currentKlawVersion);
    latestDataVersion.setExecutedAt(Timestamp.from(Instant.now()));
    latestDataVersion.setComplete(true);
    versionRepo.save(latestDataVersion);
  }

  /**
   * Removes any migration instructions not applicable to this upgrade. Orders the data migration
   * instructions in the order intended to maintain data integrity.
   *
   * @param currentDataVersion The version of data that is currently deployed
   * @param classes A set of Migration Instructions for migrating data.
   * @return A sorted map that has removed any unnecessary instructions and ordered the rest.
   */
  private SortedMap<Integer, Pair<String, Class<?>>> orderApplicableMigrationInstructions(
      String currentDataVersion, Set<Class<?>> classes) {
    SortedMap<Integer, Pair<String, Class<?>>> orderedMapOfMigrationInstructions = new TreeMap<>();
    log.info("Classes discovered {}, number of classes {}", classes, classes.size());
    // order the Migration classes and remove any instructions from previous releases that have
    // already been executed.
    classes.forEach(
        migrate -> {
          DataMigration migration = migrate.getAnnotation(DataMigration.class);
          if (isVersionGreaterThenCurrentVersion(currentDataVersion, migration.version())) {
            // successfully run data migration.
            Pair<String, Class<?>> pair = Pair.of(migration.version(), migrate);

            orderedMapOfMigrationInstructions.put(migration.order(), pair);
          }
        });
    return orderedMapOfMigrationInstructions;
  }

  /**
   * @param orderedMapOfMigrationInstructions The ordered list of data migration instruction
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws KlawDataMigrationException If any of the instructions fail to complete their migration
   *     task an exception is thrown with information on the failed instruction set.
   */
  private void executeMigrationInstructions(
      SortedMap<Integer, Pair<String, Class<?>>> orderedMapOfMigrationInstructions)
      throws IllegalAccessException, InvocationTargetException, KlawDataMigrationException,
          NoSuchMethodException, ClassNotFoundException {
    for (Integer value : orderedMapOfMigrationInstructions.keySet()) {
      Class<?> runner = orderedMapOfMigrationInstructions.get(value).getRight();
      for (Method method : runner.getDeclaredMethods()) {
        // Find the correct method to invoke to execute the migration instructions.
        if (method.isAnnotationPresent(MigrationRunner.class)) {

          Object bd = context.getBean(getNameFromClass(runner));

          Object statusObj = method.invoke(bd);

          Boolean status = (Boolean) statusObj;
          // If not completed successfully do not continue
          log.info("Execution of {} with status {}", bd.getClass(), status);

          if (!status) {
            throw new KlawDataMigrationException(
                String.format(MIGRATION_ERR_101, runner.getCanonicalName()));
          } else {
            // update table that this version completed.
            updateDataVersionInDB(orderedMapOfMigrationInstructions.get(value).getLeft());
          }
        }
      }
    }
  }

  /**
   * This class expects that the instantiated name of the class is the same as the declared name of
   * the class if case is ignored.
   *
   * @param runner The name of the class including the package
   * @return The instantiated name of the class.
   */
  private String getNameFromClass(Class<?> runner) {

    String str = runner.getName();
    str = str.substring(runner.getPackageName().length() + 1);

    String[] instantiatedBeans = context.getBeanNamesForAnnotation(DataMigration.class);
    // Get Instantiated Bean name of the correct type.
    for (String beanStr : instantiatedBeans) {
      if (str.equalsIgnoreCase(beanStr)) {
        return beanStr;
      }
    }
    return str;
  }

  /**
   * @param currentVersion The Current Data Version that Klaw has in the database.
   * @param comparedVersion The version of the migration instructions which have been found.
   * @return false if the compared version is greater than the current version. true if it is less
   *     than or equal to the current version.
   */
  private boolean isVersionGreaterThenCurrentVersion(
      String currentVersion, String comparedVersion) {

    if (currentVersion.equals(comparedVersion)) {
      return true;
    }

    String[] currentVersionParts = currentVersion.split("\\.");
    String[] compareVersionParts = comparedVersion.split("\\.");
    // currently klaw supports a 3 part version number.
    for (int i = 0; i < SUPPORTED_KLAW_VERSION_NUMBER_SYSTEM; i++) {
      if (Integer.parseInt(currentVersionParts[i]) < Integer.parseInt(compareVersionParts[i])) {
        return true;
      }
    }

    return false;
  }
}
