# Onlineshop Monolith

[TOC]

## Ziele / Zweck von diesem Projekt
Ziel dieses Projekts ist es, eine monlithische Applikation zur Verfügung zu stellen, die im Rahmen der Projektarbeit (LB2) von der monolithischen Architektur umgebaut und in eine Microservice Architektur überführt werden soll.

Die Applikation stellt einen Online-Shop zur Verfügung. Der Onlineshop bietet - abhängig von der Rolle des angemeldeten Benutzers - folgende Funktionalitäten:
- Unangemeldete Benutzer:
  - Benutzeranmeldung
  - Benutzerregistrierung
  - Produktübersicht
  - Warenkorb
- Angemeldete normale Benutzer:
  - Profilverwaltung
  - Bestellung
  - Bestellhistory
- Angemeldete Administratoren:
  - Benutzerverwaltung
  - Produktverwaltung (inkl. Lagerbestand)

Der Onlineshop wurde mithilfe von KI generiert. Er ist nicht für den produktiven Einsatz vorgesehen, sondern ist lediglich für Schulungszwecke gedacht. Die Umsetzung des Shops erhebt folglich nicht den Anspruch komplett "state-of-the-art" oder fehlerfrei zu sein. Sicherheitsaspekte wurden folglich auch nicht besonders berücksichtigt.

## Projektstruktur
Die Datenhaltung erfolgt über die Ablage der Daten als serialisierte JSON-Dateien (Dateien im Ordner `data`). Dies ist bewusst so gehalten, weil es im Projekt darum geht, den Monolithen in ein verteiltes System zu überführen. Eine Datenbank wäre da sinnvoller und sollte entsprechend im Rahmen der Projektarbeit (LB2) implementiert werden.

Das Projekt basiert auf Node.js und verwendet Express.js als Webserver. Mit dem Befehl `npm install` im Projektverzeichnis (hier wo dieses README.md liegt) installieren Sie die Paketabhängigkeiten. Mit `npm start` starten Sie den Webserver. Dieser ist unter `http://localhost:3000` erreichbar.

Zu Testzwecken sind zwei Benutzer angelegt:
- Benutzername: `admin@localhost`, Passwort: `testAdmin`
- Benutzername: `benutzer@localhost`, Passwort: `testBenutzer`

Es existieren drei Testprodukte, wobei eines bereits ausverkauft ist (Lagerbestand = 0). Beim benuter@localhost ist bereits eine Bestellhistory zu sehen.

Im Verzeichnis `src` befinden sich sämtliche Node.js-Quelldateien. Unter `public` sind die CSS-Definitionen und das Logo des Onlineshops abgelegt.

## Fragen und Anregungen
Bitte melden Sie sich für Fragen und Anregungen bei Ihrer Lehrperson oder der modulverantwortlichen Person.
