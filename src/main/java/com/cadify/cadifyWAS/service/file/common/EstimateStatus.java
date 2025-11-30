package com.cadify.cadifyWAS.service.file.common;

public enum EstimateStatus {
    SUCCESS, // 변화없음
    ESTIMATE_POLICY_VERSION_MISMATCH, // 정책 버전 불일치 (검증 시 변경된 수치로 제작불가인 경우 [장바구니에서 삭제요청])
    ESTIMATE_FILE_NOT_FOUND, // 파일 없음 (장바구니에서 삭제 요청)
    CHANGE_AMOUNT, // 가격 변경
    INVALID_ERROR,
}
