# Synclog

AI 기반 회의록/문서 협업 백엔드 프로젝트입니다.  
워크스페이스 단위로 문서를 관리하고, 문서 본문을 저장한 뒤 임베딩 기반 유사 문서 검색과 질의응답(RAG), 업무 추출 기능을 제공합니다.

## 1. 프로젝트 개요

Synclog는 회의록/문서를 협업 단위인 워크스페이스에 저장하고, 저장된 문서 내용을 기반으로 다음 기능을 수행합니다.

- 문서 생성 및 메타데이터 조회
- 문서 스냅샷 저장
- RAG 기반 질의응답
- 문서 내용으로부터 할 일(Task) 추출
- 워크스페이스 생성, 초대, 멤버 역할 관리

문서 내용은 plain text와 binary snapshot을 함께 저장하고, 텍스트가 변경되면 임베딩을 생성하여 pgvector 기반 검색에 활용합니다.

## 2. 기술 스택

### Backend
<div>
<img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/postgresql-4169E1.svg?style=for-the-badge&logo=postgresql&logoColor=white"> 
</div>

### Monitoring / Performance
<div>
<img src="https://img.shields.io/badge/prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white">
<img src="https://img.shields.io/badge/grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white">
<img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">
</div>

### Infra / Deployment
<div>
<img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=Amazon%20EC2&logoColor=white">
<img src="https://img.shields.io/badge/Amazon%20RDS-0000CC?style=for-the-badge&logo=amazonrds&logoColor=white">   
<img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> 
<img src="https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white">
</div>

## 3. 핵심 기능

### 3.1 사용자 인증

- 회원가입
- 로그인
- JWT 발급 및 검증
- Stateless 인증 방식

인증이 필요한 요청은 `JwtAuthenticationFilter`에서 `Authorization: Bearer ...` 토큰을 파싱하고, `userId`를 request attribute에 주입하는 방식으로 처리합니다.
<div>
<img width="45%" height="auto" alt="로그인" src="https://github.com/user-attachments/assets/bd87242d-c29d-4f03-8b44-c2fdf3c5e74c" />
<img width="45%" height="auto" alt="회원가입" src="https://github.com/user-attachments/assets/50159189-e954-4673-915a-88b041256bf7" />
</div>

관련 코드:
- [UserService.kt](/src/main/kotlin/com/example/synclog/user/service/UserService.kt)
- [JwtAuthenticationFilter.kt](/src/main/kotlin/com/example/synclog/common/security/JwtAuthenticationFilter.kt)
- [SecurityConfig.kt](/src/main/kotlin/com/example/synclog/common/security/SecurityConfig.kt)

### 3.2 워크스페이스 협업

- 워크스페이스 생성
- 워크스페이스 목록 조회
- 멤버 초대
- 멤버 역할 변경
- 멤버 제거
- 워크스페이스 삭제

역할은 `OWNER`, `ADMIN`, `MEMBER` 3단계로 구분합니다.
<div>
<img width="54%" height="auto" alt="메인" src="https://github.com/user-attachments/assets/b8b04f43-2724-4c13-bbdc-6e0a1498e7f6" />
<img width="36%" height="auto" alt="멤버관리" src="https://github.com/user-attachments/assets/d63d47b7-e1df-45df-a521-ea8b0f4f9bb5" />
</div>

- `OWNER`: 워크스페이스 최종 권한
- `ADMIN`: 멤버 관리 가능
- `MEMBER`: 일반 문서 작업 권한

관련 코드:
- [WorkspaceController.kt](/src/main/kotlin/com/example/synclog/workspace/controller/WorkspaceController.kt)
- [WorkspaceService.kt](/src/main/kotlin/com/example/synclog/workspace/service/WorkspaceService.kt)
- [WorkspaceDTO.kt](/src/main/kotlin/com/example/synclog/workspace/controller/WorkspaceDTO.kt)

### 3.3 문서 저장 및 메타데이터 관리

- 문서 생성
- 문서 제목 수정
- 문서 메타데이터 조회
- 문서 삭제
- 문서 스냅샷 저장


동시편집 기능은 yjs 지원가능한 별도 nodejs 서버로 구성했습니다.

문서 스냅샷 저장 시:

