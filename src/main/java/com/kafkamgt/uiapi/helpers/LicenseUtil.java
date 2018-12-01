package com.kafkamgt.uiapi.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Configuration
public class LicenseUtil {

    @Value("${license.spec}")
    String LICENSE_SPEC;

    @Value("${license.specsalt}")
    String SALT_STR;


    public void loadLicenseUtil(String licenseKey) throws Exception {
        byte[] salt = SALT_STR.getBytes();
        int iterationCount = 40000;
        int keyLength = 128;

       decryptLicense(licenseKey, createSecretKey(LICENSE_SPEC.toCharArray(),
                salt, iterationCount, keyLength));
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKey keyTmp = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(new PBEKeySpec(password, salt, iterationCount, keyLength));
        return new SecretKeySpec(keyTmp.getEncoded(), "AES");
    }

    private static String decryptLicense(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(string.split(":")[0])));
        return new String(pbeCipher.doFinal(base64Decode(string.split(":")[1])), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
}
