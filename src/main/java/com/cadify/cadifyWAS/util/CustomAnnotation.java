//package com.cadify.cadifyWAS.Util;
//
//import com.cadify.cadifyWAS.model.dto.auth.AuthDTO;
//import com.cadify.cadifyWAS.model.dto.member.MemberDTO;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.Parameters;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
//public class CustomAnnotation {
//    public static class Member{
//        @Target(ElementType.METHOD)
//        @Retention(RetentionPolicy.RUNTIME)
//        @Operation(summary = "로그인")
//        @ApiResponses(value = {
//                @ApiResponse(
//                        responseCode = "200",
//                        description = "로그인 성공",
//                        content = @Content(
//                                mediaType = "application/json",
//                                schema = @Schema(implementation = AuthDTO.AuthResponse.class)
//                        ))
//        })
//        @Parameters({
//                @Parameter(name = "email", description = "이메일", example = "test@example.com"),
//                @Parameter(name = "password", description = "비밀번호", example = "Password!"),
//        })
//        public @interface LoginOperation {
//        }
//        @Target(ElementType.METHOD)
//        @Retention(RetentionPolicy.RUNTIME)
//        @Operation(summary = "회원가입")
//        @ApiResponses(value = {
//                @ApiResponse(
//                        responseCode = "200",
//                        description = "회원가입 완료",
//                        content = @Content(
//                                mediaType = "application/json",
//                                schema = @Schema(implementation = MemberDTO.Response.class)
//                        ))
//        })
//        @Parameters({
//                @Parameter(name = "memberName", description = "이름", example = "신짱구"),
//                @Parameter(name = "email", description = "이메일", example = "test@example.com"),
//                @Parameter(name = "password", description = "비밀번호", example = "Password!"),
//                @Parameter(name = "addressNumber", description = "우편번호", example = "1001"),
//                @Parameter(name = "addressDetail", description = "상세주소", example = "A동 101호"),
//                @Parameter(name = "phone", description = "휴대폰 번호", example = "010-1234-5678"),
//                @Parameter(name = "memberRole", description = "권한(역할)", example = "USER"),
//                @Parameter(name = "consents", description = "정책 동의 여부",
//                        example =   "{\n" +
//                                    "  \"MARKETING\": \"AGREE\",\n" +
//                                    "  \"ALM_EMAIL\": \"DISAGREE\",\n" +
//                                    "  \"ALM_PHONE\": \"DISAGREE\"\n" +
//                                    "}")
//        })
//        public @interface SignUpOperation {
//        }
//        @Target(ElementType.METHOD)
//        @Retention(RetentionPolicy.RUNTIME)
//        @Operation(summary = "회원 정보 수정")
//        @ApiResponses(value = {
//                @ApiResponse(
//                        responseCode = "200",
//                        description = "수정 완료",
//                        content = @Content(
//                                mediaType = "application/json",
//                                schema = @Schema(implementation = AuthDTO.AuthResponse.class)
//                        ))
//        })
//        @Parameters({
//                @Parameter(name = "memberName", description = "이름", example = "신짱구"),
//                @Parameter(name = "email", description = "이메일", example = "test@example.com"),
//                @Parameter(name = "password", description = "비밀번호", example = "Password!"),
//                @Parameter(name = "addressNumber", description = "우편번호", example = "0110"),
//                @Parameter(name = "addressDetail", description = "상세주소", example = "B동 202호"),
//                @Parameter(name = "consents", description = "정책 동의 여부",
//                        example =   "{\n" +
//                                "  \"MARKETING\": \"AGREE\",\n" +
//                                "  \"ALM_EMAIL\": \"AGREE\",\n" +
//                                "  \"ALM_PHONE\": \"AGREE\"\n" +
//                                "}")
//        })
//        public @interface ModifyOperation {
//        }
//        @Target(ElementType.METHOD)
//        @Retention(RetentionPolicy.RUNTIME)
//        @Operation(summary = "회원탈퇴")
//        @ApiResponses(value = {
//                @ApiResponse(
//                        responseCode = "200",
//                        description = "회원 정보 삭제 완료",
//                        content = @Content(
//                                mediaType = "application/json",
//                                schema = @Schema(type = "String")
//                        ))
//        })
//        @Parameters({
//                @Parameter( name = "password",
//                            description = "비밀번호 / body부분에 일체의 형식 없이(괄호, 쌍따옴표 등)password에 해당하는 String만 입력.",
//                            example = "Password!")
//        })
//        public @interface LeaveOperation {
//        }
//    }
//}
