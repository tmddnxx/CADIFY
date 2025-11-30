package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.admin.estimate.AdminEstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.OptionDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.service.file.common.FileCommon;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EstimateMapper {

    private final ObjectMapper objectMapper;

    public EstimateMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Estimate estimatePostToEstimate(EstimateDTO.Post post){
        return Estimate.builder()
                .fileId(post.getFileId())
                .estKey(post.getEstKey())
                .memberKey(post.getMemberKey())
                .folderKey(post.getFolderKey())
                .fileName(post.getFileName())
                .estName(post.getEstName())
                .method(post.getMethod())
                .type(post.getType())
                .cost((post.getCost()))
                .price(post.getPrice())
                .otherPrice(post.getOtherPrice())
                .holeJson(post.getHoleJson())
                .isFastShipment(post.isFastShipment())
                .material(post.getMaterial())
                .thickness(post.getThickness())
                .fileSize(post.getFileSize())
                .surface(post.getSurface())
                .coatingColor(post.getCoatingColor())
                .kg(post.getKg())
                .isChamfer(post.isChamfer())
                .createdAt(post.getCreatedAt())
                .errorCode(post.getErrorCode())
                .errorJson(post.getErorrDetails())
                .bbox(post.getBbox())
                .policyVersion(post.getPolicyVersion())
                .build();
    }

    public EstimateDTO.Response estimateToEstimateResponse(Estimate estimate) throws JsonProcessingException {
        return EstimateDTO.Response.builder()
                .estKey(estimate.getEstKey())
                .folderKey(estimate.getFolderKey())
                .memberKey(estimate.getMemberKey())
                .fileName(estimate.getFileName())
                .estName(estimate.getEstName())
                .method(estimate.getMethod())
                .type(estimate.getType())
                .price(estimate.getPrice())
                .cost(estimate.getCost())
                .otherPrice(estimate.getOtherPrice())
                .holeJson(estimate.getHoleJson())
                .isFastShipment(estimate.isFastShipment())
                .material(estimate.getMaterial())
                .thickness(estimate.getThickness())
                .commonDiff(estimate.getCommonDiff())
                .roughness(estimate.getRoughness())
                .fileSize(estimate.getFileSize())
                .surface(estimate.getSurface())
                .coatingColor(estimate.getCoatingColor())
                .kg(estimate.getKg())
                .isChamfer(estimate.isChamfer())
                .memo(estimate.getMemo())
                .createdAt(estimate.getCreatedAt())
                .errorCode(FileCommon.convertStringToNumberList(estimate.getErrorCode()))
                .errorDetails(estimate.getErrorJson())
                .bbox(estimate.getBbox() != null
                        ? objectMapper.readValue(estimate.getBbox(), OptionDTO.BBox.class)
                        : null)
                .standardDay(estimate.getStandardShipmentDay())
                .expressDay(estimate.getExpressShipmentDay())
                .build();
    }

    public AdminEstimateDTO.EstimateResponse estimateResponseToAdmin(Estimate estimate) throws JsonProcessingException {
        return AdminEstimateDTO.EstimateResponse.builder()
                .estKey(estimate.getEstKey())
                .folderKey(estimate.getFolderKey())
                .memberKey(estimate.getMemberKey())
                .fileName(estimate.getFileName())
                .estName(estimate.getEstName())
                .method(estimate.getMethod())
                .type(estimate.getType())
                .price(estimate.getPrice())
                .cost(estimate.getCost())
                .otherPrice(estimate.getOtherPrice())
                .holeJson(estimate.getHoleJson())
                .isFastShipment(estimate.isFastShipment())
                .material(estimate.getMaterial())
                .thickness(estimate.getThickness())
                .commonDiff(estimate.getCommonDiff())
                .roughness(estimate.getRoughness())
                .fileSize(estimate.getFileSize())
                .surface(estimate.getSurface())
                .coatingColor(estimate.getCoatingColor())
                .kg(estimate.getKg())
                .isChamfer(estimate.isChamfer())
                .memo(estimate.getMemo())
                .createdAt(estimate.getCreatedAt())
                .errorCode(FileCommon.convertStringToNumberList(estimate.getErrorCode()))
                .errorDetails(estimate.getErrorJson())
                .bbox(estimate.getBbox() != null
                        ? objectMapper.readValue(estimate.getBbox(), OptionDTO.BBox.class)
                        : null)
                .build();
    }
}
