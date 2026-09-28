# Ford Radar API

![DevSecOps](https://github.com/gabriel-mazza/fordradar-backend/actions/workflows/devsecops.yml/badge.svg)

Gabriel Barros Mazzariol RM 555410
Jefferson Junior Alvarez Urbina RM 558497

API back-end do projeto **Ford Radar**, desenvolvida em Spring Boot para atender dois objetivos principais:

1. **Inteligência competitiva com IA**: receber dados de veículos concorrentes, consultar a IA quando necessário e devolver uma ficha técnica padronizada.
2. **Retenção / predição de clientes**: expor dados de clientes com score de retenção para consumo pelo app mobile.

| Item | Link |
|---|---|
| API publicada (HTTPS) | https://fordradar-backend.onrender.com |
| App mobile | https://github.com/gabriel-mazza/ford-radar-frontend |
| Documentação de Cybersecurity | [DOCUMENTACAO_CYBERSECURITY.md](DOCUMENTACAO_CYBERSECURITY.md) |
| Pipeline (GitHub Actions) | https://github.com/gabriel-mazza/fordradar-backend/actions |

> A API roda no plano gratuito do Render e "dorme" após alguns minutos sem uso. O primeiro acesso depois disso pode levar cerca de 1 minuto.

---

## Visão geral, em linguagem simples

O sistema funciona como uma camada de apoio para o aplicativo mobile da equipe comercial.

- O **mobile** envia solicitações para a API.
- A **API valida, autentica e processa** os dados.
- Quando o fluxo exige análise de veículo concorrente, a API consulta o banco primeiro.
- Se o resultado ainda não estiver salvo, a API chama a **IA Gemini** para montar a resposta.
- O resultado é salvo no banco para reaproveitamento futuro.
- Para os dados de retenção, a API apenas recebe, guarda e entrega as informações preditivas.

Em resumo: o mobile conversa com a API, e a API faz o trabalho pesado.

---

## Onde a IA entra

A IA entra somente no fluxo de **comparação de veículos concorrentes**.

### Fluxo simplificado
1. O usuário do app mobile informa marca, modelo e versão do veículo concorrente.
2. A API procura esse veículo no banco.
3. Se já existir, a API devolve o resultado salvo.
4. Se não existir, a API monta um prompt e envia para o **Gemini**.
5. A IA devolve uma ficha técnica em JSON.
6. A API valida o JSON, salva no banco e retorna para o mobile.

### Importante
A IA **não aparece diretamente no app mobile**. Ela é um detalhe interno da API. Nenhum dado pessoal de cliente é enviado para a IA: apenas marca, modelo, versão e atributos técnicos do veículo.

---

## Estrutura do projeto

- `controllers` → recebem as requisições HTTP
- `services` → concentram as regras de negócio
- `repositories` → fazem o acesso ao banco
- `models` → representam os dados principais
- `integrations` → comunicação com serviços externos, como a IA
- `security` → login, token, rate limit, criptografia e auditoria
- `config` → configurações gerais (OpenAPI/Swagger)
- `exceptions` → padronizam as mensagens de erro
- `dtos` → definem os dados de entrada e saída da API

---

## Banco de dados

O projeto usa **Oracle Database**. O **Flyway** cria e organiza as tabelas.

Principais tabelas:
- `users` → usuários do sistema
- `competitor_vehicles` → veículos concorrentes e ficha técnica salva
- `customer_predictions` → dados de retenção / previsão (nome, e-mail e telefone criptografados)

Migrations:
- `V1` → criação das tabelas
- `V2__Widen_pii_columns_for_encryption.sql` → amplia as colunas de dados pessoais para comportar o texto criptografado (AES-256-GCM)

---

## Segurança

A API usa autenticação por **JWT** e controle de acesso por perfil (RBAC).

### Como funciona
- O usuário faz login e a API devolve um token.
- O mobile guarda o token e o envia no header:

```http
Authorization: Bearer SEU_TOKEN_AQUI
```

### Rotas públicas
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Rotas protegidas
| Rota | Perfil |
|---|---|
| `POST /api/v1/vehicles/compare` | ANALISTA, ADMIN |
| `GET /api/v1/predictions/{vin}` | ANALISTA, ADMIN |
| `GET /api/v1/predictions` | ANALISTA, ADMIN |
| `POST /api/v1/predictions` | somente ADMIN (ingestão do pipeline de ML) |
| `PATCH /api/v1/admin/users/{id}/role` | somente ADMIN |

### Controles implementados (Sprint 3 - DevSecOps)

| Controle | Implementação |
|---|---|
| Autenticação | JWT HS256 com `iss`, `jti` e expiração de 1h; segredo por variável de ambiente (>= 32 bytes) |
| Senhas | BCrypt (custo 12) + política de senha forte no cadastro |
| Controle de acesso (RBAC) | `ANALISTA` / `ADMIN` aplicado em `SecurityConfig` e `@PreAuthorize`; o cadastro público não escolhe perfil |
| Rate limit | 10 req/min por IP em `/auth/**` e 60 req/min nas demais rotas (HTTP 429 + `Retry-After`) |
| Validação de entrada | Bean Validation nos DTOs, regex de VIN, tamanho máximo de página (50), atributos do prompt sanitizados |
| Prompt injection / saída da IA | valores tratados como dados no prompt; resposta do LLM só é persistida se for um objeto JSON |
| Criptografia em repouso | AES-256-GCM (`CryptoConverter`) em nome, e-mail e telefone dos clientes |
| Logs de auditoria | JSON estruturado, logger `AUDIT`, e-mail em hash e VIN mascarado (sem dados pessoais) |
| Headers / erros | HSTS, X-Frame-Options, nosniff, Referrer-Policy; sem stack trace nas respostas; 401/403 padronizados |
| Swagger | desabilitado por padrão (`SWAGGER_ENABLED=true` só em desenvolvimento) |
| Container | imagem multi-stage, usuário não-root, `.dockerignore` |
| Pipeline CI | Gitleaks, Semgrep (SAST), Trivy (SCA, IaC e imagem), Dependabot |

---

## Pipeline DevSecOps

Arquivo: `.github/workflows/devsecops.yml`. Roda em push (`main`, `homol`, `develop`), em pull requests e toda segunda-feira às 06:00 UTC.

```
Gitleaks ─┐
Semgrep  ─┤
Trivy SCA ├──► Build & testes (mvn verify) ──► Container (docker build + Trivy image)
Trivy IaC ┘
```

| Job | Ferramenta | O que verifica | Bloqueia? |
|---|---|---|---|
| Secret scanning | Gitleaks | segredos no código e no histórico do Git | Sim |
| SAST | Semgrep (`p/java`, `p/owasp-top-ten`, `p/secrets`) | falhas no código-fonte | Sim |
| SCA | Trivy (`fs`) | CVEs HIGH/CRITICAL nas dependências do `pom.xml` | Sim |
| IaC | Trivy (`config`) | más práticas no `Dockerfile` | Sim |
| Build & testes | Maven | compilação e testes | Sim |
| Container | Trivy (`image`) | CVEs HIGH/CRITICAL na imagem final | **Não, apenas reporta** (risco aceito) |

O `Dependabot` (`.github/dependabot.yml`) abre PRs semanais para Maven, Docker e GitHub Actions.

### Risco aceito: vulnerabilidades em dependências

O scan de imagem encontrou **59 vulnerabilidades (49 HIGH e 10 CRITICAL)** em bibliotecas Java transitivas do **Spring Boot 3.2.5** (Tomcat, Netty, Jackson, Spring Security e Spring Framework). A imagem base Alpine está limpa (0 vulnerabilidades).

Decisão: o job `container-scan` usa `exit-code: "0"` e continua gerando o relatório completo, para não bloquear a entrega. O risco está analisado e registrado na [documentação de Cybersecurity](DOCUMENTACAO_CYBERSECURITY.md).

Plano de ação: atualizar o Spring Boot para a linha 3.5.x (o que atualiza Tomcat, Netty e Spring Security em conjunto), revalidar todos os endpoints e voltar o `exit-code` para `"1"`.

---

## Endpoints principais

### 1. Cadastro
`POST /api/v1/auth/register`

```json
{
  "name": "Jefferson",
  "email": "jefferson@email.com",
  "password": "Senha@12345"
}
```

O perfil inicial é sempre `ANALISTA`. Somente um ADMIN pode promover usuários.

### 2. Login
`POST /api/v1/auth/login`

```json
{
  "email": "jefferson@email.com",
  "password": "Senha@12345"
}
```

Resposta esperada:
```json
{
  "token": "JWT_AQUI"
}
```

### 3. Comparar veículo concorrente com IA
`POST /api/v1/vehicles/compare`

```json
{
  "brand": "Toyota",
  "model": "Hilux",
  "version": "SRX",
  "targetAttributes": ["engine", "power", "torque", "transmission", "payloadCapacity"]
}
```

O retorno traz a ficha técnica padronizada. Se a informação não existir, o sistema devolve o texto: `empty / not available`

### 4. Salvar predição de retenção (somente ADMIN)
`POST /api/v1/predictions`

```json
{
  "vin": "9BWZZZ377VT004251",
  "customerName": "Carlos Silva",
  "customerEmail": "carlos@email.com",
  "phone": "11999999999",
  "retentionScore": 87.5
}
```

### 5. Buscar predição por VIN
`GET /api/v1/predictions/{vin}`

### 6. Listar predições
`GET /api/v1/predictions?page=0&size=10` (tamanho máximo de página: 50)

### 7. Promover usuário (somente ADMIN)
`PATCH /api/v1/admin/users/{id}/role`

---

## Como o app mobile deve consumir essa API

1. Abrir a tela de login e autenticar.
2. Guardar o token JWT com segurança no dispositivo.
3. Enviar o token em todas as rotas protegidas.
4. Exibir os dados retornados e tratar erros com mensagens amigáveis.

Regras: sempre enviar `Content-Type: application/json`, sempre incluir `Authorization: Bearer TOKEN` nas rotas protegidas e nunca guardar senha em texto puro no dispositivo.

---

## IA e cache

Para reduzir custo e tempo, a API salva o resultado da IA no banco. Se o mesmo veículo for consultado de novo, a API usa o que já está salvo antes de chamar a IA novamente.

---

## Configuração local

Copie `.env.example`, preencha os valores e exporte-os como variáveis de ambiente. **Não há segredos no repositório**: a aplicação não sobe sem as variáveis obrigatórias.

| Variável | Obrigatória | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Sim | URL JDBC do Oracle |
| `SPRING_DATASOURCE_USERNAME` | Sim | usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | Sim | senha do banco |
| `JWT_SECRET` | Sim | segredo de assinatura do JWT (>= 32 bytes) |
| `DATA_ENCRYPTION_KEY` | Sim | chave AES-256 em Base64 dos dados pessoais. **Perdeu a chave, perdeu os dados** |
| `LLM_API_KEY` | Sim | chave da API do Gemini |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Opcional | cria o primeiro ADMIN no startup (senha >= 12 caracteres) |
| `CORS_ALLOWED_ORIGINS` | Opcional | origens permitidas |
| `SERVER_FORWARD_HEADERS` | Opcional | use `framework` atrás de proxy (Render), para o rate limit enxergar o IP real |
| `SWAGGER_ENABLED` | Opcional | `false` por padrão |

Geração dos segredos:
```bash
openssl rand -base64 48   # JWT_SECRET
openssl rand -base64 32   # DATA_ENCRYPTION_KEY (AES-256)
mvn spring-boot:run
```

O primeiro ADMIN é criado no startup a partir de `ADMIN_EMAIL` / `ADMIN_PASSWORD`. Depois, ele promove outros usuários via `PATCH /api/v1/admin/users/{id}/role`.

Container:
```bash
docker build -t fordradar-api .
```

---

## Deploy (Render)

A API está publicada no **Render** como Web Service com runtime **Docker**, a partir do `Dockerfile` do repositório, com deploy automático a cada commit na `main`.

Configuração aplicada:
- Todas as variáveis da tabela acima são configuradas no painel do Render (Environment), nunca no repositório.
- `SERVER_PORT=10000` (porta esperada pelo Render).
- `JAVA_TOOL_OPTIONS=-Xmx384m` (limite de memória para o plano gratuito de 512 MB).
- `SERVER_FORWARD_HEADERS=framework`.
- Health Check Path vazio (a API não expõe rota de health pública).
- `SPRING_FLYWAY_VALIDATE_ON_MIGRATE=false`: a migration `V2` foi alterada depois de aplicada (remoção de comentários), gerando divergência de checksum. Risco documentado na documentação de Cybersecurity, com o comando de correção definitiva (`flyway repair`).

---

## Como testar rapidamente

### No Postman
Importe `Ford-Radar.postman_collection.json` e siga: Register → Login → use o token nas demais requisições → `vehicles/compare` → `predictions`.

### No navegador
Com `SWAGGER_ENABLED=true`:
```text
http://localhost:8080/swagger-ui/index.html
https://fordradar-backend.onrender.com/swagger-ui/index.html
```

A tabela de testes que comprovam cada controle de segurança está na seção "Validação dos controles" da [documentação de Cybersecurity](DOCUMENTACAO_CYBERSECURITY.md).

---

## Observabilidade

- A API emite **logs de auditoria em JSON estruturado** (logger `AUDIT`), sem dados pessoais: e-mail em hash e VIN mascarado.
- Em produção, os logs ficam disponíveis no painel do Render.
- Proposta de dashboard (Grafana) com métricas, alertas e plano de resposta a incidentes: veja a [documentação de Cybersecurity](DOCUMENTACAO_CYBERSECURITY.md), seção "Observabilidade e Resposta".

---

## Observações para a equipe mobile

- O token JWT é obrigatório depois do login.
- A IA é interna à API, então não precisa ser tratada no app.
- O app só precisa lidar com os endpoints REST e com os dados retornados.
- A API já faz validações e retornos padronizados (401, 403, 429 com `Retry-After`).

---

## Resumo final

Esse projeto entrega duas coisas para o mobile:

1. **Comparação inteligente de veículos concorrentes com apoio de IA**
2. **Consulta e envio de dados de retenção / predição de clientes**

A API foi desenhada para ser simples de consumir, segura e preparada para crescer.
