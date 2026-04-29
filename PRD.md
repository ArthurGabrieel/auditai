# Documento de Requisitos do Produto (PRD) – AuditAI (MVP)

## 1. Visão Geral do Produto
O **AuditAI** é uma aplicação *Full Stack* de auditoria automatizada. O sistema permite o upload de registros de ponto (arquivos de texto), processa os dados de forma assíncrona utilizando Inteligência Artificial para detectar anomalias ou violações trabalhistas, e notifica o usuário do resultado em tempo real através de um painel interativo.

**Objetivo Principal:** Homologar a arquitetura técnica da stack proposta (Java, Spring Boot, RabbitMQ, LangChain4j, WebSockets e Angular 16) em um cenário de uso real e funcional.

---

## 2. Casos de Uso e Fluxo Principal

1. O usuário acessa o painel de auditoria.
2. O usuário faz o upload de um arquivo `.txt` ou insere um bloco de texto contendo os registros de horários de um funcionário.
3. O frontend envia os dados para a API e recebe um status de "Processamento Iniciado".
4. A API salva o registro no banco de dados com status `PENDENTE` e publica um evento na fila do *message broker*.
5. Um *worker* em background consome a mensagem, envia o texto para o LLM via LangChain4j com o prompt de auditoria e aguarda a resposta.
6. Ao receber a resposta da IA, o *worker* atualiza o status no banco de dados para `CONCLUIDO`.
7. O backend emite um evento via WebSocket para o frontend.
8. A interface do usuário é atualizada em tempo real, exibindo o parecer da auditoria sem necessidade de recarregar a página.

---

## 3. Escopo Técnico e Arquitetura

### 3.1. Frontend (Interface do Usuário)
- **Framework:** Angular 16 + TypeScript.
- **Gerenciamento de Estado:** RxJS (utilização de `BehaviorSubject` para a lista de auditorias e `Observable` para conexões de socket).
- **Comunicação em Tempo Real:** `stompjs` e `sockjs-client` para a conexão WebSocket.
- **UX/UI Guidelines:** A interface deve seguir uma estética moderna, priorizando um *dark theme* nativo com layouts amplos e espaçados. Para o estado de "Processamento da IA", a implementação de animações fluidas (como Lottie files) garantirá um excelente feedback visual enquanto a requisição ocorre em background.

### 3.2. Backend (Core API & Processamento)
- **Linguagem & Framework:** Java 25 + Spring Boot 4.x.
- **Integração IA:** LangChain4j configurado com o provider do Google Gemini.
- **Mensageria:** RabbitMQ para desacoplar a requisição HTTP do processamento do LLM.
- **WebSockets:** Spring WebSocket habilitado com protocolo STOMP.
- **Banco de Dados:** PostgreSQL (acessado via Spring Data JPA / Hibernate).

### 3.3. Infraestrutura & DevOps
- O ambiente de desenvolvimento local deve ser estritamente conteinerizado. Um arquivo `docker-compose.yml` será responsável por orquestrar os serviços essenciais de forma unificada:
  - PostgreSQL (Porta 5432)
  - RabbitMQ com *Management Plugin* (Portas 5672 e 15672)
  - Aplicação Spring Boot

---

## 4. Histórias de Usuário (User Stories)

| ID | Épico | História de Usuário | Critérios de Aceitação |
| :--- | :--- | :--- | :--- |
| **US01** | Ingestão | Como usuário, quero submeter um log de ponto para análise. | - Formulário Angular usando `ReactiveFormsModule`.<br>- API REST recebe o payload e retorna HTTP 202 (Accepted). |
| **US02** | Fila Assíncrona | Como sistema, quero enfileirar a requisição para não travar a API. | - Registro salvo no PostgreSQL como `PENDENTE`.<br>- Mensagem publicada com sucesso na fila `audit.process` do RabbitMQ. |
| **US03** | Motor de IA | Como sistema, quero que um *worker* avalie as horas via LLM. | - `@RabbitListener` consome a fila.<br>- LangChain4j envia o prompt + dados ao Gemini.<br>- Status atualizado para `CONCLUIDO` no banco. |
| **US04** | Tempo Real | Como usuário, quero ver o resultado da auditoria aparecer na tela instantaneamente. | - Backend publica no tópico STOMP `/topic/audits`.<br>- Frontend via RxJS intercepta a mensagem e atualiza o DOM sem *refresh*. |
| **US05** | Observabilidade | Como time técnico, quero visibilidade operacional para identificar gargalos e falhas rapidamente. | - Logs estruturados com `auditId`, `tenant/contexto`, `status`, `latenciaMs`.<br>- Métricas expostas (`Micrometer/Prometheus`) para tempo de processamento, tamanho da fila e taxa de erro.<br>- Endpoint de healthcheck e readiness ativo. |
| **US06** | Resiliência | Como sistema, quero tratar falhas externas sem perder auditorias. | - Política de `retry` com backoff para falhas transitórias do LLM.<br>- Mensagens inválidas ou excedidas em tentativas vão para DLQ (`audit.process.dlq`).<br>- Status `ERRO` persistido com `motivo_erro` e possibilidade de reprocessamento manual. |
| **US07** | Segurança | Como produto SaaS, quero garantir acesso seguro e isolamento por usuário. | - API protegida com autenticação JWT.<br>- Endpoints de consulta retornam apenas dados do usuário autenticado.<br>- Validação de payload, limites de tamanho do texto e sanitização de entrada. |
| **US08** | Qualidade | Como equipe, quero reduzir regressão com testes automatizados de ponta a ponta. | - Testes de integração para REST + PostgreSQL + RabbitMQ (Testcontainers).<br>- Teste de fluxo completo: criação, enfileiramento, processamento e notificação via WebSocket.<br>- Pipeline CI executa suíte de testes antes de merge. |

