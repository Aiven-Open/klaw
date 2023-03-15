package io.aiven.klaw.config;

import io.aiven.klaw.dao.DataVersion;
import io.aiven.klaw.error.KlawDataMigrationException;
import io.aiven.klaw.repository.DataVersionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {io.aiven.klaw.dao.migration.test.MigrationTestData2x1x0.class,io.aiven.klaw.dao.migration.test.MigrationTestData2x1x0.class})
@ExtendWith(SpringExtension.class)
class MigrationUtilityTest {


    MigrationUtility utility;

    @Mock
    private DataVersionRepo versionRepo;

    private final static String KLAW_VERSION = "2.2.0";
    private final static String PACKAGE_TO_SCAN = "io.aiven.klaw.dao.migration.test";

    private final static String PROD_PACKAGE_TO_SCAN = "io.aiven.klaw.dao.migration";

    @Captor
    ArgumentCaptor<DataVersion> dataVersionCaptor;

    @BeforeEach
    public void setup() {
        utility = new MigrationUtility();
        ReflectionTestUtils.setField(utility, "versionRepo", versionRepo);
        ReflectionTestUtils.setField(utility, "currentKlawVersion", KLAW_VERSION);
        ReflectionTestUtils.setField(utility, "packageToScan", PACKAGE_TO_SCAN);
    }

    @Test
    public void VersionIsAlreadyAtLatest_NoMigration() throws KlawDataMigrationException, InvocationTargetException, IllegalAccessException {
        when(versionRepo.findTopByOrderByIdDesc()).thenReturn(getDataVersion(KLAW_VERSION));

        utility.migrate();
        verify(versionRepo,times(0)).save(any());
    }

    @Test
    public void VersionIsNotAtLatest_MigrateData() throws KlawDataMigrationException, InvocationTargetException, IllegalAccessException {
        when(versionRepo.findTopByOrderByIdDesc()).thenReturn(getDataVersion("1.0.0"));

        utility.migrate();
        verify(versionRepo,times(2)).save(dataVersionCaptor.capture());
    }

    @Test
    public void NoExistingVersionInDatabase_MigrateData() throws KlawDataMigrationException, InvocationTargetException, IllegalAccessException {
        when(versionRepo.findTopByOrderByIdDesc()).thenReturn(null);

        utility.migrate();
        verify(versionRepo,times(2)).save(dataVersionCaptor.capture());


    }

    private DataVersion getDataVersion(String version) {
        DataVersion dataVersion = new DataVersion();
        dataVersion.setVersion(version);
        dataVersion.setComplete(true);
        dataVersion.setExecutedAt(Timestamp.from(Instant.now()));
        return dataVersion;
    }

}