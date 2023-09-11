package io.aiven.klaw.config;

import static io.aiven.klaw.error.KlawErrorMessages.SEC_CONFIG_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.SEC_CONFIG_ERR_102;
import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;

import io.aiven.klaw.auth.KwAuthenticationFailureHandler;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import io.aiven.klaw.dao.UserInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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

  @Autowired LdapTemplate ldapTemplate;

  private void shutdownApp() {
    // TODO
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeHttpRequests()
        .requestMatchers(ConfigUtils.getStaticResources(coralEnabled))
        .permitAll()
        .anyRequest()
        .fullyAuthenticated()
        .and()
        .formLogin()
        .successHandler(kwAuthenticationSuccessHandler)
        .failureHandler(kwAuthenticationFailureHandler)
        .failureForwardUrl("/login?error")
        .failureUrl("/login?error")
        .loginPage("/login")
        .permitAll()
        .and()
        .logout()
        .logoutSuccessUrl("/login");

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    AuthenticationManager authenticationManager;
    if (authenticationType != null && authenticationType.equals(ACTIVE_DIRECTORY.value)) {
      log.info("AD authentication configured.");
      log.info(
          "AD URL : {}, AD Domain : {}, AD Root DN : {}, AD Filter : {}",
          adUrl,
          adDomain,
          adRootDn,
          adFilter);
      authenticationManager =
          new ProviderManager(
              Collections.singletonList(activeDirectoryLdapAuthenticationProvider()));
    } else {
      authenticationManager = authenticationConfiguration.getAuthenticationManager();
    }
    return authenticationManager;
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

      if (users.size() == 0) {
        shutdownApp();
        throw new Exception(SEC_CONFIG_ERR_101);
      }

      Iterator<UserInfo> iter = users.iterator();
      PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      loadAllUsers(globalUsers, iter, encoder);
    }
    return new InMemoryUserDetailsManager(globalUsers);
  }

  private void loadAllUsers(
      Properties globalUsers, Iterator<UserInfo> iter, PasswordEncoder encoder) {
    UserInfo userInfo;
    while (iter.hasNext()) {
      userInfo = iter.next();
      try {
        String secPwd = userInfo.getPwd();
        if (secPwd == null || secPwd.equals("")) {
          continue;
        } else {
          secPwd = decodePwd(secPwd);
        }
        globalUsers.put(
            userInfo.getUsername(), encoder.encode(secPwd) + "," + userInfo.getRole() + ",enabled");
      } catch (Exception e) {
        log.error("Error : User not loaded {}. Check password.", userInfo.getUsername(), e);
      }
    }
  }

  private String decodePwd(String pwd) {
    if (pwd != null) {
      BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
      textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

      return textEncryptor.decrypt(pwd);
    }
    return "";
  }
}
