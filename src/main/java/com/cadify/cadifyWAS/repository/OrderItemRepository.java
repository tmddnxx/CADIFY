package com.cadify.cadifyWAS.repository;

import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    Optional<OrderItem> findOrderItemByOrderItemKey(String orderItemKey);

    List<OrderItem> findAllByOrderKey(String orderKey);

    @Query("SELECT o FROM OrderItem o WHERE o.orderKey = :orderKey AND o.orderReceivedStatus <> :status")
    List<OrderItem> findNewOrderItem(@Param("orderKey") String orderKey,
                                     @Param("status") OrderReceivedStatus status);


    List<OrderItem> findAllByMemberKey(String memberKey);

    Optional<OrderItem> findByEstKey(String estKey);
}