- plain text 저장
- binary snapshot 저장(node 서버 다운 시 복구 위함)
- 텍스트가 실제로 변경된 경우에만 임베딩 재생성

이 조건 분기를 통해 불필요한 외부 임베딩 호출을 줄이도록 설계했습니다.

<img width="100%" height="auto" alt="문서" src="https://github.com/user-attachments/assets/04a90e2c-3f67-4273-88e2-08c8d643a779" />


관련 코드:
- [DocumentController.kt](/src/main/kotlin/com/example/synclog/document/controller/DocumentController.kt)
- [DocumentService.kt](/src/main/kotlin/com/example/synclog/document/service/DocumentService.kt)

### 3.4 RAG 기반 질의응답

질문이 들어오면 다음 과정을 거칩니다.

1. 질문을 임베딩으로 변환
2. 같은 워크스페이스 내 문서들 중 유사 문서를 벡터 검색
3. 검색한 문맥을 프롬프트로 조합
4. 외부 Chat API에 요청
5. 출처 문서 ID를 파싱해 응답에 포함

<img width="100%" height="auto" alt="rag" src="https://github.com/user-attachments/assets/bebbaefd-9a6a-4e6b-b129-5cd405645849" />

관련 코드:
- [DocumentService.kt](/src/main/kotlin/com/example/synclog/document/service/DocumentService.kt)
- [DocumentContentRepository.kt](/src/main/kotlin/com/example/synclog/document/persistence/DocumentContentRepository.kt)

### 3.5 할 일(Task) 추출

문서 본문을 기반으로 AI 모델에 구조화된 JSON 응답을 요청하고, 파싱하여 할 일 목록으로 반환합니다.

- 자연어 문서를 구조화된 JSON 스키마로 변환
- 파싱 실패 시 빈 응답으로 fallback

<img width="100%" height="auto" alt="task" src="https://github.com/user-attachments/assets/5b9e4863-5570-44ea-a2d3-31d7779eb1d9" />

관련 코드:
- [DocumentService.kt](/src/main/kotlin/com/example/synclog/document/service/DocumentService.kt)

## 4. 도메인 모델

### 관계 구조

- `User`
  - 여러 `WorkspaceMember`를 가질 수 있음
- `Workspace`
  - 여러 멤버와 여러 문서를 가짐
- `WorkspaceMember`
  - `User`와 `Workspace`를 연결하는 조인 엔티티
  - 역할(Role) 포함
- `Document`
  - 하나의 `Workspace`에 속함
  - 하나의 `DocumentContent`와 1:1 관계
- `DocumentContent`
  - plain text
  - binary snapshot
  - embedding(vector 1024)

관련 코드:
- [Workspace.kt](/src/main/kotlin/com/example/synclog/workspace/persistence/Workspace.kt)
- [WorkspaceMember.kt](/src/main/kotlin/com/example/synclog/workspace/persistence/WorkspaceMember.kt)
- [Document.kt](/src/main/kotlin/com/example/synclog/document/persistence/Document.kt)
- [DocumentContent.kt](/src/main/kotlin/com/example/synclog/document/persistence/DocumentContent.kt)

## 5. 아키텍처
<img width="100%" height="auto" alt="sa" src="https://github.com/user-attachments/assets/e3d22aee-eb10-49ea-8747-aa8168b81412" />


### 계층 구조

- Controller
  - 요청/응답 DTO 처리
- Service
  - 비즈니스 로직
- Repository
  - JPA 및 native query
- Common
  - 보안, 예외 처리, AI client, config

### 외부 연동 구조

```text
Client
  -> Nginx
    -> /        -> Spring Boot API (Docker)
    -> /ws/     -> Yjs WebSocket Server (PM2)

Spring Boot API
  -> Security Filter (JWT)
  -> Service Layer
    -> PostgreSQL / pgvector
    -> HuggingFace API (embedding/chat)
  -> Actuator / Micrometer

Monitoring Stack (Docker Compose)
  -> Prometheus
  -> Grafana
  -> Tempo (실험적 tracing)
```

### AI 처리 흐름

```text
Document Snapshot Save
  -> plain text 저장
  -> text changed 확인
  -> embedding 생성
  -> document_content 저장

RAG Request
  -> query embedding 생성
  -> pgvector 유사 문서 검색
  -> context 조합
  -> chat completion 요청
  -> 답변 + 출처 문서 반환
```

