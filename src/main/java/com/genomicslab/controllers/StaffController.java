package com.genomicslab.controllers;

import animatefx.animation.SlideInLeft;
import animatefx.animation.SlideInRight;
import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.StaffModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class StaffController implements Initializable {

    @FXML
    private GridPane grid;

    @FXML
    private Button addBtn;

    @FXML
    private TextField search;

    private ObservableList<StaffModel> staff;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadStaff();
        setupAddButton();
        setupSearchListener();
    }

    // Load staff data from the database
    protected void loadStaff() {
        try {
            // Fetch the staff directly from the database
            List<StaffModel> staffList = fetchAllStaffFromDatabase();
            staff = FXCollections.observableArrayList(staffList); // Convert List to ObservableList
            populateGrid();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch all staff from the database
    private List<StaffModel> fetchAllStaffFromDatabase() {
        List<StaffModel> staffList = new ArrayList<>();
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "SELECT id, name, email, phone, picture, role, department FROM staff";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                StaffModel staffMember = new StaffModel(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("department"),
                        resultSet.getBytes("picture"),
                        resultSet.getString("role")

                );
                staffList.add(staffMember);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return staffList;
    }

    // Set up the search listener for filtering staff
    private void setupSearchListener() {
        search.textProperty().addListener((observable, oldValue, newValue) -> {
            filterStaff(newValue); // Filter staff based on the search input
        });
    }

    // Filter staff based on the search input
    private void filterStaff(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            populateGrid(); // Show all staff if the search is empty
            return;
        }

        ObservableList<StaffModel> filteredStaff = FXCollections.observableArrayList();
        for (StaffModel staffMember : staff) {
            // Check if the staff's name, email, phone, or ID contains the search text (case-insensitive)
            if (staffMember.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                    staffMember.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                    staffMember.getPhone().contains(searchText)) {
                filteredStaff.add(staffMember);
            }
        }

        populateGrid(filteredStaff); // Populate the grid with the filtered list
    }

//    // Populate the grid with staff data
    private void populateGrid() {
        populateGrid(staff); // Default to using the full staff list
    }
//
//    // Populate the grid with a given list of staff members
//    private void populateGrid(ObservableList<StaffModel> staffList) {
//        grid.getChildren().clear(); // Clear existing items
//        int column = 0;
//        int row = 1;
//
//        try {
//            for (StaffModel staffMember : staffList) {
//                FXMLLoader fxmlLoader = new FXMLLoader();
//                fxmlLoader.setLocation(getClass().getResource("/com/genomicslab/views/card.fxml"));
//                AnchorPane pane = fxmlLoader.load();
//
//                // Pass the staff data to the card controller
//                CardController controller = fxmlLoader.getController();
//                controller.setData(staffMember, this);
//
//                // Adjust grid positioning
//                if (column == 1) {
//                    column = 0;
//                    row++;
//                }
//                grid.add(pane, column++, row);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    // Populate the grid with a given list of staff members
    private void populateGrid(ObservableList<StaffModel> staffList) {
        grid.getChildren().clear(); // Clear existing items
        int column = 0;
        int row = 1;

        try {
            Timeline timeline = new Timeline();
            for (StaffModel staffMember : staffList) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/genomicslab/views/card.fxml"));
                AnchorPane pane = fxmlLoader.load();

                // Pass the staff data to the card controller
                CardController controller = fxmlLoader.getController();
                controller.setData(staffMember, this);

                // Adjust grid positioning
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




    // Set up the Add button to open the Add Staff page
    @FXML
    private void setupAddButton() {
        addBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/addStaff.fxml"));
                AnchorPane pane = loader.load();
                AddStaffController controller = loader.getController();

                // Set up the add staff stage
                Stage stage = new Stage();
                Scene scene = new Scene(pane);
                stage.setScene(scene);
                stage.setTitle("Add Staff");

                // Refresh grid when the add staff window is closed
                stage.setOnHidden(e -> loadStaff());
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}




