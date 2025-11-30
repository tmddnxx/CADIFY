package com.cadify.cadifyWAS.model.entity.Files;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 폴더 pk

    @Column(nullable = false, unique = true)
    private String folderKey; // 폴더 퍼블릭 키 (UUID)
    
    @Column(nullable = false)
    private String memberKey; // 사용자 식별키
    
    @Column(nullable = false)
    private String folderName; // 폴더이름

    @Column
    @Builder.Default
    private String parentKey = null; // 부모폴더 퍼블릭 키 (UUID)

    @Column
    @Builder.Default
    private Long parentId = null; // 부모폴더 pk

    @OneToMany(mappedBy = "parentId", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Folder> subFolders = new ArrayList<>();

    public void moveFolder(Long parentId, String parentKey){
        this.parentId = parentId;
        this.parentKey = parentKey;
    }

    public void changeFolderName(String folderName){
        this.folderName = folderName;
    }

}
