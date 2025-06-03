package de.hitec.nhplus.controller;

import de.hitec.nhplus.Main;
import de.hitec.nhplus.archiving.ArchivingService;
import de.hitec.nhplus.archiving.TreatmentArchivingService;
import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.RecordStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.Treatment;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private ComboBox<String> comboBoxPatientSelection;

    @FXML
    private Button buttonDelete;

    @FXML
    private Button buttonLock;

    @FXML
    private TableColumn<Treatment, String> columnStatus;

    private ArchivingService<Treatment> archivingService;
    private final ObservableList<Treatment> treatments = FXCollections.observableArrayList();
    private TreatmentDao dao;
    private final ObservableList<String> patientSelection = FXCollections.observableArrayList();
    private ArrayList<Patient> patientList;


    public void initialize() {
        readAllAndShowInTableView();
        comboBoxPatientSelection.setItems(patientSelection);
        comboBoxPatientSelection.getSelectionModel().select(0);
        this.archivingService = new TreatmentArchivingService();


        this.columnStatus.setCellValueFactory(new PropertyValueFactory<>("statusDisplayName"));
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("tid"));
        this.columnPid.setCellValueFactory(new PropertyValueFactory<>("pid"));
        this.columnCid.setCellValueFactory(new PropertyValueFactory<>("cid"));
        this.columnDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        this.columnBegin.setCellValueFactory(new PropertyValueFactory<>("begin"));
        this.columnEnd.setCellValueFactory(new PropertyValueFactory<>("end"));
        this.columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        this.tableView.setItems(this.treatments);

        this.buttonLock.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, oldTreatment, newTreatment) -> {
                    boolean disableDelete = newTreatment == null ||
                            newTreatment.getStatus() != RecordStatus.ACTIVE;

                    boolean disableLock = newTreatment == null ||
                            newTreatment.getStatus() != RecordStatus.ACTIVE;

                    AllTreatmentController.this.buttonDelete.setDisable(disableDelete);
                    AllTreatmentController.this.buttonLock.setDisable(disableLock);
                });


        // Disabling the button to delete treatments as long, as no treatment was selected.
        this.buttonDelete.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, oldTreatment, newTreatment) ->
                        AllTreatmentController.this.buttonDelete.setDisable(newTreatment == null));

        this.createPatientComboBoxData();
    }

    public void readAllAndShowInTableView() {
        this.treatments.clear();
        comboBoxPatientSelection.getSelectionModel().select(0);
        this.dao = DaoFactory.getDaoFactory().createTreatmentDao();
        try {
            List<Treatment> treatmentList = dao.readAll();
            this.treatments.setAll(treatmentList);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void createPatientComboBoxData() {
        PatientDao dao = DaoFactory.getDaoFactory().createPatientDAO();
        try {
            patientList = (ArrayList<Patient>) dao.readAll();
            this.patientSelection.add("alle");
            for (Patient patient: patientList) {
                this.patientSelection.add(patient.getSurname());
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }


    @FXML
    public void handlePatientComboBox() {
        String selectedPatient = this.comboBoxPatientSelection.getSelectionModel().getSelectedItem();
        this.treatments.clear();
        this.dao = DaoFactory.getDaoFactory().createTreatmentDao();

        if (selectedPatient.equals("alle")) {
            try {
                this.treatments.addAll(this.dao.readAll());
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }

        Patient patient = searchInPatientList(selectedPatient);
        if (patient !=null) {
            try {
                this.treatments.addAll(this.dao.readTreatmentsByPid(patient.getPid()));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }


    private Patient searchInPatientList(String surname) {
        for (Patient patient : this.patientList) {
            if (patient.getSurname().equals(surname)) {
                return patient;
            }
        }
        return null;
    }

    @FXML
    public void handleLock() {
        int index = this.tableView.getSelectionModel().getSelectedIndex();
        Treatment treatment = this.treatments.get(index);

        boolean success = this.archivingService.lockRecord(treatment.getTid());

        if (success) {
            readAllAndShowInTableView();
        }
    }


    public void handleDelete() {
        int index = this.tableView.getSelectionModel().getSelectedIndex();
        Treatment treatment = this.treatments.get(index);

        // Check if the record can be deleted
        if (treatment.getStatus() == RecordStatus.LOCKED) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Treatment cannot be deleted");
            alert.setContentText("The selected treatment is locked and cannot be deleted.");
            alert.showAndWait();
            return;
        }

        // Check if the treatment is older than 10 years
        LocalDate treatmentDate = LocalDate.parse(treatment.getDate());
        if (treatmentDate.plusYears(10).isAfter(LocalDate.now())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Treatment cannot be deleted");
            alert.setContentText("The selected treatment is not yet 10 years old and therefore cannot be deleted.");
            alert.showAndWait();
            return;
        }

        // If everything is fine, proceed with deletion
        boolean success = this.archivingService.deleteRecord(treatment.getTid());

        if (success) {
            this.treatments.remove(index);
        }
    }


    @FXML
    public void handleNewTreatment() {
        try{
            String selectedPatient = this.comboBoxPatientSelection.getSelectionModel().getSelectedItem();
            Patient patient = searchInPatientList(selectedPatient);
            newTreatmentWindow(patient);
        } catch (NullPointerException exception){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Patient für die Behandlung fehlt!");
            alert.setContentText("Wählen Sie über die Combobox einen Patienten aus!");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleMouseClick() {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && (tableView.getSelectionModel().getSelectedItem() != null)) {
                int index = this.tableView.getSelectionModel().getSelectedIndex();
                Treatment treatment = this.treatments.get(index);
                treatmentWindow(treatment);
            }
        });
    }

    public void newTreatmentWindow(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/NewTreatmentView.fxml"));
            AnchorPane pane = loader.load();
            Scene scene = new Scene(pane);

            // the primary stage should stay in the background
            Stage stage = new Stage();

            NewTreatmentController controller = loader.getController();
            controller.initialize(this, stage, patient);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void treatmentWindow(Treatment treatment){
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/TreatmentView.fxml"));
            AnchorPane pane = loader.load();
            Scene scene = new Scene(pane);

            // the primary stage should stay in the background
            Stage stage = new Stage();
            TreatmentController controller = loader.getController();
            controller.initializeController(this, stage, treatment);

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
