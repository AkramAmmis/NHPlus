package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.RecordStatus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.Treatment;
import de.hitec.nhplus.utils.DateConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class TreatmentController {

    @FXML
    private Label labelPatientName;

    @FXML
    private Label labelCareLevel;

    @FXML
    private TextField textFieldBegin;

    @FXML
    private TextField textFieldEnd;

    @FXML
    private TextField textFieldDescription;

    @FXML
    private TextArea textAreaRemarks;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> comboBoxCaregiverSelection;

    @FXML
    private Label labelCaregiverNumber;

    @FXML
    private Button btnChange;


    private AllTreatmentController controller;
    private Stage stage;
    private Patient patient;
    private final ObservableList<String> caregiverSelection = FXCollections.observableArrayList();
    private ArrayList<Caregiver> caregiverList;
    private Caregiver caregiver;
    private Treatment treatment;

    public void initializeController(AllTreatmentController controller, Stage stage, Treatment treatment) {
        this.stage = stage;
        this.controller= controller;
        PatientDao pDao = DaoFactory.getDaoFactory().createPatientDAO();
        CaregiverDao cDao = DaoFactory.getDaoFactory().createCaregiverDAO();
        try {
            this.patient = pDao.read((int) treatment.getPid());
            this.caregiver = cDao.read(treatment.getCid());
            this.treatment = treatment;
            showData();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        this.createCaregiverComboBoxData();
    }

    private void showData(){
        this.labelPatientName.setText(patient.getSurname()+", "+patient.getFirstName());
        this.labelCareLevel.setText(patient.getCareLevel());
        this.comboBoxCaregiverSelection.setValue(this.caregiver.getSurname()+ ", "+ this.caregiver.getFirstName());
        this.labelCaregiverNumber.setText(this.caregiver.getTelephone());
        LocalDate date = DateConverter.convertStringToLocalDate(treatment.getDate());
        this.datePicker.setValue(date);
        this.textFieldBegin.setText(this.treatment.getBegin());
        this.textFieldEnd.setText(this.treatment.getEnd());
        this.textFieldDescription.setText(this.treatment.getDescription());
        this.textAreaRemarks.setText(this.treatment.getRemarks());

        boolean isEditable = (treatment.getStatus() != RecordStatus.LOCKED);

        datePicker.setEditable(isEditable);
        datePicker.setDisable(!isEditable);
        textFieldBegin.setEditable(isEditable);
        textFieldEnd.setEditable(isEditable);
        textFieldDescription.setEditable(isEditable);
        textAreaRemarks.setEditable(isEditable);
        comboBoxCaregiverSelection.setDisable(!isEditable);

        if (btnChange != null) {
            btnChange.setDisable(!isEditable);
        }

    }

    @FXML
    public void handleCaregiverComboBox() {
        String selectedCaregiver = this.comboBoxCaregiverSelection.getSelectionModel().getSelectedItem();
        this.caregiver = searchInCaregiverList(selectedCaregiver);
    }
    private void createCaregiverComboBoxData() {
        CaregiverDao dao = DaoFactory.getDaoFactory().createCaregiverDAO();
        try {
            caregiverList = (ArrayList<Caregiver>) dao.readAll();
            for (Caregiver caregiver: caregiverList) {
                this.caregiverSelection.add(caregiver.getSurname()+ ", "+caregiver.getFirstName());
            }
            comboBoxCaregiverSelection.setItems(FXCollections.observableArrayList(caregiverSelection));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
    private Caregiver searchInCaregiverList(String selectedName) {
        for (Caregiver caregiver : this.caregiverList) {
            String name = caregiver.getSurname()+ ", "+caregiver.getFirstName();
            if (name.equals(selectedName)) {
                return caregiver;
            }
        }
        return null;
    }

    @FXML
    public void handleChange(){
        this.treatment.setDate(this.datePicker.getValue().toString());
        this.treatment.setBegin(textFieldBegin.getText());
        this.treatment.setEnd(textFieldEnd.getText());
        this.treatment.setDescription(textFieldDescription.getText());
        this.treatment.setRemarks(textAreaRemarks.getText());
        this.treatment.setCid(this.caregiver.getCid());
        doUpdate();
        controller.readAllAndShowInTableView();
        stage.close();
    }

    private void doUpdate(){
        TreatmentDao dao = DaoFactory.getDaoFactory().createTreatmentDao();
        try {
            dao.update(treatment);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    public void handleCancel(){
        stage.close();
    }
}