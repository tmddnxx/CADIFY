package com.cadify.cadifyWAS.service.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.FactoryMapper;
import com.cadify.cadifyWAS.model.dto.factory.estimate.FactoryEstimateDTO;
import com.cadify.cadifyWAS.repository.factory.estimate.FactoryEstimateQueryRepository;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class FactoryEstimateService {
    private final FactoryEstimateQueryRepository factoryEstimateQueryRepository;
    private final FactoryMapper factoryMapper;

    public FactoryEstimateDTO.EstimateResponse getEstimate(String orderItemKey) {
        Tuple tuple = factoryEstimateQueryRepository.findEstimateByorderItemKey(orderItemKey);
        if (tuple == null) {
            throw new CustomLogicException(ExceptionCode.ORDER_ITEM_NOT_FOUND);
        }
        FactoryEstimateDTO.EstimateResponse estimateResponse = null;
        try {
            estimateResponse = factoryMapper.estimateToResponse(tuple);
        } catch (JsonProcessingException e) {
            throw new CustomLogicException(ExceptionCode.UNKNOWN_EXCEPTION_OCCURED);
        }
        estimateResponse.setDxfName(FileCommon.extractDxfFileName(estimateResponse.getS3DxfAddress()));
        return estimateResponse;
    }
}
