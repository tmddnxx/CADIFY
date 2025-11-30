package com.cadify.cadifyWAS.controller.files;

import com.cadify.cadifyWAS.model.dto.files.FolderDTO;
import com.cadify.cadifyWAS.result.ResultResponse;
import com.cadify.cadifyWAS.service.file.FolderService;
import com.cadify.cadifyWAS.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
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
