# CADIFY 리팩토링 진행사항

## 개요
- **목표:** `.claude/rule/totally_rule.md` 코딩 컨벤션에 맞게 전체 코드베이스 통일
- **시작일:** 2026-03-14
- **기준 브랜치:** refactor/0.0.1
- **제약사항:** 로컬 Java 미설치 → 컴파일/테스트 불가, 기계적 안전 변경 우선

## Phase 구분

| Phase | 브랜치 | 대상 | 상태 | 위반 파일수 |
|-------|--------|------|------|-----------|
| Phase 1 | refactor/0.0.1 | Controller 계층 | 🔄 진행중 | 18/25 |
| Phase 2 | refactor/0.0.2 | Service / Facade 계층 | ⏳ 대기 | 29개 |
| Phase 3 | refactor/0.0.3 | DTO / Entity / Mapper | ⏳ 대기 | 53 DTO + 17 Entity |
| Phase 4 | refactor/0.0.4 | Repository / QueryDSL / 전역 규칙 | ⏳ 대기 | 68 (와일드카드 import) |

---

## 전체 분석 결과 요약

### Controller (25개)
- 준수: 4개 (AdminDashboard, Folder, Order, PaymentTest)
- 비활성: 3개 (Factory-주석처리, TestController-빈파일)
- **위반 유형:** try-catch(7), System.out(8), 비즈니스 로직(11), @Valid 누락(11), @Slf4j 누락(10)

### Service/Facade (29개 위반)
- **심각:** RuntimeException 직접 throw(20+), System.out.println(30+), @Slf4j 누락(15+)
- **@Transactional 누락:** 8+ 메서드
- **로직 버그:** FactoryAdminService/FactoryService의 `||` → `&&` 조건문 오류
- **Facade:** @Transactional 누락(2건)

### DTO (53개 전수 위반)
- Response에 @Setter 사용 (불변성 위반)
- outer class에 미포함된 standalone Response (20+개)
- @Builder/@AllArgsConstructor 누락
- @Data 사용 (정확한 어노테이션 조합 필요)

### Entity (17/19 위반)
- @Version 누락 (17개 중 2개만 보유: OrderItem, Orders)
- BaseTimeEntity 미상속 (8개)
- GenerationType.UUID 미사용

### Mapper (12개 전수 준수)

### Repository/QueryDSL
- Q-class `private static final` 누락: 5건
- 와일드카드 import: 68개 파일
- 영문 주석: 7개 파일

---

## Phase 1: Controller 계층 리팩토링

### 작업 그룹

#### Group A: Admin + Auth + Member (에이전트 A)
| 파일 | 위반 | 상태 |
|------|------|------|
| AdminController | try-catch, System.out, 비즈니스 로직 | 🔄 |
| AdminMemberController | @Valid 누락 | 🔄 |
| AdminOrderController | 비즈니스 로직(분기) | 🔄 |
| AuthController | @Slf4j 누락, 비즈니스 로직(쿠키) | 🔄 |
| CompanyController | @Valid 누락(2), Map 파싱 | 🔄 |
| MemberController | 비즈니스 로직(역할매핑), @Valid 누락(4) | 🔄 |
| FactoryAdminController | @Slf4j, @Valid 누락 | 🔄 |
| MemberAgreementController | @Slf4j, @Valid 누락 | 🔄 |
| AdminDashboardController | ✅ 준수 | - |

#### Group B: Factory + File (에이전트 B)
| 파일 | 위반 | 상태 |
|------|------|------|
| FactoryEstimateController | 중복import, try-catch, System.out | 🔄 |
| FactoryOrderController | @Slf4j 누락, 검증로직 | 🔄 |
| FactoryDashboardController | @Slf4j 누락 | 🔄 |
| KFactorController | @Slf4j 누락 | 🔄 |
| EstimateController | try-catch(5), @ExceptionHandler | 🔄 |
| FileController | System.out(6), 라우팅 로직 | 🔄 |

#### Group C: Order + Payment + Mail (에이전트 C)
| 파일 | 위반 | 상태 |
|------|------|------|
| CartController | 비즈니스 로직(분기) | 🔄 |
| PaymentController | @Slf4j, @Valid 누락(3) | 🔄 |
| PaymentWebHookController | @Valid 누락 | 🔄 |
| MailController | @Slf4j 누락, void 반환 | 🔄 |
| AWSController | @RequiredArgsConstructor, @Slf4j 누락 | 🔄 |

---

## Phase 2: Service / Facade 계층 리팩토링 (예정)

### 주요 작업
1. RuntimeException → CustomLogicException 전환 (20+ 건)
2. System.out.println → log.xxx() 전환 (30+ 건)
3. @Slf4j 누락 추가 (15+ 건)
4. @Transactional 누락 추가 (8+ 건)
5. Facade @Transactional 규칙 적용 (2건)
6. 로직 버그 수정 (FactoryAdminService, FactoryService)
7. jakarta.transaction → org.springframework.transaction 전환

## Phase 3: DTO / Entity / Mapper 리팩토링 (예정)

### 주요 작업
1. Response DTO: @Setter 제거, @Builder + @AllArgsConstructor 추가
2. Request DTO: @Getter @Setter @NoArgsConstructor 통일
3. standalone Response → outer class inner class로 통합 (선택적)
4. Entity: @Version 추가, BaseTimeEntity 상속 통일
5. @Data → 정확한 어노테이션 조합 전환

## Phase 4: Repository / QueryDSL / 전역 규칙 리팩토링 (예정)

### 주요 작업
1. Q-class `private static final` 통일 (5건)
2. 와일드카드 import 제거 (68개 파일)
3. System.out.println 잔여분 제거
4. 영문 주석 → 한글 전환 (7건)

---

## 커밋 로그
| 날짜 | 브랜치 | 커밋 메시지 | 대상 파일 |
|------|--------|------------|----------|
| - | - | _(진행중)_ | - |
