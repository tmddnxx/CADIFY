package com.cadify.cadifyWAS.repository;

import com.cadify.cadifyWAS.model.entity.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT c FROM CartItem c WHERE c.cartItemKey IN :cartItemKeys ORDER BY c.createdAt ASC, c.cartItemKey ASC")
    List<CartItem> findAllByCartItemKeys(@Param("cartItemKeys") List<Long> cartItemKeys);

    Optional<CartItem> findByEstKey(String estKey);

    Optional<CartItem> findCartItemsByCartItemKey(Long cartItemKey);

    int findTotalPriceByCartItemKey(Long cartItemKey);

    void deleteCartItemsByEstKey(String estKey);

    List<CartItem> findAllByCartKey(Long cartKey);

    boolean existsCartItemByEstKey(String estKey);
}
