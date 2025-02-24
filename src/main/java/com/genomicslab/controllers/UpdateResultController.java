package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.ResultsModel;
import com.jfoenix.controls.JFXComboBox;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class UpdateResultController implements Initializable {

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

    private ResultsModel currentResult;
    private ResultsController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up button actions
        saveBtn.setOnAction(this::saveResult);


        // Load available patient and test IDs for the combo boxes
        loadPatientIds();
        loadTestIds();
    }


    public void setResultToUpdate(ResultsModel result) {
        this.currentResult = result;

        // Populate the form with result data
        resultId.setText(result.getResultId());
        testId.setValue(result.getTestId()); // Set the test ID in the combo box
        patientId.setValue(result.getPatientId()); // Set the patient ID in the combo box
        resultDetails.setText(result.getResultDetails());

        // Convert LocalDateTime to LocalDate for the DatePicker
        LocalDate resultLocalDate = result.getResultDate().toLocalDate(); // Convert if needed
        resultDate.setValue(resultLocalDate); // Set the result date (LocalDate)

        // Result ID is not editable
        resultId.setDisable(true);
    }


    public void setParentController(ResultsController controller) {
        this.parentController = controller;
    }



    private void loadPatientIds() {
        String query = "SELECT id FROM patients";  // Make sure the table and column are correct

        try (Connection connection = DatabaseManager.connectDb();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            // Clear existing items in the combo box
            patientId.getItems().clear();

            // Loop through the result set and add patient IDs to the combo box
            while (resultSet.next()) {
                patientId.getItems().add(resultSet.getString("id"));
            }

        } catch (SQLException e) {
            // Log the exception to understand the issue better
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error Loading Patient IDs", "An error occurred while loading patient IDs. Please try again.");
        }
    }


    private void loadTestIds() {
        String query = "SELECT test_id FROM tests";  // Modify with actual query to fetch test IDs
        try (Connection connection = DatabaseManager.connectDb();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Clear the combo box before adding new items
            testId.getItems().clear();

            while (rs.next()) {
                testId.getItems().add(rs.getString("test_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error Loading Test IDs", "An error occurred while loading test IDs.");
        }
    }


    @FXML
    private void saveResult(ActionEvent event) {
        // Ensure all required fields are filled
        if (testId.getValue() == null || patientId.getValue() == null || resultDetails.getText().isEmpty() || resultDate.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Missing Data", "Incomplete Form", "Please fill in all required fields.");
            return;
        }

        // Prepare the update query
        String query = "UPDATE test_results SET test_id = ?, patient_id = ?, result_details = ?, result_date = ? WHERE result_id = ?";

        try (Connection connection = DatabaseManager.connectDb();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the parameters for the prepared statement
            preparedStatement.setString(1, testId.getValue());
            preparedStatement.setString(2, patientId.getValue());
            preparedStatement.setString(3, resultDetails.getText());
            preparedStatement.setString(4, resultDate.getValue().toString()); // Convert LocalDate to String
            preparedStatement.setString(5, resultId.getText());

            // Execute the update query
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                // Show success alert
                showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Result Updated", "The result has been updated successfully.");

                // Refresh the parent table
                if (parentController != null) {
                    parentController.refreshTable();
                }

                // Close the update window
                Stage stage = (Stage) saveBtn.getScene().getWindow();
                stage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Error Updating Result", "An error occurred while updating the result. Please try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error Updating Result", "An error occurred while updating the result. Please try again.");
        }
    }


    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


