package de.hitec.nhplus;

import de.hitec.nhplus.datastorage.ConnectionBuilder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

// Die Main-Klasse erbt von JavaFX Application und ist der Einstiegspunkt der JavaFX-Anwendung.
public class Main extends Application {

    private Stage primaryStage; // Die Hauptbühne (Fenster) der Anwendung.

    @Override
    public void start(Stage primaryStage) {
        // Diese Methode wird beim Start der Anwendung aufgerufen.
        this.primaryStage = primaryStage; // Speichert die Hauptbühne.
        mainWindow(); // Lädt das Hauptfenster der Anwendung.
    }

    public void mainWindow() {
        try {
            // Lädt die FXML-Datei, die das Layout des Hauptfensters definiert.
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            BorderPane pane = loader.load(); // Lädt das Layout als BorderPane.

            // Erstellt eine neue Szene mit dem geladenen Layout.
            Scene scene = new Scene(pane);
            this.primaryStage.setTitle("NHPlus"); // Setzt den Fenstertitel.
            this.primaryStage.setScene(scene); // Setzt die Szene auf die Hauptbühne.
            this.primaryStage.setResizable(false); // Verhindert, dass das Fenster skaliert wird.
            this.primaryStage.show(); // Zeigt das Fenster an.

            // Fügt eine Aktion hinzu, die beim Schließen des Fensters ausgeführt wird.
            this.primaryStage.setOnCloseRequest(event -> {
                ConnectionBuilder.closeConnection(); // Schließt die Datenbankverbindung.
                Platform.exit(); // Beendet die JavaFX-Plattform.
                System.exit(0); // Beendet die Anwendung vollständig.
            });
        } catch (IOException exception) {
            // Gibt den Fehler aus, falls die FXML-Datei nicht geladen werden kann.
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Startet die JavaFX-Anwendung.
        launch(args);
    }
}