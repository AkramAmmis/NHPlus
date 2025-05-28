package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.AuthorizationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Controller für die Login-Ansicht bei Verwendung von FXML
 */
public class LoginViewController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label lblStatus;

    private UserDao userDao;
    private MainWindowController mainController;

    /**
     * Setzt den MainWindowController für die Navigation nach dem Login
     * @param controller Der MainWindowController
     */
    public void setMainWindowController(MainWindowController controller) {
        this.mainController = controller;
    }

    /**
     * Initialisiert den Controller
     */
    @FXML
    public void initialize() {
        try {
            // DAO initialisieren
            this.userDao = DaoFactory.getDaoFactory().createUserDAO();

            // Status-Label zurücksetzen
            if (this.lblStatus != null) {
                this.lblStatus.setText("");
            }
        } catch (Exception e) {
            System.err.println("Fehler bei der Initialisierung des LoginViewController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Behandelt den Login-Button-Klick
     */
    @FXML
    public void handleLogin() {
        try {
            String username = this.usernameField.getText();
            String password = this.passwordField.getText();

            // Eingabeprüfung
            if (username == null || username.trim().isEmpty() || 
                password == null || password.isEmpty()) {
                this.lblStatus.setText("Bitte Benutzername und Passwort eingeben!");
                this.lblStatus.setTextFill(Color.RED);
                return;
            }

            // Anmeldung durchführen
            User user = this.userDao.authenticate(username, password);

            if (user != null) {
                // Anmeldung erfolgreich
                AuthorizationManager.getInstance().setCurrentUser(user);
                System.out.println("Benutzer '" + user.getUsername() + "' erfolgreich authentifiziert.");

                try {
                    // Stage für das Hauptfenster holen
                    Stage loginStage = (Stage) this.usernameField.getScene().getWindow();

                    // Zur Hauptansicht wechseln
                    if (this.mainController != null) {
                        // Stage im MainWindowController setzen
                        this.mainController.setPrimaryStage(loginStage);
                        this.mainController.showMainView();
                    } else {
                    System.err.println("MainWindowController ist null. Kann nicht zur Hauptansicht wechseln.");
                    this.showError("Anwendungsfehler", "Die Anwendung konnte nicht korrekt initialisiert werden.");
                }
                                } catch (Exception e) {
                System.err.println("Fehler bei der Anmeldung: " + e.getMessage());
                e.printStackTrace();
                this.showError("Anmeldefehler", "Fehler beim Anzeigen der Hauptansicht: " + e.getMessage());
                                }
            } else {
                // Anmeldung fehlgeschlagen
                this.lblStatus.setText("Benutzername oder Passwort falsch!");
                this.lblStatus.setTextFill(Color.RED);

                // Passwortfeld leeren und Fokus setzen
                this.passwordField.clear();
                this.passwordField.requestFocus();
            }
        } catch (Exception e) {
            System.err.println("Fehler bei der Anmeldung: " + e.getMessage());
            e.printStackTrace();

            this.lblStatus.setText("Fehler bei der Anmeldung: " + e.getMessage());
            this.lblStatus.setTextFill(Color.RED);

            this.showError("Anmeldefehler", "Bei der Anmeldung ist ein Fehler aufgetreten: " + e.getMessage());
        }
    }

    /**
     * Zeigt eine Fehlermeldung an
     * @param title Der Titel des Dialogs
     * @param message Die Nachricht
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
