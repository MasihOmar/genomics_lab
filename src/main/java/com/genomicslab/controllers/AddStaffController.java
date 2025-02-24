package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.StaffModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ResourceBundle;

public class AddStaffController implements Initializable {

    @FXML
    private TextField nameField;

//    @FXML
//    private ImageView imageView;

    @FXML
    private Button uploadImageBtn;

    @FXML
    private TextField idField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Circle circle;

    @FXML
    private TextField departmentField;

    @FXML
    private TextField roleField;

    @FXML
    private Button saveBtn;
//
//    @FXML
//    private Button cancelBtn;

    private byte[] staffImage;
    private ObservableList<StaffModel> staffList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set default image for the staff member
        Image defaultImage = new Image(getClass().getResource("/com/genomicslab/views/img/user.png").toExternalForm());
        circle.setFill(new ImagePattern(defaultImage));
        setupButtons();
        fetchAllStaff(); // Populate the ObservableList with existing staff
    }

    private void setupButtons() {
        saveBtn.setOnAction(event -> saveStaff());
        uploadImageBtn.setOnAction(event -> uploadImage());
    }

    @FXML
    private void saveStaff() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String department = departmentField.getText();
        String role = roleField.getText();

        // Validate inputs
        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || department.isEmpty() || role.isEmpty()) {
            showAlert("Validation Error", "All fields must be filled out.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!isValidPhone(phone)) {
            showAlert("Invalid Phone", "Please enter a valid phone number(10 digits).");
            return;
        }

        if (staffImage == null) {
            try {
                InputStream is = getClass().getResourceAsStream("/com/genomicslab/views/img/user.png");
                if (is == null) {
                    showAlert("Error", "Default image not found");
                    return;
                }
                staffImage = is.readAllBytes();
                is.close();
            } catch (IOException e) {
                showAlert("Error", "Could not load default image: " + e.getMessage());
                return;
            }
        }

        long maxSize = 60* 1024; // 512KB in bytes
        if (staffImage.length > maxSize) {
            showAlert("Image Size Too Large", "The uploaded image must be smaller than 60KB.Use this website (https://imresizer.com/resize-image-to-60kb)");
            return;
        }

        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "INSERT INTO staff (id, name, email, phone, department, role, picture) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, department);
            statement.setString(6, role);
            statement.setBytes(7, staffImage);

            try {
                if (statement.executeUpdate() > 0) {
                    System.out.println("Staff saved successfully.");
                    fetchAllStaff(); // Refresh the ObservableList after saving

                    // Show a success alert
                    showAlert("Save Successful", "The staff member has been saved successfully.");

                    // Close the current window
                    Stage stage = (Stage) saveBtn.getScene().getWindow();
                    stage.close();
                } else {
                    showAlert("Error", "Error saving staff. Please try again.");
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                showAlert("Duplicate Entry", "A staff member with this ID already exists.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while saving the staff member.");
        }
    }


    private boolean isValidPhone(String phone) {
        return phone.length()==10;
    }



    private void fetchAllStaff() {
        staffList.clear(); // Clear the ObservableList before fetching new data
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "SELECT id, name, email, phone, department, role, picture FROM staff";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                StaffModel staff = new StaffModel(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("department"),
                        resultSet.getBytes("picture"),
                        resultSet.getString("role")


                );
                staffList.add(staff);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg"));
        File file = fileChooser.showOpenDialog(uploadImageBtn.getScene().getWindow());

        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file));
                circle.setFill(new ImagePattern(image));
                staffImage = Files.readAllBytes(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

