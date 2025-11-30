package com.cadify.cadifyWAS.repository.factory.admin;

import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactoryAdminRepository extends JpaRepository<FactoryAdmin, String>, FactoryAdminQueryRepository {
    Optional<FactoryAdmin> findByEmailAndDeletedFalse(String email);
    Optional<FactoryAdmin> findByUsernameAndDeletedFalse(String username);
    Optional<FactoryAdmin> findByMemberKeyAndDeletedFalse(String memberKey);
    Optional<FactoryAdmin> findFirstByEmailOrUsernameAndDeletedFalse(String email, String username);

}
