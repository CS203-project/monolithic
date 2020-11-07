package com.example.demo.portfolio;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.example.demo.user.User;
import java.util.Optional;

// This will be AUTO IMPLEMENTED by Spring into a Bean called userRepository
// CRUD refers Create, Read, Update, Delete

public interface AssetRepository extends CrudRepository<Asset, Integer> {
    Optional<List<Asset>> findByCode(String code);
}