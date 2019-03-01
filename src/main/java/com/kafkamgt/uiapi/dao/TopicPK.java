package com.kafkamgt.uiapi.dao;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class TopicPK implements Serializable {

    @Column(name = "topicname")
    private String topicname;

    @Column(name = "env")
    private String environment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicPK)) return false;
        TopicPK that = (TopicPK) o;
        return Objects.equals(getTopicname(), that.getTopicname()) &&
                Objects.equals(getEnvironment(), that.getEnvironment());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEnvironment(),getTopicname());
    }

    @Override
    public String toString() {
        return "TopicPK{" +
                "topicname='" + topicname + '\'' +
                ", environment='" + environment + '\'' +
                '}';
    }
}
