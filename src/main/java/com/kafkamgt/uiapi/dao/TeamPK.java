package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TeamPK implements Serializable {

    @Column(name = "team")
    private String teamname;

    @Column(name = "app")
    private String app;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamPK)) return false;
        TeamPK that = (TeamPK) o;
        return Objects.equals(getTeamname(), that.getTeamname()) &&
                Objects.equals(getApp(), that.getApp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTeamname(),getApp());
    }
}
