# CADIFY 리팩토링 진행사항

## 개요
- **목표:** `.claude/rule/totally_rule.md` 코딩 컨벤션에 맞게 전체 코드베이스 통일
- **시작일:** 2026-03-14
- **완료일:** 2026-03-14
- **Java 17:** Homebrew로 설치, 전체 Phase 컴파일 검증 완료

## Phase 구분

| Phase | 브랜치 | 대상 | 상태 | 변경 파일수 |
|-------|--------|------|------|-----------|
| Phase 1 | refactor/0.0.1 | Controller 계층 | ✅ 완료 | 32 |
| Phase 2 | refactor/0.0.2 | Service / Facade 계층 | ✅ 완료 | 29 |
| Phase 3 | refactor/0.0.3 | DTO / Entity / Mapper | ✅ 완료 | 57 |
| Phase 4 | refactor/0.0.4 | Repository / QueryDSL / 전역 규칙 | ✅ 완료 | 38 |

---

## Phase 1: Controller 계층 리팩토링 (refactor/0.0.1)

**커밋:** `2cc96ce` — 32 files changed, +662 -304

### 변경 내용
- 전체 Controller에 `@Slf4j` + `@RestController` + `@RequestMapping` + `@RequiredArgsConstructor` 통일
- Controller 내 try-catch 제거 → GlobalExceptionHandler 위임 (7건)
- Controller 내 비즈니스 로직/분기 → Service 계층으로 이동 (11건)
  - AdminController: DXF 다운로드 → FilesByFactoryService
  - AuthController: 쿠키 추출 → AuthService.extractRefreshToken()
  - MemberController: Role 매핑 → OAuthMemberService.assignMemberRole()
  - CartController: 중복 분기 → CartService.addCartIem()
  - EstimateController: try-catch 5건 + @ExceptionHandler 제거
  - FileController: METAL/CNC 라우팅 → FileTaskProducer.sendByResult()
- `@Valid @RequestBody` 누락 11건 추가
- `System.out.println` → `log.info/error` 전환 (8건)
- `@Log4j2` → `@Slf4j` 전환
- 와일드카드 import → 명시적 import 전환
- `ResultCode.SEND_MAIL_SUCCESS` 추가
- `MailController`: void → `ResponseEntity<ResultResponse>` 반환 전환

---

## Phase 2: Service/Facade 계층 리팩토링 (refactor/0.0.2)

**커밋:** `b510625` — 29 files changed, +242 -178

### 변경 내용
- 전체 Service에 `@Slf4j` 통일 (`@Log4j2` → `@Slf4j` 전환)
- `RuntimeException/Exception/IllegalArgumentException` → `CustomLogicException` 전환 (30+ 건)
- `System.out.println/System.err.println/e.printStackTrace()` → `log.info/error` (30+ 건)
- `jakarta.transaction.Transactional` → `org.springframework.transaction.annotation.Transactional` 전환
- `@Transactional` 누락 보완: CartService, FolderService, KFactorService, AdminMemberService(readOnly)
- Facade `@Transactional` 규칙 적용: EstimateCartFacade, PaymentEstimateFacade
- `ExceptionCode` enum 21개 신규 추가
- **로직 버그 수정:** FactoryAdminService/FactoryService 역할 검증 조건 (`||` → `&&`)
- OrderService `@Autowired` 제거
- CompanyManagerService `return null` → 실제 목록 반환
- PaymentWebHookService `@Value` 누락 추가
- MailService 에러 핸들링 추가
- 파라미터 로깅 형식 통일: `log.info("msg: {}", value)`

---

## Phase 3: DTO/Entity 리팩토링 (refactor/0.0.3)

**커밋:** `d566156` — 57 files changed, +204 -50

### 변경 내용

**Entity (16개)**
- 전체 Entity에 `@Version private Long version` 추가 (낙관적 락)

**DTO - Request**
- `@Getter @Setter @NoArgsConstructor` 통일
- `@Data` → 정확한 어노테이션 조합 전환 (AdminMemberDTO, AdminOrderDTO)
- 누락 `@Setter` 추가 (FactoryOrderDTO, CartItemDTO, MemberDTO 등)

**DTO - Response**
- setter 미사용 확인 후 `@Setter` 제거 (OrdersDTO, OrderItemDTO, AddressDTO, CartItemDTO 등)
- setter 사용 확인된 Response는 `@Setter` 유지 (EstimateDTO, CostDTO, PaymentDTO 등)
- `@AllArgsConstructor` + `@NoArgsConstructor` 추가
- OrderItemDTO에서 `@Column` JPA 어노테이션 제거
- `@Builder.Default` 적용 (OrderItemDTO.Response.unitPrice)

---

## Phase 4: Repository/QueryDSL/전역 규칙 (refactor/0.0.4)

**커밋:** `788e228` — 38 files changed, +239 -57

### 변경 내용

**QueryDSL (5개)**
- Q-class 선언 `private final` → `private static final` 통일
- 생성자 파라미터명 `entitymanager` → `entityManager` (camelCase)

**와일드카드 Import 제거 (33개 파일)**
- `jakarta.persistence.*`, `lombok.*`, `java.util.*`, `querydsl.*`, `io.jsonwebtoken.*` 등 38개 와일드카드 → 개별 import

**주석 한글화 (2개 파일)**
- AdminMemberQueryRepositoryImpl, AdminOrderQueryRepositoryImpl

**잔여 System.out 제거 (1개)**
- HolidayAPI: `System.out.println` → `@Slf4j log.info`

---

## 전체 요약

| 항목 | 수량 |
|------|------|
| 총 변경 파일 | 156개 |
| 총 커밋 | 4개 |
| 총 브랜치 | 4개 (refactor/0.0.1 ~ 0.0.4) |
| 컴파일 에러 | 0 (전체 Phase BUILD SUCCESSFUL) |
| 로직 버그 수정 | 2건 (FactoryAdminService, FactoryService) |
| ExceptionCode 신규 추가 | 21개 |
| System.out.println 제거 | 40+ 건 |
| RuntimeException 전환 | 30+ 건 |
| 와일드카드 import 제거 | 70+ 건 |
| @Version 추가 | 16개 Entity |
