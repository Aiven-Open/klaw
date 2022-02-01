package com.kafkamgt.uiapi.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@IdClass(KwPropertiesID.class)
@Table(name="kwproperties")
public class KwProperties implements Serializable {

    @Id
    @Column(name = "kwkey")
    private String kwKey;

    @Id
    @Column(name = "tenantid")
    private int tenantId;

    @Column(name = "kwvalue")
    private String kwValue;

    @Column(name = "kwdesc")
    private String kwDesc;
}
