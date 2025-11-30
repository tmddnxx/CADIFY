package com.cadify.cadifyWAS.util.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    // 생성일
    @CreatedDate
    private LocalDateTime createdAt;
    // 마지막 수정일
    @LastModifiedDate
    private LocalDateTime modifiedAt;
    // 삭제여부
    @Column(name = "deleted")
    private Boolean deleted = false;


    // 소프트 딜리트 메서드
    public void softDelete(){
        this.deleted = true;
    }
}
