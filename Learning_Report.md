# Applying Advanced Architectural Patterns in a Microservices-Based System

Modern microservices architectures provide scalability and flexibility but introduce challenges in data consistency and fault tolerance. Advanced patterns such as Saga for distributed transactions and Circuit Breaker for preventing cascading failures help address these challenges. In this project, these patterns are implemented using Spring Boot and RabbitMQ to ensure resilience and maintainability.


## 1. Saga and Circuit Breaker Patterns

### Why These Patterns Were Needed

In a microservice-based system, services are independent and maintain their own databases. As a result, traditional ACID transactions across services are not possible. Business workflows such as *creating an order and processing a payment* span multiple services and introduce risks related to data consistency and system stability.

To address these challenges, two architectural patterns were applied:

* **Saga (Choreography)** to manage distributed transactions
* **Circuit Breaker** to prevent cascading failures

---

### Saga Pattern – Choreography

The project uses the **Saga Choreography** pattern to coordinate multi-step business processes without a central orchestrator.

Each service:

* Performs its local transaction
* Publishes an event after completion
* Reacts to events published by other services

Example flow:

```
1. Booking Service publishes "BookingRequested"
2. Resource Service consumes the event and updates resource status
3. Resource Service publishes "ResourceStatusChanged"
4. Payment Service processes payment and publishes result
```

If any step fails, the responsible service emits a **compensating event** to undo previous actions.

**Why Saga Choreography?**

* Preserves service autonomy
* Avoids tight coupling
* Fits naturally with event-driven messaging

---

### Circuit Breaker Pattern

Some services depend on others to complete their tasks. For example, the **Exam Service** sends exam results to the **Notification Service** for email delivery.

If the Notification Service becomes slow or unavailable, synchronous calls can cause request accumulation and lead to cascading failures.

To prevent this, the **Circuit Breaker** pattern was applied.

#### Circuit Breaker States

**Closed**

* Requests flow normally
* Failures are monitored

**Open**

* Triggered when failure rate exceeds a threshold
* Requests are blocked or redirected to fallback logic

**Half-Open**

* A limited number of test requests are allowed
* Success closes the circuit, failure reopens it

This mechanism ensures system stability and graceful degradation.

---

## 2. Use of Spring and Spring Boot

### What Is Spring and Why It Was Used

Spring is a Java-based backend framework designed to build modular, loosely coupled, and testable applications. It provides infrastructure-level support for building enterprise systems without forcing strong dependencies between components.

Spring Boot was selected as the main framework because the project is based on a **microservice architecture**, where each service must be developed, deployed, and scaled independently.

### Why Spring Boot

Spring Boot simplifies microservice development by providing:

* Auto-configuration
* Embedded web server
* Opinionated defaults for production-ready services

This allowed the team to focus on architectural concerns rather than boilerplate configuration.

### How Spring Boot Was Used in the Project

Each microservice was implemented as an independent **Spring Boot application**. The framework was used to support:

* REST APIs for query operations (CQRS)
* Event-driven communication using RabbitMQ
* Local database transactions via Spring Data JPA
* Implementation of Saga and Circuit Breaker patterns
* API Gateway routing, authentication, and authorization via Spring Cloud Gateway

Spring Boot enabled consistent structure across services while maintaining loose coupling and future scalability.

---

## 3. RabbitMQ in Resource Service

### Why RabbitMQ Is Needed

The project follows an **event-driven architecture** combined with **Saga Choreography**. Services must not communicate through direct synchronous calls. Instead, they exchange events asynchronously.

RabbitMQ is used as the message broker to enable this communication.

---

### Publishing Events in Resource Service

The Resource Service publishes events whenever important state changes occur:

```java
// When a resource is added
eventPublisher.publishResourceAdded(resource);

// When resource status changes (AVAILABLE → BOOKED)
eventPublisher.publishResourceStatusChanged(resource, oldStatus, newStatus);
```

**Why publish events?** Other services rely on this information:

* Booking Service tracks availability
* Notification Service informs users
* Analytics Service (future) analyzes usage patterns

---

### Saga Choreography Using RabbitMQ

RabbitMQ enables Saga coordination without a central controller.

Example flow:

```
1. Booking Service publishes "BookingRequested"
2. Resource Service consumes the event
3. Resource Service updates status to BOOKED
4. Resource Service publishes "ResourceStatusChanged"
5. Booking Service confirms the reservation
```

Each service reacts only to events it is interested in.

---

### Receiving Events from Other Services

The Resource Service also listens to events published by other services:

```java
@RabbitListener(queues = "resource.status.update.queue")
public void handleBookingEvent(BookingEvent event) {
    // Update resource status after booking
}
```

This approach allows the service to make decisions solely based on incoming events.

---

### Decoupling Services

**Without RabbitMQ (Direct HTTP Calls):**

* Strong coupling
* High failure propagation
* Difficult to extend

**With RabbitMQ:**

```java
rabbitTemplate.convertAndSend("resource.events", "resource.added", event);
```

* Producers do not know consumers
* Services can be added or removed independently
* Improved resilience and scalability

---

### Real-World Example from the Project

**Scenario: Adding a new computer lab**

```
1. Request passes through API Gateway
2. Resource Service saves data to database
3. "ResourceAdded" event is published
4. Booking Service updates availability
5. Notification Service sends alerts
```

If one service is unavailable, others continue to operate normally.

---

## Summary

RabbitMQ, Spring Boot, Saga, and Circuit Breaker patterns work together to support:

* Event-driven communication
* Loose coupling between services
* Distributed transaction management
* Fault tolerance and resilience
* Long-term scalability and maintainability

These choices form the foundation of a robust and production-ready microservice architecture.
