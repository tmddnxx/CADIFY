package com.cadify.cadifyWAS.controller.factory;

import com.cadify.cadifyWAS.model.dto.factory.estimate.FactoryEstimateDTO;
import com.cadify.cadifyWAS.service.factory.FactoryEstimateService;
import com.cadify.cadifyWAS.service.file.FilesByFactoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/factory/estimate")
public class FactoryEstimateController {

    private final FactoryEstimateService factoryEstimateService;
    private final FilesByFactoryService filesByFactoryService;

    @GetMapping("/{orderItemKey}")
    public ResponseEntity<FactoryEstimateDTO.EstimateResponse> getEstimate(@PathVariable("orderItemKey") String orderItemKey) {
        FactoryEstimateDTO.EstimateResponse estimateResponse = factoryEstimateService.getEstimate(orderItemKey);
        return ResponseEntity.ok(estimateResponse);
    }

    @GetMapping("/files/{estKey}")
    public ResponseEntity<List<String>> downloadStepFile(@PathVariable("estKey") String estKey) {
        List<String> fileUrls = filesByFactoryService.getEstimateFileUrls(estKey);
        return ResponseEntity.ok(fileUrls);
    }

}
