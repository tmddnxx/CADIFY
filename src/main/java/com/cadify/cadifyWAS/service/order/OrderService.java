package com.cadify.cadifyWAS.service.order;

import com.cadify.cadifyWAS.infra.order.RetryOnOptimisticLock;
import com.cadify.cadifyWAS.model.entity.member.CompanyManager;
import com.cadify.cadifyWAS.model.entity.member.MemberRole;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.entity.cart.Cart;
import com.cadify.cadifyWAS.model.entity.cart.CartItem;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.model.entity.order.Orders;
import com.cadify.cadifyWAS.model.entity.payment.Payment;
import com.cadify.cadifyWAS.repository.company.CompanyManagerRepository;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.mapper.AddressMapper;
import com.cadify.cadifyWAS.mapper.OrderItemMapper;
import com.cadify.cadifyWAS.mapper.OrderMapper;
import com.cadify.cadifyWAS.model.dto.order.AddressDTO;
import com.cadify.cadifyWAS.model.dto.order.OrdersDTO;
import com.cadify.cadifyWAS.model.entity.*;
import com.cadify.cadifyWAS.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;

import static com.cadify.cadifyWAS.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final JwtUtil jwtUtil;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final PaymentRepository paymentRepository;
    private final WebClient.Builder webClientBuilder;
    private final CompanyManagerRepository managerRepository;

    private final RestTemplate restTemplate;


    @Value("${delivery.client}")
    private String deliveryClient;
    @Value("${delivery.secret}")
    private String deliverySecret;

    /**
     * 견적 주문 로직
     * @param orderRequest
     * List<Long> cartItemKey : 장바구니 상품 키값
     */
    @Transactional
    @RetryOnOptimisticLock
    public OrdersDTO.CreateResponse createOrder(OrdersDTO.Request orderRequest) {

        List<OrderItem> orderItems = new ArrayList<>();
        OAuthMember loginMember = jwtUtil.getLoginMember();

        Cart cart = cartRepository.findCartByMemberKey(loginMember.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(CART_NOT_FOUND));

        Orders order;

        if (loginMember.getRole().equals(MemberRole.USER) || loginMember.getRole().equals(MemberRole.ADMIN)) {
            if (orderRequest.getManagerKey() != null && !orderRequest.getManagerKey().isBlank()) {
                throw new CustomLogicException(ExceptionCode.INVALID_MANAGER);
            }
            // 일반 회원 주문 생성
            order = createByUserMember(orderRequest, loginMember, cart);
        } else if (loginMember.getRole().equals(MemberRole.COMPANY)) {
            if (orderRequest.getManagerKey() == null || orderRequest.getManagerKey().isBlank()) {
                throw new CustomLogicException(ExceptionCode.MANAGER_REQUIRED);
            }
            validManager(orderRequest); // 담당자 유효성 검사
            // 회사 회원 주문 생성
            order = createByCompanyMember(orderRequest, loginMember, cart);
        } else{
            throw new RuntimeException();
        }

        Orders saveOrder = orderRepository.save(order);

        createOrderItem(orderRequest, saveOrder, orderItems, loginMember);

        if(order.getTotalPrice() < 7000){
            throw new CustomLogicException(ExceptionCode.MinimumOrderAmountNotMetException);
        }

        updateCart(cart);

        orderRepository.save(order);
        cartRepository.save(cart);

        return OrdersDTO.CreateResponse.builder()
                .orderKey(order.getOrderKey())
                .build();
    }

    private static Orders createByCompanyMember(OrdersDTO.Request orderRequest, OAuthMember loginMember, Cart cart) {
        return Orders.builder()
                .memberKey(loginMember.getMemberKey())
                .deliveryCharge(cart.getDeliveryCharge())
                .vat(cart.getVat())
                .totalPrice(cart.getCartTotalPrice())
                .orderDiscount(cart.getCartDiscount())
                .orderTotalPaymentPrice(cart.getCartTotalPaymentPrice())
                .addressKey(orderRequest.getAddressKey())
                .deliveryMemo(orderRequest.getDeliveryMemo())
                .managerKey(orderRequest.getManagerKey())
                .build();
    }

    private static Orders createByUserMember(OrdersDTO.Request orderRequest, OAuthMember loginMember, Cart cart) {
        return Orders.builder()
                .memberKey(loginMember.getMemberKey())
                .deliveryCharge(cart.getDeliveryCharge())
                .vat(cart.getVat())
                .totalPrice(cart.getCartTotalPrice())
                .orderDiscount(cart.getCartDiscount())
                .orderTotalPaymentPrice(cart.getCartTotalPaymentPrice())
                .addressKey(orderRequest.getAddressKey())
                .deliveryMemo(orderRequest.getDeliveryMemo())
                .build();

    }

    private void validManager(OrdersDTO.Request orderRequest) {
        CompanyManager manager = managerRepository.findByManagerKey(orderRequest.getManagerKey())
                .orElseThrow(() -> new CustomLogicException(MANAGER_NOT_FOUND));
    }

    private void updateCart(Cart cart) {
        // 장바구니 후처리
        List<CartItem> remainingCartItems = cartItemRepository.findAllByCartKey(cart.getCartKey());
        cart.recalculate(remainingCartItems);
    }

    private void createOrderItem(OrdersDTO.Request orderRequest, Orders saveOrder, List<OrderItem> orderItems, OAuthMember loginMember) {
        //장바구니 아이템에서 결제할 것만 빼서 주문으로 넘김(하나의 주문에 여러 상품 있도록)
        LocalDate minLocalDate = null;
        for(Long cartItemKey: orderRequest.getCartItemKey()) {
            CartItem cartItem = cartItemRepository.findById(cartItemKey)
                    .orElseThrow(() -> new CustomLogicException(CART_ITEM_NOT_FOUND));

            // 주문할때 현재 기준으로 출하일 새로 계산
            cartItem.updateShipmentDate();

            // 카트에서 주문으로 변환
            OrderItem orderItem = orderItemMapper.cartItemToOrderItem(cartItem, saveOrder.getOrderKey(), loginMember.getMemberKey());
            orderItemRepository.save(orderItem);

            LocalDate shipmentDate = orderItem.getShipmentDate();

            // 최소 납기일 갱신
            if (shipmentDate != null) {
                if (minLocalDate == null || shipmentDate.isBefore(minLocalDate)) {
                    minLocalDate = shipmentDate;
                }
            }
            // 주문으로 견적 옮기고 장바구니에서는 삭제
            orderItems.add(orderItem);
            cartItemRepository.delete(cartItem);
        }
        saveOrder.updateShipmentDate(minLocalDate);
    }

    /**
     * 내 주문 내역 전체 조회 로직
     * @return 주문 내역 DTO
     */
    public List<OrdersDTO.AllResponse> getAllOrder() {
        OAuthMember loginMember = jwtUtil.getLoginMember();

        // 내 주문 목록 가져오기
        List<Orders> orderList = orderRepository.findByMemberKeyOrderByCreatedAtDesc(loginMember.getMemberKey());
        List<OrdersDTO.AllResponse> orderDTOList = new ArrayList<>();

        // DTO로 변환 후 반환
        // TODO: 효율이 너무 안좋아 보임 O(n)번 디비에 커넥션 연결
        for(Orders order : orderList) {
            OrdersDTO.AllResponse allResponse = orderMapper.orderToAllOrderResponse(order);
            orderDTOList.add(allResponse);
        }

        return orderDTOList;
    }


    /**
     * 주문 단건 조회
     * 단건 조회에서 배송 정보를 통해서 주문 상태를 최신화 하기 때문에 @Transactional
     */
    @Transactional
    public OrdersDTO.Response getOrder(String orderKey) {
        Orders order = orderRepository.findOrderByOrderKey(orderKey)
                .orElseThrow(() -> new CustomLogicException(ORDER_NOT_FOUND));

        Address address = addressRepository.findByAddressKey(order.getAddressKey())
                .orElseThrow(() -> new CustomLogicException(ADDRESS_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(orderKey);

        // 배송 상태 최신화
        for (OrderItem orderItem : orderItems) {
            //배송회사나 송장번호가 없으면 뛰어넘음
            String courier = orderItem.getCourier();
            String trackingNumber = orderItem.getTrackingNumber();

            if (courier == null || courier.isBlank() || trackingNumber == null || trackingNumber.isBlank()) {
                continue;
            }
            updateDeliveryStatus(orderItem);
        }

        Optional<Payment> tossPayment = paymentRepository.findByOrderKey(order.getOrderKey());

        order.updateOrderReceivedStatus(orderItems);

        return orderMapper.orderToOrderResponse(order, address, tossPayment);
    }

    /**
     * 내 주소 전체 조회
     */
    public List<AddressDTO.Response> getMyAddress() {

        OAuthMember loginMember = jwtUtil.getLoginMember();

        List<Address> myAddress = addressRepository.findAllByMemberKey(loginMember.getMemberKey());
        List<AddressDTO.Response> addressDTOList = new ArrayList<>();

        for(Address address : myAddress) {
            AddressDTO.Response addressDTO = addressMapper.addressToAddressDTO(address);
            addressDTOList.add(addressDTO);
        }

        return addressDTOList;
    }

    /**
     * 주문 삭제
     */
    @Transactional
    public void deleteOrder(String orderKey) {

        // TODO: dxf 다운전까지

        Orders order = orderRepository.findOrderByOrderKey(orderKey)
                .orElseThrow(() -> new CustomLogicException(ORDER_NOT_FOUND));

        orderRepository.delete(order);
    }


    /**
     * 주소 상세 내용 변경
     * @param addressKey 주소 pk
     */
    @Transactional
    public void updateAddress(String addressKey, AddressDTO.Request request) {

        Address address = addressRepository.findByAddressKey(addressKey)
                .orElseThrow(() -> new CustomLogicException(ADDRESS_NOT_FOUND));

        address.updateAddress(request.getAddress(), request.getAddressDetail(), request.getAddressLabel());
        addressRepository.save(address);
    }

    /**
     * 주소 생성
     */
    @Transactional
    public String createAddress(AddressDTO.CreateRequest request) {

        OAuthMember loginMember = jwtUtil.getLoginMember();

        Address address = Address.builder()
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .addressLabel(request.getAddressLabel())
                .addressDetail(request.getAddressDetail())
                .memberKey(loginMember.getMemberKey())
                .build();

        addressRepository.save(address);

        return address.getAddressKey();
    }

    /**
     * 주소 삭제
     */
    @Transactional
    public void deleteAddress(String addressKey) {

        //먼저 존재하는 주소인지 확인
        addressRepository.findByAddressKey(addressKey)
                        .orElseThrow(()->new CustomLogicException(ADDRESS_NOT_FOUND));

        addressRepository.deleteById(addressKey);
    }

    public OrderReceivedStatus getOrderReceivedStatus(String estKey) {
        OrderItem orderItem = orderItemRepository.findByEstKey(estKey)
                .orElseThrow(() -> new CustomLogicException(ORDER_ITEM_NOT_FOUND_BY_ESTKEY));

        return orderItem.getOrderReceivedStatus();
    }

    private void updateDeliveryStatus(OrderItem orderITem) {

        WebClient webClient = webClientBuilder.baseUrl("https://apis.tracker.delivery").build();

        String query = """
        query Track($carrierId: ID!, $trackingNumber: String!) {
          track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
            lastEvent {
              time
              status {
                code
              }
            }
          }
        }
    """;

        Map<String, Object> body = Map.of(
                "query", query,
                "variables", Map.of(
                        "carrierId", orderITem.getCourier(),
                        "trackingNumber", orderITem.getTrackingNumber()
                )
        );

        String authHeader = "TRACKQL-API-KEY " + deliveryClient + ":" + deliverySecret;

        String responseJson = webClient.post()
                .uri("/graphql")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // JSON 파싱
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseJson);
            String status = root.path("data")
                    .path("track")
                    .path("lastEvent")
                    .path("status")
                    .path("code")
                    .asText();// 예: "DELIVERED"

            if (status.equals("DELIVERED")) {
                orderITem.updateStatusDelivered();
            }

        } catch (Exception e) {
            System.err.println("배송 상태 응답 파싱 오류: " + e.getMessage());
        }
    }

    public void updateDeliveryStatusByRestTemplate(OrderItem orderItem) {
        String url = "https://apis.tracker.delivery/graphql";

        String query = """
        query Track($carrierId: ID!, $trackingNumber: String!) {
          track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
            lastEvent {
              time
              status {
                code
              }
            }
          }
        }
        """;

        Map<String, Object> body = Map.of(
                "query", query,
                "variables", Map.of(
                        "carrierId", orderItem.getCourier(),
                        "trackingNumber", orderItem.getTrackingNumber()
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authHeader = "TRACKQL-API-KEY " + deliveryClient + ":" + deliverySecret;
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            String responseJson = response.getBody();

            // JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseJson);
            String status = root.path("data")
                    .path("track")
                    .path("lastEvent")
                    .path("status")
                    .path("code")
                    .asText();

            if (status.equals("DELIVERED")) {
                orderItem.updateStatusDelivered();
            }

        } catch (Exception e) {
            System.err.println("배송 상태 응답 파싱 오류: " + e.getMessage());
        }
    }
}
