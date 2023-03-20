package io.aiven.klaw.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.dao.DataVersion;
import io.aiven.klaw.dao.migration.DataMigration;
import io.aiven.klaw.dao.migration.MigrationRunner;
import io.aiven.klaw.dao.test.MigrationTestData2x1x0;
import io.aiven.klaw.dao.test.MigrationTestData2x2x0;
import io.aiven.klaw.error.KlawDataMigrationException;
import io.aiven.klaw.repository.DataVersionRepo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.reflections.Reflections;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(classes = {MigrationTestData2x1x0.class, MigrationTestData2x2x0.class})
@ExtendWith(SpringExtension.class)
@Slf4j
class MigrationUtilityTest {

  MigrationUtility utility;

  @Mock private DataVersionRepo versionRepo;

  private static final String KLAW_VERSION = "2.2.0";
  private static final String PACKAGE_TO_SCAN = "io.aiven.klaw.dao.test";

  private static final String PROD_PACKAGE_TO_SCAN = "io.aiven.klaw.dao.migration";

  @Captor ArgumentCaptor<DataVersion> dataVersionCaptor;

  private MigrationTestData2x1x0 m1 = new MigrationTestData2x1x0();
  private MigrationTestData2x2x0 m2 = new MigrationTestData2x2x0();

  @Mock ApplicationContext context;

  @BeforeEach
  public void setup() {
    utility = new MigrationUtility();
    ReflectionTestUtils.setField(utility, "versionRepo", versionRepo);
    ReflectionTestUtils.setField(utility, "currentKlawVersion", KLAW_VERSION);
    ReflectionTestUtils.setField(utility, "context", context);
    ReflectionTestUtils.setField(utility, "packageToScan", PACKAGE_TO_SCAN);
  }

  @Test
  public void VersionIsAlreadyAtLatest_NoMigration()
      throws KlawDataMigrationException, InvocationTargetException, IllegalAccessException,
          NoSuchMethodException, ClassNotFoundException {
    when(versionRepo.findTopByOrderByIdDesc()).thenReturn(getDataVersion(KLAW_VERSION));

    utility.migrate();
    verify(versionRepo, times(0)).save(any());
  }

  @Test
  public void VersionIsNotAtLatest_MigrateData()
      throws KlawDataMigrationException, InvocationTargetException, IllegalAccessException,
          NoSuchMethodException, ClassNotFoundException {
    when(versionRepo.findTopByOrderByIdDesc()).thenReturn(getDataVersion("1.0.0"));
    when(context.getBeanNamesForAnnotation(eq(DataMigration.class)))
        .thenReturn(new String[] {"migrationTestData2x1x0", "migrationTestData2x2x0"});
    when(context.getBean(eq("migrationTestData2x1x0"))).thenReturn(m1);
    when(context.getBean(eq("migrationTestData2x2x0"))).thenReturn(m2);

    ReflectionTestUtils.setField(m1, "success", true);
    ReflectionTestUtils.setField(m2, "success", true);

    utility.migrate();
    verify(versionRepo, times(2)).save(dataVersionCaptor.capture());
  }

  @Test
  public void NoExistingVersionInDatabase_MigrateData()
      throws KlawDataMigrationException, InvocationTargetException, IllegalAccessException,
          NoSuchMethodException, ClassNotFoundException {
    when(versionRepo.findTopByOrderByIdDesc()).thenReturn(null);
    when(context.getBeanNamesForAnnotation(eq(DataMigration.class)))
        .thenReturn(new String[] {"migrationTestData2x1x0", "migrationTestData2x2x0"});
    when(context.getBean(eq("migrationTestData2x1x0"))).thenReturn(m1);
    when(context.getBean(eq("migrationTestData2x2x0"))).thenReturn(m2);

    ReflectionTestUtils.setField(m1, "success", true);
    ReflectionTestUtils.setField(m2, "success", true);

    utility.migrate();
    verify(versionRepo, times(2)).save(dataVersionCaptor.capture());
  }

  @Test
  public void CheckAllProdDataMigrationClassesHaveAMigrationRunner() {

    // Find all DataMigration annotated classes.// Migrate each one
    Reflections reflections = new Reflections(PROD_PACKAGE_TO_SCAN);
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(DataMigration.class);
    // When adding a new package you will need to increment this by 1.
    assertThat(classes.size()).isEqualTo(1);
    Set<Integer> uniqueOrder = new HashSet<>();
    // Check Order Numbers are correctly assigned
    classes.forEach(
        migrate -> {
          DataMigration migration = migrate.getAnnotation(DataMigration.class);
          if (!uniqueOrder.add(migration.order())) {
            throw new RuntimeException(
                "Unique Order not Maintained "
                    + migrate.getCanonicalName()
                    + " has duplicate order number : "
                    + migration.order());
          } else {
            log.info(migrate.getCanonicalName() + "has unique Order Number {}", migration.order());
          }
        });

    // Check Each Class has a migrationRunner.
    classes.forEach(
        runner -> {
          Boolean status = false;
          for (Method method : runner.getDeclaredMethods()) {

            // Find the correct method to invoke to execute the migration instructions.
            if (method.isAnnotationPresent(MigrationRunner.class)) {
              log.info(
                  "Method {} on class {} is correctly annotated.",
                  method.getName(),
                  runner.getName());
              // If not completed successfully do not continue
              status = true;
            }
          }
          if (!status) {
            throw new RuntimeException(
                "Unable to complete Migration instructions successfully from "
                    + runner.getCanonicalName());
          }
        });
  }

  private DataVersion getDataVersion(String version) {
    DataVersion dataVersion = new DataVersion();
    dataVersion.setVersion(version);
    dataVersion.setComplete(true);
    dataVersion.setExecutedAt(Timestamp.from(Instant.now()));
    return dataVersion;
  }
}
