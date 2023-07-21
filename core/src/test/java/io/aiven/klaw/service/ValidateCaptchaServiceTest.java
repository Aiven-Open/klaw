package io.aiven.klaw.service;

import io.aiven.klaw.constants.TestConstants;
import io.aiven.klaw.model.CaptchaResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ValidateCaptchaServiceTest {

  @Mock private RestTemplateBuilder templateBuilder;
  @Mock private RestTemplate template;
  private ValidateCaptchaService validateCaptchaService;

  @BeforeEach
  public void setUp() {
    Mockito.when(templateBuilder.build()).thenReturn(template);
    validateCaptchaService = new ValidateCaptchaService(templateBuilder);
    ReflectionTestUtils.setField(validateCaptchaService, "validateRecaptcha", true);
    ReflectionTestUtils.setField(
        validateCaptchaService,
        "recaptchaEndpoint",
        "https://www.google.com/recaptcha/api/siteverify");
    ReflectionTestUtils.setField(validateCaptchaService, "recaptchaSecret", "secret");
  }

  @Test
  public void validateCaptcha() {
    CaptchaResponse response = new CaptchaResponse();
    response.setSuccess(true);

    Mockito.when(
            template.postForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.eq(CaptchaResponse.class)))
        .thenReturn(response);

    Assertions.assertTrue(validateCaptchaService.validateCaptcha(TestConstants.CAPTCHA_RESPONSE));
  }

  @Test
  public void validateCaptcha_Failure() {
    CaptchaResponse response = new CaptchaResponse();
    response.setSuccess(true);

    Mockito.when(
            template.postForObject(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyMap(),
                ArgumentMatchers.eq(CaptchaResponse.class)))
        .thenThrow(new RestClientException(""));

    Assertions.assertFalse(validateCaptchaService.validateCaptcha(TestConstants.CAPTCHA_RESPONSE));
  }

  @Test
  public void validateCaptcha_ValidationDisabled() {
    ReflectionTestUtils.setField(validateCaptchaService, "validateRecaptcha", false);

    Assertions.assertTrue(validateCaptchaService.validateCaptcha(TestConstants.CAPTCHA_RESPONSE));
  }
}
