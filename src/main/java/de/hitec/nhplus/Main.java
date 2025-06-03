package de.hitec.nhplus;

import de.hitec.nhplus.controller.MainWindowController;
import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.gui.LoginView;
import de.hitec.nhplus.utils.AuthorizationManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            BorderPane dummy = loader.load();

            MainWindowController mainController = loader.getController();
            mainController.setMain(this);
            mainController.setPrimaryStage(primaryStage);

           
            Stage loginStage = new Stage();
            new LoginView(loginStage, mainController);
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
              
                DaoFactory.getDaoFactory().createUserDAO().createTable();
                System.out.println("Benutzer-Tabelle initialisiert.");

               
                CaregiverDao caregiverDao = DaoFactory.getDaoFactory().createCaregiverDAO();
                caregiverDao.createTable();
                System.out.println("Pfleger-Tabelle initialisiert.");
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