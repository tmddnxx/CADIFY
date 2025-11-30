package com.cadify.cadifyWAS.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {

    // Member
    INVALID_LOGIN_INFO(400, "올바르지 않은 로그인 정보"),
    USER_ALREADY_LOGOUT(404, "이미 로그아웃된 사용자"),
    MEMBER_NOT_FOUND(404, "존재하지 않는 사용자"),
    PROVIDER_NOT_FOUND(404, "존재하지 않는 인증 제공자"),
    USER_ALREADY_EXIST(400, "이미 존재하는 사용자"),
    UPDATE_ROLE_FAILED(400, "잘못된 role 입력"),
    NOT_USER_MEMBER(401, "개인 회원이 아닙니다."),
    WITHDRAWN_MEMBER(403, "탈퇴한 회원입니다."),
    // Member: Agreement
    AGREEMENT_NOT_FOUND(404, "존재하지 않는 약관"),
    NO_AGREED_TERMS(404, "동의 내역이 없습니다."),

//    // Mail
//    FAIL_SEND_EMAIL(500, "메일 전송 실패"),

    // Admin
    ADMIN_NOT_FOUND(404, "존재하지 않는 관리자"),
    NOT_SUPER_ADMIN(401, "권한이 없습니다. 해당 작업은 관리자에게 요청하세요"),

    // LoginType
    INVALID_LOGIN_TYPE(400, "유효하지 않은 로그인 접근"),

    // Manager
    NOT_COMPANY_MEMBER(401, "기업 회원이 아닙니다."),
    MANAGER_NOT_FOUND(404, "존재하지 않는 관리자 입니다."),
    INVALID_UPDATE_MANAGER(401, "해당 관리자의 수정 권한이 없습니다."),
    INVALID_ASSIGN_ROLE(401, "변경 권한이 없습니다."),
    ALREADY_DELETED_MANAGER(404, "이미 삭제된 관리자 입니다."),
    INVALID_MANAGER(400, "일반 유저는 담당자가 있을 수 없음"),
    MANAGER_REQUIRED(400, "담당자가 필요함"),

    // Factory
    NOT_FACTORY_ADMIN(401, "공장 관리자가 아닙니다."),
    INVALID_FACTORY_TYPE(401, "유효한 공장 타입이 아닙니다."),
    REQUIRED_ITEM_KEYS(401, "등록 대상이 없습니다."),
    REQUIRED_TRACKING_NUMBER(401, "송장 입력이 필요합니다."),
    REQUIRED_COURIER_NAME(401, "담당 택배사를 선택해야 합니다."),
    ORDER_UPDATE_FAILED(404, "해당 주문이 존재하지 않거나 이미 처리되었습니다."),
    ITEM_UPDATE_FAILED(404, "아직 처리중인 주문입니다."),
    ORDER_ALREADY_CANCELLED(404, "이미 취소된 주문입니다."),
    ORDER_ALREADY_REJECTED(404, "이미 거절된 주문입니다."),
    ORDER_ALREADY_SHIPPING(404, "이미 배송중인 주문입니다."),
    REJECTED_REASON_REQUIRED(401, "거절사유를 입력해주세요."),
    FACTORY_NOT_FOUND(404, "등록된 공장이 아닙니다."),
    ALREADY_EXIST_EMAIL(401, "이미 존재하는 사용자 입니다. ( 존재하는 이메일 )"),
    ALREADY_EXIST_USERNAME(401, "이미 사용중인 아이디 입니다."),

    // Auth
    UNKNOWN_AUTH_PROVIDER(400, "지원하지 않는 인증 제공자"),
    INVALID_ROLE(400, "지원되지 않는 사용자 역할"),
    GENERATE_TOKEN_ERROR(500, "토큰 생성 중 에러 발생"),
    PHONE_ALREADY_EXISTS(401, "이미 사용중인 전화번호 입니다."),

    // Token
    INVALID_ACCESS_TOKEN(401, "유효하지 않은 단기 토큰"),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 장기 토큰"),
    EXPIRED_ACCESS_TOKEN(401, "만료된 단기 토큰, 재 발급 필요"),
    EXPIRED_REFRESH_TOKEN(401, "만료된 장기 토큰, 재 로그인 필요"),

    // Login
    UNKNOWN_LOGIN_PRINCIPAL(401, "알 수 없는 로그인 주체"),
    WRONG_PASSWORD(401, "잘못된 패스워드"),
    UNKNOWN_LOGIN_ERROR(401, "알 수 없는 로그인 에러"),
    REQUIRED_LOGIN(401, "로그인 필요"),

    // Admin
    NOT_ALLOWED_COLUMN(500, "허용되지 않은 정렬 칼럼"),

    // Admin File
    UPLOAD_FAILED(500, "업로드 실패"),
    INVALID_TYPE(500, "올바른 타입이 아닙니다"),

    //Affiliation
    AFFILIATION_NOT_FOUND(404, "담당자가 존재하지 않음"),

    // Token
    REFRESH_TOKEN_EXPIRED(401, "만료된 사용자"),
    NOT_FOUND_TOKEN(404, "이미 삭제되었거나 존재하지 않는 토큰"),

    // Estimate
    ESTIMATE_NOT_FOUND(404, "해당 견적이 존재 하지 않음"),
    UNKNOWN_SURFACE(404, "표면처리 값을 알 수 없습니다"),
    FLAG_ALREADY_TRUE(400, "이미 요청 보냈습니다."),
    INVALID_ESTIMATE_STATUS(400, "견적의 상태가 올바르지 않습니다"),

    //Files
    FILES_NOT_FOUNT(404, "해당 파일이 존재하지 않습니다"),
    ESTIMATE_ERROR_JSON(400, "JSON 변환이 잘못되었습니다"),
    TYPE_NOT_FOUND(404, "해당 타입이 존재하지 않습니다"),

    // Folder
    FOLDER_NOT_FOUND(404, "폴더가 존재하지 않습니다."),

    // Cart
    CART_NOT_FOUND(400, "장바구니가 존재 하지 않음"),
    CART_ITEM_NOT_FOUND(400, "장바구니 상품이 존재 하지 않음"),


    //Address
    ADDRESS_NOT_FOUND(404, "주소가 존재하지 않음"),

    //Payment
    PAYMENT_NOT_FOUND(404, "데이터베이스에 결제정보가 존재하지 않음"),
    PAYMENT_FAILED(500, "결제 검증 실패"),
    PAYMENT_CANCEL_FAILED(500, "결제 취소 실패"),
    PAYMENT_CASH_RECEIPT(500, "현금 영수증 발급 실패"),
    GET_PAYMENT_FAILED(500, "결제 조회 실패"),

    // Order
    //TODO : 아래 둘 중에 하나 없애야됨 (dev 브랜치에서 하자)
    NO_ORDERS_FOUND(404, "주문이 존재하지 않음"),
    ORDER_NOT_FOUND(404, "해당 주문이 존재하지 않음"),
    ORDER_ITEM_NOT_FOUND(404, "주문 상품이 존재 하지 않음"),
    ORDER_ALREADY_PAID(400, "이미 결제된 주문입니다"),
    ALREADY_REJECTED_ORDER(400, "이미 거절된 주문"),
    REJECT_REASON_REQUIRED(400, "거절 사유 카테고리와 상세 내용을 모두 입력해야 함"),
    ALREADY_SHIPPING_ORDER(400, "이미 배송 중인 주문"),
    TRACKING_INFO_REQUIRED(400, "송장 번호와 택배사 정보를 모두 입력해야 함"),
    ORDER_AMOUNT_MISMATCH(400, "요청한 금액과 계산한 금액이 일치하지 않음"),
    MinimumOrderAmountNotMetException(400, "최소 주문금액을 만족하지 못했습니다"),
    INVALID_ORDER_STATUS(400, "유효하지 않은 주문 상태 값"),
    ORDER_ITEM_NOT_FOUND_BY_ESTKEY(404, "해당 estKey의 주문이 없습니다"),

    //pdf
    CONVERT_PDF_FAILED(500, "pdf 파일 변환에 실패했습니다"),

    //Shipment
    CALCULATE_SHIPMENT_DATE_FAILED(500, "납기일 계산 실패"),

    // SMS
    INVALID_AUTH_CODE(401, "잘못된 인증번호"),
    FAILED_SEND_CODE(500, "인증번호 전송 실패"),
    UNKNOWN_CODE_ERROR(500, "알 수 없는 코드 전송 에러"),

    // Unknwon Exception
    UNKNOWN_EXCEPTION_OCCURED(500, "알 수 없는 에러"),
    NAME_INVALID(401, "이름이 올바르지 않습니다."),

    // KFactor
    COMBINATION_NOT_FOUND(404, "해당 재료와 두께 조합이 존재하지 않음"),
    MISSING_KFACTOR_FOR_COMBINATION(400, "모든 kFactor 값을 입력해야 함"),
    INVALID_KFACTOR_RANGE(400, "KFactor 값은 0 이상 1 미만이어야 함"),
    INVALID_KFACTOR_PRECISION(400, "KFactor 값은 소수점 3자리까지 입력해야 함"),
    KFACTOR_NULL_EXISTS(400, "KFactor 값은 null 불가"),

    // JPA
    INVALID_COLUMN(401, "지원되지 않는 칼럼."),
    INVALID_VALUE(401, "허용되지 않은 값");

    private final int status;
    private final String message;

    ExceptionCode(int code, String message){
        this.status = code;
        this.message = message;
    }
}
