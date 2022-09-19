package io.aiven.klaw.dao;

import java.io.Serializable;
import javax.persistence.*;
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
