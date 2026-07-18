# API Gestão de Usuários

Projeto prático desenvolvido para consolidar conhecimento em desenvolvimento backend e servir de base para testes manuais e automatizados (QA).

> Este README está sendo construído progressivamente, junto com o código. A seção **Progresso** funciona como rastreador interno durante o desenvolvimento e será removida quando o projeto estiver completo, dando lugar a uma documentação final limpa.

## Status

🔄 Em desenvolvimento — construção incremental, documentada a cada etapa.

## Repositório relacionado

- [`gestao-usuarios-qa`](#) — casos de teste manuais e automação com Cypress cobrindo esta API (a ser criado)

## Tecnologias

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA
- H2 Database (desenvolvimento) / PostgreSQL (migração planejada)
- Spring Security + JWT
- Lombok

---

## Progresso (rastreador interno)

| Camada | Componente | Status |
|---|---|---|
| Model | `User` | ✅ Commitado |
| Repository | `UserRepository` | ⬜ Não iniciado |
| DTO | `RegisterRequest`, `LoginRequest`, `UpdateRequest`, `AuthResponse`, `UserResponse` | ⬜ Não iniciado |
| Security | `JwtUtil` | ⬜ Não iniciado |
| Security | `UserDetailsServiceImpl` | ⬜ Não iniciado |
| Security | `JwtAuthenticationFilter` | ⬜ Não iniciado |
| Security | `SecurityConfig` | ⬜ Não iniciado |
| Service | `UserService` | ⬜ Não iniciado |
| Controller | `AuthController` | ⬜ Não iniciado |
| Controller | `UserController` | ⬜ Não iniciado |

---

## Pré-requisitos

- Java 21 (Temurin recomendado)
- Maven (incluso via `mvnw`, não precisa instalar separado)
- Git

## Como rodar o projeto

```bash
git clone https://github.com/SrLucasGaldinor/gestao-usuarios-api.git
cd gestao-usuarios-api
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. O H2 Console fica disponível em `http://localhost:8080/h2-console`.

(seção a validar quando a aplicação estiver completa e testada de ponta a ponta)

---

## Endpoints planejados

| Método | Endpoint | Descrição | Autenticação | Status |
|---|---|---|---|---|
| POST | /auth/register | Cadastrar usuário | Não | ⬜ |
| POST | /auth/login | Autenticar e receber token | Não | ⬜ |
| GET | /users | Listar usuários | Sim | ⬜ |
| GET | /users/{id} | Buscar usuário por ID | Sim | ⬜ |
| GET | /users/me | Buscar usuário autenticado | Sim | ⬜ |
| PUT | /users/{id} | Atualizar usuário | Sim | ⬜ |
| DELETE | /users/{id} | Deletar usuário | Sim | ⬜ |

## Exemplos de requisição

*(preenchido conforme cada endpoint é implementado e testado)*

```
POST /auth/register
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "Senh@123"
}
```

---

## Testes

Este projeto é coberto por testes manuais documentados e testes automatizados com Cypress, mantidos no repositório [`gestao-usuarios-qa`](#).

- Casos de teste manuais: *(a preencher)*
- Cobertura de automação: *(a preencher)*
- Como rodar os testes: *(a preencher)*

---

## Documentação técnica

Conforme cada componente é commitado, a documentação técnica correspondente é adicionada aqui.

### User (Model)

Entidade central da aplicação, mapeada via JPA para a tabela `users` no banco de dados. Usa Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) para eliminar código repetitivo de getters, setters e construtores.

Campos: `id` (chave primária, auto increment), `nome`, `email` (único), `senha`, `role` (padrão `USER`).

---

## Migração para PostgreSQL

*(preenchido na etapa de migração, planejada para depois da automação de testes com Cypress)*