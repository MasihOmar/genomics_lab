package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.TestsModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UpdateTestController implements Initializable {

    @FXML
    private TextField testId;

    @FXML
    private TextField testName;

    @FXML
    private TextArea testDescription;

    @FXML
    private TextField testPrice;

    @FXML
    private Button saveBtn;
    

    private TestsModel currentTest;
    private TestController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up button actions
        saveBtn.setOnAction(this::saveTest);

    }


    public void setTestData(TestsModel test) {
        this.currentTest = test;

        // Populate the form with test data
        testId.setText(test.getTestId());
        testName.setText(test.getName());
        testDescription.setText(test.getDescription());
        testPrice.setText(String.valueOf(test.getPrice()));

        // Test ID is not editable
        testId.setDisable(true);
    }

    /**
     * Sets the parent controller to refresh the table after updating.
     *
     * @param controller The parent TestController.
     */
    public void setParentController(TestController controller) {
        this.parentController = controller;
    }

    /**
     * Handles saving the updated test data to the database.
     *
     * @param event The ActionEvent triggered by the save button.
     */
    @FXML
    private void saveTest(ActionEvent event) {
        // Validate the price input
        String priceText = testPrice.getText();
        if (!isValidNumber(priceText)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Invalid Price", "Please enter a valid numeric value for the price.");
            return;
        }

        // Ensure all required fields are filled
        if (testName.getText().isEmpty() || testDescription.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Missing Data", "Incomplete Form", "Please fill in all required fields.");
            return;
        }

        // Update the test in the database
        String query = "UPDATE tests SET name = ?, description = ?, price = ? WHERE test_id = ?";
        try (Connection connection = DatabaseManager.connectDb();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, testName.getText());
            preparedStatement.setString(2, testDescription.getText());
            preparedStatement.setDouble(3, Double.parseDouble(priceText));
            preparedStatement.setString(4, testId.getText());

            preparedStatement.executeUpdate();

            // Show success alert
            showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Test Updated", "The test has been updated successfully.");

            // Refresh the parent table
            if (parentController != null) {
                parentController.refreshTable();
            }

            // Close the update window
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error Updating Test", "An error occurred while updating the test. Please try again.");
        }
    }


    private boolean isValidNumber(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
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
