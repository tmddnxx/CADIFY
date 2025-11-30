package com.cadify.cadifyWAS.service.payment;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.infra.order.RetryOnOptimisticLock;
import com.cadify.cadifyWAS.mapper.OrderItemMapper;
import com.cadify.cadifyWAS.mapper.OrderMapper;
import com.cadify.cadifyWAS.mapper.PaymentMapper;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.order.OrderItemDTO;
import com.cadify.cadifyWAS.model.dto.order.OrdersDTO;
import com.cadify.cadifyWAS.model.dto.payment.InvoiceDto;
import com.cadify.cadifyWAS.model.dto.payment.PaymentDTO;
import com.cadify.cadifyWAS.model.entity.Address;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.model.entity.order.OrderItem;
import com.cadify.cadifyWAS.model.entity.order.OrderReceivedStatus;
import com.cadify.cadifyWAS.model.entity.order.Orders;
import com.cadify.cadifyWAS.model.entity.payment.Payment;
import com.cadify.cadifyWAS.repository.AddressRepository;
import com.cadify.cadifyWAS.repository.OrderItemRepository;
import com.cadify.cadifyWAS.repository.OrderRepository;
import com.cadify.cadifyWAS.repository.PaymentRepository;
import com.cadify.cadifyWAS.service.file.common.EstimateStatus;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import jakarta.transaction.Transactional;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final JwtUtil jwtUtil;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final WebClient webClientNicePayment;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;

    @Value("${payment.nicepay.secret_api_key}")
    String secretKey;

    @Value("${payment.nicepay.client_api_key}")
    String clientKey;

    @Transactional
    @RetryOnOptimisticLock
    public OrdersDTO.SuccessResponse confirmPayment(List<EstimateDTO.EstimateValidStatus> estimateValidStatusList, PaymentDTO.Confirm confirmRequest) throws IOException, InterruptedException {

        estimateStatusVerify(estimateValidStatusList);

        Orders order = verifyPayment(confirmRequest);

        String confirmResponse = requestConfirm(confirmRequest);

        JsonNode root = objectMapper.readTree(confirmResponse);
        String resultCode = root.path("resultCode").asText();

        if(!Objects.equals(resultCode, "0000")){
            throw new CustomLogicException(ExceptionCode.PAYMENT_FAILED);
        }

        // Payment 객체 생성
        Payment payment = createPayment(root);

        // 주문 상태 최신화
        updateStatusAfterPaymentConfirm(confirmRequest);

        // 응답 생성
        return getSuccessResponse(confirmRequest.getOrderId(), payment);
    }

    private static void estimateStatusVerify(List<EstimateDTO.EstimateValidStatus> estimateValidStatusList) throws JsonProcessingException {
        List<EstimateDTO.EstimateValidStatus> invalidEstimates = new ArrayList<>();

        for (EstimateDTO.EstimateValidStatus estimateValidStatus : estimateValidStatusList) {
            if (estimateValidStatus.getStatus() != EstimateStatus.SUCCESS) {
                invalidEstimates.add(estimateValidStatus);
            }
        }

        if (!invalidEstimates.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(invalidEstimates);

            throw new CustomLogicException(
                    ExceptionCode.INVALID_ESTIMATE_STATUS,
                    jsonMessage
            );
        }
    }

    private void updateStatusAfterPaymentConfirm(PaymentDTO.Confirm confirmRequest) {
        Orders order = orderRepository.findById(confirmRequest.getOrderId())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(confirmRequest.getOrderId());
        // 주문 상태 결제됨으로 변경
        order.successPaid(orderItems);
    }

    @NotNull
    private Payment createPayment(JsonNode root) {
        Payment payment = Payment.builder()
                .resultCode(root.path("resultCode").asText())
                .resultMsg(root.path("resultMsg").asText())
                .tid(root.path("tid").asText())
                .orderKey(root.path("orderId").asText())
                .status(root.path("status").asText())
                .paidAt(root.path("paidAt").asText())
                .failedAt(root.path("failedAt").asText())
                .cancelledAt(root.path("cancelledAt").asText())
                .payMethod(root.path("payMethod").asText())
                .amount(root.path("amount").asInt())
                .balanceAmt(root.path("balanceAmt").asInt())
                .goodsName(root.path("goodsName").asText())
                .receiptUrl(root.path("receiptUrl").asText())
                .build();
        paymentRepository.save(payment);
        return payment;
    }

    private OrdersDTO.SuccessResponse getSuccessResponse(String orderKey, Payment payment) {

        Orders order = orderRepository.findById(orderKey)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        Address address = addressRepository.findByAddressKey(order.getAddressKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ADDRESS_NOT_FOUND));

        List<OrderItem> orderItemList = orderItemRepository.findAllByOrderKey(order.getOrderKey());

        List<OrderItemDTO.Response> orderItemDTOList = new ArrayList<>();

        for (OrderItem orderItem : orderItemList) {
            orderItemDTOList.add(orderItemMapper.orderItemToOrderItemResponse(orderItem));
        }

        PaymentDTO.SuccessResponse nicePaymentDTO = paymentMapper.toPaymentDTO(payment);

        return orderMapper.orderToOrderSuccess(order, orderItemDTOList, address, nicePaymentDTO);
    }

    @NotNull
    private Orders verifyPayment(PaymentDTO.Confirm confirmRequest) {
        // 해당 주문없으면 예외
        Orders order = orderRepository.findOrderByOrderKey(confirmRequest.getOrderId())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        //주문 결제 되어있으면 예외
        if(order.isPaid()){
            throw new CustomLogicException(ExceptionCode.ORDER_ALREADY_PAID);
        }

        int requestPrice = Integer.parseInt(confirmRequest.getAmount());

        // request로 들어온 amount랑 order의 totalPrice랑 같아야함
        if(requestPrice != order.getTotalPrice()){
            throw new CustomLogicException(ExceptionCode.ORDER_AMOUNT_MISMATCH);
        }
        return order;
    }

    /**
     * 나이스페이 결제 승인
     */
    public String requestConfirm(PaymentDTO.Confirm confirmPaymentRequest) {
        String amount = confirmPaymentRequest.getAmount();

        // WebClient 요청
        return webClientNicePayment.post()
                .uri("/{tid}", confirmPaymentRequest.getTid()) // baseUrl 뒤에 /{tid} 붙여짐
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json;charset=utf-8")
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 유저 결제 취소 요청
     */
    @Transactional
    @RetryOnOptimisticLock
    public void cancelPaymentByUser(String tid, PaymentDTO.CancelRequest cancelRequest) throws IOException, InterruptedException {
        // API URL 생성
        Payment nicePayment = paymentRepository.findByTid(tid)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.PAYMENT_NOT_FOUND));
        String orderKey = nicePayment.getOrderKey();

        String cancelResponse = requestCancel(tid, orderKey, cancelRequest);

        JsonNode root = objectMapper.readTree(cancelResponse);
        String resultCode = root.path("resultCode").asText();
        String resultMsg = root.path("resultMsg").asText();
        log.info("resultMsg = {}", resultMsg);

        if(!Objects.equals(resultCode, "0000")){
            throw new CustomLogicException(ExceptionCode.PAYMENT_CANCEL_FAILED, resultMsg);
        }

        updateStatusAfterPaymentCancel(nicePayment);
    }

    private void updateStatusAfterPaymentCancel(Payment nicePayment) {
        Orders orders = orderRepository.findByOrderKey(nicePayment.getOrderKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(orders.getOrderKey());

        // 주문 상태 취소로 업데이트
        orderItems.forEach(OrderItem::updateStatusCanceled);
        orders.updateOrderReceivedStatus(orderItems);
    }

    public String requestCancel(String tid, String orderKey, PaymentDTO.CancelRequest cancelRequest){

        return webClientNicePayment.post()
                .uri("/{tid}/cancel", tid)
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json;charset=utf-8")
                .bodyValue(Map.of(
                        "reason", cancelRequest.getCancelReason(),
                        "orderId", orderKey
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 또는 Mono<String> 반환
    }

    private String getAuthorization() {
        String credentials = clientKey + ":" + secretKey; // 클라이언트 아이디 + 시크릿 키
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 거래 명세서 만드는 메소드
     * 주문 정보 pdf로 변환하는 로직
     */
    public byte[] generateInvoicePdf(InvoiceDto.Request request) {
        Context context = getHtmlContext(request);

        String htmlContent = templateEngine.process("invoice", context);

        return createPdf(htmlContent);
    }

    @NotNull
    private Context getHtmlContext(InvoiceDto.Request request) {
        Context context = new Context();
        context.setVariable("issueDate", LocalDate.now().toString());
        context.setVariable("customerName", request.getCompanyName());
        context.setVariable("receiverName", request.getReceiverName());

        // 거래 데이터 가져오기
        OAuthMember loginMember = jwtUtil.getLoginMember();
        List<OrderItem> orderItems = orderItemRepository.findAllByMemberKey(loginMember.getMemberKey());

        double totalAmount = 0.0;
        double totalTax = 0.0;

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            // 결제 안된 주문이면 continue
            if(orderItem.getOrderReceivedStatus() == OrderReceivedStatus.PAYMENT_PENDING) continue;

            // 품목 정보 생성
            Map<String, Object> item = new HashMap<>();
            item.put("name", orderItem.getEstName());
            item.put("quantity", orderItem.getAmount());
            item.put("unitPrice", orderItem.getPrice());
            item.put("supplyPrice", orderItem.getTotalPrice());
            item.put("tax", orderItem.getTotalPrice() / 10.0);

            itemList.add(item);

            // 총액 계산
            totalAmount += orderItem.getTotalPrice();
            totalTax += orderItem.getTotalPrice() / 10.0;

        }

        double totalWithVat = totalAmount + totalTax;

        context.setVariable("items", itemList);
        context.setVariable("totalAmount", totalAmount);
        context.setVariable("totalWithVat", totalWithVat);
        return context;
    }

    private static byte [] createPdf(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver()
                    .addFont(new ClassPathResource("/templates/NanumBarunGothic.ttf")
                                    .getURL()
                                    .toString(),
                            BaseFont.IDENTITY_H,
                            BaseFont.EMBEDDED);
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new CustomLogicException(ExceptionCode.CONVERT_PDF_FAILED, e.getMessage());
        }
    }

    // 영수증 발급
    public PaymentDTO.ReceiptResponse downloadReceipt(String orderKey) {

        Payment payment = paymentRepository.findByOrderKey(orderKey)
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.PAYMENT_NOT_FOUND));

        return PaymentDTO.ReceiptResponse.builder()
                .receiptUrl(payment.getReceiptUrl())
                .build();
    }

    public int recalculateDeliveryCharge(List<OrderItem> orderItems){
        Set<LocalDate> shipmentDates = orderItems.stream()
                .map(OrderItem::getShipmentDate)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return shipmentDates.size() * 7000;
    }

    @Transactional
    public void cancelPaymentByNetwork(PaymentDTO.CancelRequestByNetwork cancelRequest) {
        log.warn("망 취소 요청됨. 요청 주문 key = {}", cancelRequest.getOrderId());
        // 해당 주문키를 가진 결제 삭제
        paymentRepository.deleteByOrderKey(cancelRequest.getOrderId());
        Orders order = orderRepository.findById(cancelRequest.getOrderId())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(order.getOrderKey());
        //주문의 상태 취소로 변경
        order.updateStatusToCanceled(orderItems);
    }

    public PaymentDTO.Response getPayment(String tid) {
        return webClientNicePayment.get()
                .uri("/{tid}",tid)
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("결제 조회 실패: statusCode={}, body={}", response.statusCode(), errorBody);
                                    return Mono.error(new CustomLogicException(ExceptionCode.GET_PAYMENT_FAILED, errorBody));
                                }))
                .bodyToMono(PaymentDTO.Response.class)
                .block();
    }

    /**
     * 현금 영수증 요청
     */
