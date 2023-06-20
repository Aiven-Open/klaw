package io.aiven.klaw.dao.metadata;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.KwRolesPermissions;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.ProductDetails;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.UserInfo;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KwAdminConfig {
  String klawVersion;
  String createdTime;
  List<KwTenants> tenants;
  List<KwClusters> clusters;
  List<Env> environments;
  List<KwRolesPermissions> rolesPermissions;
  List<Team> teams;
  List<UserInfo> users;
  List<KwProperties> properties;
  ProductDetails productDetails;
}
