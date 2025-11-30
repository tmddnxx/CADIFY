package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.factory.estimate.FactoryEstimateDTO;
import com.cadify.cadifyWAS.model.dto.factory.order.FactoryOrderDTO;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.Orders;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FactoryMapper {
    
    @Getter
    private final OrderItemMapper orderItemMapper;
    private final ObjectMapper objectMapper;
    private final EstimateRepository estimateRepository;
    private final OrderItemRepository orderItemRepository;

//    public FactoryOrderDTO.BaseResponse orderToBaseResponse(Orders order, MemberDTO.MemberInfo customerInfo) {
//        AtomicInteger totalCost = new AtomicInteger(0);
//
//        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(order.getOrderKey());
//
//        for (OrderItem orderItem : orderItems) {
//            if (orderItem != null && orderItem.getEstKey() != null) {
//                estimateRepository.findByEstKey(orderItem.getEstKey())
//                    .ifPresent(estimate -> {
//                        totalCost.addAndGet((int)(estimate.getCost() * orderItem.getAmount()));
//                    });
//            }
//        }
//
//        return FactoryOrderDTO.BaseResponse.builder()
//                .orderKey(order.getOrderKey())
//                .customerName(customerInfo.getMemberName())
//                .createdAt(order.getCreatedAt())
//                .totalCost(totalCost.get())
//                .overallOrderReceivedStatus(order.getOrderReceivedStatus())
//                .build();
//    }
//
//    public FactoryOrderDTO.DetailResponse orderToDetailResponse(
//            Orders order, MemberDTO.MemberInfo customerInfo, List<OrderItem> orderItems) {
//
//        return FactoryOrderDTO.DetailResponse.builder()
//                .baseInfo(orderToBaseResponse(order, customerInfo))
//                .orderItems(orderItems.stream()
//                        .map(orderItemMapper::orderItemToOrderItemResponse)
//                        .collect(Collectors.toList()))
//                .customerInfo(customerInfo)
//                .build();
//    }

    public FactoryEstimateDTO.EstimateResponse estimateToResponse (Tuple tuple) throws JsonProcessingException {
        return FactoryEstimateDTO.EstimateResponse.builder()
                .estKey(tuple.get(0, OrderItem.class).getEstKey())
                .fileName(tuple.get(0, OrderItem.class).getFileName())
                .estName(tuple.get(0, OrderItem.class).getEstName())
                .method(tuple.get(0, OrderItem.class).getMethod())
                .type(tuple.get(0, OrderItem.class).getType())
                .holeJson(tuple.get(0, OrderItem.class).getHoleJson())
                .isFastShipment(tuple.get(0, OrderItem.class).isFastShipment())
                .material(tuple.get(0, OrderItem.class).getMaterial())
                .thickness(tuple.get(0, OrderItem.class).getThickness())
                .kg(tuple.get(0, OrderItem.class).getKg())
                .surface(tuple.get(0, OrderItem.class).getSurface())
                .coatingColor(tuple.get(0, OrderItem.class).getCoatingColor())
                .isChamfer(tuple.get(0, OrderItem.class).isChamfer())
                .commonDiff(tuple.get(0, OrderItem.class).getCommonDiff())
                .roughness(tuple.get(0, OrderItem.class).getRoughness())
                .memo(tuple.get(0, OrderItem.class).getMemo())
                .factoryDxfAddress(tuple.get(1, String.class))
                .s3DxfAddress(tuple.get(2, String.class))
                .imageUrl(tuple.get(3, String.class))
                .stepS3(tuple.get(4, String.class))
                .bbox(tuple.get(0, OrderItem.class).getBbox() != null ?
                        objectMapper.readValue(tuple.get(0, OrderItem.class).getBbox(), OptionDTO.BBox.class)
                        : null)
                .build();
    }
} 
