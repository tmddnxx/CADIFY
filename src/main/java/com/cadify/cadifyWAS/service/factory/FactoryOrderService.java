package com.cadify.cadifyWAS.service.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.admin.order.CalendarOrderCountsResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.FactoryOrderDTO;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderItemResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.OrderResponse;
import com.cadify.cadifyWAS.model.dto.factory.order.RejectedItemResponse;
import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
import com.cadify.cadifyWAS.repository.factory.order.FactoryOrderQueryRepository;
import com.cadify.cadifyWAS.security.jwt.JwtPrincipal;
import com.cadify.cadifyWAS.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryOrderService {
    private final FactoryOrderQueryRepository factoryOrderRepository;
    private final FactoryAdminRepository factoryAdminRepository;
    private final OrderItemRepository orderItemRepository;
    private final JwtUtil jwtUtil;


    // 공장용 주문 조회
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(List<String> status){
        List<OrderReceivedStatus> statusParam = status.stream()
                .map(String::toUpperCase)
                .map(statusStr -> {
                    try{
                        return OrderReceivedStatus.valueOf(statusStr);
                    }catch (Exception e){
                        throw new CustomLogicException(ExceptionCode.INVALID_ORDER_STATUS);
                    }
                })
                .toList();

        Factory factory = getValidFactory();
        return factoryOrderRepository.getOrderList(factory.getFactoryKey(), factory.getFactoryType(), statusParam);
    }

    // 공장용 주문 아이템 조회
    @Transactional(readOnly = true)
    public List<OrderItemResponse> getOrderItems(FactoryOrderDTO.OrderItemRequest request){
        Factory factory = getValidFactory();
//        isValidStatus(request.getStatus());

        return factoryOrderRepository.getOrderItemLists(factory.getFactoryKey(), factory.getFactoryType(), request.getOrderKey(), request.getStatus());
    }

    // order item 제작 가능 판정
    @Transactional
    public List<OrderItemResponse> confirmOrderItem(FactoryOrderDTO.ConfirmRequest request){
        Factory factory = getValidFactory();
        List<OrderItemResponse> response = factoryOrderRepository.confirmOrderItem(factory.getFactoryKey(), factory.getFactoryType(), request);

        // 아이템 리스트 결과가 존재하지 않을때 해당 아이템이 속한 주문 상태 업데이트
        if(response == null || response.isEmpty()){
            factoryOrderRepository.updateOrderStatus(request.getOrderKey(), OrderReceivedStatus.CREATING);
        }

        return response;
    }

    // order item 제작 불가 판정
    @Transactional
    public RejectedItemResponse rejectOrderItem(FactoryOrderDTO.RejectRequest request){
        Factory factory = getValidFactory();
        return factoryOrderRepository.rejectOrderItem(factory.getFactoryKey(), factory.getFactoryType(), request);
    }

    // order item 송장 등록
    @Transactional
    public List<OrderItemResponse> startShipping(FactoryOrderDTO.StartShippingRequest request){
        Factory factory = getValidFactory();
        List<OrderItemResponse> response = factoryOrderRepository.startShippingAndReturnRemainedItems(factory.getFactoryKey(), factory.getFactoryType(), request);

        // 아이템 리스트 결과가 존재하지 않을때 해당 아이템이 속한 주문 상태 업데이트
        if(response == null || response.isEmpty()){
            factoryOrderRepository.updateOrderStatus(request.getOrderKey(), OrderReceivedStatus.SHIPPING);
        }

        return response;
    }

    // 납기일 별 주문 수 조회
    @Transactional(readOnly = true)
    public List<CalendarOrderCountsResponse> getShipmentDateOrderCount(List<String> yearMonth){
        Factory factory = getValidFactory();
        return factoryOrderRepository.getShipmentDateOrderCount(
                factory.getFactoryKey(), factory.getFactoryType(), yearMonth);
    }

    // 최근 정산내역 조회
    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentlySettlementOrders(){
        Factory factory = getValidFactory();
        return factoryOrderRepository.getRecentlySettlementOrders(
                factory.getFactoryKey(), factory.getFactoryType()
        );
    }

//  --------------------------------- 클래스 내부 유틸

    // 인증객체를 사용해서 FactoryType 조회
    private Factory getValidFactory() {
        JwtPrincipal principal = jwtUtil.getAuthPrincipalObject();
        // 인증객체의 역할이 Factory 인지 확인
        if (principal.getRole() != MemberRole.FACTORY && principal.getRole() != MemberRole.ADMIN) {
            throw new CustomLogicException(ExceptionCode.NOT_FACTORY_ADMIN);
        }
        // 공장관리자가 소속된 Factory 의 가공가능 유형 리턴
        return factoryAdminRepository.findFactoryByMemberKey(principal.getMemberKey());
    }

//    // 상태값 검증 메서드
//    private void isValidStatus(List<OrderReceivedStatus> statusList){
//        for(OrderReceivedStatus status : statusList){
//            boolean isValid = EnumSet.allOf(OrderReceivedStatus.class).contains(status);
//            if(! isValid){
//                throw new CustomLogicException(ExceptionCode.INVALID_ORDER_STATUS, "허용되지 않은 상태값 : " + status);
//            }
//        }
//    }

}
