package com.genomicslab.controllers;

import com.genomicslab.backend.DatabaseManager;
import com.jfoenix.controls.JFXRadioButton;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginController implements Initializable {

    @FXML
    private PasswordField password;

    @FXML
    private JFXRadioButton rememberMeBtn;

    @FXML
    private Button closeBtn;

    @FXML
    private Button loginBtn;




    @FXML
    private TextField usernameField;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private static final String CREDENTIALS_FILE = "user_credentials.properties";

    public void loginAdmin() {
        String sql = "SELECT * FROM labs WHERE name = ? and password = ?";
        connect = DatabaseManager.connectDb();

        try {
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, usernameField.getText());
            prepare.setString(2, password.getText());

            result = prepare.executeQuery();
            Alert alert;

            if (usernameField.getText().isEmpty() || password.getText().isEmpty()) {
                alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all blank fields");
                alert.showAndWait();
            } else {
                if (result.next()) {
                    // Save credentials if "Remember Me" is checked
                    if (rememberMeBtn.isSelected()) {
                        saveCredentials(usernameField.getText(), password.getText(), true);
                    } else {
                        saveCredentials("", "", false); // Clear credentials if not selected
                    }

                    alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Login");
                    alert.showAndWait();

                    // Load dashboard scene
                    loginBtn.getScene().getWindow().hide();
                    try {
                        SceneManager.switchScene("/com/genomicslab/views/mainTemplate.fxml");
                    } catch (IOException e) {
                        e.printStackTrace(); // Handle exception appropriately
                    }
                } else {
                    alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Wrong Username/Password");
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCredentials(String username, String password, boolean rememberMe) {
        try (OutputStream output = new FileOutputStream(CREDENTIALS_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("username", username);
            prop.setProperty("password", password);
            prop.setProperty("rememberMe", Boolean.toString(rememberMe));
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void loadCredentials() {
        try (InputStream input = new FileInputStream(CREDENTIALS_FILE)) {
            Properties prop = new Properties();
            prop.load(input);
            usernameField.setText(prop.getProperty("username", ""));
            password.setText(prop.getProperty("password", ""));
            rememberMeBtn.setSelected(Boolean.parseBoolean(prop.getProperty("rememberMe", "false")));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void close() {
        System.out.println("Close button clicked!");
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Load saved credentials if they exist
        loadCredentials();
    }
}

