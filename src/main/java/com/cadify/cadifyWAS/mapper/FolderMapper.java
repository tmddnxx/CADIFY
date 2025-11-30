package com.cadify.cadifyWAS.mapper;

import com.cadify.cadifyWAS.model.dto.files.FolderDTO;
import com.cadify.cadifyWAS.model.entity.Files.Folder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FolderMapper {

    // DTO -> Entity
    public Folder dtoToEntity(FolderDTO.Post post){
        return Folder.builder()
                .folderKey(post.getFolderKey())
                .memberKey(post.getMemberKey())
                .folderName(post.getFolderName())
                .parentId(post.getParentId())
                .parentKey(post.getParentKey())
                .build();
    }

    // Entity -> DTO
    public FolderDTO.Response entityToDto(Folder folder){
        return FolderDTO.Response.builder()
                .folderKey(folder.getFolderKey())
                .folderName(folder.getFolderName())
                .parentKey(folder.getParentKey())
                .subFolders(folder.getSubFolders()
                        .stream().map(this::entityToDto)
                        .collect(Collectors.toList()))
                .build();
    }
}
