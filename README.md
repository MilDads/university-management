# University-Management Architecture

A loosely coupled, event-driven microservices architecture implementing Saga pattern for distributed transactions.

## Architecture Overview

### Multi-Tenancy
- **Database per Service**: Each microservice owns its dedicated PostgreSQL instance for true data isolation

### Communication Patterns
- **API Gateway**: Single entry point (Spring Cloud Gateway)
- **Synchronous**: HTTP/REST for queries only (CQRS - Query side)
- **Asynchronous**: RabbitMQ for all commands and inter-service communication
- **Caching**: Redis for sessions, JWT blacklist, and rate limiting

### Authentication & Security
- JWT authentication via Auth Service
- JWT validation at API Gateway (per request)
- RBAC enforcement per operation
- Audit logging for sensitive operations

### Failure Handling
- Circuit Breakers (Resilience4j)
- Retry logic with exponential backoff
- Dead letter queues for failed messages

---

## Service Inventory

| Service | Port | Framework | Database | Description |
|---------|------|-----------|----------|-------------|
| API Gateway | 8080 | Java 25 / Spring Cloud Gateway | - | Entry point, routing, JWT validation, rate limiting |
| Auth Service | 8081 | Java 25 / Spring Boot | PostgreSQL (5432) | User authentication, JWT generation |
| User Service | 8082 | Java 25 / Spring Boot | PostgreSQL (5433) | User profiles, RBAC |
| Resource Service | 8083 | Java 25 / Spring Boot | PostgreSQL (5434) | Resource catalog, availability |
| Booking Service | 8084 | Java 25 / Spring Boot | PostgreSQL (5435) | Reservations, overbooking prevention |
| Marketplace Service | 8085 | Java 25 / Spring Boot | PostgreSQL (5436) | Products, orders, **Saga Orchestrator** |
| Payment Service | 8086 | Java 25 / Spring Boot | PostgreSQL (5437) | Payment processing, Saga participant |
| Exam Service | 8087 | Java 25 / Spring Boot | PostgreSQL (5438) | Exams, submissions, Circuit Breaker |
| Notification Service | 8088 | Java 25 / Spring Boot | PostgreSQL (5439) | Email, Observer pattern |
| IoT Service | 8089 | Java 25 / Spring Boot | TimescaleDB (5441) | Sensor data, time-series analytics |
| Tracking Service | 8090 | Java 25 / Spring Boot | PostgreSQL (5440) | Shuttle GPS tracking |

### Infrastructure Services

| Service | Port(s) | Description |
|---------|---------|-------------|
| RabbitMQ | 5672, 15672 | Message broker, Saga orchestration, event-driven messaging |
| Redis | 6379 | Caching, session storage, rate limiting |

---

## architecture diagram

