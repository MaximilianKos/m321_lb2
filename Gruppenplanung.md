# Gruppenplanung - M321 LB2
## Verteiltes Online-Shop System

**Datum:** 15. Dezember 2025

---

## 1. Gruppenmitglieder und Verantwortlichkeiten

| Name | Systemkomponente | Verantwortungsbereich |
|------|------------------|------------------------|
| **Davi** | Orders Service | Bestellverwaltung, Bestellhistorie, Bestellstatus |
| **Rilind** | Products Service | Produktverwaltung, Produktkatalog, Inventar |
| **Maximilian** | Users Service | Benutzerverwaltung, Authentifizierung, Autorisierung |

---

## 2. Projektbeschreibung

### 2.1 Verteiltes System
Wir migrieren den bestehenden **monolithischen Online-Shop** zu einer **Microservices-Architektur**. Der Monolith wird in drei unabhängige Services aufgeteilt, die über definierte REST-APIs miteinander kommunizieren.

### 2.2 Ausgangslage
Der bestehende Monolith (`onlineshop-monolith`) ist eine Node.js/Express-Applikation mit folgenden Features:
- Benutzerverwaltung (Registrierung, Login, Profil)
- Produktverwaltung (CRUD-Operationen, Lagerbestand)
- Bestellverwaltung (Warenkorb, Bestellhistorie)
- Admin-Funktionen (Benutzer- und Produktverwaltung)
- Session-basierte Authentifizierung
- JSON-Datei basierte Datenhaltung (soll durch Datenbanken ersetzt werden)

### 2.3 Ziel der Migration
Die monolithische Applikation wird in drei eigenständige Microservices aufgeteilt:
- **Users Service** (Maximilian): Benutzerverwaltung und Authentifizierung
- **Products Service** (Rilind): Produktverwaltung und Lagerbestand
- **Orders Service** (Davi): Bestellverwaltung und Warenkorb

Jeder Service wird mit eigener Datenbank, eigenem Repository und eigenständigem Deployment umgesetzt.

---

## 3. Anforderungen an das System

### 3.1 Funktionale Anforderungen

#### Zwingende Anforderungen (Must-Have)
1. **Benutzerverwaltung (Users Service)**
   - Benutzerregistrierung
   - Login/Logout mit JWT-Authentifizierung (Migration von Session zu JWT)
   - Benutzerprofilverwaltung
   - Admin-Funktionen (Benutzer aktivieren/deaktivieren)
   - Token-Validierung für andere Services

2. **Produktverwaltung (Products Service)**
   - Produktliste anzeigen
   - Produktdetails abrufen
   - CRUD-Operationen für Produkte (Admin)
   - Lagerverwaltung
   - Lagerbestand prüfen und reservieren

3. **Bestellverwaltung (Orders Service)**
   - Warenkorb-Funktionalität
   - Bestellung erstellen (mit Lagerbestand-Prüfung)
   - Bestellungen anzeigen (eigene und alle für Admin)
   - Bestellhistorie anzeigen
   - Bestellstatus verwalten

#### Optionale Anforderungen (Nice-to-Have)
- Frontend-Integration (API Gateway für bestehende Views)
- Produktbewertungen
- Bestellhistorie mit erweiterten Filteroptionen
- E-Mail-Benachrichtigungen bei Bestellung
- Admin-Dashboard als separate Komponente

### 3.2 Nicht-funktionale Anforderungen

1. **Verfügbarkeit**
   - Hochverfügbarkeit durch Load Balancing
   - Mindestens 99% Uptime angestrebt

2. **Performance**
   - Antwortzeiten unter 500ms für Standard-Anfragen
   - Skalierbarkeit für mehrere gleichzeitige Benutzer

3. **Sicherheit**
   - Sichere Passwort-Speicherung (Hashing)
   - Token-basierte Authentifizierung (JWT)
   - HTTPS für alle Kommunikationen

4. **Wartbarkeit**
   - Klare API-Dokumentation
   - Modularer Aufbau
   - Versionskontrolle mit Git

---

## 4. Systemarchitektur

### 4.1 Überblick der Systemkomponenten

