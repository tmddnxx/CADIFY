package com.cadify.cadifyWAS.util.mock;

import com.cadify.cadifyWAS.util.UUIDGenerator;

public class ConstantMockData {
    public static String SUPER_FACTORY_KEY = UUIDGenerator.generateUUID().toString();
    public static String CNC_FACTORY_KEY = UUIDGenerator.generateUUID().toString();
    public static String METAL_FACTORY_KEY = UUIDGenerator.generateUUID().toString();
    public static String TEST_ADMIN_KEY = UUIDGenerator.generateUUID().toString();
    public static String TEST_USER_KEY = UUIDGenerator.generateUUID().toString();
    public static String TEST_FACTORY_ADMIN_KEY = UUIDGenerator.generateUUID().toString();
    public static String CNC_FACTORY_ADMIN_KEY = UUIDGenerator.generateUUID().toString();
    public static String METAL_FACTORY_ADMIN_KEY = UUIDGenerator.generateUUID().toString();

    private ConstantMockData() {} // 생성 방지
}
