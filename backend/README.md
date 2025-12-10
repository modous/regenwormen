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


## Supabase intergratie

Een persistente database opzetten voor gebruikers (register & login), zodat:
- gebruikers blijven bestaan na een server restart
- we Spring Boot + JPA blijven gebruiken
- H2 alleen optioneel is (niet meer leidend)
- later eenvoudig security (JWT) toegevoegd kan worden
- We gebruiken Supabase PostgreSQL als database en JPA/Hibernate als ORM.

✅ Gekozen Architectuur

Stack

- Spring Boot 3
- Spring Data JPA
- PostgreSQL (Supabase)
- Hibernate ORM
- BCrypt voor wachtwoord-hashing

Waarom deze keuze?

- JPA maakt database-onafhankelijk werken mogelijk
- Supabase levert een gratis, beheerde PostgreSQL database
- Data blijft opgeslagen na server restarts
- Compatibel met latere uitbreidingen (JWT, roles, permissions)

🛠️ Database Configuratie
application.yml

H2 is verwijderd als primaire database.
PostgreSQL (Supabase) wordt nu gebruikt.

spring:
application:
name: regenwormen

datasource:
url: jdbc:postgresql://db.<SUPABASE_ID>.supabase.co:5432/postgres
username: postgres
password: <SUPABASE_PASSWORD>

jpa:
hibernate:
ddl-auto: update
show-sql: true

server:
port: 8080

Belangrijke instellingen

ddl-auto: update
→ Hibernate maakt/updated tabellen automatisch op basis van entities

show-sql: true
→ SQL queries zijn zichtbaar in de console

🧱 UserEntity (Database Model)

De gebruiker wordt opgeslagen als een JPA Entity.

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    private String location;

    private String profilePictureUrl;
}

Design-keuzes

id is een UUID (String)

email en username zijn uniek

wachtwoord wordt gehashed opgeslagen

extra velden voorbereid voor toekomstige features

🗂️ Repository Layer

Spring Data JPA repository voor database-interactie:

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}


Geen SQL nodig — JPA genereert queries automatisch.

🔐 AuthService (Business Logic)

Verantwoordelijk voor:

registreren

inloggen

validatie

password hashing

Registratie
public UserEntity register(String email, String username, String password) {
if (userRepository.existsByEmail(email)) {
throw new IllegalArgumentException("Email already registered");
}
if (userRepository.existsByUsername(username)) {
throw new IllegalArgumentException("Username already taken");
}

    UserEntity user = new UserEntity(
        UUID.randomUUID().toString(),
        email,
        passwordEncoder.encode(password),
        username,
        null,
        null
    );

    return userRepository.save(user);
}

Login
public UserEntity login(String identifier, String password) {
UserEntity user = userRepository.findByEmail(identifier)
.or(() -> userRepository.findByUsername(identifier))
.orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new IllegalArgumentException("Invalid credentials");
    }

    return user;
}

🌐 AuthController (API)

Endpoints:

POST /api/auth/register

POST /api/auth/login

Register request (JSON)
{
"email": "test@supabase.com",
"username": "mohamed",
"password": "123456"
}

Succes-response
{
"id": "5309dc2c-88a8-41c0-add5-6eec2a355cd5",
"email": "test@supabase.com",
"password": "$2a$10$...",
"username": "mohamed",
"location": null,
"profilePictureUrl": null
}

🔐 Security Config

Voor ontwikkeling:

CSRF uitgeschakeld

Auth endpoints publiek

.requestMatchers(
"/api/auth/**",
"/api/lobbies/**"
).permitAll()


✅ Resultaat

✔ Gebruikers worden opgeslagen in Supabase
✔ Data blijft bestaan na server restart
✔ Wachtwoorden zijn veilig gehashed
✔ Spring Boot + JPA structuur behouden
