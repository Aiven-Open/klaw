package io.aiven.klaw.repository;

import io.aiven.klaw.dao.KwEntitySequence;
import io.aiven.klaw.dao.KwEntitySequenceID;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface KwEntitySequenceRepo extends CrudRepository<KwEntitySequence, KwEntitySequenceID> {
  Optional<KwEntitySequence> findById(KwEntitySequenceID id);

  List<KwEntitySequence> findAllByEntityNameAndTenantId(String entityName, Integer tenantId);
}
