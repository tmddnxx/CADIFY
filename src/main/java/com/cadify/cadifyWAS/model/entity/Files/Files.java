package com.cadify.cadifyWAS.model.entity.Files;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Files {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 파일 pk

    @Column(nullable = false, unique = true)
    private String fileKey; // 파일 pk

    @Column(nullable = false)
    private String memberKey; // 사용자 pk

    @Column(nullable = false, unique = true)
    private String fileName; // 파일이름_타임스탬프(S3)

    @Column(nullable = false, unique = true, length = 1000)
    private String s3StepAddress; // step 파일의 s3 주소

    @Column(unique = true, length = 1000)
    private String s3DxfAddress; // 사용자가 올린 dxf 파일의 s3 주소

    @Column(unique = true, length = 1000)
    private String factoryDxfAddress; // 공장에서 생성한 dxf 파일의 s3 주소
    /* https:// ~ /memberKey/dxf/factory/파일이름_timestamp_kFactor.dxf */

    @Column(unique = true, length = 1000)
    private String imageAddress; // 이미지 주소

    @Column(nullable = false, columnDefinition = "text")
    private String metaJson; // sdk에서 얻어온 파일 json정보

    public void updateS3DxfAddress(String s3DxfAddress){
        this.s3DxfAddress = s3DxfAddress;
    }

    public void uploadDxfByFactory(String factoryDxfAddress){
        this.factoryDxfAddress = factoryDxfAddress;
    }

    public void deleteDxfByUser() {
        this.s3DxfAddress = null;
    }
}