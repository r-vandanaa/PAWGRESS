package com.digitalpet;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class for Digital Pet Evolution
 */
public class DigitalPetApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Digital Pet Evolution");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}