package com.kafkamgt.uiapi.dao;

import com.kafkamgt.uiapi.model.KafkaFlavors;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@Getter
@Setter
@Entity
@IdClass(KwClusterID.class)
@Table(name="kwclusters")
public class KwClusters implements Serializable {

    @Id
    @Column(name = "clusterid")
    private Integer clusterId;

    @Id
    @Column(name = "tenantid")
    private Integer tenantId;

    @Column(name = "clustername")
    private String clusterName;

    @Column(name = "bootstrapservers")
    private String bootstrapServers;

    @Column(name = "protocol")
    private String protocol;

    @Column(name = "clustertype")
    private String clusterType;

    @Column(name = "sharedcluster")
    private String sharedCluster;

    @Column(name = "publickey")
    private String publicKey;

    @Column(name = "cstatus")
    private String clusterStatus;

    @Column(name = "projectname")
    private String projectName;

    @Column(name = "servicename")
    private String serviceName;

    @Column(name = "kafkaflavor")
    private String kafkaFlavor;
}
