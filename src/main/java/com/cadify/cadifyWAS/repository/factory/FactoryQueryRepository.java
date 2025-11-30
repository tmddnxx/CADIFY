package com.cadify.cadifyWAS.repository.factory;

import com.cadify.cadifyWAS.model.entity.factory.Factory;

import java.util.Optional;

public interface FactoryQueryRepository {
    Optional<Factory> getFactoryFromPrincipal(String memberKey);
}
