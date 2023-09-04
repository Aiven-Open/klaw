package io.aiven.klaw.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KwAuthenticationSuccessHandlerTest {

  @Mock HttpServletRequest httpServletRequest;

  @Mock HttpSession httpSession;

  @Mock DefaultSavedRequest defaultSavedRequest;

  private KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler;
  String springSavedReqAttribute;

  @BeforeEach
  void setUp() {
    springSavedReqAttribute = "SPRING_SECURITY_SAVED_REQUEST";
    kwAuthenticationSuccessHandler = new KwAuthenticationSuccessHandler();
  }

  @AfterEach
  void tearDown() {}

  @Test
  void getRedirectPageWhenSavedReqIsNullReturnIndexTest() {
    when(httpServletRequest.getSession()).thenReturn(httpSession);
    defaultSavedRequest = null;
    when(httpSession.getAttribute(springSavedReqAttribute)).thenReturn(defaultSavedRequest);
    String redirectedPage =
        kwAuthenticationSuccessHandler.getRedirectPage(httpServletRequest, null);
    assertThat(redirectedPage).isEqualTo("index");
  }

  @Test
  void getRedirectPageReturnIndexLoginTest() {
    when(httpServletRequest.getSession()).thenReturn(httpSession);
    when(httpSession.getAttribute(springSavedReqAttribute)).thenReturn(defaultSavedRequest);
    when(defaultSavedRequest.getRequestURI()).thenReturn("/login");
    String redirectedPage =
        kwAuthenticationSuccessHandler.getRedirectPage(httpServletRequest, null);
    assertThat(redirectedPage).isEqualTo("index");
  }

  @Test
  void getRedirectPageReturnRootForServletPathCheckTest() {
    when(httpServletRequest.getSession()).thenReturn(httpSession);
    when(httpSession.getAttribute(springSavedReqAttribute)).thenReturn(defaultSavedRequest);
    when(defaultSavedRequest.getServletPath()).thenReturn("/{{ provider }}");
    String redirectedPage =
        kwAuthenticationSuccessHandler.getRedirectPage(httpServletRequest, null);
    assertThat(redirectedPage).isEqualTo("/");
  }

  @Test
  void getRedirectPageReturnFullPathRequestUriQueryTest() {
    when(httpServletRequest.getSession()).thenReturn(httpSession);
    when(httpSession.getAttribute(springSavedReqAttribute)).thenReturn(defaultSavedRequest);
    when(defaultSavedRequest.getRequestURI()).thenReturn("/browseTopics");
    when(defaultSavedRequest.getQueryString()).thenReturn("topicName=testtopic");
    String redirectedPage =
        kwAuthenticationSuccessHandler.getRedirectPage(httpServletRequest, null);
    assertThat(redirectedPage).isEqualTo("/browseTopics?topicName=testtopic");
  }
}
