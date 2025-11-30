package com.cadify.cadifyWAS.config;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalEnv {
    private static final ThreadLocal<Map<String, String>> threadLocalEnv = ThreadLocal.withInitial(HashMap::new);

    // 환경 변수 설정
    public static void setEnv(String key, String value) {
        threadLocalEnv.get().put(key, value);
    }

    // 환경 변수 가져오기
    public static String getEnv(String key) {
        return threadLocalEnv.get().get(key);
    }

    // 전체 환경 변수 반환
    public static Map<String, String> getAllEnv() {
        return threadLocalEnv.get();
    }

    // 환경 변수 초기화
    public static void clear() {
        threadLocalEnv.remove();
    }
}
