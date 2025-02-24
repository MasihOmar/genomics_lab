package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.genomicslab.models.ResultsModel;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
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
import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import com.itextpdf.text.*;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.FileOutputStream;
import java.io.IOException;


public class ResultsController implements Initializable {

    @FXML
    private TextField search;

    @FXML
    private TableView<ResultsModel> tableView;

    @FXML
    private TableColumn<ResultsModel, String> idCol;

    @FXML
    private TableColumn<ResultsModel, String> testIdCol;

    @FXML
    private TableColumn<ResultsModel, String> patientIdCol;


    @FXML
    private TableColumn<ResultsModel, String> detailsCol;

    @FXML
    private TableColumn<ResultsModel, String> dateCol;

    @FXML
    private TableColumn<ResultsModel, String> editCol;



    private ObservableList<ResultsModel> resultsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        loadTableData();
    }

    private void configureTableColumns() {

        // Bind the columns to the properties of the ResultsModel
        idCol.setCellValueFactory(cellData -> cellData.getValue().resultIdProperty());
        testIdCol.setCellValueFactory(cellData -> cellData.getValue().testIdProperty());
        patientIdCol.setCellValueFactory(cellData -> cellData.getValue().patientIdProperty());
        detailsCol.setCellValueFactory(cellData -> cellData.getValue().resultDetailsProperty());
        dateCol.setCellValueFactory(cellData -> cellData.getValue().resultDateProperty().asString());

        // Make the columns editable using TextFieldTableCell
        testIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patientIdCol.setCellFactory(TextFieldTableCell.forTableColumn());
        detailsCol.setCellFactory(TextFieldTableCell.forTableColumn());

        // Add edit/delete/print buttons
        Callback<TableColumn<ResultsModel, String>, TableCell<ResultsModel, String>> cellFactory = param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE);
                    FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                    FontAwesomeIconView printIcon = new FontAwesomeIconView(FontAwesomeIcon.PRINT);

                    editIcon.setStyle("-fx-cursor: hand; -glyph-size: 20px; -fx-fill: #00E676;");
                    deleteIcon.setStyle("-fx-cursor: hand; -glyph-size: 20px; -fx-fill: #ff1744;");
                    printIcon.setStyle("-fx-cursor: hand; -glyph-size: 20px; -fx-fill: #0000FF;");

                    deleteIcon.setOnMouseClicked(event -> {
                        ResultsModel selectedResult = getTableView().getItems().get(getIndex());
                        showConfirmationAlert("Delete Result", "Are you sure you want to delete this result?", () -> {
                            deleteResult(selectedResult);
                            loadTableData();
                        });
                    });


                    editIcon.setOnMouseClicked(event -> {
                        ResultsModel selectedResult = getTableView().getItems().get(getIndex());// Open the update result window
                        openUpdateResultWindow(selectedResult);

                    });


                    printIcon.setOnMouseClicked(event -> {
                        ResultsModel selectedResult = getTableView().getItems().get(getIndex());
                        Stage primaryStage = (Stage) printIcon.getScene().getWindow(); // Get the current stage (window)
                        generatePdf(selectedResult, primaryStage);
                    });

                    HBox manageButtons = new HBox(editIcon, deleteIcon, printIcon);
                    manageButtons.setSpacing(10);
                    setGraphic(manageButtons);
                    setText(null);
                }
            }
        };

        editCol.setCellFactory(cellFactory);
    }

    private void showConfirmationAlert(String title, String content, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }



    @FXML
    private void handleAddResultBtnClick() {
        try {
            // Load the FXML for the Add Test popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/addResult.fxml"));
            Parent root = loader.load();

            // Get the controller for the Add Test popup
            AddResultController addResultController = loader.getController();
            addResultController.setResultController(this);

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

    private void loadTableData() {
        Connection connection = DatabaseManager.connectDb();
        String query = "SELECT * FROM test_results";

        try {
            resultsList.clear();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                resultsList.add(new ResultsModel(
                        resultSet.getString("result_id"),
                        resultSet.getString("test_id"),
                        resultSet.getString("patient_id"),
                        resultSet.getString("result_details"),
                        resultSet.getTimestamp("result_date").toLocalDateTime()
                ));
            }

            tableView.setItems(resultsList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteResult(ResultsModel result) {
        Connection connection = DatabaseManager.connectDb();
        String query = "DELETE FROM test_results WHERE result_id = ?";

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, result.getResultId());
            preparedStatement.executeUpdate();
            resultsList.remove(result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void generatePdf(ResultsModel result, Stage primaryStage) {
        Document document = new Document();
        Connection connection = DatabaseManager.connectDb();  // Use your existing DatabaseManager to connect

        try {
            // Ask user to choose where to save the PDF using JavaFX FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(primaryStage);

            if (file == null) {
                return; // User canceled the file save dialog
            }

            String filePath = file.getAbsolutePath();

            // Fetch patient information from the database
            String patientInfo = "";
            String queryPatient = "SELECT * FROM patients WHERE id = ?";
            try (PreparedStatement pst = connection.prepareStatement(queryPatient)) {
                pst.setString(1, result.getPatientId());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    patientInfo =
                            "Name: " + rs.getString("name") +
                                    "\nPhone: " + rs.getString("phone") +
                                    "\nGender: " + rs.getString("gender") +
                                    "\nDate of Birth: " + rs.getDate("dob") +
                                    "\nMedical History: " + rs.getString("medical_history");
                } else {
                    patientInfo = "No patient information found.";
                }
            }

            // Fetch test information from the database
            String testInfo = "";
            String queryTest = "SELECT * FROM tests WHERE test_id = ?";
            try (PreparedStatement pst = connection.prepareStatement(queryTest)) {
                pst.setString(1, result.getTestId());
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    testInfo =
                            "Test Name: " + rs.getString("name") +
                                    "\nDescription: " + rs.getString("description") +
                                    "\nPrice: " + rs.getBigDecimal("price");
                } else {
                    testInfo = "No test information found.";
                }
            }


            // Create a PDF document using iText
            PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Open the document for writing
            document.open();


            // Set fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font subTitleFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 8);
// Load the image from resources (use getClass().getResourceAsStream for relative path)
// Load the image from the resources
            URL imageUrl = getClass().getResource("/com/genomicslab/views/img/logo.png");
            if (imageUrl != null) {
                InputStream imageStream = imageUrl.openStream();
                Image img = Image.getInstance(imageStream.readAllBytes()); // Load image as byte array
                img.scaleToFit(100, 100); // Scale the image to fit a certain size
                img.setAlignment(Image.ALIGN_CENTER);
                document.add(img); // Add the image to the PDF
            } else {
                System.out.println("Image not found");
            }



            // Add content to the PDF
            document.add(new Paragraph("Test Result Report", titleFont));
            document.add(new Paragraph("Result ID: " + result.getResultId(), contentFont));
            document.add(new Paragraph("Date: " + result.getResultDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), contentFont));

            // Add a line separator
            LineSeparator line = new LineSeparator();
            line.setLineWidth(1f);
            line.setPercentage(90);
            line.setAlignment(Element.ALIGN_CENTER);
            line.setLineColor(BaseColor.LIGHT_GRAY);
            document.add(new Chunk(line));

            // Add patient information
            document.add(new Paragraph("Patient Details:", subTitleFont));
            document.add(new Paragraph(patientInfo, contentFont));
            document.add(new Chunk(line));


            // Add test information
            document.add(new Paragraph("Test Details:", subTitleFont));
            document.add(new Paragraph(testInfo, contentFont));
            document.add(new Chunk(line));

            // Add result remarks
            document.add(new Paragraph("Result Remarks:", subTitleFont));
            document.add(new Paragraph(result.getResultDetails(), contentFont));
            document.add(new Chunk(line));

            // Add final space and signature
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Laboratorian's Signature", contentFont));

            // Close the document
            document.close();

            // Show success message using JavaFX Alert
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("PDF Generation Successful");
            alert.setContentText("PDF generated successfully: " + filePath);
            alert.showAndWait();

        } catch (DocumentException | IOException | SQLException e) {
            e.printStackTrace();
            // Show error message using JavaFX Alert
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error Generating PDF");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    private void handleSearch() {
        String searchText = search.getText().toLowerCase();
        ObservableList<ResultsModel> filteredList = FXCollections.observableArrayList();

        for (ResultsModel result : resultsList) {
            if (result.getResultId().toLowerCase().contains(searchText) ||
                    result.getTestId().toLowerCase().contains(searchText) ||
                    result.getPatientId().toLowerCase().contains(searchText) ||
                    result.getResultDetails().toLowerCase().contains(searchText)) {
                filteredList.add(result);
            }
        }

        tableView.setItems(filteredList);
        tableView.refresh();
    }

    public void refreshTable() {
        // Reload the data from the database and update the table
        loadTableData();
    }
    private void openUpdateResultWindow(ResultsModel result) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/updateResult.fxml")); // Correct FXML path for update result page
            Parent root = loader.load();
            Object controller = loader.getController();
            System.out.println("Loaded controller class: " + controller.getClass().getName());

            if (controller instanceof UpdateResultController) {
                UpdateResultController updateResultController = (UpdateResultController) controller;
                updateResultController.setResultToUpdate(result);  // Pass the result data to the controller
                updateResultController.setParentController(this);  // Pass the parent controller to refresh after update

                // Open the new window
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Update Result");
                stage.show();
            } else {
                System.err.println("Controller is not an instance of UpdateResultController");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to Load Update Result Page", "An error occurred while opening the update result page.");
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


