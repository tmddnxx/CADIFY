package com.cadify.cadifyWAS.service.file;

import com.cadify.cadifyWAS.model.dto.files.FileTask;
import com.cadify.cadifyWAS.model.entity.Files.FileUploadFailedLog;
import com.cadify.cadifyWAS.repository.Files.FailedLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileLogService {

    private final FailedLogRepository failedLogRepository;

    // 파일 업로드 실패 로그 저장
    public void saveFileUploadFailedLog(FileTask task, String errorMessage) {
        failedLogRepository.save(
                FileUploadFailedLog.builder()
                        .fileName(task.getOriginFileName())
                        .memberKey(task.getMemberKey())
                        .outPath(task.getJsonOutPath())
                        .method(task.getMethod())
                        .logMessage(errorMessage)
                        .build()
        );

    }
}
