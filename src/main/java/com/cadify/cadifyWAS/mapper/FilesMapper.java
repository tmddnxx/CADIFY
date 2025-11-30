package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.files.FilesDTO;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import org.springframework.stereotype.Component;

@Component
public class FilesMapper {

    // DTO -> Entity
    public Files filesDtoToFileEntity(FilesDTO.Post post){
        return Files.builder()
                .fileKey(post.getFileKey())
                .memberKey(post.getMemberKey())
                .fileName(post.getFileName())
                .s3StepAddress(post.getS3StepAddress())
                .s3DxfAddress(post.getS3DxfAddress())
                .imageAddress(post.getImageAddress())
                .metaJson(post.getMetaJson())
                .build();
    }



}
