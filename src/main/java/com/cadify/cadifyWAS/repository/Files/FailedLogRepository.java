package com.cadify.cadifyWAS.repository.Files;

import com.cadify.cadifyWAS.model.entity.Files.FileUploadFailedLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FailedLogRepository extends JpaRepository<FileUploadFailedLog, Long> {
}
