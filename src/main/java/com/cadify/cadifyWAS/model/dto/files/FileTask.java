package com.cadify.cadifyWAS.model.dto.files;

import com.cadify.cadifyWAS.service.file.common.Method;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
@Builder
public class FileTask implements Serializable {
    private String originFileName;
    private String stepName;
    private String jsonName;
    private String imageName;
    private String jsonOutPath;
    private String memberKey;
    private String folderKey;
    private String tempKey;
    private long fileSize; // 파일사이즈
    private LocalDateTime createdAt;
    private Method method; // METAL or CNC
    private int exitCode; // ecs 태스크 실행결과 코드
}
