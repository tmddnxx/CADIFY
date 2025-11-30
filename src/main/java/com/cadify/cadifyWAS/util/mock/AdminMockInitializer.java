    package com.cadify.cadifyWAS.util.mock;

    import com.cadify.cadifyWAS.model.entity.factory.Factory;
    import com.cadify.cadifyWAS.model.entity.factory.FactoryAdmin;
    import com.cadify.cadifyWAS.model.entity.member.MemberRole;
    import com.cadify.cadifyWAS.repository.factory.FactoryRepository;
    import com.cadify.cadifyWAS.repository.factory.admin.FactoryAdminRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    public class AdminMockInitializer {

        private final FactoryAdminRepository factoryAdminRepository;
        private final FactoryRepository factoryRepository;
        private final PasswordEncoder passwordEncoder;

        public void init() {

            // testAdmin ( 서비스 관리자 ) * 임시 role = USER
            saveFactoryAdmin(
                    ConstantMockData.TEST_ADMIN_KEY, "testAdmin", "qwer1234!", "테스트_관리자",
                    "testAdmin01@test.com", "010-1111-1111", MemberRole.USER, findFactoryKey("DefaultSuperFactory"));
            // testUser ( 유저 테스트 ) Form 로그인용 데이터
            saveFactoryAdmin(
                    ConstantMockData.TEST_USER_KEY, "testUser", "qwer1234#", "테스트_유저",
                    "testUser01@test.com", "010-5555-5555", MemberRole.USER, findFactoryKey("DefaultSuperFactory"));
            // testFactory ( 공장 관리자 )
            saveFactoryAdmin(
                    ConstantMockData.TEST_FACTORY_ADMIN_KEY, "testFactory", "qwer1234@", "테스트_공장관리자",
                    "testFactoryAdmin01@test.com", "010-3333-3333", MemberRole.FACTORY, findFactoryKey("DefaultSuperFactory"));
            // testMetal ( 판금 공장 관리자 )
            saveFactoryAdmin(
                    ConstantMockData.METAL_FACTORY_ADMIN_KEY, "testMetal", "qwer1234%", "테스트_판금관리자",
                    "testMetal01@test.com", "010-7777-7777", MemberRole.FACTORY, findFactoryKey("DefaultMETALFactory"));
            // testCNC ( 절삭 공장 관리자 )
            saveFactoryAdmin(
                    ConstantMockData.CNC_FACTORY_ADMIN_KEY, "testCNC", "qwer1234$", "테스트_절삭관리자",
                    "testCNC01@test.com", "010-9999-9999", MemberRole.FACTORY, findFactoryKey("DefaultCNCFactory"));
        }


        private void saveFactoryAdmin(String key, String username, String password, String name, String email, String phone, MemberRole role, String factoryKey) {
            if (factoryAdminRepository.findByUsernameAndDeletedFalse(username).isEmpty()) {
                factoryAdminRepository.save(FactoryAdmin.builder()
                        .memberKey(key)
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .name(name)
                        .email(email)
                        .phone(phone)
                        .role(role)
                        .factoryKey(factoryKey)
                        .build());
            }
        }

        private String findFactoryKey(String factoryName){
            Factory factory = factoryRepository.findFactoryByFactoryNameAndDeletedFalse(factoryName)
                    .orElseThrow();
            return factory.getFactoryKey();
        }
    }
