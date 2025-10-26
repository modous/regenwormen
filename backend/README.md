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


### voor de volgene checkpoint Domain, Game regels, dto , test, controller, service en mockrepo

src
└── main
├── java/nl/hva/ewa/regenwormen
│   ├── api/               → (optioneel) API-specifieke interfaces of externe integraties
│   ├── config/            → Spring Boot configuratieklassen (bijv. security, CORS, data)
│   ├── controller/        → REST controllers die HTTP-verzoeken afhandelen
│   ├── domain/            → Domeinlogica en entiteiten
│   │   ├── dto/           → Data Transfer Objects voor communicatie met de frontend
│   │   └── Enum/          → Enumeraties voor vaste waarden (zoals GameState, DiceType)
│   ├── repository/        → Interfaces en mockrepositories voor dataopslag
│   └── service/           → Businesslogica; verbindt controllers met repositories
└── resources/
└── db/migration/      → Flyway migratiebestanden voor database-initialisatie



## Checkpoint 2 – Domeinlaag, DTO’s en Services

In deze fase hebben we de kern van het spel geïmplementeerd:

Game-entiteit: beheert spelers, tegels, dobbelstenen en spelstatus.

Player-entiteit: houdt naam, score en verzamelde tegels bij.

DTO’s: maken veilige dataoverdracht tussen backend en frontend mogelijk (GameDTO, PlayerDTO, enz.).

Service-laag: bevat spel- en beurtlogica en verbindt controllers met repositories.

Repository-laag: gestart met een MockRepository (later uitbreidbaar naar JPA).

Controller-laag: biedt endpoints voor het aanmaken van games, spelers en beurten.

### Belangrijke REST-endpoints
Methode	Endpoint	Beschrijving
GET	/api/ping	Health check
POST	/api/auth/register	Gebruiker registreren
POST	/api/auth/login	Gebruiker inloggen
GET	/api/lobbies	Alle beschikbare lobbies ophalen
POST	/api/lobbies	Nieuwe lobby aanmaken
POST	/api/lobbies/{id}/join	Speler toevoegen aan lobby
POST	/api/lobbies/{id}/ready	Speler markeren als ready
GET	/api/games/{id}	Game-status ophalen
POST	/api/games/{id}/roll	Dobbelsteen gooien
POST	/api/games/{id}/pickTile	Tegel kiezen

De frontend communiceert via fetch() en JSON met deze REST API.

## Checkpoint 3 – WebSockets & Real-time Functionaliteit

Om polling te vervangen door realtime-updates hebben we een WebSocket-systeem geïmplementeerd.

Belangrijkste componenten

WebSocketConfig: stelt STOMP-over-SockJS in en staat verbindingen toe vanaf http://localhost:5173.

GameWebSocketController: stuurt updates naar alle spelers bij acties zoals gooien, tegel pakken of beurt wisselen.

Turn Timer: houdt automatisch de tijd per speler bij en stuurt meldingen als een beurt bijna voorbij is.

Real-time synchronisatie: alle clients ontvangen direct updates, zonder de pagina te verversen.

Commit-samenvatting

feat: add real-time turn system with WebSocket sync and countdown timer (removed polling)

## Database

Tijdens de ontwikkeling gebruiken we H2 (in-memory).
Voor productie is een PostgreSQL database voorbereid via Flyway-migraties.
De volgende migratiebestanden zijn toegevoegd in src/main/resources/db/migration/.