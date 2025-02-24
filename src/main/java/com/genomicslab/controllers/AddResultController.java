package com.genomicslab.controllers;

import com.jfoenix.controls.JFXComboBox;
import com.genomicslab.backend.DatabaseManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AddResultController {

    @FXML
    private Button cancelBtn;

    @FXML
    private JFXComboBox<String> patientId;

    @FXML
    private DatePicker resultDate;

    @FXML
    private TextArea resultDetails;

    @FXML
    private TextField resultId;

    @FXML
    private Button saveBtn;

    @FXML
    private JFXComboBox<String> testId;

    private ResultsController resultsController;

    private ObservableList<String> patientIds = FXCollections.observableArrayList();
    private ObservableList<String> testIds = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadPatientIds();
        loadTestIds();
    }


    public void setResultController(ResultsController controller) {
        this.resultsController = controller;
    }

    private void loadPatientIds() {
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "SELECT id FROM patients";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                patientIds.add(resultSet.getString("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Populate the combo box with the retrieved patient IDs
        patientId.setItems(patientIds);
    }

    private void loadTestIds() {
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "SELECT test_id FROM tests";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                testIds.add(resultSet.getString("test_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Populate the combo box with the retrieved test IDs
        testId.setItems(testIds);
    }

    @FXML
    void saveTest(ActionEvent event) {
        String selectedPatientId = patientId.getValue();
        String selectedTestId = testId.getValue();
        String resultIdText = resultId.getText();
        String resultDateText = resultDate.getValue() != null ? resultDate.getValue().toString() : null;
        String resultDetailsText = resultDetails.getText();

        // Validate inputs
        if (selectedPatientId == null || selectedTestId == null || resultIdText.isEmpty() || resultDateText == null || resultDetailsText.isEmpty()) {
            showAlert(AlertType.WARNING, "Validation Error", "Please fill all the required fields.");
            return;
        }

        // Insert the result into the database
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "INSERT INTO test_results (result_id, patient_id, test_id, result_date, result_details) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, resultIdText);
            statement.setString(2, selectedPatientId);
            statement.setString(3, selectedTestId);
            statement.setString(4, resultDateText);
            statement.setString(5, resultDetailsText);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Test result saved successfully.");
                closeWindow();
            } else {
                showAlert(AlertType.ERROR, "Error", "Error saving test result.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "An error occurred while saving the test result.");
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close(); // Close the current window after saving the result
    }
}

