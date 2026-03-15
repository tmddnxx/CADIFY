# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

CADIFY는 3D STEP 파일 분석 및 제조 원가 자동 견적 플랫폼이다. Docker 샌드박스 기반 외부 분석 엔진으로 STEP 파일을 분석하고, RabbitMQ 큐를 통해 비동기 처리한 뒤, 판금(METAL) 및 CNC 가공 비용을 자동 산정한다.

## 빌드 및 실행

```bash
# 빌드
./gradlew build

# 테스트 실행 (JUnit 5)
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.cadify.cadifyWAS.CadifyWasApplicationTests"

# 애플리케이션 실행
./gradlew bootRun

# JAR 빌드 (cadfiy-1.0.jar 생성)
./gradlew bootJar
```

**Java 17** 필수 (Gradle toolchain으로 설정됨).

## 기술 스택

- **프레임워크:** Spring Boot 3.3.3, Java 17
- **데이터베이스:** PostgreSQL (JPA/Hibernate, QueryDSL)
- **메시지 큐:** RabbitMQ (4개 큐: metal/CNC 업로드 + 결과)
- **클라우드:** AWS ECS Fargate (STEP 분석기 실행), S3 (파일 저장), STS
- **캐시:** Redis
- **인증:** OAuth2 (Google) + JWT 토큰
- **API 문서:** SpringDoc OpenAPI (Swagger UI)

## 아키텍처

### 계층 구조

```
Controllers → Services (+ Facades) → Repositories → PostgreSQL
                 ↕
          RabbitMQ Queues
                 ↕
         AWS ECS Fargate (STEP 분석기)
```

모든 소스는 `src/main/java/com/cadify/cadifyWAS/` 하위에 위치한다.

### 핵심 처리 파이프라인

1. **파일 업로드:** `FileController`가 STEP 파일 수신 → `FileTaskProducer`가 RabbitMQ에 발행
2. **분석:** `FileTaskConsumer`가 큐에서 수신 → `SdkService`를 통해 ECS Fargate 태스크 실행
3. **결과 처리:** 분석 JSON 반환 → 피처(홀, 벤드, 두께, 표면적) 파싱
4. **견적 산정:** `EstimateService`가 추출된 기하 데이터 기반으로 제조 비용 계산

### 두 가지 가공 방식

- **METAL** — 판금 가공 (벤딩, 커팅). 큐: `metal-file-upload-queue`, `metal-file-result-queue`
- **CNC** — CNC 가공. 큐: `cnc-file-upload-queue`, `cnc-file-result-queue`. `CNCAxisAnalyzer`로 축 분석 수행

### Facade 패턴

- `EstimateCartFacade` — 견적 + 장바구니 연동
- `PaymentEstimateFacade` — 결제 + 견적 연동

### 실시간 업데이트

Server-Sent Events (`SseEmitters`)로 클라이언트에 처리 상태를 실시간 전송한다.

### 동시성 처리

- `OptimisticLockRetryAspect` + `@RetryOnOptimisticLock` 어노테이션으로 낙관적 락 실패 시 AOP 기반 재시도
- 메인 애플리케이션에 `@EnableAsync`, `@EnableScheduling` 적용
- RabbitMQ 리스너는 전용 스레드 풀 사용 (업로드: 10-20 스레드, 결과 처리: 5-10 스레드)

## 설정

`application.properties`는 저장소에 포함되지 않으며, 환경 변수 또는 외부 설정으로 제공해야 한다:
- PostgreSQL 연결 정보
- RabbitMQ 연결 정보
- AWS 자격증명 (S3, ECS, STS)
- Redis 연결 정보
- Google OAuth2 클라이언트 자격증명
- JWT 시크릿 키

## 코딩 컨벤션

- **Lombok:** 전체적으로 사용 — `@Slf4j`, `@Getter`, `@Builder` 등. 어노테이션 프로세싱 활성화 필수
- **QueryDSL:** 커스텀 쿼리 리포지토리에서 `QueryDslUtils` 활용
- **DTO:** 도메인별 `dto/` 패키지로 분류 (order, auth, payment, member, admin, factory)
- **컴파일러 옵션:** `-parameters` 플래그 활성화 (메서드 파라미터명 보존)
