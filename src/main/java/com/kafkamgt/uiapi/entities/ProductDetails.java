package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
}
