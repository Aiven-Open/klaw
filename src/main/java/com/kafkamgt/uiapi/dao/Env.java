package com.kafkamgt.uiapi.dao;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="kwenv")
public class Env implements Serializable {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "host")
    private String host;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "type")
    private String type;

    @Column(name = "keystorelocation")
    private String keyStoreLocation;

    @Column(name = "truststorelocation")
    private String trustStoreLocation;

    @Column(name = "keystorepwd")
    private String keyStorePwd;

    @Column(name = "keypwd")
    private String keyPwd;

    @Column(name = "truststorepwd")
    private String trustStorePwd;

    @Column(name = "other_params")
    private String otherParams;

    @Transient
    private String envStatus;

}
