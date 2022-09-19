package io.aiven.klaw.repository;

import io.aiven.klaw.dao.ProductDetails;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ProductDetailsRepo extends CrudRepository<ProductDetails, String> {
  Optional<ProductDetails> findById(String name);
}
