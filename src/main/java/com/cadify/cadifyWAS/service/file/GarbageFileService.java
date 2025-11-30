package com.cadify.cadifyWAS.service.file;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.cadify.cadifyWAS.model.dto.files.GarbageFilesDTO;
import com.cadify.cadifyWAS.model.entity.Files.GarbageFiles;
import com.cadify.cadifyWAS.repository.Files.GarbageFilesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
@Service
public class GarbageFileService {

    private final AmazonS3 amazonS3;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;
    private final GarbageFilesRepository garbageFilesRepository;

    // Garbage File DB 저장
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 별도의 트랜잭션 생성 (트랜잭션 메서드 내부호출이지만 별도로 정상적인 커밋을위해)
    protected void saveGarbageFiles(GarbageFilesDTO garbageFilesDTO){
        GarbageFiles garbageFiles = garbageFilesDTO.toEntity(garbageFilesDTO);
        garbageFilesRepository.save(garbageFiles);
    }

    // s3 쓰레기데이터 삭제 배치
    protected void deleteGarbageFile(GarbageFiles garbageFiles){
        garbageFilesRepository.delete(garbageFiles);
    }

    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시 실행
    public void deleteS3GarbageFile(){ // s3와 db에 쓰레기파일 삭제 (s3에 없는 파일을 삭제하려고해도 오류안남)
        int page = 0;
        int size = 100;
        Page<GarbageFiles> result;

        do {
            result = garbageFilesRepository.findAll(PageRequest.of(page++, size));
            for (GarbageFiles g : result) {
                try {
                    amazonS3.deleteObject(g.getBucket(), g.getPath());
                    deleteGarbageFile(g); // 직접 호출해도 괜찮음
                } catch (AmazonClientException e) {
                    log.error("AmazonClientException: {}", e.getMessage());
                }
            }
        } while (!result.isLast());
    }
}
