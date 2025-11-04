● # NoMoreGeoBuk

목표 추적 시스템을 위한 Spring Boot 백엔드 애플리케이션입니다. 사용자가 목표와 하위 목표를 설정하고 일정에 따라 완료 여부를 추적할 수 있습니다.

## 📋 목차

- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
    - [필수 요구사항](#필수-요구사항)
    - [환경 설정](#환경-설정)
    - [데이터베이스 설정](#데이터베이스-설정)
- [실행 방법](#실행-방법)
    - [개발 환경 (H2 Database)](#개발-환경-h2-database)
    - [프로덕션 환경 (PostgreSQL)](#프로덕션-환경-postgresql)
- [테스트](#테스트)
- [API 문서](#api-문서)
- [데이터베이스 접근](#데이터베이스-접근)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)

## 🛠 기술 스택

- **Java 21**
- **Spring Boot 3.5.6**
    - Spring Web
    - Spring Data JPA
    - Spring Security
- **OAuth2 Client** (Google, Naver, Kakao)
- **JWT 기반 인증** (Refresh Token 포함)
- **데이터베이스**
    - 개발: H2 Database
    - 프로덕션: PostgreSQL
- **Lombok** - 보일러플레이트 코드 감소
- **Swagger/OpenAPI** - API 문서화
- **Gradle (Kotlin DSL)** - 빌드 도구

## 🚀 시작하기

### 필수 요구사항

- Java 21 이상
- Docker & Docker Compose (프로덕션 환경 시)
- intelliJ IDEA 또는 선호하는 IDE
- .env 파일 프로젝트에 넣기 (디스코드 참고)

### 빠른 시작
``` bash
 docker-compose up -d

 ./gradlew build
 
 ./gradlew bootRun --args='--spring.profiles.active=prod'
 
```
### oauth 링크
http://localhost:8080/login/oauth2/code/google

http://localhost:8080/login/oauth2/code/naver

http://localhost:8080/login/oauth2/code/kakao

각 링크를 통해 소셜로그인 페이지로 이동가능

-> 현재 성공시 success 페이지로 이동 / 실패시 fail 페이지로 이동

### 빠른 종료
``` bash
    docker-compose down
    
    ./gradlew --stop

```


### 환경 설정

1. 프로젝트 클론
  ```bash
  git clone [repository-url]
  cd NoMoreGeoBuk

  2. 환경 변수 설정
  디스코드의 env 파일 참고

  .env 파일에 다음 항목을 설정해야 합니다:
  - JWT_SECRET - JWT 토큰 암호화 키
  - custom.jwt.secretKey - 추가 JWT 시크릿 키
  - custom.accessToken.expirationSeconds - 액세스 토큰 만료 시간 (기본: 7200초 = 2시간)
  - custom.site.cookieDomain - 쿠키 도메인
  - custom.dev.frontUrl - 프론트엔드 URL
  - custom.dev.backUrl - 백엔드 URL
  - OAuth2 클라이언트 ID 및 시크릿 (Google, Naver, Kakao)

  데이터베이스 설정

  프로덕션 환경 - PostgreSQL (Docker)

  PostgreSQL 컨테이너를 실행합니다:
  # PostgreSQL 데이터베이스 시작
  docker-compose up -d

  # 로그 확인
  docker-compose logs -f postgres

  # 데이터베이스 중지
  docker-compose down

  💻 실행 방법

  개발 환경 (H2 Database)

  개발 환경에서는 H2 인메모리 데이터베이스를 사용합니다.

  # 기본 실행 (dev 프로필 자동 적용)
  ./gradlew bootRun

  # 또는 명시적으로 dev 프로필 지정
  ./gradlew bootRun --args='--spring.profiles.active=dev'

  개발 환경 특징:
  - H2 인메모리 데이터베이스 사용
  - 데이터베이스 스키마 자동 생성 (ddl-auto: create-drop)
  - Swagger UI 활성화
  - 애플리케이션 재시작 시 데이터 초기화
  - H2 Console: http://localhost:8080/h2-console

  프로덕션 환경 (PostgreSQL)

  프로덕션 환경에서는 Docker로 실행되는 PostgreSQL을 사용합니다.

  # 1. PostgreSQL 데이터베이스 시작 (Docker)
  docker-compose up -d

  # 2. 프로덕션 프로필로 애플리케이션 실행
  ./gradlew bootRun --args='--spring.profiles.active=prod'

  프로덕션 환경 특징:
  - PostgreSQL 데이터베이스 사용 (Docker)
  - 데이터베이스 스키마 검증 모드 (ddl-auto: validate)
  - 영구 데이터 저장
  - init.sql 스키마가 데이터베이스와 일치해야 함

  빌드

  # 프로젝트 빌드
  ./gradlew build

  # 클린 빌드
  ./gradlew clean build

  🧪 테스트

  # 모든 테스트 실행
  ./gradlew test

  # 특정 테스트 클래스 실행
  ./gradlew test --tests "com.back.domain.user.service.UserServiceTest"

  # 상세 출력과 함께 테스트 실행
  ./gradlew test --info

  📚 API 문서

  애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

  - Swagger UI: http://localhost:8080/swagger-ui.html
  - OpenAPI Docs: http://localhost:8080/api-docs

  💾 데이터베이스 접근

  H2 Console (개발 환경)

  - URL: http://localhost:8080/h2-console
  - JDBC URL: jdbc:h2:./db_dev;MODE=MySQL
  - Username: sa
  - Password: (비어있음)

  PostgreSQL (프로덕션 환경)

  - Host: localhost
  - Port: 15432
  - Database: capstone_db
  - Username: admin
  - Password: admin

  📁 프로젝트 구조

  src/main/java/com/back/
  ├── BackApplication.java           # 애플리케이션 진입점
  ├── domain/                        # 도메인 계층 (비즈니스 로직)
  │   ├── user/                      # 사용자 도메인
  │   │   ├── controller/           # REST 엔드포인트
  │   │   ├── service/              # 비즈니스 로직
  │   │   ├── repository/           # 데이터 접근
  │   │   ├── entity/               # JPA 엔티티
  │   │   └── dto/                  # 데이터 전송 객체
  │   └── profile/                   # 프로필 도메인 (구현 예정)
  └── global/                        # 글로벌 인프라 계층
      ├── security/                  # OAuth2 + JWT 보안 설정
      ├── jwt/                       # JWT 유틸리티 및 리프레시 토큰 관리
      ├── exception/                 # 커스텀 예외
      ├── globalExceptionHandler/    # 글로벌 예외 처리
      ├── appConfig/                 # 애플리케이션 및 Swagger 설정
      ├── rq/                        # 요청 컨텍스트 유틸리티
      ├── rsData/                    # 표준 응답 래퍼
      └── standard/util/             # 유틸리티 클래스

  ✨ 주요 기능

  인증 플로우

  1. OAuth2 로그인 - Google, Naver, Kakao를 통한 소셜 로그인
  2. JWT 토큰 발급 - 액세스 토큰 (2시간) + 리프레시 토큰 (7일)
  3. HTTP-Only 쿠키 - 토큰을 안전하게 쿠키에 저장
  4. 토큰 검증 - 각 요청마다 JWT 검증
  5. 토큰 갱신 - 리프레시 토큰을 통한 액세스 토큰 갱신

  보안 설정

  - 세션 정책: IF_REQUIRED (OAuth 플로우에만 세션 사용, API는 stateless JWT)
  - CSRF: 비활성화 (JWT in cookies 사용)
  - CORS: 환경 변수로 프론트엔드/백엔드 URL 설정
  - 현재 상태: 테스트를 위해 permitAll() 설정 (프로덕션에서는 authenticated()로 변경 필요)

  데이터베이스 스키마

  init.sql 파일에 정의된 전체 스키마:
  - users ↔ profiles (1:1)
  - users → goals (1:N)
  - goals → goals_sub (1:N)
  - goals_sub → sub_goal_completions (1:N)
  - goals ↔ summary (1:1)
  - goals → goal_schedule_days (1:N)

  🔧 개발 노트

  새로운 도메인 기능 추가하기

  1. domain/ 하위에 도메인 패키지 생성 (예: domain/goal)
  2. 계층 구조 따르기: entity → repository → service → controller → dto
  3. REST 엔드포인트는 @RestController 사용, 일관된 응답을 위해 RsData<T> 반환
  4. API 문서화를 위해 Swagger 어노테이션 추가 (@Operation, @Tag)

  OAuth2 설정

  OAuth2 인증 정보는 각 플랫폼의 개발자 콘솔에서 등록해야 합니다:
  - Callback URL: http://localhost:8080/login/oauth2/code/{provider}
  - provider: google, naver, kakao

  보안 테스트

  - 현재 설정은 모든 요청을 허용합니다 (SecurityConfig.java:67 의 permitAll())
  - 프로덕션 배포 전에 .anyRequest().authenticated()로 변경하고 permitAll 라인 제거
  - 배포 전 OAuth 플로우 전체 테스트 필요
