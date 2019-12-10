package com.kafkamgt.uiapi.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Service
public class UtilService {
    public boolean checkAuthorizedSU(){
        UserDetails userDetails = getUserDetails();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        if(!authority.equals("ROLE_SUPERUSER"))
            return false;
        else
            return true;
    }

    public String getUserName(){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }

    public UserDetails getUserDetails(){
        return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean validateLicense(String licenseKey, String organization){

        try {
            loadLicenseUtil(licenseKey, organization);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void loadLicenseUtil(String licenseKey, String SALT_STR) throws Exception {
        byte[] salt = SALT_STR.getBytes();
        int iterationCount = 40000;
        int keyLength = 128;

        decryptLicense(licenseKey, createSecretKey(SALT_STR.toCharArray(),
                salt, iterationCount, keyLength));
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKey keyTmps = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(new PBEKeySpec(password, salt, iterationCount, keyLength));
        return new SecretKeySpec(keyTmps.getEncoded(), "AES");
    }

    private static String decryptLicense(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        Cipher cInstance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cInstance.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(string.split(":")[0])));
        return new String(cInstance.doFinal(base64Decode(string.split(":")[1])), "UTF-8");
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }
}
