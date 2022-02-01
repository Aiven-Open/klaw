package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TenantConfig implements Serializable {
    private KwTenantConfigModel tenantModel;
}
