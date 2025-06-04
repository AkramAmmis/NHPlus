
package de.hitec.nhplus.controller;

import de.hitec.nhplus.Main;
import de.hitec.nhplus.archiving.ArchivingService;
import de.hitec.nhplus.archiving.TreatmentArchivingService;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Treatment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The AllTreatmentController contains the entire logic of the treatment view.
 * It determines which data is displayed and how to react to events.
 */
public class AllTreatmentController {

    @FXML
    private TableView<Treatment> tableView;

    @FXML
    private TableColumn<Treatment, Integer> columnId;

    @FXML
    private TableColumn<Treatment, String> columnPid;

    @FXML
    private TableColumn<Treatment, String> columnCid;

    @FXML
    private TableColumn<Treatment, String> columnDate;

    @FXML
    private TableColumn<Treatment, String> columnBegin;

    @FXML
    private TableColumn<Treatment, String> columnEnd;

    @FXML
    private TableColumn<Treatment, String> columnDescription;

    @FXML
    private TableColumn<Treatment, String> columnStatus;

    @FXML
    private ComboBox<String> comboBoxPatientSelection;

    @FXML
    private Button buttonLock;

    private ArchivingService<Treatment> archivingService;
    private final ObservableList<Treatment> treatments = FXCollections.observableArrayList();
    private TreatmentDao dao;
    private final ObservableList<String> patientSelection = FXCollections.observableArrayList();
    private ArrayList<Patient> patientList;

    /**
     * Initializes the controller. This method is called after all FXML fields are initialized.
     * Sets up table columns, loads data, and configures event listeners.
     */
    public void initialize() {
        readAllAndShowInTableView();
        comboBoxPatientSelection.setItems(patientSelection);
        comboBoxPatientSelection.getSelectionModel().select(0);
        this.archivingService = new TreatmentArchivingService();

        // Configure table columns
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("tid"));
        this.columnPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        this.columnCid.setCellValueFactory(new PropertyValueFactory<>("cid"));
        this.columnDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        this.columnBegin.setCellValueFactory(new PropertyValueFactory<>("begin"));
        this.columnEnd.setCellValueFactory(new PropertyValueFactory<>("end"));
        this.columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        this.columnStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplayName"));

        // Display data
        this.tableView.setItems(this.treatments);

        // Set row factory to apply styling for locked records
        this.tableView.setRowFactory(getRowFactory());

