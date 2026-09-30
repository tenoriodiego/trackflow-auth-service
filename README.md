# 🔐 TrackFlow Auth Service

Serviço de autenticação e gerenciamento de usuários do **TrackFlow**, uma aplicação distribuída desenvolvida com Java e Spring Boot.

O `trackflow-auth-service` é responsável pelo cadastro de usuários, gerenciamento de roles e, nas próximas etapas, autenticação e emissão de tokens JWT para os demais serviços do ecossistema TrackFlow.

> 🚧 **Status:** Em desenvolvimento

---

## 📌 Sobre o TrackFlow

O **TrackFlow** é um projeto de portfólio desenvolvido com o objetivo de demonstrar conceitos e práticas utilizados no desenvolvimento de aplicações backend Java modernas, incluindo:

* Arquitetura baseada em microsserviços
* APIs REST
* Spring Boot
* Spring Security
* JWT
* PostgreSQL
* Mensageria
* Testes automatizados
* Spring Batch
* Docker
* CI/CD
* Integração com serviços de cloud

O projeto será desenvolvido de forma incremental, com cada microsserviço possuindo responsabilidade e ciclo de desenvolvimento próprios.

---

# 🔐 TrackFlow Auth Service

O `trackflow-auth-service` é o microsserviço responsável pela autenticação e identidade dos usuários do TrackFlow.

### Responsabilidades

* Cadastro de usuários
* Validação dos dados de cadastro
* Criptografia de senhas
* Gerenciamento de roles
* Autenticação de usuários
* Emissão de JWT
* Validação de tokens
* Controle de acesso baseado em roles

> Algumas dessas funcionalidades ainda estão em desenvolvimento.

---

## 🛠️ Tecnologias utilizadas

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* Bean Validation
* Lombok

### Banco de dados

* PostgreSQL
* Flyway

### Segurança

* Spring Security
* BCrypt
* JWT — em implementação

### Testes

* JUnit 5
* Mockito
* Spring Boot Test

### Documentação

* Springdoc OpenAPI / Swagger — planejado

---

# 🏗️ Arquitetura

O projeto utiliza organização por **feature/domain**, evitando concentrar todas as classes em pacotes globais de Controller, Service e Repository.

```text
br.com.trackflow.auth
│
├── authentication
│   ├── controller
│   ├── dto
│   ├── security
│   └── service
│
├── role
│   ├── entity
│   └── repository
│
├── shared
│   ├── config
│   └── exception
│
├── user
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
└── TrackflowAuthServiceApplication
```

### Fluxo atual de cadastro

```text
Cliente
   │
   ▼
UserController
   │
   ▼
RegisterRequest
   │
   ▼
UserService
   │
   ├── Validação do e-mail
   │
   ├── Normalização do e-mail
   │
   ├── Busca ROLE_CUSTOMER
   │
   ├── Criptografia da senha
   │
   └── Persistência
          │
          ▼
   UserRepository
          │
          ▼
      PostgreSQL
```

---

# 👤 Usuários

O cadastro de usuários atualmente possui os seguintes campos:

| Campo       | Descrição                               |
| ----------- | --------------------------------------- |
| `id`        | Identificador do usuário                |
| `name`      | Nome do usuário                         |
| `email`     | E-mail único                            |
| `password`  | Senha armazenada de forma criptografada |
| `enabled`   | Indica se o usuário está habilitado     |
| `createdAt` | Data de criação                         |
| `updatedAt` | Data da última atualização              |

A senha nunca é retornada pela API.

---

# 🛡️ Roles

As roles disponíveis atualmente são:

```text
ROLE_ADMIN
ROLE_OPERATOR
ROLE_CUSTOMER
```

Novos usuários recebem automaticamente:

```text
ROLE_CUSTOMER
```

O gerenciamento de permissões baseado nessas roles será utilizado nas próximas etapas da implementação do Spring Security.

---

# 🌐 Endpoints

## Usuários

### Registrar usuário

```http
POST /api/users
```

Cria um novo usuário no sistema.

### Request

```json
{
  "name": "José Diego",
  "email": "diego@trackflow.com",
  "password": "12345678"
}
```

### Response

```http
HTTP/1.1 201 Created
```

```json
{
  "id": 1,
  "name": "José Diego",
  "email": "diego@trackflow.com",
  "enabled": true
}
```

---

# 🔎 Validações

Durante o cadastro:

* O nome é obrigatório.
* O nome possui limite de 100 caracteres.
* O e-mail é obrigatório.
* O e-mail deve possuir formato válido.
* O e-mail possui limite de 150 caracteres.
* O e-mail é normalizado para lowercase.
* O e-mail deve ser único.
* A senha é obrigatória.
* A senha deve possuir entre 8 e 100 caracteres.
* A senha é armazenada utilizando BCrypt.
* Novos usuários recebem `ROLE_CUSTOMER`.

### Exemplo de e-mail normalizado

```text
  DIEGO@TRACKFLOW.COM
```

é armazenado como:

```text
diego@trackflow.com
```

---

# ⚠️ Tratamento de erros

A aplicação possui um `GlobalExceptionHandler` para tratar erros da API.

### E-mail já cadastrado

```http
HTTP/1.1 409 Conflict
```

Exemplo:

```json
{
  "timestamp": "2026-09-29T21:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Já existe um usuário cadastrado com o e-mail: diego@trackflow.com"
}
```

### Dados inválidos

```http
HTTP/1.1 400 Bad Request
```

Exemplo:

