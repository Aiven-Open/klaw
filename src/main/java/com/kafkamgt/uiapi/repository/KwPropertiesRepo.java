package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.KwProperties;
import com.kafkamgt.uiapi.dao.KwPropertiesID;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface KwPropertiesRepo extends CrudRepository<KwProperties, KwPropertiesID> {
  List<KwProperties> findAllByKwKeyAndTenantId(String kwKey, int tenantId);

  List<KwProperties> findAllByTenantId(int tenantId);
}
