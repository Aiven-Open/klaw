package io.aiven.klaw.config.ad;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.ppolicy.PasswordPolicyControl;
import org.springframework.security.ldap.ppolicy.PasswordPolicyResponseControl;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

@Configuration
@Slf4j
public class UserDetailsMapper extends LdapUserDetailsMapper implements UserDetailsContextMapper {

  public static final String GIVEN_NAME = "givenName";
  public static final String SN = "sn";
  public static final String USER_PASSWORD = "userPassword";

  @Override
  public LdapUserDetails mapUserFromContext(
      DirContextOperations ctx,
      String username,
      Collection<? extends GrantedAuthority> authorities) {

    String firstName = ctx.getStringAttribute(GIVEN_NAME);
    String lastName = ctx.getStringAttribute(SN);
    String dn = firstName + " " + lastName;

    LdapUserDetailsImpl.Essence essence = new LdapUserDetailsImpl.Essence();
    essence.setDn(dn);

    Object passwordValue = ctx.getObjectAttribute(USER_PASSWORD);

    if (passwordValue != null) {
      essence.setPassword(mapPassword(passwordValue));
    }

    essence.setUsername(username);

    // Map the roles
    //        for (int i = 0; (this.roleAttributes != null)
    //                && (i < this.roleAttributes.length); i++) {
    //            String[] rolesForAttribute =
    // ctx.getStringAttributes(this.roleAttributes[i].toLowerCase().trim());
    //
    //            if (rolesForAttribute == null) {
    //                log.debug("Couldn't read role attribute '"
    //                        + this.roleAttributes[i] + "' for user " + dn);
    //                continue;
    //            }
    //
    //            for (String role : rolesForAttribute) {
    //                GrantedAuthority authority = createAuthority(role.toLowerCase().trim());
    //
    //                if (authority != null) {
    //                    essence.addAuthority(authority);
    //                }
    //            }
    //        }

    // Add the supplied authorities

    for (GrantedAuthority authority : authorities) {
      essence.addAuthority(authority);
    }

    // Check for PasswordPolicy data

    PasswordPolicyResponseControl passwordPolicyResponseControl =
        (PasswordPolicyResponseControl) ctx.getObjectAttribute(PasswordPolicyControl.OID);

    if (passwordPolicyResponseControl != null) {
      essence.setTimeBeforeExpiration(passwordPolicyResponseControl.getTimeBeforeExpiration());
      essence.setGraceLoginsRemaining(passwordPolicyResponseControl.getGraceLoginsRemaining());
    }

    return essence.createUserDetails();
  }
}
