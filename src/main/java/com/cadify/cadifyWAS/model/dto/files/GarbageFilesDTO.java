package com.cadify.cadifyWAS.model.dto.files;

import com.cadify.cadifyWAS.model.entity.Files.GarbageFiles;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GarbageFilesDTO {

    private Long gno;
    private String path;
    private String bucket; // s3 버킷 이름

    public GarbageFiles toEntity(GarbageFilesDTO garbageFilesDTO){
        return GarbageFiles.builder()
                .bucket(garbageFilesDTO.getBucket())
                .path(garbageFilesDTO.getPath())
                .build();
    }
}
