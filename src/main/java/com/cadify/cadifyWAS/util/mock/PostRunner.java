package com.cadify.cadifyWAS.util.mock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostRunner implements CommandLineRunner {

    private final FactoryMockInitializer factoryMockInitializer;
    private final AdminMockInitializer adminMockInitializer;
    private final MemberMockInitializer memberMockInitializer;

    @Override
    public void run(String... args) throws Exception {
        factoryMockInitializer.init();
        adminMockInitializer.init();
        memberMockInitializer.init();
    }
}
