package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.model.Caregiver;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

// ... andere Importe ...
import de.hitec.nhplus.model.Caregiver; // Sicherstellen, dass Caregiver importiert ist
import java.sql.SQLException;

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

        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        // this.columnFirstName.setOnEditCommit(this::handleOnEditFirstname); // Alternative zur FXML-Deklaration

        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());
        // this.columnSurname.setOnEditCommit(this::handleOnEditSurname); // Alternative zur FXML-Deklaration

        this.columnTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        this.columnTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
        // this.columnTelephone.setOnEditCommit(this::handleOnEditTelephone); // Alternative zur FXML-Deklaration

        this.tableView.setItems(this.caregivers);

        this.buttonDelete.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldCaregiver, newCaregiver) ->
            AllCaregiverController.this.buttonDelete.setDisable(newCaregiver == null));

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
        event.getRowValue().setTelephone(event.getNewValue());
        this.doUpdate(event);
    }

    /**
     * Updates a caregiver by calling the method <code>update()</code> of {@link CaregiverDao}.
     *
     * @param event Event including the changed object and the change.
     */
    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        try {
            this.dao.update(event.getRowValue());
        } catch (SQLException exception) {
            exception.printStackTrace();
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
        Caregiver selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                DaoFactory.getDaoFactory().createCaregiverDAO().deleteById(selectedItem.getCid());
                this.tableView.getItems().remove(selectedItem);
            } catch (SQLException exception) {
                exception.printStackTrace();
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
        // Ist der Button überhaupt klickbar und die Methode wird aufgerufen?
        System.out.println("AllCaregiverController.handleAdd() wurde aufgerufen!");

        // Ist das DAO-Objekt initialisiert?
        if (this.dao == null) {
            System.err.println("FEHLER: this.dao in handleAdd() ist null!");
            // Hier könnten Sie versuchen, es erneut zu initialisieren oder eine Fehlermeldung anzuzeigen
            // Fürs Debugging ist es erstmal wichtig zu wissen, ob es null ist.
            // this.dao = DaoFactory.getDaoFactory().createCaregiverDAO(); // Notlösung fürs Debugging, Ursache finden!
            // if (this.dao == null) {
            //     System.err.println("FEHLER: this.dao ist auch nach erneuter Initialisierung null!");
            //     return;
            // }
        }

        String surname = this.textFieldSurname.getText();
        String firstName = this.textFieldFirstName.getText();
        String telephone = this.textFieldTelephone.getText();

        try {
            Caregiver newCaregiver = new Caregiver(firstName, surname, telephone);
            System.out.println("Versuche, Pfleger hinzuzufügen: " + firstName + " " + surname + ", Tel: " + telephone);

            long newId = this.dao.create(newCaregiver);

            System.out.println("Pfleger vermutlich hinzugefügt mit ID: " + newId);
            if (newId <= 0) {
                System.err.println("WARNUNG: Erhaltene ID ist " + newId + ". Dies deutet oft auf ein Problem beim Einfügen hin, auch wenn keine SQLException geworfen wurde.");
            }

        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Hinzufügen des Pflegers:");
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("NullPointer Exception beim Hinzufügen des Pflegers (DAO-Methoden oder this.dao prüfen!):");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler beim Hinzufügen des Pflegers:");
            e.printStackTrace();
        }

        readAllAndShowInTableView();
        clearTextfields();
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
}