# University-Management Architecture

## Multi-Tenancy
-  PostgreSQL: Schema-per-tenant (database level isolation)

## Communications:
-  API gateway (single entry point)
-  synchronous: API gateway --> gRPC (internal)
-  ssynchronous: RabbitMQ
-  caching: Redis

## Authentication and Security
- JWT authentication --> auth-service
- JWT validation --> API gateway (per request)
-  RBAC check per operation
-  logging for sensitive operations

## Handling Failure
-  circuit breakers
-  retry logic

## Service Inventory

| Service | Port | Language/Framework | Database | Description |
|---------|------|-------------------|----------|-------------|
| API Gateway | 8080 | java/Spring Cloud Gateway | - | Entry point, routing, JWT validation |
| Auth Service | 8081 | java/Spring Boot | PostgreSQL | User authentication, JWT generation |
| User Service | 8082 | java/Spring Boot | PostgreSQL | User profiles, RBAC |
| Resource Service | 8083 | java/Spring Boot | PostgreSQL | Resource catalog, availability |
| Booking Service | 8084 | java/Spring Boot | PostgreSQL | Reservations, overbooking prevention |
| Marketplace Service | 8085 | java/Spring Boot | PostgreSQL | Products, orders, Saga orchestration |
| Exam Service | 8087 | java/Spring Boot | PostgreSQL | Exams, submissions, Circuit Breaker |
| Notification Service | 8088 | java/Spring Boot | PostgreSQL | Email/SMS, Observer pattern |
| IoT Service | 8089 | java/Spring Boot | - | Sensor data processing |
| Tracking Service | 8090 | java/Spring Boot | PostgreSQL | Shuttle GPS tracking |

-------------------------------------------------------------------------------------
## Level 2 C4 diagram
```mermaid
---
config:
  theme: dark
---
flowchart TB
 subgraph CoreServices["Core Microservices"]
        AuthService["🔐 Auth Service<br>Port 8081<br><br>JWT authentication<br>User management<br>[FR-01, FR-02]"]
        UserService["👤 User Service<br>Port 8082<br><br>User profiles<br>RBAC management"]
        ResourceService["📚 Resource Service<br>Port 8083<br><br>Resource catalog<br>Availability check<br>[FR-03]"]
        BookingService["📅 Booking Service<br>Port 8084<br><br>Reservations<br>Overbooking prevention<br>[FR-04, NFR-R02]"]
  end
 subgraph BusinessServices["Business Microservices"]
        MarketplaceService["🛒 Marketplace Service<br>Port 8085<br><br>Products &amp; Orders<br>SAGA PATTERN<br>[FR-05, FR-06]"]
        PaymentService["💰 Payment Service<br>Port 8086<br><br>Payment processing<br>Saga participant<br>Strategy Pattern"]
        ExamService["📝 Exam Service<br>Port 8087<br><br>Exams &amp; Submissions<br>CIRCUIT BREAKER<br>[FR-07, FR-08]"]
  end
 subgraph SupportServices["Support Microservices"]
        NotificationService["📬 Notification Service<br>Port 8088<br><br>Email &amp; SMS<br>Circuit Breaker target<br>Observer Pattern"]
        IoTService["🌡️ IoT Service<br>Port 8089<br><br>Sensor data processing<br>Time-series analytics<br>[FR-09]"]
        TrackingService["🚌 Tracking Service<br>Port 8090<br><br>Shuttle GPS tracking<br>Real-time location<br>[FR-10]"]
  end
 subgraph DataStores["Data Storage Layer"]
        PostgreSQL["🗄️ PostgreSQL<br>Port 5432<br><br>Relational database<br>Schema-per-tenant<br>[NFR-MT01]"]
        TimescaleDB["⏱️ TimescaleDB<br>Port 5432<br><br>Time-series database<br>IoT sensor data"]
        Redis["⚡ Redis Cache<br>Port 6379<br><br>• Session storage<br>• JWT blacklist<br>• Response cache<br>• Rate limiting"]
  end
    WebApp["🌐 Web App<br>----<br>student/instructor"] -- HTTPS/REST<br>JSON --> APIGateway["🚪 API Gateway<br>Spring Cloud Gateway<br>Port 8080<br><br>• Request routing<br>• JWT validation<br>• Rate limiting<br>• Load balancing"]
    APIGateway -- gRPC --> AuthService & UserService & ResourceService & BookingService & MarketplaceService & ExamService & IoTService & TrackingService
    AuthService -- JDBC --> PostgreSQL
    UserService -- JDBC --> PostgreSQL
    ResourceService -- JDBC --> PostgreSQL
    BookingService -- JDBC --> PostgreSQL
    MarketplaceService -- JDBC --> PostgreSQL
    PaymentService -- JDBC --> PostgreSQL
    ExamService -- JDBC --> PostgreSQL
    NotificationService -- JDBC --> PostgreSQL
    TrackingService -- JDBC --> PostgreSQL
    IoTService -- JDBC --> TimescaleDB
    MarketplaceService <-- AMQP<br>Events --> MessageBroker["🐰 RabbitMQ<br>Ports 5672, 15672<br><br>• Event-driven messaging<br>• Saga orchestration<br>• Pub/Sub pattern<br>• Work queues"]
    PaymentService <-- AMQP<br>Events --> MessageBroker
    BookingService <-- AMQP<br>Events --> MessageBroker
    ExamService <-- AMQP<br>Events --> MessageBroker
    NotificationService -- AMQP<br>Consume --> MessageBroker
    AuthService -- Cache tokens --> Redis
    BookingService -- Cache availability --> Redis
    APIGateway -- Rate limiting --> Redis
    ExamService -. HTTP/REST<br>Circuit Breaker .-> NotificationService
    style MarketplaceService fill:#2a9d8f,stroke:#1a6d5f,stroke-width:2px,color:#ffffff
    style ExamService fill:#e76f51,stroke:#b74c2f,stroke-width:2px,color:#ffffff
    style PostgreSQL fill:#336791,stroke:#1a3a52,stroke-width:2px,color:#ffffff
    style TimescaleDB fill:#fdb515,stroke:#c48a00,stroke-width:2px,color:#000000
    style Redis fill:#dc143c,stroke:#a00000,stroke-width:3px,color:#ffffff
    style APIGateway fill:#1168bd,stroke:#0b4884,stroke-width:3px,color:#ffffff
    style MessageBroker fill:#ff6b6b,stroke:#cc5555,stroke-width:3px,color:#ffffff


````