        // Configure buttons
        this.buttonLock.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, oldTreatment, newTreatment) -> {
                    boolean disableLock = newTreatment == null ||
                            newTreatment.getStatus() != RecordStatus.ACTIVE;

                    AllTreatmentController.this.buttonLock.setDisable(disableLock);
                });

        // Create patient combo box data
        this.createPatientComboBoxData();

        // Run automatic deletion check
        checkForAutomaticDeletion();
    }

    /**
     * Creates a row factory for styling locked and deleted records.
     *
     * @return A row factory that applies appropriate styles
     */
    private Callback<TableView<Treatment>, TableRow<Treatment>> getRowFactory() {
        return tableView -> new TableRow<Treatment>() {
            @Override
            protected void updateItem(Treatment treatment, boolean empty) {
                super.updateItem(treatment, empty);

                if (treatment == null || empty) {
                    setStyle("");
                } else if (treatment.getStatus() == RecordStatus.LOCKED) {
                    setStyle("-fx-background-color: lightgray;");
                } else if (treatment.getStatus() == RecordStatus.DELETED) {
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
            TreatmentDao treatmentDao = DaoFactory.getDaoFactory().createTreatmentDao();
            List<Treatment> allTreatments = treatmentDao.readAll();

            for (Treatment treatment : allTreatments) {
                // Check if the treatment should be deleted (10 years since lock or treatment date)
                LocalDate deletionDate = null;

                if (treatment.getStatus() == RecordStatus.LOCKED) {
                    deletionDate = treatment.getStatusChangeDate().plusYears(10);
                } else {
                    LocalDate treatmentDate = LocalDate.parse(treatment.getDate());
                    deletionDate = treatmentDate.plusYears(10);
                }

                if (deletionDate.isBefore(LocalDate.now()) || deletionDate.isEqual(LocalDate.now())) {
                    // Mark for deletion
                    archivingService.deleteRecord(treatment.getTid());
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
     * Reloads all treatments from the database and displays them in the table.
     */
    public void readAllAndShowInTableView() {
        this.treatments.clear();
        comboBoxPatientSelection.getSelectionModel().select(0);
        this.dao = DaoFactory.getDaoFactory().createTreatmentDao();
        try {
            List<Treatment> treatmentList = dao.readAll();
            this.treatments.setAll(treatmentList);
        } catch (SQLException exception) {
            exception.printStackTrace();
            showErrorMessage("Database Error", "Failed to load treatments.");
        }
    }

    /**
     * Populates the patient selection combo box with all available patients.
     */
    private void createPatientComboBoxData() {
        PatientDao dao = DaoFactory.getDaoFactory().createPatientDAO();
        try {
            patientList = (ArrayList<Patient>) dao.readAll();
            this.patientSelection.add("all");
            for (Patient patient: patientList) {
                this.patientSelection.add(patient.getSurname());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            showErrorMessage("Database Error", "Failed to load patient data for selection.");
        }
    }

    /**
     * Handles selection changes in the patient combo box.
     * Filters treatments based on the selected patient.
     */
    @FXML
    public void handlePatientComboBox() {
        String selectedPatient = this.comboBoxPatientSelection.getSelectionModel().getSelectedItem();
        this.treatments.clear();
        this.dao = DaoFactory.getDaoFactory().createTreatmentDao();

        if (selectedPatient.equals("all")) {
            try {
                this.treatments.addAll(this.dao.readAll());
            } catch (SQLException exception) {
                exception.printStackTrace();
                showErrorMessage("Database Error", "Failed to load all treatments.");
            }
        }

        Patient patient = searchInPatientList(selectedPatient);
        if (patient != null) {
            try {
                this.treatments.addAll(this.dao.readTreatmentsByPid(patient.getPid()));
            } catch (SQLException exception) {
                exception.printStackTrace();
                showErrorMessage("Database Error", "Failed to load treatments for the selected patient.");
            }
        }
    }

    /**
     * Searches for a patient by surname in the patient list.
     *
     * @param surname The surname to search for
     * @return The found patient or null if not found
     */
    private Patient searchInPatientList(String surname) {
        for (Patient patient : this.patientList) {
            if (patient.getSurname().equals(surname)) {
                return patient;
            }
        }
        return null;
    }

    /**
     * Handles the lock button action. Locks the selected treatment record.
     */
    @FXML
    public void handleLock() {
        Treatment treatment = this.tableView.getSelectionModel().getSelectedItem();
        if (treatment != null && treatment.getStatus() == RecordStatus.ACTIVE) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Datensatz sperren");
            alert.setHeaderText("Sind Sie sicher?");
            alert.setContentText("Möchten Sie die Behandlung mit ID " + treatment.getTid() + " sperren?\n" +
                    "Gesperrte Datensätze können angezeigt, aber nicht bearbeitet werden.");

            ButtonType buttonTypeYes = new ButtonType("Ja");
            ButtonType buttonTypeNo = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == buttonTypeYes) {
                boolean success = archivingService.lockRecord(treatment.getTid());

                if (success) {
                    readAllAndShowInTableView();
                    showInfoMessage("Erfolgreich gesperrt", "Die Behandlung wurde erfolgreich gesperrt.");
                } else {
                    showErrorMessage("Fehler", "Die Behandlung konnte nicht gesperrt werden.");
                }
            }
        }
    }

    /**
     * Handles the new treatment button action. Opens a window to create a new treatment.
     */
    @FXML
    public void handleNewTreatment() {
        try {
            String selectedPatient = this.comboBoxPatientSelection.getSelectionModel().getSelectedItem();
            Patient patient = searchInPatientList(selectedPatient);
            if (patient == null) {
                showInfoMessage("Information", "Bitte wählen Sie einen Patienten für die Behandlung aus der Liste aus!");
                return;
            }
            newTreatmentWindow(patient);
        } catch (NullPointerException exception) {
            showInfoMessage("Information", "Bitte wählen Sie einen Patienten für die Behandlung aus der Liste aus!");
        }
    }

    /**
     * Handles mouse click events on the table. Opens treatment details on double-click.
     */
    @FXML
    public void handleMouseClick() {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && (tableView.getSelectionModel().getSelectedItem() != null)) {
                Treatment treatment = this.tableView.getSelectionModel().getSelectedItem();
                treatmentWindow(treatment);
            }
        });
    }

    /**
     * Opens a window to create a new treatment for a patient.
     *
     * @param patient The patient for whom to create a treatment
     */
    public void newTreatmentWindow(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/NewTreatmentView.fxml"));
            AnchorPane pane = loader.load();
            Scene scene = new Scene(pane);

            // The primary stage should stay in the background
            Stage stage = new Stage();

            NewTreatmentController controller = loader.getController();
            controller.initialize(this, stage, patient);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
            showErrorMessage("Error", "Could not open new treatment window.");
        }
    }

    /**
     * Opens a window to view or edit treatment details.
     *
     * @param treatment The treatment to view or edit
     */
    public void treatmentWindow(Treatment treatment) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/TreatmentView.fxml"));
            AnchorPane pane = loader.load();
            Scene scene = new Scene(pane);

            // The primary stage should stay in the background
            Stage stage = new Stage();
            TreatmentController controller = loader.getController();
            controller.initializeController(this, stage, treatment);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
            showErrorMessage("Error", "Could not open treatment details window.");
        }
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