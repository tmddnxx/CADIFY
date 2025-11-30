package com.cadify.cadifyWAS.repository.Files;

import com.cadify.cadifyWAS.model.entity.Files.GarbageFiles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GarbageFilesRepository extends JpaRepository<GarbageFiles, Long> {
}
