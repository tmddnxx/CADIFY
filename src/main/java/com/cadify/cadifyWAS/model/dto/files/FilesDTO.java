package com.cadify.cadifyWAS.model.dto.files;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class FilesDTO {
    
    @Getter
    @Builder
    public static class Post{ // 파일 업로드 시
        private String fileKey;
        private String memberKey; // 사용자 pk
        private String fileName; // 파일이름
        private String s3StepAddress; // step 파일 s3 주소
        private String s3DxfAddress; // dxf 파일 s3 주소
        private String imageAddress; // dxf 파일 s3 주소
        private String metaJson; // metaJson
    }

    @Setter
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusResponse{
        private String fileName; // 파일이름
        private String estKey; // 견적서 pk
        private boolean isSuccess; // 성공실패 플래그
        private String message; // 메시지
        private String tempKey; // 임시키
        private long fileSize; // 파일 사이즈
    }

    @Getter
    @Builder
    public static class KeyResponse{
        private Long fileId; // 파일 pk
        private String estKey; // 견적서 pk
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SSEResponse{
        private String fileName; // 파일이름
        private String tempKey; // 임시키
        private String method; // 가공방법(판금 sheet_metal) or 절삭 cnc
        private String folderKey; // 소속 폴더 pk (null이면 root에 위치해있음)
        private String createdAt; // 생성일
    }
}
