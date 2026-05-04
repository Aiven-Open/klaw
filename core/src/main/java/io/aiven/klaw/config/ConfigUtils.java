package io.aiven.klaw.config;

import io.aiven.klaw.auth.KwAuthenticationFailureHandler;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/*
- Provide static resources to be loaded, required by the application
- Apply HttpSecurity configs
 */
public class ConfigUtils {
  protected static AntPathRequestMatcher[] getStaticResources(boolean coralEnabled) {

    List<String> staticResourcesHtmlArray =
        new ArrayList<>(
            List.of(
                "/assets/**",
                "/lib/**",
                "/js/**",
                "/home",
                "/home/**",
                "/register**",
                "/authenticate",
                "/login**",
                "/terms**",
                "/registrationReview**",
                "/forgotPassword",
                "/getDbAuth",
                "/feedback**",
                "/reset/token",
                "/reset/password",
                "/getRoles",
                "/getTenantsInfo",
                "/getBasicInfo",
                "/getAllTeamsSUFromRegisterUsers",
                "/registerUser",
                "/resetMemoryCache/**",
                "/getActivationInfo**",
                "/v3/**",
                "/cache/**"));

    if (coralEnabled) {
      staticResourcesHtmlArray.add("/assets/coral/**");
    }

    AntPathRequestMatcher[] antPathRequestMatchersArray =
        new AntPathRequestMatcher[staticResourcesHtmlArray.size()];
    int i = 0;
    for (String s : staticResourcesHtmlArray) {
      antPathRequestMatchersArray[i] = new AntPathRequestMatcher(s);
      i++;
    }

    return antPathRequestMatchersArray;
  }

  protected static void applyHttpSecurityConfig(
      HttpSecurity http,
      boolean coralEnabled,
      KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler,
      KwAuthenticationFailureHandler kwAuthenticationFailureHandler)
      throws Exception {
    http.csrf(
            csrf -> {
              csrf.ignoringRequestMatchers("/logout");
              csrf.ignoringRequestMatchers("/resetMemoryCache/**"); // Internal service endpoint
              csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
              csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
            })
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(getStaticResources(coralEnabled))
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauthLogin ->
                oauthLogin
                    .successHandler(kwAuthenticationSuccessHandler)
                    .failureHandler(kwAuthenticationFailureHandler)
                    .failureUrl("/login?error")
                    .loginPage("/login")
                    .permitAll())
        .logout(
            logout ->
                logout
                    .invalidateHttpSession(true)
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .addLogoutHandler(
                        new HeaderWriterLogoutHandler(
                            new ClearSiteDataHeaderWriter(
                                ClearSiteDataHeaderWriter.Directive.CACHE,
                                ClearSiteDataHeaderWriter.Directive.COOKIES,
                                ClearSiteDataHeaderWriter.Directive.STORAGE))));
  }
}
