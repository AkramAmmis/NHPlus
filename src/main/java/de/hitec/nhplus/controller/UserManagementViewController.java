package de.hitec.nhplus.controller;

import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.AuthorizationManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller für die Benutzeroberfläche der Benutzerverwaltung
 */
public class UserManagementViewController {

    @FXML
    private TableView<User> tableView;

    @FXML
    private TableColumn<User, Long> columnId;

    @FXML
    private TableColumn<User, String> columnUsername;

    @FXML
    private TableColumn<User, String> columnFirstName;

    @FXML
    private TableColumn<User, String> columnLastName;

    @FXML
    private TableColumn<User, String> columnEmail;

    @FXML
    private TableColumn<User, String> columnPhone;

    @FXML
    private TableColumn<User, UserRole> columnRole;

    @FXML
    private TextField textFieldUsername;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField textFieldFirstName;

    @FXML
    private TextField textFieldLastName;

    @FXML
    private TextField textFieldEmail;

    @FXML
    private TextField textFieldPhone;

    @FXML
    private ComboBox<UserRole> comboBoxRole;

    @FXML
    private Button buttonAdd;

    @FXML
    private Button buttonUpdate;

    @FXML
    private Button buttonDelete;

    private UserManagementController controller;
    private ObservableList<User> users = FXCollections.observableArrayList();
    private User selectedUser;

    /**
     * Initialisiert die Benutzeroberfläche
     */
    public void initialize() {
        // Controller erstellen
        this.controller = new UserManagementController();

        // Berechtigungsprüfung - nur Admins dürfen die Benutzerverwaltung nutzen
        if (!AuthorizationManager.getInstance().isAdmin()) {
            disableControls();
            showAlert("Keine Berechtigung", "Sie haben keine Berechtigung für die Benutzerverwaltung.");
            return;
        }

        // TableView konfigurieren
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("uid"));
        this.columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.columnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        this.columnPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        this.columnRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        // ComboBox für Rollen füllen
        this.comboBoxRole.getItems().addAll(UserRole.values());

        // Benutzer laden
        loadUsers();

