

package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.AuthorizationManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;


import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private Button buttonChangePassword;
    @FXML
    private TextField textFieldSurname;
    @FXML
    private TextField textFieldFirstName;
    @FXML
    private TextField textFieldTelephone;

    private final ObservableList<Caregiver> caregivers = FXCollections.observableArrayList();
    private CaregiverDao dao;
    private Stage primaryStage;
    private MainWindowController mainWindowController;

    public void initialize() {
        this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("cid"));
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Editierbare Spalten einrichten
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnTelephone.setCellFactory(TextFieldTableCell.forTableColumn());

        this.tableView.setItems(this.caregivers);
        readAllAndShowInTableView();

        // Prüfe, ob der Benutzer ein Administrator ist, direkt über den AuthorizationManager
        boolean isAdmin = AuthorizationManager.getInstance().isAdmin();
        this.buttonDelete.setDisable(true);
        this.buttonChangePassword.setDisable(true);

        if (isAdmin) {
            this.buttonChangePassword.setVisible(isAdmin);
            this.tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldCaregiver, newCaregiver) -> {
                AllCaregiverController.this.buttonDelete.setDisable(newCaregiver == null || !isAdmin);
                AllCaregiverController.this.buttonChangePassword.setDisable(newCaregiver == null || !isAdmin);
            });
        } else {
            this.tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldCaregiver, newCaregiver) ->
                    AllCaregiverController.this.buttonDelete.setDisable(newCaregiver == null || !isAdmin));
        }

        this.buttonAdd.setDisable(true);
        ChangeListener<String> inputNewCaregiverListener = (observableValue, oldText, newText) ->
                AllCaregiverController.this.buttonAdd.setDisable(!AllCaregiverController.this.areInputDataValid());
        this.textFieldSurname.textProperty().addListener(inputNewCaregiverListener);
        this.textFieldFirstName.textProperty().addListener(inputNewCaregiverListener);
        this.textFieldTelephone.textProperty().addListener(inputNewCaregiverListener);
    }

    @FXML
    public void handleOnEditFirstname(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setFirstName(event.getNewValue());
        doUpdate(event);
    }

    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setSurname(event.getNewValue());
        doUpdate(event);
    }

    @FXML
    public void handleOnEditTelephone(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setTelephone(event.getNewValue());
        doUpdate(event);
    }

    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        try {
            this.dao.update(event.getRowValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void readAllAndShowInTableView() {
        this.caregivers.clear();
        try {
            this.caregivers.addAll(this.dao.readAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private User findAssociatedUser(Caregiver caregiver) {
        // Implementierung für die Suche des zugehörigen Benutzers
        // Da der Caregiver jetzt direkt die Benutzerinformationen enthält,
        // können wir einfach einen neuen User erstellen
        if (caregiver.getUsername() != null && !caregiver.getUsername().isEmpty()) {
            return new User(caregiver.getUsername(), caregiver.getPassword(), UserRole.CAREGIVER);
        }
        return null;
    }

    @FXML
    public void handleChangePassword() {
        Caregiver selectedCaregiver = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedCaregiver != null) {
            // Dialog zum Passwort ändern anzeigen
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Passwort ändern");
            dialog.setHeaderText("Passwort für " + selectedCaregiver.getFullName() + " ändern");
            dialog.setContentText("Neues Passwort:");

            dialog.showAndWait().ifPresent(password -> {
                if (!password.isEmpty()) {
                    selectedCaregiver.setPassword(password);
                    try {
                        this.dao.update(selectedCaregiver);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showErrorAlert("Fehler", "Passwort konnte nicht geändert werden: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleDelete() {
        Caregiver selectedCaregiver = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedCaregiver != null) {
            this.dao.delete(selectedCaregiver);
            this.caregivers.remove(selectedCaregiver);

        }
    }

    @FXML
    public void handleAdd() {
        if (areInputDataValid()) {
            String firstname = this.textFieldFirstName.getText();
            String surname = this.textFieldSurname.getText();
            String telephone = this.textFieldTelephone.getText();

            // Erstelle einen einfachen Benutzernamen aus Vorname + ersten Buchstaben des Nachnamens
            String suggestedUsername = firstname.toLowerCase().charAt(0) + surname.toLowerCase();

            // Dialog für Benutzername und Passwort erstellen
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Benutzeraccount für Pfleger");
            dialog.setHeaderText("Bitte Benutzernamen bestätigen und Passwort eingeben für " + firstname + " " + surname);

            // Dialogbuttons definieren
            ButtonType loginButtonType = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

            // Dialog-Layout erstellen
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField usernameField = new TextField(suggestedUsername);
            PasswordField passwordField = new PasswordField();

            grid.add(new Label("Benutzername:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Passwort:"), 0, 1);
            grid.add(passwordField, 1, 1);

            // Button aktivieren/deaktivieren je nach Eingabefeldern
            Node saveButton = dialog.getDialogPane().lookupButton(loginButtonType);
            saveButton.setDisable(true);

            // Listener für Eingabefelder
            usernameField.textProperty().addListener((observable, oldValue, newValue) ->
                    saveButton.setDisable(newValue.trim().isEmpty() || passwordField.getText().trim().isEmpty()));

            passwordField.textProperty().addListener((observable, oldValue, newValue) ->
                    saveButton.setDisable(newValue.trim().isEmpty() || usernameField.getText().trim().isEmpty()));

            dialog.getDialogPane().setContent(grid);

            // Fokus auf Benutzernamen setzen
            Platform.runLater(usernameField::requestFocus);

            // Dialog-Ergebnis konvertieren
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == loginButtonType) {
                    return new Pair<>(usernameField.getText(), passwordField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();

            result.ifPresent(usernamePassword -> {
                try {
                    String username = usernamePassword.getKey();
                    String password = usernamePassword.getValue();

                    // Erstelle einen Pfleger mit den neuen Feldern
                    Caregiver caregiver = new Caregiver(0, username, password, firstname, surname, telephone);

                    this.dao.create(caregiver);
                    this.readAllAndShowInTableView();
                    clearTextfields();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Pfleger hinzugefügt");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Der Pfleger " + firstname + " " + surname +
                            " wurde erfolgreich hinzugefügt.\nBenutzername: " + username);
                    successAlert.showAndWait();

                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Fehler beim Hinzufügen", "Der Pfleger konnte nicht hinzugefügt werden: " + e.getMessage());
                }
            });
        }
    }


    private boolean areInputDataValid() {
        return !this.textFieldFirstName.getText().isEmpty() &&
                !this.textFieldSurname.getText().isEmpty() &&
                !this.textFieldTelephone.getText().isEmpty();
    }

    private void clearTextfields() {
        this.textFieldFirstName.clear();
        this.textFieldSurname.clear();
        this.textFieldTelephone.clear();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }
}
