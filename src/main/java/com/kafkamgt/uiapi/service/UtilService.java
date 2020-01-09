package com.kafkamgt.uiapi.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
@Slf4j
public class UtilService {

    private UserDetails userDetails;

    @Value("${custom.license.key}")
    String licenseKey;

    @Value("${custom.org.name}")
    String orgName;

    public static boolean licenceLoaded = false;

    public void setUserDetails(UserDetails userDetails){
        this.userDetails = userDetails;
    }

    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public BoundStatement getBoundStatement(Session session, String query){
        return new BoundStatement(session.prepare(query));
    }

    public Cluster getCluster(String clusterConnHost, int clusterConnPort, CodecRegistry myCodecRegistry){
        Cluster cluster = Cluster
                .builder()
                .addContactPoint(clusterConnHost)
                .withPort(clusterConnPort)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withCodecRegistry(myCodecRegistry)
                .withoutJMXReporting()
                .withoutMetrics()
                .withSocketOptions(
                        new SocketOptions()
                                .setConnectTimeoutMillis(10000))
                .build();

        return cluster;
    }

    public String getAuthority(UserDetails userDetails){
        GrantedAuthority ga = this.userDetails.getAuthorities().iterator().next();
        return ga.getAuthority();
    }

    public boolean checkAuthorizedSU(){
        GrantedAuthority ga = this.userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        if(!authority.equals("ROLE_SUPERUSER"))
            return false;
        else
            return true;
    }

    public String getUserName(){
        if(this.userDetails == null)
            validateLicense(licenseKey, orgName);
        return this.userDetails.getUsername();
    }

    public UserDetails getUserDetails(){
        validateLicense(licenseKey, orgName);

        if(this.userDetails == null){
            log.error("Users not loaded .. exiting");
            System.exit(0);
        }
        licenceLoaded = true;
        return this.userDetails;
    }

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean validateLicense(String licenseKey, String organization){
        try {
            loadLicenseUtil(licenseKey, organization);
        } catch (Exception e) {
            licenceLoaded = false;
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
        try {
            this.userDetails = (UserDetails) getContext(licenseKey).getAuthentication().getPrincipal();
        }catch (Exception e){}
    }

    private SecurityContext getContext(String licenseKey){
        if(licenseKey!=null && licenseKey.length() > 0)
            return SecurityContextHolder.getContext();
        else
            return null;
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKey keyTmps = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(new PBEKeySpec(password, salt, iterationCount, keyLength));
        licenceLoaded = true;
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