```
┌─────────────────────────────────────────────────────────┐
│                    API Gateway / Load Balancer           │
│                      (HAProxy / Nginx)                   │
└────────────────┬────────────────┬───────────────────────┘
                 │                │
        ┌────────┴────────┐      │      ┌──────────────────┐
        │                 │      │      │                  │
┌───────▼──────┐  ┌──────▼──────▼┐  ┌──▼─────────────┐   │
│ Users Service │  │Orders Service│  │Products Service│   │
│  (Maximilian) │  │    (Davi)    │  │   (Rilind)     │   │
│               │  │              │  │                │   │
│  Port: 3001   │  │  Port: 3002  │  │  Port: 3003    │   │
└───────┬───────┘  └──────┬───────┘  └────────┬───────┘   │
        │                 │                   │            │
        │          ┌──────┴───────────────────┘            │
        │          │                                        │
   ┌────▼──────────▼───┐                                   │
   │   Datenbanken     │                                   │
   │ - Users DB        │                                   │
   │ - Orders DB       │                                   │
   │ - Products DB     │                                   │
   └───────────────────┘                                   │
                                                            │
        ┌───────────────────────────────────────────────────┘
        │
┌───────▼──────────┐
│  Health Monitor  │
│  & Service Reg.  │
└──────────────────┘
```

### 4.2 Service-Instanzen (für Hochverfügbarkeit)

Jeder Service wird in **mindestens 2 Instanzen** betrieben:
- Users Service: Instanz 1 (Port 3001), Instanz 2 (Port 3011)
- Orders Service: Instanz 1 (Port 3002), Instanz 2 (Port 3012)
- Products Service: Instanz 1 (Port 3003), Instanz 2 (Port 3013)

---

## 5. Schnittstellen-Definition

### 5.1 Users Service API (Maximilian)

**Base URL:** `http://localhost:3001/api/users`

**Migration von:** `authRoutes.js`, `userRoutes.js`, `authService.js`, `userService.js`

| Endpoint | Method | Beschreibung | Request Body | Response |
|----------|--------|--------------|--------------|----------|
| `/register` | POST | Neuen Benutzer registrieren | `{email, password}` | `{userId, token, email, role}` |
| `/login` | POST | Benutzer anmelden | `{email, password}` | `{userId, token, email, role}` |
| `/logout` | POST | Benutzer abmelden | - | `{success: true}` |
| `/:userId` | GET | Benutzerprofil abrufen | Header: `Authorization: Bearer <token>` | `{id, email, role, active}` |
| `/:userId` | PUT | Benutzerprofil aktualisieren | `{email, password}` | `{success: true}` |
| `/` | GET | Alle Benutzer auflisten (Admin) | Header: `Authorization: Bearer <token>` | `[{id, email, role, active}]` |
| `/:userId/active` | PUT | Benutzer aktivieren/deaktivieren (Admin) | `{active: true/false}` | `{success: true}` |
| `/validate` | POST | Token validieren (für andere Services) | `{token}` | `{valid: true, userId, role}` |
| `/health` | GET | Health Check | - | `{status: "OK", timestamp}` |

**Anmerkung:** Rolle (role) kann `customer` oder `admin` sein.

### 5.2 Products Service API (Rilind)

**Base URL:** `http://localhost:3003/api/products`

**Migration von:** `productRoutes.js`, `productService.js`, `adminRoutes.js` (Produktteil)

| Endpoint | Method | Beschreibung | Request Body | Response |
|----------|--------|--------------|--------------|----------|
| `/` | GET | Alle Produkte abrufen | - | `[{id, name, description, price, stock}]` |
| `/:productId` | GET | Produktdetails abrufen | - | `{id, name, description, price, stock}` |
| `/` | POST | Neues Produkt erstellen (Admin) | `{name, description, price, stock}` + Auth Header | `{id, name, description, price, stock}` |
| `/:productId` | PUT | Produkt aktualisieren (Admin) | `{name, description, price, stock}` + Auth Header | `{success: true}` |
| `/:productId` | DELETE | Produkt löschen (Admin) | Auth Header | `{success: true}` |
| `/:productId/stock` | GET | Lagerbestand prüfen | - | `{productId, stock, available: true/false}` |
| `/:productId/decrease` | POST | Lagerbestand reduzieren (intern) | `{quantity}` | `{success: true, newStock}` |
| `/health` | GET | Health Check | - | `{status: "OK", timestamp}` |

**Anmerkung:** Admin-Endpoints benötigen JWT Token mit `role: "admin"` im Header `Authorization: Bearer <token>`

### 5.3 Orders Service API (Davi)

