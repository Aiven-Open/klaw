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
@Table(name="teams")
public class Team implements Serializable {

    @Id
    @Column(name = "team")
    private String teamname;

    @Id
    @Column(name = "app")
    private String app;

    @Column(name = "teammail")
    private String teammail;

    @Column(name = "teamPhone")
    private String teamphone;

    @Column(name = "contactperson")
    private String contactperson;
}
