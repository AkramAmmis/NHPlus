package de.hitec.nhplus.controller;

import de.hitec.nhplus.Main;
import de.hitec.nhplus.utils.AuthorizationManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import java.io.IOException;

public class MainWindowController {
    @FXML
    private BorderPane mainBorderPane;
    private Stage primaryStage;
    private Main main;
    private PauseTransition inactivityTimer;
    private static final int INACTIVITY_MINUTES = 15;
    public void setMain(Main main) {
        this.main = main;
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void showMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            BorderPane pane = loader.load();
            MainWindowController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            if (main != null) {
                controller.setMain(main);
            }
            updateUserStatusDisplay(pane);
            Scene scene = new Scene(pane);
            primaryStage.setTitle("NHPlus - Angemeldet als " + 
                AuthorizationManager.getInstance().getCurrentUser().getFullName());
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            setupInactivityTimer(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setupInactivityTimer(Scene scene) {
        inactivityTimer = new PauseTransition(Duration.minutes(INACTIVITY_MINUTES));
        inactivityTimer.setOnFinished(e -> handleAutoLogout());
        scene.addEventFilter(MouseEvent.ANY, e -> resetInactivityTimer());
        scene.addEventFilter(KeyEvent.ANY, e -> resetInactivityTimer());
        resetInactivityTimer();
    }
    private void resetInactivityTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.stop();
            inactivityTimer.playFromStart();
        }
    }
    private void handleAutoLogout() {
        AuthorizationManager.getInstance().logout();
        Platform.runLater(() -> {
            if (main != null) {
                main.showLoginView();
            } else if (primaryStage != null) {
                new de.hitec.nhplus.gui.LoginView(primaryStage, this);
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Automatische Abmeldung");
            alert.setHeaderText(null);
            alert.setContentText("Sie wurden nach 15 Minuten Inaktivität automatisch abgemeldet.");
            alert.showAndWait();
        });
    }
    private void updateUserStatusDisplay(BorderPane pane) {
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
            AllCaregiverController controller = loader.getController();
            if (controller != null) {
                controller.setPrimaryStage(primaryStage);
                controller.setMainWindowController(this);
            }
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
    @FXML
    private void handleUserManagement(ActionEvent event) {
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
        // Perform logout
        AuthorizationManager.getInstance().logout();

        // Close the current stage
        Stage stage = (Stage) mainBorderPane.getScene().getWindow();
        stage.close();

        // Open the login view
        Main mainApp = getMain();
        if (mainApp != null) {
            mainApp.showLoginView();
        }
    }
    public Main getMain() {
        return this.main;
    }
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
