package com.genomicslab.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;

public class MainTemplateController {
    @FXML private Button closeBtn;
    @FXML private AnchorPane contentPane;
    @FXML private Button logoutBtn;
    @FXML private Button patientBtn;
    @FXML private Button reportBtn;
    @FXML private AnchorPane sidebar;
    @FXML private Button staffBtn;
    @FXML private Button testBtn;

    private Button currentActiveButton;
    private static final String ACTIVE_BUTTON_STYLE = "-fx-background-color: #79cee8; -fx-background-radius: 30;";
    private static final String INACTIVE_BUTTON_STYLE = "-fx-background-color: transparent;";
    private static final String ACTIVE_ICON_COLOR = "WHITE";
    private static final String INACTIVE_ICON_COLOR = "#b8b8b8";

    @FXML
    public void initialize() {
        setActiveButton(patientBtn);
        loadPage("patients.fxml");
    }

    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.setStyle(INACTIVE_BUTTON_STYLE);
            FontAwesomeIconView icon = (FontAwesomeIconView) currentActiveButton.getGraphic();
            icon.setFill(javafx.scene.paint.Paint.valueOf(INACTIVE_ICON_COLOR));
        }

        button.setStyle(ACTIVE_BUTTON_STYLE);
        FontAwesomeIconView icon = (FontAwesomeIconView) button.getGraphic();
        icon.setFill(javafx.scene.paint.Paint.valueOf(ACTIVE_ICON_COLOR));
        currentActiveButton = button;
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/genomicslab/views/" + fxmlFile));
            AnchorPane newPage = loader.load();

            // Ensure the loaded page fits inside contentPane
            contentPane.getChildren().clear();
            contentPane.getChildren().add(newPage);
            AnchorPane.setTopAnchor(newPage, 0.0);
            AnchorPane.setBottomAnchor(newPage, 0.0);
            AnchorPane.setLeftAnchor(newPage, 0.0);
            AnchorPane.setRightAnchor(newPage, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void testPage(ActionEvent event) {
        setActiveButton(testBtn);
        loadPage("tests.fxml");
    }

    @FXML
    void staffPage(ActionEvent event) {
        setActiveButton(staffBtn);
        loadPage("staff.fxml");
    }

    @FXML
    void resultPage(ActionEvent event) {
        setActiveButton(reportBtn);
        loadPage("results.fxml");
    }

    @FXML
    void loginPage(ActionEvent event) {
        try {
            SceneManager.switchScene("/com/genomicslab/views/login.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    void patientsPage(ActionEvent event) {
        setActiveButton(patientBtn);
        loadPage("patients.fxml");
    }

    @FXML
    void close(ActionEvent event) {
        System.exit(0);
    }
}