package com.cadify.cadifyWAS.service.orchestrator;

import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.service.file.EstimateService;
import com.cadify.cadifyWAS.service.order.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class EstimateCartFacade {

    private final CartService cartService;
    private final EstimateService estimateService;

    // 견적 수정 시 카트에 담긴 아이템 삭제
    public EstimateDTO.StatusResponse deleteCartItemAtEstimateModifyingWithMetal(EstimateDTO.MetalOptionPut optionPut) {
        EstimateDTO.StatusResponse result = estimateService.putOption(optionPut);
        cartService.deleteCartItemByEstKey(optionPut.getEstKey());
        return result;
    }

    public EstimateDTO.StatusResponse deleteCartItemAtEstimateModifyingWithCNC(EstimateDTO.CnCOptionPut optionPut) {
        EstimateDTO.StatusResponse result = estimateService.putCncOption(optionPut);
        cartService.deleteCartItemByEstKey(optionPut.getEstKey());
        return result;
    }
}
