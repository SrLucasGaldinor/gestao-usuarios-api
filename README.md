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
- Spring-Dotenv

---

## Progresso (rastreador interno)

| Camada | Componente | Status |
|---|---|---|
| Model | `User` | ✅ Commitado |
| Repository | `UserRepository` | ✅ Commitado |
| DTO | `RegisterRequest`, `LoginRequest`, `UpdateRequest`, `AuthResponse`, `UserResponse` | ✅ Commitado |
| Security | `JwtUtil` | ✅ Commitado |
| Security | `UserDetailsServiceImpl` | ✅ Commitado |
| Security | `JwtAuthenticationFilter` | ✅ Commitado |
| Security | `SecurityConfig` | ✅ Commitado |
| Service | `UserService` | ✅ Commitado |
| Controller | `AuthController` | ✅ Commitado |
| Controller | `UserController` | ✅ Commitado |

---

## Pré-requisitos

- Java 21 (Temurin recomendado)
- Maven (incluso via `mvnw`, não precisa instalar separado)
- PostgreSQL (rodando localmente, porta padrão 5432)
- Git

## Como rodar o projeto

1. Crie um banco de dados PostgreSQL chamado `gestao_usuarios`

2. Clone o repositório e crie um arquivo `.env` na raiz, baseado no `.env.example`:
```bash
git clone https://github.com/SrLucasGaldinor/gestao-usuarios-api.git
cd gestao-usuarios-api
cp .env.example .env
```

3. Preencha o `.env` com suas credenciais reais do PostgreSQL e uma chave JWT forte

4. Rode a aplicação:
```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

---

## Endpoints planejados

| Método | Endpoint | Descrição | Autenticação | Status |
|---|---|---|---|---|
| POST | /auth/register | Cadastrar usuário | Não | ✅ |
| POST | /auth/login | Autenticar e receber token | Não | ✅ |
| GET | /users | Listar usuários | Sim | ✅ |
| GET | /users/{id} | Buscar usuário por ID | Sim | ✅ |
| GET | /users/me | Buscar usuário autenticado | Sim | ⬜ |
| PUT | /users/{id} | Atualizar usuário | Sim | ✅ |
| DELETE | /users/{id} | Desativar usuário (soft delete) | Sim | ✅ |

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

### Testes Unitários

5 testes cobrindo as regras de negócio da camada `UserService`: duplicidade de e-mail 
e criptografia de senha no cadastro, exclusão lógica (soft delete) e seu comportamento 
para usuário inexistente, e filtragem de usuários ativos na listagem. Detalhes de cada 
teste na seção `Testes Unitários` da Documentação Técnica.

```bash
./mvnw test
```

### Testes Manuais

21 casos de teste executados manualmente via Insomnia, cobrindo os cenários positivos 
e negativos de cada endpoint.

**POST /auth/register (5 casos):** cadastro válido (201); e-mail duplicado (409); 
e-mail inválido, senha em branco e nome ausente (400, via Bean Validation).

**POST /auth/login (4 casos):** login válido (200 + token); senha incorreta e e-mail 
inexistente (401, com mensagem idêntica nos dois casos, evitando enumeração de 
usuários); campos em branco (400, barrado pela validação antes de tentar autenticar).

**GET /users (2 casos):** listagem com token válido (200, retornando apenas usuários 
ativos); sem token (403).

**GET /users/{id} (3 casos):** busca por ID existente (200, inclusive para usuários 
desativados); ID inexistente (404); sem token (403).

**PUT /users/{id} (3 casos):** atualização válida (200); ID inexistente (404); sem 
token (403).

**DELETE /users/{id} (3 casos):** desativação de usuário existente (204), confirmando 
que o registro some da listagem geral mas continua acessível por busca direta; ID 
inexistente (404); sem token (403).

**Observação:** endpoints protegidos sem autenticação retornam `403 Forbidden`, não 
`401 Unauthorized`. Esse é o comportamento padrão do Spring Security quando não há 
nenhuma tentativa de autenticação (nem token malformado, nem token ausente) e nenhum 
`AuthenticationEntryPoint` customizado foi configurado.

---

## Documentação técnica

Conforme cada componente é commitado, a documentação técnica correspondente é adicionada aqui.

### User (Model)

Entidade central da aplicação, mapeada via JPA para a tabela `users` no banco de dados. Usa Lombok (`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) para eliminar código repetitivo de getters, setters e construtores.

Campos: `id` (chave primária, auto increment), `nome`, `email` (único), `senha`, `role` (padrão `USER`), `ativo` (padrão `true`, usado para soft delete).

### UserRepository (Repository)

