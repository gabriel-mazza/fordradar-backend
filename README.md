# Ford Radar API

API back-end do projeto **Ford Radar**, desenvolvida em Spring Boot para atender dois objetivos principais:

1. **Inteligência competitiva com IA**: receber dados de veículos concorrentes, consultar a IA quando necessário e devolver uma ficha técnica padronizada.
2. **Retenção / predicção de clientes**: expor dados de clientes com score de retenção para consumo pelo app mobile.

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
6. A API salva esse resultado no banco e retorna para o mobile.

### Importante
A IA **não aparece diretamente no app mobile**. Ela é um detalhe interno da API. Para o aplicativo, tudo parece uma chamada normal de endpoint REST.

---

## Estrutura do projeto

A aplicação foi organizada de forma simples e escalável:

- `controllers` → recebem as requisições HTTP
- `services` → concentram as regras de negócio
- `repositories` → fazem o acesso ao banco
- `models` → representam os dados principais
- `integrations` → fazem a comunicação com serviços externos, como a IA
- `security` → cuidam de login, token e acesso protegido
- `exceptions` → padronizam as mensagens de erro
- `dtos` → definem os dados de entrada e saída da API

---

## Banco de dados

O projeto usa **Oracle Database**.

O Flyway cria e organiza as tabelas da aplicação.

Principais tabelas:
- `users` → usuários do sistema
- `competitor_vehicles` → veículos concorrentes e ficha técnica salva
- `customer_predictions` → dados de retenção / previsão

---

## Segurança

A API usa autenticação por **JWT**.

### Como funciona
- O usuário faz login.
- A API devolve um token.
- O mobile salva esse token.
- Nas próximas requisições, o mobile envia o token no header:

```http
Authorization: Bearer SEU_TOKEN_AQUI
```

### Rotas públicas
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Rotas protegidas
- `POST /api/v1/vehicles/compare`
- `POST /api/v1/predictions`
- `GET /api/v1/predictions/{vin}`
- `GET /api/v1/predictions`

---

## Endpoints principais

### 1. Cadastro
`POST /api/v1/auth/register`

Exemplo de corpo:
```json
{
  "name": "Jefferson",
  "email": "jefferson@email.com",
  "password": "123456",
  "role": "ADMIN"
}
```

### 2. Login
`POST /api/v1/auth/login`

Exemplo de corpo:
```json
{
  "email": "jefferson@email.com",
  "password": "123456"
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

Exemplo de corpo:
```json
{
  "brand": "Toyota",
  "model": "Hilux",
  "version": "SRX",
  "targetAttributes": [
    "engine",
    "power",
    "torque",
    "transmission",
    "payloadCapacity"
  ]
}
```

O retorno traz a ficha técnica padronizada. Se a informação não existir, o sistema devolve o texto:
`empty / not available`

### 4. Salvar predição de retenção
`POST /api/v1/predictions`

Exemplo de corpo:
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

Exemplo:
```http
GET /api/v1/predictions/9BWZZZ377VT004251
```

### 6. Listar predições
`GET /api/v1/predictions?page=0&size=10`

---

## Como o app mobile deve consumir essa API

### Sequência recomendada
1. Abrir a tela de login.
2. Fazer login na API.
3. Guardar o token JWT com segurança no dispositivo.
4. Enviar o token em todas as rotas protegidas.
5. Exibir os dados retornados pela API.

### Regras importantes para o mobile
- Sempre enviar `Content-Type: application/json`.
- Sempre incluir `Authorization: Bearer TOKEN` nas rotas protegidas.
- Tratar retorno de erro da API exibindo mensagens amigáveis.
- Não guardar senha em texto puro no dispositivo.

---

## Fluxo do negócio, de forma simples

### Cenário 1: consulta de veículo concorrente
- O consultor abre o app.
- Informa marca, modelo e versão.
- A API checa se já existe aquele resultado salvo.
- Se existir, entrega rápido.
- Se não existir, chama a IA, monta a ficha e salva.

### Cenário 2: consulta de retenção
- A equipe de pós-venda consulta o VIN do cliente.
- A API devolve o score de retenção e os dados já cadastrados.

---

## IA e cache

Para reduzir custo e tempo, a API salva o resultado da IA no banco.

Isso significa que, se o mesmo veículo for consultado de novo, a API tenta usar o que já está salvo antes de chamar a IA novamente.

---

## Como testar rapidamente

### No Postman
Importe o arquivo:
`Ford-Radar.postman_collection.json`

Fluxo recomendado:
1. Register
2. Login
3. Use o token nas demais requisições
4. Teste `vehicles/compare`
5. Teste `predictions`

### No navegador
Se a aplicação estiver rodando, você pode abrir o Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Configuração local

A aplicação lê as configurações do arquivo `application.yml`.

Os valores de banco e credenciais podem ser ajustados por ambiente.

Exemplo de execução local:
```powershell
mvn spring-boot:run
```

---

## Observações para a equipe mobile

- O token JWT é obrigatório depois do login.
- A IA é interna à API, então não precisa ser tratada no app.
- O app só precisa lidar com os endpoints REST e com os dados retornados.
- A API já faz validações e retornos padronizados.

---

## Resumo final

Esse projeto entrega duas coisas para o mobile:

1. **Comparação inteligente de veículos concorrentes com apoio de IA**
2. **Consulta e envio de dados de retenção / predição de clientes**

A API foi desenhada para ser simples de consumir, segura e preparada para crescer.

