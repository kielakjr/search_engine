# Search Engine

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-green?logo=springboot)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.x-005571?logo=elasticsearch)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

A full-stack search engine that crawls curated web sources and indexes them into Elasticsearch for full-text search with highlighting, fuzzy matching, autocomplete, and domain filtering. It ships with a React frontend, JWT-based auth, Redis-backed caching and rate limiting, and a full Docker Compose setup.

Built as a portfolio project to demonstrate backend architecture, distributed systems integration, and search infrastructure.

## Table of Contents

- [Architecture](#architecture)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [License](#license)

## Architecture

```
                         +-----------+
                         |  React +  |
                         |   Vite    |
                         +-----+-----+
                               |
                               v
                      +--------+--------+
                      |  Spring Boot    |
                      |  REST API       |
                      |  JWT + Rate     |
                      |  Limit Filter   |
                      +--+----------+---+
                         |          |
              +----------+      +---+----------+
              |                 |              |
      +-------v----+     +------v----+   +-----v-------+
      | PostgreSQL |     | Elastic-  |   |    Redis    |
      |            |     |  search   |   |             |
      | Users      |     | Full-text |   | URL dedup   |
      | Sources    |     |  index    |   | Query cache |
      | Crawl jobs |     | Highlight |   | Rate limit  |
      | History    |     | Suggest   |   | counters    |
      +------------+     +-----------+   +-------------+
```

**Crawl pipeline.** A scheduler triggers a BFS crawl per source. Jsoup fetches pages (respecting `robots.txt` and a configurable request delay), the extracted content is indexed into Elasticsearch, and visited URLs are cached in Redis to prevent re-indexing across crawl cycles. Multiple sources can be crawled in parallel through a dedicated async thread pool.

**Request pipeline.** Requests first hit a Redis-backed rate limit filter, then Spring Security's JWT filter resolves the current user. Search results and autocomplete suggestions are cached in Redis (TTL 5m) so repeated queries hit Elasticsearch at most once per window.

## Features

### Crawling

- **BFS crawler** with configurable max depth and max pages per source
- **Robots.txt compliance** — parses `User-agent: *` disallow rules with path-prefix matching
- **Politeness delay** between requests to avoid overwhelming target servers
- **Async crawling** via a dedicated thread pool for parallel source processing
- **Scheduled re-crawling** at a configurable interval
- **URL deduplication** — Redis set with TTL across crawl cycles, SHA-256 hashed document IDs to prevent duplicates in Elasticsearch
- **Crawl job lifecycle** tracking: `PENDING -> RUNNING -> COMPLETED / FAILED`

### Search

- **Full-text search** across `title` (boosted 3x) and `content` with Elasticsearch
- **Hybrid exact + fuzzy matching** — exact matches are boosted, fuzzy matches (edit distance 1) catch typos
- **Highlighted snippets** returned with each search hit
- **Autocomplete / suggestions** via `match_phrase_prefix` on titles
- **Domain filtering** to narrow results to a specific source
- **Pagination** with configurable page size and offset
- **Redis query cache** (5 min TTL) for search results and suggestions

### Auth & API

- **JWT authentication** (stateless) with role-based authorization (`USER` / `ADMIN`)
- **BCrypt password hashing**
- **Rate limiting** — 20 requests / 60s per IP via a Redis-backed servlet filter
- **Health endpoint** reporting overall + dependency status (Postgres / Elasticsearch / Redis)
- **OpenAPI / Swagger UI** served by springdoc
- **Global exception handler** with structured error responses
- **Bean Validation** on all public request parameters
- **CORS** configured for local dev and Docker deployment

### Frontend

- **React 19 + TypeScript + Vite**
- Search page with **live autocomplete** (keyboard navigable) and **domain filter**
- **Login / register** flow with token persistence
- **Search history** view per user
- **Admin panel** — manage sources, trigger crawls, monitor job status
- Modern dark theme UI with responsive layout

## Tech Stack

| Layer      | Technology                            | Purpose                                             |
| ---------- | ------------------------------------- | --------------------------------------------------- |
| Backend    | Java 25, Spring Boot 4.0.5            | REST API, business logic                            |
| Search     | Elasticsearch 9.x                     | Full-text indexing, highlighting, fuzzy, suggest    |
| Database   | PostgreSQL 17                         | Users, sources, crawl jobs, search history          |
| Cache      | Redis 7                               | URL dedup, query cache, rate limit counters         |
| Auth       | Spring Security + jjwt 0.13           | JWT authentication, role-based authorization        |
| Crawler    | Jsoup 1.21                            | HTTP fetching and HTML parsing                      |
| API docs   | springdoc-openapi                     | OpenAPI 3 spec + Swagger UI                         |
| Frontend   | React 19, TypeScript, Vite            | Single-page UI                                      |
| Testing    | JUnit 5, Testcontainers, MockMvc      | Unit + integration tests with real infra containers |
| Infra      | Docker, Docker Compose, Nginx         | Full-stack orchestration + frontend serving        |

## API Reference

Interactive API docs are available at `/swagger-ui.html` once the backend is running.

### Authentication

| Method | Endpoint             | Auth   | Description                  |
| ------ | -------------------- | ------ | ---------------------------- |
| POST   | `/api/auth/register` | Public | Register a new user          |
| POST   | `/api/auth/login`    | Public | Login and receive JWT token  |

### Sources

| Method | Endpoint             | Auth          | Description          |
| ------ | -------------------- | ------------- | -------------------- |
| GET    | `/api/sources`       | Authenticated | List all sources     |
| POST   | `/api/sources`       | ADMIN         | Add a new source     |
| DELETE | `/api/sources/{id}`  | ADMIN         | Delete a source      |

### Crawler

| Method | Endpoint                         | Auth          | Description                |
| ------ | -------------------------------- | ------------- | -------------------------- |
| POST   | `/api/crawler/start/{sourceId}`  | ADMIN         | Trigger crawl for a source |
| GET    | `/api/crawler/jobs`              | Authenticated | List all crawl jobs        |

### Search

| Method | Endpoint                                    | Auth          | Description                                   |
| ------ | ------------------------------------------- | ------------- | --------------------------------------------- |
| GET    | `/search?query=&page=&size=&domain=`        | Public        | Full-text search with optional domain filter  |
| GET    | `/search/suggest?prefix=`                   | Public        | Autocomplete suggestions from indexed titles  |
| GET    | `/search/history`                           | Authenticated | Current user's search history                 |
| GET    | `/search/history/{userId}`                  | ADMIN         | Search history for a specific user            |

### Health

| Method | Endpoint       | Auth   | Description                                               |
| ------ | -------------- | ------ | --------------------------------------------------------- |
| GET    | `/api/health`  | Public | Aggregated status of Postgres, Elasticsearch, and Redis   |

## Getting Started

### Prerequisites

- Java 25
- Docker & Docker Compose
- Node.js 22+ (for running the frontend outside Docker)

### Run with Docker Compose (recommended)

1. Clone the repository:

   ```bash
   git clone https://github.com/mkielak/search-engine.git
   cd search-engine
   ```

2. Create a `.env` file in the project root:

   ```env
   DB_NAME=search_engine
   DB_USER=postgres
   DB_PASSWORD=postgres
   JWT_SECRET=<your-long-random-secret>
   ```

3. Start all services:

   ```bash
   docker compose up --build
   ```

4. Open the app:
   - Frontend: http://localhost
   - Backend API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

### Run locally (development)

1. Start infrastructure containers only:

   ```bash
   docker compose up postgres elasticsearch redis
   ```

2. Run the backend:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Run the frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. Open http://localhost:5173 — the Vite dev server proxies API calls to `:8080`.

### Creating an admin user

Users are created with the `USER` role by default. To promote a user to `ADMIN`, connect to Postgres and run:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## Configuration

### Environment variables

| Variable      | Description                        |
| ------------- | ---------------------------------- |
| `DB_NAME`     | PostgreSQL database name           |
| `DB_USER`     | PostgreSQL username                |
| `DB_PASSWORD` | PostgreSQL password                |
| `JWT_SECRET`  | Secret key for signing JWT tokens  |

### Application properties

Defined in `src/main/resources/application.properties`:

| Property                         | Description                             | Default              |
| -------------------------------- | --------------------------------------- | -------------------- |
| `app.crawler.max-depth`          | Maximum BFS crawl depth                 | `4`                  |
| `app.crawler.max-pages`          | Maximum pages per crawl                 | `1000`               |
| `app.crawler.request-delay-ms`   | Delay between requests (ms)             | `1000`               |
| `app.crawler.crawl-interval-ms`  | Scheduled crawl interval (ms)           | `21600000` (6h)      |
| `app.crawler.initial-delay-ms`   | Delay before first scheduled crawl (ms) | `10000`              |
| `app.jwt.expiration-ms`          | JWT token expiration (ms)               | `604800000` (7 days) |

Rate limit (hard-coded in `RateLimitFilter`): **20 requests / 60 seconds per IP**.
Redis query cache TTL (in `CacheConfig`): **5 minutes**.

## Testing

The project includes both unit tests and integration tests backed by **Testcontainers** — real PostgreSQL, Redis, and Elasticsearch containers spin up for each integration test run, so what's tested matches what ships.

```bash
./mvnw test
```

## Project Structure

```
src/main/java/com/kielakjr/search_engine/
├── auth/                    # Authentication & authorization
│   ├── AuthController       # Login / register endpoints
│   ├── AuthService          # Registration, password hashing, JWT generation
│   ├── JwtService           # Token creation, validation, claims extraction
│   ├── JwtAuthFilter        # HTTP filter for JWT-based auth
│   ├── User                 # JPA entity
│   └── Role                 # USER, ADMIN enum
├── crawler/                 # Web crawling engine
│   ├── CrawlerController    # Manual crawl trigger, job listing
│   ├── CrawlerService       # BFS crawl, robots.txt, Redis dedup
│   ├── CrawlScheduler       # Scheduled re-crawling
│   ├── CrawlJob             # JPA entity with status tracking
│   ├── JsoupFetcher         # HTTP fetch abstraction (testable)
│   └── DefaultJsoupFetcher
├── search/                  # Search & indexing
│   ├── SearchController     # Search, suggest, history endpoints
│   ├── SearchService        # Elasticsearch queries, highlighting, suggest
│   ├── SearchHistoryService
│   ├── PageDocument         # Elasticsearch document mapping
│   └── SearchHistory        # JPA entity
├── source/                  # Crawl source management
│   ├── SourceController     # CRUD endpoints
│   ├── SourceService
│   └── Source               # JPA entity
├── health/                  # Health check
│   ├── HealthController     # /api/health endpoint
│   └── HealthService        # Dependency status aggregation
└── config/                  # Application configuration
    ├── SecurityConfig       # Spring Security, CORS, JWT filter chain
    ├── RateLimitFilter      # Redis-backed per-IP rate limiting
    ├── CacheConfig          # Redis cache manager for @Cacheable
    ├── AsyncConfig          # Thread pool for async crawling
    ├── OpenApiConfig        # Swagger UI / OpenAPI metadata
    ├── CrawlerProperties    # Externalized crawler settings
    ├── JwtProperties        # Externalized JWT settings
    └── GlobalExceptionHandler

frontend/src/
├── api.ts                      # Fetch-based API client
├── types.ts                    # TypeScript types matching backend DTOs
├── context/AuthContext.tsx     # Auth state with localStorage persistence
├── components/Navbar.tsx       # Navigation with role-aware links
└── pages/
    ├── SearchPage.tsx          # Search with autocomplete, pagination, domain filter
    ├── LoginPage.tsx           # Login / register
    ├── HistoryPage.tsx         # Search history table
    └── AdminPage.tsx           # Source management, crawl triggers, job status
```

## License

This project is for portfolio / educational purposes.
