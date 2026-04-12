# Master Migration Context

## Project Overview
Migrating 5 separate Java backend projects + 1 frontend project
into 2 new unified projects.

## Source Projects
| Project              | Technology          | Purpose              |
|----------------------|---------------------|----------------------|
| ThorsteinPlatform            | Spring 4 + Hibernate 5 + MySQL 5.7 | Platform/Auth/User |
| ThorsteinFinance             | Spring 4 + Hibernate 5 + MySQL 5.7 | Finance Module     |
| ThorsteinLogistics           | Spring 4 + Hibernate 5 + MySQL 5.7 | Logistics Module   |
| ThorsteinSalesDistribution   | Spring 4 + Hibernate 5 + MySQL 5.7 | Sales Module       |
| ThorsteinProduction          | Spring 4 + Hibernate 5 + MySQL 5.7 | Production Module  |
| ThorSalesDistributionUI | HTML5 + Vue 2       | Sales Frontend     |

> All 5 backend projects shared a single MySQL database.
> Table names = Java class names (camelCase). Column names = Java field names (camelCase).

## Target Projects
| Project           | Technology                    | Purpose         | Location |
|-------------------|-------------------------------|-----------------|----------|
| IntelligentPlatform | Spring Boot 3.2 + Hibernate 6 + MySQL 8 + Java 17 | All Backend | `/Users/I043125/work2/IntelligentPlatform` |
| IntelligentServiceUI | Vue 3 + Vite + TypeScript + Pinia | All Frontend | `/Users/I043125/work2/IntelligentServiceUI` |

---

## How to Run the Full System

### Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java (JDK) | 17 | Use sapmachine-jdk-17 on macOS |
| Maven | 3.8+ | |
| MySQL | 8.0+ | |
| Node.js | 20+ | Tarball at `/Users/I043125/SAPDevelop/tools/node-v20.18.0-darwin-arm64` |

---

### Step 1 — Set up environment variables

```bash
# Java
export JAVA_HOME=/Library/Java/JavaVirtualMachines/sapmachine-jdk-17.0.2.jdk/Contents/Home

# Node.js 20
export PATH="/Users/I043125/SAPDevelop/tools/node-v20.18.0-darwin-arm64/bin:$PATH"
```

Tip: add both lines to `~/.zshrc` so you don't have to repeat them.

---

### Step 2 — Create MySQL databases (once only)

```sql
-- Run as MySQL root
CREATE DATABASE IF NOT EXISTS platform;
CREATE DATABASE IF NOT EXISTS finance;
CREATE DATABASE IF NOT EXISTS logistics;
CREATE DATABASE IF NOT EXISTS sales;
CREATE DATABASE IF NOT EXISTS production;

CREATE USER IF NOT EXISTS 'ip_user'@'localhost' IDENTIFIED BY 'ip_password';
GRANT ALL PRIVILEGES ON platform.*   TO 'ip_user'@'localhost';
GRANT ALL PRIVILEGES ON finance.*    TO 'ip_user'@'localhost';
GRANT ALL PRIVILEGES ON logistics.*  TO 'ip_user'@'localhost';
GRANT ALL PRIVILEGES ON sales.*      TO 'ip_user'@'localhost';
GRANT ALL PRIVILEGES ON production.* TO 'ip_user'@'localhost';
FLUSH PRIVILEGES;
```

Then import the legacy table dump into each schema.

---

### Step 3 — Start the backend

```bash
cd /Users/I043125/work2/IntelligentPlatform
mvn spring-boot:run
```

Backend starts on **http://localhost:8080**.

Verify login works:

```bash
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"userId":"i00101","client":"001","password":"654321"}'
```

Expected: HTTP 200 with a `token` field.

---

### Step 4 — Start the frontend

```bash
cd /Users/I043125/work2/IntelligentServiceUI
npm run dev
```

Frontend starts on **http://localhost:3000** and proxies `/api/*` to the backend automatically.

Open http://localhost:3000 in your browser and log in.

---

### Running both as background processes

```bash
# Terminal 1 — backend
cd /Users/I043125/work2/IntelligentPlatform
nohup mvn spring-boot:run > /tmp/ip-backend.log 2>&1 &
tail -f /tmp/ip-backend.log

# Terminal 2 — frontend
cd /Users/I043125/work2/IntelligentServiceUI
npm run dev
```

---

## Key Migration Rules
1. Merge all 5 backends into Modular Monolith structure
2. ThorsteinPlatform becomes the common/ module (auth, user, security)
3. Each other project becomes its own module
4. No cross-module Foreign Keys in DB
5. Communicate between modules via Spring Events
6. All APIs follow /api/v1/{module}/{resource} pattern
7. Unified JWT authentication from ThorsteinPlatform
8. Flyway manages all DB migrations

## Non-Functional Requirements
- Java 17 LTS
- Spring Boot 3.2.x
- MySQL 8.x
- Redis for caching
- Swagger/OpenAPI documentation
- JWT stateless authentication