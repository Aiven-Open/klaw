package com.kafkamgt.uiapi.helpers.db.jdbc.repo;

import com.kafkamgt.uiapi.entities.ProductDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductDetailsRepo extends CrudRepository<ProductDetails, String> {
    Optional<ProductDetails> findById(String name);
}
