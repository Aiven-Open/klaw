package io.aiven.klaw.dao.migration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.model.enums.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MigrateData2x5x0Test {

  private MigrateData2x5x0 migrateData2x5x0;

  @Mock private SelectDataJdbc selectDataJdbc;

  @Mock private InsertDataJdbc insertDataJdbc;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    migrateData2x5x0 = new MigrateData2x5x0(selectDataJdbc, insertDataJdbc);
    utilMethods = new UtilMethods();
  }

  @Test
  public void addSequencesWhenNoData() {
    when(selectDataJdbc.getDataFromKwEntitySequences()).thenReturn(0L);
    when(selectDataJdbc.getTenants()).thenReturn(utilMethods.getTenants());
    when(selectDataJdbc.getNextClusterId(anyInt())).thenReturn(null);
    when(selectDataJdbc.getNextEnvId(anyInt())).thenReturn(null);
    when(selectDataJdbc.getNextTeamId(anyInt())).thenReturn(null);

    boolean success = migrateData2x5x0.migrate();
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.CLUSTER.name()), eq(101), eq(101));
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.ENVIRONMENT.name()), eq(101), eq(101));
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.TEAM.name()), eq(101), eq(101));
    assertThat(success).isTrue();
  }

  @Test
  public void addSequencesWhenDataAvailable() {
    when(selectDataJdbc.getDataFromKwEntitySequences()).thenReturn(0L);
    when(selectDataJdbc.getTenants()).thenReturn(utilMethods.getTenants());
    when(selectDataJdbc.getNextClusterId(anyInt())).thenReturn(101);
    when(selectDataJdbc.getNextEnvId(anyInt())).thenReturn(102);
    when(selectDataJdbc.getNextTeamId(anyInt())).thenReturn(1003);

    boolean success = migrateData2x5x0.migrate();
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.CLUSTER.name()), eq(102), eq(101));
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.ENVIRONMENT.name()), eq(103), eq(101));
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.TEAM.name()), eq(1004), eq(101));
    assertThat(success).isTrue();
  }

  @Test
  public void addSequencesWhenSequencesUpdatedAlready() {
    when(selectDataJdbc.getDataFromKwEntitySequences()).thenReturn(3L);
    when(selectDataJdbc.getTenants()).thenReturn(utilMethods.getTenants());
    when(selectDataJdbc.getNextClusterId(anyInt())).thenReturn(101);
    when(selectDataJdbc.getNextEnvId(anyInt())).thenReturn(102);
    when(selectDataJdbc.getNextTeamId(anyInt())).thenReturn(1003);

    boolean success = migrateData2x5x0.migrate();
    verify(insertDataJdbc, times(0))
        .insertIntoKwEntitySequence(eq(EntityType.CLUSTER.name()), eq(102), eq(101));
    verify(insertDataJdbc, times(0))
        .insertIntoKwEntitySequence(eq(EntityType.ENVIRONMENT.name()), eq(103), eq(101));
    verify(insertDataJdbc, times(0))
        .insertIntoKwEntitySequence(eq(EntityType.TEAM.name()), eq(1004), eq(101));
    assertThat(success).isTrue();
  }

  @Test
  public void addSequencesFailWhenSequencesUpdatedAlready() {
    when(selectDataJdbc.getDataFromKwEntitySequences())
        .thenReturn(2L); // only 2 recs found instead of 3 (CLUSTER, ENV, TEAM)
    when(selectDataJdbc.getTenants()).thenReturn(utilMethods.getTenants());
    when(selectDataJdbc.getNextClusterId(anyInt())).thenReturn(101);
    when(selectDataJdbc.getNextEnvId(anyInt())).thenReturn(102);
    when(selectDataJdbc.getNextTeamId(anyInt())).thenReturn(1003);

    boolean success = migrateData2x5x0.migrate();
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.CLUSTER.name()), eq(102), eq(101));
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.ENVIRONMENT.name()), eq(103), eq(101));
    verify(insertDataJdbc, times(1))
        .insertIntoKwEntitySequence(eq(EntityType.TEAM.name()), eq(1004), eq(101));
    assertThat(success).isTrue();
  }
}
