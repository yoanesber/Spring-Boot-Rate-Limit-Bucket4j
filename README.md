# Rate Limiting with Bucket4j and Hazelcast

## 📖 Overview
This project implements a **rate-limiting** mechanism for an API service using **Bucket4j** and **Hazelcast**. The rate limiter is used to control excessive requests within a given time period to ensure the stability and security of the API service.  

### 🗂️ Hazelcast
**Hazelcast** is an in-memory data grid used to store data in a distributed structure. It provides **a fast, scalable, and highly available data storage solution**. Hazelcast is well-suited for **distributed caching, session clustering, and real-time data processing**. In this project, Hazelcast is utilized to store rate-limiting data in a distributed map, ensuring efficient and scalable rate-limiting operations across multiple instances of the application.  

### 🛢️ Bucket4j
**Bucket4j** is a token-bucket-based Java library that enables fine-grained rate limiting. It operates by maintaining a **bucket of tokens** that replenish over time. Each request consumes a token, and when the bucket is empty, further requests are denied or delayed until tokens are refilled. This mechanism ensures controlled request flow and prevents API abuse. In this project, Bucket4j is integrated with Hazelcast to provide a distributed and efficient rate-limiting solution, enabling request throttling across multiple instances of the service.  

---

## 🤖 Tech Stack
The technology used in this project are:  
- `Spring Boot Starter Web` – Building RESTful APIs or web applications
- `bucket4j` – A token-bucket-based rate limiting library
- `hazelcast` – In-memory data grid for storing maps supporting rate limiting
- `Lombok` – Reducing boilerplate code
---

## 🏗️ Project Structure
The project is organized into the following package structure:  
```bash
rate-limit-with-bucket4j/
│── src/main/java/com/yoanesber/rate_limit_with_bucket4j/
│   ├── 📂config/                # Hazelcast Configuration
│   ├── 📂controller/            # Contains REST controllers that handle HTTP requests and return responses
│   ├── 📂entity/                # Contains entity classes
│   ├── 📂service/               # Business logic layer
│   │   ├── 📂impl/              # Implementation of services
```
---

## ⚙ Environment Configuration
Configuration values are stored in `.env.development` and referenced in `application.properties`.  
Example `.env.development` file content:  
```properties
# Application properties
APP_PORT=8081
SPRING_PROFILES_ACTIVE=development

# Hazelcast Configurations
HAZELCAST_MAP_CONFIG_RATE_LIMIT_NAME=rate-limit-map                     # Name of the map configuration
HAZELCAST_MAP_CONFIG_RATE_LIMIT_TTL=3600                                # Time-to-live for rate limiting (1 hour)
HAZELCAST_MAP_CONFIG_RATE_LIMIT_BACKUP_COUNT=1                          # setBackupCount(n) ensures n backup copies exist across Hazelcast nodes
HAZELCAST_MAP_CONFIG_RATE_LIMIT_EVICTION_POLICY=LRU                     # Eviction policy (LRU, LFU, NONE)
HAZELCAST_MAP_CONFIG_RATE_LIMIT_MAX_SIZE_POLICY=USED_HEAP_PERCENTAGE    # Max size policy (USED_HEAP_PERCENTAGE, USED_HEAP_SIZE, FREE_HEAP_PERCENTAGE, FREE_HEAP_SIZE)
HAZELCAST_MAP_CONFIG_RATE_LIMIT_EVICTION_SIZE=80                        # Max size of the map; Eviction triggers at 80% of heap usage; Use USED_HEAP_PERCENTAGE (e.g., 80%) to avoid OutOfMemoryError
HAZELCAST_MAP_CONFIG_RATE_LIMIT_MERGE_POLICY=PutIfAbsentMergePolicy     # Merge policy for partitions; It is used to merge two entries in case of a conflict (e.g., two nodes updating the same entry)

# Rate Limiter Configurations
RATE_LIMITER_
RATE_LIMITER_EVICTION_JITTER=5                                          # Jitter factor to avoid the thundering herd problem
RATE_LIMITER_TOKEN_LIMIT_CAPACITY=5                                     # Maximum number of tokens that can be stored in the rate limiter
RATE_LIMITER_TOKEN_REFILL_AMOUNT=5                                      # Number of tokens to be refilled in the rate limiter
RATE_LIMITER_TOKEN_REFILL_PERIOD=1                                      # Time period (in minutes) to refill the tokens
```

