package com.kafkamgt.uiapi.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
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
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

@Service
@Slf4j
public class UtilService {

    @Value("${kafkawize.license.key}")
    private
    String licenseKey;

    @Value("${kafkawize.org.name}")
    private
    String organization;

    @Autowired
    ManageDatabase manageDatabase;

    public static boolean licenceLoaded = false;

    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public BoundStatement getBoundStatement(Session session, String query){
        return new BoundStatement(session.prepare(query));
    }

    public Cluster getCluster(String clusterConnHost, int clusterConnPort, CodecRegistry myCodecRegistry){

        return Cluster
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
    }

    String getAuthority(UserDetails userDetails){
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();

        return "ROLE_" + reqsHandle.getUsersInfo(userDetails.getUsername()).getRole();
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
//        return ga.getAuthority();
    }

    boolean checkAuthorizedAdmin_SU(UserDetails userDetails){
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = getAuthority(userDetails);
        return authority.equals("ROLE_SUPERUSER") || authority.equals("ROLE_ADMIN");
    }

    boolean checkAuthorizedSU(UserDetails userDetails){
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = getAuthority(userDetails);
        return authority.equals("ROLE_SUPERUSER");
    }

    boolean checkAuthorizedAdmin(UserDetails userDetails){
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = getAuthority(userDetails);
        return authority.equals("ROLE_ADMIN");
    }

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public HashMap<String,String> validateLicense(){
        HashMap<String,String> hLicenseMap = new HashMap<>();

        try {
            loadLicenseUtil(licenseKey, organization);
            hLicenseMap.put("LICENSE_STATUS", Boolean.TRUE.toString());
            hLicenseMap.put("LICENSE_KEY", licenseKey);
            licenceLoaded=true;
            return hLicenseMap;
        } catch (Exception e) {
            hLicenseMap.put("LICENSE_STATUS", Boolean.FALSE.toString());
            return hLicenseMap;
        }
    }


    private void loadLicenseUtil(String licenseKey, String SALT_STR) throws Exception {
        byte[] salt = SALT_STR.getBytes();
        int iterationCount = 40000;
        int keyLength = 128;

        int sz = (int)Long.parseLong(licenseKey.substring(licenseKey.length()-3,licenseKey.length()-1),16);
        String expData = licenseKey.substring(licenseKey.length()-sz-3);
        expData = new String(base64Decode(new String(base64Decode(expData.substring(0,expData.length()-3)+"=="))));
        String dateExpFound = expData.substring(expData.lastIndexOf(":")+1);
        boolean checkExp = new SimpleDateFormat("yyyy-MM-dd").parse(dateExpFound).before(new Date());
        if(checkExp)
            throw new KafkawizeException("Invalid License key !!");

        decryptLicense(licenseKey.substring(0, licenseKey.length()-sz-3), createSecretKey(expData.toCharArray(),
                salt, iterationCount, keyLength));
    }

    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKey keyTmps = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
                .generateSecret(new PBEKeySpec(password, salt, iterationCount, keyLength));
        return new SecretKeySpec(keyTmps.getEncoded(), "AES");
    }

    private static void decryptLicense(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
        Cipher cInstance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cInstance.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(string.split(":")[0])));
        cInstance.doFinal(base64Decode(string.split(":")[1]));
    }

    private static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }


}
