package com.kafkamgt.uiapi.dao;

public class Env {
    private String name;
    private String host;
    private String port;
    private String protocol;
    private String type;
    private String keystorelocation;
    private String truststorelocation;
    private String keystorepwd;
    private String keypwd;
    private String truststorepwd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKeystorelocation() {
        return keystorelocation;
    }

    public void setKeystorelocation(String keystorelocation) {
        this.keystorelocation = keystorelocation;
    }

    public String getTruststorelocation() {
        return truststorelocation;
    }

    public void setTruststorelocation(String truststorelocation) {
        this.truststorelocation = truststorelocation;
    }

    public String getKeystorepwd() {
        return keystorepwd;
    }

    public void setKeystorepwd(String keystorepwd) {
        this.keystorepwd = keystorepwd;
    }

    public String getKeypwd() {
        return keypwd;
    }

    public void setKeypwd(String keypwd) {
        this.keypwd = keypwd;
    }

    public String getTruststorepwd() {
        return truststorepwd;
    }

    public void setTruststorepwd(String truststorepwd) {
        this.truststorepwd = truststorepwd;
    }
}
