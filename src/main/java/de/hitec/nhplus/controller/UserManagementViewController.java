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

    public void initialize() {
        this.controller = new UserManagementController();

        if (!AuthorizationManager.getInstance().isAdmin()) {
            disableControls();
            showAlert("Keine Berechtigung", "Sie haben keine Berechtigung für die Benutzerverwaltung.");
            return;
        }

        this.columnId.setCellValueFactory(new PropertyValueFactory<>("uid"));
        this.columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        this.columnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        this.columnPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        this.columnRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        this.comboBoxRole.getItems().addAll(UserRole.values());

        loadUsers();

        this.tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends User> observable, User oldValue, User newValue) {
                if (newValue != null) {
                    selectedUser = newValue;
                    fillFormWithUser(newValue);
                    buttonDelete.setDisable(false);
                    buttonUpdate.setDisable(false);

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

        this.buttonDelete.setDisable(true);
        this.buttonUpdate.setDisable(true);
    }

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

    private void loadUsers() {
        this.users.clear();
        this.users.addAll(this.controller.getAllUsers());
        this.tableView.setItems(this.users);
    }

    private void fillFormWithUser(User user) {
        this.textFieldUsername.setText(user.getUsername());
        this.passwordField.setText("");
        this.textFieldFirstName.setText(user.getFirstName());
        this.textFieldLastName.setText(user.getLastName());
        this.textFieldEmail.setText(user.getEmail());
        this.textFieldPhone.setText(user.getPhoneNumber());
        this.comboBoxRole.setValue(user.getRole());
    }

    private void clearForm() {
        this.textFieldUsername.clear();
        this.passwordField.clear();
        this.textFieldFirstName.clear();
        this.textFieldLastName.clear();
        this.textFieldEmail.clear();
        this.textFieldPhone.clear();
        this.comboBoxRole.setValue(null);
    }

    @FXML
    private void handleAdd() {
        if (!validateInput(true)) {
            return;
        }

        if (controller.isUsernameAlreadyTaken(textFieldUsername.getText())) {
            showAlert("Fehler", "Der Benutzername ist bereits vergeben.");
            return;
        }

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
            users.add(newUser);
            clearForm();
            showAlert("Erfolg", "Der Benutzer wurde erfolgreich erstellt.");
        } else {
            showAlert("Fehler", "Der Benutzer konnte nicht erstellt werden.");
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUser == null) {
            showAlert("Fehler", "Bitte wählen Sie einen Benutzer aus.");
            return;
        }

        if (!validateInput(false)) {
            return;
        }

        if (!selectedUser.getUsername().equals(textFieldUsername.getText())) {
            showAlert("Fehler", "Der Benutzername kann nicht geändert werden.");
            return;
        }

        selectedUser.setFirstName(textFieldFirstName.getText());
        selectedUser.setLastName(textFieldLastName.getText());
        selectedUser.setEmail(textFieldEmail.getText());
        selectedUser.setPhoneNumber(textFieldPhone.getText());
        selectedUser.setRole(comboBoxRole.getValue());

        if (!passwordField.getText().isEmpty()) {
            selectedUser.setPassword(passwordField.getText());
        }

        if (controller.updateUser(selectedUser)) {
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

    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showAlert("Fehler", "Bitte wählen Sie einen Benutzer aus.");
            return;
        }

        if ("admin".equals(selectedUser.getUsername())) {
            showAlert("Fehler", "Der Administrator-Benutzer kann nicht gelöscht werden.");
            return;
        }

        if (controller.deleteUser(selectedUser)) {
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

    private boolean validateInput(boolean isNewUser) {
        if (textFieldUsername.getText().isEmpty() ||
            textFieldFirstName.getText().isEmpty() ||
            textFieldLastName.getText().isEmpty() ||
            comboBoxRole.getValue() == null) {
            showAlert("Fehler", "Bitte füllen Sie alle Pflichtfelder aus (Benutzername, Vorname, Nachname, Rolle).");
            return false;
        }

        if (isNewUser && passwordField.getText().isEmpty()) {
            showAlert("Fehler", "Bitte geben Sie ein Passwort ein.");
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
