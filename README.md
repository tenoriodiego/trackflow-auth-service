# 🔐 TrackFlow Auth Service

Microsserviço de autenticação e gerenciamento de usuários do **TrackFlow**, projeto de portfólio desenvolvido com Java e Spring Boot.

O serviço oferece cadastro de usuários, autenticação, emissão e renovação de tokens JWT e consulta dos dados do usuário autenticado.

> **Status:** em desenvolvimento. A implementação atual inclui cadastro, autenticação JWT, refresh tokens, documentação OpenAPI e testes automatizados. Consulte os endpoints e instruções abaixo para executar a versão atual.

## 📌 Sobre o TrackFlow

O TrackFlow é um projeto de portfólio voltado à demonstração prática de desenvolvimento backend Java, com evolução incremental para uma arquitetura baseada em microsserviços.

Tecnologias e conceitos previstos no ecossistema incluem APIs REST, Spring Boot, Spring Security, PostgreSQL, Flyway, testes automatizados, mensageria, processamento em lote, cloud e CI/CD. Nem todos esses recursos fazem parte deste microsserviço neste momento.

## 🛠️ Tecnologias

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- Bean Validation
- PostgreSQL
- Flyway
- BCrypt para hash de senhas
- JWT com JJWT
- Springdoc OpenAPI / Swagger UI
- JUnit 5, Mockito e Spring Boot Test
- Testcontainers para testes com PostgreSQL, quando habilitado na configuração de testes
- Maven

## 🏗️ Organização do código

O projeto organiza o código por funcionalidade/domínio, separando autenticação, usuários, roles e componentes compartilhados.

```text
br.com.trackflow.auth
├── authentication
│   ├── controller
│   ├── dto
│   ├── security
│   └── service
├── role
│   ├── entity
│   └── repository
├── shared
│   ├── config
│   └── exception
└── user
    ├── controller
    ├── dto
    ├── entity
    ├── repository
    └── service
```

A estrutura acima resume os principais pacotes; classes adicionais podem existir conforme a implementação evolui.

## 🔐 Funcionalidades

- Cadastro de usuários com validação de entrada
- Normalização do e-mail para letras minúsculas
- Verificação de e-mail duplicado
- Hash de senha com BCrypt
- Atribuição da role padrão `ROLE_CUSTOMER`
- Login com emissão de access token JWT e refresh token
- Renovação de tokens por refresh token
- Endpoint protegido para consultar o usuário autenticado
- Proteção de endpoints com Spring Security e filtro JWT
- Tratamento centralizado de exceções
- Documentação OpenAPI / Swagger UI

## 👤 Usuários e roles

O cadastro inclui nome, e-mail e senha. A senha não deve ser retornada nas respostas da API.

Roles previstas no serviço:

- `ROLE_ADMIN`
- `ROLE_OPERATOR`
- `ROLE_CUSTOMER`

Novos usuários recebem `ROLE_CUSTOMER` por padrão.

## 🌐 Endpoints

| Método | Endpoint | Descrição | Acesso |
|---|---|---|---|
| `POST` | `/api/users` | Cadastrar usuário | Público |
| `POST` | `/api/auth/login` | Autenticar e obter tokens | Público |
| `POST` | `/api/auth/refresh` | Renovar tokens | Público |
| `GET` | `/api/auth/me` | Consultar usuário autenticado | JWT obrigatório |

### Cadastrar usuário

`POST /api/users`

Exemplo de requisição:

```json
{
  "name": "José Diego",
  "email": "diego@trackflow.com",
  "password": "SenhaForte123"
}
```

Resposta esperada: `201 Created`.

```json
{
  "id": 1,
  "name": "José Diego",
  "email": "diego@trackflow.com",
  "enabled": true
}
```

Os valores acima são ilustrativos; o identificador e os dados retornados dependem da execução.

### Login

`POST /api/auth/login`

```json
{
  "email": "diego@trackflow.com",
  "password": "SenhaForte123"
}
```

O login retorna um access token, um refresh token e o tipo de token. Use o access token nas rotas protegidas:

```http
Authorization: Bearer <access_token>
```

### Renovar tokens

`POST /api/auth/refresh`

```json
{
  "refreshToken": "<refresh_token>"
}
```

Envie um refresh token válido para solicitar um novo conjunto de tokens. Tokens inválidos ou expirados devem ser rejeitados.

### Consultar usuário autenticado

`GET /api/auth/me`

Requer o cabeçalho:

```http
Authorization: Bearer <access_token>
```

Retorna os dados do usuário associado ao token, incluindo suas roles.

## ⚠️ Validações e erros

O cadastro valida campos obrigatórios, formato do e-mail e limites definidos no DTO de registro. O e-mail é normalizado para lowercase e deve ser único. A senha deve atender às regras de tamanho e é armazenada usando BCrypt.

