package com.cadify.cadifyWAS.repository.factory.estimate;

import com.cadify.cadifyWAS.model.entity.Files.KFactor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KFactorRepository extends JpaRepository<KFactor, Long> {

    List<KFactor> findAllByMaterialInAndThicknessIn(List<String> materials, List<Double> thicknesses);

    KFactor findKFactorByMaterialAndThickness(String material, Double thickness);
}
