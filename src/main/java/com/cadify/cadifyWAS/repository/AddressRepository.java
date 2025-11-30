package com.cadify.cadifyWAS.repository;

import com.cadify.cadifyWAS.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findAllByMemberKey(String memberKey);

    Optional<Address> findByAddressKey(String addressKey);
}
