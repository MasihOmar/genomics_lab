package com.genomicslab.controllers;


import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.PatientsModel;
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

public class AddPatientController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private ImageView imageView;

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
    private DatePicker dobPicker;

    @FXML
    private TextField genderField;

    @FXML
    private TextArea medicalHistoryField;

    @FXML
    private Button saveBtn;

    @FXML
    private Button cancelBtn;

    private byte[] patientImage;
    private ObservableList<PatientsModel> patients = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image defaultImage = new Image(getClass().getResource("/com/genomicslab/views/img/user.png").toExternalForm());
        circle.setFill(new ImagePattern(defaultImage));
        setupButtons();
        fetchAllPatients(); // Populate the ObservableList with existing patients
    }

    private void setupButtons() {
        saveBtn.setOnAction(event -> savePatient());
        uploadImageBtn.setOnAction(event -> uploadImage());
    }

    @FXML
    private void savePatient() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String dob = dobPicker.getValue() != null ? dobPicker.getValue().toString() : null;
        String gender = genderField.getText();
        String medicalHistory = medicalHistoryField.getText();

        // Validate inputs
        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || dob == null || gender.isEmpty()) {
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

        if (patientImage == null) {
            try {
                InputStream is = getClass().getResourceAsStream("/com/genomicslab/views/img/user.png");
                if (is == null) {
                    showAlert("Error", "Default image not found");
                    return;
                }
                patientImage = is.readAllBytes();
                is.close();
            } catch (IOException e) {
                showAlert("Error", "Could not load default image: " + e.getMessage());
                return;
            }
        }


        long maxSize = 60* 1024; // 512KB in bytes
        if (patientImage.length > maxSize) {
            showAlert("Image Size Too Large", "The uploaded image must be smaller than 60KB. Use this website (https://imresizer.com/resize-image-to-60kb)");
            return;
        }

        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "INSERT INTO patients (id, name, email, phone, dob, gender, medical_history, picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, dob);
            statement.setString(6, gender);
            statement.setString(7, medicalHistory);
            statement.setBytes(8, patientImage);

            try {
                if (statement.executeUpdate() > 0) {
                    System.out.println("Patient saved successfully.");
                    fetchAllPatients(); // Refresh the ObservableList after saving

                    // Show a success alert
                    showAlert("Save Successful", "The patient has been saved successfully.");

                    // Close the current window
                    Stage stage = (Stage) saveBtn.getScene().getWindow();
                    stage.close();
                } else {
                    showAlert("Error", "Error saving patient. Please try again.");
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                showAlert("Duplicate Entry", "A patient with this ID already exists.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred while saving the patient.");
        }
    }


    private boolean isValidPhone(String phone) {
        return phone.length()==10;
    }



    private void fetchAllPatients() {
        patients.clear(); // Clear the ObservableList before fetching new data
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "SELECT id, name, email, phone, dob, gender, medical_history, picture FROM patients";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                PatientsModel patient = new PatientsModel(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getBytes("picture"),
                        resultSet.getString("dob"),
                        resultSet.getString("gender"),
                        resultSet.getString("medical_history")
                );
                patients.add(patient);
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
                patientImage = Files.readAllBytes(file.toPath());
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




    public ObservableList<PatientsModel> getPatients() {
        return patients;
    }
}