```mermaid
---
config:
  theme: dark
---
flowchart TB
 subgraph CoreServices["Core Microservices"]
        AuthService["🔐 Auth Service<br>Port 8081<br><br>JWT authentication<br>User management"]
        UserService["👤 User Service<br>Port 8082<br><br>User profiles<br>RBAC management"]
        ResourceService["📚 Resource Service<br>Port 8083<br><br>Resource catalog<br>Availability check"]
        BookingService["📅 Booking Service<br>Port 8084<br><br>Reservations<br>Overbooking prevention"]
  end
 subgraph BusinessServices["Business Microservices"]
        MarketplaceService["🛒 Marketplace Service<br>Port 8085<br><br>Products & Orders<br>SAGA ORCHESTRATOR"]
        PaymentService["💰 Payment Service<br>Port 8086<br><br>Payment processing<br>Saga participant"]
        ExamService["📝 Exam Service<br>Port 8087<br><br>Exams & Submissions<br>CIRCUIT BREAKER"]
  end
 subgraph SupportServices["Support Microservices"]
        NotificationService["📬 Notification Service<br>Port 8088<br><br>Email<br>Observer Pattern"]
        IoTService["🌡️ IoT Service<br>Port 8089<br><br>Sensor data processing<br>Time-series analytics"]
        TrackingService["🚌 Tracking Service<br>Port 8090<br><br>Shuttle GPS tracking<br>Real-time location"]
  end
 subgraph DataStores["Data Storage Layer - Database per Service"]
        AuthDB["🗄️ Auth DB<br>PostgreSQL:5432"]
        UserDB["🗄️ User DB<br>PostgreSQL:5433"]
        ResourceDB["🗄️ Resource DB<br>PostgreSQL:5434"]
        BookingDB["🗄️ Booking DB<br>PostgreSQL:5435"]
        MarketplaceDB["🗄️ Marketplace DB<br>PostgreSQL:5436"]
        PaymentDB["🗄️ Payment DB<br>PostgreSQL:5437"]
        ExamDB["🗄️ Exam DB<br>PostgreSQL:5438"]
        NotificationDB["🗄️ Notification DB<br>PostgreSQL:5439"]
        TrackingDB["🗄️ Tracking DB<br>PostgreSQL:5440"]
        TimescaleDB["⏱️ TimescaleDB:5441<br>IoT sensor data"]
        Redis["⚡ Redis Cache<br>Port 6379"]
  end

    WebApp["🌐 Web App<br>student/instructor"] -- HTTPS/REST --> APIGateway["🚪 API Gateway<br>Spring Cloud Gateway<br>Port 8080"]
    
    APIGateway -- "HTTP/REST<br>(Queries)" --> AuthService
    APIGateway -- "HTTP/REST<br>(Queries)" --> ResourceService
    APIGateway -- "HTTP/REST<br>(Queries)" --> TrackingService
    APIGateway -- "Publish Commands" --> MessageBroker
    
    AuthService -- JDBC --> AuthDB
    UserService -- JDBC --> UserDB
    ResourceService -- JDBC --> ResourceDB
    BookingService -- JDBC --> BookingDB
    MarketplaceService -- JDBC --> MarketplaceDB
    PaymentService -- JDBC --> PaymentDB
    ExamService -- JDBC --> ExamDB
    NotificationService -- JDBC --> NotificationDB
    TrackingService -- JDBC --> TrackingDB
    IoTService -- JDBC --> TimescaleDB

    MessageBroker["🐰 RabbitMQ<br>Ports 5672, 15672<br><br>Event-driven messaging<br>Saga orchestration"]
    
    AuthService <-- AMQP --> MessageBroker
    UserService <-- AMQP --> MessageBroker
    ResourceService <-- AMQP --> MessageBroker
    BookingService <-- AMQP --> MessageBroker
    MarketplaceService <-- "AMQP<br>Saga Events" --> MessageBroker
    PaymentService <-- "AMQP<br>Saga Events" --> MessageBroker
    ExamService <-- AMQP --> MessageBroker
    NotificationService -- "AMQP<br>Consume" --> MessageBroker
    IoTService <-- AMQP --> MessageBroker
    TrackingService <-- AMQP --> MessageBroker

    AuthService -- Cache tokens --> Redis
    BookingService -- Cache availability --> Redis
    APIGateway -- Rate limiting --> Redis

    style MarketplaceService fill:#2a9d8f,stroke:#1a6d5f,stroke-width:2px,color:#ffffff
    style ExamService fill:#e76f51,stroke:#b74c2f,stroke-width:2px,color:#ffffff
    style Redis fill:#dc143c,stroke:#a00000,stroke-width:3px,color:#ffffff
    style APIGateway fill:#1168bd,stroke:#0b4884,stroke-width:3px,color:#ffffff
    style MessageBroker fill:#ff6b6b,stroke:#cc5555,stroke-width:3px,color:#ffffff
```
## Level 3 C4 diagram
```mermaid
  ---
config:
  theme: dark
---
flowchart TB
    Gateway["🚪 API Gateway"]
    MQ["🐰 RabbitMQ"]
    DB[("🗄️ Marketplace DB<br/>PostgreSQL")]
    PaymentSvc["💰 Payment Service"]

    subgraph Marketplace["Marketplace Service"]
        
        Controller["📡 REST Controller<br/><br/>Handles HTTP requests<br/>Product & Order endpoints"]
        
        ProductMgmt["📦 Product Component<br/><br/>Product catalog<br/>Inventory management"]
        
        OrderMgmt["🛒 Order Component<br/><br/>Order lifecycle<br/>Validation"]
        
        SagaOrch["⚙️ Saga Orchestrator<br/><br/>Coordinates distributed<br/>transactions across services"]
        
        EventPub["📤 Event Publisher<br/><br/>Publishes domain events<br/>to message broker"]
        
        EventSub["📥 Event Subscriber<br/><br/>Handles incoming events<br/>Saga step responses"]
        
        Repo["💾 Repository Layer<br/><br/>Data access<br/>JPA/Hibernate"]
    end

    Gateway -->|"REST"| Controller
    Controller --> ProductMgmt
    Controller --> OrderMgmt
    OrderMgmt --> SagaOrch
    SagaOrch --> EventPub
    EventPub -->|"Publish"| MQ
    MQ -->|"Subscribe"| EventSub
    EventSub --> SagaOrch
    ProductMgmt --> Repo
    OrderMgmt --> Repo
    Repo -->|"JDBC"| DB
    MQ <-->|"Saga events"| PaymentSvc

    style SagaOrch fill:#2a9d8f,stroke:#1a6d5f,stroke-width:2px,color:#fff
    style Gateway fill:#1168bd,stroke:#0b4884,stroke-width:2px,color:#fff
    style MQ fill:#ff6b6b,stroke:#cc5555,stroke-width:2px,color:#fff
    style Controller fill:#438dd5,stroke:#2e6295,stroke-width:2px,color:#fff
    style PaymentSvc fill:#438dd5,stroke:#2e6295,stroke-width:2px,color:#fff
```
## Level 2 C4 diagram
```mermaid
  ---
config:
  theme: dark
---
flowchart TB
    User["👤 User<br/>(Student/Instructor/Admin)"]

    subgraph boundary["University Management System"]
        
        WebApp["🌐 Web Application<br/><br/>Single Page Application<br/>User interface"]
        
        Gateway["🚪 API Gateway<br/><br/>Spring Cloud Gateway<br/>Routing, auth validation,<br/>rate limiting"]
        
        subgraph Services["Microservices"]
            Auth["🔐 Auth Service<br/><br/>JWT authentication<br/>User credentials"]
            UserSvc["👤 User Service<br/><br/>Profiles & RBAC"]
            Resource["📚 Resource Service<br/><br/>Resource catalog"]
            Booking["📅 Booking Service<br/><br/>Reservations"]
            Marketplace["🛒 Marketplace<br/><br/>Products & Orders<br/>Saga Orchestrator"]
            Payment["💰 Payment Service<br/><br/>Payment processing"]
            Exam["📝 Exam Service<br/><br/>Exams & grading"]
            Notification["📬 Notification Service<br/><br/>Email"]
            IoT["🌡️ IoT Service<br/><br/>Sensor analytics"]
            Tracking["🚌 Tracking Service<br/><br/>Shuttle GPS"]
        end

        MQ["🐰 Message Broker<br/><br/>RabbitMQ<br/>Async messaging & Saga"]
        
        Cache["⚡ Cache<br/><br/>Redis<br/>Sessions & rate limits"]
        
        subgraph Databases["Data Stores"]
            DB["🗄️ PostgreSQL<br/><br/>Service databases<br/>(one per service)"]
            TSDB["⏱️ TimescaleDB<br/><br/>IoT time-series data"]
        end
    end

    ExtEmail["📧 Email Provider"]
    ExtPay["💳 Payment Provider"]
    Sensors["🌡️ IoT Sensors"]

    User -->|"HTTPS"| WebApp
    WebApp -->|"HTTPS/REST"| Gateway
    
    Gateway -->|"REST queries"| Services
    Gateway -->|"Commands"| MQ
    Gateway -->|"Rate limit"| Cache
    
    Services <-->|"AMQP"| MQ
    Services -->|"JDBC"| Databases
    Auth -->|"Token cache"| Cache
    Booking -->|"Availability cache"| Cache
    
    Notification -->|"SMTP"| ExtEmail
    Payment -->|"API"| ExtPay
    Sensors -->|"MQTT/HTTP"| IoT

    style Gateway fill:#1168bd,stroke:#0b4884,stroke-width:2px,color:#fff
    style MQ fill:#ff6b6b,stroke:#cc5555,stroke-width:2px,color:#fff
    style Cache fill:#dc143c,stroke:#a00000,stroke-width:2px,color:#fff
    style Marketplace fill:#2a9d8f,stroke:#1a6d5f,stroke-width:2px,color:#fff
    style WebApp fill:#438dd5,stroke:#2e6295,stroke-width:2px,color:#fff
    style User fill:#08427b,stroke:#052e56,stroke-width:2px,color:#fff
    style ExtEmail fill:#999999,stroke:#666,color:#fff
    style ExtPay fill:#999999,stroke:#666,color:#fff
    style Sensors fill:#999999,stroke:#666,color:#fff
```
## Level 1 C4 diagram
```mermaid
  ---
config:
  theme: dark
---
flowchart TB
    subgraph boundary [University Management System Boundary]
        System["📦 University Management System<br/><br/>Manages resources, bookings,<br/>marketplace, exams, and<br/>campus operations"]
    end

    Student["👨‍🎓 Student<br/><br/>Books resources, takes exams,<br/>purchases from marketplace,<br/>tracks shuttles"]
    
    Instructor["👩‍🏫 Instructor<br/><br/>Manages resources, creates exams,<br/>views analytics"]
    
    Admin["👤 Administrator<br/><br/>Manages users, system config,<br/>views reports"]

    EmailSystem["📧 Email System<br/><br/>External email provider<br/>for notifications"]
    
    PaymentProvider["💳 Payment Provider<br/><br/>External payment processing"]

    IoTSensors["🌡️ IoT Sensors<br/><br/>Campus environmental sensors<br/>and shuttle GPS devices"]

    Student -->|"Uses"| System
    Instructor -->|"Uses"| System
    Admin -->|"Administers"| System
    
    System -->|"Sends emails via"| EmailSystem
    System -->|"Processes payments via"| PaymentProvider
    IoTSensors -->|"Sends telemetry to"| System

    style System fill:#1168bd,stroke:#0b4884,stroke-width:3px,color:#fff
    style Student fill:#08427b,stroke:#052e56,stroke-width:2px,color:#fff
    style Instructor fill:#08427b,stroke:#052e56,stroke-width:2px,color:#fff
    style Admin fill:#08427b,stroke:#052e56,stroke-width:2px,color:#fff
    style EmailSystem fill:#999999,stroke:#666666,stroke-width:2px,color:#fff
    style PaymentProvider fill:#999999,stroke:#666666,stroke-width:2px,color:#fff
    style IoTSensors fill:#999999,stroke:#666666,stroke-width:2px,color:#fff
```

---

## Design Patterns

| Pattern | Implementation | Service(s) |
|---------|---------------|------------|
| **Saga** | Choreography via RabbitMQ | Marketplace, Payment, Booking |
| **CQRS** | Queries via REST, Commands via MQ | All services |
| **Circuit Breaker** | Resilience4j | Exam → Notification |
| **Database per Service** | Isolated PostgreSQL instances | All services |
| **Observer** | Event-driven notifications | Notification Service |
| **Strategy** | Payment method selection | Payment Service |


