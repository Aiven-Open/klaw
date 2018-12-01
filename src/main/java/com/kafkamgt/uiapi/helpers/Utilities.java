package com.kafkamgt.uiapi.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Utilities {


    @Autowired
    LicenseUtil licenseUtil;

    @Value("${license.key}")
    String licenseKey;

    public boolean validateLicense(){

//        try {
//            licenseUtil.loadLicenseUtil(licenseKey);
//        } catch (Exception e) {
//            return false;
//        }
        return true;
    }

}
