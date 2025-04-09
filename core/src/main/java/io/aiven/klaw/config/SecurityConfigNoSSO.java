package io.aiven.klaw.config;

import static io.aiven.klaw.error.KlawErrorMessages.SEC_CONFIG_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.SEC_CONFIG_ERR_102;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;
import static io.aiven.klaw.service.UsersTeamsControllerService.UNUSED_PASSWD;

import io.aiven.klaw.auth.KwAuthenticationFailureHandler;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import io.aiven.klaw.dao.UserInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@EnableWebSecurity
@ConditionalOnProperty(name = "klaw.enable.sso", havingValue = "false")
@Slf4j
@Configuration
public class SecurityConfigNoSSO {

  @Autowired KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler;

  @Autowired KwAuthenticationFailureHandler kwAuthenticationFailureHandler;

  @Autowired private ManageDatabase manageTopics;

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${spring.ad.domain:}")
  private String adDomain;

  @Value("${spring.ad.url:}")
  private String adUrl;

  @Value("${spring.ad.rootDn:}")
  private String adRootDn;

  @Value("${spring.ad.filter:}")
  private String adFilter;

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.coral.enabled:false}")
  private boolean coralEnabled;

  @Value("${klaw.core.app2app.username:KlawApp2App}")
  private String apiUser;

  @Autowired LdapTemplate ldapTemplate;

  public static final String BCRYPT_ENCODING_ID = "{bcrypt}";

  private void shutdownApp() {
    // TODO
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationConfiguration authenticationConfiguration) throws Exception {

    http.csrf(
            csrf -> {
              csrf.ignoringRequestMatchers("/logout");
              csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
              csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
            })
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(ConfigUtils.getStaticResources(coralEnabled))
                    .permitAll()
                    .anyRequest()
                    .fullyAuthenticated())
        .formLogin(
            formLogin ->
                formLogin
                    .successHandler(kwAuthenticationSuccessHandler)
                    .failureHandler(kwAuthenticationFailureHandler)
                    .failureForwardUrl("/login?error")
                    .failureUrl("/login?error")
                    .loginPage("/login")
                    .permitAll())
        .logout(logout -> logout.logoutSuccessUrl("/login"))
        .authenticationManager(authenticationManager(authenticationConfiguration));

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {

    if (authenticationType != null && authenticationType.equals(ACTIVE_DIRECTORY.value)) {
      log.info("AD authentication configured.");
      log.info(
          "AD URL : {}, AD Domain : {}, AD Root DN : {}, AD Filter : {}",
          adUrl,
          adDomain,
          adRootDn,
          adFilter);
      return new ProviderManager(
          Collections.singletonList(activeDirectoryLdapAuthenticationProvider()));
    } else {
      return authenticationConfiguration.getAuthenticationManager();
    }
  }

  public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
    ActiveDirectoryLdapAuthenticationProvider provider =
        new ActiveDirectoryLdapAuthenticationProvider(adDomain, adUrl, adRootDn);
    provider.setConvertSubErrorCodesToExceptions(true);
    provider.setUseAuthenticationRequestCredentials(true);

    if (adFilter != null && !adFilter.equals("")) {
      provider.setSearchFilter(adFilter);
    }
    return provider;
  }

  @ConditionalOnProperty(name = "klaw.login.authentication.type", havingValue = "db")
  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() throws Exception {
    final Properties globalUsers = new Properties();
    if (authenticationType != null && authenticationType.equals(DATABASE.value)) {
      log.info("Db authentication configured.");
      log.debug("Loading all users !!");
      List<UserInfo> users;
      try {
        users = manageTopics.selectAllUsersInfo();
      } catch (Exception e) {
        log.error("Please check if tables are created.", e);
        shutdownApp();
        throw new Exception(SEC_CONFIG_ERR_102);
      }

      if (users.isEmpty()) {
        shutdownApp();
        throw new Exception(SEC_CONFIG_ERR_101);
      }

      loadAllUsers(globalUsers, users.iterator());
      globalUsers.put(apiUser, ",CACHE_ADMIN,enabled");
    }
    return new InMemoryUserDetailsManager(globalUsers);
  }

  private void loadAllUsers(Properties globalUsers, Iterator<UserInfo> iter) {
    UserInfo userInfo;
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    while (iter.hasNext()) {
      userInfo = iter.next();
      try {
        String secPwd = userInfo.getPwd();
        if (StringUtils.isEmpty(secPwd) || secPwd.equals(UNUSED_PASSWD)) {
          continue;
        }

        globalUsers.put(
            userInfo.getUsername(),
            getBcryptPassword(secPwd, encoder) + "," + userInfo.getRole() + ",enabled");
      } catch (Exception e) {
        log.error("Error : User not loaded {}. Check password.", userInfo.getUsername(), e);
      }
    }
  }

  /**
   * Temporary method until BCrypt migration is completed Currently Klaw supports two types of
   * encryption for database authentication but all use BCrypt at runtime, this method checks the
   * encryption type already being used and returns the BCrypt encrypted Password
   *
   * @param encodedPassword is the password from database already encoded
   * @return The BCrypt encoded password
   */
  private String getBcryptPassword(String encodedPassword, PasswordEncoder encoder) {
    if (encodedPassword != null) {
      // All passwords use bcrypt encoding, check here if they have already been encoded so they
      // don't get double encoded.
      if (encodedPassword.startsWith(BCRYPT_ENCODING_ID)) {
        return encodedPassword;
      } else {
        // not saved a Bcrypt and should be changed to bcrypt
        return encodePwd(getJasyptEncryptor().decrypt(encodedPassword), encoder);
      }
    } else {
      return "";
    }
  }

  private String encodePwd(String pwd, PasswordEncoder encoder) {
    return encoder.encode(pwd);
  }

  private BasicTextEncryptor getJasyptEncryptor() {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

    return textEncryptor;
  }
}
