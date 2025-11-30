package com.cadify.cadifyWAS.util.mock;

import com.cadify.cadifyWAS.model.entity.factory.Factory;
import com.cadify.cadifyWAS.model.entity.factory.FactoryType;
import com.cadify.cadifyWAS.repository.factory.FactoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactoryMockInitializer {

    private final FactoryRepository factoryRepository;

    public void init() {
        // SUPER Factory
        saveFactory(ConstantMockData.SUPER_FACTORY_KEY, "DefaultSuperFactory", FactoryType.ALL, "기본공장관리");
        // CNC Factory
        saveFactory(ConstantMockData.CNC_FACTORY_KEY, "DefaultCNCFactory", FactoryType.CNC, "절삭공장관리");
        // METAL Factory
        saveFactory(ConstantMockData.METAL_FACTORY_KEY, "DefaultMETALFactory", FactoryType.SHEET_METAL, "판금공장관리");
    }

    private void saveFactory(String key, String name, FactoryType type, String owner){
        if(factoryRepository.findFactoryByFactoryNameAndDeletedFalse(name).isEmpty()){
            factoryRepository.save(Factory.builder()
                    .factoryKey(key)
                    .factoryName(name)
                    .factoryType(type)
                    .businessCode("NONE")
                    .addressNumber("NONE")
                    .addressDetail("NONE")
                    .owner(owner)
                    .ownerPhone("000-0000-0000")
                    .officePhone("00-0000-0000")
                    .bank("NONE")
                    .account("NONE")
                    .build());
        }
    }
}
