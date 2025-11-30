package com.cadify.cadifyWAS.controller.factory;

import com.cadify.cadifyWAS.model.dto.factory.estimate.KFactorDTO;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cadify.cadifyWAS.service.factory.KFactorService;

import java.util.List;

@RestController
@RequestMapping("/api/factory/kFactor")
@RequiredArgsConstructor
public class KFactorController {
    private final KFactorService kFactorService;

    @GetMapping
    public ResponseEntity<List<KFactorDTO.Response>> getKFactorList() {
        List<KFactorDTO.Response> kFactorList = kFactorService.getAllKFactors();
        return ResponseEntity.ok(kFactorList);
    }

    @PostMapping
    public ResponseEntity<ResultResponse> upsertKFactors(@RequestBody List<KFactorDTO.Upsert> kFactorDTOList) {
        kFactorService.upsertKFactors(kFactorDTOList);
        return ResponseEntity.ok(ResultResponse.of(ResultCode.SUCCESS));
    }
}
