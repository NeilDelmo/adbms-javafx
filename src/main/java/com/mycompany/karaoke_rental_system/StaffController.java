/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class StaffController implements Initializable {

    @FXML
    private BorderPane rootPane;

    @FXML
    private StaffMenuController staffMenuController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        rootPane.setCenter(Model.getInstance().getViewFactory().getDashboardView());

        if (staffMenuController != null) {
            staffMenuController.gethome_btn().setOnAction(e
                    -> rootPane.setCenter(Model.getInstance().getViewFactory().getDashboardView())
            );
            staffMenuController.getcustomer_btn().setOnAction(e
                    -> rootPane.setCenter(Model.getInstance().getViewFactory().getCustomerView())
            );
            staffMenuController.getreservation_btn().setOnAction(e
                    -> rootPane.setCenter(Model.getInstance().getViewFactory().getReservationView())
            );
            staffMenuController.getequipment_btn().setOnAction(e
                    -> rootPane.setCenter(Model.getInstance().getViewFactory().getEquipmentView())
            );
            staffMenuController.getpayment_btn().setOnAction(e
                    -> rootPane.setCenter(Model.getInstance().getViewFactory().getPaymentView())
            );
            staffMenuController.getadduser_btn().setOnAction(e
                            -> rootPane.setCenter(Model.getInstance().getViewFactory().getManagementView())
                    );

            staffMenuController.getlogout_btn().setOnAction(e -> handleLogout());
        } else {
            System.err.println("StaffMenuController not injected!");
        }
    }

    private void handleLogout() {
        Stage currentStage = (Stage) rootPane.getScene().getWindow();
        currentStage.close();
        Model.getInstance().getViewFactory().showLoginWindow();
        Model.getInstance().clearUserData();
    }

}
