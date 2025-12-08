# Gruppenplanung - M321 LB2
## Verteiltes Online-Shop System

**Datum:** 8. Dezember 2025

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
Wir entwickeln ein **verteiltes Online-Shop System** mit einer Microservices-Architektur. Das System besteht aus drei unabhängigen Services, die über definierte Schnittstellen miteinander kommunizieren.

### 2.2 Hauptfunktionalität
- Benutzer können sich registrieren und anmelden
- Benutzer können Produkte durchsuchen und ansehen
- Benutzer können Bestellungen aufgeben und verwalten

---

## 3. Anforderungen an das System

### 3.1 Funktionale Anforderungen

#### Zwingende Anforderungen (Must-Have)
1. **Benutzerverwaltung**
   - Benutzerregistrierung
   - Login/Logout
   - Benutzerprofilverwaltung

2. **Produktverwaltung**
   - Produktliste anzeigen
   - Produktdetails abrufen
   - Produktsuche

3. **Bestellverwaltung**
   - Bestellung erstellen
   - Bestellungen anzeigen
   - Bestellstatus aktualisieren

#### Optionale Anforderungen (Nice-to-Have)
- Warenkorb-Funktionalität
- Produktbewertungen
- Bestellhistorie mit Filteroptionen
- E-Mail-Benachrichtigungen

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

| Endpoint | Method | Beschreibung | Request Body | Response |
|----------|--------|--------------|--------------|----------|
| `/register` | POST | Neuen Benutzer registrieren | `{username, email, password}` | `{userId, token}` |
| `/login` | POST | Benutzer anmelden | `{email, password}` | `{userId, token}` |
| `/profile/:userId` | GET | Benutzerprofil abrufen | - | `{userId, username, email}` |
| `/profile/:userId` | PUT | Benutzerprofil aktualisieren | `{username, email}` | `{success: true}` |
| `/validate` | POST | Token validieren | `{token}` | `{valid: true, userId}` |

### 5.2 Products Service API (Rilind)

**Base URL:** `http://localhost:3003/api/products`

| Endpoint | Method | Beschreibung | Request Body | Response |
|----------|--------|--------------|--------------|----------|
| `/` | GET | Alle Produkte abrufen | - | `[{productId, name, price, stock}]` |
| `/:productId` | GET | Produktdetails abrufen | - | `{productId, name, description, price, stock}` |
| `/` | POST | Neues Produkt erstellen | `{name, description, price, stock}` | `{productId}` |
| `/:productId` | PUT | Produkt aktualisieren | `{name, price, stock}` | `{success: true}` |
| `/:productId/stock` | GET | Lagerbestand prüfen | - | `{productId, stock}` |
| `/:productId/reserve` | POST | Produkt reservieren | `{quantity}` | `{success: true, reserved}` |

### 5.3 Orders Service API (Davi)

**Base URL:** `http://localhost:3002/api/orders`

| Endpoint | Method | Beschreibung | Request Body | Response |
|----------|--------|--------------|--------------|----------|
| `/` | POST | Neue Bestellung erstellen | `{userId, items: [{productId, quantity}]}` | `{orderId, total}` |
| `/:orderId` | GET | Bestelldetails abrufen | - | `{orderId, userId, items, status, total}` |
| `/user/:userId` | GET | Bestellungen eines Users | - | `[{orderId, date, status, total}]` |
| `/:orderId/status` | PUT | Bestellstatus aktualisieren | `{status}` | `{success: true}` |

### 5.4 Service-zu-Service Kommunikation

- **Orders Service → Products Service:** Produktinformationen und Lagerbestand prüfen
- **Orders Service → Users Service:** Benutzerinformationen validieren
- Authentifizierung: JWT Token im Header `Authorization: Bearer <token>`

---

## 6. Technologie-Stack

### 6.1 Gemeinsame Technologien
- **Versionskontrolle:** Git / GitHub
- **API-Format:** REST (JSON)
- **Authentifizierung:** JWT (JSON Web Tokens)
- **Deployment:** LernMAAS (VM)

### 6.2 Pro Service (individuell wählbar)

Jedes Teammitglied kann für seinen Service frei wählen:
- **Programmiersprache:** Node.js, Python, Java, etc.
- **Framework:** Express, Flask, Spring Boot, etc.
- **Datenbank:** PostgreSQL, MongoDB, MySQL, etc.

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

### Phase 1: Planung und Setup (Woche 1-2)
- [x] Gruppenbildung und Rollendefinition
- [ ] Schnittstellendefinition abstimmen
- [ ] Technologie-Stack festlegen
- [ ] Git-Repositories einrichten
- [ ] LernMAAS VM Setup
- [ ] Planung mit Lehrperson besprechen und freigeben lassen

### Phase 2: Prototyping (Woche 3-4)
- [ ] HA-Konzepte testen (Load Balancer Setup)
- [ ] Basis-Implementierung der Services starten
- [ ] Erste API-Endpoints implementieren
- [ ] Datenbank-Schema aufsetzen

### Phase 3: Entwicklung (Woche 5-8)
- [ ] Vollständige Implementierung aller Services
- [ ] Service-zu-Service Kommunikation implementieren
- [ ] JWT-Authentifizierung integrieren
- [ ] Parallele Dokumentation der Komponenten
- [ ] **Zwischenstand-Besprechung mit Lehrperson (ca. Woche 6-7)**

### Phase 4: Integration (Woche 9-10)
- [ ] Services zusammenführen
- [ ] End-to-End Testing
- [ ] HA-Massnahmen implementieren und testen
- [ ] Bugfixing

### Phase 5: Finalisierung (Woche 11-12)
- [ ] Deployment auf LernMAAS
- [ ] Dokumentation vervollständigen
- [ ] Testing und Qualitätssicherung
- [ ] Demonstration vorbereiten
- [ ] Finale Abgabe

---

## 9. Git-Repository Links

**Haupt-Repository:**
- Link: `https://github.com/MaximilianKos/m321_lb2` (wird noch erstellt)

**Optionale Service-Repositories:**
- Users Service: TBD
- Orders Service: TBD
- Products Service: TBD

**Branch-Strategie:**
- `main` - Produktiver/finaler Code
- `develop` - Entwicklungsbranch
- `feature/*` - Feature-Branches

---

## 10. Bestehende Software als Basis

☐ Wir verwenden **keine** bestehende Software als Basis
☐ Wir verwenden folgende Software: ___________________
☑ Wir verwenden das Online-Shop-Projekt als Referenz: `https://gitlab.com/ch-tbz-it/Stud/m321/onlineshop-monolith`

---

## 11. Kommunikation und Zusammenarbeit

### Regelmäßige Meetings
- **Wöchentlich:** Montag, 10:00 Uhr (während Unterricht)
- **Bei Bedarf:** Ad-hoc via Discord/Teams

### Kommunikationskanäle
- **Chat:** Discord/Teams (für schnelle Fragen)
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

## 13. Notizen

- Alle Services müssen `/health` Endpoint implementieren
- JWT Token Gültigkeit: 24 Stunden
- Alle Passwörter werden mit bcrypt gehashed (mind. 10 rounds)
- API-Dokumentation mit Swagger/OpenAPI (optional)
- Logging-Format: JSON mit Timestamp, Service-Name, Log-Level

---

**Erstellt am:** 8. Dezember 2025  
**Letzte Aktualisierung:** 8. Dezember 2025  
**Status:** Entwurf - Wartet auf Freigabe durch Lehrperson
