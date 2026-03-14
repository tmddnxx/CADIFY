package com.cadify.cadifyWAS.controller.files;

import com.cadify.cadifyWAS.model.dto.files.FolderDTO;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.FolderService;
import com.cadify.cadifyWAS.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/api/folder")
public class FolderController {

    private final FolderService folderService;
    private final JwtUtil jwtUtil;
    
    // 폴더생성
    @PostMapping("/create")
    public ResponseEntity<ResultResponse> createFolder(@RequestBody FolderDTO.Post post){
        String memberKey = jwtUtil.getAuthPrincipal();
        post.setMemberKey(memberKey);
        ResultResponse response = folderService.createFolder(post);

        return ResponseEntity.ok(response);
    }
    
    // 전체 폴더 구조 반환
    @GetMapping("/list")
    public ResponseEntity<List<FolderDTO.Response>> getFolderList(){
        String memberKey = jwtUtil.getAuthPrincipal();
        List<FolderDTO.Response> responseList = folderService.getFolders(memberKey);

        return ResponseEntity.ok(responseList);
    }

    // 폴더 이름 수정
    @PatchMapping("/change/{folderKey}/{folderName}")
    public ResponseEntity<ResultResponse> modifyFolderName(@PathVariable("folderKey")String folderKey,
                                                               @PathVariable("folderName") String folderName){

        ResultResponse response = folderService.modifyFolderName(folderKey, folderName);

        return ResponseEntity.ok(response);
    }

    // 폴더 이동
    @PostMapping("/move")
    public ResponseEntity<ResultResponse> moveFolder(@RequestBody FolderDTO.Move move){
        ResultResponse response = folderService.moveFolder(move);

        return ResponseEntity.ok(response);
    }

    // 폴더 삭제
    @DeleteMapping("/delete/{folderKey}")
    public ResponseEntity<ResultResponse> deleteFolder(@PathVariable("folderKey") String folderKey){
        ResultResponse response = folderService.deleteFolder(folderKey);

        return ResponseEntity.ok(response);
    }
}
