package com.genomicslab.controllers;

import animatefx.animation.FadeIn;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class SceneManager {
    private static Stage stage;
    private static double xOffset = 0;
    private static double yOffset = 0;

    // Set the main application stage
    public static void setStage(Stage mainStage) {
        stage = mainStage;
        stage.initStyle(StageStyle.TRANSPARENT); // Apply transparent style globally

    }

    // Switch scenes with draggable functionality
    public static void switchScene(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));
        makeWindowDraggable(root); // Add draggable behavior

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();

        new FadeIn(root).play();
    }

    // Add draggable functionality to the root node
    private static void makeWindowDraggable(Parent root) {
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
            stage.setOpacity(.8);
        });

        root.setOnMouseReleased(event -> {
            stage.setOpacity(1);

        });

    }
}