**Base URL:** `http://localhost:3002/api/orders`

**Migration von:** `orderRoutes.js`, `cartRoutes.js`, `orderService.js`

| Endpoint | Method | Beschreibung | Request Body | Response |
|----------|--------|--------------|--------------|----------|
| `/cart` | GET | Warenkorb anzeigen | Auth Header | `{items: [{productId, name, price, quantity}], total}` |
| `/cart/add` | POST | Produkt zum Warenkorb hinzufügen | `{productId, quantity}` + Auth Header | `{success: true, cart}` |
| `/cart/remove` | POST | Produkt aus Warenkorb entfernen | `{productId}` + Auth Header | `{success: true, cart}` |
| `/cart/clear` | POST | Warenkorb leeren | Auth Header | `{success: true}` |
| `/` | POST | Bestellung aufgeben (aus Warenkorb) | Auth Header | `{orderId, items, totalAmount, status, createdAt}` |
| `/` | GET | Alle Bestellungen (Admin) | Auth Header | `[{id, userId, items, totalAmount, status, createdAt}]` |
| `/user/:userId` | GET | Bestellungen eines Users | Auth Header | `[{id, userId, items, totalAmount, status, createdAt}]` |
| `/:orderId` | GET | Bestelldetails abrufen | Auth Header | `{id, userId, items, totalAmount, status, createdAt}` |
| `/:orderId/status` | PUT | Bestellstatus aktualisieren (Admin) | `{status}` + Auth Header | `{success: true}` |
| `/health` | GET | Health Check | - | `{status: "OK", timestamp}` |

**Anmerkung:** 
- Orders Service kommuniziert mit Products Service für Produktinfos und Lagerbestand
- Orders Service kommuniziert mit Users Service für Token-Validierung
- Warenkorb wird in-memory (browser) gespeichert

### 5.4 Service-zu-Service Kommunikation

**Orders Service → Products Service:**
- `GET /api/products/:productId` - Produktinformationen abrufen
- `GET /api/products/:productId/stock` - Lagerbestand prüfen
- `POST /api/products/:productId/decrease` - Lagerbestand reduzieren bei Bestellung

**Orders Service → Users Service:**
- `POST /api/users/validate` - JWT Token validieren und User-Info erhalten

**Alle Requests zwischen Services:**
- Verwenden HTTP/REST
- JSON als Datenformat
- Timeouts und Retry-Mechanismen implementieren
- Circuit Breaker Pattern für Fehlertoleranz

**Client → Services:**
- Authentifizierung: JWT Token im Header `Authorization: Bearer <token>`
- Token wird vom Users Service bei Login/Registration ausgestellt
- Token enthält: `userId`, `role`, `exp` (Ablaufzeit)

---

## 6. Technologie-Stack

### 6.1 Bestehende Technologien (Monolith)
- **Runtime:** Node.js
- **Framework:** Express.js
- **Template Engine:** EJS (Views)
- **Session Management:** express-session
- **Passwort-Hashing:** bcryptjs
- **Datenhaltung:** JSON-Dateien (users.json, products.json, orders.json)

### 6.2 Gemeinsame Technologien (Microservices)
- **Versionskontrolle:** Git / GitHub
- **API-Format:** REST (JSON)
- **Authentifizierung:** JWT (JSON Web Tokens) - Migration von Session-basiert zu JWT
- **Deployment:** LernMAAS (VM)
- **Load Balancer:** HAProxy oder Nginx

### 6.3 Pro Service (individuell wählbar)

Jedes Teammitglied kann für seinen Service frei wählen:
- **Programmiersprache:** Node.js (empfohlen für schnelle Migration), Python, Java, etc.
- **Framework:** Express, Flask, Spring Boot, etc.
- **Datenbank:** PostgreSQL (ersetzt JSON-Dateien)

---

## 7. Hochverfügbarkeits-Massnahmen

### 7.1 Geplante HA-Massnahmen

1. **Load Balancing** (Verantwortlich: Alle gemeinsam)
   - Einsatz von HAProxy oder Nginx als Load Balancer
   - Round-Robin Verteilung der Requests
   - Automatisches Failover bei Service-Ausfall

2. **Service-Replikation** (Verantwortlich: Jeder für seinen Service)
   - Mindestens 2 Instanzen pro Service
   - Unabhängige Prozesse auf verschiedenen Ports
   - Health-Check Endpoints für Monitoring

