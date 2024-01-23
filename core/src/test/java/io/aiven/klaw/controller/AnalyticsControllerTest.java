package io.aiven.klaw.controller;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.service.AnalyticsControllerService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnalyticsControllerTest {

  private AnalyticsController controller;

  @Mock private AnalyticsControllerService chartsProcessor;

  @BeforeEach
  public void setup() {
    controller = new AnalyticsController();
    ReflectionTestUtils.setField(controller, "chartsProcessor", chartsProcessor);
  }

  @ParameterizedTest
  @CsvSource({"-1", "91", "92", "92", "-2", "0"})
  public void checkAnalyticsControllerThrowsException(String numberOfDays) {
    assertThatThrownBy(
            () -> {
              controller.getActivityLogForTeamOverview("true", Integer.parseInt(numberOfDays));
            })
        .isInstanceOf(KlawBadRequestException.class);
  }

  @ParameterizedTest
  @CsvSource({"1", "81", "72", "67", "2", "8"})
  public void checkAnalyticsController(String numberOfDays) throws KlawBadRequestException {

    controller.getActivityLogForTeamOverview("true", Integer.parseInt(numberOfDays));
    verify(chartsProcessor, times(1))
        .getActivityLogForTeamOverview("true", Integer.parseInt(numberOfDays));
  }
}
