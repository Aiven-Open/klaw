package io.aiven.klaw.controller;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;

@ConditionalOnProperty(name = "klaw.enable.sso", havingValue = "true")
@Controller
@Slf4j
public class ResourceClientController {

  @Value("${resourceserver.api.url:testResourceUrl}")
  private String resourceUrl;

  @Value("${spring.security.oauth2.client.provider.klaw.user-info-uri:testResourceUrl}")
  private String userInfoUri;

  @Autowired private OAuth2AuthorizedClientService authorizedClientService;

  @Autowired private WebClient webClient;

  @GetMapping("/resources")
  public String getFoos(
      Model model,
      OAuth2AuthenticationToken authentication,
      HttpServletRequest request,
      HttpServletResponse response) {
    String foos =
        this.webClient
            .get()
            .uri(resourceUrl)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<String>() {})
            .block();
    return checkAnonymousLogin(request, response, authentication);
  }

  private String checkAnonymousLogin(
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken authentication) {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) principal;
      OAuth2AuthorizedClient client =
          authorizedClientService.loadAuthorizedClient(
              authentication.getAuthorizedClientRegistrationId(),
              (String) defaultOAuth2User.getAttributes().get("preferred_username"));
      if (client == null) {
        return ("redirect:oauthLogin");
      }
      return ("redirect:index");
    } catch (Exception e) {
      return ("redirect:oauthLogin");
    }
  }

  private static final String authorizationRequestBaseUri = "oauth2/authorize-client";
  Map<String, String> oauth2AuthenticationUrls = new HashMap<>();

  @Autowired private ClientRegistrationRepository clientRegistrationRepository;

  @GetMapping("/oauthLogin")
  public String getLoginPage(
      Model model, OAuth2AuthenticationToken authentication, HttpServletResponse response) {
    Iterable<ClientRegistration> clientRegistrations = null;
    ResolvableType type =
        ResolvableType.forInstance(clientRegistrationRepository).as(Iterable.class);
    if (type != ResolvableType.NONE
        && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
      clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
    }

    clientRegistrations.forEach(
        registration ->
            oauth2AuthenticationUrls.put(
                registration.getClientName(),
                authorizationRequestBaseUri + "/" + registration.getRegistrationId()));
    model.addAttribute("urls", oauth2AuthenticationUrls);
    return "oauthLogin";
  }
}
