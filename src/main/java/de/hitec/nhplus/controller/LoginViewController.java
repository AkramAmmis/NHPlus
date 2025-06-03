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

            // Spezialbehandlung für Admin-Account entsperren (Notfall-Zugang)
            if ("admin".equals(username) && "unlock123".equals(password)) {
                // Notfall-Entsperrung für Admin-Account
                if (this.userDao instanceof de.hitec.nhplus.datastorage.UserDaoImpl) {
                    boolean unlocked = ((de.hitec.nhplus.datastorage.UserDaoImpl)this.userDao).unlockUser("admin");
                    if (unlocked) {
                        this.lblStatus.setText("Admin-Account wurde entsperrt. Bitte mit normalem Passwort anmelden.");
                        this.lblStatus.setTextFill(Color.GREEN);
                        this.passwordField.clear();
                        return;
                    }
                }
            }

            // Benutzer für Sperrprüfung holen
            User user = this.userDao.findByUsername(username);
            long now = System.currentTimeMillis();
            if (user != null && user.getLockUntil() > now) {
                long minLeft = (user.getLockUntil() - now) / 60000 + 1;
                this.lblStatus.setText("Account gesperrt für " + minLeft + " min.");
                this.lblStatus.setTextFill(Color.RED);
                return;
            }

            // Anmeldung durchführen
            User authUser = this.userDao.authenticate(username, password);

            if (authUser != null) {
                // Anmeldung erfolgreich
                authUser.setFailedAttempts(0);
                authUser.setLockUntil(0);
                this.userDao.updateUser(authUser);
                AuthorizationManager.getInstance().setCurrentUser(authUser);
                System.out.println("Benutzer '" + authUser.getUsername() + "' erfolgreich authentifiziert.");

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
                if (user != null) {
                    // Nur failed attempts erhöhen, wenn es sich nicht um den Admin-Account handelt
                    // oder wenn es sich um ein wirklich falsches Passwort handelt
                    if (!"admin".equals(username) || !"admin123".equals(password)) {
                        int attempts = user.getFailedAttempts() + 1;
                        user.setFailedAttempts(attempts);
                        if (attempts >= 3) {
                            user.setLockUntil(System.currentTimeMillis() + 15 * 60 * 1000); // 15 Minuten
                            this.lblStatus.setText("Account gesperrt für 15 min. (Notfall: 'unlock123')");
                        } else {
                            this.lblStatus.setText("Benutzername oder Passwort falsch! (" + attempts + "/3)");
                        }
                        this.userDao.updateUser(user);
                    } else {
                        this.lblStatus.setText("Benutzername oder Passwort falsch!");
                    }
                } else {
                    this.lblStatus.setText("Benutzername oder Passwort falsch!");
                }
                this.lblStatus.setTextFill(Color.RED);
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
