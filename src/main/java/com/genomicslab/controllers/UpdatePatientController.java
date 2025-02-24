package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.PatientsModel;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class UpdatePatientController implements Initializable {

    @FXML
    private Circle circle;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private TextField emailField;

    @FXML
    private TextField genderField;

    @FXML
    private TextField idField;  // ID field will be read-only

    @FXML
    private ImageView imageView;

    @FXML
    private TextArea medicalHistoryField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button saveBtn;

    @FXML
    private Button uploadImageBtn;

    private PatientsModel patient;  // To store the patient data
    private byte[] patientImage; // Store the uploaded image bytes

    private ObservableList<PatientsModel> patientsList; // Observable list for existing patients

    // Initialize method from Initializable interface
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set a default image to the Circle (profile picture)
        Image defaultImage = new Image(getClass().getResource("/com/genomicslab/views/img/user.png").toExternalForm());
        circle.setFill(new ImagePattern(defaultImage));

        // Set up button actions
        setupButtons();
    }

    // Set the patient data to the fields
    public void setPatientData(PatientsModel patient) {
        this.patient = patient;

        // Populate the fields with the existing data
        idField.setText(patient.getId());
        idField.setEditable(false);
        nameField.setText(patient.getName());
        emailField.setText(patient.getEmail());
        phoneField.setText(patient.getPhone());
        dobPicker.setValue(patient.getDob() != null ? LocalDate.parse(patient.getDob()) : null);
        genderField.setText(patient.getGender());
        medicalHistoryField.setText(patient.getMedicalHistory());

        // Set the profile image
        // Set the profile image
        if (patient.getPicture() != null) {
            Image image = new Image(new ByteArrayInputStream(patient.getPicture()));
            circle.setFill(new ImagePattern(image));
        }

    }

    protected PatientsController patientUpdateCallback;  // Reference to the callback

    // Method to set the callback
    public void setPatientUpdateCallback(PatientsController callback) {
        this.patientUpdateCallback = callback;
    }


    @FXML
    private void savePatient() {
        // Validate inputs
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String dob = dobPicker.getValue() != null ? dobPicker.getValue().toString() : null;
        String gender = genderField.getText();
        String medicalHistory = medicalHistoryField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || dob == null || gender.isEmpty()) {
            showAlert("Validation Error", "All fields must be filled out.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (!isValidPhone(phone)) {
            showAlert("Invalid Phone number", "Please enter a valid phone number(10 digits).");
            return;
        }

        // Check image size only if a new image was uploaded
        if (patientImage != null && patientImage != patient.getPicture()) {
            long maxSize = 60 * 1024; // 60KB in bytes
            if (patientImage.length > maxSize) {
                showAlert("Image Size Too Large", "The uploaded image must be smaller than 60KB. Use this website (https://imresizer.com/resize-image-to-60kb)");
                return;
            }
        }
        // If no new image was uploaded, use the existing image from database
        byte[] imageToSave = patientImage != null ? patientImage : patient.getPicture();


        // Update the patient in the database
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "UPDATE patients SET name = ?, email = ?, phone = ?, dob = ?, gender = ?, medical_history = ?, picture = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, phone);
            statement.setString(4, dob);
            statement.setString(5, gender);
            statement.setString(6, medicalHistory);
            statement.setBytes(7, patientImage != null ? patientImage : patient.getPicture());  // Save new image if uploaded, otherwise retain the old one
            statement.setString(8, patient.getId());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                // Trigger the callback to refresh the grid in the main controller

                showAlert("Success", "Patient updated successfully.");
                closeForm();

                // After updating, refresh the patient list in PatientsController
                if (patientUpdateCallback != null) {
                    patientUpdateCallback.loadPatients();  // Refresh the list in PatientsController
                }
            } else {
                showAlert("Error", "Failed to update patient. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while updating the patient.");
        }
    }

    private boolean isValidPhone(String phone) {
        return phone.length()==10;
    }




    // Utility function to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Close the form after saving the patient
    private void closeForm() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }

    // Utility function to validate email format
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    // Set up button actions for save and upload image buttons
    private void setupButtons() {
        saveBtn.setOnAction(event -> savePatient());
        uploadImageBtn.setOnAction(event -> uploadImage());
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
                patientImage = Files.readAllBytes(file.toPath()); // Store the image bytes for database update
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load the selected image.");
            }
        }
    }
}

