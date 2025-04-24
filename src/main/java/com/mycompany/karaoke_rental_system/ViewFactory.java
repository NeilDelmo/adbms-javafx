package com.mycompany.karaoke_rental_system;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewFactory {

    private AnchorPane dashboardView;
    private AnchorPane customerView;
    private VBox reservationView;
    private AnchorPane equipmentView;
    private CustomerController customerController;

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

    public AnchorPane getCustomerView() {
        if (customerView == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Customer.fxml"));
                customerView = loader.load();
                customerController = loader.getController(); // ✅ Save the controller
                customerController.refreshData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            customerController.refreshData(); // ✅ Reuse the saved controller
        }

        return customerView;
    }
    public VBox getReservationView() {
        if (reservationView == null) {
            try {
                reservationView = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Reservation.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return reservationView;
    }

    public AnchorPane getEquipmentView() {
        if (equipmentView == null) {
            try {
                equipmentView = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Equipment.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return equipmentView;
    }

    public void showLoginWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/login.fxml"));
        createStage(loader);

    }

    public void showStaffWindow() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Staff.fxml"));
        createStage(loader);
    }

    private void createStage(FXMLLoader loader) {
        Scene scene = null;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            e.printStackTrace();

        }
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Karaoke Rental");
        stage.show();
    }

    public void clearViews() {
        dashboardView = null;
        customerView = null;
        reservationView = null;
        equipmentView = null;
    }
}