---

## 5. Estrutura de Dados (Modelagem Básica)

**Entidade: `Auditoria` (PostgreSQL)**
- `id` (UUID, Primary Key)
- `conteudo_ponto` (Text)
- `status` (Enum: PENDENTE, PROCESSANDO, CONCLUIDO, ERRO)
- `parecer_ia` (Text, nullable)
- `motivo_erro` (Text, nullable)
- `tentativas_processamento` (Integer, default 0)
- `data_criacao` (Timestamp)
- `data_conclusao` (Timestamp, nullable)

---

## 6. Engenharia de Prompts (Definição Inicial)

O sistema utilizará um *System Prompt* fixo no backend para garantir que o LLM retorne o formato desejado, mitigando alucinações.

**Prompt Template:**
> "Você é um auditor de compliance trabalhista altamente rigoroso. Analise os seguintes registros de ponto e identifique: 1) Possíveis horas extras acima do limite legal de 2h/dia. 2) Falta de intervalo intrajornada. Retorne sua análise de forma direta, clara e em formato de tópicos curtos. Registros: {{conteudo_ponto}}"

---

## 7. Requisitos Não Funcionais

- **Observabilidade:** logs estruturados + correlação por `auditId` + dashboard mínimo de métricas.
- **Resiliência:** tempo limite de chamada LLM, `retry` com backoff e DLQ para mensagens envenenadas.
- **Segurança:** autenticação JWT, autorização por recurso e proteção contra payload abusivo.
- **Performance:** API de ingestão deve responder com HTTP 202 em até 500ms (sem processamento síncrono do LLM).
- **Confiabilidade:** nenhuma auditoria pode ser perdida entre API, fila e banco.

---

## 8. Critérios de Sucesso do MVP

- O fluxo de ponta a ponta (Upload -> Fila -> IA -> WebSocket -> Tela) funciona sem falhas de comunicação.
- O código Angular utiliza *Lazy Loading* para as rotas e boas práticas de inscrição/desinscrição (*unsubscribe*) nos *Observables* do RxJS para evitar vazamento de memória.
- Os serviços de infraestrutura (Banco e Mensageria) sobem com um único comando `docker-compose up -d`.
- Métricas e logs permitem diagnosticar claramente falhas de processamento.
- Existe caminho claro para reprocessar auditorias que falharam.
- A suíte mínima de testes de integração e E2E está automatizada na CI.

---

## 9. Backlog Técnico Executável (MVP)

### 9.1. Ordem de Implementação (Fases)

1. **Fase 0 - Fundação local**
2. **Fase 1 - Ingestão + Persistência**
3. **Fase 2 - Processamento assíncrono (RabbitMQ + Worker)**
4. **Fase 3 - Integração IA (LangChain4j + Gemini)**
5. **Fase 4 - Tempo real (WebSocket STOMP/SockJS)**
6. **Fase 5 - Resiliência e operação**
7. **Fase 6 - Segurança**
8. **Fase 7 - Qualidade e CI**

### 9.2. Épicos e Tarefas

#### Épico E1 - Fundação e Ambiente
**Objetivo:** Subir stack local reproduzível e preparada para desenvolvimento.

- **T1.1** Criar estrutura inicial de repositório (`backend/`, `frontend/`, `infra/`, `docs/`).
- **T1.2** Subir `docker-compose.yml` com PostgreSQL e RabbitMQ (management plugin).
- **T1.3** Criar app Spring Boot base com `actuator`, JPA, AMQP e WebSocket.
- **T1.4** Criar app Angular 16 com roteamento e lazy loading inicial.
- **T1.5** Definir variáveis de ambiente e arquivo `.env.example`.

**Definition of Done (DoD):**
- `docker-compose up -d` funcional.
- Backend e frontend inicializam localmente sem erro.
- Readme com instruções de bootstrap.

---

#### Épico E2 - Ingestão de Auditoria (US01)
**Objetivo:** Receber registros, validar payload e persistir auditoria como `PENDENTE`.

- **T2.1** Criar entidade `Auditoria`, enum `StatusAuditoria` e migration SQL.
- **T2.2** Criar endpoint `POST /api/auditorias` retornando HTTP 202.
- **T2.3** Validar payload (não vazio, limite de tamanho, charset esperado).
- **T2.4** Persistir auditoria com timestamps e `tentativas_processamento=0`.
- **T2.5** Criar tela Angular com Reactive Form para texto/upload `.txt`.

