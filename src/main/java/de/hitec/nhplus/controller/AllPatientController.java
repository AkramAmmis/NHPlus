package de.hitec.nhplus.controller;

import de.hitec.nhplus.archiving.ArchivingService;
import de.hitec.nhplus.archiving.PatientArchivingService;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Treatment;
import de.hitec.nhplus.utils.AuthorizationManager;
import de.hitec.nhplus.utils.DateConverter;
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
 * The AllPatientController contains the entire logic of the patient view.
 * It determines which data is displayed and how to react to events.
 */

public class AllPatientController {
    @FXML
    private TableView<Patient> tableView;
    @FXML
    private TableColumn<Patient, Integer> columnId;
    @FXML
    private TableColumn<Patient, String> columnFirstName;
    @FXML
    private TableColumn<Patient, String> columnSurname;
    @FXML
    private TableColumn<Patient, String> columnDateOfBirth;
    @FXML
    private TableColumn<Patient, String> columnCareLevel;
    @FXML
    private TableColumn<Patient, String> columnRoomNumber;
    @FXML
    private TableColumn<Patient, String> columnStatus;
    @FXML
    private Button buttonLock;
    @FXML
    private Button buttonAdd;
    @FXML
    private TextField textFieldSurname;
    @FXML
    private TextField textFieldFirstName;
    @FXML
    private TextField textFieldDateOfBirth;
    @FXML
    private TextField textFieldCareLevel;
    @FXML
    private TextField textFieldRoomNumber;

    private final ObservableList<Patient> patients = FXCollections.observableArrayList();
    private PatientDao dao;
    private ArchivingService<Patient> archivingService;


    /**
     * Initializes the controller. This method is called after all FXML fields are initialized.
     * Sets up table columns, loads data, and configures event listeners.
     */
    public void initialize() {
        this.readAllAndShowInTableView();
        this.archivingService = new PatientArchivingService();

        // Configure table columns
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("pid"));
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        this.columnDateOfBirth.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnCareLevel.setCellValueFactory(new PropertyValueFactory<>("careLevel"));
        this.columnCareLevel.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        this.columnRoomNumber.setCellFactory(TextFieldTableCell.forTableColumn());


        // Set up status column if available in the view
        if (this.columnStatus != null) {
            this.columnStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplayName"));
        }

        // Display data
        this.tableView.setItems(this.patients);

        // Set row factory to apply styling for locked records
        this.tableView.setRowFactory(getRowFactory());

        boolean isAdmin = AuthorizationManager.getInstance().isAdmin();

        // Configure buttons
        this.buttonLock.setDisable(true);
        this.buttonLock.setVisible(isAdmin);

