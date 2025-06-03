Ein vorkonfigurierter Admin-Zugang ist für Entwicklungs- und Testzwecke vorhanden:

- **Benutzername:** `admin`
- **Passwort:** `admin123`

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
| 1    | Jeder Pfleger hat eigene Login-Daten                    |
| 2    | Passwörter werden verschlüsselt gespeichert             |
| 3    | Nach 3 Fehlversuchen wird der Account temporär gesperrt |
| 4    | Automatische Abmeldung nach 15 Minuten Inaktivität      |
| 5    | Protokollierung aller Login-Versuche                    |
| 6    | Möglichkeit zur sicheren Passwortänderung               |

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