3. **Health Monitoring** (Verantwortlich: Alle gemeinsam)
   - Implementierung von `/health` Endpoints
   - Automatische Erkennung ausgefallener Services
   - Logging und Alerting bei Problemen

### 7.2 Zusätzliche Massnahmen (Optional)
- Database Replication
- Circuit Breaker Pattern
- Request Retry Mechanismen
- Caching (Redis)

---

## 8. Projektplanung / Vorgehen

**Gesamtaufwand:** 20 Lektionen pro Person  
**Verfügbare Termine:** 5 Schultage à 4 Lektionen = 20 Lektionen total  
**Projektabgabe:** 19.01.2026  
**Optionale Testing-Abgabe:** 26.01.2026

---

### Lektion 1-4: Montag, 08.12.2025 (Planung & Setup)
**Aufwand:** 4 Lektionen

- [x] Gruppenbildung und Rollendefinition
- [x] Monolith-Code analysieren und verstehen
- [ ] Schnittstellendefinition abstimmen (basierend auf Monolith-Analyse)
- [ ] Aufteilung der bestehenden Funktionalität auf Services planen
- [ ] Datenbank-Schema für jeden Service designen (Migration von JSON zu DB)
- [ ] Technologie-Stack pro Service festlegen
- [ ] Git-Repository-Struktur festlegen (monorepo oder separate repos)
- [ ] **Planung mit Lehrperson besprechen und freigeben lassen**

**Ziel Ende Tag 1:** Genehmigte Planung, klare API-Definitionen, jeder kennt seine Aufgaben

---

### Lektion 5-8: Montag, 15.12.2025 (Basis-Implementierung)
**Aufwand:** 4 Lektionen

- [ ] LernMAAS VM Setup und Zugriff testen
- [ ] Git-Repositories einrichten und initialen Code committen
- [ ] Datenbank-Setup pro Service (lokal und auf VM)
- [ ] Datenmigration: JSON-Daten in Datenbanken übertragen
- [ ] JWT-Authentifizierung Grundgerüst implementieren
- [ ] Basis-Service-Implementierung starten:
  - [ ] **Users Service (Maximilian):** Register, Login, Token-Validierung
  - [ ] **Products Service (Rilind):** Produkte auflisten, Details abrufen
  - [ ] **Orders Service (Davi):** Warenkorb-Grundfunktionen
- [ ] Health-Check Endpoints (`/health`) für alle Services

**Ziel Ende Tag 2:** Funktionierende Basis-Services mit Datenbank-Anbindung

---

### Lektion 9-12: Montag, 05.01.2026 (Hauptentwicklung)
**Aufwand:** 4 Lektionen

- [ ] Vollständige API-Implementierung aller Services:
  - [ ] **Users Service:** Profilverwaltung, Admin-Funktionen (User aktivieren/deaktivieren)
  - [ ] **Products Service:** CRUD-Operationen, Lagerbestand-Management
  - [ ] **Orders Service:** Bestellungen erstellen, Bestellhistorie
- [ ] Service-zu-Service Kommunikation implementieren:
  - [ ] Orders → Products (Produktinfo, Lagerbestand)
  - [ ] Orders → Users (Token-Validierung)
- [ ] Error Handling und einheitliche Fehlerbehandlung
- [ ] Logging implementieren (JSON-Format mit Timestamps)
- [ ] **Zwischenstand-Besprechung mit Lehrperson einplanen**

**Ziel Ende Tag 3:** Alle Services funktional vollständig, Service-Kommunikation funktioniert

---

### Lektion 13-16: Montag, 12.01.2026 (HA-Massnahmen & Integration)
**Aufwand:** 4 Lektionen

- [ ] HA-Konzepte umsetzen:
  - [ ] Load Balancer Setup (HAProxy oder Nginx)
  - [ ] Service-Instanzen replizieren (je 2 Instanzen pro Service)
  - [ ] Health-Monitoring konfigurieren
  - [ ] Automatisches Failover testen
- [ ] End-to-End Testing des gesamten Systems
- [ ] Integration Testing zwischen Services
- [ ] Bugfixing und Optimierungen
- [ ] Code-Review und Code-Qualität verbessern
- [ ] Dokumentation parallel zur Entwicklung pflegen

**Ziel Ende Tag 4:** Hochverfügbares System funktioniert, alle Services integriert