        // Add listeners for button activation/deactivation
        this.tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldPatient, newPatient) -> {
            boolean isDisabled = newPatient == null;
            // Only enable lock button if record is active
            if (buttonLock != null) {
                AllPatientController.this.buttonLock.setDisable(isDisabled ||
                        (newPatient != null && newPatient.getStatus() != RecordStatus.ACTIVE));
            }
        });


        // Configure validation for new patient input
        this.buttonAdd.setDisable(true);
        ChangeListener<String> inputValidationListener = (observableValue, oldText, newText) ->
                AllPatientController.this.buttonAdd.setDisable(!AllPatientController.this.areInputDataValid());
        this.textFieldSurname.textProperty().addListener(inputValidationListener);
        this.textFieldFirstName.textProperty().addListener(inputValidationListener);
        this.textFieldDateOfBirth.textProperty().addListener(inputValidationListener);
        this.textFieldCareLevel.textProperty().addListener(inputValidationListener);
        this.textFieldRoomNumber.textProperty().addListener(inputValidationListener);

        // Run automatic deletion check
        checkForAutomaticDeletion();
    }

    /**
     * Creates a row factory for styling locked and deleted records.
     *
     * @return A row factory that applies appropriate styles
     */
    private Callback<TableView<Patient>, TableRow<Patient>> getRowFactory() {
        return tableView -> new TableRow<Patient>() {
            @Override
            protected void updateItem(Patient patient, boolean empty) {
                super.updateItem(patient, empty);

                if (patient == null || empty) {
                    setStyle("");
                } else if (patient.getStatus() == RecordStatus.LOCKED) {
                    setStyle("-fx-background-color: lightgray;");
                } else if (patient.getStatus() == RecordStatus.DELETED) {
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
            PatientDao patientDao = DaoFactory.getDaoFactory().createPatientDAO();
            TreatmentDao treatmentDao = DaoFactory.getDaoFactory().createTreatmentDao();
            List<Patient> allPatients = patientDao.readAll();

            for (Patient patient : allPatients) {
                // Skip patients without status change date
                if (patient.getStatusChangeDate() == null) {
                    continue;
                }

                // Check if 10 years have passed since the patient was locked or last updated
                LocalDate deletionDate = patient.getStatusChangeDate().plusYears(10);

                if (deletionDate.isBefore(LocalDate.now()) || deletionDate.isEqual(LocalDate.now())) {
                    // Check if patient has any non-deleted treatments
                    List<Treatment> treatments = treatmentDao.readTreatmentsByPid(patient.getPid());
                    boolean hasActiveTreatments = false;

                    for (Treatment treatment : treatments) {
                        if (treatment.getStatus() != RecordStatus.DELETED) {
                            hasActiveTreatments = true;
                            break;
                        }
                    }

                    if (!hasActiveTreatments) {
                        // Mark for deletion if all related treatments are deleted
                        archivingService.deleteRecord(patient.getPid());
                    }
                }
            }

            // Refresh the view after potential deletions
            readAllAndShowInTableView();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Automatic Deletion Error", "Failed to check for records to delete automatically.");
        } catch (Exception e) {
            e.printStackTrace();
            // Keine Meldung, falls das Archivierungssystem nicht verfügbar ist
        }
    }

    @FXML
    public void handleOnEditFirstname(TableColumn.CellEditEvent<Patient, String> event) {
        Patient patient = event.getRowValue();
        // Check if editing is allowed (status check)
        if (patient.getStatus() != null && patient.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }
        patient.setFirstName(event.getNewValue());
        this.doUpdate(event);
    }

    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Patient, String> event) {
        Patient patient = event.getRowValue();
        // Check if editing is allowed (status check)
        if (patient.getStatus() != null && patient.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }
        patient.setSurname(event.getNewValue());
        this.doUpdate(event);
    }

    @FXML
    public void handleOnEditDateOfBirth(TableColumn.CellEditEvent<Patient, String> event) {
        Patient patient = event.getRowValue();
        // Check if editing is allowed (status check)
        if (patient.getStatus() != null && patient.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }
        patient.setDateOfBirth(event.getNewValue());
        this.doUpdate(event);
    }

    @FXML
    public void handleOnEditCareLevel(TableColumn.CellEditEvent<Patient, String> event) {
        Patient patient = event.getRowValue();
        // Check if editing is allowed (status check)
        if (patient.getStatus() != null && patient.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }
        patient.setCareLevel(event.getNewValue());
        this.doUpdate(event);
    }

    @FXML
    public void handleOnEditRoomNumber(TableColumn.CellEditEvent<Patient, String> event) {
        Patient patient = event.getRowValue();
        // Check if editing is allowed (status check)
        if (patient.getStatus() != null && patient.getStatus() != RecordStatus.ACTIVE) {
            showErrorMessage("Edit not possible", "This record is locked or deleted and cannot be edited.");
            readAllAndShowInTableView();
            return;
        }
        patient.setRoomNumber(event.getNewValue());
        this.doUpdate(event);
    }

    private void doUpdate(TableColumn.CellEditEvent<Patient, String> event) {
        try {
            this.dao.update(event.getRowValue());
        } catch (SQLException exception) {
            exception.printStackTrace();
            showErrorMessage("Database Error", "Failed to update the patient.");
        }
    }

    private void readAllAndShowInTableView() {
        this.patients.clear();
        this.dao = DaoFactory.getDaoFactory().createPatientDAO();
        try {
            this.patients.addAll(this.dao.readAll());
        } catch (SQLException exception) {
            exception.printStackTrace();
            showErrorMessage("Database Error", "Failed to load patients.");
        }
    }

    /**
     * Handles the lock button action. Locks the selected patient record.
     */

    @FXML
    public void handleLock() {
        Patient selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null &&
                (selectedItem.getStatus() == null || selectedItem.getStatus() == RecordStatus.ACTIVE)) {
            try {
                // Check if this patient has active treatments
                TreatmentDao treatmentDao = DaoFactory.getDaoFactory().createTreatmentDao();
                List<Treatment> activeTreatments = treatmentDao.readTreatmentsByPid(selectedItem.getPid());

                // Filter to get only active treatments
                List<Treatment> activeRecords = new ArrayList<>();
                for (Treatment treatment : activeTreatments) {
                    if (treatment.getStatus() == null || treatment.getStatus() == RecordStatus.ACTIVE) {
                        activeRecords.add(treatment);
                    }
                }

                if (!activeRecords.isEmpty()) {
                    // Cannot lock if there are active treatments
                    showInfoMessage("Sperrung nicht möglich",
                            "Der Patient ist mit " + activeRecords.size() + " aktiven Behandlungen verknüpft.\n" +
                                    "Sie müssen erst alle verknüpften Behandlungen sperren, bevor Sie den Patienten sperren können.");
                    return;
                }

                // If no active treatments, proceed with locking
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Datensatz sperren");
                alert.setHeaderText("Sind Sie sicher?");
                alert.setContentText("Möchten Sie den Patienten mit ID " + selectedItem.getPid() + " sperren?\n" +
                        "Gesperrte Datensätze können angezeigt, aber nicht bearbeitet werden.");

                ButtonType buttonTypeYes = new ButtonType("Ja");
                ButtonType buttonTypeNo = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == buttonTypeYes) {
                    boolean success = archivingService.lockRecord(selectedItem.getPid());

                    if (success) {
                        readAllAndShowInTableView();
                        showInfoMessage("Erfolgreich gesperrt", "Der Patientendatensatz wurde erfolgreich gesperrt.");
                    } else {
                        showErrorMessage("Fehler", "Der Patientendatensatz konnte nicht gesperrt werden.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showErrorMessage("Datenbankfehler", "Beim Zugriff auf die Behandlungsdaten ist ein Fehler aufgetreten.");
            }
        }
    }


    @FXML
    public void handleAdd() {
        String surname = this.textFieldSurname.getText();
        String firstName = this.textFieldFirstName.getText();
        String dateOfBirth = this.textFieldDateOfBirth.getText();
        String careLevel = this.textFieldCareLevel.getText();
        String roomNumber = this.textFieldRoomNumber.getText();

        try {
            LocalDate dateOfBirthLocalDate = DateConverter.convertStringToLocalDate(dateOfBirth);
            Patient newPatient = new Patient(firstName, surname, dateOfBirthLocalDate, careLevel, roomNumber);
            this.dao.create(newPatient);
            readAllAndShowInTableView();
            clearTextfields();
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorMessage("Database Error", "A database error occurred. The patient could not be added.");
        }
    }

    private void clearTextfields() {
        this.textFieldFirstName.clear();
        this.textFieldSurname.clear();
        this.textFieldDateOfBirth.clear();
        this.textFieldCareLevel.clear();
        this.textFieldRoomNumber.clear();
    }

    /**
     * Validates if all required input fields are filled.
     *
     * @return true if input data is valid, false otherwise
     */
    private boolean areInputDataValid() {
        return !this.textFieldFirstName.getText().isBlank() &&
                !this.textFieldSurname.getText().isBlank() &&
                !this.textFieldDateOfBirth.getText().isBlank() &&
                !this.textFieldCareLevel.getText().isBlank() &&
                !this.textFieldRoomNumber.getText().isBlank();
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
}