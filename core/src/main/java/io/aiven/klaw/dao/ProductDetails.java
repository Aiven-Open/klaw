package io.aiven.klaw.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "kwproductdetails")
public class ProductDetails implements Serializable {

  @Id
  @Column(name = "name")
  private String name;

  @Column(name = "version")
  private String version;
}