---

### Lektion 17-20: Montag, 19.01.2026 (Finalisierung & Abgabe)
**Aufwand:** 4 Lektionen

- [ ] Finales Deployment auf LernMAAS
- [ ] Alle Services auf VM deployen und testen
- [ ] Load Balancer konfigurieren und testen
- [ ] Dokumentation vervollständigen:
  - [ ] Systemübersicht und Architektur
  - [ ] Jede Person dokumentiert eigene Komponente(n)
  - [ ] Installationsanleitung
  - [ ] API-Dokumentation
  - [ ] Fazit und Ausblick
- [ ] Demonstration vorbereiten und durchführen
- [ ] Finale Code-Bereinigung (auskommentierter Code entfernen)
- [ ] Testing und Qualitätssicherung
- [ ] **Abgabe: Code + Dokumentation**

**Ziel Ende Tag 5:** Vollständige Abgabe, funktionierendes System auf LernMAAS

---

### Optional: 26.01.2026 (Optionale Testing-Abgabe)
**Hinweis:** Nur wenn automatisierte Tests für Extrapunkte erstellt werden

- [ ] Testdefinition und Testcases dokumentieren
- [ ] Testprotokoll erstellen
- [ ] Automatisierte Tests (Unit-Tests, Integration-Tests)
- [ ] Testabdeckung erhöhen
- [ ] Test-Dokumentation abgeben

---

### Meilensteine

| Datum | Meilenstein | Deliverables |
|-------|-------------|--------------|
| **08.12.2025** | Planung abgeschlossen | Genehmigte Planung, API-Definitionen |
| **15.12.2025** | Basis-Services funktional | Login, Produktliste, Warenkorb funktioniert |
| **05.01.2026** | Alle Features implementiert | Vollständige API, Service-Kommunikation |
| **12.01.2026** | HA-System funktioniert | Load Balancer, mehrere Instanzen, Failover |
| **19.01.2026** | **PROJEKT-ABGABE** | Code, Dokumentation, Demonstration |
| **26.01.2026** | Optional: Testing-Abgabe | Test-Dokumentation, automatisierte Tests |

---

### Aufwandsverteilung pro Person

Jede Person hat **20 Lektionen** für folgende Aufgaben:

| Aktivität | Geschätzte Lektionen |
|-----------|---------------------|
| Planung & Analyse | 3-4 Lektionen |
| Implementierung eigener Service | 8-10 Lektionen |
| Integration & Testing | 3-4 Lektionen |
| HA-Massnahmen & Deployment | 2-3 Lektionen |
| Dokumentation | 2-3 Lektionen |
| **Total** | **~20 Lektionen** |

---

## 9. Git-Repository Links

**Repository:**
- Link: `https://github.com/MaximilianKos/m321_lb2`

**Branch-Strategie:**
- `main` - Produktiver/finaler Code
- `feature/*` - Feature-Branches

---

## 10. Bestehende Software als Basis

☐ Wir verwenden **keine** bestehende Software als Basis
☑ Wir verwenden folgende Software als Basis: **Onlineshop Monolith**
☐ Wir verwenden das Online-Shop-Projekt als Referenz: `https://gitlab.com/ch-tbz-it/Stud/m321/onlineshop-monolith`

### Details zur Basis-Software
 
**Quelle:** `https://gitlab.com/ch-tbz-it/Stud/m321/onlineshop-monolith` (als Vorlage)  

**Technologien der Basis:**
- Node.js mit Express.js
- EJS Template Engine
- bcryptjs für Passwort-Hashing
- express-session für Session Management
- JSON-Dateien für Datenhaltung

**Vorhandene Testdaten:**
- 2 Benutzer: `admin@localhost` (Admin), `benutzer@localhost` (Customer)
- 3 Produkte (eines ausverkauft)
- Bestellhistorie für Testbenutzer

**Was wird übernommen/migriert:**
- Service-Logik (userService.js, productService.js, orderService.js)
- Repository-Pattern (userRepository.js, productRepository.js, orderRepository.js)
- Bestehende Datenstrukturen (users.json, products.json, orders.json)
- Business-Logik (Validierungen, Berechnungen)

**Was wird neu entwickelt:**
- JWT-basierte Authentifizierung (ersetzt Sessions)
- REST-API Endpoints (strukturiert nach Service-Grenzen)
- Datenbank-Integration (ersetzt JSON-Dateien)
- Load Balancer und Service-Orchestrierung
- Health-Check und Monitoring
- Service-zu-Service Kommunikation

