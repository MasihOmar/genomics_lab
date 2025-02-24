package com.genomicslab.launcher;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Application;
import com.genomicslab.controllers.SceneManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;

import java.io.IOException;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;


public class Launcher extends Application {
//
//    private double x = 0;
//    private double y = 0;

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setStage(stage);

        // Switch to the login scene
        SceneManager.switchScene("/com/genomicslab/views/login.fxml");
//        Parent root = FXMLLoader.load(getClass().getResource("/com/genomicslab/views/login.fxml"));
//
//        Scene scene = new Scene(root);
//
//        root.setOnMousePressed((MouseEvent event) ->{
//            x = event.getSceneX();
//            y = event.getSceneY();
//        });
//
//        root.setOnMouseDragged((MouseEvent event) ->{
//            stage.setX(event.getScreenX() - x);
//            stage.setY(event.getScreenY() - y);
//
//            stage.setOpacity(.8);
//        });
//
//        root.setOnMouseReleased((MouseEvent event) ->{
//            stage.setOpacity(1);
//        });
//
//        stage.initStyle(StageStyle.TRANSPARENT);
//        scene.setFill(Color.TRANSPARENT);
//
//        stage.setScene(scene);
//        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}

//public class Launcher extends Application {
//
//    @Override
//    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(Launcher.class.getResource("/com/genomicslab/views/tests.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 650, 600);
//        stage.setTitle("Hello!");
//        scene.setFill(Color.TRANSPARENT);
//        stage.initStyle(StageStyle.TRANSPARENT);
//        stage.setScene(scene);
//        stage.show();
//    }
//
//    public static void main(String[] args) {
//        launch();
//    }
//}