# Shortly - URL Shortener Architecture

Shortly is a high-traffic URL shortening system designed using a microservices and event-driven architecture. The system prioritizes high performance, scalability, and availability.

<img width="1031" height="571" alt="shortly-arstecture1" src="https://github.com/user-attachments/assets/91b63dbb-1dbf-47fd-9298-53187c3b10a1" />

## Core Components

### 1. API Service (Go + Fiber)

**Function:** Main entry point for clients.

**Features:**

* Authentication & API key validation
* Bulk URL shortening
* QR code generation
* Expiring URLs support

**Communication:** REST API

---

### 2. Redis Cache (DB 1)

**Function:** Stores `ShortURL → LongURL` mappings for fast lookups.

**Features:**

* User profile caching
* 24-hour TTL

**Purpose:** Reduces load on PostgreSQL.

---

### 3. PostgreSQL

**Function:** Primary relational data store.

**Stored Data:**

* Users
* URLs
* API keys & quotas
* Expiry metadata

**Purpose:** Ensures data consistency and reliability.

---

### 4. Key Generation Service (KGS) – gRPC

**Function:** Generates unique keys for short URLs.

**Techniques:**

* Base62 encoding
* Collision-free generation

**Database:** MongoDB

**Responsibilities:**

* Key history tracking
* Key state management (used / available)

---

### 5. Redis Queue (DB 0)

**Function:** Stores pre-generated keys ready for immediate use.

**Mechanism:**

* `LPUSH` / `RPOP`

**Purpose:**

* Speeds up URL shortening by eliminating real-time key generation.

---

### 6. Analytics Processor

**Function:** Processes analytics data asynchronously.

**Technology:** Go routines

**Processed Data:**

* User device & OS
* Geolocation (GeoIP)

**Purpose:**

* Prevents blocking the main request flow.

---

### 7. Redis Rate Limiter (DB 2)

**Function:** Limits requests per IP or API key.

**Mechanism:**

* Distributed counter

**Purpose:**

* Prevents abuse and excessive usage.

---

### 8. Redis HLL Counters

**Function:** Tracks unique visitors in real time.

**Technology:** HyperLogLog (HLL)

**Purpose:**

* Memory-efficient unique visitor estimation.

---

## Basic Workflow

### URL Shortening Flow

1. Client sends a request to the API Service.
2. API Service checks the rate limiter.
3. API Service fetches a pre-generated key from Redis Queue.
4. The `ShortURL → LongURL` mapping is stored in:

   * Redis Cache
   * PostgreSQL
5. Analytics data is processed asynchronously.

### URL Redirection Flow

1. Client accesses the short URL.
2. API Service looks up the key in Redis Cache.
3. If found → immediate redirect.
4. If not found → query PostgreSQL, then cache the result.
5. Analytics and HLL counters are updated asynchronously.

### Key Generation Flow

1. KGS generates new keys.
2. Keys are stored and tracked in MongoDB.
3. Keys are pushed to Redis Queue for consumption.

---

## Architecture Advantages

* **High Availability**

  * Redis cache and queue ensure fast responses.

* **Scalability**

  * API Service and KGS can be horizontally scaled.

* **Fault Tolerance**

  * MongoDB persists key state.
  * Redis acts as a temporary high-speed buffer.

* **Real-time Analytics**

  * HyperLogLog enables efficient unique visitor tracking.

* **Asynchronous Processing**

  * Analytics does not block critical request paths.

---

## Tech Stack

| Component             | Technology          |
| --------------------- | ------------------- |
| API Framework         | Go + Fiber          |
| Relational Database   | PostgreSQL          |
| Cache & Queue         | Redis               |
| Key Storage           | MongoDB             |
| Service Communication | gRPC                |
| Deployment            | Docker / Kubernetes |

---

## Notes

This architecture is ideal for large-scale systems that require:

* Low latency
* High throughput
* Strong consistency for critical components such as **key generation** and **quota management**

---

**Shortly Architecture** is built with *performance-first*, *scalability*, and *resilience* principles.

