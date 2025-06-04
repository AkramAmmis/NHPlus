<<<<<< Datenarchivierungssystem
package de.hitec.nhplus.controller;

import de.hitec.nhplus.archiving.ArchivingService;
import de.hitec.nhplus.archiving.CaregiverArchivingService;
import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Treatment;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The AllCaregiverController contains the entire logic of the caregiver view.
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
    private TableColumn<Caregiver, String> columnStatus;

    @FXML
    private Button buttonLock;

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
    private ArchivingService<Caregiver> archivingService;

    /**
     * Initializes the controller. This method is called after all FXML fields are initialized.
     * Sets up table columns, loads data, and configures event listeners.
     */
    public void initialize() {
        this.readAllAndShowInTableView();
        this.archivingService = new CaregiverArchivingService();

        // Configure table columns
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("cid"));
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        this.columnTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplayName"));

        // Display data
        this.tableView.setItems(this.caregivers);

        // Set row factory to apply styling for locked records
        this.tableView.setRowFactory(getRowFactory());

        // Configure buttons
        this.buttonLock.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldCaregiver, newCaregiver) -> {
            boolean isDisabled = newCaregiver == null;
            AllCaregiverController.this.buttonLock.setDisable(isDisabled || newCaregiver.getStatus() != RecordStatus.ACTIVE);
        });

        // Configure validation for new caregiver input
        this.buttonAdd.setDisable(true);
        ChangeListener<String> inputValidationListener = (observableValue, oldText, newText) ->
                AllCaregiverController.this.buttonAdd.setDisable(!AllCaregiverController.this.areInputDataValid());
        this.textFieldSurname.textProperty().addListener(inputValidationListener);
        this.textFieldFirstName.textProperty().addListener(inputValidationListener);
        this.textFieldTelephone.textProperty().addListener(inputValidationListener);

        // Run automatic deletion check
        checkForAutomaticDeletion();
    }

    /**
     * Creates a row factory for styling locked and deleted records.
     *
     * @return A row factory that applies appropriate styles
     */
    private Callback<TableView<Caregiver>, TableRow<Caregiver>> getRowFactory() {
        return tableView -> new TableRow<Caregiver>() {
            @Override
            protected void updateItem(Caregiver caregiver, boolean empty) {
                super.updateItem(caregiver, empty);

                if (caregiver == null || empty) {
                    setStyle("");
                } else if (caregiver.getStatus() == RecordStatus.LOCKED) {
                    setStyle("-fx-background-color: lightgray;");
                } else if (caregiver.getStatus() == RecordStatus.DELETED) {
                    setStyle("-fx-background-color: #ffcccc;");
                } else {
                    setStyle("");
                }
            }
        };
    }

    /**
     * Checks for records that should be automatically deleted based on the deletion rules.
     */
    private void checkForAutomaticDeletion() {
        try {
            CaregiverDao caregiverDao = DaoFactory.getDaoFactory().createCaregiverDAO();
            TreatmentDao treatmentDao = DaoFactory.getDaoFactory().createTreatmentDao();
            List<Caregiver> allCaregivers = caregiverDao.readAll();

            for (Caregiver caregiver : allCaregivers) {
                // Check if 10 years have passed since the caregiver was locked or last updated
                LocalDate deletionDate = caregiver.getStatusChangeDate().plusYears(10);

                if (deletionDate.isBefore(LocalDate.now()) || deletionDate.isEqual(LocalDate.now())) {
                    // Check if caregiver has any non-deleted treatments
                    List<Treatment> treatments = treatmentDao.readTreatmentsByCid(caregiver.getCid());
                    boolean hasActiveTreatments = false;

                    for (Treatment treatment : treatments) {
                        if (treatment.getStatus() != RecordStatus.DELETED) {
                            hasActiveTreatments = true;
                            break;
                        }
                    }

                    if (!hasActiveTreatments) {
                        // Mark for deletion if all related treatments are deleted
                        archivingService.deleteRecord(caregiver.getCid());
                    }
                }
            }

            // Refresh the view after potential deletions
            readAllAndShowInTableView();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Automatic Deletion Error", "Failed to check for records to delete automatically.");
        }
    }

    /**
     * Handles editing of the first name column.
     *
     * @param event The cell edit event
     */
    @FXML
    public void handleOnEditFirstname(TableColumn.CellEditEvent<Caregiver, String> event) {
        Caregiver caregiver = event.getRowValue();
        if (caregiver.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }

        caregiver.setFirstName(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * Handles editing of the surname column.
     *
     * @param event The cell edit event
     */
    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Caregiver, String> event) {
        Caregiver caregiver = event.getRowValue();
        if (caregiver.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }

        caregiver.setSurname(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * Handles editing of the telephone column.
     *
     * @param event The cell edit event
     */
    @FXML
    public void handleOnEditTelephone(TableColumn.CellEditEvent<Caregiver, String> event) {
        Caregiver caregiver = event.getRowValue();
        if (caregiver.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }

        caregiver.setTelephone(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * Updates a caregiver in the database.
     *
     * @param event The cell edit event containing the caregiver to update
     */
    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        try {
            this.dao.update(event.getRowValue());
        } catch (SQLException exception) {
            exception.printStackTrace();
            showErrorMessage("Database Error", "Failed to update the caregiver.");
        }
    }

    /**
     * Reloads all caregivers from the database and displays them in the table.
     */
    public void readAllAndShowInTableView() {
        this.caregivers.clear();
        this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
        try {
            this.caregivers.addAll(this.dao.readAll());
        } catch (SQLException exception) {
            exception.printStackTrace();
            showErrorMessage("Database Error", "Failed to load caregivers.");
        }
    }

    /**
     * Handles the lock button action. Locks the selected caregiver record.
     */
    @FXML
    public void handleLock() {
        Caregiver selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getStatus() == RecordStatus.ACTIVE) {
            try {
                // Check if this caregiver has active treatments
                TreatmentDao treatmentDao = DaoFactory.getDaoFactory().createTreatmentDao();
                List<Treatment> activeTreatments = treatmentDao.readTreatmentsByCid(selectedItem.getCid());

                // Filter to get only active treatments
                List<Treatment> activeRecords = new ArrayList<>();
                for (Treatment treatment : activeTreatments) {
                    if (treatment.getStatus() == RecordStatus.ACTIVE) {
                        activeRecords.add(treatment);
                    }
                }

                if (!activeRecords.isEmpty()) {
                    // Cannot lock if there are active treatments
                    showInfoMessage("Sperrung nicht möglich",
                            "Der Pfleger ist mit " + activeRecords.size() + " aktiven Behandlungen verknüpft.\n" +
                                    "Sie müssen erst alle verknüpften Behandlungen sperren, bevor Sie den Pfleger sperren können.");
                    return;
                }

                // If no active treatments, proceed with locking
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Datensatz sperren");
                alert.setHeaderText("Sind Sie sicher?");
                alert.setContentText("Möchten Sie den Pfleger mit ID " + selectedItem.getCid() + " sperren?\n" +
                        "Gesperrte Datensätze können angezeigt, aber nicht bearbeitet werden.");

                ButtonType buttonTypeYes = new ButtonType("Ja");
                ButtonType buttonTypeNo = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == buttonTypeYes) {
                    boolean success = archivingService.lockRecord(selectedItem.getCid());

                    if (success) {
                        readAllAndShowInTableView();
                        showInfoMessage("Erfolgreich gesperrt", "Der Pflegerdatensatz wurde erfolgreich gesperrt.");
                    } else {
                        showErrorMessage("Fehler", "Der Pflegerdatensatz konnte nicht gesperrt werden.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorMessage("Datenbankfehler", "Beim Zugriff auf die Behandlungsdaten ist ein Fehler aufgetreten.");
            }
        }
    }

    /**
     * Handles the add button action. Creates a new caregiver from the input fields.
     */
    @FXML
    public void handleAdd() {
        String surname = this.textFieldSurname.getText();
        String firstName = this.textFieldFirstName.getText();
        String telephone = this.textFieldTelephone.getText();

        try {
            Caregiver newCaregiver = new Caregiver(firstName, surname, telephone);
            this.dao.create(newCaregiver);
            readAllAndShowInTableView();
            clearTextfields();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Datenbankfehler", "Ein Datenbankfehler ist aufgetreten. Der Pfleger konnte nicht hinzugefügt werden.");
        }
    }

    /**
     * Clears all input fields.
     */
    private void clearTextfields() {
        this.textFieldFirstName.clear();
        this.textFieldSurname.clear();
        this.textFieldTelephone.clear();
    }

    /**
     * Validates if all required input fields are filled.
     *
     * @return true if input data is valid, false otherwise
     */
    private boolean areInputDataValid() {
        return !this.textFieldFirstName.getText().isBlank() &&
                !this.textFieldSurname.getText().isBlank() &&
                !this.textFieldTelephone.getText().isBlank();
    }

    /**
     * Shows an error message in a dialog window.
     *
     * @param title The title of the dialog window
     * @param message The message to display
     */
    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information message in a dialog window.
     *
     * @param title The title of the dialog window
     * @param message The message to display
     */
    private void showInfoMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a warning message in a dialog window.
     *
     * @param title The title of the dialog window
     * @param header The header text
     * @param message The message to display
     */
    private void showWarningMessage(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

=======

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
>>>>>> FixMerge