```json
{
  "timestamp": "2026-09-29T21:00:00",
  "status": 400,
  "error": "Validation error",
  "messages": {
    "email": "E-mail inválido",
    "password": "Senha deve ter entre 8 e 100 caracteres"
  }
}
```

---

# 🗄️ Banco de dados

O projeto utiliza **PostgreSQL**.

As alterações do banco são controladas pelo **Flyway**, evitando que a estrutura seja criada ou alterada automaticamente pelo Hibernate.

Atualmente existem as seguintes tabelas:

```text
users
roles
user_roles
```

### Relacionamento

```text
User
  │
  │ N:N
  ▼
Role
```

A tabela intermediária:

```text
user_roles
```

é responsável pelo relacionamento entre usuários e roles.

---

# 🗃️ Migrations

As migrations estão localizadas em:

```text
src/main/resources/db/migration
```

Migration atual:

```text
V1__create_users_and_roles.sql
```

O Hibernate está configurado para validar a estrutura existente:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

A criação e evolução do schema são responsabilidades do Flyway.

---

# ⚙️ Configuração

As configurações sensíveis são mantidas em um arquivo `.env` local.

Exemplo:

```properties
DB_URL=jdbc:postgresql://localhost:5432/trackflow_auth
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_DRIVER=org.postgresql.Driver

SERVER_PORT=8081

JWT_SECRET=your-jwt-secret
JWT_EXPIRATION=3600
JWT_REFRESH_TOKEN_TIME=604800
```

> ⚠️ O arquivo `.env` não deve ser versionado no Git.

Para disponibilizar um exemplo de configuração sem expor credenciais, será utilizado futuramente:

```text
.env.example
```

---

# 🚀 Como executar

## 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
```

Entre no diretório:

```bash
cd trackflow-auth-service
```

---

## 2. Criar o banco PostgreSQL

Crie um banco chamado:

```text
trackflow_auth
```

Exemplo:

```sql
CREATE DATABASE trackflow_auth;
```

---

## 3. Configurar o `.env`

Crie:

```text
src/main/resources/.env
```

Configure as credenciais do PostgreSQL e as demais propriedades necessárias.

---

## 4. Executar a aplicação

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

A aplicação será iniciada, por padrão, na porta:

```text
8081
```

---

# 🧪 Testes

O projeto utiliza **JUnit 5 + Mockito** para testes automatizados.

Para executar todos os testes:

```bash
mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

Para executar uma classe específica:

```bash
mvnw.cmd -Dtest=UserServiceTest test
```

### Cobertura atual

O `UserService` possui testes para:

* Cadastro de usuário
* E-mail duplicado
* Role `ROLE_CUSTOMER` inexistente
* Criptografia da senha
* Normalização do e-mail
* Atribuição da role padrão

---

# 🔒 Segurança

Atualmente o projeto utiliza:

* Spring Security
* BCrypt para armazenamento seguro das senhas
* Configuração stateless
* Validação de requisições
* Controle de acesso preparado para roles

A autenticação baseada em **JWT** será implementada nas próximas etapas.

---

# 🗺️ Roadmap

O desenvolvimento do `trackflow-auth-service` seguirá aproximadamente esta ordem:

```text
[x] Estrutura inicial do projeto
[x] Entidades User e Role
[x] Repositories
[x] DTOs de cadastro
[x] Cadastro de usuários
[x] BCrypt
[x] PostgreSQL
[x] Flyway
[x] Tratamento de exceções
[x] Testes do UserService
[ ] Testes do UserController
[ ] Login
[ ] AuthenticationService
[ ] JWT
[ ] JwtService
[ ] JwtAuthenticationFilter
[ ] Refresh Token
[ ] Autorização baseada em roles
[ ] Swagger / OpenAPI
[ ] Testes de integração
[ ] Docker
[ ] CI/CD
```

---

# 🧩 Ecossistema TrackFlow

O `trackflow-auth-service` faz parte de uma arquitetura maior baseada em microsserviços.

```text
                    ┌──────────────────────┐
                    │    API Gateway       │
                    └──────────┬───────────┘
                               │
             ┌─────────────────┼─────────────────┐
             │                 │                 │
             ▼                 ▼                 ▼
      ┌─────────────┐   ┌─────────────┐   ┌─────────────┐
      │ Auth Service│   │Order Service│   │  Tracking   │
      │             │   │             │   │   Service   │
      └─────────────┘   └─────────────┘   └─────────────┘
             │                 │                 │
             ▼                 ▼                 ▼
        PostgreSQL        PostgreSQL        PostgreSQL
```

Outros serviços serão adicionados gradualmente ao projeto.

---

# 📚 Próximas etapas do TrackFlow

Após finalizar o Auth Service, o desenvolvimento seguirá para os demais microsserviços:

```text
1. Auth Service
2. Order Service
3. Tracking Service
4. RabbitMQ / Mensageria
5. Notification Service
6. Batch Service
7. AWS
8. Docker
9. CI/CD
```

A arquitetura será evoluída gradualmente conforme novas necessidades forem adicionadas ao sistema.

---

# 👨‍💻 Autor

**José Diego Tenório**

Desenvolvedor Backend Java

* GitHub: [github.com/tenoriodiego](https://github.com/tenoriodiego)
* LinkedIn: [linkedin.com/in/tjdiegoss](https://linkedin.com/in/tjdiegoss)

---

# 📄 Licença

Este projeto é desenvolvido para fins de estudo, portfólio e demonstração de conhecimentos em desenvolvimento backend Java.
