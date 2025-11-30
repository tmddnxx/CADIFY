package com.cadify.cadifyWAS.repository.factory.admin;

import com.cadify.cadifyWAS.model.entity.factory.Factory;

public interface FactoryAdminQueryRepository {
    Factory findFactoryByMemberKey(String memberKey);
}
