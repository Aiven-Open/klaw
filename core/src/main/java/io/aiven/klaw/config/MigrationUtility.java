package io.aiven.klaw.config;

import io.aiven.klaw.dao.DataVersion;
import io.aiven.klaw.dao.migration.DataMigration;
import io.aiven.klaw.dao.migration.MigrationRunner;
import io.aiven.klaw.error.KlawDataMigrationException;
import io.aiven.klaw.repository.DataVersionRepo;
import jakarta.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @PostConstruct
  public void migrate()
      throws InvocationTargetException, IllegalAccessException, KlawDataMigrationException {

    // Find the latest version in DB
    DataVersion currentDataVersion = versionRepo.findTopByOrderByIdDesc();
    if (isVersionGreaterThenOrEqualToCurrentVersion(
        currentDataVersion.getVersion(), currentKlawVersion)) {
      log.info(
          "Current Data Version {} is greater then or equal to the Klaw version {}, no action needed.",
          currentDataVersion.getVersion(),
          currentKlawVersion);
      return;
    }

    // Find all DataMigration annotated classes.// Migrate each one
    Reflections reflections = new Reflections(packageToScan);
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(DataMigration.class);

    SortedMap<Integer, Pair<String, Class<?>>> orderedMapOfMigrationInstructions =
        orderApplicableMigrationInstructions(currentDataVersion, classes);

    // execute the migration
    executeMigrationInstructions(orderedMapOfMigrationInstructions);

    // Update the database with the version
    // need to see if already created.
    updateDataVersionInDB(currentKlawVersion);
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
      DataVersion currentDataVersion, Set<Class<?>> classes) {
    SortedMap<Integer, Pair<String, Class<?>>> orderedMapOfMigrationInstructions = new TreeMap<>();
    log.info("Classes discovered {}, number of classes {}", classes, classes.size());
    // order the Migration classes and remove any instructions from previous releases that have
    // already been executed.
    classes.forEach(
        migrate -> {
          DataMigration migration = migrate.getAnnotation(DataMigration.class);
          if (isVersionGreaterThenOrEqualToCurrentVersion(
              currentDataVersion.getVersion(), migration.version())) {
            // TODO add the version and class as a tuple that way we can set the DB for each
            // successfully run data migration.
            Pair<String, Class<?>> pair = Pair.of(migration.version(), migrate);

            orderedMapOfMigrationInstructions.put(migration.order(), pair);
          }
        });
    return orderedMapOfMigrationInstructions;
  }

  /**
   * @param orderedMapOfMigrationInstructions The ordered list of data migration instructions
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws KlawDataMigrationException If any of the instructions fail to complete their migration
   *     task an exception is thrown with information on the failed instruction set.
   */
  private void executeMigrationInstructions(
      SortedMap<Integer, Pair<String, Class<?>>> orderedMapOfMigrationInstructions)
      throws IllegalAccessException, InvocationTargetException, KlawDataMigrationException {
    for (Integer value : orderedMapOfMigrationInstructions.keySet()) {
      Class<?> runner = orderedMapOfMigrationInstructions.get(value).getRight();
      for (Method method : runner.getDeclaredMethods()) {
        // Find the correct method to invoke to execute the migration instructions.
        if (method.isAnnotationPresent(MigrationRunner.class)) {
          boolean status = (boolean) method.invoke(runner);

          // If not completed successfully do not continue
          if (!status) {
            throw new KlawDataMigrationException(
                "Unable to complete Migration instructions successfully from "
                    + runner.getCanonicalName());
          } else {
            // update table that this version completed.
            updateDataVersionInDB(orderedMapOfMigrationInstructions.get(value).getLeft());
          }
        }
      }
    }
  }

  /**
   * @param currentVersion The Current Data Version that Klaw has in the database.
   * @param comparedVersion The version of the migration instructions which have been found.
   * @return true if the compared version is greater then or equal to the current version. false if
   *     it is less then the current version.
   */
  private boolean isVersionGreaterThenOrEqualToCurrentVersion(
      String currentVersion, String comparedVersion) {

    String[] currentVersionParts = currentVersion.split("\\.");
    String[] compareVersionParts = comparedVersion.split("\\.");
    // currently klaw supports a 3 part version number.
    for (int i = 0; i < SUPPORTED_KLAW_VERSION_NUMBER_SYSTEM; i++) {
      if (Integer.parseInt(currentVersionParts[i]) < Integer.parseInt(compareVersionParts[i])) {
        return false;
      }
    }

    return true;
  }
}
