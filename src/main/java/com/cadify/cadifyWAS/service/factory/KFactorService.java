package com.cadify.cadifyWAS.service.factory;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.model.dto.factory.estimate.KFactorDTO;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.model.entity.Files.KFactor;
import com.cadify.cadifyWAS.repository.factory.estimate.KFactorRepository;
import com.cadify.cadifyWAS.service.file.enumValues.metal.limitValue.material.MetalMaterialByThickness;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class KFactorService {

    private final KFactorRepository kFactorRepository;

    public void upsertKFactors(List<KFactorDTO.Upsert> kFactorDTOList) {
        List<OptionDTO.OptionType> optionTypes = MetalMaterialByThickness.getAllThicknessListByMaterial();
        Map<String, List<Double>> validMap = optionTypes.stream()
                .collect(Collectors.toMap(
                        OptionDTO.OptionType::getMaterial,
                        OptionDTO.OptionType::getThicknessList
                ));
        
        // 존재하지 않는 조합일때
        boolean allInvalid = kFactorDTOList.stream()
                .noneMatch(dto -> validMap.containsKey(dto.getMaterial()) &&
                        validMap.get(dto.getMaterial()).contains(dto.getThickness()));
        if (allInvalid) {
            throw new CustomLogicException(ExceptionCode.COMBINATION_NOT_FOUND);
        }

        // 값 범위 검사 (0 이상 1 미만)
        boolean invalidRange = kFactorDTOList.stream()
                .filter(dto -> dto.getKFactor() != null)
                .anyMatch(dto -> dto.getKFactor() < 0 || dto.getKFactor() >= 1);
        if (invalidRange) {
            throw new CustomLogicException(ExceptionCode.INVALID_KFACTOR_RANGE);
        }

        // 소수점 자리수 검사 (3자리까지)
        boolean invalidPrecision = kFactorDTOList.stream()
                .filter(dto -> dto.getKFactor() != null)
                .anyMatch(dto -> {
                    double val = dto.getKFactor();
                    double rounded = Math.round(val * 1000.0) / 1000.0;
                    return Math.abs(val - rounded) > 1e-9;
                });
        if (invalidPrecision) {
            throw new CustomLogicException(ExceptionCode.INVALID_KFACTOR_PRECISION);
        }

        List<KFactorDTO.Upsert> filtered = kFactorDTOList.stream()
                .filter(dto -> validMap.containsKey(dto.getMaterial()) &&
                        validMap.get(dto.getMaterial()).contains(dto.getThickness()))
                .toList();

        List<String> materials = filtered.stream().map(KFactorDTO.Upsert::getMaterial).distinct().toList();
        List<Double> thicknesses = filtered.stream().map(KFactorDTO.Upsert::getThickness).distinct().toList();

        List<KFactor> existing = kFactorRepository.findAllByMaterialInAndThicknessIn(materials, thicknesses);
        Map<String, KFactor> existingMap = existing.stream()
                .collect(Collectors.toMap(
                        k -> k.getMaterial() + "_" + k.getThickness(),
                        Function.identity()
                ));

        List<KFactor> toSave = new ArrayList<>();

        for (KFactorDTO.Upsert dto : filtered) {
            String key = dto.getMaterial() + "_" + dto.getThickness();
            if (existingMap.containsKey(key)) {
                // update
                KFactor kf = existingMap.get(key);
                kf.updateKFactor(dto.getKFactor());
                toSave.add(kf);
            } else {
                // insert
                KFactor kf = KFactor.builder()
                        .material(dto.getMaterial())
                        .thickness(dto.getThickness())
                        .kFactor(dto.getKFactor())
                        .build();
                toSave.add(kf);
            }
        }

        kFactorRepository.saveAll(toSave); // 한 번에 저장
    }

    // 모든 KFactor 조회
    public List<KFactorDTO.Response> getAllKFactors() {
        // enum에서 가능한 material+thickness 리스트 가져오기
        List<OptionDTO.OptionType> materialByThicknessList = MetalMaterialByThickness.getAllThicknessListByMaterial();

        // DB에서 현재 저장된 kfactor 값들 한번에 조회 (material+thickness 쌍별로 Map으로)
        List<KFactor> savedKFactors = kFactorRepository.findAll();  // 전체 조회 예시
        Map<String, Map<Double, Double>> kfactorMap = savedKFactors.stream()
                .filter(k -> k.getMaterial() != null && k.getKFactor() != null && k.getThickness() > 0)
                .collect(Collectors.groupingBy(
                        KFactor::getMaterial,
                        Collectors.toMap(
                                KFactor::getThickness,
                                KFactor::getKFactor // null 허용
                        )
                ));

        // materialByThicknessList를 돌면서 kfactorMap에서 값 있으면 넣고, 없으면 null 넣기
        List<KFactorDTO.Response> responseList = new ArrayList<>();

        for (OptionDTO.OptionType mbt : materialByThicknessList) {
            String material = mbt.getMaterial();
            List<Double> thicknessList = mbt.getThicknessList();

            Map<Double, Double> thicknessToKfactor = kfactorMap.getOrDefault(material, Collections.emptyMap());

            for (Double thickness : thicknessList) {
                Double kfactor = thicknessToKfactor.getOrDefault(thickness, null);

                responseList.add(
                        KFactorDTO.Response.builder()
                                .material(material)
                                .thickness(thickness)
                                .kFactor(kfactor)  // 값 없으면 null
                                .build()
                );
            }
        }

        return responseList;
    }
}
