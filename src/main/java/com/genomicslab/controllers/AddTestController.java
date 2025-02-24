package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class AddTestController implements Initializable {

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


    private TestController testController;
    private boolean isEditMode = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveBtn.setOnAction(event -> saveTest());
    }

    public void setTestController(TestController controller) {
        this.testController = controller;
    }

    @FXML
    private void saveTest() {
        // Validate the price input before proceeding
        String priceText = testPrice.getText();
        if (!isValidNumber(priceText)) {
            // Display an error alert for invalid price input
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Invalid Price");
            alert.setContentText("Please enter a valid numeric value for the price.");
            alert.showAndWait();
            return; // Exit the method early to prevent invalid data from being saved
        }

        // Check if the test_id already exists in the database
        if (isTestIdDuplicate(testId.getText())) {
            // Display an error alert for duplicate test_id
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Duplicate Test ID");
            alert.setHeaderText("Test ID Already Exists");
            alert.setContentText("The test ID you entered already exists. Please choose a different ID.");
            alert.showAndWait();
            return; // Exit the method if the test ID is a duplicate
        }

        // Proceed with the database operation if the price is valid and test_id is unique
        Connection connection = DatabaseManager.connectDb();
        String query = isEditMode
                ? "UPDATE tests SET name = ?, description = ?, price = ? WHERE test_id = ?"
                : "INSERT INTO tests (test_id, name, description, price) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            if (isEditMode) {
                preparedStatement.setString(1, testName.getText());
                preparedStatement.setString(2, testDescription.getText());
                preparedStatement.setDouble(3, Double.parseDouble(priceText)); // Parsing happens here
                preparedStatement.setString(4, testId.getText());
            } else {
                preparedStatement.setString(1, testId.getText());
                preparedStatement.setString(2, testName.getText());
                preparedStatement.setString(3, testDescription.getText());
                preparedStatement.setDouble(4, Double.parseDouble(priceText)); // Parsing happens here
            }

            preparedStatement.executeUpdate();
            testController.refreshTable();

            // Show a success alert when the data is saved
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Save Successful");
            alert.setHeaderText("Test Saved");
            alert.setContentText("The test has been saved successfully.");
            alert.showAndWait();

            // Close the current window after saving
            Stage stage = (Stage) saveBtn.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Error Saving Test");
            alert.setContentText("An error occurred while saving the test. Please try again.");
            alert.showAndWait();
        }
    }

    // Method to check if a test_id already exists in the database
    private boolean isTestIdDuplicate(String testId) {
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "SELECT COUNT(*) FROM tests WHERE test_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, testId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Returns true if a duplicate test_id is found
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // No duplicate found
    }


    // Method to validate if a string is a valid number
    private boolean isValidNumber(String input) {
        try {
            Double.parseDouble(input); // Try parsing to a double
            return true;
        } catch (NumberFormatException e) {
            return false; // If an exception occurs, it's not a valid number
        }
    }
}


