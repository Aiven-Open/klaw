package io.aiven.klaw.config;

import io.aiven.klaw.auth.KwRequestFilter;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import java.util.*;
import javax.naming.directory.Attributes;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@ConditionalOnProperty(name = "klaw.enable.sso", havingValue = "false")
@Slf4j
public class SecurityConfigNoSSO extends WebSecurityConfigurerAdapter {

  @Autowired private ManageDatabase manageTopics;

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${spring.ldap.base:@null}")
  private String ldapBase;

  @Value("${spring.ldap.url:@null}")
  private String ldapUrls;

  @Value("${spring.ldap.userDnPatterns:@null}")
  private String userDnPatterns;

  @Value("${spring.ldap.groupSearchBase:ou=groups:@null}")
  private String groupSearchBase;

  @Value("${spring.ldap.passwordAttribute:@null}")
  private String passwordAttribute;

  @Value("${spring.ldap.managerusername:@null}")
  private String managerUserName;

  @Value("${spring.ldap.managerpassword:@null}")
  private String managerPwd;

  @Value("${klaw.ldap.password.encryption:@null}")
  private String encryptionType;

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

  @Autowired private KwRequestFilter kwRequestFilterup;

  private void shutdownApp() {
    // ((ConfigurableApplicationContext) contextApp).close();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
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
                "/resetPassword",
                "/getRoles",
                "/getTenantsInfo",
                "/getBasicInfo",
                "/getAllTeamsSUFromRegisterUsers",
                "/registerUser",
                "/resetMemoryCache/**",
                "/userActivation**",
                "/getActivationInfo**"));

    if (coralEnabled) {
      staticResourcesHtmlArray.add("/coral/**");
      staticResourcesHtmlArray.add("/assets/coral/**");
    }

    http.csrf()
        .disable()
        .authorizeRequests()
        .antMatchers(staticResourcesHtmlArray.toArray(new String[0]))
        .permitAll()
        .anyRequest()
        .fullyAuthenticated()
        .and()
        .formLogin()
        .failureForwardUrl("/login?error")
        .failureUrl("/login?error")
        .loginPage("/login")
        .permitAll()
        .and()
        .logout()
        .logoutSuccessUrl("/login");

    //         Add a filter to validate the username/pwd with every request
    http.addFilterBefore(kwRequestFilterup, UsernamePasswordAuthenticationFilter.class);
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    if (authenticationType != null && authenticationType.equals("db")) {
      dbAuthentication(auth);
    } else if (authenticationType != null && authenticationType.equals("ldap")) {
      ldapAuthentication(auth);
    } else if (authenticationType != null && authenticationType.equals("ad")) {
      auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider())
          .userDetailsService(userDetailsService());
    } else {
      log.info("No proper authentication set. Possible values db/ldap/ad");
      shutdownApp();
    }
  }

  @Override
  @Bean
  public AuthenticationManager authenticationManagerBean() throws Exception {
    AuthenticationManager result;
    if (authenticationType != null && authenticationType.equals("ad")) {
      log.info("AD authentication configured.");
      log.info(
          "AD URL : {}, AD Domain : {}, AD Root DN : {}, AD Filter : {}",
          adUrl,
          adDomain,
          adRootDn,
          adFilter);
      result =
          new ProviderManager(
              Collections.singletonList(activeDirectoryLdapAuthenticationProvider()));
    } else {
      return super.authenticationManagerBean();
    }
    return result;
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

  private void ldapAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    try {
      log.info("Ldap authentication configured.");
      if (!checkLdapConnectivity()) {
        throw new KlawException("Cannot connect to Ldap !!");
      }

      if (encryptionType != null && encryptionType.equals("bcrypt")) {
        auth.ldapAuthentication()
            .userSearchBase(ldapBase)
            .userDnPatterns(userDnPatterns)
            .contextSource()
            .url(ldapUrls)
            .managerDn(managerUserName)
            .managerPassword(managerPwd)
            .and()
            .passwordCompare()
            .passwordEncoder(new BCryptPasswordEncoder())
            .passwordAttribute(passwordAttribute);
      }

    } catch (Exception e) {
      log.error("Cannot connect to Ldap !! ", e);
      shutdownApp();
      throw new Exception("Cannot connect to Ldap !!");
    }
  }

  private void dbAuthentication(AuthenticationManagerBuilder auth) throws Exception {
    log.info("Db authentication configured.");
    auth.userDetailsService(inMemoryUserDetailsManager());
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() throws Exception {
    final Properties globalUsers = new Properties();
    if (authenticationType != null && authenticationType.equals("db")) {
      log.info("Loading all users !!");
      List<UserInfo> users;

      try {
        users = manageTopics.selectAllUsersInfo();
      } catch (Exception e) {
        log.error("Please check if tables are created.", e);
        shutdownApp();
        throw new Exception("Please check if tables are created.");
      }

      if (users.size() == 0) {
        shutdownApp();
        throw new Exception("Please check if insert scripts are executed.");
      }

      Iterator<UserInfo> iter = users.iterator();
      UserInfo userInfo;
      PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
      while (iter.hasNext()) {
        userInfo = iter.next();
        try {
          String secPwd = userInfo.getPwd();
          if (secPwd != null && secPwd.equals("")) {
            secPwd = "gfGF%64GFDd766hfgfHFD$%#453";
          } else {
            secPwd = decodePwd(secPwd);
          }
          globalUsers.put(
              userInfo.getUsername(),
              encoder.encode(secPwd) + "," + userInfo.getRole() + ",enabled");
        } catch (Exception e) {
          log.error("Error : User not loaded {}. Check password.", userInfo.getUsername(), e);
        }
      }
    }
    return new InMemoryUserDetailsManager(globalUsers);
  }

  private boolean checkLdapConnectivity() {

    try {
      log.info("Checking Ldap connectivity.");
      ldapTemplate.search(
          ldapBase,
          "(objectclass=top)",
          new AttributesMapper() {
            public Object mapFromAttributes(Attributes attrs) throws NamingException {
              try {
                return attrs.get("cn").get();
              } catch (javax.naming.NamingException e) {
                log.error("Ldap connectivity error : ", e);
                return null;
              }
            }
          });
    } catch (org.springframework.ldap.NameNotFoundException e) {
      log.error("Exception:", e);
      return true;
    } catch (Exception e) {
      log.error("Exception:", e);
      return false;
    }

    return true;
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
