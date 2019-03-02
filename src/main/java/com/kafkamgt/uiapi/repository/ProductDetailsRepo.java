package com.kafkamgt.uiapi.repository;

import com.kafkamgt.uiapi.dao.ProductDetails;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProductDetailsRepo extends CrudRepository<ProductDetails, String> {
    Optional<ProductDetails> findById(String name);
}
