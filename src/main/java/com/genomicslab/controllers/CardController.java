package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.PatientsModel;
import com.genomicslab.models.PersonModel;
import com.genomicslab.models.StaffModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;

public class CardController implements Initializable {

    @FXML
    private Label idField;

    @FXML
    private Button deleteBtn;

    @FXML
    private Label nameField;

    @FXML
    private Label dobField;

    @FXML
    private Label roleTitle;

    @FXML
    private Circle circle;

    @FXML
    private Label emailField;

    @FXML
    private Label phoneField;

    @FXML
    private Button update;

    protected PersonModel person;
    protected Object parentController; // Can handle both PatientsController and StaffController

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        deleteBtn.setOnAction(event -> deleteCard());
        update.setOnAction(event -> handleUpdate());
    }

    public void setData(PersonModel person, Object parentController) {
        this.person = person;
        this.parentController = parentController;

        // Common data
        idField.setText(String.valueOf(person.getId()));
        nameField.setText(person.getName());
        emailField.setText(person.getEmail());
        phoneField.setText(person.getPhone());

        // Profile picture
        ImagePattern imagePattern = new ImagePattern(
                person.getPicture() != null && person.getPicture().length > 0
                        ? new javafx.scene.image.Image(new ByteArrayInputStream(person.getPicture()))
                        : new javafx.scene.image.Image(getClass().getResource("/com/genomicslab/views/img/user.png").toExternalForm())
        );
        circle.setFill(imagePattern);

        // Role-specific data
        if (person instanceof PatientsModel) {
            dobField.setText(((PatientsModel) person).getDob());
//            roleTitle.setText("Patient");
        } else if (person instanceof StaffModel) {
            dobField.setText(""); // Staff might not have a DOB
            roleTitle.setText("Role: " + ((StaffModel) person).getRole());
        }
    }

    private void handleUpdate() {
        try {
            if (person instanceof PatientsModel) {
                showUpdateView("/com/genomicslab/views/updatePatient.fxml", (PatientsModel) person);
            } else if (person instanceof StaffModel) {
                showUpdateView("/com/genomicslab/views/updateStaff.fxml", (StaffModel) person);
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load update view.");
        }
    }

    private void showUpdateView(String viewPath, PersonModel person) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
        Parent parent = loader.load();

        if (person instanceof PatientsModel) {
            UpdatePatientController controller = loader.getController();
            controller.setPatientData((PatientsModel) person);
        } else if (person instanceof StaffModel) {
            UpdateStaffController controller = loader.getController();
            controller.setStaffData((StaffModel) person);
        }

        Stage stage = new Stage();
        stage.setTitle("Update " + (person instanceof PatientsModel ? "Patient" : "Staff"));
        stage.setScene(new Scene(parent));
        stage.show();

        stage.setOnHidden(event -> {
            if (parentController instanceof PatientsController) {
                ((PatientsController) parentController).loadPatients();
            } else if (parentController instanceof StaffController) {
                ((StaffController) parentController).loadStaff();
            }
        });
    }

    private void deleteCard() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this record?", ButtonType.OK, ButtonType.CANCEL);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean isDeleted = deleteFromDatabase(person.getId());
                if (isDeleted) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Record deleted successfully.");
                    refreshParentController();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the record.");
                }
            }
        });
    }

    private boolean deleteFromDatabase(String id) {
        String tableName = person instanceof PatientsModel ? "patients" : "staff";
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, id);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void refreshParentController() {
        if (parentController instanceof PatientsController) {
            ((PatientsController) parentController).loadPatients();
        } else if (parentController instanceof StaffController) {
            ((StaffController) parentController).loadStaff();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}



