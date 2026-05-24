# Projeto: Agente Ford Radar - Back-end Context & Architecture

## 1. Visão Geral do Projeto
O **Agente Ford Radar** é o motor de back-end (API RESTful) de uma plataforma B2B desenvolvida para consultores e analistas da rede de concessionárias Ford.

O sistema resolve dois problemas centrais definidos nos desafios de negócio:
*   **Desafio 01 (Inteligência Competitiva):** Recebe dados de veículos da concorrência (Marca, Modelo, Versão e Atributos desejados) e orquestra, através de integração com IA (LLM), a extração e padronização das especificações técnicas. A saída do sistema deve ser sempre uma lista padronizada. Se um dado não existir, o campo será preenchido explicitamente como "vazio / não disponível". A validação final será feita comparando a saída com a ficha técnica oficial da Ford Ranger Raptor.
*   **Desafio 02 (VIN Share / Retenção):** O back-end também gerencia e expõe os dados oriundos de um modelo preditivo de Machine Learning que identifica clientes com risco de evasão da rede de pós-venda (Service Share).

## 2. Stack Tecnológica
A arquitetura foi desenhada em uma abordagem Orientada a Serviços (SOA), com separação clara entre as camadas de apresentação (mobile), serviço (API) e dados.

*   **Linguagem & Framework:** Java com Spring Boot.
*   **Banco de Dados Relacional:** SQL Server.
*   **Controle de Migrações:** Flyway ou Liquibase para versionamento do banco.
*   **Documentação da API:** Swagger/OpenAPI configurado como contrato de endpoints.
*   **Integração Externa:** RestTemplate ou WebClient para consumo de APIs de IA (ex: LLMs).

## 3. Arquitetura Orientada a Serviços (SOA)
O sistema é modular e baseado em serviços independentes. Os pacotes principais devem refletir a separação de responsabilidades (Clean Architecture ou Hexagonal):

*   `controllers`: Exposição das rotas RESTful (uso estrito dos métodos HTTP: GET, POST, PUT, DELETE).
*   `services`: Regras de negócio, validações de domínio e a construção do Prompt dinâmico que será enviado ao LLM para a extração de dados da concorrência.
*   `integrations`: Camada de comunicação externa (chamadas para a API do LLM). O tratamento de erros aqui não deve expor stack traces para o front-end.
*   `repositories`: Interface com o SQL Server (uso de Spring Data JPA/Hibernate).
*   `security`: Filtros de autenticação, JWT e controle de permissões (RBAC).

## 4. Requisitos de Cybersecurity
O projeto obedece a restrições estritas de segurança para a Sprint de Cybersecurity:

*   **Validação de Entradas:** Todas as requisições (ex: DTOs de entrada contendo Marca, Modelo e Versão) devem ser sanitizadas contra SQL Injection, XSS e entradas malformadas.
*   **Autenticação e Autorização:** Implementação de JWT com controle de expiração e RBAC (Role-Based Access Control) para separar acessos de analistas e administradores.
*   **Proteção de APIs:** Obrigatório o uso de HTTPS, configuração correta de CORS e implementação de Rate Limiting/Throttling para evitar DoS e abuso das chamadas de IA.
*   **Privacidade:** Dados sensíveis em repouso devem ser criptografados e a API deve possuir logs estruturados que não exponham dados PII (Personally Identifiable Information).

## 5. Modelagem de Dados (SQL Server)
A persistência no banco de dados deve suportar o histórico de buscas (para economizar chamadas ao LLM via cache) e os insights preditivos:

*   **Tabela `veiculos_concorrencia`**: Armazena Marca, Modelo, Versão e um campo JSON ou estruturado com a Ficha Técnica Padronizada.
*   **Tabela `usuarios` e `roles`**: Gestão de acessos.
*   **Tabela `clientes_predicao`**: Armazena os leads e o score de probabilidade de retenção (gerados pelo Jupyter Notebook da equipe de Data Science) para exposição no front-end.

## 6. Fluxo de Trabalho (Git Workflow)
*   **Branch `main`**: Código de produção, totalmente estável.
*   **Branch `homol` / `develop`**: Ambiente de homologação e integração contínua.
*   **Branches de Tarefa**: Padrão `feature/nome-da-funcionalidade` ou `draft/nome-da-tarefa-em-progresso` para desenvolvimentos incompletos ou rascunhos arquiteturais, garantindo que código não testado não entre na esteira principal.

## 7. Critérios de Aceite para Assistentes de IA
Qualquer código gerado para este projeto deve:
1. Seguir as boas práticas do Java e Spring Boot.
2. Não incluir comentários óbvios, focando na lógica de negócio e segurança.
3. Tratamento de exceções global (`@ControllerAdvice`) retornando respostas padronizadas e seguras.
4. Conter a estrutura para os testes da Sprint de QA.