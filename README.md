## Vorwort:

Das ist unser NHPlus-Projekt. 
Wenn es Probleme mit dem Starten gibt, starten Sie vorher nochmal die [SetUpDB.java].
Und wenn es dann immer noch Probleme gibt, dann löschen Sie die [nursingHome.db] und starten die SetUpDB nochmal.
Danach sollten Sie die [Main.java] ganz normal starten können.
*Es könnte noch Probleme mit der misc.xml und der jdk bei Ihnen geben.

Ein vorkonfigurierter Admin-Zugang ist für Entwicklungs- und Testzwecke vorhanden:

- **Benutzername:** `admin`
- **Passwort:** `admin123`

Vorkonfigurierter Pfleger:

- **Benutzername:** `hneumann`
- **Passwort:** `password123`

## 🔐 Features

- Benutzerbezogene Logins für Pflegekräfte
- Sichere Passwortspeicherung mit Hashing
- Automatische Abmeldung bei Inaktivität
- Protokollierung aller Login-Versuche
- Temporäre Sperrung nach mehreren Fehlversuchen
- Möglichkeit zur sicheren Passwortänderung (für Admins)
- Zugriffsschutz durch Security-Framework
- Automatische Löschung nach 10 Jahren
- Pfleger können in der Tabelle angelegt und bearbeitet werden
- Sperrlogik von Treatments, Caregivers und Patients
- Pfleger können bei Behandlungen hinterlegt und geändert werden
- Vermögensstand wurde entfernt

