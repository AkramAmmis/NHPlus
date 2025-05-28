package de.hitec.nhplus.controller;

import de.hitec.nhplus.Main;
import de.hitec.nhplus.utils.AuthorizationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane mainBorderPane;

    private Stage primaryStage;
    private Main main;

    /**
     * Setzt die Hauptanwendung
     * @param main Die Hauptanwendung
     */
    public void setMain(Main main) {
        this.main = main;
    }

    /**
     * Setzt die Hauptbühne der Anwendung
     * @param primaryStage Die Hauptbühne
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Zeigt die Hauptansicht der Anwendung
     */
    public void showMainView() {
        try {
            // Hauptfenster laden und anzeigen
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            BorderPane pane = loader.load();

            // Controller holen und konfigurieren
            MainWindowController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            if (main != null) {
                controller.setMain(main);
            }

            // Statusanzeige für den angemeldeten Benutzer hinzufügen
            updateUserStatusDisplay(pane);

            // Szene erstellen und anzeigen
            Scene scene = new Scene(pane);
            primaryStage.setTitle("NHPlus - Angemeldet als " + 
                AuthorizationManager.getInstance().getCurrentUser().getFullName());
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Aktualisiert die Benutzerstatusanzeige im Hauptfenster
     * @param pane Der Hauptcontainer
     */
    private void updateUserStatusDisplay(BorderPane pane) {
        // Hier könnte man zusätzliche UI-Elemente hinzufügen, die den aktuellen Benutzer anzeigen
        // Zum Beispiel ein Label in der Statuszeile oder eine Benutzerinfo im Menü
    }

    @FXML
    private void handleShowAllPatient(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/AllPatientView.fxml"));
        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleShowAllCaregiver(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/AllCaregiverView.fxml"));
        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleShowAllTreatments(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/AllTreatmentView.fxml"));
        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Behandelt das Logout-Ereignis
     * @param event Das ActionEvent
     */
    @FXML
    private void handleUserManagement(ActionEvent event) {
        // Nur Administratoren dürfen die Benutzerverwaltung öffnen
        if (!AuthorizationManager.getInstance().isAdmin()) {
            showErrorAlert("Keine Berechtigung", "Sie haben keine Berechtigung für die Benutzerverwaltung.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/UserManagementView.fxml"));
        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Benutzer abmelden
        AuthorizationManager.getInstance().logout();

        try {
            // Login-Fenster anzeigen
            if (main != null) {
                main.showLoginView();
                return;
            }

            // Fallback, falls main null ist
            javafx.scene.Node source = (javafx.scene.Node) event.getSource();
            javafx.stage.Stage currentStage = (javafx.stage.Stage) source.getScene().getWindow();

            // Login-Fenster mit der LoginView-Klasse erstellen
            new de.hitec.nhplus.gui.LoginView(currentStage, this);
        } catch (Exception e) {
            System.err.println("Fehler beim Anzeigen des Login-Fensters: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Aktuelle Anwendung schließen
            try {
                javafx.scene.Node source = (javafx.scene.Node) event.getSource();
                javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
                stage.close();
            } catch (Exception ex) {
                System.err.println("Fehler beim Schließen des Fensters: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Zeigt eine Fehlermeldung an
     * @param title Titel der Meldung
     * @param message Inhalt der Meldung
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
