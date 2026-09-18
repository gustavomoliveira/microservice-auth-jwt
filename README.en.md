# microservices-auth-jwt

[🇧🇷 Português](README.md) | 🇺🇸 English

Example system in Java/Spring Boot demonstrating **JWT**-based authentication and authorization in a microservices architecture, with **Eureka** (service discovery), **Config Server** (centralized configuration), and an **API Gateway** (single entry point).

Academic project — built for a Microservices and DevOps course.

## Solution architecture

The system is composed of 5 independent microservices:

| Service | Port | Responsibility |
|---|---|---|
| `discovery-server` | 8761 | Eureka server — registration and discovery for the other services |
| `config-server` | 8888 | Centralized configuration (*native* profile, serving local files) |
| `auth-service` | 8081 | User seeding, login, and JWT issuance/refresh |
| `produto-service` | 8082 | Product CRUD, with routes protected by JWT |
| `api-gateway` | 8080 | Single entry point, routing to `auth-service` and `produto-service` |

### Authentication flow

```
Client → api-gateway (8080)
             │
             ├── /auth/**          → auth-service (8081)   [public route]
             └── /api/produtos/**  → produto-service (8082) [protected route]
```

1. The client logs in via `POST /auth/login`, receiving a short-lived `accessToken` and a longer-lived `refreshToken`.
2. The client uses the `accessToken` in the `Authorization: Bearer <token>` header to access protected routes.
3. `produto-service` validates the token **locally**, through a filter (`JwtAuthFilter`), using a shared secret key — no network call to `auth-service` on every request.
4. When the `accessToken` expires, the client calls `POST /auth/refresh` with the `refreshToken` to obtain a new `accessToken` without logging in again.

The JWT signing secret (`jwt.secret`) is centralized in the `config-server` and distributed to both `auth-service` and `produto-service`, so both sign/validate with the same secret.

Each service with persistence (`auth-service`, `produto-service`) has its **own in-memory H2 database**, independent from one another.

## Authentication technology

**JWT (JSON Web Token)**, implemented with the [`jjwt`](https://github.com/jwtk/jjwt) library (version 0.12.6), HS256 signature.

- `auth-service` implements the full Spring Security authentication flow (`UserDetails`, `UserDetailsService`, `PasswordEncoder`, `AuthenticationManager`), responsible for issuing tokens.
- `produto-service` does not perform login — it only validates already-issued tokens, through a custom filter (`JwtAuthFilter`) plugged into the `SecurityFilterChain`.
- Passwords are stored using **BCrypt** hashing, never in plain text.
- The JWT payload contains only `sub` (username), `iat`, and `exp` — no sensitive information.

## How to run the services

Prerequisites: Java 21, Maven.

Start the services **in this order**, waiting for each to fully start before the next:

1. `discovery-server` — confirm at `http://localhost:8761`
2. `config-server` — confirm at `http://localhost:8888/actuator/health`
3. `auth-service`
4. `produto-service`
5. `api-gateway`

Each service can be run via IDE (running the `*Application.java` main class) or via Maven:

```bash
./mvnw spring-boot:run
```

## Test users

`auth-service` automatically seeds two users on startup (`DataSeeder`), with the password already BCrypt-hashed:

| Username | Password |
|---|---|
| `gustavo` | `senha123` |
| `teste` | `teste123` |

## How to authenticate

**Login:**

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "gustavo",
    "password": "senha123"
}
```

Response (`200 OK`):

```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Invalid credentials return `401 Unauthorized`.

## How to use the refresh endpoint

```
POST http://localhost:8080/auth/refresh?refreshToken=<refresh_token_from_login>
```

Response (`200 OK`): a new `accessToken`, along with the same `refreshToken`.

## Public endpoints

| Method | Route | Description |
|---|---|---|
| `POST` | `/auth/login` | Authentication — returns `accessToken` and `refreshToken` |
| `POST` | `/auth/refresh` | Renews the `accessToken` from a valid `refreshToken` |

## Protected endpoints

Require the `Authorization: Bearer <accessToken>` header.

| Method | Route | Description |
|---|---|---|
| `GET` | `/api/produtos` | Lists all products |
| `POST` | `/api/produtos` | Creates a new product |

Requests without a token, or with an invalid/expired token, return `403 Forbidden`.

## Sample requests for testing

**Create a product (with token):**

```
POST http://localhost:8080/api/produtos
Authorization: Bearer <accessToken>
Content-Type: application/json

{
    "nome": "Notebook",
    "preco": 3500.00,
    "quantidade": 10
}
```

Response (`201 Created`):

```json
{
    "id": 1,
    "nome": "Notebook",
    "preco": 3500.00,
    "quantidade": 10
}
```

**List products (with token):**

```
GET http://localhost:8080/api/produtos
Authorization: Bearer <accessToken>
```

**Access without a token (expected: 403 Forbidden):**

```
GET http://localhost:8080/api/produtos
```

## Repository structure

```
microservices-auth-jwt/
├── discovery-server/
├── config-server/
├── auth-service/
├── produto-service/
├── api-gateway/
└── README.md
```

## Development workflow

The project is versioned following **Gitflow** (`main`, `develop`, `feature/*` branches) and **Conventional Commits** (`feat:`, `fix:`, `chore:`, etc.).