### 운영 배포 구조

실제 운영 환경은 역할별로 프로세스를 분리해 구성했습니다.

- `Spring Boot API`
  - Docker 단독 컨테이너로 실행
- `Yjs WebSocket Server`
  - Node.js 서버를 PM2로 실행
- `Prometheus / Grafana / Tempo`
  - Docker Compose로 별도 관리
- `Nginx`
  - TLS 종료 및 경로 기반 reverse proxy

라우팅 구조:

- `https://api.synclog.shop/`
  - Spring Boot API로 프록시
- `https://api.synclog.shop/ws/`
  - Yjs WebSocket 서버로 프록시

이 구조를 통해 API 서버와 협업 편집 서버의 역할을 분리하고, 모니터링 스택도 애플리케이션 런타임과 분리해 운영하도록 구성했습니다.

## 6. 주요 구현 포인트

### 6.1 JWT 기반 Stateless 인증

- 세션을 사용하지 않고 JWT로 인증
- 필터에서 토큰 파싱 후 `SecurityContext`에 인증 정보 주입
- 인증 성공 시 `userId`를 컨트롤러에서 바로 활용 가능

### 6.2 pgvector 기반 문서 검색

문서 내용 임베딩을 `vector(1024)` 컬럼에 저장하고, native query로 벡터 거리 연산을 수행합니다.

핵심 구현:

- PostgreSQL + pgvector
- `embedding <=> queryVector` 기반 정렬
- 워크스페이스 단위 검색 범위 제한

이는 단순 텍스트 LIKE 검색이 아니라, 의미 기반 검색을 위한 구조입니다.

### 6.3 N+1 문제를 의식한 조회 최적화

워크스페이스-멤버-사용자 관계를 조회할 때는 N+1 문제가 발생하기 쉬워, 필요한 경우 `fetch join` 기반 쿼리를 사용하도록 구현했습니다.

예시:

- 사용자 기준 워크스페이스 목록 조회 시 `workspace`를 함께 fetch
- 워크스페이스 멤버 조회/권한 수정 시 `user`를 함께 fetch

관련 코드:
- [WorkspaceMemberRepository.kt](/src/main/kotlin/com/example/synclog/workspace/persistence/WorkspaceMemberRepository.kt)

핵심 쿼리:

- `findAllByUserIdWithWorkspace`
- `findAllByWorkspaceIdWithUser`
- `findByWorkspaceIdWithUser`

이를 통해 협업 도메인에서 자주 발생하는 연관 엔티티 조회 비용을 줄이도록 했습니다.

### 6.4 텍스트 변경 시에만 임베딩 재생성

문서 snapshot 저장 시 매 요청마다 임베딩을 다시 생성하지 않고, text가 실제로 달라졌을 때만 외부 embedding API를 호출합니다.

이 구현은 다음 두 가지 의미가 있습니다.

- 불필요한 외부 API 비용 감소
- 응답 지연 감소

### 6.5 협업 편집 서버 분리

실시간 협업 편집은 Spring Boot 내부에서 직접 처리하지 않고, 별도 Node.js + Yjs WebSocket 서버로 분리했습니다.

이 구조의 이유는 다음과 같습니다.

- 문서 편집 동기화와 일반 REST API 트래픽 분리
- WebSocket 연결 유지와 협업 상태 전파를 별도 런타임에서 처리
- 문서 저장/권한/API 로직과 실시간 편집 로직의 책임 분리

Yjs 서버는:

- `/ws/{docId}` 형태로 문서별 room 연결
- 메모리 상 `Y.Doc` 관리
- PostgreSQL의 `document_content.yjs_binary`에서 초기 상태 로드
- 동기화 업데이트와 awareness(커서 상태) 브로드캐스트 수행

즉 편집 세션의 실시간 상태는 Yjs 서버가 담당하고, 백엔드는 문서 저장/검색/AI 처리를 담당하는 구조입니다.

### 6.6 예외 응답 일원화

`DomainException` 계층으로 사용자/문서/워크스페이스 예외를 통합하고, `GlobalExceptionHandler`에서 일관된 JSON 응답을 반환합니다.

관련 코드:
- [DomainException.kt](/src/main/kotlin/com/example/synclog/common/exception/DomainException.kt)
- [GlobalExceptionHandler.kt](/src/main/kotlin/com/example/synclog/common/exception/GlobalExceptionHandler.kt)

## 7. 성능 분석 및 운영 관점 개선

이 프로젝트에서는 AI 기능이 포함된 API의 병목을 파악하기 위해 애플리케이션 로그와 메트릭 기반으로 성능을 분석했습니다.

### 7.1 문제 인식

체감상 `rag`, `tasks`, `snapshot` 계열 기능이 느리게 느껴졌고, 특히 외부 AI 연동이 포함된 요청은 응답시간 편차가 컸습니다.

### 7.2 계측 방식

핵심 서비스 메서드에 단계별 실행 시간 로그를 추가했습니다.

- `document.snapshot`
  - `totalMs`
  - `embedMs`
  - `persistMs`
- `document.rag`
  - `totalMs`
  - `embedMs`
  - `searchContextMs`
  - `aiCallMs`
  - `refLookupMs`
- `document.tasks`
  - `totalMs`
  - `aiExtractMs`
  - `parseMs`

이를 통해 “어느 함수가 느린가”가 아니라 “어느 단계가 느린가”를 확인할 수 있도록 했습니다.

### 7.3 관찰 결과

로그 분석 결과:

- `persistMs`는 매우 작음
- `parseMs`도 매우 작음
- 대부분의 지연시간이 `embedMs`, `aiCallMs`, `aiExtractMs`에 집중

즉 병목은 DB보다 외부 AI 호출에 가까웠습니다.

또한 부하 테스트 중 외부 provider 상태에 따라 504, timeout, 402(quota/결제 관련 추정) 등이 발생해, 외부 의존성의 변동성이 전체 API 응답시간에 직접 영향을 준다는 점을 확인했습니다.

### 7.4 모니터링 구성

- Spring Boot Actuator
- Micrometer + Prometheus
- Grafana 대시보드
- `http.server.requests` histogram 활성화
- p95 / p99 latency 확인

이를 통해

- request count
- 평균 latency
- p95 / p99
- 5xx 증가 추이

를 시각적으로 확인할 수 있도록 구성했습니다.

### 7.5 부하 테스트

`k6`를 사용해 `snapshot` API를 중심으로 부하 테스트를 진행했습니다.

테스트 포인트:

- `TEXT_CHANGED=false`
  - 외부 임베딩 호출이 없는 baseline
- `TEXT_CHANGED=true`
  - 외부 임베딩 호출이 포함된 실제 병목 시나리오

관찰 포인트:

- 평균 응답시간
- p95 / p99
- 최대 응답시간
- 실패율
- 로그 상 `embedMs` / `persistMs`

### 7.6 외부 AI 호출 보호 전략

외부 AI API가 느려질 경우 요청 스레드가 장시간 점유되고, 전체 시스템의 응답성이 저하되는 문제가 발생할 수 있어 다음 보호 전략을 적용했습니다.

- 외부 API timeout 설정
- 외부 API 최대 동시 요청 수 제한
- 일시적 실패에 대한 제한적 재시도 로직 도입

핵심 의도는 성공률을 무조건 높이는 것이 아니라, 외부 의존성 문제로 인해 서버 전체가 장시간 대기 상태로 끌려가지 않게 보호하는 것입니다.

## 8. 배포 구조

애플리케이션은 Docker 이미지로 빌드하고, GitHub Actions를 통해 EC2에 배포합니다.

배포 흐름:

1. GitHub Actions에서 Docker 이미지 빌드
2. Docker Hub push
3. EC2에서 이미지 pull
4. 컨테이너 재기동

관련 파일:
- [Dockerfile](/Dockerfile)
- [cicd.yml](/.github/workflows/cicd.yml)

## 9. API 요약

### Auth

- `POST /api/auth/signup`
- `POST /api/auth/login`

### Workspace

- `POST /api/workspace/create`
- `GET /api/workspace/get`
- `GET /api/workspace/{workspaceId}/members`
- `POST /api/workspace/{workspaceId}/invite`
- `PUT /api/workspace/{workspaceId}/update/role`
- `DELETE /api/workspace/{workspaceId}/delete`
- `DELETE /api/workspace/{workspaceId}/delete/{targetUserId}`

### Document

