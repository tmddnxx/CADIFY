package com.cadify.cadifyWAS.repository.factory;

import com.cadify.cadifyWAS.model.entity.factory.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactoryRepository extends JpaRepository<Factory, String> {
    Optional<Factory> findFactoryByFactoryNameAndDeletedFalse(String factoryName);
    Optional<Factory> findFactoryByFactoryKeyAndDeletedFalse(String factoryKey);
}
