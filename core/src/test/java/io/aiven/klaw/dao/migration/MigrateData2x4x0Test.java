package io.aiven.klaw.dao.migration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.dao.test.MigrationTestData2x1x0;
import io.aiven.klaw.dao.test.MigrationTestData2x2x0;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MigrateData2x4x0Test {

  private MigrateData2x4x0 migrateData2x4x0;

  @Mock private SelectDataJdbc selectDataJdbc;

  @Mock private InsertDataJdbc insertDataJdbc;

  @Mock private ManageDatabase manageDatabase;

  @Bean
  public MigrationTestData2x1x0 MigrateTestData2x1x0() {
    return new MigrationTestData2x1x0();
  }

  @Bean
  public MigrationTestData2x2x0 MigrationTestData2x1x0() {
    return new MigrationTestData2x2x0();
  }

  @Captor ArgumentCaptor<List<KwProperties>> propertiesArgumentCaptor;

  @BeforeEach
  public void setUp() {
    migrateData2x4x0 = new MigrateData2x4x0(selectDataJdbc, insertDataJdbc, manageDatabase);
  }

  @Test
  public void givenNoTenantsDoNotMigrateAnyData() {

    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(0));

    boolean success = migrateData2x4x0.migrate();
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    // No other calls made
    verify(insertDataJdbc, times(0)).addNewEnv(any(Env.class));
    verify(selectDataJdbc, times(0)).selectAllEnvs(any(KafkaClustersType.class), anyInt());
    //    called once per tenant
    verify(manageDatabase, times(0)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantAndNoPropertiesMigrateEmptyListData() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));

    when(selectDataJdbc.selectAllKwPropertiesPerTenant(anyInt()))
        .thenReturn(Collections.EMPTY_LIST);

    // Execute
    boolean success = migrateData2x4x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1)).selectAllKwPropertiesPerTenant(anyInt());

    verify(insertDataJdbc, times(1)).insertDefaultKwProperties(eq(Collections.EMPTY_LIST));
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();
  }

  @Test
  public void givenOneTenantAndNoDisabledPropertiesMigrateData_NoDisabledProperties() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));

    when(selectDataJdbc.selectAllKwPropertiesPerTenant(anyInt())).thenReturn(getProperties(8));

    // Execute
    boolean success = migrateData2x4x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1)).selectAllKwPropertiesPerTenant(anyInt());

    verify(insertDataJdbc, times(1)).insertDefaultKwProperties(propertiesArgumentCaptor.capture());
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();

    List<KwProperties> properties = propertiesArgumentCaptor.getValue();
    assertThat(properties.size()).isEqualTo(8);
    for (KwProperties prop : properties) {

      assertThat(prop.isEnabled()).isTrue();
    }
  }

  @Test
  public void givenOneTenantAndMatchingDisabledPropertiesMigrateData_disabledProperties() {
    // Setup
    when(selectDataJdbc.selectAllUsersAllTenants()).thenReturn(getUserAndTenantInfo(1));
    List<KwProperties> returnProperties = getProperties(8);
    KwProperties disableProp = new KwProperties();
    disableProp.setTenantId(101);
    disableProp.setKwValue("added value");
    disableProp.setKwKey("klaw.superuser.mailid");
    returnProperties.add(disableProp);

    when(selectDataJdbc.selectAllKwPropertiesPerTenant(anyInt())).thenReturn(returnProperties);

    // Execute
    boolean success = migrateData2x4x0.migrate();

    // Verify
    verify(selectDataJdbc, times(1)).selectAllUsersAllTenants();
    verify(selectDataJdbc, times(1)).selectAllKwPropertiesPerTenant(anyInt());

    verify(insertDataJdbc, times(1)).insertDefaultKwProperties(propertiesArgumentCaptor.capture());
    //    called once per tenant
    verify(manageDatabase, times(1)).loadEnvMapForOneTenant(anyInt());
    assertThat(success).isTrue();

    List<KwProperties> properties = propertiesArgumentCaptor.getValue();
    assertThat(properties.size()).isEqualTo(9);
    for (KwProperties prop : properties) {
      if (prop.getKwKey().equalsIgnoreCase("klaw.superuser.mailid")) {
        assertThat(prop.isEnabled()).isFalse();
      } else {
        assertThat(prop.isEnabled()).isTrue();
      }
    }
  }

  private List<UserInfo> getUserAndTenantInfo(int numberOfEntries) {
    List<UserInfo> users = new ArrayList<>();
    for (int i = 0; i < numberOfEntries; i++) {
      UserInfo info = new UserInfo();
      info.setTenantId(101 + i);
      info.setTeamId(1 + i);
      info.setRole("User");
      info.setUsername("User" + i);
      info.setFullname("User" + i + " LastName");
      info.setSwitchTeams(false);
      users.add(info);
    }

    return users;
  }

  private List<KwProperties> getProperties(int numberOfEntries) {
    List<KwProperties> kwProperties = new ArrayList<>();
    for (int i = 0; i < numberOfEntries; i++) {
      KwProperties prop = new KwProperties();
      prop.setTenantId(101);
      prop.setKwKey("klaw.config.version." + i);
      prop.setKwValue(String.valueOf(i));

      kwProperties.add(prop);
    }

    return kwProperties;
  }
}
