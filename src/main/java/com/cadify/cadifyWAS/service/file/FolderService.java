package com.cadify.cadifyWAS.service.file;

import com.cadify.cadifyWAS.exception.CustomLogicException;
import com.cadify.cadifyWAS.exception.ExceptionCode;
import com.cadify.cadifyWAS.mapper.FolderMapper;
import com.cadify.cadifyWAS.model.dto.files.FolderDTO;
import com.cadify.cadifyWAS.model.entity.Files.Folder;
import com.cadify.cadifyWAS.repository.Files.EstimateRepository;
import com.cadify.cadifyWAS.repository.Files.FilesRepository;
import com.cadify.cadifyWAS.repository.Files.FolderRepository;
import com.cadify.cadifyWAS.result.ResultCode;
import com.cadify.cadifyWAS.result.ResultResponse;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class FolderService {

    private final FolderRepository folderRepository;
    private final FolderMapper folderMapper;
    private final EstimateRepository estimateRepository;

     // 폴더 생성
    public ResultResponse createFolder(FolderDTO.Post post){

        // 폴더 이름검증
        String folderName = post.getFolderName();
        if(!validFolderName(folderName)){
           throw new CustomLogicException(ExceptionCode.NAME_INVALID);
        }

        if (post.getParentKey() == null) {
            post.setParentKey(null);
            post.setParentId(null);
        }
        // 부모 폴더가 존재하는지 확인
        if (post.getParentKey() != null) {
            Folder parentFolderOptional = folderRepository.findByFolderKey(post.getParentKey())
                    .orElseThrow(() -> new CustomLogicException(ExceptionCode.FOLDER_NOT_FOUND));
            post.setParentId(parentFolderOptional.getId());
        }
        String folderKey = UUID.randomUUID().toString();
        post.setFolderKey(folderKey);
        Folder folder = folderMapper.dtoToEntity(post);
        Folder folderEntity = folderRepository.save(folder);

        return ResultResponse.of(ResultCode.SUCCESS, folderMapper.entityToDto(folderEntity));
    }

    // 전체 폴더 구조 반환
    @Transactional(readOnly = true)
    public List<FolderDTO.Response> getFolders(String memberKey){

        List<Folder> folderList = folderRepository.findByMemberKeyWithSubFolders(memberKey);

        return folderList.stream()
                .filter(folder -> folder.getParentId() == null || folder.getParentId() == 0)
                .map(folderMapper::entityToDto)
                .collect(Collectors.toList());
    }

    // 폴더 이름 수정
    public ResultResponse modifyFolderName(String folderKey, String folderName){

        // 폴더이름 검증
        if(!validFolderName(folderName)){
            throw new CustomLogicException(ExceptionCode.NAME_INVALID);
        }

        Optional<Folder> folderOptional = folderRepository.findByFolderKey(folderKey);
        if(folderOptional.isEmpty()){
            throw new CustomLogicException(ExceptionCode.FOLDER_NOT_FOUND);
        }

        Folder folder = folderOptional.get();
        folder.changeFolderName(folderName);
        folderRepository.save(folder);

        FolderDTO.Response response = FolderDTO.Response
            .builder()
            .folderKey(folder.getFolderKey())
            .folderName(folder.getFolderName())
            .parentKey(folder.getParentKey() != null ? folder.getParentKey() : null)
            .build();

        return ResultResponse.of(ResultCode.SUCCESS, response);
    }


    // 폴더삭제 (hard delete, 견적은 소프트딜리트)
    @Transactional
    public ResultResponse deleteFolder(String folderKey) {
        Folder folder = folderRepository.findByFolderKey(folderKey).orElseThrow(
                () -> new CustomLogicException(ExceptionCode.FOLDER_NOT_FOUND));

        int deletedCnt = 0;
        try {
            deletedCnt = estimateRepository.deleteEstimateByFolderKey(folderKey);
            folderRepository.delete(folder);
        } catch (Exception e) {

            throw new CustomLogicException(ExceptionCode.valueOf("폴더 삭제 실패"));
        }

        return ResultResponse.of(ResultCode.SUCCESS, deletedCnt);
    }

    // 폴더 이동 (해당 폴더의 parentKey 변경)
    public ResultResponse moveFolder(FolderDTO.Move move) {
        Folder folder = folderRepository.findByFolderKey(move.getFolderKey())
                .orElseThrow(() -> new CustomLogicException(ExceptionCode.FOLDER_NOT_FOUND));

        if (move.getParentKey() != null) {
            Folder parentFolder = folderRepository.findByFolderKey(move.getParentKey())
                    .orElseThrow(() -> new CustomLogicException(ExceptionCode.FOLDER_NOT_FOUND));
            folder.moveFolder(parentFolder.getId(), move.getParentKey());
        }
        else {
            folder.moveFolder(null, null);
        }

        folderRepository.save(folder);

        FolderDTO.Response response = FolderDTO.Response
                .builder()
                .folderKey(folder.getFolderKey())
                .folderName(folder.getFolderName())
                .parentKey(folder.getParentKey() != null ? folder.getParentKey() : null)
                .build();

        return ResultResponse.of(ResultCode.SUCCESS, response);
    }

    // 폴더 이름 검증
    private boolean validFolderName(String folderName){
        // 폴더 이름에 공백만 있는건 안돼요
        if(folderName == null || folderName.trim().isEmpty()){
            return false;
        }

        String normalizedFileName = Normalizer.normalize(folderName, Normalizer.Form.NFC);

        // 허용문자
        String regex = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ_\\-\\.\\(\\)\\{\\}\\[\\] <>\t]+$";
        if(!normalizedFileName.matches(regex)){
            return false;
        }

        // 폴더이름 길이 255자까지
        if(folderName.length() > 256){
            return false;
        }

        // 모든 조건을 통과한 경우 성공
        return true;
    }


}
