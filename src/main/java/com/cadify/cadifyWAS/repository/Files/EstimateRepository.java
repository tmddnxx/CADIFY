package com.cadify.cadifyWAS.repository.Files;

import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {

    // 퍼블릭 키로 조회
    Optional<Estimate> findByEstKey(String estKey);

    List<Estimate> findAllByEstKeyIn(List<String> estKeys);

    // 견적 리스트 요청 
    @Query("""
        SELECT e, f.imageAddress
        FROM Estimate e
        JOIN Files f ON e.fileId = f.id
        WHERE e.memberKey = :member_key
          AND (:folder_key IS NULL AND e.folderKey IS NULL OR :folder_key IS NOT NULL AND e.folderKey = :folder_key) AND e.deletedAt IS NULL
        ORDER BY e.createdAt DESC
    """)
    Page<Tuple> findEstimateWithFile(@Param("member_key") String memberKey, @Param("folder_key") String folderKey, Pageable pageable);

    // 총 파일 사이즈
    @Query("SELECT COALESCE(SUM(e.fileSize), 0) FROM Estimate e WHERE e.memberKey = :member_key AND e.deletedAt IS NULL")
    long findTotalFileSize(@Param("member_key") String memberKey);

    // 단일 견적 뷰어 확인
    @Query("SELECT e, f.s3StepAddress From Estimate e JOIN Files f ON e.fileId = f.id where e.estKey = :est_key AND e.memberKey = :member_key AND e.deletedAt IS NULL")
    List<Tuple> findEstimateWithFileJson(@Param("est_key") String estKey, @Param("member_key") String memberKey);
    
    // metaJson 가져오기
    @Query("select f.metaJson from Estimate e join Files f on e.fileId = f.id where e.estKey = :est_key")
    String findMetaJsonByEstkey(@Param("est_key") String estKey);

    // 여러 견적 폴더이동
    @Modifying
    @Transactional
    @Query("UPDATE Estimate e SET e.folderKey = :folderKey WHERE e.estKey IN :estKeys")
    int moveEstimateFolder(@Param("folderKey") String folderKey, @Param("estKeys") List<String> estKeys);

    // 여러 견적 삭제처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Estimate e SET e.deletedAt = CURRENT_TIMESTAMP WHERE e.estKey IN :estKeys")
    int deleteEstimates(@Param("estKeys") List<String> estKeys);

    // 여러 견적 파일키 조회
    @Query("SELECT e.fileId FROM Estimate e WHERE e.estKey IN :estKeys")
    List<Long> findFileKeysByEstKeys(@Param("estKeys") List<String> estKeys);

    // 폴더 내 견적 삭제처리
    @Modifying
    @Transactional
    @Query("UPDATE Estimate e SET e.deletedAt = CURRENT_TIMESTAMP WHERE e.folderKey = :folderKey")
    int deleteEstimateByFolderKey(@Param("folderKey") String folderKey);

    // stp 주소 가져오기
    @Query("SELECT f.s3StepAddress FROM Estimate e JOIN Files f ON e.fileId = f.id WHERE e.estKey = :estKey")
    String findStepAddressByEstKey(@Param("estKey") String estKey);

    // 사용자 dxf 주소 가져오기
    @Query("SELECT f.s3DxfAddress FROM Estimate e JOIN Files f ON e.fileId = f.id WHERE e.estKey = :estKey")
    String findUserDxfAddressByEstKey(@Param("estKey") String estKey);
}
