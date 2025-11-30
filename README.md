📘 CADIFY – Automated STEP File Analysis & Manufacturing Cost Estimation System
<p align="left"> <img src="https://img.shields.io/badge/Java-17-007396?logo=java&logoColor=white" /> <img src="https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white" /> <img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white" /> <img src="https://img.shields.io/badge/RabbitMQ-3.x-FF6600?logo=rabbitmq&logoColor=white" /> <img src="https://img.shields.io/badge/AWS-ECS%20Fargate-FF9900?logo=amazonaws&logoColor=white" /> <img src="https://img.shields.io/badge/Docker-Engine-2496ED?logo=docker&logoColor=white" /> </p>

CADIFY는 3D 제조업 자동 견적 시스템을 위해 설계된
STEP 3D 모델 분석 · 파트 구조 추출 · 제조 공정 난이도 분석 · 견적 자동화 플랫폼입니다.

외부 STEP 분석 엔진을 Docker 기반으로 샌드박싱하여 실행하고,
RabbitMQ 기반의 분산 처리 아키텍처를 통해 대규모 파일 업로드에도 안정적인 처리 성능을 보장합니다.

🚀 Highlights
✔ Industrial-Grade STEP File Analysis Pipeline

외부 라이선스 기반 STEP 분석 엔진을 Docker 이미지 단위로 실행

분석 결과(JSON)를 후처리하여 피처 / 홀 / 두께 / 표면적 / 난이도 정보 추출

파트별 ID 관리 및 제조 요소 자동 계산

✔ Asynchronous Distributed Processing

RabbitMQ 기반 비동기 작업 큐로 안정적 병렬 처리

파일 업로드 수백 건 이상에서도 지연 없이 처리

메시지 Failure → 재처리 지원

✔ Cloud-Native Architecture

AWS ECS Fargate 기반 완전 관리형 컨테이너 실행 환경

ECR로 Docker 이미지 중앙 관리

Auto Scaling 기반 수평 확장

✔ Manufacturing Cost Estimation Engine

파트의 기하 정보 & 분석 데이터 기반 비용 산정

가공 난이도, 면적, 홀 수, 피처 종류 등 반영

파트 단위 상세 견적 계산

🏗️ Architecture Overview
          ┌──────────────────────┐
          │      User Upload     │
          └──────────┬───────────┘
                     │
          ┌──────────▼──────────┐
          │   Spring Boot API    │
          └──────────┬──────────┘
                     │  Publish Task
          ┌──────────▼──────────┐
          │      RabbitMQ        │
          └──────────┬──────────┘
                     │  Consume Task
      ┌──────────────▼──────────────┐
      │   Analyzer Worker (Docker)   │
      │  ▶ STEP → JSON Feature Data  │
      └──────────────┬──────────────┘
                     │
          ┌──────────▼──────────┐
          │  Post Processor      │
          │ Feature Mapping, Cost│
          └──────────┬──────────┘
                     │
          ┌──────────▼──────────┐
          │    PostgreSQL DB    │
          └──────────────────────┘

📂 Directory Structure (Example)
/api
  ├── controller
  ├── service
  ├── domain
  ├── repository
  ├── config
/worker
  ├── docker
  ├── parser
  ├── scheduler
  ├── utils
/analysis
  ├── json-mapper
  ├── geometry
infra/
  ├── aws
  ├── ecs
  ├── ecr
  ├── rabbitmq

⚙️ Technology Stack
Area	Tech
Backend	Java 17, Spring Boot, JPA
Database	PostgreSQL
Async / Queue	RabbitMQ
Infrastructure	Docker, AWS ECS Fargate, Amazon ECR
DevOps	GitHub Actions, Cloud Logging/Monitoring
STEP Analysis	External Analyzer (Docker Sandbox)
📡 API Examples
▶ Upload STEP File
POST /api/v1/files
Content-Type: multipart/form-data

▶ Get Analysis Result
GET /api/v1/files/{fileId}/analysis

▶ Get Cost Estimation
GET /api/v1/files/{fileId}/cost

📈 Performance Achievements
🔧 분석 파이프라인 최적화

STEP 분석 후처리 최적화로 처리 속도 50% 개선

대량 파일 처리 시 서버 부하 60% 감소

⚡ 분산 처리 구조

RabbitMQ 기반 큐 처리로 동시 사용자 증가에도 안정성 확보

ECS Fargate 활용 → 트래픽 증가 시 자동 확장

🛠 운영 안정성

Docker 기반 분석 엔진 격리로 장애 영향 최소화

JSON 분석 파이프라인에서 오류 감지 & 자동 재처리 기능 구현

🧑‍💻 Responsibilities (Author)

전체 시스템 아키텍처 설계 및 초기 기반 구축

Spring Boot 기반 API 서버 개발

RabbitMQ 기반 비동기 메시지 파이프라인 설계 및 구현

Docker 기반 STEP 분석 엔진 연동 및 후처리 자동화

PostgreSQL 스키마 설계 및 성능 개선

AWS ECS Fargate 기반 배포 및 컨테이너 운영 자동화

제조 견적 계산 알고리즘 개발

대량 파일 처리 최적화 및 에러 복구 로직 구축

📅 Roadmap

 IGES / Parasolid 등 CAD 포맷 확장

 3D 뷰어 기반 시각화 페이지 추가

 ML 기반 자동 피처 분류 모델 적용

 분석 이력 대시보드 구축

📄 License

본 프로젝트는 사내/개인 목적의 Private Repository로 사용됩니다.
무단 사용 및 배포를 금합니다.