Interface que estende `JpaRepository<User, Long>`, herdando automaticamente operações 
de CRUD (`save`, `findById`, `findAll`, `deleteById`, entre outras) sem necessidade de 
implementação manual.

Três métodos customizados usam Query Derivation, mecanismo do Spring Data JPA que gera 
a query JPQL a partir do nome do método:

- `findByEmail(String email)`: retorna `Optional<User>`, usado na autenticação (sem filtro de status, necessário para o Spring Security identificar contas desativadas)
- `existsByEmail(String email)`: retorna `boolean`, usado para validar duplicidade no cadastro
- `findByAtivoTrue()`: retorna `List<User>`, usado na listagem geral para ocultar usuários desativados

Essa camada isola todo o acesso a dados do restante da aplicação, seguindo o padrão 
Repository. O `UserService` nunca interage diretamente com o banco, apenas com essa interface.

### DTOs (Data Transfer Objects)

Camada de tradução entre a API e o banco de dados, evitando expor a entidade `User` 
diretamente (especialmente o campo `senha`).

**DTOs de entrada (Request), validados com Bean Validation:**
- `RegisterRequest`: nome, email, senha (`@NotBlank`, `@Email`, `@Size(min = 6)`)
- `LoginRequest`: email, senha (`@NotBlank`, `@Email`)
- `UpdateRequest`: nome (`@NotBlank`)

**DTOs de saída (Response):**
- `AuthResponse`: token JWT retornado após login bem-sucedido
- `UserResponse`: dados seguros do usuário (sem senha), com método estático `fromEntity()` 
para conversão a partir da entidade `User`

A validação só é aplicada quando o parâmetro do controller usa `@Valid`. Se alguma regra 
falhar, o Spring retorna automaticamente `400 Bad Request` com a mensagem definida em 
cada anotação.

### JwtUtil (Security)

Classe responsável por gerar e validar tokens JWT (JSON Web Token), usados para 
autenticação stateless da API.

**Geração:** `generateToken(UserDetails)` monta um token contendo o email do usuário 
(subject), data de emissão e data de expiração (24 horas), assinado com o algoritmo 
HMAC-SHA usando uma chave secreta.

**Validação:** `isTokenValid(String, UserDetails)` confirma que o token pertence ao 
usuário correto e ainda não expirou.

**Extração de dados:** usa programação funcional (`Function<Claims, T>`) para reutilizar 
a lógica de decodificação do token entre os métodos `extractUsername` e `extractExpiration`.

**Segurança do segredo:** o `jwt.secret` é lido de uma variável de ambiente 
(`${JWT_SECRET:valor_padrao}`), nunca commitado em texto puro. Um arquivo `.env.example` 
documenta o formato esperado, sem expor o valor real. O `.gitignore` impede que um 
eventual `.env` real seja commitado.

### UserDetailsServiceImpl (Security)

Implementa a interface `UserDetailsService` do Spring Security, funcionando como 
tradutor entre a entidade `User` (nossa, específica da aplicação) e a interface 
`UserDetails` (genérica, usada pelo Spring Security).

O método `loadUserByUsername(String email)` é chamado automaticamente pelo Spring 
Security sempre que uma autenticação é necessária (tanto no login quanto na validação 
de tokens JWT em requisições). Ele busca o usuário no `UserRepository` por email e 
monta um `UserDetails` com:

- **username**: o email do usuário
- **password**: a senha armazenada no banco, já em formato hash BCrypt (ver detalhes 
de criptografia na seção `UserService`)
- **authorities**: permissões no formato `ROLE_<role>`, exigido pelo Spring Security
- **disabled**: reflete o campo `ativo` do usuário. Quando um usuário é desativado 
(soft delete), esse campo passa a `true`, e o Spring Security bloqueia automaticamente 
qualquer tentativa de login para essa conta

Essa camada de desacoplamento permite que o Spring Security funcione sem conhecer a 
estrutura real da nossa entidade `User`, enxergando apenas o mínimo necessário para 
autenticação e autorização.

### JwtAuthenticationFilter (Security)

Filtro que intercepta toda requisição HTTP antes dela chegar ao controller, executando 
exatamente uma vez por requisição (`OncePerRequestFilter`). Responsável por verificar 
se a requisição trouxe um token JWT válido e, se sim, registrar essa requisição como 
autenticada para o restante da aplicação.

