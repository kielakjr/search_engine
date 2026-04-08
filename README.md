# Search Engine

![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-green?logo=springboot)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-9.x-005571?logo=elasticsearch)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)

A full-stack search engine that crawls curated tech news sources (Hacker News, dev.to, Stack Overflow) and indexes them into Elasticsearch for full-text search with highlighting, fuzzy matching, and domain filtering.

Built as a portfolio project to demonstrate backend architecture, distributed systems integration, and search infrastructure.

## Architecture Overview

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
                      |  + JWT Auth     |
                      +--+----------+---+
                         |          |
              +----------+      +---+----------+
              |                 |              |
      +-------v---+      +------v----+   +-----v-------+
      | PostgreSQL |     |Elasticsea-|   |    Redis    |
      |            |     |   rch     |   |             |
      | Users      |     | Full-text |   |    URL      |
      | Sources    |     |  index    |   | dedup cache |
      | Crawl jobs |     |           |   |             |
      | History    |     |           |   |             |
      +------------+     +-----------+   +-------------+
```

**Crawl pipeline:** Scheduler triggers BFS crawl per source -> Jsoup fetches pages -> content extracted and indexed into Elasticsearch -> visited URLs cached in Redis to prevent duplicates across crawl cycles.

## Features

- **BFS Web Crawler** - Domain-filtered crawling with configurable max depth and page limits
- **Robots.txt Compliance** - Parses and respects `User-agent: *` disallow rules with path prefix matching
- **Crawl Politeness** - Configurable delay between requests to avoid overwhelming target servers
- **Async Crawling** - Dedicated thread pool enables parallel crawling of multiple sources
- **Scheduled Re-crawling** - Sources are automatically re-crawled at a configurable interval
- **URL Deduplication** - Redis set with TTL prevents re-indexing the same pages across crawl cycles; SHA-256 hashed document IDs prevent Elasticsearch duplicates
- **Full-text Search** - Multi-field search across title (boosted 3x) and content with Elasticsearch
- **Fuzzy Search** - Tolerates typos with configurable fuzziness
- **Highlighting** - Search results include highlighted matching snippets
- **Domain Filtering** - Filter search results by source domain
- **Pagination** - Configurable page size and offset for search results
- **JWT Authentication** - Stateless auth with role-based access control (USER / ADMIN)
- **Search History** - Tracks queries per authenticated user
- **Crawl Job Tracking** - Full status lifecycle: PENDING -> RUNNING -> COMPLETED / FAILED
- **CORS Configuration** - Configured for frontend development and Docker deployment

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Backend | Java 25, Spring Boot 4.0.5 | REST API, business logic |
| Search | Elasticsearch 9.x | Full-text indexing, highlighting, fuzzy search |
| Database | PostgreSQL 17 | Users, sources, crawl jobs, search history |
| Cache | Redis 7 | URL deduplication between crawl cycles |
| Auth | Spring Security + JWT (jjwt 0.13.0) | Authentication, role-based authorization |
| Crawler | Jsoup 1.21.2 | HTML fetching and parsing |
| Frontend | React 19 + Vite + TypeScript | UI (in progress) |
| Infra | Docker Compose | Full-stack orchestration |

## API Endpoints

### Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Login and receive JWT token |

### Sources

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/sources` | Authenticated | List all sources |
| POST | `/api/sources` | ADMIN | Add a new source |
| DELETE | `/api/sources/{id}` | ADMIN | Delete a source |

### Crawler

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/crawler/start/{sourceId}` | ADMIN | Trigger crawl for a source |
| GET | `/api/crawler/jobs` | Authenticated | List all crawl jobs |

### Search

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/search?query=&page=&size=&domain=` | Public | Full-text search with optional domain filter |
| GET | `/search/history` | Authenticated | Current user's search history |
| GET | `/search/history/{userId}` | ADMIN | Search history for a specific user |

## Getting Started

### Prerequisites

- Java 25
- Docker & Docker Compose
- Node.js 22+ (for frontend)

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
JWT_SECRET=<your-secret-key>
```

3. Start all services:

```bash
docker compose up --build
```

4. Access the application:
   - Frontend: http://localhost
   - Backend API: http://localhost:8080

### Run Locally (development)

1. Start infrastructure services:

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

4. Access at http://localhost:5173 (Vite dev server proxies API calls to `:8080`)

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_NAME` | PostgreSQL database name | - |
| `DB_USER` | PostgreSQL username | - |
| `DB_PASSWORD` | PostgreSQL password | - |
| `JWT_SECRET` | Secret key for signing JWT tokens | - |

### Application Configuration (`application.properties`)

| Property | Description | Default |
|----------|-------------|---------|
| `app.crawler.max-depth` | Maximum BFS crawl depth | `3` |
| `app.crawler.max-pages` | Maximum pages per crawl | `100` |
| `app.crawler.request-delay-ms` | Delay between requests (ms) | `1000` |
| `app.crawler.crawl-interval-ms` | Scheduled crawl interval (ms) | `21600000` (6h) |
| `app.crawler.initial-delay-ms` | Delay before first scheduled crawl (ms) | `10000` |
| `app.jwt.expiration-ms` | JWT token expiration (ms) | `604800000` (7d) |

## Project Structure

```
src/main/java/com/kielakjr/search_engine/
├── auth/                  # Authentication & authorization
│   ├── AuthController     # Login / register endpoints
│   ├── AuthService        # Registration, password hashing, JWT generation
│   ├── JwtService         # Token creation, validation, claims extraction
│   ├── JwtAuthFilter      # HTTP filter for JWT-based auth
│   ├── User               # JPA entity
│   └── Role               # USER, ADMIN enum
├── crawler/               # Web crawling engine
│   ├── CrawlerController  # Manual crawl trigger, job listing
│   ├── CrawlerService     # BFS crawl logic, robots.txt, Redis dedup
│   ├── CrawlScheduler     # Scheduled re-crawling
│   ├── CrawlJob           # JPA entity with status tracking
│   ├── JsoupFetcher       # HTTP fetch abstraction (testable)
│   └── DefaultJsoupFetcher
├── search/                # Search & indexing
│   ├── SearchController   # Search + history endpoints
│   ├── SearchService      # Elasticsearch queries, highlighting
│   ├── SearchHistoryService
│   ├── PageDocument       # Elasticsearch document mapping
│   └── SearchHistory      # JPA entity
├── source/                # Crawl source management
│   ├── SourceController   # CRUD endpoints
│   ├── SourceService
│   └── Source             # JPA entity
└── config/                # Application configuration
    ├── SecurityConfig     # Spring Security, CORS, JWT filter chain
    ├── AsyncConfig        # Thread pool for async crawling
    ├── CrawlerProperties  # Externalized crawler settings
    ├── JwtProperties      # Externalized JWT settings
    └── GlobalExceptionHandler

frontend/src/
├── api.ts                 # Fetch-based API client
├── types.ts               # TypeScript types matching backend DTOs
├── context/AuthContext.tsx # Auth state with localStorage persistence
├── components/Navbar.tsx  # Navigation with role-aware links
└── pages/
    ├── SearchPage.tsx     # Search with pagination and domain filter
    ├── LoginPage.tsx      # Login / register
    ├── HistoryPage.tsx    # Search history table
    └── AdminPage.tsx      # Source management, crawl triggers, job status
```

## Screenshots

> Coming soon

## License

This project is for portfolio/educational purposes.
