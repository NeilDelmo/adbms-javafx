package com.mycompany.karaoke_rental_system;

import com.mycompany.karaoke_rental_system.Equipment;
import com.mycompany.karaoke_rental_system.Model.Model;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.mycompany.karaoke_rental_system.data.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.scene.control.Alert;

public class EquipmentDialogController implements Initializable {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField rentalPriceField;

    @FXML
    private TextField overduePenaltyField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Equipment equipment;

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
        if (equipment != null) {
            nameField.setText(equipment.getName());
            descriptionField.setText(equipment.getDescription());
            rentalPriceField.setText(String.valueOf(equipment.getRentalPrice()));
            overduePenaltyField.setText(String.valueOf(equipment.getOverduePenalty()));
            statusComboBox.setValue(equipment.getStatus());
        }
    }

    @FXML
    private void handleSave() {
        // Validate required fields
        if (nameField.getText().isEmpty() || descriptionField.getText().isEmpty()) {
            showAlert("Input Error", "Name and Description are required fields.");
            return;
        }

        // Validate rental price
        double rentalPrice;
        try {
            rentalPrice = Double.parseDouble(rentalPriceField.getText());
            if (rentalPrice <= 0) {
                showAlert("Input Error", "Rental Price must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for Rental Price.");
            return;
        }

        // Validate overdue penalty
        double overduePenalty;
        try {
            overduePenalty = Double.parseDouble(overduePenaltyField.getText());
            if (overduePenalty < 0) {
                showAlert("Input Error", "Overdue Penalty cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for Overdue Penalty.");
            return;
        }

        // Validate status selection
        String status = statusComboBox.getValue();
        if (status == null || status.isEmpty()) {
            showAlert("Input Error", "Please select a status from the dropdown.");
            return;
        }

        // Validate user ID
        int currentUserId = Model.getInstance().getcurrentuserid();
        if (currentUserId <= 0) {
            showAlert("Authentication Error", "No valid user session. Please login again.");
            return;
        }

        // Proceed with database operations if all validations pass
        String query;
        if (equipment == null) {
            query = "INSERT INTO equipment (name, description, rental_price, overdue_penalty, status, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            query = "UPDATE equipment SET name = ?, description = ?, rental_price = ?, overdue_penalty = ?, status = ? WHERE equipment_id = ?";
        }

        try (Connection connection = DatabaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {

            if (equipment == null) {
                // Insert new equipment
                statement.setString(1, nameField.getText());
                statement.setString(2, descriptionField.getText());
                statement.setDouble(3, rentalPrice);
                statement.setDouble(4, overduePenalty);
                statement.setString(5, status);
                statement.setInt(6, currentUserId);
            } else {
                // Update existing equipment
                statement.setString(1, nameField.getText());
                statement.setString(2, descriptionField.getText());
                statement.setDouble(3, rentalPrice);
                statement.setDouble(4, overduePenalty);
                statement.setString(5, status);
                statement.setInt(6, equipment.getEquipmentId());
            }

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                showAlert("Database Error", "Failed to save equipment information.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "An error occurred while saving to the database: " + e.getMessage());
        }

        // Close the dialog
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        // Close the dialog
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        statusComboBox.getItems().addAll("Available", "Rented", "Maintenance");
    }

}