Principais códigos HTTP utilizados:

- `201 Created`: usuário cadastrado.
- `200 OK`: operação de autenticação bem-sucedida.
- `400 Bad Request`: dados de entrada inválidos.
- `401 Unauthorized`: credenciais inválidas, token ausente ou token inválido/expirado em endpoint protegido.
- `409 Conflict`: e-mail já cadastrado.

As respostas específicas dependem do tipo de erro tratado pela aplicação.

## 🗄️ Banco de dados e migrations

O serviço utiliza PostgreSQL e Flyway para controlar a evolução do schema. O Hibernate deve validar o schema existente, em vez de criá-lo automaticamente.

As migrations ficam em:

```text
src/main/resources/db/migration
```

Antes de executar a aplicação, crie um banco PostgreSQL para o serviço, por exemplo `trackflow_auth`, e configure o acesso no arquivo de ambiente.

## ⚙️ Configuração local

Use Java 21 e o Maven Wrapper incluído no repositório. Configure as variáveis de ambiente necessárias para o banco de dados e para os tokens. Exemplo ilustrativo:

```properties
DB_URL=jdbc:postgresql://localhost:5432/trackflow_auth
DB_USERNAME=postgres
DB_PASSWORD=altere_esta_senha
DB_DRIVER=org.postgresql.Driver
SERVER_PORT=8081
JWT_SECRET=substitua_por_um_segredo_forte
JWT_EXPIRATION=3600
JWT_REFRESH_TOKEN_TIME=604800
```

> **Segurança:** não versione o arquivo `.env` nem segredos reais. Use um segredo JWT forte e apropriado ao algoritmo configurado. Em ambientes compartilhados ou de produção, injete segredos por variáveis de ambiente ou pelo gerenciador de segredos da plataforma.

Confirme no `application.properties`/configuração da aplicação o local em que o projeto lê o arquivo `.env` e o valor de porta efetivamente configurado. O exemplo acima considera a configuração documentada do projeto.

## 🚀 Como executar

### 1. Clonar o repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd trackflow-auth-service
```

Substitua `<URL_DO_REPOSITORIO>` pela URL real do repositório.

### 2. Criar o banco

No PostgreSQL, crie o banco configurado para o serviço, por exemplo:

```sql
CREATE DATABASE trackflow_auth;
```

### 3. Configurar o ambiente

Crie o arquivo `.env` no local esperado pela configuração da aplicação e informe as variáveis necessárias. Não envie esse arquivo ao Git.

### 4. Executar a aplicação

Windows:

```bat
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

A porta deve corresponder à variável `SERVER_PORT` ou à configuração efetiva da aplicação.

## 📚 Swagger / OpenAPI

Com a aplicação em execução, verifique:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

Se a porta configurada for diferente de `8081`, ajuste a URL. A autenticação de endpoints protegidos pode ser testada no Swagger usando o esquema Bearer JWT, após obter um access token pelo login.

## 🧪 Testes

Executar todos os testes:

Windows:

```bat
mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

Executar uma classe específica:

```bat
mvnw.cmd -Dtest=UserServiceTest test
```

Mais detalhes sobre estratégia, classes e cenários estão em [`TESTING.md`](TESTING.md).

Testes que utilizam Testcontainers podem exigir Docker disponível e em execução, conforme a configuração usada pela suíte.

## 🗺️ Roadmap

- [x] Cadastro de usuários
- [x] Persistência com PostgreSQL e migrations Flyway
- [x] Hash de senha com BCrypt
- [x] Tratamento centralizado de exceções
- [x] Autenticação e tokens JWT
- [x] Refresh token
- [x] Endpoint de usuário autenticado
- [x] Testes automatizados das camadas principais
- [x] Documentação OpenAPI / Swagger
- [ ] Revisão final da documentação e configuração para execução limpa
- [ ] Avaliar testes de repositório e integração adicionais conforme necessidade
- [ ] Configurar análise de cobertura com JaCoCo
- [ ] Preparar deploy
- [ ] Docker e CI/CD em etapa posterior

## 🧩 Ecossistema TrackFlow

O Auth Service será um dos serviços do ecossistema TrackFlow. Outros microsserviços, mensageria, processamento em lote e integrações cloud serão adicionados gradualmente, em repositórios ou módulos próprios conforme a arquitetura evoluir.

## 👨‍💻 Autor

**José Diego Tenório** — Desenvolvedor Backend Java

- GitHub: [tenoriodiego](https://github.com/tenoriodiego)
- LinkedIn: [tjdiegoss](https://linkedin.com/in/tjdiegoss)

## 📄 Licença

Projeto desenvolvido para estudo, portfólio e demonstração de conhecimentos em desenvolvimento backend Java. Uma licença específica poderá ser adicionada ao repositório quando definida.
