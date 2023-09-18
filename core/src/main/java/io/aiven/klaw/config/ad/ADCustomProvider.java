package io.aiven.klaw.config.ad;

import static io.aiven.klaw.error.KlawErrorMessages.AD_105;
import static io.aiven.klaw.error.KlawErrorMessages.AD_106;
import static io.aiven.klaw.error.KlawErrorMessages.AD_107;
import static io.aiven.klaw.error.KlawErrorMessages.AD_108;
import static io.aiven.klaw.error.KlawErrorMessages.AD_109;
import static io.aiven.klaw.error.KlawErrorMessages.AD_110;
import static io.aiven.klaw.error.KlawErrorMessages.AD_111;
import static io.aiven.klaw.error.KlawErrorMessages.AD_112;
import static io.aiven.klaw.error.KlawErrorMessages.AD_113;
import static io.aiven.klaw.error.KlawErrorMessages.AD_114;
import static io.aiven.klaw.error.KlawErrorMessages.AD_115;
import static io.aiven.klaw.error.KlawErrorMessages.AD_116;
import static io.aiven.klaw.error.KlawErrorMessages.AD_117;
import static io.aiven.klaw.error.KlawErrorMessages.AD_118;

import io.aiven.klaw.error.ActiveDirectoryAuthenticationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.*;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Slf4j
public class ADCustomProvider extends AbstractLdapAuthenticationProvider {

  private String searchFilter;

  private static final Pattern SUB_ERROR_CODE = Pattern.compile(".*data\\s([0-9a-f]{3,4}).*");

  // Error codes
  private static final int USERNAME_NOT_FOUND = 0x525;
  private static final int INVALID_PASSWORD = 0x52e;
  private static final int NOT_PERMITTED = 0x530;
  private static final int PASSWORD_EXPIRED = 0x532;
  private static final int ACCOUNT_DISABLED = 0x533;
  private static final int ACCOUNT_EXPIRED = 0x701;
  private static final int PASSWORD_NEEDS_RESET = 0x773;
  private static final int ACCOUNT_LOCKED = 0x775;

  private final String domain;
  private final String rootDn;
  private final String url;
  private boolean convertSubErrorCodesToExceptions;

  //  public ADCustomProvider(String adDomain, String adUrl, String rootDn) {
  //    Assert.isTrue(StringUtils.hasText(adUrl), "AD Url cannot be empty");
  //    this.domain = StringUtils.hasText(adDomain) ? adDomain.toLowerCase() : null;
  //    this.url = adUrl;
  //    this.rootDn = StringUtils.hasText(rootDn) ? rootDn.toLowerCase() : null;
  //  }

  public ADCustomProvider(String adDomain, String adUrl, String adFilter) {
    Assert.isTrue(StringUtils.hasText(adUrl), "AD Url cannot be empty");
    this.domain = StringUtils.hasText(adDomain) ? adDomain.toLowerCase() : null;
    this.url = adUrl;
    rootDn = this.domain == null ? null : rootDnFromDomain(this.domain);
    this.searchFilter = adFilter;
  }

  @Override
  protected DirContextOperations doAuthentication(UsernamePasswordAuthenticationToken auth) {
    String username = auth.getName();
    String password = (String) auth.getCredentials();
    DirContext ctx = bindAsUser(username, password);
    try {
      return searchForUser(ctx, username);
    } catch (NamingException e) {
      log.error(AD_115 + username, e);
      throw badCredentials(e);
    } finally {
      LdapUtils.closeContext(ctx);
    }
  }

  @Override
  protected Collection<? extends GrantedAuthority> loadUserAuthorities(
      DirContextOperations userData, String username, String password) {
    String[] groups = userData.getStringAttributes("memberOf");

    if (groups == null) {
      log.debug("No values for 'memberOf' attribute.");
      return AuthorityUtils.NO_AUTHORITIES;
    }

    if (log.isDebugEnabled()) {
      log.debug("'memberOf' attribute values: " + Arrays.asList(groups));
    }

    ArrayList<GrantedAuthority> authorities = new ArrayList<>(groups.length);
    for (String group : groups) {
      try {
        LdapName ln = new LdapName(group);
        for (Rdn rdn : ln.getRdns()) {
          if (rdn.getType().equalsIgnoreCase("CN")) {
            authorities.add(new SimpleGrantedAuthority(rdn.getValue().toString()));
          }
        }
      } catch (InvalidNameException e) {
        log.error("Unable to map group name to spring authority " + e.getMessage());
      }
    }

    return authorities;
  }

  private DirContext bindAsUser(String username, String password) {
    Hashtable<String, String> env = new Hashtable<>();
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    String bindPrincipal = createBindPrincipal(username);
    env.put(Context.SECURITY_PRINCIPAL, bindPrincipal);
    env.put(Context.PROVIDER_URL, url);
    env.put(Context.SECURITY_CREDENTIALS, password);
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.OBJECT_FACTORIES, DefaultDirObjectFactory.class.getName());

    ContextFactory contextFactory = new ContextFactory();

