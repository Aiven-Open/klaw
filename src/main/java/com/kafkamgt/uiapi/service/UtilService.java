package com.kafkamgt.uiapi.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UtilService {

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
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        return ga.getAuthority();
    }

    boolean checkAuthorizedAdmin_SU(UserDetails userDetails){
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        return authority.equals("ROLE_SUPERUSER") || authority.equals("ROLE_ADMIN");
    }

    boolean checkAuthorizedSU(UserDetails userDetails){
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        return authority.equals("ROLE_SUPERUSER");
    }

    boolean checkAuthorizedAdmin(UserDetails userDetails){
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        return authority.equals("ROLE_ADMIN");
    }

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

//    public HashMap<String,String> validateLicense(){
//        HashMap<String,String> hLicenseMap = new HashMap<>();
//
//        try {
//            loadLicenseUtil(licenseKey, organization);
//            hLicenseMap.put("LICENSE_STATUS", Boolean.TRUE.toString());
//            hLicenseMap.put("LICENSE_KEY", licenseKey);
//            return hLicenseMap;
//        } catch (Exception e) {
//            hLicenseMap.put("LICENSE_STATUS", Boolean.FALSE.toString());
//            return hLicenseMap;
//        }
//    }


//    private void loadLicenseUtil(String licenseKey, String SALT_STR) throws Exception {
//        byte[] salt = SALT_STR.getBytes();
//        int iterationCount = 40000;
//        int keyLength = 128;
//
//        decryptLicense(licenseKey, createSecretKey(SALT_STR.toCharArray(),
//                salt, iterationCount, keyLength));
//    }
//
//    private static SecretKeySpec createSecretKey(char[] password, byte[] salt, int iterationCount, int keyLength)
//            throws NoSuchAlgorithmException, InvalidKeySpecException {
//        SecretKey keyTmps = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
//                .generateSecret(new PBEKeySpec(password, salt, iterationCount, keyLength));
//        return new SecretKeySpec(keyTmps.getEncoded(), "AES");
//    }
//
//    private static void decryptLicense(String string, SecretKeySpec key) throws GeneralSecurityException, IOException {
//        Cipher cInstance = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        cInstance.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(base64Decode(string.split(":")[0])));
//        cInstance.doFinal(base64Decode(string.split(":")[1]));
//    }
//
//    private static byte[] base64Decode(String property) throws IOException {
//        return Base64.getDecoder().decode(property);
//    }

}
