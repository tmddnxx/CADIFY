package com.cadify.cadifyWAS.repository.Files;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cadify.cadifyWAS.model.entity.Files.Folder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    
    // 사용자별 폴더리스트(서브폴더 포함)
    @Query("SELECT DISTINCT f FROM Folder f LEFT JOIN FETCH f.subFolders WHERE f.memberKey = :memberKey")
    List<Folder> findByMemberKeyWithSubFolders(@Param("memberKey") String memberKey);

    // 폴더 퍼블릭 키로 조회
    Optional<Folder> findByFolderKey(String folderKey);
}
