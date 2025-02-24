package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.StaffModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class UpdateStaffController implements Initializable {

    @FXML
    private Button cancelBtn;
    @FXML
    private Circle circle;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField idField;
    @FXML
    private ImageView imageView;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField roleField;
    @FXML
    private Button saveBtn;
    @FXML
    private Button uploadImageBtn;

    private StaffModel staff;
    private byte[] staffImage;
    private ObservableList<StaffModel> staffList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set a default image to the Circle (profile picture)
        Image defaultImage = new Image(getClass().getResource("/com/genomicslab/views/img/user.png").toExternalForm());
        circle.setFill(new ImagePattern(defaultImage));

        // Set up button actions
        setupButtons();

    }


    // Utility function to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Close the form after saving the staff
    private void closeForm() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    // Utility function to validate email format
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    private boolean isValidPhone(String phone) {
        return phone.length()==10;
    }

    // Set up button actions for save and upload image buttons
    private void setupButtons() {
        saveBtn.setOnAction(event -> saveStaff());
        uploadImageBtn.setOnAction(event -> uploadImage());
    }




    // Set the staff data to the fields
    public void setStaffData(StaffModel staff) {
        this.staff = staff;
        idField.setText(staff.getId());
        idField.setEditable(false);
        nameField.setText(staff.getName());
        emailField.setText(staff.getEmail());
        phoneField.setText(staff.getPhone());
        departmentField.setText(staff.getDepartment());
        roleField.setText(staff.getRole());

        // Set the profile image and staffImage if available
        if (staff.getPicture() != null) {
            this.staffImage = staff.getPicture(); // Initialize staffImage with current picture
            Image image = new Image(new ByteArrayInputStream(staff.getPicture()));
            circle.setFill(new ImagePattern(image));
        }
    }

    // Save the staff details to the database
    @FXML
    private void saveStaff() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String department = departmentField.getText();
        String role = roleField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || department.isEmpty() || role.isEmpty()) {
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

        // Check image size only if a new image was uploaded
        if (staffImage != null && staffImage != staff.getPicture()) {
            long maxSize = 60 * 1024; // 60KB in bytes
            if (staffImage.length > maxSize) {
                showAlert("Image Size Too Large", "The uploaded image must be smaller than 60KB. Use this website (https://imresizer.com/resize-image-to-60kb)");
                return;
            }
        }

        // If no new image was uploaded, use the existing image from database
        byte[] imageToSave = staffImage != null ? staffImage : staff.getPicture();

        // Update the staff in the database
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "UPDATE staff SET name = ?, email = ?, phone = ?, department = ?, role = ?, picture = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, department);
            statement.setString(5, role);
            statement.setBytes(6, imageToSave);
            statement.setString(7, staff.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Staff updated successfully.");
                closeForm();
            } else {
                showAlert("Error", "Failed to update staff. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while updating the staff.");
        }
    }

    // Handle image upload
    @FXML
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.jpeg"));
        File file = fileChooser.showOpenDialog(uploadImageBtn.getScene().getWindow());
        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file));
                circle.setFill(new ImagePattern(image));
                staffImage = Files.readAllBytes(file.toPath()); // Store the new image bytes
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load the selected image.");
            }
        }
    }
}