Example `application.properties` file content:  
```properties
# Application properties
spring.application.name=rate-limit-with-bucket4j
server.port=${APP_PORT}
spring.profiles.active=${SPRING_PROFILES_ACTIVE}

# Hazelcast Configurations
hazelcast.map.config.rate-limit.name=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_NAME}
hazelcast.map.config.rate-limit.ttl=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_TTL}
hazelcast.map.config.rate-limit.backup.count=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_BACKUP_COUNT}
hazelcast.map.config.rate-limit.eviction.policy=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_EVICTION_POLICY}
hazelcast.map.config.rate-limit.max.size.policy=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_MAX_SIZE_POLICY}
hazelcast.map.config.rate-limit.eviction.size=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_EVICTION_SIZE}
hazelcast.map.config.rate-limit.merge.policy=${HAZELCAST_MAP_CONFIG_RATE_LIMIT_MERGE_POLICY}

# Rate Limiter Configurations
rate.limiter.eviction-jitter=${RATE_LIMITER_EVICTION_JITTER}
rate.limiter.token.limit-capacity=${RATE_LIMITER_TOKEN_LIMIT_CAPACITY}
rate.limiter.token.refill.amount=${RATE_LIMITER_TOKEN_REFILL_AMOUNT}
rate.limiter.token.refill.period=${RATE_LIMITER_TOKEN_REFILL_PERIOD}
```
---

## 🛠️ Installation & Setup
A step by step series of examples that tell you how to get a development env running.  
1. Clone the repository
```bash
git clone https://github.com/yoanesber/Spring-Boot-Rate-Limit-Bucket4j.git
cd Spring-Boot-Rate-Limit-Bucket4j
```

2. Run the application locally
Make sure PostgreSQL is running, then execute: 
```bash
mvn spring-boot:run
```

3. The API will be available at:
```bash
http://localhost:8081/ 
```
---

## 🌐 API Endpoints
The API provides the following endpoints to manage department data. Each endpoint follows RESTful conventions and operates on the /departments resource. The base URL for all endpoints is `http://localhost:8081`.  

### Find All Departments
`GET` http://localhost:8081/api/v1/departments  

### Find Department by ID
`GET` http://localhost:8081/api/v1/departments/{id}  

**Successful Response:**
```json
{
    "statusCode": 200,
    "timestamp": "2025-03-20T08:47:10.730217400Z",
    "message": "Department retrieved successfully",
    "data": {
        "id": "d011",
        "deptName": "Operation",
        "active": true,
        "createdBy": 1001,
        "createdDate": "2025-03-20T10:00:00",
        "updatedBy": 1001,
        "updatedDate": "2025-03-20T10:00:00"
    }
}
```

### Too Many Requests Response (Rate Limiting)
If the number of requests exceeds the allowed limit within a given time frame, the API returns:  

```json
{
    "statusCode": 429,
    "timestamp": "2025-03-22T05:31:46.242425700Z",
    "message": "Too many requests",
    "data": null
}
```
---

## 🔗 Related Repositories
- Rate Limit with Redis GitHub Repository, check out [Department REST API with Redis Cache and Rate Limiting](https://github.com/yoanesber/Spring-Boot-Rate-Limit-Redis).
- Rate Limit with Kong GitHub Repository, check out [Spring Boot Department API with Kong API Gateway & Rate Limiting](https://github.com/yoanesber/Spring-Boot-Rate-Limit-Kong).