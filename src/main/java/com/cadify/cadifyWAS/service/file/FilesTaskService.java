package com.cadify.cadifyWAS.service.file;

import com.cadify.cadifyWAS.mapper.EstimateMapper;
import com.cadify.cadifyWAS.mapper.FilesMapper;
import com.cadify.cadifyWAS.model.dto.files.EstimateDTO;
import com.cadify.cadifyWAS.model.dto.files.FilesDTO;
import com.cadify.cadifyWAS.model.dto.files.GarbageFilesDTO;
import com.cadify.cadifyWAS.model.entity.Files.Estimate;
import com.cadify.cadifyWAS.model.entity.Files.Files;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.repository.Files.FilesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class FilesTaskService {

    private final FilesMapper filesMapper;
    private final FilesRepository filesRepository;
    private final EstimateRepository estimateRepository;
    private final EstimateMapper estimateMapper;
    private final RedisTemplate<String, Object> fileStatusTemplate;
    private static final String KEY_PREFIX = "file_status:"; // 레디스 저장 키
    
    @Transactional
    public FilesDTO.KeyResponse saveEntities(FilesDTO.Post post, EstimateDTO.Post estimateDTO) {
        // 파일 저장
        Files filesEntity = filesMapper.filesDtoToFileEntity(post);
        filesRepository.save(filesEntity);

        estimateDTO.setFileId(filesEntity.getId());
        // 견적 저장
        Estimate estimate = estimateMapper.estimatePostToEstimate(estimateDTO);
        estimateRepository.save(estimate);

        return FilesDTO.KeyResponse.builder()
                .fileId(filesEntity.getId())
                .estKey(estimate.getEstKey())
                .build();
    }

    // 레디스에 tempkey 저장 : tempkey가있으면 아직 처리중, 없으면 처리완료(정상 or 예외)
    public void saveTempKey(String memberKey, FilesDTO.SSEResponse response) throws JsonProcessingException {
        String key = KEY_PREFIX + memberKey;

        ObjectMapper objectMapper = new ObjectMapper();
        String obj = objectMapper.writeValueAsString(response);

        fileStatusTemplate.opsForList().rightPush(key, obj);
        fileStatusTemplate.expire(key, 30, TimeUnit.MINUTES); //30분 만료
    }

    // 레디스에 저장된 tempkey 리스트 가져오기
    public List<JsonNode> getTempKeys(String memberKey) {
        String key = KEY_PREFIX + memberKey;
        Long size = fileStatusTemplate.opsForList().size(key);

        if (size == null || size == 0) {
            return new ArrayList<>(); // tempKey가 없으면 빈 리스트 반환
        }

        ObjectMapper mapper = new ObjectMapper();
        return fileStatusTemplate.opsForList().range(key, 0, size - 1).stream()
                .map(Object::toString)
                .map(str -> {
                    try {
                        return mapper.readTree(str);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse JSON: " + str, e);
                    }
                })
                .collect(Collectors.toList());
    }

    // 처리 완료된 tempKey 제거
    public void removeTempKey(String memberKey, String tempKey) throws JsonProcessingException {
        String key = KEY_PREFIX + memberKey;

        List<Object> rawList = fileStatusTemplate.opsForList().range(key, 0, -1);
        if (rawList == null || rawList.isEmpty()) return;

        List<String> tempKeyList = rawList.stream()
                .map(obj -> obj.toString())
                .collect(Collectors.toList());

        ObjectMapper objectMapper = new ObjectMapper();

        for (String item : tempKeyList) {
            FilesDTO.SSEResponse response = objectMapper.readValue(item, FilesDTO.SSEResponse.class);
            if (response.getTempKey().equals(tempKey)) {
                fileStatusTemplate.opsForList().remove(key, 0, item);
                break;
            }
        }
    }
}
