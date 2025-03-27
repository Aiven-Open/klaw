package io.aiven.klaw.service;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Handles password encryption for Klaw, including bcrypt migration */
@Component
@Slf4j
public class PasswordService {

  private PasswordEncoder passwordEncoder;
  public static final String BCRYPT_ENCODING_ID = "{bcrypt}";

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  public PasswordService() {
    this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  /**
   * Currently Klaw supports two types of encryption for database authentication but all use BCrypt
   * at runtime, this method checks the encryption type already being used and returns the BCrypt
   * encrypted Password
   *
   * @param encodedPassword is the password from database already encoded
   * @return The BCrypt encoded password
   */
  public String getBcryptPassword(String encodedPassword) {
    if (encodedPassword != null) {
      // All passwords use bcrypt encoding, check here if they have already been encoded so they
      // don't get double encoded.
      if (encodedPassword.startsWith(BCRYPT_ENCODING_ID)) {
        return encodedPassword;
      } else {
        // not saved a Bcrypt and should be changed to bcrypt
        return encodePwd(getJasyptEncryptor().decrypt(encodedPassword));
      }
    } else {
      return "";
    }
  }

  protected String encodePwd(String pwd) {
    return passwordEncoder.encode(pwd);
  }

  private BasicTextEncryptor getJasyptEncryptor() {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

    return textEncryptor;
  }
}
