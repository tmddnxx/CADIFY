package com.cadify.cadifyWAS.service.order;

import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.CartMapper;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import com.cadify.cadifyWAS.model.entity.member.OAuthMember;
import com.cadify.cadifyWAS.model.entity.cart.Cart;
import com.cadify.cadifyWAS.model.entity.cart.CartItem;
import com.cadify.cadifyWAS.repository.Files.FilesRepository;
import com.cadify.cadifyWAS.service.file.enumValues.common.Shipment;
import com.cadify.cadifyWAS.util.JwtUtil;
import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.mapper.CartItemMapper;
import com.cadify.cadifyWAS.model.dto.cart.CartDTO;
import com.cadify.cadifyWAS.model.dto.cart.CartItemDTO;
import com.cadify.cadifyWAS.repository.CartItemRepository;
import com.cadify.cadifyWAS.repository.CartRepository;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.cadify.cadifyWAS.exception.ExceptionCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final EstimateRepository estimateRepository;
    private final CartItemMapper cartItemMapper;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FilesRepository filesRepository;
    private final CartMapper cartMapper;
    private final JwtUtil jwtUtil;

    /**
     * 장바구니에 견적(상품) 담는 로직
     * @param cartRequest
     * List<Long> estIdList
     */
    @Transactional
    public List<Long> addCartIem(CartDTO.Request cartRequest) {

        OAuthMember loginMember = jwtUtil.getLoginMember();

        Cart cart = getMyCart(loginMember);

        // 장바구니와 추가하려는 견적 중 중복된 견적 리스트
        List<Long> duplicateEstKeyList = getDuplicateEstKeyList(cartRequest, cart);

        if(!duplicateEstKeyList.isEmpty()) {
            if (!cartRequest.isOverwrite()) {
                // 덮어쓰기 허용 안하면 중복된 아이템 리스트 반환
                return duplicateEstKeyList;
            } else {
                cartItemRepository.deleteAllByIdInBatch(duplicateEstKeyList);
                cartItemRepository.flush(); // 삭제를 즉시 DB에 반영
            }
        }

        addEstimatesToCart(cartRequest, cart);

        List<CartItem> cartItems = cartItemRepository.findAllByCartKey(cart.getCartKey());
        cart.recalculate(cartItems);

        cartRepository.save(cart);
        return null;
    }

    /**
     * 장바구니에서 견적(상품) 삭제
     * @param cartItemKey : 삭제할 견적(상품)의 장바구니아이템 키값
     */
    @Transactional
    public void deleteCartItem(Long cartItemKey) {
        OAuthMember loginMember = jwtUtil.getLoginMember();

        Cart cart = cartRepository.findCartByMemberKey(loginMember.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findCartItemsByCartItemKey(cartItemKey)
                .orElseThrow(() -> new CustomLogicException(CART_ITEM_NOT_FOUND));

        cartItemRepository.delete(cartItem);

        // 여기서 남아 있는 카트 아이템 다시 조회
        List<CartItem> remainingCartItems = cartItemRepository.findAllByCartKey(cart.getCartKey());

        cart.recalculate(remainingCartItems);
    }

    /**
     * 내 장바구니 조회 (조회할때마다 출하일도 오늘 기준으로 새로 계산)
     * @return : 장바구니 아이템 DTO 리스트
     */
    @Transactional
    public CartDTO.GetCartResponse getCart() {

        OAuthMember loginMember = jwtUtil.getLoginMember();

        List<CartItemDTO.GetCartResponse> cartItems = new ArrayList<>();

        // 장바구니 없으면 장바구니 생성함
        Cart cart = cartRepository.findCartByMemberKey(loginMember.getMemberKey())
                .orElse(Cart.builder().memberKey(loginMember.getMemberKey()).build());

        List<CartItem> allByCartItemKeys = cartItemRepository.findAllByCartKey(cart.getCartKey());

        // 장바구니 아이템 dto 변환 + 출하일 새로 계산
        for(CartItem cartItem: allByCartItemKeys) {
            cartItem.updateShipmentDate();
            CartItemDTO.GetCartResponse cartItemResponse = cartItemMapper.cartItemToCartItemResponse(cartItem);
            cartItems.add(cartItemResponse);
        }

        return cartMapper.CartToGetCartResponse(cart, cartItems);

    }

    @Transactional
    public void updateCartItemAmount(CartItemDTO.UpdateAmount updateRequest) {

        OAuthMember loginMember = jwtUtil.getLoginMember();

        Cart cart = cartRepository.findCartByMemberKey(loginMember.getMemberKey())
                .orElseThrow(() -> new CustomLogicException(CART_NOT_FOUND));

        CartItem cartItem = cartItemRepository.findCartItemsByCartItemKey(updateRequest.getCartItemKey())
                .orElseThrow(() -> new CustomLogicException(CART_ITEM_NOT_FOUND));

        List<CartItem> cartItems = cartItemRepository.findAllByCartKey(cart.getCartKey()); // DB 조회
        cartItem.updateAmount(updateRequest.getAmount());
        cartItemRepository.save(cartItem);
        cart.recalculate(cartItems);

        cartRepository.save(cart);
    }

    /**
     * estId 장바구니에서 삭제
     * @param estKey 고유한 estId 값
     */
    @Transactional
    public void deleteCartItemByEstKey(String estKey) {
        OAuthMember loginMember = jwtUtil.getLoginMember();

        cartItemRepository.deleteCartItemsByEstKey(estKey);
    }

    @NotNull
    private List<Long> getDuplicateEstKeyList(CartDTO.Request cartRequest, Cart cart) {
        // 기존 장바구니의 estKey → cartItemKey 매핑을 저장할 Map
        Map<String, Long> cartItemEstKeyMap = new HashMap<>();

        // 추가할 장바구니 아이템의 견적 키 리스트
        List<CartDTO.Request.EstKeyAmount> estKeyListRequest = cartRequest.getEstKeyList();

        // 원래 장바구니에 있던 장바구니 아이템 키 리스트
        List<CartItem> oldCartItem = cartItemRepository.findAllByCartKey(cart.getCartKey());

        // cartItemKey -> estId 매핑 저장
        for (CartItem cartItem : oldCartItem) {
            cartItemEstKeyMap.put(cartItem.getEstKey(), cartItem.getCartItemKey());
        }

        List<Long> duplicationCartItemKeyList = estKeyListRequest.stream()
                .map(item -> item.getEstKey().trim()) // 공백 제거
                .filter(cartItemEstKeyMap::containsKey)
                .map(cartItemEstKeyMap::get)
                .collect(Collectors.toList());
        return duplicationCartItemKeyList;
    }

    @NotNull
    private Cart getMyCart(OAuthMember loginMember) {
        Cart cart = cartRepository.findCartByMemberKey(loginMember.getMemberKey())
                .orElse(Cart.builder().memberKey(loginMember.getMemberKey()).build());
        cartRepository.save(cart);
        return cart;
    }

    private static LocalDate getShipmentDate(CartItem cartItem) {
        if(cartItem.getMethod().equals("cnc")){
            if(cartItem.isFastShipment()){
                return Shipment.getShipmentDateByCNC(cartItem.getExpressShipmentDay());
            } else {
                return Shipment.getShipmentDateByCNC(cartItem.getStandardShipmentDay());
            }
        } else if(cartItem.getMethod().equals("sheet_metal")){
            if(cartItem.isFastShipment()){
                return Shipment.getShipmentDateByMetal(cartItem.getExpressShipmentDay());
            } else {
                return Shipment.getShipmentDateByMetal(cartItem.getStandardShipmentDay());
            }
        } else {
            throw new CustomLogicException(ExceptionCode.CALCULATE_SHIPMENT_DATE_FAILED);
        }
    }

    private void addEstimatesToCart(CartDTO.Request cartRequest, Cart cart) {
        // 장바구니에 견적 추가하는 로직
        for(CartDTO.Request.EstKeyAmount item: cartRequest.getEstKeyList()) {
            //존재하는 견적인지 확인
            Estimate estimate = estimateRepository.findByEstKey(item.getEstKey())
                    .orElseThrow(() -> new CustomLogicException(ESTIMATE_NOT_FOUND));

            // 견적 금액 먼저 확인
            if (estimate.getPrice() == 0) continue;

            Files files = filesRepository.findById(estimate.getFileId())
                    .orElseThrow(() -> new CustomLogicException(FILES_NOT_FOUNT));

            // 견적을 장바구니 아이템으로 변환
            CartItem cartItem = cartItemMapper.estimateToCartItem(cart, estimate, files, item.getAmount());
            cartItem.updateTotalPrice();

            // 납기일 계산해서 CartItem에 업데이트
            cartItem.updateShipmentDate();
            cartItemRepository.save(cartItem);
        }
    }
}