**Fluxo:**
1. Verifica o cabeçalho `Authorization`. Sem token no formato `Bearer`, deixa a 
requisição seguir sem tentar autenticar (rotas públicas passam despreocupadas)
2. Extrai o email do token (delega para `JwtUtil`)
3. Busca o usuário atual no banco (delega para `UserDetailsServiceImpl`)
4. Confirma que o token pertence a esse usuário e não expirou (`JwtUtil` novamente)
5. Se válido, registra a autenticação no `SecurityContextHolder`, usando 
`UsernamePasswordAuthenticationToken` já com `authenticated = true` (a validação já 
ocorreu via assinatura do JWT, não é necessário passar por um `AuthenticationProvider` 
novamente)
6. Sempre deixa a requisição continuar, independente do resultado

**Tratamento de exceção:** tokens malformados ou corrompidos (`JwtException`) são 
capturados e registrados via log (SLF4J), sem interromper a requisição. O comportamento 
nesse caso é idêntico a "nenhum token presente", deixando a decisão de bloqueio para 
o `SecurityConfig`.

**Importante:** esse filtro não decide se uma rota exige autenticação, essa 
responsabilidade é do `SecurityConfig`. Ele só orquestra a validação e publica o resultado.

### SecurityConfig (Security)

Classe central que orquestra toda a configuração de segurança da aplicação, conectando 
os componentes (`JwtUtil`, `UserDetailsServiceImpl`, `JwtAuthenticationFilter`) 
e definindo as regras de acesso.

**Beans expostos:**

- **`passwordEncoder()`**: instância única de `BCryptPasswordEncoder`, compartilhada 
entre o cadastro (criptografar senha) e o login (comparar senha)
- **`authenticationProvider()`**: `DaoAuthenticationProvider`, componente que efetivamente 
busca o usuário (via `UserDetailsServiceImpl`) e compara a senha (via `PasswordEncoder`) 
durante o login
- **`authenticationManager()`**: interface de mais alto nível usada pelo `AuthController`, 
que delega a verificação para o `authenticationProvider()` registrado

**`securityFilterChain(HttpSecurity)` — regras de acesso:**

- `/auth/**`: liberado sem autenticação
- `/error`: liberado sem autenticação, necessário para que o próprio mecanismo de 
tratamento de erros do Spring consiga responder corretamente. Sem essa liberação, um 
redirecionamento interno que o Spring faz ao processar qualquer erro acaba sendo 
bloqueado pela regra de autenticação, mascarando o status HTTP real da resposta
- Qualquer outra rota: exige autenticação válida
- Sessão configurada como `STATELESS` (sem sessão HTTP; cada requisição se autentica 
por conta própria via token, sem o servidor guardar memória de autenticações anteriores)
- CSRF desabilitado (proteção não aplicável a APIs stateless autenticadas por JWT)
- `JwtAuthenticationFilter` registrado via `addFilterBefore`, executando antes do filtro 
padrão de autenticação do Spring Security

### UserService (Service)

Camada de regras de negócio, separando a lógica da aplicação do acesso a dados 
(`Repository`) e do recebimento de requisições HTTP (`Controller`).

**`register(RegisterRequest)`:** valida duplicidade de email (`409 CONFLICT` se já 
existir) e criptografa a senha com BCrypt (`passwordEncoder.encode()`) antes de salvar. 
A partir desse ponto, a senha em texto puro nunca mais existe na aplicação, apenas o 
hash irreversível.

**`findAll()`:** usa `findByAtivoTrue()`, respeitando o soft delete — usuários 
desativados não aparecem na listagem geral.

**`findById(Long)` / `findByEmail(String)`:** localizam um usuário pelo ID ou pelo 
e-mail. São usados tanto em consultas administrativas quanto pelo `UserDetailsServiceImpl` 
durante o processo de login.

**`update(Long, UpdateRequest)`:** atualiza o nome do usuário correspondente ao ID 
informado.

**`deleteById(Long)`:** realiza a exclusão lógica do usuário (soft delete), sem remover 
o registro do banco de dados.

### AuthController (Controller)

Camada que recebe as requisições HTTP de autenticação e devolve as respostas 
correspondentes, delegando toda a lógica para `UserService`, `AuthenticationManager` 
e `JwtUtil`.

**`POST /auth/register`:** valida os dados de entrada (`@Valid`) e delega o cadastro 
para `UserService.register()`. Retorna `201 Created` com os dados do usuário.

**`POST /auth/login`:** autentica via `AuthenticationManager`, que dispara a cadeia 
`DaoAuthenticationProvider` → `UserDetailsServiceImpl` → `PasswordEncoder`. Dois 
cenários de falha são tratados separadamente:

