# 🧪 Testing — TrackFlow Auth Service

Este documento descreve a estratégia de testes utilizada no **TrackFlow Auth Service**, incluindo os testes implementados, ferramentas utilizadas e instruções para execução da suíte.

> 🚧 **Status:** Em desenvolvimento. A estratégia de testes será expandida conforme novas funcionalidades forem implementadas.

---

## 1. 🎯 Objetivo

Os testes têm como objetivo garantir o comportamento esperado das principais regras de negócio do serviço de autenticação, principalmente relacionadas ao cadastro de usuários.

Atualmente, o foco está nos testes unitários da camada de serviço.

### Escopo atual

- Testes unitários do `UserService`
- Validação das regras de cadastro
- Tratamento de exceções de negócio
- Validação da criptografia de senha
- Normalização de e-mail
- Atribuição da role padrão
- Verificação das interações com repositories e `PasswordEncoder`

---

# 2. 🛠️ Ferramentas utilizadas

### JUnit 5

Framework utilizado para criação e execução dos testes automatizados.

### Mockito

Utilizado para criar mocks das dependências do `UserService`, permitindo testar a lógica de negócio de forma isolada.

### AssertJ

Utilizado para realizar as asserções dos resultados dos testes de maneira mais legível.

---

# 3. 🧩 Estratégia de testes

Os testes do `UserService` seguem uma abordagem de testes unitários, isolando a classe de suas dependências externas.

As principais dependências são simuladas utilizando Mockito:

```text
UserService
    │
    ├── UserRepository       → Mock
    ├── RoleRepository       → Mock
    └── PasswordEncoder      → Mock
```

Dessa forma, os testes concentram-se exclusivamente nas regras implementadas no serviço.

---

# 4. ⚙️ UserServiceTest

Arquivo:

```text
src/test/java/br/com/trackflow/auth/user/service/UserServiceTest.java
```

Atualmente foram implementados **6 cenários de teste**.

---

## 4.1 Cadastro de usuário

### `shouldRegisterUserSuccessfully`

Valida o fluxo principal de cadastro de um usuário.

São verificados:

- Cadastro com dados válidos
- Normalização do e-mail
- Busca da `ROLE_CUSTOMER`
- Criptografia da senha
- Persistência do usuário
- Retorno correto do `UserResponse`

---

## 4.2 E-mail já cadastrado

### `shouldThrowExceptionWhenEmailAlreadyExists`

Valida o comportamento quando já existe um usuário cadastrado com o e-mail informado.

Esperado:

```text
EmailAlreadyExistsException
```

Também é verificado que o processo é interrompido antes de:

- Buscar a role
- Criptografar a senha
- Salvar o usuário

---

## 4.3 Role padrão inexistente

### `shouldThrowExceptionWhenCustomerRoleDoesNotExist`

Valida o comportamento quando a `ROLE_CUSTOMER` não é encontrada no banco.

Esperado:

```text
RoleNotFoundException
```

Também é verificado que o usuário não é persistido quando a role necessária não existe.

---

## 4.4 Criptografia da senha

### `shouldEncodePasswordBeforeSaving`

Valida se a senha informada pelo usuário é enviada ao `PasswordEncoder` antes da persistência.

Exemplo:

```text
Senha informada
      ↓
PasswordEncoder
      ↓
Senha criptografada
      ↓
UserRepository.save()
```

O teste garante que a senha armazenada no objeto `User` é a senha codificada e não a senha original.

---

## 4.5 Normalização do e-mail

### `shouldNormalizeEmailBeforeSaving`

Valida a normalização do endereço de e-mail antes da consulta e persistência.

Exemplo:

```text
  DIEGO@TRACKFLOW.COM
          ↓
diego@trackflow.com
```

O teste verifica que o e-mail normalizado é utilizado durante o fluxo de cadastro.

---

## 4.6 Atribuição da role padrão

### `shouldAssignCustomerRoleToNewUser`

Valida se novos usuários recebem automaticamente:

```text
ROLE_CUSTOMER
```

Também é verificado que a role atribuída ao usuário corresponde à role retornada pelo `RoleRepository`.

## 4.7 UserControllerTest

Arquivo:

`src/test/java/br/com/trackflow/auth/user/controller/UserControllerTest.java`

Atualmente foram implementados 3 cenários de teste.

### 4.7.1 Cadastro de usuário

`shouldRegisterUserSuccessfully`

Valida o cadastro de um usuário através do endpoint:

`POST /api/users`

Resultado esperado:

