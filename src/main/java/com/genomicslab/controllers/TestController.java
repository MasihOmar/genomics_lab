package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.TestsModel;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class TestController implements Initializable {

    @FXML
    private TextField search;

//    @FXML
//    private AnchorPane blurPanel;

    @FXML
    private TableView<TestsModel> tableView;

    @FXML
    private TableColumn<TestsModel, String> idCol;

    @FXML
    private TableColumn<TestsModel, String> nameCol;

    @FXML
    private TableColumn<TestsModel, String> descriptonCol;

    @FXML
    private TableColumn<TestsModel, String> priceCol;

    @FXML
    private TableColumn<TestsModel, String> editCol;

    @FXML
    private Button staffBtn;


    @FXML
    private Button patientBtn;

    @FXML
    private Button reportBtn;

    @FXML
    private Button closeBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button addTestBtn;

    private ObservableList<TestsModel> testList = FXCollections.observableArrayList();

    @FXML
    private void handleAddTestBtnClick() {
        try {
            // Load the FXML for the Add Test popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/addTests.fxml"));
            Parent root = loader.load();

            // Get the controller for the Add Test popup
            AddTestController addTestController = loader.getController();
            addTestController.setTestController(this);

            // Create a new stage for the popup
            Stage stage = new Stage();
            stage.setTitle("Add Test");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Makes it a modal dialog
            stage.showAndWait(); // Wait until the popup is closed

            // After the popup is closed, refresh the table if a new test was added
            loadTableData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        if (tableView != null) {
            loadTableData();
        } else {
            System.err.println("TableView is not properly initialized.");
        }

        // Set event for the Add Test button
        addTestBtn.setOnAction(event -> handleAddTestBtnClick());
    }

    private void configureTableColumns() {
        // Enable editing on the TableView
//        tableView.setEditable(true);
//
//        // Enable editing on individual columns
//        nameCol.setEditable(true);
//        descriptonCol.setEditable(true);
//        priceCol.setEditable(true);

        // Bind the columns to the properties of the TestsModel
        idCol.setCellValueFactory(cellData -> cellData.getValue().testIdProperty());
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        descriptonCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        priceCol.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asString());

        // Make the columns editable by using TextFieldTableCell
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptonCol.setCellFactory(TextFieldTableCell.forTableColumn());
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Listen for changes in table cells
        nameCol.setOnEditCommit(this::handleEditTestName);
        descriptonCol.setOnEditCommit(this::handleEditTestDescription);
        priceCol.setOnEditCommit(this::handleEditTestPrice);

        // Add edit/delete buttons
        Callback<TableColumn<TestsModel, String>, TableCell<TestsModel, String>> cellFactory = param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                    FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE);

                    deleteIcon.setStyle("-fx-cursor: hand; -glyph-size: 20px; -fx-fill: #ff1744;");
                    editIcon.setStyle("-fx-cursor: hand; -glyph-size: 20px; -fx-fill: #00E676;");

                    deleteIcon.setOnMouseClicked(event -> {
                        TestsModel selectedTest = getTableView().getItems().get(getIndex());

                        // Show a confirmation alert before deleting
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Confirm Deletion");
                        alert.setHeaderText("Are you sure you want to delete this test?");
                        alert.setContentText("This will permanently remove the test from the database.");

                        // If the user clicks "OK", delete the test
                        alert.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.OK) {
                                deleteTest(selectedTest);
                                loadTableData(); // Refresh the table data after deleting
                            }
                        });
                    });
                    editIcon.setOnMouseClicked(event -> {
                        TestsModel selectedTest = getTableView().getItems().get(getIndex());
                        openUpdateTestWindow(selectedTest);
                    });


                    HBox manageButtons = new HBox(editIcon, deleteIcon);
                    manageButtons.setSpacing(10);
                    setGraphic(manageButtons);
                    setText(null);
                }
            }
        };

        editCol.setCellFactory(cellFactory);
    }
    private void openUpdateTestWindow(TestsModel test) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/updateTest.fxml")); // Ensure correct FXML
            Parent root = loader.load();
            Object controller = loader.getController();
            System.out.println("Loaded controller class: " + controller.getClass().getName());

            if (controller instanceof UpdateTestController) {
                UpdateTestController updateTestController = (UpdateTestController) controller;
                updateTestController.setTestData(test); // Set the current test data
                updateTestController.setParentController(this); // Pass the parent controller

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Update Test");
                stage.show();
            } else {
                System.err.println("Controller is not an instance of UpdateTestController");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to Load Update Test Page", "An error occurred while opening the update test page.");
        }
    }


    private void deleteTest(TestsModel test) {
        Connection connection = DatabaseManager.connectDb();
        String query = "DELETE FROM tests WHERE test_id = ?"; // Replace `tests` with your table name

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, test.getTestId());
            preparedStatement.executeUpdate();
            testList.remove(test);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTestInDatabase(TestsModel test) {
        try (Connection connection = DatabaseManager.connectDb()) {
            String query = "UPDATE tests SET name = ?, description = ?, price = ? WHERE test_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, test.getName());
            preparedStatement.setString(2, test.getDescription());
            preparedStatement.setDouble(3, test.getPrice());
            preparedStatement.setString(4, test.getTestId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void loadTableData() {
        Connection connection = DatabaseManager.connectDb();
        String query = "SELECT * FROM tests"; // Replace `tests` with your table name

        try {
            testList.clear();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                testList.add(new TestsModel(
                        resultSet.getString("test_id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getDouble("price")
                ));
            }

            tableView.setItems(testList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Handle editing the test name
    private void handleEditTestName(TableColumn.CellEditEvent<TestsModel, String> event) {
        TestsModel test = event.getRowValue();
        test.setName(event.getNewValue()); // Update the name property

        // Update the database
//        updateTestInDatabase(test);
    }

    // Handle editing the test description
    private void handleEditTestDescription(TableColumn.CellEditEvent<TestsModel, String> event) {
        TestsModel test = event.getRowValue();
        test.setDescription(event.getNewValue()); // Update the description property

        // Update the database
//        updateTestInDatabase(test);
    }

    // Handle editing the test price
    private void handleEditTestPrice(TableColumn.CellEditEvent<TestsModel, String> event) {
        TestsModel test = event.getRowValue();
        try {
            double newPrice = Double.parseDouble(event.getNewValue()); // Parse the new price as double
            test.setPrice(newPrice); // Update the price property
        } catch (NumberFormatException e) {
            // Handle invalid input (non-numeric values)
            System.err.println("Invalid price input: " + event.getNewValue());
            return;
        }

    }




    // Open the edit window (for testing purposes, here we simply update the table and database directly)
    private void openEditTestWindow(TestsModel test) {
        // Directly update the test in the table and database when the edit icon is clicked
        updateTestInDatabase(test);
        loadTableData(); // Refresh the table data after updating
    }
    @FXML
    private void handleSearch() {
        String searchText = search.getText().toLowerCase();  // Get search input and convert it to lowercase

        // Filter the testList based on the search text
        ObservableList<TestsModel> filteredList = FXCollections.observableArrayList();

        // Check if the searchText is a valid number (price search)
        boolean isPriceSearch = false;
        double price = 0.0;
        try {
            price = Double.parseDouble(searchText);
            isPriceSearch = true;  // Set flag to indicate price search
        } catch (NumberFormatException e) {
            // If it's not a valid number, we just proceed with text-based search
        }

        for (TestsModel test : testList) {
            // If searching by price, compare the price
            if (isPriceSearch) {
                if (test.getPrice() == price) {
                    filteredList.add(test);  // Add matching tests to the filtered list
                }
            } else {
                // If not searching by price, check if name, description, or testId matches
                if (test.getName().toLowerCase().contains(searchText) ||
                        test.getDescription().toLowerCase().contains(searchText) ||
                        test.getTestId().toLowerCase().contains(searchText)) {
                    filteredList.add(test);  // Add matching tests to the filtered list
                }
            }
        }

        // Update the TableView with the filtered list
        tableView.setItems(filteredList);


        tableView.refresh();
    }

    // Utility method to switch scenes
    private void switchPage(String fxmlPath, Button button) {
        button.getScene().getWindow().hide();
        try {
            SceneManager.switchScene(fxmlPath);
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception appropriately
        }
    }

    // Specific page navigation methods
    @FXML
    private void patientPage() {
        switchPage("/com/genomicslab/views/patients.fxml", patientBtn);
    }

    @FXML
    private void resultPage() {
        switchPage("/com/genomicslab/views/results.fxml", reportBtn);
    }

    @FXML
    private void staffPage() {
        switchPage("/com/genomicslab/views/staff.fxml", staffBtn);
    }

    @FXML
    private void loginPage() {
        switchPage("/com/genomicslab/views/login.fxml", logoutBtn);
    }

    @FXML
    public void close() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to exit?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("Closing application...");
                System.exit(0);
            }
        });
    }


    public void refreshTable() {
        loadTableData();
    }

}