//    private HttpResponse<String> requestCashReceipt(PaymentDTO.Confirm confirmPaymentRequest) throws IOException, InterruptedException {
//
//        Orders orders = orderRepository.findById(confirmPaymentRequest.getOrderId())
//                .orElseThrow(() -> new CustomLogicException(ExceptionCode.ORDER_NOT_FOUND));
//
//        List<OrderItem> orderItems = orderItemRepository.findAllByOrderKey(orders.getOrderKey());
//
//        String goodsName = orderItems.get(0).getFileName();
//
//
//        String orderId = confirmPaymentRequest.getOrderId();
//        String amount = confirmPaymentRequest.getAmount();
//
//
//        ObjectNode requestObj = objectMapper.createObjectNode()
//                .put("orderId", orderId)
//                .put("amount", amount)
//                .put("goodsName", goodsName) //
//                .put("receiptType", type)
//                .put("receiptNo", customerIdentityNumber)
//                .put("supplyAmt") // 공급 가액
//                .put("goodsVat") // 부가 가치 세
//                .put("taxFreeAmt", 0) // 면세료
//                .put("serviceAmt", 0); // 봉사료
//
//        String requestBody = objectMapper.writeValueAsString(requestObj);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create("https://api.tosspayments.com/v1/cash-receipt"))
//                .header("Authorization", getAuthorizations())
//                .header("Content-Type", "application/json")
//                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
//                .build();
//
//        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
//    }

}
