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