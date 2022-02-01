package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.KwProperties;
import com.kafkamgt.uiapi.dao.KwPropertiesID;
import com.kafkamgt.uiapi.dao.Team;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KwPropertiesRepo extends CrudRepository<KwProperties, KwPropertiesID> {
    List<KwProperties> findAllByKwKeyAndTenantId(String kwKey, int tenantId);
    List<KwProperties> findAllByTenantId(int tenantId);
}
