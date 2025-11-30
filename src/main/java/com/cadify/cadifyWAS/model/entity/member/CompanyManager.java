package com.cadify.cadifyWAS.model.entity.member;

import com.cadify.cadifyWAS.util.base.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor
public class CompanyManager extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String managerKey;
    private String memberKey;
    private String department;
    private String position;
    private String managerName;
    private String phone;

    @Builder
    public CompanyManager(String memberKey, String department, String position, String managerName, String phone){
        this.memberKey = memberKey;
        this.department = department;
        this.position = position;
        this.managerName = managerName;
        this.phone = phone;
    }

    public CompanyManager updateManagerInfo(String department, String position, String managerName){
        Optional.ofNullable(department)
                .ifPresent(value -> this.department = department);
        Optional.ofNullable(position)
                .ifPresent(value -> this.position = position);
        Optional.ofNullable(managerName)
                .ifPresent(value -> this.managerName = managerName);
        Optional.ofNullable(managerName)
                .ifPresent(value -> this.managerName = managerName);
        return this;
    }
}