/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

/**
 * FXML Controller class
 *
 * @author Neil
 */
public class LoginController implements Initializable {

    @FXML
    private Button login_button;
    @FXML
    private AnchorPane main_form;

    @FXML
    private PasswordField password;

    @FXML
    private TextField username;

    @FXML
    private void close() {
        System.exit(0);
    }

    private void login() {
        String usernameText = username.getText().trim();
        char[] passwordChars = password.getText().toCharArray();

        if (usernameText.isEmpty() || passwordChars.length == 0) {
            showAlert(Alert.AlertType.ERROR, "Empty Fields", "Please fill in all fields");
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {

            String query = "SELECT user_id,password, role FROM users WHERE username = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, usernameText);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int userID = rs.getInt("user_id");
                // Verify BCrypt hashed password
                String storedHash = rs.getString("password");
                String userRole = rs.getString("role");
                if (BCrypt.checkpw(new String(passwordChars), storedHash)) {
                    Model.getInstance().setcurrentuserid(userID);
                    Model.getInstance().setRole(userRole);
                    handleSuccessfulLogin();

                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect password");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Username not found");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        } finally {
            // Securely clear password data from memory
            Arrays.fill(passwordChars, '\0');
            password.clear();
        }
    }

    private void handleSuccessfulLogin() {
        try {
            // Use the singleton ViewFactory to show the staff window
            Model.getInstance().getViewFactory().showStaffWindow();

            // Close the login window
            Stage currentStage = (Stage) login_button.getScene().getWindow();
            currentStage.close();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "UI Error", "Failed to load staff window: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        login_button.setOnAction(event -> login());
        
    }

}