    try {
      return contextFactory.createContext(env);
    } catch (NamingException e) {
      if ((e instanceof AuthenticationException) || (e instanceof OperationNotSupportedException)) {
        handleBindException(bindPrincipal, e);
        throw badCredentials(e);
      } else {
        throw LdapUtils.convertLdapException(e);
      }
    }
  }

  private void handleBindException(String bindPrincipal, NamingException exception) {
    if (log.isDebugEnabled()) {
      log.debug("Authentication for " + bindPrincipal + " failed:" + exception);
    }

    int subErrorCode = parseSubErrorCode(exception.getMessage());

    if (subErrorCode <= 0) {
      log.debug("Failed to locate AD-specific sub-error code in message");
      return;
    }
    log.error("Active Directory authentication failed: " + getCodeToLog(subErrorCode));

    if (convertSubErrorCodesToExceptions) {
      throwExceptionForCode(subErrorCode, exception);
    }
  }

  private int parseSubErrorCode(String message) {
    Matcher m = SUB_ERROR_CODE.matcher(message);
    if (m.matches()) {
      return Integer.parseInt(m.group(1), 16);
    }
    return -1;
  }

  private void throwExceptionForCode(int code, NamingException exception) {
    Throwable cause = new ActiveDirectoryAuthenticationException(exception.getMessage(), exception);
    switch (code) {
      case PASSWORD_EXPIRED -> throw new CredentialsExpiredException(
          messages.getMessage("LdapAuthenticationProvider.credentialsExpired", AD_116), cause);
      case ACCOUNT_DISABLED -> throw new DisabledException(
          messages.getMessage("LdapAuthenticationProvider.disabled", AD_117), cause);
      case ACCOUNT_EXPIRED -> throw new AccountExpiredException(
          messages.getMessage("LdapAuthenticationProvider.expired", AD_118), cause);
      case ACCOUNT_LOCKED -> throw new LockedException(
          messages.getMessage("LdapAuthenticationProvider.locked", AD_105), cause);
      default -> throw badCredentials(cause);
    }
  }

  private String getCodeToLog(int code) {
    return switch (code) {
      case USERNAME_NOT_FOUND -> AD_106;
      case INVALID_PASSWORD -> AD_107;
      case NOT_PERMITTED -> AD_108;
      case PASSWORD_EXPIRED -> AD_109;
      case ACCOUNT_DISABLED -> AD_110;
      case ACCOUNT_EXPIRED -> AD_111;
      case PASSWORD_NEEDS_RESET -> AD_112;
      case ACCOUNT_LOCKED -> AD_113;
      default -> "Unknown (error code " + Integer.toHexString(code) + ")";
    };
  }

  private BadCredentialsException badCredentialsMessage() {
    return new BadCredentialsException(
        messages.getMessage("LdapAuthenticationProvider.badCredentials", AD_114));
  }

  private BadCredentialsException badCredentials(Throwable cause) {
    return (BadCredentialsException) badCredentialsMessage().initCause(cause);
  }

  private DirContextOperations searchForUser(DirContext context, String username)
      throws NamingException {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    String bindPrincipal = createBindPrincipal(username);
    String searchRoot = rootDn != null ? rootDn : searchRootFromPrincipal(bindPrincipal);

    try {
      return SpringSecurityLdapTemplate.searchForSingleEntryInternal(
          context, searchControls, searchRoot, searchFilter, new Object[] {username});
    } catch (IncorrectResultSizeDataAccessException incorrectResults) {
      // Search should never return multiple results if properly configured - just
      // rethrow
      if (incorrectResults.getActualSize() != 0) {
        throw incorrectResults;
      }
      // If we found no results, then the username/password did not match
      UsernameNotFoundException userNameNotFoundException =
          new UsernameNotFoundException(
              "User " + username + " not found in directory.", incorrectResults);
      throw badCredentials(userNameNotFoundException);
    }
  }

  private String searchRootFromPrincipal(String bindPrincipal) {
    int atChar = bindPrincipal.lastIndexOf('@');

    if (atChar < 0) {
      log.debug(
          "User principal '"
              + bindPrincipal
              + "' does not contain the domain, and no domain has been configured");
      throw badCredentialsMessage();
    }

    return rootDnFromDomain(bindPrincipal.substring(atChar + 1, bindPrincipal.length()));
  }

  private String rootDnFromDomain(String domain) {
    String[] tokens = StringUtils.tokenizeToStringArray(domain, ".");
    StringBuilder root = new StringBuilder();

    for (String token : tokens) {
      if (root.length() > 0) {
        root.append(',');
      }
      root.append("dc=").append(token);
    }

    return root.toString();
  }

  String createBindPrincipal(String username) {
    if (domain == null || username.toLowerCase().endsWith(domain)) {
      return username;
    }

    return username + "@" + domain;
  }

  public void setConvertSubErrorCodesToExceptions(boolean convertSubErrorCodesToExceptions) {
    this.convertSubErrorCodesToExceptions = convertSubErrorCodesToExceptions;
  }

  public void setSearchFilter(String searchFilter) {
    if (this.searchFilter != null) {
      this.searchFilter = searchFilter;
    }
  }

  static class ContextFactory {
    DirContext createContext(Hashtable<?, ?> env) throws NamingException {
      return new InitialLdapContext(env, null);
    }
  }
}
