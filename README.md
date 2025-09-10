# Spring Batch & Stored Procedure 기반 대용량 정산 시스템

## 📋 프로젝트 개요

### 비즈니스 목표

온라인 플랫폼에서 발생하는 대규모 금융 거래를 정확하고 신속하게 정산하여, **안정적인 금융 서비스를 제공**하고 **데이터의 무결성을 보장**하는 것을 목표로 합니다. 이 시스템은 매일 수십만 건 이상의 입출금 거래를 처리하고, 복잡한 비즈니스 규칙(수수료, 보너스 등)을 적용하여 최종적으로 고객의 잔고를 업데이트합니다.

### 기술적 목표

- **대용량 데이터 처리:** Spring Batch를 사용하여 대규모 데이터를 안정적이고 효율적으로 처리합니다.
- **성능 최적화:** 복잡하고 성능이 중요한 정산 로직은 MySQL Stored Procedure로 구현하여 데이터베이스의 처리 성능을 극대화합니다.
- **안정적인 운영:** Scheduler를 통한 정산 자동화, Docker를 활용한 개발 환경 표준화, API 문서화를 통해 안정적인 시스템 운영 및 유지보수를 지향합니다.

## ✨ API 주요 기능 (Swagger UI)

_(이곳에 Swagger UI 스크린샷을 추가해 주세요.)_

- **계좌(Account) API:** 계좌 생성, 조회, 상태 변경 등
- **거래(Transaction) API:** 입출금 거래 생성, 기간별/계좌별 조회 등
- **정산(Settlement) API:** 수동 정산 실행, 정산 이력 조회 등

## 🛠️ 기술 스택

- **Language:** Java 21
- **Framework:** Spring Boot 3.5.5, Spring Batch
- **Database:** MySQL 8.0
- **ORM:** JPA / Hibernate
- **DevOps:** Docker Compose, Gradle, Flyway
- **API:** Spring REST, SpringDoc (Swagger UI)

## 🚀 프로젝트 실행 방법

### 사전 요구사항

- Java 21
- Docker Desktop

### 실행 순서

1.  **프로젝트 클론**

    ```bash
    git clone https://github.com/your-username/spring_batch-stored_procedure.git
    cd spring_batch-stored_procedure
    ```

2.  **데이터베이스 및 애플리케이션 실행**
    Docker Compose를 사용하여 MySQL, phpMyAdmin, 그리고 Spring Boot 애플리케이션을 한 번에 실행합니다.

    ```bash
    docker-compose up --build
    ```

    > ` -d` 옵션을 추가하면 백그라운드에서 실행할 수 있습니다.

3.  **애플리케이션 접속 정보**
    - **API 서버:** `http://localhost:8080`
    - **Swagger UI (API 문서):** `http://localhost:8080/swagger-ui/index.html` (또는 `/swagger`, `/docs` 등)
    - **phpMyAdmin (DB 관리):** `http://localhost:8081`
      - **서버:** `mysql`
      - **사용자명:** `sa`
      - **비밀번호:** `1234`
