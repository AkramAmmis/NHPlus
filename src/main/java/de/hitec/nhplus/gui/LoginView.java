package de.hitec.nhplus.gui;

import de.hitec.nhplus.controller.LoginController;
import de.hitec.nhplus.controller.LoginViewController;
import de.hitec.nhplus.controller.MainWindowController;
import de.hitec.nhplus.service.LoginLogService;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView {

    private Stage stage;
    private LoginController controller;
    private MainWindowController mainController;

    private LoginLogService loginLogService = new LoginLogService();

    public LoginView(Stage primaryStage, MainWindowController mainController) {
        this.stage = primaryStage;
        this.controller = new LoginController();
        this.mainController = mainController;

        try {
            initializeWithFXML();
        } catch (Exception e) {
            System.err.println("Fehler beim Laden der FXML-UI: " + e.getMessage());
            e.printStackTrace();
            initializeUI();
        }
    }

    private void initializeWithFXML() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/de/hitec/nhplus/fxml/LoginView.fxml"));
        Parent root = loader.load();

        LoginViewController controller = loader.getController();
        controller.setMainWindowController(this.mainController);

        if (this.mainController != null) {
            this.mainController.setPrimaryStage(this.stage);
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("NHPlus - Login");
        stage.show();
    }

    private void initializeUI() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Willkommen bei NHPlus");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label userName = new Label("Benutzername:");
        grid.add(userName, 0, 1);
        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Passwort:");
        grid.add(pw, 0, 2);
        PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);

        Button btn = new Button("Anmelden");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        btn.setOnAction(event -> {
            String username = userTextField.getText();
            String password = pwBox.getText();
            String ipAddress = "localhost";

            if (username.isEmpty() || password.isEmpty()) {
                showError("Bitte geben Sie Benutzername und Passwort ein.");
                loginLogService.logLoginAttempt(username, ipAddress, false, "Eingabe leer");
                return;
            }

            boolean success = controller.login(username, password);

            if (success) {
                loginLogService.logLoginAttempt(username, ipAddress, true, null);
                if (mainController != null) {
                    mainController.setPrimaryStage(stage);
                    mainController.showMainView();
                }
            } else {
                loginLogService.logLoginAttempt(username, ipAddress, false, "Falsche Anmeldedaten");
                showError("Anmeldung fehlgeschlagen. Bitte überprüfen Sie Benutzername und Passwort.");
            }
        });

        Scene scene = new Scene(grid, 400, 275);
        stage.setScene(scene);
        stage.setTitle("NHPlus - Login");
        stage.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
