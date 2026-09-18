# microservices-auth-jwt

🇧🇷 Português | [🇺🇸 English](README.en.md)

Sistema de exemplo em Java/Spring Boot demonstrando autenticação e autorização via **JWT** em uma arquitetura de microsserviços, com **Eureka** (service discovery), **Config Server** (configuração centralizada) e **API Gateway** (ponto único de entrada).

Projeto acadêmico — desenvolvido para a disciplina de Microsserviços e DevOps.

## Arquitetura da solução

O sistema é composto por 5 microsserviços independentes:

| Serviço | Porta | Responsabilidade |
|---|---|---|
| `discovery-server` | 8761 | Servidor Eureka — registro e descoberta dos demais serviços |
| `config-server` | 8888 | Configuração centralizada (modo *native*, servindo arquivos locais) |
| `auth-service` | 8081 | Cadastro de usuários (via seed), login e emissão/renovação de JWT |
| `produto-service` | 8082 | CRUD de produtos, com rotas protegidas por JWT |
| `api-gateway` | 8080 | Ponto único de entrada, roteando para `auth-service` e `produto-service` |

### Fluxo de autenticação

```
Cliente → api-gateway (8080)
             │
             ├── /auth/**          → auth-service (8081)   [rota pública]
             └── /api/produtos/**  → produto-service (8082) [rota protegida]
```

1. O cliente faz login em `POST /auth/login`, recebendo um `accessToken` (curta duração) e um `refreshToken` (longa duração).
2. O cliente usa o `accessToken` no header `Authorization: Bearer <token>` para acessar rotas protegidas.
3. O `produto-service` valida o token **localmente**, através de um filtro (`JwtAuthFilter`), usando uma chave secreta compartilhada — sem chamada de rede ao `auth-service` a cada requisição.
4. Quando o `accessToken` expira, o cliente usa o `refreshToken` em `POST /auth/refresh` para obter um novo `accessToken`, sem precisar logar novamente.

A chave secreta de assinatura do JWT (`jwt.secret`) é centralizada no `config-server` e distribuída para `auth-service` e `produto-service`, garantindo que ambos assinem/validem com o mesmo segredo.

Cada serviço com persistência (`auth-service`, `produto-service`) possui seu **próprio banco H2 em memória**, independente entre si.

## Tecnologia de autenticação

**JWT (JSON Web Token)**, implementado com a biblioteca [`jjwt`](https://github.com/jwtk/jjwt) (versão 0.12.6), assinatura HS256.

- O `auth-service` implementa o fluxo completo de autenticação do Spring Security (`UserDetails`, `UserDetailsService`, `PasswordEncoder`, `AuthenticationManager`), responsável por gerar os tokens.
- O `produto-service` não realiza login — apenas valida tokens já emitidos, através de um filtro customizado (`JwtAuthFilter`) plugado na `SecurityFilterChain`.
- Senhas são armazenadas com hash **BCrypt**, nunca em texto puro.
- O payload do JWT contém apenas `sub` (username), `iat` e `exp` — nenhuma informação sensível.

## Como executar os serviços

Pré-requisitos: Java 21, Maven.

Suba os serviços **nesta ordem**, aguardando cada um subir completamente antes do próximo:

1. `discovery-server` — confirme em `http://localhost:8761`
2. `config-server` — confirme em `http://localhost:8888/actuator/health`
3. `auth-service`
4. `produto-service`
5. `api-gateway`

Cada serviço pode ser executado via IDE (rodando a classe principal `*Application.java`) ou via Maven:

```bash
./mvnw spring-boot:run
```

## Usuários de teste

O `auth-service` popula automaticamente dois usuários na inicialização (`DataSeeder`), com senha já criptografada em BCrypt:

| Username | Senha |
|---|---|
| `gustavo` | `senha123` |
| `teste` | `teste123` |

## Como realizar autenticação

**Login:**

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
    "username": "gustavo",
    "password": "senha123"
}
```

Resposta (`200 OK`):

```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Credenciais inválidas retornam `401 Unauthorized`.

## Como utilizar o endpoint de refresh

```
POST http://localhost:8080/auth/refresh?refreshToken=<refresh_token_recebido_no_login>
```

Resposta (`200 OK`): novo `accessToken`, com o mesmo `refreshToken`.

## Endpoints públicos

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/login` | Autenticação — retorna `accessToken` e `refreshToken` |
| `POST` | `/auth/refresh` | Renova o `accessToken` a partir de um `refreshToken` válido |

## Endpoints protegidos

Exigem o header `Authorization: Bearer <accessToken>`.

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/api/produtos` | Lista todos os produtos |
| `POST` | `/api/produtos` | Cria um novo produto |

Requisições sem token, ou com token inválido/expirado, retornam `403 Forbidden`.

## Exemplos de requisição para teste

**Criar produto (com token):**

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

Resposta (`201 Created`):

```json
{
    "id": 1,
    "nome": "Notebook",
    "preco": 3500.00,
    "quantidade": 10
}
```

**Listar produtos (com token):**

```
GET http://localhost:8080/api/produtos
Authorization: Bearer <accessToken>
```

**Acesso sem token (esperado: 403 Forbidden):**

```
GET http://localhost:8080/api/produtos
```

## Estrutura do repositório

```
microservices-auth-jwt/
├── discovery-server/
├── config-server/
├── auth-service/
├── produto-service/
├── api-gateway/
└── README.md
```

## Fluxo de desenvolvimento

Projeto versionado seguindo **Gitflow** (branches `main`, `develop`, `feature/*`) e **Conventional Commits** (`feat:`, `fix:`, `chore:`, etc.).
