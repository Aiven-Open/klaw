package com.kafkamgt.uiapi.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class SchemaRequestPK implements Serializable {

    @Column(name = "topicname")
    private String topicname;

    @Column(name = "env")
    private String environment;

    @Column(name = "versionschema")
    private String schemaversion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SchemaRequestPK)) return false;
        SchemaRequestPK that = (SchemaRequestPK) o;
        return Objects.equals(getTopicname(), that.getTopicname()) &&
                Objects.equals(getEnvironment(), that.getEnvironment()) &&
                Objects.equals(getSchemaversion(), that.getSchemaversion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnvironment(),getTopicname(), getSchemaversion());
    }
}
