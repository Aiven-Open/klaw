package io.aiven.klaw.dao.migration;

import static io.aiven.klaw.service.MailUtils.NEW_USER_ADDED_V2_KEY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.service.DefaultDataService;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class MigrateData2x10x0Test {

  public static final int TENANT_ID = 101;
  private MigrateData2x10x0 migrateData2x10x0;

  @Mock private SelectDataJdbc selectDataJdbc;

  @Mock private ManageDatabase manageDatabase;
  @Mock private InsertDataJdbc insertDataJdbc;

  DefaultDataService dataService = new DefaultDataService();

  private UtilMethods utilMethods;

  @Captor private ArgumentCaptor<List<KwProperties>> kwPropertiesCaptor;

  @BeforeEach
  public void setUp() {
    migrateData2x10x0 = new MigrateData2x10x0(selectDataJdbc, manageDatabase, insertDataJdbc);
    utilMethods = new UtilMethods();
  }

  @Test
  public void addEntryToPropertiesWhenItDoesNotAlreadyExist() {

    when(selectDataJdbc.selectAllKwPropertiesPerTenant(TENANT_ID))
        .thenReturn(getKWProperties(TENANT_ID, List.of(NEW_USER_ADDED_V2_KEY)));
    when(selectDataJdbc.getTenants()).thenReturn(utilMethods.getTenants());

    boolean success = migrateData2x10x0.migrate();
    verify(insertDataJdbc, times(1)).insertDefaultKwProperties(kwPropertiesCaptor.capture());
    assertThat(kwPropertiesCaptor.getValue().size()).isEqualTo(1);
    kwPropertiesCaptor.getValue().stream()
        .allMatch(prop -> Objects.equals(prop.getKwKey(), NEW_USER_ADDED_V2_KEY));
    assertThat(success).isTrue();
  }

  @Test
  public void skipAddEntryToPropertiesWhenItDoesAlreadyExist() {
    when(selectDataJdbc.selectAllKwPropertiesPerTenant(TENANT_ID))
        .thenReturn(getKWProperties(TENANT_ID, List.of()));
    when(selectDataJdbc.getTenants()).thenReturn(utilMethods.getTenants());

    boolean success = migrateData2x10x0.migrate();
    verify(insertDataJdbc, times(0)).insertDefaultKwProperties(any());
    assertThat(success).isTrue();
  }

  /**
   * @param propertyKeys A specific set of properties to filter from the existing list of kw
   *     properties
   * @return A list of kwProperties
   */
  private List<KwProperties> getKWProperties(int tenantId, List<String> propertyKeys) {
    dataService = new DefaultDataService();
    return dataService.createDefaultProperties(tenantId, null).stream()
        .filter(kwProperty -> !propertyKeys.contains(kwProperty.getKwKey()))
        .toList();
  }
}
