package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.AuthorizationManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Pair;

// SQL-Importe
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Statement;
import java.util.Optional;

/**
 * The <code>AllCaregiverController</code> contains the entire logic of the caregiver view.
 * It determines which data is displayed and how to react to events.
 */
public class AllCaregiverController {

    @FXML
    private TableView<Caregiver> tableView;

    @FXML
    private TableColumn<Caregiver, Long> columnId;

    @FXML
    private TableColumn<Caregiver, String> columnFirstName;

    @FXML
    private TableColumn<Caregiver, String> columnSurname;

    @FXML
    private TableColumn<Caregiver, String> columnTelephone;

    @FXML
    private Button buttonDelete;

    @FXML
    private Button buttonAdd;

    @FXML
    private TextField textFieldSurname;

    @FXML
    private TextField textFieldFirstName;

    @FXML
    private TextField textFieldTelephone;

    private final ObservableList<Caregiver> caregivers = FXCollections.observableArrayList();
    private CaregiverDao dao;

    /**
     * When <code>initialize()</code> gets called, all fields are already initialized. For example from the FXMLLoader
     * after loading an FXML-File. At this point of the lifecycle of the Controller, the fields can be accessed and
     * configured.
     */
    public void initialize() {
        this.readAllAndShowInTableView();

        // Anpassung an die neuen fx:id Namen (falls geändert)
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("cid"));

        // Benutzerrechte prüfen
        boolean isAdmin = de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin();

        // Spalten konfigurieren - Bearbeitung nur für Admins erlauben
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnFirstName.setEditable(isAdmin);

        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnSurname.setEditable(isAdmin);

        this.columnTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        this.columnTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnTelephone.setEditable(isAdmin);

        // Gesamte Tabelle nur für Admins editierbar machen
        this.tableView.setEditable(isAdmin);

        this.tableView.setItems(this.caregivers);

        // Abmelde-Button zur Toolbar hinzufügen, falls noch nicht vorhanden
        addLogoutButton();

        // Lösch-Button basierend auf Benutzerrechten und Auswahl konfigurieren

        this.buttonDelete.setDisable(true);
        this.buttonDelete.setVisible(isAdmin); // Nur für Admins sichtbar machen