- `BadCredentialsException` → `401 Unauthorized`, com mensagem genérica ("Email ou 
senha inválidos"), sem especificar qual dos dois está incorreto, evitando ataques de 
enumeração de usuários
- `DisabledException` → `403 Forbidden`, com mensagem específica ("Esta conta está 
desativada"), já que não há risco de segurança em revelar essa informação

Em caso de sucesso, extrai o `UserDetails` autenticado e gera o token via 
`JwtUtil.generateToken()`, retornando `200 OK` com o token no formato `AuthResponse`.

### UserController (Controller)

Expõe os endpoints de consulta, atualização e desativação de usuários, delegando toda 
a lógica para `UserService`.

**`GET /users`:** lista usuários ativos.

**`GET /users/{id}`:** busca um usuário pelo ID.

**`GET /users/me`:** retorna os dados do usuário autenticado na requisição atual. Não 
recebe nenhum parâmetro de identificação explícito (como um ID na URL); em vez disso, 
recebe um `Authentication` injetado automaticamente pelo Spring, já preenchido pelo 
`JwtAuthenticationFilter` antes da requisição chegar ao controller. O e-mail do usuário 
autenticado é obtido via `authentication.getName()` e usado para buscar os dados 
completos através de `UserService.findByEmail()`.

**`PUT /users/{id}`:** atualiza o nome de um usuário.

**`DELETE /users/{id}`:** realiza a exclusão lógica (soft delete) de um usuário.

## Testes Unitários

Cobertura de testes unitários (JUnit 5 + Mockito) para as regras de negócio da camada 
`UserService`, isolando a lógica de dependências externas (`UserRepository`, 
`PasswordEncoder`) através de mocks, sem subir o contexto do Spring nem acessar banco 
de dados real.

**Testes implementados:**

- `register_DeveLancarConflito_QuandoEmailJaExiste`: confirma que o cadastro é 
bloqueado (`409 CONFLICT`) quando o e-mail já está em uso, e que nenhum registro é 
salvo nesse caso
- `register_DeveCriptografarSenhaEsSalvar_QuandoEmailNaoExiste`: confirma que a senha 
salva é o hash criptografado, nunca a senha em texto puro, e que o usuário nasce ativo
- `deleteById_DeveMarcarComoInativo_EmVezDeRemover`: confirma que a exclusão lógica 
marca `ativo = false` e que o método de exclusão real do banco nunca é chamado
- `deleteById_DeveLancarNotFound_QuandoUsuarioNaoExiste`: confirma o retorno 
`404 NOT_FOUND` para IDs inexistentes
- `findAll_DeveRetornarApenasUsuariosAtivos`: confirma que a listagem usa a consulta 
filtrada por status, não a consulta genérica do repositório

**Como rodar:** `./mvnw test`

---

## Migração para PostgreSQL

O projeto começou usando H2, um banco de dados que existe apenas na memória enquanto 
a aplicação está rodando e se apaga por completo a cada reinício. Isso é ótimo para 
desenvolver rápido, mas não reflete como um sistema funciona de verdade. Esta seção 
documenta a migração para PostgreSQL, um banco real e persistente, aproximando o 
projeto de um ambiente de produção.

**O que mudou:**

- A dependência do H2 foi substituída pela do PostgreSQL no `pom.xml`
- A configuração que decide como o banco trata as tabelas (`ddl-auto`) mudou de 
`create-drop` para `update`. Com H2, a cada reinício da aplicação as tabelas eram 
apagadas e recriadas do zero, sem problema, já que nada ali precisava ser mantido. 
Num banco real, isso apagaria os dados dos usuários toda vez que o servidor 
reiniciasse. `update` faz o banco ajustar sua estrutura sem apagar o que já existe
- As credenciais de acesso ao banco (usuário e senha) passaram a vir de variáveis de 
ambiente, em vez de ficarem escritas diretamente no código. Essa é a mesma lógica já 
aplicada à chave de autenticação (JWT): informações sensíveis não devem ficar expostas 
no repositório
- Foi adicionada uma biblioteca (`spring-dotenv`) que lê essas variáveis de um arquivo 
local (`.env`, nunca enviado ao GitHub) e as disponibiliza automaticamente para a 
aplicação, sem precisar configurar nada manualmente

**Dificuldade encontrada:** a versão inicial da biblioteca escolhida para ler o `.env` 
simplesmente não funcionava com a versão do Spring Boot usada neste projeto, sem 
apresentar nenhum erro visível, ela apenas não fazia nada. A causa: bibliotecas desse 
tipo às vezes lançam versões diferentes para cada versão principal do Spring Boot, e 
usar a versão errada não gera aviso, só faz a funcionalidade parecer "quebrada" sem 
motivo aparente. A solução foi trocar para a versão da biblioteca feita especificamente 
para a versão do Spring Boot em uso.