        // TableView-Auswahl überwachen
        this.tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends User> observable, User oldValue, User newValue) {
                if (newValue != null) {
                    selectedUser = newValue;
                    fillFormWithUser(newValue);
                    buttonDelete.setDisable(false);
                    buttonUpdate.setDisable(false);

                    // Admin-Benutzer kann nicht gelöscht werden
                    if ("admin".equals(newValue.getUsername())) {
                        buttonDelete.setDisable(true);
                    }
                } else {
                    selectedUser = null;
                    clearForm();
                    buttonDelete.setDisable(true);
                    buttonUpdate.setDisable(true);
                }
            }
        });

        // Initial Buttons deaktivieren
        this.buttonDelete.setDisable(true);
        this.buttonUpdate.setDisable(true);
    }

    /**
     * Deaktiviert alle Steuerelemente
     */
    private void disableControls() {
        this.tableView.setDisable(true);
        this.textFieldUsername.setDisable(true);
        this.passwordField.setDisable(true);
        this.textFieldFirstName.setDisable(true);
        this.textFieldLastName.setDisable(true);
        this.textFieldEmail.setDisable(true);
        this.textFieldPhone.setDisable(true);
        this.comboBoxRole.setDisable(true);
        this.buttonAdd.setDisable(true);
        this.buttonUpdate.setDisable(true);
        this.buttonDelete.setDisable(true);
    }

    /**
     * Lädt alle Benutzer
     */
    private void loadUsers() {
        this.users.clear();
        this.users.addAll(this.controller.getAllUsers());
        this.tableView.setItems(this.users);
    }

    /**
     * Füllt das Formular mit den Daten eines Benutzers
     * @param user Der anzuzeigende Benutzer
     */
    private void fillFormWithUser(User user) {
        this.textFieldUsername.setText(user.getUsername());
        this.passwordField.setText(""); // Passwort nicht anzeigen
        this.textFieldFirstName.setText(user.getFirstName());
        this.textFieldLastName.setText(user.getLastName());
        this.textFieldEmail.setText(user.getEmail());
        this.textFieldPhone.setText(user.getPhoneNumber());
        this.comboBoxRole.setValue(user.getRole());
    }

    /**
     * Leert das Formular
     */
    private void clearForm() {
        this.textFieldUsername.clear();
        this.passwordField.clear();
        this.textFieldFirstName.clear();
        this.textFieldLastName.clear();
        this.textFieldEmail.clear();
        this.textFieldPhone.clear();
        this.comboBoxRole.setValue(null);
    }

    /**
     * Verarbeitet das Hinzufügen eines Benutzers
     */
    @FXML
    private void handleAdd() {
        // Eingaben validieren
        if (!validateInput(true)) {
            return;
        }

        // Prüfen, ob der Benutzername bereits vergeben ist
        if (controller.isUsernameAlreadyTaken(textFieldUsername.getText())) {
            showAlert("Fehler", "Der Benutzername ist bereits vergeben.");
            return;
        }

        // Benutzer erstellen
        User newUser = controller.createUser(
            textFieldUsername.getText(),
            passwordField.getText(),
            textFieldFirstName.getText(),
            textFieldLastName.getText(),
            textFieldEmail.getText(),
            textFieldPhone.getText(),
            comboBoxRole.getValue()
        );

        if (newUser != null) {
            // Benutzer zur Tabelle hinzufügen
            users.add(newUser);
            clearForm();
            showAlert("Erfolg", "Der Benutzer wurde erfolgreich erstellt.");
        } else {
            showAlert("Fehler", "Der Benutzer konnte nicht erstellt werden.");
        }
    }

    /**
     * Verarbeitet das Aktualisieren eines Benutzers
     */
    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            showAlert("Fehler", "Bitte wählen Sie einen Benutzer aus.");
            return;
        }

        // Eingaben validieren
        if (!validateInput(false)) {
            return;
        }

        // Benutzername darf nicht geändert werden
        if (!selectedUser.getUsername().equals(textFieldUsername.getText())) {
            showAlert("Fehler", "Der Benutzername kann nicht geändert werden.");
            return;
        }

        // Benutzer aktualisieren
        selectedUser.setFirstName(textFieldFirstName.getText());
        selectedUser.setLastName(textFieldLastName.getText());
        selectedUser.setEmail(textFieldEmail.getText());
        selectedUser.setPhoneNumber(textFieldPhone.getText());
        selectedUser.setRole(comboBoxRole.getValue());

        // Passwort nur aktualisieren, wenn ein neues eingegeben wurde
        if (!passwordField.getText().isEmpty()) {
            selectedUser.setPassword(passwordField.getText());
        }

        // Benutzer in der Datenbank aktualisieren
        if (controller.updateUser(selectedUser)) {
            // Tabelle aktualisieren
            tableView.refresh();
            clearForm();
            selectedUser = null;
            tableView.getSelectionModel().clearSelection();
            buttonUpdate.setDisable(true);
            buttonDelete.setDisable(true);
            showAlert("Erfolg", "Der Benutzer wurde erfolgreich aktualisiert.");
        } else {
            showAlert("Fehler", "Der Benutzer konnte nicht aktualisiert werden.");
        }
    }

    /**
     * Verarbeitet das Löschen eines Benutzers
     */
    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showAlert("Fehler", "Bitte wählen Sie einen Benutzer aus.");
            return;
        }

        // Admin-Benutzer kann nicht gelöscht werden
        if ("admin".equals(selectedUser.getUsername())) {
            showAlert("Fehler", "Der Administrator-Benutzer kann nicht gelöscht werden.");
            return;
        }

        // Benutzer löschen
        if (controller.deleteUser(selectedUser)) {
            // Benutzer aus der Tabelle entfernen
            users.remove(selectedUser);
            clearForm();
            selectedUser = null;
            buttonDelete.setDisable(true);
            buttonUpdate.setDisable(true);
            showAlert("Erfolg", "Der Benutzer wurde erfolgreich gelöscht.");
        } else {
            showAlert("Fehler", "Der Benutzer konnte nicht gelöscht werden.");
        }
    }

    /**
     * Validiert die Eingaben im Formular
     * @param isNewUser true wenn ein neuer Benutzer erstellt wird, false wenn ein Benutzer aktualisiert wird
     * @return true wenn alle Eingaben gültig sind, sonst false
     */
    private boolean validateInput(boolean isNewUser) {
        // Pflichtfelder prüfen
        if (textFieldUsername.getText().isEmpty() ||
            textFieldFirstName.getText().isEmpty() ||
            textFieldLastName.getText().isEmpty() ||
            comboBoxRole.getValue() == null) {
            showAlert("Fehler", "Bitte füllen Sie alle Pflichtfelder aus (Benutzername, Vorname, Nachname, Rolle).");
            return false;
        }

        // Bei neuem Benutzer muss ein Passwort eingegeben werden
        if (isNewUser && passwordField.getText().isEmpty()) {
            showAlert("Fehler", "Bitte geben Sie ein Passwort ein.");
            return false;
        }

        return true;
    }

    /**
     * Zeigt eine Meldung an
     * @param title Titel der Meldung
     * @param message Inhalt der Meldung
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