        this.tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldCaregiver, newCaregiver) ->
            AllCaregiverController.this.buttonDelete.setDisable(newCaregiver == null || !isAdmin));

        this.buttonAdd.setDisable(true);
        ChangeListener<String> inputNewCaregiverListener = (observableValue, oldText, newText) ->
            AllCaregiverController.this.buttonAdd.setDisable(!AllCaregiverController.this.areInputDataValid());
    
        // Anpassung an die neuen fx:id Namen (falls geändert)
        this.textFieldSurname.textProperty().addListener(inputNewCaregiverListener);
        this.textFieldFirstName.textProperty().addListener(inputNewCaregiverListener);
        this.textFieldTelephone.textProperty().addListener(inputNewCaregiverListener);
    }

    /**
     * When a cell of the column with first names was changed, this method will be called, to persist the change.
     *
     * @param event Event including the changed object and the change.
     */
    @FXML
    public void handleOnEditFirstname(TableColumn.CellEditEvent<Caregiver, String> event) {
        // Prüfen, ob der Benutzer Admin-Rechte hat
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten bearbeiten!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pflegerdaten bearbeiten!");
            alert.showAndWait();

            // Änderung verwerfen - Tabelle neu laden
            readAllAndShowInTableView();
            return;
        }

        event.getRowValue().setFirstName(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * When a cell of the column with surnames was changed, this method will be called, to persist the change.
     *
     * @param event Event including the changed object and the change.
     */
    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Caregiver, String> event) {
        // Prüfen, ob der Benutzer Admin-Rechte hat
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten bearbeiten!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pflegerdaten bearbeiten!");
            alert.showAndWait();

            // Änderung verwerfen - Tabelle neu laden
            readAllAndShowInTableView();
            return;
        }

        event.getRowValue().setSurname(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * When a cell of the column with telephone numbers was changed, this method will be called, to persist the change.
     *
     * @param event Event including the changed object and the change.
     */
    @FXML
    public void handleOnEditTelephone(TableColumn.CellEditEvent<Caregiver, String> event) {
        // Prüfen, ob der Benutzer Admin-Rechte hat
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten bearbeiten!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pflegerdaten bearbeiten!");
            alert.showAndWait();

            // Änderung verwerfen - Tabelle neu laden
            readAllAndShowInTableView();
            return;
        }

        event.getRowValue().setTelephone(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * Updates a caregiver by calling the method <code>update()</code> of {@link CaregiverDao}.
     *
     * @param event Event including the changed object and the change.
     */
    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        // Prüfen, ob der Benutzer Admin-Rechte hat
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten aktualisieren!");
            // Keine Fehlermeldung hier, da die Handler bereits eine Meldung anzeigen

            // Änderung verwerfen - Tabelle neu laden
            readAllAndShowInTableView();
            return;
        }

        try {
            this.dao.update(event.getRowValue());

            // Auch den zugehörigen Benutzer aktualisieren, falls vorhanden
            try {
                UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                User user = null;

                // Versuche, den Benutzer anhand der Caregiver-ID zu finden
                for (User u : userDao.readAllUsers()) {
                    if (u.getCaregiverId() == event.getRowValue().getCid()) {
                        user = u;
                        break;
                    }
                }

                if (user != null) {
                    // Nur die entsprechenden Felder aktualisieren
                    if ("firstName".equals(event.getTableColumn().getId())) {
                        user.setFirstName(event.getNewValue());
                    } else if ("surname".equals(event.getTableColumn().getId())) {
                        user.setLastName(event.getNewValue());
                    } else if ("telephone".equals(event.getTableColumn().getId())) {
                        user.setPhoneNumber(event.getNewValue());
                    }

                    userDao.updateUser(user);
                    System.out.println("Zugehöriger Benutzeraccount wurde ebenfalls aktualisiert: " + user.getUsername());
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Aktualisieren des zugehörigen Benutzeraccounts: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();

            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Datenbankfehler");
            errorAlert.setHeaderText("Fehler beim Aktualisieren");
            errorAlert.setContentText("Der Pfleger konnte nicht aktualisiert werden: " + exception.getMessage());
            errorAlert.showAndWait();
        }
    }

    /**
     * Reloads all caregivers to the table by clearing the list of all caregivers and filling it again by all persisted
     * caregivers, delivered by {@link CaregiverDao}.
     */
    private void readAllAndShowInTableView() {
        this.caregivers.clear();
        this.dao = DaoFactory.getDaoFactory().createCaregiverDAO(); // Annahme: Methode existiert in DaoFactory
        try {
            this.caregivers.addAll(this.dao.readAll());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * This method handles events fired by the button to delete caregivers. It calls {@link CaregiverDao} to delete the
     * caregiver from the database and removes the object from the list, which is the data source of the
     * <code>TableView</code>.
     */
    @FXML
    public void handleDelete() {
        // Prüfen, ob der Benutzer Admin-Rechte hat
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pfleger löschen!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pfleger löschen!");
            alert.showAndWait();
            return;
        }

        Caregiver selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // Sicherheitsabfrage vor dem Löschen
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Pfleger löschen");
            confirmAlert.setHeaderText("Pfleger löschen bestätigen");
            confirmAlert.setContentText("Möchten Sie den Pfleger " + selectedItem.getFirstName() + " " + 
                                       selectedItem.getSurname() + " wirklich löschen?");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    DaoFactory.getDaoFactory().createCaregiverDAO().deleteById(selectedItem.getCid());
                    this.tableView.getItems().remove(selectedItem);

                    // Benutzeraccount des Pflegers auch löschen, falls vorhanden
                    try {
                        UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                        User user = null;

                        // Versuche, den Benutzer anhand der Caregiver-ID zu finden
                        for (User u : userDao.readAllUsers()) {
                            if (u.getCaregiverId() == selectedItem.getCid()) {
                                user = u;
                                break;
                            }
                        }

                        if (user != null) {
                            userDao.delete(user);
                            System.out.println("Zugehöriger Benutzeraccount wurde ebenfalls gelöscht: " + user.getUsername());
                        }
                    } catch (Exception e) {
                        System.err.println("Fehler beim Löschen des zugehörigen Benutzeraccounts: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();

                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Datenbankfehler");
                    errorAlert.setHeaderText("Fehler beim Löschen");
                    errorAlert.setContentText("Der Pfleger konnte nicht gelöscht werden: " + exception.getMessage());
                    errorAlert.showAndWait();
                }
            }
        }
    }

    /**
     * This method handles the events fired by the button to add a caregiver. It collects the data from the
     * <code>TextField</code>s, creates an object of class <code>Caregiver</code> of it and passes the object to
     * {@link CaregiverDao} to persist the data.
     */
    @FXML
    public void handleAdd() {
        System.out.println("AllCaregiverController.handleAdd() wurde aufgerufen!");

        // Prüfen, ob alle benötigten Textfelder vorhanden sind
        if (this.textFieldSurname == null || this.textFieldFirstName == null || this.textFieldTelephone == null) {
            System.err.println("FEHLER: Eines der Textfelder ist null!");
            return;
        }

        // Prüfen, ob der Benutzer Admin-Rechte hat
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können neue Pfleger hinzufügen!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können neue Pfleger hinzufügen!");
            alert.showAndWait();
            return;
        }

        // Falls dao null ist, initialisieren wir es neu
        if (this.dao == null) {
            System.out.println("DAO wird neu initialisiert...");
            try {
                this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
                if (this.dao == null) {
                    System.err.println("FEHLER: DAO konnte nicht initialisiert werden!");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Fehler bei der Initialisierung des DAOs: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Explizit sicherstellen, dass die Tabelle mit den erforderlichen Spalten existiert
        try {
            Connection conn = ConnectionBuilder.getConnection();
            Statement stmt = conn.createStatement();

            // Prüfen, ob die Tabelle existiert
            ResultSet tables = conn.getMetaData().getTables(null, null, "caregiver", null);
            boolean tableExists = tables.next();
            tables.close();

            if (!tableExists) {
                System.out.println("Tabelle 'caregiver' existiert nicht. Erstelle neu...");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS caregiver (" +
                        "cid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "firstname TEXT NOT NULL, " +
                        "surname TEXT NOT NULL, " +
                        "telephone TEXT, " +
                        "username TEXT, " +
                        "password TEXT)"; 
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabelle 'caregiver' erfolgreich erstellt.");
            } else {
                // Prüfen, ob die Spalten username und password existieren
                boolean needsAlter = false;

                ResultSet columns = conn.getMetaData().getColumns(null, null, "caregiver", "username");
                boolean usernameExists = columns.next();
                columns.close();

                if (!usernameExists) {
                    System.out.println("Spalte 'username' fehlt. Füge hinzu...");
                    stmt.executeUpdate("ALTER TABLE caregiver ADD COLUMN username TEXT");
                    needsAlter = true;
                }

                columns = conn.getMetaData().getColumns(null, null, "caregiver", "password");
                boolean passwordExists = columns.next();
                columns.close();

                if (!passwordExists) {
                    System.out.println("Spalte 'password' fehlt. Füge hinzu...");
                    stmt.executeUpdate("ALTER TABLE caregiver ADD COLUMN password TEXT");
                    needsAlter = true;
                }

                if (needsAlter) {
                    System.out.println("Spalten wurden zur Tabelle hinzugefügt.");
                }
            }

            stmt.close();
        } catch (SQLException e) {
            System.err.println("Fehler bei der Tabellenerstellung: " + e.getMessage());
            e.printStackTrace();
        }

        String surname = this.textFieldSurname.getText();
        String firstName = this.textFieldFirstName.getText();
        String telephone = this.textFieldTelephone.getText();

        // Prüfen, ob alle Felder ausgefüllt sind
        if (surname.isBlank() || firstName.isBlank() || telephone.isBlank()) {
            System.err.println("FEHLER: Nicht alle erforderlichen Felder sind ausgefüllt!");
            return;
        }

        System.out.println("Versuche, Pfleger hinzuzufügen: " + firstName + " " + surname + ", Tel: " + telephone);

        try {
            // Dialog für Benutzername und Passwort erstellen
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Benutzerinformationen");
            dialog.setHeaderText("Bitte geben Sie Benutzername und Passwort für den neuen Pfleger ein");

            // Buttons einrichten
            ButtonType confirmButtonType = new ButtonType("Bestätigen", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

            // Layout erstellen
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            // Vorgeschlagener Benutzername aus Vor- und Nachnamen
            String suggestedUsername = (firstName.toLowerCase().charAt(0) + 
                                     surname.toLowerCase().replace(" ", ""));

            TextField usernameField = new TextField();
            usernameField.setText(suggestedUsername);
            PasswordField passwordField = new PasswordField();

            grid.add(new Label("Benutzername:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Passwort:"), 0, 1);
            grid.add(passwordField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Fokus auf das Passwortfeld setzen
            Platform.runLater(() -> passwordField.requestFocus());

            // Ergebnis konvertieren
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    return new Pair<>(usernameField.getText(), passwordField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            if (!result.isPresent()) {
                System.out.println("Dialog abgebrochen.");
                return; // Abbruch, wenn der Benutzer auf Abbrechen klickt
            }

            String username = result.get().getKey();
            String password = result.get().getValue();

            // Validierung des Benutzernamens
            if (username == null || username.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler");
                alert.setHeaderText("Ungültiger Benutzername");
                alert.setContentText("Bitte geben Sie einen gültigen Benutzernamen ein.");
                alert.showAndWait();
                return;
            }

            // Caregiver mit eingegebenen Werten erstellen
            Caregiver caregiver = new Caregiver(firstName, surname, telephone);
            caregiver.setUsername(username);
            caregiver.setPassword(password);

            System.out.println("Rufe create-Methode auf mit Caregiver: " + caregiver);

            // Prüfen, welche Spalten tatsächlich in der Tabelle existieren
            Connection conn = ConnectionBuilder.getConnection();
            boolean hasUsernamePassword = false;

            try {
                // Prüfen, ob die Spalten username und password existieren
                ResultSet columns = conn.getMetaData().getColumns(null, null, "caregiver", "username");
                hasUsernamePassword = columns.next();
                columns.close();
            } catch (SQLException e) {
                System.out.println("Fehler beim Prüfen der Spalten: " + e.getMessage());
            }

            String sql;
            PreparedStatement ps;

            if (hasUsernamePassword) {
                sql = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, firstName);
                ps.setString(2, surname);
                ps.setString(3, telephone);
                ps.setString(4, username);
                ps.setString(5, password);
            } else {
                // Wenn die Spalten nicht existieren, nur die Grunddaten einfügen
                sql = "INSERT INTO caregiver (firstname, surname, telephone) VALUES (?, ?, ?)"; 
                ps = conn.prepareStatement(sql);
                ps.setString(1, firstName);
                ps.setString(2, surname);
                ps.setString(3, telephone);
            }

            int affected = ps.executeUpdate();
            System.out.println("Anzahl betroffener Zeilen: " + affected);
            ps.close();

            // SQLite unterstützt getGeneratedKeys() nicht - stattdessen letzte erzeugte ID direkt abfragen
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            long newId = -1;
            if (rs.next()) {
                newId = rs.getLong(1);
                System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + newId);
            }
            rs.close();
            stmt.close();

            // Erstellen eines entsprechenden User-Eintrags für die Anmeldung
            try {
                // Prüfen, ob ein Benutzer mit diesem Benutzernamen bereits existiert
                UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                User existingUser = userDao.findByUsername(username);

                if (existingUser == null) {
                    // Benutzer existiert noch nicht, erstellen
                    User newUser = new User(username, password, firstName, surname, "", telephone, UserRole.CAREGIVER);
                    if (newId > 0) {
                        newUser.setCaregiverId(newId); // Verknüpfung zum Caregiver herstellen
                    } else {
                        System.err.println("Warnung: Ungültige Caregiver-ID (" + newId + "). Verknüpfung kann nicht hergestellt werden.");
                    }

                    User createdUser = userDao.createUser(newUser);
                    if (createdUser != null) {
                        System.out.println("Benutzer erfolgreich erstellt: " + createdUser.getUsername());
                    } else {
                        System.err.println("Fehler beim Erstellen des Benutzers");
                    }
                } else {
                    System.out.println("Benutzer mit diesem Benutzernamen existiert bereits. Aktualisiere Verknüpfung...");
                    // Benutzer existiert bereits, Verknüpfung aktualisieren
                    if (existingUser != null && newId > 0) {
                        existingUser.setCaregiverId(newId);
                        userDao.updateUser(existingUser);
                    } else if (existingUser == null) {
                        System.err.println("Fehler: Benutzer mit Benutzernamen '" + username + "' konnte nicht gefunden werden.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Erstellen des Benutzers: " + e.getMessage());
                e.printStackTrace();
            }

            // Aktualisieren der Tabelle und Textfelder leeren
            readAllAndShowInTableView();
            clearTextfields();

        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Hinzufügen des Pflegers: " + e.getMessage());
            e.printStackTrace();

            // Detaillierte Fehlerbeschreibung für SQL-Fehler
            if (e.getMessage() != null) {
                if (e.getMessage().contains("no column named")) {
                    System.err.println("Es fehlt eine Spalte in der Datenbank. Versuche, die Tabelle neu zu erstellen...");
                    try {
                        // Versuch, die Tabelle neu zu erstellen
                        this.dao.createTable();
                        System.out.println("Tabelle wurde neu erstellt. Versuche erneut, den Pfleger hinzuzufügen...");

                        // Erneuter Versuch nach Tabellenerstellung
                        Caregiver caregiver = new Caregiver(firstName, surname, telephone);
                        String username = (firstName.toLowerCase().charAt(0) + surname.toLowerCase().replace(" ", ""));
                        caregiver.setUsername(username);
                        caregiver.setPassword("");

                        long newId = this.dao.create(caregiver);
                        System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + newId);

                        readAllAndShowInTableView();
                        clearTextfields();
                    } catch (Exception ex) {
                        System.err.println("Erneuter Versuch fehlgeschlagen: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else if (e.getMessage().contains("PreparedStatement ist null")) {
                    System.err.println("PreparedStatement ist null. Versuche direkten SQL-Ansatz...");
                    try {

                        java.sql.Connection conn = ConnectionBuilder.getConnection();
                        String sql = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)"; 
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, firstName);
                        ps.setString(2, surname);
                        ps.setString(3, telephone);
                        ps.setString(4, firstName.toLowerCase().charAt(0) + surname.toLowerCase().replace(" ", ""));
                        ps.setString(5, "");

                        ps.executeUpdate();
                        ps.close();

                        // Letzten Eintrag abrufen
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                        if (rs.next()) {
                            System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + rs.getLong(1));
                        }
                        rs.close();
                        stmt.close();

                        readAllAndShowInTableView();
                        clearTextfields();
                    } catch (Exception ex) {
                        System.err.println("Direkter SQL-Ansatz fehlgeschlagen: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        } catch (NullPointerException e) {
            System.err.println("NullPointer Exception beim Hinzufügen des Pflegers: " + e.getMessage());
            e.printStackTrace();

            // Versuche einen alternativen Ansatz bei NullPointerException
            try {
                System.out.println("Versuche alternativen Ansatz nach NullPointerException...");
                // Neu verbinden und neu versuchen
                this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
                this.dao.createTable();

                // Direkter SQL-Ansatz
                java.sql.Connection conn = ConnectionBuilder.getConnection();
                String sql = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)"; 
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, firstName);
                ps.setString(2, surname);
                ps.setString(3, telephone);
                ps.setString(4, firstName.toLowerCase().charAt(0) + surname.toLowerCase().replace(" ", ""));
                ps.setString(5, "");

                ps.executeUpdate();
                ps.close();

                // Letzten Eintrag abrufen
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + rs.getLong(1));
                }
                rs.close();
                stmt.close();

                readAllAndShowInTableView();
                clearTextfields();
            } catch (Exception ex) {
                System.err.println("Alternativer Ansatz fehlgeschlagen: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler beim Hinzufügen des Pflegers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears all contents from all <code>TextField</code>s.
     */
    private void clearTextfields() {
        this.textFieldFirstName.clear();
        this.textFieldSurname.clear();
        this.textFieldTelephone.clear();
    }

    private boolean areInputDataValid() {
        return !this.textFieldFirstName.getText().isBlank() &&
               !this.textFieldSurname.getText().isBlank() &&
               !this.textFieldTelephone.getText().isBlank();
    }

    /**
     * Fügt einen Abmelde-Button zur Benutzeroberfläche hinzu
     */
    private void addLogoutButton() {
        try {
            // Referenz zur Szene holen (wenn verfügbar)
            if (tableView.getScene() == null) {
                // Wenn wir noch keine Szene haben, später initialisieren
                tableView.sceneProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        createLogoutButton(newValue);
                    }
                });
            } else {
                createLogoutButton(tableView.getScene());
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Hinzufügen des Abmelde-Buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Erstellt den Abmelde-Button in der Szene
     * @param scene Die aktuelle Szene
     */
    private void createLogoutButton(javafx.scene.Scene scene) {
        try {
            // Nach der Toolbar in der Szene suchen
            javafx.scene.Node toolbar = scene.lookup("#toolbar"); // Annahme: Die Toolbar hat die ID "toolbar"

            if (toolbar instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox toolbarBox = (javafx.scene.layout.HBox) toolbar;

                // Prüfen, ob bereits ein Logout-Button existiert
                boolean logoutButtonExists = false;
                for (javafx.scene.Node node : toolbarBox.getChildren()) {
                    if (node instanceof javafx.scene.control.Button && "logoutButton".equals(node.getId())) {
                        logoutButtonExists = true;
                        break;
                    }
                }

                if (!logoutButtonExists) {
                    // Logout-Button erstellen
                    Button logoutButton = new Button("Abmelden");
                    logoutButton.setId("logoutButton");
                    logoutButton.setOnAction((javafx.event.ActionEvent e) -> handleLogout(e));

                    // Rechts ausrichten mit Spacer
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // Hinzufügen zum Ende der Toolbar
                    toolbarBox.getChildren().addAll(spacer, logoutButton);
                }
            } else if (scene.getRoot() instanceof javafx.scene.layout.BorderPane) {
                // Wenn keine Toolbar gefunden, aber BorderPane existiert, eigene Toolbar erstellen
                javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) scene.getRoot();

                // Prüfen, ob bereits ein Top-Element existiert
                javafx.scene.Node existingTop = root.getTop();
                javafx.scene.layout.HBox toolbarBox;

                if (existingTop instanceof javafx.scene.layout.HBox) {
                    toolbarBox = (javafx.scene.layout.HBox) existingTop;
                } else {
                    // Neue Toolbar erstellen
                    toolbarBox = new javafx.scene.layout.HBox(10);
                    toolbarBox.setPadding(new javafx.geometry.Insets(10));
                    toolbarBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

                    // Wenn bereits ein Element existiert, füge es hinzu
                    if (existingTop != null) {
                        toolbarBox.getChildren().add(existingTop);
                    }

                    root.setTop(toolbarBox);
                }

                // Prüfen, ob bereits ein Logout-Button existiert
                boolean logoutButtonExists = false;
                for (javafx.scene.Node node : toolbarBox.getChildren()) {
                    if (node instanceof javafx.scene.control.Button && "logoutButton".equals(node.getId())) {
                        logoutButtonExists = true;
                        break;
                    }
                }

                if (!logoutButtonExists) {
                    // Label für den aktuellen Benutzer erstellen
                    Label usernameLabel = new Label();
                    usernameLabel.setId("usernameLabel");

                    // Aktuellen Benutzernamen anzeigen
                    User currentUser = AuthorizationManager.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String displayName = currentUser.getFullName();
                        String roleName = currentUser.isAdmin() ? "Administrator" : "Pfleger";
                        usernameLabel.setText("Angemeldet als: " + displayName + " (" + roleName + ")");
                    } else {
                        usernameLabel.setText("Nicht angemeldet");
                    }

                    // Logout-Button erstellen
                    Button logoutButton = new Button("Abmelden");
                    logoutButton.setId("logoutButton");
                    logoutButton.setOnAction((javafx.event.ActionEvent e) -> handleLogout(e));

                    // CSS für die Toolbar laden
                    try {
                        Scene currentScene = toolbarBox.getScene();
                        if (currentScene != null) {
                            String cssPath = getClass().getResource("/de/hitec/nhplus/css/toolbar.css").toExternalForm();
                            if (!currentScene.getStylesheets().contains(cssPath)) {
                                currentScene.getStylesheets().add(cssPath);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Fehler beim Laden der CSS-Datei: " + e.getMessage());
                    }

                    // Rechts ausrichten mit Spacer
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // Hinzufügen zum Ende der Toolbar
                    toolbarBox.getChildren().addAll(spacer, usernameLabel, logoutButton);
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Erstellen des Abmelde-Buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Behandelt den Klick auf den Abmelde-Button
     * @param event Das ActionEvent
     */
    @FXML
    private void handleLogout(javafx.event.ActionEvent event) {
        // Benutzer abmelden
        AuthorizationManager.getInstance().logout();

        try {
            // Login-Fenster anzeigen
            javafx.stage.Stage currentStage = (javafx.stage.Stage) tableView.getScene().getWindow();

            // MainWindowController mit korrekter Stage erstellen
            MainWindowController newMainController = new MainWindowController();
            newMainController.setPrimaryStage(currentStage);

            // Login-Fenster mit der LoginView-Klasse erstellen
            new de.hitec.nhplus.gui.LoginView(currentStage, newMainController);

        } catch (Exception e) {
            System.err.println("Fehler beim Anzeigen des Login-Fensters: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Aktuelle Anwendung schließen
            try {
                javafx.stage.Stage stage = (javafx.stage.Stage) tableView.getScene().getWindow();
                stage.close();
            } catch (Exception ex) {
                System.err.println("Fehler beim Schließen des Fensters: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}