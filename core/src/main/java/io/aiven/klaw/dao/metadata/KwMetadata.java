package io.aiven.klaw.dao.metadata;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KwMetadata {
  KwAdminConfig kwAdminConfig;
  KwData kwData;
  KwRequests KwRequests;
}
