package com.mycompany.karaoke_rental_system;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ViewFactory {

    private AnchorPane dashboardView;

    public ViewFactory() {
    }

    public AnchorPane getDashboardView() {
        if (dashboardView == null) {
            try {
                dashboardView = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Dashboard.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dashboardView;
    }

    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/login.fxml"));
        createStage(loader);
       

    }
    public void showStaffWindow(){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Staff.fxml"));
        StaffController staffController = new StaffController();
        loader.setController(staffController);
        createStage(loader);
    }
    
    private void createStage(FXMLLoader loader){
        Scene scene = null;
        try{
            scene = new Scene(loader.load());
        }catch(IOException e){
            e.printStackTrace();

        }
         Stage stage = new Stage();
         stage.setScene(scene);
         stage.setTitle("Karaoke Rental");
         stage.show();
    }
}
