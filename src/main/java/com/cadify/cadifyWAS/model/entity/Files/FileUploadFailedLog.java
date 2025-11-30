package com.cadify.cadifyWAS.model.entity.Files;

import com.cadify.cadifyWAS.service.file.common.Method;
import com.cadify.cadifyWAS.util.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "file_upload_failed_log")
public class FileUploadFailedLog extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String memberKey;

    @Column
    private String fileName;

    @Column
    private String outPath;

    @Column
    @Enumerated(EnumType.STRING)
    private Method method; // METAL or CNC

    @Column(nullable = false, columnDefinition = "text")
    private String logMessage;
}
