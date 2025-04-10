/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class StaffMenuController implements Initializable {

    @FXML
    private Button add_user_btn;

    @FXML
    private Button customer_btn;

    @FXML
    private Button equipment_btn;

    @FXML
    private Button home_btn;

    @FXML
    private Button logout_btn;

    @FXML
    private Button payment_btn;

    @FXML
    private Button reservation_btn;
    
    @FXML
    private Button actlog_btn;

    public Button getcustomer_btn() {
        return customer_btn;
    }

    public Button getequipment_btn() {
        return equipment_btn;
    }

    public Button gethome_btn() {
        return home_btn;
    }

    public Button getlogout_btn() {
        return logout_btn;
    }

    public Button getpayment_btn() {
        return payment_btn;
    }

    public Button getreservation_btn() {
        return reservation_btn;
    }

    public Button getadduser_btn() {
        return add_user_btn;
    }
    public Button getactlog_btn(){
        return actlog_btn;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
         boolean isAdmin = Model.getInstance().getRole().equalsIgnoreCase("Admin");        
        // Hide buttons completely
        add_user_btn.setVisible(isAdmin);
        actlog_btn.setVisible(isAdmin);
    }

}
