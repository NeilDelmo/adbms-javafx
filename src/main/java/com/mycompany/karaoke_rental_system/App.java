package com.mycompany.karaoke_rental_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.stage.StageStyle;

/**
 * JavaFX App
 */
public class App extends Application {
    private double x = 0;
    private double y = 0;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login"));
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        try {
            // Use consistent path structure with your MainController
            String resourcePath = "/com/mycompany/karaoke_rental_system/" + fxml + ".fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(resourcePath));
            return fxmlLoader.load();
        } catch (IOException e) {
            System.err.println("Failed to load: " + fxml + ".fxml");
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}