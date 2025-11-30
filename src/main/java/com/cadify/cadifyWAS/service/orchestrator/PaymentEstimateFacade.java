package com.cadify.cadifyWAS.service.orchestrator;

import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.service.file.EstimateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class PaymentEstimateFacade {

    private final EstimateService estimateService;

    public List<EstimateDTO.EstimateValidStatus> getEstimateValidStatusList(List<String> estKeys) {
        return estimateService.reValidEstimates(estKeys);
    }
}
