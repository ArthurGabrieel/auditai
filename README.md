# AuditAI

MVP para validação de arquitetura full stack com processamento assíncrono e IA:
- Backend: Spring Boot + PostgreSQL + RabbitMQ + WebSocket
- Frontend: Angular 16 + RxJS

## Estrutura

- `backend/`: API e worker
- `frontend/`: painel Angular
- `infra/`: `docker-compose.yml`
- `docs/`: documentação complementar

## Pré-requisitos

- Docker e Docker Compose
- Java 25+
- Maven 3.9+
- Node 18+ e npm 9+

## Subir infraestrutura

```bash
cp .env.example .env
docker compose -f infra/docker-compose.yml --env-file .env up -d
```

RabbitMQ management: `http://localhost:15672` (`guest` / `guest`)

## Rodar backend local

```bash
cd backend
mvn spring-boot:run
```

Healthcheck: `http://localhost:8080/actuator/health`

## Rodar frontend local

```bash
cd frontend
npm install
npm run start
```

App: `http://localhost:4200`
