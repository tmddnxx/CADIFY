package com.cadify.cadifyWAS.controller.factory;

import com.cadify.cadifyWAS.model.dto.factory.estimate.FactoryEstimateDTO;
import com.cadify.cadifyWAS.service.factory.FactoryEstimateService;
import com.cadify.cadifyWAS.service.file.FilesByFactoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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
        String stepFileUrl = filesByFactoryService.downloadStepFile(estKey);
        String dxfFileUrl = filesByFactoryService.downloadDxfByUser(estKey);
        String dxfFactoryFileUrl = null;
        try {
            dxfFactoryFileUrl = filesByFactoryService.downloadDxfByFactory(estKey);
        }catch (Exception e) {
            System.out.println(e.getMessage());
            dxfFactoryFileUrl = null;
        }
        List<String> fileUrls = new ArrayList<>();
        if (stepFileUrl != null) fileUrls.add(stepFileUrl);
        if (dxfFileUrl != null) fileUrls.add(dxfFileUrl);
        if (dxfFactoryFileUrl != null) fileUrls.add(dxfFactoryFileUrl);
        return new ResponseEntity<>(fileUrls, HttpStatus.OK);
    }

}
