package io.aiven.klaw.dao;

import java.io.Serializable;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ServiceAccounts implements Serializable {

  private int numberOfAllowedAccounts;

  private Set<String> serviceAccountsList;
}
