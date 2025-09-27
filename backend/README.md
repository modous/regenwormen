# Regenwormen Backend (Spring Boot)

Dit is de backend van het Regenwormen-spel (HBO-ICT, EWA project).  
Frontend (Vue) staat in `/frontend`, backend (Spring Boot) staat in `/backend`.

---

## 📌 Checkpoint 1 – Setup & Health Check

We hebben tot nu toe de basis van het project opgezet, getest en uitgebreid met een eerste endpoint.

---

## ⚙️ Project setup

### Keuzes bij aanmaken
- **Build tool:** Maven  
  *Bekendste, makkelijkste, veel voorbeeldcode.*
- **Java:** 21 (LTS)  
  *Long Term Support, stabiel tot 2030, aanbevolen door Spring Boot.*
- **Packaging:** JAR  
  *Simpel te draaien en te deployen met Docker of server.*
- **Spring Boot:** 3.5.6  
  *Laatste stabiele versie (op dit moment).*
- **Dependencies gekozen:**
    - Spring Web → voor REST API
    - Spring Data JPA → ORM/database toegang
    - Flyway → database migraties
    - H2 Database → in-memory dev database
    - PostgreSQL Driver → productie database
    - Lombok → minder boilerplate code
    - DevTools → auto-restart in dev
    - Spring Boot Starter Test → JUnit5, AssertJ, Mockito

### Project structuur
src/main/java/nl/hva/ewa/regenwormen
RegenwormenApplication.java                 # startpunt
├── api/                                    # controllers (REST endpoints)
├── config/                                 # configuratie (CORS, etc.)
├── domain/                                 # spelregels (komt later)
├── application/                            # services / use-cases (komt later)
└── infrastructure/jpa/                     # persistence / JPA (komt later)
src/main/resources
application.yml
application-dev.yml
application-prod.yml
db/migration/                               # Flyway migraties

### Configuratie

### `application.yml`
Algemene instellingen + default profile = dev:
```yaml
spring:
  application:
    name: regenwormen
  profiles:
    default: dev
server:
  port: 8080
  ````


### Healthcontroller
Uitleg:

@RestController → class levert REST endpoints (JSON).

@RequestMapping("/api") → alle methodes beginnen met /api.

@GetMapping("/ping") → GET-request op /api/ping.

Return waarde → automatisch omgezet naar JSON.

### WebConfig

Uitleg:

Zonder dit blokkeert de browser requests van frontend → backend (CORS error).

Hier geven we expliciet toestemming voor origin http://localhost:5173.

### TO TEST
Run RegenwormenBackendApplication

Ga naar localhost:8080/api/ping to test output should be {"status":"ok","service":"regenwormen"}



application.yml: algemene settings, en default profile = dev.

application-dev.yml: zegt “gebruik H2”.

application-prod.yml: straks “gebruik Postgres”.

Flyway: bij start zoekt het db/migration/*.sql en past ze toe op de actieve DB (nu H2).

(Optioneel) H2 Console:
Ga naar http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:regenwormen

User: sa, Password: leeg

Je ziet GAMES en PLAYERS.
