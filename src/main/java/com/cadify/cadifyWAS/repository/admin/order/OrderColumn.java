package com.cadify.cadifyWAS.repository.admin.order;

import com.cadify.cadifyWAS.model.entity.order.QOrders;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;

import java.time.LocalDate;
import java.time.LocalTime;

public enum OrderColumn {

    CREATED {
        @Override
        public BooleanExpression getDateCondition(QOrders order, LocalDate start, LocalDate end) {
            return order.createdAt.between(start.atStartOfDay(), end.atTime(LocalTime.MAX));
        }

        @Override
        public OrderSpecifier<?> getOrderSpecifier(QOrders order, String direction) {
            return "DESC".equalsIgnoreCase(direction) ? order.createdAt.desc() : order.createdAt.asc();
        }
    },
    SHIPMENT {
        @Override
        public BooleanExpression getDateCondition(QOrders order, LocalDate start, LocalDate end) {
            return order.shipmentDate.between(start, end);
        }

        @Override
        public OrderSpecifier<?> getOrderSpecifier(QOrders order, String direction) {
            return "DESC".equalsIgnoreCase(direction) ? order.shipmentDate.desc() : order.shipmentDate.asc();
        }
    };

    public abstract BooleanExpression getDateCondition(QOrders order, LocalDate start, LocalDate end);
    public abstract OrderSpecifier<?> getOrderSpecifier(QOrders order, String direction);
}
