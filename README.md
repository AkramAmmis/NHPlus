Ein vorkonfigurierter Admin-Zugang ist für Entwicklungs- und Testzwecke vorhanden:

- **Benutzername:** `admin`
- **Passwort:** `admin123`

# Pflege-Login-System

Dieses Modul implementiert ein sicheres Login-System für Pflegekräfte. Ziel ist es, die Zugriffe auf die Anwendung kontrolliert, nachvollziehbar und benutzerfreundlich zu gestalten.

---

## 🔐 Features

- Benutzerbezogene Logins für Pflegekräfte
- Sichere Passwortspeicherung mit Hashing und Salt
- Automatische Abmeldung bei Inaktivität
- Protokollierung aller Login-Versuche
- Temporäre Sperrung nach mehreren Fehlversuchen
- Möglichkeit zur sicheren Passwortänderung
- Zugriffsschutz durch Security-Framework

---

## ✅ Akzeptanzkriterien

| Code | Beschreibung                                            |
| ---- | ------------------------------------------------------- |
| A_1  | Jeder Pfleger hat eigene Login-Daten                    |
| A_2  | Passwörter werden verschlüsselt gespeichert             |
| A_3  | Nach 3 Fehlversuchen wird der Account temporär gesperrt |
| A_4  | Automatische Abmeldung nach 15 Minuten Inaktivität      |
| A_5  | Protokollierung aller Login-Versuche                    |
| A_6  | Möglichkeit zur sicheren Passwortänderung               |

---

### T_3: Benutzeroberfläche

- Login-Dialog gestalten
- Passwortänderung-Dialog entwickeln
- Validierung und Fehlermeldungen anzeigen

### T_4: Sicherheits-Framework

- Zugriffskontrollen durchsetzen
- Session-Timeout implementieren
- Sicherheitsrelevantes Logging integrieren

---
