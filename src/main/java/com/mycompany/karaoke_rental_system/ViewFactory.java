package com.mycompany.karaoke_rental_system;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewFactory {

    private AnchorPane dashboardView;
    private VBox customerView;
    private VBox reservationView;
    private AnchorPane equipmentView;
    private CustomerController customerController;
    private ReservationController reservationController;
    private Customer selectedCustomer;
    private VBox paymentView;
    private BorderPane managementView;

    public void setSelectedCustomer(Customer selectedCustomer){
        this.selectedCustomer = selectedCustomer;
    }
    private Customer getSelectedCustomer(){
        return selectedCustomer;
    }

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

    public VBox getCustomerView() {
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Reservation.fxml"));
                reservationView = loader.load();
                reservationController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (selectedCustomer != null && reservationController != null) {
            reservationController.setSelectedCustomer(selectedCustomer);
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

    public VBox getPaymentView(){
        if(paymentView == null){
            try{
                paymentView = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/Payment.fxml")).load();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return  paymentView;
    }

    public BorderPane getManagementView() {
        if (managementView == null) {
            try {
                managementView = new FXMLLoader(getClass().getResource("/com/mycompany/karaoke_rental_system/User.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return managementView;
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
