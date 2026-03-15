# Coles Orders Service

A small REST backend implemented in Java + Spring Boot with an in-memory H2 database. It implements a simple order lifecycle (PENDING → PROCESSING → COMPLETED / FAILED) and exposes endpoints to create, retrieve, list and process orders.

---

## Technology stack

- Java 21
- Spring Boot (Web MVC, Data JPA)
- H2 (in-memory database) for development and tests
- Maven (wrapper `./mvnw`) for build and dependency management
- JUnit + Mockito + AssertJ for unit testing

All dependencies are declared in `pom.xml` and the project uses Spring Boot's opinionated defaults.

---

## Architecture overview

This project follows a simple layered architecture (typical for small Spring Boot services):

- Controller layer (`controller` package)
  - Exposes HTTP endpoints, performs request-level validation and delegates to the service layer.
  - Example: `OrderController` (base path `/api/orders`).

- Service layer (`service` package)
  - Contains business rules and orchestrates persistence operations.
  - Responsible for lifecycle transitions and validation of state transitions (for example, only `PENDING` orders can be updated or processed).
  - Example: `OrderServiceImpl`.

- Persistence / Repository layer (`repo` package)
  - JPA repositories that interact with the H2 database.
  - Example: `OrderRepository extends JpaRepository<Order, UUID>`.

- Domain model (`domain` package)
  - `Order` entity and `OrderStatus` enum.

- API DTOs (`api` package)
  - `OrderRequest` and `OrderResponse` are used to decouple internal entity representation from external APIs.

- Error handling
  - `GlobalExceptionHandler` converts domain/service exceptions to appropriate HTTP responses (404, 409, 400, 500).

This layered separation keeps concerns isolated and makes unit testing straightforward.

---

## Important design notes

- Order lifecycle enforcement is implemented in the service layer. The controller performs request-level validation (non-blank customer name, non-zero amount). The service enforces state transition rules.
- DTOs are used to avoid exposing internal entity internals directly to clients.

---

## Run / development

Prerequisites

- Java 21 (or a compatible JDK) installed
- Maven (you can use the included `./mvnw` wrapper)

Build and run the app locally:

```bash
./mvnw package
./mvnw spring-boot:run
```

Or run the packaged jar:

```bash
./mvnw package
java -jar target/coles-0.0.1-SNAPSHOT.jar
```

---

## API (summary + examples)

Base path: `/api/orders`

1. Create order

- POST /api/orders
- Body: { "customerName": "Alice", "amount": 12.5 }
- Successful response: 201 Created with body containing the order (id, status, timestamps)

Example curl:

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerName":"Alice","amount":12.5}'
```

2. List orders

- GET /api/orders

```bash
curl http://localhost:8080/api/orders
```

3. Get order

- GET /api/orders/{id}

```bash
curl http://localhost:8080/api/orders/<uuid>
```

4. Process order

- POST /api/orders/{id}/process

```bash
curl -X POST http://localhost:8080/api/orders/<uuid>/process 
```

Error responses return JSON with an `error` or `errors` field (see `GlobalExceptionHandler`). Validation failures for controller-level checks return 400 Bad Request with a short reason message.

---

## Tests

Unit tests are in `src/test/java`. Key test coverage:

- `OrderServiceImplTest` covers service behaviors and lifecycle rules (create, update, process success/failure, delete cases).
- `OrderControllerTest` checks controller-level validation and that valid requests are delegated to the service.

Run tests with:

```bash
./mvnw test
```

---

## Where to look in the code

- `src/main/java/com/example/coles/controller` — REST controllers and exception handler
- `src/main/java/com/example/coles/service` — business logic and lifecycle rules
- `src/main/java/com/example/coles/repo` — JPA repositories
- `src/main/java/com/example/coles/domain` — entity models and enums
- `src/main/java/com/example/coles/api` — request/response DTOs

---

## Docker: build and run

Build the Docker image locally:

```bash
# from the project root
docker build -t coles-app:latest .
```

Run the image as a container (exposes port 8080):

```bash
docker run --rm -p 8080:8080 --name coles-app coles-app:latest
```

Notes:
- The `Dockerfile` performs a Maven build with `-DskipTests` to speed up image builds. Run the test suite locally before building images if you want to catch test failures earlier:
