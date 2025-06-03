
package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.AuthorizationManager;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Treatment;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Pair;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Statement;
import java.util.Optional;
import java.util.List;

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
        this.readAllAndShowInTableView();
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("cid"));
        boolean isAdmin = de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin();
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnFirstName.setEditable(isAdmin);
        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnSurname.setEditable(isAdmin);
        this.columnTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        this.columnTelephone.setCellFactory(TextFieldTableCell.forTableColumn());
        this.columnTelephone.setEditable(isAdmin);
        this.tableView.setEditable(isAdmin);
        this.tableView.setItems(this.caregivers);
        addLogoutButton();
        this.buttonDelete.setDisable(true);
        this.buttonDelete.setVisible(isAdmin);
        if (this.buttonChangePassword != null) {
            this.buttonChangePassword.setDisable(true);
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
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten bearbeiten!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pflegerdaten bearbeiten!");
            alert.showAndWait();
            readAllAndShowInTableView();
            return;
        }
        event.getRowValue().setFirstName(event.getNewValue());
        this.doUpdate(event);
        syncUsernameWithCaregiver(event.getRowValue());
    }
    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Caregiver, String> event) {
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten bearbeiten!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pflegerdaten bearbeiten!");
            alert.showAndWait();
            readAllAndShowInTableView();
            return;
        }
        event.getRowValue().setSurname(event.getNewValue());
        this.doUpdate(event);
        syncUsernameWithCaregiver(event.getRowValue());
    }
    @FXML
    public void handleOnEditTelephone(TableColumn.CellEditEvent<Caregiver, String> event) {
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten bearbeiten!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pflegerdaten bearbeiten!");
            alert.showAndWait();
            readAllAndShowInTableView();
            return;
        }
        event.getRowValue().setTelephone(event.getNewValue());
        this.doUpdate(event);
    }
    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pflegerdaten aktualisieren!");
            readAllAndShowInTableView();
            return;
        }
        try {
            this.dao.update(event.getRowValue());
            try {
                UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                User user = null;
                for (User u : userDao.readAllUsers()) {
                    if (u.getCaregiverId() == event.getRowValue().getCid()) {
                        user = u;
                        break;
                    }
                }
                if (user != null) {
                    if ("firstName".equals(event.getTableColumn().getId())) {
                        user.setFirstName(event.getNewValue());
                    } else if ("surname".equals(event.getTableColumn().getId())) {
                        user.setLastName(event.getNewValue());
                    } else if ("telephone".equals(event.getTableColumn().getId())) {
                        user.setPhoneNumber(event.getNewValue());
                    }
                    userDao.updateUser(user);
                    System.out.println("Zugehöriger Benutzeraccount wurde ebenfalls aktualisiert: " + user.getUsername());
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Aktualisieren des zugehörigen Benutzeraccounts: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Datenbankfehler");
            errorAlert.setHeaderText("Fehler beim Aktualisieren");
            errorAlert.setContentText("Der Pfleger konnte nicht aktualisiert werden: " + exception.getMessage());
            errorAlert.showAndWait();
        }
    }
    private void readAllAndShowInTableView() {
        this.caregivers.clear();
        this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
        try {
            this.caregivers.addAll(this.dao.readAll());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
    private User findAssociatedUser(Caregiver caregiver) {
        try {
            UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
            for (User user : userDao.readAllUsers()) {
                if (user.getCaregiverId() == caregiver.getCid()) {
                    System.out.println("User gefunden über caregiver_id: " + user.getUsername());
                    return user;
                }
            }
            if (caregiver.getUsername() != null && !caregiver.getUsername().isEmpty()) {
                User user = userDao.findByUsername(caregiver.getUsername());
                if (user != null) {
                    System.out.println("User gefunden über Username: " + user.getUsername());
                    return user;
                }
            }
            for (User user : userDao.readAllUsers()) {
                if (user.getFirstName() != null && user.getLastName() != null &&
                    user.getFirstName().equalsIgnoreCase(caregiver.getFirstName()) &&
                    user.getLastName().equalsIgnoreCase(caregiver.getSurname())) {
                    System.out.println("User gefunden über Namen: " + user.getUsername());
                    user.setCaregiverId(caregiver.getCid());
                    userDao.updateUser(user);
                    return user;
                }
            }
            String generatedUsername = (caregiver.getFirstName().toLowerCase().charAt(0) + 
                                     caregiver.getSurname().toLowerCase().replace(" ", ""));
            User user = userDao.findByUsername(generatedUsername);
            if (user != null) {
                System.out.println("User gefunden über generierten Username: " + user.getUsername());
                user.setCaregiverId(caregiver.getCid());
                userDao.updateUser(user);
                return user;
            }
            System.out.println("Kein zugehöriger User für Caregiver " + caregiver.getFullName() + " gefunden.");
            return null;
        } catch (Exception e) {
            System.err.println("Fehler beim Suchen des zugehörigen Users: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    @FXML
    public void handleChangePassword() {
        if (!AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Passwörter ändern!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Passwörter ändern!");
            alert.showAndWait();
            return;
        }
        Caregiver selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Keine Auswahl");
            alert.setHeaderText("Kein Pfleger ausgewählt");
            alert.setContentText("Bitte wählen Sie einen Pfleger aus, dessen Passwort Sie ändern möchten.");
            alert.showAndWait();
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Passwort ändern");
        dialog.setHeaderText("Neues Passwort für " + selectedItem.getFirstName() + " " + selectedItem.getSurname() + " festlegen");
        ButtonType confirmButtonType = new ButtonType("Bestätigen", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Neues Passwort");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Passwort bestätigen");
        grid.add(new Label("Neues Passwort:"), 0, 0);
        grid.add(passwordField, 1, 0);
        grid.add(new Label("Passwort bestätigen:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> passwordField.requestFocus());
        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButton.setDisable(newValue.trim().isEmpty() || 
                                     !newValue.equals(confirmPasswordField.getText()));
        });
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            confirmButton.setDisable(newValue.trim().isEmpty() || 
                                     !newValue.equals(passwordField.getText()));
        });
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newPassword -> {
            try {
                System.out.println("Passwort wird geändert für: " + selectedItem.getFullName());
                User userToUpdate = findAssociatedUser(selectedItem);
                if (userToUpdate != null) {
                    userToUpdate.setPassword(newPassword);
                    UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                    userDao.updateUser(userToUpdate);
                    System.out.println("Passwort für Benutzer '" + userToUpdate.getUsername() + "' erfolgreich aktualisiert.");
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Passwort geändert");
                    successAlert.setHeaderText("Passwort erfolgreich geändert");
                    successAlert.setContentText("Das Passwort für " + selectedItem.getFullName() + " wurde erfolgreich aktualisiert.\n" +
                                               "Benutzername: " + userToUpdate.getUsername());
                    successAlert.showAndWait();
                } else {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Benutzer nicht gefunden");
                    confirmAlert.setHeaderText("Kein zugehöriger Benutzeraccount gefunden");
                    confirmAlert.setContentText("Für den Pfleger " + selectedItem.getFullName() + 
                                               " wurde kein Benutzeraccount gefunden.\n\n" +
                                               "Möchten Sie einen neuen Benutzeraccount erstellen?");
                    Optional<ButtonType> createResult = confirmAlert.showAndWait();
                    if (createResult.isPresent() && createResult.get() == ButtonType.OK) {
                        createUserForCaregiver(selectedItem, newPassword);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Datenbankfehler");
                errorAlert.setHeaderText("Fehler beim Ändern des Passworts");
                errorAlert.setContentText("Das Passwort konnte nicht geändert werden: " + exception.getMessage());
                errorAlert.showAndWait();
            }
        });
    }
    private void createUserForCaregiver(Caregiver caregiver, String password) {
        try {
            String username = caregiver.getUsername();
            if (username == null || username.isEmpty()) {
                username = (caregiver.getFirstName().toLowerCase().charAt(0) + 
                          caregiver.getSurname().toLowerCase().replace(" ", ""));
            }
            User newUser = new User(username, password, caregiver.getFirstName(), caregiver.getSurname(), 
                                  "", caregiver.getTelephone(), UserRole.CAREGIVER);
            newUser.setCaregiverId(caregiver.getCid());
            UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
            User createdUser = userDao.createUser(newUser);
            if (createdUser != null) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Benutzer erstellt");
                successAlert.setHeaderText("Neuer Benutzeraccount erstellt");
                successAlert.setContentText("Ein neuer Benutzeraccount wurde für " + caregiver.getFullName() + 
                                           " erstellt.\nBenutzername: " + username + 
                                           "\nPasswort wurde gesetzt.");
                successAlert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Erstellen des Benutzeraccounts: " + e.getMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Fehler");
            errorAlert.setHeaderText("Benutzeraccount konnte nicht erstellt werden");
            errorAlert.setContentText("Fehler: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
    @FXML
    public void handleDelete() {
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können Pfleger löschen!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können Pfleger löschen!");
            alert.showAndWait();
            return;
        }
        Caregiver selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Pfleger löschen");
            confirmAlert.setHeaderText("Pfleger löschen bestätigen");
            confirmAlert.setContentText("Möchten Sie den Pfleger " + selectedItem.getFirstName() + " " + 
                                       selectedItem.getSurname() + " wirklich löschen?");
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
              TreatmentDao treatmentDao = DaoFactory.getDaoFactory().createTreatmentDao();
                List<Treatment> activeTreatments = treatmentDao.readTreatmentsByCid(selectedItem.getCid());

                if (!activeTreatments.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warnung");
                    alert.setHeaderText("Pfleger hat aktive Behandlungen");
                    alert.setContentText("Der Pfleger ist mit " + activeTreatments.size() +
                            " Behandlungen verknüpft. Sie können den Pfleger noch nicht löschen?");

                    ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.CANCEL_CLOSE);
                    alert.getButtonTypes().setAll(buttonTypeOk);

                    Optional<ButtonType> secResult = alert.showAndWait();
                    if (result.isPresent() && secResult.get() == buttonTypeOk) {
                        return;
                    }
                try {
                    DaoFactory.getDaoFactory().createCaregiverDAO().deleteById(selectedItem.getCid());
                    this.tableView.getItems().remove(selectedItem);
                    try {
                        UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                        User user = null;
                        for (User u : userDao.readAllUsers()) {
                            if (u.getCaregiverId() == selectedItem.getCid()) {
                                user = u;
                                break;
                            }
                        }
                        if (user != null) {
                            userDao.delete(user);
                            System.out.println("Zugehöriger Benutzeraccount wurde ebenfalls gelöscht: " + user.getUsername());
                        }
                    } catch (Exception e) {
                        System.err.println("Fehler beim Löschen des zugehörigen Benutzeraccounts: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (SQLException exception) {
                    exception.printStackTrace();
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Datenbankfehler");
                    errorAlert.setHeaderText("Fehler beim Löschen");
                    errorAlert.setContentText("Der Pfleger konnte nicht gelöscht werden: " + exception.getMessage());
                    errorAlert.showAndWait();
                }
            }
        }
    }
    @FXML
    public void handleAdd() {
        System.out.println("AllCaregiverController.handleAdd() wurde aufgerufen!");
        if (this.textFieldSurname == null || this.textFieldFirstName == null || this.textFieldTelephone == null) {
            System.err.println("FEHLER: Eines der Textfelder ist null!");
            return;
        }
        if (!de.hitec.nhplus.utils.AuthorizationManager.getInstance().isAdmin()) {
            System.err.println("Nur Administratoren können neue Pfleger hinzufügen!");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Zugriff verweigert");
            alert.setHeaderText("Fehlende Berechtigung");
            alert.setContentText("Nur Administratoren können neue Pfleger hinzufügen!");
            alert.showAndWait();
            return;
        }
        if (this.dao == null) {
            System.out.println("DAO wird neu initialisiert...");
            try {
                this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
                if (this.dao == null) {
                    System.err.println("FEHLER: DAO konnte nicht initialisiert werden!");
                    return;
                }
            } catch (Exception e) {
                System.err.println("Fehler bei der Initialisierung des DAOs: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        try {
            Connection conn = ConnectionBuilder.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet tables = conn.getMetaData().getTables(null, null, "caregiver", null);
            boolean tableExists = tables.next();
            tables.close();
            if (!tableExists) {
                System.out.println("Tabelle 'caregiver' existiert nicht. Erstelle neu...");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS caregiver (" +
                        "cid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "firstname TEXT NOT NULL, " +
                        "surname TEXT NOT NULL, " +
                        "telephone TEXT, " +
                        "username TEXT, " +
                        "password TEXT)"; 
                stmt.executeUpdate(createTableSQL);
                System.out.println("Tabelle 'caregiver' erfolgreich erstellt.");
            } else {
                boolean needsAlter = false;
                ResultSet columns = conn.getMetaData().getColumns(null, null, "caregiver", "username");
                boolean usernameExists = columns.next();
                columns.close();
                if (!usernameExists) {
                    System.out.println("Spalte 'username' fehlt. Füge hinzu...");
                    stmt.executeUpdate("ALTER TABLE caregiver ADD COLUMN username TEXT");
                    needsAlter = true;
                }
                columns = conn.getMetaData().getColumns(null, null, "caregiver", "password");
                boolean passwordExists = columns.next();
                columns.close();
                if (!passwordExists) {
                    System.out.println("Spalte 'password' fehlt. Füge hinzu...");
                    stmt.executeUpdate("ALTER TABLE caregiver ADD COLUMN password TEXT");
                    needsAlter = true;
                }
                if (needsAlter) {
                    System.out.println("Spalten wurden zur Tabelle hinzugefügt.");
                }
            }
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Fehler bei der Tabellenerstellung: " + e.getMessage());
            e.printStackTrace();
        }
        String surname = this.textFieldSurname.getText();
        String firstName = this.textFieldFirstName.getText();
        String telephone = this.textFieldTelephone.getText();
        if (surname.isBlank() || firstName.isBlank() || telephone.isBlank()) {
            System.err.println("FEHLER: Nicht alle erforderlichen Felder sind ausgefüllt!");
            return;
        }
        System.out.println("Versuche, Pfleger hinzuzufügen: " + firstName + " " + surname + ", Tel: " + telephone);
        try {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Benutzerinformationen");
            dialog.setHeaderText("Bitte geben Sie Benutzername und Passwort für den neuen Pfleger ein");
            ButtonType confirmButtonType = new ButtonType("Bestätigen", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            String suggestedUsername = (firstName.toLowerCase().charAt(0) + 
                                     surname.toLowerCase().replace(" ", ""));
            TextField usernameField = new TextField();
            usernameField.setText(suggestedUsername);
            PasswordField passwordField = new PasswordField();
            grid.add(new Label("Benutzername:"), 0, 0);
            grid.add(usernameField, 1, 0);
            grid.add(new Label("Passwort:"), 0, 1);
            grid.add(passwordField, 1, 1);
            dialog.getDialogPane().setContent(grid);
            Platform.runLater(() -> passwordField.requestFocus());
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    return new Pair<>(usernameField.getText(), passwordField.getText());
                }
                return null;
            });
            Optional<Pair<String, String>> result = dialog.showAndWait();
            if (!result.isPresent()) {
                System.out.println("Dialog abgebrochen.");
                return;
            }
            String username = result.get().getKey();
            String password = result.get().getValue();
            if (username == null || username.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler");
                alert.setHeaderText("Ungültiger Benutzername");
                alert.setContentText("Bitte geben Sie einen gültigen Benutzernamen ein.");
                alert.showAndWait();
                return;
            }
            Caregiver caregiver = new Caregiver(firstName, surname, telephone);
            caregiver.setUsername(username);
            caregiver.setPassword(password);
            System.out.println("Rufe create-Methode auf mit Caregiver: " + caregiver);
            Connection conn = ConnectionBuilder.getConnection();
            boolean hasUsernamePassword = false;
            try {
                ResultSet columns = conn.getMetaData().getColumns(null, null, "caregiver", "username");
                hasUsernamePassword = columns.next();
                columns.close();
            } catch (SQLException e) {
                System.out.println("Fehler beim Prüfen der Spalten: " + e.getMessage());
            }
            String sql;
            PreparedStatement ps;
            if (hasUsernamePassword) {
                sql = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, firstName);
                ps.setString(2, surname);
                ps.setString(3, telephone);
                ps.setString(4, username);
                ps.setString(5, password);
            } else {
                sql = "INSERT INTO caregiver (firstname, surname, telephone) VALUES (?, ?, ?)"; 
                ps = conn.prepareStatement(sql);
                ps.setString(1, firstName);
                ps.setString(2, surname);
                ps.setString(3, telephone);
            }
            int affected = ps.executeUpdate();
            System.out.println("Anzahl betroffener Zeilen: " + affected);
            ps.close();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
            long newId = -1;
            if (rs.next()) {
                newId = rs.getLong(1);
                System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + newId);
            }
            rs.close();
            stmt.close();
            try {
                UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
                User existingUser = userDao.findByUsername(username);
                if (existingUser == null) {
                    User newUser = new User(username, password, firstName, surname, "", telephone, UserRole.CAREGIVER);
                    if (newId > 0) {
                        newUser.setCaregiverId(newId);
                    } else {
                        System.err.println("Warnung: Ungültige Caregiver-ID (" + newId + "). Verknüpfung kann nicht hergestellt werden.");
                    }
                    User createdUser = userDao.createUser(newUser);
                    if (createdUser != null) {
                        System.out.println("Benutzer erfolgreich erstellt: " + createdUser.getUsername());
                    } else {
                        System.err.println("Fehler beim Erstellen des Benutzers");
                    }
                } else {
                    System.out.println("Benutzer mit diesem Benutzernamen existiert bereits. Aktualisiere Verknüpfung...");
                    if (existingUser != null && newId > 0) {
                        existingUser.setCaregiverId(newId);
                        userDao.updateUser(existingUser);
                    } else if (existingUser == null) {
                        System.err.println("Fehler: Benutzer mit Benutzernamen '" + username + "' konnte nicht gefunden werden.");
                    }
                }
            } catch (Exception e) {
                System.err.println("Fehler beim Erstellen des Benutzers: " + e.getMessage());
                e.printStackTrace();
            }
            readAllAndShowInTableView();
            clearTextfields();
        } catch (SQLException e) {
            System.err.println("SQL Fehler beim Hinzufügen des Pflegers: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage() != null) {
                if (e.getMessage().contains("no column named")) {
                    System.err.println("Es fehlt eine Spalte in der Datenbank. Versuche, die Tabelle neu zu erstellen...");
                    try {
                        this.dao.createTable();
                        System.out.println("Tabelle wurde neu erstellt. Versuche erneut, den Pfleger hinzuzufügen...");
                        Caregiver caregiver = new Caregiver(firstName, surname, telephone);
                        String username = (firstName.toLowerCase().charAt(0) + surname.toLowerCase().replace(" ", ""));
                        caregiver.setUsername(username);
                        caregiver.setPassword("");
                        long newId = this.dao.create(caregiver);
                        System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + newId);
                        readAllAndShowInTableView();
                        clearTextfields();
                    } catch (Exception ex) {
                        System.err.println("Erneuter Versuch fehlgeschlagen: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                } else if (e.getMessage().contains("PreparedStatement ist null")) {
                    System.err.println("PreparedStatement ist null. Versuche direkten SQL-Ansatz...");
                    try {
                        java.sql.Connection conn = ConnectionBuilder.getConnection();
                        String sql = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)"; 
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, firstName);
                        ps.setString(2, surname);
                        ps.setString(3, telephone);
                        ps.setString(4, firstName.toLowerCase().charAt(0) + surname.toLowerCase().replace(" ", ""));
                        ps.setString(5, "");
                        ps.executeUpdate();
                        ps.close();
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                        if (rs.next()) {
                            System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + rs.getLong(1));
                        }
                        rs.close();
                        stmt.close();
                        readAllAndShowInTableView();
                        clearTextfields();
                    } catch (Exception ex) {
                        System.err.println("Direkter SQL-Ansatz fehlgeschlagen: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        } catch (NullPointerException e) {
            System.err.println("NullPointer Exception beim Hinzufügen des Pflegers: " + e.getMessage());
            e.printStackTrace();
            try {
                System.out.println("Versuche alternativen Ansatz nach NullPointerException...");
                this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
                this.dao.createTable();
                java.sql.Connection conn = ConnectionBuilder.getConnection();
                String sql = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)"; 
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, firstName);
                ps.setString(2, surname);
                ps.setString(3, telephone);
                ps.setString(4, firstName.toLowerCase().charAt(0) + surname.toLowerCase().replace(" ", ""));
                ps.setString(5, "");
                ps.executeUpdate();
                ps.close();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    System.out.println("Pfleger erfolgreich hinzugefügt mit ID: " + rs.getLong(1));
                }
                rs.close();
                stmt.close();
                readAllAndShowInTableView();
                clearTextfields();
            } catch (Exception ex) {
                System.err.println("Alternativer Ansatz fehlgeschlagen: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler beim Hinzufügen des Pflegers: " + e.getMessage());
            e.printStackTrace();
        }
    }
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
    private void addLogoutButton() {
        try {
            if (tableView.getScene() == null) {
                tableView.sceneProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        createLogoutButton(newValue);
                    }
                });
            } else {
                createLogoutButton(tableView.getScene());
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Hinzufügen des Abmelde-Buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void createLogoutButton(javafx.scene.Scene scene) {
        try {
            javafx.scene.Node toolbar = scene.lookup("#toolbar");
            if (toolbar instanceof javafx.scene.layout.HBox) {
                javafx.scene.layout.HBox toolbarBox = (javafx.scene.layout.HBox) toolbar;
                boolean logoutButtonExists = false;
                for (javafx.scene.Node node : toolbarBox.getChildren()) {
                    if (node instanceof javafx.scene.control.Button && "logoutButton".equals(node.getId())) {
                        logoutButtonExists = true;
                        break;
                    }
                }
                if (!logoutButtonExists) {
                    Button logoutButton = new Button("Abmelden");
                    logoutButton.setId("logoutButton");
                    logoutButton.setOnAction((javafx.event.ActionEvent e) -> handleLogout(e));
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    toolbarBox.getChildren().addAll(spacer, logoutButton);
                }
            } else if (scene.getRoot() instanceof javafx.scene.layout.BorderPane) {
                javafx.scene.layout.BorderPane root = (javafx.scene.layout.BorderPane) scene.getRoot();
                javafx.scene.Node existingTop = root.getTop();
                javafx.scene.layout.HBox toolbarBox;
                if (existingTop instanceof javafx.scene.layout.HBox) {
                    toolbarBox = (javafx.scene.layout.HBox) existingTop;
                } else {
                    toolbarBox = new javafx.scene.layout.HBox(10);
                    toolbarBox.setPadding(new javafx.geometry.Insets(10));
                    toolbarBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                    if (existingTop != null) {
                        toolbarBox.getChildren().add(existingTop);
                    }
                    root.setTop(toolbarBox);
                }
                boolean logoutButtonExists = false;
                for (javafx.scene.Node node : toolbarBox.getChildren()) {
                    if (node instanceof javafx.scene.control.Button && "logoutButton".equals(node.getId())) {
                        logoutButtonExists = true;
                        break;
                    }
                }
                if (!logoutButtonExists) {
                    Label usernameLabel = new Label();
                    usernameLabel.setId("usernameLabel");
                    User currentUser = AuthorizationManager.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String displayName = currentUser.getFullName();
                        String roleName = currentUser.isAdmin() ? "Administrator" : "Pfleger";
                        usernameLabel.setText("Angemeldet als: " + displayName + " (" + roleName + ")");
                    } else {
                        usernameLabel.setText("Nicht angemeldet");
                    }
                    Button logoutButton = new Button("Abmelden");
                    logoutButton.setId("logoutButton");
                    logoutButton.setOnAction((javafx.event.ActionEvent e) -> handleLogout(e));
                    try {
                        Scene currentScene = toolbarBox.getScene();
                        if (currentScene != null) {
                            String cssPath = getClass().getResource("/de/hitec/nhplus/css/toolbar.css").toExternalForm();
                            if (!currentScene.getStylesheets().contains(cssPath)) {
                                currentScene.getStylesheets().add(cssPath);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Fehler beim Laden der CSS-Datei: " + e.getMessage());
                    }
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    toolbarBox.getChildren().addAll(spacer, usernameLabel, logoutButton);
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Erstellen des Abmelde-Buttons: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        AuthorizationManager.getInstance().logout();
        try {
            Stage currentStage;
            if (primaryStage != null) {
                currentStage = primaryStage;
            } else if (mainWindowController != null) {
                currentStage = null;
                if (mainWindowController.getMain() != null) {
                    mainWindowController.getMain().handleLogout();
                    return;
                }
            } else {
                if (event != null && event.getSource() instanceof javafx.scene.Node) {
                    javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                    if (source.getScene() != null && source.getScene().getWindow() instanceof Stage) {
                        currentStage = (Stage) source.getScene().getWindow();
                    } else {
                        currentStage = null;
                    }
                } else {
                    currentStage = null;
                }
            }
            if (currentStage != null) {
                MainWindowController newMainController = new MainWindowController();
                newMainController.setPrimaryStage(currentStage);
                Platform.runLater(() -> {
                    try {
                        new de.hitec.nhplus.gui.LoginView(currentStage, newMainController);
                    } catch (Exception e) {
                        System.err.println("Fehler beim Anzeigen des Login-Fensters: " + e.getMessage());
                        Platform.exit();
                    }
                });
            } else {
                System.err.println("Konnte keine gültige Stage finden. Verwende Platform.exit().");
                Platform.runLater(() -> Platform.exit());
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Schließen des Fensters: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> Platform.exit());
        }
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }
    private void syncUsernameWithCaregiver(Caregiver caregiver) {
        try {
            UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
            User user = null;
            for (User u : userDao.readAllUsers()) {
                if (u.getCaregiverId() == caregiver.getCid()) {
                    user = u;
                    break;
                }
            }
            if (user != null && user.getRole() == UserRole.CAREGIVER) {
                String newUsername = (caregiver.getFirstName().toLowerCase().charAt(0) +
                        caregiver.getSurname().toLowerCase().replace(" ", ""));
                if (!user.getUsername().equals(newUsername)) {
                    user.setUsername(newUsername);
                    userDao.updateUser(user);
                    System.out.println("Benutzername für User '" + user.getUid() + "' wurde auf '" + newUsername + "' geändert.");
                }
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Synchronisieren des User-Benutzernamens: " + e.getMessage());
        }
    }

}