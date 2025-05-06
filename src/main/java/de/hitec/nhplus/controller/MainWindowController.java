package de.hitec.nhplus.controller;

import de.hitec.nhplus.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane mainBorderPane; // Das Hauptlayout des Fensters (BorderPane), in das andere Ansichten geladen werden.

    @FXML
    private void handleShowAllPatient(ActionEvent event) {
        // Diese Methode wird aufgerufen, wenn der Button "Patienten anzeigen" geklickt wird.
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de.hitec/nhplus/AllPatientView.fxml")); // Lädt die Patientenansicht (AllPatientView.fxml).
        try {
            mainBorderPane.setCenter(loader.load()); // Setzt die Patientenansicht in den zentralen Bereich des BorderPane.
        } catch (IOException exception) {
            exception.printStackTrace(); // Gibt einen Fehler aus, falls die FXML-Datei nicht geladen werden kann.
        }
    }

    @FXML
    private void handleShowAllTreatments(ActionEvent event) {
        // Diese Methode wird aufgerufen, wenn der Button "Behandlungen anzeigen" geklickt wird.
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de.hitec/nhplus/AllTreatmentView.fxml")); // Lädt die Behandlungsansicht (AllTreatmentView.fxml).
        try {
            mainBorderPane.setCenter(loader.load()); // Setzt die Behandlungsansicht in den zentralen Bereich des BorderPane.
        } catch (IOException exception) {
            exception.printStackTrace(); // Gibt einen Fehler aus, falls die FXML-Datei nicht geladen werden kann.
        }
    }
}