- HTTP `201 Created`
- Retorno dos dados do usuário
- Chamada do `UserService`

### 4.7.2 Requisição inválida

`shouldReturnBadRequestWhenRequestIsInvalid`

Valida o comportamento quando os dados enviados não atendem às validações do `RegisterRequest`.

Resultado esperado:

- HTTP `400 Bad Request`
- Mensagens de validação retornadas
- `UserService` não é chamado

### 4.7.3 E-mail já cadastrado

`shouldReturnConflictWhenEmailAlreadyExists`

Simula uma tentativa de cadastro utilizando um e-mail já existente.

Resultado esperado:

- HTTP `409 Conflict`
- Mensagem de erro retornada
- `UserService` é chamado

---

# 5. 📊 Cenários testados

| Cenário | Resultado esperado |
|---|---|
| Cadastro com dados válidos | Usuário criado |
| E-mail já cadastrado | `EmailAlreadyExistsException` |
| `ROLE_CUSTOMER` inexistente | `RoleNotFoundException` |
| Criptografia da senha | Senha codificada antes do save |
| E-mail em formato diferente | E-mail normalizado |
| Cadastro de novo usuário | `ROLE_CUSTOMER` atribuída |

---

# 6. 🧱 Padrão utilizado nos testes

Os testes seguem, sempre que possível, a estrutura:

```text
Arrange
   ↓
Act
   ↓
Assert
```

### Arrange

Configuração dos dados e comportamentos dos mocks.

### Act

Execução do método que está sendo testado.

### Assert

Validação do resultado obtido e das interações realizadas.

Também são utilizadas verificações do Mockito, como:

```java
verify(...)
```

e:

```java
verify(..., never())
```

para garantir que determinadas dependências sejam ou não acionadas durante cada cenário.

---

# 7. ▶️ Executando os testes

### Executar todos os testes

Windows:

```bash
mvnw.cmd test
```

Linux/macOS:

```bash
./mvnw test
```

---

### Executar somente o `UserServiceTest`

Windows:

```bash
mvnw.cmd -Dtest=UserServiceTest test
```

Linux/macOS:

```bash
./mvnw -Dtest=UserServiceTest test
```

---

# 8. 📈 Status atual

### UserServiceTest

├── ✅ Cadastro de usuário
├── ✅ E-mail duplicado
├── ✅ Role inexistente
├── ✅ Criptografia da senha
├── ✅ Normalização do e-mail
└── ✅ Atribuição da ROLE_CUSTOMER

**6 testes, 6 aprovados, 0 falhas.**

### UserControllerTest

├── ✅ Cadastro de usuário
├── ✅ Requisição inválida
└── ✅ E-mail já cadastrado

**3 testes, 3 aprovados, 0 falhas.**

### Total

**9 testes, 9 aprovados, 0 falhas.**

---

# 9. 🗺️ Roadmap de testes

# 9. 🗺️ Roadmap de testes

[x] UserServiceTest
[x] UserControllerTest
[ ] AuthenticationServiceTest
[ ] JwtServiceTest
[ ] Security Tests
[ ] Repository Tests
[ ] Integration Tests
[ ] Test Coverage / JaCoCo

### Próximas etapas

```text

[ ] AuthenticationServiceTest
    [ ] Login
    [ ] Credenciais inválidas
    [ ] Usuário desabilitado

[ ] JwtServiceTest
    [ ] Geração de token
    [ ] Validação de token
    [ ] Expiração
    [ ] Claims

[ ] Security Tests
    [ ] JWT Filter
    [ ] Autorização por roles
    [ ] Endpoints protegidos

[ ] Repository Tests
    [ ] Consultas personalizadas
    [ ] Persistência
    [ ] Constraints

[ ] Integration Tests
    [ ] Testcontainers
    [ ] PostgreSQL

[ ] Test Coverage
    [ ] JaCoCo
```

O roadmap será atualizado conforme as funcionalidades forem implementadas no projeto.

---

# 10. 📌 Princípios

A estratégia de testes do TrackFlow busca priorizar:

- Testes legíveis
- Testes determinísticos
- Isolamento das regras de negócio
- Cobertura de cenários de sucesso e erro
- Validação do comportamento, não apenas da implementação
- Feedback rápido durante o desenvolvimento
- Testes unitários do `UserService`
- Testes do `UserController` com MockMvc
- Validação das requisições HTTP
- Validação dos códigos de status HTTP
- Tratamento de exceções no controller

A suíte de testes será evoluída junto com o projeto, evitando a criação de testes artificiais apenas para aumentar métricas de cobertura.