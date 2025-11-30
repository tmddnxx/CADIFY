package com.cadify.cadifyWAS.repository;

import com.cadify.cadifyWAS.model.entity.order.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, String>{

    List<Orders> findByMemberKeyOrderByCreatedAtDesc(String memberKey);

    Optional<Orders> findOrderByOrderKey(String orderKey);

    // key 기반 Orders 조회
    Optional<Orders> findByOrderKey(String orderKey);
}
