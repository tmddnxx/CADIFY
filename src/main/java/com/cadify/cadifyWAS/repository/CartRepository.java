package com.cadify.cadifyWAS.repository;

import com.cadify.cadifyWAS.model.entity.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findCartByMemberKey(String memberKey);
}
