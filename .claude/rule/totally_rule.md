# CADIFY 코딩 컨벤션

## 언어 규칙

- 코드 주석, TODO, Javadoc은 **한글**로 작성
- 클래스명, 메서드명, 변수명은 **영문 camelCase/PascalCase** 유지
- 예외 메시지는 한글 허용 (ExceptionCode 참고)

## 네이밍 규칙

### 클래스명 (PascalCase + 접미사)
- Controller: `OrderController`, `FileController`
- Service: `OrderService`, `EstimateService`
- Facade: `EstimateCartFacade`, `PaymentEstimateFacade`
- Repository: `OrderRepository`, `CartRepository`
- QueryDSL 구현체: `AdminEstimateQueryRepository` + `AdminEstimateQueryRepositoryImpl`
- DTO: `OrdersDTO`, `AddressDTO` (내부에 static inner class)
- Entity: `Orders`, `OrderItem`, `Cart`
- Mapper: `OrderMapper` (`@Component` 사용, `@Service` 아님)

### 메서드명 (camelCase)
- CRUD 동사: `createOrder()`, `getCart()`, `updateAddress()`, `deleteOrder()`
- Boolean 필드: `isPaid`, `isFastShipment`, `isChamfer`

### 변수명
- camelCase: `loginMember`, `cartItemKey`, `deliveryCharge`
- 약어 허용: `estKey` (estimate key), `repName` (representative name)

## 계층 구조 규칙

### Controller
- `@RestController` + `@RequestMapping` + `@RequiredArgsConstructor` + `@Slf4j`
- **서비스 호출 및 반환만 처리** — 비즈니스 로직, 분기 처리, 예외 처리 금지
- **try-catch 사용 금지** — 예외는 GlobalExceptionHandler에서 일괄 처리
- 반환 타입: `ResponseEntity<T>` 사용
- 요청 검증: `@Valid @RequestBody`
- Swagger: `@Operation`, `@ApiResponse` 어노테이션 사용

### Service
- `@Service` + `@RequiredArgsConstructor` + `@Slf4j`
- **정상 응답과 에러 응답 모두 Service 메서드에서 처리**
- 에러 발생 시 반드시 `CustomLogicException` 사용 — 다른 예외 타입 직접 throw 금지
- 에러 메시지는 반드시 `ExceptionCode` enum에 정의 — 문자열 직접 사용 금지
- 데이터 변경 메서드: `@Transactional` 필수
- 동시성 이슈 있는 메서드: `@RetryOnOptimisticLock` 적용
- 복잡한 로직은 private 헬퍼 메서드로 분리

### Facade (Orchestrator)
- 여러 Service 간 조율 담당
- 자체 비즈니스 로직 최소화, 순수 조율 역할
- **기본 원칙: Facade 메서드에 `@Transactional` 부여**
- 외부 API 호출이 포함된 경우에는 `@Transactional` 부여하지 않음
- Facade에 `@Transactional`을 달았다면, 해당 Facade가 호출하는 Service 메서드에는 개별 `@Transactional`을 달지 않음
- 단, 해당 Service 메서드가 개별적으로도 호출되고 Facade에서도 호출되어야 한다면, 트랜잭션 유/무 버전을 **별도 메서드로 분리**

### Mapper
- `@Component`로 선언 (`@Service` 아님)
- **DTO ↔ Entity 상호 변환만 담당**
- 변환 시 필요하면 Repository 의존 허용
- 비즈니스 로직 금지

### Repository
- `JpaRepository<Entity, KeyType>` 상속
- 단건 조회: `Optional<T>` 반환
- 다건 조회: `List<T>` 반환
- 복잡한 쿼리: 별도 QueryRepository 인터페이스 + Impl 클래스
- Spring Data 메서드 네이밍 컨벤션 준수: `findByXxxOrderByYyyDesc()`

### QueryDSL 구현체
- Q-class는 `private static final`로 클래스 레벨에 선언
- `EntityManager`를 생성자에서 주입받아 `JPAQueryFactory` 초기화
- 다중 엔티티 프로젝션 시 `Tuple` 반환

## DTO 규칙

- 하나의 외부 DTO 클래스 안에 **static inner class**로 Request/Response 정의
- Request: `@Getter @Setter @NoArgsConstructor`
- Response: `@Getter @Builder @AllArgsConstructor` (Setter 없음)
- 날짜 포맷: `@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")`

## Entity 규칙

- `BaseTimeEntity` 상속 (`@CreatedDate`, `@LastModifiedDate` 자동 관리)
- PK: `@GeneratedValue(strategy = GenerationType.UUID)` 사용
- 낙관적 락: `@Version private Long version;`
- Enum 필드: `@Enumerated(EnumType.STRING)`
- 큰 텍스트: `@Column(columnDefinition = "text")`
- S3 주소: `@Column(length = 1000)`
- 도메인 로직을 Entity 내부에 포함 (Rich Domain Model)
- `@Builder`는 생성자에 적용 (선택적 필드 초기화)

## 예외 처리 규칙

- `CustomLogicException` + `ExceptionCode` enum 사용
- `orElseThrow(() -> new CustomLogicException(ExceptionCode.XXX))` 패턴
- ExceptionCode는 static import: `import static ...ExceptionCode.*;`

## API 응답 규칙

- **정상 응답:** `ResultResponse` 사용 (`status`, `code`, `message`, `data`)
  - `ResultResponse.of(ResultCode.XXX, data)` 또는 `ResultResponse.of(ResultCode.XXX)`
  - 정상 응답 코드는 반드시 `ResultCode` enum에 정의
- **에러 응답:** `ErrorResponse` 사용 (`status`, `code`, `message`, `occurredAt`)
  - Service에서 `CustomLogicException` throw → `GlobalExceptionHandler`가 `ErrorResponse`로 변환하여 반환
  - 에러 코드는 반드시 `ExceptionCode` enum에 정의
  - 에러 시 반드시 적절한 HttpStatus 사용 (400, 401, 403, 404, 500 등) — 에러인데 200 반환 금지
- **컨트롤러 반환 타입은 정상 케이스만 정의** — 에러는 `GlobalExceptionHandler`가 가로채므로 컨트롤러에 도달하지 않음
  ```java
  // 컨트롤러는 정상 응답 타입만 선언
  public ResponseEntity<ResultResponse> getOrder(@PathVariable String orderKey) {
      return ResponseEntity.ok().body(orderService.getOrder(orderKey));
  }
  ```

## Git 커밋 메시지 규칙

- `행위: 설명(한글)` 형식으로 작성
- 행위 접두사:
  - `feat:` 기능 추가
  - `fix:` 기존 버그 수정
  - `refactor:` 코드 리팩터링 (기능 변경 없음)
  - `chore:` 빌드, 설정, 의존성 등 기타 작업
  - `docs:` 문서 수정
  - `test:` 테스트 추가/수정
  - `style:` 코드 포맷팅, 세미콜론 등 (기능 변경 없음)
- 예시: `feat: 주문 취소 기능 추가`, `fix: 장바구니 중복 등록 오류 수정`

## 의존성 주입

- **생성자 주입만 사용** — `@RequiredArgsConstructor` + `private final` 필드
- `@Autowired` 사용 금지

## Import 규칙

- 와일드카드 import (`*`) 사용 금지
- 명시적으로 개별 import

## 로깅

- 클래스 레벨 `@Slf4j` 사용 (`log.info()`, `log.error()` 등)
- `System.out.println()` / `System.err.println()` 사용 금지
