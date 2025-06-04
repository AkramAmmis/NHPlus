package de.hitec.nhplus.controller;

import de.hitec.nhplus.model.LoginLog;
import de.hitec.nhplus.service.LoginLogService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class LoginLogController implements Initializable {
    @FXML
    private TableView<LoginLog> logTable;
    @FXML
    private TableColumn<LoginLog, LocalDateTime> timestampColumn;
    @FXML
    private TableColumn<LoginLog, String> usernameColumn;
    @FXML
    private TableColumn<LoginLog, String> ipAddressColumn;
    @FXML
    private TableColumn<LoginLog, String> statusColumn;
    @FXML
    private TableColumn<LoginLog, String> reasonColumn;

    private LoginLogService loginLogService = new LoginLogService();
    private ObservableList<LoginLog> logs = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadLogs();
    }

    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        statusColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isSuccessful() ? "SUCCESS" : "FAILED");
        });
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("failureReason"));

        logTable.setItems(logs);
    }

    private void loadLogs() {
        logs.clear();
        logs.addAll(loginLogService.getRecentLogs(100));
    }

    @FXML
    private void refreshLogs() {
        loadLogs();
    }
}
