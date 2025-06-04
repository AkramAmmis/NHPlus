package de.hitec.nhplus;

import de.hitec.nhplus.controller.MainWindowController;
import de.hitec.nhplus.controller.LoginViewController;
import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.DatabaseInitializer;
import de.hitec.nhplus.gui.LoginView;
import de.hitec.nhplus.utils.AuthorizationManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

     
        if (AuthorizationManager.getInstance().isLoggedOut()) {
    
            showLoginView();
        } else {
     
            mainWindow();
        }
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/fxml/LoginView.fxml"));
            Parent root = loader.load();

            LoginViewController controller = loader.getController();
            controller.setMainWindowController(new MainWindowController());

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            BorderPane pane = loader.load();

           
            MainWindowController controller = loader.getController();
            controller.setMain(this);
            controller.setPrimaryStage(this.primaryStage);

          
            try {
                // Use DatabaseInitializer to ensure all required tables are created
                DatabaseInitializer.initializeDatabase();
                System.out.println("Alle Tabellen wurden initialisiert.");
            } catch (Exception e) {
                System.err.println("Fehler bei der Tabelleninitialisierung: " + e.getMessage());
                e.printStackTrace();
            }

       
            String title = "NHPlus";
            if (!AuthorizationManager.getInstance().isLoggedOut()) {
                title += " - Angemeldet als " + 
                    AuthorizationManager.getInstance().getCurrentUser().getFullName();
            }

            Scene scene = new Scene(pane);
            this.primaryStage.setTitle(title);
            this.primaryStage.setScene(scene);
            this.primaryStage.setResizable(true);
            this.primaryStage.show();

            this.primaryStage.setOnCloseRequest(event -> {
                ConnectionBuilder.closeConnection();
                Platform.exit();
                System.exit(0);
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Returns the primary stage for window operations
     * @return the primary stage
     */
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }
    
    /**
     * Handles logout and returns to login view
     */
    public void handleLogout() {
        Platform.runLater(() -> {
            // Clear current scene and show login
            AuthorizationManager.getInstance().logout();
            showLoginView();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}