**DoD:**
- API persiste auditoria corretamente no PostgreSQL.
- Front envia payload válido e trata feedback de aceite.

---

#### Épico E3 - Fila Assíncrona (US02)
**Objetivo:** Desacoplar ingestão HTTP do processamento da IA.

- **T3.1** Definir exchange, routing key e fila `audit.process`.
- **T3.2** Publicar mensagem após persistência com `auditId`.
- **T3.3** Implementar consumidor com `@RabbitListener`.
- **T3.4** Atualizar status para `PROCESSANDO` ao iniciar consumo.

**DoD:**
- Mensagem publicada e consumida com ACK correto.
- API responde rápido sem aguardar LLM.

---

#### Épico E4 - Motor de IA (US03)
**Objetivo:** Processar o conteúdo de ponto via Gemini (LangChain4j) e salvar parecer.

- **T4.1** Configurar cliente LangChain4j com provider Gemini.
- **T4.2** Implementar serviço de prompt com template fixo e parâmetros seguros.
- **T4.3** Persistir `parecer_ia`, `status=CONCLUIDO` e `data_conclusao`.
- **T4.4** Mapear falhas para `status=ERRO` com `motivo_erro`.

**DoD:**
- Fluxo completo de análise gera parecer persistido.
- Erros de LLM ficam rastreáveis no banco e logs.

---

#### Épico E5 - Atualização em Tempo Real (US04)
**Objetivo:** Notificar frontend sem refresh após conclusão/erro.

- **T5.1** Configurar broker STOMP no Spring (`/topic/audits`).
- **T5.2** Publicar evento de domínio após atualização da auditoria.
- **T5.3** Configurar `sockjs-client` + `stompjs` no Angular.
- **T5.4** Atualizar lista no frontend via `BehaviorSubject`.

**DoD:**
- Mudança de status aparece em tempo real no painel.
- Reconexão básica de socket tratada no frontend.

---

#### Épico E6 - Resiliência e Operação (US05, US06)
**Objetivo:** Tornar o pipeline robusto para falhas transitórias e operável em produção.

- **T6.1** Implementar retry com backoff para chamadas ao LLM.
- **T6.2** Configurar DLQ (`audit.process.dlq`) para falhas irreversíveis.
- **T6.3** Incrementar `tentativas_processamento` por execução.
- **T6.4** Criar endpoint/manual flow de reprocessamento `POST /api/auditorias/{id}/reprocessar`.
- **T6.5** Adicionar logs estruturados com correlação (`auditId`).
- **T6.6** Expor métricas (`/actuator/prometheus`) de latência, erro e throughput.

**DoD:**
- Falhas recorrentes não perdem mensagem.
- Reprocessamento manual funciona e é auditável.

---

#### Épico E7 - Segurança (US07)
**Objetivo:** Proteger endpoints e acesso a dados.

- **T7.1** Implementar autenticação JWT no Spring Security.
- **T7.2** Restringir endpoints de consulta ao usuário autenticado.
- **T7.3** Aplicar validação de entrada e limites anti-abuso.
- **T7.4** Proteger canal WebSocket (handshake autenticado).

**DoD:**
- Endpoint sem token retorna `401`.
- Usuário autenticado acessa apenas seus recursos.

---

#### Épico E8 - Qualidade, Testes e CI (US08)
**Objetivo:** Garantir evolução segura com cobertura do fluxo crítico.

- **T8.1** Testes unitários de domínio (status e regras de transição).
- **T8.2** Testes de integração backend com Testcontainers (Postgres + RabbitMQ).
- **T8.3** Teste de integração do worker com mock de LLM.
- **T8.4** Teste E2E do fluxo completo (ingestão -> processamento -> websocket).
- **T8.5** Pipeline CI rodando build + testes + quality gate.

**DoD:**
- Pipeline bloqueia merge quando fluxo crítico falha.
- Evidência de teste para caminho feliz e caminho de erro.

---

### 9.3. Dependências Entre Épicos

- E2 depende de E1.
- E3 depende de E2.
- E4 depende de E3.
- E5 depende de E4.
- E6 depende de E3 e E4.
- E7 pode iniciar após E2 (paralelo com E4/E5).
- E8 inicia em paralelo desde E2, mas fecha após E7.

---

### 9.4. Critérios de Pronto por História (Checklist Rápido)

- **US01:** endpoint 202 + validação + persistência `PENDENTE`.
- **US02:** publicação RabbitMQ confirmada + consumo funcional.
- **US03:** parecer IA salvo + `CONCLUIDO` ou `ERRO` com motivo.
- **US04:** atualização em tempo real sem refresh.
- **US05:** logs estruturados + métricas + health/readiness.
- **US06:** retry/backoff + DLQ + reprocessamento manual.
- **US07:** JWT + autorização por recurso + hardening de input.
- **US08:** testes integração/E2E no CI.

---

### 9.5. Plano de Entrega Sugerido (2 semanas)

**Semana 1**
- E1, E2, E3 concluídos.
- E4 iniciado com integração Gemini funcional.

**Semana 2**
- E4 finalizado.
- E5, E6, E7 implementados.
- E8 consolidado com CI e testes E2E.