---

## 11. Kommunikation und Zusammenarbeit

### Regelmäßige Meetings
- **Wöchentlich:** Montag, 10:00 Uhr (während Unterricht)

### Kommunikationskanäle
- **Chat:** Teams (für schnelle Fragen)
- **Code-Review:** GitHub Pull Requests
- **Dokumentation:** GitHub Wiki / Shared Docs

### Verantwortlichkeiten
- **Davi:** Orders Service, Testing Koordination
- **Rilind:** Products Service, Datenbank Setup
- **Maximilian:** Users Service, Git/Deployment Koordination

---

## 12. Risiken und Mitigation

| Risiko | Wahrscheinlichkeit | Impact | Mitigation |
|--------|-------------------|--------|------------|
| Schnittstellenänderungen während Entwicklung | Mittel | Hoch | Frühe Definition, regelmäßige Abstimmung |
| Technische Probleme mit LernMAAS | Niedrig | Mittel | Frühe Tests, Alternative Cloud-Lösung |
| Zeitknappheit einzelner Mitglieder | Mittel | Mittel | Faire Aufgabenverteilung, frühzeitige Kommunikation |
| HA-Konzept funktioniert nicht | Niedrig | Hoch | Frühes Testing der Konzepte (Phase 2) |

---

## 13. Migrations-Strategie

### 13.1 Code-Übernahme aus Monolith

| Monolith-Datei | Ziel-Service | Anpassungen |
|----------------|--------------|-------------|
| `src/services/userService.js` | Users Service | JWT statt Session, DB statt JSON |
| `src/services/authService.js` | Users Service | JWT-Token generieren/validieren |
| `src/repositories/userRepository.js` | Users Service | DB-Adapter statt JSON-Dateien |
| `src/services/productService.js` | Products Service | REST-API, DB statt JSON |
| `src/repositories/productRepository.js` | Products Service | DB-Adapter statt JSON-Dateien |
| `src/services/orderService.js` | Orders Service | Service-Calls zu Products/Users |
| `src/repositories/orderRepository.js` | Orders Service | DB-Adapter statt JSON-Dateien |
| `data/users.json` | Users Service DB | Testdaten migrieren |
| `data/products.json` | Products Service DB | Testdaten migrieren |
| `data/orders.json` | Orders Service DB | Testdaten migrieren |

### 13.2 Datenmodelle

**Users:**
```javascript
{
  id: UUID,
  email: String (unique),
  passwordHash: String (bcrypt),
  role: String ('customer' | 'admin'),
  active: Boolean,
  createdAt: Date
}
```

**Products:**
```javascript
{
  id: UUID,
  name: String,
  description: String,
  price: Number,
  stock: Number,
  createdAt: Date,
  updatedAt: Date
}
```

**Orders:**
```javascript
{
  id: UUID,
  userId: UUID (foreign key),
  items: [{
    productId: UUID,
    productName: String,
    quantity: Number,
    price: Number
  }],
  totalAmount: Number,
  status: String ('confirmed', 'processing', 'shipped', 'delivered'),
  createdAt: Date
}
```

## 14. Notizen

### Technische Details
- Alle Services müssen `/health` Endpoint implementieren
- JWT Token Gültigkeit: 24 Stunden (86400 Sekunden)
- JWT Secret: Umgebungsvariable `JWT_SECRET` (pro Service oder geteilt)
- Alle Passwörter werden mit bcrypt gehashed (mind. 10 rounds, wie im Monolith)
- Logging-Format: JSON mit Timestamp, Service-Name, Log-Level, Request-ID

### Entwicklungs-Richtlinien
- Umgebungsvariablen in `.env` Dateien (nicht committen!)
- Error Handling: Einheitliche Fehlerstruktur `{error: String, message: String, status: Number}`
- CORS: Konfiguriert für Cross-Origin Requests zwischen Services
- Rate Limiting: Optional für Production
- Input Validation: Alle Endpoints validieren Input-Daten

### Testing
- Mindestens manuelle Tests mit Postman/Thunder Client
- Testdaten aus Monolith übernehmen für Vergleichstests
- Integration Tests: Services zusammen testen
- Load Balancer Testing: Failover-Szenarien durchspielen


