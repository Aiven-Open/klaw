package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@IdClass(KwEntitySequenceID.class)
@Table(name = "kwentityseq")
public class KwEntitySequence implements Serializable {

  @Column(name = "seq_id")
  private Integer seqId;

  @Id
  @Column(name = "tenantid")
  private Integer tenantId;

  @Id
  @Column(name = "entity_name")
  private String entityName;
}
