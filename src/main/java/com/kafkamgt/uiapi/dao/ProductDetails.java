package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name="productdetails")
public class ProductDetails implements Serializable {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @Column(name = "licensekey")
    private String licensekey;
}
