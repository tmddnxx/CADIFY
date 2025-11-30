package com.cadify.cadifyWAS.model.entity.Files;

import com.cadify.cadifyWAS.model.dto.files.GarbageFilesDTO;
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
public class GarbageFiles {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long gno;

    @Column(nullable = false)
    private String bucket; // s3 버킷 이름

    @Column(nullable = false, unique = true)
    private String path; // s3 주소


    public GarbageFilesDTO toDto(){
        return GarbageFilesDTO.builder()
                .gno(this.gno)
                .bucket(this.bucket)
                .path(this.path)
                .build();
    }
}
