package com.cadify.cadifyWAS.model.entity.Files;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // KFactor pk

    private String material;
    private double thickness; // 두께
    private Double kFactor; // KFactor 값

    public void updateKFactor(Double kFactor) {
        this.kFactor = kFactor;
    }
}
