package io.aiven.klaw.repository;

import io.aiven.klaw.dao.DataVersion;
import org.springframework.data.repository.CrudRepository;

public interface DataVersionRepo extends CrudRepository<DataVersion, Integer> {

  DataVersion findTopByOrderByIdDesc();
}