- `POST /api/document/create/{workspaceId}`
- `PUT /api/document/{documentId}/snapshot`
- `GET /api/document/{documentId}/metadata`
- `PATCH /api/document/{documentId}/update/title`
- `POST /api/document/{documentId}/rag`
- `GET /api/document/{documentId}/tasks`
- `DELETE /api/document/{documentId}/delete`

## 10. 실행 방법

### 환경 변수

예시:

```bash
DB_URL=...
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET_KEY=...
HF_API_KEY=...
HOST_PROD=...
HF_MAX_CONCURRENCY=3
HF_MAX_RETRIES=1
HF_RETRY_BACKOFF_MS=250
```

### 로컬 실행

```bash
./gradlew bootRun
```

### Swagger / Actuator

- Swagger UI: `/swagger-ui/index.html`
- Prometheus metrics: `/actuator/prometheus`

## 11. 프로젝트를 통해 고민한 점

### 11.1 외부 AI 의존성을 어디까지 동기적으로 처리할 것인가

AI 기능을 사용자 요청-응답 사이클 안에서 바로 처리하면 사용자 경험은 단순하지만, 외부 provider 지연이 그대로 서비스 응답시간에 전이됩니다.

이 프로젝트에서는 우선 동기 구조를 유지하되:

- timeout
- 최대 동시 요청 제한
- 단계별 지연 로그

를 통해 운영 리스크를 줄이는 방향을 선택했습니다.

### 11.2 평균 응답시간보다 최악 구간을 어떻게 관리할 것인가

외부 API가 포함된 서비스는 평균값만으로 상태를 설명하기 어렵습니다.  
실제 운영에서는 p95/p99, timeout, 실패율이 더 중요하다고 판단했고, 모니터링과 부하 테스트도 이 관점으로 진행했습니다.

### 11.3 비용과 성능 사이의 균형

AI 호출은 응답시간 문제뿐 아니라 비용 문제도 함께 있습니다.  
그래서 text 변경 시에만 임베딩을 다시 생성하는 방식으로 불필요한 호출을 줄이도록 설계했습니다.

### 11.4 협업 편집 데이터와 AI 백엔드의 경계

코드상 문서 본문은 `plainText`와 `yjsBinary`를 함께 저장하도록 설계되어 있습니다.

- `plainText`
  - 검색, 태스크 추출, RAG 문맥 생성에 사용
- `yjsBinary`
  - 협업 편집 상태 스냅샷 저장에 사용

즉 문서 편집 자체의 실시간 동기화 데이터와, AI 처리를 위한 텍스트 데이터를 분리해 다루는 구조를 의도했습니다.

관련 코드:
- [DocumentContent.kt](/src/main/kotlin/com/example/synclog/document/persistence/DocumentContent.kt)
- [DocumentService.kt](/src/main/kotlin/com/example/synclog/document/service/DocumentService.kt)

## 12. 한계와 향후 개선

- 외부 AI provider 상태에 따라 응답시간 변동성이 큼
- 현재는 동기 처리 기반이라 장기적으로는 비동기/큐 구조도 검토 가능
- fallback 응답, circuit breaker, 재시도 정책 세분화 필요
- 공통 요청 로깅 및 trace correlation 고도화 가능
- 중앙 로그 수집 스택(Loki/CloudWatch Logs 등) 도입 여지 있음
- Yjs 서버와 API 서버 간 권한/문서 접근 검증을 더 강하게 연결할 여지 있음
- Yjs 서버 운영 설정(PM2, Nginx, persistence)을 코드 저장소 수준에서 더 명시적으로 관리할 수 있음

## 13. 회고

이 프로젝트를 통해 단순히 API를 구현하는 것만으로는 충분하지 않다는 점을 배웠습니다.  
특히 AI 기능처럼 외부 의존성이 큰 시스템에서는,

- 어디서 시간이 걸리는지 계측하고
- 부하가 걸렸을 때 어떤 실패 양상이 나타나는지 관찰하고
- 장시간 대기를 줄이는 보호 전략을 두는 것

이 실제 서비스 품질에 큰 영향을 준다는 점을 확인했습니다.

또한 협업 편집 서버(Yjs)와 일반 API 서버(Spring Boot)를 분리하면서, 기능 구현뿐 아니라 런타임의 역할 분리와 운영 구조 설계도 중요하다는 점을 체감했습니다.
