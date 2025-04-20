package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage){
       Model.getInstance().getViewFactory().showLoginWindow();
      
    }

    public static void main(String[] args) {
        launch();
    }
}