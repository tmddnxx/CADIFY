package com.cadify.cadifyWAS.model.dto.files;


import lombok.*;

import java.util.List;

public class FolderDTO {


    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Post{
        private String folderKey; // 폴더 퍼블릭 키 (UUID)
        private String memberKey; // 사용자 식별키
        private String folderName;
        private Long parentId;
        private String parentKey;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private String folderKey;
        private String folderName;
        private String parentKey;
        private List<Response> subFolders;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Move{
        private String folderKey;
        private String parentKey;
    }
}
