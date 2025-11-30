package com.cadify.cadifyWAS.repository.Files;

import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilesRepository extends JpaRepository<Files, Long> {
    // 파일 레코드 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Files f WHERE f.id IN :fileIds")
    int deleteFiles(@Param("fileIds") List<Long> fileIds);
    
    // s3 주소 조회
    @Query("SELECT f FROM Files f WHERE f.id IN :fileIds")
    List<Files> findAddressByIds(@Param("fileIds") List<Long> fileIds);
}
