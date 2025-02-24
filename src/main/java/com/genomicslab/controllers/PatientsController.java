package com.genomicslab.controllers;

import animatefx.animation.*;
import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.PatientsModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;


public class PatientsController implements Initializable {

    @FXML
    private GridPane grid;

    @FXML
    private Button addBtn;

    @FXML
    private TextField search;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button patientBtn;

    @FXML
    private Button reportBtn;

    @FXML
    private ScrollPane scroll;

    @FXML
    private AnchorPane sidebar;

    @FXML
    private Button staffBtn;

    @FXML
    private Button testBtn;

    private ObservableList<PatientsModel> patients;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadPatients();
        setupAddButton();
        setupSearchListener();
    }

    public void loadPatients() {
        try {
            // Fetch the patients directly from the database
            List<PatientsModel> patientList = fetchAllPatientsFromDatabase();
            patients = FXCollections.observableArrayList(patientList); // Convert List to ObservableList
            populateGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private List<PatientsModel> fetchAllPatientsFromDatabase() {
        List<PatientsModel> patientList = new ArrayList<>();
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
                patientList.add(patient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return patientList;
    }

    private void setupSearchListener() {
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filterPatients(newValue); // Filter patients based on the search input
        });
    }

    private void filterPatients(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            populateGrid(); // Show all patients if the search is empty
            return;
        }

        ObservableList<PatientsModel> filteredPatients = FXCollections.observableArrayList();
        for (PatientsModel patient : patients) {
            // Convert numeric fields to strings for comparison
            String phone = patient.getPhone() != null ? patient.getPhone() : "";
            String id = patient.getId() != null ? patient.getId() : "";

            // Check if the patient's name, email, phone, or ID contains the search text (case-insensitive)
            if (patient.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    patient.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                    phone.contains(searchText) || // Match numbers in phone
                    id.contains(searchText)) {    // Match numbers in ID
                filteredPatients.add(patient);
            }
        }

        populateGrid(filteredPatients); // Populate the grid with the filtered list
    }


    private void populateGrid() {
        populateGrid(patients); // Default to using the full patients list
    }


    // Populate the grid with a given list of patients
    private void populateGrid(ObservableList<PatientsModel> patientList) {
        grid.getChildren().clear();
        int column = 0;
        int row = 1;

        try {
            Timeline timeline = new Timeline();
            for (PatientsModel patient : patientList) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/genomicslab/views/card.fxml"));
                AnchorPane pane = fxmlLoader.load();

                CardController controller = fxmlLoader.getController();
                controller.setData(patient, this);

                if (column == 1) {
                    column = 0;
                    row++;
                }

                int finalColumn = column;
                int finalRow = row;
                KeyFrame keyFrame = new KeyFrame(Duration.seconds((column + row) * 0.2), e -> {
                    grid.add(pane, finalColumn, finalRow);
                    new SlideInRight(pane).play();
                    GridPane.setMargin(pane, new Insets(10));
                });

                timeline.getKeyFrames().add(keyFrame);
                column++;
            }
            timeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setupAddButton() {
        addBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/addPatient.fxml"));
                AnchorPane pane = loader.load();
                AddPatientController controller = loader.getController();

                // Set up the add patient stage
                Stage stage = new Stage();
//                stage.initStyle(StageStyle.TRANSPARENT);
                Scene scene = new Scene(pane);
//                scene.setFill(Color.TRANSPARENT);
                stage.setScene(scene);
                stage.setTitle("Add Patient");

                // Refresh grid when the add patient window is closed
                stage.setOnHidden(e -> loadPatients());
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

//    // Utility method to switch scenes
//    private void switchPage(String fxmlPath, Button button) {
//        button.getScene().getWindow().hide();
//        try {
//            SceneManager.switchScene(fxmlPath);
//        } catch (IOException e) {
//            e.printStackTrace(); // Handle exception appropriately
//        }
//    }

//    // Specific page navigation methods
//    @FXML
//    private void testPage() {
//        switchPage("/com/genomicslab/views/tests.fxml", testBtn);
//    }
//
//    @FXML
//    private void resultPage() {
//        switchPage("/com/genomicslab/views/results.fxml", reportBtn);
//    }
//
//    @FXML
//    private void staffPage() {
//        switchPage("/com/genomicslab/views/staff.fxml", staffBtn);
//    }
//
//    @FXML
//    private void loginPage() {
//        switchPage("/com/genomicslab/views/login.fxml", logoutBtn);
//    }

//    @FXML
//    public void close() {
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?");
//        alert.showAndWait().ifPresent(response -> {
//            if (response == ButtonType.OK) {
//                System.out.println("Closing application...");
//                System.exit(0);
//            }
//        });
//    }
}


